/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.webmanager;

import java.util.ArrayList;

/**
 *
 * @author brian
 */
public class ConnectionManagement {
    public static ConnectionManagement instance;
    private Integer pos;
    private final ArrayList<String> servers;
    

    private ConnectionManagement() {
        this.servers = new ArrayList<>();
        this.pos = 0;
    }
    
    public static ConnectionManagement getInstance() {
        if (instance == null) {
            instance = new ConnectionManagement();
        }
        return instance;
    }
    
    public void addServer(String address) {
        this.servers.add(address);
    }

    public String getServer() {
        ArrayList<String> serverList = new ArrayList<>();
        this.servers.forEach((server) -> {
            serverList.add(server);
        });
        String server;   
        synchronized (pos) {   
            if (pos > serverList.size()) {
                pos = 0;   
            }    
            server = serverList.get(pos);   
            pos ++;   
        }
        return server;   
    }
}