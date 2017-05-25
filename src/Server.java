import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Server.java is a class that allows each client to connect, create a thread to run on, and then assigns that connection
 * a MessageHandler to handle communication between the server and client.
 */
public class Server {

    // concurrent hashmaps to take advantage of optimized synchronization within the java.util.concurrent package
    private static ConcurrentHashMap<String, User> activePlayers = new ConcurrentHashMap<String, User>(); // hashmap to link user tokens to users
    private static ConcurrentHashMap<String, Game> activeGames = new ConcurrentHashMap<String, Game>(); // hashmap to link game tokens to active games
    private static ConcurrentHashMap<User, Game> playersToGames = new ConcurrentHashMap<User, Game>(); // hashmap to link users to their games

    public static ConcurrentHashMap<String, User> getActivePlayers() {
        return activePlayers;
    }

    public static ConcurrentHashMap<User, Game> getPlayersToGames() { return playersToGames; }

    public static ConcurrentHashMap<String, Game> getActiveGames() {
        return activeGames;
    }

    public static void main(String[] args){
        try {
            ServerSocket ss = new ServerSocket(40000);
            System.out.printf("Waiting for client...\n");
            while(true){
                new Thread(new MessageHandler(ss.accept())).start();
            }
        }
        catch (IOException e){
            System.out.printf("%s\n--------------------\n", e.getMessage());
            e.printStackTrace();
        }
    }
}
