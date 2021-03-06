package com.server.files;

import com.mycompany.webmanagerclient.SharedFile;
import com.mycompany.webservice.InitializerSetting;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author brian
 */
public class ServerFiles{
    static final String UPLOADS_FOLDER_PATH = System.getProperty("catalina.base") 
        + File.separator + "uploads";
    private static ServerFiles instance;
    
    private ServerFiles() {
        File folder = new File(UPLOADS_FOLDER_PATH);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }
    
    public static ServerFiles getInstance() {
        if (instance == null) {
            instance = new ServerFiles();
        }
        return instance;
    }
    
    public ArrayList<SharedFile> indexSharedFiles() {
        File folder = new File(UPLOADS_FOLDER_PATH);
        File[] listOfFiles = folder.listFiles();
        ArrayList<SharedFile> parsedFiles = new ArrayList<>();
        for(File file : listOfFiles) {
            if (file.isFile()) {
                try {
                    BasicFileAttributes attrs = Files
                        .readAttributes(file.toPath(), BasicFileAttributes.class);
                    SharedFile sharedFile = new SharedFile(
                        file.getName(), 
                        file.length(),
                        new Date(attrs.creationTime().toMillis()
                    ));
                    parsedFiles.add(sharedFile);
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
        return parsedFiles;
    }
    
    public File getSharedFile(String filename) {
        try {
            return new File(UPLOADS_FOLDER_PATH + File.separator + filename);
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }
    
    public void syncFiles(ArrayList<String> filesname) {
        filesname.forEach(filename -> {
            try {
                this.downloadFile(filename);
            } catch (IOException ex) {
                Logger.getLogger(ServerFiles.class.getName()).log(
                    Level.SEVERE, null,"SYNC WEB SERVICE INDEX FILE ERROR =>" + ex
                );
            }
        });
    }
    
    private void downloadFile(String filename) throws IOException {
        String baseUrl = "http://" 
            + InitializerSetting.SERVER_SETTING_VARS.get("WEB_MANAGER_ADDRESS")
            + "/WebManager/webresources/shared_files";
        URL url = new URL(
            baseUrl 
            + "/" 
            + URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
        );
        File file = new File(UPLOADS_FOLDER_PATH + File.separator + filename);
        FileUtils.copyURLToFile(url, file);
    }
}
