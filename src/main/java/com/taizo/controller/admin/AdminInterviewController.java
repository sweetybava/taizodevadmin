 package com.taizo.controller.admin;


import com.taizo.DTO.InterviewCandidateDTO;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.CandidateAnalyticsService;
import com.taizo.service.JobService;
import com.taizo.service.WAAlertService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/admin")
public class AdminInterviewController {
	
	private static final Logger logger = LoggerFactory.getLogger(AdminInterviewController.class);

	

	@Autowired
	CandidateRepository candidateRepository;
	
	@Autowired
	RescheduleInterviewRepository rescheduleInterviewRepository;
	
	@Autowired
	EmpInterviewNotificationRepository empInterviewNotificationRepository;
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	JobService jobService;
	
	@Autowired
	EmpActivityRepository empActivityRepository;
	
	@Autowired
	CanInterviewRepository caninterviewrepository;
	
	@Autowired
	WAAlertService waAlertService;
	
	@Autowired
	AdminRepository adminRepository;
	
	@Autowired
	InterviewRepository interviewRepository;
	
	@Autowired
	AdminAnalyticsRepository adminAnalyticsRepository;

	@Autowired
	CandidateTimeLineRepository candidateTimeLineRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	EmployerTimelineRepository employerTimelineRepository;
	
	@Autowired
	CandidateAnalyticsService candidateAnalyticsService;

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Value("${firebase.js.api.key}")
	private String firebaseJSApiKey;

	@Value("${firebase.deeplink.js.hostname}")
	private String firebaseJSHost;

	@Value("${firebase.jsapp.package.name}")
	private String firebaseJSPackage;

	@Value("${firebase.emp.api.key}")
	private String firebaseEmpApiKey;
	
	@Value("${firebase.deeplink.emp.hostname}")
	private String firebaseEmpHost;
	
