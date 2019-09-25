/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.udpmanager;

import com.mycompany.udpreceptor.UDPReceptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author sjdonado
 */
public class Utils {
//    private static int unsignedToBytes(byte b) {
//        return b & 0xFF;
//    }
    public static int getClientSocketIdFromHeader(byte[] data) {
        String byte5 = Integer.toBinaryString((data[4] & 0xFF) + 0x100).substring(1);
        
        int clientSocketId = Integer.parseInt(byte5.substring(1, 8));
        return clientSocketId;
    }
    
    public static int getPositionFromHeader(byte[] data) {
        return ((0xFF & data[0]) << 24) | ((0xFF & data[1]) << 16) | ((0xFF & data[2]) << 8) | (0xFF & data[3]);
    }
    
    public static boolean getFinalBitFromHeader(byte[] data) {
        String byte5 = Integer.toBinaryString((data[4] & 0xFF) + 0x100).substring(1);
        int fin = Integer.parseInt(byte5.substring(0, 1));
        return fin != 0;
    }
    
    public static byte[] createHeader(int position, int remainingBytes, int socketID) {
        byte[] offset = new byte[5];
        
        offset[0] = (byte) (position >> 24);
        offset[1] = (byte) (position >> 16);
        offset[2] = (byte) (position >> 8);
        offset[3] = (byte) (position);
//        offset[0] = (byte) (position % 255);
//        offset[1] = (byte) (position / 255);
//        offset[2] = (byte) (position / 65535);
        
        String id = Integer.toBinaryString(socketID);
        while (id.length() < 7){
            id = "0" + id;
        }
        offset[4] = (byte) Integer.parseInt((remainingBytes == 0 ? 1 : 0) + id);
        return offset;
    }
    
    public static String getFilePath(byte[] data) {
        String dirPath = System.getProperty("user.home") + File.separator 
                + "GlassFish_Server" + File.separator + "glassfish" 
                + File.separator + "domains" + File.separator + "domain1"
                + File.separator + "uploads" + File.separator;
        String parsedData = new String(data);
        Logger.getLogger(Utils.class.getName()).log(
            Level.INFO,
            "PARSED_DATA => {0}", parsedData
        );
        String filename = parsedData.substring(0, parsedData.indexOf("/*/"));
        Logger.getLogger(Utils.class.getName()).log(
            Level.INFO,
            "FILENAME => {0}", filename
        );
        File directory = new File(dirPath);
        if (! directory.exists()) {
            directory.mkdirs();
        }
        return dirPath + filename;
    }
    
    public static String getSenderAddress(byte[] data){
        String ipAddress = new String(data);
        ipAddress = ipAddress.substring(ipAddress.indexOf("/&/")+3, ipAddress.length());
        return ipAddress.substring(ipAddress.indexOf("/")+1,ipAddress.length());
    }
    
    public static long getFileSize(byte[] data) {
        String parsedData = new String(data).replace("\0", "");
        long size = Long.parseLong(parsedData.substring(
                parsedData.indexOf("/*/") + 3,
                parsedData.indexOf("/&/"))
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

//        Logger.getLogger(UDPReceptor.class.getName()).log(Level.INFO,
//                "CHUNK temp file => {0}", tempChunkFile.getAbsolutePath());
        return new Chunk(position, tempChunkFile.getAbsolutePath());
    }
    
    public static ArrayList<Chunk> organizeChunks(ArrayList<Chunk> fileChunks){
        Comparator<Chunk> comparator = (Chunk c1, Chunk c2) ->
                (new Integer(c1.getPosition())).compareTo(new Integer(c2.getPosition()));
        
        Collections.sort(fileChunks, comparator);
        return fileChunks;
    }
    
    public static ArrayList<Integer> getChunksPositions(ArrayList<Chunk> chunks){
        ArrayList<Integer> chunksPositions = new ArrayList();
        for (Chunk chunk: chunks){
            chunksPositions.add(chunk.getPosition());
        }
        return chunksPositions;
    }
    
    public static ArrayList<Integer> checkMissingChunks(ArrayList<Integer> chunksPositions){
        ArrayList<Integer> missingChunks = new ArrayList();
        for (int i = 1; i <= chunksPositions.get(chunksPositions.size()-1); i++){
            if (!chunksPositions.contains(i)){
                missingChunks.add(i);
            }
        }
        return missingChunks;
    }
    
    public static boolean createFileByClientSocketId(String filePath,
            ArrayList<Chunk> fileChunks) {
        
        fileChunks = organizeChunks(fileChunks);
        
        ArrayList<Integer> chunksPositions = getChunksPositions(fileChunks);
        ArrayList<Integer> missingChunks = checkMissingChunks(chunksPositions);
        
        for (int chunkpos: missingChunks){
            System.out.println("missing: "+chunkpos);
        }
        
        for (Chunk chunk: fileChunks){
            System.out.println("pos: "+chunk.getPosition());
        }
//        System.out.println("fileChunks => " + fileChunks
//                        .stream()
//                        .map(v -> v.getPosition())
//                        .collect(Collectors.toList()));
        try {
            FileInputStream input;
            FileOutputStream output;
            output = new FileOutputStream(filePath, false);
            for (int index = 0; index < fileChunks.size(); index++) {
                input = new FileInputStream(fileChunks.get(index).getFilePath());
                IOUtils.copy(input, output);
                IOUtils.closeQuietly(input);
            }
            IOUtils.closeQuietly(output);
            Logger.getLogger(UDPReceptor.class.getName()).log(Level.INFO,
                "FINAL file created => {0}", filePath);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
