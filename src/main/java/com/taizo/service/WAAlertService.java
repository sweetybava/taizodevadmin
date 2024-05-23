package com.taizo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.taizo.model.Admin;
import com.taizo.model.EmpInterviewNotificationModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.LeadModel;
import com.taizo.model.RescheduleInterviewModel;
import com.taizo.model.WAAlert;
import com.taizo.model.WABodyValues;
import com.taizo.model.WAButtonValues;
import com.taizo.model.WADetails;
import com.taizo.model.WAParameters;
import com.taizo.model.WARecipient;
import com.taizo.model.WATemplate;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.LeadRepository;

@Service
public class WAAlertService {

	@Value("${gallabox.campaign.url}")
	private String campaignUrl;
	@Value("${gallabox.auth.apiKey}")
	private String apiKey;
	@Value("${gallabox.auth.apiSecret}")
	private String apiSecret;
	@Value("${gallabox.channel.id}")
	private String channelId;
	
	@Value("${gallabox.js.jobalert.template.name}")
	private String jobTemplateName;
	@Value("${gallabox.js.jobalert.ta.template.name}")
	private String jobTaTemplateName;
	@Value("${gallabox.js.jobalert.hi.template.name}")
	private String jobHiTemplateName;
	@Value("${gallabox.emp.callalert.template.name}")
	private String callTemplateName;
	@Value("${gallabox.emp.applyalert.template.name}")
	private String applyTemplateName;
	@Value("${gallabox.emp.joblimitoveralert.template.name}")
	private String joblimitoverTemplateName;
	@Value("${gallabox.emp.freejoblimitoveralert.template.name}")
	private String freejoblimitoverTemplateName;
	@Value("${gallabox.emp.paymentfailedalert.template.name}")
	private String paymentFailedTemplateName;
	@Value("${gallabox.emp.kycfailedalert.template.name}")
	private String kycFailedTemplateName;
	@Value("${gallabox.emp.interviewalert.template.name}")
	private String empInterviewTemplateName;
	@Value("${gallabox.js.interviewalert.template.name}")
	private String interviewTemplateName;
	@Value("${gallabox.js.interviewalert.ta.template.name}")
	private String interviewTaTemplateName;
	@Value("${gallabox.js.interviewalert.hi.template.name}")
	private String interviewHiTemplateName;
	@Value("$gallabox.emp.interviewalert.template.name")
	private String canInterviewScheduledTemplateName;
	
	@Autowired
	EmployerRepository employerRepository;
	
