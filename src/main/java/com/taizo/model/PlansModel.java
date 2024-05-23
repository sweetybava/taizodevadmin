package com.taizo.model;

import java.sql.Timestamp;
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
@Table(name = "plans")
@Getter
@Setter
@ToString
public class PlansModel {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="id")
	    private int id;
	 
	 @Column(name="plan_name")
	 private String planName;	 
	 
	 @Column(name="job_posting")
	 private int jobPosting;
	 
	 @Column(name="active_jobs")
	 private int activeJobs; 
	 
	 @Column(name="profiles")
	 private int profiles; 
	 
	 @Column(name="amount")
	 private int amount;
	 
	 @Column(name="discount_amount")
	 private int discountAmount;
	 
	 @Column(name="plan_expiry_days")
	 private int planValidity ;	
	 
	 @Column(name="job_post_validity")
	 private int jobPostValidity ;	
	 
	 @Column(name="active")
	 private boolean active ;	
	 
	 @Column(name="no_of_openings")
	 private int noOfOpenings;
	 
	 @Column(name="is_experienced")
	 private boolean isExperienced;

	 @CreationTimestamp
	 @ColumnDefault("created_time")
	 protected Date createdTime;
	

	}
