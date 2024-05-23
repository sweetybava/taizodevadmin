package com.taizo.model;

import javax.persistence.*;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "emp_proforma_invoices")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmpProformaInvoiceModel {
	

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private long id;

	    @Column(name = "mobile_number")
	    private long mobileNumber;
	    
	    @Column(name="admin_id")
	    private Long adminId;
	    
	    @Column(name = "mobile_country_code")
	    private String  mobileCountryCode;

	    @Column(name = "invoice_amount", nullable = false)
	    private int invoiceAmount;

	    @Column(name = "invoice_date")
	    private String invoiceDate;

	    @Column(name = "discount_in_percentage", columnDefinition = "DOUBLE DEFAULT 0")
	    private double discountInPercentage;

	    @Column(name = "company_name")
	    private String companyName;

	    @Column(name = "address")
	    private String address;

	    @Column(name = "email_id")
	    private String emailId;

	    
	    @Column(name = "employer_id")
	    private int employerId;

	    @Column(name = "payment_reference_id")
	    private String paymentReferenceId;
	    
	    @Column(name = "notes", nullable = true)
	    private String notes;
	    
	    @Column(name = "wa_notification")
	    private boolean waNotification;

	   @Column(name = "paid")
	   private boolean paid;

	    @Column(name = "email_notification")
	    private boolean emailNotification;

	    @Column(name = "payment_link")
	    private String paymentLink;
	    
	    @Column(name = "original_amount")
	    private int originalAmount;
	    
	    @Column(name = "contact_person_name")
	    private String contactPersonName;
	    
	    @Column(name = "GST_number")
	    private String GSTNumber;
	    
	    @Column(name = "payment_link_id")
	    private String paymentLinkId;
	    
	    @Column(name = "job_details")
	    private String jobDetails;
	   
	    
	    @Column(name  = "payment_link_validity_date")
	    private String paymentLinkValidityDate;
	    
	    @Column(name = "active")
	    private boolean active;
	    
	    @Transient
	    private String noOfOpenings;
	    
	    @CreationTimestamp
	    @Column(name = "created_on", updatable = false)
	    protected Timestamp createdOn; 
	    
	    @CreationTimestamp
	    @ColumnDefault("created_time")
	    protected Date createdTime;
	
        @Transient
        int page;
        
        @Transient
        int size;
        
	   @Transient
	   Date endDate;
	


}
