/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.tcpmanager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sjdonado
 */
public class TCPServiceManager extends Thread implements TCPServiceManagerCallerInterface {
    private final int NUMBER_OF_THREADS = 1; // DEVELOPMENT ENV, FOR PROD ENV CHANGE TO 50
    private ServerSocket serverSocket;
    private final int port;
    private final TCPServiceManagerCallerInterface caller;
    private final boolean isEnabled = true;
    private Vector<TCPClientManager> clients = new Vector<>();
    
    public TCPServiceManager(int port, TCPServiceManagerCallerInterface caller) {
        this.port = port;
        this.caller = caller;
        initializeThreads();
        this.start();
    }
    
    public void initializeThreads() {
        try {
            for (int index=0; index < NUMBER_OF_THREADS; index++) {
                clients.add(new TCPClientManager(this));
            }
        } catch (Exception ex) {
            Logger.getLogger(
                TCPServiceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public TCPClientManager getNotBusyClientSocketManager() {
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
                clients.add(new TCPClientManager(serverSocket.accept(), this));
//                Socket receivedSocket = serverSocket.accept();
//                TCPClientManager freeClientSocketManager = getNotBusyClientSocketManager();
//                Logger.getLogger(
//                    TCPServiceManager.class.getName()).log(Level.INFO, "NEW CLIENT CONNECTED! ");
//                
//                if (freeClientSocketManager != null) {
//                    freeClientSocketManager.assignSocketToThisThread(receivedSocket);
//                } else {
//                    try {
//                        receivedSocket.close();
//                    } catch(IOException error) {
//                        this.caller.errorHasBeenThrown(error);
//                    }
//                }
            }
        } catch (Exception error) {
            this.caller.errorHasBeenThrown(error);
        }
    }

    @Override
    public void messageReceivedFromClient(Socket clientSocket, byte[] data) {
//        SendMessageToAllClients(
//                                clientSocket.getInetAddress().
//                                getHostName()+":"+clientSocket.getPort()
//                                +": "+new String(data));
    }
    
    @Override
    public void fileReceivedFromClient(Socket clientSocket, byte[] file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void errorHasBeenThrown(Exception ex) {
        Logger.getLogger(
                TCPServiceManager.class.getName()).log(Level.SEVERE, null, ex);
    }
}
