package com.taizo.model;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Entity
@Table(name = "employer")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmployerModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "password")
    private String password;

    @Column(name = "token")
    private String token;
    
    @Column (name = "from_admin")
    private boolean fromAdmin;
    
    @Column(name = "google_user_name")
    private String googleUserName;

    @Column(name = "category")
    private String category;

    @Column(name = "industry")
    private String industry;

    @Column(name = "profile_pic")
    private String profilePic;

    @Column(name = "company_logo")
    private String CompanyLogo;

    @Column(name = "company_name")
    private String companyName;
    
    @Column(name = "assign_to")
    private int assignTo;

    @Column(name = "cantact_person_name")
    private String contactPersonName;

    @Column(name = "mobile_country_code")
    private String mobileCountryCode;

    @Column(name = "phone_country_code")
    private String phoneCountryCode;

    @Column(name = "mobile_number")
    private long mobileNumber;
    
    @Column(name="whatsapp_number")
    private long whatsappNumber;
    
    @Column(name="sla_email_on")
    private Date slaEmailOn;
    
    @Column(name="intro_email_on")
    private Date introEmailOn;
    
    @Column(name="alternate_mobile_number")
    private String alternateMobileNumber;

    @Column(name = "phone")
    private long phone;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "pincode")
    private int pincode;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "linkedln_url")
    private String linkedlnURl;

    @Column(name = "reference")
    private String reference;

    @Column(name = "notes")
    private String notes;

    @Column(name = "reg_proof_number")
    private String regProofNumber;

    @Column(name = "reg_proof_doc")
    private String regProofDoc;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "tax_doc")
    private String taxDoc;
    
    @Column(name = "no_of_employees")
    private String noOfEmployees;
    
    @Column(name = "year_founded")
    private String yearFounded;

    @Column(name = "registration_id")
    private String registrationID;

    @Column(name = "plan")
    private int plan;

    @Column(name = "no_of_viewed_candidate")
    private int noOfViewedCandidate;

    @Column(name = "plan_expiry_date")
    private String expiryDate;
    
    @Column(name = "free_plan_expiry_date")
    private String freePlanExpiryDate;
    
    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "deactivated")
    private boolean deactivated;
    
    @Column(name = "email_verified")
    private boolean emailVerified;

    @Column(name = "kyc_status")
    private String kycStatus;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "used_free_trial")
    private String usedFreeTrial;
    
    @Column(name = "plan_job_count")
    private int planJobCount;
    
    @Column(name = "reset_code")
    private String resetCode;

    @Column(name = "company_details_filled")
    private boolean companyDetailsFilled;
    
    @Column(name = "contact_details_filled")
    private boolean contactDetailsFilled;
    
    @Column(name = "latitude")
    private String latitude;
    
    @Column(name = "longitude")
    private String longitude;
    
    @Column(name = "last_login_date")
    private String lastLoginDate;
    
    @CreationTimestamp
	@ColumnDefault("notification_last_read_dt")
	private Date notificationLastReadDt;
    
    @Column(name = "push_notification")
    private boolean pushNotification;
    
    @Column(name = "email_notification")
    private boolean emailNotification;
    
    @Column(name = "whatsapp_notification")
    private boolean whatsappNotification;
    
    @Column(name = "notification_sound")
    private boolean notificationSound;
    
    @Column(name = "from_web")
    private boolean fromWeb;
    
    @Column(name = "area")
    private String area;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "mn_verified")
    private boolean mnVerified;
    
    @Column(name = "registered")
    private boolean registered;
    
    @Column(name = "registered_in_app")
    private boolean registeredInApp;
    
    @Column(name = "from_app")
    private boolean fromApp;
    
    @Column(name = "from_facebook")
    private boolean fromFacebook;
    
    @Column(name = "from_whatsapp")
    private boolean fromWhatsapp;
    
    @Column(name = "from_webbot")
    private boolean fromWebBot;

    @Column(name = "qualified")
    private boolean qualified;

    @Column(name = "not_qualified")
    private boolean notQualified;
    

    public Date getNotificationLastReadDt() {
        return notificationLastReadDt;
    }

    public void setNotificationLastReadDt(Date notificationLastReadDt) {
        this.notificationLastReadDt = notificationLastReadDt;
    }

    @CreationTimestamp
    @ColumnDefault("created_time")
    protected Date createdTime;
    
    @CreationTimestamp
    @ColumnDefault("updated_time")
    protected Date updatedTime;

    @Transient
    private List<Integer> ListPlans;
    
    @Transient
    private int page;
    
    @Transient
    private int size;
    
    @Transient
    private Date endDate;
    
    @Column(name = "sla_email_notification")
    private boolean slaEmailNotification;
    
    @Column(name = "replacement_duration")
    private String replacementDuration;
    
    @Column(name = "payment_duration")
    private String paymentDuration;
    
    @Column(name = "recruitment_fee_percentage")
    private String recruitmentFeePercentage;

    @Column(name = "recruitment_fee_type")
    private String recruitmentFeeType;

@Transient
private String designation;

}
