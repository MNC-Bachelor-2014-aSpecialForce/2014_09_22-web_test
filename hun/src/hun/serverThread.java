package hun;

import java.io.*;
import java.net.*;

public class serverThread implements Runnable {
	public void run(){
		try {    
		    ServerSocket serverSocket = new ServerSocket(8080);
		    while(true){
				Socket insocket = serverSocket.accept( );
					
				BufferedReader in = new BufferedReader (new InputStreamReader(insocket.getInputStream()));
				//PrintWriter pOut = new PrintWriter (insocket.getOutputStream(), true);
			
			    String instring = in.readLine();//클라이언트로 부터 읽음
			    System.out.println("dd");
			    //out.println("get this: " + instring);
			    //pOut.println("The server got this: " + instring);//클라이언트로 보냄
			    insocket.close();
			}
		}
		catch (Exception e) {} 
	}
}
