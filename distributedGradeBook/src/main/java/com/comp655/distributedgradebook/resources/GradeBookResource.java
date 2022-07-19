package com.comp655.distributedgradebook.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author 
 */
@Path("gradebook")
public class GradeBookResource {
    
    @GET
    public Response ping(){
        return Response
                .ok("no data")
                .build();
    }
}
