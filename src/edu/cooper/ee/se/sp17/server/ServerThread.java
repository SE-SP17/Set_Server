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
				} else if (words[0].toUpperCase().equals("GAMES")) {
					print(listGames());
				} else if (words[0].toUpperCase().equals("CREATE")) {
					if (uid < 0) {
						println("You must be logged in to create a game");
					} else {
						SetGame g;
						if (words.length == 1) {
							g = new SetGame(uid);
						} else {
							g = new SetGame(uid, Integer.parseInt(words[1]));
						}
						gid = SetServer.master.addGame(g);
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
		case SetServer.R_ERR_INTERNAL:
			return "Internal error";
		default:
			return "User registered successfully";
		}
	}

	private String login(String un, String pwh) {
		uid = SetServer.master.login(un, pwh);
		switch (uid) {
		case SetServer.L_ERR_ALRDY_IN:
			return "User already logged in";
		case SetServer.L_ERR_INVALID_CREDS:
			return "Invalid user credentials";
		case SetServer.L_ERR_INTERNAL:
			return "Internal error";
		default:
			username = un;
			return "User logged in successfully";
		}
	}

	private void print(String s) throws IOException {
		out.writeBytes(s);
		out.flush();
	}

	private void println(String s) throws IOException {
		print(s + "\r\n");
	}

	public int getUid() {
		return uid;
	}

	public String getUsername() {
		return username;
	}

	public String listGames() {
		String res = "";
		HashMap<Integer, SetGame> gs = SetServer.master.getGames();

		for (Entry<Integer, SetGame> hme : gs.entrySet()) {
			SetGame g = hme.getValue();
			res += hme.getKey() + ": " + SetServer.master.getUsernameFromId(g.getOwner()) + "'s room (" + g.getCurrCap()
					+ "/" + g.getMaxCap() + ")\r\n";
		}

		return res;
	}
}