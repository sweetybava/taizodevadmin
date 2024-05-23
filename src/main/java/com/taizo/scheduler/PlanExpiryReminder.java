package com.taizo.scheduler;

import java.io.ByteArrayInputStream; 
import java.io.IOException;
import java.net.URISyntaxException;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.taizo.model.DeeplinkRequest;
import com.taizo.model.DeeplinkSuffix;
import com.taizo.model.EmployerModel;
import com.taizo.model.Example;
import com.taizo.model.FirebaseShortLink;
import com.taizo.model.Parameters;
import com.taizo.model.To;
import com.taizo.model.WAAlert;
import com.taizo.model.WABodyValues;
import com.taizo.model.WADetails;
import com.taizo.model.WARecipient;
import com.taizo.model.WATemplate;
import com.taizo.repository.EmployerRepository;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.service.EmployerService;
import com.taizo.service.ReportService;
import com.taizo.utils.TupleStore;

@Component
public class PlanExpiryReminder {

	private static final Logger logger = LoggerFactory.getLogger(PlanExpiryReminder.class);

	@Autowired
	EmployerRepository employerRepository;
	
	@Value("${firebase.emp.api.key}")
	private String firebaseEmpApiKey;

	@Value("${firebase.deeplink.emp.hostname}")
	private String firebaseEmpHost;

	@Value("${firebase.empapp.package.name}")
	private String firebaseEmpPackage;
	
	@Value("${gallabox.campaign.url}")
	private String campaignUrl;
	@Value("${gallabox.auth.apiKey}")
	private String apiKey;
	@Value("${gallabox.auth.apiSecret}")
	private String apiSecret;
	@Value("${gallabox.channel.id}")
	private String channelId;
	
