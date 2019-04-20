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

public class FXNet extends Application {
    private HashMap<String, Integer> ClientInfo = new HashMap<String, Integer>();  // maps client username to their ID
    private NetworkConnection conn;
    private int portNum;
    private Label clientsConnected = new Label();
    private TextArea messages = new TextArea();
    private boolean gameStarted = false;
    private int questionNum = 1;
    
    private HashMap<Integer, String> randQtns = new HashMap<Integer, String>();

    // create the contents of the server GUI
    private Parent createContent() {
        messages.setEditable(false);
        messages.setMaxHeight(40);
        messages.setPadding(new Insets(5, 5, 5, 5));
        clientsConnected.setPrefHeight(30);

        // contains introductory content
        VBox box = new VBox();
        box.setSpacing(10);
        box.setAlignment(Pos.CENTER);
        // lists the instructions
        VBox instrBox = new VBox();
        box.setAlignment(Pos.CENTER);
        // Hbox for clients connected and PLAY
        HBox playBox = new HBox();
        playBox.setSpacing(65);
        messages.setPrefHeight(60);
        messages.setEditable(false);

        Label greetLabel = new Label("Welcome to the Trivia Game!");
        Label instLabel = new Label("Here's how to play: ");
        Label instLabel2 = new Label("A group of players will be asked a new question during each round with 3 choices. The players ");
        Label instLabel3 = new Label("who get the question correct are rewarded 1 point. At the end of the game, a ranking of player");
        Label instLabel4 = new Label(" scores will be displayed.");
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
        VBox root = new VBox(20, box, messages);
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
                randQtns = extractQs.getTriviaQnsHashMap();

                //DOUBLE CHECKING HASHMAP TO CHECK IF IT WORKED, REMOVE LATER. FOR SAEMA'S REFERENCE
                for (HashMap.Entry<Integer,String> entry : randQtns.entrySet()) {
                    System.out.println("Key: " + entry.getKey() + " Question: " + entry.getValue());
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

                            // send a message to the clients when to begin the game
                            if (conn.numClients == 2 && !gameStarted) {
                                for (int i=0; i<2; i++) {
                                    conn.send("Start game", i);
                                }
                                gameStarted = true;
                            }
                        }
                        else{
                            // the name is taken
                            conn.send("Username not approved", conn.threadID);
                        }
                    }

                });
            }
        });

    }

}
