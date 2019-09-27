/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.webmanager;

import com.google.gson.Gson;
import java.util.ArrayList;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author brian
 */
@Path("connection")
public class ConnectionResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of ConnectionResource
    */
    ConnectionManagement cm;
    public ConnectionResource() {
        this.cm = ConnectionManagement.getInstance();
    }   
    
    /**
     * Retrieves representation of an instance of com.mycompany.webmanager.SharedFilesResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index() {
        Gson gson = new Gson();
        return Response.ok(gson.toJson(this.cm.getServers())).build();
    }

    /**
     * POST method for updating or creating an instance of ConnectionResource
     * @param server representation for the resource
     * @return 
     */
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(String server) {
        Gson gson = new Gson();
        System.out.println(server);
        if (!this.cm.getServers().contains(server)) {
            this.cm.addServer(server);
        }
        ArrayList<String> fileIndex = this.cm.fetchAllFiles(server);
        return Response.ok(gson.toJson(fileIndex)).build();
    }
}
