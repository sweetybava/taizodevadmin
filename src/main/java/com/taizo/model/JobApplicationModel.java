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
import org.hibernate.annotations.UpdateTimestamp;

import lombok.ToString;

@Entity
@Table(name = "job_application")
@ToString
public class JobApplicationModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	 @Column(name="job_id")
	    private int jobId;	
	 
	    @Column(name="candidate_id")
	    private int candidateId;	 
	    
	    @Column(name="employer_id")
	    private int employerId;	
	    
	    @Column(name="status")
	    private String status;
	    
	    @Column(name="message")
	    private String message;
	    
	    @Column(name="user_message")
	    private String userMessage;
	    
	    @Column(name="job_video")
	    private String jobVideo;
	    
	    @Column(name="applied_time")
	    private Date appliedTime;
	    
	    @CreationTimestamp
	    @ColumnDefault("created_time")
	    protected Date createdTime;
	    
	    @UpdateTimestamp
	    @ColumnDefault("updated_time")
	    protected Date updatedTime;

		public JobApplicationModel(int id, int jobId, int candidateId, String status) {
			super();
			this.id = id;
			this.jobId = jobId;
			this.candidateId = candidateId;
			this.status = status;
		}

		public JobApplicationModel() {
			// TODO Auto-generated constructor stub
		}

		public JobApplicationModel(ArrayList<JobApplicationModel> details1) {
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

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getUserMessage() {
			return userMessage;
		}

		public void setUserMessage(String userMessage) {
			this.userMessage = userMessage;
		}

		public Date getCreatedTime() {
			return createdTime;
		}

		public void setCreatedTime(Date createdTime) {
			this.createdTime = createdTime;
		}

		public Date getUpdatedTime() {
			return updatedTime;
		}

		public void setUpdatedTime(Date updatedTime) {
			this.updatedTime = updatedTime;
		}

		public String getJobVideo() {
			return jobVideo;
		}

		public void setJobVideo(String jobVideo) {
			this.jobVideo = jobVideo;
		}

		public Date getAppliedTime() {
			return appliedTime;
		}

		public void setAppliedTime(Date appliedTime) {
			this.appliedTime = appliedTime;
		}



		
	    

}
