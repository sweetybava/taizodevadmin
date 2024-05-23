package com.taizo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "facebook_meta_leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookMetaLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_name")
    private String candidateName;

    @Column(name = "education_qualification")
    private String educationQualification;

    @Column(name = "job_category")
    private String jobCategory;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Column(name = "experience")
    private String experience;

    @Column(name = "preferred_location")
    private String preferredLocation;
    
    @Column(name = "area")
    private String area;

    @Column(name = "candidate_preferred_location")
    private String candidatePreferredLocation;

    @Column(name = "resource_platform")
    private String resourcePlatform;

    @Column(name = "form_name")
    private String formName;

    @Column(name = "form_id")
    private String formId;


    @Column(name = "qualified")
    private boolean qualified;

    @Column(name = "not_qualified")
    private boolean notQualified;
    
    @Column(name = "inactive")
    private boolean inActive;
    
    @Column(name = "is_not_attend")
    private boolean isNotAttend;

    @Column(name = "assign_to")
    private int assignTo;

    @Column(name = "is_currently_working")
    private boolean isCurrentlyWorking;

    @Column(name = "joining_availability")
    private String joiningAvailability;

    @Column(name = "industry")
    private String industry;
    
    @Column(name = "is_canLead")
    private Boolean isCanLead;
    
    @Column(name = "is_candidate")
    private Boolean isCandidate;
    
    @Column(name = "candidate_id")
    private Integer candidateId;
    
    @Column(name = "canLead_id")
    private Integer canLeadId;

    @CreationTimestamp
    @ColumnDefault("created_time")
    protected Date createdTime;
    
    @Transient
    private Date endDate;
    
    @Transient
    private boolean noStatus;
    
    @Transient
    private int pages;
    
    @Transient
    private int size;
    
    @Column(name = "is_seen")
    private boolean isSeen;
    
    
    
}
