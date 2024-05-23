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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "candidate_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateAnalyticsModel {

	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(name = "admin_id")
	    private Long adminId;

	    @Column(name = "fb_meta_leads")
	    @ColumnDefault("0")
	    private Integer fbMetaLeads;

	    @Column(name = "qualified_fb_meta_leads")
	    @ColumnDefault("0")
	    private Integer qualifiedFbMetaLeads;

	    @Column(name = "can_leads")
	    @ColumnDefault("0")
	    private Integer canLeads;

	    @Column(name = "qualified_can_leads")
	    @ColumnDefault("0")
	    private Integer qualifiedCanLeads;

	    @Column(name = "Total_leads")
	    @ColumnDefault("0")
	    private Integer totalLeads;

	    @Column(name = "Total_qualified_leads")
	    @ColumnDefault("0")
	    private Integer totalQualifiedLeads;

	    @Column(name = "can_registration")
	    @ColumnDefault("0")
	    private Integer canRegistration;

	    @Column(name = "interview_scheduled")
	    @ColumnDefault("0")
	    private Integer interviewScheduled;

	    @Column(name = "interview_attended")
	    @ColumnDefault("0")
	    private Integer interviewAttended;

	    @Column(name = "company_selected")
	    @ColumnDefault("0")
	    private Integer companySelected;

	    @Column(name = "offer_accepted")
	    @ColumnDefault("0")
	    private Integer offerAccepted;

	    @Column(name = "joined")
	    @ColumnDefault("0")
	    private Integer joined;

	    @CreationTimestamp
	    @Column(name = "created_on", updatable = false)
	    protected Timestamp createdOn;

	    @CreationTimestamp
	    @Column(name = "created_time", updatable = false)
	    protected Date createdTime;

	  
}
