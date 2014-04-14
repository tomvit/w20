/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package memoryinterface;

/**
 *
 * @author Jana Vitvarova
 */
public interface MemoryInterface {
    /**
     * Sets number of cards for the game 
     * @param NumCards
     * @throws Exception if the number of cards is odd 
     */
    public void setNumCards(int NumCards) throws Exception;
    
    
    /**
     * Returns the index of the player that is on turn. At the beginning the 
     * player with index 0 is on turn.
     * @return  
     */
    public int getActivePlayerInx();

    /**
     * Getter for number of cards
     * @return 
     */
    public int getNumCards();
    
    /**
     * Getter for number of players, for this implementation
     * it is fixed to 2, player with index 0 and player with index 1 
     * @return 
     */
    public int getNumPlayers();
    
    /**
     * Returns values of cards, already turned cards have value 0 
     * @return 
     */
    int[] getCardsOnBoard();
    
    /**
     * 
     * @param card1inx position of the first card in 1 dimentional array, from 0
     * @param card2inx position of the second card in 1 dimentional array, from 0
     * @return true if the two cards are same, false when they are different 
     * @throws Exception when the cards ware already turned or there are no such cards
     */
    public boolean turnCards(int card1inx, int card2inx) throws Exception;
    
    /**
     * 
     * @param playerInx
     * @return number of pairs that the player with the given player index
     * has already found
     * @throws Exception when there is no such playerInx
     */
    public int getNumCardsPlayer(int playerInx) throws Exception;
    
    /**
     * 
     * @return true if the game is over, there are no more cards to be turned
     */
    public boolean gameOver();

    /**
     * 
     * @return the index of the winner
     */
    public int winner();   
}
