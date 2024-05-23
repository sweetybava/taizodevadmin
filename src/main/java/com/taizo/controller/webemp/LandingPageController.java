 package com.taizo.controller.webemp;

import java.io.ByteArrayOutputStream; 
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.Callback;
import com.taizo.model.CandidateModel;
import com.taizo.model.DeeplinkRequest;
import com.taizo.model.DeeplinkSuffix;
import com.taizo.model.EmployerCallModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.Example;
import com.taizo.model.FirebaseShortLink;
import com.taizo.model.InterviewsModel;
import com.taizo.model.JobApplicationModel;
import com.taizo.model.JobsModel;
import com.taizo.model.LeadModel;
import com.taizo.model.Parameters;
import com.taizo.model.To;
import com.taizo.model.WAAlert;
import com.taizo.model.WABodyValues;
import com.taizo.model.WAButtonValues;
import com.taizo.model.WADetails;
import com.taizo.model.WAParameters;
import com.taizo.model.WARecipient;
import com.taizo.model.WATemplate;
import com.taizo.model.WebJobsModel;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.JobRepository;
import com.taizo.repository.WebJobsRepository;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.service.JobService;
import com.taizo.service.WAAlertService;
import com.taizo.utils.FreeMarkerUtils;

import freemarker.template.TemplateException;

@CrossOrigin
@RestController
@RequestMapping("/webEmployer")
public class LandingPageController {

	@Autowired
	AmazonSESMailUtil amazonSESMailUtil;

	@Autowired
	private FreeMarkerUtils freeMarkerUtils;

	@Autowired
	WebJobsRepository webJobsRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	JobService jobService;
	
	@Value("${firebase.js.api.key}")
	private String firebaseJSApiKey;

	@Value("${firebase.deeplink.js.hostname}")
	private String firebaseJSHost;

	@Value("${firebase.jsapp.package.name}")
	private String firebaseJSPackage;

	@Autowired
	WAAlertService waAlertService;

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

	@PostMapping(value = "/firebase")
	public Object getFirebaseJob() {

		String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseEmpApiKey;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		DeeplinkRequest ex = new DeeplinkRequest();
		ex.setLongDynamicLink(
				firebaseEmpHost + "/?link=" + firebaseEmpHost + "/candidateDetails/228/11&apn=" + firebaseEmpPackage);

		// System.out.println(ex.getLongDynamicLink());
		DeeplinkSuffix c = new DeeplinkSuffix();
		c.setOption("UNGUESSABLE");

		String jsonString = new com.google.gson.Gson().toJson(ex);

		HttpEntity<String> request = new HttpEntity<String>(jsonString.toString(), headers);

		RestTemplate restTemplate = new RestTemplate();

		FirebaseShortLink response = restTemplate.postForObject(url, request, FirebaseShortLink.class);

		return response;

	}

	@GetMapping(value = "/whatsappJSAlert")
	public List<CandidateModel> getJSAlert() {
		List<CandidateModel> fresherResult = null;
		fresherResult = candidateRepository.getJSAlert();
		
		Optional<JobsModel> job = jobRepository.findById(1140);
		Optional<EmployerModel> emp = employerRepository.findById(job.get().getEmployerId());
		
		System.out.println(fresherResult.size());

		sendFreshersJobAlert(fresherResult, job.get(), emp.get());
		
		

		return fresherResult;
	}

	private void sendFreshersJobAlert(List<CandidateModel> fresherResult, JobsModel job,
			EmployerModel empData) {
		// TODO Auto-generated method stub
		if (!fresherResult.isEmpty()) {
			String jobLink = getJobLink(job.getId());
			for (CandidateModel cm : fresherResult) {

				String salary = String.valueOf(job.getMaxSalary());
				String sal = getSalaryByLanKey(cm.getLanguageKey(),salary);
				
				String loc = job.getJobLocation();
				if (job.getArea() != null) {
					loc = job.getArea() + ", " + job.getJobLocation();
				}
				
				String newstring = getPostedDate(job.getJobPostedTime());
				
				HashMap<String, String> d = new HashMap<>();
				d.put("mn", "91" + String.valueOf(cm.getWhatsappNumber()));
				d.put("name", cm.getFirstName());
				d.put("jobCategory", job.getJobCategory());
				d.put("city", loc);
				d.put("salary", sal);
				d.put("companyName", job.getCompanyName());
				d.put("exp", String.valueOf(job.getJobExp()));
				d.put("industry", job.getIndustry());
				d.put("postedDate", newstring);
				d.put("jobLink", jobLink);
				d.put("languageKey", cm.getLanguageKey());
				
				waAlertService.sendJobAlert(d);
			}
		}
		
	}
	
