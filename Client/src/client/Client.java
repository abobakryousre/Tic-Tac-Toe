/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import libs.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import libs.*;
import org.json.simple.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author hebaa
 */
public class Client extends Application implements Serializable {
    
    
    /*
    ***************** multi mode game functino*********************************
    */
    
    private Game forwardedGameRequest;
    private Game gameRequest;
    private boolean isMySymbolReceved = false;
    private  volatile boolean isGameRunning = true;
    private String currentMove; 
    private static String[] recordedPositions;
    
    Game gameMessages;
    Game myGame;
    String mySymbol;
    
    
    private boolean recordGameFlag;
    private Thread gamethread;
    
    private void forwardGameRequest(JSONObject obj){
        forwardedGameRequest = convert.fromJsonToGame(obj);
    }
    
    private Game getforwardGameRequest(){
        
        Game newGameRequest = forwardedGameRequest; // tmp varibale to save the request
        forwardedGameRequest = null;
        return newGameRequest;
        
    }
    
    private void receveMySymbol(){
        
        while(!isMySymbolReceved){
            
            gameRequest = getforwardGameRequest();
            
            if(gameRequest != null){
                
                isMySymbolReceved = true;
                mySymbol = gameRequest.getNextMove();
                isGameRunning = true;
                System.out.println("reciving game symbol " + mySymbol + " from gmae handler");
                System.out.println("and this is my  symbol " + currentMove );
                gamethread.start();
                System.out.println("game Thread started");
            }
        }
    }
    
    private void startGame(){
        
        sendGameMove(Request.START_GAME);
        receveMySymbol();
        
        gamethread = new Thread(new Runnable() {
            @Override
            public void run() {
                
                while (isGameRunning) {
                    
                    gameRequest = getforwardGameRequest();
            
                    if(gameRequest != null){
                        
                        Platform.runLater(() -> {
                        receiveGameMove(gameRequest);
                        });
                    }
                }
            }
        });
        
    }
    
    
    private String checkWinner(){
        String winner = "";
        List topRow = Arrays.asList(1,2,3);
        List midRow = Arrays.asList(4,5,6);
        List botRow = Arrays.asList(7,8,9);
        
        List liftCol = Arrays.asList(1,4,7);
        List midtCol = Arrays.asList(2,5,8);
        List righCol = Arrays.asList(3,6,9);
        
        List cross1 = Arrays.asList(1,5,9);
        List cross2 = Arrays.asList(3,5,7);
        
        List<List> winning = new ArrayList<List>();
        
        winning.add(topRow);
        winning.add(midRow);
        winning.add(botRow);
        winning.add(liftCol);
        winning.add(midtCol);
        winning.add(righCol);
        winning.add(cross1);
        winning.add(cross2);
        
        for(List winningComp: winning){
           
            if(playerXpositions.containsAll(winningComp)){
                winner = Game.X_MOVE;
                 playerXpositions.clear();
                 playerXpositions.addAll(winningComp);
            }
            else if(playerOpositions.containsAll(winningComp)){
                winner = Game.O_MOVE;
                 playerOpositions.clear();
                 playerOpositions.addAll(winningComp);
            }
            else if(playerXpositions.size() + playerOpositions.size() == 9)
                winner =  Game.DRAW;
        }
        return winner;
    }
    
    private void highLightWinner(ArrayList<Integer> winner){
        // will take the winner position and highlight each button equl to this position
        
        for(int i=0; i<9; i++){
            if( i+1 == (int) winner.get(0) || i+1 == (int) winner.get(1) || i+1 == (int) winner.get(2))
                buttons[i].setStyle("-fx-background-color: #90EE90");
        }         
    }
    
    private void recordPositions(){
        
        recordedPositions = new String[9];
        // save the value of each buttons of the 9'th, X or O or bull
        for(int index = 0; index <9; index++){
            recordedPositions[index] = buttons[index].getText();
        }
    }
    
    private void drawOldPositions(){
        
         for(int index = 0; index <9; index++){
             
            if( recordedPositions[index].equalsIgnoreCase(Game.X_MOVE) || 
                recordedPositions[index].equalsIgnoreCase(Game.O_MOVE)){
                
                
                
                
                if(recordedPositions[index] == "X"){
                    
                    buttons[index].setTextFill(javafx.scene.paint.Color.rgb(255, 157, 10));
                    buttons[index].setText(recordedPositions[index]);
                    buttons[index].setDisable(true);
                    playerXpositions.add(index+1);
                    
                }else{
                    buttons[index].setTextFill(javafx.scene.paint.Color.rgb(255, 157, 10));
                    buttons[index].setText(recordedPositions[index]);
                buttons[index].setDisable(true);
                    playerOpositions.add(index+1);
                }
                    
            }
        }
        //change the current move to the right turn
        // the defualt move at the begining is x,
        // so we need to check if it correct or no
        if(playerXpositions.size() > playerOpositions.size()){
            currentMove = Game.O_MOVE;
            
        }
    }
    
    private void resetBoard(){
        
        currentMove = Game.X_MOVE;
        playerXpositions.clear();
        playerOpositions.clear();
        recordGame.setText("Record Game");
        recordGameFlag = true;
        
        Button tmpButton = new Button();
        for(Button btn: buttons){
            
            // reset the old style
            btn.setStyle(tmpButton.getStyle());
            btn.setFont(new Font("Engravers MT", 36.0));
            btn.setText(null);
            btn.setDisable(false); 
            
        }
    }
    
    private void playAgainMultiMode(){
        
        sendGameMove(Request.GAME_PLAYAGAIN);
    }
    
    private void updateScoreIndataBase(){
        
        this.recordGame.setText("Play Again!");
        this.recordGameFlag = false;

    }
    
    private void drawTie(){
        
        
        for(Button btn: buttons){
            btn.setText(Game.DRAW);
            btn.setDisable(true);
        }
        
    }
    
    private void drawXO(){
        for(String str: recordedPositions){
            System.out.println(str);
        }
    }
    
   
    private void updateBoard(Long buttonsPosition){
        
        System.out.println("is  game turn " + currentMove + " == " + mySymbol);
                
                if(mySymbol.equalsIgnoreCase(currentMove)){
                    buttons[buttonsPosition.intValue()-1].setDisable(true);
                    
                    if(currentMove.equalsIgnoreCase(Game.X_MOVE)){
                        
                        
                        if(checkWinner() == Game.X_MOVE){
                            myGame.setWinner(Game.X_MOVE);
                            myGame.setPlayedMove(currentMove);
                            myGame.setNextMove(Game.GAME_OVER); 
                            myGame.setPosition(buttonsPosition);
                            sendGameMove(Request.GAME_MOVE);
                            
                        }else if(checkWinner() == Game.DRAW){
                            drawTie();
                            myGame.setPlayedMove(currentMove);
                            myGame.setPosition(buttonsPosition);
                            sendGameMove(Request.GAME_MOVE);
                            
                        }else{                        
                            myGame.setPlayedMove(currentMove); 
                            myGame.setPosition(buttonsPosition);  
                            myGame.setNextMove(Game.O_MOVE); 
                            sendGameMove(Request.GAME_MOVE);
                        }
                    }
                    else{
                        
                        if(checkWinner().equalsIgnoreCase(Game.O_MOVE)){
                            myGame.setWinner(Game.O_MOVE);
                            myGame.setPlayedMove(currentMove);
                            myGame.setNextMove(Game.GAME_OVER);
                            myGame.setPosition(buttonsPosition);
                            sendGameMove(Request.GAME_MOVE);
                        }
                        else if(checkWinner().equalsIgnoreCase(Game.DRAW)){
                            drawTie();
                            myGame.setPlayedMove(currentMove);
                            myGame.setPosition(buttonsPosition);
                            sendGameMove(Request.GAME_MOVE);
                        }else{
                            myGame.setPlayedMove(currentMove);
                            myGame.setPosition(buttonsPosition);
                            myGame.setNextMove(Game.X_MOVE);
                            sendGameMove(Request.GAME_MOVE);
                        }
                        
                        
                    }   
                }
    }
    
    private void sendGameMessage(String gameMessage){
        
        gameMessages.setRequest(gameMessage);
        obj = convert.fromGameToJson(gameMessages);
        this.outStream.println(obj.toString());
    }
    
    private void sendGameMove(String gameRequest){
        
        myGame.setRequest(gameRequest);
        JSONObject obj = convert.fromGameToJson(myGame);
        System.out.println("Sending move to handlers " + "+ chat message: ");
        this.outStream.println(obj.toString());
    }
    
    private void receiveGameMove(Game gameMove){
        
        
        if(gameMove.getRequest().equalsIgnoreCase(Request.Chat_Message)){
            gameMessages = gameMove;
            appendMessage();
        }
        else{
            
            myGame = gameMove;
            
            switch(myGame.getRequest()){
                
                case Request.RECORD_GAME:
                    checkRecordGameRespond();
                    break;
                case Request.GET_RECORDEDGAME:
                    drawOldPositions();
                    break;
                case Request.GAME_PLAYAGAIN:
                    resetBoard();
                    break;
                    
                default:
                    
                    drawAndSaveMove(myGame.getPosition(), myGame.getPlayedMove());
                    currentMove = myGame.getNextMove();
            
                    if(checkWinner().equalsIgnoreCase(Game.X_MOVE)){
                        highLightWinner(playerXpositions);
                        updateScore();
                    }else if(checkWinner().equalsIgnoreCase(Game.O_MOVE)){
                        highLightWinner(playerOpositions);
                        updateScore();
                    }else if(checkWinner().equalsIgnoreCase(Game.DRAW)){
                        drawTie();
                        updateScore();
                    }
                break;
            }
           
            
                
            
        }
            
            
            
            
        }
    
