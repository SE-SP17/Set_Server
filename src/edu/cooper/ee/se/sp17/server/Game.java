package edu.cooper.ee.se.sp17.server;

import java.util.ArrayList;

public class Game {

	private static final int MAX_CAP = 12;

	private int uid_owner, max_cap;
	private ArrayList<Player> players;
	private ArrayList<Card> board;
	private boolean started;

	public Game(Player p, int max_cap){
		this.uid_owner = p.getUid();
		this.max_cap = max_cap;
		players = new ArrayList<Player>(max_cap);
		started = false;
		
		players.add(p);
		p.setGid(uid_owner);
		
		board = new ArrayList<Card>(12);
		for(int i = 0; i < 12; i++){
			board.add(i, new Card());
		}
	}
	
	public Game(Player p){
		this(p, MAX_CAP);
	}
	
	public int getOwner(){
		return uid_owner;
	}
	
	public void join(Player p){
		SetServer.master.sendMsg(p.getUid(), "You've joined a game\r\n");
		players.add(p);
		p.setGid(uid_owner);
	}

	public void remove(Player p){
		SetServer.master.sendMsg(p.getUid(), "You've left a game\r\n");
		players.remove(p);
		p.setGid(-1);
	}	

	public void removeAll(){
		SetServer.master.sendMsg(getPlayersArray(), "You've left a game\r\n");
		for(Player p : players){
			p.setGid(-1);
		}
		players.removeAll(players);
	}	

	public int getMaxCap(){
		return max_cap;
	}
	
	public int getCurrCap(){
		return players.size();
	}

	public String start() {
		started = true;
		//SetServer.master.startAll(gid);
		SetServer.master.sendMsg(getPlayersArray(), "Game started!\r\n");
		return "";
	}
	
	public boolean isStarted(){
		return started;
	}
	
	public String getBoard(){
		String b = "";
		int i = 0;
		for(Card sc : board){
			b += i++ + ": " + sc.toString() + "\r\n";
		}
		return b;
	}
	
	public boolean isSet(int a, int b, int c){
		Card x = board.get(a);
		Card y = board.get(b);
		Card z = board.get(c);

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
	
	public int[] getPlayersArray(){
		int[] a = new int[players.size()];
		for(int i = 0; i < players.size(); i++){
			a[i] = players.get(i).getUid();
		}
			
		return a;
	}
	
	public ArrayList<Player> getPlayers(){
		return players;
	}
	
	public String set(int u, int a, int b, int c){
		String o = "";
		if(isSet(a, b, c)){
			board.set(a, new Card());
			board.set(b, new Card());
			board.set(c, new Card());
			
			o += a + ": " + board.get(a).toString() + "\r\n";
			o += b + ": " + board.get(b).toString() + "\r\n";
			o += c + ": " + board.get(c).toString() + "\r\n";
		}else{
			o += "Cards " + a + ", " + b + ", and " + c + " is not a set\r\n";
		}
		return o;
	}

	public String process(int uid, String[] cmd) {
		if(cmd[0].toUpperCase().equals("BOARD"))
			return getBoard();
		else if(cmd[0].toUpperCase().equals("SET")){
			if(cmd.length < 4)	return "SET needs 3 cards";
			return set(uid, Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), Integer.parseInt(cmd[3]));
		}
		return null;
	}
}
