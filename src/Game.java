import javax.sound.midi.SysexMessage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Game.java is a class that holds the actual gameplay for the game. It contains data structures for holding player data
 * as well as methods for playing rounds of the game.
 */
public class Game implements Runnable{
    private ArrayList<User> players = new ArrayList<User>(); // list of all players in the game

    // contains all the users and their suggestion for the round
    private HashMap<User, String> usersToSuggestions = new HashMap<User, String>();
    private final Object suggestionLock = new Object();

    // contains all the users and their answer for the round
    private HashMap<User, String> usersToAnswers = new HashMap<User, String>();
    private final Object answerLock = new Object();

    // wait for all suggestions
    private CountDownLatch cdlSuggestions;
    // wait for all answers
    private CountDownLatch cdlAnswers;

    // contains the users and the correct message that they will receive at the end of the round
    private HashMap<User, String> roundMessage = new HashMap<User, String>();

    // game details
    private String gameToken; // unique game token
    private User leader; // designates the leader of the game

    public Game(User leader, String gameToken){
        players.add(leader);
        this.leader = leader;
        this.gameToken = gameToken;

    }

    /**
     * Method to return the arraylist of active players in the game
     * @return the active players list
     */
    public ArrayList<User> getPlayers() {
        return players;
    }

    /**
     * Method to add a player to the active players array list
     * @param user to add to the active players list
     */
    public void addPlayer(User user){
        players.add(user);
    }

    /**
     * Method to remove a player form the active players array list
     * @param user to remove to the active players list
     */
    public void removePlayer(User user){
        players.remove(user);
    }

    /**
     * Method to get the game's leader
     * @return the User that is the game's leader
     */
    public User getLeader() { return leader; }

    /**
     * Method to return the 3 character game token
     * @return string game token
     */
    public String getGameToken() {
        return gameToken;
    }

    /**
     * Method to loop through all active players and send them a message
     * @param s string to be sent to the players
     */
    public void sendToPlayers(String s){
        for(User player : players){
            player.getOut().println(s);
            player.getOut().flush();
        }
    }

    /**
     * Method to add a player's suggestion for the phrase to the suggestions map
     * @param player User that the suggestion belongs to.
     * @param suggestion String suggestion for the phrase.
     */
    public void addSuggestion(User player, String suggestion){
        synchronized (suggestionLock) {
            usersToSuggestions.put(player, suggestion);
        }
    }

    /**
     * Method to add the player's answer for the phrase to the answers map
     * @param player User that the answer belongs to.
     * @param answer String answer for the phrase
     */
    public void addAnswer(User player, String answer){
        synchronized (answerLock){
            usersToAnswers.put(player, answer);
        }
    }

    /**
     * Method to decrement the suggestion latch in order to properly wait for all players to add a suggestion to the round
     */
    public void decrementSuggestionLatch(){
        cdlSuggestions.countDown();
    }

    /**
     * Method to decrement the answer latch in order to properly wait for all players to add an answer to the round
     */
    public void decrementAnswerLatch() {
        cdlAnswers.countDown();
    }

    /**
     * Method to read all the suggestions for the round and send them back to each user in a random order.
     * @param answer String: the correct answer for the phrase for this round
     * @return String response to be sent back to the client
     */
    public String randomizeRoundOptions(String answer){
        String response = "ROUNDOPTIONS--";
        String separator = "--";
        Random r = new Random();
        String[] suggestions = new String[usersToSuggestions.size() + 1];
        usersToSuggestions.values().toArray(suggestions); // put all the options into the array
        suggestions[suggestions.length - 1] = answer; // add answer

        for(int i = suggestions.length-1; i > 0; i-- ){
            int index = r.nextInt(i);
            String s = suggestions[index];
            String temp;

            temp = suggestions[i];
            suggestions[i] = s;
            suggestions[index] = temp;

            response += s + separator;
        }
        response += suggestions[0];
        return response;
    }

    /**
     * Method to properly score each user for each round once all players have submitted a suggestion.
     * @param player User that is being scored
     * @param correctAnswer String correct answer for the round's phrase
     */
    public void scoreUser(User player, String correctAnswer) {
        String playerAnswer = usersToAnswers.get(player);
        String message = "";

        if (playerAnswer.equals(correctAnswer)) {
            player.addScore(10);
            message += "You got it right!";
        } else {
            for (User participant : players) {
                if (usersToSuggestions.get(participant).equals(playerAnswer) && participant != player) {
                    message += "You were fooled by " + participant.getUsername();
                    player.incrementTimesFooledByOthers();

                    roundMessage.put(participant, "You fooled " + player.getUsername());
                    participant.addScore(5);
                    participant.incrementTimesFooledOthers();
                }
            }
        }

        if (roundMessage.get(player) == null) {
            roundMessage.put(player, "ROUNDRESULT--" + player.getUsername() + "--" + message + "--" + player.getScore() + "--" + player.getNumPlayersFooled() + "--" + player.getNumFooledBy());
        }else{
            String currentMessage = roundMessage.get(player);
            roundMessage.put(player, "ROUNDRESULT--" + player.getUsername() + "--" + (message + currentMessage) + "--" + player.getScore() + "--" + player.getNumPlayersFooled() + "--" + player.getNumFooledBy());

        }
    }

    @Override
    public void run() {
        try {
            BufferedReader readQuestions = new BufferedReader(new FileReader("WordleDeck"));
            String line;
            while((line = readQuestions.readLine()) != null ){
                if(players.size() < 2){
                    //exit game if there is only 1 player left
                    return;
                }
                cdlSuggestions = new CountDownLatch(players.size()); // new countdownlatch in case players leave mid game!
                cdlAnswers = new CountDownLatch(players.size()); // new countdownlatch in case players leave mid game!

                usersToSuggestions.clear(); // empty suggestion bin for this round
                roundMessage.clear(); // empty the message bins for this round

                String question = line.substring(0, line.indexOf(":")).trim(); // definition being sent to players
                String answer = line.substring(line.indexOf(":")+1).trim(); // correct word for the definition
                String outString = String.format("NEWGAMEWORD--%s--%s", question, answer); // properly formatted string to be sent to the players

                // send to all players
                sendToPlayers(outString);

                // wait for all players to submit a suggestion
                cdlSuggestions.await();

                // randomize the options and send them back to the user
                sendToPlayers(randomizeRoundOptions(answer));

                // wait for all players to submit their answer
                cdlAnswers.await();

                // update player scores and send the round message to the players
                for(User player : players){
                    scoreUser(player, answer);
                    player.getOut().println(roundMessage.get(player));
                    player.getOut().flush();
                }
            }
            readQuestions.close();
            sendToPlayers("GAMEOVER--");

        } catch (IOException e){
            System.out.printf("IO Exception\n");
            e.printStackTrace();
        } catch (InterruptedException e){
            System.out.printf("Interrupted Exception\n");
            e.printStackTrace();
        }
    }
}
