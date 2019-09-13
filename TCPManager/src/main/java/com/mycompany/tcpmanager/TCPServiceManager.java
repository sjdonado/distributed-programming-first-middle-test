/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.tcpmanager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sjdonado
 */
public class TCPServiceManager extends Thread implements TCPServiceManagerCallerInterface {
    private final int NUMBER_OF_THREADS = 3; // DEVELOPMENT ENV, FOR PROD ENV CHANGE TO 50
    private ServerSocket serverSocket;
    private final int port;
    private final TCPServiceManagerCallerInterface caller;
    private final boolean isEnabled = true;
    private final ArrayList<TCPClientManager> clients = new ArrayList<>();
    
    public TCPServiceManager(int port) {
        this.port = port;
        this.caller = this;
        initializeThreads();
        this.start();
    }
    
    public void initializeThreads() {
        try {
            for (int index = 0; index < NUMBER_OF_THREADS; index++) {
                clients.add(new TCPClientManager(this));
            }
        } catch (Exception ex) {
            Logger.getLogger(
                TCPServiceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public TCPClientManager getNotBusyTCPClientManager() {
        try {
            for (TCPClientManager current: this.clients) {
                if (current != null) {
                    if (!current.isThisThreadBusy()) {
                        return current;
                    }
                }
            }
        }catch (Exception ex) {
            Logger.getLogger(
                TCPServiceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
//    public void sendMessageToAllClients(String message) {
//        for (TCPClientManager current : clients){
//            if (current!=null) {
//                current.sendMessage(message);
//            }
//        }
//    }
    
    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(port);
            while (this.isEnabled) {
//                clients.add(new TCPClientManager(serverSocket.accept(), this));
                Socket receivedSocket = serverSocket.accept();
                TCPClientManager freeTCPClientManager = getNotBusyTCPClientManager();
                if (freeTCPClientManager != null) {
                    freeTCPClientManager.assignSocketToThisThread(receivedSocket);
                } else {
                    try {
                        receivedSocket.close();
                    } catch(IOException error) {
                        this.caller.errorHasBeenThrown(error);
                    }
                }
            }
        } catch (IOException error) {
            this.caller.errorHasBeenThrown(error);
        }
    }

//    TCPClientManager caller interface
    @Override
    public void messageReceivedFromClient(Socket clientSocket, String message) {
        Logger.getLogger(TCPServiceManager.class.getName()).log(
                Level.INFO,
                "MESSAGE - {0}:{1}=> {2}",new Object[]{
                    clientSocket.getInetAddress().getHostName(),
                    clientSocket.getPort(),
                    message
                }
        );
    }
    
    @Override
    public void chunkReceivedFromClient(Socket clientSocket, byte[] data) {
        Logger.getLogger(TCPServiceManager.class.getName()).log(
                Level.INFO,
                "CHUNK - {0}:{1}=> {2}",new Object[]{
                    clientSocket.getInetAddress().getHostName(),
                    clientSocket.getPort(),
                    new String(data)
                }
        );
    }

    @Override
    public void errorHasBeenThrown(Exception ex) {
        Logger.getLogger(
                TCPServiceManager.class.getName()).log(Level.SEVERE, null, ex);
    }
}
