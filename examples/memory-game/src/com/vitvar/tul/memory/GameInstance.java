package com.vitvar.tul.memory;

import java.util.ArrayList;

import memoryinterface.MemoryInterface;

public class GameInstance {
	
	private long id;
	private MemoryInterface gameInstance = null;
	private ArrayList<Boolean> slots = new ArrayList<Boolean>();
	private int turnedCard1 = -1;
	private int turnedCard2 = -1;
	
	public GameInstance(long id, MemoryInterface gameInstance) {
		this.gameInstance = gameInstance;
		this.id = id;
	}
	
	public String getKey() {
		return Long.toHexString(id);
	}
	
	public void turnCard(int inx) {
		if (turnedCard1 == -1)
			turnedCard1 = inx;
		else
			if (turnedCard2 == -1 && turnedCard1 != inx) {
				turnedCard2 = inx;
			}
	}

	public void move() throws Exception {
		if (turnedCard1 != -1 && turnedCard2 != -1) {
			gameInstance.turnCards(turnedCard1, turnedCard2);
			turnedCard1 = -1;
			turnedCard2 = -1;
		}		
	}
	
	public int getTurnedCard1() {
		return turnedCard1;
	}
	
	public synchronized int getTurnedCard2() {
		return turnedCard2;
	}
	
	/*public String createSlot() {
	}*/
	
	public MemoryInterface getGameInstance() {
		return gameInstance;
	}

}
