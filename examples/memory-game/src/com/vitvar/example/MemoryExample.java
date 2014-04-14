package com.vitvar.example;

import java.util.Random;

import memoryinterface.MemoryInterface;

import com.vitvar.tul.memory.MemoryServer;


public class MemoryExample implements MemoryInterface {

	private int[] cards = new int[28];
	private int player1cards = 0;
	private int player2cards = 0;	
	private int activePlayerInx = 0;
	
	public MemoryExample() {
		for (int i = 0; i < cards.length; i++) {
			// generating a random number from 1 to 10
			cards[i] = 1 + (int)(Math.random() * ((10 - 1) + 1));
		}
	}
	
	@Override
	public int getNumPlayers() {
		return 2;
	}

	@Override
	public void setNumCards(int NumCards) {
		cards = new int[NumCards];
		for (int i = 0; i < cards.length; i++) {
			// generating a random number from 1 to 10
			cards[i] = 1 + (int)(Math.random() * ((10 - 1) + 1));
		}		
	}

	@Override
	public int getNumCards() {
		return cards.length;
	}

	@Override
	public int[] getCardsOnBoard() {
		return cards;
	}

	@Override
	public int getNumCardsPlayer(int playerInx) throws Exception {
		if (playerInx > 1)
			throw new Exception("Player index out of bounds!");		
		return playerInx == 0 ? player1cards : player2cards;
	}

	@Override
	public boolean turnCards(int card1inx, int card2inx)
			throws Exception {
		Random random = new Random();
		boolean result = false;
		if (random.nextBoolean()) {
			if (activePlayerInx == 0) 
				player1cards++; 
			else 
				player2cards++;
			cards[card1inx] = 0;
			cards[card2inx] = 0;
			result = true;
		}
		
		activePlayerInx = activePlayerInx + 1;
		if (activePlayerInx > 1) 
			activePlayerInx = 0;
		return result;
	}

	@Override
	public int getActivePlayerInx() {
		return activePlayerInx;
	}

	@Override
	public boolean gameOver() {
		for (int i = 0; i < cards.length; i++)
			if (cards[i] != 0)
				return false;
		return true;
	}

	@Override
	public int winner() {
		return player1cards > player2cards ? 0 : 1;
	}
	

}
