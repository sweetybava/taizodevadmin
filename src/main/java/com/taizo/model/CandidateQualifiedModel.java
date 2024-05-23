package com.taizo.model;

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
import lombok.ToString;

@Entity
@Table(name = "candidate_qualify_form")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CandidateQualifiedModel {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
	

	 @CreationTimestamp
	    @ColumnDefault("created_time")
	    protected Date createdTime;

    @Column(name = "applied_jobrole")
    private String appliedJobrole;
    
    @ColumnDefault("admin_id")
    private int adminId;
    
    @Column(name = "fb_metaLead_id")
    private long fbMetaLeadId;
    
    @Column(name = "can_Lead_id")
    private long canLeadId;
    
    @Column(name = "candidate_id")
    private long candidateId;

    @Column(name = "is_experienced")
    private boolean isExperienced;

    @Column(name = "is_currently_working")
    private boolean isCurrentlyWorking;

    @Column(name = "preferred_job_location")
    private String preferredJobLocation;

    @Column(name = "having_job_location")
    private boolean havingJobLocation;

    @Column(name = "can_suitable_job_location")
    private String canSuitableJobLocation;

    @Column(name = "education")
    private String education;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "is_mechanical_related_degree")
    private Boolean isMechanicalRelatedDegree;

    @Column(name = "is_course_completed")
    private Boolean isCourseCompleted;

    @Column(name = "skills_certifications")
    private String skillsCertifications;

    @Column(name = "current_candidate_location")
    private String currentCandidateLocation;

    @Column(name = "current_stay_type")
    private String currentStayType;

    @Column(name = "is_ready_to_relocate")
    private boolean isReadyToRelocate;

    @Column(name = "expected_salary")
    private Long expectedSalary;

    @Column(name = "salary_expectation_admin_preference")
    private String salaryExpectationAdminPreference;

    @Column(name = "is_work_for_suggested_salary")
    private boolean isWorkForSuggestedSalary;
    
    @Column(name = "is_ready_for_shifts")
    private boolean isReadyForShifts;

    @Column(name = "is_need_accommodation")
    private boolean isNeedAccommodation;

    @Column(name = "is_need_transport")
    private boolean isNeedTransport;
    
    @Column(name = "is_immediate_joiner")
    private boolean isImmediateJoiner;

    @Column(name = "is_having_updated_cv")
    private boolean isHavingUpdatedCV;
    
    @Column(name = "jobrole")
    private String jobrole;

    @Column(name = "industry")
    private String industry;

    @Column(name = "experience_in_month")
    private String experienceInMonth;

    @Column(name = "experience_in_year")
    private String experienceInYear;

    @Column(name = "overall_experience")
    private boolean overallExperience;

    @Column(name = "is_having_work_gap")
    private boolean isHavingWorkGap;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_location")
    private String companyLocation;

    @Column(name = "job_type")
    private String jobType;

    @Column(name = "job_work_hours")
    private String jobWorkHours;

    @Column(name = "notice_period")
    private String noticePeriod;

    @Column(name = "reason_for_job_change")
    private String reasonForJobChange;

    @Column(name = "take_home_salary")
    private String takeHomeSalary;

    @Column(name = "admin_suggested_salary")
    private String adminSuggestedSalary;

    @Column(name = "is_having_salary_proof")
    private boolean isHavingSalaryProof;

    @Column(name = "salary_proof_document_type")
    private String salaryProofDocumentType;

    @CreationTimestamp
    @ColumnDefault("created_on")
    protected Date createdOn;
    
    @CreationTimestamp
    @ColumnDefault("updated_time")
    protected Date updatedTime;
    
    

}
