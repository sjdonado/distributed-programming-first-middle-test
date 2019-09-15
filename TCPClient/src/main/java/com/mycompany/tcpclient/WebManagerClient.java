/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.tcpclient;

import com.google.gson.Gson;
import com.server.files.SharedFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author brian
 */
public class WebManagerClient {
    static final String DOWNLOAD_DIR = System.getProperty("user.home") 
        + File.separator + "distributed_midterm_downloads";
    private final String BASE_URL;
    public WebManagerClient(String ip, int port) {
        this.BASE_URL = "http://" + ip + ":" + port 
            + "/WebService/webresources/files";
    }
    
    public SharedFile downloadFile(String filename) {
        try{
            String a = DOWNLOAD_DIR;
            URL url = new URL(this.BASE_URL + "/" + filename);
            File file = new File(DOWNLOAD_DIR + File.separator + filename);
            FileUtils.copyURLToFile(url, file);
        }catch (IOException ex) {
            System.err.println(ex);
        }
        return null;
    }
    
    public SharedFile[] fetchFiles() {
        try{
            String methodType = "GET";
            URL url = new URL(this.BASE_URL);
            HttpURLConnection urlConnection=(HttpURLConnection)url.openConnection();
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
                SharedFile[] a = gson.fromJson(content.toString(), SharedFile[].class);
                return a;
            }
        }catch (IOException ex) {
            System.err.println(ex);
        }
        return null;
    }
    
}
