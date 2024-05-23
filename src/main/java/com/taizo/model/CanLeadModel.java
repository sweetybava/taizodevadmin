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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "can_lead")
@Getter
@Setter
@ToString
public class CanLeadModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @Column(name = "mobile_number")
    private long mobileNumber;
    
    @Column(name="country_code")
    private String countryCode;
    
    @Column(name = "mobile_num_verified")
    private boolean mnverified;

    @Column(name = "name")
    private String name;
    
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "assign_to")
    private Integer assignTo;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "age")
    private int age;

    @Column(name = "gender")
    private String gender;
    
    @Column(name = "country")
    private String country;

    @Column(name = "state")
    private String state;

    @Column(name = "city")
    private String city;

    @Column(name="profile_pic")
    private String profilePic;
    
    @Column(name = "qualification")
    private String qualification;

    @Column(name = "specialization")
    private String specification;
    
    @Column(name = "exp_in_manufacturing")
    private boolean expInManufacturing;
    
    @Column(name = "experienced")
    private boolean experienced;
    
    @Column(name = "candidate_type")
    private String candidateType;

    @Column(name = "industry")
    private String industry;
    
    @Column(name = "job_category")
    private String jobCategory;
    
    @Column(name = "key_Skill")
    private String keySkill;

    @Column(name = "exp_in_years")
    private int expYears;

    @Column(name = "exp_in_months")
    private int expMonths;
    
    @Column(name = "job_location")
    private String jobLocation;
    
    @Column(name = "known_languages")
    private String knownLanguages;
    
    @Column(name = "certification_courses")
    private String courses;

    @Column(name = "wa_campaign")
    private boolean WACampaign;
    
    @Column(name = "stop_wa_campaign")
    private boolean stopWACampaign;

    @Column(name = "language_key")
    private String languageKey;

    @Column(name = "fcm_token")
    private String fcmToken;
    
    @Column(name = "profile_page_no")
    private int profilePageNo;
    
    @Column(name = "looking_for_a_job")
    private boolean lookingForaJob;
    
    @Column(name = "from_app")
    private boolean fromApp;
    
    @Column(name = "from_wa")
    private boolean fromWA;
    
    @Column(name = "from_admin")
    private boolean fromAdmin;
   
    @CreationTimestamp
    @ColumnDefault("created_time")
    protected Date createdTime;
    
    @Column(name = "passed_out_year")
    private int passed_out_year;

    @Column(name = "passed_out_month")
    private int passed_out_month;
    
    @Column(name = "student")
    private String student;
    
    
    @Column(name = "currently_working")
    private boolean currentlyworking;
    
    @Column(name = "immediate_joiner")
    private boolean immediateJoiner;
    
    @Column(name = "reason_for_unemployment")
    private String reason_for_unemployment;
    
    @Column(name = "reason_for_jobchange")
    private String reason_for_jobchange;
    
    @Column(name = "pref_location")
    private String prefLocation;
    
    @Column(name = "pref_area")
    private String prefArea;
    
    @Column(name = "whatsapp_number")
    private long whatsappNumber;
    
    @Column(name = "pf_esi_account")
    private String pfEsiAccount;
    
    @Column(name = "is_having_arrear")
    private String isHavingArrear;
   
    @Column(name = "reference")
    private String reference;
    
    
    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "qualified")
    private boolean qualified;

    @Column(name = "not_qualified")
    private boolean notQualified;

    @Column(name = "from_fb_meta_lead_ad")
    private boolean fromFbMetaLeadAd;

    @Column(name = "joining_availability")
    private String joiningAvailability;

    @Column(name = "notes")
    private String notes;
    
    @Transient
    private String fromSource;
    
    @Transient
    private String status;
    
    @Transient
    private String scheduledBy;
    
    @Transient
    private Date endDate;
    
    @Transient
    private int page;
    
    @Transient
    private int size;
    
    @Transient
    private int maxExperience;

    
}
