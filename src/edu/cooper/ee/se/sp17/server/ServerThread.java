package edu.cooper.ee.se.sp17.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerThread extends Thread {

	private Socket sock;
	private BufferedReader in;
	private DataOutputStream out;
	private boolean closed;
	
	public boolean isClosed(){
		return closed;
	}

	public ServerThread(Socket s) {
		sock = s;
		closed = false;
	}

	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new DataOutputStream(sock.getOutputStream());

			out.writeBytes("Hello!\r\n");
			out.flush();

			String line;
			while (true) {
				line = in.readLine();
				if (line.equals("byebye")) {
					closed = true;
					sock.close();
					return;
				} else {
					//out.writeBytes("Unrecognized command!");
					out.writeBytes(line + "\r\n"); // Echo back
					out.flush();
				}
			}
		} catch (IOException e) {
			//e.printStackTrace(); // socket just probably closed
			closed = true;
			return;
		}
	}
	
	public Socket getSocket(){
		return sock;
	}
}