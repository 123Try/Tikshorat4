package multithreadedServer;

/*
 * This class implements a simple multithreaded server 
 * that processes the incoming requests in the one thread (is called a listening thread)
 * and hands off the client connection to a other thread (is called a worker thread) that will process the request. 
 * --- one thread per each client connection ---
 */

/**
 *
 * @author annaf
 */

import java.io.*;
import java.net.*;

public class SimpleMultithreadedServer implements Runnable{

    int          serverPort   = 45000; // The port that this server is listening on
    ServerSocket serverSocket = null;  // Server socket that will listen for incoming connections
    Thread       runningThread = null;
    boolean      isStopped    = false;

    public SimpleMultithreadedServer(int port){
        this.serverPort = port;
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        //open server Socket
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            System.err.println("Cannot listen on this port.\n" + e.getMessage());
            System.exit(1);
        }    
            while(!isStopped()){
                Socket clientSocket = null;  // socket created by accept
                
                try {
                       clientSocket = this.serverSocket.accept(); // wait for a client to connect
                       
                } catch (IOException e) {
                       if(isStopped()) {
                                System.out.println("IOException: Server Stopped.") ;
                                return;
                       }
                       throw new RuntimeException(
                                "Error accepting client connection", e);    //Accept failed
                }
                //server code here ...
                System.out.println("Server accepts the client connection");
                
                // Client information               
                InetAddress addr = clientSocket.getInetAddress();
                System.out.println("Server: Received a new connection from ("+ addr.getHostAddress()  + "): " + addr.getHostName() + " on port: " + clientSocket.getPort());
                String clientInfo="";
                clientInfo= "Client on port " + clientSocket.getPort();
                
                new Thread( new WorkerRunnable(clientSocket, clientInfo)).start();
            }
            
            System.out.println("Server Stopped.");            
        
    }

private synchronized boolean isStopped() {
        return this.isStopped;
    }

public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }
 

}


/*
 * 
 */
class WorkerRunnable implements Runnable{

    protected Socket clientSocket = null;
    protected String serverText   = null;

    public WorkerRunnable(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }

    public void run() {
         try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                                        new InputStreamReader(clientSocket.getInputStream()));
                String msg;
                while ((msg = in.readLine()) != null) {
                        out.println(msg);
                        if (msg.equals("bye"))
                            break;
                }

                System.out.println(serverText + " leaved.");
                out.close();
                in.close();
                clientSocket.close();

        } catch (IOException e) {
            System.err.println("Error " + e.getMessage());	               
        }
    }
}