    private void drawAndSaveMove(Long position,String playedMove){
        
        
        
        if(playedMove.equalsIgnoreCase(Game.X_MOVE)){
            
            buttons[position.intValue()-1].setTextFill(javafx.scene.paint.Color.rgb(5, 112, 255));
            buttons[position.intValue()-1].setText(playedMove);
            buttons[position.intValue()-1].setDisable(true);
            
            System.out.println("adding position in x " + position.intValue());
            playerXpositions.add(position.intValue());
            
        }else{
            
            buttons[position.intValue()-1].setTextFill(javafx.scene.paint.Color.rgb(255, 88, 66));
            buttons[position.intValue()-1].setText(playedMove);
            buttons[position.intValue()-1].setDisable(true);
            
            System.out.println("adding position in o " + position.intValue());
            playerOpositions.add(position.intValue());
        }
        
        
        
    }
    
    private void sendChat(){
        
        gameMessages = new Game();
        gameMessages.setMessage(messageField.getText());
        sendGameMessage(Request.Chat_Message);
        messageField.clear();
        
    }
    
    private void appendMessage(){
        
        if(!gameMessages.getMessage().equalsIgnoreCase(null)){
            chatArea.appendText(gameMessages.getMessage() + "\n");
        }
    }
    
    public static void setRecordedPosition(String[] oldGame){
        recordedPositions = oldGame;
    }
    
    public static String[] getRecordedPosition(){
        
        return recordedPositions;
    }
    
    private void sendRecordedPosition(){
        recordPositions();
        sendGamePosition(Request.RECORD_GAME);
    }
    
    private void sendGamePosition(String request){
        
        JSONArray positions = new JSONArray();
        positions = convert.fromRecordedGamePositionTOJsonArray(recordedPositions);
        myGame.setRequest(request);
        obj = convert.fromGameToJsonWithArray(myGame, positions);
        this.outStream.println(obj.toString());
    }
    
    private void checkRecordGameRespond(){
        System.out.println(myGame.getRespond());
        if(myGame.getRespond().equalsIgnoreCase(Respond.SUCCESS)){
            System.out.println("Record game success");
            drawXO();
        }else if(myGame.getRespond().equalsIgnoreCase(Respond.FAILURE)){
            System.out.println("Record game faliure");
        }
    }
    
    private void sendExitGameRequest(){
        
        sendGameMove(Request.END_GAME);
        isGameRunning = false;
        gameRequest = null;
        gamethread.stop();
        
        
                    
    }

    /**
     * **********************************socket**********************************************************************
     */
    Socket socket;
    ObjectInputStream readObj;
    ObjectOutputStream writeObj;
    DataInputStream inStream;
    PrintStream outStream;
    JsonConverter convert;
    JSONObject obj;
    JSONParser parse;
    Thread thread;
    
    /**
     * **************************Player
     * class*****************************************************************************
     */
    Player p;
    /**
     * ****************************Single Mode
     * Game*********************************************************************************
     */
    int count = 0;
//    SingleModeGame single ;
    /**
     * ***********************************Alert*************************************************************************
     */
    Alert alertEmptyLogIn1 = new Alert(Alert.AlertType.ERROR);
    Alert alertWrongLogIn1 = new Alert(Alert.AlertType.ERROR);
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    Alert alertUserExists = new Alert(Alert.AlertType.ERROR);
    Alert alertEmptySignUp1 = new Alert(Alert.AlertType.ERROR);
    Alert alertWrongLogout1;
    Alert unSelected4 = new Alert(Alert.AlertType.ERROR);
    /**
     * *******************************AnchorPane
     * Screens**********************************************************************************
     */
    AnchorPane ScreenOne;
    AnchorPane ScreenTwo;
    AnchorPane ScreenThree;
    AnchorPane ScreenFour;
    AnchorPane ScreenSingleMode;
    AnchorPane ScreenMultiMode;
    /**
     * *********************************Screen one
     * variables*********************************************************************
     */
    public ImageView LoginImg;
    public Label LoginLabel;
    public Label usrName1;
    public TextField userText1;
    public Label pass1;
    public TextField passText1;
    public Button logInBtn1;
    public Button signupBtn1;
    String userName3;
    Text usernameDislay3 = new Text();
    ImageView imageView;
    InputStream stream;
    /**
     * *************************************Screen two
     * variables*******************************************************************
     */
    public GridPane GridOfImageAndForm;
    public ColumnConstraints columnConstraints;
    public ColumnConstraints columnConstraints0;
    public RowConstraints rowConstraints;
    public ImageView SignUpImg;
    public AnchorPane SignUpFormPanel;
    public Label FirstName2;
    public TextField userText2;
    public Label usrName2;
    public Label label;
    public TextField FirstText2;
    public Label LastName2;
    public TextField LastText2;
    public Label pass2;
    public PasswordField passText2;
    public Button signupBtn2;
    public Button backBtn2;
    /**
     * ***************************************Screen three
     * variables******************************************************************
     */
    public GridPane GridOfPlay;
    public ColumnConstraints tableColumnConstraints;
    public RowConstraints tableRowConstraints;
    public AnchorPane PlayPanel;
    public ImageView PlayImg;
    public Label PlayLable;
    public Button singleBtn3;
    public Button multiBtn3;
    public Button logOutBtn3;
    /**
     * *****************************************Screen four
     * variables****************************************************************************
     */
    ImageView TableImg;
    TableView PlayerTable;
    TableColumn PlayerName;
    TableColumn Score;
    TableColumn Status;
    Label TableLabel;
    Button playBtn4;
    Button backBtn4;
    /**
     * *********************************************ScreenSingleMode*****************************************************************
     */
    ImageView GameImg;
    Button exit;
    Button recordGame;
    Button bt1;
    Button bt2;
    Button bt3;
    Button bt4;
    Button bt5;
    Button bt6;
    Button bt7;
    Button bt8;
    Button bt9;

    ArrayList<Integer> playerXpositions = new ArrayList<Integer>();
    ArrayList<Integer> playerOpositions = new ArrayList<Integer>();
    Label playerName;
    Label computerName;
    Label playerScore;
    Label computerScore;
    Label scoreSeperator;//remove
    Button[] buttons;
//    String[] recordedPositions;
    String symbol;
    boolean gameFlag;
    int move;
    int playerScoreCounter;
    int computerScoreCounter;
    int index;

    private static String username;
    /**
     * ******************************************ScreenMultiMode*********************************************************************
     */

    ImageView MultiGameImage;
    TextArea chatArea;
    TextField messageField;
    Button sendMsg;
    Button btn1;
    Button btn2;
    Button btn3;
    Button btn4;
    Button btn5;
    Button btn6;
    Button btn7;
    Button btn8;
    Button btn9;
    Button exitGame;
    Label playerOneName;
    Label playerTwoName;
    Label playerO;
    Label currentTurn;
    Label playerX;
    Label playerOneScore;
    Label playerTwoScore;
    Label scoreSeperator2;
    Button recordGame2;

