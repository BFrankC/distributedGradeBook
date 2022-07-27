/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook.resources;

import com.comp655.distributedgradebook.Gradebook;
import com.comp655.distributedgradebook.GradebookList;
import com.comp655.distributedgradebook.GradebookMap;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
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
    GradeBookResource primaryGradebook;
    private GradebookMap<UUID, Gradebook> primaryGradebookMap = primaryGradebook.getPrimaryGradebookMap();
    
    public GradebookMap<UUID, Gradebook> getSecondaryGradebookMap() {
        return secondaryGradebookMap;
    }
    
    @PUT
    @Path("{id}")   
    public Response putCreateSecondaryGradebook(@PathParam("id") String id) {
        //  To create a secondary copy (not allowed on primary server)
        // TODO 
        // 1. lookup a gradebook on the primary server by ID.
        // 2. get a copy of that gradebook 
        // 3. move the copy into our secondaryGradebookList
        
        // We should probably do some checking here to see if we are adding duplicates or something.  
        // Could also make secondaryGradebookList a map or something to keep duplicates from happening.  
        return Response
                .ok("Added") // " + name + " to secondary gradebook")
                .build();
    }
    
    @POST
    @Path("{id}")   
    public Response postCreateSecondaryGradebook(@PathParam("id") String id) {
        //  To create a secondary copy (not allowed on primary server)
        // TODO 
        // 1. lookup a gradebook on the primary server by ID.
        // 2. get a copy of that gradebook 
        // 3. move the copy into our secondaryGradebookList
        
        // We should probably do some checking here to see if we are adding duplicates or something.  
        // Could also make secondaryGradebookList a map or something to keep duplicates from happening.  
        return Response
                .ok("Added") //+ name + " to gradebook")
                .build();
    }
    
    @DELETE
    @Path("{id}")   
    public Response deleteSecondaryGradebookById(@PathParam("id") String id) {
        Gradebook removed = secondaryGradebookMap.remove(UUID.fromString(id));
        if (removed != null) {
            return Response
                    .ok("Deleted gradebook " + removed.getTitle())
                    .build();
        }
        return Response
                .status(Response.Status.NOT_FOUND)
                .build();
    }
}
