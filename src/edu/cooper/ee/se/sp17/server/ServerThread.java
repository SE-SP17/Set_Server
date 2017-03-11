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
	
	private String username;
	private int uid;
	
	private enum r_ret{
		SUCCESS, USER_EXISTS, INTERNAL_ERROR, ETC_GO_FUCK_YOURSELF
	}

	private enum l_ret{
		SUCCESS, INVALID_CREDENTIALS, ALREADY_LOGGED_IN, INTERNAL_ERROR
	}

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

			println("Hello!");

			String line;
			while (true) {
				line = in.readLine();
				if (line == null || line.toUpperCase().startsWith("BYEBYE")) {
					println("Goodbye!");
					closed = true;
					sock.close();
					return;
				}
				
				String words[] = line.split("[ \t]");
				if(words[0].toUpperCase().startsWith("LOGIN")){
					if(words.length == 3){
						switch(login(words[1], words[2])){
						case SUCCESS:
							println("Logged in!");
							break;
						case INVALID_CREDENTIALS:
							println("Invalid LOGIN credentials!");
							break;
						case ALREADY_LOGGED_IN:
							println("User is already logged in!");
							break;
						case INTERNAL_ERROR:
							println("Internal error!");
						}
					}else{
						println("Invalid LOGIN command!");
					}
				} else if(words[0].toUpperCase().startsWith("REGISTER")){
					if(words.length == 3){
						switch(register(words[1], words[2])){
						case SUCCESS:
							println("User account registered successfully!");
							break;
						case USER_EXISTS:
							println("Username already exists!");
							break;
						case INTERNAL_ERROR:
							println("Error: Internal error!");
							break;
						case ETC_GO_FUCK_YOURSELF:
							println("Etc. Error");
						}
					}else{
						println("Invalid REGISTER command!");
					}
				} else {
					println("Unrecognized command!");
				}
			}
		} catch (IOException e) {
			//e.printStackTrace(); // socket just probably closed
			closed = true;
			return;
		}
	}
	
	private r_ret register(String un, String pw) {
		// Do database lookup and add
		return r_ret.SUCCESS;
	}

	private l_ret login(String un, String pwh) {
		// Do database lookup
		// pwd is the hased password. Hashing algorithm not determined yet.
		return l_ret.SUCCESS;
	}

	private void print(String s) throws IOException{
		out.writeBytes(s);
		out.flush();
	}

	private void println(String s) throws IOException{
		print(s + "\r\n");
	}
	
	public Socket getSocket(){
		return sock;
	}
}