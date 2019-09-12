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
    public void chunkReceivedFromClient(Socket clientSocket, byte[] data);
    public void errorHasBeenThrown(Exception ex); 
}
