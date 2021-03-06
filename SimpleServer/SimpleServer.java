package SimpleServer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * This class implements a singlethreaded server
 * that processes the incoming requests in the same thread
 * that accepts the client connection.
 */

/**
 *
 * @author annaf
 */

import java.io.*;
import java.net.*;

public class SimpleServer implements Runnable{

    int          serverPort   = 45000; // The port that this server is listening on
    ServerSocket serverSocket = null;  // Server sock that will listen for incoming connections
    Thread       runningThread = null;
    boolean      isStopped    = false;

    public SimpleServer(int port){
        this.serverPort = port;
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            System.err.println("Cannot listen on this port.\n" + e.getMessage());
            System.exit(1);
        }    
            while(!isStopped()){
                Socket clientSocket = null;  // socket created by accept
                
                try {
                       clientSocket = serverSocket.accept(); // wait for a client to connect
                       
                } catch (IOException e) {
                       if(isStopped()) {
                                System.out.println("Server Stopped.") ;
                                return;
                       }
                       throw new RuntimeException(
                                "Error accepting client connection", e);    //Accept failed
                }
                //server code here ...
                try {
                        System.out.println("Server accepts the client connection");
                        // Log some debugging information
                        InetAddress addr = clientSocket.getInetAddress();
                        System.out.println( "Server: Received a new connection from ("+ addr.getHostAddress() 
                                                + "): " + addr.getHostName() );

                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(
                                                new InputStreamReader(clientSocket.getInputStream()));
                        String msg;
                        while ((msg = in.readLine()) != null) {
                                out.println(msg);
                                if (msg.equals("bye"))
                                    break;
                        }
                        
                        System.out.println("Client leaved.");
                        out.close();
                        in.close();
                        clientSocket.close();
                     
                } catch (IOException e) {
                    System.err.println("Error " + e.getMessage());	               
                }
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
