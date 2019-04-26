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
import java.util.Collections;

public class FXNet extends Application{
    private NetworkConnection conn;
    private int portNum, questionNum, questionsAnswered = 0;
    private String ip;
    private int numPlayersOnline;
    private HashMap<String, Scene> sceneMap = new HashMap<String, Scene>();
    private Scene root;
    private TextField usernameField = new TextField();

    private HashMap<String, Integer> QtoQNum = new HashMap<String, Integer>();
    private ArrayList<Label> pictures = new ArrayList<Label>();
    private ArrayList<String> answerListArr;
    private Label answerPic = new Label();
    private Label answerText = new Label();
    private Label questionLbl = new Label();
    private VBox picBox = new VBox();
    private Button next = new Button("Next");
    private int score = 0;
    private TextArea gameBoard = new TextArea("Your score: "+ score + "\n");
    private Button exitBtn = new Button("Exit");

    private HashMap<String,ArrayList<String>> QtoAmap; // Question is the key and value is the multi choice answers in ArrayList. 0 index is correct answer
    private String correctAnswer; //store the correct answer to the current trivia question

    private ArrayList<Button> answerBtns = new ArrayList<Button>();
    private ArrayList<Integer> indices = new ArrayList<Integer>(3); // Initialize

    // variables to hold player and opponent game information
    private String usernameApproved = "";
    private String username;

    // declare and initialize needed GUI components
    private TextArea messages = new TextArea();
    private Button helperBtn = new Button();
    private HBox triviaBox = new HBox(20);


