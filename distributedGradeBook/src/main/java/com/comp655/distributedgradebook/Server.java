/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook;

import java.net.URL;
import java.util.UUID;

/**
 *
 * @author berrm
 */
public class Server {
    private URL url;
    private final UUID id = UUID.randomUUID();
    
    public Server () {
        
    }
    
    public URL getUrl() {
        return this.url;
    }
    
    public void setUrl(URL url) {
        this.url = url;
    }
    
    public UUID getId() {
        return this.id;
    }

}
