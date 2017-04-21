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

	public void removeAll(){
		players.removeAll(players);
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
		int i = 0;
		for(SetCard sc : board){
			b += i++ + ": " + sc.toString() + "\r\n";
		}
		return b;
	}
	
	public boolean isSet(int a, int b, int c){
		SetCard x = board.get(a);
		SetCard y = board.get(b);
		SetCard z = board.get(c);

		if(x.color == y.color && x.color != z.color)
			return false;
		else if(x.color == z.color && x.color != y.color)
			return false;
		else if(y.color == z.color && x.color != y.color)
			return false;
		
		if(x.num == y.num && x.num != z.num)
			return false;
		else if(x.num == z.num && x.num != y.num)
			return false;
		else if(y.num == z.num && x.num != y.num)
			return false;
		
		if(x.shape == y.shape && x.shape != z.shape)
			return false;
		else if(x.shape == z.shape && x.shape != y.shape)
			return false;
		else if(y.shape == z.shape && x.shape != y.shape)
			return false;
		
		if(x.shade == y.shade && x.shade != z.shade)
			return false;
		else if(x.shade == z.shade && x.shade != y.shade)
			return false;
		else if(y.shade == z.shade && x.shade != y.shade)
			return false;
		
		return true;
	}
	
	public String set(int u, int a, int b, int c){
		String o = "";
		if(isSet(a, b, c)){
			board.set(a, new SetCard());
			board.set(b, new SetCard());
			board.set(c, new SetCard());
			
			o += a + ": " + board.get(a).toString() + "\r\n";
			o += b + ": " + board.get(b).toString() + "\r\n";
			o += c + ": " + board.get(c).toString() + "\r\n";
		}else{
			o += "Cards " + a + ", " + b + ", and " + c + " is not a set\r\n";
		}
		return o;
	}
	
	public String term(int uid){
		
		return "";
	}
}