    // creates a GUI scene
    private Parent createContent() {
        // initialize the GUI components
        Button connect = new Button("Connect");
        connect.setTranslateX(245);
        messages.setPrefHeight(150);
        messages.setEditable(false);
        gameBoard.setPrefHeight(30);
        gameBoard.setEditable(false);

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

        exitBtn.setOnAction(event -> {
            try{
                conn.closeConn();
                Platform.exit();
            }
            catch (Exception e){
                System.out.println("Client failed to close connect");
                e.printStackTrace();
            }
        });

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
                ip = IPField.getText();
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
                QtoQNum = extractFile.getQuestionNum();
                QtoAmap = extractFile.getQtoAmap();
                extractFile.getAnswersTxt();
                answerListArr =extractFile.getAnswersArray();

                for(int i = 0; i<3; i++){
                    indices.add(i);
                }

            }
            catch(Exception e){
                portPromptLbl.setDisable(false);
                portField.setDisable(false);
                messages.setText("Could not create a client with port number "
                        + portNum + " and IP address " + ip + " Retry!");

            }
        });

        next.setOnAction(e->{
            if(questionsAnswered == 10){
                gameBoard.clear();
                gameBoard.setText("Calculating rank...\n");
                conn.send("final score: " + score);
                root.getChildren().removeAll(triviaBox, questionLbl, picBox, next);
            }
            else{
                conn.send("Send question");
                next.setDisable(true);
            }
        });

        // generic event handler to know which button the client pressed
        EventHandler<ActionEvent> answerBtn = event ->{
            disableBtns();
            Button b = (Button) event.getSource();
            String playerAnswer = b.getText();
            try{

                String check = "";
                if(playerAnswer.equals(correctAnswer)){
                    check = "Correct! ";
                    score++;
                    gameBoard.setText("Your score:"+ score);

                }
                else {
                    check = "Wrong! ";
                }
                next.setVisible(true);
                answerPic.setGraphic(pictures.get(questionNum-1).getGraphic());
                answerText.setText(check + answerListArr.get(questionNum-1));
                picBox.setVisible(true);
            }
            catch (Exception e){
                System.out.println("Something went wrong");
                e.printStackTrace();
            }
        };

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

                    //initialize the buttons for the answers for the first time
                    for(int i = 0; i < 3; i++){
                        Button b = new Button();
                        answerBtns.add(b);
                    }
                    // add game btns in a HBox and set on action with the generic button event handler
                    for(int i = 0; i < 3; i++){
                        answerBtns.get(i).setOnAction(answerBtn);
                        triviaBox.getChildren().add(answerBtns.get(i));
                    }
                    // remove GUI contents that are not required anymore and add required ones
                    root.getChildren().removeAll(portPromptLbl, portField, IPPromptLbl, IPField, connect, usernameLbl, usernameField);
                    root.getChildren().add(messages);

                    messages.setText("Connected to server with port number " +
                            portNum + " and IP address " + ip + "\n");
                    messages.appendText("To play this trivia quiz you will answer a set of 10 questions.\n");
                    messages.appendText("All players will be ranked once every player answers all 10 question.\n");
                    messages.appendText("Current players: \n");
                    root.getChildren().addAll(gameBoard, questionLbl, triviaBox, picBox, next,exitBtn);

                    exitBtn.setVisible(false);
                    gameBoard.setVisible(false);
                    triviaBox.setVisible(false);
                    picBox.setVisible(false);
                    next.setVisible(false);
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

    //This function will set the answer buttons to the correct multiple choice answers
    private void setButtonTxt(ArrayList<String> answers){
        Collections.shuffle(indices);  //shuffle the indices so the answers are in a different order for every question
        for(int i = 0; i <3; i++ ){
            answerBtns.get(i).setText(answers.get(indices.get(i)));  //set the text for each button
        }
    }

    // populates an array with label pictures that will be displayed along with the correct answer for each question
    private void assignPictures() {
        String jpg = "qn.jpg";
        int numQuestions = QtoAmap.size();
        for (int i=1; i<=numQuestions; i++) {
            String imgNum = Integer.toString(i);
            jpg = jpg.replace("n", imgNum);
            Image pic = new Image(jpg);
            ImageView v = new ImageView(pic);
            v.setFitHeight(250);
            v.setFitWidth(250);
            v.setPreserveRatio(true);
            Label picture = new Label();
            picture.setGraphic(v);
            jpg = jpg.replace(imgNum, "n");
            pictures.add(picture);
        }
    }

    // main method
    public static void main(String[] args) { launch(args); }

    // opens the GUI window
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Welcome to Triva Game!");
        root = new Scene(createContent());
        picBox.getChildren().addAll(answerPic, answerText);
        picBox.setAlignment(Pos.CENTER);
        picBox.setSpacing(20);
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

    private void enableBtns(){
        for(int i = 0; i < 3; i++){
            triviaBox.getChildren().get(i).setDisable(false);
        }
    }

    private void disableBtns(){
        for(int i = 0; i < 3; i++){
            triviaBox.getChildren().get(i).setDisable(true);
        }
    }

    // creates and returns a client socket connection
    private Client createClient() {
        return new Client(ip, portNum, data -> {
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
                    input = input.substring(19);
                    messages.appendText(input +" has joined the game.\n");
                    numPlayersOnline++;
                }

                // begin the game
                else if (input.equals("Start game")) {
                    assignPictures();   // create an array of pictures for each answer
                    messages.appendText(numPlayersOnline + " other players have joined. Begin the game!\n");
                    gameBoard.setVisible(true);
                    conn.send("Send question");
                }

                // receive a new question from the server
                else if (input.length() > 10 && input.contains("Question: ")) {
                    enableBtns();
                    picBox.setVisible(false);
                    next.setVisible(false);
                    next.setDisable(false);
                    input=input.substring(10);

                    System.out.println("QUESTION RECEIVED: " + input);
                    correctAnswer = QtoAmap.get(input).get(0);  //record the correct answer to check if the client answered correctly later
                    setButtonTxt(QtoAmap.get(input));       //gets array from hashamp
                    questionLbl.setText("Q" + (++questionsAnswered) + ": " + input);
                    triviaBox.setVisible(true);                 //set the box question buttons visible once receiving a question for the first time
                    System.out.println("CORRECT ANSWER: " + correctAnswer);
                    questionNum = QtoQNum.get(input);        // use the string to return the question number

                }
                
                //Check if the score and rank has been calculated
                else if(input.contains("Score: ") && input.contains("Rank: ")){
                    if(input.contains(username)){
                        input = input.replace(username, "Your");
                    }
                    if(gameBoard.getText().equals("Calculating rank...\n")){
                        gameBoard.setPrefHeight(100);
                        gameBoard.clear();
                    }
                    gameBoard.appendText(input);
                    exitBtn.setVisible(true);
                }
                
                //if another player quits the game, then prompt the user to exit the window
                else if(input.equals("Client disconnected")){
                    disableBtns();
                    next.setDisable(true);
                    gameBoard.setText("A player has left the game, please exit window.");
                }
            });
        });
    }
}
