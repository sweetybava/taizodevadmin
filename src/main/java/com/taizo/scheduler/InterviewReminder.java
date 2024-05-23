package com.taizo.scheduler;

import java.io.IOException; 
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.taizo.model.CanInterviewsModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.DeeplinkRequest;
import com.taizo.model.DeeplinkSuffix;
import com.taizo.model.EmployerModel;
import com.taizo.model.Example;
import com.taizo.model.FirebaseShortLink;
import com.taizo.model.JobsModel;
import com.taizo.model.Parameters;
import com.taizo.model.To;
import com.taizo.model.WAAlert;
import com.taizo.model.WABodyValues;
import com.taizo.model.WAButtonValues;
import com.taizo.model.WADetails;
import com.taizo.model.WAParameters;
import com.taizo.model.WARecipient;
import com.taizo.model.WATemplate;
import com.taizo.repository.CanInterviewRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.JobRepository;
import com.taizo.service.EmployerService;
import com.taizo.utils.DateUtils;

import freemarker.template.TemplateException;

@Component
public class InterviewReminder {

	private static final Logger logger = LoggerFactory.getLogger(InterviewReminder.class);

	@Autowired
	CanInterviewRepository canInterviewRepository;
	
	@Autowired
	CandidateRepository candidateRepository;

	@Value("${gallabox.campaign.url}")
	private String campaignUrl;
	@Value("${gallabox.auth.apiKey}")
	private String apiKey;
	@Value("${gallabox.auth.apiSecret}")
	private String apiSecret;
	@Value("${gallabox.channel.id}")
	private String channelId;
	
	@Value("${gallabox.js.interviewremainder.template.name}")
	private String interviewremainderTemplate;
	@Value("${gallabox.js.interviewremainder.ta.template.name}")
	private String interviewremainderTamilTemplate;
	@Value("${gallabox.js.interviewremainder.hi.template.name}")
	private String interviewremainderHindiTemplate;
	@Value("${gallabox.js.beforeinterviewremainder.template.name}")
	private String beforeInterviewremainderTemplate;
	@Value("${gallabox.js.beforeinterviewremainder.ta.template.name}")
	private String beforeInterviewremainderTamilTemplate;
	@Value("${gallabox.js.beforeinterviewremainder.hi.template.name}")
	private String beforeInterviewremainderHindiTemplate;
	
	@Value("${spring.profiles.active}")
	private String activeProfile;
	
	 @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Kolkata")
	public void sendInterviewReminder() {

		if (activeProfile.equalsIgnoreCase("prod")) {
			logger.info("Running the interview remainder alert started at {}", new Date());

		List<CanInterviewsModel> interviews = canInterviewRepository.findInterviewReminderOnDay();
		if (!interviews.isEmpty()) {

			for (CanInterviewsModel cm : interviews) {

				Optional<CandidateModel> can = candidateRepository.findById(cm.getCanId());
				CandidateModel c = can.get();
				
				String mn = String.valueOf(c.getMobileNumber());
								
				WAAlert ex = new WAAlert();
				ex.setChannelId(channelId);
				ex.setChannelType("whatsapp");
				WARecipient p = new WARecipient();
				p.setPhone("91" + mn);
				ex.setRecipient(p);
				WADetails wa = new WADetails();
				wa.setType("template");
				WATemplate t = new WATemplate();
				String key = c.getLanguageKey();
				try {
				if(key.equalsIgnoreCase("ta")) {
					t.setTemplateName(interviewremainderTamilTemplate);
				}else if(key.equalsIgnoreCase("hi")) {
					t.setTemplateName(interviewremainderHindiTemplate);
				}else {
					t.setTemplateName(interviewremainderTemplate);
				}
				}catch(Exception e) {
					t.setTemplateName(interviewremainderTemplate);
				}
				
				WABodyValues b = new WABodyValues();
				b.setCompanyName(cm.getCompanyName());
				b.setCity(cm.getCity());
				b.setArea(cm.getArea());
				b.setInterviewTime(cm.getInterviewTime());
				b.setContactPersonName(cm.getContactPersonName());
				b.setContactPersonNumber(String.valueOf(cm.getContactNumber()));
				b.setInterviewDocuments(cm.getDocuments());
				t.setBodyValues(b);
				wa.setTemplate(t);
				ex.setWhatsapp(wa);
				
				List<WAButtonValues> btnlist = new ArrayList<>();
				WAButtonValues btn = new WAButtonValues();
				btn.setIndex(0);
				
				btn.setSubType("quick_reply");

				WAParameters para = new WAParameters();
				para.setType("payload");
				try {
				if(key.equalsIgnoreCase("ta")) {
					para.setPayload("உதவி வேண்டும்");
				}else if(key.equalsIgnoreCase("hi")) {
					para.setPayload("मदद चाहिए");
				}else {
					para.setPayload("Need Help");
				}
				}catch(Exception e) {
					para.setPayload("Need Help");
				}
				btn.setParameters(para);
				btnlist.add(btn);
				
				WAButtonValues btn1 = new WAButtonValues();
				btn1.setIndex(1);
				btn1.setSubType("quick_reply");

				WAParameters para1 = new WAParameters();
				para1.setType("payload");
				try {
				if(key.equalsIgnoreCase("ta")) {
					para1.setPayload("தேதி மாற்றவும்");
				}else if(key.equalsIgnoreCase("hi")) {
					para1.setPayload("तिथियां बदलें");
				}else {
					para1.setPayload("Change Date");
				}
				}catch(Exception e) {
					para1.setPayload("Change Date");
				}
				btn1.setParameters(para1);
				btnlist.add(btn1);
				
				WAButtonValues btn2 = new WAButtonValues();
				btn2.setIndex(2);
				btn2.setSubType("quick_reply");

				WAParameters para2 = new WAParameters();
				para2.setType("payload");
				try {
				if(key.equalsIgnoreCase("ta")) {
					para2.setPayload("வேறு வேலை சேர்ந்தேன்");
				}else if(key.equalsIgnoreCase("hi")) {
					para2.setPayload("दूसरी नौकरी मिल गई");
				}else {
					para2.setPayload("Joined Another Job");
				}
				}catch(Exception e) {
					para2.setPayload("Joined Another Job");
				}
				btn2.setParameters(para2);
				btnlist.add(btn2);

				t.setButtonValues(btnlist);
				
				String jsonString = new com.google.gson.Gson().toJson(ex);
				sendMessage(jsonString);

				logger.debug("Running the interview reminder alert completed at {}", new Date());

			}
		}
		logger.debug("Running the interview reminder alert completed at {}", new Date());
		}
	}
	
