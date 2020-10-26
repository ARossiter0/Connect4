package core;

import java.util.InputMismatchException;
import java.util.Scanner;
import ui.Connect4GUI;
import ui.Connect4TextConsole;

/**
 * Connect4 class that contains all logic needed to run
 * a game of connect 4.
 * @author Alexander Rossiter
 * @version 2.0
 */
public class Connect4 {

        /**
         * 2D array that stores board data, 0 represents empty space
         * 1 represents X, 2 represents O
         */
	private char[][] board;
        private char gameMode;
        
        public static void main(String args[]) {
            System.out.println("Please type 'G' for a GUI or 'T' for a "
                    + "Text-Based Interface");
            Scanner in = new Scanner(System.in);
            char input;
            try {
                input = in.next().charAt(0);
                if (input != 'g' && input != 'G' && input != 't' && input != 'T') {
                    throw new InputMismatchException();
                }
            } catch (InputMismatchException ex) {
                System.out.println("Invalid input: Default to Text-Based Interface");
                input = 't';
            }
            if (input == 't' || input == 'T') {
                Connect4TextConsole.main(args);
            } else {
                Connect4GUI.main(args);
            }
        }

        /**
         * Constructor, creates an empty board of pieces with 6 rows 
         * and 7 columns
         */
	public Connect4() {
            gameMode = 0;
            board = new char[6][7];
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 7; c++) {
                    board[r][c] = ' ';
                }
            }
	}

        /**
         * Returns current state of board
         * @return board
         */
	public char[][] getBoard() {
            return board;
	}

        /**
         * Adds a piece to the current board
         * @param column where the user wants to add the piece
         * @param piece type of piece to be added, 1 for X, 2 for O
         * @return int row in which the piece added ended up in. Returns -1
         * if piece was not added.
         */
	public int addPiece(int column, char piece) {
            if (column < 0 || column > 6 || this.hasPiece(0, column)) {
                return 0;
            }
            for (int i = 0; i < 6; i++) {
                if (i == 5 || !hasPiece(i, column) && hasPiece(i + 1, column)) {
                    board[i][column] = piece;
                    return i;
                }
            }   
            return -1;
	}

        /**
         * Helper method to determine if recently added piece resulted in
         * a victory
         * @param row starting row
         * @param column starting column
         * @param piece the piece to be checked
         * @return true if owner of current piece has won, false if not.
         */
	public boolean isWon(int row, int column, char piece) {
            int matches = 0;
            int tempCol = column;
            int tempRow = row;
            //Check below
            if (row < 3) {
                for (int i = 1; board[row + i][column] == piece; i++) {
                    if (i == 3) {
                        return true;
                    }
                }
            }
            //Check horizontal
            while (tempCol > 0 && board[row][tempCol - 1] == piece) {
                matches++;
                tempCol--;
            }
            tempCol = column;
            while (tempCol < 6 && board[row][tempCol + 1] == piece) {
                matches++;
                tempCol++;
            }
            if (matches >= 3) {
                return true;
            }
            matches = 0;
            tempCol = column;
            
            //Check left diagonal
            while(tempRow < 5 && tempCol > 0 && board[tempRow + 1][tempCol - 1] == piece) {
                matches++;
                tempRow++;
                tempCol--;
            }
            tempCol = column;
            tempRow = row;
            while (tempRow > 0 && tempCol < 6 && board[tempRow - 1][tempCol + 1] == piece) {
                matches++;
                tempRow--;
                tempCol++;
            }
            if (matches >= 3) {
                return true;
            }
            matches = 0;
            tempCol = column;
            tempRow = row;
            //Check right diagonal
            while(tempRow < 5 && tempCol < 6 && board[tempRow + 1][tempCol + 1] == piece) {
                matches++;
                tempRow++;
                tempCol++;
            }
            tempCol = column;
            tempRow = row;
            while (tempRow > 0 && tempCol > 0 && board[tempRow - 1][tempCol - 1] == piece) {
                matches++;
                tempRow--;
                tempCol--;
            }
            if (matches >= 3) {
                return true;
            }
            return false;
	}

        /** 
         * Helper method used by addPiece() to check a specific spot on the 
 board whether or not there is a piece currently placed there.
         * @param row
         * @param column
         * @return true - there is a piece, false - there is no piece
         */
	private boolean hasPiece(int row, int column) {
            if (board[row][column] == ' ') {
                return false;
            }
            return true;
	}

        /**
         * Determines whether the board is full of pieces
         * @return true if board is full, false if not
         */
        public boolean isFull() {
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 7; c++) {
                    if (!hasPiece(r, c)) {
                        return false;
                    }
                }
            }
            return true;
        }
        
        /**
         * Takes in a character input and determines whether or not it is a 
         * valid input, if valid, sets gameMode to 0 for player v computer or 
         * 1 for player v player
         * @param mode character input from user
         * @throws IllegalArgumentException if character input isn't a 'C' or 'P'
         */
        public void setGameMode(char mode) throws IllegalArgumentException {
            if (mode != 'c' && mode != 'C' && mode != 'p' && mode != 'P') {
                throw new IllegalArgumentException("Invalid Input: " + mode);
            }
            if (mode == 'c' || mode == 'C') {
                gameMode = 0;
            } else {
                gameMode = 1;
            }
        }
        
        /**
         * Returns current game mode set by user
         * @return 0 for computer v player, 1 for player v player
         */
        public char getGameMode() {
            return gameMode;
        }
}
