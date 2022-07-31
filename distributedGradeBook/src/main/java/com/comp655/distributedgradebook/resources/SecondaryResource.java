/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook.resources;

import com.comp655.distributedgradebook.Gradebook;
import com.comp655.distributedgradebook.GradebookList;
import com.comp655.distributedgradebook.GradebookMap;
import com.comp655.distributedgradebook.Server;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * This resource provides the /app-root/secondary/ endpoints.
 * 
 * @author ben
 */

// N O T E
// T H I S   I S  N O T  T H E  S E C O N D A R Y   S E R V E R 
// It is the resource which provides the secondary functions.
// This Resource and GradeBookResource are on the same server.

@Path("/secondary")
@ApplicationScoped
public class SecondaryResource {
    private GradebookMap<UUID, Gradebook> secondaryGradebookMap = new GradebookMap<>(); // the collection of secondary gradebooks
    @Inject
    GradeBookResource priGradebookRes;
    @Inject
    SystemNetworkResource networkContext;
    
    public GradebookMap<UUID, Gradebook> getLocalPrimaryGradebooks() {
        GradebookMap localBooks = priGradebookRes.getPrimaryGradebookMap();
        return localBooks;
    }
    
    public GradebookMap<UUID, Gradebook> getLocalSecondaryGradebooks() {
        return this.secondaryGradebookMap;
    }
    
    @PUT
    @Path("{id}")   
    public Response putCreateSecondaryGradebook(@PathParam("id") String id) throws JAXBException {
        GradebookMap<UUID, Gradebook> localBooks = this.getLocalPrimaryGradebooks();
        if (localBooks.contains(UUID.fromString(id))) {
            // id found locally:
            if (localBooks.get(UUID.fromString(id)).getSecondaryUrl() != null) {
                // max of one secondary already exists
                return Response
                        .status(Response.Status.CONFLICT)
                        .build();
            }
            // create remote copy
            ArrayList<Server> networkPeerList = networkContext.getPeersInNetwork();
            Random random = new Random();
            String remoteAddress = networkPeerList.get(random.nextInt(networkPeerList.size()))
                    .getUrl()
                    .toString();
            JAXBContext jc = JAXBContext.newInstance( Gradebook.class );
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            
        } else {
            // id not local, create local copy
            // for server in network peer list:
                // /server/get/gradebook/{id}/student
                // if found
                    // store results locally in new gradebook
                    // update remote gradebook with local URL
                        // new REST resource PUT/POST /gradebook/{id}/secondaryURL/
                    // response 200 or 201
                // if NOT found
                    // response BAD_REQUEST
        }
        return Response
                // either local copy 
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .build();
    }
    
    @POST
    @Path("{id}")   
    public Response postCreateSecondaryGradebook(@PathParam("id") String id) {
        //  TODO copy/paste other method when it is working/done
        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }
    
    @DELETE
    @Path("{id}")   
    public Response deleteSecondaryGradebookById(@PathParam("id") String id) {
        Gradebook removed = secondaryGradebookMap.remove(UUID.fromString(id));
        if (removed != null) {
            // TODO notify removed's primary copy that secondary URL is no longer valid
            return Response
                    .ok("Deleted secondary gradebook " + removed.getTitle())
                    .build();
        }
        return Response
                .status(Response.Status.NOT_FOUND)
                .build();
    }
}
