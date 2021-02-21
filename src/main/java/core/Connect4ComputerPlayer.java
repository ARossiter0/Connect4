package core;

/**
 * Class that represents a Computer Player in Connect 4 game.
 * @author Alexander Rossiter
 * @version 1.0
 */
public class Connect4ComputerPlayer {
      
    /** 
     * Static method that returns random number in order to make moves on 
     * Connect4 board.
     * @return randomly generated integer between 0 and 6 inclusive
     */
    public static int makeMove() {
        return (int)(Math.random() * 7);
    }
}
