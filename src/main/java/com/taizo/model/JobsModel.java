package com.taizo.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@Table(name = "jobs")
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor


public class JobsModel {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name="job_id")
	    private int id;
	 
	    @Column(name="job_type")
	    private String jobType;	
	    
	    @Column(name="industry")
	    private String industry;	 
	    
	    @Column(name="job_category")
	    private String jobCategory;	 
	    
	    @Column(name="job_pic")
	    private String jobPic;
	    
	    @Column(name="state")
	    private String state;
	    @Column(name="area")
	    private String area;
	    @Column(name="salary_currency")
	    private String salaryCurrency;

	   
	    @Column(name="job_city")
	    private String jobLocation;
	    
	    @Column(name = "assign_to")
	    private int assignTo;
	    
	    @Column(name="min_salary")
	    private int salary;
	    
	    @Column(name="max_salary")
	    private int maxSalary;
	    
	    @Column(name="job_min_exp")
	    private int jobExp;
	    
	    @Column(name="job_max_exp")
	    private int jobMaxExp;
	
	    @Column(name="min_age")
	    private Integer age;
	    
	    @Column(name="max_age")
	    private Integer maxAge;
	    
	    @Column(name="job_description")
	    private String jobDescription;
	    
	    @Column(name="job_country")
	    private String jobCountry;
	 
	   @Column(name="company_name")
	    private String companyName;	
	    
	    @Column(name="contact_person_name")
	    private String contactPersonName;
	    
	    @Column(name="is_view_cpn")
	    private String IsViewContactPersonName;
	    
	    @Column(name="mobile_number")
	    private String mobileNumber;
	    
	    @Column(name="whatsapp_number")
	    private long whatsappNumber;
	    
	    @Column(name="alternate_mobile_number")
	    private String alternateMobileNumber;
	    
	    @Column(name="is_view_mn")
	    private String IsViewMobileNumber;
	    
	    @Column(name="email_id")
	    private String emailId;
	    
	    @Column(name="is_view_email")
	    private String IsViewEmailId;
	    
	    @Column(name="job_video")
	    private String jobVideo;
	    
	    @Column(name="benefits")
	    private String benefits;
	    
	    @Column(name="specialization")
	    private String specialization;
	    
	    @Column(name="pref_qualification")
	    private String qualification;
	    
	    @Column(name="pref_candidate_location")
	    private String prefCandidateLocation;
	    
	    @Column(name="pref_languages")
	    private String prefLanguages;
	    
	    @Column(name="approval_status")
	    private String approvalStatus;
	    
	    @Column(name="job_status")
	    private String jobStatus;
	    
	    @Column(name="created_by")
	    private String createdBy;
	    
	    @Column(name="employer_id")
	    private int employerId;
	    
	    @Column(name="expiry_date")
	    private String expiryDate;
	    
	    @Column(name="reason_for_close")
	    private String reasonForClose;
	    
	    @Column(name="employer_personalization")
	    private String personalization;
	    
	    @Column(name="work_hours")
	    private String workHours;
	    
	    @Column(name="ot")
	    private String ot;
	    
	    @Column(name="shift_type")
	    private String shiftType;
	    
	    @Column(name="shift_timings")
	    private String shiftTimings;
	    
	    @Column(name="emp_job_id")
	    private String empJobId;
	    
	    @Transient
	    private String videoStatus;
	    
	    @Transient
	    private int calls;
	    @Transient
	    private int applied;
	    
	    @Transient
	    private String kycStatus;
	    
	    @Transient
	    private int pages;
	    
	    @Transient
	    private int size;
	    
	    @Transient
	    private Date endDate;
	    
	    @Transient
	    private String priority;
	    
	    @Column(name="start_date")
	    private String wstartDate;
	    
	    @Column(name="end_date")
	    private String wendDate;
	    
	    @Column(name="start_time")
	    private String wstartTime;
	    
	    @Column(name="end_time")
	    private String wendTime;
	    
	    @Column(name="doc_required")
	    private String wdocRequired;
	    
	    @Column(name="walkin_address")
	    private String waddress;
	    
	    @Column(name="landmark")
	    private String wlandmark;
	    
	    @Column(name="whatsapp_notification")
	    private boolean whatsappNoti;
	    
	    @Column(name="job_latitude")
	    private String jobLatitude;
	    
	    @Column(name="job_longitude")
	    private String jobLongitude;
	    
	    @Column(name="job_location")
	    private String jobLocationAddr;
	    
	    @Column(name="walkin_latitude")
	    private String walkinLatitude;
	    
	    @Column(name="walkin_longitude")
	    private String walkinLongitude;
	    
	   
	    @CreationTimestamp
	   // @ColumnDefault("created_time")
	    @Column(name = "created_time", updatable = false)
	    protected Date createdTime;
	    
	    @UpdateTimestamp
	    @ColumnDefault("updated_time")
	    protected Date updatedTime;
	    
	    @ColumnDefault("job_posted_time")
	    protected Date jobPostedTime;
	    
	    
	    @ColumnDefault("pre_questions_id")
	    protected String preQuestionsId;
	    
	    @Column(name="additional_details_filled")
	    private boolean additionalDetailsFilled;
	    
	    @Column(name = "from_web")
	    private boolean fromWeb;
	    
	    @Column(name = "inactive")
	    private boolean inActive;
	    
	    @Column(name = "deleted")
	    private boolean deleted;

	    @Column(name="gender")
	    private String gender;
	    
	    @Column(name="keyskills",columnDefinition = "MEDIUMTEXT")
	    private String keyskills;
	    
	    @Column(name="deeplink")
	    private String deeplink;
	    
	    @Column(name="can_response_count")
	    private int canResponseCount;
	    
	    @Column(name="total_can_response")
	    private int totalCanResponse;
	    
	    @Column(name = "is_freetrial_job")
	    private boolean isFreetrialJob;
	    
	    @Column(name = "is_draft_job")
	    private boolean isDraftJob;
	    
	    @Column(name = "admin_top_priority")
	    private boolean adminTopPriority;
	    
	    @ColumnDefault("can_res_completed_on")
	    protected Date canResCompletedOn;
	    
	    @Column(name="no_of_openings")
	    private int noOfOpenings;
	    @Column(name="completion_year")
	    private String completionYear;
	    @Column(name="can_with_arrears")
	    private String canWithArrears;
	    @Column(name="can_with_pf_esi")
	    private String canWithPfEsi;
	    @Column(name="can_from_other_state")
	    private String canFromOtherState;
	    @Column(name="contract_duration")
	    private int contractDuration;
	    @Column(name="employment_type")
	    private String employmentType;
	    @Column(name="interview_location")
	    private String interviewLocation;
	    @Column(name="preferred_interview_days")
	    private String preferredInterviewDays;
	    @Column(name="preferred_interview_timings")
	    private String preferredInterviewTimings;
	    
	    //new Column Added (13-07-2023)
	    
	    @Column(name = "mobile_number_country_code")
	    private String mobileNumberCountryCode;

	    @Column(name = "mobile_number_verified")
	    private boolean mobileNumberVerified;

	    @Column(name = "alternate_mobile_number_country_code")
	    private String alternateMobileNumberCountryCode;

	    @Column(name = "alternate_mobile_number_verified")
	    private boolean alternateMobileNumberVerified;

	    @Column(name = "whatsapp_number_country_code")
	    private String whatsappNumberCountryCode;

	    @Column(name = "male")
	    private int male;

	    @Column(name = "female")
	    private int female;

	    @Column(name = "contact_person_position")
	    private String contactPersonPosition;
	    
	    @Column(name = " retention_by_admin_id")
	    private Long retentionByAdminId;

	    @Column(name = "job_closed_by_admin_id")
		private Long jobClosedByAdminId;

	    @Column(name = "job_closed_time")
		private String jobClosedTime;

	   @Column(name = "is_retention_job")
	   private boolean isRetentionJob;




		public JobsModel(int id, String jobDescription, String jobLocation, String jobCountry,
				String jobCategory, int jobExp,String jobPic,String approvalStatus) {
			super();
			this.id = id;
			this.jobDescription = jobDescription;
			this.jobLocation = jobLocation;
			this.jobCountry = jobCountry;
			this.jobCategory = jobCategory;
			this.jobExp = jobExp;
			this.jobPic = jobPic;
			this.approvalStatus = approvalStatus;
		}

		
}
