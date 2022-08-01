/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;


/**
 *
 * @author ben
 */
@XmlRootElement
public class Gradebook {

    private final UUID id = UUID.randomUUID();
    private String title;
    private ConcurrentHashMap<String, String> students;
    private ConcurrentHashMap<String, Student> studentMap;
    private URL secondaryURL = null;
    
    public Gradebook (String title)
    {
        this.title = title;
        this.students = new ConcurrentHashMap<>();
        this.studentMap = new ConcurrentHashMap<>();
    }
    
    public Gradebook ()
    {
        this.title = "default";
        this.students = new ConcurrentHashMap<>();
        this.studentMap = new ConcurrentHashMap<>();
    }

    @XmlElement(name = "id")
    public UUID getID()
    {
        return this.id;
    }
    
    @XmlElement(name = "title")
    public String getTitle()
    {
        return this.title;
    }
    
    @XmlElement(name = "students")
    public String getStudents()
    {
        return studentMap.keySet().toString();
    }
    
    public void setTitle(String newTitle)
    {
        this.title = newTitle;
    }
   

    public void addOrUpdateStudent(String name, String grade) {
        Student s = studentMap.get(name);
        if (s != null) {
            s.setGrade(grade);
        } else {
            Student nS = new Student();
            nS.setGrade(grade);
            nS.setName(name);
            studentMap.put(name, nS);
        }
    }
    
    public String getStudentGrade(String studentName) {
        if (studentMap.containsKey(studentName)) {
            return studentMap.get(studentName).getName();
        }
        return "";
    }
    
    public void deleteStudent(String name) {
        if (studentMap.containsKey(name)) {
            studentMap.remove(name);
        }
    }
    
    public void setSecondaryUrl(URL url) {
        this.secondaryURL = url;
    }
    
    public URL getSecondaryUrl() {
        return this.secondaryURL;
    }

}
