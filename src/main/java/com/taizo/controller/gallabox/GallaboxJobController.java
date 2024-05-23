package com.taizo.controller.gallabox;

import java.net.URISyntaxException; 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.taizo.repository.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.taizo.model.Admin;
import com.taizo.model.CanInterviewsModel;
import com.taizo.model.CanLeadModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.CfgStateCityModel;
import com.taizo.model.DeeplinkRequest;
import com.taizo.model.DeeplinkSuffix;
import com.taizo.model.EmployerModel;
import com.taizo.model.FirebaseShortLink;
import com.taizo.model.InterviewDates;
import com.taizo.model.JobApplicationModel;
import com.taizo.model.JobCanResponsesModel;
import com.taizo.model.JobsModel;
import com.taizo.service.WAAlertService;

@RestController
@CrossOrigin
@RequestMapping("/chatbot")
public class GallaboxJobController {

	@Autowired
	JobRepository jobRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	CanInterviewRepository canInterviewRepository;

	@Autowired
	JobCanResponsesRepository jobCanResponsesRepository;
	
	@Autowired
	ExotelCallController exotelCallController;
	
	@Autowired
	AdminRepository adminRepository;

	@Autowired
	WAAlertService waAlertService;

	@PersistenceContext
	EntityManager em;

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Value("${firebase.emp.api.key}")
	private String firebaseEmpApiKey;

	@Value("${firebase.deeplink.emp.hostname}")
	private String firebaseEmpHost;

	@Value("${firebase.empapp.package.name}")
	private String firebaseEmpPackage;

	@GetMapping(path = "/job")
	public ResponseEntity<?> getMatchedJob(@RequestParam("phone_number") final String mobileNumber) {

		long mn = Long.parseLong(mobileNumber);
		CandidateModel can = candidateRepository.findByMobileNumber(mn);

		HashSet<String> jobIDs = new HashSet<String>();

		List<CanInterviewsModel> list = canInterviewRepository.findByCanId(can.getId());

		list.forEach(interview -> {
			jobIDs.add(String.valueOf(interview.getJobId()));
		});

		List<JobApplicationModel> canJobs = null;
		List<Integer> empJobIDs = new ArrayList<Integer>();

		canJobs = em.createQuery("SELECT j FROM JobApplicationModel j WHERE j.candidateId IN :ids")
				.setParameter("ids", can.getId()).getResultList();

		if (canJobs.size() > 0) {
			canJobs.forEach(job -> {
				jobIDs.add(String.valueOf(job.getJobId()));

				Optional<JobsModel> j1 = jobRepository.findById(job.getJobId());
				List<JobsModel> jobList = jobRepository.findByJobDetails(j1.get().getEmployerId(),
						j1.get().getJobCategory());
				jobList.forEach(job1 -> {
					empJobIDs.add(job1.getId());
				});
			});
			empJobIDs.forEach(jobid -> {

				if (!jobIDs.contains(jobid)) {
					jobIDs.add(String.valueOf(jobid));
				}

			});

		}

		String jobIDS = String.join(",", jobIDs);

		List<Map<String, Object>> job = jobRepository.findByChatMatchedJobs(can.getCandidateType(), can.getIndustry(),
				can.getJobCategory(), can.getCity(), can.getExperience(), jobIDS,can.getQualification());

		if (job != null && !job.isEmpty()) {
			
			Map<String, Object> j = job.get(0);

			CanInterviewsModel i = new CanInterviewsModel();
			i.setCanId(can.getId());
			i.setJobId((Integer) j.get("job_id"));
			i.setStatus("S");
			i.setCompanyName((String) j.get("company_name"));
			i.setContactPersonName((String) j.get("contact_person_name"));
			i.setContactNumber(Long.valueOf((String) j.get("mobile_number")));
			i.setCity((String) j.get("job_city"));
			i.setArea((String) j.get("area"));
			canInterviewRepository.save(i);

			String key = can.getLanguageKey();
			String limitText = getLimitText(key, can.getJobLimit());

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("jobLimit", can.getJobLimit());
			map.put("limitText", limitText);
			map.put("interviewId", i.getId());
			map.put("jobs", j);
			return new ResponseEntity<>(map, HttpStatus.OK);
			
		} else {
			/*
			 * List<Map<String, Object>> staff = null;
			 * 
			 * try { if(can.getCandidateType().equalsIgnoreCase("Fresher")) {
			 * List<CanInterviewsModel> existIn =
			 * canInterviewRepository.findByCanIdandJobId(can.getId(),1140);
			 * 
			 * if(existIn.isEmpty()) { int age = Integer.parseInt(can.getAge()); staff =
			 * jobRepository.findByStaffingMatchedJobs(age,can.getState(),can.getCity(),can.
			 * getPassed_out_year(), can.getQualification(),can.getSpecification()); } } }
			 * catch(Exception e) { } if(staff != null && !staff.isEmpty()) { Map<String,
			 * Object> j = staff.get(0);
			 * 
			 * CanInterviewsModel i = new CanInterviewsModel(); i.setCanId(can.getId());
			 * i.setJobId((Integer) j.get("job_id")); i.setStatus("S");
			 * i.setCompanyName((String) j.get("company_name"));
			 * i.setContactPersonName((String) j.get("contact_person_name"));
			 * i.setContactNumber(Long.valueOf((String) j.get("mobile_number")));
			 * i.setCity((String) j.get("job_city")); i.setArea((String) j.get("area"));
			 * canInterviewRepository.save(i);
			 * 
			 * String key = can.getLanguageKey(); String limitText = getLimitText(key,
			 * can.getJobLimit());
			 * 
			 * Map<String, Object> s = staff.get(0); job.add(s);
			 * 
			 * HashMap<String, Object> map = new HashMap<>(); map.put("code", 200);
			 * map.put("jobLimit", can.getJobLimit()); map.put("limitText", limitText);
			 * map.put("interviewId", i.getId()); map.put("jobs", job.get(0)); return new
			 * ResponseEntity<>(map, HttpStatus.OK); }else {
			 */
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Jobs Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			//}
		}
	}

