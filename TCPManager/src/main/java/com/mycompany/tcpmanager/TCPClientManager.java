/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.tcpmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sjdonado
 */
public class TCPClientManager extends Thread {
    private TCPServiceManagerCallerInterface caller;
    private Socket clientSocket;
    private boolean isEnabled = true;
    private String serverIpAdress;
    private int port;
    private PrintWriter writer;
    private BufferedReader reader;
    private final Object mutex = new Object();

    public void waitForAWhile(){
        try {
            synchronized(mutex) {
                mutex.wait();
            }
        } catch (Exception ex) {    
            Logger.getLogger(
                    TCPClientManager.class.getName()).log(Level.SEVERE, null, ex);            
        }
    }
        
    public void notifyMutex(){
        try {                
            synchronized(mutex) {
                mutex.notify();
            }
        } catch (Exception ex) {
            Logger.getLogger(
                    TCPClientManager.class.getName()).log(Level.SEVERE, null, ex);   
        }
    }
        
    public TCPClientManager(TCPServiceManagerCallerInterface caller) {
        this.caller = caller;            
        this.start();            
    }
    
    public TCPClientManager(Socket clientSocket,
            TCPServiceManagerCallerInterface caller) {
        this.clientSocket = clientSocket;
        this.caller = caller;
        this.start();
    }

    public TCPClientManager(String serverIpAddress, int port, 
            TCPServiceManagerCallerInterface caller) {
        this.serverIpAdress = serverIpAddress;
        this.port = port;            
        this.caller = caller;
        this.start();
    }
    
    public void assignSocketToThisThread(Socket socket){
        this.clientSocket = socket;
        this.notifyMutex();
    }
        
    public boolean initializeSocket(){
        try {
            if (this.serverIpAdress == null) {
                return false;
            }
            this.clientSocket = new Socket(this.serverIpAdress, this.port);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(
                TCPClientManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean initializeStreams(){
        try {
            if (this.clientSocket == null){
                if(!initializeSocket()){
                    return false;
                }
            }
            this.reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new PrintWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream()), true);
            return true;
        } catch (Exception ex) {
            caller.errorHasBeenThrown(ex);
        }
        return false;
    }
    
    @Override
    public void run(){
        try {
            while (this.isEnabled) {
                if(this.clientSocket == null) {
                    this.waitForAWhile();
                }
                if (initializeStreams()) {
                    String line = null;
                    while((line = this.reader.readLine()) != null) {
                       Logger.getLogger(
                            TCPClientManager.class.getName()).log(
                                    Level.INFO,
                                    this.clientSocket.getInetAddress().getHostAddress() + ": " + line
                        );
                    }
//                    sendMessage("Successful connection".getBytes());
//                    while (true) {
//                        try {
//                            int data = this.reader.read();
//                            Logger.getLogger(
//                                TCPClientManager.class.getName()).log(
//                                        Level.INFO,
//                                        this.clientSocket.getInetAddress().getHostAddress() + ": " + data
//                                );
//    //                        caller.messageReceiveFromClient(clientSocket, newMessage.getBytes());
//                        } catch (Exception ex) {
//                            break;
//                        }
//                    }
                }
               clearLastSocket();
            }
        } catch (Exception ex) {
            Logger.getLogger(
                TCPClientManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void clearLastSocket() {
        try {
//            this.writer.close();
//            this.reader.close();
            this.clientSocket.close();
        } catch (Exception ex) {
            Logger.getLogger(
                TCPClientManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.clientSocket = null;
    }
    
    public boolean isThisThreadBusy() {
        return this.clientSocket != null;
    }
    
    public void sendMessage(byte[] message) {
        try {
            if (this.clientSocket.isConnected()) {
                this.writer.write(message + "\n");
                this.writer.flush();
            }
        } catch (Exception ex) {
            this.caller.errorHasBeenThrown(ex);
        }
    }
}
