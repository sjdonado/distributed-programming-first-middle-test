/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.tcpmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

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
    private PrintWriter printWriter;
    private BufferedReader printReader;
    private BufferedInputStream reader;
    private BufferedOutputStream writer;
    private final Object mutex = new Object();

    public void waitForAWhile(){
        try {
            synchronized(mutex) {
                mutex.wait();
            }
        } catch (InterruptedException ex) {    
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
   
//    TCPServiceManager connectio
    public TCPClientManager(TCPServiceManagerCallerInterface caller) {
        this.caller = caller;            
        this.start();            
    }
    
//    public TCPClientManager(Socket clientSocket,
//            TCPServiceManagerCallerInterface caller) {
//        this.clientSocket = clientSocket;
//        this.caller = caller;
//        this.start();
//    }

//    GUI connection
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
            this.writer = new BufferedOutputStream(clientSocket.getOutputStream());
            this.reader = new BufferedInputStream(clientSocket.getInputStream());
            
            this.printReader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            this.printWriter = new PrintWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream()), true);

            return true;
        } catch (IOException ex) {
            caller.errorHasBeenThrown(ex);
        }
        return false;
    }
    
    @Override
    public void run() {
        try {
            while (this.isEnabled) {
                if(this.clientSocket == null && this.serverIpAdress == null) {
                    this.waitForAWhile();
                }
                if (initializeStreams()) {
                    sendMessage("Successful connection");
//                    String message;
//                    while((message = this.printReader.readLine()) != null) {
//                       this.caller.messageReceivedFromClient(clientSocket, message.getBytes());
//                    }
                    while (true) {
                        this.caller.chunkReceivedFromClient(clientSocket, this.reader.readNBytes(1500));
                        if (this.reader.read() == -1) break;
                    }
                }
               clearLastSocket();
            }
        } catch (IOException ex) {
            Logger.getLogger(
                TCPClientManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void clearLastSocket() {
        try {
            this.printWriter.close();
            this.printReader.close();
            this.clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(
                TCPClientManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.clientSocket = null;
    }
    
    public boolean isThisThreadBusy() {
        return this.clientSocket != null;
    }
    
    public void sendMessage(String message) {
        try {
            if (this.clientSocket.isConnected()) {
                this.printWriter.write(message + "\n");
                this.printWriter.flush();
            }
        } catch (Exception ex) {
            this.caller.errorHasBeenThrown(ex);
        }
    }
    
    public void sendFile(File file) {
        try {
            if (this.clientSocket.isConnected()) {
                OutputStream out = this.clientSocket.getOutputStream();
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                IOUtils.copy(in, out);
            }
        } catch (IOException ex) {
            this.caller.errorHasBeenThrown(ex);
        }
    }
}
