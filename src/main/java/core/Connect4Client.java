/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * Connect4Client class used to load a UI and communicate with Connect4Server
 * in order to run a Connect4 game either with a single player or two players
 * 
 * 
 * @author Alexander Rossiter
 * @version 1.0
 */
public class Connect4Client extends Application implements Constants {
    // Indicate whether the player has the turn
    private boolean myTurn = false;

    // Indicate the token for the player
    private char myToken = ' ';

    // Indicate the token for the other player
    private char otherToken = ' ';

    // Create and initialize cells
    private Pane[][] boardCells =  new Pane[6][7];
    private MoveCell[] moveCells = new MoveCell[7];
    
    private Image redPiece = new Image("red.png");
    private Image blackPiece = new Image("black.png");
    
    Button computerPlayer = new Button("Play vs Computer");
    Button humanPlayer = new Button("2 Player game");
    
    private HBox buttonPane = new HBox();

    // Create and initialize player info text
    private Text playerText = new Text();

    // Create and initialize a status info text
    private Text bottomText = new Text();

    // Indicate selected row and column by the current move-
    private int columnSelected;

    // Input and output streams from/to server
    private DataInputStream fromServer;
    private DataOutputStream toServer;

    // Continue to play?
    private boolean continueToPlay = true;

    // Wait for the player to mark a cell
    private boolean waiting = true;

    // Host name or ip
    private String host = "localhost";

  @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {
        //Main Pane used to carry all components
        BorderPane borderpane = new BorderPane();
        borderpane.setPrefSize(490, 630);

        //Top Section of BorderPane
        playerText.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
        playerText.setTextAlignment(TextAlignment.CENTER);
        StackPane playerPane = new StackPane();
        playerPane.setPrefHeight(70);
        playerPane.getChildren().add(playerText);
        borderpane.setTop(playerPane);

        //Center Section of BorderPane
        VBox centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);

        GridPane moveGrid = new GridPane();
        for (int i = 0; i < 7; i++) {
            moveGrid.add(moveCells[i] = new MoveCell(i), i, 0);
        }
        centerPane.getChildren().add(moveGrid);

