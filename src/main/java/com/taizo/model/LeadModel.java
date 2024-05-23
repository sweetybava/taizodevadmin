package com.taizo.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Data
@Entity
@Table(name ="emp_lead")
@Getter
@Setter
@ToString
public class LeadModel {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private int id;
	
	@Column(name = "email_id")
	private String emailId;
	
    @Column(name = "mn_verified")
    private boolean mnverified;
	
	@Column(name = "company_name")
	private String companyName;
	
	@Column(name = "sla_notes")
	private String slaNotes;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "industry")
	private String industry;
	
	@Column(name = "city")
	private String city;

    @Column(name = "mobile_number")
    private long mobileNumber;
    
    @Column(name = "mobile_country_code")
    private String mobileCountryCode;
    
    @Column(name = "whatsapp_number")
    private long whatsappNumber;
    
    @Column(name = "registered")
    private boolean registered;
    
    @Column(name = "registered_in_app")
    private boolean registeredInApp;
    
    @Column(name = "from_app")
    private boolean fromApp;
    
    @Column(name = "from_web")
    private boolean fromWeb;
    
    @Column(name = "from_facebook")
    private boolean fromFacebook;
    
    @Column(name = "from_whatsapp")
    private boolean fromWhatsapp;
    
    @Column(name = "from_webbot")
    private boolean fromWebBot;
    
    
    @Column (name = "from_admin")
    private boolean fromAdmin;
    
    @Column(name = "contact_person_name")
    private String contactPersonName;
    
    @Column(name = "address")
    private String address;

    @Column(name = "notes")
    private String notes;
    
    @Column(name = "assign_to")
    private Long assignTo;
    
    @CreationTimestamp
    @ColumnDefault("created_time")
    protected Date createdTime;
    
    @Column(name="sla_email_on")
    private Date slaEmailOn;
    
    @Column(name="intro_email_on")
    private Date introEmailOn;
    
    
    @Column(name = "email_notification")
    private boolean emailNotification;

    @Column(name = "qualified")
    private boolean qualified;

    @Column(name = "not_qualified")
    private boolean notQualified;
    
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
	/*
	 * @ColumnDefault("registered_time") private Date registeredTime;
	 */

    @Column(name = "deactivated")
    private boolean deactivated;

}
