package com.taizo.controller.webemp;

import java.net.URISyntaxException; 
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.Gson;
import com.taizo.model.*;
import com.taizo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

import com.taizo.controller.gallabox.ExotelCallController;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.service.DraftJobsService;
import com.taizo.service.JobService;
import com.taizo.service.NotificationService;

@CrossOrigin
@RestController
@RequestMapping("/webEmployer")
public class WebJobPostController {

	@Autowired
	JobService jobService;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	WebJobsRepository webJobsRepository;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	LeadRepository leadRepository;
	
	@Autowired
	EmployerFieldLeadRepository employerFieldLeadRepository;
	
	@Autowired
	CfgCanAdminAreaRepository cfgCanAdminAreaRepository;

	@Autowired
	@Lazy
	NotificationService notificationService;

	@Autowired
	EmployerJobPrescreeningQuestionsRepository employerJobPrescreeningQuestionsRepository;


	@Autowired
	EmpActivityRepository empActivityRepository;

	@Autowired
	ExotelCallController exotelCallController;

	@Autowired
	PlansRepository plansRepository;
	
	@Autowired
	AdminRepository adminRepository;

	@Autowired
	private DraftJobsRepository draftJobsRepository;

	@Autowired
	private DraftJobsService draftJobsService;
	
	@Autowired
    EmpPlacementPlanDetailsRepository empPlacementPlanDetailsRepository;

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

	@Autowired
	CfgEmpPrescreeningQuestionsRepository cfgEmpPrescreeningQuestionsRepository;


