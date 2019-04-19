import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
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

    // hashmap that maps client username to their ID
    private HashMap<String, Integer> ClientInfo = new HashMap<String, Integer>();
    private NetworkConnection conn;
    private int portNum;
    private TextArea clientsConnected = new TextArea();
    private TextArea messages = new TextArea();


    // method to create the GUI contents of the server GUI
    private Parent createContent() {

        // initialize all of the needed GUI components
        messages.setEditable(false);
        messages.setPrefHeight(60);
        messages.setPadding(new Insets(5, 5, 5, 5));
        clientsConnected.setPrefHeight(30);
        Label portPromptLbl = new Label("Enter a port number to connect to server");
        portPromptLbl.setId("bold");
        portPromptLbl.setTranslateX(5);
        portPromptLbl.setTranslateY(10);
        TextField portField = new TextField();
        portField.setPrefWidth(80);

        // Buttons to turn the server on and off
        Button srvOn, srvOff;
        srvOn = new Button("Server ON");
        srvOff = new Button("Server OFF");
        srvOn.setTranslateX(15);
        srvOn.getStyleClass().add("button");
        srvOn.setId("bold");
        srvOff.setId("bold");
        srvOn.setDisable(true);
        srvOff.setDisable(true);
        // Hbox that holds server on and off buttons
        HBox OnOffBtns = new HBox(400, srvOn, srvOff);

        // create a VBox root and initialize
        VBox root = new VBox(20, portPromptLbl, portField, messages);
        root.setPrefSize(600, 600);
        root.getStylesheets().add("Background.css");

        // event handler for portField
        portField.setOnAction(event -> {
            // read the text entered and enable srvOn button
            messages.clear();
            messages.setDisable(false);
            try {
                portNum = Integer.parseInt(portField.getText());
                if (portNum >= 1024 && portNum <= 65535) {
                    root.getChildren().add(OnOffBtns);
                    srvOn.setDisable(false);
                    portField.setDisable(true);
                    portPromptLbl.setDisable(true);
                    messages.appendText("Press Server ON button below to create the server.");
                } else {
                    messages.appendText("The port must be between 1024 and 65535. Retry!");
                }
            } catch (Exception e) {
                portField.clear();
                messages.appendText("The port number must contain digits only. Retry!");
            }
        });

        // event handler for srvOn button
        srvOn.setOnAction(event -> {
            // turn server on
            try {
                conn = createServer();
                init();
                root.getChildren().remove(portPromptLbl);
                root.getChildren().remove(portField);
                messages.clear();
                messages.setPrefHeight(125);
                messages.appendText("A server with port number " + portNum + " is ON and is listening for client connections.\n");
                messages.appendText("Players will be able to challenge each other once there are 2 or more clients online\n");
                srvOn.setDisable(true);
                srvOff.setDisable(false);
                root.getChildren().add(clientsConnected);
                clientsConnected.setDisable(false);
                clientsConnected.appendText("Number of clients connected: " + conn.numClients + "\n");
                clientsConnected.setId("bold");
                clientsConnected.setPadding(new Insets(5, 5, 5, 5));
            } catch (Exception e) {
                srvOn.setDisable(true);
                messages.setDisable(true);
                messages.appendText("Could not create server with port number " + portNum);
            }


        });

        // event handler for srvOff button
        srvOff.setOnAction(event -> {
            // turn server off
            try {
                stop();
            } catch (Exception e) {
                System.out.println("Could not close server with port number " + portNum);
            }
            Platform.exit();
        });

        return root;
    }


    // main method
    public static void main(String[] args) {
        launch(args);
    }

    // method to open the GUI window
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("RPSLS Server");
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.show();

    }

    // initializes the conn server connection
    @Override
    public void init() throws Exception {
        if (conn != null) {
            conn.startConn();
        }
    }

    // closes the conn server connection
    @Override
    public void stop() throws Exception {
        if (conn != null)
            conn.closeConn();
    }

    // creates and returns a server
    private synchronized Server createServer() {
        return new Server(portNum, data -> {
            // synchronized block so that it can be used by only one thread
            synchronized (this) {
                Platform.runLater(() -> {
                    // get the string value of data
                    String input = data.toString();
                    System.out.println("Client " + conn.threadID + ": " + input);

                    // a new client connected to server
                    if (input.equals("I am connected")) {
                        clientsConnected.setText("Number of players connected: " + conn.numClients + "\n");
                    }

                    //Checking if the username is already in the list
                    else if (input.length() >= 10 && input.substring(0, 10).equals("Username: ")) {
                        input = input.substring(10, input.length());
                        // check if the user name is not already takem
                        if (!ClientInfo.containsKey(input)) {
                            // add the name and id to hashmap
                            ClientInfo.put(input, conn.threadID);
                            conn.send("Username approved", conn.threadID);
                            conn.setSenderUsername(input);

                            // send this player's username to other players
                            for (int i = 0; i < conn.numClients; i++) {
                                if (i != conn.threadID) {
                                    conn.send("New player joined: " + conn.getSenderUsername(), i);
                                } else {
                                    // send other players' usernames to this player
                                    for (int j = 0; j < conn.numClients; j++) {
                                        if (j != conn.threadID) {
                                            String playerName = conn.threads.get(j).getClientUsername();
                                            conn.send("New player joined: " + playerName, conn.threadID);
                                            if (!conn.threads.get(ClientInfo.get(playerName)).isAvailable) {
                                                conn.send("Player is occupied: " + playerName, conn.threadID);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            // the name is taken
                            conn.send("Username not approved", conn.threadID);
                        }
                    }
                });
            }
        });

    }

}