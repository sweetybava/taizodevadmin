package com.taizo.controller.webemp;

import java.net.URISyntaxException; 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.CandidateModel;
import com.taizo.model.DeeplinkRequest;
import com.taizo.model.DeeplinkSuffix;
import com.taizo.model.EmpInterviewNotificationModel;
import com.taizo.model.EmployerActivityModel;
import com.taizo.model.EmployerApplication;
import com.taizo.model.EmployerModel;
import com.taizo.model.FirebaseShortLink;
import com.taizo.model.InterviewAddressesModel;
import com.taizo.model.InterviewsModel;
import com.taizo.model.JobsModel;
import com.taizo.model.RescheduleInterviewModel;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.EmpActivityRepository;
import com.taizo.repository.EmpInterviewNotificationRepository;
import com.taizo.repository.EmployerApplicationRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.InterviewAddressRepository;
import com.taizo.repository.InterviewRepository;
import com.taizo.repository.JobRepository;
import com.taizo.repository.RescheduleInterviewRepository;
import com.taizo.service.InterviewService;
import com.taizo.service.WAAlertService;

@CrossOrigin
@RestController
@RequestMapping("/webEmployer")
public class WebEmployerInterviewController {

	@Autowired
	InterviewService interviewService;

	@PersistenceContext
	EntityManager em;

	@Autowired
	InterviewAddressRepository interviewAddressRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	EmployerApplicationRepository employerApplicationRepository;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	EmpActivityRepository empActivityRepository;

	@Autowired
	InterviewRepository interviewRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	EmpInterviewNotificationRepository empInterviewNotificationRepository;

