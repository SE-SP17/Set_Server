package edu.cooper.ee.se.sp17.server;

import java.util.ArrayList;

public class SetGame {

	private static final int MAX_CAP = 4;

	private int uid_owner, max_cap;
	private ArrayList<Integer> players;
	private ArrayList<SetCard> board;

	public SetGame(int uid_owner, int max_cap){
		this.uid_owner = uid_owner;
		this.max_cap = max_cap;
		players = new ArrayList<Integer>(max_cap);
		players.add(uid_owner);
		board = new ArrayList<SetCard>(12);
		
		for(int i = 0; i < 12; i++){
			board.add(new SetCard());
		}
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

	public void remove(int uid){
		players.remove(new Integer(uid));
	}	

	public int getMaxCap(){
		return max_cap;
	}
	
	public int getCurrCap(){
		return players.size();
	}

	public void start(int gid) {
		SetServer.master.startAll(gid);
		SetServer.master.sendMsg(gid, -1, getBoard());
	}
	
	public String getBoard(){
		String b = "";
		for(SetCard sc : board){
			b += sc.toString() + "\r\n";
		}
		return b;
	}
}
