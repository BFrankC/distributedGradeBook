/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Matt
 */
@XmlRootElement
public class GradebookMap<E, V> extends ConcurrentHashMap<E, V> {
    
    @XmlElement(name = "gradebook")
    public List getGradebookList() {
        return new ArrayList(this.values());
    }
}