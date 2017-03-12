package edu.cooper.ee.se.sp17.server;

import java.util.ArrayList;

public class SetGame {

	private static final int MAX_CAP = 4;

	private int uid_owner, max_cap;
	private ArrayList<Integer> players;

	public SetGame(int uid_owner, int max_cap){
		this.uid_owner = uid_owner;
		this.max_cap = max_cap;
		players = new ArrayList<Integer>(max_cap);
		players.add(uid_owner);
	}
	
	public SetGame(int uid_owner){
		this(uid_owner, MAX_CAP);
	}
	
	public int getOwner(){
		return uid_owner;
	}
	
	public void join(int uid){
		players.add(uid);
	}
	
	public int getMaxCap(){
		return max_cap;
	}
	
	public int getCurrCap(){
		return players.size();
	}
}
