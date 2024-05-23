package com.taizo.controller.admin;

import com.google.gson.Gson;
import com.taizo.controller.gallabox.ExotelCallController;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import com.google.gson.Gson;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.taizo.controller.gallabox.ExotelCallController;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.repository.AdminAnalyticsRepository;
import com.taizo.repository.AdminRepository;
import com.taizo.repository.CandidateCallRepository;
import com.taizo.repository.EmpActivityRepository;
import com.taizo.repository.EmpPlacementPlanDetailsRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.InterviewRepository;
import com.taizo.repository.JobApplicationRepository;
import com.taizo.repository.JobRepository;
import com.taizo.repository.LeadRepository;
import com.taizo.repository.PlansRepository;
import com.taizo.service.JobService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


@CrossOrigin
@RestController
@RequestMapping("/admin")
public class AdminJobController {
	
	@Autowired
	JobService jobService;
	
	@Autowired
	CfgCanAdminAreaRepository cfgCanAdminAreaRepository;
	
	@Autowired
	CandidateCallRepository candidateCallRepository;

	@Autowired
	JobApplicationRepository jobApplicationRepository;

	@Autowired
	InterviewRepository interviewRepository;
	
	@Autowired
	EmployerRepository employerRepository;
	
	@Autowired
    EmpPlacementPlanDetailsRepository empPlacementPlanDetailsRepository;
	
	@Autowired
	LeadRepository leadRepository;
	
	@Autowired
	PlansRepository plansRepository;
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	AdminAnalyticsRepository adminAnalyticsRepository;
	
	@Autowired
	AdminRepository adminRepository;
	
	@Autowired
	ExotelCallController exotelCallController;
	
	@Autowired
	EmpActivityRepository empActivityRepository;

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
	

	
	@GetMapping(value = "/job/{id}")
	public ResponseEntity<?> getNotiCount(@PathVariable("id") int id) throws ResourceNotFoundException {

		String status = "I";

		Page<JobApplicationModel> jobApplicationModel = jobApplicationRepository.getCanAppliedNotificationCount(id,
				status, PageRequest.of(0, 100));

		Page<InterviewsModel> InterviewsModel = interviewRepository.getCanInterviewScheduledNotificationCount(id,
				PageRequest.of(0, 100));

		Page<CandidateCallModel> employerCallModel = candidateCallRepository.getCanCallNotificationCount(id,
				PageRequest.of(0, 100));

		HashMap<String, Long> count = new HashMap<>();

		count.put("CandidatesAppliedCount", jobApplicationModel.getTotalElements());
		count.put("InterviewScheduledCount", InterviewsModel.getTotalElements());
		count.put("CandidatesCalledCount", employerCallModel.getTotalElements());

		ArrayList<HashMap<String, Long>> al = new ArrayList<>();
		al.add(count);
		HashMap<String, Object> map = new HashMap<>();
		map.put("jobOverview", al);
		map.put("jobDetails", jobService.getById(id));
		return new ResponseEntity<>(map, HttpStatus.OK);

	}
	
	public class JobFilterResponse {
	    private long totalCount;
	    private List<Map<String, Object>> jobList;

	    public JobFilterResponse(long totalCount, List<Map<String, Object>> jobList) {
	        this.totalCount = totalCount;
	        this.jobList = jobList;
	    }

	    public long getTotalCount() {
	        return totalCount;
	    }

	    public List<Map<String, Object>> getJobList() {
	        return jobList;
	    }
	}

	
	 @PostMapping("/filterByJobDetails")
	 public ResponseEntity<JobFilterResponse> filterJobs(
		        @RequestBody JobsModel jobsModel
		) {
		    List<Map<String, Object>> filteredCandidates = jobService.filterJob(
		    		jobsModel.getEmployerId(),jobsModel.getGender(),jobsModel.getCompanyName(),
		    		jobsModel.getJobLocation(),jobsModel.getArea(),jobsModel.getIndustry(),
		    		jobsModel.getJobCategory(),jobsModel.getBenefits(),jobsModel.getKeyskills(),
		    		jobsModel.getQualification(),jobsModel.getSalary(),jobsModel.getMaxSalary(),
		    		jobsModel.getJobExp(),jobsModel.getJobMaxExp(),jobsModel.getPages(),jobsModel.getSize(),
		    		jobsModel.getCreatedTime(),jobsModel.getEndDate()
				
				 );
		    long totalCount = jobService.filterjobCount(
		    		jobsModel.getEmployerId(),jobsModel.getGender(),jobsModel.getCompanyName(),
		    		jobsModel.getJobLocation(),jobsModel.getArea(),jobsModel.getIndustry(),
		    		jobsModel.getJobCategory(),jobsModel.getBenefits(),jobsModel.getKeyskills(),
		    		jobsModel.getQualification(),jobsModel.getSalary(),jobsModel.getMaxSalary(),
		    		jobsModel.getJobExp(),jobsModel.getJobMaxExp(),jobsModel.getPages(),jobsModel.getSize(),
		    		jobsModel.getCreatedTime(),jobsModel.getEndDate()
		    		
				);

		    JobFilterResponse response = new JobFilterResponse(totalCount, filteredCandidates);

		    return ResponseEntity.ok(response);

	 }
	 