	private String getLimitText(String key, int jobLimit) {
		String limitText;
		try {
			if (jobLimit == 0) {
				if (key.equalsIgnoreCase("ta")) {
					limitText = "குறிப்பு: உங்களுக்கான இலவச வேலை வாய்ப்பு அறிவிப்புகள் முடிந்துவிட்டது.";
				} else if (key.equalsIgnoreCase("hi")) {
					limitText = "नोट: आपकी मुफ़्त नौकरी सिफारिश की सीमा समाप्त हो गई है।";
				} else {
					limitText = "Note: Your free job recommendation limit is over.";
				}
			} else {
				if (key.equalsIgnoreCase("ta")) {
					limitText = "குறிப்பு: உங்களால் மீதமுள்ள " + jobLimit
							+ " முறை மட்டுமே இலவசமாக வேலை வாய்ப்பு விவரங்களை பெற முடியும் என்பதை நினைவில் கொள்ளவும்.";
				} else if (key.equalsIgnoreCase("hi")) {
					limitText = "नोट: कृपया ध्यान दें कि आपके पास " + jobLimit + " नौकरी सिफारिश शेष हैं।";
				} else {
					limitText = "Note: Please note that you have " + jobLimit + " job recommendations left.";
				}
			}
		} catch (Exception e) {
			if (jobLimit == 0) {
				limitText = "Note: Your free job recommendation limit is over.";
			} else {
				limitText = "Note: Please note that you have " + jobLimit + " job recommendations left.";
			}
		}
		return limitText;
	}

