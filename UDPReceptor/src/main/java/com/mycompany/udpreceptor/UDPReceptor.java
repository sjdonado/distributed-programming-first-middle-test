/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpreceptor;

import com.mycompany.udpmanager.UDPManager;
import com.mycompany.udpmanager.UDPManagerCallerInterface;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sjdonado
 */
public class UDPReceptor implements UDPManagerCallerInterface {
    private final int NUMBER_OF_RECEPTORS = 3;
    private final ArrayList<UDPManager> receptors = new ArrayList<>();
    
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
//        Index 0 is taken by TCPManager
        for (int index = 1; index <= NUMBER_OF_RECEPTORS; index++) {
            receptors.add(new UDPManager(index, this));
            Logger.getLogger(
                UDPReceptor.class.getName()).log(Level.INFO, "UDP receptor {0} running", index);
        }
    }

//    UDPManager caller interface
    @Override
    public void dataReceived(int receptorId, String ipAdress, int sourcePort, byte[] data) {
        Logger.getLogger(UDPReceptor.class.getName()).log(
                Level.INFO,
                "CHUNK - ReceptorId: {0} - {1}:{2}=> {3}",new Object[]{
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
}
