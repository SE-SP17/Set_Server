package edu.cooper.ee.se.sp17.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

public class Game {

	private static final int MAX_CAP = 12;

	private int uid_owner, max_cap;
	private ArrayList<Player> players;
	private HashMap<Integer,Card> board;
	private ArrayList<Card> deck;
	private boolean started;
	Random rn;

	public Game(Player p, int max_cap){
		this.uid_owner = p.getUid();
		this.max_cap = max_cap;
		players = new ArrayList<Player>(max_cap);
		started = false;
		rn = new Random();
		
		players.add(p);
		p.setGid(uid_owner);
		
		// 4-D Loops FTW
		deck = new ArrayList<Card>();
		for(int i = 1; i < 4; i++){
			for(int j = 1; j < 4; j++){
				for(int k = 1; k < 4; k++){
					for(int l = 1; l < 4; l++){
						deck.add(new Card(i, j, k, l));
					}
				}
			}
		}
		
		board = new HashMap<Integer, Card>(12);
		for(int i = 0; i < 12; i++){
			int j = rn.nextInt(deck.size());
			board.put(i, deck.get(j));
			deck.remove(j);
		}
	}
	
	public Game(Player p){
		this(p, MAX_CAP);
	}
	
	public int getOwner(){
		return uid_owner;
	}
	
	public void join(Player p){
		SetServer.master.sendMsg(getPlayersArray(), "Player "+p.getUsername()+" has joined the game\r\n");
		players.add(p);
		p.setGid(uid_owner);
	}

	public void remove(Player p){
		players.remove(p);
		SetServer.master.sendMsg(getPlayersArray(), "Player "+p.getUsername()+" has left the game\r\n");
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
		for(Player p : players){
			p.setScore(0);
			p.setNomore(false);
		}
		
		deck.clear();
		for(int i = 1; i < 4; i++){
			for(int j = 1; j < 4; j++){
				for(int k = 1; k < 4; k++){
					for(int l = 1; l < 4; l++){
						deck.add(new Card(i, j, k, l));
					}
				}
			}
		}
		
		board.clear();
		for(int i = 0; i < 12; i++){
			int j = rn.nextInt(deck.size());
			board.put(i, deck.get(j));
			deck.remove(j);
		}
		
		SetServer.master.sendMsg(getPlayersArray(), "Game started!\r\n");
		return "";
	}
	
	public boolean isStarted(){
		return started;
	}
	
	public String getBoard(){
		String b = "";
		for(Entry<Integer,Card> hme : board.entrySet())
			b += hme.getKey() + ": " + hme.getValue().toString() + "\r\n";
		//b = b.substring(0, b.length()-2);
		return b;
	}
	
	public boolean isSet(int a, int b, int c){
		//return true; // Testing
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
	
	public Player getPlayer(int uid){
		for(Player p : players)
			if(p.getUid() == uid)
				return p;
		return null; //HOPEFULLY NEVER USED...
	}
	
	public String set(int u, int a, int b, int c){
		String o = "";
		Player p = getPlayer(u);
		if(a == b || b == c || a == c) return "Invalid cards";
		if(isSet(a, b, c)){
			if(deck.isEmpty()){
				if(board.isEmpty()){
					int highest = 0;
					int uid = 0;
					int sec = 0;
					for(Player cp : players){
						if(cp.getScore() > highest){
							highest = cp.getScore();
							uid = cp.getUid();
						}else if(cp.getScore() == highest){
							sec = cp.getUid();
						}
					}
					win((getPlayer(sec).getScore() != getPlayer(uid).getScore())?uid:0);
					return "";
				}
			}
			
			if(deck.isEmpty() || board.size() > 12){
				board.remove(a);
				board.remove(b);
				board.remove(c);
			}else{
				int j = rn.nextInt(board.size());
				board.put(a, deck.get(j));
				deck.remove(j);
				j = rn.nextInt(board.size());
				board.put(b, deck.get(j));
				deck.remove(j);
				j = rn.nextInt(board.size());
				board.put(c, deck.get(j));
				deck.remove(j);
				
				o += a + ": " + board.get(a).toString() + "\r\n";
				o += b + ": " + board.get(b).toString() + "\r\n";
				o += c + ": " + board.get(c).toString() + "\r\n";
			}
			
			p.point();
			SetServer.master.sendMsg(getPlayersArray(), "Player "+p.getUsername()+" has got a set ("+a+","+b+","+c+")\r\n");
			return "";
		}
		p.depoint();
		return "Cards " + a + ", " + b + ", and " + c + " is not a set";
	}

	public String process(int uid, String[] cmd) {
		if(cmd[0].toUpperCase().equals("BOARD"))	return getBoard()+"--END--";
		if(cmd[0].toUpperCase().equals("SET")){
			if(cmd.length < 4)						return "SET needs 3 cards";
													return set(uid, Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), Integer.parseInt(cmd[3]));
		}
		if(cmd[0].toUpperCase().equals("SCORE"))	return scores();
		if(cmd[0].toUpperCase().equals("NOMORE"))	return nomore(uid);
		if(cmd[0].toUpperCase().equals("END")){
			end(uid);
													return "";
		}
													return null; // NO commands found
	}

