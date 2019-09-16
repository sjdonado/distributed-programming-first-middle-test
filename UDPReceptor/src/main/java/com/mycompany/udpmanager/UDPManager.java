/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpmanager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 *
 * @author sjdonado
 */
public class UDPManager extends Thread {
    private final int listeningPort = 8080;
    private final String multicastAddress = "224.0.0.1";

    private MulticastSocket multicastSocket;
    private UDPManagerCallerInterface caller;
    private boolean isEnabled = true;
    private int receptorId;

    public UDPManager(int receptorId, UDPManagerCallerInterface caller) {
        this.receptorId = receptorId;
        this.caller = caller;
        this.start();
    }
    
    public UDPManager(UDPManagerCallerInterface caller) {
        this.caller = caller;
        this.start();
    }

    private boolean initializeMulticastSocket() {
        try {
            this.multicastSocket = new MulticastSocket(listeningPort);
            multicastSocket.joinGroup(InetAddress.getByName(multicastAddress));
            return true;
        } catch(IOException error) {
            this.caller.exceptionHasBeenThrown(error);
        }
        return false;
    }
    
    @Override
    public void run() {
        try {
            DatagramPacket datagramPacket = new DatagramPacket(new byte[1500], 1500);
            if (initializeMulticastSocket()) {
                while (this.isEnabled) {
                    multicastSocket.receive(datagramPacket);
//                    if (datagramPacket.getLength() <= 6) {
//                        int socketClientId = Integer.parseInt(
//                                new String(datagramPacket.getData()));
//                        this.caller.clientUploadFileFinished(socketClientId);
//                    }
                    this.caller.dataReceived(
                        this.receptorId,
                        datagramPacket.getAddress().toString(),
                        datagramPacket.getPort(),
                        datagramPacket.getData()
                    );
                }
            }
        } catch(IOException error) {
            this.caller.exceptionHasBeenThrown(error);
        }
    }
    
    public boolean sendMessage(byte[] data) {
        try {
            DatagramPacket datagramPacketToBeSent = new DatagramPacket(data, data.length);
            datagramPacketToBeSent.setAddress(InetAddress.getByName(this.multicastAddress));
            datagramPacketToBeSent.setPort(this.listeningPort);
            multicastSocket.send(datagramPacketToBeSent);
            return true;
        } catch (IOException error) {
            this.caller.exceptionHasBeenThrown(error);
        }
        return false;
    }

    public int getReceptorId() {
        return receptorId;
    }
}