	@Value("${firebase.empapp.package.name}")
	private String firebaseEmpPackage;
	
	

	
	@PostMapping("/sendInterviewAlert")
	public ResponseEntity<?> candidateInterviews(
			@RequestParam("can_id") int candidateId,
			@RequestParam("job_id") int jobId,
			@RequestParam("interview_date") String interviewDate,
			@RequestParam ("admin_id")int adminId,
			@RequestParam(required=false) String candidatePercentage,
	        @RequestParam(required=false)String emergencyContactNumber,
			@RequestParam(required=false)String relationshipType,
			@RequestParam(required=false)String relationName) {
		Optional<CandidateModel> candidateDetails = candidateRepository.findById(candidateId);
		CandidateModel candidateModel = candidateDetails.get();
		String lan=candidateModel.getLanguageKey();
		Optional<JobsModel> jobDetails = jobRepository.findById(jobId);
		JobsModel job = jobDetails.get();
		EmployerModel emp =employerRepository.findById(job.getEmployerId()).get();
		
		if (!candidateDetails.isPresent()) {
	        HashMap<String, Object> errorMap = new HashMap<>();
	        errorMap.put("code", 400);
	        errorMap.put("message", "Candidate not found for id: ");
	        return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
	    }

	    if (!jobDetails.isPresent()) {
	        HashMap<String, Object> errorMap = new HashMap<>();
	        errorMap.put("code", 400);
	        errorMap.put("message", "Job not found for id: ");
	        return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
	    }

		JobsModel jobsModel = jobDetails.get();
		CandidateModel candidate = candidateDetails.get();
		
        Optional<Admin>admin = adminRepository.findById(Long.valueOf(adminId));
        Admin a =admin.get();
        String module = a.getModule();
        

		if (candidateDetails.isPresent() && jobDetails.isPresent()) {
			// CandidateModel candidate = candidateDetails.get();
			// JobsModel job = jobDetails.get();

			CanInterviewsModel canInterview = new CanInterviewsModel();

			canInterview.setCanId(candidateId);
			canInterview.setJobId(jobId);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date interviewdate = null;
			try {
				interviewdate = dateFormat.parse(interviewDate.trim());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			canInterview.setInterviewDate(interviewDate);
			canInterview.setInterviewScheduledDt(interviewDate);
			canInterview.setCandidatePercentage(candidatePercentage);
			canInterview.setInterviewTime("10:00 AM - 05:00 PM");
			canInterview.setStatus("I");
			canInterview.setActive(true);
			canInterview.setDocuments(
					"Bio-data, All Education Certificate, Aadhaar Card, Ration Card, 2 Passport Size Photo, Bank Passbook");
			canInterview.setCity(canInterview.getCity());
			canInterview.setArea(canInterview.getArea());

			long contactNumber = Long.parseLong(jobsModel.getMobileNumber());
			canInterview.setContactNumber(contactNumber);
			canInterview.setContactPersonName(jobsModel.getContactPersonName());
			canInterview.setCompanyName(jobsModel.getCompanyName());
			canInterview.setCity(jobsModel.getJobLocation());
			canInterview.setArea(jobsModel.getArea());
			canInterview.setAdminId(adminId);

			caninterviewrepository.save(canInterview);
			
			//save to CandidateTable
			candidateModel.setRelationshipType(relationshipType);
			candidateModel.setRelationName(relationName);
			candidateModel.setEmergencyContactNumber(emergencyContactNumber);
			
			candidateRepository.save(candidateModel);
			

			CandidateTimeLine can = new CandidateTimeLine();

			Date currentDate = new Date();
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
			String formattedDate = dateFormat1.format(currentDate);
			can.setCanId(candidateId);
			can.setEventName("Interview scheduled");
			can.setEventDescription("Interview Scheduled for <b>" + emp.getCompanyName() + "</b> on " + formattedDate + " by " + a.getUserName());

			candidateTimeLineRepository.save(can);
			
			candidateAnalyticsService.InterviewScheduledCounts(Long.valueOf(adminId),LocalDate.now());

			HashMap<String, String> employerdata = new HashMap<>();
			employerdata.put("mn", "91" + jobsModel.getWhatsappNumber());
			employerdata.put("candidate_name", candidate.getFirstName());
			employerdata.put("position_applied", jobsModel.getJobCategory());
			employerdata.put("interview_date", canInterview.getInterviewDate());
			employerdata.put("interview_time", "10:00 AM - 05:00 PM");
			employerdata.put("candidate_experience",
					candidate.getExperience() + " Years " + candidate.getExpMonths() + "  Month");
			employerdata.put("qualification", candidate.getQualification());
			employerdata.put("skills", candidate.getKeySkill());
			employerdata.put("can_web_link", "https://web.taizo.in/console/candidates?status=int&Cid=" + canInterview.getCanId()
					+ "&jobId=" + canInterview.getJobId());
			// d.put("can_web_link",
			// "https://console.taizo.in/candidates?status=call&Cid="+candidateId+"&jobId="+jobId);
			String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseEmpApiKey;
			DeeplinkRequest dl = new DeeplinkRequest();
			dl.setLongDynamicLink(firebaseEmpHost + "/?link=" + firebaseEmpHost + "/candidateDetails/"
					+ canInterview.getCanId() + "/" + canInterview.getJobId() + "&apn=" + firebaseEmpPackage);
			// System.out.println(ex.getLongDynamicLink());
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
			} catch (Exception ex) {
			}
			employerdata.put("can_link", response.getShortLink());
			 if (activeProfile.equalsIgnoreCase("prod")) {
			waAlertService.adminInterviewAlertEmp(employerdata);
			 }

			Integer responseCount = jobsModel.getCanResponseCount();
			responseCount -= 1;
			jobsModel.setCanResponseCount(responseCount);
			jobRepository.save(jobsModel);

			
			HashMap<String, String> candidateAlert = new HashMap<>();
			candidateAlert.put("mn", "91" + candidate.getWhatsappNumber());
			candidateAlert.put("name", candidate.getFirstName());
			candidateAlert.put("company_name", jobsModel.getCompanyName());
			candidateAlert.put("area", jobsModel.getArea());
			candidateAlert.put("city", jobsModel.getJobLocation());
			candidateAlert.put("interview_date", interviewDate);
			candidateAlert.put("interview_time", "08:30 AM");
			candidateAlert.put("contact_person_name", jobsModel.getContactPersonName());
			candidateAlert.put("contact_person_number", jobsModel.getMobileNumber());
			candidateAlert.put("interview_documents",
					"Bio-data, All Education Certificate, Aadhaar Card,2 Passport Size Photo, Bank Passbook");
			 if (activeProfile.equalsIgnoreCase("prod")) {
				 try {
				 if(lan.equals("ta")) {
					 waAlertService.adminSendInterviewAlertCanTa(candidateAlert);	 
				 }
				 else if(lan.equals("hi")) {
					 waAlertService.adminSendInterviewAlertCanHi(candidateAlert); 
				 }
				 else {
			waAlertService.adminSendInterviewAlertCan(candidateAlert);
				 }
				 }
				 catch(Exception e){
					 waAlertService.adminSendInterviewAlertCan(candidateAlert);
				 }
			 }

			Integer canLimit = candidate.getJobLimit();
			canLimit -= 1;
			candidate.setJobLimit(canLimit);
			candidateRepository.save(candidate);

			EmployerTimeline employerTimeline = new EmployerTimeline();
			Date currentDate1 = new Date();
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yyyy");
			String formattedDate1 = dateFormat2.format(currentDate1);
			String eventDescription = candidateModel.getFirstName() + " " +
					"<b>" + jobsModel.getJobCategory() + " Interview Scheduled on " + formattedDate1 + "</b>";
			employerTimeline.setEmpId(emp.getId());
			employerTimeline.setEmpLeadId(0);
			employerTimeline.setEventName("Interview Scheduled");
			employerTimeline.setEventDescription(eventDescription);
			employerTimelineRepository.save(employerTimeline);

			
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "interview alert sent successfully");
			map.put("candidateWhatsappNUmber", "91" + candidate.getWhatsappNumber());
			map.put("candidateName", candidate.getFirstName());
		    map.put("company_name", jobsModel.getCompanyName());
			map.put("area", jobsModel.getArea());
			map.put("city", jobsModel.getJobLocation());
			map.put("interview_date", interviewDate);
			map.put("interview_time", "08:30 AM");
			map.put("contact_person_name", jobsModel.getContactPersonName());
			map.put("contact_person_number", jobsModel.getMobileNumber());
			return new ResponseEntity<>(map, HttpStatus.OK);

		}

