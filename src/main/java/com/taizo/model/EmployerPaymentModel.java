package com.taizo.model;

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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "employer_payment")
@Getter
@Setter
@ToString
public class EmployerPaymentModel
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;
    
    @Column(name="employer_id")
    private int employerId;
    
    @Column(name="plan_id")
    private int planId;
    
    @Column(name="admin_id")
    private Integer adminId;
    
    @Column(name="type_of_purchase")
    private String typeOfPurchase;
    
    @Column(name="amount")
    private int amount;
    
    @Column(name="email_id")
    private String emailId;
    
    @Column(name="mobile_number")
    private long mobileNumber;
    
    @Column(name="payment_id")
    private String paymentId;
    
    @Column(name="order_id")
    private String orderId;
    
    @Column(name="status")
    private String status;
    
    @Column(name="signature_verified")
    private String signature;
    
    @Column(name="captured")
    private boolean captured;
    
    @Column(name="reason")
    private String reason;
    
    @Column(name="notes")
    private String notes;
    
    @Column(name="invoice_no")
    private int invoiceNo;
    
    @Column(name="invoice_url")
    private String invoiceUrl;
    
    @Column(name="from_web")
    private boolean fromWeb;
    
    @CreationTimestamp
    @ColumnDefault("created_time")
    protected Date createdTime;
    
    //new Column Added (12-07-2023)
    @Column(name = "no_of_openings")
    private int numberOfOpenings;

    @Column(name = "no_of_job_category")
    private int numberOfJobCategory;
    
    @Column(name = "proforma_invoice_id")
    private int proformaInvoiceId;
    
    @Column(name = "lead_id ")
    private int leadId ;
    
    @Column(name = "from_admin ")
    private boolean fromAdmin ;

    @Column(name = "cheque_date ")
    private String chequeDate ;
    
    @Column(name="cheque_no")
    private String chequeNo;
    
    @Column(name="paid_on")
    private String paidOn;

   
    
    }
