package com.taizo.controller.employer;

import java.lang.reflect.Field; 
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.taizo.model.*;
import com.taizo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.taizo.controller.gallabox.ExotelCallController;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.service.CloudWatchLogService;
import com.taizo.service.JobService;
import com.taizo.service.NotificationService;
import com.taizo.utils.TupleStore;

@CrossOrigin
@RestController
@RequestMapping("/employer")
public class  NewJobPostController {

	@Autowired
	JobService jobService;

	@Autowired
	@Lazy
	NotificationService notificationService;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	LeadRepository leadRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	PlansRepository plansRepository;

	@Autowired
	CloudWatchLogService cloudWatchLogService;

	@Autowired
	EmpActivityRepository empActivityRepository;
	
	@Autowired
	EmpPlacementPlanDetailsRepository empPlacementPlanDetailsRepository;

	@PersistenceContext
	EntityManager em;

	@Autowired
	FullTimeGroupingRepository fullTimeGroupingRepository;

	@Autowired
	IndustryRepository industryRepository;

	@Autowired
	EmployerJobPrescreeningQuestionsRepository employerJobPrescreeningQuestionsRepository;

	@Autowired
	CfgEmpPrescreeningQuestionsRepository cfgEmpPrescreeningQuestionsRepository;
	
	@Autowired
	ExotelCallController exotelCallController;

	@Autowired
	EmployerTimelineRepository employerTimelineRepository;

	@Value("${spring.profiles.active}")
	private String activeProfile;
	
	@Value("${firebase.js.api.key}")
	private String firebaseJSApiKey;

	@Value("${firebase.deeplink.js.hostname}")
	private String firebaseJSHost;

	@Value("${firebase.jsapp.package.name}")
	private String firebaseJSPackage;
	
	@GetMapping("/getJobs")
	public ResponseEntity<?> getEmployerJobs(@RequestParam("emp_id") final int employerId,
			@RequestParam("status") final String status, @RequestParam("page") int pgNo,
			@RequestParam("size") int length) throws ResourceNotFoundException {


		if (status.equalsIgnoreCase("C")) {

			Page<JobsModel> Cjobs = jobService.findEmployerClosedJobs(employerId,status, pgNo ,length);
			HashMap<String, Object> hm = new HashMap<>();
			hm.put("data", Cjobs.getContent());
			hm.put("start", pgNo);
			hm.put("recordsTotal", Cjobs.getTotalElements());
			hm.put("recordsFiltered", Cjobs.getContent().size());
			return new ResponseEntity<>(hm, HttpStatus.OK);	
		} else if (status.equalsIgnoreCase("D")) {

			Page<JobsModel> Cjobs = jobService.findEmployerDraftJobs(employerId,status, pgNo ,length);
			HashMap<String, Object> hm = new HashMap<>();
			hm.put("data", Cjobs.getContent());
			hm.put("start", pgNo);
			hm.put("recordsTotal", Cjobs.getTotalElements());
			hm.put("recordsFiltered", Cjobs.getContent().size());
			return new ResponseEntity<>(hm, HttpStatus.OK);	
		} else {
			Page<JobsModel> jobs = jobService.findEmployerJobs(employerId, status, pgNo ,length);

			HashMap<String, Object> hm = new HashMap<>();
			hm.put("data", jobs.getContent());
			hm.put("start", pgNo);
			hm.put("recordsTotal", jobs.getTotalElements());
			hm.put("recordsFiltered", jobs.getContent().size());
			return new ResponseEntity<>(hm, HttpStatus.OK);		}

	}
	
