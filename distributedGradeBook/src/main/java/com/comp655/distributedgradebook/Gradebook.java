/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.comp655.distributedgradebook;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


/**
 *
 * @author ben
 */
@XmlRootElement(name = "gradebook")
public class Gradebook {

    private UUID id = UUID.randomUUID();
    private String title;
    @XmlElement (name = "student")
    private ArrayList<Student> studentList;
    private HashMap<String, Student> studentMap;
    private URL secondaryURL = null;
    
    public Gradebook (String title)
    {
        this.title = title;
        this.studentList = new ArrayList<>();
        this.studentMap = new HashMap<>();
    }
    
    public Gradebook ()
    {
        this.title = "default";
        this.studentList = new ArrayList<>();
        this.studentMap = new HashMap<>();
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
    
    public HashMap<String, Student> getStudents()
    {
        return studentMap;
    }
    
    public void setTitle(String newTitle)
    {
        this.title = newTitle;
    }
   

    public void addOrUpdateStudent(String name, String grade) {
        if (studentMap.containsKey(name)) {
            studentMap.get(name).setGrade(grade);
        } else {
            Student newStudent = new Student();
            newStudent.setGrade(grade);
            newStudent.setName(name);
            studentMap.put(name, newStudent);
        }
        for (Student s : studentList) {
            if (s.getName().equals(name)) {
                s.setGrade(grade);
                return;
            }
        }
        // if didn't return already, name was not in studentList
        Student newStu = new Student();
        newStu.setGrade(grade);
        newStu.setName(name);
        studentList.add(newStu);
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
        studentMap.remove(name);
        for (Student s : studentList) {
            if (s.getName().equals(name)) {
                studentList.remove(s);
                break;
            }
        }
    }
    
    public void setSecondaryUrl(URL url) {
        this.secondaryURL = url;
    }
    
    public URL getSecondaryUrl() {
        return this.secondaryURL;
    }

}
