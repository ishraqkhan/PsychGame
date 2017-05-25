import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

/**
 * MessageHandler.java is a class that handles all the messages being sent to the server from the client. It is also
 * responsible for returning error messages to the client if the input is invalid or another type of error occurs.
 */
public class MessageHandler implements Runnable {
    //connection references
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private static final Object registerLock = new Object();
    private static final Object loginLock = new Object();

    // user reference
    private User currentUser;

    // game which the user either starts or joins
    // set to null when the user leaves the game
    private Game g;

    public MessageHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());

            String message;
            String outMessage;

            while (true) {
                message = in.readLine();
                if(isValidFormat(message)) { // make sure the message to the server isn't null, empty, and is a valid action
                    if (message.startsWith("CREATENEWUSER")) {
                        out.println(registerNewUser(message.split("--")));
                        out.flush();
                    }
                    if (message.startsWith("LOGIN")) {
                        out.println(logUserIn(message.split("--")));
                        out.flush();
                    }
                    if(message.startsWith("STARTNEWGAME")){
                        outMessage = startNewGame(message.split("--"));
                        out.println(outMessage);
                        out.flush();
                    }
                    if(message.startsWith("JOINGAME")){
                        String[] messageArray = message.split("--");
                        outMessage = joinGame(messageArray);
                        out.println(outMessage);
                        out.flush();
                        String newParticipantMessage = String.format("NEWPARTICIPANT--%s--%d", currentUser.getUsername(), currentUser.getScore());
                        g.getLeader().getOut().println(newParticipantMessage);
                        g.getLeader().getOut().flush();

                    }
                    if(message.startsWith("ALLPARTICIPANTSHAVEJOINED")){
                        outMessage = beginGame(message.split("--"));
                        if(outMessage != null){
                            out.println(outMessage);
                            out.flush();
                        } else {
                            new Thread(g).start();
                        }
                    }
                    if(message.startsWith("PLAYERSUGGESTION")){
                        outMessage = getSuggestions(message.split("--"));
                        if(!outMessage.equals("SUCCESS")){
                            out.println(outMessage);
                        }
                    }
                    if(message.startsWith("PLAYERCHOICE")){
                        String[] input = message.split("--");
                        outMessage = playerChoice(input);
                        if(!outMessage.equals("SUCCESS")){
                            out.println(outMessage);
                        }
                    }
                    if(message.startsWith("LOGOUT")){
                        outMessage = logout();
                        out.println(outMessage);
                        out.flush();
                        in.close();
                        out.close();
                        return;
                    }
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Method to check the validity of the string format being set to the server
     * @param s string messgae being set to the server
     * @return true if formatted correctly
     */
    public boolean isValidFormat(String s){
        String[] validActions = {"CREATENEWUSER", "LOGIN", "STARTNEWGAME", "JOINGAME", "ALLPARTICIPANTSHAVEJOINED", "PLAYERSUGGESTION", "PLAYERCHOICE", "LOGOUT"};

        /*
        if (s == null || s.isEmpty()){
            System.out.printf("Null or empty string sent to server!\n");
            return false;
        }
        */

        String[] messageArray = s.split("--");
        for (int i = 0; i < validActions.length; i++){
            if(validActions[i].equals(messageArray[0])){
                return true;
            }
        }
        return false;
    }


    /**
     * Method to validate and register the user's log in in the userdatabase file
     *
     * @param userInfo String array of the message sent to the server.
     * @return String response to be sent to the client - either an error message or a "SUCCESS" message
     * @throws IOException if read and write streams to userdatabase.txt file cannot be opened
     */
    public String registerNewUser(String[] userInfo) throws IOException{
        synchronized (registerLock) {
            PrintWriter toFile = new PrintWriter(new FileOutputStream("/home/alexander/Desktop/CS180/Projects/Project4/UserDatabase", true));
            toFile.flush();
            BufferedReader fromFile = new BufferedReader(new FileReader("/home/alexander/Desktop/CS180/Projects/Project4/UserDatabase"));

            String response = "RESPONSE--CREATENEWUSER--";
            String username;
            String password;
            String validUserChars = "abcdefghijklmnopqrstuvwxyz0123456789_";
            String validPasswordChars = "abcdefghijklmnopqrstuvwxyz0123456789#&$*";
            boolean hasCapital = false;
            boolean hasDigit = false;

            // valid format
            if (userInfo.length != 3) {
                return response + "INVALIDMESSAGEFORMAT";
            }

            // correct  username length
            if (userInfo[1].isEmpty() || userInfo[1].length() >= 10) {
                return response + "INVALIDUSERNAME";
            }

            // correct user characters
            for (int i = 0; i < userInfo[1].length(); i++) {
                String lookFor = "" + userInfo[1].charAt(i);
                if (!validUserChars.contains(lookFor.toLowerCase())) {
                    return response + "INVALIDUSERNAME";
                }
            }

            // correct password length
            if (userInfo[2].isEmpty() || userInfo[2].length() >= 10) {
                return response + "INVALIDUSERPASSWORD";
            }

            // correct password characters
            for (int i = 0; i < userInfo[2].length(); i++) {
                String lookFor = String.valueOf(userInfo[2].charAt(i));
                if (!validPasswordChars.contains(lookFor.toLowerCase())) {
                    return response + "INVALIDUSERPASSWORD";
                }
                if (Character.isDigit(userInfo[2].charAt(i))) {
                    hasDigit = true;
                }
                if (Character.isUpperCase(userInfo[2].charAt(i))) {
                    hasCapital = true;
                }
            }

            // password contains 1 digit and 1 capital letter
            if (!hasDigit || !hasCapital) {
                return response + "INVALIDUSERPASSWORD";
            }

            username = userInfo[1];
            password = userInfo[2];

            // check if the user is already in the user database
            String line;
            while ((line = fromFile.readLine()) != null) {
                String[] lineArray = line.split(":");
                if (lineArray[0].equals(username)) {
                    return response + "USERALREADYEXISTS";
                }
            }

            // put new user into the user database and close the streams
            toFile.printf("%s:%s:0:0:0\n", username, password);
            toFile.flush();
            toFile.close();
            fromFile.close();

            return response + "SUCCESS";
        }
    }

    /**
     * Method to validate the log in message and decide if to log user into the game
     *
     * @param userInfo String array of the message sent to the server
     * @return String response to be sent to the client - either an error message or a "SUCCESS" message
     * @throws IOException if read and write streams to the userdatabase.txt file cannot open
     */
    public String logUserIn(String[] userInfo) throws IOException{
        synchronized (loginLock) {
            BufferedReader fromFile = new BufferedReader(new FileReader("UserDatabase"));
            String response = "RESPONSE--LOGIN--";
            String line;

            // valid format
            if (userInfo.length != 3) {
                return response + "INVALIDMESSAGEFORMAT";
            }


            // check if user object with username is already in serever's activeplayers
            for(User user : Server.getActivePlayers().values()){
                if(user.getUsername().equals(userInfo[1])){
                    return response + "USERALREADYLOGGEDIN";
                }
            }

            // read the user database file and create the user if username and password match
            // else return the correct error
            while ((line = fromFile.readLine()) != null) {
                String[] lineArray = line.split(":");
                if (lineArray[0].equals(userInfo[1])) {
                    if (lineArray[1].equals(userInfo[2])) {
                        if(Integer.parseInt(lineArray[2]) == 0) {
                            currentUser = new User(lineArray[0], lineArray[1], generateSessionCookie(), out, in);
                        } else {
                            currentUser = new User(lineArray[0], lineArray[1], generateSessionCookie(), out, in, Integer.parseInt(lineArray[2]),Integer.parseInt(lineArray[3]), Integer.parseInt(lineArray[4]));
                        }
                    } else {
                        return response + "INVALIDUSERPASSWORD";
                    }
                }
            }

            // if the username didn't match any username form the file return unknown user message
            if (currentUser == null) {
                return response + "UNKNOWNUSER";
            }

            Server.getActivePlayers().put(currentUser.getUserToken(), currentUser);
            fromFile.close();

            return response + "SUCCESS--" + currentUser.getUserToken();
        }
    }

    /**
     * Method to generate a random 10 character session cookie for the user
     * @return String session cookie
     */
    private String generateSessionCookie(){
        Random r = new Random();
        String cookie = "";
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for(int i = 0; i < 10; i++){
            cookie += letters.charAt(r.nextInt(letters.length()));
        }
        return cookie;
    }

    /**
     * Method to validate the message sent to server and try to start the game
     * @param message String array of the message sent to the server
     * @return String response to be sent to the client - either an error message or a "SUCCESS" message
     */
    public String startNewGame(String[] message){
        String response = "RESPONSE--STARTNEWGAME--";
        if(!Server.getActivePlayers().containsKey(message[1])){
            return response + "USERNOTLOGGEDIN";
        }

        if(Server.getPlayersToGames().containsKey(Server.getActivePlayers().get(message[1]))){
            return response + "FAILURE - Player already in a game";
        }
        String gameToken = generateGameToken();

        if(Server.getActiveGames().containsKey(gameToken)) {
            return response + "FAILURE - game token already in use - what are the chances?";
        }

        this.g = new Game(currentUser, gameToken);
        // add 3 char key and game to active games
        Server.getActiveGames().put(gameToken, g);

        // add user to server's playersToGamers map
        Server.getPlayersToGames().put(currentUser, g);

        return response + "SUCCESS--" + gameToken;
    }

    /**
     * Method to generate a unique 3 character game token for each game started
     * @return String 3 character game token
     */
    private String generateGameToken(){
        Random r = new Random();
        String token = "";
        String letters = "abcdefghijklmnopqrstuvwxyz";
        for(int i = 0; i < 3; i++){
            token += letters.charAt(r.nextInt(letters.length()));
        }
        return token;
    }

    /**
     * Method to validate the join game message to the server and decide if the user can just the requested game
     * @param message String array of the message sent to the server
     * @return String response to be sent to the client - either an error message or a "SUCCESS" message
     */
    public String joinGame(String[] message){
        String response = "RESPONSE--JOINGAME--";

        // check if user object with username is already in serever's activeplayers
        if (!Server.getActivePlayers().containsKey(message[1])){
            return response + "USERNOTLOGGEDIN";
        }

        if (!Server.getActiveGames().containsKey(message[2])){
            return response + "GAMEKEYNOTFOUND";
        }

        if(Server.getPlayersToGames().containsKey(Server.getActivePlayers().get(message[1]))){
            return response + "FAILURE - Player already in a game";
        }

        this.g = Server.getActiveGames().get(message[2]);

        // add user to server's playersToGamers map
        Server.getPlayersToGames().put(currentUser, g);

        // add player to game
        g.addPlayer(currentUser);

        // response for the user joining
        return response + "SUCCESS--" + message[2];

    }

    /**
     * Method that properly starts the game if the start game message is sent from the leader of the game. Game must have
     * at least 2 players (including the leader).
     * @param message String array of the message sent to the server
     * @return String response to be sent to the client - either an error message or a "SUCCESS" message
     */
    public String beginGame(String[] message){
        String response = "RESPONSE--ALLPARTICIPANTSHAVEJOINED--";

        // check if user object with username is already in serever's activeplayers
        if (!Server.getActivePlayers().containsKey(message[1])){
            return response + "USERNOTLOGGEDIN";
        }

        // check if game token matches a game that is in the active games list
        if (!Server.getActiveGames().containsKey(message[2])){
            return response + "INVALIDGAMETOKEN";
        }

        // check if launch game command is coming from the game's leader
        // handout has a WRONG DESCRIPTION OF THIS ERROR MESSAGE
        // Says this message is sent when the current user is already in a game
        if(!g.getLeader().getUserToken().equals(message[1])){
            return response + "USERNOTGAMELEADER";
        }

        return null;
    }

    /**
     * Method to collect the player's suggestion and add it to the game's suggestion list.
     * @param message message sent to the server from the client
     * @return String response - either a success message or an error to send to the client.
     */
    public String getSuggestions(String[] message){
        String response = "RESPONSE--PLAYERSUGGESTION--";

        // check if message is the right type
        if(!message[0].equals("PLAYERSUGGESTION")){
            return response + "UNEXPECTEDMESSAGETYPE";
        }

        // check if user is logged in and is a player in this game g
        if(!Server.getActivePlayers().containsKey(message[1]) && g.getPlayers().contains(Server.getActivePlayers().get(message[1]))){
            return response + "USERNOTLOGGEDIN";
        }

        // check if game token matches an active game and that the game token is the correct token for this game
        if(!Server.getActiveGames().containsKey(message[2]) && g.getGameToken().equals(message[2])){
            return response + "INVALIDGAMETOKEN";
        }

        g.addSuggestion(Server.getActivePlayers().get(message[1]), message[3]);
        g.decrementSuggestionLatch();

        return "SUCCESS";
    }

    /**
     * Method to collect the player's answer choice and add it to the game's answer list
     * @param message message sent to the server from the client
     * @return String reponse - either a success message or an error to send ot the client.
     */
    public String playerChoice(String[] message){
        String response = "RESPONSE--PLAYERCHOICE--";

        // check if message is the right type
        if(!message[0].equals("PLAYERCHOICE")){
            System.out.println(message[0]);
            return response + "UNEXPECTEDMESSAGETYPE";
        }

        // check if user is logged in and is a player in this game g
        if(!Server.getActivePlayers().containsKey(message[1]) && g.getPlayers().contains(Server.getActivePlayers().get(message[1]))){
            return response + "USERNOTLOGGEDIN";
        }

        // check if game token matches an active game and that the game token is the correct token for this game
        if(!Server.getActiveGames().containsKey(message[2]) && g.getGameToken().equals(message[2])){
            return response + "INVALIDGAMETOKEN";
        }

        g.addAnswer(currentUser, message[3]);
        g.decrementAnswerLatch();

        return "SUCCESS";
    }

    /**
     * Method to log the user out and remove all references to the user from the game and server.
     * @return String response - either a success message or an error to send to the client.
     * @throws IOException if read and write streams to the userdatabase.txt file cannot open
     */
    public String logout() throws IOException{
        if(!Server.getActivePlayers().containsKey(currentUser.getUserToken())){
            return "RESPONSE--LOGOUT--USERNOTLOGGEDIN";
        }

        /*
       TODO: basically need to copy, change, and paste the database file
       1) read all lines into an array list
       2) find line with the user's correct username
       3) replace this line with the current user's stats (aka to string method)
       4) overwrite old database file with the new contents.
         */

        File dbFile = new File("UserDatabase");
        BufferedReader readDatabaseFile = new BufferedReader(new FileReader(dbFile));
        ArrayList<String> fileContents = new ArrayList<String>();

        String line;
        while((line = readDatabaseFile.readLine()) != null){
            if(!line.startsWith(currentUser.getUsername())) {
                fileContents.add(line);
            } else {
                fileContents.add(currentUser.toString());
            }
        }

        readDatabaseFile.close();

        PrintWriter toFile = new PrintWriter(new FileOutputStream(dbFile));
        for (String listItem : fileContents){
            toFile.println(listItem);
            toFile.flush();
        }

        toFile.close();

        // remove current player references in all places
        Server.getActivePlayers().remove(currentUser.getUserToken());
        Server.getPlayersToGames().remove(currentUser);
        g.removePlayer(currentUser);
        currentUser = null;
        return "RESPONSE--LOGOUT--SUCCESS";
    }
}
