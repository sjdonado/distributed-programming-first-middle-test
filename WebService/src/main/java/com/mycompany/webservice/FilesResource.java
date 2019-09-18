/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.webservice;

import com.google.gson.Gson;
import com.mycompany.webmanagerclient.SharedFile;
import com.server.files.ServerFiles;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    public Response index() {
        Gson gson = new Gson();
        ServerFiles serverFiles = ServerFiles.getInstance();
        try {
            ArrayList<SharedFile> files = serverFiles.indexSharedFiles();
            return Response.ok(gson.toJson(files)).build();
        } catch (Exception e) {
            return Response.serverError().build();
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
    public Response show(@PathParam("filename") String filename) {
        ServerFiles serverFiles = ServerFiles.getInstance();
        try {
            File file = serverFiles.getSharedFile(
                URLDecoder.decode(filename, StandardCharsets.UTF_8.toString())
            );
            return Response.ok(file).build();
        } catch (UnsupportedEncodingException e) {
            System.err.println(e);
        }
        return Response.status(404).build();
    }
}
