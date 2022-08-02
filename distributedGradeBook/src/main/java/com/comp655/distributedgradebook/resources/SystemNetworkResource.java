/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook.resources;

import com.comp655.distributedgradebook.Server;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@Path("/admin")
@ApplicationScoped
public class SystemNetworkResource {
    private String protocol = "http";
    String localIPv4Address = "";
    // TODO add RESTful getters and setters for the following two Strings
    private int port = 8080;
    private String appPath = "/distributedGradeBook";
    
    private ArrayList<Server> networkMembers = new ArrayList<Server>();
    
    private Server thisServer = new Server();
    
    public SystemNetworkResource() {
        this.thisServer.setUrl(getLocalUrl());
        this.networkMembers.add(thisServer);
    }
    
    public Server getLocalServer() {
        return this.thisServer;
    }
    
    public ArrayList<Server> getPeersInNetwork() {
        return this.networkMembers;
    }
    
    /*
    * this method is for this distributedGradeBook instance to join the network
    * when manually provided with one valid address for another instance
    */
    @PUT
    @Path("join_network/host/{host}/port/{port}")
    public Response joinNetwork(@PathParam("host") String host, @PathParam("port") int port) throws JAXBException {
        String rem_address = host;
        int rem_port = port;
        URL remoteUrl;
        try {
            remoteUrl = new URL(protocol, rem_address, rem_port, appPath);
        } catch (MalformedURLException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }
        String remoteUri = remoteUrl.toString();
        String localUri = getLocalUrl().toString();
        Client cli = ClientBuilder.newClient();
        Response rsp;
        try {
            rsp = cli.target(remoteUrl.toURI()).request().put(Entity.xml(localUri));
        } catch (URISyntaxException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }
        if (rsp != null) {
            if (rsp.getStatus() == Response.Status.BAD_REQUEST.ordinal()) {
                return rsp;     //Bad request from remote
            }
            // happy path:
            // jaxb unmarshal
            JAXBContext c = JAXBContext.newInstance(Server.class);
            Unmarshaller u = c.createUnmarshaller();
            // parse returned XML for servers to add to local networkMembers EXCEPT local_address
            return Response
                    .ok()
                    .build();
        }
        return Response
                .status(Response.Status.BAD_REQUEST)
                .build();
    }
    
    public URL getLocalUrl() {
        if (!"".equals(localIPv4Address)) {
            return buildLocalUri();
        }
        
        // find local address
        Client client = ClientBuilder.newClient();
        // TODO try/catch when network is down, exit gracefully
        Response rsp = client
                .target("http://api.ipify.org")
                .request(MediaType.TEXT_PLAIN)
                .get();
        if ( rsp.getStatus() == 200 ) {
            localIPv4Address = rsp.readEntity(String.class);
        } else {
            localIPv4Address = "localhost";
        }
        
        return buildLocalUri();
    }
    
    /*
    * This method relies on SystemNetworkResource properties having already been set
    * It should be refactored to take parameters
    */
    private URL buildLocalUri() {
        URL localUri = null;
        try {
            localUri = new URL(protocol, localIPv4Address, port, appPath);
        } catch (MalformedURLException e) {
            //TODO log something here
            // I know this is lazy
        }
        // localUri.toString looks like http://74.139.104.38:8080/distributedGradeBook
        return localUri;
    }
        
    /*
    * this method is for automated or manual access from a
    * remote distributedGradeBook instance to join the network. 
    */
    @PUT
    @Path("peer/host/{host}/port/{port}")
    @Produces("application/xml")
    public Response addServerToNetwork(@PathParam("host") String host, @PathParam("port") int port) throws JAXBException, FileNotFoundException {
        Server newServer = new Server();
        try {
            newServer.setUrl(new URL(protocol, host, port, appPath));
        } catch (MalformedURLException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }
        // check for duplicate
        for (Server s : networkMembers) {
            if (s.getUrl().equals(newServer.getUrl())) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .build();
            }
        }
        // build and return list
        JAXBContext c = JAXBContext.newInstance( Server.class );
        Marshaller m = c.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        File xml = new File("servers.xml");
        OutputStream os = new FileOutputStream(xml);
        for (Server s : networkMembers) {
            m.marshal(s, os);
        }
        networkMembers.add(newServer);
        return Response
                .ok(xml)
                .build();
    }
}
