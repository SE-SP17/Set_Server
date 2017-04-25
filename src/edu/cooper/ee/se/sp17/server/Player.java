package edu.cooper.ee.se.sp17.server;

public class Player {

	private int uid, gid;
	private String username;
	private int score;
	private boolean nomore;

	public Player(int uid, String username){
		this.uid = uid;
		this.username = username;
		gid = -1;
		score = 0;
		nomore = false;
	}

	// Getters and Setters
	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}
	
	public void point(){
		score++;
	}

	public int getScore(){
		return score;
	}
	
	public void setScore(int s){
		score = s;
	}

	public void depoint() {
		score--;
	}
	
	public void setNomore(boolean nm){
		nomore = nm;
	}
	
	public boolean getNomore(){
		return nomore;
	}
}