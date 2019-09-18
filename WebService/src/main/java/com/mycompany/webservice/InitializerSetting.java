/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.webservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author brian
 */
@Singleton
@Startup
public class InitializerSetting {
    static final String DEFAULT_PORT = "8080";
    static final Map<String,String> SERVER_SETTING_VARS 
        = new HashMap<>();  
    static final String SERVER_CONFIG_FILE_PATH = System
        .getProperty("catalina.base") + File.separator + "config"
        + File.separator + "web-service-setting.txt";
    
    @PostConstruct
    public void onStartup() {
        try {
            this.readEnvVariables();
            this.connectToWebManagement();
        } catch (Exception e) {
            System.err.println(
                "Initializing web service error: " + e.getMessage()
            );
        }
    }
    
    private void readEnvVariables() throws FileNotFoundException, IOException {
        File settingFile = new File(SERVER_CONFIG_FILE_PATH);
        BufferedReader br = new BufferedReader(new FileReader(settingFile));
        String line;
        while ((line = br.readLine()) != null) {
          String[] config = line.split("=");
          SERVER_SETTING_VARS.put(config[0], config [1]);
        }
    }   
    
    private void connectToWebManagement() {
        try{
            String ip = InetAddress.getLocalHost().getHostAddress();
            String address = ip + ":"
                + SERVER_SETTING_VARS.get("WEB_SERVICE_PORT");
            String endpoint = "http://"
                + SERVER_SETTING_VARS.get("WEB_MANAGER_ADDRESS")
                + "/WebManager/webresources/connection";
            String methodType = "POST";
            System.out.println("ADDRESS " + endpoint);
            URL url = new URL(endpoint);
            HttpURLConnection urlConnection = (HttpURLConnection)
                url.openConnection();
            urlConnection.setRequestMethod(methodType);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty(
                "Content-type", MediaType.TEXT_PLAIN
            );
            urlConnection.setRequestProperty(
                "Accept", MediaType.TEXT_PLAIN
            );
            OutputStream os = urlConnection.getOutputStream();
            byte[] input = address.getBytes("utf-8");
            os.write(input, 0, input.length);
            System.out.println(
                "Connection with Web Manager: " + urlConnection.getResponseCode());
        }catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