		HashMap<String, Object> map = new HashMap<>();
		map.put("code", 400);
		map.put("message", "error");
		return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

	}

	public String getJobLink(int jobId) {
		String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseJSApiKey;

		DeeplinkRequest dl = new DeeplinkRequest();
		dl.setLongDynamicLink(firebaseJSHost + "/?link=" + firebaseJSHost + "/jobDetails/" + 0 + "/" + jobId + "&apn="
				+ firebaseJSPackage);

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
	
	@PutMapping("/interviewStatus")
	public ResponseEntity<?> updateStatus(
	        @RequestParam int id,
	        @RequestParam String statusField,
	        @RequestParam boolean statusFlag,
	        @RequestParam Long adminId,
	        @RequestParam(required =false) String date,
	        @RequestParam (required =false) String notes){
	       
	    Map<String, String> response = new HashMap<>();

	    Optional<CanInterviewsModel> optionalModel = caninterviewrepository.findById(id);
		CanInterviewsModel ci =caninterviewrepository.findById(id).get();
		CandidateModel candidate =candidateRepository.findById(ci.getCanId()).get();
		Optional<JobsModel> jobDetails = jobRepository.findById(ci.getJobId());
		JobsModel job = jobDetails.get();
		EmployerModel emp =employerRepository.findById(job.getEmployerId()).get();
        Optional<Admin>admin = adminRepository.findById(Long.valueOf(adminId));
        
        Admin a =admin.get();
        String module = a.getModule();

	    if (optionalModel.isPresent()) {
	        CanInterviewsModel model = optionalModel.get();

	        if ("isJoined".equals(statusField)) {
	            model.setJoined(statusFlag);
	            model.setInterviewCurrentStatus(6);
	            model.setJoinedOn(String.valueOf(LocalDateTime.now()));
	            
	            int jobId = model.getJobId();
	            
	            JobsModel jobsModel = jobRepository.getById(jobId);

	            if (statusFlag) {
	               
	                if (jobsModel.getCanResponseCount() > 0) {
	                    jobsModel.setCanResponseCount(jobsModel.getCanResponseCount() - 1);
	                }

	              
	                if (jobsModel.getCanResponseCount() == 0) {
	                    jobsModel.setJobStatus("C");
	                    jobsModel.setJobClosedByAdminId(adminId);
						jobsModel.setJobClosedTime(String.valueOf(LocalDateTime.now()));
					}
	                
	                jobRepository.save(jobsModel);

					EmployerTimeline employerTimeline = new EmployerTimeline();
					Date currentDate1 = new Date();
					SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
					String formattedDate1 = dateFormat1.format(currentDate1);
					String eventDescription = "<b>" + candidate.getFirstName() + "</b> " + job.getJobCategory() + " joined on " + formattedDate1;
					employerTimeline.setEmpId(emp.getId());
					employerTimeline.setEmpLeadId(0);
					employerTimeline.setEventName("Position Closed");
					employerTimeline.setEventDescription(eventDescription);
					employerTimeline.setNotes(notes);
					employerTimelineRepository.save(employerTimeline);
	            }


				CandidateTimeLine can = new CandidateTimeLine();

				Date currentDate = new Date();
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
				String formattedDate = dateFormat1.format(currentDate);
				can.setCanId(ci.getCanId());
				can.setNotes(notes);
				can.setEventName("Interview joined");
				can.setEventDescription("Interview Joined for <b>" + emp.getCompanyName() + "</b> on " + formattedDate + " by " + a.getUserName());

				candidateTimeLineRepository.save(can);
				
				 //Joined Counts
				candidateAnalyticsService.joinedCounts(adminId,LocalDate.now());
	            
	        } else if ("isSelected".equals(statusField)) {
	            model.setSelected(statusFlag);
	            model.setInterviewCurrentStatus(3);
	            model.setSelectedOn(String.valueOf(LocalDateTime.now()));
	            
	            //InterviewSelected Counts
				candidateAnalyticsService.companySelectedCounts(adminId,LocalDate.now());

				CandidateTimeLine can = new CandidateTimeLine();

				Date currentDate = new Date();
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
				String formattedDate = dateFormat1.format(currentDate);
				can.setCanId(ci.getCanId());
				can.setNotes(notes);
				can.setEventName("Interview selected");
				can.setEventDescription("Interview Selected for <b>" + emp.getCompanyName() + "</b> on " + formattedDate + " by " + a.getUserName());

				candidateTimeLineRepository.save(can);

	        } else if ("isAttended".equals(statusField)) {
	            model.setAttended(statusFlag);
	            model.setInterviewCurrentStatus(1);
	            model.setAttendedOn(LocalDateTime.now());
	            
	            List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(Long.valueOf(adminId));

	            //InterviewAttended Counts
				candidateAnalyticsService.interviewAttendedCounts(adminId,LocalDate.now());
				
				CandidateTimeLine can = new CandidateTimeLine();

				Date currentDate = new Date();
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
				String formattedDate = dateFormat1.format(currentDate);
				can.setCanId(ci.getCanId());
				can.setNotes(notes);
				can.setEventName("Interview attended");
				can.setEventDescription("Interview Attended for <b>" + emp.getCompanyName() + "</b> on " + formattedDate + " by " + a.getUserName());

				candidateTimeLineRepository.save(can);
	        }
	        else if ("isNotSelected".equals(statusField)) {
	            model.setNotSelected(statusFlag);
	            model.setInterviewCurrentStatus(4);
	            model.setNotSelectedOn(String.valueOf(LocalDateTime.now()));
	            
	            
	            List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(Long.valueOf(adminId));

	            if (!adminAnalyticsList.isEmpty()) {
	                // Check if the createdOn date is the same as the current date
	                LocalDate currentDate = LocalDate.now();
	                boolean dateMatch = false;

	                for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
	                    LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
	                    if (currentDate.isEqual(createdOnDate)) {
	                        dateMatch = true;
	                        adminAnalytics.setCanInterviewNotSelectedCount(
	                            adminAnalytics.getCanInterviewNotSelectedCount() != null
	                                ? adminAnalytics.getCanInterviewNotSelectedCount() + 1
	                                : 1
	                        );
	                    }
	                }

	                if (!dateMatch) {
	          
	                    // If the dates are different for all records, insert a new record
	                    AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
	                    newAdminAnalytics.setAdminId(Long.valueOf(adminId));
	                    newAdminAnalytics.setModule(module);
	                    newAdminAnalytics.setCanInterviewNotSelectedCount(1);
	                    adminAnalyticsList.add(newAdminAnalytics);
	                }

	                adminAnalyticsRepository.saveAll(adminAnalyticsList);
	            } else {
	              
	                // If there are no existing records for the adminId, insert a new record
	                AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
	                adminAnalytics.setAdminId(Long.valueOf(adminId));
	                adminAnalytics.setModule(module);
	                adminAnalytics.setCanInterviewNotSelectedCount(1);
	                adminAnalyticsRepository.save(adminAnalytics);
	            }
				CandidateTimeLine can = new CandidateTimeLine();

				Date currentDate = new Date();
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
				String formattedDate = dateFormat1.format(currentDate);
				can.setCanId(ci.getCanId());
				can.setNotes(notes);
				can.setEventName("Interview rejected");
				can.setEventDescription("Interview Rejected for <b>" + emp.getCompanyName() + "</b> on " + formattedDate + " by " + a.getUserName());

				candidateTimeLineRepository.save(can);

	        }
	        else if ("isNotAttended".equals(statusField)) {
	            model.setNotAttended(statusFlag);
	            model.setInterviewCurrentStatus(2);
	            model.setNotAttendedOn(String.valueOf(LocalDateTime.now()));
	            
	            
	            List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(Long.valueOf(adminId));

	            if (!adminAnalyticsList.isEmpty()) {
	                // Check if the createdOn date is the same as the current date
	                LocalDate currentDate = LocalDate.now();
	                boolean dateMatch = false;

	                for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
	                    LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
	                    if (currentDate.isEqual(createdOnDate)) {
	                        dateMatch = true;
	                        adminAnalytics.setCanInterviewNotAttendedCount(
	                            adminAnalytics.getCanInterviewNotAttendedCount() != null
	                                ? adminAnalytics.getCanInterviewNotAttendedCount() + 1
	                                : 1
	                        );
	                    }
	                }

	                if (!dateMatch) {
	          
	                    // If the dates are different for all records, insert a new record
	                    AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
	                    newAdminAnalytics.setAdminId(Long.valueOf(adminId));
	                    newAdminAnalytics.setModule(module);
	                    newAdminAnalytics.setCanInterviewNotAttendedCount(1);
	                    adminAnalyticsList.add(newAdminAnalytics);
	                }

	                adminAnalyticsRepository.saveAll(adminAnalyticsList);
	            } else {
	              
	                // If there are no existing records for the adminId, insert a new record
	                AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
	                adminAnalytics.setAdminId(Long.valueOf(adminId));
	                adminAnalytics.setModule(module);
	                adminAnalytics.setCanInterviewNotAttendedCount(1);
	                adminAnalyticsRepository.save(adminAnalytics);
	            }
				CandidateTimeLine can = new CandidateTimeLine();

				Date currentDate = new Date();
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
				String formattedDate = dateFormat1.format(currentDate);
				can.setCanId(ci.getCanId());
				can.setNotes(notes);
				can.setEventName("Interview rejected");
				can.setEventDescription("Interview Rejected for <b>" + emp.getCompanyName() + "</b> on " + formattedDate + " by " + a.getUserName());

				candidateTimeLineRepository.save(can);

	        }
	        else if ("isOfferRejected".equals(statusField)) {
	            model.setOfferRejected(statusFlag);
	            model.setInterviewCurrentStatus(5);
	            model.setOfferRejectedOn(String.valueOf(LocalDateTime.now()));
	            
	         
				CandidateTimeLine can = new CandidateTimeLine();

				Date currentDate = new Date();
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
				String formattedDate = dateFormat1.format(currentDate);
				can.setCanId(ci.getCanId());
				can.setNotes(notes);
				can.setEventName("Interview rejected");
				can.setEventDescription("Interview Rejected for <b>" + emp.getCompanyName() + "</b> on " + formattedDate + " by " + a.getUserName());

				candidateTimeLineRepository.save(can);

	        }
	        else if ("isLeftCompany".equals(statusField)) {
	            model.setLeftTheCompany(statusFlag);
	            model.setInterviewCurrentStatus(8);
	            model.setLeftTheCompanyOn(date);
	            LocalDateTime currentDateTime = LocalDateTime.now();
	            Date currentDates = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());

	            model.setLeftTheCompanyAt(String.valueOf(LocalDateTime.now()));

	           
	           
				CandidateTimeLine can = new CandidateTimeLine();

				Date currentDate = new Date();
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
				String formattedDate = dateFormat1.format(currentDate);
				can.setCanId(ci.getCanId());
				can.setNotes(notes);
				can.setEventName("Left The Company");
				can.setEventDescription("Left The Company for <b>" + emp.getCompanyName() + "</b> on " + formattedDate + " by " + a.getUserName());

				candidateTimeLineRepository.save(can);
	        }
	 
	        caninterviewrepository.save(model);
	        
	        //InterviewAttended Counts
			candidateAnalyticsService.OfferAcceptedCounts(adminId,LocalDate.now());
	        
	        
	        response.put("code", "200");
	        response.put("message", "Status updated successfully");
	        return ResponseEntity.ok(response);
	    } else {
	        response.put("code", "400");
	        response.put("message", "CanInterviewsModel not found with id");
	        return ResponseEntity.badRequest().body(response);
	    }
	}

	@PostMapping(path = "/rescheduleInterviewRequest")
	public ResponseEntity<?> rescheduleInterview(@RequestBody RescheduleInterviewModel model,
			@RequestParam (required=false) String notes){

		Optional<CanInterviewsModel> optional = caninterviewrepository.findById(model.getInterviewId());
		 CanInterviewsModel interviewModel = optional.get();
		 
		    // Now you have the interviewModel and its jobId
		    int jobId = interviewModel.getJobId();
		    int canId=interviewModel.getCanId();
		    long adminId = interviewModel.getAdminId();
		    String companyName = interviewModel.getCompanyName();
		    JobsModel job=jobRepository.findById(jobId).get();
		    int empId = job.getEmployerId();
		    Admin a = adminRepository.findById(adminId).get();
			  String userName = a.getUserName();
		    if (optional.isPresent()) {

			Optional<RescheduleInterviewModel> reModel = rescheduleInterviewRepository
					.findByInterviewId(model.getInterviewId());

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String date = dtf.format(now);
			Date currentDate;

			if (!reModel.isPresent()) {

				CanInterviewsModel existing = optional.get();
//				int adminId = existing.getAdminId();
//				String adminIdString = String.valueOf(adminId);
//				Optional<Admin> adminOptional = adminRepository.findById(adminIdString);
//				Admin admin = adminOptional.get(); 

				Optional<CandidateModel> candidateModel = candidateRepository.findById(existing.getCanId());
				CandidateModel can = candidateModel.get();
				int candidateId=can.getId();
				
				String lan=can.getLanguageKey();

				existing.setRescheduled(true);

				RescheduleInterviewModel reScheModel = new RescheduleInterviewModel();
				reScheModel.setInterviewId(model.getInterviewId());
				reScheModel.setReScheduledOn(model.getReScheduledOn());
				existing.setRescheduledDate(model.getReScheduledOn());
				existing.setInterviewCurrentStatus(7);
				reScheModel.setStartTime(model.getStartTime());
				// reScheModel.setEndTime(model.getEndTime());
				reScheModel.setStatus(model.getStatus());

				try {
					currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
					reScheModel.setReScheduledDate(currentDate);
					existing.setRescheduledDateTime(currentDate);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}

				rescheduleInterviewRepository.save(reScheModel);
				caninterviewrepository.save(existing);
				

				EmployerActivityModel EA = new EmployerActivityModel();
				EA.setEmpId(existing.getJobId());
				EA.setActivity(can.getFirstName() + " - " + can.getJobCategory()
						+ " , <b>interview</b> has been rescheduled!");
				empActivityRepository.save(EA);

				EmpInterviewNotificationModel m = new EmpInterviewNotificationModel();
				m.setInterviewId(existing.getId());
				m.setCanId(existing.getCanId());
				m.setEmpId(job.getEmployerId());
				m.setScheduledDate(reScheModel.getReScheduledOn());
				m.setScheduledTime(reScheModel.getStartTime());
				m.setStatus(model.getStatus());
				m.setNotes("Interview Rescheduled");

				empInterviewNotificationRepository.save(m);
				
				Optional<JobsModel> jobs = jobRepository.findById(jobId);
				JobsModel jobsModel=jobs.get();
				
				Optional<CandidateModel> mod=candidateRepository.findById(canId);
				CandidateModel candidate=mod.get();
				CanInterviewsModel canInterview = new CanInterviewsModel();
				
				HashMap<String, String> data = new HashMap<>();
				data.put("mn", "91" + jobsModel.getMobileNumber());
				data.put("candidate_name", candidate.getFirstName());
				data.put("position_applied", jobsModel.getJobCategory());
				data.put("rescheduled_on", model.getReScheduledOn());
				data.put("interview_time", "10:00 AM - 05:00 PM");
				data.put("candidate_experience",
						candidate.getExperience() + " Years " + candidate.getExpMonths() + "  Month");
				data.put("qualification", candidate.getQualification());
				data.put("skills", candidate.getKeySkill());
				data.put("can_web_link", "https://web.taizo.in/console/candidates?status=int&Cid=" + canInterview.getCanId()
						+ "&jobId=" + canInterview.getJobId());
				
				String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseEmpApiKey;
				DeeplinkRequest dl = new DeeplinkRequest();
				dl.setLongDynamicLink(firebaseEmpHost + "/?link=" + firebaseEmpHost + "/candidateDetails/"
						+ canInterview.getCanId() + "/" + canInterview.getJobId() + "&apn=" + firebaseEmpPackage);
				// System.out.println(ex.getLongDynamicLink());
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
				} catch (Exception ex) {
				}
				data.put("can_link", response.getShortLink());
				 if (activeProfile.equalsIgnoreCase("prod")) {
				waAlertService.adminInterviewAlertEmps(data);
				 }

				 Integer responseCount = jobsModel.getCanResponseCount();
					responseCount -= 1;
					jobsModel.setCanResponseCount(responseCount);
					jobRepository.save(jobsModel);

					HashMap<String, String> datas = new HashMap<>();
					datas.put("mn", "91" + candidate.getMobileNumber());
					datas.put("name", candidate.getFirstName());
					datas.put("company_name", jobsModel.getCompanyName());
					datas.put("area", jobsModel.getArea());
					datas.put("city", jobsModel.getJobLocation());
					datas.put("rescheduled_on", model.getReScheduledOn() );
					datas.put("interview_time", "10:00 AM - 05:00 PM");
					datas.put("contact_person_name", jobsModel.getContactPersonName());
					datas.put("contact_person_number", jobsModel.getMobileNumber());
					datas.put("interview_documents",
							"Bio-data, All Education Certificate, Aadhaar Card, Ration Card, 2 Passport Size Photo, Bank Passbook");
					 if (activeProfile.equalsIgnoreCase("prod")) {
						 try{
						 if(lan.equals("ta")) {
							 waAlertService.adminSendInterviewAlertCanTam(datas);
						 }
						 else if(lan.equals("hi")) {
					    waAlertService.adminSendInterviewAlertCanHin(datas);
						 }
						 else {
							 waAlertService.adminSendInterviewAlertCandidate(datas);
						 }
						 }
						 catch(Exception e) {
							 waAlertService.adminSendInterviewAlertCandidate(datas);
						 }
					 }

					Integer canLimit = candidate.getJobLimit();
					canLimit -= 1;
					candidate.setJobLimit(canLimit);
					candidateRepository.save(candidate);
				 
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Interview Rescheduled");
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {

				CanInterviewsModel existing = optional.get();
				RescheduleInterviewModel reScheModel = reModel.get();

				Optional<CandidateModel> candidateModel = candidateRepository.findById(existing.getCanId());
				CandidateModel can = candidateModel.get();
                 String lan=can.getLanguageKey();
				existing.setRescheduled(true);

				reScheModel.setInterviewId(model.getInterviewId());
				reScheModel.setReScheduledOn(model.getReScheduledOn());
				existing.setRescheduledDate(model.getReScheduledOn());
				reScheModel.setStartTime(model.getStartTime());
				existing.setInterviewCurrentStatus(7);
				// reScheModel.setEndTime(model.getEndTime());
				reScheModel.setStatus(model.getStatus());

				try {
					currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
					existing.setRescheduledDateTime(currentDate);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}

				rescheduleInterviewRepository.save(reScheModel);
				caninterviewrepository.save(existing);

				EmployerActivityModel EA = new EmployerActivityModel();
				EA.setEmpId(job.getEmployerId());
				EA.setActivity(can.getFirstName() + can.getJobCategory() + " interview has been rescheduled!");
				empActivityRepository.save(EA);

				EmpInterviewNotificationModel m = new EmpInterviewNotificationModel();
				m.setInterviewId(existing.getId());
				m.setCanId(existing.getCanId());
				m.setEmpId(job.getEmployerId());
				m.setScheduledDate(reScheModel.getReScheduledOn());
				m.setScheduledTime(reScheModel.getStartTime());
				m.setStatus(model.getStatus());
				m.setNotes("Interview Rescheduled");

				empInterviewNotificationRepository.save(m);
				Optional<JobsModel> jobs = jobRepository.findById(jobId);
				JobsModel jobsModel=jobs.get();
				
				Optional<CandidateModel> mod=candidateRepository.findById(canId);
				CandidateModel candidate=mod.get();
				CanInterviewsModel canInterview = new CanInterviewsModel();
				
				HashMap<String, String> data = new HashMap<>();
				data.put("mn", "91" + jobsModel.getMobileNumber());
				data.put("candidate_name", candidate.getFirstName());
				data.put("position_applied", jobsModel.getJobCategory());
				data.put("rescheduled_on", model.getReScheduledOn());
				data.put("interview_time", "10:00 AM - 05:00 PM");
				data.put("candidate_experience",
						candidate.getExperience() + " Years " + candidate.getExpMonths() + "  Month");
				data.put("qualification", candidate.getQualification());
				data.put("skills", candidate.getKeySkill());
				data.put("can_web_link", "https://web.taizo.in/console/candidates?status=int&Cid=" + canInterview.getCanId()
						+ "&jobId=" + canInterview.getJobId());
				
				String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseEmpApiKey;
				DeeplinkRequest dl = new DeeplinkRequest();
				dl.setLongDynamicLink(firebaseEmpHost + "/?link=" + firebaseEmpHost + "/candidateDetails/"
						+ canInterview.getCanId() + "/" + canInterview.getJobId() + "&apn=" + firebaseEmpPackage);
				// System.out.println(ex.getLongDynamicLink());
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
				} catch (Exception ex) {
				}
				data.put("can_link", response.getShortLink());
				 if (activeProfile.equalsIgnoreCase("prod")) {
				waAlertService.adminInterviewAlertEmps(data);
				 }
				 Integer responseCount = jobsModel.getCanResponseCount();
					responseCount -= 1;
					jobsModel.setCanResponseCount(responseCount);
					jobRepository.save(jobsModel);

					HashMap<String, String> datas = new HashMap<>();
					datas.put("mn", "91" + candidate.getMobileNumber());
					datas.put("name", candidate.getFirstName());
					datas.put("company_name", jobsModel.getCompanyName());
					datas.put("area", jobsModel.getArea());
					datas.put("city", jobsModel.getJobLocation());
					datas.put("rescheduled_on", model.getReScheduledOn() );
					datas.put("interview_time", "10:00 AM - 05:00 PM");
					datas.put("contact_person_name", jobsModel.getContactPersonName());
					datas.put("contact_person_number", jobsModel.getMobileNumber());
					datas.put("interview_documents",
							"Bio-data, All Education Certificate, Aadhaar Card, Ration Card, 2 Passport Size Photo, Bank Passbook");
					 if (activeProfile.equalsIgnoreCase("prod")) {
						 try {
						 if(lan.equals("ta")) {
							 waAlertService.adminSendInterviewAlertCanTam(datas);
						 }
						 else if(lan.equals("hi")) {
					    waAlertService.adminSendInterviewAlertCanHin(datas);
						 }
						 else {
							 waAlertService.adminSendInterviewAlertCandidate(datas);
						 }
						 }
						 catch(Exception e) {
							 waAlertService.adminSendInterviewAlertCandidate(datas);
						 }
					 }

					Integer canLimit = candidate.getJobLimit();
					canLimit -= 1;
					candidate.setJobLimit(canLimit);
					candidateRepository.save(candidate);
					
					CandidateTimeLine canTime = new CandidateTimeLine();
					

					Date currentDates = new Date();
					SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
					String formattedDate = dateFormat1.format(currentDates);
					canTime.setCanId(canId);
					canTime.setCanLeadId(0);
					canTime.setFacebookId(0l);
					canTime.setNotes(notes);
					canTime.setEventName("Interview rescheduled");
					canTime.setEventDescription("Interview rescheduled for <b> " + companyName + "</b> on " + formattedDate + " by " + userName);

					candidateTimeLineRepository.save(canTime);
					
					EmployerTimeline employerTimeline = new EmployerTimeline();
					Date currentDate1 = new Date();
					SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yyyy");
					String formattedDate1 = dateFormat2.format(currentDate1);
					String eventDescription = can.getFirstName() + " " +
							"<b>" + jobsModel.getJobCategory() + " Interview rescheduled on " + formattedDate1 + "</b>";
					employerTimeline.setEmpId(empId);
					employerTimeline.setEmpLeadId(0);
					employerTimeline.setNotes(notes);
					employerTimeline.setEventName("Interview rescheduled");
					employerTimeline.setEventDescription(eventDescription);
					employerTimelineRepository.save(employerTimeline);

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Interview Rescheduled");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			
		
			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Interview Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	@GetMapping("/interviewScheduledCandidates")
	public ResponseEntity<Page<InterviewCandidateDTO>> findInterviewCandidatesByJobIdAndDateRange(
	        @RequestParam Integer jobId,
	        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
	        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
	        @RequestParam(defaultValue = "10") int size,
	        @RequestParam(defaultValue = "0") int page) {

	    LocalDateTime startDateTime = startDate.atStartOfDay();
	    LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

	    // If the end date is the same as the start date, set it to the end of the day
	    if (startDate.equals(endDate)) {
	        endDateTime = endDateTime.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
	    }

	    // Logging to check parameters
	    logger.info("jobId: {}", jobId);
	    logger.info("startDateTime: {}", startDateTime);
	    logger.info("endDateTime: {}", endDateTime);

	    PageRequest pageRequest = PageRequest.of(page, size);

	    Page<CanInterviewsModel> candidatePage = caninterviewrepository.findByJobIdAndAttendedOnBetween(
	            jobId, startDateTime, endDateTime, pageRequest);

	    // Logging to check the number of candidates retrieved
	    logger.info("Number of candidates retrieved: {}", candidatePage.getTotalElements());

	    List<InterviewCandidateDTO> candidateDTOList = candidatePage.getContent().stream()
	            // Filter candidates with isAttended as true (including null check)
	            .filter(canInterview -> Boolean.TRUE.equals(canInterview.isAttended()))
	            .map(canInterview -> {
	                Optional<CandidateModel> can = candidateRepository.findById(canInterview.getCanId());
	                String candidateName = can.map(CandidateModel::getFirstName).orElse(null);

	                return new InterviewCandidateDTO(canInterview, candidateName);
	            })
	            .collect(Collectors.toList());

	    Page<InterviewCandidateDTO> resultPage = new PageImpl<>(candidateDTOList, pageRequest, candidatePage.getTotalElements());

	    return new ResponseEntity<>(resultPage, HttpStatus.OK);
	}



	
	@GetMapping(value = "/jobs/{id}")
	public ResponseEntity<?> getNotiCount(@PathVariable("id") int id) throws ResourceNotFoundException {
	    String statusInterviewScheduled = "I";
	 
	    
	    // Get counts for different interview statuses
	    long countInterviewScheduled = caninterviewrepository.countByJobIdAndStatus(id, statusInterviewScheduled);
	    long countInterviewAttended = caninterviewrepository.countByJobIdAndIsAttended(id, true);
	    long countInterviewSelected = caninterviewrepository.countByJobIdAndIsSelected(id, true);
	    long countInterviewJoined = caninterviewrepository.countByJobIdAndIsJoined(id, true);
	    long countInterviewOfferRejected = caninterviewrepository.countByJobIdAndIsOfferRejected(id, true);
	    long countInterviewIsNotAttended = caninterviewrepository.countByJobIdAndIsNotAttended(id, true);
	    long countInterviewIsNotSelected = caninterviewrepository.countByJobIdAndIsNotSelected(id, true);
	    
	    HashMap<String, Long> count = new HashMap<>();

	    count.put("InterviewScheduledCount", countInterviewScheduled);
	    count.put("InterviewSelectedCount", countInterviewSelected);
	    count.put("InterviewJoinedCount", countInterviewJoined);
	    count.put("InterviewRejectedCount", countInterviewOfferRejected);
	    count.put("InterviewAttendedCount", countInterviewAttended);
	    count.put("InterviewIsNotAttended", countInterviewIsNotAttended);
	    count.put("InterviewIsNotSelected", countInterviewIsNotSelected);
	    
	    ArrayList<HashMap<String, Long>> al = new ArrayList<>();
	    al.add(count);
	    HashMap<String, Object> map = new HashMap<>();
	    map.put("jobOverview", al);
	    map.put("jobDetails", jobService.getById(id));
	    return new ResponseEntity<>(map, HttpStatus.OK);
	}








	
	
}
