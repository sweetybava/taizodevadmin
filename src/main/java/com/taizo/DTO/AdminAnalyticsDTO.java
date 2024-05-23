package com.taizo.DTO;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminAnalyticsDTO {
	
	    private Long id;
	    private Long adminId;
	    private String module;
	    private int followUpCount;
	    private int noOfCalls;
	    private int qualifiedCount;
	    private int notQualifiedCount;
	    private int newLeadCount;
	    private int noOfPayment;
	    private int totalChatCount;
	    private int interviewScheduledCount;
	    private int interviewAttendedCount;
	    private int selectedCandidatesCount;
	    private int joinedCandidatesCount;
	    private int followUpVisitCount;
	    private int newVisitCount;
	    private Timestamp createdOn;
	    private String Time;
	    private String Time_Period;

}
