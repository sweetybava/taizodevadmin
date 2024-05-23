package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@Entity
@Table(name = "notifications")
@ToString
public class NotificationModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	  @Column(name="employer_id")
	    private int employerId;
	  
	  @Column(name="candidate_id")
	    private int candidateId;
	
	  @Column(name="message")
	    private String message;

	public NotificationModel(int id, String message) {
		super();
		this.id = id;
		this.message = message;
	}
	
	

	public NotificationModel() {
		super();
		// TODO Auto-generated constructor stub
	}



	public int getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}



	public void setId(int id) {
		this.id = id;
	}

	public void setMessage(String message) {
		this.message = message;
	}



	public int getEmployerId() {
		return employerId;
	}



	public int getCandidateId() {
		return candidateId;
	}


	public void setEmployerId(int employerId) {
		this.employerId = employerId;
	}



	public void setCandidateId(int candidateId) {
		this.candidateId = candidateId;
	}



	  
	  
	

}
