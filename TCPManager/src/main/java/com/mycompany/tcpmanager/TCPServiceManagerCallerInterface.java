/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.tcpmanager;

import java.net.Socket;

/**
 *
 * @author sjdonado
 */
public interface TCPServiceManagerCallerInterface {
    public void messageReceivedFromClient(Socket clientSocket, byte[] message);
    public void fileReceivedFromClient(Socket clientSocket, byte[] file);
    public void errorHasBeenThrown(Exception ex); 
}
