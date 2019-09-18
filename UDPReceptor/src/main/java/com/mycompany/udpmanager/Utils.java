/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpmanager;

import com.mycompany.udpreceptor.UDPReceptor;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;


/**
 *
 * @author sjdonado
 */
public class Utils {
    private static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }
    
    public static int getClientSocketIdFromHeader(byte[] data) {
        String byte4 = Integer.toBinaryString((data[3] & 0xFF) + 0x100).substring(1);
        
        int clientSocketId = Integer.parseInt(byte4.substring(1, 7));
        return clientSocketId;
    }
    
    public static int getPositionFromHeader(byte[] data) {
        int byte1 = unsignedToBytes(data[0]);
        int byte2 = unsignedToBytes(data[1]) * 2^(8);
        int byte3 = unsignedToBytes(data[2]) * 2^(16);
        String byte4 = Integer.toBinaryString((data[3] & 0xFF) + 0x100).substring(1);
        int pos = Integer.parseInt(byte4.substring(7)) * 2^(24);
        int position = byte1 + byte2 + byte3 + pos;
        return position;
    }
    
    public static boolean getFinalBitFromHeader(byte[] data) {
        String byte4 = Integer.toBinaryString((data[3] & 0xFF) + 0x100).substring(1);
        int fin = Integer.parseInt(byte4.substring(0,1));
        boolean end = fin != 0;
        return end;
    }
    
    public static byte[] createHeader(int position, int remainingBytes, int socketID) {
        byte[] offset = new byte[4];
        
        offset[0] = (byte) (position % 255);
        offset[1] = (byte) (position / 255);
        offset[2] = (byte) (position / 65535);
        int bit25th = position / 16777215;
        
        String id = Integer.toBinaryString(socketID);
        while (id.length() < 6){
            id = "0" + id;
        }
        //System.out.println((remainingBytes == 0 ? 1 : 0) + id + bit25th);
        offset[3] = (byte) Integer.parseInt((remainingBytes == 0 ? 1 : 0) + id + bit25th);
        //System.out.println((unsignedToBytes(offset[3]) + 0x100).substring(1));
        Logger.getLogger(Utils.class.getName()).log(
            Level.INFO,
            Integer.toBinaryString((offset[3] & 0xFF) + 0x100).substring(1)
        );
        return offset;
    }
    
    public static String getFilePath(byte[] data) {
        String userPath = System.getProperty("user.home");
        String parsedData = new String(data).replace("\0", "");
        String filename = parsedData.substring(0, parsedData.indexOf("/*/"));
        Logger.getLogger(Utils.class.getName()).log(
            Level.INFO,
            "FILENAME => {0}", filename
        );
        return userPath+File.separator+"GlassFish_Server"+File.separator+"glassfish"+File.separator+"domains"+File.separator+"domain1"+File.separator+"uploads"+File.separator+filename;
    }
    
    public static long getFileSize(byte[] data) {
        String parsedData = new String(data).replace("\0", "");
        long size = Long.parseLong(parsedData.substring(
                parsedData.indexOf("/*/") + 3,
                parsedData.length())
        );
        Logger.getLogger(Utils.class.getName()).log(
            Level.INFO,
            "FILE SIZE => {0}", size
        );
        return size;
    }
    
    public static ClientFile getClientFile(int clientSocketId,
            ArrayList<ClientFile> clientfiles) {
        for (ClientFile clientFile : clientfiles) {
            if (clientFile.getClientSocketId() == clientSocketId) {
                return clientFile;
            }
        }
        return null;
    }
    
    public static Chunk createChunk(byte[] data, int position) throws IOException {
        File tempChunkFile = File.createTempFile("chunk", null);
        FileUtils.writeByteArrayToFile(tempChunkFile, data);
        tempChunkFile.deleteOnExit();

        Logger.getLogger(UDPReceptor.class.getName()).log(Level.INFO,
                "CHUNK temp file => {0}", tempChunkFile.getAbsolutePath());
        return new Chunk(position, tempChunkFile.getAbsolutePath());
    }
    
    public static File createFileByClientSocketId(String filePath,
            ArrayList<Chunk> fileChunks) {
        File newFile = new File(filePath);
        
        Comparator<Chunk> comparator = (Chunk c1, Chunk c2) ->
                (c1.getPosition() + "").compareTo((c2.getPosition() + ""));
        
        Collections.sort(fileChunks, comparator);
        
        File[] files = new File[fileChunks.size()];
        for (int index = 0; index < fileChunks.size(); index++) {
            files[index] = new File(fileChunks.get(index).getFilePath());
        }
        try {
            joinFiles(files, newFile);
            return newFile;
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
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
