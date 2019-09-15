/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.webservice;

import com.google.gson.Gson;
import com.server.files.ServerFiles;
import com.server.files.SharedFile;
import java.io.File;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author brian
 */
@Path("files")
public class FilesResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of FilesResource
     */
    public FilesResource() {
    }

    /**
     * Retrieves representation of an instance of com.mycompany.webservice.FilesResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String index() {
        Gson gson = new Gson();
        ServerFiles serverFiles = ServerFiles.getInstance();
        try {
            SharedFile[] files = serverFiles.indexSharedFiles();
            return gson.toJson(files);
        } catch (Exception e) {
            return gson.toJson("An error ocurred");
        }
    }
    
    /**
     * Retrieves representation of an instance of com.mycompany.webservice.FilesResource
     * @param filename
     * @return an instance of java.io.File
     */
    @GET
    @Path("{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public File show(@PathParam("filename") String filename) {
        ServerFiles serverFiles = ServerFiles.getInstance();
        try {
            return serverFiles.getSharedFile(filename);
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }
}
