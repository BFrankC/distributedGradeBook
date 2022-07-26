/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
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
    
    public Gradebook (String title)
    {
        this.title = title;
        this.students = new ConcurrentHashMap<>();
    }
    
    public Gradebook ()
    {
        this.title = "default";
        this.students = new ConcurrentHashMap<>();
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
        return students.keySet().toString();
    }
    
    public void setTitle(String newTitle)
    {
        this.title = newTitle;
    }
   

    public void addStudent(String name, String grade) {
        students.put(name, grade);
    }
    
    public String getStudentGrade(String studentName) {
        if (students.containsKey(studentName)) {
            return students.get(studentName);
        }
        return "";
    }
    
    public void deleteStudent(String name) {
        if (students.containsKey(name)) {
            students.remove(name);
        }
    }

}
