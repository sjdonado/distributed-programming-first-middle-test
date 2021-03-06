/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.webmanagerclient;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author brian
 */
public class WebManagerClient {
    private final String BASE_URL;
    
    public WebManagerClient(String ip, int port) {
        this.BASE_URL = "http://" + ip + ":" + port 
            + "/WebManager/webresources/shared_files";
    }
    
    public boolean downloadFile(String filename, String folderPath) {
        try{
            URL url = new URL(
                this.BASE_URL 
                + "/" 
                + URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
            );
            File file = new File(folderPath + File.separator + filename);
            FileUtils.copyURLToFile(url, file);
            return true;
        }catch (IOException ex) {
            System.err.println(ex);
        }
        return false;
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