	private void win(int uid) {
		if(uid == 0)
			SetServer.master.sendMsg(getPlayersArray(), "There was no winner\r\nThe final scores are:\r\n"+scores()+"\r\n--END--\r\n");			
		else
			SetServer.master.sendMsg(getPlayersArray(), getPlayer(uid).getUsername() + " Won!\r\nThe final scores are:\r\n"+scores()+"\r\n--END--\r\n");
		started = false;
	}

	private void end(int uid) {
		SetServer.master.sendMsg(getPlayersArray(), getPlayer(uid).getUsername() + " has ended the game\r\nThe final scores are:\r\n"+scores()+"\r\n--END--\r\n");
		started = false;
	}

	private String nomore(int uid) {
		Player p = getPlayer(uid);
		p.setNomore(true);
		SetServer.master.sendMsg(getPlayersArray(), "Player "+p.getUsername()+" called that there are NO MORE sets\r\n");
		for(Player cp : players)
			if(cp.getNomore() == false)
				return "";
		
		for(Player cp : players)
			cp.setNomore(false);
		
		if(deck.size() == 0){
			end();
			return "";
		}
		
		int a = -1;
		int b = -1;
		int c = -1;
		
		// Find card with the highest location
		int h = -1;
		for(int i : board.keySet()){
			if(i > h)
				h = i;
		}
		
		// Look for open spots
		for(int i = 0; i < h+1; i++){
			if(board.get(i) == null){
				if(a < 0)
					a = i;
				else if(b < 0)
					b = i;
				else if(c < 0)
					c = i;
			}
		}
		
		if(a < 0){
			a = board.size();
			b = board.size()+1;
			c = board.size()+2;
		}else if(b < 0){
			b = board.size()+1;
			c = board.size()+2;
		}else if(c < 0){
			c = board.size()+2;
		}
		
		//System.err.printf("%d %d %d\n", a, b, c);
		//System.err.println(deck.size());
		
		int j = rn.nextInt(deck.size());
		board.put(a, deck.get(j));
		deck.remove(j);
		j = rn.nextInt(deck.size());
		board.put(b, deck.get(j));
		deck.remove(j);
		j = rn.nextInt(deck.size());
		board.put(c, deck.get(j));
		deck.remove(j);

//		String o = "";
//		o += a + ": " + board.get(a).toString() + "\r\n";
//		o += b + ": " + board.get(b).toString() + "\r\n";
//		o += c + ": " + board.get(c).toString() + "\r\n";
				
//		SetServer.master.sendMsg(getPlayersArray(), o);
		return "";
	}

	public void end() {
		SetServer.master.sendMsg(getPlayersArray(), "Game has ended\r\nThe final scores are:\r\n"+scores()+"\r\n--END--\r\n");
		started = false;
	}

	private String scores() {
		String s = "";
		for(Player p : players)
			s += p.getUsername() + ": " + p.getScore() + "\r\n";
		return s.substring(0, s.length()-2);
	}
}
