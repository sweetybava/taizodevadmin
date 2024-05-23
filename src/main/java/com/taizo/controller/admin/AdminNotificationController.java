 package com.taizo.controller.admin;

 import com.google.gson.Gson;
 import com.taizo.model.*;
 import com.taizo.repository.*;
 import com.taizo.service.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Lazy;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Sort;
 import org.springframework.http.*;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.client.RestTemplate;

 import java.text.SimpleDateFormat;
 import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminNotificationController {

	@Value("${gallabox.campaign.url}")
	private String campaignUrl;
	@Value("${gallabox.auth.apiKey}")
	private String apiKey;
	@Value("${gallabox.auth.apiSecret}")
	private String apiSecret;
	@Value("${gallabox.channel.id}")
	private String channelId;
	
	@Value("${firebase.emp.api.key}")
	private String firebaseEmpApiKey;
	@Value("${firebase.deeplink.emp.hostname}")
	private String firebaseEmpHost;
	@Value("${firebase.empapp.package.name}")
	private String firebaseEmpPackage;

	@Value("${gallabox.js.jobalert.template.name}")
	private String jobTemplateName;
	@Value("${gallabox.js.jobalert.ta.template.name}")
	private String jobTaTemplateName;
	@Value("${gallabox.js.jobalert.hi.template.name}")
	private String jobHiTemplateName;

	@Value("${gallabox.js.interviewalert.template.name}")
	private String interviewTemplateName;
	
	@Autowired
	InterviewRepository interviewRepository;
	
	@Autowired
	InterviewAddressRepository interviewAddressRepository;
	
	@Autowired
	JobService jobservice;

	@Autowired
	CandidateService candidateservice;

	@Autowired
	AdminNotificationRepository adminNotificationRepository;

	@Autowired
	AdminEmpNotificationRepository adminEmpNotificationRepository;
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	CandidateRepository candidateRepository;
	
	@Autowired
	@Lazy
	NotificationService notificationService;
	
	@Autowired
	@Lazy
	TestNotificationService testNotificationService;

	@Autowired
	WAAlertService waAlertService;
	
	@Value("${spring.profiles.active}")
	private String activeProfile;
	
	@Autowired
	AdminCallNotiRepository adminCallNotiRepository;
	
	@Autowired
	AdminService adminService;

	@GetMapping("/callNotifications")
	public ResponseEntity<HashMap<String, Object>> getAdminCallNotifications(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "50") int size
	) {
	    PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "callTime"));
	    Page<AdminCallNotiModel> adminCallNotis = adminService.getAllAdminCallNotifications(pageable);

	    if (!adminCallNotis.isEmpty()) {
	        HashMap<String, Object> map = new HashMap<>();
	        map.put("status", "success");
	        map.put("message", "success");
	        map.put("code", 200);
	        map.put("data", adminCallNotis);
	        return ResponseEntity.ok(map);
	    } else {
	        HashMap<String, Object> map = new HashMap<>();
	        map.put("code", 400);
	        map.put("message", " Call Notifications Not Found");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
	    }
	}

	
	 
	@GetMapping("/callNotificationById")
	public ResponseEntity<?> adminCallNotificationById(@RequestParam("id") final int id) {
		Optional<AdminCallNotiModel> call =  adminCallNotiRepository.findById(Long.valueOf(id));

		if (call.get()!=null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", call.get());

			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Notification Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	
	@PutMapping("/notificationReadById")
	public ResponseEntity<?> adminNotificationReadById(@RequestParam("id") final int id) {
		Optional<AdminCallNotiModel> call =  adminCallNotiRepository.findById(Long.valueOf(id));

		if (call.get()!=null) {
			AdminCallNotiModel noti = call.get();
			noti.setNotificationRead(true);
			adminCallNotiRepository.save(noti);
			
			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", noti);

			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Notification Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}


	@GetMapping("/jobNotification")
	public ResponseEntity<?> adminJobNotification(@RequestParam("emp_id") final int employerId,
			@RequestParam("job_id") final int jobId) {

		Optional<EmployerModel> optional = employerRepository.findById(employerId);
		Optional<JobsModel> job = jobRepository.findById(jobId);

		if (optional.isPresent()) {

			if (activeProfile.equalsIgnoreCase("prod")) {
				notificationService.sendNotification(job.get(), optional.get());
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", job);

			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/testJobNotification")
	public ResponseEntity<?> adminTestJobNotification(@RequestParam("emp_id") final int employerId,
			@RequestParam("job_id") final int jobId) {

		Optional<EmployerModel> optional = employerRepository.findById(employerId);
		Optional<JobsModel> job = jobRepository.findById(jobId);

		if (optional.isPresent()) {

			if (activeProfile.equalsIgnoreCase("prod")) {
			testNotificationService.sendAdminNotification(job.get(), optional.get());
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", job);

			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	
	@PutMapping(path = "/jobEdit")
	public ResponseEntity<?> verifyKyc(@RequestParam("jid") final int eid,@RequestParam("expiry_date") final String eDate,
			@RequestParam("posted_time") final String PDate) {
		Optional<JobsModel> job = jobRepository.findById(eid);
		if (job.isPresent()) {
			job.get().setExpiryDate(eDate);
			Date currentDate;
			try {
				currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(PDate);
				job.get().setJobPostedTime(currentDate);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			jobRepository.save(job.get());

			return new ResponseEntity<>("Job Details updated", HttpStatus.OK);
		}
		return new ResponseEntity<>("Job Details not updated", HttpStatus.BAD_REQUEST);
	}

	@PostMapping("/notification")
	public AdminNotificationModel createUserMessage(@RequestBody AdminNotificationModel notification) {
		adminNotificationRepository.save(notification);
		return notification;
	}

	@PostMapping("/employerNotification")
	public AdminEmpNotificationModel createEmployerMessage(@RequestBody AdminEmpNotificationModel notification) {
		adminEmpNotificationRepository.save(notification);
		return notification;
	}
	
	@PostMapping("/sendJobAlert")
	public ResponseEntity<String> adminSendJobAlert(@RequestParam String candidateId, @RequestParam String jobId) {
	    // Retrieve the CandidateModel based on the candidateId
	    CandidateModel candidateModel = candidateservice.findById(candidateId);
	    // Retrieve the JobsModel based on the jobId
	    JobsModel jobsModel = jobservice.findById(jobId);
	    // Check if both models are found
	    if (candidateModel != null && jobsModel != null) {
	        // Populate the data HashMap with the necessary values from the models
	        HashMap<String, String> data = new HashMap<>();
	        data.put("mn", "91" + String.valueOf(candidateModel.getMobileNumber()));
	        data.put("name", candidateModel.getFirstName());
	        data.put("jobCategory", jobsModel.getJobCategory());
	        data.put("city", jobsModel.getJobLocation());
	        data.put("salary", String.valueOf(jobsModel.getSalary()));
	        data.put("company_name", jobsModel.getCompanyName());
	        data.put("job_exp", String.valueOf(jobsModel.getJobExp()));
	        data.put("industry", jobsModel.getIndustry());
	        data.put("posted_date", String.valueOf(jobsModel.getJobPostedTime()));
	        // Check if the jobLink is not null before adding it to the data HashMap
	        if (jobsModel.getDeeplink() != null) {
	            data.put("jobLink", jobsModel.getDeeplink());
	        } else {
	            // Handle the case when the jobLink is null
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Job link is missing");
	        }
	        
	        sendJobAlert(data);
	        return ResponseEntity.ok("Job alert sent successfully");
	    } else {
	        // Handle the case when either the CandidateModel or JobsModel is not found
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Candidate or Jobs not found");
	    }
	}
		public void sendJobAlert(HashMap<String, String> data) {
			WAAlert ex = new WAAlert();
			ex.setChannelId(channelId);
			WARecipient p = new WARecipient(); 
			p.setPhone(data.get("mn"));
			ex.setRecipient(p);
			WADetails wa = new WADetails();
			wa.setType("template");
			WATemplate t = new WATemplate();
			t.setTemplateName("job_alert");
			
			String link = data.get("jobLink");
			String jobLink = link.substring(link.lastIndexOf("/") + 1);
			
			WABodyValues b = new WABodyValues();
			b.setName("*" + data.get("name") + "*");
			b.setJobCategory("*" + data.get("jobCategory") + "*");
			b.setJobCity("*" + data.get("city") + "*");
			b.setSalary("*" + data.get("salary") + "*");
			b.setCompanyName("*" + data.get("company_name") + "*");
			b.setJobExp("*" + data.get("job_exp") + "*");
			b.setIndustry("*" + data.get("industry") + "*");
			b.setPostedDate("*" + data.get("posted_date") + "*"); 
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
			
			String jsonString = new Gson().toJson(ex);
			sendMessage(jsonString);
		}
		private void sendMessage(String jsonString) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("apiKey", "63ae7f09424930ac9823ede7");
			headers.add("apiSecret", "62035c3b4ecd4b0eaa0b4c295d3d253d");
			HttpEntity<String> request = new HttpEntity<>(jsonString, headers);
			RestTemplate restTemplate = new RestTemplate();
			try {
				restTemplate.postForObject("https://server.gallabox.com/devapi/messages/whatsapp", request, Object.class);
			} catch (Exception e) {
				// Handle the exception
			}
		}
		
}
