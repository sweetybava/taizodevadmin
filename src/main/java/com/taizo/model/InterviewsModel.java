package com.taizo.model;

import java.io.Serializable;
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
@Table(name = "interviews")
@ToString

@Getter

@Setter

public class InterviewsModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "emp_id")
	private int empId;

	@Column(name = "job_id")
	private int jobId;

	@Column(name = "can_id")
	private int canId;

	@Column(name = "address_id")
	private int addressId;

	@Column(name = "contact_person_name")
	private String contactPersonName;

	@Column(name = "mobile_number")
	private long mobileNumber;

	@Column(name = "scheduled_on")
	private String scheduled_on;

	@Column(name = "start_tm")
	private String startTime;

	@Column(name = "end_tm")
	private String endTime;

	@Column(name = "documents")
	private String documents;

	@Column(name = "is_rescheduled")
	private boolean isRescheduled;

	@Column(name = "scheduled_dt")
	private Date scheduledDate;
	@Column(name = "accepted_dt")
	private Date acceptedDate;

	@Column(name = "active")
	private boolean active;

	@Column(name = "status")
	private String status;
	
    @Column(name = "from_web")
    private Boolean fromWeb;

}
