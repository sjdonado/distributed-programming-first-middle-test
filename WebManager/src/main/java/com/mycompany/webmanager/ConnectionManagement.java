/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.webmanager;

import com.google.gson.Gson;
import java.util.ArrayList;
import com.mycompany.webmanagerclient.SharedFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author brian
 */
public class ConnectionManagement {
    public static ConnectionManagement instance;
    private final ArrayList<String> servers;

    private Integer pos;    

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
            if (pos >= serverList.size()) {
                pos = 0;   
            }    
            server = serverList.get(pos);   
            pos ++;   
        }
        return server;
    }
    
    public ArrayList<String> getServers() {
        return servers;
    }
    
    public ArrayList<String> fetchAllFiles(String sv) {
        ArrayList<String> currentFiles = new ArrayList<>();
        this.servers.forEach(server -> {
            if (!server.equals(sv)) {
                SharedFile[] fls = this.fetchServerFileList(server);
                for(SharedFile sharedFile : fls) {
                    String filename = sharedFile.getName();
                    if (!checkIfFileExists(currentFiles, filename)) {
                        currentFiles.add(server);
                    }
                }
            }
        });
        return currentFiles;
    }
    
    private SharedFile[] fetchServerFileList(String address) {
        try{
            String methodType = "GET";
            URL url = new URL("http://" + address + "/WebService/webresources/files");
            HttpURLConnection urlConnection= (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(methodType);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            int httpResponseCode = urlConnection.getResponseCode();
            if (httpResponseCode == HttpURLConnection.HTTP_OK) {
                Gson gson = new Gson();
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                return gson.fromJson(content.toString(), SharedFile[].class);
            }
        }catch (IOException ex) {
            System.err.println(ex);
        }
        return null;
    }
    
    private boolean checkIfFileExists(ArrayList<String> currentFiles, String filename) {
        return currentFiles.stream().anyMatch(
            (fname) -> (fname.equals(filename))
        );
    }
}