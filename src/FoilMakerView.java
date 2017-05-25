import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * What the client sees and interacts with. buttons here to call the client
 */
public class FoilMakerView implements ActionListener, WindowListener{

    private FoilMakerClient controller; // game controller
    private FoilMakerModel model;
    private JFrame frame = new JFrame("FoilMaker"); // jframe for UI
    private JLabel headerLabel = new JLabel("Welcome!", SwingConstants.CENTER); // header label
    private JLabel footerLabel = new JLabel(); // footer label
    private JButton logIn, register, startNewGame, startGame, joinAGame, joinGame, submitSuggestion, submitOption, nextRound;
    private JTextField textField1, textField2; // general use textfields
    private JTextArea textarea1, textarea2;
    private ButtonGroup optionsGroup;

    public FoilMakerView(FoilMakerClient controller, FoilMakerModel model){
        this.controller = controller;
        this.model = model;
        frame.addWindowListener(this);
        frame.setResizable(false);
    }

    public void logInUI(){
        logIn = new JButton("Log in");
        logIn.setActionCommand("Log in");
        logIn.addActionListener(this);
        register = new JButton("Register");
        register.setActionCommand("Register");
        register.addActionListener(this);

        //labels - use label3 to display error message
        JLabel label1 = new JLabel("Username:");
        JLabel label2 = new JLabel("Password:");
        footerLabel.setText("Please log in or register a new user");

        textField1 = new JTextField("", 20);
        textField2 = new JPasswordField("", 20);

        JPanel p1 = new JPanel(new FlowLayout());
        p1.add(label1);
        p1.add(textField1);
        JPanel p2 = new JPanel(new FlowLayout());
        p2.add(label2);
        p2.add(textField2);
        JPanel p3 = new JPanel(new FlowLayout());
        p3.add(logIn);
        p3.add(register);
        JPanel p4 = new JPanel(new FlowLayout());
        p4.add(footerLabel);

        frame.setLayout(new GridLayout(0,1));
        frame.getContentPane().add(p1);
        frame.getContentPane().add(p2);
        frame.getContentPane().add(p3);
        frame.getContentPane().add(p4);

        frame.setSize(300,400);
        frame.setVisible(true);
    }

    public void gameLobby(){
        frame.getContentPane().removeAll();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        footerLabel.setText("Welcome!");
        footerLabel.setForeground(Color.black);
        startNewGame = new JButton("Start a game");
        startNewGame.addActionListener(this);
        joinAGame = new JButton("Join a game");
        joinAGame.addActionListener(this);

        frame.setLayout(new BorderLayout());

        frame.getContentPane().add(headerLabel, BorderLayout.NORTH);
        frame.getContentPane().add(footerLabel, BorderLayout.SOUTH);
        panel.add(startNewGame);
        panel.add(joinAGame);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setSize(300,300);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }

