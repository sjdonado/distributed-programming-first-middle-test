/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpmanager;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 *
 * @author sjdonado
 */
public class ClientFile {
    private final int receptorId;
    private final String senderAddress;
    private final int clientSocketId;
    private final String path;
    private final long size;
    private final ArrayList<Chunk> chunks;

    public ClientFile(int receptorId, int clientSocketId, String filePath, String senderAddress,
            long fileSize) {
        this.receptorId = receptorId;
        this.clientSocketId = clientSocketId;
        this.path = filePath;
        this.senderAddress = senderAddress;
        this.size = fileSize;
        this.chunks = new ArrayList<>();
    }

    public int getReceptorId() {
        return receptorId;
    }

    public int getClientSocketId() {
        return clientSocketId;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }
    
    public ArrayList<Chunk> getChunks() {
        return chunks;
    }
    
    public void addChunk(Chunk chunk) {
        chunks.add(chunk);
    }
    
}
