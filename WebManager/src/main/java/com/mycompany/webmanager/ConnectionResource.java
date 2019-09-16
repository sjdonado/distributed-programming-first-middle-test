/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.webmanager;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
     * POST method for updating or creating an instance of ConnectionResource
     * @param server representation for the resource
     * @return 
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putJson(String server) {
        this.cm.addServer(server);
        return Response.noContent().build();
    }
}