	@PostMapping(path = "/staffingJobPost", consumes = "application/json")
	public ResponseEntity<?> StaffingJobPost(@RequestBody JobsModel job) {

		Optional<EmployerModel> optional = employerRepository.findById(job.getEmployerId());
		if (optional.isPresent()) {
			EmployerModel empData = optional.get();
			if (!empData.isDeactivated()) {

				job.setFromWeb(true);
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
	public ResponseEntity<?> newJobPost(@RequestParam("used_free_trial") final boolean usedFreeTrail,
										@RequestBody JobsModel jobs) {

		Optional<EmployerModel> optional = employerRepository.findById(jobs.getEmployerId());

		if (optional.isPresent()) {
			EmployerModel empData = optional.get();
			if (!empData.isDeactivated()) {

				JobsModel job = new JobsModel();

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
				job.setJobStatus("O");
				job.setEmployerId(jobs.getEmployerId());
				job.setFromWeb(true);
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
					if (usedFreeTrail) {
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
					data1.put("Location", job.getJobLocation() != null ? job.getJobLocation() : "");
					data1.put("Experience", String.valueOf(job.getJobExp()) + " to " + String.valueOf(job.getJobMaxExp()));
					data1.put("Source", "Web");
					data1.put("Mobile Number", String.valueOf(job.getMobileNumber()));
					data1.put("Job Status", jobStatus);
					data1.put("ID Type", "Job ID");
					data1.put("ID", String.valueOf(job.getId()));

					exotelCallController.connectToAgent("+91" + String.valueOf(job.getMobileNumber()), "Emp", data1);

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
			job.setFromWeb(true);
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

			// TODO Auto-generated method stub
			if (!optional.isPresent()) {
				// logger.debug("Job not found with id {}.", jobs.getEmployerId());
				throw new ResourceNotFoundException("Job not found.");
			}

			JobsModel job = jobs1.get();

			// JobsModel job = new JobsModel();
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
			job.setJobStatus(jobs.getJobStatus());
			job.setEmployerId(jobs.getEmployerId());
			job.setFromWeb(true);
			job.setInActive(false);

			if (jobs.getJobStatus().equalsIgnoreCase("O")) {
				if (!empData.isDeactivated()) {

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
							Optional<LeadModel> em = leadRepository
									.findByMobileNumber(Long.valueOf(job.getMobileNumber()));
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
					} catch (Exception e2) {

					}

					if (activeProfile.equalsIgnoreCase("prod")) {
						String jobStatus = "Paid";
						String eventName = "Job Published";
						if (usedFreeTrail) {
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
						data1.put("Location", job.getJobLocation() != null ? job.getJobLocation() : "");
						data1.put("Experience", String.valueOf(job.getJobExp()) + " to " + String.valueOf(job.getJobMaxExp()));
						data1.put("Source", "Web");
						data1.put("Mobile Number", String.valueOf(job.getMobileNumber()));
						data1.put("Job Status", jobStatus);
						data1.put("ID Type", "Job ID");
						data1.put("ID", String.valueOf(job.getId()));

						exotelCallController.connectToAgent("+91" + String.valueOf(job.getMobileNumber()), "Emp", data1);

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

	@PostMapping(path = "/postJob", consumes = "application/json")
	public JobsModel jobDetails(@RequestBody JobsModel jobs) throws ResourceNotFoundException {
		return jobService.saveJobDetails(jobs);
	}

	// website job post
	@PostMapping(path = "/webPostJob", consumes = "application/json")
	public WebJobsModel webJobDetails(@RequestBody WebJobsModel jobs) throws ResourceNotFoundException {
		return jobService.saveWebJobDetails(jobs);
	}

	@PostMapping(path = "/webEmpRegister")
	public WebJobsModel webEmpregDetails(@RequestParam("job_id") final int jobId,
										 @RequestParam(value = "email_id") final String emailId) throws ResourceNotFoundException {
		Optional<WebJobsModel> webJob = webJobsRepository.findById(jobId);
		if (!webJob.isPresent()) {
			throw new ResourceNotFoundException("Job not found.");
		}
		WebJobsModel existing = webJob.get();
		existing.setEmailId(emailId);
		webJobsRepository.save(existing);

		EmployerModel employerExists = employerRepository.findByEmailId(emailId);
		if (employerExists != null) {

			existing.setEmployerId(employerExists.getId());
			webJobsRepository.save(existing);

			employerExists.setIndustry(existing.getIndustry());
			employerExists.setCompanyName(existing.getCompanyName());
			employerExists.setCategory("Company");
			employerExists.setCompanyDetailsFilled(true);

			if (existing.getJobLocationAddr() != null && !existing.getJobLocationAddr().isEmpty()) {
				employerExists.setState(existing.getState());
				employerExists.setAddress(existing.getJobLocationAddr());
				employerExists.setCity(existing.getJobCity());
				employerExists.setState(existing.getState());
				employerExists.setCountry(existing.getJobCountry());
				employerExists.setArea(existing.getArea());

				// employerExists.setPincode(existing.getP);
				employerExists.setLatitude(existing.getJobLatitude());
				employerExists.setLongitude(existing.getJobLongitude());

			}
		}

		employerRepository.save(employerExists);

		return webJob.get();
	}

	@PostMapping(path = "/saveJob", consumes = "application/json")
	public JobsModel jobPost(@RequestBody JobsModel jobs) throws ResourceNotFoundException {
		return jobService.saveJobPost(jobs);
	}

	@PutMapping(path = "/saveJobDetails", consumes = "application/json")
	public JobsModel updateJobDetails(@RequestBody JobsModel jobs) throws ResourceNotFoundException {
		return jobService.updateJobDetails(jobs);
	}

	@PutMapping(path = "/saveJobPersonalization", consumes = "application/json")
	public JobsModel jobPersonalization(@RequestBody JobsModel jobs) throws ResourceNotFoundException {
		return jobService.saveJobPersonalization(jobs);
	}

	@PutMapping(path = "/saveJobReview", consumes = "application/json")
	public JobsModel savejobPost(@RequestBody JobsModel jobs) throws ResourceNotFoundException {
		return jobService.saveJobReviewPost(jobs);
	}

	@PutMapping(path = "/postDraftJob", consumes = "application/json")
	public JobsModel postDraftjob(@RequestBody JobsModel jobs) throws ResourceNotFoundException {
		return jobService.postDraftjob(jobs);
	}

	@PostMapping(path = "/openJobPost")
	public JobsModel openJobPost(@RequestParam("emp_id") final int empId, @RequestParam("job_id") final int jobId,
								 @RequestParam(value = "used_free_trial", required = false, defaultValue = "false") final boolean usedFreeTrail)
			throws ResourceNotFoundException {
		return jobService.openPostJob(empId, jobId, usedFreeTrail);
	}

	@PutMapping(path = "/additionalDetails", consumes = "application/json")
	public JobsModel updateJobAdditionalDetails(@RequestBody JobsModel jobs) throws ResourceNotFoundException {
		return jobService.saveJobAdditionalDetails(jobs);
	}

	@PutMapping(path = "/empPreScreeningQuestions")
	public ResponseEntity<?> updateLocation(@RequestParam("jobId") final int job_id,
											@RequestParam(value = "empId") final int emp_id,
											@RequestParam(value = "questionId") final String question_id,
											@RequestParam(value = "answer") final String answer) {

		Optional<JobsModel> job = jobRepository.findById(job_id);
		JobsModel jmodel = new JobsModel();
		List<String> jobPreId = new ArrayList<>();

		if (job.isPresent()) {

			String num = question_id;
			String str[] = num.split(",");
			List<String> al = new ArrayList<String>(Arrays.asList(str));
			for (String s : al) {
				EmployerJobPrescreeningQuestionsModel empPreQue = new EmployerJobPrescreeningQuestionsModel();
				empPreQue.setEmpId(emp_id);
				empPreQue.setJobId(job_id);
				empPreQue.setQuestionId(Integer.parseInt(s));

				String num1 = answer;
				String str1[] = num1.split(",");
				List<String> al1 = new ArrayList<String>(Arrays.asList(str1));
				for (String s1 : al1) {
					empPreQue.setAnswer(s1);
					employerJobPrescreeningQuestionsRepository.save(empPreQue);

					int id = empPreQue.getId();
					jobPreId.add(String.valueOf(id));
					List<String> intLIst = jobPreId;

					jmodel.setPreQuestionsId(String.valueOf(intLIst));
					jobRepository.save(jmodel);

				}

			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "Saved Succesfully");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not Found");
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
					
				//	setAssignToBasedOnArea(job);

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
					Optional<PlansModel> empPlan1 = plansRepository.findById(placementPlanDetail.getPlanId());
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
					
					String jobLocation = job.getJobLocation();
					if (jobLocation != null) {
					    jobLocation = jobLocation.trim();  // Trim the string if it's not null
					    if ("Coimbatore".equalsIgnoreCase(jobLocation)) {
					        job.setAssignTo(10);
					    } else {
					        job.setAssignTo(1);
					    }
					} 
					
					placementPlanDetail.setActive(false);
			        empPlacementPlanDetailsRepository.save(placementPlanDetail);
				
					EmployerActivityModel EA = new EmployerActivityModel();
					EA.setEmpId(job.getEmployerId());
					EA.setActivity("Your job " + "<b>" + job.getJobCategory() + "</b>" + " has been published!");
					empActivityRepository.save(EA);

					/*
					 * Integer jobUpdateCount = empData.getPlanJobCount(); jobUpdateCount -= 1;
					 * empData.setPlanJobCount(jobUpdateCount); employerRepository.save(empData);
					 */

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
					String eventDescription = "Job Posted on  <b>" + formattedDate1 + " from Web";
					employerTimeline.setEmpId(jobs.getEmployerId());
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
	// Method to set the "assignTo" based on the area
    private void setAssignToBasedOnArea(JobsModel job) {
        String area = job.getArea();
        CfgCanAdminArea cfgCanAdminArea = (area != null) ? cfgCanAdminAreaRepository.findByAreas(area) : null;

        if (cfgCanAdminArea != null) {
            int adminId = findAdminIdByAssingnedToAdminId(cfgCanAdminArea.getAssingnedToAdminId());
            job.setAssignTo(Math.toIntExact(adminId));
        } else {
        	job.setAssignTo(1);
        }
    }
    private int findAdminIdByAssingnedToAdminId(int assingnedToAdminId) {
		// TODO Auto-generated method stub
		List<CfgCanAdminArea> adminJobRolesMapping =cfgCanAdminAreaRepository.findByAssingnedToAdminId(assingnedToAdminId);
		if (adminJobRolesMapping != null && !adminJobRolesMapping.isEmpty()) {
			return adminJobRolesMapping.get(0).getAssingnedToAdminId();
		} else {
			return 0;
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
    		job.setCompanyName(jobs.getCompanyName());
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
	
	@PostMapping("/moveJob")
	public ResponseEntity<?> moveJobToAnotherAdmin(@RequestParam(required=false) int jobId, @RequestParam(required=false) int adminId) {
	    // Find the job by ID
	    Optional<JobsModel> optionalJob = jobRepository.findById(jobId);

	    // Assuming AdminRepository is the repository for the Admin entity
	  //  Optional<Admin> optionalAdmin = adminRepository.findById(adminId);

	    if (optionalJob.isPresent()) {
	        JobsModel job = optionalJob.get();

	        // Move the job to the new admin
	        job.setAssignTo(adminId);
	        jobRepository.save(job);

	        // Return a success response
	        Map<String, Object> response = new HashMap<>();
	        response.put("status", "success");
	        response.put("message", "Job moved to another admin successfully");
	        return ResponseEntity.ok(response);
	    } else {
	        // Job or Admin with the given ID not found
	        Map<String, Object> response = new HashMap<>();
	        response.put("status", "error");
	        response.put("message", "Job or Admin not found");
	        return ResponseEntity.badRequest().body(response);
	    }
	}

	@PostMapping("/moveEmployerFieldLead")
	public ResponseEntity<?> moveFieldToAnotherAdmin(@RequestParam(required = false) int id, @RequestParam(required = false) int adminId) {
	    // Find the EmployerFieldLead by ID
	    Optional<EmployerFieldLead> optionalEmployerFieldLead = employerFieldLeadRepository.findById(id);

	    if (optionalEmployerFieldLead.isPresent()) {
	        EmployerFieldLead employerFieldLead = optionalEmployerFieldLead.get();

	        // Move the EmployerFieldLead to the new admin
	        employerFieldLead.setAdminId(adminId);
	        employerFieldLeadRepository.save(employerFieldLead);

	        // Return a success response
	        Map<String, Object> response = new HashMap<>();
	        response.put("status", "success");
	        response.put("message", "EmployerFieldLead moved to another admin successfully");
	        return ResponseEntity.ok(response);
	    } else {
	        // EmployerFieldLead with the given ID not found
	        Map<String, Object> response = new HashMap<>();
	        response.put("status", "error");
	        response.put("message", "EmployerFieldLead not found");
	        return ResponseEntity.badRequest().body(response);
	    }
	}


}



