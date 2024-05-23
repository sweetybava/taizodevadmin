package com.taizo.model;

import java.util.Date;
import java.util.List;

import lombok.ToString;

@ToString
public class EmpCustomPaymentModel {

	private String planName;
    private int planId;	 
    private int amount;	 
    private int jobPost;   
    private int activeJobPost;   
    private int jobPostValidity;   
    private int planExpiry;  
    private String planExpiryDate;   

    private int jobPosted;   
    private int balance;   
    private List<EmployerPaymentModel> paymentList;
    
	public String getPlanName() {
		return planName;
	}
	public void setPlanName(String planName) {
		this.planName = planName;
	}
	
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public int getJobPost() {
		return jobPost;
	}
	public void setJobPost(int jobPost) {
		this.jobPost = jobPost;
	}
	public int getActiveJobPost() {
		return activeJobPost;
	}
	public void setActiveJobPost(int activeJobPost) {
		this.activeJobPost = activeJobPost;
	}
	public int getJobPostValidity() {
		return jobPostValidity;
	}
	public void setJobPostValidity(int jobPostValidity2) {
		this.jobPostValidity = jobPostValidity2;
	}
	public int getPlanExpiry() {
		return planExpiry;
	}
	public void setPlanExpiry(int planExpiry2) {
		this.planExpiry = planExpiry2;
	}
	public int getJobPosted() {
		return jobPosted;
	}
	public void setJobPosted(int jobPosted) {
		this.jobPosted = jobPosted;
	}
	public int getBalance() {
		return balance;
	}
	public void setBalance(int balance) {
		this.balance = balance;
	}
	public List<EmployerPaymentModel> getPaymentList() {
		return paymentList;
	}
	public void setPaymentList(List<EmployerPaymentModel> his) {
		this.paymentList = his;
	}
	public int getPlanId() {
		return planId;
	}
	public void setPlanId(int planId) {
		this.planId = planId;
	}
	public String getPlanExpiryDate() {
		return planExpiryDate;
	}
	public void setPlanExpiryDate(String planExpiryDate) {
		this.planExpiryDate = planExpiryDate;
	}

    
}
