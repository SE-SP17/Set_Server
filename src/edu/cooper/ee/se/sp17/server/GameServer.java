package edu.cooper.ee.se.sp17.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class GameServer {
	private int size = 255;
	private ArrayList<Player> players;
	private HashMap<Integer, Game> games;


	public GameServer(int max_clients) {
		size = max_clients;
		players = new ArrayList<Player>(size);
		games = new HashMap<Integer, Game>();
	}

	public void login(int uid, String un) {
		players.add(new Player(uid, un));
	}

	public void logout(int uid) {
		Player p = getPlayer(uid);
		
		System.out.printf("User %s logged out\n", getUsername(uid));
		if(p != null){
			Game g = games.get(p.getGid());
			if(g.getOwner() == uid){
				g.removeAll();
				games.remove(uid);
			}else
				g.remove(p);
        }
	}
	
	public Player getPlayer(int uid){
		for(Player p : players)
			if(p.getUid() == uid)
				return p;
		return null;
	}

	public String process(int uid, String[] cmd) {
		Player p = getPlayer(uid);
		if (cmd[0].toUpperCase().equals("GAMES")) {
			String res = "";
			for (Entry<Integer, Game> hme : games.entrySet()) {
				Game g = hme.getValue();
				res += String.format("%d: %s's game (%d/%d)%s\r\n", hme.getKey(), getUsername(g.getOwner()), g.getCurrCap(), g.getMaxCap(), ((hme.getKey() == p.getGid())?"*":""));
			}
			res += "--END--";
			return res;
		} else if (cmd[0].toUpperCase().equals("CREATE")) {
			if(p.getGid() > 0)	return "You are already in a game";
			
            Game g = (cmd.length == 1)? new Game(p) : new Game(p, Integer.parseInt(cmd[1]));
			games.put(uid, g);
			return "Your game number is " + p.getGid();
		} else if (cmd[0].toUpperCase().equals("LEAVE")){
			if(p.getGid() < 0)	return "You are not in a game";
			
			Game g = games.get(p.getGid());
			if(g.getOwner() == uid){
				g.removeAll();
				games.remove(uid);
			}else{
				g.remove(p);
			}
			return "";
		} else if (cmd[0].toUpperCase().equals("JOIN")){
			if(cmd.length != 2)	return "Invalid JOIN command";
			if(p.getGid() > 0)	return"You are already in a game";
			
			Game g = games.get(Integer.parseInt(cmd[1]));
			if(g == null)		return "No game found with such id";
			g.join(p);
			return "";
		} else if (cmd[0].toUpperCase().equals("START")){
			if(p.getGid() < 0)							return "You are not in a game";
			else if(games.get(p.getGid()).isStarted())	return "You are already playing a game";
			else if(p.getGid() != p.getUid())			return "You can't start this game. It is not yours";
			else										return games.get(p.getGid()).start();
		} else if(cmd[0].toUpperCase().equals("LIST")){
			if(p.getGid() < 0)							return "You are not in a game";
			ArrayList<Player> pl = games.get(p.getGid()).getPlayers();
			String l = "";
			for(Player cp : pl)
				l += cp.getUid() + ": " + cp.getUsername() + "\r\n";
			return l.substring(0, l.length()-2);
		} else if(p.getGid() < 0 || !games.get(p.getGid()).isStarted())
			return "You're not playing a game";
		else
			return games.get(p.getGid()).process(uid, cmd);
	}

	public String getUsername(int uid) {
		Player p = getPlayer(uid);
		if(p == null)
			return "null";
		else
			return p.getUsername();
	}
	
	
	public void removeGame(int gid) {
		Game g = games.get(gid);
		g.removeAll();
		games.remove(gid);
	}
}