    /**
     * ***********************************************************************************************************
     */
    @Override
    public void init() {
        p = new Player("", "");
        Game game = new Game();
        convert = new JsonConverter();
        alertWrongLogIn1.setTitle("LogIn ");
        alertWrongLogIn1.setHeaderText(null);
        alertWrongLogIn1.setContentText("Invalid User Name or Password");

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("127.0.0.1", 5005);
                    inStream = new DataInputStream(socket.getInputStream());
                    outStream = new PrintStream(socket.getOutputStream());
//                    writeObj = new ObjectOutputStream(socket.getOutputStream());
//                    readObj = new ObjectInputStream(socket.getInputStream());                      
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
                System.out.println("hoooo");
                while (true) {
                    try {
                        //(JSONObject) parser.parse(inStream.readLine());
                        JSONParser parse = new JSONParser();
                        obj = new JSONObject();
                        obj = (JSONObject) parse.parse(inStream.readLine());
                        p = convert.fromJsonToPlayer((JSONObject) obj);
                        System.out.println("Line 196: " + p.getRespond());
                        messageHandelr(p);
                        System.out.println("Line 98: " + p.getRespond());
                        System.out.println("Line 196: " + p.getUsername() + p.getPassword());
                    } catch (IOException ex) {
                        System.out.println("Line 100: " + p.getRespond());
                        System.out.println(ex.getMessage());
                    } catch (NullPointerException ex) {
                        System.out.println("You should run server first Line 111: " + p.getRespond());
                        System.out.println(ex.getMessage());
                    } catch (ParseException ex) {
//                        Logger.getLogger(TicTacToe.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        thread.start();

    }

    /**
     * **********************************Screens methods*******************************************************************************
     */
    public AnchorPane ScreenOne() {
        ScreenOne = new AnchorPane();
        LoginImg = new ImageView();
        LoginLabel = new Label();
        usrName1 = new Label();
        userText1 = new TextField();
        pass1 = new Label();
        passText1 = new PasswordField();
        logInBtn1 = new Button();
        signupBtn1 = new Button();

        ScreenOne.setMaxHeight(USE_PREF_SIZE);
        ScreenOne.setMaxWidth(USE_PREF_SIZE);
        ScreenOne.setMinHeight(USE_PREF_SIZE);
        ScreenOne.setMinWidth(USE_PREF_SIZE);
        ScreenOne.setPrefHeight(540.0);
        ScreenOne.setPrefWidth(642.0);
        ScreenOne.setStyle("-fx-background-color: linear-gradient(to right, #5c258d, #4389a2);;");

        LoginImg.setFitHeight(540.0);
        LoginImg.setFitWidth(307.0);
        LoginImg.setPickOnBounds(true);

        try {
            FileInputStream stream = new FileInputStream("ProjectImg/login.jpg");
            Image image = new Image(stream);
            LoginImg.setImage(image);

        } catch (FileNotFoundException ex) {

            System.out.println("Faild to load login image");
        }

        LoginLabel.setAlignment(javafx.geometry.Pos.CENTER);
        LoginLabel.setLayoutX(387.0);
        LoginLabel.setLayoutY(78.0);
        LoginLabel.setPrefHeight(17.0);
        LoginLabel.setPrefWidth(204.0);
        LoginLabel.setText("Login");
        LoginLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        LoginLabel.setFont(new Font("Javanese Text", 24.0));

        usrName1.setLayoutX(315.0);
        usrName1.setLayoutY(164.0);
        usrName1.setPrefHeight(51.0);
        usrName1.setPrefWidth(77.0);
        usrName1.setText("User Name");
        usrName1.setTextFill(javafx.scene.paint.Color.valueOf("#fffefe"));
        usrName1.setFont(new Font("Javanese Text", 15.0));

        userText1.setLayoutX(411.0);
        userText1.setLayoutY(177.0);
        userText1.setPrefHeight(34.0);
        userText1.setPrefWidth(213.0);

        pass1.setLayoutX(315.0);
        pass1.setLayoutY(256.0);
        pass1.setPrefHeight(51.0);
        pass1.setPrefWidth(77.0);
        pass1.setText("Password");
        pass1.setTextFill(javafx.scene.paint.Color.valueOf("#fffefe"));
        pass1.setFont(new Font("Javanese Text", 15.0));

        passText1.setAccessibleRole(javafx.scene.AccessibleRole.PASSWORD_FIELD);
        passText1.setLayoutX(411.0);
        passText1.setLayoutY(264.0);
        passText1.setPrefHeight(34.0);
        passText1.setPrefWidth(213.0);

        logInBtn1.setLayoutX(411.0);
        logInBtn1.setLayoutY(361.0);
        logInBtn1.setMnemonicParsing(false);
        logInBtn1.setPrefHeight(53.0);
        logInBtn1.setPrefWidth(213.0);
        logInBtn1.setStyle("-fx-background-color: linear-gradient(to right, #283048, #859398);;");
        logInBtn1.setText("Login");
        logInBtn1.setTextFill(javafx.scene.paint.Color.WHITE);
        logInBtn1.setFont(new Font("Lucida Calligraphy Italic", 18.0));

        signupBtn1.setLayoutX(410.0);
        signupBtn1.setLayoutY(432.0);
        signupBtn1.setMnemonicParsing(false);
        signupBtn1.setPrefHeight(53.0);
        signupBtn1.setPrefWidth(213.0);
        signupBtn1.setText("Sign up");
        signupBtn1.setFont(new Font("Lucida Calligraphy Italic", 18.0));

        ScreenOne.getChildren().add(LoginImg);
        ScreenOne.getChildren().add(LoginLabel);
        ScreenOne.getChildren().add(usrName1);
        ScreenOne.getChildren().add(userText1);
        ScreenOne.getChildren().add(pass1);
        ScreenOne.getChildren().add(passText1);
        ScreenOne.getChildren().add(logInBtn1);
        ScreenOne.getChildren().add(signupBtn1);

        userText1.clear();
        passText1.clear();
        alertEmptyLogIn1.setTitle("sign in ");
        alertEmptyLogIn1.setHeaderText(null);
        alertEmptyLogIn1.setContentText("All Fields are Required");

        /**
         * ***********Screen One Button Action*******************
         */
        signupBtn1.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                ScreenTwo().getChildren().clear();

                signupBtn1.getScene().setRoot(ScreenTwo());

            }

        });

