
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class FXNet extends Application {
    private HashMap<String, Integer> ClientInfo = new HashMap<String, Integer>();  // maps client username to their ID
    private ArrayList<Label> scoreLabels = new ArrayList<Label>();
    private HashMap<Integer, String> questionBank = new HashMap<Integer, String>();
    private ArrayList<Integer> questionsUsed = new ArrayList<>();
    private NetworkConnection conn;
    private int portNum;
    private Label clientsConnected = new Label();
    private TextArea messages = new TextArea();
    private boolean gameStarted = false;
    private VBox scoreBoard = new VBox();
    private Label boardTitle;

    // create the contents of the server GUI
    private Parent createContent() {
        messages.setEditable(false);
        messages.setMaxHeight(40);
        messages.setPadding(new Insets(5, 5, 5, 5));
        clientsConnected.setPrefHeight(30);

        // set up the scoreboard
        scoreBoard.setSpacing(10);
        boardTitle = new Label("Scores");
        boardTitle.setVisible(false);
        scoreBoard.getChildren().add(boardTitle);
        scoreBoard.setAlignment(Pos.CENTER);

        VBox box = new VBox();
        box.setSpacing(10);
        box.setAlignment(Pos.CENTER);
        // lists the instructions
        VBox instrBox = new VBox();
        box.setAlignment(Pos.CENTER);
        // HBox for clients connected
        HBox playBox = new HBox();
        playBox.setSpacing(65);
        messages.setPrefHeight(60);
        messages.setEditable(false);

        Label greetLabel = new Label("Welcome to the Trivia Game!");
        Label instLabel = new Label("Here's how to play: ");
        Label instLabel2 = new Label("A group of players will be asked a new question during each round with 3 choices. The players ");
        Label instLabel3 = new Label("who get the question correct are rewarded 1 point. At the end of the game, a ranking of player");
        Label instLabel4 = new Label("scores will be displayed.");
        instrBox.getChildren().addAll(instLabel, instLabel2, instLabel3, instLabel4);

        Label serverLabel = new Label("Begin by connecting to a server.");
        serverLabel.setPadding(new Insets(30, 0, 0, 0));
        Label portLabel = new Label("Enter a port number to connect.");
        TextField portField = new TextField();
        portField.setMaxWidth(130);

        playBox.getChildren().addAll(clientsConnected);
        box.getChildren().addAll(greetLabel, instrBox, serverLabel, portLabel, portField);

        // buttons to turn the server on and off
        Button srvOn = new Button("Server ON");
        Button srvOff = new Button("Server OFF");
        srvOn.setDisable(true);
        srvOff.setDisable(true);
        HBox OnOffBtns = new HBox(400, srvOn, srvOff);

        // holds the main server content
        VBox root = new VBox(20, box, messages, scoreBoard);
        root.setPrefSize(600, 600);

        portField.setOnAction(event -> {
            messages.clear();
            messages.setDisable(false);
            try {
                portNum = Integer.parseInt(portField.getText());    // read the text from the port textfield
                if (portNum >= 1024 && portNum <= 65535) {
                    root.getChildren().add(OnOffBtns);
                    srvOn.setDisable(false);
                    portField.setDisable(true);
                    portLabel.setDisable(true);
                    messages.appendText("Press Server ON button below to create the server.");
                }
                else {
                    messages.appendText("The port must be between 1024 and 65535. Retry!");
                }
            }
            catch (Exception e){
                portField.clear();
                messages.appendText("The port number must contain digits only. Retry!");
            }
        });

        srvOn.setOnAction(event ->{
            // turn server on
            try {
                conn = createServer();
                init();
                messages.clear();
                messages.appendText("A server with port number " + portNum + " is ON and is listening for client connections.\n");
                messages.appendText("A game will be initiated once there are 4 players online.\n");
                srvOn.setDisable(true);
                srvOff.setDisable(false);
                root.getChildren().add(playBox);
                clientsConnected.setText("Number of clients connected: " + conn.numClients + "\n");
                clientsConnected.setPadding(new Insets(5, 5, 5, 5));

                ReadTxtFile extractQs = new ReadTxtFile();
                extractQs.openFile();
                extractQs.readFile();
                extractQs.closeFile();
                questionBank = extractQs.getTriviaQnsHashMap();

                //DOUBLE CHECKING HASHMAP TO CHECK IF IT WORKED, REMOVE LATER. FOR SAEMA'S REFERENCE
                for (HashMap.Entry<Integer,String> entry : questionBank.entrySet()) {
                    // the key is the question number
                    System.out.println("Key: " + entry.getKey() + " Question: " + entry.getValue());
                }

                // display the questions with their corresponding numbers
                for (int i=1; i<=questionBank.size(); i++) {
                    System.out.println("Q" + i + ": " + questionBank.get(i));
                }
            }
            catch (Exception e){
                srvOn.setDisable(true);
                messages.setDisable(true);
                messages.appendText("Could not create server with port number " + portNum + ".");
            }
        });

        srvOff.setOnAction(event -> {
            // turn server off
            try {
                stop();
            }
            catch (Exception e){
                System.out.println("Could not close server with port number " + portNum + ".");
            }
            Platform.exit();
        });
        return root;
    }

    // create the label for the player's score
    public Label makeLabel() {
        return new Label();
    }

    public static void main(String[] args){
        launch(args);
    }

    // method to open the GUI window
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Trivia Game");
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.show();
    }

    // initializes the conn server connection
    @Override
    public void init() throws Exception{
        if(conn != null) {
            conn.startConn();
        }
    }

    // closes the conn server connection
    @Override
    public void stop() throws Exception{
        if(conn != null)
            conn.closeConn();
    }


    // game state information
    private int numAnswered;         // check how many people answered the question
    private int questionNum = 1;     // current question number the game is on, beginning from 1
    private final int endQnum = 11;  // the ending qNum will be the # of questions in a game + 2

    // creates and returns a server
    private synchronized Server createServer() {
        return new Server(portNum, data-> {
            // synchronized block so that it can be used by only one thread
            synchronized (this) {
                Platform.runLater(() -> {
                    // get the string value of data
                    String input = data.toString();
                    System.out.println("Client " + conn.threadID + ": " + input);

                    // a new client connected to server
                    if (input.equals("I am connected")){
                        clientsConnected.setText("Number of players connected: " + conn.numClients + "\n");
                    }
                    //Checking if the username is already in the list
                    else if(input.length() >= 10 && input.substring(0,10).equals("Username: ")){
                        input = input.substring(10,input.length());
                        // check if the user name is not already takem
                        if(!ClientInfo.containsKey(input)) {
                            // add the name and id to hash map
                            ClientInfo.put(input, conn.threadID);
                            conn.send("Username approved", conn.threadID);
                            conn.setSenderUsername(input);

                            // send this player's username to other players
                            for(int i = 0; i < conn.numClients; i++){
                                if(i != conn.threadID){
                                    conn.send("New player joined: " + conn.getSenderUsername(), i);
                                }
                                else{
                                    // send other players' usernames to this player
                                    for(int j = 0; j < conn.numClients; j++){
                                        if(j != conn.threadID){
                                            String playerName = conn.threads.get(j).getClientUsername();
                                            conn.send("New player joined: " + playerName, conn.threadID);
                                        }
                                    }
                                }
                            }
                            // send a message to the clients when to begin the game
                            if (!gameStarted && conn.numClients == 4) {
                                for (int i=0; i<conn.numClients; i++) {
                                    conn.send("Start game", i);
                                    scoreLabels.add(makeLabel());   // create a label for each player's score
                                }
                                gameStarted = true;
                            }

                        }
                        else{
                            // the name is taken
                            conn.send("Username not approved", conn.threadID);
                        }
                    }
                    // parse the score to display the final points on the scoreboard
                    if (input.length() >= 13 && input.substring(0, 13).equals("final score: ")) {
                        int score = Integer.parseInt(input.substring(13));
                        conn.threads.get(conn.threadID).score = score;

                        int numPlayersFinishedGame = 0;
                        for(int i = 0; i < conn.numClients; i++){
                            if(conn.threads.get(i).score != -1){
                                numPlayersFinishedGame++;
                            }
                        }
                        if(numPlayersFinishedGame == conn.numClients){
                            determineRanks();
                            // send the rank of each client to all clients
                            for (int i=0; i<conn.numClients; i++) {
                                for (int j = 0; j < conn.numClients; j++) {
                                    conn.send(conn.threads.get(i).getClientUsername() + "'s score: " + conn.threads.get(i).score + "\n", j);
                                    conn.send(conn.threads.get(i).getClientUsername() + "'s rank: " + conn.threads.get(i).rank + "\n", j);
                                }
                            }

                            // update a label with that player's game info
                            Label l = scoreLabels.get(0);
                            l.setText("" + conn.getSenderUsername() + "  | Score: " + score);
                            boardTitle.setVisible(true);
                            scoreBoard.getChildren().add(l);
                            scoreLabels.remove(0);

                        }
                        // update a label with that player's game info
                        Label l = scoreLabels.get(0);
                        l.setText("" + conn.getSenderUsername() + "  | Score: " + score);
                        boardTitle.setVisible(true);
                        scoreBoard.getChildren().add(l);
                        scoreLabels.remove(0);
                    }

                    //Send another question
                    else if(gameStarted && input.contains("Send question")) {
                        numAnswered++;
                        Random randomGenerator = new Random();
                        boolean regenerate = true;
                        while(regenerate) {
                            int questionNum = randomGenerator.nextInt(questionBank.size()) + 1;
                            regenerate = false;
                            for (int i = 0; i < questionsUsed.size(); i++) {
                                if (questionNum == questionsUsed.get(i)) {
                                    regenerate = true;
                                }
                            }
                        }
                        System.out.println("Question num :" + questionNum);
                        if(numAnswered == conn.numClients && questionNum < endQnum){
                            for(int i = 0; i< conn.numClients; i++){ //testing with the first 10 questions with 2 players
                                conn.send("Question: " + questionBank.get(questionNum), i);
                            }
                            questionNum++;
                            numAnswered = 0;
                        }
                    }
                });
            }
        });

    }

    void determineRanks(){
        // Score Array
        ArrayList<Integer> scores = new ArrayList<>();
        for(int i = 0; i < conn.numClients; i++){
            scores.add(i, conn.threads.get(i).score);
        }
        // Rank Array
        ArrayList<Double> ranks = new ArrayList<Double>();

        // Sweep through all elements in Scores for each
        // element count the number of less than and
        // equal elements separately in r and s.
        for (int i = 0; i < conn.numClients; i++) {
            int r = 1, s = 1;

            for (int j = 0; j < conn.numClients; j++) {
                if (j != i && scores.get(j) < scores.get(i))
                    r += 1;

                if (j != i && scores.get(j) == scores.get(i))
                    s += 1;
            }
            // Use formula to obtain rank
            ranks.add(i, r + Double.valueOf(s-1) / Double.valueOf(2));

        }

        for(int i = 0; i < conn.numClients; i++){
            int tmp = (int)Math.round(ranks.get(i));
            if(tmp == 4){
                conn.threads.get(i).rank = 1;
            }
            if(tmp == 3){
                conn.threads.get(i).rank = 2;
            }
            if(tmp == 2){
                conn.threads.get(i).rank = 3;
            }
            if(tmp == 1){
                conn.threads.get(i).rank = 4;
            }
            System.out.println("Client " + conn.threads.get(i).getName() + " ranked: " + conn.threads.get(i).rank);
        }

    }

}
