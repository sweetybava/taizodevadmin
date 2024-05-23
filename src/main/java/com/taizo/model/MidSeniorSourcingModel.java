package com.taizo.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mid_senior_sourcing")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MidSeniorSourcingModel {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "last_name",nullable = false)
    private String lastName;
    
    @Column(name = "email_id",nullable = false)
    private String emailId;

    @Column(name = "mobile_number",nullable = false)
    private String mobileNumber;

    @Column(name = "applied_jobrole",nullable = false)
    private String appliedJobrole;

    @Column(name = "jobrole",nullable = false)
    private String jobrole;

    @Column(name = "experience_in_years",nullable = false)
    private int experienceInYears;

    @Column(name = "experience_in_months",nullable = false)
    private int experienceInMonths;

    @Column(name = "skills",nullable = false)
    private String skills;

    @Column(name = "current_location",nullable = false)
    private String currentLocation;

    @Column(name = "preferred_job_location",nullable = false)
    private String preferredJobLocation;

    @Column(name = "resume_link",nullable = false)
    private String resumeLink;

    @Column(name = "admin_preferred_company",nullable = false)
    private String adminPreferredCompany;

    @Column(name = "link_sent")
    private boolean linkSent;

    @Column(name = "notes",nullable = false)
    private String notes;

    @Column(name = "qualified")
    private boolean qualified;

    @Column(name = "not_qualified")
    private boolean notQualified;
    
    @CreationTimestamp
    @ColumnDefault("created_time")
    protected Date createdTime;
    
    @CreationTimestamp
    @ColumnDefault("created_on")
    protected Date createdOn;

}
