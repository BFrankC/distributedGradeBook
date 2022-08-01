package com.comp655.distributedgradebook.resources;

import com.comp655.distributedgradebook.Gradebook;
import com.comp655.distributedgradebook.GradebookList;
import com.comp655.distributedgradebook.GradebookMap;
import com.comp655.distributedgradebook.Server;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author 
 */
@Path("/gradebook")
@ApplicationScoped
public class GradeBookResource {
    
    private GradebookMap<UUID, Gradebook> gradebookMap = new GradebookMap<>();
    
    @Inject
    SecondaryResource secGradebookRes;
    @Inject
    SystemNetworkResource networkContext;
    
    
    public GradebookMap<UUID, Gradebook> getPrimaryGradebookMap() {
        return gradebookMap;
    }
        
    @GET
    @Produces("application/xml")
    public StreamingOutput getAllStudents() throws JAXBException {   
        // TODO fix: this is returning the students within the gradebooks too, not just title + ID
        // TODO: this should search local and remote gradebookMap
        
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
    
    /*
    * only have to search for primaries.
    * this method's caller may want to read or write, and 
    * when primary is found, it will have the url of a secondary
    * (if one exists)
    */
    private URL searchLocalAndRemote(UUID uuid) {
        URL url = null; 
        
        if (gradebookMap.contains(uuid)) {
            url = networkContext.getLocalUrl();
        } else {
            for (Server peer : networkContext.getPeersInNetwork()) {
                URL remote = peer.getUrl();
                Client c = ClientBuilder.newClient();
                Response rsp;
                rsp = c.target(remote.toString() + "/gradebook/" + uuid.toString()).request(MediaType.APPLICATION_XML).get();
                if (rsp.getStatus() == 200) {
                    return remote;
                }
            }
        }
        return url;
    }
    
    @PUT
    @Path("{name}")   
    public Response putCreatePrimaryGradebook(@PathParam("name") String name) {
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
            if (removed.getSecondaryUrl() != null) {
                Client c = ClientBuilder.newClient();
                Response rsp;
                rsp = c.target(removed.getSecondaryUrl().toString() + "/gradebook/" + id).request().delete();
            }
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
        // TODO this should search gradebookMap and secGradebookMap
        // and if not found search remotes
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
                m.marshal("gradebook not found", outputStream);
            } catch (JAXBException ex) {
                Logger.getLogger(GradeBookResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
    }
    
    @GET
    @Path("{id}/student/{name}")
    @Produces("application/xml")
    public Response getStudentFromGradebook(@PathParam("id") String id, @PathParam("name") String name) {
        if (this.gradebookMap.containsKey(UUID.fromString(id))) {
            if (this.gradebookMap.get(UUID.fromString(id)).getStudents().contains(name)) {
            // found student in a primary gradebook
            return Response
                    .ok("name: " + name + " | grade: " + this.gradebookMap.get(UUID.fromString(id)).getStudentGrade(name))
                    .build();
            }
            // found gradebook but not student
            return Response
                    .status(Status.NOT_FOUND)
                    .build();
        }
        // didn't find student in primary ... look in secondary
        GradebookMap<UUID, Gradebook> secGradebookMap = secGradebookRes.getLocalSecondaryGradebooks();
        if (secGradebookMap.containsKey(UUID.fromString(id))) {
            if (secGradebookMap.get(UUID.fromString(id)).getStudents().contains(name)) {
            // found student in a primary gradebook
            return Response
                    .ok("name: " + name + " | grade: " + secGradebookMap.get(UUID.fromString(id)).getStudentGrade(name))
                    .build();
            }
            // found gradebook but not student
            return Response
                    .status(Status.NOT_FOUND)
                    .build();
        }
        // did not find locally, look in remote server
        ArrayList<Server> networkPeerList = networkContext.getPeersInNetwork();
        Client c = ClientBuilder.newClient();
        for (Server remote : networkPeerList) {
            Response rsp = c.target(remote.getUrl().toString() + "/gradebook/" + id + "/student/" + name).request(MediaType.APPLICATION_XML).get();
            if (rsp.getStatus() == 200) {
                return rsp;
            }
        }
        return Response
                .status(Status.NOT_FOUND)
                .build();
    }
    
    @DELETE
    @Path("{id}/student/{name}")
    @Produces("application/xml")
    public Response deleteStudentFromGradebook(@PathParam("id") String id, @PathParam("name") String name) {
        if (this.gradebookMap.containsKey(UUID.fromString(id))) {
            this.gradebookMap.get(UUID.fromString(id)).deleteStudent(name);
            // - - - - - - - - - - - - - - - - - - T O D O - - - - - - - - - - -
            // P R O P I G A T E   T O   S E C O N D A R Y   S E R V E R
            return Response
                    .ok("deleted student " + name)
                    .build();
        }
        return Response
                .status(Status.NOT_FOUND)
                .build();
    }
    
    @PUT
    @Path("{id}/student/{name}/grade/{grade}")
    @Produces("application/xml")
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
        this.gradebookMap.get(UUID.fromString(id)).addOrUpdateStudent(name, grade);

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
        this.gradebookMap.get(UUID.fromString(id)).addOrUpdateStudent(name, grade);

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
