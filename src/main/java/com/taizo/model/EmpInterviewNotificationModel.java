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

import lombok.ToString;

@Entity
@Table(name = "emp_interview_notifications")
@ToString
public class EmpInterviewNotificationModel {
	
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

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getCanId() {
			return canId;
		}

		public void setCanId(int canId) {
			this.canId = canId;
		}

		public int getEmpId() {
			return empId;
		}

		public void setEmpId(int empId) {
			this.empId = empId;
		}

		public int getInterviewId() {
			return interviewId;
		}

		public void setInterviewId(int interviewId) {
			this.interviewId = interviewId;
		}

		public String getNotes() {
			return notes;
		}

		public void setNotes(String notes) {
			this.notes = notes;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public Date getDateTime() {
			return dateTime;
		}

		public void setDateTime(Date dateTime) {
			this.dateTime = dateTime;
		}

		public String getScheduledDate() {
			return scheduledDate;
		}

		public void setScheduledDate(String scheduledDate) {
			this.scheduledDate = scheduledDate;
		}

		public String getScheduledTime() {
			return scheduledTime;
		}

		public void setScheduledTime(String scheduledTime) {
			this.scheduledTime = scheduledTime;
		}
	    
	    
}
