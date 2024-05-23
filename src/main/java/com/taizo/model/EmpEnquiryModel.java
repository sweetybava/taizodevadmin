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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emp_enquiries")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmpEnquiryModel {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(name = "company_name")
	    private String companyName;

	    @Column(name = "contact_person_name")
	    private String contactPersonName;

	    @Column(name = "mobile_country_code")
	    private String mobileCountryCode;

	    @Column(name = "mobile_number")
	    private String mobileNumber;
	    
	    @Column(name = "designation")
	    private String designation;

	    @Column(name = "email_id")
	    private String emailId;

	    @Column(name = "industry")
	    private String industry;

	    @Column(name = "city")
	    private String city;
	    
	    @Column(name="whatsapp_notification")
	    private boolean WaNotification;
	    
	    @CreationTimestamp
	    @Column(name = "created_on", updatable = false)
	    protected Timestamp createdOn; 
	    
	    @CreationTimestamp
	    @ColumnDefault("created_time")
	    protected Date createdTime;
	    
	    @Transient
	    private Date endDate;
	    
	    @Transient
	    private int page;
	    
	    @Transient
	    private int size;
	    
	    @Transient
	    private String type;

}
