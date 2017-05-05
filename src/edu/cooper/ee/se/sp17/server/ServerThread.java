/**
 * Set game server
 * @author Brian Hong
 */
package edu.cooper.ee.se.sp17.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerThread extends Thread {
	private Socket sock;
	private BufferedReader in;
	private DataOutputStream out;

	private int uid;

    /**
     * Constructor of ServerThread
     * @param s Socket of this connection
     */
	public ServerThread(Socket s) {
		sock = s;
		uid = -1;

        try{
		    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		    out = new DataOutputStream(sock.getOutputStream());
        }catch(IOException e){
            // Connection dropped, so terminate
            SetServer.master.terminate(this);
        }
	}


    @Override
	public void run() {
		try {
			println("Hello!"); // Welcome message!

			String line;
			while (true) {
				line = in.readLine();
				System.out.println("A User said: " + line);
				if (line == null || line.toUpperCase().startsWith("BYEBYE")) {
					println("Goodbye!");
					if(SetServer.master.isConnected(this)) // ^C would terminate connection if the above line
						SetServer.master.terminate(this);
					sock.close();
					return;
				}
				if(line.equals(""))
					continue;
				String words[] = line.split("[ \t]");
                String output = SetServer.master.processQuery(this, words);
                if(output != null) {
                    if(output.equals(""))
                    	continue;
                    println(output);
				}else
                	println("Unrecognized command!");
			}
		} catch (IOException e) {
			// e.printStackTrace(); // socket just probably closed
			SetServer.master.terminate(this);
			return;
		}
	}

    /**
     * Get method for socket
     * @return sock Socket of this ServerThread's connection
     */
    public Socket getSocket(){
        return sock;
    }

    /**
     * Gets UID
     * @return uid UID
     */
	public int getUid() {
		return uid;
	}

    /**
     * Set UID
     * @param u New UID
     */
    public void setUid(int u) {
        uid = u;
    }

    /**
     * Prints a line to the user
     * @param s String message
     */
	public void println(String s) {
		print(s + "\r\n");
	}

    /**
     * Prints a message to the user
     * @param s String message
     */
	public void print(String s){
        try{
		    out.writeBytes(s);
		    out.flush();
        }catch(IOException e){
            SetServer.master.terminate(this);
        }
	}
}
