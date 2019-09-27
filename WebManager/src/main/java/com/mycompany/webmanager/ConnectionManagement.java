/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.webmanager;

import com.google.gson.Gson;
import java.io.File;
import java.util.ArrayList;
import com.mycompany.webmanagerclient.SharedFile;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

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
    
    public void syncWebServiceFiles(String sv) {
        Map<String, String> serverFiles = new HashMap<>();
        this.servers.forEach(server -> {
            if (!server.equals(sv)) {
                SharedFile[] fls = this.fetchServerFileList(server);
                for(SharedFile sharedFile : fls) {
                    String filename = sharedFile.getName();
                    if (!checkIfFileExists(serverFiles.values(), filename)) {
                        serverFiles.put(server, filename);
                        InputStream fileStream;
                        try {
                            File fl = this.downloadFile(server, filename);
                            this.uploadFile(sv, filename, fl);
                        } catch (IOException ex) {
                            Logger.getLogger(ConnectionManagement.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        });
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
    
    private boolean checkIfFileExists(Collection<String> currentFiles, String filename) {
        return currentFiles.stream().anyMatch(
            (fname) -> (fname.equals(filename))
        );
    }
    
    private File downloadFile(String address, String filename) throws IOException {
        String endpoint = "http://" + address
            + "/WebService/webresources/files";
        URL url = new URL(endpoint + "/" + filename);
        String methodType = "GET";
        HttpURLConnection urlConnection = (HttpURLConnection) 
            url.openConnection();
        urlConnection.setRequestMethod(methodType);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty(
            "Content-type", MediaType.APPLICATION_OCTET_STREAM
        );
        urlConnection.setRequestProperty(
            "Accept", MediaType.APPLICATION_OCTET_STREAM
        );
        InputStream t = urlConnection.getInputStream();
        File file = new File(filename);
        FileUtils.copyURLToFile(url, file);
        return file;
    }
    
    private void uploadFile(String address, String filename, File file) throws IOException {
        String endpoint = "http://" + address
            + "/WebService/webresources/files/upload";
        URL url = new URL(endpoint + "/" + filename);
        String methodType = "POST";
        HttpURLConnection urlConnection = (HttpURLConnection) 
            url.openConnection();
        urlConnection.setRequestMethod(methodType);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty(
            "Content-type", MediaType.APPLICATION_OCTET_STREAM
        );
        urlConnection.setRequestProperty(
            "Accept", MediaType.APPLICATION_OCTET_STREAM
        );
        OutputStream os = urlConnection.getOutputStream();
        IOUtils.copy(new FileInputStream(file),os);
        System.out.println(
            "Upload file to Web Service => " + urlConnection.getResponseCode()
        );
    }
}