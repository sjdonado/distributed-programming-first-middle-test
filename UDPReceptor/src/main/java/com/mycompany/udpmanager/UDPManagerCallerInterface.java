/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpmanager;

import java.net.Socket;

/**
 *
 * @author sjdonado
 */
public interface UDPManagerCallerInterface {
    public void dataReceived(int receptorId, String ipAdress, int sourcePort, byte[] data);
    public void exceptionHasBeenThrown(Exception ex);
    public void sendMissingChunksPositions(int clientSocket, byte[] data);
    public void clientUploadFileStatus(int clientManagerId, int progress);
}
