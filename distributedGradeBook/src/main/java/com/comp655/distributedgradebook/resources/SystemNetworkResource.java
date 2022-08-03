/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook.resources;

import com.comp655.distributedgradebook.Server;
import com.comp655.distributedgradebook.ServerList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
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
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;

@Path("/admin")
@ApplicationScoped
public class SystemNetworkResource {
    private final String protocol = "http";
    String localIPv4Address = "";
    // TODO add RESTful getters and setters for the following two Strings
    private int port = 8080;
    private final String appPath = "/distributedGradeBook";
    
    private ArrayList<Server> networkMembers = new ArrayList<Server>();
    
    private final Server thisServer = new Server();
    
    public SystemNetworkResource() {
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
        URL remoteUrl = buildUrl(protocol, rem_address, rem_port, appPath);
        URL localUrl = thisServer.getUrl();
        String remoteUri = remoteUrl.toString() + "/admin/peer/host/" + localIPv4Address + "/port/" + port;
        Client cli = ClientBuilder.newClient();
        Response rsp;
        //rsp = cli.target("http://192.168.0.200:8080/distributedGradeBook/admin/peer/host/192.168.0.115/port/8080/").request().put(Entity.text(localUrl.toString()));
        rsp = cli.target(remoteUri).request().put(Entity.text(localUrl.toString()));
        if (rsp != null) {
            if (rsp.getStatus() == Response.Status.BAD_REQUEST.ordinal()) {
                return rsp;     //Bad request from remote
            }
            // happy path:
            // jaxb unmarshal
            JAXBContext c = JAXBContext.newInstance(ServerList.class);
            Unmarshaller u = c.createUnmarshaller();
            StringBuffer xmlString = new StringBuffer(rsp.readEntity(String.class));
            ServerList peers = (ServerList) u.unmarshal(new StreamSource(new StringReader(xmlString.toString())));
            for (Server newServer : peers.getServers()) {
                boolean match = false;
                for (Server knownServer : networkMembers) {
                    if (ServerList.equals(newServer, knownServer)) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    networkMembers.add(newServer);
                }
            }
            return Response
                    .ok()
                    .build();
        }
        return Response
                .status(Response.Status.BAD_REQUEST)
                .build();
    }
    
    @PUT
    @Path("peer/set_local_address/{host}/port/{port}")
    public Response setLocalIPv4Address(@PathParam("host") String host, @PathParam("port") int port) {
        this.localIPv4Address = host;
        this.thisServer.setUrl(buildUrl(protocol, this.localIPv4Address, port, appPath));
        return Response
                .ok()
                .build();
    }
    
    /*
    * builds a URI for this server instance, either using a pre-configured
    * host address, or finding a public IP address if no pre-configured address
    * exists.
    */
    public URL getLocalUrl() {
        if (!"".equals(localIPv4Address)) {
            return buildUrl(protocol, localIPv4Address, port, appPath);
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
        
        return buildUrl(protocol, localIPv4Address, port, appPath);
    }
    
    /*
    * Formats a URI using supplied parameters
    */
    private URL buildUrl(String appProtocol, String host_address, int portNum, String appURI) {
        URL localUri = null;
        try {
            localUri = new URL(appProtocol, host_address, portNum, appURI);
        } catch (MalformedURLException e) {
            //TODO log something here
            // I know this is lazy
        }
        // localUri.toString looks like http://74.139.104.38:8080/distributedGradeBook
        return localUri;
    }
        
    /*
    * this method adds a new instance of distributedGradeBook to this server's
    * list of network peers, and returns an XML list of all known network
    * peers including this instance.
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
                // just return the list without adding duplicate
                return Response
                        .ok(getNetworkPeers())
                        .build();
            }
        }
        networkMembers.add(newServer);
        
        // build and return list
        return Response
                .ok(getNetworkPeers())
                .build();
    }
    
    @GET
    @Path("peer")
    @Produces("application/xml")
    public StreamingOutput getNetworkPeers() throws JAXBException, FileNotFoundException {
        ServerList peers = new ServerList();
        peers.setServers(networkMembers);
        JAXBContext c = JAXBContext.newInstance( ServerList.class );
        Marshaller m = c.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        return (OutputStream outputStream) -> {
            try {
                m.marshal(peers, outputStream);
            } catch (JAXBException ex) {
                Logger.getLogger(SystemNetworkResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
    }
}
