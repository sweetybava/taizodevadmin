package com.taizo.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="sales_lead")
@Getter
@Setter
@ToString
public class SalesLeadModel {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private int id;
	
	@Column(name = "email_id")
	private String emailId;
	
    @Column(name = "mobile_number")
    private long mobileNumber;
    
    @Column(name = "mobile_country_code")
    private String mobileCountryCode;
	
	@Column(name = "company_name")
	private String companyName;
	
	@Column(name = "business_type")
	private String businessType;
	
	@Column(name = "contact_person_name")
	private String contactPersonName;
    
    @Column(name = "registered_in_app")
    private boolean registeredInApp;
    
    @Column(name = "emp_id")
    private int empId;
    
	@Column(name = "location")
	private String location;

}
