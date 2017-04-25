/**
 * Set server
 * @author Brian Hong
 */
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
import java.net.ServerSocket;

public class SetServer {
	public static SetServer master;
	private static GameServer gserver;
	private int port;
	private int max_clients;

    // Defaults
	private static final int MAX_CLIENTS = 255;

    // Error Codes
	public static final int L_ERR_DATABASE = -1;
	public static final int L_ERR_ALRDY_IN = -2;
	public static final int L_ERR_INVALID_CREDS = -3;

	public static final int R_ERR_DATABASE = -1;
	public static final int R_ERR_USER_EXISTS = -2;

    // Server Objects
	private ServerSocket ss_listening;
	private ArrayList<ServerThread> connections;

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

        // Start master SetServer
		master.start();
	}

    /**
     * Constructor for SetServer class/object.
     * @param port Port the server listens on
     * @param max_clients Maximum number of clients that can be connected at any time
     */
	public SetServer(int port, int max_clients) {
		this.port = port;
		this.max_clients = max_clients;
		gserver = new GameServer(max_clients);

        // Init and start listening
		try {
		    connections = new ArrayList<ServerThread>(max_clients);
			ss_listening = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Couldn't open socket...");
			e.printStackTrace();
			System.exit(-2);
		}
	}
	
    /**
     * Starts the SetServer and listens for connections.
     */
	private void start() {
		try {
			System.out.printf("Started server on port %d...\n", port);
			Socket s = null;
            ServerThread st = null;

            // Listen loop
			while (true) {
				s = ss_listening.accept();

                // Accept
				if (connections.size() < max_clients) {
					st = new ServerThread(s);
					System.out.printf("A connection from %s! Accepted.\n", s.getInetAddress().getHostAddress());
					connections.add(st);
					st.start();

                // Reject
				} else {
					System.err.printf("Max connection limit of %d reached! Ignoring further connections.\n", max_clients);
					DataOutputStream bleh = new DataOutputStream(s.getOutputStream());
					bleh.writeBytes("Server's connection limit reached. Please try again later.\r\n");
					bleh.flush();
					bleh.close();
					s.close();
				}
			}
		} catch (IOException e) {
            // Some error or socket closed
			e.printStackTrace();
			System.exit(-3);
		}
	}

    /**
     * Sends message to a list of users
     * Sends to all users if any UID is -1 (everytime)
     * @param uid An array of UIDs to send the message to
     * @param msg The message to send
     */
    public void sendMsg(int[] uid, String msg){
        for(int i : uid)
            sendMsg(i, msg);
    }

    /**
     * Sends message to a user
     * Sends to all users if UID is -1
     * @param uid The UID of the user to send the message to
     * @param msg The message to send
     */
    public void sendMsg(int uid, String msg){
        for(ServerThread st : connections)
            if(uid == -1 | st.getUid() == uid)
                st.print(msg);
    }

    /**
     * Performs login check.
     * Returns an int, which is either:
     *  uid          if greater than 0
     *  error code   if less than 0
     *
     * @param un String username
     * @param pw String password
     * @return uid UID of user or error code
     */
	public int login(String un, String pw) {
		int uid = DBManager.login(un, pw);

        // No such login
		if (uid == 0)
			return L_ERR_INVALID_CREDS;

        // DB Error
		if (uid < 0)
			return L_ERR_DATABASE;

        // Check if already logged in
		for (ServerThread st : connections)
			if (st.getUid() == uid)
				return L_ERR_ALRDY_IN;

        // Success!
		return uid;
	}

    /**
     * Registers a new user
     * Returns a status code:
     *  0 if success
     *  or ERROR CODE
     * @param un String username
     * @param pw String password
     */
	public int register(String un, String pw) {
		int stat = DBManager.register(un, pw);

        // User already exist
		if (stat == 0)
			return R_ERR_USER_EXISTS;

        // DB Error
		if (stat < 0)
			return R_ERR_DATABASE;

        // Success!
		return 0;
	}
	
	public boolean isConnected(ServerThread st){
		return connections.contains(st);
	}

    /**
     * This method is (should be) called when a
     * connection is terminated/closed/dropped.
     * It is called in most IOException try-catch.
     * @param st ServerThread being terminated.
     */
	public synchronized void terminate(ServerThread st) {
		System.out.printf("A Connection from %s terminated.\n", st.getSocket().getInetAddress().getHostAddress());
		connections.remove(st);

        // Call method in game server to handle stuff
		if(st.getUid() > 0)
			gserver.logout(st.getUid());
	}

    /**
     * Processes query from ServerThreads.
     * Takes care of login and stuff,
     * all other commands are passed through to GameServer
     * @param c ServerThread That the command is for.
     * @param cmd User commandline split into cmd.
     * @return res Resulting message from processing query.
     */
    public String processQuery(ServerThread c, String[] cmd){
		if (cmd[0].toUpperCase().equals("LOGIN")) {
			if (cmd.length != 3)	return "Invalid LOGIN command!";
			if(c.getUid() >= 0)		return "Already logged in. Please logout first";

			int uid = SetServer.master.login(cmd[1], cmd[2]);
			
			switch (uid) {
				case SetServer.L_ERR_ALRDY_IN:		return "User already logged in";
				case SetServer.L_ERR_INVALID_CREDS:	return "Invalid user credentials";
				case SetServer.L_ERR_DATABASE:		return "Database error";
				default:
					c.setUid(uid);
					gserver.login(uid, cmd[1]);
					System.out.printf("User %s logged in from %s\n", cmd[1], c.getSocket().getInetAddress().getHostAddress());
					return "User logged in successfully";
			}
		} else if (cmd[0].toUpperCase().equals("REGISTER")) {
			if (cmd.length != 3)
				return "Invalid REGISTER command!";
			
			switch (SetServer.master.register(cmd[1], cmd[2])) {
				case SetServer.R_ERR_USER_EXISTS:	return "User already exists";
				case SetServer.R_ERR_DATABASE:		return "Database error";
				default:							return "User registered successfully";
			}
		} else if (cmd[0].toUpperCase().equals("WHOAMI")) {
			int uid = c.getUid();
			String un = gserver.getUsername(uid);
			return "User " + un + " has uid of " + uid;
		} else if (cmd[0].toUpperCase().equals("LOGOUT")) {
			if (c.getUid() >= 0) {
				gserver.logout(c.getUid());
				c.setUid(-1);
				return "User logged out successfully";
			}
			return "Not logged in";
		}else if(c.getUid() < 0){
			return "Please log in first";
		}else{
			return gserver.process(c.getUid(), cmd);
		}
    }
}
