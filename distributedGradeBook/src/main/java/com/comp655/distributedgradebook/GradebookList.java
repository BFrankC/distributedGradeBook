/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ben
 */
@XmlRootElement()
public class GradebookList {
    private ArrayList<IdName> primary;
    private ArrayList<IdName> secondary;
    
    @XmlElement(name = "Primary gradebooks")
    public List getPrimaryList() {
        return primary;
    }
    public ArrayList<IdName> getPrimary()
    {
        return primary;
    }
    public ArrayList<IdName> getSecondary()
    {
        return secondary;
    }
    
    @XmlElement(name = "Secondary gradebooks")
    public List getSecondaryList() {
        return secondary;
    }
    public GradebookList()
    {
        primary = new ArrayList<>();
        secondary = new ArrayList<>();
    }
    
}






