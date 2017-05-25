import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Controller for the game. All server communication done here.
 */
public class FoilMakerClient extends Thread{

    private FoilMakerModel model; // the model to hold the current game state
    private FoilMakerView view; // the view class holds all the game UIs
    private Socket socket = null; // reference to the client-server socket
    private BufferedReader in = null; // bufferedreader to read messages from the server
    private PrintWriter out = null; // printwriter to write messages to the server
    private String inString = null; // String to hold the message received from the server

    public FoilMakerClient(){
        this.model = new FoilMakerModel(this, view);
        this.view = new FoilMakerView(this, model);
    }

    public void connect() throws IOException{
        String serverIP = "localhost";
        int serverPort = 50000;
        this.socket = new Socket(serverIP, serverPort);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void disconnect() throws IOException{
        if(this.socket != null){
            socket.close();
        }
        if(this.in != null){
            in.close();
        }
        if(this.out != null){
            out.close();
        }
    }

    public void run(){
        view.logInUI();
    }

    public void sendMessage(String outString){
        if (outString != null && !outString.equals("")) {
            out.println(outString);
        }
    }

    public String getInString(){
        return inString;
    }

    public void messageHandler(){
        try{
            while(true){
                inString = in.readLine();
                System.out.printf("%s\n", inString);
                String[] messageArray = inString.split(FoilMakerNetworkProtocol.SEPARATOR);
                if(messageArray[0].equals("RESPONSE")) {
                    switch (messageArray[1]) {
                        case "CREATENEWUSER":
                            createNewUser(messageArray);
                            break;
                        case "LOGIN":
                            login(messageArray);
                            break;
                        case "STARTNEWGAME":
                            startAGame(messageArray);
                            break;
                        case "JOINGAME":
                            joinGame(messageArray);
                            break;
                        case "ALLPARTICIPANTSHAVEJOINED":
                            allParticipantsHaveJoinedError(messageArray);
                            break;
                        case "PLAYERSUGGESTION":
                            playerSuggestionError(messageArray);
                            break;
                        case "PLAYERCHOICE":playerChoiceError(messageArray);
                            break;
                        case "LOGOUT":
                            if(messageArray[2].equals("SUCCESS")){
                                disconnect();
                                return;
                            }
                            logout(messageArray);
                            break;

                    }
                }
                if(messageArray[0].equals("NEWPARTICIPANT")){
                    model.addParticipant(messageArray[1]);
                    view.getTextarea1().setText(model.stringParticipants());
                }
                if(messageArray[0].equals("NEWGAMEWORD") && model.getNumQuestions() == 1){
                     view.getSuggestionUI();
                     view.getTextarea1().setText(messageArray[1]);
                     model.setNumQuestions(1);
                }

                if(messageArray[0].equals("ROUNDOPTIONS")){
                    view.sendPlayerSuggestionUI(messageArray);
                }
                if(messageArray[0].equals("ROUNDRESULT")){
                    roundResult(messageArray);
                }
                if(messageArray[0].equals("GAMEOVER")){
                    view.roundResults();
                    view.getTextarea2().setText(model.getCurrentState());
                    view.getNextRound().setEnabled(false);
                    view.getFooterLabel().setText("Game Over!");
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void createNewUser(String[] fromServer){
        String labelText;
        switch (fromServer[2]) {
            case "SUCCESS":
                labelText = "User created - Please log in";
                break;
            case "INVALIDMESSAGEFORMAT":
                labelText = "Message to server not formatted properly";
                break;
            case "INVALIDUSERNAME":
                labelText = "Username field must be filled out";
                break;
            case "INVALIDUSERPASSWORD":
                labelText = "Password field must be filled out";
                break;
            case "USERALREADYLEXISTS":
                labelText = "User already in user store";
                break;
            default:
                labelText = "something went wrong";
        }
        view.getFooterLabel().setText(labelText);
        view.getFooterLabel().setForeground(Color.RED);
    }

    public void login(String[] fromServer){
        String labelText = null;
        switch(fromServer[2]){
            case "SUCCESS":
                model.setUsername(view.getUsername());
                model.setUserToken(fromServer[3]);
                view.getHeaderLabel().setText(model.getUsername());
                view.gameLobby();
                break;
            case "INVALIDMESSAGEFORMAT":
                labelText = "Message to server not formatted correctly";
                break;
            case "UNKNOWNUSER":
                labelText = "Invalid username";
                break;
            case "INVALIDUSERPASSWORD":
                labelText = "Password does not match username";
                break;
            case "USERALREADYLOGGEDIN":
                labelText = "User already logged in";
                break;
            default:
                labelText = "something went wrong";
        }
        if (labelText != null){
            view.getFooterLabel().setText(labelText);
            view.getFooterLabel().setForeground(Color.RED);
        }

    }

    public void startAGame(String[] fromServer) {
        String labelText;
        switch(fromServer[2]){
            case "SUCCESS":
                model.setGameToken(fromServer[3]);
                model.addParticipant(model.getUsername());
                labelText = "Game started: You are the leader";
                view.startGameUI();
                view.getTextarea1().setText(model.stringParticipants());
                break;
            case "USERNOTLOGGEDIN":
                labelText = "The user is not logged in";
                break;
            case "FAILURE":
                labelText = "Failure message";
                break;
            default:
                labelText ="default error message";
        }
        view.getFooterLabel().setText(labelText);
        if(fromServer[2].equals("SUCCESS")){
            view.getFooterLabel().setForeground(Color.black);
        }
        else {
            view.getFooterLabel().setForeground(Color.RED);
        }
    }

    public void joinGame(String[] fromServer){
        String labelText = "";
        switch (fromServer[2]){
            case "SUCCESS":
                labelText = "Game joined! Waiting on the leader";
                model.setGameToken(fromServer[3]);
                view.joinedUI();
                break;
            case "GAMEKEYNOTFOUND":
                labelText = "Invalid game key";
                break;
            case "USERNOTLOGGEDIN":
                labelText = "The user is not logged in";
                break;
            case "FAILURE":
                labelText = "Failure message";
                break;
            default:
                labelText = "something went wrong";
        }
        view.getFooterLabel().setText(labelText);
    }

    public void allParticipantsHaveJoinedError(String[] fromServer){
        String labelText;
        switch (fromServer[2]){
            case "INVALIDGAMETOKEN":
                labelText = "Invalid game token";
                break;
            case "USERNOTLOGGEDIN":
                labelText = "User name token";
                break;
            case "USERNOTGAMELEADER":
                labelText = "User already playing the game";
                break;
            default:
                labelText = "something went wrong";
        }
        view.getFooterLabel().setText(labelText);
        view.getFooterLabel().setForeground(Color.RED);
    }

    public void playerSuggestionError(String[] fromServer){
        String labelText = "";
        switch (fromServer[2]){
            case "INVALIDGAMETOKEN":
                labelText = "Invalid game token";
                break;
            case "USERNOTLOGGEDIN":
                labelText = "User name token";
                break;
            case "UNEXPECTEDMESSAGETYPE":
                labelText = "Message to the server was different than what was expected by the server";
                break;
            case "INVALIDMESSAGEFORMAT":
                labelText = "Message format not correct";
                break;
        }
        view.getFooterLabel().setText(labelText);
        view.getFooterLabel().setForeground(Color.RED);
    }

    public void playerChoiceError(String[] fromServer){
        String labelText = "";
        switch (fromServer[2]){
            case "INVALIDGAMETOKEN":
                labelText = "Invalid game token";
                break;
            case "USERNOTLOGGEDIN":
                labelText = "User name token";
                break;
            case "UNEXPECTEDMESSAGETYPE":
                labelText = "Message to the server was different than what was expected by the server";
                break;
            case "INVALIDMESSAGEFORMAT":
                labelText = "Message format not correct";
                break;
        }
        view.getFooterLabel().setText(labelText);
        view.getFooterLabel().setForeground(Color.RED);
    }

    public void roundResult(String[] fromServer){
        String overallResults = "";
        String message = "";
        for(int i = 1; i < fromServer.length - 4; i += 5){
            if(fromServer[i].equals(model.getUsername())){
                message = fromServer[i+1];
            }
            overallResults += fromServer[i] + " => Score: " + fromServer[i+2] + " | Fooled: " + fromServer[i+3] + "player(s) | Fooled by " + fromServer[i+4] + " player(s)\n";
        }
        view.roundResults();
        model.setCurrentState(overallResults);
        view.getTextarea1().setText(message);
        view.getTextarea2().setText(overallResults);
    }

    public void nextRound(String[] fromServer){
        view.getSuggestionUI();
        view.getTextarea1().setText(fromServer[1]);
        model.setNumQuestions(1);
    }

    public void logout(String[] fromServer){
        switch (fromServer[2]){
            case "SUCCESS":
                break;
            case "USERNOTLOGGEDIN":
                view.getFooterLabel().setText("This user isn't even logged in!");
                view.getFooterLabel().setForeground(Color.red);
                break;
        }
    }

    public static void main(String[] args){
        FoilMakerClient game = new FoilMakerClient();
        try {
            game.connect();
            game.run();
            game.messageHandler();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

/**
 * TODO
 * 1) do error messages come with a "RESPONSE" at messageArray[0]?*
 *
 */
