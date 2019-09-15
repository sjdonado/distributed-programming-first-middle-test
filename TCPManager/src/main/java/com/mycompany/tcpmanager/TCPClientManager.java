/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.tcpmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import static java.lang.Byte.parseByte;
import java.net.Socket;
import java.util.Arrays;
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
    private boolean serviceConnection = false;
    private String serverIpAdress;
    private int port;
    private PrintWriter printWriter;
    private BufferedInputStream reader;
    private BufferedOutputStream writer;
    private final Object mutex = new Object();
    private int clientManagerId;

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
   
//    TCPServiceManager connection
    public TCPClientManager(int id, TCPServiceManagerCallerInterface caller) {
        this.clientManagerId = id;
        this.serviceConnection = true;
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
            if (this.clientSocket == null) {
                if (!initializeSocket()) {
                    return false;
                }
            }

            if (this.writer == null)
                this.writer = new BufferedOutputStream(clientSocket.getOutputStream());

            if (this.reader == null)
                this.reader = new BufferedInputStream(clientSocket.getInputStream());

//            if (this.printWriter == null)
//                this.printWriter = new PrintWriter(
//                    new OutputStreamWriter(clientSocket.getOutputStream()), true);

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
                if(this.serviceConnection && this.clientSocket == null) {
                    this.waitForAWhile();
                }
                if (initializeStreams()) {
                    sendMessage(new byte[] {0, 0});
                    byte[] chunk = new byte[1500];
                    int data, index = 0, remainingBytes;
                    int counter = 0;
                    while ((data = this.reader.read()) != -1) {
                        remainingBytes = this.reader.available();
                        chunk[index] = (byte) data;
                        if (index == 1496 || remainingBytes == 0) {
                            if (index == 1) {
                                if ((chunk[0] &255) == 0 && (chunk[1] &255) == 0) {
                                    this.caller.messageReceivedFromClient(
                                        clientSocket,
                                        "Successful connection"
                                    );
                                }
                                if ((chunk[0] &255) == 0 && (chunk[1] &255) == 1) {
                                    this.caller.messageReceivedFromClient(
                                        clientSocket,
                                        "File saved successfully"
                                    );
                                }
                            } else {
                                byte[] offset = binaryCounter(counter, remainingBytes, this.clientManagerId);
                                chunk[1496] = offset[0];
                                chunk[1497] = offset[1];
                                chunk[1498] = offset[2];
                                chunk[1499] = offset[3];
//                                
                                this.caller.chunkReceivedFromClient(clientSocket, chunk);
                                counter++;
//                                if (remainingBytes == 0) sendMessage(new byte[] {0, 1});
                            }
                            index = 0;
                        }
                        index += 1;
                    }
                }
//                sendMessage(new byte[] {0, 1});
                if (this.serviceConnection) {
                    clearLastSocket();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(
                TCPClientManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void clearLastSocket() {
        try {
            this.writer.close();
            this.reader.close();

            this.clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPClientManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        this.clientSocket = null;
    }
    
    public boolean isThisThreadBusy() {
        return this.clientSocket != null;
    }
    
    public void sendMessage(byte[] message) {
        try {
            if (this.clientSocket.isConnected()) {
                this.writer.write(message, 0, 2);
                this.writer.flush();
            }
        } catch (Exception ex) {
            this.caller.errorHasBeenThrown(ex);
        }
    }
    
    public void sendFile(File file) {
        try {
            if (this.clientSocket.isConnected()) {
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                IOUtils.copy(in, this.writer);
                this.writer.flush();
            }
        } catch (IOException ex) {
            this.caller.errorHasBeenThrown(ex);
        }
    }
    
    public byte[] binaryCounter(int counter, int remainingBytes, int socketID){
        byte[] offset = new byte[4];
        
        offset[0] = (byte) (counter % 255);
        offset[1] = (byte) (counter / 255);
        offset[2] = (byte) (counter / 65535);
        int bit25th = counter/16777215;
        
        
        String id = Integer.toBinaryString(socketID);
        while (id.length() < 7){
            id = "0"+id;
        }
        //System.out.println((remainingBytes == 0 ? 1 : 0) + id + bit25th);
        offset[3] = (byte) Integer.parseInt((remainingBytes == 0 ? 1 : 0) + id + bit25th);
        System.out.println(Byte.valueOf(offset[3]));
        
        return offset;
    }
}
