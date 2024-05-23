package com.taizo.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employer_invoice")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployerInvoiceModel {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private int id;

	    @Column(name = "emp_id", nullable = false)
	    private int empId;
	    
	    @Column(name = "admin_id", nullable = false)
	    private Long adminId;
	    
	    @Column(name = "email_id")
	    private String emailId;

	    @Column(name = "invoice_no", nullable = false)
	    private String invoiceNo;

	    @Column(name = "invoice_amount", nullable = false)
	    private long invoiceAmount;

	    @Column(name = "invoice_date")
	    private String invoiceDate;

	    @Column(name = "due_date")
	    private String dueDate;

	    @Column(name = "paid", nullable = false)
	    private boolean paid;
	    
	    @CreationTimestamp
	    @ColumnDefault("created_time")
	    protected Date createdTime;


}
