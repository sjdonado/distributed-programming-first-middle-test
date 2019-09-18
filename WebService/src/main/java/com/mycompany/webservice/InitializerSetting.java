/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.webservice;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
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
    final static String DEFAULT_PORT = "8080";
    @PostConstruct
    public void onStartup() {
        this.connectToWebManagement();
    }
    
    private void connectToWebManagement() {
        try{
            String ip = InetAddress.getLocalHost().getHostAddress();
            String address = ip + ":" + DEFAULT_PORT; 
            String endpoint = "http://127.0.0.1:8080/WebManager/webresources/connection";
            String methodType = "POST";
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
