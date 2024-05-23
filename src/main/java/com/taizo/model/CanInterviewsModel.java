package com.taizo.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "can_interviews")
@Getter
@Setter
@ToString
public class CanInterviewsModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "admin_id")
	private int adminId;

	@Column(name = "job_id")
	private int jobId;

	@Column(name = "can_id")
	private int canId;
	
	@Column(name = "interview_current_status")
	private int interviewCurrentStatus;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "candidate_percentage")
	private String candidatePercentage;
	
	@Column(name = "company_name")
	private String companyName;

	@Column(name = "contact_person_name")
	private String contactPersonName;

	@Column(name = "contact_number")
	private long contactNumber;
	
	@Column(name = "city")
	private String city;
	
	@Column(name = "area")
	private String area;

	@Column(name = "interview_date")
	private String interviewDate;
	
	@Transient
	private String interviewEndDate;
	
	@Column(name = "rescheduled_date")
	private String rescheduledDate;

	@Column(name = "interview_time")
	private String interviewTime;
	
	@Column(name = "documents")
	private String documents;

	@Column(name = "interview_scheduled_dt")
	private String interviewScheduledDt;

	@Column(name = "is_rescheduled")
	private boolean isRescheduled;
	
	@Column(name = "rescheduled_dt")
	private Date rescheduledDateTime;
	
	@Column(name = "active")
	private boolean active;
	
	@Column(name = "is_attended")
	private boolean isAttended;
	
	@Column(name = "is_selected")
	private boolean isSelected;
	
	@Column(name = "is_joined")
	private boolean isJoined;
	
	@Column(name = "not_selected_on")
	private String notSelectedOn;
	
	@Column(name = "joined_on")
	private String joinedOn;
	
	@Column(name = "selected_on")
	private String selectedOn;
	
	@Column(name = "attended_on")
	private LocalDateTime  attendedOn;
	
	@Column(name = "not_attended_on")
	private String notAttendedOn;
	

	@Column(name = "offer_rejected_on")
	private String offerRejectedOn;
	
	@Column(name = "left_the_company_on")
	private String leftTheCompanyOn;
	
	@Column(name = "is_not_selected")
	private boolean isNotSelected;
	
	@Column(name = "is_left_the_company")
	private boolean isLeftTheCompany;
	
	@Column(name = "is_offer_rejected")
	private boolean isOfferRejected;
	
	@Column(name = "is_not_attended")
	private boolean isNotAttended;
	
	@CreationTimestamp
	 @ColumnDefault("created_time")
	 protected Date createdTime;
	
	 @Column(name = "left_the_company_at")
	 private String leftTheCompanyAt;

	@Transient
	private Date endDate;
	
	@Transient
	private long candidateMobileNumber;
	
	@Transient
	private String jobCategory;
	
	@Transient
	private String scheduledBy;
	
	@Transient
	private int page;
	
	@Transient
	private int size;

	@ManyToOne
	@JoinColumn(name = "admin_id", insertable = false, updatable = false)
	private Admin admin;
	
	 @Transient
	 private LocalDateTime interviewDateNew;


}