	@PostMapping(path = "/staffingJobPost", consumes = "application/json")
	public ResponseEntity<?> StaffingJobPost(@RequestBody JobsModel job) {

		Optional<EmployerModel> optional = employerRepository.findById(job.getEmployerId());
		if (optional.isPresent()) {
			EmployerModel empData = optional.get();
			if (!empData.isDeactivated()) {

			job.setFromWeb(false);
			job.setInActive(false);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String date = dtf.format(now);
			Date currentDate;
			try {
				currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
				job.setJobPostedTime(currentDate);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			Optional<PlansModel> empPlan1 = plansRepository.findById(empData.getPlan());
			PlansModel plan1 = empPlan1.get();
			int jobPostValidity = plan1.getJobPostValidity();

			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DATE, jobPostValidity);
			String output = sdf1.format(c.getTime());

			job.setExpiryDate(output);
			job.setCanResponseCount(100);
			job.setTotalCanResponse(100);
			job = jobRepository.save(job);
			getJobLink(job.getId());

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", job);
			return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "Account Deactivated");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	private void getJobLink(int id) {
		// TODO Auto-generated method stub
		String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseJSApiKey;

		DeeplinkRequest dl = new DeeplinkRequest();
		dl.setLongDynamicLink(firebaseJSHost + "/?link=" + firebaseJSHost + "/jobDetails/" + 0 + "/" + id + "&apn="
				+ firebaseJSPackage);

		DeeplinkSuffix c1 = new DeeplinkSuffix();
		c1.setOption("UNGUESSABLE");

		String json = new com.google.gson.Gson().toJson(dl);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> req = new HttpEntity<String>(json.toString(), headers);

		RestTemplate restTemp = new RestTemplate();
		FirebaseShortLink response = null;
		try {
			response = restTemp.postForObject(url, req, FirebaseShortLink.class);
			Optional<JobsModel> job = jobRepository.findById(id);
			job.get().setDeeplink(response.getShortLink());
			jobRepository.save(job.get());
		} catch (Exception e1) {
		}
	}

	@PostMapping(path = "/newJobPost", consumes = "application/json")
	public ResponseEntity<?> newJobPost(@RequestBody JobsModel jobs) {

		Optional<EmployerModel> optional = employerRepository.findById(jobs.getEmployerId());

		if (optional.isPresent()) {

			EmployerModel empData = optional.get();
			if(!empData.isDeactivated()) {
			int planId = empData.getPlan();
			Optional<PlansModel> empPlan = plansRepository.findById(planId);
			PlansModel plan = empPlan.get();

			JobsModel job = new JobsModel();
			job.setJobType("Full Time");
			job.setIndustry(jobs.getIndustry());
			job.setJobCategory(jobs.getJobCategory());
			job.setSalaryCurrency("INR");
			job.setSalary(jobs.getSalary());
			job.setMaxSalary(jobs.getMaxSalary());
			job.setJobLocationAddr(jobs.getJobLocationAddr());
			job.setJobLatitude(jobs.getJobLatitude());
			job.setJobLongitude(jobs.getJobLongitude());
			job.setJobCountry(jobs.getJobCountry());
			job.setState(jobs.getState());
			job.setArea(jobs.getArea());
			job.setJobLocation(jobs.getJobLocation());
			job.setPersonalization(jobs.getPersonalization());
			
			Integer adminId = getAdminIdForCity(empData.getCity());
		    job.setAssignTo(adminId);

			job.setJobExp(jobs.getJobExp());
			
			int ex = jobs.getJobExp();
			int maxEx = jobs.getJobMaxExp();

			int max = 0;
			if(ex>=1 && ex<3) {
				max=3;
			}else if(ex==0 && maxEx==1) {
				max=1;
			}else if(ex==0 && maxEx==0) {
				max=0;
			}else {
				max=20;
			}
			
			job.setJobMaxExp(max);
			
			job.setQualification(jobs.getQualification());

				job.setContactPersonName(jobs.getContactPersonName());
				job.setMobileNumber(jobs.getMobileNumber());
				job.setWhatsappNumber(jobs.getWhatsappNumber());
				job.setAlternateMobileNumber(jobs.getAlternateMobileNumber());
				job.setIsViewContactPersonName("true");
				job.setIsViewMobileNumber("true");
				job.setIsViewEmailId("false");
			
			job.setWorkHours(jobs.getWorkHours());
			job.setOt(jobs.getOt());
			job.setShiftType(jobs.getShiftType());
			job.setShiftTimings(jobs.getShiftTimings());
			job.setBenefits(jobs.getBenefits());
			if(jobs.getWorkHours()!=null || jobs.getOt()!=null || jobs.getShiftType()!=null || jobs.getBenefits()!=null) {
			job.setAdditionalDetailsFilled(true);
			}
			job.setKeyskills(jobs.getKeyskills());
			job.setGender(jobs.getGender());
			job.setWhatsappNoti(jobs.isWhatsappNoti());
			job.setCompanyName(empData.getCompanyName());
			job.setJobStatus(jobs.getJobStatus());
			job.setEmployerId(jobs.getEmployerId());
			job.setInActive(false);
			job.setCanResponseCount(plan.getProfiles());
			job.setTotalCanResponse(plan.getProfiles());

			if(plan.getId()==5) {
				job.setFreetrialJob(true);
			}

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String date = dtf.format(now);
			Date currentDate;
			try {
				currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
				job.setJobPostedTime(currentDate);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			int jobPostValidity = plan.getJobPostValidity();

			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DATE, jobPostValidity);
			String output = sdf1.format(c.getTime());

			job.setExpiryDate(output);

			if (empData.getContactPersonName() == null) {
				empData.setContactPersonName(job.getContactPersonName());
				employerRepository.save(empData);
			}

			if (empData.getMobileNumber() == 0) {
				try {
					empData.setMobileNumber(Long.parseLong(job.getMobileNumber()));
					empData.setMobileCountryCode("91");
					employerRepository.save(empData);
					Optional<LeadModel> em = leadRepository.findByMobileNumber(Long.valueOf(job.getMobileNumber()));
					if (em.isPresent()) {
						LeadModel l = em.get();
						l.setRegisteredInApp(true);
						leadRepository.save(l);
					}
				} catch (Exception e) {

				}
			}

			try {
				if(jobs.getAlternateMobileNumber()!=null && !jobs.getAlternateMobileNumber().isEmpty()) {
					if(empData.getAlternateMobileNumber()==null && empData.getAlternateMobileNumber().isEmpty()) {
					empData.setAlternateMobileNumber(jobs.getAlternateMobileNumber());
					employerRepository.save(empData);
					}
				}
				if (jobs.getJobLatitude() != null && !jobs.getJobLatitude().isEmpty()) {
					empData.setAddress(jobs.getJobLocationAddr());
					empData.setLatitude(jobs.getJobLatitude());
					empData.setLongitude(jobs.getJobLongitude());
					empData.setCountry(jobs.getJobCountry());
					empData.setState(jobs.getState());
					empData.setArea(jobs.getArea());
					empData.setCity(jobs.getJobLocation());
					jobs.setAssignTo(empData.getAssignTo());
					
					employerRepository.save(empData);
				}

			} catch (Exception e) {

			}
			jobRepository.save(job);

			int jobUniqID = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);

			job.setEmpJobId(String.valueOf(jobUniqID));
			job = jobRepository.save(job);

			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(job.getEmployerId());
			EA.setActivity("Your job " + "<b>" + job.getJobCategory() + "</b>" + " has been published!");
			empActivityRepository.save(EA);

			Integer jobUpdateCount = optional.get().getPlanJobCount();
			jobUpdateCount -= 1;
			optional.get().setPlanJobCount(jobUpdateCount);
			employerRepository.save(optional.get());
			
			String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseJSApiKey;
			
			DeeplinkRequest dl = new DeeplinkRequest();
			dl.setLongDynamicLink(firebaseJSHost + "/?link=" + firebaseJSHost + "/jobDetails/" + 0 + "/"
					+ job.getId() + "&apn=" + firebaseJSPackage);

			DeeplinkSuffix c1 = new DeeplinkSuffix();
			c1.setOption("UNGUESSABLE");

			String json = new com.google.gson.Gson().toJson(dl);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> req = new HttpEntity<String>(json.toString(), headers);

			RestTemplate restTemp = new RestTemplate();
			FirebaseShortLink response = null;
			try {
				response = restTemp.postForObject(url, req, FirebaseShortLink.class);
				job.setDeeplink(response.getShortLink());
				job = jobRepository.save(job);
			} catch (Exception e) {

			}
			
				if (activeProfile.equalsIgnoreCase("prod")) {
				String jobStatus = "Paid";
				String eventName = "Job Published";
				if(plan.getId()==5) {
					jobStatus = "Free Trial";
					eventName = "Free Trial Published";
				}
				
				HashMap<String, String> data1 = new HashMap<>();
				data1.put("Event Name", "Job Alert");
				data1.put("Event Type", eventName);
				data1.put("Type", "Job");
				data1.put("Company Name", job.getCompanyName());
				data1.put("Contact Person Name", job.getContactPersonName());
				data1.put("Position", job.getJobCategory());
				data1.put("Experience", String.valueOf(job.getJobExp())+" to "+String.valueOf(job.getJobMaxExp()));
				data1.put("Source", "App");
				data1.put("Location", job.getJobLocation() != null ? job.getJobLocation() : "");
				data1.put("Mobile Number", String.valueOf(job.getMobileNumber()));
				data1.put("Job Status", jobStatus);
				data1.put("ID Type", "Job ID");
				data1.put("ID", String.valueOf(job.getId()));

				exotelCallController.connectToAgent("+91" + String.valueOf(job.getMobileNumber()),"Emp",data1);
				
				//notificationService.sendNotification(job, optional.get());
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
		map.put("message", "Account Deactivated");
		return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	

	@PostMapping(path = "/draftJob", consumes = "application/json")
	public ResponseEntity<?> newDraftJob(@RequestBody JobsModel jobs) {

		Optional<EmployerModel> optional = employerRepository.findById(jobs.getEmployerId());

		if (optional.isPresent()) {
			EmployerModel empData = optional.get();

			JobsModel job = new JobsModel();
			job.setJobType("Full Time");
			job.setIndustry(jobs.getIndustry());
			job.setJobCategory(jobs.getJobCategory());
			job.setSalaryCurrency("INR");
			job.setSalary(jobs.getSalary());
			job.setMaxSalary(jobs.getMaxSalary());

			job.setJobLocationAddr(empData.getAddress());
			job.setJobLatitude(empData.getLatitude());
			job.setJobLongitude(empData.getLongitude());
			job.setJobCountry(empData.getCountry());
			job.setState(empData.getState());
			job.setArea(empData.getArea());
			job.setJobLocation(empData.getCity());
			job.setQualification(jobs.getQualification());

			job.setPersonalization("1");

			job.setJobExp(jobs.getJobExp());
			int e = jobs.getJobExp();
			int max = 0;
			int maxEx = jobs.getJobMaxExp();
			if(e>=1 && e<3) {
				max=3;
			}else if(e==0 && maxEx==1) {
				max=1;
			}else if(e==0 && maxEx==0) {
				max=0;
			}else {
				max=20;
			}

			job.setJobMaxExp(max);
			job.setContactPersonName(jobs.getContactPersonName());
			job.setMobileNumber(jobs.getMobileNumber());
			job.setWhatsappNumber(jobs.getWhatsappNumber());
			job.setAlternateMobileNumber(jobs.getAlternateMobileNumber());
			job.setIsViewContactPersonName("true");
			job.setIsViewMobileNumber("true");
			job.setIsViewEmailId("false");

			job.setWorkHours(jobs.getWorkHours());
			job.setOt(jobs.getOt());
			job.setShiftType(jobs.getShiftType());
			// job.setShiftTimings(jobs.getShiftTimings());
			job.setBenefits(jobs.getBenefits());
			if (jobs.getWorkHours() != null || jobs.getOt() != null || jobs.getShiftType() != null
					|| jobs.getBenefits() != null) {
				job.setAdditionalDetailsFilled(true);
			}
			job.setKeyskills(jobs.getKeyskills());
			job.setGender(jobs.getGender());
			job.setWhatsappNoti(jobs.isWhatsappNoti());
			job.setCompanyName(empData.getCompanyName());
			job.setJobStatus("D");
			job.setEmployerId(jobs.getEmployerId());
			job.setFromWeb(false);
			job.setInActive(false);
			job.setDraftJob(true);

			jobRepository.save(job);

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

	@PutMapping(path = "/updateDraftJob", consumes = "application/json")
	public ResponseEntity<?> newUpdateDraftJob(@RequestParam("used_free_trial") final boolean usedFreeTrail,
			@RequestBody JobsModel jobs) throws ResourceNotFoundException {

		Optional<EmployerModel> optional = employerRepository.findById(jobs.getEmployerId());

		if (optional.isPresent()) {
			EmployerModel empData = optional.get();

			Optional<JobsModel> jobs1 = jobRepository.findById(jobs.getId());

			if (!optional.isPresent()) {
				throw new ResourceNotFoundException("Job not found.");
			}

			JobsModel job = jobs1.get();

			job.setJobType("Full Time");
			job.setIndustry(jobs.getIndustry());
			job.setJobCategory(jobs.getJobCategory());
			job.setSalaryCurrency("INR");
			job.setSalary(jobs.getSalary());
			job.setMaxSalary(jobs.getMaxSalary());

			job.setJobLocationAddr(empData.getAddress());
			job.setJobLatitude(empData.getLatitude());
			job.setJobLongitude(empData.getLongitude());
			job.setJobCountry(empData.getCountry());
			job.setState(empData.getState());
			job.setArea(empData.getArea());
			job.setJobLocation(empData.getCity());
			job.setQualification(jobs.getQualification());

			job.setPersonalization("1");

			job.setJobExp(jobs.getJobExp());
			int e = jobs.getJobExp();
			int max = 0;
			int maxEx = jobs.getJobMaxExp();
			if(e>=1 && e<3) {
				max=3;
			}else if(e==0 && maxEx==1) {
				max=1;
			}else if(e==0 && maxEx==0) {
				max=0;
			}else {
				max=20;
			}

			job.setJobMaxExp(max);
			job.setContactPersonName(jobs.getContactPersonName());
			job.setMobileNumber(jobs.getMobileNumber());
			job.setWhatsappNumber(jobs.getWhatsappNumber());
			job.setAlternateMobileNumber(jobs.getAlternateMobileNumber());
			job.setIsViewContactPersonName("true");
			job.setIsViewMobileNumber("true");
			job.setIsViewEmailId("false");

			job.setWorkHours(jobs.getWorkHours());
			job.setOt(jobs.getOt());
			job.setShiftType(jobs.getShiftType());
			job.setBenefits(jobs.getBenefits());
			if (jobs.getWorkHours() != null || jobs.getOt() != null || jobs.getShiftType() != null
					|| jobs.getBenefits() != null) {
				job.setAdditionalDetailsFilled(true);
			}
			job.setKeyskills(jobs.getKeyskills());
			job.setGender(jobs.getGender());
			job.setWhatsappNoti(jobs.isWhatsappNoti());
			job.setCompanyName(empData.getCompanyName());
			job.setJobStatus(jobs.getJobStatus());
			job.setEmployerId(jobs.getEmployerId());
			job.setFromWeb(false);
			job.setInActive(false);

			if (jobs.getJobStatus().equalsIgnoreCase("O")) {
				if(!empData.isDeactivated()) {

				if (usedFreeTrail) {
					Optional<PlansModel> empPlan = plansRepository.findById(5);
					PlansModel plan = empPlan.get();
					int expDays = plan.getPlanValidity();

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Calendar c = Calendar.getInstance();
					c.setTime(new Date());
					c.add(Calendar.DATE, expDays);
					String output = sdf.format(c.getTime());

					// empData.setPaymentStatus("Paid");
					empData.setPlan(5);
					empData.setExpiryDate(output);
					empData.setUsedFreeTrial("Yes");
					empData.setPlanJobCount(plan.getActiveJobs());
					empData.setFreePlanExpiryDate(output);

					employerRepository.save(empData);
					
					job.setFreetrialJob(true);

				}
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime now = LocalDateTime.now();
				String date = dtf.format(now);
				Date currentDate;
				try {
					currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
					job.setJobPostedTime(currentDate);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				Optional<PlansModel> empPlan1 = plansRepository.findById(empData.getPlan());
				PlansModel plan1 = empPlan1.get();
				int jobPostValidity = plan1.getJobPostValidity();

				SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.DATE, jobPostValidity);
				String output = sdf1.format(c.getTime());

				job.setExpiryDate(output);
				job.setCanResponseCount(plan1.getProfiles());
				job.setTotalCanResponse(plan1.getProfiles());

				if (empData.getContactPersonName() == null) {
					empData.setContactPersonName(job.getContactPersonName());
					employerRepository.save(empData);
				}

				if (empData.getMobileNumber() == 0) {
					try {
						empData.setMobileNumber(Long.parseLong(job.getMobileNumber()));
						empData.setMobileCountryCode("91");
						employerRepository.save(empData);
						Optional<LeadModel> em = leadRepository.findByMobileNumber(Long.valueOf(job.getMobileNumber()));
						if (em.isPresent()) {
							LeadModel l = em.get();
							l.setRegisteredInApp(true);
							leadRepository.save(l);
						}
					} catch (Exception e1) {

					}
				}

				try {
					if (jobs.getAlternateMobileNumber() != null && !jobs.getAlternateMobileNumber().isEmpty()) {
						if (empData.getAlternateMobileNumber() == null
								&& empData.getAlternateMobileNumber().isEmpty()) {
							empData.setAlternateMobileNumber(jobs.getAlternateMobileNumber());
							employerRepository.save(empData);
						}
					}
					if (jobs.getJobLatitude() != null && !jobs.getJobLatitude().isEmpty()) {
						empData.setAddress(jobs.getJobLocationAddr());
						empData.setLatitude(jobs.getJobLatitude());
						empData.setLongitude(jobs.getJobLongitude());
						empData.setCountry(jobs.getJobCountry());
						empData.setState(jobs.getState());
						empData.setArea(jobs.getArea());
						empData.setCity(jobs.getJobLocation());
						employerRepository.save(empData);
					}
				} catch (Exception e2) {

				}
				jobRepository.save(job);

				int jobUniqID = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);

				job.setEmpJobId(String.valueOf(jobUniqID));
				job = jobRepository.save(job);

				EmployerActivityModel EA = new EmployerActivityModel();
				EA.setEmpId(job.getEmployerId());
				EA.setActivity("Your job " + "<b>" + job.getJobCategory() + "</b>" + " has been published!");
				empActivityRepository.save(EA);

				Integer jobUpdateCount = optional.get().getPlanJobCount();
				jobUpdateCount -= 1;
				optional.get().setPlanJobCount(jobUpdateCount);
				employerRepository.save(optional.get());
				
				String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseJSApiKey;
				
				DeeplinkRequest dl = new DeeplinkRequest();
				dl.setLongDynamicLink(firebaseJSHost + "/?link=" + firebaseJSHost + "/jobDetails/" + 0 + "/"
						+ job.getId() + "&apn=" + firebaseJSPackage);

				DeeplinkSuffix c1 = new DeeplinkSuffix();
				c1.setOption("UNGUESSABLE");

				String json = new com.google.gson.Gson().toJson(dl);

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);

				HttpEntity<String> req = new HttpEntity<String>(json.toString(), headers);

				RestTemplate restTemp = new RestTemplate();
				FirebaseShortLink response = null;
				try {
					response = restTemp.postForObject(url, req, FirebaseShortLink.class);
					job.setDeeplink(response.getShortLink());
					job = jobRepository.save(job);
				} catch (Exception e1) {

				}

				if (activeProfile.equalsIgnoreCase("prod")) {
				String jobStatus = "Paid";
				String eventName = "Job Published";
				if(usedFreeTrail) {
					jobStatus = "Free Trial";
					eventName = "Free Trial Published";
				}
				
				HashMap<String, String> data1 = new HashMap<>();
				data1.put("Event Name", "Job Alert");
				data1.put("Event Type", eventName);
				data1.put("Type", "Job");
				data1.put("Company Name", job.getCompanyName());
				data1.put("Contact Person Name", job.getContactPersonName());
				data1.put("Position", job.getJobCategory());
				data1.put("Experience", String.valueOf(job.getJobExp())+" to "+String.valueOf(job.getJobMaxExp()));
				data1.put("Location", job.getJobLocation() != null ? job.getJobLocation() : "");
				data1.put("Source", "App");
				data1.put("Mobile Number", String.valueOf(job.getMobileNumber()));
				data1.put("Job Status", jobStatus);
				data1.put("ID Type", "Job ID");
				data1.put("ID", String.valueOf(job.getId()));

				exotelCallController.connectToAgent("+91" + String.valueOf(job.getMobileNumber()),"Emp",data1);

					//notificationService.sendNotification(job, optional.get());
				}
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 400);
					map.put("message", "Account Deactivated");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}
			} else {
				jobRepository.save(job);
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

	@PostMapping(path = "/jobDetails", consumes = "application/json")
	public ResponseEntity<?> jobDetailss(@RequestBody JobsModel jobs) {

		Optional<EmployerModel> optional = employerRepository.findById(jobs.getEmployerId());

		if (optional.isPresent()) {

			JobsModel j = new JobsModel();
			j.setJobType("Full Time");
			j.setIndustry(jobs.getIndustry());
			j.setJobCategory(jobs.getJobCategory());
			j.setSalaryCurrency("INR");
			j.setSalary(jobs.getSalary());
			j.setMaxSalary(jobs.getMaxSalary());
			j.setJobLocationAddr(jobs.getJobLocationAddr());
			j.setJobLatitude(jobs.getJobLatitude());
			j.setJobLongitude(jobs.getJobLongitude());
			j.setJobCountry(jobs.getJobCountry());
			j.setState(jobs.getState());
			j.setJobLocation(jobs.getJobLocation());
			j.setJobStatus(jobs.getJobStatus());
			j.setEmployerId(jobs.getEmployerId());
			j.setInActive(false);

			int jobUniqID = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);
			j.setEmpJobId(String.valueOf(jobUniqID));

			jobRepository.save(jobs);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping(path = "/updateJobDetails", consumes = "application/json")
	public ResponseEntity<?> updateJobDetails(@RequestBody JobsModel jobs) {

		Optional<JobsModel> optional = jobRepository.findById(jobs.getId());

		if (optional.isPresent()) {

			JobsModel j = optional.get();
			j.setIndustry(jobs.getIndustry());
			j.setJobCategory(jobs.getJobCategory());
			j.setSalary(jobs.getSalary());
			j.setMaxSalary(jobs.getMaxSalary());
			j.setJobLocationAddr(jobs.getJobLocationAddr());
			j.setJobLatitude(jobs.getJobLatitude());
			j.setJobLongitude(jobs.getJobLongitude());
			j.setJobCountry(jobs.getJobCountry());
			j.setState(jobs.getState());
			j.setJobLocation(jobs.getJobLocation());
			j.setJobStatus(jobs.getJobStatus());
			j.setInActive(false);

			jobRepository.save(jobs);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path = "/jobPersonalization", consumes = "application/json")
	public ResponseEntity<?> jobPersonalization(@RequestBody JobsModel jobs) {

		Optional<JobsModel> optional = jobRepository.findById(jobs.getId());

		if (optional.isPresent()) {

			JobsModel existing = optional.get();
			existing.setPersonalization(jobs.getPersonalization());
			existing.setContactPersonName(jobs.getContactPersonName());
			existing.setMobileNumber(jobs.getMobileNumber());
			existing.setEmailId(jobs.getEmailId());
			existing.setWstartDate(jobs.getWstartDate());
			existing.setWendDate(jobs.getWendDate());
			existing.setWstartTime(jobs.getWstartTime());
			existing.setWendTime(jobs.getWendTime());
			existing.setWdocRequired(jobs.getWdocRequired());
			existing.setWaddress(jobs.getWaddress());
			existing.setWalkinLatitude(jobs.getWalkinLatitude());
			existing.setWalkinLongitude(jobs.getWalkinLongitude());
			existing.setJobStatus(jobs.getJobStatus());

			jobRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(path = "/jobPost", consumes = "application/json")
	public ResponseEntity<?> savejobPost(@RequestBody JobsModel jobs) {

		Optional<EmployerModel> optional = employerRepository.findById(jobs.getEmployerId());

		if (optional.isPresent()) {

			EmployerModel empData = optional.get();
			int planId = empData.getPlan();
			Optional<PlansModel> empPlan = plansRepository.findById(planId);
			PlansModel plan = empPlan.get();

			JobsModel job = new JobsModel();
			job.setJobType("Full Time");
			job.setIndustry(jobs.getIndustry());
			job.setJobCategory(jobs.getJobCategory());
			job.setSalaryCurrency("INR");
			job.setSalary(jobs.getSalary());
			job.setMaxSalary(jobs.getMaxSalary());
			job.setJobLocationAddr(jobs.getJobLocationAddr());
			job.setJobLatitude(jobs.getJobLatitude());
			job.setJobLongitude(jobs.getJobLongitude());
			job.setJobCountry(jobs.getJobCountry());
			job.setState(jobs.getState());
			job.setArea(jobs.getArea());
			job.setJobLocation(jobs.getJobLocation());
			
			Integer adminId = getAdminIdForCity(empData.getCity());
		    job.setAssignTo(adminId);;
			
			job.setPersonalization(jobs.getPersonalization());

			job.setJobExp(jobs.getJobExp());
			job.setJobMaxExp(jobs.getJobExp());

			if (jobs.getPersonalization().equalsIgnoreCase("1")) {
				job.setContactPersonName(jobs.getContactPersonName());
				job.setMobileNumber(jobs.getMobileNumber());
				job.setWhatsappNumber(jobs.getWhatsappNumber());
				job.setIsViewContactPersonName("true");
				job.setIsViewMobileNumber("true");
				job.setIsViewEmailId("false");

			} else if (jobs.getPersonalization().equalsIgnoreCase("2")) {
				job.setEmailId(jobs.getEmailId());
				job.setIsViewEmailId("true");
				job.setIsViewContactPersonName("false");
				job.setIsViewMobileNumber("false");
			} else if (jobs.getPersonalization().equalsIgnoreCase("6")) {
				job.setWstartDate(jobs.getWstartDate());
				job.setWendDate(jobs.getWendDate());
				job.setWstartTime(jobs.getWstartTime());
				job.setWendTime(jobs.getWendTime());
				job.setWdocRequired(jobs.getWdocRequired());
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
			job.setJobStatus(jobs.getJobStatus());
			job.setEmployerId(jobs.getEmployerId());
			job.setInActive(false);

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String date = dtf.format(now);
			Date currentDate;
			try {
				currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
				job.setJobPostedTime(currentDate);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			int jobPostValidity = plan.getJobPostValidity();

			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DATE, jobPostValidity);
			String output = sdf1.format(c.getTime());

			job.setExpiryDate(output);
			job.setCanResponseCount(plan.getProfiles());
			job.setTotalCanResponse(plan.getProfiles());


			if (empData.getContactPersonName() == null) {
				empData.setContactPersonName(job.getContactPersonName());
				employerRepository.save(empData);
			}

			if (empData.getMobileNumber() == 0) {
				try {
					empData.setMobileNumber(Long.parseLong(job.getMobileNumber()));
					empData.setMobileCountryCode("91");
					employerRepository.save(empData);
					Optional<LeadModel> em = leadRepository.findByMobileNumber(Long.valueOf(job.getMobileNumber()));
					if (em.isPresent()) {
						LeadModel l = em.get();
						l.setRegisteredInApp(true);
						leadRepository.save(l);
					}
				} catch (Exception e) {

				}
			}

			try {
				if (empData.getAddress() == null && empData.getAddress().isEmpty()) {
					empData.setAddress(jobs.getJobLocationAddr());
					empData.setLatitude(jobs.getJobLatitude());
					empData.setLongitude(jobs.getJobLongitude());
					empData.setCountry(jobs.getJobCountry());
					empData.setState(jobs.getState());
					empData.setArea(jobs.getArea());
					empData.setCity(jobs.getJobLocation());
					employerRepository.save(empData);
				}
			} catch (Exception e) {

			}
			jobRepository.save(job);

			int jobUniqID = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);

			job.setEmpJobId(String.valueOf(jobUniqID));
			job = jobRepository.save(job);

			Integer jobUpdateCount = optional.get().getPlanJobCount();
			jobUpdateCount -= 1;
			optional.get().setPlanJobCount(jobUpdateCount);
			employerRepository.save(optional.get());

			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(job.getEmployerId());
			EA.setActivity("Your job " + "<b>" + job.getJobCategory() + "</b>" + " has been published!");
			empActivityRepository.save(EA);


			if (activeProfile.equalsIgnoreCase("prod")) {
				notificationService.sendNotification(job, empData);
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
	 private Integer getAdminIdForCity(String city) {
		    Map<String, Integer> cityAdminMapping = new HashMap<>();
		    cityAdminMapping.put("Chennai", 2);
		    cityAdminMapping.put("Chengalpattu", 2);
		    cityAdminMapping.put("Coimbatore", 3);
		    cityAdminMapping.put("Hosur", 3);
		    cityAdminMapping.put("Kanchipuram", 2);
		    
		    return cityAdminMapping.getOrDefault(city, 2); 
		}

	@PutMapping(path = "/jobPost", consumes = "application/json")
	public ResponseEntity<?> jobPost(@RequestBody JobsModel jobs) {

		Optional<JobsModel> optional = jobRepository.findById(jobs.getId());

		if (optional.isPresent()) {

			JobsModel existing = optional.get();
			existing.setJobType("Full Time");
			existing.setIndustry(jobs.getIndustry());
			existing.setJobCategory(jobs.getJobCategory());
			existing.setSalaryCurrency("INR");
			existing.setSalary(jobs.getSalary());
			existing.setMaxSalary(jobs.getMaxSalary());
			existing.setJobLocationAddr(jobs.getJobLocationAddr());
			existing.setJobLatitude(jobs.getJobLatitude());
			existing.setJobLongitude(jobs.getJobLongitude());
			existing.setJobCountry(jobs.getJobCountry());
			existing.setState(jobs.getState());
			existing.setJobLocation(jobs.getJobLocation());
			existing.setAssignTo(existing.getAssignTo());
			existing.setPersonalization(jobs.getPersonalization());
			existing.setContactPersonName(jobs.getContactPersonName());
			existing.setMobileNumber(jobs.getMobileNumber());
			existing.setEmailId(jobs.getEmailId());
			existing.setWstartDate(jobs.getWstartDate());
			existing.setWendDate(jobs.getWendDate());
			existing.setWstartTime(jobs.getWstartTime());
			existing.setWendTime(jobs.getWendTime());
			existing.setWdocRequired(jobs.getWdocRequired());
			existing.setWaddress(jobs.getWaddress());
			existing.setWalkinLatitude(jobs.getWalkinLatitude());
			existing.setWalkinLongitude(jobs.getWalkinLongitude());
			existing.setWhatsappNoti(jobs.isWhatsappNoti());
			existing.setJobStatus(jobs.getJobStatus());
			existing.setIsViewContactPersonName("true");
			existing.setIsViewEmailId("true");
			existing.setIsViewMobileNumber("true");
			existing.setInActive(false);

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();

			String date = dtf.format(now);
			Date currentDate;

			try {
				currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
				existing.setJobPostedTime(currentDate);
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			jobRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path = "/additionalDetails", consumes = "application/json")
	public ResponseEntity<?> updateJobAdditionalDetails(@RequestBody JobsModel jobs) {

		Optional<JobsModel> optional = jobRepository.findById(jobs.getId());

		if (optional.isPresent()) {

			JobsModel existing = optional.get();
			existing.setWorkHours(jobs.getWorkHours());
			existing.setOt(jobs.getOt());
			existing.setShiftType(jobs.getShiftType());
			//existing.setShiftTimings(jobs.getShiftTimings());
			//existing.setBenefits(jobs.getBenefits());
			if(jobs.getWorkHours()!=null || jobs.getOt()!=null || jobs.getShiftType()!=null) {
			existing.setAdditionalDetailsFilled(true);
			}
			jobRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path = "/empPreScreeningQuestions", consumes = "application/json")
	public ResponseEntity<?> updateEmpPreScreeningQuestions(@RequestBody EmployerJobPrescreeningQuestionsModel model)

	{
		Optional<JobsModel> job = jobRepository.findById(model.getJobId());
		EmployerJobPrescreeningQuestionsModel empPreQue = new EmployerJobPrescreeningQuestionsModel();

		if (job.isPresent()) {

			empPreQue.setEmpId(model.getEmpId());
			empPreQue.setJobId(model.getJobId());
			empPreQue.setQuestionId(model.getQuestionId());
			empPreQue.setAnswer(model.getAnswer());
			employerJobPrescreeningQuestionsRepository.save(empPreQue);

			JobsModel jmodel = job.get();
			jmodel.setPreQuestionsId(String.valueOf(empPreQue.getId()));
			jobRepository.save(jmodel);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	@PostMapping(path = "/newPostJob", consumes = "application/json")
	public ResponseEntity<?> newJobPost(@RequestBody JobsModel jobs, @RequestParam Long placementPlanId) {
		Optional<EmpPlacementPlanDetailsModel> PlacementPlanDetails = empPlacementPlanDetailsRepository.findById(placementPlanId);

		if (PlacementPlanDetails.isPresent()) {
			EmpPlacementPlanDetailsModel placementPlanDetail = PlacementPlanDetails.get();
			
			if (placementPlanDetail.getActive()) {
			    placementPlanDetail.setActive(false);
			    empPlacementPlanDetailsRepository.save(placementPlanDetail);
			} else {
			    Map<String, Object> response = new HashMap<>();
			    response.put("statusCode", 400);
			    response.put("message", "Job post is not allowed for inactive placement plans");
			    return ResponseEntity.badRequest().body(response);
			}
			Optional<EmployerModel> optional = employerRepository.findById(placementPlanDetail.getEmployerId());

			if (optional.isPresent()) {
				EmployerModel empData = optional.get();
				if (!empData.isDeactivated()) {
					JobsModel job = new JobsModel();

					job.setJobType("Full Time");
					job.setIndustry(jobs.getIndustry());
					job.setJobCategory(jobs.getJobCategory());
					job.setSalaryCurrency("INR");
					job.setSalary(jobs.getSalary());
					job.setMaxSalary(jobs.getMaxSalary());

					job.setJobLocationAddr(empData.getAddress());
					job.setJobLatitude(empData.getLatitude());
					job.setJobLongitude(empData.getLongitude());
					job.setJobCountry(empData.getCountry());
					job.setState(empData.getState());
					job.setArea(empData.getArea());
					job.setJobLocation(empData.getCity());
					job.setAssignTo(1);

					job.setPersonalization("1");

					job.setJobExp(jobs.getJobExp());
					int e = jobs.getJobExp();
					int maxEx = jobs.getJobMaxExp();
					int max = 0;
					if (e >= 1 && e < 3) {
						max = 3;
					} else if (e == 0 && maxEx == 1) {
						max = 1;
					} else if (e == 0 && maxEx == 0) {
						max = 0;
					} else {
						max = 20;
					}

					job.setJobMaxExp(max);
					job.setQualification(jobs.getQualification());

					job.setContactPersonName(jobs.getContactPersonName());
					job.setMobileNumber(jobs.getMobileNumber());
					job.setMobileNumberVerified(true);
					job.setWhatsappNumber(jobs.getWhatsappNumber());
					job.setAlternateMobileNumber(jobs.getAlternateMobileNumber());
					job.setAlternateMobileNumberCountryCode("91");
					job.setAlternateMobileNumberVerified(true);
					job.setIsViewContactPersonName("true");
					job.setIsViewMobileNumber("true");
					job.setIsViewEmailId("false");
					job.setMale(jobs.getMale());
					job.setFemale(jobs.getFemale());
					job.setContactPersonPosition(jobs.getContactPersonPosition());
					job.setNoOfOpenings(jobs.getNoOfOpenings());
					job.setCanResponseCount(jobs.getNoOfOpenings());

					job.setWorkHours(jobs.getWorkHours());
					job.setOt(jobs.getOt());
					job.setShiftType(jobs.getShiftType());
					// job.setShiftTimings(jobs.getShiftTimings());
					job.setBenefits(jobs.getBenefits());
					if (jobs.getWorkHours() != null || jobs.getOt() != null || jobs.getShiftType() != null
							|| jobs.getBenefits() != null) {
						job.setAdditionalDetailsFilled(true);
					}
					job.setKeyskills(jobs.getKeyskills());
					job.setGender(jobs.getGender());

					job.setWhatsappNoti(jobs.isWhatsappNoti());
					job.setCompanyName(empData.getCompanyName());
					job.setJobStatus("O");
					job.setEmployerId(jobs.getEmployerId());
					job.setFromWeb(true);
					job.setInActive(false);
					job.setWhatsappNumberCountryCode("91");

					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					LocalDateTime now = LocalDateTime.now();
					String date = dtf.format(now);
					Date currentDate;
					try {
						currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
						job.setJobPostedTime(currentDate);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					Optional<PlansModel> empPlan1 = plansRepository.findById(empData.getPlan());
					PlansModel plan1 = empPlan1.get();
					int jobPostValidity = plan1.getJobPostValidity();

					SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
					Calendar c = Calendar.getInstance();
					c.setTime(new Date());
					c.add(Calendar.DATE, jobPostValidity);
					String output = sdf1.format(c.getTime());

					job.setExpiryDate(output);
					job.setTotalCanResponse(plan1.getProfiles());

					if (empData.getContactPersonName() == null) {
						empData.setContactPersonName(job.getContactPersonName());
						employerRepository.save(empData);
					}

					if (empData.getMobileNumber() == 0) {
						try {
							empData.setMobileNumber(Long.parseLong(job.getMobileNumber()));
							empData.setMobileCountryCode("91");
							employerRepository.save(empData);
							Optional<LeadModel> em = leadRepository.findByMobileNumber(Long.valueOf(job.getMobileNumber()));
							if (em.isPresent()) {
								LeadModel l = em.get();
								l.setRegisteredInApp(true);
								leadRepository.save(l);
							}
						} catch (Exception e1) {

						}
					}

					try {
						if (jobs.getAlternateMobileNumber() != null && !jobs.getAlternateMobileNumber().isEmpty()) {
							if (empData.getAlternateMobileNumber() == null
									&& empData.getAlternateMobileNumber().isEmpty()) {
								empData.setAlternateMobileNumber(jobs.getAlternateMobileNumber());
								employerRepository.save(empData);
							}
						}
					} catch (Exception e2) {

					}
					
					int jobUniqID = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);
					job.setEmpJobId(String.valueOf(jobUniqID));
					
					jobRepository.save(job);
					
					placementPlanDetail.setActive(false);
			        empPlacementPlanDetailsRepository.save(placementPlanDetail);
				
					EmployerActivityModel EA = new EmployerActivityModel();
					EA.setEmpId(job.getEmployerId());
					EA.setActivity("Your job " + "<b>" + job.getJobCategory() + "</b>" + " has been published!");
					empActivityRepository.save(EA);

					Integer jobUpdateCount = empData.getPlanJobCount();
	                jobUpdateCount -= 1;
	                empData.setPlanJobCount(jobUpdateCount);
	                employerRepository.save(empData);

					String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseJSApiKey;

					DeeplinkRequest dl = new DeeplinkRequest();
					dl.setLongDynamicLink(firebaseJSHost + "/?link=" + firebaseJSHost + "/jobDetails/" + 0 + "/"
							+ job.getId() + "&apn=" + firebaseJSPackage);

					DeeplinkSuffix c1 = new DeeplinkSuffix();
					c1.setOption("UNGUESSABLE");

					String json = new Gson().toJson(dl);

					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);

					HttpEntity<String> req = new HttpEntity<String>(json.toString(), headers);

					RestTemplate restTemp = new RestTemplate();
					FirebaseShortLink response = null;
					try {
						response = restTemp.postForObject(url, req, FirebaseShortLink.class);
						job.setDeeplink(response.getShortLink());
						job = jobRepository.save(job);
					} catch (Exception e1) {

					}

					DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
					String pdate = formatter.format(new Date());
					SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
					Calendar cal = Calendar.getInstance();
					String time = simpleDateFormat1.format(cal.getTime());

					if (activeProfile.equalsIgnoreCase("prod")) {
						String jobStatus = "Paid";
						String eventName = "Job Published";

						HashMap<String, String> data1 = new HashMap<>();
						data1.put("Event Name", "Job Alert");
						data1.put("Event Type", eventName);
						data1.put("Type", "Job");
						data1.put("Company Name", job.getCompanyName());
						data1.put("Contact Person Name", job.getContactPersonName());
						data1.put("Position", job.getJobCategory());
						data1.put("Location", job.getJobLocation() != null ? job.getJobLocation() : "");
						data1.put("Experience", String.valueOf(job.getJobExp())+" to "+String.valueOf(job.getJobMaxExp()));
						data1.put("Source", "Web");
						data1.put("Mobile Number", String.valueOf(job.getMobileNumber()));
						data1.put("Job Status", jobStatus);
						data1.put("ID Type", "Job ID");
						data1.put("ID", String.valueOf(job.getId()));

						exotelCallController.connectToAgent("+91" + String.valueOf(job.getMobileNumber()),"Emp",data1);

						//notificationService.sendNotification(job, optional.get());
					}

					EmployerTimeline employerTimeline = new EmployerTimeline();
					Date currentDate1 = new Date();
					SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
					String formattedDate1 = dateFormat1.format(currentDate1);
					String eventDescription = "Job Posted on  <b>" + formattedDate1+ " from App";
					employerTimeline.setEmpId(job.getEmployerId());
					employerTimeline.setEmpLeadId(0);
					employerTimeline.setEventName("Job Posted");
					employerTimeline.setEventDescription(eventDescription);
					employerTimelineRepository.save(employerTimeline);

					HashMap<String, Object> map = new HashMap<>();
					map.put("status", "success");
					map.put("message", "success");
					map.put("code", 200);
					map.put("data", job);

					return new ResponseEntity<>(map, HttpStatus.OK);
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 400);
					map.put("message", "Account Deactivated");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "Employer Not Found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Draft Job Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path="/updateNewPostJob" ,consumes = "application/json")
	public ResponseEntity<?> updatePostJob(@RequestParam int jobId, @RequestBody JobsModel jobs) {
		Optional<JobsModel> jobOptional = jobRepository.findById(jobId);

		if (jobOptional.isPresent()) {
			JobsModel job = jobOptional.get();

			// Update the job fields based on the data in the JobsModel received in the request body.
			job.setEmployerId(jobs.getEmployerId());
			job.setJobType(jobs.getJobType());
			job.setIndustry(jobs.getIndustry());
			job.setJobCategory(jobs.getJobCategory());
//			job.setEmpJobId(job.getEmpJobId());
			job.setSalary(jobs.getSalary());
			job.setMaxSalary(jobs.getMaxSalary());
			job.setJobLocationAddr(jobs.getJobLocationAddr());
			job.setJobLatitude(jobs.getJobLatitude());
			job.setJobLongitude(jobs.getJobLongitude());
			job.setJobCountry(jobs.getJobCountry());
			job.setState(jobs.getState());
			job.setArea(jobs.getArea());
			job.setJobLocation(jobs.getJobLocation());
			job.setAssignTo(jobs.getAssignTo());
			job.setQualification(jobs.getQualification());
    		job.setCompanyName(job.getCompanyName());
			job.setContactPersonName(jobs.getContactPersonName());
			job.setMobileNumber(jobs.getMobileNumber());
			job.setWhatsappNumber(jobs.getWhatsappNumber());
			job.setAlternateMobileNumber(jobs.getAlternateMobileNumber());
			job.setMale(jobs.getMale());
			job.setFemale(jobs.getFemale());
			job.setContactPersonPosition(jobs.getContactPersonPosition());
			job.setNoOfOpenings(jobs.getNoOfOpenings());
			job.setWorkHours(jobs.getWorkHours());
			job.setOt(jobs.getOt());
			job.setShiftType(jobs.getShiftType());
			job.setJobExp(jobs.getJobExp());
			job.setJobMaxExp(jobs.getJobMaxExp());
			job.setKeyskills(jobs.getKeyskills());
			job.setBenefits(jobs.getBenefits());

			// Save the updated job entity in the database.
			jobRepository.save(job);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "Job updated successfully");
			map.put("code", 200);
			map.put("data", job);

			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 404);
			map.put("message", "Job not found");
			return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
		}
	}

}
