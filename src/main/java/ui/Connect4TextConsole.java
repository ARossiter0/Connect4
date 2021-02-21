package ui;

import core.Connect4;
import core.Connect4ComputerPlayer;
import java.util.Scanner;

/**
 * Connect4TextConsole class that contains main method for
 * connect4 and also contains method for printing connect4 board.
 * @author Alexander Rossiter
 * @version 2.0
 */
public class Connect4TextConsole {

	public static void main(String[] args) {
            
            Scanner in = new Scanner(System.in);
            Connect4 board = new Connect4();
            boolean gameOver = false;
            boolean playerTurn = true;
            char playerPiece = 'X';
            
            displayBoard(board.getBoard());
            System.out.println("Begin Game. Enter ‘P’ if you want to play "
                    + "against another player; enter ‘C’ to play against "
                    + "computer.");
            char mode = in.next().charAt(0);
            try {
                board.setGameMode(mode);
            } catch (IllegalArgumentException ex){
                System.out.println("Invalid Input: '" + mode + "' \nGame mode "
                        + "set to default: ");
            }
            mode = board.getGameMode();
            if (mode == 0) {
                System.out.print("Computer v Player");
            } else {
                System.out.print("Player v Player");
            }
            while (!gameOver) {
                int row;
                int column;
                System.out.println("\nPlayer" + playerPiece + " – your turn. "
                        + "Choose a column number from 1-7.");
                if (playerTurn == true) {
                    column = in.nextInt() - 1;
                    row = board.addPiece(column, playerPiece);
                    if (mode == 0) playerTurn = false;
                } else {
                    column = Connect4ComputerPlayer.makeMove();
                    row = board.addPiece(column, playerPiece);
                    playerTurn = true;
                }
                //If piece was not added
                if (row == -1) {
                    if (board.isFull()) {
                        System.out.println("Tie, please try again.");
                        gameOver = true;
                    } else {
                        System.out.println("Please select valid column");
                    }
                } else if (board.isWon(row, column, playerPiece)) {
                    displayBoard(board.getBoard());
                    System.out.println("Player " + playerPiece + " won the game!");
                    gameOver = true;
                } else {
                    displayBoard(board.getBoard());
                    if (playerPiece == 'X') playerPiece = 'O';
                    else playerPiece = 'X';
                }
            }
	}

        /**
         * Displays the current state of the Connect4 board.
         * @param board
         */
	public static void displayBoard(char[][] board) {
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 7; c++) {
                    System.out.print("|" + board[r][c]);
                }
                System.out.print("|\n");
            }
            System.out.println("---------------");
            System.out.println("|1|2|3|4|5|6|7|");
	}

}
