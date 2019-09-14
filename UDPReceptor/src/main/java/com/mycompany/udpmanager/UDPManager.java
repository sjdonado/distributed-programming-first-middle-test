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
    
    MulticastSocket multicastSocket;
    static int receptorIdCounter = 0;
    private final int listeningPort;
    private final String multicastAddress;
    private UDPManagerCallerInterface caller;
    private boolean isEnabled = true;
    private final int receptorId;

    public UDPManager(UDPManagerCallerInterface caller) {
        this.listeningPort = 8080;
        this.multicastAddress = "224.0.0.1";
        this.caller = caller;
        this.receptorId = receptorIdCounter;
        receptorIdCounter += 1;
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