        GridPane boardGrid = new GridPane();
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                boardGrid.add(boardCells[r][c] = new Pane(), c, r);
                boardCells[r][c].setPrefSize(70, 70);
            }
        }

        Pane board = new Pane();
        board.getChildren().add(new ImageView(new Image("board.png")));

        StackPane boardPane = new StackPane();
        boardPane.setAlignment(Pos.CENTER);

        boardPane.getChildren().addAll(boardGrid, board);
        centerPane.getChildren().add(boardPane);
        borderpane.setCenter(centerPane);

        //Bottom Section
        bottomText.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
        bottomText.setTextAlignment(TextAlignment.CENTER);
        StackPane bText = new StackPane();
        bText.getChildren().add(bottomText);

        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.getChildren().add(computerPlayer);
        buttonPane.getChildren().add(humanPlayer);

        VBox bottomPane = new VBox();
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPrefSize(490, 70);
        bottomPane.getChildren().add(bText);
        bottomPane.getChildren().add(buttonPane);

        borderpane.setBottom(bottomPane);

        Scene scene = new Scene(borderpane);
        primaryStage.setTitle("Connect 4");
        primaryStage.setScene(scene);
        primaryStage.show();  

        // Connect to the server
        connectToServer();
    }

    /**
     * Method used to connect client to server and start a new thread for 
     * each player.
     */
    private void connectToServer() {
        try {
            // Create a socket to connect to the server
            Socket socket = new Socket(host, 8004);

            // Create an input stream to receive data from the server
            fromServer = new DataInputStream(socket.getInputStream());

            // Create an output stream to send data to the server
            toServer = new DataOutputStream(socket.getOutputStream());
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Control the game on a separate thread
        new Thread(() -> {
            try {
                // Get notification from the server
                int player = fromServer.readInt();

                // Am I player 1 or 2?
                if (player == PLAYER1) {
                    myToken = 'X';
                    otherToken = 'O';
                    Platform.runLater(() -> {
                            playerText.setText("PLAYER 1: BLACK\n"
                                    + "PLEASE WAIT FOR PLAYER 2 TO JOIN");
                            bottomText.setText("PLEASE SELECT GAME MODE");
                    });
                    computerPlayer.setOnAction(eh -> isTwoPlayer(toServer, false));
                    humanPlayer.setOnAction(eh -> isTwoPlayer(toServer, true));
                    
                    // Receive startup notification from the server
                    fromServer.readInt(); // Whatever read is ignored

                    // The other player has joined
                    Platform.runLater(() ->
                            playerText.setText("PLAYER 2 HAS JOINED, MY TURN"));

                    // It is my turn
                    myTurn = true;
                } else if (player == PLAYER2) {
                    myToken = 'O';
                    otherToken = 'X';
                    Platform.runLater(() -> {
                            playerText.setText("WAIT FOR PLAYER 1 TO MOVE");
                            bottomText.setText("PLAYER 2 (RED)");
                            buttonPane.getChildren().remove(0, 2);
                    });
                }

                // Continue to play
                while (continueToPlay) {
                    if (player == PLAYER1) {
                        waitForPlayerAction(); // Wait for player 1 to move
                        sendMove(); // Send the move to the server
                        receiveInfoFromServer(); // Receive info from the server
                    }
                    else if (player == PLAYER2) {
                        receiveInfoFromServer(); // Receive info from the server
                        waitForPlayerAction(); // Wait for player 2 to move
                        sendMove(); // Send player 2's move to the server
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Determines whether player 1 will be playing against a computer or 
     * waiting for a second player.
     * 
     * @param mode true for 2 player game and false for computer player game.
     */
    private void isTwoPlayer(DataOutputStream out, boolean mode) {
        try {
            out.writeBoolean(mode);
        } catch (IOException ex) {
            Logger.getLogger(Connect4Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Remove buttons once clicked
        buttonPane.getChildren().remove(0, 2);
        bottomText.setText("PLAYER 1 (BLACK)");
    }

    /**
     * Makes this thread wait until player has mad a move
     * 
     * @throws InterruptedException 
     */
    private void waitForPlayerAction() throws InterruptedException {
        while (waiting) {
            Thread.sleep(100);
        }

        waiting = true;
    }

    /**
     * Used to sent the current move made by this player to the server
     * 
     * @throws IOException 
     */
    private void sendMove() throws IOException {
        toServer.writeInt(columnSelected); // Send the selected column
    }

    /**
     * Receives information from server and uses that information to determine
     * whether the game has ended or should continue.
     * 
     * @throws IOException 
     */
    private void receiveInfoFromServer() throws IOException {
        // Receive game status
        int status = fromServer.readInt();

        if (status == PLAYER1_WON) {
            // Player 1 won, stop playing
            continueToPlay = false;
            if (myToken == 'X') {
                Platform.runLater(() -> playerText.setText("I WON! (BLACK)"));
            }
            else if (myToken == 'O') {
                Platform.runLater(() ->
                        playerText.setText("PLAYER 1 (BLACK) HAS WON!"));
                receiveMove();
            }
        } else if (status == PLAYER2_WON) {
            // Player 2 won, stop playing
            continueToPlay = false;
            if (myToken == 'O') {
                Platform.runLater(() -> playerText.setText("I WON! (RED)"));
            }
            else if (myToken == 'X') {
                Platform.runLater(() ->
                        playerText.setText("PLAYER 2 (RED) HAS WON!"));
                receiveMove();
            }
        } else if (status == DRAW) {
            // No winner, game is over
            continueToPlay = false;
            Platform.runLater(() ->
                    playerText.setText("GAME IS OVER, NO WINNER!"));

            if (myToken == 'O') {
                receiveMove();
            }
        } else {
            receiveMove();
            Platform.runLater(() -> playerText.setText("MY TURN"));
            myTurn = true; // It is my turn
        }
    }

    /**
     * Used to receive move from another player.
     * 
     * @throws IOException 
     */
    private void receiveMove() throws IOException {
        // Get the other player's move
        int row = fromServer.readInt();
        int column = fromServer.readInt();
        if (otherToken == 'X') {
            Platform.runLater(() -> boardCells[row][column].getChildren().add(
                    new ImageView(blackPiece)));
        } else {
            Platform.runLater(() -> boardCells[row][column].getChildren().add(
                    new ImageView(redPiece)));
        }
    }
    
    /**
     * Class for top Cells above board in order to handle mouse clicks when player
     * wants to make a move in the game. Shows current piece above each column
     * of board when mouse is hovered over, and places that piece in current column
     * on a mouse click.
     */
    private class MoveCell extends Pane {
        private final int col; 
        private boolean rowFull = false;
        
        /**
         * Constructor to initialize all handlers and cell size
         * @param col sets this.col to whatever column this cell appears above
         */
        public MoveCell(int col) {
            this.col = col;
            this.setPrefSize(70, 70);
            this.setOnMouseClicked(eh -> handleMouseClick());
            this.setOnMouseEntered(eh -> handleMouseEnter());
            this.setOnMouseExited(eh -> handleMouseExit());
        }
        
        /**
         * Handler for Mouse click
         */
        private void handleMouseClick() {
            if (!this.getChildren().isEmpty()) {
                this.getChildren().remove(0);
            }
            
            if (continueToPlay && myTurn) {
                columnSelected = col;
                int row = -1;
                for (int r = 5; r >= 0; r--) {
                    if (boardCells[r][col].getChildren().isEmpty()) {
                        row = r;
                        break;
                    }
                }
                //When row is -1 the row is full and move cannot be made
                if (row != -1) {
                    if (myToken == 'X') {
                        boardCells[row][col].getChildren().add(new ImageView(blackPiece));
                    } else {
                        boardCells[row][col].getChildren().add(new ImageView(redPiece));
                    }
                    playerText.setText("WAITING FOR OTHER PLAYER TO MOVE");
                    myTurn = false;
                    waiting = false;
                } 
                if (row <= 0) {
                    rowFull = true;
                }
            }
        }
        
        /**
         * Handles mouse hover in order to display current players piece
         */
        private void handleMouseEnter() {
            if (rowFull) {
                this.setOnMouseClicked(null);
                this.setOnMouseExited(null);
                this.setOnMouseEntered(null);
            }
            if (continueToPlay && myTurn && !rowFull) {
                if (myToken == 'X') {
                    this.getChildren().add(new ImageView(blackPiece));
                } else {
                    this.getChildren().add(new ImageView(redPiece));
                }
            }
        }
        
        /**
         * Handles mouse hover and removes piece when mouse is no longer over 
         * a cell.
         */
        private void handleMouseExit() {
            if (!this.getChildren().isEmpty()) {
                this.getChildren().remove(0);
            }
        }
        
    }
    
    /**
     * The main method is only needed for the IDE with limited
     * JavaFX support. Not needed for running from the command line.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