	 @GetMapping(value = "/PublishedJob")
		public ResponseEntity<Map<String, Object>> getpublishedJob(
				@RequestParam(name = "mobile_number", required = false) String mobileNumber,
				@RequestParam(name = "page", defaultValue = "0") int page,
				@RequestParam(name = "size", defaultValue = "10") int size) {

			Page<JobsModel> result;
			Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());

			if (mobileNumber != null) {
				result = jobService.getPublishedJobs(mobileNumber, page, size);

			} else {
				result = jobService.getAllpublishedjob(pageable);
			}

			if (result.isEmpty()) {
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("code", 400);
				errorResponse.put("message", "UnPublished Job Not Found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
			}
			
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("status", "success");
			successResponse.put("message", "success");
			successResponse.put("code", 200);
			successResponse.put("data", result);
			return ResponseEntity.ok(successResponse);
		}
	 
	 @PostMapping(path = "/newPostJob", consumes = "application/json")
		public ResponseEntity<?> newJobPost(@RequestBody JobsModel jobs,
				                           @RequestParam(value = "admin_id", required = false, defaultValue = "0") Long adminId){
				                           
		
			Admin admin = adminRepository.findById(adminId).get();
			

				Optional<EmployerModel> optional = employerRepository.findById(jobs.getEmployerId());

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
						job.setEmailId(jobs.getEmailId());

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
						
						/*
						 * Optional<PlansModel> empPlan1 =
						 * plansRepository.findById(placementPlanDetail.getPlanId()); PlansModel plan1 =
						 * empPlan1.get(); int jobPostValidity = plan1.getJobPostValidity();
						 */

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

						EmployerTimeline employerTimeline = new EmployerTimeline();
						Date currentDate1 = new Date();
						SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
						String formattedDate1 = dateFormat1.format(currentDate1);
						String eventDescription = "Job Posted on  <b>" + formattedDate1 + " by " + admin.getUserName();
						employerTimeline.setEmpId(job.getEmployerId());
						employerTimeline.setEmpLeadId(0);
						employerTimeline.setEventName("Job Posted");
						employerTimeline.setEventDescription(eventDescription);
						employerTimelineRepository.save(employerTimeline);
						
						
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

	 @PutMapping("/retentionByAdminId")
	 public ResponseEntity<?> updateRetentionByAdminId(@RequestParam int id,
	                                                   @RequestParam Long adminId,
	                                                   @RequestParam boolean retention) {

	     JobsModel jobs = jobRepository.findById(id).orElse(null);
	     Admin admin = adminRepository.findById(adminId).orElse(null);

	     if (jobs == null || admin == null) {
	         // Return a 400 Bad Request response if job or admin not found
	         Map<String, Object> errorMap = new HashMap<>();
	         errorMap.put("code", 400);
	         errorMap.put("message", "Job or Admin not found");
	         return ResponseEntity.badRequest().body(errorMap);
	     }

	     if (retention) {
	         jobs.setRetentionByAdminId(adminId);
	         jobRepository.save(jobs);

	         List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(adminId);

	         LocalDate currentDate = LocalDate.now();
	         boolean dateMatch = false;

	         for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
	             LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
	             if (currentDate.isEqual(createdOnDate)) {
	                 dateMatch = true;
	                 adminAnalytics.setRetentionCount(
	                         adminAnalytics.getRetentionCount() != null
	                                 ? adminAnalytics.getRetentionCount() + 1
	                                 : 1
	                 );
	             }
	         }

	         if (!dateMatch) {
	             // If no matching date is found, insert a new record
	             AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
	             newAdminAnalytics.setAdminId(adminId);
	             newAdminAnalytics.setModule(admin.getModule());
	             newAdminAnalytics.setRetentionCount(1);
	             adminAnalyticsList.add(newAdminAnalytics);
	         }

	         adminAnalyticsRepository.saveAll(adminAnalyticsList);

	         // Return a 200 OK response
	         Map<String, Object> successMap = new HashMap<>();
	         successMap.put("code", 200);
	         successMap.put("message", "Updated Success");
	         return ResponseEntity.ok(successMap);
	     } else {
	         // Return a 400 Bad Request response if retention is false
	         Map<String, Object> errorMap = new HashMap<>();
	         errorMap.put("code", 400);
	         errorMap.put("message", "Retention is false");
	         return ResponseEntity.badRequest().body(errorMap);
	     }
	 }

	 @PutMapping("/moveToJobAssign")
	 public ResponseEntity<?> jobAssignTo(@RequestParam int jobId,
	                                      @RequestParam int adminId) {
	     Optional<JobsModel> jobsModel = jobRepository.findById(jobId);
	     JobsModel assignTo=jobsModel.get();
	     HashMap<String, Object> map = new HashMap<>();

	     if (jobsModel != null) {
	    	 assignTo.setAssignTo(adminId);
	         jobRepository.save(assignTo); // Save and immediately flush changes
	         map.put("code", 200);
	         map.put("message", "Updated Successfully");
	         return new ResponseEntity<>(map, HttpStatus.OK);
	     } else {
	         map.put("code", 404);
	         map.put("message", "Job not found");
	         return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
	     }
	 }

}
