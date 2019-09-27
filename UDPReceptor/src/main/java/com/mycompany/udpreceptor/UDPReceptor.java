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
    private byte[] lastMetadataReceived;
    
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
            ClientFile clientFile;
            int clientSocketId = Utils.getClientSocketIdFromHeader(data);
            int position = Utils.getPositionFromHeader(data);

            byte[] headlessChunk = Arrays.copyOfRange(data, 9, data.length);

            if ((clientFile = Utils.getClientFile(clientSocketId, clientFiles)) == null) {
                lastMetadataReceived = data;
                clientFiles.add(new ClientFile(receptorId, clientSocketId,
                    Utils.getFilePath(headlessChunk), Utils.getSenderAddress(data),
                    Utils.getFileSize(headlessChunk))
                );
            } else {
                int progress = (int) (((double) (clientFile.getChunks().size()) / (clientFile.getSize() / TCPServiceManager.MTU - 9)) * 100);
                ArrayList<Integer> missingChunks = Utils.getMissingChunks(clientFile.getChunks(), Utils.getTotalChunks(clientFile, TCPServiceManager.MTU));
                
                Logger.getLogger(UDPReceptor.class.getName()).log(Level.INFO,
                    "UDPReceptor  progress => {0} missingChunks => {1}", new Object[]{progress, missingChunks.isEmpty()});
                
                if (missingChunks.isEmpty() && !clientFile.getChunks().isEmpty()) {
                    byte [] finalHeadlessChunk = headlessChunk;
                    if (clientFile.getSize() % TCPServiceManager.MTU - 9 > 0) {
                        finalHeadlessChunk = Arrays.copyOfRange(
                            headlessChunk,
                            0,
                            (int) clientFile.getSize() % TCPServiceManager.MTU - 9
                        );
                    }
                    clientFile.addChunk(Utils.createChunk(
                        finalHeadlessChunk,
                        position
                    ));
                    if (Utils.createFileByClientSocketId(clientFile.getPath(), clientFile.getChunks(),clientFile.getSize() / TCPServiceManager.MTU)) {
//                        receptors.get(receptorId).sendMessage((clientSocketId + "|" + 100).getBytes(),null);
                        clientFiles.remove(clientFile);
                    }
                } else {
                    Chunk tempchunk = Utils.createChunk(headlessChunk, position);
                    if (!clientFile.getChunks().contains(tempchunk)){
                        clientFile.addChunk(Utils.createChunk(headlessChunk, position));
                    }
//                    receptors.get(receptorId).sendMessage((clientSocketId + "|" + progress).getBytes(),null);
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
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
public void timeoutExpired(int receptorId) {
        ClientFile clientFile;
        if ((clientFile = Utils.getClientFile(
                Utils.getClientSocketIdFromHeader(lastMetadataReceived),
                clientFiles)) != null) {
            
            int clientSocketId = Utils.getClientSocketIdFromHeader(lastMetadataReceived);
            byte[] header = Utils.createHeader(0, true, clientSocketId);
            
            ArrayList<Integer> missingChunks = Utils.getMissingChunks(clientFile.getChunks(), Utils.getTotalChunks(clientFile, TCPServiceManager.MTU));
            Logger.getLogger(
                UDPReceptor.class.getName()).log(Level.INFO,
                    "RETRANSMISSION  clientSocketId => {0} missingChunks => {1}", new Object[]{clientSocketId, missingChunks.toString()});
            
            byte[] data = Utils.getMissingChunksPositions(header, clientFile, TCPServiceManager.MTU);
            System.out.println("RETRANSMISSION => " + Utils.getUnicastBitFromHeader(data));
            receptors.get(receptorId).sendMessage(
                data,
                Utils.getSenderAddress(lastMetadataReceived)
            );
        }
    }
}
