package com.taizo.model;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.ToString;

@Entity
@Table(name = "employer_application")
@ToString
public class EmployerApplication {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	    @Column(name="employer_id")
	    private int employerId;	
	 
	 @Column(name="job_id")
	    private int jobId;	
	 
	    @Column(name="candidate_id")
	    private int candidateId;	  
	    
	    @Column(name="status")
	    private String status;
	    
	    @CreationTimestamp
	    @ColumnDefault("created_time")
	    protected Date createdTime;


		public EmployerApplication() {
			// TODO Auto-generated constructor stub
		}
		
		

		public EmployerApplication(int id, int employerId, int jobId, int candidateId, String status) {
			super();
			this.id = id;
			this.employerId = employerId;
			this.jobId = jobId;
			this.candidateId = candidateId;
			this.status = status;
		}



		public EmployerApplication(ArrayList<EmployerApplication> details1) {
			// TODO Auto-generated constructor stub
		}

		public int getId() {
			return id;
		}

		public int getJobId() {
			return jobId;
		}

		public int getCandidateId() {
			return candidateId;
		}

		public String getStatus() {
			return status;
		}

		public void setId(int id) {
			this.id = id;
		}

		public void setJobId(int jobId) {
			this.jobId = jobId;
		}

		public void setCandidateId(int candidateId) {
			this.candidateId = candidateId;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public int getEmployerId() {
			return employerId;
		}

		public void setEmployerId(int employerId) {
			this.employerId = employerId;
		}



		public Date getCreatedTime() {
			return createdTime;
		}



		public void setCreatedTime(Date createdTime) {
			this.createdTime = createdTime;
		}

		
	    

}
