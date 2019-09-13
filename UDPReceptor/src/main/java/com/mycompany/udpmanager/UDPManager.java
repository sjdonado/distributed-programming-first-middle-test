/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpmanager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *
 * @author sjdonado
 */
public class UDPManager extends Thread {
    private DatagramSocket datagramSocket;
    private int listeningPort;
    private String defaultIpAddress;
    private UDPManagerCallerInterface caller;
    private boolean isEnabled = true;
    private int receptorId;

    public UDPManager(int id, UDPManagerCallerInterface caller) {
        this.listeningPort = 8080;
        this.defaultIpAddress = "224.0.0.1";
        this.caller = caller;
        this.receptorId = id;
        this.start();
    }

    private boolean initializeDatagramSocket() {
        try {
            this.datagramSocket = new DatagramSocket(listeningPort);
            return true;
        } catch(SocketException error) {
            this.caller.exceptionHasBeenThrown(error);
        }
        return false;
    }
    
    @Override
    public void run() {
        try {
            DatagramPacket datagramPacket = new DatagramPacket(new byte[1500], 1500);
            if (initializeDatagramSocket()) {
                while (this.isEnabled) {
                    datagramSocket.receive(datagramPacket);
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
            datagramPacketToBeSent.setAddress(InetAddress.getByName(this.defaultIpAddress));
            datagramPacketToBeSent.setPort(this.listeningPort);
            datagramSocket.send(datagramPacketToBeSent);
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
