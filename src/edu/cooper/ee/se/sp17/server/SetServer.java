package edu.cooper.ee.se.sp17.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

	public static final int L_ERR_ALRDY_IN = -2;
	public static final int L_ERR_INVALID_CREDS = -3;
	public static final int L_ERR_INTERNAL = -4;

	public static final int R_ERR_USER_EXISTS = -1;
	public static final int R_ERR_INTERNAL = -2;

	private ServerSocket ss_listening;
	private ArrayList<ServerThread> threads;
	private HashMap<Integer, SetGame> games;

	public static void main(String[] args) {
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

		master.start();
	}

	public SetServer(int port, int max_clients) {
		this.port = port;
		this.max_clients = max_clients;
		threads = new ArrayList<ServerThread>(max_clients);
		games = new HashMap<Integer, SetGame>(MAX_GAMES);

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
					System.out.printf("%d: A connection from %s! Accepted.\n", threads.size(),
							s.getInetAddress().getHostAddress());
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
		// Query database for entry uid with un and pwh
		int uid = 1234;
		// or if not exists/invalid user/pass combo
		// uid = -1;
		// or server/db error
		boolean db_error = false;

		if (db_error) {
			return L_ERR_INTERNAL;
		}

		if (uid < 0) {
			return L_ERR_INVALID_CREDS;
		}

		for (ServerThread st : threads) {
			if (st.getUid() == uid) {
				return L_ERR_ALRDY_IN;
			}
		}

		return uid;
	}

	/* returns status */
	public int register(String un, String pw) {
		// Query database for below booleans
		boolean u_exists = true;
		boolean db_error = false;

		if (db_error) {
			return R_ERR_INTERNAL;
		}

		if (u_exists) {
			return R_ERR_USER_EXISTS;
		}

		return 0;
	}
	
	public HashMap<Integer, SetGame> getGames(){
		return games;
	}

	public String getValue(String name) {
		// Query database
		return name;
	}

	public synchronized int setValue(String name, String value) {
		// Query database
		return 0;
	}

	public synchronized void terminate(ServerThread st) {
		// Originally had it print threads.getIndex(st)
		// but index changes as elements are added/removed
		System.out.printf("A Connection terminated\n");
		threads.remove(st);
	}

	public String getUsernameFromId(int uid) {
		for (ServerThread st : threads) {
			if (st.getUid() == uid) {
				return st.getUsername();
			}
		}

		return null;
	}
	
	public int addGame(SetGame sg){
		// How to find open key value?
		int gid = 1;
		games.put(gid, sg);
		return gid;
	}
}