	@GetMapping(path = "/interviewDates")
	public ResponseEntity<?> getInterviewDate(@RequestParam("phone_number") final String mobileNumber,
			@RequestParam("job_id") final int jobId) {
		Optional<JobsModel> j = jobRepository.findById(jobId);
		Optional<EmployerModel> emp = employerRepository.findById(j.get().getEmployerId());

		LocalDate date = LocalDate.now();
		DateTimeFormatter s = DateTimeFormatter.ofPattern("E, dd MMM yyyy");
		List<InterviewDates> jsonArray = new ArrayList<>();
		int businessDays = 7;

		try {
			if (emp.get().getCategory().equalsIgnoreCase("Staffing Services")) {
				if(jobId==1140) {
					TemporalAdjuster adj1 = TemporalAdjusters.next(DayOfWeek.MONDAY);
					LocalDate nextMon = date.with(adj1);
					String firstMon = nextMon.format(s);
					TemporalAdjuster adj2 = TemporalAdjusters.next(DayOfWeek.WEDNESDAY);
					LocalDate nextWed = date.with(adj2);
					String firstWednes = nextWed.format(s);
					TemporalAdjuster adj3 = TemporalAdjusters.next(DayOfWeek.FRIDAY);
					LocalDate nextFri = date.with(adj3);
					String firstFri = nextFri.format(s);
					
					InterviewDates first = new InterviewDates();
					first.setDate(firstMon);
					jsonArray.add(first);
					InterviewDates sec = new InterviewDates();
					sec.setDate(firstWednes);
					jsonArray.add(sec);
					InterviewDates thi = new InterviewDates();
					thi.setDate(firstFri);
					jsonArray.add(thi);

				}else {
				TemporalAdjuster adj = TemporalAdjusters.next(DayOfWeek.THURSDAY);
				LocalDate nextWed = date.with(adj);
				LocalDate secondWed = nextWed.with(adj);
				String firstWednes = nextWed.format(s);
				String secondWednes = secondWed.format(s);
				InterviewDates first = new InterviewDates();
				first.setDate(firstWednes);
				jsonArray.add(first);
				InterviewDates sec = new InterviewDates();
				sec.setDate(secondWednes);
				jsonArray.add(sec);
				}
			} else {
				List<String> listBusinessDates = businessDaysFrom(date, businessDays);
				for (String p : listBusinessDates) {
					InterviewDates formDetailsJson = new InterviewDates();
					formDetailsJson.setDate(p);
					jsonArray.add(formDetailsJson);
				}

			}
		} catch (Exception e) {
			List<String> listBusinessDates = businessDaysFrom(date, businessDays);
			for (String p : listBusinessDates) {
				InterviewDates formDetailsJson = new InterviewDates();
				formDetailsJson.setDate(p);
				jsonArray.add(formDetailsJson);
			}
		}

		if (jsonArray.size() > 0) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("interviewDates", jsonArray);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Dates Not Available");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	public static List<String> businessDaysFrom(LocalDate date1, int businessDays) {

		LocalDate date2 = date1.plusDays(businessDays);
		DateTimeFormatter s = DateTimeFormatter.ofPattern("E, dd MMM yyyy");
		List<String> listBusinessDates = new ArrayList<>();
		for (LocalDate date11 = date1; !date11.isAfter(date2); date11 = date11.plusDays(1)) {
			DayOfWeek dayOfWeek = date11.getDayOfWeek();
			if (!(dayOfWeek.equals(DayOfWeek.SATURDAY) || dayOfWeek.equals(DayOfWeek.SUNDAY))) {
				String formattedDate = date11.format(s);
				listBusinessDates.add(formattedDate);
			}
		}

		listBusinessDates.remove(0);
		return listBusinessDates;
	}

