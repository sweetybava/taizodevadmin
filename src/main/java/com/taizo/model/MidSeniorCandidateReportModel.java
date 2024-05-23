package com.taizo.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "mid_senior_candidate_report")
@Getter
@Setter
@ToString
public class MidSeniorCandidateReportModel {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
	
	 @Column(name="candidate_name")
	    private String candidateName;
	 
	 @Column(name="titles")
	    private String titles;
	 
	 @Column(name="age")
	    private int age;
	 
	 @Column(name="looking_for")
	    private String lookingFor;
	 
	 @Column(name="years_of_experience")
	    private int yearsOfExperience;
	 
	 @Column(name="mid_senior_id")
	    private long midSeniorId;
	 
	 @Column(name="previous_designation")
	    private String previousDesignation;
	 
	 @Column(name="qualification")
	    private String qualification;
	 
	 @Column(name="core_skill_set_matching_jd")
	    private String coreSkillSetMatchingJd;
	 
	 @Column(name="skills")
	    private String skills;
	 
	 @Column(name="certifications")
	    private String certifications;
	 
	 @Column(name="taizo_suggestion")
	    private String taizoSuggestion;
	 
	 @Column(name="taizo_score")
	    private String taizoScore;
	 
	 @Column(name="is_report")
	 private boolean isReport;
	 
	 @CreationTimestamp
	    @ColumnDefault("created_time")
	    protected Date createdTime;
	  
	  @CreationTimestamp
	    @ColumnDefault("updated_time")
	    protected Date updatedTime;
	  
	  @Transient
	  private int page;
	  
	  @Transient
	  private int size;
}
