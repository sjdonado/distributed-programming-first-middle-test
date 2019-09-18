/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpreceptor;

import com.mycompany.udpmanager.Chunk;
import com.mycompany.udpmanager.ClientFile;
import com.mycompany.udpmanager.UDPManager;
import com.mycompany.udpmanager.UDPManagerCallerInterface;
import com.mycompany.udpmanager.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author sjdonado
 */
public class UDPReceptor implements UDPManagerCallerInterface {
    private final int NUMBER_OF_RECEPTORS = 1;
    private final ArrayList<UDPManager> receptors = new ArrayList<>();
    private final ArrayList<ClientFile> clientFiles = new ArrayList<>();
    
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
            boolean finalChunk = Utils.getFinalBitFromHeader(data);

            byte[] headlessChunk = Arrays.copyOfRange(data, 4, data.length);
            
            Logger.getLogger(UDPReceptor.class.getName()).log(
                Level.INFO,
                "CHUNK - ReceptorId: {0} - |{1}|{2}|{3}|{4}| - {5}:{6} \n"
                        + "DATA: {7} \n"
                        + "PARSED_DATA: {8} \n"
                        + "HEADLESS_DATA: {9}",
                new Object[] {
                    receptorId,
                    String.format("HEAD[0] => %8s", Integer.toBinaryString(data[0] & 0xFF)).replace(' ', '0'),
                    String.format("HEAD[1] => %8s", Integer.toBinaryString(data[1] & 0xFF)).replace(' ', '0'),
                    String.format("HEAD[2] => %8s", Integer.toBinaryString(data[2] & 0xFF)).replace(' ', '0'),
                    String.format("HEAD[3] => %8s", Integer.toBinaryString(data[3] & 0xFF)).replace(' ', '0'),
                    ipAdress,
                    sourcePort,
                    data,
                    new String(data),
                    new String(headlessChunk),
                }
            );
            
            if ((clientFile = Utils.getClientFile(clientSocketId, clientFiles)) == null) {
                clientFiles.add(new ClientFile(receptorId, clientSocketId,
                        Utils.getFilePath(headlessChunk),
                        Utils.getFileSize(headlessChunk))
                );
            } else {
                
                if (finalChunk) {
                    byte [] finalHeadlessChunk = headlessChunk;
                    if (clientFile.getSize() % 1496 > 0) {
                        finalHeadlessChunk = Arrays.copyOfRange(
                            headlessChunk,
                            0,
                            (int) clientFile.getSize() % 1496
                        );
                    }
                    
                    clientFile.addChunk(Utils.createChunk(
                        finalHeadlessChunk,
                        position
                    ));

                    File finalFile = Utils.createFileByClientSocketId(
                        clientFile.getPath(),
                        clientFile.getChunks()
                    );

                    if (finalFile != null) {
    //                    receptors.get(receptorId).sendMessage(
    //                            Integer.toBinaryString(clientSocketId).getBytes());
                        Logger.getLogger(UDPReceptor.class.getName()).log(Level.INFO,
                            "FINAL file created => {0}", finalFile.getAbsolutePath());
                    }
                    clientFiles.remove(clientFile);
                } else {
                    clientFile.addChunk(Utils.createChunk(headlessChunk, position));
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
    public void clientUploadFileFinished(int clientManagerId) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
