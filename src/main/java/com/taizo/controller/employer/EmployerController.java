package com.taizo.controller.employer;

import java.io.IOException; 
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.mail.MessagingException;

import com.google.gson.Gson;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.service.CloudWatchLogService;
import com.taizo.service.EmployerService;
import com.taizo.utils.TupleStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import freemarker.template.TemplateException;

@CrossOrigin
@RestController
@RequestMapping("/employer")
public class EmployerController {

	private static final Logger logger = LoggerFactory.getLogger(EmployerController.class);

	@Autowired
	EmployerCallRepository employerCallRepository;

	@Autowired
	LeadRepository leadRepository;

	@Autowired
	EmpActivityRepository empActivityRepository;

	@Autowired
	AmazonSESMailUtil amazonSESMailUtil;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	EmployerService employerService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	CandidateCallsRepository candidateCallsRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	CountryRepository countryRepository;

	@Autowired
	IndiaStateRepository indiaStateRepository;

	@Autowired
	CloudWatchLogService cloudWatchLogService;
	
	@Value("${firebase.js.api.key}")
	private String firebaseJSApiKey;

	@Value("${firebase.deeplink.js.hostname}")
	private String firebaseJSHost;

	@Value("${firebase.jsapp.package.name}")
	private String firebaseJSPackage;

	@Value("${aws.host.url}")
	private String baseUrl;

	private Gson gson = new Gson();
	
	


