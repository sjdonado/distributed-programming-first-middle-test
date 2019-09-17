/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpmanager;

import java.util.ArrayList;

/**
 *
 * @author sjdonado
 */
public class ClientFile {
    private final int receptorId;
    private final int clientSocketId;
    private final String filePath;
    private final long fileSize;
    private final ArrayList<Chunk> chunks;

    public ClientFile(int receptorId, int clientSocketId, String filePath,
            long fileSize) {
        this.receptorId = receptorId;
        this.clientSocketId = clientSocketId;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.chunks = new ArrayList<>();
    }

    public int getReceptorId() {
        return receptorId;
    }

    public int getClientSocketId() {
        return clientSocketId;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getFileSize() {
        return fileSize;
    }
    
    public ArrayList<Chunk> getChunks() {
        return chunks;
    }
    
    public void addChunk(Chunk chunk) {
        chunks.add(chunk);
    }
    
}
