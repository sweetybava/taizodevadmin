package com.taizo.controller.employer;

import java.net.URISyntaxException; 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.taizo.model.CanInterviewNotificationModel;
import com.taizo.model.CanLanguageModel;
import com.taizo.model.CandidateCallsModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.CfgInterRequiredDocModel;
import com.taizo.model.CloudwatchLogEventModel;
import com.taizo.model.DeeplinkRequest;
import com.taizo.model.DeeplinkSuffix;
import com.taizo.model.EmpInterviewNotificationModel;
import com.taizo.model.EmployerActivityModel;
import com.taizo.model.EmployerApplication;
import com.taizo.model.EmployerCallModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.FirebaseShortLink;
import com.taizo.model.InterviewAddressesModel;
import com.taizo.model.InterviewsModel;
import com.taizo.model.JobsModel;
import com.taizo.model.RescheduleInterviewModel;
import com.taizo.model.StateCityModel;
import com.taizo.model.UserModel;
import com.taizo.repository.CanInterviewNotificationRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.EmpActivityRepository;
import com.taizo.repository.EmpInterviewNotificationRepository;
import com.taizo.repository.EmployerApplicationRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.InterviewAddressRepository;
import com.taizo.repository.InterviewRepository;
import com.taizo.repository.InterviewRequiredDocRepository;
import com.taizo.repository.JobRepository;
import com.taizo.repository.RescheduleInterviewRepository;
import com.taizo.repository.UserRepository;
import com.taizo.service.InterviewService;
import com.taizo.service.WAAlertService;

@CrossOrigin
@RestController
@RequestMapping("/employer")
public class InterviewController {

	private static final Logger logger = LoggerFactory.getLogger(InterviewController.class);

	@Autowired
	InterviewService interviewService;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	EmpActivityRepository empActivityRepository;

	@Autowired
	InterviewRepository interviewRepository;

	@Autowired
	InterviewAddressRepository interviewAddressRepository;

	@Autowired
	InterviewRequiredDocRepository interviewReqDocRepository;

	@Autowired
	RescheduleInterviewRepository rescheduleInterviewRepository;

	@Autowired
	EmpInterviewNotificationRepository empInterviewNotificationRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	EmployerApplicationRepository employerApplicationRepository;
	
	@Autowired
	WAAlertService waAlertService;

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Value("${firebase.js.api.key}")
	private String firebaseJSApiKey;

	@Value("${firebase.deeplink.js.hostname}")
	private String firebaseJSHost;

	@Value("${firebase.jsapp.package.name}")
	private String firebaseJSPackage;

