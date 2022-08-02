/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "server-list")
@XmlAccessorType (XmlAccessType.FIELD)
public class ServerList {
    
    @XmlElement(name = "server")
    private List<Server> servers = null;
    
    public List<Server> getServers() {
        return servers;
    }
    
    public void setServers(List<Server> servers) {
        this.servers = servers;
    }
}
