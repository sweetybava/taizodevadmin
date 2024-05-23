package com.taizo.model;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "emp_plans")
@Getter
@Setter
@ToString
public class EmpPlansModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "plan_id")
	private int planId;

	@Column(name = "job_count")
	private int jobCount;

	@Column(name = "total_amount")
	private int totalAmount;

	@Column(name = "discount")
	private int discount;
	
	@Column(name = "discount_desc")
	private String discountDesc;
	
	@Column(name = "saved_amount")
	private int savedAmount;

	@Column(name = "pay_amount")
	private int payAmount;

	@Column(name = "can_response")
	private int canResponse;

	@Column(name = "plan_expiry_in_days")
	private int planExpiryInDays;

	@Column(name = "plan_expiry_in_months")
	private int planExpiryInMonths;

	@Column(name = "job_expiry_days")
	private int jobExpiryDays;
	}
