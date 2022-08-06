/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook.resources;

import com.comp655.distributedgradebook.Gradebook;
import com.comp655.distributedgradebook.GradebookMap;
import com.comp655.distributedgradebook.Server;
import com.comp655.distributedgradebook.Student;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;

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
        if (localBooks.containsKey(UUID.fromString(id))) {
            // id found locally:
            if ( localBooks.get(UUID.fromString(id)).getSecondaryUrl() != null ) {
                // max of one secondary already exists
                return Response
                        .status(Response.Status.CONFLICT)
                        .build();
            }
            // create remote copy of local Gradebook
            Gradebook bookToCopy = localBooks.get(UUID.fromString(id));
            // choose a destination
            ArrayList<Server> networkPeerList = networkContext.getPeersInNetwork();
            Random random = new Random();
            Server randomDestination = networkPeerList.get(random.nextInt(networkPeerList.size()));
            if (randomDestination.getId().equals(networkContext.getLocalServer().getId())) {
                // this instance is the only server in the network
                // sysadmin forgot to setup networking
                return Response
                        .status(Response.Status.PRECONDITION_REQUIRED)
                        .build();
            }
            if (randomDestination.getUrl().equals(networkContext.getLocalServer().getUrl())) {
                // randomly chose self, choose the next one
                randomDestination = networkPeerList.get( (networkPeerList.indexOf(randomDestination) + 1) % networkPeerList.size());
            }
            String remoteAddress = randomDestination
                    .getUrl()
                    .toString() + "/secondary/" + id;
            // marshal bookToCopy and send to a REST resource
            // that can unmarshal and add it to the destination secondary map
            // this will preserve the name and UUID
            JAXBContext c = JAXBContext.newInstance(Gradebook.class);
            Marshaller m = c.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StreamingOutput out = (OutputStream os) -> {
                try {
                    m.marshal(bookToCopy, os);
                } catch (JAXBException ex) {
                    Logger.getLogger(SecondaryResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            // new client, request, send to /remoteAddress/secondary/id
            // then put remoteAddress in bookToCopy.setSecondaryUrl()
            Client cli = ClientBuilder.newClient();
            Response rsp = cli.target(remoteAddress).request().put(Entity.xml(out));
            bookToCopy.setSecondaryUrl(randomDestination.getUrl());
            return Response
                .ok()
                .build();
        } else {
            // id not found locally, find remotely and store locally here
            URL remoteLocation = priGradebookRes.searchLocalAndRemote(UUID.fromString(id));
            if (remoteLocation == null ) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .build();
            }
            Client cli = ClientBuilder.newClient();
            Response rsp = cli.target(remoteLocation + "/gradebook/" + id + "/student").request().get();
            // jaxb unmarshal
            JAXBContext c = JAXBContext.newInstance(Gradebook.class);
            Unmarshaller u = c.createUnmarshaller();
            StringBuffer xmlString = new StringBuffer(rsp.readEntity(String.class));
            Gradebook importedGradebook = (Gradebook) u.unmarshal(new StreamSource(new StringReader(xmlString.toString())));
            importedGradebook.setID(UUID.fromString(id));
            this.secondaryGradebookMap.put(importedGradebook.getID(), importedGradebook);
            
            return Response
                    .ok(xmlString.toString())
                    .build();
        }
    }
    
    @POST
    @Path("{id}")   
    public Response postCreateSecondaryGradebook(@PathParam("id") String id) throws JAXBException {
        GradebookMap<UUID, Gradebook> localBooks = this.getLocalPrimaryGradebooks();
        if (localBooks.containsKey(UUID.fromString(id))) {
            // id found locally:
            if ( localBooks.get(UUID.fromString(id)).getSecondaryUrl() != null ) {
                // max of one secondary already exists
                return Response
                        .status(Response.Status.CONFLICT)
                        .build();
            }
            // create remote copy of local Gradebook
            Gradebook bookToCopy = localBooks.get(UUID.fromString(id));
            // choose a destination
            ArrayList<Server> networkPeerList = networkContext.getPeersInNetwork();
            Random random = new Random();
            Server randomDestination = networkPeerList.get(random.nextInt(networkPeerList.size()));
            if (randomDestination.getId().equals(networkContext.getLocalServer().getId())) {
                // this instance is the only server in the network
                // sysadmin forgot to setup networking
                return Response
                        .status(Response.Status.PRECONDITION_REQUIRED)
                        .build();
            }
            if (randomDestination.getUrl().equals(networkContext.getLocalServer().getUrl())) {
                // randomly chose self, choose the next one
                randomDestination = networkPeerList.get( (networkPeerList.indexOf(randomDestination) + 1) % networkPeerList.size());
            }
            String remoteAddress = randomDestination
                    .getUrl()
                    .toString() + "/secondary/" + id;
            // marshal bookToCopy and send to a REST resource
            // that can unmarshal and add it to the destination secondary map
            // this will preserve the name and UUID
            JAXBContext c = JAXBContext.newInstance(Gradebook.class);
            Marshaller m = c.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StreamingOutput out = (OutputStream os) -> {
                try {
                    m.marshal(bookToCopy, os);
                } catch (JAXBException ex) {
                    Logger.getLogger(SecondaryResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            // new client, request, send to /remoteAddress/secondary/id
            // then put remoteAddress in bookToCopy.setSecondaryUrl()
            Client cli = ClientBuilder.newClient();
            Response rsp = cli.target(remoteAddress).request().put(Entity.xml(out));
            bookToCopy.setSecondaryUrl(randomDestination.getUrl());
            return Response
                .ok()
                .build();
        } else {
            // id not found locally, find remotely and store locally here
            URL remoteLocation = priGradebookRes.searchLocalAndRemote(UUID.fromString(id));
            if (remoteLocation == null ) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .build();
            }
            Client cli = ClientBuilder.newClient();
            Response rsp = cli.target(remoteLocation + "/gradebook/" + id + "/student").request().get();
            // jaxb unmarshal
            JAXBContext c = JAXBContext.newInstance(Gradebook.class);
            Unmarshaller u = c.createUnmarshaller();
            StringBuffer xmlString = new StringBuffer(rsp.readEntity(String.class));
            Gradebook importedGradebook = (Gradebook) u.unmarshal(new StreamSource(new StringReader(xmlString.toString())));
            importedGradebook.setID(UUID.fromString(id));
            this.secondaryGradebookMap.put(importedGradebook.getID(), importedGradebook);
            
            return Response
                    .ok(xmlString.toString())
                    .build();
        }
    }
    
    /*
    * this is only called by other instances that have already
    * validated the values
    *
    * if we cared, this would be a huge security issue
    */
    @PUT
    @Path("{id}/update/{name}/grade/{grade}")
    public Response privateUpdate(@PathParam("id") String id,
                                    @PathParam("name") String name,
                                    @PathParam("grade") String grade) {
        if (secondaryGradebookMap.containsKey(UUID.fromString(id))) {
            Gradebook bookToUpdate = secondaryGradebookMap.get(UUID.fromString(id));
            bookToUpdate.addOrUpdateStudent(name, grade);
            return Response
                .ok()
                .build();
        }
        return Response
                .status(Response.Status.NOT_FOUND)
                .build();
    }
    
    @DELETE
    @Path("{id}/student/{name}")
    public Response privateDelete(@PathParam("id") String id,
                                    @PathParam("name") String name) {
        if (secondaryGradebookMap.containsKey(UUID.fromString(id))) {
            Gradebook bookToUpdate = secondaryGradebookMap.get(UUID.fromString(id));
            bookToUpdate.deleteStudent(name);
            return Response
                .ok()
                .build();
        }
        return Response
                .status(Response.Status.NOT_FOUND)
                .build();
    }
    
    @DELETE
    @Path("{id}")   
    public Response deleteSecondaryGradebookById(@PathParam("id") String id) {
        Gradebook removed = secondaryGradebookMap.remove(UUID.fromString(id));
        if (removed == null) {
            return Response
                .status(Response.Status.NOT_FOUND)
                .build();
        }
        // notify removed's primary copy that secondary URL is no longer valid
        URL primaryLocation = priGradebookRes.searchLocalAndRemote(UUID.fromString(id));
        if (primaryLocation != null) {
            Client c = ClientBuilder.newClient();
            c.target(primaryLocation.toString() + "/gradebook/" + id + "/secondary").request().delete();
        }
        
        return Response
                .ok("Deleted secondary gradebook " + removed.getTitle())
                .build();
    }
}
