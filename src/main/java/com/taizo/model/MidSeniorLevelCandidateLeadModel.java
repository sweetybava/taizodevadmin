package com.taizo.model;

import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "mid_senior_level_candidate_lead")
@Getter
@Setter
@ToString
public class MidSeniorLevelCandidateLeadModel {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
	 
	 @Column(name="first_name")
	    private String firstName;
	 
	 @Column(name="last_name")
	    private String lastName;
	 
	 
	 @Column(name = "mobile_number")
	    private String mobileNumber;
	 
	  @Column(name = "whatsapp_number")
	    private String whatsappNumber;
	  
	  @Column(name = "educational_qualification")
	    private String educationalQualification;
	  
	  @Column(name = "pref_job_location")
	    private String prefJobLocation;

	  @Column(name = "exp_in_manufacturing")
	    private boolean expInManufacturing;
	  
	  @Column(name = "admin_id")
	    private Long adminId;
	  
	  @Column(name = "exp_in_years")
	    private int expInYears;
	  
	  @Column(name = "exp_in_months")
	    private int expInMonths;
	  
	  @Column(name = "job_category")
	    private String jobCategory;
	  
	  @Column(name = "is_currently_working")
	    private boolean isCurrentlyWorking;
	  
	  @Column(name = "joining_date")
	    private String joiningDate;
	  
	  @Column(name = "resume_link")
	    private String resumeLink;
	  
	  @Column(name = "linkedin_url")
	    private String linkedinUrl;
	  
	  @Column(name = "notice_period")
	    private String noticePeriod;
	  
	  @Column(name = "expected_salary")
	    private int expectedSalary;
	  
	  @Column(name = "email_id")
	    private String emailId;
	  
	  @Column(name = "status")
	    private int status;
	  
	  @Column(name = "current_salary")
	    private int currentSalary;
	  
	  @Column(name = "profile_page_no")
	    private int profilePageNo;
	  
	  @Column(name = "is_registered")
	    private boolean isRegistered;
	  
	    @CreationTimestamp
	    @ColumnDefault("registered_time")
	    protected Date registeredTime;	
	  
	    @CreationTimestamp
	    @ColumnDefault("created_time")
	    protected Date createdTime;
	  
	    @Column(name="screening_at")
	    private String screeningAt;
	  
	    @Column(name="shortlisted_at")
	    private String shortlistedAt;

  	    @CreationTimestamp
	    @ColumnDefault("updated_time")
	    protected Date updatedTime;
	  
	    @Column(name = "qualified")
	    private boolean qualified;

	    @Column(name = "not_qualified")
	    private boolean notQualified;
	    
	    @Column(name = "is_report")
	    private boolean isReport;
	    
	    @Column(name="screening_date")
	    private String screeningDate;
	    
	    @Column(name="screening_time")
	    private String screeningTime;
	    
	    @Column(name="meeting_link")
	    private String meetingLink;

	@Column(name = "applied_jobrole",nullable = false)
	private String appliedJobrole;

	@Column(name = "jobrole",nullable = false)
	private String jobrole;

	@Column(name = "skills",nullable = false)
	private String skills;

	@Column(name = "current_location",nullable = false)
	private String currentLocation;

	@Column(name = "admin_preferred_company",nullable = false)
	private String adminPreferredCompany;
	  
	  
	   
}
