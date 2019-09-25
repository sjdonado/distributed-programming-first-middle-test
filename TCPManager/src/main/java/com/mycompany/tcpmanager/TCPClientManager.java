/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.tcpmanager;

import com.mycompany.udpmanager.Chunk;
import com.mycompany.udpmanager.Utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
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
    public ArrayList<Chunk> lastSentChunks;
    
   
//    TCPServiceManager connection
    public TCPClientManager(int id, TCPServiceManagerCallerInterface caller) {
        this.clientManagerId = id;
        this.serviceConnection = true;
        this.caller = caller;
        this.start();            
    }
    
//    GUI connection
    public TCPClientManager(String serverIpAddress, int port, 
            TCPServiceManagerCallerInterface caller) {
        this.serverIpAdress = serverIpAddress;
        this.port = port;            
        this.caller = caller;
        this.start();
    }
    
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
                if (!initializeSocket()) return false;
            }
            if (this.writer == null)
                this.writer = new BufferedOutputStream(clientSocket.getOutputStream());

            if (this.reader == null)
                this.reader = new BufferedInputStream(clientSocket.getInputStream());
            
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
                    byte[] chunk = new byte[TCPServiceManager.MTU];
                    int data, index = 5, remainingBytes;
                    int position = 0;
                    lastSentChunks = new ArrayList();
                    while ((data = this.reader.read()) != -1) {
                        remainingBytes = this.reader.available();
                        byte stream = (byte) data;
//                        Logger.getLogger(
//                            TCPClientManager.class.getName()).log(Level.INFO, new String(new byte[]{stream}));
                        chunk[index] = stream;
                        if (index == TCPServiceManager.MTU - 1 || remainingBytes == 0) {
//                            Logger.getLogger(TCPClientManager.class.getName())
//                                    .log(Level.INFO, "INDEX: {0} CHUNK - 4:{1} 5:{2}", new Object[]{index, (chunk[4] &255), (chunk[5] &255)});
                            if (index == 6) {
                                if ((chunk[5] &255) == 0 && (chunk[6] &255) == 0) {
                                    this.caller.messageReceivedFromClient(
                                        clientSocket,
                                        "Successful connection"
                                    );
                                }
                                if ((chunk[5] &255) == 0 && (chunk[6] &255) > 0) {
                                    this.caller.messageReceivedFromClient(
                                        clientSocket,
                                        (chunk[6] &255) + "%"
                                    );
                                }
                            } else {
                                byte[] offset = Utils.createHeader(
                                    position,
                                    false,
                                    this.clientManagerId
                                );
                                chunk[0] = offset[0];
                                chunk[1] = offset[1];
                                chunk[2] = offset[2];
                                chunk[3] = offset[3];
                                chunk[4] = offset[4];
//                                Logger.getLogger(
//                                    TCPClientManager.class.getName()).log(Level.INFO,
//                                        "CHUNK ReceivedFromClient {0} {1}",
//                                        new Object[]{chunk, new String(chunk)}
//                                );
                                this.caller.chunkReceivedFromClient(clientSocket, chunk);
                                position++;
                                
                                lastSentChunks.add(Utils.createChunk(chunk, position));
                                
                                if (Utils.getUnicastBitFromHeader(chunk)){
                                    position = 0;
                                }
                                Arrays.fill(chunk, (byte) 0);
                            }
                            index = 4;
                        }
                        index += 1;
                    }
                }
                if (this.serviceConnection) {
                    Logger.getLogger(TCPClientManager.class.getName())
                    .log(Level.INFO, "TERMINATED");
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
        } catch (IOException ex) {
            this.caller.errorHasBeenThrown(ex);
        }
    }
    
    public void sendFile(File file) {
        try {
            if (this.clientSocket.isConnected()) {
                
                String metadata = file.getName() + "/*/" + file.length() + "/&/" + InetAddress.getLocalHost().toString();
                if (metadata.length() < TCPServiceManager.MTU - 5) {
                    char[] chars = new char[TCPServiceManager.MTU - 5 - metadata.length()];
                    Arrays.fill(chars, '\0');
                    metadata += new String(chars);
                }
                BufferedInputStream filenameStream = new BufferedInputStream(new ByteArrayInputStream(metadata.getBytes("UTF-8")));
                
                Logger.getLogger(TCPClientManager.class.getName())
                    .log(Level.INFO, "METADATA - {0} {1}", new Object[]{metadata, new String(metadata.getBytes("UTF-8"))});
                
                IOUtils.copy(filenameStream, this.writer);

                this.writer.flush();

                BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
                
                IOUtils.copy(fileInputStream, this.writer);
                
                this.writer.flush();
                
                Logger.getLogger(TCPClientManager.class.getName())
                    .log(Level.INFO, "FILE SENT TO GATEWAY");
            }
        } catch (IOException ex) {
            this.caller.errorHasBeenThrown(ex);
        }
    }
}
