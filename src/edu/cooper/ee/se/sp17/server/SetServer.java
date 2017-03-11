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
	ServerSocket ss_listening;

	public static void main(String[] args) {
		System.out.println("Hello world!");

		if (args.length < 2) {
			System.err.println("Fewer than 2 arguments provided. Need 2.");
			System.exit(-1);
		}

		master = new SetServer(args[0], Integer.parseInt(args[1]), MAX_CLIENTS);
	}

	public SetServer(String hostname, int port, int max_clients) {

		try {
			ss_listening = new ServerSocket(port);
			System.out.printf("Started server on port %d...\n", port);
			Socket s = null;

			while (true) {
				s = ss_listening.accept();
				
				System.out.printf("A connection from %s! Accepted.\n", s.getInetAddress().getHostAddress());
				new ServerThread(s).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-2);
		}

	}

}