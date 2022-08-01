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
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

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
    *
    * call it with a full host.domain:port/app_uri/ string
    */
    @PUT
    @Path("join_network/{address}")
    public Response joinNetwork(@PathParam("address") String address) {
        String rem_address = address.split(":")[0];
        int rem_port = Integer.parseInt(address.split(":")[1].split("/")[0]);
        String rem_appPath = address.split("/")[1];
        URL remoteUrl;
        try {
            remoteUrl = new URL(protocol, rem_address, rem_port, rem_appPath);
        } catch (MalformedURLException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }
        String localUri = getLocalUri().toString();
        Client c = ClientBuilder.newClient();
        Response rsp;
        try {
            rsp = c.target(remoteUrl.toURI()).request().put(Entity.xml(localUri));
        } catch (URISyntaxException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }
        if (rsp != null) {
            // jaxb unmarshal
            // parse returned XML for servers to add to local networkMembers EXCEPT address and local_address
            return Response
                    .ok()
                    .build();
        }
        return Response
                .status(Response.Status.BAD_REQUEST)
                .build();
    }
    
    private URL getLocalUri() {
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
    * this method is only for automated access from a
    * remote distributedGradeBook instance to join the network. 
    */
    @PUT
    @Path("peer/{address}")
    public Response addServerToNetwork(@PathParam("address") String address) {
        Server newServer = new Server();
        try {
            newServer.setUrl(new URL(address));
        } catch (MalformedURLException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }
        networkMembers.add(newServer);
        return Response
                // TODO send an XML list of this server's peers
                .ok()
                .build();
    }
}
