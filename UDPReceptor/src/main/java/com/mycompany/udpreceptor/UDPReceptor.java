/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpreceptor;

import com.mycompany.tcpmanager.TCPServiceManager;
import com.mycompany.udpmanager.Chunk;
import com.mycompany.udpmanager.ClientFile;
import com.mycompany.udpmanager.UDPManager;
import com.mycompany.udpmanager.UDPManagerCallerInterface;
import com.mycompany.udpmanager.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sjdonado
 */
public class UDPReceptor implements UDPManagerCallerInterface {
    private final int NUMBER_OF_RECEPTORS = 1;
    private final ArrayList<UDPManager> receptors = new ArrayList<>();
    private final ArrayList<ClientFile> clientFiles = new ArrayList<>();
    private byte[] lastReceivedChunk;
    
    public UDPReceptor() {
        initializeReceptors();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new UDPReceptor();
    }
    
    public void initializeReceptors() {
        for (int index = 0; index < NUMBER_OF_RECEPTORS; index++) {
            receptors.add(new UDPManager(index, this));
            Logger.getLogger(
                UDPReceptor.class.getName()).log(Level.INFO,
                        "UDP receptor {0} running", index + 1);
        }
    }
    
    
    

//    UDPManager caller interface
    @Override
    public void dataReceived(int receptorId, String ipAdress,
            int sourcePort, byte[] data) {
        try {
            lastReceivedChunk = data;
            ClientFile clientFile;
            int clientSocketId = Utils.getClientSocketIdFromHeader(data);
            int position = Utils.getPositionFromHeader(data);
            boolean UnicastBit = Utils.getUnicastBitFromHeader(data);

            byte[] headlessChunk = Arrays.copyOfRange(data, 5, data.length);
//            Logger.getLogger(UDPReceptor.class.getName()).log(
//                Level.INFO,
//                "CHUNK - ReceptorId: {0} - |{1}|{2}|{3}|{4}|{5} - {6}:{7} \n"
//                        + "DATA: {8} \n",
//                new Object[] {
//                    receptorId,
//                    String.format("HEAD[0] => %8s", Integer.toBinaryString(data[0] & 0xFF)).replace(' ', '0'),
//                    String.format("HEAD[1] => %8s", Integer.toBinaryString(data[1] & 0xFF)).replace(' ', '0'),
//                    String.format("HEAD[2] => %8s", Integer.toBinaryString(data[2] & 0xFF)).replace(' ', '0'),
//                    String.format("HEAD[3] => %8s", Integer.toBinaryString(data[3] & 0xFF)).replace(' ', '0'),
//                    String.format("HEAD[4] => %8s", Integer.toBinaryString(data[4] & 0xFF)).replace(' ', '0'),
//                    ipAdress,
//                    sourcePort,
//                    new String(headlessChunk),
//                }
//            );
            if ((clientFile = Utils.getClientFile(clientSocketId, clientFiles)) == null) {
                clientFiles.add(new ClientFile(receptorId, clientSocketId,
                        Utils.getFilePath(headlessChunk),Utils.getSenderAddress(data),
                        Utils.getFileSize(headlessChunk))
                );
            } else {
                int progress = (int) (((double) (clientFile.getChunks().size()) / (clientFile.getSize() / TCPServiceManager.MTU - 5)) * 100);
                if (UnicastBit) {
                    
                    byte [] finalHeadlessChunk = headlessChunk;
                    if (clientFile.getSize() % TCPServiceManager.MTU - 5 > 0) {
                        finalHeadlessChunk = Arrays.copyOfRange(
                            headlessChunk,
                            0,
                            (int) clientFile.getSize() % TCPServiceManager.MTU - 5
                        );
                    }

                    clientFile.addChunk(Utils.createChunk(
                        finalHeadlessChunk,
                        position
                    ));

                    if (Utils.createFileByClientSocketId(clientFile.getPath(),clientFile.getChunks())) {
                        receptors.get(receptorId).sendMessage((clientSocketId + "|" + 100).getBytes(),null);
                        clientFiles.remove(clientFile);
                    }else{
                         ArrayList<Integer> chunksPositions = Utils.getChunksPositions(clientFile.getChunks());
                         ArrayList<Integer> missingChunks = Utils.checkMissingChunks(chunksPositions);
                         
                         
                    }
                    
                } else {
                    Chunk tempchunk = Utils.createChunk(headlessChunk, position);
                    if (!clientFile.getChunks().contains(tempchunk)){
                        clientFile.addChunk(Utils.createChunk(headlessChunk, position));
                    }
                    receptors.get(receptorId).sendMessage((clientSocketId + "|" + progress).getBytes(),null);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(UDPReceptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void exceptionHasBeenThrown(Exception ex) {
        Logger.getLogger(
                UDPReceptor.class.getName()).log(Level.SEVERE, null, ex);
    }

    @Override
    public void clientUploadFileStatus(int clientManagerId, int progress) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sendMissingChunksPositions(int clientSocket, byte[] data, String destAddress) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void timeoutExpired() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
    }

    
}
