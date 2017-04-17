package edu.cooper.ee.se.sp17.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ServerThread extends Thread {
	private Socket sock;
	private BufferedReader in;
	private DataOutputStream out;

	private String username;
	private int uid;

	private int gid;
	private boolean ingame = false;

	public ServerThread(Socket s) {
		sock = s;
		username = null;
		uid = -1;
		gid = -1;
	}

	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new DataOutputStream(sock.getOutputStream());

			println("Hello!");

			String line;
			while (true) {
				line = in.readLine();
				if (line == null || line.toUpperCase().startsWith("BYEBYE")) {
					println("Goodbye!");
					SetServer.master.terminate(this);
					sock.close();
					return;
				}

					// LOGIN
				String words[] = line.split("[ \t]");
				if (words[0].toUpperCase().startsWith("LOGIN")) {
					if (words.length == 3) {
						println(login(words[1], words[2]));
					} else {
						println("Invalid LOGIN command!");
					}
				} else if (words[0].toUpperCase().startsWith("REGISTER")) {
					if (words.length == 3) {
						println(register(words[1], words[2]));
					} else {
						println("Invalid REGISTER command!");
					}
				} else if (words[0].toUpperCase().equals("WHOAMI")) {
					println("User " + username + " has uid of " + uid);
				} else if (words[0].toUpperCase().equals("LOGOUT")) {
					println(logout());
				
					// GAMES
				} else if (words[0].toUpperCase().equals("GAMES")) {
					print(listGames());
				} else if (words[0].toUpperCase().equals("CREATE")) {
					if (uid < 0)
						println("You must be logged in to create a game");
					else if(ingame)
						println("You are playing a game");
					else if(gid > 0)
						println("You are already in a game");
					else {
						SetGame g;
						if (words.length == 1) {
							g = new SetGame(uid);
						} else {
							g = new SetGame(uid, Integer.parseInt(words[1]));
						}
						gid = SetServer.master.addGame(g);
						println("Your game number is " + gid);
					}
				} else if (words[0].toUpperCase().equals("LEAVE")){
					if(gid < 0)
						println("You are not in a game");
					else{
						SetGame sg = SetServer.master.getGame(gid);
						if(sg.getOwner() == uid){
							SetServer.master.removeGame(gid);
							gid = -1;
						}else{
							sg.remove(uid);
							gid = -1;
						}
					}
				} else if (words[0].toUpperCase().equals("JOIN")){
					if(words.length != 2)
						println("Invalid JOIN command");
					else if(uid < 0)
						println("You must be logged in to create a game");
					else if(ingame)
						println("You are playing a game");
					else if(gid > 0)
						println("You are already in a game");
					else{
						gid = Integer.parseInt(words[1]);
						SetGame sg = SetServer.master.getGame(gid);
						sg.join(uid);
					}
				} else if (words[0].toUpperCase().equals("START")){
					if(uid < 0)
						println("You must be logged in to start a game");
					else if(gid < 0)
						println("You are not in a game");
					else if(ingame)
						println("You are playing a game");
					else{
						SetGame sg = SetServer.master.getGame(gid);
						if(sg.getOwner() == uid)
							sg.start(gid);
						else
							println("You can't start this game. It is not yours");
					}
					
					// IN-GAME
				} else if (ingame){
					if(words[0].toUpperCase().equals("BOARD")){
						SetGame sg = SetServer.master.getGame(gid);
						print(sg.getBoard());
					}
				} else {
					println("Unrecognized command!");
				}
			}
		} catch (IOException e) {
			// e.printStackTrace(); // socket just probably closed
			SetServer.master.terminate(this);
			return;
		}
	}
	
	public void setIngame(boolean ig){
		ingame = ig;
	}

	private String logout() {
		if (uid >= 0) {
			uid = -1;
			username = null;
			return "User logged out successfully";
		}
		return "Not logged in";
	}

	private String register(String un, String pw) {
		switch (SetServer.master.register(un, pw)) {
		case SetServer.R_ERR_USER_EXISTS:
			return "User already exists";
		case SetServer.R_ERR_DATABASE:
			return "Database error";
		default:
			return "User registered successfully";
		}
	}

	private String login(String un, String pwh) {
		if(uid >= 0)
			return "Already logged in. Please logout first";

		uid = SetServer.master.login(un, pwh);
		switch (uid) {
		case SetServer.L_ERR_ALRDY_IN:
			return "User already logged in";
		case SetServer.L_ERR_INVALID_CREDS:
			return "Invalid user credentials";
		case SetServer.L_ERR_DATABASE:
			return "Database error";
		default:
			username = un;
			return "User logged in successfully";
		}
	}

	public void print(String s) throws IOException {
		out.writeBytes(s);
		out.flush();
	}

	public void println(String s) throws IOException {
		print(s + "\r\n");
	}

	public int getUid() {
		return uid;
	}
	
	public int getGid(){
		return gid;
	}

	public String getUsername() {
		return username;
	}

	public String listGames() {
		String res = "";
		HashMap<Integer, SetGame> gs = SetServer.master.getGames();

		for (Entry<Integer, SetGame> hme : gs.entrySet()) {
			SetGame g = hme.getValue();
			res += hme.getKey() + ": " + SetServer.master.getUsernameFromId(g.getOwner()) + "'s game (" + g.getCurrCap()
					+ "/" + g.getMaxCap() + ")" + ((hme.getKey() == gid)?"*\r\n":"\r\n");
		}
		res += "--END--\r\n";
		return res;
	}
}