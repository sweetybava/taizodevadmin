package com.taizo.controller.candidate;



import java.io.IOException; 
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.CandidateService;
import com.taizo.service.CloudWatchLogService;
import com.taizo.service.WAAlertService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
public class CandidateController {

	private static final Logger logger = LogManager.getLogger(CandidateController.class);

	@Autowired
	EmployerRepository employerRepository;
	
	@Value("${firebase.emp.api.key}")
	private String firebaseEmpApiKey;

	@Value("${firebase.deeplink.emp.hostname}")
	private String firebaseEmpHost;

	@Value("${firebase.empapp.package.name}")
	private String firebaseEmpPackage;

	@Autowired
	CandidateRepository candidateRepository;
	
	@Autowired
	CanDocumentsRepository canDocumentsRepository;
	@Autowired
	JobRepository jobRepository;

	@Autowired
	EmployerCallRepository employerCallRepository;

	@Autowired
	CandidateCallRepository candidateCallRepository;

	@Autowired
	LanguagesRepository languagesRepository;

	@Autowired
	CanLanguagesRepository canLanguagesRepository;

	@Autowired
	EmpActivityRepository empActivityRepository;

	@Autowired
	CandidateService candidateService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	VideosRepository videosRepository;

	@PersistenceContext
	EntityManager em;

	@Autowired
	CloudWatchLogService cloudWatchLogService;

	@Autowired
	CandidateCallsRepository candidateCallsRepository;
	
	@Autowired
	JobCanResponsesRepository jobCanResponsesRepository;
	
	
	@Value("${spring.profiles.active}")
	private String activeProfile;
	
	@Autowired
	WAAlertService waAlertService;

