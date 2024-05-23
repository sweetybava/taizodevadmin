package com.taizo.DTO;


import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateAnalyticsFilterDTO {
    public CandidateAnalyticsFilterDTO(Long adminId2, Integer integer, Integer integer2, Integer integer3,
			Integer integer4, Integer integer5, Integer integer6, Object object, Object object2, Object object3,
			Object object4, Object object5, Object object6, Object object7, Date startDate2, Date endDate2) {
		// TODO Auto-generated constructor stub
	}
	private Long adminId;
    private Integer fbMetaLeads;
    private Integer qualifiedFbMetaLeads;
    private Integer canLeads;
    private Integer qualifiedCanLeads;
    private Integer totalLeads;
    private Integer totalQualifiedLeads;
    private Integer canRegistration;
    private Integer interviewScheduled;
    private Integer interviewAttended;
    private Integer companySelected;
    private Integer offerAccepted;
    private Integer joined;
    private Date startDate; 
    private Date endDate;
    
    
}