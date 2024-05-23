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
@Table(name = "employer_field_lead")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployerFieldLead {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
	
	@Column(name = "company_name")
	private String companyName;
	
	@Column(name = "area")
	private String area;
	
	@Column(name = "city")
	private String city;
	
	@Column(name = "mobile_number")
	private long mobileNumber;
	
	@Column(name = "whatsapp_number")
	private long whatsappNumber;
	
	@Column(name = "alternate_mobile_number")
	private long alternateMobileNumber;
	
	@Column(name = "lead_image_link")
	private String leadImageLink;
	
	@Column(name = "email_id")
	private String emailId;
	
	@Column(name = "lattitude")
	private String lattitude;
	    
	@Column(name = "longitude")
    private String longitude;
	
	@Column(name = "admin_id")
    private int adminId;
	
    @CreationTimestamp
	@ColumnDefault("created_time")
    protected Date createdTime;
	    
    @CreationTimestamp
    @ColumnDefault("updated_time")
    protected Date updatedTime;
    
    @Transient
    private int pages;
    
    @Transient
    private int size;
    
    @Transient
    private Date endDate;
	    
}
