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
@Table(name = "employer_feedback")
@Getter
@Setter
@ToString
public class EmployerFeedbackModel {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="emp_id")
	    private int empId;
	
	    @Column(name="message")
	    private String message;
	    
	    @Column(name="module")
	    private String module;

}