	@Scheduled(cron = "0 0 10 * * *", zone = "Asia/Kolkata")
	public void sendBeforeInterviewReminder() {
		if (activeProfile.equalsIgnoreCase("prod")) {

		logger.info("Running the before interview remainder alert started at {}", new Date());

		List<CanInterviewsModel> interviews = canInterviewRepository.findBeforeInterviewReminder();
		if (!interviews.isEmpty()) {

			for (CanInterviewsModel cm : interviews) {

				Optional<CandidateModel> can = candidateRepository.findById(cm.getCanId());
				CandidateModel c = can.get();
				
				String mn = String.valueOf(c.getMobileNumber());
				String inDate = String.valueOf(cm.getInterviewDate());

				Date date = null;
				try {
					date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(inDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				String interviewDate = new SimpleDateFormat("dd-MMM-yy").format(date);

								
				WAAlert ex = new WAAlert();
				ex.setChannelId(channelId);
				ex.setChannelType("whatsapp");
				WARecipient p = new WARecipient();
				p.setPhone("91" + mn);
				ex.setRecipient(p);
				WADetails wa = new WADetails();
				wa.setType("template");
				WATemplate t = new WATemplate();
				String key = c.getLanguageKey();
				try {
				if(key.equalsIgnoreCase("ta")) {
					t.setTemplateName(beforeInterviewremainderTamilTemplate);
				}else if(key.equalsIgnoreCase("hi")) {
					t.setTemplateName(beforeInterviewremainderHindiTemplate);
				}else {
					t.setTemplateName(beforeInterviewremainderTemplate);
				}
				}catch(Exception e) {
					t.setTemplateName(beforeInterviewremainderTemplate);
				}
				
				WABodyValues b = new WABodyValues();
				b.setName(c.getFirstName());
				b.setCompanyName(cm.getCompanyName());
				b.setCity(cm.getCity());
				b.setArea(cm.getArea());
				b.setInterviewTime(cm.getInterviewTime());
				b.setInterviewDate(interviewDate);
				b.setContactPersonName(cm.getContactPersonName());
				b.setContactPersonNumber(String.valueOf(cm.getContactNumber()));
				
				t.setBodyValues(b);
				wa.setTemplate(t);
				ex.setWhatsapp(wa);

				
				String jsonString = new com.google.gson.Gson().toJson(ex);
				sendMessage(jsonString);

				logger.debug("Running the before interview reminder alert completed at {}", new Date());

			}
		}
		logger.debug("Running the before interview reminder alert completed at {}", new Date());
		}
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
}
