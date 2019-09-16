/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpmanager;

/**
 *
 * @author sjdonado
 */
public class Chunk {
    private final int receptorId;
    private final int clientSocketId;
    private final int position;
    private final String filePath;

    public Chunk(int receptorId, int clientSocketId, int position, String filePath) {
        this.receptorId = receptorId;
        this.clientSocketId = clientSocketId;
        this.position = position;
        this.filePath = filePath;
    }

    public int getReceptorId() {
        return receptorId;
    }

    public int getClientSocketId() {
        return clientSocketId;
    }

    public int getPosition() {
        return position;
    }

    public String getFilePath() {
        return filePath;
    }
    
}
