/**
 * Created by sunil on 6/28/16.
 * This class defined the protocol used to communicate between FoilMaker clients and the server.
 *
 */
public class FoilMakerNetworkProtocol {
    public static enum MSG_TYPE {
        //Client messages to server

        CREATENEWUSER,// Tokens: userName  passWord
        LOGIN, // Tokens: userName password
        LOGOUT, // Tokens:  currentLoginToken?
        STARTNEWGAME, // No tokens?
        JOINGAME, // Tokens: currentLoginToken gameKey
        ALLPARTICIPANTSHAVEJOINED, // Send from leader to server; Tokens: currentLoginToken gameKey

        //Client message to server during a game
        PLAYERCHOICE, // Tokens: currentLoginToken gameKey user'sChoice
        PLAYERSUGGESTION, // Tokens: currentLoginToken gameKey user'sChoice

        // Server messages to client
        NEWPARTICIPANT, //From server to leader; Tokens: participantName cummulativeScore
        RESPONSE, // Server response to user request
                    /* Tokens:
                     * clientRequestMsgType -- the MSG_TYPE of the client's request
                     * responseDetail -- the MSG_DETAIL_T of the server's response
                     * <Other optional tokens specific to MSG_DETAIL_T>
                     */


        //Server messages to clients during a game
        NEWGAMEWORD, // From server to players; Tokens: cardFrontText cardBackText
        ROUNDOPTIONS, // From server to players; Tokens: randomized list of user suggestions and true answer
        ROUNDRESULT, //From server to players; Tokens: uName1 score1 message1 uName2 score2 message2 ....
        GAMEOVER // From server to players: Tokens: MSG_DETAIL
    };

    public static enum MSG_DETAIL_T {
        SUCCESS, // Request was successfull. For LOGIN: currentLoginToken;  For STARTNEWGAME: gameKey; For JOINGAME:
        // gameKey;
        INVALIDUSERNAME,
        INVALIDUSERPASSWORD,
        USERALREADYEXISTS,
        UNKNOWNUSER,
        USERALREADYLOGGEDIN,
        GAMEKEYNOTFOUND,
        NO_CONNECTION_TO_SERVER,
        ERROR_OPENING_NETWORK_CONNECTION,
        USERNOTLOGGEDIN,
        USERNOTGAMELEADER,
        INVALIDGAMETOKEN,
        UNEXPECTEDMESSAGETYPE,
        INVALIDMESSAGEFORMAT, //TODO received msg with tokens EXPECTING: expected format
        FAILURE // optional details of failure cause
    };

    //TODO Create error codes type and values
    public static final String SEPARATOR = "--";
    public static final int LOGIN_TOKEN_LENGTH = 10;
    public static final int GAME_KEY_LENGTH = 3;
}
