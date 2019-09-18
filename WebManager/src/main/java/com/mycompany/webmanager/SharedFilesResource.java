/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.webmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author brian
 */
@Path("shared_files")
public class SharedFilesResource {
    
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of SharedFilesResource
    */
    ConnectionManagement cm;
    public SharedFilesResource() {
        this.cm = ConnectionManagement.getInstance();
    }

    /**
     * Retrieves representation of an instance of com.mycompany.webmanager.SharedFilesResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index() {
        ArrayList<String> serversClone = this.cm.getServers();
        while (!serversClone.isEmpty()) {
            String selectedServer = this.cm.getServer();
            serversClone.remove(selectedServer);
            try{
                String endpoint = "http://" + this.cm.getServer() 
                    + "/WebService/webresources/files";
                String methodType="GET";
                URL url = new URL(endpoint);
                HttpURLConnection urlConnection = (HttpURLConnection) 
                    url.openConnection();
                urlConnection.setRequestMethod(methodType);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty(
                    "Content-type", MediaType.APPLICATION_JSON
                );
                urlConnection.setRequestProperty(
                    "Accept", MediaType.APPLICATION_JSON
                );
                int httpResponseCode = urlConnection.getResponseCode();
                if (httpResponseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    return Response.ok(content.toString()).build();
                } else {
                    if (serversClone.isEmpty()) {
                        return Response
                            .status(httpResponseCode).build();
                    }
                } 
            } catch (IOException ex) {
                System.err.println(ex);
                return Response.serverError().build();
            }
        }
        return Response.status(HttpURLConnection.HTTP_UNAVAILABLE).build();
    }
    
     /**
     * Retrieves representation of an instance of com.mycompany.webservice.FilesResource
     * @param filename
     * @return an instance of java.io.File;
     */
    @GET
    @Path("{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response show(@PathParam("filename") String filename) {
        ArrayList<String> serversClone = this.cm.getServers();
        while (!serversClone.isEmpty()) {
            String selectedServer = this.cm.getServer();
            serversClone.remove(selectedServer);
            try{
                String endpoint = "http://" + this.cm.getServer() 
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
                int httpResponseCode = urlConnection.getResponseCode();
                if (httpResponseCode == HttpURLConnection.HTTP_OK) {
                    return Response.ok(urlConnection.getInputStream()).build();
                } else {
                    if (serversClone.isEmpty()) {
                         return Response
                            .status(httpResponseCode).build();
                    }
                }
            } catch (IOException e) {
                System.err.println(e);
                return Response.serverError().build();
            }
        }
        return Response.status(HttpURLConnection.HTTP_UNAVAILABLE).build();
    }
}
