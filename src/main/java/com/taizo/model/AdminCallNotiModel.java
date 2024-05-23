package com.taizo.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "admin_call_notifications")
@Getter
@Setter
@ToString
public class AdminCallNotiModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private long id;

	@Column(name = "event_name")
	private String eventName;
	
	@Column(name = "admin_id")
	private int adminId;

	@Column(name = "type")
	private String type;

	@Column(name = "event_type")
	private String eventType;
	
	@Column(name = "source")
	private String source;

	@Column(name = "contact_person_name")
	private String contactPersonName;

	@Column(name = "company_name")
	private String companyName;
	
	@Column(name = "mobile_number")
	private String mobileNumber;

	@Column(name = "id_type")
	private String idType;

	@Column(name = "location")
	private String location;
	
	@Column(name = "position")
	private String position;

	@Column(name = "job_role")
	private String jobRole;

	@Column(name = "exp")
	private String exp;
	
	@Column(name = "job_status")
	private String jobStatus;

	@Column(name = "candidate_name")
	private String candidateName;

	@Column(name = "interview_date")
	private String interviewDate;

	@Column(name = "reference_id")
	private int referenceId;

	@Column(name = "sid")
	private String sid;
	
	@Column(name = "notification_read")
	private boolean notificationRead;
		
	@Column(name = "call_time")
	private Date callTime;
	
	  @PrePersist
	  protected void onCreate() {
		  callTime = new Date();
	  }

	public void addAttribute(String string, String responseData) {
		// TODO Auto-generated method stub
		
	}
}
