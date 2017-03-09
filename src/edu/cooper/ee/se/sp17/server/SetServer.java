package edu.cooper.ee.se.sp17.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public class SetServer {
	public static SetServer master;

	private static final int MAX_CLIENTS = 2;
	private int connected;
	ServerSocket ss_listening;
	private List<ServerThread> threads;

	public static void main(String[] args) {
		System.out.println("Hello world!");

		if (args.length < 2) {
			System.err.println("Fewer than 2 arguments provided. Need 2.");
			System.exit(-1);
		}

		master = new SetServer(args[0], Integer.parseInt(args[1]), MAX_CLIENTS);
	}

	public SetServer(String hostname, int port, int max_clients) {
		connected = 0;
		threads = new ArrayList<ServerThread>();
		//threads = Collections.synchronizedList(new ArrayList<ServerThread>());

		try {
			ss_listening = new ServerSocket(port);
			System.out.printf("Started server on port %d...\n", port);
			Socket s = null;

			while (true) {
				s = ss_listening.accept();
				
				// Synchronous operation
				/*
				for(ServerThread st_i: threads){
					if(st_i.isClosed()){
						System.err.printf("%d: Connection from %s dropped.\n", 0, s.getInetAddress().getHostAddress());
						threads.remove(st_i);
					}
				}
				*/

				for(Iterator<ServerThread> it = threads.iterator(); it.hasNext();){
					ServerThread st_i = it.next();
					if(st_i.isClosed()){
						System.err.printf("%d: Connection from %s dropped.\n", 0, st_i.getSocket().getInetAddress().getHostAddress());
						it.remove();
					}
				}

				if (threads.size() < max_clients) {
					ServerThread st = new ServerThread(s);
					threads.add(st);
					st.start();
					System.out.printf("%d: A connection from %s! Accepted.\n", threads.size(),
							s.getInetAddress().getHostAddress());
				} else {
					System.err.printf("Max connection limit of %d reached! Ignoring further connections...\n", max_clients);
					DataOutputStream bleh = new DataOutputStream(s.getOutputStream());
					bleh.writeBytes("Server's connection limit reached. Please try again later.\r\n");
					bleh.flush();
					bleh.close();
					s.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-2);
		}

	}

}