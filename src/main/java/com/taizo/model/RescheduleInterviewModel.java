package com.taizo.model;

import java.time.LocalDateTime;
import java.util.Date;

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
@Table(name = "interview_reschedule")
@ToString
@Getter
@Setter
public class RescheduleInterviewModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	 @Column(name="interview_id")
	 private int interviewId;
	 
	 @Column(name="rescheduled_on")
	 private String reScheduledOn;
	 
	 @Column(name="start_tm")
	 private String startTime;
	 
	 @Column(name="end_tm")
	 private String endTime;
	 
	 @Column(name="rescheduled_dt")
	 private Date reScheduledDate;
		@Column(name = "accepted_dt")
		private Date acceptedDate;
	 
	 @Column(name="status")
	 private String status;

	 
}
