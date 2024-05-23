package com.taizo.model;

import lombok.ToString;

@ToString
public class EmpJobDashboardModel {
	
    private String jobCategory;  
    private int appliedCandidates;	 
    private int processedCandidates;
    private int remainingCandidates;

	public String getJobCategory() {
		return jobCategory;
	}
	public void setJobCategory(String jobCategory) {
		this.jobCategory = jobCategory;
	}
	public int getAppliedCandidates() {
		return appliedCandidates;
	}
	public void setAppliedCandidates(int appliedCandidates) {
		this.appliedCandidates = appliedCandidates;
	}
	public int getProcessedCandidates() {
		return processedCandidates;
	}
	public void setProcessedCandidates(int processedCandidates) {
		this.processedCandidates = processedCandidates;
	}
	public int getRemainingCandidates() {
		return remainingCandidates;
	}
	public void setRemainingCandidates(int remainingCandidates) {
		this.remainingCandidates = remainingCandidates;
	}
    
    

}
