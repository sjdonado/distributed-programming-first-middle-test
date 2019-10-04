/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpmanager;

import com.mycompany.tcpmanager.TCPServiceManager;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author sjdonado
 */
public class UDPManager extends Thread {
    private final int listeningPort = 8080;
    private final String multicastAddress = "224.0.0.2";

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
            DatagramPacket datagramPacket = new DatagramPacket(new byte[TCPServiceManager.MTU], TCPServiceManager.MTU);
            if (initializeMulticastSocket()) {
                while (this.isEnabled) {
                    multicastSocket.receive(datagramPacket);
                    String parsedData = new String(datagramPacket.getData())
                            .replace("\0", "");
                    if (parsedData.length() <= 6
                        && parsedData.contains("|")
                        && StringUtils.isNumeric(parsedData.substring(0, parsedData.indexOf("|")))
                    ) {
                        int socketClientId = Integer.parseInt(
                            parsedData.substring(0, parsedData.indexOf("|"))
                        );
                        int progress = Integer.parseInt(
                            parsedData.substring(parsedData.indexOf("|") + 1)
                        );
                        //this.caller.clientUploadFileStatus(socketClientId, progress);
                    } else {
                        this.caller.dataReceived(
                            this.receptorId,
                            datagramPacket.getAddress().toString(),
                            datagramPacket.getPort(),
                            datagramPacket.getData()
                        );
                    }
                    datagramPacket.setData(new byte[TCPServiceManager.MTU]);
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
