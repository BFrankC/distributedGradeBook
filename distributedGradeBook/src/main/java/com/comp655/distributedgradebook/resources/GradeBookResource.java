package com.comp655.distributedgradebook.resources;

import com.comp655.distributedgradebook.Gradebook;
import com.comp655.distributedgradebook.GradebookList;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 
 */
@Path("gradebook")
public class GradeBookResource {
    
    private GradebookList<Gradebook> gradebookList = new GradebookList<>(); // This server's list of gradebooks.
    
    
    @GET
    @Produces("application/xml")
    public StreamingOutput getAllStudents() throws JAXBException {        
        // set up marshaller.
        JAXBContext jc = JAXBContext.newInstance( GradebookList.class, Gradebook.class );
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        
        
        // - - - - - - -  E X A M P L E     C O D E 
        // - - - - - - - TO BE REMOVED BY BACK END IMPLEMENTER - - - - - - - - - -
//        GradebookList<Gradebook> gradebookList = new GradebookList<>();
        Gradebook testOne = new Gradebook("Alpha");
        testOne.addStudent("name", "A");
        gradebookList.add(testOne);
        
        
        Gradebook testTwo = new Gradebook("Bravo");
        testOne.addStudent("name", "A");
        gradebookList.add(testTwo);
        // - - - - - - - - E N D    E X A M P L E      C O D E
        
        
        
        return (OutputStream outputStream) -> {
            try {
                m.marshal(gradebookList, outputStream);
            } catch (JAXBException ex) {
                Logger.getLogger(GradebookList.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
    }
    
    @PUT
    @Path("{name}")   
    public Response putCreatePrimaryGradebook(@PathParam("name") String name) {
        gradebookList.add(new Gradebook(name));
        // We should probably do some checking here to see if we are adding duplicates or something.  
        // Could also make gradebookList a map or something to keep duplicates from happening.  
        return Response
                .ok("Added " + name + " to gradebook")
                .build();
        
        // - - - - - - - - - - - - - - - - - - T O D O - - - - - - - - - - -
        // P R O P I G A T E   T O   S E C O N D A R Y   S E R V E R
    }
    
    @POST
    @Path("{name}")   
    public Response postCreatePrimaryGradebook(@PathParam("name") String name) {
        gradebookList.add(new Gradebook(name));
        // We should probably do some checking here to see if we are adding duplicates or something.  
        // Could also make gradebookList a map or something to keep duplicates from happening.  
        return Response
        .ok("Added " + name + " to gradebook")
        .build();
        // - - - - - - - - - - - - - - - - - - T O D O - - - - - - - - - - -
        // P R O P I G A T E   T O   S E C O N D A R Y   S E R V E R
    }
    
    @DELETE
    @Path("{id}")   
    public Response deleteGradebookByID(@PathParam("id") String id) {
        // This will be slow.
        String title;
        for(Gradebook book : gradebookList)
        {
            if (book.getID().equals(UUID.fromString(id)))
            {
                title = book.getTitle();
                gradebookList.remove(book);
                return Response
                    .ok("Removed gradebook: " + title)
                    .build();
            }
        }
        return Response
        .ok("No Gradebook Found")
        .build();
        // - - - - - - - - - - - - - - - - - - T O D O - - - - - - - - - - -
        // P R O P I G A T E   T O   S E C O N D A R Y   S E R V E R
    }
    
    @GET
    @Path("{id}/student")
    @Produces("application/xml")
    public StreamingOutput allStudentsInGradebook(@PathParam("id") String id) throws JAXBException
    {
        // set up marshaller.
        JAXBContext jc = JAXBContext.newInstance( Gradebook.class );
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        
        //This should be a function.
        for(Gradebook book : gradebookList)
        {
            if (book.getID().equals(UUID.fromString(id)))
            {
                
                return (OutputStream outputStream) -> {
                    try {
                        m.marshal(book.getStudents(), outputStream);
                    } catch (JAXBException ex) {
                        Logger.getLogger(GradebookList.class.getName()).log(Level.SEVERE, null, ex);
                    }
                };
            }
        }
        
        return (OutputStream outputStream) -> {
            try {
                m.marshal("", outputStream);
            } catch (JAXBException ex) {
                Logger.getLogger(GradebookList.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        
    }
    
    
//    @GET
//    public Response ping(){
//        return Response
//                .ok("no data")
//                .build();
//    }
}
