/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * @author ben
 */
@XmlRootElement
public class Gradebook {

    private UUID id = UUID.randomUUID();
    private String title;
    private ConcurrentHashMap<String, Student> studentMap;
    private URL secondaryURL = null;
    
    public Gradebook (String title)
    {
        this.title = title;
        this.studentMap = new ConcurrentHashMap<>();
    }
    
    public Gradebook ()
    {
        this.title = "default";
        this.studentMap = new ConcurrentHashMap<>();
    }
    
    public void setID(UUID id) {
        this.id = id;
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
    
    public String getStudents()
    {
        return studentMap.keySet().toString();
    }
    
    @XmlElement(name = "students")
    public List<Student> getStudentList() {
        List<Student> students = new ArrayList<>();
        for (String key : studentMap.keySet()) {
            students.add(studentMap.get(key));
        }
        return students;
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
    
    public Student getStudent(String studentName) {
        return this.studentMap.get(studentName);
    }
    
    public String getStudentGrade(String studentName) {
        if (studentMap.containsKey(studentName)) {
            return studentMap.get(studentName).getGrade();
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
