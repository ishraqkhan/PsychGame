import java.io.BufferedReader;
import java.io.PrintWriter;

/**
 * User.java is a class that holds all the data about each player who is currently logged in
 */

public class User {
    private String username;
    private String password;
    private String userToken;
    private PrintWriter out;
    private BufferedReader in;
    private int score = 0;
    private int numPlayersFooled = 0;
    private int numFooledBy = 0;

    public User(String username, String password, String userToken, PrintWriter out, BufferedReader in){
        this.username = username;
        this.password = password;
        this.userToken = userToken;
        this.out = out;
        this.in = in;
    }

    // use this if the player isn't a new player
    public User(String username, String password, String userToken, PrintWriter out, BufferedReader in, int score, int numPlayersFooled, int numFooledBy){
        this(username, password, userToken, out, in);
        this.score = score;
        this.numPlayersFooled = numPlayersFooled;
        this.numFooledBy = numFooledBy;
    }

    public String getUsername() {
        return username;
    }

    public PrintWriter getOut() {
        return out;
    }

    public String getUserToken() {
        return userToken;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void incrementTimesFooledOthers(){
        numPlayersFooled++;
    }

    public int getNumPlayersFooled() {
        return numPlayersFooled;
    }

    public void incrementTimesFooledByOthers(){
        numFooledBy++;
    }

    public int getNumFooledBy() {
        return numFooledBy;
    }

    /**
     * toString is the method that will be used to update the userdatabase.txt for this user.
     * @return String s is a properly formatted string for the userdatabase.txt user store file.
     */
    @Override
    public String toString(){
        return String.format("%s:%s:%s:%s:%s", username, password, score, numPlayersFooled, numFooledBy);
    }
}