	@GetMapping(path = "/candidateDetails")
	public ResponseEntity<?> getCandidateDetails(@RequestParam("user_id") final int userId) {

		CandidateModel details = candidateRepository.finduser(userId);

		if (details != null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/candidateCalls")
	public ResponseEntity<?> getCanCalls(@RequestParam("can_id") Integer can_id) {
		List<CandidateCallsModel> candidateCallModel = candidateCallsRepository.getCandidateInCallHistory(can_id);

		if (!candidateCallModel.isEmpty()) {
			try {
				// List<CandidateCallModel> candidateCallModel1 = candidateCallModel.get();
				List<HashMap<String, Object>> callDetails = new ArrayList<>();
				for (CandidateCallsModel candidateCallModel2 : candidateCallModel) {
					Optional<EmployerModel> userModel = employerRepository.findById(candidateCallModel2.getEmpId());
					Optional<JobsModel> jobModel = jobRepository.findById(candidateCallModel2.getJid());

					HashMap<String, Object> emp_can_details = new HashMap<>();

					emp_can_details.put("jobDetails", jobModel.get());
					emp_can_details.put("empJobId", jobModel.get().getEmpJobId());
					emp_can_details.put("companyLogo", userModel.isPresent() ? userModel.get().getCompanyLogo() : null);
					emp_can_details.put("callTime", candidateCallModel2.getCallTime());
					emp_can_details.put("count", candidateCallModel2.getCount());
					callDetails.add(emp_can_details);

				}

				HashMap<String, Object> map = new HashMap<>();
				map.put("status", "success");
				map.put("message", "success");
				map.put("code", 200);
				map.put("data", callDetails);
				return new ResponseEntity<>(map, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				HashMap<String, Object> map = new HashMap<>();
				map.put("status", "failed");
				map.put("message", "failed");
				map.put("code", 400);
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		}
		HashMap<String, Object> map = new HashMap<>();
		map.put("status", "No Details Found");
		map.put("message", "No Details Found");
		map.put("code", 200);
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@GetMapping(path = "/employerCallNotification")
	public ResponseEntity<?> getEmployerJobNotification(@RequestParam("candidate_id") final int candidateId) {
		List<EmployerCallModel> employerCallModels = employerCallRepository.getCallDetails(candidateId);
		List<CandidateNotificationModel> candidateNotificationModelList = new ArrayList<>();
		if (!employerCallModels.isEmpty()) {
			for (EmployerCallModel ecm : employerCallModels) {

				Optional<EmployerModel> optional = employerRepository.findById(ecm.getEmpId());
				EmployerModel existing = optional.get();

				String jobId = String.valueOf(ecm.getJid());

				Optional<JobsModel> jobsModel = jobRepository.findById(ecm.getJid());
				String companyName = null;
				String companyLogo = null;
				String contactPersonName = null;
				String phoneNumber = null;
				int dupCount = ecm.getCount();
				Date callTime = null;
				if (jobsModel.isPresent()) {
					companyName = jobsModel.get().getCompanyName();
					if (existing.getProfilePic() != null) {
						companyLogo = existing.getProfilePic();
					} else {
						companyLogo = "https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/job-image-default.png";
					}
					contactPersonName = jobsModel.get().getContactPersonName();
					phoneNumber = jobsModel.get().getMobileNumber();
					callTime = ecm.getCallTime();
				}

				CandidateNotificationModel candidateNotificationModel = new CandidateNotificationModel();
				candidateNotificationModel.setCallTime(callTime);
				candidateNotificationModel.setCompanyLogo(companyLogo);
				candidateNotificationModel.setJobId(jobId);
				candidateNotificationModel.setCompanyName(companyName);
				candidateNotificationModel.setPhoneNumber(phoneNumber);
				candidateNotificationModel.setContactPersonName(contactPersonName);
				candidateNotificationModel.setCount(dupCount);

				candidateNotificationModelList.add(candidateNotificationModel);
			}
		}
		HashMap<String, Object> map = new HashMap<>();
		map.put("statuscode", 200);
		map.put("message", "success");
		map.put("results", candidateNotificationModelList);
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@PostMapping(path = "/callEmployer")
	public ResponseEntity<?> callEmployer(@RequestParam("job_id") final int job_id,
			@RequestParam("candidate_id") final int can_id) {

		Optional<JobsModel> jobsModel = jobRepository.findById(job_id);

		Optional<CandidateModel> candidateModel = candidateRepository.findById(can_id);

		Optional<EmployerModel> empModel = employerRepository.findById(jobsModel.get().getEmployerId());
		if (jobsModel.isPresent()) {
			Integer empId = jobsModel.get().getEmployerId();
			CandidateCallModel candidateCallModel = new CandidateCallModel();
			candidateCallModel.setEmpId(empId);
			candidateCallModel.setJid(job_id);
			candidateCallModel.setcId(can_id);
			candidateCallRepository.save(candidateCallModel);
			
			JobCanResponsesModel res = jobCanResponsesRepository.findByCanJobId(job_id,can_id);
			if(res==null) {
				JobCanResponsesModel r = new JobCanResponsesModel();
				r.setJobId(job_id);
				r.setCanId(can_id);
				r.setResponse("C");
				r.setResponseCount(1);
				jobCanResponsesRepository.save(r);
				
				JobsModel j = jobsModel.get();
				if(j.getCanResponseCount()==1) {
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					LocalDateTime now = LocalDateTime.now();
					String date = dtf.format(now);
					Date currentDate = null;
					try {
						currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
						j.setCanResCompletedOn(currentDate);

					} catch (ParseException e1) {
						e1.printStackTrace();
					}
				}
				j.setCanResponseCount(j.getCanResponseCount()-1);
				jobRepository.save(j);
				
			}else {
				res.setResponseCount(res.getResponseCount()+1);
				jobCanResponsesRepository.save(res);
			}

			CandidateModel candidateModel1 = candidateModel.get();

			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(empId);
			EA.setActivity(candidateModel1.getFirstName() + " called you");
			empActivityRepository.save(EA);

			DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
			String date = formatter.format(new Date());
			SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
			Calendar cal = Calendar.getInstance();
			String time = simpleDateFormat1.format(cal.getTime());
			
			String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseEmpApiKey;

			DeeplinkRequest dl = new DeeplinkRequest();
			dl.setLongDynamicLink(firebaseEmpHost + "/?link=" + firebaseEmpHost + "/candidateDetails/" +empId
					+ "/" + can_id +"/"+job_id+"&apn=" + firebaseEmpPackage);

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
			if (activeProfile.equalsIgnoreCase("prod")) {
				String edu = candidateModel1.getQualification();
				if(candidateModel1.getSpecification()!=null && !candidateModel1.getSpecification().isEmpty()) {
					edu = candidateModel1.getSpecification();
				}
				String exp = "Fresher";
				if(candidateModel1.getCandidateType().equalsIgnoreCase("Experienced")) {
					exp = String.valueOf(candidateModel1.getExperience())+" year(s) "+String.valueOf(candidateModel1.getExpMonths())+" month(s)";
				}
				HashMap<String, String> d = new HashMap<>();
				d.put("mn", "91" + jobsModel.get().getWhatsappNumber());
				d.put("name", candidateModel1.getFirstName());
				d.put("jobCategory", jobsModel.get().getJobCategory());
				d.put("exp", exp);
				//d.put("expMonths", String.valueOf(candidateModel1.getExpMonths()));
				d.put("qualification", edu);
				d.put("keySkills", candidateModel1.getKeySkill());
				d.put("webLink", "https://web.taizo.in/console/candidates");
				d.put("appLink", response.getShortLink());

				waAlertService.sendCallAlertToEmployer(d);


			if(jobsModel.get().getAlternateMobileNumber()!=null && !jobsModel.get().getAlternateMobileNumber().isEmpty()) {
			
				HashMap<String, String> d11 = new HashMap<>();
				d11.put("mn", "91" + String.valueOf(jobsModel.get().getAlternateMobileNumber()));
				d11.put("name", candidateModel1.getFirstName());
				d11.put("jobCategory", jobsModel.get().getJobCategory());
				d11.put("exp", exp);
				//d11.put("expMonths", String.valueOf(candidateModel1.getExpMonths()));
				d11.put("qualification", edu);
				d11.put("keySkills", candidateModel1.getKeySkill());
				d11.put("webLink", "https://web.taizo.in/console/candidates");
				d11.put("appLink", response.getShortLink());

				waAlertService.sendCallAlertToEmployer(d11);
			}
			}
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

		HashMap<String, Object> map = new HashMap<>();
		map.put("statuscode", 400);
		map.put("message", "User Not Found");
		return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

	}

	@GetMapping(path = "/languages")
	public ResponseEntity<?> getLanguages() {

		List<LanguagesModel> details = languagesRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Languages Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path = "/updateCandidateDetails")
	public ResponseEntity<?> updateCandidateDetails(@RequestParam("user_id") final int userId,
			@RequestParam(value = "first_name", required = false) final String firstName,
			@RequestParam(value = "last_name", required = false) final String lastName,
			@RequestParam(value = "date_of_birth", required = false) final String dateOfBirth,
			@RequestParam(value = "gender", required = false) final String gender,
			@RequestParam(value = "age", required = false) final String age,
			@RequestParam(value = "current_country", required = false) final String currentCountry,
			@RequestParam(value = "current_state", required = false) final String currentState,
			@RequestParam(value = "current_city", required = false) final String currentCity,
			@RequestParam(value = "per_country", required = false) final String perCountry,
			@RequestParam(value = "per_state", required = false) final String perState,
			@RequestParam(value = "per_city", required = false) final String perCity,
			@RequestParam(value = "whatsapp_number", defaultValue = "0") final long whatsappNumber,
			@RequestParam(value = "email_id", required = false) final String emailId,
			@RequestParam(value = "qualification", required = false) final String qualification,
			@RequestParam(value = "specification", required = false) final String specification,
			@RequestParam(value = "key_skill", required = false) final String keySkill,
			@RequestParam(value = "certification_courses", required = false) final String certificationCourses,
			@RequestParam(value = "certification_specialization", required = false) final String certificationSpecialization,
			@RequestParam(value = "languages", required = false) final String languages,
			@RequestParam(value = "candidate_type", required = false) final String candidateType,
			@RequestParam(value = "industry", required = false) final String industry,
			@RequestParam(value = "job_category", required = false) final String jobCategory,
			@RequestParam(value = "experience_months", required = false) final Integer experienceMonths,
			@RequestParam(value = "experience_years", required = false) final Integer experienceYears,
			@RequestParam(value = "overseas_exp_years", required = false) final Integer overseasExpYears,
			@RequestParam(value = "overseas_exp_months", required = false) final Integer overseasExpMonths,
			@RequestParam(value = "exp_certificate", required = false) final String expCertificate,
			@RequestParam(value = "license", required = false) final String license) {

		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		Optional<UserModel> optional1 = userRepository.findById(userId);

		if (optional.isPresent() && optional1.isPresent()) {
			profileInitiated(firstName, optional.get().getMobileNumber());

			CandidateModel existing = optional.get();
			UserModel user = optional1.get();
			user.setLastName(lastName);

			existing.setLastName(lastName);
			existing.setDateOfBirth(dateOfBirth);
			existing.setAge(age);
			existing.setGender(gender);
			existing.setCurrentCountry(currentCountry);
			existing.setCurrentState(currentState);
			existing.setCurrentCity(currentCity);
			existing.setPerCountry(perCountry);
			existing.setPerState(perState);
			existing.setPerCity(perCity);
			existing.setWhatsappNumber(whatsappNumber);
			existing.setEmailId(emailId);
			existing.setQualification(qualification);
			existing.setSpecification(specification);
			existing.setCertificationCourses(certificationCourses);
			existing.setCertificationSpecialization(certificationSpecialization);
			existing.setCandidateType(candidateType);
			existing.setJobType("Full Time (8hrs to 10hrs)");
			existing.setIndustry(industry);
			existing.setJobCategory(jobCategory);
			existing.setExperience(experienceYears);
			existing.setExpMonths(experienceMonths);
			existing.setOverseasExp(overseasExpYears);
			existing.setOverseasExpMonths(overseasExpMonths);
			existing.setExpCertificate(expCertificate);
			existing.setLicense(license);
			existing.setKeySkill(keySkill);

			String myArray[] = null;
			if (expCertificate != null && !expCertificate.isEmpty()) {
				String[] items = expCertificate.split(",");
				int itemCount = items.length;

				myArray = new String[itemCount];
				for (int i = 0; i < itemCount; i++) {
					int num = i + 1;
					myArray[i] = "Experience Certificate" + " " + num;
				}
				String listString = String.join(",", myArray);
				existing.setCertificateType(listString);

			}

			String myArray1[] = null;
			if (license != null && !license.isEmpty()) {
				String[] items = license.split(",");
				int itemCount = items.length;

				myArray1 = new String[itemCount];
				for (int i = 0; i < itemCount; i++) {
					int num = i + 1;
					myArray1[i] = "License" + " " + num;
				}
				String listString = String.join(",", myArray1);
				existing.setLicenseType(listString);

			}

			existing = candidateRepository.save(existing);

			if (!languages.isEmpty()) {

				List<CanLanguageModel> lang = canLanguagesRepository.findByCandidateId(existing.getId());

				if (!lang.isEmpty()) {

					List<Integer> list = new ArrayList();

					int j = 0;

					for (CanLanguageModel s : lang) {

						j = s.getId();
						list.add(j);
					}

					canLanguagesRepository.delete(list);
				}

				List<Integer> x = Arrays.stream(languages.split(",")).map(Integer::parseInt)
						.collect(Collectors.toList());

				for (int f : x) {
					CanLanguageModel f1 = new CanLanguageModel();

					f1.setLanguageId(f);
					existing.getLanguages().add(f1);
					f1.setCandidate(existing);
					candidateRepository.save(existing);
				}
			}


			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Successfully Updated");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@Async
	private void profileInitiated(String firstName, long l) {
		DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
		String date = formatter.format(new Date());
		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
		Calendar cal = Calendar.getInstance();
		String time = simpleDateFormat1.format(cal.getTime());

	}

	@PutMapping(path = "/updateCandidateSkills")
	public ResponseEntity<?> updateCandidateSkills(@RequestParam("user_id") final int userId,
			@RequestParam("skills") final String skills) {

		CandidateModel details = candidateRepository.finduser(userId);

		if (details != null) {
			String type = "Skill Video";
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", candidateService.updateSkill(userId, skills, type));
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	
	@PutMapping(path = "/updateDocuments")
	public ResponseEntity<?> updateCandidateDocuments(@RequestParam("can_id") final int canId,
			@RequestParam("document_link") final String documentLink,
			@RequestParam("document_title") final String documentTitle,@RequestParam("key") final String key) {

		Optional<CandidateModel> details = candidateRepository.findById(canId);

		if (details.isPresent()) {
			CandidateModel existing = details.get();
			CanDocuments f1 = new CanDocuments();
			f1.setDocLink(documentLink);
			f1.setDocTitle(documentTitle);
			f1.setDocKey(key);
			existing.getDocuments().add(f1);
			f1.setDocuments(existing);
			candidateRepository.save(existing);
			
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", existing);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	@DeleteMapping(path = "/deleteDocuments")
	public ResponseEntity<?> deleteCandidateDocuments(@RequestParam("can_id") final int canId,
			@RequestParam("document_id") final int id) {

		Optional<CandidateModel> details = candidateRepository.findById(canId);

		if (details.isPresent()) {
			
			canDocumentsRepository.delete(id);
			
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Successfully Deleted");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping(path = "/updateJobRole")
	public ResponseEntity<?> updateCandidateJobRole(@RequestParam("user_id") final int userId,
			@RequestParam("industry") final String industry, @RequestParam("job_category") final String jobCategory) {

		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);

		if (optional.isPresent()) {

			CandidateModel existing = optional.get();
			existing.setIndustry(industry);
			existing.setJobCategory(jobCategory);
			candidateRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Successfully updated");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping(path = "/updateCity")
	public ResponseEntity<?> updateCandidateJobRole(@RequestParam("user_id") final int userId,
			@RequestParam("city") final String city) {

		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);

		if (optional.isPresent()) {

			CandidateModel existing = optional.get();
			existing.setCity(city);
			candidateRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Successfully updated");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping(path = "/updateCandidateAddSkills")
	public ResponseEntity<?> updateCandidateAddSkills(@RequestParam("user_id") final int userId) {

		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		if (optional.isPresent()) {

			CandidateModel existing = optional.get();
			String skill = existing.getSkills();

			if (skill == null) {

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "No Videos");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Already Video Added");
				map.put("results", optional);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping(path = "/updateCandidateDocument")
	public ResponseEntity<?> updateCandidateDocument(@RequestParam("user_id") final int userId,
			@RequestParam("document") final String document,
			@RequestParam(value = "titles", required = false) final String titles) {

		CandidateModel details = candidateRepository.finduser(userId);

		if (details != null) {
			/*
			 * String listString = null;
			 * 
			 * String myArray[] = null; if (document != null && !document.isEmpty()) {
			 * String[] items = document.split(","); int itemCount = items.length;
			 * 
			 * myArray = new String[itemCount]; for (int i = 0; i < itemCount; i++) { int
			 * num = i + 1; myArray[i] = "Experience Certificate" + " " + num; } listString
			 * = String.join(",", myArray); }
			 */

			CandidateModel c = candidateService.updateDocument(userId, titles, document);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", c);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping(path = "/updateCandidateLicense")
	public ResponseEntity<?> updateCandidateLicense(@RequestParam("user_id") final int userId,
			@RequestParam("license") final String license,
			@RequestParam(value = "titles", required = false) final String titles) {

		CandidateModel details = candidateRepository.finduser(userId);

		if (details != null) {
			/*
			 * String listString = null;
			 * 
			 * String myArray[] = null; if (license != null && !license.isEmpty()) {
			 * String[] items = license.split(","); int itemCount = items.length;
			 * 
			 * myArray = new String[itemCount]; for (int i = 0; i < itemCount; i++) { int
			 * num = i + 1; myArray[i] = "License" + " " + num; } listString =
			 * String.join(",", myArray);
			 * 
			 * }
			 */

			CandidateModel c = candidateService.updateLicense(userId, titles, license);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", c);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			map.put("results", null);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@RequestMapping(value = "/updateProfile1", method = RequestMethod.PUT)
	public ResponseEntity<?> updateProfile1(@RequestParam("user_id") final int userId,
			@RequestParam(value = "first_name", required = false) final String firstName,

			@RequestParam(value = "last_name", required = false) final String lastName,

			@RequestParam(value = "gender", required = false) final String gender,

			@RequestParam(value = "date_of_birth", required = false) final String dateOfBirth,

			@RequestParam(value = "age", required = false) final String age,

			@RequestParam(value = "current_country", required = false) final String currentCountry,

			@RequestParam(value = "current_state", required = false) final String currentState,

			@RequestParam(value = "current_city", required = false) final String currentCity,

			@RequestParam(value = "per_country", required = false) final String perCountry,

			@RequestParam(value = "per_state", required = false) final String perState,

			@RequestParam(value = "per_city", required = false) final String perCity,
			@RequestParam(value = "whatsapp_number", defaultValue = "0") final long whatsappNumber,
			@RequestParam(value = "email_id", required = false) final String emailId) {

		CandidateModel userExists = candidateRepository.finduser(userId);
		if (userExists == null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		} else {

			CandidateModel user = candidateService.updateProfile1(userId, firstName, lastName, dateOfBirth, age, gender,
					currentCountry, currentState, currentCity, perCountry, perState, perCity, whatsappNumber, emailId);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Successfully Updated");
			map.put("results", user);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/updateProfile2", method = RequestMethod.PUT)
	public ResponseEntity<?> updateProfile2(@RequestParam("user_id") final int userId,
			@RequestParam(value = "qualification", required = false) final String qualification,
			@RequestParam(value = "specification", required = false) final String specification,
			@RequestParam(value = "key_skill", required = false) final String keySkill,
			@RequestParam(value = "certification_courses", required = false) final String certificationCourses,
			@RequestParam(value = "certification_specialization", required = false) final String certificationSpecialization,
			@RequestParam(value = "languages", required = false) final String languages) {

		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			map.put("results", null);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {

			CandidateModel existing = optional.get();
			existing.setQualification(qualification);
			existing.setSpecification(specification);
			if (keySkill != null && !keySkill.isEmpty()) {
				existing.setKeySkill(keySkill);
			}
			existing.setCertificationCourses(certificationCourses);
			existing.setCertificationSpecialization(certificationSpecialization);
			candidateRepository.save(existing);

			if (languages != null) {

				List<CanLanguageModel> lang = canLanguagesRepository.findByCandidateId(existing.getId());

				if (!lang.isEmpty()) {

					List<Integer> list = new ArrayList();

					int j = 0;

					for (CanLanguageModel s : lang) {

						j = s.getId();
						list.add(j);
					}

					canLanguagesRepository.delete(list);
				}

				List<Integer> x = Arrays.stream(languages.split(",")).map(Integer::parseInt)
						.collect(Collectors.toList());

				for (int f : x) {
					CanLanguageModel f1 = new CanLanguageModel();

					f1.setLanguageId(f);
					existing.getLanguages().add(f1);
					f1.setCandidate(existing);
					candidateRepository.save(existing);
				}
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Successfully Updated");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

	}

	@RequestMapping(value = "/updateProfile3", method = RequestMethod.PUT)
	public ResponseEntity<?> updateProfile3(@RequestParam("user_id") final int userId,
			@RequestParam(value = "experience_years", required = false) final Integer experienceYears,
			@RequestParam(value = "experience_months", required = false) final Integer experienceMonths,
			@RequestParam(value = "canditate_type", required = false) final String candidateType,
			@RequestParam(value = "industry", required = false) final String industry,
			@RequestParam(value = "job_category", required = false) final String jobCategory,
			@RequestParam(value = "key_skill", required = false) final String keySkill,
			@RequestParam(value = "city", required = false) final String city,
			@RequestParam(value = "overseas_exp_years", required = false) final Integer overseasExpYears,
			@RequestParam(value = "overseas_exp_months", required = false) final Integer overseasExpMonths,

			@RequestParam(value = "exp_certificate", required = false) final String expCertificate,

			@RequestParam(value = "license", required = false) final String license) {

		CandidateModel userExists = candidateRepository.finduser(userId);
		if (userExists == null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			map.put("results", null);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		} else {

			String listString = null;

			String myArray[] = null;
			if (expCertificate != null && !expCertificate.isEmpty()) {
				String[] items = expCertificate.split(",");
				int itemCount = items.length;

				myArray = new String[itemCount];
				for (int i = 0; i < itemCount; i++) {
					int num = i + 1;
					myArray[i] = "Experience Certificate" + " " + num;
				}
				listString = String.join(",", myArray);
			}

			String listString1 = null;

			String myArray1[] = null;
			if (license != null && !license.isEmpty()) {
				String[] items = license.split(",");
				int itemCount = items.length;

				myArray1 = new String[itemCount];
				for (int i = 0; i < itemCount; i++) {
					int num = i + 1;
					myArray[i] = "License" + " " + num;
				}
				listString1 = String.join(",", myArray);

			}

			CandidateModel user = candidateService.updateProfile3(userId, experienceYears, experienceMonths,
					candidateType, overseasExpYears, overseasExpMonths, expCertificate, listString, license,
					listString1, industry, jobCategory, city, keySkill);
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Successfully Updated");
			map.put("results", user);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

	}

	@RequestMapping(value = "/updateProfile4", method = RequestMethod.PUT)
	public ResponseEntity<?> updateProfile4(@RequestParam("user_id") final int userId,
			@RequestParam(value = "job_type", required = false) final String jobType,
			@RequestParam(value = "industry", required = false) final String industry,
			@RequestParam(value = "job_category", required = false) final String jobRole,
			@RequestParam(value = "candidate_location", required = false) final String candidateLocation,
			@RequestParam(value = "pref_location", required = false) final String prefLocation,
			@RequestParam(value = "city", required = false) final String domesticLocation,
			@RequestParam(value = "pre_over_location", required = false) final String overseasLocation,
			@RequestParam(value = "experience_years", required = false) final Integer experienceYears,
			@RequestParam(value = "experience_months", required = false) final Integer experienceMonths,
			@RequestParam(value = "canditate_type", required = false) final String candidateType) {

		CandidateModel userExists = candidateRepository.finduser(userId);
		if (userExists == null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			map.put("results", null);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		} else {

			CandidateModel user = candidateService.updateProfile4(userId, jobType, industry, jobRole, candidateLocation,
					prefLocation, domesticLocation, overseasLocation, candidateType, experienceYears, experienceMonths);
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Successfully Updated");
			map.put("results", user);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	@GetMapping(path = "/CandidateStatus")
	public ResponseEntity<?> getCandidateStatus(@RequestParam("user_id") final int userId) {

		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {

			CandidateModel existing = optional.get();

			String country = existing.getCurrentCountry();

			if (country != null && !country.isEmpty()) {

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", false);
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", true);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		}

	}

	@GetMapping(path = "/profileStatus")
	public ResponseEntity<?> getCandidateProfileStatus(@RequestParam("user_id") final int userId) {

		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {

			CandidateModel existing = optional.get();
			Optional<UserModel> user = userRepository.findById(userId);

			int percentage = 0;
			boolean abt = false;
			boolean stud = false;
			boolean wrk = false;

			String about = existing.getGender();
			String work = existing.getCandidateType();
			String study = existing.getQualification();
			String pic = user.get().getProfilePic();

			if (about != null && !about.isEmpty()) {
				percentage = percentage + 30;
				abt = true;
			}
			if (work != null && !work.isEmpty()) {
				percentage = percentage + 30;
				wrk = true;
			}
			if (study != null && !study.isEmpty()) {
				percentage = percentage + 30;
				stud = true;
			}
			if (pic != null && !pic.isEmpty()) {
				percentage = percentage + 10;
			}
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("about", abt);
			map.put("study", stud);
			map.put("work", wrk);
			map.put("percentage", percentage);

			return new ResponseEntity<>(map, HttpStatus.OK);
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping(path = "/candidateKnownLanguages")
	public ResponseEntity<?> getCandidateKnownLanguages(@RequestParam("user_id") final int userId) {

		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {

			CandidateModel existing = optional.get();

			List<CanLanguageModel> details = canLanguagesRepository.findByCandidateId(existing.getId());
			if (!details.isEmpty()) {

				List<LanguagesModel> persons = null;
				Set<Integer> list = new HashSet();

				int j = 0;

				for (CanLanguageModel s : details) {

					j = s.getLanguageId();
					list.add(j);
				}

				persons = em.createQuery("SELECT j FROM LanguagesModel j WHERE j.id IN :ids").setParameter("ids", list)
						.getResultList();
			

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "success");
				map.put("results", persons);
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "No languages found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		}

	}

	@PutMapping(path = "/updateLanguageKey")
	public ResponseEntity<?> updateCandidateLanguageKey(@RequestParam("user_id") final int userId,
			@RequestParam(value = "language_key") final String languageKey) {

		CandidateModel details = candidateRepository.finduser(userId);

		if (details != null) {

			CandidateModel c = candidateService.updateLanguageKey(userId, languageKey);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", c);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(value = "/skillVideo/{id}")
	public ResponseEntity<?> updateJob(@PathVariable("id") int id,
			@RequestPart(name = "skill_video") MultipartFile video) {

		Optional<CandidateModel> optional = candidateRepository.findByUserId(id);

		if (optional.isPresent()) {

			CandidateModel c = optional.get();

			if (video != null && !video.isEmpty()) {

				String Skillvideo = c.getSkills();

				if (Skillvideo != null && !Skillvideo.isEmpty()) {

					boolean imageResult = candidateService.deleteImage(Skillvideo);

				}

			}
			String skillvideo = null;

			if (video != null && !video.isEmpty()) {
				try {
					skillvideo = candidateService.uploadFile(video, id, video.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("error [" + e.getMessage() + "] occurred while uploading [" + video + "] skill video");

					CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
					logEventModel.setType("Profile");
					logEventModel.setMessage("failure");
					logEventModel.setDescription(
							"error [" + e.getMessage() + "] occurred while uploading [" + video + "] skill video");
					try {
						cloudWatchLogService.cloudLogFailure(logEventModel, "C");
					} catch (Exception e1) {

					}

				}
				if (skillvideo != null && !skillvideo.isEmpty()) {
					String type = "Skill Video";

					c.setSkills(skillvideo);
					c.setSkillVideoType(type);
					c = candidateRepository.save(c);

					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", "success");
					map.put("results", c);
					return new ResponseEntity<>(map, HttpStatus.OK);

				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 400);
					map.put("message", "Video Not Saved");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Video is not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Candidate not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path = "/updateFcmToken")
	public ResponseEntity<?> updateFcmToken(@RequestParam("candidate_id") final int userID,
			@RequestParam(value = "fcm_token") final String token) {

		Optional<CandidateModel> details = candidateRepository.findByUserId(userID);

		if (details.isPresent()) {

			CandidateModel existing = details.get();
			existing.setFcmToken(token);
			candidateRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Saved Succesfully");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Candidate Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping(path = "/sampleVideos")
	public ResponseEntity<?> getSampleVideos() {

		List<SampleVideosModel> details = videosRepository.findAllByActive(true);

		if (!details.isEmpty()) {

			Collections.reverse(details);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "No Videos Available");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/sampleVideos/{pageNo}/{pageSize}")
	public ResponseEntity<?> getPaginatedCountries(@PathVariable int pageNo, @PathVariable int pageSize) {
		try {
			Page<SampleVideosModel> videoPage = candidateService.findPaginated(pageNo, pageSize);
			HashMap<String, Object> responseMap = new HashMap<>();
			responseMap.put("statuscode", 200);
			responseMap.put("message", "success");
			responseMap.put("results", videoPage.stream().collect(Collectors.toList()));
			responseMap.put("totalPages", videoPage.getTotalPages());
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		} catch (Exception e) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "No Videos Available");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	
	@GetMapping("/candidatesources")
    public List<CfgCanSources> getAllEntities() {
        return candidateService.getAllEntities();
    }

}
