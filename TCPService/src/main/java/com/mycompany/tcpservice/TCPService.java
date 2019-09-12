/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.tcpservice;

import com.mycompany.tcpmanager.TCPServiceManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sjdonado
 */
public class TCPService {

    public TCPService(){
        new TCPServiceManager(9090);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new TCPService();
        Logger.getLogger(
                TCPService.class.getName()).log(Level.INFO, "Server is running");
    }
}