        logInBtn1.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                if (userText1.getText().isEmpty() || passText1.getText().isEmpty()) {
                    alertEmptyLogIn1.showAndWait();
                } else {
                    Player newPlayer = new Player(userText1.getText(), passText1.getText());
                    setPlayerName(userText1.getText());

                    convert = new JsonConverter();
                    JSONObject obj = new JSONObject();
                    obj = convert.fromPlayerToJson(newPlayer);
                    outStream.println(obj.toString());
                    System.out.println("object sent!");
                }

            }
        });

        return ScreenOne;

    }

    public String getusername() {
        System.out.println(userName3);
        return userText1.getText();

    }
    
    public AnchorPane ScreenTwo() {
        ScreenTwo = new AnchorPane();
        GridOfImageAndForm = new GridPane();
        columnConstraints = new ColumnConstraints();
        columnConstraints0 = new ColumnConstraints();
        rowConstraints = new RowConstraints();
        SignUpImg = new ImageView();
        SignUpFormPanel = new AnchorPane();
        FirstName2 = new Label();
        userText2 = new TextField();
        usrName2 = new Label();
        label = new Label();
        FirstText2 = new TextField();
        LastName2 = new Label();
        LastText2 = new TextField();
        pass2 = new Label();
        passText2 = new PasswordField();
        signupBtn2 = new Button();
        backBtn2 = new Button();

        ScreenTwo.setMaxHeight(USE_PREF_SIZE);
        ScreenTwo.setMaxWidth(USE_PREF_SIZE);
        ScreenTwo.setMinHeight(USE_PREF_SIZE);
        ScreenTwo.setMinWidth(USE_PREF_SIZE);
        ScreenTwo.setPrefHeight(540.0);
        ScreenTwo.setPrefWidth(642.0);

        GridOfImageAndForm.setPrefHeight(540.0);
        GridOfImageAndForm.setPrefWidth(642.0);
        GridOfImageAndForm.setStyle("-fx-background-color: linear-gradient(to right, #5c258d, #4389a2);;");

        columnConstraints.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        columnConstraints.setMinWidth(10.0);
        columnConstraints.setPrefWidth(100.0);

        columnConstraints0.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        columnConstraints0.setMinWidth(10.0);
        columnConstraints0.setPrefWidth(100.0);

        rowConstraints.setMinHeight(10.0);
        rowConstraints.setPrefHeight(30.0);
        rowConstraints.setVgrow(javafx.scene.layout.Priority.SOMETIMES);

        SignUpImg.setFitHeight(538.0);
        SignUpImg.setFitWidth(321.0);
        SignUpImg.setPickOnBounds(true);
        try {
            FileInputStream stream = new FileInputStream("ProjectImg/registration.jpg");
            Image image = new Image(stream);
            SignUpImg.setImage(image);

        } catch (FileNotFoundException ex) {
            System.out.println("Faild load signup image");
        }
        GridPane.setColumnIndex(SignUpFormPanel, 1);
        SignUpFormPanel.setPrefHeight(200.0);
        SignUpFormPanel.setPrefWidth(200.0);

        FirstName2.setLayoutX(4.0);
        FirstName2.setLayoutY(181.0);
        FirstName2.setPrefHeight(51.0);
        FirstName2.setPrefWidth(77.0);
        FirstName2.setText("First Name");
        FirstName2.setTextFill(javafx.scene.paint.Color.valueOf("#fffefe"));
        FirstName2.setFont(new Font("Javanese Text", 15.0));

        userText2.setLayoutX(94.0);
        userText2.setLayoutY(127.0);
        userText2.setPrefHeight(34.0);
        userText2.setPrefWidth(213.0);

        usrName2.setLayoutX(4.0);
        usrName2.setLayoutY(118.0);
        usrName2.setPrefHeight(51.0);
        usrName2.setPrefWidth(77.0);
        usrName2.setText("User Name");
        usrName2.setTextFill(javafx.scene.paint.Color.valueOf("#fffefe"));
        usrName2.setFont(new Font("Javanese Text", 15.0));

        label.setAlignment(javafx.geometry.Pos.CENTER);
        label.setLayoutX(23.0);
        label.setLayoutY(46.0);
        label.setPrefHeight(51.0);
        label.setPrefWidth(287.0);
        label.setText("SIGN UP");
        label.setTextFill(javafx.scene.paint.Color.WHITE);
        label.setFont(new Font("Javanese Text", 24.0));

        FirstText2.setLayoutX(94.0);
        FirstText2.setLayoutY(190.0);
        FirstText2.setPrefHeight(34.0);
        FirstText2.setPrefWidth(213.0);

        LastName2.setLayoutX(4.0);
        LastName2.setLayoutY(245.0);
        LastName2.setPrefHeight(51.0);
        LastName2.setPrefWidth(77.0);
        LastName2.setText("Last Name");
        LastName2.setTextFill(javafx.scene.paint.Color.valueOf("#fffefe"));
        LastName2.setFont(new Font("Javanese Text", 15.0));

        LastText2.setLayoutX(94.0);
        LastText2.setLayoutY(254.0);
        LastText2.setPrefHeight(34.0);
        LastText2.setPrefWidth(213.0);

        pass2.setLayoutX(4.0);
        pass2.setLayoutY(307.0);
        pass2.setPrefHeight(51.0);
        pass2.setPrefWidth(77.0);
        pass2.setText("Password");
        pass2.setTextFill(javafx.scene.paint.Color.valueOf("#fffefe"));
        pass2.setFont(new Font("Javanese Text", 15.0));

        passText2.setLayoutX(92.0);
        passText2.setLayoutY(311.0);
        passText2.setPrefHeight(34.0);
        passText2.setPrefWidth(213.0);

        signupBtn2.setLayoutX(92.0);
        signupBtn2.setLayoutY(383.0);
        signupBtn2.setMnemonicParsing(false);
        signupBtn2.setPrefHeight(53.0);
        signupBtn2.setPrefWidth(173.0);
        signupBtn2.setStyle("-fx-background-color: linear-gradient(to right, #283048, #859398);;");
        signupBtn2.setText("sign up");
        signupBtn2.setTextFill(javafx.scene.paint.Color.WHITE);
        signupBtn2.setFont(new Font("Lucida Calligraphy Italic", 18.0));

        backBtn2.setLayoutX(94.0);
        backBtn2.setLayoutY(452.0);
        backBtn2.setMnemonicParsing(false);
        backBtn2.setPrefHeight(53.0);
        backBtn2.setPrefWidth(173.0);
        backBtn2.setText("Back");
        backBtn2.setFont(new Font("Lucida Calligraphy Italic", 18.0));

        GridOfImageAndForm.getColumnConstraints().add(columnConstraints);
        GridOfImageAndForm.getColumnConstraints().add(columnConstraints0);
        GridOfImageAndForm.getRowConstraints().add(rowConstraints);
        GridOfImageAndForm.getChildren().add(SignUpImg);
        SignUpFormPanel.getChildren().add(FirstName2);
        SignUpFormPanel.getChildren().add(userText2);
        SignUpFormPanel.getChildren().add(usrName2);
        SignUpFormPanel.getChildren().add(label);
        SignUpFormPanel.getChildren().add(FirstText2);
        SignUpFormPanel.getChildren().add(LastName2);
        SignUpFormPanel.getChildren().add(LastText2);
        SignUpFormPanel.getChildren().add(pass2);
        SignUpFormPanel.getChildren().add(passText2);
        SignUpFormPanel.getChildren().add(signupBtn2);
        SignUpFormPanel.getChildren().add(backBtn2);
        GridOfImageAndForm.getChildren().add(SignUpFormPanel);
        ScreenTwo.getChildren().add(GridOfImageAndForm);

        userText2.clear();
        FirstText2.clear();
        LastText2.clear();
        passText2.clear();
        alert.setTitle("SignUp");
        alert.setHeaderText(null);
        alert.setContentText("Your data saved successfully");

        alertEmptySignUp1.setTitle("signUp ");
        alertEmptySignUp1.setHeaderText(null);
        alertEmptySignUp1.setContentText("All fields are required");

        alertUserExists.setTitle("signUp ");
        alertUserExists.setHeaderText(null);
        alertUserExists.setContentText("User already exist choose another uername");

        signupBtn2.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (userText2.getText().isEmpty() || FirstText2.getText().isEmpty() || LastText2.getText().isEmpty() || passText2.getText().isEmpty()) {

                    alertEmptySignUp1.showAndWait();
                } else {

                    Player signUpPlayer = new Player(userText2.getText(), passText2.getText(), FirstText2.getText(), LastText2.getText());
                    outStream.println(convert.fromPlayerToJson(signUpPlayer).toString());
//                    try {
//                        writeObj.writeObject(signUpPlayer);
//                        System.out.println("New Player has sign up!");
//                    } catch (IOException ex) {
//                        System.out.println(ex.getMessage());
//                    }

                }

            }
        });
        backBtn2.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                ScreenOne().getChildren().clear();
                backBtn2.getScene().setRoot(ScreenOne());
            }

        });
        return ScreenTwo;
    }

    public AnchorPane ScreenThree() {
        ScreenThree = new AnchorPane();
        GridOfPlay = new GridPane();
        tableColumnConstraints = new ColumnConstraints();
        tableRowConstraints = new RowConstraints();
        PlayPanel = new AnchorPane();
        PlayImg = new ImageView();
        PlayLable = new Label();
        singleBtn3 = new Button();
        multiBtn3 = new Button();
        logOutBtn3 = new Button();

        ScreenThree.setMaxHeight(USE_PREF_SIZE);
        ScreenThree.setMaxWidth(USE_PREF_SIZE);
        ScreenThree.setMinHeight(USE_PREF_SIZE);
        ScreenThree.setMinWidth(USE_PREF_SIZE);
        ScreenThree.setPrefHeight(540.0);
        ScreenThree.setPrefWidth(642.0);

        GridOfPlay.setPrefHeight(540.0);
        GridOfPlay.setPrefWidth(642.0);

        tableColumnConstraints.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        tableColumnConstraints.setMinWidth(10.0);
        tableColumnConstraints.setPrefWidth(100.0);

        tableRowConstraints.setMinHeight(10.0);
        tableRowConstraints.setPrefHeight(30.0);
        tableRowConstraints.setVgrow(javafx.scene.layout.Priority.SOMETIMES);

        PlayPanel.setPrefHeight(200.0);
        PlayPanel.setPrefWidth(200.0);
        PlayPanel.setStyle("-fx-background-image: <?xml version='1.0' encoding='UTF-8'?><?import javafx.scene.control.Button?><?import javafx.scene.text.Font?><Button fx:id='signupBtn2' layoutX='226.0' layoutY='153.0' mnemonicParsing='false' prefHeight='53.0' prefWidth='173.0' style='-fx-background-color: linear-gradient(to right, #283048, #859398);;' text='Sign UP' textFill='WHITE' xmlns='http://javafx.com/javafx/8.0.171' xmlns:fx='http://javafx.com/fxml/1'>   <font>      <Font name='Lucida Calligraphy Italic' size='18.0' />   </font></Button>;");

        PlayImg.setFitHeight(540.0);
        PlayImg.setFitWidth(642.0);
        PlayImg.setPickOnBounds(true);
        try {
            FileInputStream stream = new FileInputStream("ProjectImg/rocket.jpg");
            Image image = new Image(stream);
            PlayImg.setImage(image);

        } catch (FileNotFoundException ex) {
            System.out.println("Fsild to load menu image");
        }

        PlayLable.setAlignment(javafx.geometry.Pos.CENTER);
        PlayLable.setLayoutX(145.0);
        PlayLable.setLayoutY(48.0);
        PlayLable.setPrefHeight(67.0);
        PlayLable.setPrefWidth(339.0);
        PlayLable.setText("Let's Play");
        PlayLable.setTextFill(javafx.scene.paint.Color.WHITE);
        PlayLable.setFont(new Font("Javanese Text", 36.0));

        singleBtn3.setLayoutX(226.0);
        singleBtn3.setLayoutY(153.0);
        singleBtn3.setMnemonicParsing(false);
        singleBtn3.setPrefHeight(53.0);
        singleBtn3.setPrefWidth(173.0);
        singleBtn3.setStyle("-fx-background-color: linear-gradient(to right, #283048, #859398);;");
        singleBtn3.setText("Signle Mode");
        singleBtn3.setTextFill(javafx.scene.paint.Color.WHITE);
        singleBtn3.setFont(new Font("Lucida Calligraphy Italic", 18.0));

        multiBtn3.setLayoutX(226.0);
        multiBtn3.setLayoutY(244.0);
        multiBtn3.setMnemonicParsing(false);
        multiBtn3.setPrefHeight(53.0);
        multiBtn3.setPrefWidth(173.0);
        multiBtn3.setText("Multi Mode");
        multiBtn3.setFont(new Font("Lucida Calligraphy Italic", 18.0));

        logOutBtn3.setLayoutX(226.0);
        logOutBtn3.setLayoutY(346.0);
        logOutBtn3.setMnemonicParsing(false);
        logOutBtn3.setPrefHeight(53.0);
        logOutBtn3.setPrefWidth(173.0);
        logOutBtn3.setStyle("-fx-background-color: linear-gradient(to right, #283048, #859398);;");
        logOutBtn3.setText("Logout");
        logOutBtn3.setTextFill(javafx.scene.paint.Color.WHITE);
        logOutBtn3.setFont(new Font("Lucida Calligraphy Italic", 18.0));

        GridOfPlay.getColumnConstraints().add(tableColumnConstraints);
        GridOfPlay.getRowConstraints().add(tableRowConstraints);
        PlayPanel.getChildren().add(PlayImg);
        PlayPanel.getChildren().add(PlayLable);
        PlayPanel.getChildren().add(singleBtn3);
        PlayPanel.getChildren().add(multiBtn3);
        PlayPanel.getChildren().add(logOutBtn3);
        GridOfPlay.getChildren().add(PlayPanel);
        ScreenThree.getChildren().add(GridOfPlay);

        logOutBtn3.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                Player logOutPlayer = new Player(userText1.getText(), passText1.getText());
                logOutPlayer.setRequest(Request.LOGOUT);
                System.out.println(logOutPlayer.getRequest());

                outStream.println(convert.fromPlayerToJson(logOutPlayer).toString());
                System.out.println("User send logout Request!");
            }

        });
        //to reuest to play with another player send playrqeuest to server --->sender
        //in case of success respond start game means to move to multimode screen (recieve respond on playrqeuest)then send another request to server (startgame)

        //reciver sent a request (answer) to the other player -->reciver
        //respond with another (answer) request holding success or falier 
        multiBtn3.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                ScreenFour().getChildren().clear();
                multiBtn3.getScene().setRoot(ScreenFour());

            }

        });

        singleBtn3.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                ScreenSingleMode().getChildren().clear();
                singleBtn3.getScene().setRoot(ScreenSingleMode());

            }

        });

        return ScreenThree;
    }

    public AnchorPane ScreenFour() {
        ScreenFour = new AnchorPane();
        TableImg = new ImageView();
//        PlayerTable = new TableView();
       TableView<Player> PlayerTable = new TableView<Player>();
//       ObservableList<Player> playerData = FXCollections.observableArrayList()
       
       

        PlayerName = new TableColumn();
        Score = new TableColumn();
        Status = new TableColumn();
        TableLabel = new Label();
        playBtn4 = new Button();
        backBtn4 = new Button();
        ScreenFour.setMaxHeight(USE_PREF_SIZE);
        ScreenFour.setMaxWidth(USE_PREF_SIZE);
        ScreenFour.setMinHeight(USE_PREF_SIZE);
        ScreenFour.setMinWidth(USE_PREF_SIZE);
        ScreenFour.setPrefHeight(540.0);
        ScreenFour.setPrefWidth(642.0);
//        ScreenFour.setStyle("-fx-background-color: linear-gradient(to right, #0f0c29, #302b63, #24243e);");

        TableImg.setFitHeight(540.0);
        TableImg.setFitWidth(642.0);
        TableImg.setPickOnBounds(true);
//        TableImg.setImage(new Image(getClass().getResource("../../../Project/Img/image.jpg").toExternalForm()));
        try {
            FileInputStream stream = new FileInputStream("ProjectImg/image.jpg");
            Image image = new Image(stream);
            TableImg.setImage(image);

        } catch (FileNotFoundException ex) {
//            Logger.getLogger(FXMLScreenOne.class.getName()).log(Level.SEVERE, null, ex);
        }

        PlayerTable.setLayoutX(89.0);
        PlayerTable.setLayoutY(109.0);
        PlayerTable.setPrefHeight(287.0);
        PlayerTable.setPrefWidth(480.0);

        PlayerName.setPrefWidth(178.0);
        PlayerName.setText("Player Name");

        Score.setPrefWidth(132.0);
        Score.setText("Score");

        Status.setPrefWidth(169.0);
        Status.setText("Status");

        TableLabel.setAlignment(javafx.geometry.Pos.CENTER);
        TableLabel.setLayoutX(167.0);
        TableLabel.setLayoutY(28.0);
        TableLabel.setPrefHeight(46.0);
        TableLabel.setPrefWidth(309.0);
        TableLabel.setText("Player List");
        TableLabel.setTextFill(javafx.scene.paint.Color.valueOf("#fffbfb"));
        TableLabel.setFont(new Font("Javanese Text", 24.0));
        playBtn4.setAlignment(javafx.geometry.Pos.CENTER);
        playBtn4.setLayoutX(379.0);
        playBtn4.setLayoutY(456.0);
        playBtn4.setMnemonicParsing(false);
        playBtn4.setPrefHeight(44.0);
        playBtn4.setPrefWidth(173.0);
        playBtn4.setStyle("-fx-background-color: linear-gradient(to right, #000428, #004e92);;");
        playBtn4.setText("Play");
        playBtn4.setTextFill(javafx.scene.paint.Color.valueOf("#fffdfd"));
        playBtn4.setFont(new Font("Lucida Calligraphy Italic", 20.0));

        backBtn4.setAlignment(javafx.geometry.Pos.CENTER);
        backBtn4.setLayoutX(98.0);
        backBtn4.setLayoutY(456.0);
        backBtn4.setMnemonicParsing(false);
        backBtn4.setPrefHeight(44.0);
        backBtn4.setPrefWidth(173.0);
        backBtn4.setStyle("-fx-background-color: linear-gradient(to right, #000428, #004e92);;");
        backBtn4.setText("Back");
        backBtn4.setTextFill(javafx.scene.paint.Color.valueOf("#fffdfd"));
        backBtn4.setFont(new Font("Lucida Calligraphy Italic", 20.0));

        ScreenFour.getChildren().add(TableImg);
        PlayerTable.getColumns().add(PlayerName);
        PlayerTable.getColumns().add(Score);
        PlayerTable.getColumns().add(Status);
        ScreenFour.getChildren().add(PlayerTable);
        ScreenFour.getChildren().add(TableLabel);
        ScreenFour.getChildren().add(playBtn4);
        ScreenFour.getChildren().add(backBtn4);
        PlayerName.setCellValueFactory(
                new PropertyValueFactory<Player, String>("username")
        );
        Score.setCellValueFactory(
                new PropertyValueFactory<Player, String>("scour")
        );
        Status.setCellValueFactory(
                new PropertyValueFactory<Player, String>("state")
        );

//       PlayerTable.setItems(playerData);

        unSelected4.setTitle("player list");
        unSelected4.setHeaderText(null);
        unSelected4.setContentText("please select player ");

        backBtn4.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                ScreenThree().getChildren().clear();
                backBtn4.getScene().setRoot(ScreenThree());

            }
        });
        playBtn4.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                   Player pl = PlayerTable.getSelectionModel().getSelectedItem();
                    Platform.runLater(() -> {
                        ScreenMultiMode().getChildren().clear();
                    });
                    Platform.runLater(() -> {
                        playBtn4.getScene().setRoot(ScreenMultiMode());
                    });

                } catch (NullPointerException q) {

                    unSelected4.showAndWait();
                }

            }
        });

        return ScreenFour;

    }

    public AnchorPane ScreenSingleMode() {
        ScreenSingleMode = new AnchorPane();
        GameImg = new ImageView();
        exit = new Button();
        recordGame = new Button();
        bt1 = new Button();
        bt2 = new Button();
        bt3 = new Button();
        bt4 = new Button();
        bt5 = new Button();
        bt6 = new Button();
        bt7 = new Button();
        bt8 = new Button();
        bt9 = new Button();
        playerScore = new Label();
        computerScore = new Label();

        playerName = new Label();
        computerName = new Label();
        scoreSeperator = new Label();
        username = getusername();

        ScreenSingleMode.setMaxHeight(USE_PREF_SIZE);
        ScreenSingleMode.setMaxWidth(USE_PREF_SIZE);
        ScreenSingleMode.setMinHeight(USE_PREF_SIZE);
        ScreenSingleMode.setMinWidth(USE_PREF_SIZE);
        ScreenSingleMode.setPrefHeight(540.0);
        ScreenSingleMode.setPrefWidth(642.0);
        ScreenSingleMode.setStyle("-fx-background-color: linear-gradient(to right, #0f0c29, #302b63, #24243e);");

        GameImg.setFitHeight(540.0);
        GameImg.setFitWidth(642.0);
        GameImg.setPickOnBounds(true);
        try {
            FileInputStream stream = new FileInputStream("ProjectImg/image.jpg");
            Image image = new Image(stream);
            GameImg.setImage(image);

        } catch (FileNotFoundException ex) {
//            Logger.getLogger(FXMLScreenOne.class.getName()).log(Level.SEVERE, null, ex);
        }

        exit.setAlignment(javafx.geometry.Pos.CENTER);
        exit.setLayoutX(331.0);
        exit.setLayoutY(456.0);
        exit.setMnemonicParsing(false);
        exit.setPrefHeight(46.0);
        exit.setPrefWidth(179.0);
        exit.setStyle("-fx-background-color: linear-gradient(to right, #616161, #9bc5c3);;");
        exit.setText("Exit");
        exit.setTextFill(javafx.scene.paint.Color.WHITE);
        exit.setFont(new Font("Lucida Calligraphy Italic", 20.0));

        recordGame.setAlignment(javafx.geometry.Pos.CENTER);
        recordGame.setLayoutX(93.0);
        recordGame.setLayoutY(456.0);
        recordGame.setMnemonicParsing(false);
        recordGame.setPrefHeight(46.0);
        recordGame.setPrefWidth(179.0);
        recordGame.setStyle("-fx-background-color: linear-gradient(to right, #616161, #9bc5c3);;");
        recordGame.setText("Record Game");
        recordGame.setTextFill(javafx.scene.paint.Color.valueOf("#fffdfd"));
        recordGame.setFont(new Font("Lucida Calligraphy Italic", 20.0));

        bt1.setLayoutX(120.0);
        bt1.setLayoutY(102.0);
        bt1.setMnemonicParsing(false);
        bt1.setPrefHeight(89.0);
        bt1.setPrefWidth(95.0);

        bt2.setLayoutX(249.0);
        bt2.setLayoutY(100.0);
        bt2.setMnemonicParsing(false);
        bt2.setPrefHeight(89.0);
        bt2.setPrefWidth(95.0);

        bt3.setLayoutX(376.0);
        bt3.setLayoutY(100.0);
        bt3.setMnemonicParsing(false);
        bt3.setPrefHeight(89.0);
        bt3.setPrefWidth(95.0);

        bt4.setLayoutX(120.0);
        bt4.setLayoutY(215.0);
        bt4.setMnemonicParsing(false);
        bt4.setPrefHeight(89.0);
        bt4.setPrefWidth(95.0);

        bt5.setLayoutX(249.0);
        bt5.setLayoutY(215.0);
        bt5.setMnemonicParsing(false);
        bt5.setPrefHeight(89.0);
        bt5.setPrefWidth(95.0);

        bt6.setLayoutX(377.0);
        bt6.setLayoutY(215.0);
        bt6.setMnemonicParsing(false);
        bt6.setPrefHeight(89.0);
        bt6.setPrefWidth(95.0);

        bt7.setLayoutX(120.0);
        bt7.setLayoutY(331.0);
        bt7.setMnemonicParsing(false);
        bt7.setPrefHeight(89.0);
        bt7.setPrefWidth(95.0);

        bt8.setLayoutX(249.0);
        bt8.setLayoutY(334.0);
        bt8.setMnemonicParsing(false);
        bt8.setPrefHeight(89.0);
        bt8.setPrefWidth(95.0);

        bt9.setLayoutX(377.0);
        bt9.setLayoutY(336.0);
        bt9.setMnemonicParsing(false);
        bt9.setPrefHeight(89.0);
        bt9.setPrefWidth(95.0);

        playerName.setLayoutX(122.0);
        playerName.setLayoutY(5.0);
        playerName.setText(username);
        playerName.setTextFill(javafx.scene.paint.Color.WHITE);
        playerName.setFont(Font.font("Javanese Text", FontWeight.BOLD, 20));

        computerName.setLayoutX(390.0);
        computerName.setLayoutY(5.0);
        computerName.setText("Computer");
        computerName.setTextFill(javafx.scene.paint.Color.WHITE);
        computerName.setFont(Font.font("Javanese Text", FontWeight.BOLD, 20));

        playerScore.setLayoutX(140.0);
        playerScore.setLayoutY(50.0);
        playerScore.setText(String.valueOf(playerScoreCounter));
        playerScore.setTextFill(javafx.scene.paint.Color.WHITE);
        playerScore.setFont(Font.font("Engravers MT", FontWeight.BOLD, 36));

        scoreSeperator.setLayoutX(300.0);
        scoreSeperator.setLayoutY(14.0);
        scoreSeperator.setPrefWidth(28.0);
        scoreSeperator.setText(":");
        scoreSeperator.setTextFill(javafx.scene.paint.Color.WHITE);
        scoreSeperator.setFont(Font.font("Engravers MT", FontWeight.BOLD, 36));

        computerScore.setLayoutX(410.0);
        computerScore.setLayoutY(50.0);
        computerScore.setText(String.valueOf(computerScoreCounter));
        computerScore.setFont(Font.font("Engravers MT", FontWeight.BOLD, 36));
        computerScore.setTextFill(javafx.scene.paint.Color.WHITE);

        ScreenSingleMode.getChildren().add(GameImg);
        ScreenSingleMode.getChildren().add(exit);
        ScreenSingleMode.getChildren().add(recordGame);
        ScreenSingleMode.getChildren().add(bt1);
        ScreenSingleMode.getChildren().add(bt2);
        ScreenSingleMode.getChildren().add(bt3);
        ScreenSingleMode.getChildren().add(bt4);
        ScreenSingleMode.getChildren().add(bt5);
        ScreenSingleMode.getChildren().add(bt6);
        ScreenSingleMode.getChildren().add(bt7);
        ScreenSingleMode.getChildren().add(bt8);
        ScreenSingleMode.getChildren().add(bt9);
        ScreenSingleMode.getChildren().add(playerScore);
        ScreenSingleMode.getChildren().add(computerScore);
        ScreenSingleMode.getChildren().add(playerName);
        ScreenSingleMode.getChildren().add(computerName);
        ScreenSingleMode.getChildren().add(scoreSeperator);

        playerScoreCounter = 0;
        computerScoreCounter = 0;
        recordedPositions = new String[9];
        gameFlag = true;
        symbol = "X";
        buttons = new Button[9];

        buttons[0] = bt1;
        buttons[1] = bt2;
        buttons[2] = bt3;
        buttons[3] = bt4;
        buttons[4] = bt5;
        buttons[5] = bt6;
        buttons[6] = bt7;
        buttons[7] = bt8;
        buttons[8] = bt9;
        for (index = 0; index < 9; index++) {
            buttons[index].setFont(Font.font("Engravers MT", FontWeight.BOLD, 36));
        }

        bt1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (gameFlag) {
                    bt1.setText(symbol);
                    bt1.setDisable(true);
                    if (symbol == "X") {
                        playerXpositions.add(1);
                        symbol = "O";
                        if (checkWinner() == "X") {
                            highLightWinner(playerXpositions);
                            gameFlag = false;
                            playerScoreCounter++;
                            updateScore();
                        } else if (checkWinner() == "tie") {
                            drawTie();
                            gameFlag = false;
                            updateScore();
                        } else {
                            playComputerMove();
                        }
                    }
                }
            }

        });
        bt2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (gameFlag) {
                    bt2.setText(symbol);
                    bt2.setDisable(true);
                    if (symbol == "X") {

                        playerXpositions.add(2);
                        symbol = "O";
                        if (checkWinner() == "X") {
                            highLightWinner(playerXpositions);
                            gameFlag = false;
                            playerScoreCounter++;
                            updateScore();
                        } else if (checkWinner() == "tie") {
                            drawTie();
                            gameFlag = false;
                            updateScore();
                        } else {
                            playComputerMove();
                        }
                    }
                }
            }

        });
        bt3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (gameFlag) {
                    bt3.setText(symbol);
                    bt3.setDisable(true);
                    if (symbol == "X") {
                        playerXpositions.add(3);
                        symbol = "O";
                        if (checkWinner() == "X") {
                            highLightWinner(playerXpositions);
                            gameFlag = false;
                            playerScoreCounter++;
                            updateScore();
                        } else if (checkWinner() == "tie") {
                            bt3.setText("Tie");
                            drawTie();
                            gameFlag = false;
                            updateScore();
                        } else {
                            playComputerMove();
                        }
                    }
                }
            }

        });
        bt4.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (gameFlag) {
                    bt4.setText(symbol);
                    bt4.setDisable(true);
                    if (symbol == "X") {

                        playerXpositions.add(4);
                        symbol = "O";
                        if (checkWinner() == "X") {
                            highLightWinner(playerXpositions);
                            gameFlag = false;
                            playerScoreCounter++;
                            updateScore();
                        } else if (checkWinner() == "tie") {
                            bt4.setText("Tie");
                            drawTie();
                            gameFlag = false;
                            updateScore();
                        } else {
                            playComputerMove();
                        }
                    }
                }
            }

        });
        bt5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (gameFlag) {
                    bt5.setText(symbol);
                    bt5.setDisable(true);
                    if (symbol == "X") {

                        playerXpositions.add(5);
                        symbol = "O";
                        if (checkWinner() == "X") {
                            highLightWinner(playerXpositions);
                            gameFlag = false;
                            playerScoreCounter++;
                            updateScore();
                        } else if (checkWinner() == "tie") {
                            bt5.setText("Tie");
                            drawTie();
                            updateScore();
                        } else {
                            playComputerMove();
                        }
                    }
                }
            }

        });
        bt6.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (gameFlag) {
                    bt6.setText(symbol);
                    bt6.setDisable(true);
                    if (symbol == "X") {

                        playerXpositions.add(6);
                        symbol = "O";
                        if (checkWinner() == "X") {
                            highLightWinner(playerXpositions);
                            gameFlag = false;
                            playerScoreCounter++;
                            updateScore();
                        } else if (checkWinner() == "tie") {
                            bt6.setText("Tie");
                            drawTie();
                            gameFlag = false;
                            updateScore();
                        } else {
                            playComputerMove();
                        }
                    }
                }
            }

        });
        bt7.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (gameFlag) {
                    bt7.setText(symbol);
                    bt7.setDisable(true);
                    if (symbol == "X") {

                        playerXpositions.add(7);
                        symbol = "O";
                        if (checkWinner() == "X") {
                            highLightWinner(playerXpositions);
                            gameFlag = false;
                            playerScoreCounter++;
                            updateScore();
                        } else if (checkWinner() == "tie") {
                            bt7.setText("Tie");
                            drawTie();
                            gameFlag = false;
                            updateScore();
                        } else {
                            playComputerMove();
                        }
                    }
                }
            }

        });
        bt8.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (gameFlag) {
                    bt8.setText(symbol);
                    bt8.setDisable(true);
                    if (symbol == "X") {

                        playerXpositions.add(8);
                        symbol = "O";
                        if (checkWinner() == "X") {
                            highLightWinner(playerXpositions);
                            gameFlag = false;
                            playerScoreCounter++;
                            updateScore();
                        } else if (checkWinner() == "tie") {
                            bt8.setText("Tie");
                            drawTie();
                            gameFlag = false;
                            updateScore();
                        } else {
                            playComputerMove();
                        }
                    }
                }
            }

        });
        bt9.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (gameFlag) {
                    bt9.setText(symbol);
                    bt9.setDisable(true);
                    if (symbol == "X") {

                        playerXpositions.add(9);
                        symbol = "O";
                        if (checkWinner() == "X") {
                            highLightWinner(playerXpositions);
                            gameFlag = false;
                            playerScoreCounter++;
                            updateScore();
                        } else if (checkWinner() == "tie") {
                            bt9.setText("Tie");
                            drawTie();
                            gameFlag = false;
                            updateScore();

                        } else {
                            playComputerMove();
                        }
                    }
                }
            }

        });

        recordGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!gameFlag) {
                    playAgain();
                } else {
                    recordPositions();
                    drawXO();
                }

            }

        });

        exit.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                ScreenThree().getChildren().clear();
                exit.getScene().setRoot(ScreenThree());
                ;
            }

        });

        playerName.setText(username);

        return ScreenSingleMode;
    }

    
    public AnchorPane ScreenMultiMode() {
        
        myGame = new Game();
        convert = new JsonConverter();
        currentMove = Game.X_MOVE;
        recordGameFlag = true;
        
        if(recordedPositions == null){
            recordedPositions = new String[9];
        }
        
        ScreenMultiMode = new AnchorPane();
        MultiGameImage = new ImageView();
        chatArea = new TextArea();
        messageField = new TextField();
        sendMsg = new Button();
        btn1 = new Button();
        btn2 = new Button();
        btn3 = new Button();
        btn4 = new Button();
        btn5 = new Button();
        btn6 = new Button();
        btn7 = new Button();
        btn8 = new Button();
        btn9 = new Button();
        
        buttons[0] = btn1;
        buttons[1] = btn2;
        buttons[2] = btn3;
        buttons[3] = btn4;
        buttons[4] = btn5;
        buttons[5] = btn6;
        buttons[6] = btn7;
        buttons[7] = btn8;
        buttons[8] = btn9;
        
        btn1.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                Long buttonPosition = 1l;
                updateBoard(buttonPosition);
            }
        
        });
        btn2.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                Long buttonPosition = 2l;
                updateBoard(buttonPosition);
            }
            
        
        
        });
        btn3.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                Long buttonPosition = 3l;
                updateBoard(buttonPosition);
            }
        
        
        });
        btn4.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                Long buttonPosition = 4l;
                updateBoard(buttonPosition);
            }
        
        
        });
        btn5.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                Long buttonPosition = 5l;
                updateBoard(buttonPosition);
            }
        
        
        });
        btn6.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                Long buttonPosition = 6l;
                updateBoard(buttonPosition);
            }
        
        
        });
        btn7.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                Long buttonPosition = 7l;
                updateBoard(buttonPosition);
            }
        
        
        });
        btn8.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                Long buttonPosition = 8l;
                updateBoard(buttonPosition);
            }
        
        
        });
        btn9.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                Long buttonPosition = 9l;
                updateBoard(buttonPosition);
            }
        
        
        });
        
        
        exitGame = new Button();
        playerOneName = new Label();
        playerTwoName = new Label();
        playerO = new Label();
        currentTurn = new Label();
        playerX = new Label();
        playerOneScore = new Label();
        playerTwoScore = new Label();
        scoreSeperator = new Label();
        recordGame = new Button();
        
        recordGame.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                
                if(recordGameFlag){
                    sendRecordedPosition();
                    drawXO();
                }else{
                    playAgain();
                }
            }
        
        });

        ScreenMultiMode.setMaxHeight(USE_PREF_SIZE);
        ScreenMultiMode.setMaxWidth(USE_PREF_SIZE);
        ScreenMultiMode.setMinHeight(USE_PREF_SIZE);
        ScreenMultiMode.setMinWidth(USE_PREF_SIZE);
        ScreenMultiMode.setPrefHeight(540.0);
        ScreenMultiMode.setPrefWidth(700.0);

        MultiGameImage.setFitHeight(540.0);
        MultiGameImage.setFitWidth(700.0);
        MultiGameImage.setPickOnBounds(true);