	@Autowired
	RescheduleInterviewRepository rescheduleInterviewRepository;

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
					// TODO Auto-generated catch block
					e1.printStackTrace();
					strDate = i.getScheduled_on();
				}

				HashMap<String, String> d = new HashMap<>();
				d.put("mn", "91" + String.valueOf(CModel.get().getMobileNumber()));
				d.put("name", CModel.get().getFirstName());
				d.put("companyName", jobsModel.get().getCompanyName());
				d.put("address", jobsModel.get().getJobLocationAddr());
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

	@SuppressWarnings("unchecked")
	@GetMapping(path = "/acceptedCanDetails")
	public ArrayList<HashMap<String, String>> AcceptedInterviewDetails(@RequestParam("emp_id") final int empId,
			@RequestParam(value = "job_category", required = false) final String jobCategory,
			@RequestParam(value = "location", required = false) final String location,
			@RequestParam(value = "exp", required = false) final int exp,
			@RequestParam("start_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
			@RequestParam("end_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
			@RequestParam("page") final int pageNumber, @RequestParam("size") final int pageSize)
			throws ResourceNotFoundException {
		String status = "A";

		List<Object[]> results = null;

		results = em.createQuery(
				" SELECT j.id as jobId,c.id as candidateId,c.firstName,c.jobCategory,c.age,c.experience,c.currentState,c.currentCity,ja.scheduledDate,u.profilePic FROM InterviewsModel ja "
						+ "left join EmployerModel e on e.id = ja.empId " + "left join JobsModel j on j.id = ja.jobId "
						+ "left join CandidateModel c on c.id  =ja.canId "
						+ "left join UserModel u on  u.id = c.userId " + "where ja.status = :status "
						+ "and ja.empId = :emp_id and ja.scheduledDate between :startdate and :enddate "
						+ "and (c.currentState=:location or :location IS NULL) "
						+ "and (c.jobCategory=:jobCategory or :jobCategory IS NULL) "
						+ "and (c.experience=:exp or :exp = 0)",
				Object[].class).setParameter("emp_id", empId).setParameter("status", status)
				.setParameter("startdate", startDate).setParameter("enddate", endDate)
				.setParameter("jobCategory", jobCategory).setParameter("location", location).setParameter("exp", exp)
				.setFirstResult((pageNumber - 1) * pageSize).setMaxResults(pageSize).getResultList();
		if (results.isEmpty()) {
			throw new ResourceNotFoundException("Candidates not found.");
		}
		Collections.reverse(results);

		ArrayList<HashMap<String, String>> al = new ArrayList<>();
		for (Object[] row : results) {
			HashMap<String, String> count = new HashMap<>();
			count.put("jobId", String.valueOf(row[0]));
			count.put("candidateId", String.valueOf(row[1]));
			count.put("firstName", String.valueOf(row[2]));
			count.put("jobCategory", String.valueOf(row[3]));
			count.put("age", String.valueOf(row[4]));
			count.put("experience", String.valueOf(row[5]));
			count.put("currentState", String.valueOf(row[6]));
			count.put("currentCity", String.valueOf(row[7]));
			count.put("callTime", String.valueOf(row[8]));
			count.put("profilePic", String.valueOf(row[9]));

			al.add(count);
		}

		return al;
	}

	@SuppressWarnings("unchecked")
	@GetMapping(path = "/scheduledCanDetails")
	public ArrayList<HashMap<String, String>> getscheduledInterviewDetails(@RequestParam("emp_id") final int empId,
			@RequestParam(value = "can_type", required = false) final String canType,
			@RequestParam(value = "job_category", required = false) final String jobCategory,
			@RequestParam(value = "location", required = false) final String location,
			@RequestParam(value = "exp", required = false) final int exp,
			@RequestParam("start_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
			@RequestParam("end_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
			@RequestParam("page") final int pageNumber, @RequestParam("size") final int pageSize)
			throws ResourceNotFoundException, ParseException {
		String status = "S";

		List<Object[]> results = null;

		Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse("2024-05-03");

		results = em.createQuery(
				" SELECT j.id as jobId,c.id as candidateId,c.firstName,c.jobCategory,c.age,c.experience,c.currentState,c.currentCity,ja.scheduledDate,u.profilePic,ja.status,j.jobCategory as jobRole,c.mobileNumber,c.whatsappNumber,c.industry,c.candidateType FROM InterviewsModel ja "
						+ "left join EmployerModel e on e.id = ja.empId " + "left join JobsModel j on j.id = ja.jobId "
						+ "left join CandidateModel c on c.id  =ja.canId "
						+ "left join UserModel u on  u.id = c.userId " + "where ja.empId = :emp_id "
						+ "and ja.scheduledDate between :startdate and :enddate "
						+ "and (c.currentState=:location or :location IS NULL) "
						+ "and (j.jobCategory=:jobCategory or :jobCategory IS NULL) "
						+ "and (c.candidateType=:canType or :canType IS NULL) " + "and (c.experience=:exp or :exp = 0)",
				Object[].class).setParameter("emp_id", empId).setParameter("startdate", startDate)
				.setParameter("enddate", date1).setParameter("jobCategory", jobCategory)
				.setParameter("canType", canType).setParameter("location", location).setParameter("exp", exp)
				.setFirstResult((pageNumber - 1) * pageSize).setMaxResults(pageSize).getResultList();

		if (results.isEmpty()) {
			throw new ResourceNotFoundException("Candidates not found.");
		}
		Collections.reverse(results);

		ArrayList<HashMap<String, String>> al = new ArrayList<>();
		for (Object[] row : results) {
			HashMap<String, String> count = new HashMap<>();
			count.put("jobId", String.valueOf(row[0]));
			count.put("candidateId", String.valueOf(row[1]));
			count.put("firstName", String.valueOf(row[2]));
			count.put("jobCategory", String.valueOf(row[3]));
			count.put("age", String.valueOf(row[4]));
			count.put("experience", String.valueOf(row[5]));
			count.put("currentState", String.valueOf(row[6]));
			count.put("currentCity", String.valueOf(row[7]));
			count.put("callTime", String.valueOf(row[8]));
			count.put("profilePic", String.valueOf(row[9]));
			count.put("status", String.valueOf(row[10]));
			count.put("jobRole", String.valueOf(row[11]));
			count.put("mobileNumber", String.valueOf(row[12]));
			count.put("whatsappNumber", String.valueOf(row[13]));
			count.put("industry", String.valueOf(row[14]));
			count.put("candidateType", String.valueOf(row[15]));

			al.add(count);
		}

		return al;
	}
	
	@GetMapping(path = "/scheduledInterviewDetails")
	public List<Map<String, Object>> getscheduledCanInterviewDetails(@RequestParam("emp_id") final int empId,
			@RequestParam(value = "can_type", required = false) final String canType,
			@RequestParam(value = "job_category", required = false) final String jobCategory,
			@RequestParam(value = "location", required = false) final String location,
			@RequestParam(value = "exp", required = false) final int exp,
			@RequestParam("start_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
			@RequestParam("end_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
			@RequestParam("page") final int pageNumber, @RequestParam("size") final int pageSize)
			throws ResourceNotFoundException, ParseException {
		
		List<Map<String, Object>> details = jobRepository.findByScheduledInterviewDetails(empId, startDate, endDate,jobCategory);

		if (details.isEmpty()) {
			throw new ResourceNotFoundException("Candidates not found.");
		}

		return details;
	}
	
	@GetMapping(path = "/interviewDetails")
	public Map<String, Object> getscheduledCanInterviewDetails(@RequestParam("emp_id") final int empId,
			@RequestParam(value = "status", required = false) final String status,
			@RequestParam(value = "interview_id", required = false) final int interviewId)
			throws ResourceNotFoundException, ParseException {
		
		Map<String, Object> details = jobRepository.findByNewInterviewDetails(interviewId,status);

		if (details.isEmpty()) {
			throw new ResourceNotFoundException("Interview not found.");
		}

		return details;
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
