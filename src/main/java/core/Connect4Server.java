/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import static core.Constants.DRAW;
import java.util.List;
import java.io.*;
import java.net.*;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * Connect4Server class used to allow two Connect4Clients to communicate 
 * in order to run a game of Connect4 either between a player and a computer player
 * or between two players.
 * 
 * @author Alexander Rossiter
 * @version 1.0
 */
public class Connect4Server extends Application implements Constants {
    private int sessionNo = 1; // Number a session

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {
        TextArea taLog = new TextArea();

        // Create a scene and place it in the stage
        Scene scene = new Scene(new ScrollPane(taLog), 450, 200);
        primaryStage.setTitle("Connect4Server"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        new Thread( () -> {
            try {
                // Create a server socket
                Parameters params = getParameters();
                List<String> list = params.getRaw();
                int portNo = Integer.parseInt(list.get(0));
                ServerSocket serverSocket = new ServerSocket(portNo);
                Platform.runLater(() -> taLog.appendText(new Date() + 
                        ": Server started at socket " + portNo + "\n"));

                // Ready to create a session for every two players
                while (true) {
                    Platform.runLater(() -> taLog.appendText(new Date() +
                            ": Wait for players to join session " + sessionNo + '\n'));

                    // Connect to player 1
                    Socket player1 = serverSocket.accept();

                    Platform.runLater(() -> {
                        taLog.appendText(new Date() + ": Player 1 joined session "
                            + sessionNo + '\n');
                        taLog.appendText("Player 1's IP address" +
                        player1.getInetAddress().getHostAddress() + '\n');
                    });

                    // Notify that the player is Player 1
                    new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);
                    

                    // Connect to player 2 if player 1 chooses 2 player game
                    if (new DataInputStream(player1.getInputStream()).readBoolean()) {
                        Socket player2 = serverSocket.accept();

                        Platform.runLater(() -> {
                            taLog.appendText(new Date() +
                                    ": Player 2 joined session " + sessionNo + '\n');
                            taLog.appendText("Player 2's IP address" +
                                    player2.getInetAddress().getHostAddress() + '\n');
                        });

                        // Notify that the player is Player 2
                        new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);

                        // Display this session and increment session number
                        Platform.runLater(() -> taLog.appendText(new Date() +
                                ": Start a thread for session " + sessionNo++ + '\n'));

                        // Launch a new thread for this session of two players
                        new Thread(new HandleASession(player1, player2)).start();
                    } else {
                        new Thread(new HandleAComputerSession(player1)).start();
                    }
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Class used to handle a session between a single player and a computer.
     */
    class HandleAComputerSession implements Runnable, Constants {
        private Socket player;
        
        private Connect4 board;
        
        private DataInputStream fromPlayer;
        private DataOutputStream toPlayer;
        
        
        public HandleAComputerSession(Socket player) {
            this.player = player;
            
            board = new Connect4();
        }
        
        public void run() {
            try {
                // Create data input and output streams
                fromPlayer = new DataInputStream(
                        player.getInputStream());
                toPlayer = new DataOutputStream(
                        player.getOutputStream());

                // Write anything to notify player 1 to start
                // This is just to let player 1 know to start
                toPlayer.writeInt(1);

                // Continuously serve the players and determine and report
                // the game status to the players
                while (true) {
                    // Receive a move from player 1
                    int column = fromPlayer.readInt();
                    int moveResult = board.addPiece(column, 'X');

                    // Check if Player 1 wins
                    if (moveResult == -1) {
                        toPlayer.writeInt(PLAYER1_WON);
                        break; // Break the loop
                    }

                    // Receive a move from Player 2
                    column = Connect4ComputerPlayer.makeMove();
                    moveResult = board.addPiece(column, 'O');

                    // Check if Computer Player wins
                    if (moveResult == -1) {
                        toPlayer.writeInt(PLAYER2_WON);
                        sendMove(toPlayer, moveResult, column);
                        break;
                    } else if (board.isFull()) { // Check if all cells are filled
                        toPlayer.writeInt(DRAW);
                        sendMove(toPlayer, moveResult, column);
                        break;
                    } else {
                        // Notify player 1 to take the turn
                        toPlayer.writeInt(CONTINUE);

                        // Send player 2's selected row and column to player 1
                        sendMove(toPlayer, moveResult, column);
                    }
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Class used to handle a session between two players.
     */
    class HandleASession implements Runnable, Constants {
        private Socket player1;
        private Socket player2;
        
        private Connect4 board;

        private DataInputStream fromPlayer1;
        private DataOutputStream toPlayer1;
        private DataInputStream fromPlayer2;
        private DataOutputStream toPlayer2;

        /** Construct a thread */
        public HandleASession(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;

            // Initialize cells
            board = new Connect4();
        }

        /** Implement the run() method for the thread */
        public void run() {
            try {
                // Create data input and output streams
                fromPlayer1 = new DataInputStream(
                        player1.getInputStream());
                toPlayer1 = new DataOutputStream(
                        player1.getOutputStream());
                fromPlayer2 = new DataInputStream(
                        player2.getInputStream());
                toPlayer2 = new DataOutputStream(
                        player2.getOutputStream());

                // Write anything to notify player 1 to start
                // This is just to let player 1 know to start
                toPlayer1.writeInt(1);

                // Continuously serve the players and determine and report
                // the game status to the players
                while (true) {
                    // Receive a move from player 1
                    int column = fromPlayer1.readInt();
                    int row = board.addPiece(column, 'X');

                    // Check if Player 1 wins
                    if (board.isWon(row, column, 'X')) {
                        toPlayer1.writeInt(PLAYER1_WON);
                        toPlayer2.writeInt(PLAYER1_WON);
                        sendMove(toPlayer2, row, column);
                        break; // Break the loop
                    } else {
                        // Notify player 2 to take the turn
                        toPlayer2.writeInt(CONTINUE);

                        // Send player 1's selected row and column to player 2
                        sendMove(toPlayer2, row, column);
                    }

                    // Receive a move from Player 2
                    column = fromPlayer2.readInt();
                    row = board.addPiece(column, 'O');

                    // Check if Player 2 wins
                    if (board.isWon(row, column, 'O')) {
                        toPlayer1.writeInt(PLAYER2_WON);
                        toPlayer2.writeInt(PLAYER2_WON);
                        sendMove(toPlayer1, row, column);
                        break;
                    } else if (board.isFull()) { // Check if all cells are filled
                        toPlayer1.writeInt(DRAW);
                        toPlayer2.writeInt(DRAW);
                        sendMove(toPlayer2, row, column);
                        break;
                    } else {
                        // Notify player 1 to take the turn
                        toPlayer1.writeInt(CONTINUE);

                        // Send player 2's selected row and column to player 1
                        sendMove(toPlayer1, row, column);
                    }
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Sends the most recent move to a player.
     * 
     * @param out DataOutputStream to be written to
     * @param row of the piece added
     * @param column of the piece added
     * @throws IOException 
     */
    private void sendMove(DataOutputStream out, int row, int column) 
            throws IOException {
        out.writeInt(row); // Send row index
        out.writeInt(column); // Send column index
    }

  /**
   * The main method is only needed for the IDE with limited
   * JavaFX support. Not needed for running from the command line.
   */
public static void main(String[] args) {
        launch(args);
    }
}
