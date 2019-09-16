/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.webservice;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Set;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author brian
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        addRestResourceClasses(resources);
        this.connectToWebManagement();
        return resources;
    }
    
    private void connectToWebManagement() {
        try{
            String ip = InetAddress.getLocalHost().getHostAddress();
            String address = ip + ":8080"; 
            String endpoint = "http://localhost:8080/WebService/webresources/connection";
            String methodType = "POST";
            URL url = new URL(endpoint);
            HttpURLConnection urlConnection = (HttpURLConnection)
                url.openConnection();
            urlConnection.setRequestMethod(methodType);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty(
                "Content-type", MediaType.APPLICATION_JSON
            );
            urlConnection.setRequestProperty(
                "Accept",
                MediaType.APPLICATION_JSON
            );
            OutputStream os = urlConnection.getOutputStream();
            byte[] input = address.getBytes("utf-8");
            os.write(input, 0, input.length);
        }catch (IOException ex) {
            System.err.println(ex);
        }
    }
   
    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.mycompany.webservice.FilesResource.class);
    }
    
}