//        MultiGameImage.setImage(new Image(getClass().getResource("../../../Project/Img/image.jpg").toExternalForm()));
        try {
            FileInputStream stream = new FileInputStream("ProjectImg/image.jpg");
            Image image = new Image(stream);
            MultiGameImage.setImage(image);

        } catch (FileNotFoundException ex) {
//            Logger.getLogger(FXMLScreenOne.class.getName()).log(Level.SEVERE, null, ex);
        }

        chatArea.setEditable(false);
        chatArea.setLayoutX(383.0);
        chatArea.setLayoutY(30.0);
        chatArea.setPrefHeight(309.0);
        chatArea.setPrefWidth(306.0);

        messageField.setLayoutX(382.0);
        messageField.setLayoutY(348.0);
        messageField.setPrefHeight(43.0);
        messageField.setPrefWidth(214.0);

        sendMsg.setAlignment(javafx.geometry.Pos.CENTER);
        sendMsg.setLayoutX(604.0);
        sendMsg.setLayoutY(348.0);
        sendMsg.setMnemonicParsing(false);
        sendMsg.setPrefHeight(34.0);
        sendMsg.setPrefWidth(93.0);
        sendMsg.setStyle("-fx-background-color: linear-gradient(to right, #373b44, #4286f4);");
        sendMsg.setText("Send");
        sendMsg.setTextFill(javafx.scene.paint.Color.WHITE);
        sendMsg.setFont(new Font("Lucida Calligraphy Italic", 20.0));
        
        sendMsg.setOnAction(new EventHandler<ActionEvent> () {
            @Override
            public void handle(ActionEvent event) {
                sendChat();
            }
        });

        btn1.setLayoutX(40.0);
        btn1.setLayoutY(113.0);
        btn1.setMnemonicParsing(false);
        btn1.setPrefHeight(68.0);
        btn1.setPrefWidth(86.0);
        //add text from palyer
        btn1.setText("X");
        btn1.setFont(new Font("Engravers MT", 36.0));

        btn2.setLayoutX(152.0);
        btn2.setLayoutY(113.0);
        btn2.setMnemonicParsing(false);
        btn2.setPrefHeight(68.0);
        btn2.setPrefWidth(86.0);
        btn2.setFont(new Font("Engravers MT", 36.0));

        btn3.setLayoutX(261.0);
        btn3.setLayoutY(113.0);
        btn3.setMnemonicParsing(false);
        btn3.setPrefHeight(68.0);
        btn3.setPrefWidth(86.0);
        btn3.setFont(new Font("Engravers MT", 36.0));

        btn4.setLayoutX(40.0);
        btn4.setLayoutY(196.0);
        btn4.setMnemonicParsing(false);
        btn4.setPrefHeight(68.0);
        btn4.setPrefWidth(86.0);
        btn4.setFont(new Font("Engravers MT", 36.0));

        btn5.setLayoutX(152.0);
        btn5.setLayoutY(196.0);
        btn5.setMnemonicParsing(false);
        btn5.setPrefHeight(68.0);
        btn5.setPrefWidth(86.0);
        //add text from palyer
        btn5.setText("O");
        btn5.setFont(new Font("Engravers MT", 36.0));

        btn6.setLayoutX(263.0);
        btn6.setLayoutY(196.0);
        btn6.setMnemonicParsing(false);
        btn6.setPrefHeight(68.0);
        btn6.setPrefWidth(86.0);
        btn6.setFont(new Font("Engravers MT", 36.0));

        btn7.setLayoutX(40.0);
        btn7.setLayoutY(280.0);
        btn7.setMnemonicParsing(false);
        btn7.setPrefHeight(68.0);
        btn7.setPrefWidth(86.0);
        btn7.setFont(new Font("Engravers MT", 36.0));

        btn8.setLayoutX(152.0);
        btn8.setLayoutY(280.0);
        btn8.setMnemonicParsing(false);
        btn8.setPrefHeight(68.0);
        btn8.setPrefWidth(86.0);
        btn8.setFont(new Font("Engravers MT", 36.0));

        btn9.setLayoutX(263.0);
        btn9.setLayoutY(280.0);
        btn9.setMnemonicParsing(false);
        btn9.setPrefHeight(68.0);
        btn9.setPrefWidth(86.0);
        //add text from palyer
        btn9.setText("X");
        btn9.setFont(new Font("Engravers MT", 36.0));

        exitGame.setLayoutX(405.0);
        exitGame.setLayoutY(470.0);
        exitGame.setMnemonicParsing(false);
        exitGame.setPrefHeight(46.0);
        exitGame.setPrefWidth(191.0);
        exitGame.setStyle("-fx-background-color: linear-gradient(to right, #373b44, #4286f4);;");
        exitGame.setText("Exit");
        exitGame.setTextFill(javafx.scene.paint.Color.WHITE);
        exitGame.setFont(new Font("Lucida Calligraphy Italic", 20.0));
        
        exitGame.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                sendExitGameRequest(); 
            }
        });
        
        playerOneName.setLayoutX(55.0);
        playerOneName.setPrefHeight(27.0);
        playerOneName.setPrefWidth(78.0);
        //add playerOneName from db
        playerOneName.setText("Heba");
        playerOneName.setTextFill(javafx.scene.paint.Color.WHITE);
        playerOneName.setFont(new Font("Javanese Text", 20.0));

        playerTwoName.setLayoutX(264.0);
        //add playerTwoName from db
        playerTwoName.setText("Nawal");
        playerTwoName.setTextFill(javafx.scene.paint.Color.WHITE);
        playerTwoName.setFont(new Font("Javanese Text", 20.0));

        playerO.setAlignment(javafx.geometry.Pos.CENTER);
        playerO.setLayoutX(203.0);
        playerO.setPrefHeight(27.0);
        playerO.setPrefWidth(40.0);
        playerO.setText("O");
        playerO.setTextFill(javafx.scene.paint.Color.WHITE);
        playerO.setFont(new Font("Javanese Text", 20.0));

        currentTurn.setAlignment(javafx.geometry.Pos.CENTER);
        currentTurn.setLayoutX(69.0);
        currentTurn.setLayoutY(344.0);
        currentTurn.setPrefHeight(41.0);
        currentTurn.setPrefWidth(281.0);
        currentTurn.setText("X Turn");
        currentTurn.setTextFill(javafx.scene.paint.Color.WHITE);
        currentTurn.setFont(new Font("Javanese Text", 36.0));

        playerX.setAlignment(javafx.geometry.Pos.CENTER);
        playerX.setLayoutX(126.0);
        playerX.setLayoutY(-1.0);
        playerX.setPrefHeight(27.0);
        playerX.setPrefWidth(40.0);
        playerX.setText("X");
        playerX.setTextFill(javafx.scene.paint.Color.WHITE);
        playerX.setFont(new Font("Javanese Text", 20.0));

        playerOneScore.setLayoutX(69.0);
        playerOneScore.setLayoutY(52.0);
        playerOneScore.setPrefHeight(27.0);
        playerOneScore.setPrefWidth(20.0);
        playerOneScore.setText("1");
        playerOneScore.setTextFill(javafx.scene.paint.Color.WHITE);
        playerOneScore.setFont(new Font("Engravers MT", 36.0));

        playerTwoScore.setLayoutX(281.0);
        playerTwoScore.setLayoutY(52.0);
        playerTwoScore.setPrefHeight(27.0);
        playerTwoScore.setPrefWidth(20.0);
        playerTwoScore.setText("0");
        playerTwoScore.setTextFill(javafx.scene.paint.Color.WHITE);
        playerTwoScore.setFont(new Font("Engravers MT", 36.0));

        scoreSeperator2.setAlignment(javafx.geometry.Pos.CENTER);
        scoreSeperator2.setLayoutX(183.0);
        scoreSeperator2.setLayoutY(3.0);
        scoreSeperator2.setText(":");
        scoreSeperator2.setTextFill(javafx.scene.paint.Color.WHITE);
        scoreSeperator2.setFont(new Font(36.0));

        recordGame2.setLayoutX(55.0);
        recordGame2.setLayoutY(471.0);
        recordGame2.setMnemonicParsing(false);
        recordGame2.setPrefHeight(46.0);
        recordGame2.setPrefWidth(191.0);
        recordGame2.setStyle("-fx-background-color: linear-gradient(to right, #373b44, #4286f4);;");
        recordGame2.setText("Record Game");
        recordGame2.setTextFill(javafx.scene.paint.Color.WHITE);
        recordGame2.setFont(new Font("Lucida Calligraphy Italic", 20.0));
        
        drawOldPositions();

        ScreenMultiMode.getChildren().add(MultiGameImage);
        ScreenMultiMode.getChildren().add(chatArea);
        ScreenMultiMode.getChildren().add(messageField);
        ScreenMultiMode.getChildren().add(sendMsg);
        ScreenMultiMode.getChildren().add(bt1);
        ScreenMultiMode.getChildren().add(bt2);
        ScreenMultiMode.getChildren().add(bt3);
        ScreenMultiMode.getChildren().add(bt4);
        ScreenMultiMode.getChildren().add(bt5);
        ScreenMultiMode.getChildren().add(bt6);
        ScreenMultiMode.getChildren().add(bt7);
        ScreenMultiMode.getChildren().add(bt8);
        ScreenMultiMode.getChildren().add(bt9);
        ScreenMultiMode.getChildren().add(exitGame);
        ScreenMultiMode.getChildren().add(playerOneName);
        ScreenMultiMode.getChildren().add(playerTwoName);
        ScreenMultiMode.getChildren().add(playerO);
        ScreenMultiMode.getChildren().add(currentTurn);
        ScreenMultiMode.getChildren().add(playerX);
        ScreenMultiMode.getChildren().add(playerOneScore);
        ScreenMultiMode.getChildren().add(playerTwoScore);
        ScreenMultiMode.getChildren().add(scoreSeperator);
        ScreenMultiMode.getChildren().add(recordGame);
        return ScreenMultiMode;
    }

    public void messageHandelr(Player p) {

        System.out.println("Line 212: " + p.getRespond());
        switch (p.getRequest()) {
            case Request.LOGIN:
                login(p);
                break;
            case Request.SIGNUP:
                signup(p);
                break;
            case Request.LOGOUT:
                System.out.println("Line 313: " + p.getRespond());
                logout(p);
                break;
            case Request.GAME_INVITATION:
                checkGameRespond();
                break;
            case Request.GAME_INVITATION_RESPOND:
                generateAlertToAskUserForRespond();
                break;
            case Request.GAME_MOVE:
            case Request.GAME_PLAYAGAIN:
            case Request.Chat_Message:
            case Request.RECORD_GAME:
            case Request.END_GAME:
            case Request.GET_RECORDEDGAME:
//                forwardedGameRequest(JSONObject obj);
                break;
        }

    }
    
    private void checkGameRespond(){
        
    }
    private void generateAlertToAskUserForRespond(){
        
    }
    public void login(Player newPalyer) {
        System.out.println("Line 221: " + newPalyer.getRespond());

        if (newPalyer.getRespond().equals(Respond.SUCCESS)) {

            System.out.println("Line 223: " + newPalyer.getRespond());

            Platform.runLater(() -> {
                ScreenThree().getChildren().clear();
            });
            Platform.runLater(() -> {
                logInBtn1.getScene().setRoot(ScreenThree());
            });

            System.out.println("Line 226: " + newPalyer.getRespond());
        } else {
            System.out.println("Line 228: " + newPalyer.getRespond());
            Platform.runLater(() -> alertWrongLogIn1.showAndWait());

        }
    }

    public void signup(Player p) {

        System.out.println("Line 343: " + p.getRespond());
        if (p.getRespond().equals(Respond.SUCCESS)) {
            System.out.println("Line 344: " + p.getRespond());
            Platform.runLater(() -> {
                alert.showAndWait();
            });
            Platform.runLater(() -> {
                ScreenOne().getChildren().clear();
            });
            Platform.runLater(() -> {
                signupBtn2.getScene().setRoot(ScreenOne());
            });

            System.out.println("Line 348: " + p.getRespond());
        } else {
            System.out.println("else" + p.getRespond());
            Platform.runLater(() -> {
                alertUserExists.showAndWait();
            });
            System.out.println("Line 232: user already exsist" + p.getRespond());

        }
    }

    public void logout(Player p) {

        System.out.println("Logout function: " + p.getRespond());

        if (p.getRespond().equals(Respond.SUCCESS)) {

            Platform.runLater(() -> {
                ScreenOne().getChildren().clear();
            });
            Platform.runLater(() -> {
                logOutBtn3.getScene().setRoot(ScreenOne());
            });
            System.out.println("User logout successfully: " + p.getRespond());
        } else {
            alertWrongLogout1 = new Alert(Alert.AlertType.ERROR);
            alertWrongLogout1.setTitle("Logout ");
            alertWrongLogout1.setHeaderText(null);
            alertWrongLogout1.setContentText("Faild logout");
            Platform.runLater(() -> alertWrongLogout1.showAndWait());
            System.out.println("Line 389 logout: " + p.getRespond());

        }
    }

    /**
     * ********************************** Single Mode Game
     * methods*******************************************************************************
     */
    void drawOldPositions(String[] positions) {
        // will append symbol, and disable  buttons according to  last saved
        for (int index = 0; index < 9; index++) {

            if (positions[index] != null) {
                buttons[index].setText(positions[index]);
                buttons[index].setDisable(true);
                if (positions[index] == "X") {
                    playerXpositions.add(index + 1);
                } else {
                    playerOpositions.add(index + 1);
                }
            }
        }

        //change the symbol to the right turn
        if (playerXpositions.size() > playerOpositions.size()) {
            symbol = "O";
            playComputerMove();
        }

    }
    int computerMove() {
        Random random = new Random();
        int cpuPosition = random.nextInt(9) + 1;
        // make sure that this position is not taken before
        while (playerXpositions.contains(cpuPosition) || playerOpositions.contains(cpuPosition)) {
            cpuPosition = random.nextInt(9) + 1;
        }
        return cpuPosition;
    }

    void drawCpuMove(int movePosition) {
        movePosition--; // to indecate the index of the button
        for (int index = 0; index < 9; index++) {
            if (index == movePosition) {
                buttons[index].setText(symbol);
                buttons[index].setDisable(true);
            }
        }
    }

    void playComputerMove() {
        if (gameFlag) {
            move = computerMove();
            playerOpositions.add(move);
            drawCpuMove(move);
            symbol = "X";
            if (checkWinner() == "O") {
                highLightWinner(playerOpositions);
                gameFlag = false;
                computerScoreCounter++;
                updateScore();
            } else if (checkWinner() == "tie") {
                drawTie();
                updateScore();
            }
        }
    }

    private void playAgain() {
        recordGame.setText("Record Game");
        gameFlag = true;
        symbol = "X";
        playerXpositions.clear();
        playerOpositions.clear();
        for (Button btn : buttons) {
            btn.setText("");
            //btn.setStyle("-fx-background-color: #D2691E ");
            btn.setDisable(false);

            // reset the old style
            Button tmpButton = new Button();
            btn.setStyle(tmpButton.getStyle());
        }
    }

    public static void setPlayerName(String playerName) {

        username = playerName;
    }

    public static String getPlayerName() {

        return username;
    }

    private void updateScore() {
        playerScore.setText(String.valueOf(playerScoreCounter));
        computerScore.setText(String.valueOf(computerScoreCounter));
        recordGame.setText("Play Again!");
    }

    @Override
    public void start(Stage primaryStage) {

        try {
            Parent root = ScreenOne();
            Scene scene = new Scene(root);

            try {
                //creating the image object
                InputStream stream = new FileInputStream("ProjectImg/xo.png");
                Image image = new Image(stream);
                //Creating the image view
                ImageView imageView = new ImageView();
                //Setting image to the image view
                primaryStage.getIcons().add(image);
            } catch (FileNotFoundException ex) {

                System.out.println("Cann't load background image in start method");
            }

            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.setTitle("Tic Tac Toe");
            primaryStage.show();
        } catch (NullPointerException ex) {
            System.out.println("Error");
        }
    }

    @Override
    public void stop() {

//        p.setRequest(Request.DISCONNECT);
        try {
            writeObj.writeObject(p);
            socket.close();
            readObj.close();
            writeObj.close();
            System.out.println("Sent a disconnection request to the server");
        } catch (IOException ex) {
            System.out.println("problem in stop method ");

        }
        thread.stop();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