	@GetMapping(path = "/interviewRequiredDoc")
	public ResponseEntity<?> getInterviewRequiredDoc() {

		List<CfgInterRequiredDocModel> details = interviewReqDocRepository.findAllByActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "interviewRequiredDoc Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/interview")
	public ResponseEntity<?> createInterview(@RequestBody Map<String, Object> requestBody) {

		Optional<EmployerModel> existingUser = employerRepository.findById((Integer) requestBody.get("emp_id"));
		if (existingUser.isPresent()) {

			InterviewAddressesModel a = new InterviewAddressesModel();
			a.setLandmark((String) requestBody.get("landmark"));
			a.setEmpId((Integer) requestBody.get("emp_id"));
			a.setLatitude((String) requestBody.get("latitude"));
			a.setLongitude((String) requestBody.get("longitude"));
			a.setAddress((String) requestBody.get("address"));
			a.setActive(true);

			interviewAddressRepository.save(a);

			InterviewsModel i = new InterviewsModel();
			i.setEmpId((Integer) requestBody.get("emp_id"));
			i.setJobId((Integer) requestBody.get("job_id"));
			i.setCanId((Integer) requestBody.get("can_id"));
			i.setContactPersonName((String) requestBody.get("contact_person_name"));
			i.setMobileNumber(Long.parseLong((String) requestBody.get("mobile_number")));
			i.setScheduled_on((String) requestBody.get("scheduled_on"));
			i.setStartTime((String) requestBody.get("start_tm"));
			i.setDocuments((String) requestBody.get("documents"));
			i.setStatus("S");
			i.setAddressId(a.getId());
			i.setActive(true);

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String date = dtf.format(now);
			Date currentDate;
			try {
				currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
				i.setScheduledDate(currentDate);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}

			interviewRepository.save(i);

			EmployerApplication details = employerApplicationRepository.findByEmployerIdAndStatus(i.getEmpId(),
					i.getCanId(), i.getJobId());
			if (details != null) {
				details.setStatus("L");
				employerApplicationRepository.save(details);
			} else {
				EmployerApplication e = new EmployerApplication();

				e.setEmployerId(i.getEmpId());
				e.setCandidateId(i.getCanId());
				e.setJobId(i.getJobId());
				e.setStatus("L");
				employerApplicationRepository.save(e);
			}

			EmpInterviewNotificationModel e = new EmpInterviewNotificationModel();
			e.setInterviewId(i.getId());
			e.setCanId((Integer) requestBody.get("can_id"));
			e.setEmpId((Integer) requestBody.get("emp_id"));
			e.setScheduledDate(i.getScheduled_on());
			e.setScheduledTime(i.getStartTime());
			e.setNotes("Interview Scheduled");
			e.setStatus("S");

			empInterviewNotificationRepository.save(e);

			Optional<JobsModel> jobsModel = jobRepository.findById((Integer) requestBody.get("job_id"));
			Optional<CandidateModel> CModel = candidateRepository.findById((Integer) requestBody.get("can_id"));

			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(jobsModel.get().getEmployerId());
			EA.setActivity(CModel.get().getFirstName() + " - " + CModel.get().getJobCategory()
					+ " , <b>interview</b> has been scheduled!");
			empActivityRepository.save(EA);
			
			if (activeProfile.equalsIgnoreCase("prod")) {
				String link = getJobLink(jobsModel.get().getId());
				String jobLink = link.substring(link.lastIndexOf("/") + 1);

				SimpleDateFormat formatter1 = new SimpleDateFormat("dd MMM yyyy");
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				Date date1 = null;
				String strDate = null;
				try {
					date1 = formatter.parse(i.getScheduled_on());
					strDate = formatter1.format(date1);
				} catch (ParseException e1) {
					e1.printStackTrace();
					strDate = i.getScheduled_on();
				}

				HashMap<String, String> d = new HashMap<>();
				d.put("mn", "91" + String.valueOf(CModel.get().getMobileNumber()));
				d.put("name", CModel.get().getFirstName());
				d.put("companyName", jobsModel.get().getCompanyName());
				d.put("address", a.getAddress());
				d.put("interviewDate", strDate);
				d.put("interviewTime", i.getStartTime());
				d.put("contactPersonName", jobsModel.get().getContactPersonName());
				d.put("contactPersonNumber", jobsModel.get().getMobileNumber());
				d.put("languageKey", CModel.get().getLanguageKey());
				d.put("jobLink", jobLink);

				waAlertService.sendInterviewAlert(d);
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Interview Scheduled Successfully");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Faild to update");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}

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

	@GetMapping("/AllNotification")
	public ResponseEntity<?> getEmpInterviewDetails(@RequestParam("emp_id") final int empId) {

		List<Map<String, Object>> details = interviewRepository.getEmpInterviewNotification(empId);

		if (!details.isEmpty() && details != null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Notifications Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping(path = "/scheduledInterviewDetailsByDate")
	public ResponseEntity<?> getinterviewDetailsByDate(@RequestParam("emp_id") final int empId,
			@RequestParam("start_date") final String startdate, @RequestParam("end_date") final String endDate,
			@RequestParam(value = "job_category", required = false) final String jobRole) {

		List<Map<String, Object>> details;
		if(jobRole!=null && !jobRole.isEmpty()) {
		 details = interviewRepository.getinterviewDetailsByDate(empId, startdate, endDate,jobRole);
		} else {
		 details = interviewRepository.getinterviewDetailsByDate(empId, startdate, endDate,null);
		}
		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Interviews Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/scheduledInterviewDetailsByJobId")
	public ResponseEntity<?> getjobScheduledInterviewDetails(@RequestParam("emp_id") final int empId,
			@RequestParam("job_id") final int jobId, @RequestParam("start_date") final String startdate,
			@RequestParam("end_date") final String endDate) {

		List<Map<String, Object>> details = interviewRepository.findjobScheduledInterviewDetails(empId, jobId,
				startdate, endDate);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Interview Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping(path = "/appliedCanDetailsByJobId")
	public ResponseEntity<?> getjobAppliedCanDetails(@RequestParam("emp_id") final int empId,
			@RequestParam("job_id") final int jobId, @RequestParam("start_date") final String startdate,
			@RequestParam("end_date") final String endDate) {

		List<Map<String, Object>> details = interviewRepository.findjobAppliedCanDetails(empId, jobId, startdate,
				endDate);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Candidates Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping(path = "/interviewDetailsById")
	public ResponseEntity<?> getinterviewDetailsById(@RequestParam("interview_id") final int interviewId) {

		Map<String, Object> details = interviewRepository.findInterviewByID(interviewId);

		if (!details.isEmpty() && details != null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Interview Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(path = "/rescheduleInterviewRequest")
	public ResponseEntity<?> rescheduleInterview(@RequestBody RescheduleInterviewModel model) {

		Optional<InterviewsModel> optional = interviewRepository.findById(model.getInterviewId());
		if (optional.isPresent()) {

			Optional<RescheduleInterviewModel> reModel = rescheduleInterviewRepository
					.findByInterviewId(model.getInterviewId());

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String date = dtf.format(now);
			Date currentDate;

			if (!reModel.isPresent()) {

				InterviewsModel existing = optional.get();

				Optional<CandidateModel> candidateModel = candidateRepository.findById(existing.getCanId());
				CandidateModel can = candidateModel.get();

				existing.setRescheduled(true);

				RescheduleInterviewModel reScheModel = new RescheduleInterviewModel();
				reScheModel.setInterviewId(model.getInterviewId());
				reScheModel.setReScheduledOn(model.getReScheduledOn());
				reScheModel.setStartTime(model.getStartTime());
				// reScheModel.setEndTime(model.getEndTime());
				reScheModel.setStatus(model.getStatus());

				try {
					currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
					reScheModel.setReScheduledDate(currentDate);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}

				rescheduleInterviewRepository.save(reScheModel);
				interviewRepository.save(existing);

				EmployerActivityModel EA = new EmployerActivityModel();
				EA.setEmpId(existing.getEmpId());
				EA.setActivity(can.getFirstName() + " - " + can.getJobCategory()
						+ " , <b>interview</b> has been rescheduled!");
				empActivityRepository.save(EA);

				EmpInterviewNotificationModel m = new EmpInterviewNotificationModel();
				m.setInterviewId(existing.getId());
				m.setCanId(existing.getCanId());
				m.setEmpId(existing.getEmpId());
				m.setScheduledDate(reScheModel.getReScheduledOn());
				m.setScheduledTime(reScheModel.getStartTime());
				m.setStatus(model.getStatus());
				m.setNotes("Interview Rescheduled");

				empInterviewNotificationRepository.save(m);

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Interview Rescheduled");
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {

				InterviewsModel existing = optional.get();
				RescheduleInterviewModel reScheModel = reModel.get();

				Optional<CandidateModel> candidateModel = candidateRepository.findById(existing.getCanId());
				CandidateModel can = candidateModel.get();

				existing.setRescheduled(true);

				reScheModel.setInterviewId(model.getInterviewId());
				reScheModel.setReScheduledOn(model.getReScheduledOn());
				reScheModel.setStartTime(model.getStartTime());
				// reScheModel.setEndTime(model.getEndTime());
				reScheModel.setStatus(model.getStatus());

				try {
					currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
					reScheModel.setReScheduledDate(currentDate);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}

				rescheduleInterviewRepository.save(reScheModel);
				interviewRepository.save(existing);

				EmployerActivityModel EA = new EmployerActivityModel();
				EA.setEmpId(existing.getEmpId());
				EA.setActivity(can.getFirstName() + can.getJobCategory() + " interview has been rescheduled!");
				empActivityRepository.save(EA);

				EmpInterviewNotificationModel m = new EmpInterviewNotificationModel();
				m.setInterviewId(existing.getId());
				m.setCanId(existing.getCanId());
				m.setEmpId(existing.getEmpId());
				m.setScheduledDate(reScheModel.getReScheduledOn());
				m.setScheduledTime(reScheModel.getStartTime());
				m.setStatus(model.getStatus());
				m.setNotes("Interview Rescheduled");

				empInterviewNotificationRepository.save(m);

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
}