package edu.cooper.ee.se.sp17.server;

import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.cooper.ee.se.sp17.server.db.DBManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public class SetServer {
	public static SetServer master;
	private int port;
	private int max_clients;

	private static final int MAX_CLIENTS = 255;
	private static final int MAX_GAMES = 255;

	public static final int L_ERR_DATABASE = -1;
	public static final int L_ERR_ALRDY_IN = -2;
	public static final int L_ERR_INVALID_CREDS = -3;

	public static final int R_ERR_DATABASE = -1;
	public static final int R_ERR_USER_EXISTS = -2;

	private ServerSocket ss_listening;
	private ArrayList<ServerThread> threads;
	private HashMap<Integer, SetGame> games;

	private static int gid = 1;

	public static void main(String[] args) {
		System.out.println("ONE");
		if (args.length == 0) {
			System.err.println("No arguments provided. Exiting.");
			System.err.println("Usage: SetServer [port] <max_connections>");
			System.exit(-1);
		}

		if (args.length == 1) {
			System.err.println("One argument provided.");
			System.err.println("Defaulting to 255 max connections");
			master = new SetServer(Integer.parseInt(args[0]), MAX_CLIENTS);
		} else {
			master = new SetServer(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		}

		System.out.println("TWO");
		master.start();
	}

	public SetServer(int port, int max_clients) {
		this.port = port;
		this.max_clients = max_clients;
		threads = new ArrayList<ServerThread>(max_clients);
		games = new HashMap<Integer, SetGame>(MAX_GAMES);

		DBManager.init();

		try {
			ss_listening = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Couldn't open socket...");
			e.printStackTrace();
			System.exit(-2);
		}
	}

	private void start() {
		try {
			System.out.printf("Started server on port %d...\n", port);
			Socket s = null;
			while (true) {
				s = ss_listening.accept();

				if (threads.size() < max_clients) {
					ServerThread st = new ServerThread(s);
					System.out.printf("A connection from %s! Accepted.\n", s.getInetAddress().getHostAddress());
					threads.add(st);
					st.start();
				} else {
					System.err.printf("Max connection limit of %d reached! Ignoring further connections.\n",
							max_clients);
					DataOutputStream bleh = new DataOutputStream(s.getOutputStream());
					bleh.writeBytes("Server's connection limit reached. Please try again later.\r\n");
					bleh.flush();
					bleh.close();
					s.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-3);
		}
	}

	/* Implement next 4 functions to work with database */
	/* returns UID */
	public int login(String un, String pwh) {
		int uid = DBManager.login(un, pwh);

		if (uid == 0)
			return L_ERR_INVALID_CREDS;

		if (uid < 0)
			return L_ERR_DATABASE;

		for (ServerThread st : threads) {
			if (st.getUid() == uid) {
				return L_ERR_ALRDY_IN;
			}
		}

		return uid;
	}

	/* returns status */
	public int register(String un, String pw) {
		int stat = DBManager.register(un, pw);

		if (stat == 0)
			return R_ERR_USER_EXISTS;

		if (stat < 0)
			return R_ERR_DATABASE;

		return 0;
	}

	public HashMap<Integer, SetGame> getGames() {
		return games;
	}

	public synchronized void terminate(ServerThread st) {
		// Originally had it print threads.getIndex(st)
		// but index changes as elements are added/removed
		System.out.printf("A Connection terminated\n");
		threads.remove(st);

		// If in a game
		if (st.getGid() > 0) {
			SetGame sg = games.get(st.getGid());
			sendMsg(st.getGid(), -1, "User " + getUsernameFromId(st.getUid()) + " has left the game\r\n");
			sg.remove(st.getUid());

			// If owner of game
			if (st.getGid() == st.getUid()) {
				sendMsg(st.getGid(), -1, "This game will end\r\n");
				sg.removeAll();
			}
			removeGame(st.getGid());
		}
	}

	public String getUsernameFromId(int uid) {
		for (ServerThread st : threads) {
			if (st.getUid() == uid) {
				return st.getUsername();
			}
		}
		return null;
	}

	public int addGame(SetGame sg) {
		games.put(gid, sg);
		return gid++;
	}

	public void removeGame(int gid) {
		SetGame sg = games.get(gid);
		//for(ServerThread st : sg.)
		games.remove(gid);
	}

	public SetGame getGame(int gid) {
		return games.get(gid);
	}

	public void sendMsg(int gid, int uid, String msg) {
		for (ServerThread st : threads) {
			if ((st.getGid() == gid) && (uid == -1 || st.getUid() == uid))
				try {
					st.print(msg);
				} catch (IOException e) {
					e.printStackTrace();
					terminate(st);
				}
		}
	}

	public void startAll(int gid) {
		sendMsg(gid, -1, "Game starting\r\n");
		for (ServerThread st : threads) {
			st.setIngame(true);
		}
	}
}