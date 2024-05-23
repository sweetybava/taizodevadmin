package com.taizo.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "can_interview_notifications")
@ToString
@Getter
@Setter
public class CanInterviewNotificationModel {
	
	@Id
	  @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id",unique = true, nullable = false)
	    private int id;
	
	   @Column(name="can_id")
	   private int canId;
	   
	   @Column(name="emp_id")
	   private int empId;
	   
	   @Column(name="interview_id")
	   private int interviewId;
	   
		 @Column(name="scheduled_date")
		 private String scheduledDate;
		 
		 @Column(name="scheduled_time")
		 private String scheduledTime;

	   @Column(name="notes")
	   private String notes;
	   
	   @Column(name="status")
	   private String status;
	   
	    @Temporal(TemporalType.TIMESTAMP)
	    @Column(name = "date_time", insertable=false)
	    private Date dateTime;
	    
	    
}