	private String getPostedDate(Date date) {
		// TODO Auto-generated method stub
		String nDate = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
			nDate = dateFormat.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nDate;
	}
	
	private String getSalaryByLanKey(String languageKey, String sal) {
		// TODO Auto-generated method stub
		if (languageKey == null) {
			sal = sal + " Maximum";
		} else if (languageKey.equalsIgnoreCase("ta")) {
			sal = sal + " வரை";
		} else if (languageKey.equalsIgnoreCase("hi")) {
			sal = sal + " तक";
		} else {
			sal = sal + " Maximum";
		}
		return sal;
	}

	public String getJobLink(int jobId) {
		String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseJSApiKey;

			DeeplinkRequest dl = new DeeplinkRequest();
			dl.setLongDynamicLink(firebaseJSHost + "/?link=" + firebaseJSHost + "/jobDetails/" + 0 + "/"
					+ jobId + "&apn=" + firebaseJSPackage);

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

	@GetMapping(value = "/whatsappEmpAlert")
	public Object getWeJob() {
		List<Map<String, Object>> results = employerRepository.sendWhatsappAlert();

		if (!results.isEmpty()) {

			for (Map<String, Object> cm : results) {

				int empId = (Integer) cm.get("id");
				Optional<EmployerModel> emp = employerRepository.findById(empId);
				EmployerModel empData = emp.get();

				String cn = null;
				String cname = null;
				String name = String.valueOf(empData.getCompanyName());
				String mn = String.valueOf(empData.getMobileNumber());

				if (!name.equalsIgnoreCase("null")) {
					cn = "*" + name + "*";
					cname = name;
				} else {
					cn = "User";
					cname = "User";
				}

				/*
				 * WAAlert ex = new WAAlert(); ex.setChannelId(channelId); WARecipient p = new
				 * WARecipient(); p.setPhone("91" + mn); ex.setRecipient(p); WADetails wa = new
				 * WADetails(); wa.setType("template"); WATemplate t = new WATemplate();
				 * t.setTemplateName("employer_alert");
				 * 
				 * WABodyValues b = new WABodyValues(); b.setCompanyName(cn); //
				 * b.setPricingpageWebLink("https://web.taizo.in/console/pricing#employer"); //
				 * b.setPlanPageLink("https://emp.taizo.in/mCHxRLnhwvHkDvLY6");
				 * t.setBodyValues(b);
				 * 
				 * wa.setTemplate(t); ex.setWhatsapp(wa);
				 * 
				 * String jsonString = new com.google.gson.Gson().toJson(ex);
				 * 
				 * HttpHeaders headers = new HttpHeaders();
				 * headers.setContentType(MediaType.APPLICATION_JSON); headers.add("apiKey",
				 * apiKey); headers.add("apiSecret", apiSecret);
				 * 
				 * HttpEntity<String> request = new HttpEntity<String>(jsonString.toString(),
				 * headers);
				 * 
				 * RestTemplate restTemplate = new RestTemplate();
				 * 
				 * try { Object c = restTemplate.postForObject(campaignUrl, request,
				 * Object.class); } catch (Exception e) { System.out.println(e.getMessage()); }
				 * System.out.println(cn);
				 */

				HashMap<String, String> emailDataHM = new HashMap<>();
				emailDataHM.put("CompanyName", cname != null ? cname : "");

				String message = null;
				try {
					message = freeMarkerUtils.getHtml1("EmployerAlert.html", emailDataHM);

					ByteArrayOutputStream target = new ByteArrayOutputStream();

					ConverterProperties converterProperties = new ConverterProperties();
					converterProperties.setBaseUri("http://localhost:8000");

					HtmlConverter.convertToPdf(message, target, converterProperties);

					byte[] bytes = target.toByteArray();
					String subject = "Taizo.in க்கு மாண்புமிகு தமிழ்நாடு முதலமைச்சர் அவர்களால் TANSEED 4.0 மானியம் வழங்கப்பட்டுள்ளது";

					amazonSESMailUtil.sendEmailEmpAlert(empData.getEmailId(),subject, message, bytes);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		return results;

	}

	@GetMapping(value = "/gallabox")
	public String testGallabox() {

		/*
		 * HashMap<String, String> data = new HashMap<>(); data.put("mn",
		 * "919952346948"); data.put("name", "Sara"); data.put("jobCategory", "CNC");
		 * data.put("city", "Chennai"); data.put("salary", "10 - 20");
		 * data.put("companyName", "Taizo"); data.put("exp", "1 - 2");
		 * data.put("industry", "Production"); data.put("postedDate", "26 Dec 2022");
		 * data.put("jobLink", "WA8VyoL3kp4hY9aX9");
		 * 
		 * waAlertService.sendJobAlert(data);
		 */

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("apiKey", "63ae7f09424930ac9823ede7");
		headers.add("apiSecret", "62035c3b4ecd4b0eaa0b4c295d3d253d");

		WAAlert ex = new WAAlert();
		ex.setChannelId("63a596580e1f0105bb075afb");
		WARecipient p = new WARecipient();
		p.setPhone("919952346948");
		ex.setRecipient(p);
		WADetails wa = new WADetails();
		wa.setType("template");
		WATemplate t = new WATemplate();
		t.setTemplateName("job_alert");

		WABodyValues b = new WABodyValues();
		b.setName("Sara");
		b.setJobCategory("CNC Operator");
		b.setJobCity("Chennai");
		b.setSalary("10000 - 20000");
		b.setCompanyName("Taizo");
		b.setJobExp("1 - 2");
		b.setIndustry("Production");
		b.setPostedDate("26 Jan 2023");
		t.setBodyValues(b);

		List<WAButtonValues> btnlist = new ArrayList<>();
		WAButtonValues btn = new WAButtonValues();
		btn.setIndex(0);
		btn.setSubType("url");

		WAParameters para = new WAParameters();
		para.setType("text");
		para.setText("WA8VyoL3kp4hY9aX9");
		btn.setParameters(para);
		btnlist.add(btn);

		t.setButtonValues(btnlist);
		wa.setTemplate(t);
		ex.setWhatsapp(wa);

		String jsonString = new com.google.gson.Gson().toJson(ex);

		HttpEntity<String> request = new HttpEntity<String>(jsonString.toString(), headers);

		RestTemplate restTemplate = new RestTemplate();

		try {
			Object c = restTemplate.postForObject("https://server.gallabox.com/devapi/messages/whatsapp", request,
					Object.class);
			return c.toString();
		} catch (Exception e) {
		}

		return null;
	}

	@GetMapping(value = "/webjob/{id}")
	public ResponseEntity<?> getWebJob(@PathVariable("id") int id) throws ResourceNotFoundException {
		Optional<WebJobsModel> optional = webJobsRepository.findById(id);

		// TODO Auto-generated method stub
		if (!optional.isPresent()) {
			throw new ResourceNotFoundException("Job not found.");
		}
		HashMap<String, Object> map = new HashMap<>();
		map.put("code", 200);
		map.put("status", "success");
		map.put("jobDetails", optional.get());
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@PutMapping(path = "/saveWebJobDetails", consumes = "application/json")
	public WebJobsModel updateJobDetails(@RequestBody WebJobsModel jobs) throws ResourceNotFoundException {
		Optional<WebJobsModel> optional = webJobsRepository.findById(jobs.getId());

		// TODO Auto-generated method stub
		if (!optional.isPresent()) {
			throw new ResourceNotFoundException("Job not found.");
		}

		WebJobsModel j = optional.get();
		j.setIndustry(jobs.getIndustry());
		j.setJobCategory(jobs.getJobCategory());
		j.setMinSalary(jobs.getMinSalary());
		j.setMaxSalary(jobs.getMaxSalary());
		j.setJobExp(jobs.getJobExp());
		// j.setJobMaxExp(jobs.getJobExp());
		j.setJobLocationAddr(jobs.getJobLocationAddr());
		j.setJobLatitude(jobs.getJobLatitude());
		j.setJobLongitude(jobs.getJobLongitude());
		j.setJobCountry(jobs.getJobCountry());
		j.setState(jobs.getState());
		j.setJobCity(jobs.getJobCity());
		j.setArea(jobs.getArea());

		j = webJobsRepository.save(j);

		return j;
	}

	@PutMapping(path = "/saveWebJobPersonalization", consumes = "application/json")
	public WebJobsModel webJobPersonalization(@RequestBody WebJobsModel jobs) throws ResourceNotFoundException {
		Optional<WebJobsModel> optional = webJobsRepository.findById(jobs.getId());
		if (!optional.isPresent()) {
			throw new ResourceNotFoundException("Job not found.");

		}
		WebJobsModel existing = optional.get();
		existing.setPersonalization(jobs.getPersonalization());

		// existing.setEmailId(jobs.getEmailId());

		if (jobs.getPersonalization().equalsIgnoreCase("1")) {
			existing.setContactPersonName(jobs.getContactPersonName());
			existing.setMobileNumber(jobs.getMobileNumber());

		} else if (jobs.getPersonalization().equalsIgnoreCase("6")) {
			existing.setStartDate(jobs.getStartDate());
			existing.setEndDate(jobs.getEndDate());
			existing.setStartTime(jobs.getStartTime());
			existing.setEndTime(jobs.getEndTime());
			existing.setDocRequired(jobs.getDocRequired());
			existing.setWaddress(jobs.getWaddress());
			existing.setWalkinLatitude(jobs.getWalkinLatitude());
			existing.setWalkinLongitude(jobs.getWalkinLongitude());
			existing.setMobileNumber(jobs.getMobileNumber());
			existing.setContactPersonName(jobs.getContactPersonName());

		} else {

		}

		existing = webJobsRepository.save(existing);

		return existing;
	}

	@PostMapping(path = "/publishJobPost")
	public JobsModel jobDetails(@RequestParam("emp_id") final int empId, @RequestParam("job_id") final int jobId)
			throws ResourceNotFoundException {
		return jobService.postWebJobDetails(empId, jobId);
	}

	@PostMapping(path = "/saveWebJob")
	public JobsModel saveWebJob(@RequestParam("emp_id") final int empId, @RequestParam("job_id") final int jobId)
			throws ResourceNotFoundException {
		Optional<WebJobsModel> optional1 = webJobsRepository.findById(jobId);
		Optional<EmployerModel> emp = employerRepository.findById(empId);

		WebJobsModel jobs = optional1.get();

		EmployerModel empData = emp.get();

		JobsModel job = new JobsModel();
		job.setJobType("Full Time");
		job.setIndustry(jobs.getIndustry());
		job.setJobCategory(jobs.getJobCategory());
		job.setSalaryCurrency("INR");
		job.setSalary(jobs.getMinSalary());
		job.setMaxSalary(jobs.getMaxSalary());
		job.setJobLocationAddr(jobs.getJobLocationAddr());
		job.setJobLatitude(jobs.getJobLatitude());
		job.setJobLongitude(jobs.getJobLongitude());
		job.setJobCountry(jobs.getJobCountry());
		job.setState(jobs.getState());
		job.setArea(jobs.getArea());
		job.setJobLocation(jobs.getJobCity());
		job.setPersonalization(jobs.getPersonalization());

		job.setJobExp(jobs.getJobExp());
		job.setJobMaxExp(jobs.getJobExp());

		if (jobs.getPersonalization().equalsIgnoreCase("1")) {
			job.setContactPersonName(jobs.getContactPersonName());
			job.setMobileNumber(jobs.getMobileNumber());
			job.setIsViewContactPersonName("true");
			job.setIsViewMobileNumber("true");
			job.setIsViewEmailId("false");

		} else if (jobs.getPersonalization().equalsIgnoreCase("2")) {
			job.setEmailId(jobs.getEmailId());
			job.setIsViewEmailId("true");
			job.setIsViewContactPersonName("false");
			job.setIsViewMobileNumber("false");
		} else if (jobs.getPersonalization().equalsIgnoreCase("6")) {
			job.setWstartDate(jobs.getStartDate());
			job.setWendDate(jobs.getEndDate());
			job.setWstartTime(jobs.getStartTime());
			job.setWendTime(jobs.getEndTime());
			job.setWdocRequired(jobs.getDocRequired());
			job.setWaddress(jobs.getWaddress());
			job.setWalkinLatitude(jobs.getWalkinLatitude());
			job.setWalkinLongitude(jobs.getWalkinLongitude());
			job.setContactPersonName(jobs.getContactPersonName());
			job.setMobileNumber(jobs.getMobileNumber());
			job.setIsViewContactPersonName("true");
			job.setIsViewMobileNumber("true");
			job.setIsViewEmailId("false");

		} else {

		}

		job.setWhatsappNoti(jobs.isWhatsappNoti());
		job.setCompanyName(empData.getCompanyName());
		job.setJobStatus("D");
		job.setFromWeb(true);
		job.setEmployerId(jobs.getEmployerId());

		if (empData.getContactPersonName() == null) {
			empData.setContactPersonName(job.getContactPersonName());
			employerRepository.save(empData);
		}

		try {
			if (empData.getAddress() == null && empData.getAddress().isEmpty()) {
				empData.setAddress(jobs.getJobLocationAddr());
				empData.setLatitude(jobs.getJobLatitude());
				empData.setLongitude(jobs.getJobLongitude());
				empData.setCountry(jobs.getJobCountry());
				empData.setState(jobs.getState());
				empData.setCity(jobs.getJobCity());
				empData.setArea(jobs.getArea());

				employerRepository.save(empData);
			}
		} catch (Exception e) {

		}

		job = jobRepository.save(job);
		return job;
	}

}
