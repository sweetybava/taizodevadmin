package com.taizo.scheduler;

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
import org.springframework.web.client.RestTemplate;
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
import com.taizo.model.WADetails;
import com.taizo.model.WARecipient;
import com.taizo.model.WATemplate;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.JobRepository;
import com.taizo.service.EmployerService;
import com.taizo.utils.DateUtils;

import freemarker.template.TemplateException;

@Component
public class JobExpiryReminder {

	private static final Logger logger = LoggerFactory.getLogger(JobExpiryReminder.class);

	@Autowired
	JobRepository jobRepository;

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
	
	@Value("${gallabox.emp.job.expires.1day.template.name}")
	private String jobexpiry1dayTemplate;
	/*
	 * @Scheduled(cron = "0 15 10 * * *", zone = "Asia/Kolkata") public void
	 * sendJobExpiryInOneDayRenewal() {
	 * 
	 * logger.info("Running the job expiry alert started at {}", new Date());
	 * 
	 * List<JobsModel> jobs = jobRepository.findJobExpiryinDay(); if
	 * (!jobs.isEmpty()) {
	 * 
	 * for (JobsModel cm : jobs) {
	 * 
	 * int empId = cm.getEmployerId(); Optional<JobsModel> job =
	 * jobRepository.findById(cm.getId()); Optional<EmployerModel> emp =
	 * employerRepository.findById(empId); EmployerModel empData = emp.get();
	 * 
	 * String shortlink = getDeeplink(empData,job.get()); String mn =
	 * String.valueOf(empData.getMobileNumber());
	 * 
	 * WAAlert ex = new WAAlert(); ex.setChannelId(channelId); WARecipient p = new
	 * WARecipient(); p.setPhone(mn); ex.setRecipient(p); WADetails wa = new
	 * WADetails(); wa.setType("template"); WATemplate t = new WATemplate();
	 * t.setTemplateName(jobexpiry1dayTemplate);
	 * 
	 * WABodyValues b = new WABodyValues();
	 * b.setPosition(job.get().getJobCategory());
	 * b.setManagejobsWebLink("https://web.taizo.in/console/manage-jobs");
	 * b.setJobsPageLink(shortlink);
	 * 
	 * t.setBodyValues(b); wa.setTemplate(t); ex.setWhatsapp(wa);
	 * 
	 * String jsonString = new com.google.gson.Gson().toJson(ex);
	 * sendMessage(jsonString);
	 * 
	 * logger.debug("Running the job expiry reminder job completed at {}", new
	 * Date());
	 * 
	 * } } logger.debug("Running the job expiry alert completed at {}", new Date());
	 * }
	 */
	 
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

	private String getDeeplink(EmployerModel empData, JobsModel jobsModel) {
		// TODO Auto-generated method stub
		String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseEmpApiKey;

		DeeplinkRequest dl = new DeeplinkRequest();
		dl.setLongDynamicLink(firebaseEmpHost + "/?link=" + firebaseEmpHost + "/manageJobs/" + empData.getId()
				+ "&apn=" + firebaseEmpPackage);

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