    public void joinGameUI(){
        frame.getContentPane().removeAll();
        JLabel label1 = new JLabel("Enter the game token: ");
        joinGame = new JButton("Join Game");
        joinGame.addActionListener(this);
        textField1.setText("");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,1));
        frame.getContentPane().setLayout(new BorderLayout());

        JPanel p1 = new JPanel(new GridBagLayout());
        p1.add(label1);
        p1.add(textField1);

        JPanel p3 = new JPanel(new GridBagLayout());
        p3.add(joinGame);

        panel.add(p1);
        panel.add(p3);

        frame.getContentPane().add(headerLabel, BorderLayout.NORTH);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(footerLabel, BorderLayout.SOUTH);
        footerLabel.setForeground(Color.BLACK);

        frame.setSize(400,300);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }

    public void startGameUI(){
        frame.getContentPane().removeAll();
        footerLabel.setText("Press <Start Game> to start the game");
        JLabel label1 = new JLabel("Others should use this key to join your game:");
        JLabel label2 = new JLabel(model.getGameToken());
        JPanel p1 = new JPanel(new GridBagLayout());
        JPanel p2 = new JPanel(new GridBagLayout());

        textarea1 = new JTextArea(5, 20);
        textarea1.setEditable(false);
        JScrollPane scroll = new JScrollPane(textarea1);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        startGame = new JButton("Start Game");
        startGame.addActionListener(this);
        footerLabel.setForeground(Color.BLACK);

        p1.add(label1);
        p1.add(label2);
        p2.add(scroll);
        p2.add(startGame);

        JPanel p3 = new JPanel(new GridLayout(0,1));
        p3.add(p1);
        p3.add(p2);

        frame.add(headerLabel, BorderLayout.NORTH);
        frame.getContentPane().add(p3);
        frame.add(footerLabel, BorderLayout.SOUTH);

        frame.setSize(400, 300);
        frame.setVisible(true);
    }

    public void joinedUI(){
        frame.getContentPane().removeAll();
        JLabel label = new JLabel("Waiting for leader..");

        footerLabel.setForeground(Color.BLACK);
        frame.getContentPane().add(headerLabel, BorderLayout.NORTH);
        frame.getContentPane().add(label);
        frame.getContentPane().add(footerLabel, BorderLayout.SOUTH);
        frame.setSize(300,300);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }

    public void getSuggestionUI(){
        frame.getContentPane().removeAll();
        footerLabel.setText("Enter your suggestion");
        submitSuggestion = new JButton("Submit Suggestion");
        submitSuggestion.addActionListener(this);
        JLabel label1 = new JLabel("What is the word for:");
        textarea1 = new JTextArea(5,20);
        textarea1.setEditable(false);
        textField1.setText("");

        footerLabel.setForeground(Color.BLACK);
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        panel1.setBorder(BorderFactory.createLineBorder(Color.black,1));
        panel1.add(label1, BorderLayout.NORTH);
        panel1.add(textarea1, BorderLayout.CENTER);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        panel2.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        panel2.setBorder(BorderFactory.createTitledBorder("Your Suggestion"));
        panel2.add(textField1, BorderLayout.CENTER);
        panel2.add(submitSuggestion, BorderLayout.SOUTH);

        JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayout(0,1));
        panel3.add(panel1);
        panel3.add(panel2);


        frame.getContentPane().add(headerLabel,BorderLayout.NORTH);
        frame.getContentPane().add(panel3, BorderLayout.CENTER);
        frame.getContentPane().add(footerLabel,BorderLayout.SOUTH);

        frame.setSize(300,300);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }

    public void sendPlayerSuggestionUI(String[] roundoptions){
        frame.getContentPane().removeAll();
        submitOption = new JButton("Submit Option");
        submitOption.addActionListener(this);

        frame.getContentPane().add(headerLabel, BorderLayout.NORTH);
        frame.getContentPane().add(footerLabel, BorderLayout.SOUTH);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0,1));
        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Pick an option below"));

        optionsGroup = new ButtonGroup();
        for(int i = 1; i<roundoptions.length; i++){
            JRadioButton option = new JRadioButton(roundoptions[i]);
            option.setActionCommand(roundoptions[i]);
            buttonPanel.add(option);
            optionsGroup.add(option);
        }

        JPanel submitButtonPanel = new JPanel();
        submitButtonPanel.add(submitOption);
        footerLabel.setForeground(Color.BLACK);
        buttonPanel.add(submitButtonPanel);

        frame.getContentPane().add(buttonPanel);
        frame.setSize(300,300);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }

    public void roundResults(){
        frame.getContentPane().removeAll();
        frame.getContentPane().add(headerLabel, BorderLayout.NORTH);
        frame.getContentPane().add(footerLabel, BorderLayout.SOUTH);
        footerLabel.setForeground(Color.BLACK);

        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();
        JPanel buttonPanel = new JPanel();

        textarea2 = new JTextArea();
        textarea2.setEditable(false);
        textarea2.setSize(5, 20);
        JScrollPane scroll1 = new JScrollPane(textarea2);
        scroll1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        nextRound = new JButton("Next Round");
        nextRound.addActionListener(this);
        buttonPanel.add(nextRound);

        panel2.add(textarea1);
        panel3.add(scroll1);

        panel1.add(panel2);
        panel1.add(panel3);
        panel1.add(buttonPanel);

        frame.getContentPane().add(panel1, BorderLayout.CENTER);

        frame.setSize(400, 400);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }


    public JLabel getFooterLabel(){
        return footerLabel;
    }

    public JLabel getHeaderLabel() {
        return headerLabel;
    }

    public String getUsername(){
        return textField1.getText();
    }

    public JTextArea getTextarea1(){
        return textarea1;
    }

    public JTextArea getTextarea2() {return textarea2; }

    public JButton getNextRound(){
        return nextRound;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        String outString = "";
        if(e.getSource() == logIn){
            if(textField1.getText().trim().length() != 0 || textField2.getText().trim().length() != 0) {
                outString = String.format("LOGIN--%s--%s", textField1.getText(), textField2.getText());
                controller.sendMessage(outString);
            }
        }
        if(e.getSource() == register){
            if(textField1.getText().trim().length() != 0 || textField2.getText().trim().length() != 0) {
                outString = String.format("CREATENEWUSER--%s--%s", textField1.getText(), textField2.getText());
                controller.sendMessage(outString);
            }
        }
        if(e.getSource() == startNewGame){
            outString = String.format("STARTNEWGAME--%s", model.getUserToken());
            controller.sendMessage(outString);
        }
        if(e.getSource() == joinAGame){
            joinGameUI();
        }

        if(e.getSource() == joinGame){
            outString = String.format("JOINGAME--%s--%s", model.getUserToken(), textField1.getText());
            controller.sendMessage(outString);
        }
        if(e.getSource() == startGame){
            if (model.getParticipants().size() > 1) {
                outString = String.format("ALLPARTICIPANTSHAVEJOINED--%s--%s", model.getUserToken(), model.getGameToken());
                controller.sendMessage(outString);
            }
        }
        if(e.getSource() == submitSuggestion){
            submitSuggestion.setEnabled(false);
            outString = String.format("PLAYERSUGGESTION--%s--%s--%s", model.getUserToken(), model.getGameToken(),textField1.getText());
            controller.sendMessage(outString);
        }
        if(e.getSource() == submitOption){
            submitOption.setEnabled(false);
            String choice = optionsGroup.getSelection().getActionCommand();
            outString = String.format("PLAYERCHOICE--%s--%s--%s", model.getUserToken(),model.getGameToken(),choice);
            controller.sendMessage(outString);
        }
        if(e.getSource() == nextRound){
            controller.nextRound(controller.getInString().split(FoilMakerNetworkProtocol.SEPARATOR));
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
        //do nothing
    }

    @Override
    public void windowClosing(WindowEvent e) {
        controller.sendMessage("LOGOUT--");
    }

    @Override
    public void windowClosed(WindowEvent e) {
        //do nothing
    }

    @Override
    public void windowIconified(WindowEvent e) {
        //do nothing
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        //do nothing
    }

    @Override
    public void windowActivated(WindowEvent e) {
        //do nothing
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        //do nothing
    }
}
