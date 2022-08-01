/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.net.URL;
import java.util.UUID;

/**
 *
 * @author berrm
 */
@XmlRootElement
public class Server {
    private URL url;
    private final UUID id = UUID.randomUUID();
    
    public Server () {
        
    }
    
    @XmlElement(name = "url")
    public URL getUrl() {
        return this.url;
    }
    
    public void setUrl(URL url) {
        this.url = url;
    }
    
    @XmlElement(name = "uuid")
    public UUID getId() {
        return this.id;
    }

}
