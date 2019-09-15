/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.server.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

/**
 *
 * @author brian
 */
public class ServerFiles{
    static final String UPLOADS_FOLDER_PATH = System.getProperty("catalina.base") 
        + File.separator + "uploads";
    
    public static SharedFile[] indexSharedFiles() {
        File folder = new File(UPLOADS_FOLDER_PATH);
        File[] listOfFiles = folder.listFiles();
        SharedFile[] parsedFiles = new SharedFile[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++) {
            File file = listOfFiles[i];
            if (file.isFile()) {
                try {
                    BasicFileAttributes attrs = Files
                        .readAttributes(file.toPath(), BasicFileAttributes.class);
                    parsedFiles[i] = new SharedFile(
                        file.getName(), 
                        file.length(),
                        new Date(attrs.creationTime().toMillis())
                    );
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
        return parsedFiles;
    }
    
    public static File getSharedFile(String filename) {
        File response;
        try {
            response = new File(UPLOADS_FOLDER_PATH + File.separator + filename);
        } catch (Exception e) {
            response = null;
            System.err.println(e);
        }
        return response;
    }
}
