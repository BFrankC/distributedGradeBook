/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ben
 */
//Making the class for Marshaling/unmarshaling
//@XmlRootElement
public class IdName {
    private String title;
    private String uuid;
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setName(String uuid) {        
        this.title = uuid;
    }
    
    @XmlElement(name = "title")
    public String gettitle() {
        return this.title;
    }
    
    @XmlElement(name = "uuid")
    public String getuuid() {
        return this.uuid;
    }
    public IdName(String uuid,String title)
    {
        this.title = title;
        this.uuid = uuid;
    }
}
