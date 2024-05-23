package com.taizo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "candidate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CandidateModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "candidate_id")
    private Integer id;

    @Column(name = "user_id")
    private int userId;
    
    @Transient
    private int maxExperience;
    
    @Transient
    private String eligibility;

    @Transient
    private int pages;
    
    @Transient
    private int size;
    
    @Transient
    private Date endDate;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "assign_to")
    private int assignTo;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "job_type")
    private String jobType;

    @Column(name = "industry")
    private String industry;

    @Column(name = "city")
    private String city;

    @Column(name = "student")
    private String student;

    @Column(name = "pref_country")
    private String prefCountry;

    @Column(name = "pref_location")
    private String prefLocation;
    
    @Column(name = "pref_area")
    private String prefArea;

    @Column(name = "candidate_location")
    private String candidateLocation;

    @Column(name = "pref_dom_location")
    private String domesticLocation;

    @Column(name = "pref_over_location")
    private String overseasLocation;

    @Column(name = "job_category")
    private String jobCategory;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "age")
    private String age;

    @Column(name = "gender")
    private String gender;

    @Column(name = "current_country")
    private String currentCountry;

    @Column(name = "current_state")
    private String currentState;

    @Column(name = "current_city")
    private String currentCity;

    @Column(name = "per_country")
    private String perCountry;

    @Column(name = "per_state")
    private String perState;

    @Column(name = "per_city")
    private String perCity;

    @Column(name = "mobile_number")
    private long mobileNumber;

    @Column(name = "whatsapp_number")
    private long whatsappNumber;

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "qualification")
    private String qualification;

    @Column(name = "specialization")
    private String specification;

    @Column(name = "certification_courses")
    private String certificationCourses;

    @Column(name = "certification_specialization")
    private String certificationSpecialization;

    @JsonIgnore
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<CanLanguageModel> languages = new HashSet<CanLanguageModel>();
    
    @JsonIgnore
    @OneToMany(mappedBy = "documents", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CanDocuments> documents = new ArrayList<CanDocuments>();

    @Column(name = "candidate_type")
    private String candidateType;

    @Column(name = "exp_in_years")
    private int experience;

    @Column(name = "exp_in_months")
    private int expMonths;

    @Column(name = "overseas_exp_in_years")
    private Integer overseasExp;

    @Column(name = "overseas_exp_in_months")
    private Integer overseasExpMonths;

    @Column(name = "exp_certificate")
    private String expCertificate;

    @Column(name = "certificate_type")
    private String certificateType;

    @Column(name = "license")
    private String license;

    @Column(name = "license_type")
    private String licenseType;

    @Column(name = "key_Skill")
    private String keySkill;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "skill_video")
    private String skills;


    @Column(name = "skill_video_type")
    private String skillVideoType;

    @Column(name = "language_key")
    private String languageKey;

    @CreationTimestamp
    @ColumnDefault("created_time")
    protected Date createdTime;

    @Column(name = "amount")
    private int amount;
    
    @Column(name = "discount_amount")
    private int discountAmount;
    
    @Column(name = "job_limit")
    private int jobLimit;

    @Column(name = "status")
    private String approvalStatus;
    
    @Column(name = "reference")
    private String reference;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Transient
	private String isFirstTime;
    
    @Transient
	private String profilePic;
    
    @Column(name = "registered")
    private boolean registered;
    @Column(name = "looking_for_a_job")
    private boolean lookingForaJob;
    @Column(name = "wa_campaign")
    private boolean WACampaign;
    @Column(name = "exp_in_manufacturing")
    private boolean expInManufacturing;
    @Column(name = "experienced")
    private boolean experienced;
    @Column(name = "from_wa")
    private boolean fromWA;
    @Column(name = "registered_in_app")
    private boolean regInApp;
    @Column(name = "from_app")
    private boolean fromApp;
    
    @Column (name = "from_admin")
    private boolean fromAdmin;
    
    @Column(name = "stop_wa_campaign")
    private boolean stopWACampaign;
    
    @Column(name = "used_free_trial")
    private boolean usedFreeTrial;
    
    @Column(name = "state")
    private String state;
    
    @Column(name = "passed_out_year")
    private int passed_out_year;

    @Column(name = "passed_out_month")
    private int passed_out_month;
    
    @Column(name = "currently_working")
    private boolean currentlyworking;
    
    @Column(name = "immediate_joiner")
    private boolean immediateJoiner;
    
    @Column(name = "profile_filled")
    private boolean profileFilled;
    
    @Column(name = "reason_for_unemployment")
    private String reason_for_unemployment;
    
    @Column(name = "reason_for_jobchange")
    private String reason_for_jobchange;
    
    @Column(name = "profile_last_updated_dt")
    private String profileLastUpdatedDt;
    
    @Column(name = "pf_esi_account")
    private String pfEsiAccount;
    
    @Column(name = "is_having_arrear")
    private String isHavingArrear;
    
   @Transient
    private String knownLanguages;
    
   @Transient
    private String countryCode;
   
    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "qualified")
    private boolean qualified;

    @Column(name = "not_qualified")
    private boolean notQualified;

    @Column(name = "from_fb_meta_lead_ad")
    private boolean fromFbMetaLeadAd;

    @Column(name="resume_link")
    private String resume;
    
    @Column(name="emergency_contact_number")
    private String emergencyContactNumber;
    
    @Column(name="relationship_type")
    private String relationshipType;
    
    @Column(name="relation_name")
    private String relationName;

}