	@PostMapping("/employerRegister")
	public ResponseEntity<?> createEmployer(@RequestBody EmployerModel employer,
			@RequestParam(value = "device_token", required = false) String deviceToken)
			throws TemplateException, MessagingException, IOException {
		EmployerModel employerExists = employerRepository.findByEmailId(employer.getEmailId());
		if (employerExists != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Email ID Already exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		} else {

			String token = UUID.randomUUID().toString();
			employer.setToken(token);
			employer.setDeactivated(false);
			employer.setUsedFreeTrial("No");
			employer.setFcmToken(deviceToken);

			employerRepository.save(employer);

			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(employer.getId());
			EA.setActivity("<b>" + employer.getCompanyName() + "</b>" + " has been registered successfully!");
			empActivityRepository.save(EA);

			HashMap<String, String> emailDataHM = new HashMap<>();
			emailDataHM.put("name", employer.getContactPersonName());
			emailDataHM.put("companyName", employer.getCompanyName());

			/*
			 * TupleStore tupleStore = new TupleStore();
			 * tupleStore.setKey(employer.getEmailId());
			 * tupleStore.setValue(gson.toJson(emailDataHM));
			 * amazonSESMailUtil.sendEmailSES("EmployerRegistrationTemplateV1", tupleStore);
			 */

			CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
			logEventModel.setType("Profile");
			logEventModel.setMessage("success");
			logEventModel.setDescription(employer.toString());

			try {
				cloudWatchLogService.cloudLog(logEventModel, "E");
			} catch (Exception e) {

			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", employer);
			return new ResponseEntity<>(map, HttpStatus.CREATED);
		}
	}

	@PostMapping(path = "/login", consumes = "application/json")
	public ResponseEntity<?> getEmployerLogin(@RequestBody EmployerModel employer) {

		EmployerModel existingUser = employerRepository.findByEmailId(employer.getEmailId());
		if (existingUser != null) {
			String token = UUID.randomUUID().toString();
			existingUser.setToken(token);
			existingUser.setPassword("");

			employerRepository.save(existingUser);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", existingUser);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "EmailId does not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@PostMapping("/logout")
	public ResponseEntity<?> getLogout(@RequestParam("token") final String token) {

		Optional<EmployerModel> customer = employerRepository.findByToken(token);

		if (customer.isPresent()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "Logout Successfully");
			map.put("data", employerService.findLogout(token));
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
		HashMap<String, Object> map = new HashMap<>();
		map.put("code", 400);
		map.put("message", "Token mismatched");
		return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
	}

	@PostMapping(path = "/callCandidate")
	public ResponseEntity<?> callEmployer(@RequestParam("job_id") final int job_id,
			@RequestParam("candidate_id") final int can_id, @RequestParam("employer_id") final int emp_id) {
		EmployerCallModel employerCallModel = new EmployerCallModel();
		employerCallModel.setEmpId(emp_id);
		employerCallModel.setJid(job_id);
		employerCallModel.setcId(can_id);

		// try {
		employerCallRepository.save(employerCallModel);

		Optional<EmployerModel> empModel = employerRepository.findById(emp_id);
		Optional<JobsModel> jobsModel = jobRepository.findById(job_id);
		Optional<CandidateModel> candidateModel = candidateRepository.findById(can_id);
		
		String url = "https://firebasedynamiclinks.googleapis.com/v1/s?key=" + firebaseJSApiKey;

			DeeplinkRequest dl = new DeeplinkRequest();
			dl.setLongDynamicLink(firebaseJSHost + "/?link=" + firebaseJSHost + "/jobDetails/" + 0 + "/"
					+ job_id + "&apn=" + firebaseJSPackage);

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

		HashMap<String, Object> map = new HashMap<>();
		map.put("code", 200);
		map.put("status", "success");
		map.put("message", "Candidate Called Success");
		return new ResponseEntity<>(map, HttpStatus.OK);
		/*
		 * } catch (Exception e) { HashMap<String, Object> map = new HashMap<>();
		 * map.put("code", 400); map.put("status", "failure"); map.put("message",
		 * "Candidate Call Failed"); return new ResponseEntity<>(map,
		 * HttpStatus.BAD_REQUEST); }
		 */
	}

	@PostMapping(path = "/candidateCallNotification", consumes = "application/json")
	public ResponseEntity<?> getEmployerLogin(@RequestParam("emp_id") Integer emp_id) {
		List<CandidateCallsModel> candidateCallModel = candidateCallsRepository.getEmployerInCallHistory(emp_id);

		if (!candidateCallModel.isEmpty()) {
			try {
				// List<CandidateCallModel> candidateCallModel1 = candidateCallModel.get();
				List<HashMap<String, Object>> callDetails = new ArrayList<>();
				for (CandidateCallsModel candidateCallModel2 : candidateCallModel) {
					Optional<CandidateModel> candidateModel = candidateRepository
							.findById(candidateCallModel2.getcId());
					Optional<UserModel> userModel = userRepository.findById(candidateCallModel2.getcId());
					Optional<JobsModel> jobModel = jobRepository.findById(candidateCallModel2.getJid());

					HashMap<String, Object> emp_can_details = new HashMap<>();
					if (candidateModel.isPresent()) {
						emp_can_details.put("candidateDetails", candidateModel.get());
					}
					emp_can_details.put("jobId", candidateCallModel2.getJid());
					emp_can_details.put("empJobId", jobModel.get().getEmpJobId());
					emp_can_details.put("candidatePic", userModel.isPresent() ? userModel.get().getProfilePic() : null);
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

	@PutMapping(path = "/updateProfilePic")
	public ResponseEntity<?> updateProfilePic(@RequestParam("employer_id") final int id,
			@RequestPart(value = "photo") MultipartFile photo) throws IOException {
		Optional<EmployerModel> optional = employerRepository.findById(id);

		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer is not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {

			EmployerModel existing = optional.get();

			String image = existing.getProfilePic();
			if (image != null && !image.isEmpty()) {
				boolean imageResult = employerService.deleteCompanyLogo(image);
			}

			String ProfilePhoto = this.employerService.uploadCompanyLogo(photo, photo.getBytes());

			if (ProfilePhoto != null && !ProfilePhoto.isEmpty()) {

				existing.setProfilePic(ProfilePhoto);

				employerRepository.save(existing);

				EmployerActivityModel EA = new EmployerActivityModel();
				EA.setEmpId(id);
				EA.setActivity("Your company profile has been updated!");
				empActivityRepository.save(EA);

				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("status", "success");
				map.put("message", "Successfully Updated");
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "Image Not Saved");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			}

		}
	}

	@PutMapping(path = "/updateCompanyLogo")
	public ResponseEntity<?> updateCompanyLogo(@RequestParam("employer_id") final int id,
			@RequestPart(value = "photo") MultipartFile photo) throws IOException {
		Optional<EmployerModel> optional = employerRepository.findById(id);

		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer is not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {

			EmployerModel existing = optional.get();

			String image = existing.getCompanyLogo();
			if (image != null && !image.isEmpty()) {
				boolean imageResult = employerService.deleteCompanyLogo(image);
			}

			String ProfilePhoto = this.employerService.uploadCompanyLogo(photo, photo.getBytes());

			if (ProfilePhoto != null && !ProfilePhoto.isEmpty()) {

				existing.setCompanyLogo(ProfilePhoto);

				employerRepository.save(existing);

				EmployerActivityModel EA = new EmployerActivityModel();
				EA.setEmpId(id);
				EA.setActivity("Your <b>company logo</b> has been updated!");
				empActivityRepository.save(EA);

				DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
				String date = formatter.format(new Date());
				SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
				Calendar cal = Calendar.getInstance();
				String time = simpleDateFormat1.format(cal.getTime());

				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("status", "success");
				map.put("message", "Successfully Updated");
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "Image Not Saved");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			}

		}
	}

	@PostMapping("/kycUpdate")
	public ResponseEntity<?> kycUpdate(@ModelAttribute EmployerModel employer,
			@RequestPart(value = "reg_proof_doc", required = false) MultipartFile regProof,
			@RequestPart(value = "tax_doc", required = false) MultipartFile taxDoc,
			@RequestPart(value = "photo", required = false) MultipartFile photo)
			throws TemplateException, MessagingException, IOException {

		Optional<EmployerModel> details = employerRepository.findById(employer.getId());

		if (details != null && details.isPresent()) {

			String regProofNum = employer.getRegProofNumber();

			EmployerModel existing = details.get();

			if (regProofNum != null && !regProofNum.isEmpty()) {
				kycInitiated(existing);
			}

			String image = existing.getCompanyLogo();
			if (image != null && !image.isEmpty()) {
				boolean imageResult = employerService.deleteCompanyLogo(image);
			}

			if (photo != null && !photo.isEmpty()) {
				String ProfilePhoto = this.employerService.uploadCompanyLogo(photo, photo.getBytes());
				if (ProfilePhoto != null && !ProfilePhoto.isEmpty()) {

					existing.setCompanyLogo(ProfilePhoto);
					EmployerActivityModel EA = new EmployerActivityModel();
					EA.setEmpId(existing.getId());
					EA.setActivity("Your <b>company logo</b> has been updated!");
					empActivityRepository.save(EA);
				}
			}

			if (regProofNum != null && !regProofNum.isEmpty()) {
				existing.setRegProofNumber(regProofNum);
				existing.setKycStatus("U");

				EmployerActivityModel EA = new EmployerActivityModel();
				EA.setEmpId(employer.getId());
				EA.setActivity("<b>KYC</b> has been updated!");
				empActivityRepository.save(EA);

				DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
				String date = formatter.format(new Date());
				SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
				Calendar cal = Calendar.getInstance();
				String time = simpleDateFormat1.format(cal.getTime());

				Map<String, String> kycEmailData = new HashMap<String, String>();
				kycEmailData.put("name", existing.getContactPersonName());
				logger.info(kycEmailData.toString());

				/*
				 * TupleStore tupleStore = new TupleStore();
				 * tupleStore.setKey(existing.getEmailId());
				 * tupleStore.setValue(gson.toJson(kycEmailData));
				 * amazonSESMailUtil.sendEmailSES("EmployerKYCReviewTemplateV2", tupleStore);
				 */

			}
			employerRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "Updated Successfully");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	private void kycInitiated(EmployerModel existing) { 
	  
	  }

	@GetMapping(path = "/kycStatus")
	public ResponseEntity<?> kycStatus(@RequestParam("employer_id") final int employerId) {

		Optional<EmployerModel> emp = employerRepository.findById(employerId);
		if (emp != null && emp.isPresent()) {
			EmployerModel existing = emp.get();

			String panNum = existing.getRegProofNumber();

			if (panNum != null && !panNum.isEmpty()) {
				String status = existing.getKycStatus();

				EmpKycStatusModel kycStatus = new EmpKycStatusModel();
				kycStatus.setKycStatus(status);

				HashMap<String, Object> map = new HashMap<>();
				map.put("status", "success");
				map.put("message", "KYC Document is Uploaded");
				map.put("code", 200);
				map.put("data", kycStatus);
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "KYC Document is not yet uploaded");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@PutMapping(path = "/updateFcmToken")
	public ResponseEntity<?> updateFcmToken(@RequestParam("employer_id") final int employer_id,
			@RequestParam(value = "fcm_token") final String token) {

		Optional<EmployerModel> details = employerRepository.findById(employer_id);

		if (details.isPresent()) {

			EmployerModel existing = details.get();
			existing.setFcmToken(token);
			employerRepository.save(existing);

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

	@PutMapping(path = "/updateContacts")
	public ResponseEntity<?> updateMobileNumber(@RequestParam("employer_id") final int employer_id,
			@RequestParam(value = "mobile_number") final String number,
			@RequestParam(value = "alternate_mobile_number", required = false) final String alternateMN,
			@RequestParam(value = "contactPersonName") final String contactPersonName,
			@RequestParam(value = "mobileCountryCode") final String mobileCountryCode) {

		Optional<EmployerModel> details = employerRepository.findById(employer_id);

		if (details.isPresent()) {

			EmployerModel existing = details.get();

			if (contactPersonName != null && !contactPersonName.isEmpty()) {

				existing.setContactPersonName(contactPersonName);
			}
			if (number != null && !number.isEmpty()) {
				Optional<LeadModel> emp = leadRepository.findByMobileNumber(Long.valueOf(number));
				if (emp.isPresent()) {
					LeadModel l = emp.get();
					l.setRegisteredInApp(true);
					leadRepository.save(l);
				}

			}
			if (mobileCountryCode != null && !mobileCountryCode.isEmpty()) {

				existing.setMobileCountryCode(mobileCountryCode);
				existing.setMobileNumber(Long.valueOf(number));

			}
			if (alternateMN != null && !alternateMN.isEmpty()) {
				existing.setAlternateMobileNumber(alternateMN);
			}

			employerRepository.save(existing);

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

	@GetMapping("/EmpCallAndApplyNotification")
	public ResponseEntity<?> getEmpInterviewDetails(@RequestParam("EmpID") final int EmpID,
			@RequestParam("status") final String status) {

		List<Map<String, Object>> details = employerRepository.getEmpCallAndAppliedNotification(EmpID, status);

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
	
	@GetMapping("/EmpCallAndApplyNotificationByDate")
	public ResponseEntity<?> getEmpInterviewDetails(@RequestParam("EmpID") final int EmpID,@RequestParam("status") final String status,
			@RequestParam("start_date") final String startdate, @RequestParam("end_date") final String endDate,
			@RequestParam(value = "job_category", required = false) final String jobRole) {
		List<Map<String, Object>> details;
		if(jobRole!=null && !jobRole.isEmpty()) {
		 details = employerRepository.getEmpCallAndAppliedNotificationByDate(EmpID, status,startdate,endDate,jobRole);
		} else {
		 details = employerRepository.getEmpCallAndAppliedNotificationByDate(EmpID, status,startdate,endDate,null);
		}

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
	
	@GetMapping("/EmpCallAndApplyNotificationByJobId")
	public ResponseEntity<?> getEmpInterviewDetailsByJobId(@RequestParam("emp_id") final int EmpID,@RequestParam("status") final String status,
			@RequestParam("job_id") final int jobId,@RequestParam("start_date") final String startdate, @RequestParam("end_date") final String endDate) {

		List<Map<String, Object>> details = employerRepository.getEmpCallAndAppliedNotificationByJobId(EmpID, status,jobId,startdate,endDate);

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


	@PutMapping(path = "/updateCompany")
	public ResponseEntity<?> updateCompany(@RequestParam("employer_id") final int employer_id,
			@RequestParam(value = "company_name") final String company_name,
			@RequestParam(value = "industry") final String industry,
			@RequestParam(value = "yearFounded", required = false) final String yearFounded,
			@RequestParam(value = "noOfEmployees", required = false) final String no_of_employees,
			@RequestParam(value = "phone", required = false) final String phone,
			@RequestParam(value = "websiteUrl", required = false) final String websiteUrl,
			@RequestParam(value = "phoneCountryCode", required = false) final String phoneCountryCode) {

		Optional<EmployerModel> details = employerRepository.findById(employer_id);

		if (details.isPresent()) {

			EmployerModel existing = details.get();
			if (company_name != null && !company_name.isEmpty()) {
				existing.setCompanyName(company_name);
			}
			if (industry != null && !industry.isEmpty()) {
				existing.setIndustry(industry);
			}

			if (no_of_employees != null && !no_of_employees.isEmpty()) {
				existing.setNoOfEmployees(no_of_employees);
			}

			if (yearFounded != null && !yearFounded.isEmpty()) {
				existing.setYearFounded(yearFounded);
			}

			if (websiteUrl != null && !websiteUrl.isEmpty()) {
				existing.setWebsiteUrl(websiteUrl);
			}

			if (phone != null && !phone.isEmpty()) {
				existing.setPhone(Long.valueOf(phone));

			}

			if (phoneCountryCode != null && !phoneCountryCode.isEmpty()) {
				existing.setPhoneCountryCode(phoneCountryCode);

			}

			employerRepository.save(existing);
			
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

	@PutMapping(path = "/updateLocation")
	public ResponseEntity<?> updateLocation(@RequestParam("employer_id") final int employer_id,
			@RequestParam(value = "address") final String address, @RequestParam(value = "city", required = false) final String city,
			@RequestParam(value = "state", required = false) final String state, @RequestParam(value = "country", required = false) final String country,
		@RequestParam(value = "pin_code", required = false) final int code,@RequestParam(value = "area", required = false) final String area,
			@RequestParam(value = "latitude", required = false) final String latitude,
			@RequestParam(value = "longitude", required = false) final String longitude) {

		Optional<EmployerModel> details = employerRepository.findById(employer_id);

		if (details.isPresent()) {

			EmployerModel existing = details.get();
			existing.setAddress(address);
			existing.setCity(city);
			existing.setState(state);
			existing.setArea(area);
			existing.setCountry(country);
			existing.setPincode(code);
			existing.setLatitude(latitude);
			existing.setLongitude(longitude);

			employerRepository.save(existing);

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
	
}
