package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cfg_iti_courses")
@Getter
@Setter
@ToString
public class CfgItiCourseModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="courses")
	    private String courses;
	    
	    @Column(name="js_active")
	    private boolean jsActive;
	    
	    @Column(name="emp_active")
	    private boolean empActive;	      

}