	@Autowired
	LeadRepository leadRepository;
	
	
	//Jobseeker job alert
	public void sendJobAlert(HashMap<String, String> data) {
		// TODO Auto-generated method stub
		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient(); 
		p.setPhone(data.get("mn"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		String key = data.get("languageKey");
		try {
		if(key.equalsIgnoreCase("ta")) {
			t.setTemplateName(jobTaTemplateName);
		}else if(key.equalsIgnoreCase("hi")) {
			t.setTemplateName(jobHiTemplateName);
		}else {
			t.setTemplateName(jobTemplateName);	
		}
		}catch(Exception e) {
			t.setTemplateName(jobTemplateName);	
		}
		String link = data.get("jobLink");
		String jobLink = link.substring(link.lastIndexOf("/") + 1);

		
		WABodyValues b = new WABodyValues();
		b.setName("*"+data.get("name")+"*");
		b.setJobCategory("*"+data.get("jobCategory")+"*");
		b.setJobCity("*"+data.get("city")+"*");
		b.setSalary("*"+data.get("salary")+"*");
		b.setCompanyName("*"+data.get("companyName")+"*");
		b.setJobExp("*"+data.get("exp")+"*");
		b.setIndustry("*"+data.get("industry")+"*");
		b.setPostedDate("*"+data.get("postedDate")+"*");
		t.setBodyValues(b);
		
		List<WAButtonValues> btnlist = new ArrayList<>();
		WAButtonValues btn = new WAButtonValues();
		btn.setIndex(0);
		btn.setSubType("url");

		WAParameters para = new WAParameters();
		para.setType("text");
		para.setText(jobLink);
		btn.setParameters(para);
		btnlist.add(btn);
		
		t.setButtonValues(btnlist);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);
		String jsonString = new com.google.gson.Gson().toJson(ex);
		sendMessage(jsonString);
	}

	private void sendMessage(String jsonString) {
		// TODO Auto-generated method stub
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("apiKey", apiKey);
		headers.add("apiSecret", apiSecret);
		
		HttpEntity<String> request = new HttpEntity<String>(jsonString.toString(), headers);

		RestTemplate restTemplate = new RestTemplate();

		try {
			Object c =restTemplate.postForObject(campaignUrl, request, Object.class);
		} catch (Exception e) {
		}
	}

	//Emp Call alert
	@Async
	public void sendCallAlertToEmployer(HashMap<String, String> data) {
		// TODO Auto-generated method stub
		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(data.get("mn"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName(callTemplateName);
		
		WABodyValues b = new WABodyValues();
		b.setName("*"+data.get("name")+"*");
		b.setPositionApplied("*"+data.get("jobCategory")+"*");
		b.setExpInYears("*"+data.get("exp")+"*");
		//b.setExpInMonths("*"+data.get("expMonths")+"*");
		b.setQualification("*"+data.get("qualification")+"*");
		b.setKeyskills("*"+data.get("keySkills")+"*");
		b.setCandidateName(data.get("name"));
		b.setCanWebLink(data.get("webLink"));
		b.setCanLink(data.get("appLink"));
		
		t.setBodyValues(b);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);
		
		String jsonString = new com.google.gson.Gson().toJson(ex);
		sendMessage(jsonString);
	}
	
	//Emp Application alert
	@Async
	public void sendApplyAlertToEmployer(HashMap<String, String> data) {
		// TODO Auto-generated method stub
		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(data.get("mn"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName(applyTemplateName);
		
		WABodyValues b = new WABodyValues();
		b.setCandidateName("*"+data.get("name")+"*");
		b.setPositionApplied("*"+data.get("jobCategory")+"*");
		b.setExpInYears("*"+data.get("exp")+"*");
		b.setExpInMonths("*"+data.get("expMonths")+"*");
		b.setQualification("*"+data.get("qualification")+"*");
		b.setKeyskills("*"+data.get("keySkills")+"*");
		b.setCanWebLink(data.get("webLink"));
		b.setCanLink(data.get("appLink"));
		
		t.setBodyValues(b);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);
		
		String jsonString = new com.google.gson.Gson().toJson(ex);
		sendMessage(jsonString);
	}
	
	//Emp Payment Failure Alert
	@Async
	public void sendPaymentFailedAlertToEmployer(HashMap<String, String> data) {
		// TODO Auto-generated method stub
		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(data.get("mn"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName(paymentFailedTemplateName);
		
		WABodyValues b = new WABodyValues();
		b.setPricingpageWebLink(data.get("webLink"));
		b.setPlanPageLink(data.get("appLink"));
		
		t.setBodyValues(b);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);
		
		String jsonString = new com.google.gson.Gson().toJson(ex);
		sendMessage(jsonString);
	}

	//Emp KYC Failed Alert
	@Async
	public void sendKYCFailedAlertToEmployer(HashMap<String, String> data) {
		// TODO Auto-generated method stub
		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(data.get("mn"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName(kycFailedTemplateName);
		
		WABodyValues b = new WABodyValues();
		b.setCompanyName("*"+data.get("name")+"*");
		
		t.setBodyValues(b);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);
		
		String jsonString = new com.google.gson.Gson().toJson(ex);
		sendMessage(jsonString);
	}
	
	//Employer interview alert
	public void sendEmpInterviewAlert(HashMap<String, String> data) {
		// TODO Auto-generated method stub
		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(data.get("mn"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		
		t.setTemplateName(empInterviewTemplateName);	
		
		WABodyValues b = new WABodyValues();
		b.setCandidateName(data.get("name"));
		b.setPositionApplied(data.get("jobCategory"));
		b.setInterviewDate(data.get("interviewDate"));
		b.setInterviewTime(data.get("interviewTime"));
		b.setCanExperience(data.get("exp"));
		//b.setExpInMonths(data.get("expMonths"));
		b.setQualification(data.get("qualification"));
		b.setKeyskills(data.get("keyskills"));
		b.setCanWebLink(data.get("webLink"));
		b.setCanLink(data.get("appLink"));

		t.setBodyValues(b);
		
		wa.setTemplate(t);
		ex.setWhatsapp(wa);
		String jsonString = new com.google.gson.Gson().toJson(ex);
		sendMessage(jsonString);
	}
	
	//Jobseeker interview alert
	public void sendInterviewAlert(HashMap<String, String> data) {
		// TODO Auto-generated method stub
		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(data.get("mn"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		
		String key = data.get("languageKey");
		try {
		if(key.equalsIgnoreCase("ta")) {
			t.setTemplateName(interviewTaTemplateName);
		}else if(key.equalsIgnoreCase("hi")) {
			t.setTemplateName(interviewHiTemplateName);
		}else {
			t.setTemplateName(interviewTemplateName);	
		}
		}catch(Exception e) {
			t.setTemplateName(interviewTemplateName);	
		}
		
		WABodyValues b = new WABodyValues();
		b.setName(data.get("name"));
		b.setCompanyName(data.get("companyName"));
		b.setInterviewDate(data.get("interviewDate"));
		b.setInterviewTime(data.get("interviewTime"));
		b.setLocation(data.get("address"));
		b.setContactPersonName(data.get("contactPersonName"));
		b.setContactPersonNumber(data.get("contactPersonNumber"));

		t.setBodyValues(b);
		
		List<WAButtonValues> btnlist = new ArrayList<>();
		WAButtonValues btn = new WAButtonValues();
		btn.setIndex(0);
		btn.setSubType("url");

		WAParameters para = new WAParameters();
		para.setType("text");
		para.setText(data.get("jobLink"));
		btn.setParameters(para);
		btnlist.add(btn);
		
		t.setButtonValues(btnlist);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);
		String jsonString = new com.google.gson.Gson().toJson(ex);
		sendMessage(jsonString);
	}
	
	//Emp Job Limit Over alert
	@Async
	public void sendJobLimitOverAlertToEmployer(HashMap<String, String> data) {
		// TODO Auto-generated method stub
		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(data.get("mn"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName(joblimitoverTemplateName);
		
		WABodyValues b = new WABodyValues();
		b.setPosition("*"+data.get("jobCategory")+"*");
		b.setManagejobsWebLink(data.get("webLink"));
		b.setJobsPageLink(data.get("appLink"));
		
		t.setBodyValues(b);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);
		
		String jsonString = new com.google.gson.Gson().toJson(ex);
		sendMessage(jsonString);
	}
	
	//Emp Job Limit Over alert
	/*
	 * @Async public void sendFreeJobLimitOverAlertToEmployer(HashMap<String,
	 * String> data) { // TODO Auto-generated method stub WAAlert ex = new
	 * WAAlert(); ex.setChannelId(channelId); WARecipient p = new WARecipient();
	 * p.setPhone(data.get("mn")); ex.setRecipient(p); WADetails wa = new
	 * WADetails(); wa.setType("template"); WATemplate t = new WATemplate();
	 * t.setTemplateName(freejoblimitoverTemplateName);
	 * 
	 * WABodyValues b = new WABodyValues();
	 * b.setPricingpageWebLink(data.get("webLink"));
	 * b.setPlanPageLink(data.get("appLink"));
	 * 
	 * t.setBodyValues(b); wa.setTemplate(t); ex.setWhatsapp(wa);
	 * 
	 * String jsonString = new com.google.gson.Gson().toJson(ex);
	 * sendMessage(jsonString); }
	 */
	
	public void sendProFormaInvoiceAlert(HashMap<String, String> waData) {
	    WAAlert ex = new WAAlert();
	    ex.setChannelId(channelId);

	    WARecipient p = new WARecipient();
	    p.setPhone(waData.get("mn"));
	    ex.setRecipient(p);

	    WATemplate template = new WATemplate();
	    template.setTemplateName("pro_forma_invoice");

	    WADetails whatsappDetails = new WADetails();
	    whatsappDetails.setType("template");
	    whatsappDetails.setTemplate(template);

	    WABodyValues bodyValues = new WABodyValues();
	    bodyValues.setContactPersonName(waData.get("contact_person_name"));
	    bodyValues.setPlanAmount("*" + waData.get("plan_amount") + "*");
	    bodyValues.setNoOfOpenings("*" + waData.get("no_of_openings") + "*");
	    bodyValues.setPaymentLink(waData.get("payment_link"));

	    // Create the button values
	    List<WAButtonValues> buttonValuesList = new ArrayList<>();
	    WAButtonValues button = new WAButtonValues();
	    button.setIndex(0);
	    button.setSubType("url");

	    WAParameters buttonParameters = new WAParameters();
	    buttonParameters.setType("text"); 
	    buttonParameters.setText(waData.get("s3_bucket_link")); 
	    button.setParameters(buttonParameters);

	    buttonValuesList.add(button);

	    template.setBodyValues(bodyValues);
	    template.setButtonValues(buttonValuesList);

	    ex.setWhatsapp(whatsappDetails);

	    String jsonString = new Gson().toJson(ex);
	    sendMessage(jsonString);
	    System.out.println(jsonString);
	}
	
	public void sendProFormaInvoiceDiscountAlert(HashMap<String, String> waDataDiscounted) {
	    WAAlert ex = new WAAlert();
	    ex.setChannelId(channelId);

	    WARecipient p = new WARecipient();
	    p.setPhone(waDataDiscounted.get("mn"));
	    ex.setRecipient(p);

	    WATemplate template = new WATemplate();
	    template.setTemplateName("pro_forma_invoice_discount");

	    WADetails whatsappDetails = new WADetails();
	    whatsappDetails.setType("template");
	    whatsappDetails.setTemplate(template);

	    WABodyValues bodyValues = new WABodyValues();
	    bodyValues.setContactPersonName(waDataDiscounted.get("contact_person_name"));
	    bodyValues.setPlanAmount("*" + waDataDiscounted.get("plan_amount") + "*");
	    bodyValues.setOfferPrice("*"+waDataDiscounted.get("offer_price")+"*");
	    bodyValues.setDiscountPercentage("*" + waDataDiscounted.get("discount_percentage") +"*");
	    bodyValues.setNoOfOpenings("*" + waDataDiscounted.get("no_of_openings") + "*");
	    bodyValues.setPaymentLink("*" + waDataDiscounted.get("payment_link") + "*");
	    bodyValues.setValidity_date("*" + waDataDiscounted.get("validity_date") + "*");

	    // Create the button values
	    List<WAButtonValues> buttonValuesList = new ArrayList<>();
	    WAButtonValues button = new WAButtonValues();
	    button.setIndex(0);
	    button.setSubType("url");

	    WAParameters buttonParameters = new WAParameters();
	    buttonParameters.setType("text"); 
	    buttonParameters.setText(waDataDiscounted.get("s3_bucket_link")); 
	    button.setParameters(buttonParameters);

	    buttonValuesList.add(button);

	    template.setBodyValues(bodyValues);
	    template.setButtonValues(buttonValuesList);

	    ex.setWhatsapp(whatsappDetails);

	    String jsonString = new Gson().toJson(ex);
	    sendMessage(jsonString);
	    System.out.println(jsonString);
	}

	public void sendEmployerEnquiry(HashMap<String, String> empEnquiry) {
		
		// TODO Auto-generated method stub
		 WAAlert ex = new WAAlert();
		    ex.setChannelId(channelId);
		    ex.setChannelType("whatsapp");

		    WARecipient p = new WARecipient();
		    p.setName("Saravanan");
		    p.setPhone("919600014728");
		    ex.setRecipient(p);

		    WATemplate template = new WATemplate();
		    template.setTemplateName("employer_enquiries");

		    WADetails whatsappDetails = new WADetails();
		    whatsappDetails.setType("template");
		    
		    WABodyValues bodyValues = new WABodyValues();
		    bodyValues.setType(empEnquiry.get("type"));
		    bodyValues.setCompanyName(empEnquiry.get("company_name"));
		    bodyValues.setIndustry(empEnquiry.get("industry"));
		    bodyValues.setCity(empEnquiry.get("city"));
		    bodyValues.setContactPersonName(empEnquiry.get("contact_person_name"));
		    bodyValues.setEmail(empEnquiry.get("email"));
		    bodyValues.setMn(empEnquiry.get("mobile_number"));
		    
		  
		    template.setBodyValues(bodyValues);
		    whatsappDetails.setTemplate(template);

		    ex.setWhatsapp(whatsappDetails);

		    String jsonString = new Gson().toJson(ex);
		    sendMessage(jsonString);
		    System.out.println(jsonString);

	}
	public void adminSendInterviewAlertCan(HashMap<String, String> data) {

		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(data.get("mn"));
		p.setName(data.get("name"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName("before_int_rem_new");

		WABodyValues w = new WABodyValues();
		w.setName("*" + data.get("name") + "*");
		w.setCompanyName("*" + data.get("company_name") + "*");
		w.setArea("*" + data.get("area") + "*");
		w.setCity("*" + data.get("city") + "*");
		w.setInterviewDate("*" + data.get("interview_date") + "*");
		w.setInterviewTime("*" + data.get("interview_time") + "*");
		w.setContactPersonName("*" + data.get("contact_person_name") + "*");
		w.setContactPersonNumber("*" + data.get("contact_person_number") + "*");
		w.setInterviewDocuments("*" + data.get("interview_documents") + "*");
		t.setBodyValues(w);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);

		String jsonString = new com.google.gson.Gson().toJson(ex);
		System.out.println(jsonString);
		sendMessage(jsonString);
	}

	public void adminInterviewAlertEmp(HashMap<String, String> d) {

		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(d.get("mn"));
		p.setName(d.get("name"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName("can_interview_scheduled");

		WABodyValues w = new WABodyValues();
		w.setCandidateName("*" + d.get("candidate_name") + "*");
		w.setPositionApplied("*" + d.get("position_applied") + "*");
		w.setInterviewDate("*" + d.get("interview_date") + "*");
		w.setInterviewTime("*" + d.get("interview_time") + "*");
		w.setCanExperience("*" + d.get("candidate_experience") + "*");
		w.setQualification("*" + d.get("qualification") + "*");
		w.setKeyskills("*" + d.get("skills") + "*");
		w.setCanLink("*" + d.get("can_link") + "*");
		w.setCanWebLink("*" + d.get("can_web_link") + "*");
		t.setBodyValues(w);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);

		String jsonString = new com.google.gson.Gson().toJson(ex);
		System.out.println(jsonString);
		sendMessage(jsonString);
	}
	
public void sendEmployerEnquirys(HashMap<String, String> empEnquiry) {
		
		// TODO Auto-generated method stub
		 WAAlert ex = new WAAlert();
		    ex.setChannelId(channelId);
		    ex.setChannelType("whatsapp");

		    WARecipient p = new WARecipient();
		    p.setName("Saravanan");
		    p.setPhone("918778431401");
		    ex.setRecipient(p);

		    WATemplate template = new WATemplate();
		    template.setTemplateName("employer_enquiries");

		    WADetails whatsappDetails = new WADetails();
		    whatsappDetails.setType("template");
		    
		    WABodyValues bodyValues = new WABodyValues();
		    bodyValues.setType(empEnquiry.get("type"));
		    bodyValues.setCompanyName(empEnquiry.get("company_name"));
		    bodyValues.setIndustry(empEnquiry.get("industry"));
		    bodyValues.setCity(empEnquiry.get("city"));
		    bodyValues.setContactPersonName(empEnquiry.get("contact_person_name"));
		    bodyValues.setEmail(empEnquiry.get("email"));
		    bodyValues.setMn(empEnquiry.get("mobile_number"));
		    
		  
		    template.setBodyValues(bodyValues);
		    whatsappDetails.setTemplate(template);

		    ex.setWhatsapp(whatsappDetails);

		    String jsonString = new Gson().toJson(ex);
		    sendMessage(jsonString);
		    System.out.println(jsonString);

	}

public void sendMetaAlerts(HashMap<String, String> wa) {
	
	 WAAlert ex = new WAAlert();
	    ex.setChannelId(channelId);
	    ex.setChannelType("whatsapp");

	    WARecipient p = new WARecipient();
	    p.setPhone(wa.get("mn"));
	    ex.setRecipient(p);

	    WATemplate template = new WATemplate();
	    template.setTemplateName("meta_leads");

	    WADetails whatsappDetails = new WADetails();
	    whatsappDetails.setType("template");
	    
	    whatsappDetails.setTemplate(template);

	    ex.setWhatsapp(whatsappDetails);

	    String jsonString = new Gson().toJson(ex);
	    sendMessage(jsonString);
	    System.out.println(jsonString);
	
	
}

    public void senCanFormAlert(HashMap<String, String> wa) {

		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);

		WARecipient p = new WARecipient();
		p.setPhone(wa.get("mn"));
		ex.setRecipient(p);

		WATemplate template = new WATemplate();
		template.setTemplateName("js_profile_form");

		WADetails whatsappDetails = new WADetails();
		whatsappDetails.setType("template");
		whatsappDetails.setTemplate(template);

		WABodyValues bodyValues = new WABodyValues();
		bodyValues.setName("*" + wa.get("name") + "*");
		bodyValues.setAdminName("*" + wa.get("admin_name") + "*");

		// Create the button values
		List<WAButtonValues> buttonValuesList = new ArrayList<>();
		WAButtonValues button = new WAButtonValues();
		button.setIndex(0);
		button.setSubType("url");

		WAParameters buttonParameters = new WAParameters();
		buttonParameters.setType("text");
		buttonParameters.setText(wa.get("text"));
		button.setParameters(buttonParameters);

		buttonValuesList.add(button);

		template.setBodyValues(bodyValues);
		template.setButtonValues(buttonValuesList);

		ex.setWhatsapp(whatsappDetails);

		String jsonString = new Gson().toJson(ex);
		sendMessage(jsonString);
		System.out.println(jsonString);
    }
    public void adminInterviewAlertEmps(HashMap<String, String> d) {

		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(d.get("mn"));
		p.setName(d.get("name"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName("can_interview_rescheduled");

		WABodyValues w = new WABodyValues();
		w.setCandidateName("*" + d.get("candidate_name") + "*");
		w.setPositionApplied("*" + d.get("position_applied") + "*");
		w.setInterviewDate("*" + d.get("rescheduled_on") + "*");
		w.setInterviewTime("*" + d.get("interview_time") + "*");
		w.setCanExperience("*" + d.get("candidate_experience") + "*");
		w.setQualification("*" + d.get("qualification") + "*");
		w.setKeyskills("*" + d.get("skills") + "*");
		w.setCanLink("*" + d.get("can_link") + "*");
		w.setCanWebLink("*" + d.get("can_web_link") + "*");
		t.setBodyValues(w);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);

		String jsonString = new com.google.gson.Gson().toJson(ex);
		System.out.println(jsonString);
		sendMessage(jsonString);
	}

	public void adminSendInterviewAlertCandidate(HashMap<String, String> data) {
		// TODO Auto-generated method stub
		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(data.get("mn"));
		p.setName(data.get("name"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName("before_int_reschedule");

		WABodyValues w = new WABodyValues();
		w.setName("*" + data.get("name") + "*");
		w.setCompanyName("*" + data.get("company_name") + "*");
		w.setArea("*" + data.get("area") + "*");
		w.setCity("*" + data.get("city") + "*");
		w.setInterviewDate("*" + data.get("rescheduled_on") + "*");
		w.setInterviewTime("*" + data.get("interview_time") + "*");
		w.setContactPersonName("*" + data.get("contact_person_name") + "*");
		w.setContactPersonNumber("*" + data.get("contact_person_number") + "*");
		w.setInterviewDocuments("*" + data.get("interview_documents") + "*");
		t.setBodyValues(w);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);

		String jsonString = new com.google.gson.Gson().toJson(ex);
		System.out.println(jsonString);
		sendMessage(jsonString);
	}

	public void adminSendInterviewAlertCanTam(HashMap<String, String> data) {
		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(data.get("mn"));
		p.setName(data.get("name"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName("before_int_reschedule_ta");

		WABodyValues w = new WABodyValues();
		w.setName("*" + data.get("name") + "*");
		w.setCompanyName("*" + data.get("company_name") + "*");
		w.setArea("*" + data.get("area") + "*");
		w.setCity("*" + data.get("city") + "*");
		w.setInterviewDate("*" + data.get("rescheduled_on") + "*");
		w.setInterviewTime("*" + data.get("interview_time") + "*");
		w.setContactPersonName("*" + data.get("contact_person_name") + "*");
		w.setContactPersonNumber("*" + data.get("contact_person_number") + "*");
		w.setInterviewDocuments("*" + data.get("interview_documents") + "*");
		t.setBodyValues(w);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);

		String jsonString = new com.google.gson.Gson().toJson(ex);
		System.out.println(jsonString);
		sendMessage(jsonString);
		
	}

	public void adminSendInterviewAlertCanHin(HashMap<String, String> data) {
		// TODO Auto-generated method stub
		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(data.get("mn"));
		p.setName(data.get("name"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName("before_int_reschedule_hi");

		WABodyValues w = new WABodyValues();
		w.setName("*" + data.get("name") + "*");
		w.setCompanyName("*" + data.get("company_name") + "*");
		w.setArea("*" + data.get("area") + "*");
		w.setCity("*" + data.get("city") + "*");
		w.setInterviewDate("*" + data.get("rescheduled_on") + "*");
		w.setInterviewTime("*" + data.get("interview_time") + "*");
		w.setContactPersonName("*" + data.get("contact_person_name") + "*");
		w.setContactPersonNumber("*" + data.get("contact_person_number") + "*");
		w.setInterviewDocuments("*" + data.get("interview_documents") + "*");
		t.setBodyValues(w);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);

		String jsonString = new com.google.gson.Gson().toJson(ex);
		System.out.println(jsonString);
		sendMessage(jsonString);
	}

	public void adminSendInterviewAlertCanTa(HashMap<String, String> data) {
		// TODO Auto-generated method stub
		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(data.get("mn"));
		p.setName(data.get("name"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName("before_int_rem_ta_new");

		WABodyValues w = new WABodyValues();
		w.setName("*" + data.get("name") + "*");
		w.setCompanyName("*" + data.get("company_name") + "*");
		w.setArea("*" + data.get("area") + "*");
		w.setCity("*" + data.get("city") + "*");
		w.setInterviewDate("*" + data.get("interview_date") + "*");
		w.setInterviewTime("*" + data.get("interview_time") + "*");
		w.setContactPersonName("*" + data.get("contact_person_name") + "*");
		w.setContactPersonNumber("*" + data.get("contact_person_number") + "*");
		w.setInterviewDocuments("*" + data.get("interview_documents") + "*");
		t.setBodyValues(w);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);

		String jsonString = new com.google.gson.Gson().toJson(ex);
		System.out.println(jsonString);
		sendMessage(jsonString);
		
	}

	public void adminSendInterviewAlertCanHi(HashMap<String, String> data) {
		// TODO Auto-generated method stub
		WAAlert ex = new WAAlert();
		ex.setChannelId(channelId);
		WARecipient p = new WARecipient();
		p.setPhone(data.get("mn"));
		p.setName(data.get("name"));
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName("before_int_rem_hi_new");

		WABodyValues w = new WABodyValues();
		w.setName("*" + data.get("name") + "*");
		w.setCompanyName("*" + data.get("company_name") + "*");
		w.setArea("*" + data.get("area") + "*");
		w.setCity("*" + data.get("city") + "*");
		w.setInterviewDate("*" + data.get("interview_date") + "*");
		w.setInterviewTime("*" + data.get("interview_time") + "*");
		w.setContactPersonName("*" + data.get("contact_person_name") + "*");
		w.setContactPersonNumber("*" + data.get("contact_person_number") + "*");
		w.setInterviewDocuments("*" + data.get("interview_documents") + "*");
		t.setBodyValues(w);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);

		String jsonString = new com.google.gson.Gson().toJson(ex);
		System.out.println(jsonString);
		sendMessage(jsonString);
	}

	public void sendEmployerLead(EmployerModel existing) {
	    WAAlert ex = new WAAlert();
	    ex.setChannelId(channelId);
	    WARecipient p = new WARecipient();
	    p.setPhone("917806805802");
	    p.setName(existing.getName()); 
	    ex.setRecipient(p);
	    WADetails wa = new WADetails();
	    wa.setType("template");
	    WATemplate t = new WATemplate();
	    t.setTemplateName("employer_registration");

	    WABodyValues w = new WABodyValues();
	    w.setName("*" + existing.getName() + "*");
	    w.setCompanyName("*" + existing.getCompanyName() + "*");
	    w.setIndustry("*" + existing.getIndustry() + "*");
	    w.setCity("*" + existing.getCity() + "*"); 
	    w.setContactPersonName("*" + existing.getContactPersonName() + "*");
	    w.setEmail("*" + existing.getEmailId() + "*");
	    w.setMn("*" + existing.getMobileNumber() + "*");
	    t.setBodyValues(w);
	    wa.setTemplate(t);
	    ex.setWhatsapp(wa);

	    String jsonString = new com.google.gson.Gson().toJson(ex);
	    System.out.println(jsonString);
	    sendMessage(jsonString);
	}
	
}
