package com.taizo.model;

import java.sql.Timestamp;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsModel {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "module")
    private String module;

    @Column(name = "emp_follow_up_calls")
    private Integer empFollowUpCount;

    @Column(name = "emp_no_of_calls")
    private Integer empNoOfCalls;

    @Column(name = "emp_qualified_count")
    private Integer empQualifiedCount;

    @Column(name = "emp_not_qualified_count")
    private Integer empNotQualifiedCount;

    @Column(name = "emp_new_lead_count")
    private Integer empNewLeadCount;

    @Column(name = "can_new_lead_count")
    private Integer canNewLeadCount;

    @Column(name = "emp_no_of_payment")
    private Integer empNoOfPayment;

    @Column(name = "can_total_chat_count")
    private Integer canTotalChatCount;

    @Column(name = "can_no_of_calls")
    private Integer canNoOfCalls;

    @Column(name = "can_qualified_count")
    private Integer canQualifiedCount;

    @Column(name = "can_not_qualified_count")
    private Integer canNotQualifiedCount;

    @Column(name = "can_interview_scheduled_count")
    private Integer canInterviewScheduledCount;

    @Column(name = "can_interview_attended_count")
    private Integer canInterviewAttendedCount;

    @Column(name = "can_interview_selected_count")
    private Integer canInterviewSelectedCount;

    @Column(name = "can_interview_not_selected_count")
    private Integer canInterviewNotSelectedCount;
    
    @Column(name = "can_interview_not_attended_count")
    private Integer canInterviewNotAttendedCount;
    
    @Column(name = "can_interview_offer_rejected_count")
    private Integer canInterviewOfferRejectedCount;

    @Column(name = "can_interview_joined_count")
    private Integer canInterviewJoinedCount;

    @Column(name = "closed_job_count")
    private Integer closedJobCount;

    @Column(name = "retention_count")
    private Integer retentionCount;

    @Column(name = "emp_field_new_lead_count")
    private Integer empFieldNewLeadCount;

    @Column(name = "emp_field_follow_up_count")
    private Integer empFieldFollowUpCount;

    @Column(name = "emp_field_follow_up_visit_count")
    private Integer empFieldFollowUpVisitCount;

    @Column(name = "emp_field_new_visit_count")
    private Integer empFieldNewVisitCount;

    @Column(name = "emp_field_no_of_payment_count")
    private Integer empFieldNoOfPaymentCount;

    @Column(name = "emp_intro_calls")
    private Integer empIntroCalls;

    @Column(name = "can_follow_up_calls")
    private Integer canFollowUpCalls;

    @Column(name = "can_intro_calls")
    private Integer canIntroCall;

    @Column(name = "emp_lead_qualified_count")
    private Integer empLeadQualifiedCount;
    
    @Column(name = "not_attended_count")
    private Integer notAttendedCount;
    
    @Column(name = "wrong_person_count")
    private Integer wrongPersonCount;
    
    @Column(name = "number_blocked_count")
    private Integer numberBlockedCount;
    
    @Column(name = "not_reachable_count")
    private Integer notReachableCount;
    
    @Column(name = "switch_off_count")
    private Integer switchOffCount;

    @Column(name = "can_lead_qualified_count")
    private Integer canLeadQualifiedCount;

    @Column(name = "emp_lead_not_qualified_count")
    private Integer empLeadNotQualifiedCount;

    @Column(name = "can_lead_not_qualified_count")
    private Integer canLeadNotQualifiedCount;

    @CreationTimestamp
    @Column(name = "created_on", updatable = false)
    protected Timestamp createdOn;

@Transient
private String dateFilter;

@Transient
private Integer analyticscount;

}
