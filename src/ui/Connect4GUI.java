/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import core.Connect4;
import core.Connect4ComputerPlayer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
        

/**
 * Connect4GUI works with Connect4.java and Connect4ComputerPlayer.java and 
 * JavaFX to provide a Graphical User Interface for a Connect4 game.
 * 
 * @author Alexander Rossiter
 * @version 1.0
 */
public class Connect4GUI extends Application {
    
    private char whoseTurn = 'X';
    private boolean gameOver = true;
    
    private Image redpiece = new Image("red.png");
    private Image blackpiece = new Image("black.png");
    
    Text playerText = new Text("BLACK'S TURN");
    Text bottomText = new Text("PLEASE SELECT GAME MODE");
    
    private Connect4 gameBoard = new Connect4();
    
    HBox buttonPane = new HBox();
    
    private MoveCell[] moveCells = new MoveCell[7];
    private Pane[][] boardCells = new Pane[6][7];
    
    /**
     * Overridden start method for JavaFX
     * 
     * @param primaryStage 
     */
    @Override
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
        
        Button computerPlayer = new Button("Play vs Computer");
        Button humanPlayer = new Button("2 Player game");
        computerPlayer.setOnAction(eh -> setComputerPlayer());
        humanPlayer.setOnAction(eh -> setHumanPlayer());
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
    }
    
    /**
     * Method used as a event handler for the computerPlayer button, initializes
     * a Computer v Player game.
     */
    private void setComputerPlayer() {
        gameOver = false;
        gameBoard.setGameMode('c');
        buttonPane.getChildren().remove(0, 2);
        bottomText.setText("");
    }
    
    /**
     * Method used as an event handler for the humanPlayer button, initializes 
     * a Player v Player game.
     */
    private void setHumanPlayer() {
        gameOver = false;
        gameBoard.setGameMode('p');
        buttonPane.getChildren().remove(0, 2);
        bottomText.setText("");
    }
    
    /**
     * Class for top Cells above board in order to handle mouse clicks when player
     * wants to make a move in the game. Shows current piece above each column
     * of board when mouse is hovered over, and places that piece in current column
     * on a mouse click.
     */
    private class MoveCell extends Pane {
        private final int col;      
        
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
            
            if (!gameOver) {
                makeMove(col);
            }
            
            //Make move on behalf of Computer player
            if (gameBoard.getGameMode() == 0 && !gameOver) {
                makeMove(Connect4ComputerPlayer.makeMove());
            }
        }
        
        /** 
         * Helper method used for either a User or Computer player to make a move
         * on the board.
         * 
         * @param column in which desired piece is to be placed
         */
        private void makeMove(int column) {
            
            int row = gameBoard.addPiece(column, whoseTurn);
           
            //If the piece was successfully added.
            if (row != -1) {
                gameOver = gameBoard.isWon(row, column, whoseTurn);
                if (whoseTurn == 'X') {
                    boardCells[row][column].getChildren().add(new ImageView(blackpiece));
                } else {
                    boardCells[row][column].getChildren().add(new ImageView(redpiece));
                }
                if (whoseTurn == 'X') {
                    whoseTurn = 'O';
                    playerText.setText("RED'S TURN");
                    playerText.setFill(Color.RED);
                } else {
                    whoseTurn = 'X';
                    playerText.setText("BLACK'S TURN");
                    playerText.setFill(Color.BLACK);
                }
            }
            //If the current player has won the game
            if (gameOver) {
                gameOver = true;
                playerText.setText("GAME OVER");
                playerText.setFill(Color.BLACK);
                if (whoseTurn == 'O') {
                    bottomText.setText("BLACK PLAYER WINS");
                } else {
                    bottomText.setText("RED PLAYER WINS");
                }
            }
        }
        
        /**
         * Handles mouse hover in order to display current players piece
         */
        private void handleMouseEnter() {
            if (!gameOver) {
                if (whoseTurn == 'X') {
                    this.getChildren().add(new ImageView(blackpiece));
                } else {
                    this.getChildren().add(new ImageView(redpiece));
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
     * Main method for launching GUI.
     * @param args 
     */
    public static void main(String args[]) {
        launch(args);
    }
}
