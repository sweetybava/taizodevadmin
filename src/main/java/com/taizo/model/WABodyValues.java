package com.taizo.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WABodyValues {

	@SerializedName("type")
	@Expose
	public String type;
	@SerializedName("name")
	@Expose
	public String name;
	@SerializedName("job_category")
	@Expose
	public String jobCategory;
	@SerializedName("job_city")
	@Expose
	public String jobCity;
	@SerializedName("salary")
	@Expose
	public String salary;
	@SerializedName("company_name")
	@Expose
	public String companyName;
	@SerializedName("job_exp")
	@Expose
	public String jobExp;
	@SerializedName("industry")
	@Expose
	public String industry;
	@SerializedName("posted_date")
	@Expose
	public String postedDate;

	@SerializedName("position_applied")
	@Expose
	public String positionApplied;
	@SerializedName("exp_in_years")
	@Expose
	public String expInYears;
	@SerializedName("exp_in_months")
	@Expose
	public String expInMonths;
	@SerializedName("qualification")
	@Expose
	public String qualification;
	@SerializedName("keyskills")
	@Expose
	public String keyskills;
	@SerializedName("candidate_name")
	@Expose
	public String candidateName;
	@SerializedName("can_web_link")
	@Expose
	public String canWebLink;
	@SerializedName("can_link")
	@Expose
	public String canLink;

	@SerializedName("pricingpage_web_link")
	@Expose
	public String pricingpageWebLink;
	@SerializedName("plan_page_link")
	@Expose
	public String planPageLink;

	@SerializedName("interview_date")
	@Expose
	public String interviewDate;
	@SerializedName("interview_time")
	@Expose
	public String interviewTime;
	@SerializedName("interview_documents")
	@Expose
	public String interviewDocuments;
	@SerializedName("location")
	@Expose
	public String location;
	@SerializedName("contact_person_name")
	@Expose
	public String contactPersonName;
	@SerializedName("contact_person_number")
	@Expose
	public String contactPersonNumber;
	
	@SerializedName("position")
	@Expose
	public String position;
	@SerializedName("managejobs_web_link")
	@Expose
	public String managejobsWebLink;
	@SerializedName("jobs_page_link")
	@Expose
	public String jobsPageLink;

	@SerializedName("job_web_link")
	@Expose
	public String jobWebLink;
	@SerializedName("job_link")
	@Expose
	public String jobLink;
	
	@SerializedName("postjob_web_link")
	@Expose
	public String postjobWebLink;
	@SerializedName("postjob_mobile_link")
	@Expose
	public String postjobMobileLink;
	
	@SerializedName("area")
	@Expose
	public String area;
	@SerializedName("city")
	@Expose
	public String city;
	@SerializedName("candidate_experience")
	@Expose
	public String canExperience;
	
	@SerializedName("payment_link")
	@Expose
	public String paymentLink;
	
	@SerializedName("plan_amount")
	@Expose
	public String planAmount;
	
	@SerializedName("no_of_openings")
	@Expose
	public String NoOfOpenings;
	
	@SerializedName("offer_price")
	@Expose
	public String offerPrice;
	
	@SerializedName("validity_date")
	@Expose
	public String validity_date;
	
	@SerializedName("discount_percentage")
	@Expose
	public String discountPercentage;
	
	@SerializedName("email")
	@Expose
	public String email;
	
	@SerializedName("mobile_number")
	@Expose
	public String mn;

	@SerializedName("admin_name")
	@Expose
	public String adminName;

	
	
	
	

	public void setButtonValues(List<WAButtonValues> buttonValues) {
		// TODO Auto-generated method stub
		
	}

}
