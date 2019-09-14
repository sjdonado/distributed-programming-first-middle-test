/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;


/**
 *
 * @author sjdonado
 */
public class Utils {
    public static byte[] binaryCounter(int counter, int remainingBytes,
            int socketID) {
        byte[] offset = new byte[4];
        
        offset[0] = Byte.parseByte(Integer.toBinaryString(counter % 255), 2);
        offset[1] = Byte.parseByte(Integer.toBinaryString(counter / 255), 2);
        offset[2] = Byte.parseByte(Integer.toBinaryString(counter / 65535), 2);
        int bit25th = counter / 16777215;
        
        System.out.println(Integer.toBinaryString(socketID));
        String id2 = Integer.toBinaryString(socketID);
        
        String id = Integer.toBinaryString(socketID).substring(2, 6);
        offset[4] = Byte.parseByte((remainingBytes == 0 ? 1 : 0) + id + bit25th);
        
        return offset;
    }
    
    public static boolean getFileByClientSocketId(int clientSocketId,
            String filePath, ArrayList<Chunk> receivedChunks) {

        File newFile = new File(filePath);
        ArrayList<Chunk> found = new ArrayList<>();

        for (Chunk receivedChunk : receivedChunks) {
            if (receivedChunk.getClientSocketId() == clientSocketId) {
                found.add(receivedChunk);
            }
        }
        
        Comparator<Chunk> comparator = (Chunk c1, Chunk c2) ->
                (c1.getPosition() + "").compareTo((c2.getPosition() + ""));
        
        Collections.sort(found, comparator);
        
        File[] files = new File[found.size()];
        for (int index = 0; index < found.size(); index++) {
            files[index] = new File(found.get(index).getFilePath());
        }
        try {
            joinFiles(files, newFile);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public static void joinFiles(File[] sources, File destination)
            throws IOException {
        OutputStream output = null;
        try {
            output = createAppendableStream(destination);
            for (File source : sources) {
                appendFile(output, source);
            }
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    private static BufferedOutputStream createAppendableStream(File destination)
            throws FileNotFoundException {
        return new BufferedOutputStream(new FileOutputStream(destination, true));
    }

    private static void appendFile(OutputStream output, File source)
            throws IOException {
        InputStream input = null;
        try {
            input = new BufferedInputStream(new FileInputStream(source));
            IOUtils.copy(input, output);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
}
