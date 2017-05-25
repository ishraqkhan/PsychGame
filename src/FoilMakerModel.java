import java.util.ArrayList;

/**
 * Game logic and state are stored here.
 */
public class FoilMakerModel {
    private FoilMakerClient controller;
    private FoilMakerView view;
    private String username;
    private String userToken;
    private String gameToken;
    private int numQuestions = 1;
    private String currentState;

    private ArrayList<String> participants = new ArrayList<String>();

    public FoilMakerModel(FoilMakerClient controller, FoilMakerView view) {
        this.controller = controller;
        this.view = view;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public void setGameToken(String gameToken) {
        this.gameToken = gameToken;
    }

    public String getUsername() {
        return username;
    }

    public String getUserToken() {
        return userToken;
    }

    public String getGameToken() {
        return gameToken;
    }

    public ArrayList<String> getParticipants() {
        return participants;
    }

    public void addParticipant(String name){
        participants.add(name);
    }

    public String stringParticipants(){
        String stringParticipants = "";
        for (String participant : participants){
            stringParticipants += participant + "\n";
        }
        return stringParticipants;
    }

    public void setNumQuestions(int numQuestions) {
        this.numQuestions += numQuestions;
    }

    public int getNumQuestions() {
        return numQuestions;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }
}