	@PutMapping(path = "/updateJobStatus")
	public ResponseEntity<?> updateJobStatus(@RequestParam("phone_number") final String mobileNumber,
			@RequestParam(value = "status") final String status,
			@RequestParam(value = "interview_date", required = false) final String interviewDate,
			@RequestParam(value = "interview_id") final int interviewId) throws ParseException {

		long mn = Long.parseLong(mobileNumber);
		CandidateModel can = candidateRepository.findByMobileNumber(mn);

		Optional<CanInterviewsModel> list = canInterviewRepository.findById(interviewId);

		if (list.isPresent()) {
			if (status.equalsIgnoreCase("I")) {
				CanInterviewsModel c = list.get();
				c.setStatus(status);
				c.setInterviewTime("10 : 00 AM - 05 : 00 PM");
				c.setDocuments(
						"Bio-data, All Education Certificate, Aadhaar Card, Ration Card, 2 Passport Size Photo, Bank Passbook");
				c.setActive(true);

				String id = interviewDate + " 10:00:00";

				SimpleDateFormat input = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");
				SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd");

				Date indate = input.parse(id);
				String outputText = output.format(indate);
				c.setInterviewDate(outputText);

				Date currentDate = null;
				try {
					currentDate = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss").parse(id);
					c.setInterviewScheduledDt(String.valueOf(currentDate));
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				canInterviewRepository.save(c);

				can.setJobLimit(can.getJobLimit() - 1);
				candidateRepository.save(can);

				int jobId = c.getJobId();

				Optional<JobsModel> job = jobRepository.findById(jobId);
				JobsModel j = job.get();
				Optional<EmployerModel> emp = employerRepository.findById(j.getEmployerId());
				JobCanResponsesModel res = jobCanResponsesRepository.findByCanJobId(jobId, can.getId());
				if (res == null) {
					JobCanResponsesModel r = new JobCanResponsesModel();
					r.setJobId(jobId);
					r.setCanId(can.getId());
					r.setResponse("I");
					r.setResponseCount(1);
					jobCanResponsesRepository.save(r);
					if (activeProfile.equalsIgnoreCase("prod")) {

					HashMap<String, String> data2 = new HashMap<>();
					data2.put("Event Name", "Interview Alert");
					data2.put("Event Type", "Interview Scheduled");
					data2.put("Type", "Interview");
					data2.put("Candidate Name", can.getFirstName());
					data2.put("Company Name", c.getCompanyName());
					data2.put("Interview Date", interviewDate);
					data2.put("Job Role", can.getJobCategory() != null ? can.getJobCategory() : "Fresher");
					data2.put("Source", "Whatsapp");
					data2.put("Mobile Number", String.valueOf(can.getMobileNumber()));
					data2.put("ID Type", "Interview ID");
					data2.put("ID", String.valueOf(c.getId()));

					exotelCallController.connectToAgent("+91" + String.valueOf(can.getMobileNumber()),"JS",data2);
					}

					if (j.getCanResponseCount() == 1) {

						DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
						LocalDateTime now = LocalDateTime.now();

						String date = dtf.format(now);
						Date currentDate1 = null;

						try {
							currentDate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);

						} catch (ParseException e1) {
							e1.printStackTrace();
						}

						j.setCanResCompletedOn(currentDate1);
	
						if (activeProfile.equalsIgnoreCase("prod")) {
						String jobStatus = "Paid";
						String eventName = "Job Response Limit Reached";
						if (j.isFreetrialJob()) {
							jobStatus = "Free Trial";
							eventName = "Free Job Response Limit Reached";
						}
						HashMap<String, String> data1 = new HashMap<>();
						data1.put("Event Name", "Job Alert");
						data1.put("Event Type", eventName);
						data1.put("Type", "Job");
						data1.put("Company Name", j.getCompanyName());
						data1.put("Contact Person Name", j.getContactPersonName());
						data1.put("Position", j.getJobCategory());
						data1.put("Experience", String.valueOf(j.getJobExp())+" to "+String.valueOf(j.getJobMaxExp()));
						data1.put("Source", "Whatsapp");
						data1.put("Mobile Number", String.valueOf(j.getMobileNumber()));
						data1.put("Job Status", jobStatus);
						data1.put("ID Type", "Job ID");
						data1.put("ID", String.valueOf(j.getId()));

						exotelCallController.connectToAgent("+91" + String.valueOf(j.getMobileNumber()),"Emp",data1);
							if (j.isFreetrialJob()) {
								HashMap<String, String> d = new HashMap<>();
								d.put("mn", "91" + j.getWhatsappNumber());
								d.put("webLink", "https://web.taizo.in/console/pricing#employer");
								d.put("appLink", "https://emp.taizo.in/mCHxRLnhwvHkDvLY6");

								/* waAlertService.sendFreeJobLimitOverAlertToEmployer(d); */
							} else {
								HashMap<String, String> d = new HashMap<>();
								d.put("mn", "91" + j.getWhatsappNumber());
								d.put("jobCategory", j.getJobCategory());
								d.put("webLink", "https://web.taizo.in/console/manage-jobs");
								d.put("appLink", "https://emp.taizo.in/mCHxRLnhwvHkDvLY6");

								waAlertService.sendJobLimitOverAlertToEmployer(d);
							}
						}
					}
					j.setCanResponseCount(j.getCanResponseCount() - 1);
					jobRepository.save(j);

				} else {
					res.setResponseCount(res.getResponseCount() + 1);
					jobCanResponsesRepository.save(res);
				}
				if (activeProfile.equalsIgnoreCase("prod")) {
					sendAlert(jobId, can.getId(), job.get().getEmployerId(), interviewDate, c.getInterviewTime());
				}

				c.setInterviewDate(interviewDate);

				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("message", "Successfully Updated");
				map.put("interviewDetails", c);
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				CanInterviewsModel c = list.get();
				c.setStatus(status);
				canInterviewRepository.save(c);

				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("message", "Successfully Updated");
				map.put("interviewDetails", c);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Interview Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@Async
	private void sendAlert(int jobId, int canId, int empId, String interviewDate, String time) {
		String canLink = getCanLink(jobId, canId, empId);
		Optional<JobsModel> job = jobRepository.findById(jobId);
		Optional<CandidateModel> candidate = candidateRepository.findById(canId);
		CandidateModel can = candidate.get();
		String exp = "Fresher";
		if(can.getCandidateType().equalsIgnoreCase("Experienced")) {
			exp = String.valueOf(can.getExperience())+" year(s) "+String.valueOf(can.getExpMonths())+" month(s)";
		}
		
		HashMap<String, String> d = new HashMap<>();
		d.put("mn", "91" + job.get().getWhatsappNumber());
		d.put("name", "*" + can.getFirstName() + "*");
		d.put("jobCategory", "*" + job.get().getJobCategory() + "*");
		d.put("interviewDate", "*" + interviewDate + "*");
		d.put("interviewTime", "*" + time + "*");
		d.put("exp", "*" + exp + "*");
		//d.put("expMonths", "*" + String.valueOf(can.getExpMonths()) + "*");
		d.put("qualification", "*" + can.getQualification() + "*");
		d.put("keyskills", "*" + can.getKeySkill() + "*");
		d.put("webLink", "https://web.taizo.in/console/candidates?status=int");
		d.put("appLink", canLink);

		waAlertService.sendEmpInterviewAlert(d);

	}

	public String getCanLink(int jobId, int canId, int empId) {
		String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseEmpApiKey;

		DeeplinkRequest dl = new DeeplinkRequest();
		dl.setLongDynamicLink(firebaseEmpHost + "/?link=" + firebaseEmpHost + "/candidateDetails/" + empId + "/" + canId
				+ "/" + jobId + "&apn=" + firebaseEmpPackage);

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
		} catch (Exception e) {

		}
		return response.getShortLink();

	}

	@GetMapping(path = "/getJobLimitStatus")
	public ResponseEntity<?> updateJobStatus(@RequestParam("phone_number") final String mobileNumber) {

		long mn = Long.parseLong(mobileNumber);
		CandidateModel can = candidateRepository.findByMobileNumber(mn);

		if (can != null) {

			if (can.getJobLimit() > 0) {

				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("amount", "20");
				map.put("limit", can.getJobLimit());
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				if(can.isUsedFreeTrial()) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "Limit Exceeded");
				map.put("amount", "20");
				map.put("limit", 1);
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}else {
					int limit = 2;
					if(can.getCandidateType().equalsIgnoreCase("Fresher")) {
						limit = 1;
					}
					can.setJobLimit(limit);
					can.setUsedFreeTrial(true);
					candidateRepository.save(can);
					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 200);
					map.put("amount", "20");
					map.put("limit", can.getJobLimit());
					return new ResponseEntity<>(map, HttpStatus.OK);
				}
			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Candidate Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping(path = "/updateJobLocation")
	public ResponseEntity<?> updateJobLocation(@RequestParam("phone_number") final String mobileNumber,
			@RequestParam("city") final String city) {

		long mn = Long.parseLong(mobileNumber);
		CandidateModel can = candidateRepository.findByMobileNumber(mn);

		if (can != null) {
			can.setCity(city);
			candidateRepository.save(can);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Candidate Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	
	@GetMapping("/checkAdminAvailability")
	public ResponseEntity<Map<String, Object>> checkAdminAvailability() {

	    String emailId = "m.sangeetha262003@gmail.com";

	    Admin existingAdmin = adminRepository.findByEmailIdAndIsAvailable(emailId, true);

	    Map<String, Object> response = new HashMap<>();

	    if (existingAdmin != null) {
	        response.put("code", "200");
	        response.put("message", "Admin is available");
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    } else {
	        response.put("code", "400");
	        response.put("message", "Admin is not available");
	        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	    }
	}



}