	@Value("${gallabox.emp.freejob.expires.1day.template.name}")
	private String freejobexpiry1dayTemplate;
	
	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Scheduled(cron = "0 0 10 * * *", zone = "Asia/Kolkata")
	public void sendFreePlanExpireOneDayReminder() throws IOException {
		if (activeProfile.equalsIgnoreCase("prod")) {

		logger.debug("Running the free plan expiry reminder job started at {}", new Date());

		List<EmployerModel> employers = employerRepository.findByFreePlanExpireOneDay();
		if (!employers.isEmpty()) {

			for (EmployerModel cm : employers) {
				int empId = cm.getId();
				Optional<EmployerModel> emp = employerRepository.findById(empId);
				EmployerModel empData = emp.get();
				
				String shortlink = getDeeplink(empData);
				String mn = String.valueOf(empData.getMobileNumber());
				
				WAAlert ex = new WAAlert();
				ex.setChannelId(channelId);
				WARecipient p = new WARecipient();
				p.setPhone(mn);
				ex.setRecipient(p);
				WADetails wa = new WADetails();
				wa.setType("template");
				WATemplate t = new WATemplate();
				t.setTemplateName(freejobexpiry1dayTemplate);
				
				WABodyValues b = new WABodyValues();
				b.setPricingpageWebLink("https://web.taizo.in/console/pricing");
				b.setPlanPageLink(shortlink);
				
				t.setBodyValues(b);
				wa.setTemplate(t);
				ex.setWhatsapp(wa);
				
				String jsonString = new com.google.gson.Gson().toJson(ex);
				sendMessage(jsonString);

				logger.debug("Running the free plan expiry reminder job completed at {}", new Date());

			}
		}
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
	
	/*
	 * @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Kolkata") public void
	 * sendFreePlanExpiredReminder() throws IOException {
	 * 
	 * logger.debug("Running the free plan expired reminder job started at {}", new
	 * Date());
	 * 
	 * List<EmployerModel> employers = employerRepository.findByFreePlanExpired();
	 * if (!employers.isEmpty()) {
	 * 
	 * for (EmployerModel cm : employers) {
	 * 
	 * int empId = cm.getId(); Optional<EmployerModel> emp =
	 * employerRepository.findById(empId); EmployerModel empData = emp.get();
	 * 
	 * String shortlink = getDeeplink(empData);
	 * 
	 * 
	 * HttpHeaders headers1 = new HttpHeaders();
	 * headers1.setContentType(MediaType.APPLICATION_JSON);
	 * headers1.add("Authorization", "Bearer" + authToken);
	 * 
	 * String mn = String.valueOf(empData.getMobileNumber());
	 * 
	 * Example ex = new Example(); To t = new To(); t.setPhoneNumber("91" + mn);
	 * ex.setTo(t); Parameters p = new Parameters();
	 * ex.setCampaignID(freeexpiredCampaignId);
	 * p.setPricingpageWebLink("http://web.taizo.in/console/pricing");
	 * p.setPlanPageLink(shortlink); ex.setParameters(p);
	 * 
	 * String jsonString = new com.google.gson.Gson().toJson(ex);
	 * 
	 * HttpEntity<String> request = new HttpEntity<String>(jsonString.toString(),
	 * headers1);
	 * 
	 * RestTemplate restTemplate = new RestTemplate();
	 * 
	 * try { restTemplate.postForObject(campaignUrl, request, Object.class); } catch
	 * (Exception e) { System.out.println("Exception" + mn); }
	 * 
	 * logger.debug("Running the free plan expired reminder job completed at {}",
	 * new Date());
	 * 
	 * } } }
	 * 
	 * @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Kolkata") public void
	 * sendUpgradePaymentReminderMail() throws IOException {
	 * 
	 * logger.debug("Running the plan expiry reminder job started at {}", new
	 * Date());
	 * 
	 * List<EmployerModel> employers = employerRepository.findByPlanExpireOneDay();
	 * if (!employers.isEmpty()) {
	 * 
	 * for (EmployerModel cm : employers) {
	 * 
	 * int empId = cm.getId(); Optional<EmployerModel> emp =
	 * employerRepository.findById(empId); EmployerModel empData = emp.get();
	 * 
	 * String shortlink = getDeeplink(empData);
	 * 
	 * HttpHeaders headers1 = new HttpHeaders();
	 * headers1.setContentType(MediaType.APPLICATION_JSON);
	 * headers1.add("Authorization", "Bearer" + authToken);
	 * 
	 * String mn = String.valueOf(empData.getMobileNumber());
	 * 
	 * Example ex = new Example(); To t = new To(); t.setPhoneNumber("91" + mn);
	 * ex.setTo(t); Parameters p = new Parameters();
	 * ex.setCampaignID(subscriptionexpiry1dayCampaignId);
	 * p.setPricingpageWebLink("http://web.taizo.in/console/pricing");
	 * p.setPlanPageLink(shortlink); ex.setParameters(p);
	 * 
	 * String jsonString = new com.google.gson.Gson().toJson(ex);
	 * 
	 * HttpEntity<String> request = new HttpEntity<String>(jsonString.toString(),
	 * headers1);
	 * 
	 * RestTemplate restTemplate = new RestTemplate();
	 * 
	 * try { restTemplate.postForObject(campaignUrl, request, Object.class); } catch
	 * (Exception e) { System.out.println("Exception" + mn); }
	 * 
	 * logger.debug("Running the plan expiry reminder job completed at {}", new
	 * Date());
	 * 
	 * } } }
	 */
	
	private String getDeeplink(EmployerModel empData) {
		// TODO Auto-generated method stub
		String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseEmpApiKey;

		DeeplinkRequest dl = new DeeplinkRequest();
		dl.setLongDynamicLink(firebaseEmpHost + "/?link=" + firebaseEmpHost + "/pricing/" + empData.getId()
				+ "/" + 0 + "&apn=" + firebaseEmpPackage);

		DeeplinkSuffix c = new DeeplinkSuffix();
		c.setOption("UNGUESSABLE");

		String json = new com.google.gson.Gson().toJson(dl);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> req = new HttpEntity<String>(json.toString(), headers);

		RestTemplate restTemp = new RestTemplate();

		FirebaseShortLink response = null;
		try {
			response = restTemp.postForObject(url, req, FirebaseShortLink.class);
		} catch (Exception e) {

		}
		return response.getShortLink();
	}

}
