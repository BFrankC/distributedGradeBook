package com.comp655.distributedgradebook.resources;

import com.comp655.distributedgradebook.Gradebook;
import com.comp655.distributedgradebook.GradebookList;
import com.comp655.distributedgradebook.GradebookMap;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author 
 */
@Path("gradebook")
public class GradeBookResource {
    
    private GradebookMap<UUID, Gradebook> gradebookMap = new GradebookMap<>();
    
    @GET
    @Produces("application/xml")
    public StreamingOutput getAllStudents() throws JAXBException {        
        // set up marshaller.
        JAXBContext jc = JAXBContext.newInstance( GradebookMap.class, Gradebook.class );
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        return (OutputStream outputStream) -> {
            try {
                m.marshal(gradebookMap, outputStream);
            } catch (JAXBException ex) {
                Logger.getLogger(GradebookList.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
    }
    
    @PUT
    @Path("{name}")   
    public Response putCreatePrimaryGradebook(@PathParam("name") String name) {
        Gradebook newBook = new Gradebook(name);
        // TODO: is this needed?  probably not
        if (gradebookMap.put(newBook.getID(), newBook) == null) {
            return Response
                    .ok("Added new primary gradebook " + 
                            name + 
                            " | id: " + 
                            newBook.getID())
                    .build();
        } 
        return Response
                // a gradebook with this UUID already existed
                // but it shouldn't have
                .status(Status.CONFLICT)
                .build();
    }
    
    @POST
    @Path("{name}")   
    public Response postCreatePrimaryGradebook(@PathParam("name") String name) {
        Gradebook newBook = new Gradebook(name);
        if (gradebookMap.put(newBook.getID(), newBook) == null) {
            return Response
                    .ok("Added new primary gradebook " + 
                            name + 
                            " | id: " + 
                            newBook.getID())
                    .build();
        } 
        return Response
                .status(Status.CONFLICT)
                .build();
    }
    
    @DELETE
    @Path("{id}")   
    public Response deleteGradebookByID(@PathParam("id") String id) {
        Gradebook removed = gradebookMap.remove(UUID.fromString(id));
        if (removed != null) {
            // - - - - - - - - - - - - - - - - - - T O D O - - - - - - - - - - -
            // P R O P I G A T E   T O   S E C O N D A R Y   S E R V E R
            return Response
                    .ok("Deleted gradebook " + removed.getTitle())
                    .build();
        }
        return Response
                .status(Status.NOT_FOUND)
                .build();
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
        
        if (gradebookMap.containsKey(UUID.fromString(id))) {
            return (OutputStream outputStream) -> {
                try {
                    m.marshal(gradebookMap.get(UUID.fromString(id)).getStudents(), outputStream);
                } catch (JAXBException ex) {
                    Logger.getLogger(GradeBookResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
        }
        
        return (OutputStream outputStream) -> {
            try {
                m.marshal("", outputStream);
            } catch (JAXBException ex) {
                Logger.getLogger(GradeBookResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
    }
    
    @GET
    @Path("{id}/student/{name}")
    @Produces("application/xml")
    public Response getStudentFromGradebook(@PathParam("id") String id, @PathParam("name") String name) {
        if (this.gradebookMap.containsKey(UUID.fromString(name))) {
            // - - - - - - - - - - - - - - - - - - T O D O - - - - - - - - - - -
            // Check if student exists in gradebook {id}, build response
        }
        return Response
                .status(Status.NOT_IMPLEMENTED)
                .build();
    }
    
    @DELETE
    @Path("{id}/student/{name}")
    @Produces("application/xml")
    public Response deleteStudentFromGradebook(@PathParam("id") String id, @PathParam("name") String name) {
        if (this.gradebookMap.containsKey(UUID.fromString(name))) {
            this.gradebookMap.remove(UUID.fromString(name));
            // - - - - - - - - - - - - - - - - - - T O D O - - - - - - - - - - -
            // P R O P I G A T E   T O   S E C O N D A R Y   S E R V E R
            return Response
                    .ok()
                    .build();
        }
        return Response
                .status(Status.NOT_FOUND)
                .build();
    }
    
    @PUT
    @Path("{id}/student/{name}/grade/{grade}")
    @Produces("application/xm/")
    public Response addStudentToGradebook(@PathParam("id") String id, 
                                          @PathParam("name") String name,
                                          @PathParam("grade") String grade
                                        ) {
        if (!this.gradebookMap.containsKey(UUID.fromString(id))) {
            return Response
                    .status(Status.NOT_FOUND)
                    .build();
        }
        if (!isValidGrade(grade)) {
            return Response
                    .status(Status.BAD_REQUEST)
                    .build();
        }
        this.gradebookMap.get(UUID.fromString(id)).addStudent(name, grade);

        // - - - - - - - - - - - - - - - - - - T O D O - - - - - - - - - - -
        // P R O P I G A T E   T O   S E C O N D A R Y   S E R V E R
        return Response
                .ok()
                .build();
    }
    
    @POST
    @Path("{id}/student/{name}/grade/{grade}")
    @Produces("application/xml")
    public Response updateStudentInGradebook(@PathParam("id") String id, 
                                             @PathParam("name") String name,
                                             @PathParam("grade") String grade
                                        ) {
        if (!this.gradebookMap.containsKey(UUID.fromString(id))) {
            return Response
                    .status(Status.NOT_FOUND)
                    .build();
        }
        if (!isValidGrade(grade)) {
            return Response
                    .status(Status.BAD_REQUEST)
                    .build();
        }
        this.gradebookMap.get(UUID.fromString(id)).addStudent(name, grade);

        // - - - - - - - - - - - - - - - - - - T O D O - - - - - - - - - - -
        // P R O P I G A T E   T O   S E C O N D A R Y   S E R V E R
        return Response
                .ok()
                .build();
    }
    
    /**
     * @author Matt
     * I tested and used this on project 1, I believe it to be correct.
     * 
     */
    private boolean isValidGrade(String grade) {
        Pattern pattern = Pattern.compile("(^)(?:[A-D][+-]?|[a-d][+-]?|[FfIiWwZz])($)");
        Matcher match = pattern.matcher(grade);
        return match.matches();
    }
}
