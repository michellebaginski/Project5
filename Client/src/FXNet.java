import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import java.util.HashMap;


public class FXNet extends Application{

    private NetworkConnection conn;
    private int portNum;    // stores port number
    private String IPAddr;  // stores IP address
    private int numPlayersOnline = 0;
    private HashMap<String, Scene> sceneMap = new HashMap<String, Scene>();
    private Scene root;
    private TextField usernameField = new TextField();

    HashMap<String,ArrayList<String>> triviaQs; // Question is the key and value is the multi choice answers in ArrayList. 0 index is correct answer


    // variables to hold player and opponent game information
    private String usernameApproved = "";
    private String username;

    // declare and initialize needed GUI components
    private TextArea messages = new TextArea();

    private Button helperBtn = new Button();



    // creates a GUI scene
    private Parent createContent() {
        // initialize the GUI components
        Button connect = new Button("Connect");
        connect.setTranslateX(245);
        messages.setPrefHeight(90);


        TextField portField = new TextField();
        TextField IPField = new TextField();
        IPField.setDisable(true);
        Label portPromptLbl = new Label("Enter port number to connect to the server below");
        Label IPPromptLbl = new Label("Enter IP address below");
        Label usernameLbl = new Label("Enter a username below");
        IPPromptLbl.setDisable(true);

        // declare and initialize a root
        VBox root = new VBox(20, portPromptLbl, portField, IPPromptLbl, IPField);
        root.setPrefSize(600, 600);
        root.getStylesheets().add("Background.css");

        // event handler for port number textField
        portField.setOnAction(event -> {
            try {
                // read the entered number
                portNum = Integer.parseInt(portField.getText());
                if (portNum >= 1024 && portNum <= 65535) {
                    portPromptLbl.setDisable(true);
                    portField.setDisable(true);
                    // enable IP textField to get IP address
                    IPPromptLbl.setDisable(false);
                    IPField.setDisable(false);
                }
                else {
                    portPromptLbl.setText("The port must be between 1024 and 65535. Retry!");
                }
            }
            catch (Exception e){
                messages.setText("The port number must contain digits only. Retry!");
                portField.clear();

            }
        });

        // event handler for IP address text field
        IPField.setOnAction(event -> {
            try {
                // read the string entered
                IPAddr = IPField.getText();
                IPPromptLbl.setDisable(true);
                IPField.setDisable(true);

                root.getChildren().addAll(connect);

            }
            catch (Exception e){
                e.printStackTrace();
            }
        });

        // event handler for connect button
        connect.setOnAction(event -> {
            // connect to server
            try{
                conn = createClient();
                init();
                root.getChildren().addAll(usernameLbl,usernameField);
                connect.setDisable(true);

                //open the text file after connecting to server then extract data from the file, then close it.
                ReadTxtFile extractFile = new ReadTxtFile();
                extractFile.openFile();
                extractFile.readFile();
                extractFile.closeFile();
                triviaQs = extractFile.getTriviaQnsHashMap();

                //DOUBLE CHECKING IF HASH MAP WORKED. Take out later.
                for (HashMap.Entry<String, ArrayList<String>> entry : triviaQs.entrySet()) {
                    System.out.println("Key: "+entry.getKey());
                    ArrayList<String> val = entry.getValue();
                    System.out.println("3 values are: ");
                    for (int i = 0; i < val.size(); i++){
                        System.out.println(val.get(i));
                    }

                }


            }
            catch(Exception e){
                portPromptLbl.setDisable(false);
                portField.setDisable(false);
                messages.setText("Could not create a client with port number "
                        + portNum + " and IP address " + IPAddr + " Retry!");

            }
        });

        // event handler for usernameField
        EventHandler<ActionEvent> usernameFieldEvent = e -> {
            // get the username and send it to server
            try {
                username = usernameField.getText();
                if(usernameApproved.equals("")){
                    conn.send("Username: " + username);
                }
                else if (usernameApproved.equals("yes")) {
                    messages.setText("Press the Connect button below to connect to the server.");

                    conn.setClientUsername(username); //store client's username in NetworkConnection

                    // remove GUI contents that are not required anymore and add required ones
                    root.getChildren().remove(portPromptLbl);
                    root.getChildren().remove(portField);
                    root.getChildren().remove(IPPromptLbl);
                    root.getChildren().remove(IPField);
                    root.getChildren().remove(connect);
                    root.getChildren().remove(usernameLbl);
                    root.getChildren().remove(usernameField);
                    root.getChildren().remove(messages);
                    root.getChildren().add(messages);
                    messages.setText("Connected to server with port number " +
                            portNum + " and IP address " + IPAddr + "\n");
                }
                else if (usernameApproved.equals("no")) {
                    usernameLbl.setText("Username is taken. Try a different name. it's not working");
                    usernameApproved = "";
                }
            }
            catch (Exception E){
                E.printStackTrace();
            }


        };

        // event handler for client's username text field
        usernameField.setOnAction(usernameFieldEvent);
        // helper button used for username error checking
        helperBtn.setOnAction(usernameFieldEvent);
        return root;
    }

    // main method
    public static void main(String[] args) { launch(args); }

    // opens the GUI window
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Welcome to Triva Game!");
        root = new Scene(createContent());
        sceneMap.put("root", root);
        primaryStage.setScene(sceneMap.get("root"));
        primaryStage.show();

    }

    // starts the client connection
    @Override
    public void init() throws Exception{
        if(conn != null) {
            conn.startConn();
        }
    }

    //stops the client connection
    @Override
    public void stop() throws Exception{
        if(conn != null)
            conn.closeConn();
        Platform.exit();
    }

    // creates and returns a client socket connection
    private Client createClient() {
        return new Client(IPAddr, portNum, data -> {
            Platform.runLater(()->{
                // get the string value of data
                String input = data.toString();
                System.out.println("Server: " + input + "\n");

                // server approved player username
                if(input.equals("Username approved")){
                    usernameApproved = "yes";
                    helperBtn.fire();
                }

                // server did not approve player username
                else if(input.equals("Username not approved")){
                    usernameApproved = "no";
                    helperBtn.fire();
                }

                // server is notifying the player of other players online
                else if( input.length() >= 19 && input.substring(0, 19).equals("New player joined: ")){
                    input = input.substring(19, input.length());
                    numPlayersOnline++;
                }

            });
        });
    }

}
