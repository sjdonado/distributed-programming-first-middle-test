/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpreceptor;

import com.mycompany.udpmanager.Chunk;
import com.mycompany.udpmanager.UDPManager;
import com.mycompany.udpmanager.UDPManagerCallerInterface;
import com.mycompany.udpmanager.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sjdonado
 */
public class UDPReceptor implements UDPManagerCallerInterface {
    private final int NUMBER_OF_RECEPTORS = 1;
    private final ArrayList<UDPManager> receptors = new ArrayList<>();
    private final ArrayList<Chunk> receivedChunks = new ArrayList<>();
    
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
//            Get from header
            int clientSocketId = 0, position = 0;
            boolean end = false;

            File tempChunkFile = File.createTempFile("temp", null);
            tempChunkFile.deleteOnExit();

            receivedChunks.add(new Chunk(receptorId, clientSocketId, position,
                    end, tempChunkFile.getAbsolutePath()));
            
            if (end) {
                boolean res = Utils.getFileByClientSocketId(clientSocketId,
                        "file", receivedChunks);
                if (res) {
                    receptors.get(receptorId).sendMessage(
                            Integer.toBinaryString(clientSocketId).getBytes());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(UDPReceptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        Logger.getLogger(UDPReceptor.class.getName()).log(
                Level.INFO,
                "CHUNK - ReceptorId: {0} - {1}:{2}=> {3}", new Object[]{
                    receptorId,
                    ipAdress,
                    sourcePort,
                    new String(data)
                }
        );
    }

    @Override
    public void exceptionHasBeenThrown(Exception ex) {
        Logger.getLogger(
                UDPReceptor.class.getName()).log(Level.SEVERE, null, ex);
    }

    @Override
    public void clientUploadFileFinished(int clientManagerId) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
