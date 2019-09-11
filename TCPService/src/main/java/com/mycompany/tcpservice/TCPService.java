/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.tcpservice;

import com.mycompany.tcpmanager.TCPServiceManager;
import com.mycompany.tcpmanager.TCPServiceManagerCallerInterface;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sjdonado
 */
public class TCPService implements TCPServiceManagerCallerInterface {

    public TCPService(){
        new TCPServiceManager(9090, this);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new TCPService();
        Logger.getLogger(
                TCPService.class.getName()).log(Level.INFO, "Server is running");
    }  

    @Override
    public void messageReceivedFromClient(Socket clientSocket, byte[] data) {
        System.out.println(clientSocket.getInetAddress().getHostName()
                + ":" + clientSocket.getPort() + ": " + new String(data));
    }

    @Override
    public void errorHasBeenThrown(Exception ex) {
        Logger.getLogger(
                TCPService.class.getName()).log(Level.SEVERE, null, ex);
    }
}
