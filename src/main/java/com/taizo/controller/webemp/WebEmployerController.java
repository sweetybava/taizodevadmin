package com.taizo.controller.webemp;

import java.io.IOException; 
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.google.gson.Gson;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.service.CandidateService;
import com.taizo.service.CloudWatchLogService;
import com.taizo.service.EmployerService;
import com.taizo.utils.TupleStore;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import freemarker.template.TemplateException;
import io.netty.util.Constant;

@CrossOrigin
@RestController
@RequestMapping("/webEmployer")
public class WebEmployerController {

	private static final Logger logger = LoggerFactory.getLogger(WebEmployerController.class);

	@Autowired
	AmazonSESMailUtil amazonSESMailUtil;

	@Autowired
	IndustryRepository industryRepository;
	
	@Autowired
	PlansRepository plansRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	EmployerService employerService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	EmpActivityRepository empActivityRepository;
	
	@Autowired
	InterviewRepository interviewRepository;

	@PersistenceContext
	EntityManager em;

	@Autowired
	CloudWatchLogService cloudWatchLogService;

	@Value("${aws.host.url}")
	private String baseUrl;

	private Gson gson = new Gson();

	@PostMapping("/register")
	public ResponseEntity<?> createEmployer(@RequestParam("email_id") final String emailId,
			@RequestParam("password") final String password) throws TemplateException, MessagingException, IOException {
		EmployerModel employerExists = employerRepository.findByEmailId(emailId);
		if (employerExists != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Email ID Already exists");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {

			String token = UUID.randomUUID().toString();
			String pass = passwordEncoder.encode(password);

			EmployerModel employer = new EmployerModel();
			employer.setEmailId(emailId);
			employer.setPassword(pass);
			employer.setToken(token);
			employer.setDeactivated(false);
			employer.setEmailVerified(false);
			employer.setCompanyDetailsFilled(false);
			employer.setContactDetailsFilled(false);
			employer.setUsedFreeTrial("Yes");
			employer.setFromWeb(true);
			employer.setEmailNotification(true);
			employer.setPushNotification(true);
			employer.setWhatsappNotification(true);
			employer.setNotificationSound(true);

			employerRepository.save(employer);

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
			map.put("message", "Register Successfully");
			map.put("code", 200);
			map.put("data", employer);
			return new ResponseEntity<>(map, HttpStatus.CREATED);
		}
	}

	@PostMapping("/auth/google")
	public ResponseEntity<?> createEmployerByGoogle(@RequestParam("email_id") final String emailId,
			@RequestParam("name") final String name, @RequestParam("token") final String token)
			throws TemplateException, MessagingException, IOException {
		EmployerModel employerExists = employerRepository.findByEmailId(emailId);
		if (employerExists != null) {

			employerExists.setEmailVerified(true);
			employerRepository.save(employerExists);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", employerExists);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {

			EmployerModel employer = new EmployerModel();
			employer.setEmailId(emailId);
			employer.setToken(token);
			employer.setDeactivated(false);
			employer.setEmailVerified(true);
			employer.setGoogleUserName(name);
			employer.setCompanyDetailsFilled(false);
			employer.setContactDetailsFilled(false);
			employer.setUsedFreeTrial("Yes");
			employer.setFromWeb(true);
			employer.setEmailNotification(true);
			employer.setPushNotification(true);
			employer.setWhatsappNotification(true);
			employer.setNotificationSound(true);

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			employer.setLastLoginDate(dtf.format(now));

			employerRepository.save(employer);

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
			map.put("message", "Register Successfully");
			map.put("code", 200);
			map.put("data", employer);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	/*
	 * @PostMapping("/webRegister") public ResponseEntity<?>
	 * createWebEmployer(@RequestParam("email_id") final String emailId,
	 * 
	 * @RequestParam("password") final String password, @RequestParam("state") final
	 * String state,
	 * 
	 * @RequestParam("industry") final String
	 * industry, @RequestParam("company_name") final String companyName) throws
	 * TemplateException, MessagingException, IOException { EmployerModel
	 * employerExists = employerRepository.findByEmailId(emailId); if
	 * (employerExists != null) { HashMap<String, Object> map = new HashMap<>();
	 * map.put("code", 200); map.put("message", "Email ID Already exists"); return
	 * new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
	 * 
	 * } else {
	 * 
	 * String token = UUID.randomUUID().toString(); String pass =
	 * passwordEncoder.encode(password);
	 * 
	 * EmployerModel employer = new EmployerModel(); employer.setEmailId(emailId);
	 * employer.setPassword(pass); employer.setToken(token);
	 * employer.setDeactivated(false); employer.setEmailVerified(false);
	 * employer.setUsedFreeTrial("No"); employer.setFromWeb(true);
	 * 
	 * employer.setIndustry(industry); employer.setCompanyName(companyName);
	 * employer.setState(state);
	 * 
	 * employerRepository.save(employer);
	 * 
	 * CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
	 * logEventModel.setType("Profile"); logEventModel.setMessage("success");
	 * logEventModel.setDescription(employer.toString());
	 * 
	 * try { cloudWatchLogService.cloudLog(logEventModel, "E"); } catch (Exception
	 * e) {
	 * 
	 * }
	 * 
	 * EmployerActivityModel EA = new EmployerActivityModel();
	 * EA.setEmpId(employer.getId()); EA.setActivity("<b>" +
	 * employer.getCompanyName() + "</b>" + " has been registered successfully!");
	 * empActivityRepository.save(EA);
	 * 
	 * HashMap<String, Object> map = new HashMap<>(); map.put("status", "success");
	 * map.put("message", "Register Successfully"); map.put("code", 200);
	 * map.put("data", employer); return new ResponseEntity<>(map,
	 * HttpStatus.CREATED); } }
	 */

	@PostMapping("/registerCompanyDetails")
	public ResponseEntity<?> updateEmployer(@RequestBody EmployerModel emp) {
		EmployerModel employerExists = employerRepository.findByEmailId(emp.getEmailId());
		if (employerExists != null) {

			employerExists.setIndustry(emp.getIndustry());
			employerExists.setCompanyName(emp.getCompanyName());
			employerExists.setCategory("Company");
			employerExists.setCompanyDetailsFilled(true);

			if (emp.getAddress() != null && !emp.getAddress().isEmpty()) {
				employerExists.setState(emp.getState());
				employerExists.setAddress(emp.getAddress());
				employerExists.setCity(emp.getCity());
				employerExists.setState(emp.getState());
				employerExists.setCountry(emp.getCountry());
				employerExists.setArea(emp.getArea());
				employerExists.setPincode(emp.getPincode());
				employerExists.setLatitude(emp.getLatitude());
				employerExists.setLongitude(emp.getLongitude());

			}

			employerRepository.save(employerExists);

			DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
			String date = formatter.format(new Date());
			SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
			Calendar cal = Calendar.getInstance();
			String time = simpleDateFormat1.format(cal.getTime());

			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(employerExists.getId());
			EA.setActivity("<b>" + employerExists.getCompanyName() + "</b>" + " has been registered successfully!");
			empActivityRepository.save(EA);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "Register Successfully");
			map.put("code", 200);
			map.put("data", employerExists);
			return new ResponseEntity<>(map, HttpStatus.CREATED);

		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Email ID does not exist");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/emailVerified")
	public ResponseEntity<?> updateEmployer(@RequestParam("email_id") final String emailId,
			@RequestParam("email_verified") final boolean emailVerified) {
		EmployerModel employerExists = employerRepository.findByEmailId(emailId);
		if (employerExists != null) {

			employerExists.setEmailVerified(emailVerified);

			employerRepository.save(employerExists);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "Email Verified");
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.CREATED);

		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Email ID does not exist");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path = "/updateCompanyLogo")
	public ResponseEntity<?> updateCompanyLogo(@RequestParam("employer_id") final int id,
			@RequestPart(value = "logo") MultipartFile photo) throws IOException {
		Optional<EmployerModel> optional = employerRepository.findById(id);

		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Employer is not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {

			EmployerModel existing = optional.get();

			String image = existing.getCompanyLogo();
			if (image != null && !image.isEmpty()) {
				boolean imageResult = employerService.deleteCompanyLogo(image);
			}

			String logo = this.employerService.uploadCompanyLogo(photo, photo.getBytes());

			if (logo != null && !logo.isEmpty()) {

				existing.setCompanyLogo(logo);

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
				map.put("code", 200);
				map.put("message", "Image Not Saved");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			}

		}
	}

	@PostMapping("/kycUpdate")
	public ResponseEntity<?> kycUpdate(@RequestParam("employer_id") final int empId,
			@RequestPart(value = "reg_proof_number", required = false) String regProofNum,
			@RequestPart(value = "company_logo", required = false) MultipartFile photo)
			throws TemplateException, MessagingException, IOException {

		Optional<EmployerModel> details = employerRepository.findById(empId);

		if (details != null && details.isPresent()) {

			EmployerModel existing = details.get();

			kycInitiated(existing);

			String image = existing.getCompanyLogo();
			if (image != null && !image.isEmpty()) {
				boolean imageResult = employerService.deleteCompanyLogo(image);
			}

			if (photo != null && !photo.isEmpty()) {
				String logo = this.employerService.uploadCompanyLogo(photo, photo.getBytes());
				if (logo != null && !logo.isEmpty()) {
					existing.setCompanyLogo(logo);
					EmployerActivityModel EA = new EmployerActivityModel();
					EA.setEmpId(empId);
					EA.setActivity("Your <b>company logo</b> has been updated!");
					empActivityRepository.save(EA);
				}
			}

			if (regProofNum != null && !regProofNum.isEmpty()) {
				existing.setRegProofNumber(regProofNum);
			}
			existing.setKycStatus("U");
			employerRepository.save(existing);

			Map<String, String> kycEmailData = new HashMap<String, String>();
			kycEmailData.put("name", existing.getContactPersonName());
			logger.info(kycEmailData.toString());

			/*
			 * TupleStore tupleStore = new TupleStore();
			 * tupleStore.setKey(existing.getEmailId());
			 * tupleStore.setValue(gson.toJson(kycEmailData));
			 * amazonSESMailUtil.sendEmailSES("EmployerKYCReviewTemplateV1", tupleStore);
			 */

			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(existing.getId());
			EA.setActivity("<b>KYC</b> has been updated!");
			empActivityRepository.save(EA);

			DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
			String date = formatter.format(new Date());
			SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
			Calendar cal = Calendar.getInstance();
			String time = simpleDateFormat1.format(cal.getTime());

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "KYC Updated Successfully");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	private void kycInitiated(EmployerModel existing) { // TODO Auto-generated
	  
	  
	  }

	@GetMapping(path = "/employer")
	public ResponseEntity<?> emppDetail(@RequestParam("email_id") final String emailId) {

		EmployerModel emp = employerRepository.findByEmailId(emailId);
		if (emp != null) {
			emp.setPassword("");

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", emp);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Employer Not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@GetMapping(path = "/empDetails")
	public ResponseEntity<?> employerrDetails(@RequestParam("emp_id") final int id) {

		Optional<EmployerModel> emp = employerRepository.findById(id);
		if (emp != null) {
			EmployerModel existing = emp.get();
			existing.setPassword("");

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", existing);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Employer Not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
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
				map.put("code", 200);
				map.put("message", "KYC Document is not yet uploaded");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Employer Not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@GetMapping("/dashboard")
	public ResponseEntity<?> getEmpDashboardDetails(@RequestParam("emp_id") final int empId,
			@RequestParam("start_date") final String startDate, @RequestParam("end_date") final String endDate) {

		Map<String, Object> countDetails = employerRepository.getEmpDashboardDetails(empId, startDate, endDate);
		List<Map<String, Object>> invoiceDetails = employerRepository.getEmpInvoiceDetails(empId, startDate, endDate);
		List<EmployerActivityModel> activities = empActivityRepository.getEmpRecentActivity(empId, startDate, endDate);

		HashMap<String, Object> map = new HashMap<>();
		map.put("count", countDetails);
		map.put("RecentActivity", activities);
		map.put("invoiceDetails", invoiceDetails);
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@PostMapping(path = "/login")
	public ResponseEntity<?> empLogin(@RequestParam("email_id") final String emailId,
			@RequestParam(value = "password", required = false) final String password,
			@RequestParam("type") final String type) {

		EmployerModel employerExists = null;
		if (type.equalsIgnoreCase("E")) {
			employerExists = employerRepository.findByEmailId(emailId);

			if (employerExists != null) {

				if (passwordEncoder.matches(password, employerExists.getPassword())) {

					Optional<EmployerModel> check = employerRepository.login(emailId, password);

					String token = UUID.randomUUID().toString();
					employerExists.setToken(token);
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					LocalDateTime now = LocalDateTime.now();
					employerExists.setLastLoginDate(dtf.format(now));
					employerRepository.save(employerExists);

					employerExists.setPassword("");

					if (check != null) {
						HashMap<String, Object> map = new HashMap<>();
						map.put("statuscode", 200);
						map.put("message", "Login Successfully");
						map.put("results", employerExists);
						return new ResponseEntity<>(map, HttpStatus.OK);
					} else {
						HashMap<String, Object> map = new HashMap<>();
						map.put("statuscode", 200);
						map.put("message", "Incorrect Details");
						return new ResponseEntity<>(map, HttpStatus.OK);

					}
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", "Incorrect Password");
					return new ResponseEntity<>(map, HttpStatus.OK);

				}
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Email ID does not exists");
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		} else {
			long email = Long.parseLong(emailId);
			employerExists = employerRepository.findByMobileNumber(email);
			if (employerExists != null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Login Successfully");
				map.put("results", employerExists);
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Mobile Number does not exists");
				return new ResponseEntity<>(map, HttpStatus.OK);

			}
		}

	}

	@GetMapping(path = "/registered")
	public ResponseEntity<?> registrationCheck(@RequestParam("email_id") final String emailId,
			@RequestParam("type") final String type) {

		EmployerModel employerExists = null;
		if (type.equalsIgnoreCase("E")) {
			employerExists = employerRepository.findByEmailId(emailId);

			if (employerExists != null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("status", true);
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("status", false);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		} else {
			long email = Long.parseLong(emailId);
			employerExists = employerRepository.findByMobileNumber(email);
			if (employerExists != null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("status", true);
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("status", false);
				return new ResponseEntity<>(map, HttpStatus.OK);

			}
		}

	}

	@PutMapping(path = "/updateProfile")
	public ResponseEntity<?> updateProfile(@RequestBody EmployerModel emp) {

		Optional<EmployerModel> details = employerRepository.findById(emp.getId());

		if (details.isPresent()) {

			EmployerModel existing = details.get();

			if (emp.getNoOfEmployees() != null && !emp.getNoOfEmployees().isEmpty()) {
				existing.setNoOfEmployees(emp.getNoOfEmployees());
			}

			if (emp.getYearFounded() != null && !emp.getYearFounded().isEmpty()) {
				existing.setYearFounded(emp.getYearFounded());
			}

			if (emp.getWebsiteUrl() != null && !emp.getWebsiteUrl().isEmpty()) {
				existing.setWebsiteUrl(emp.getWebsiteUrl());
			}

			if (emp.getPhone() != 0) {
				existing.setPhone(Long.valueOf(emp.getPhone()));

			}

			if (emp.getPhoneCountryCode() != null && !emp.getPhoneCountryCode().isEmpty()) {
				existing.setPhoneCountryCode(emp.getPhoneCountryCode());

			}

			if (emp.getContactPersonName() != null && !emp.getContactPersonName().isEmpty()) {

				existing.setContactPersonName(emp.getContactPersonName());
			}
			if (emp.getMobileNumber() != 0) {
				existing.setMobileNumber(Long.valueOf(emp.getMobileNumber()));
			}
			if (emp.getMobileCountryCode() != null && !emp.getMobileCountryCode().isEmpty()) {

				existing.setMobileCountryCode(emp.getMobileCountryCode());
			}
			if (emp.getAlternateMobileNumber() != null && !emp.getAlternateMobileNumber().isEmpty()) {
				existing.setAlternateMobileNumber(emp.getAlternateMobileNumber());
			}
			if (emp.getAddress() != null && !emp.getAddress().isEmpty()) {
				existing.setAddress(emp.getAddress());
				// existing.setCity(emp.getCity());
				existing.setState(emp.getState());
				existing.setCountry(emp.getCountry());
				existing.setPincode(emp.getPincode());
				existing.setLatitude(emp.getLatitude());
				existing.setLongitude(emp.getLongitude());
			}

			employerRepository.save(existing);

			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(existing.getId());
			EA.setActivity("Your <b>company profile</b> has been updated!");
			empActivityRepository.save(EA);

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
	
	@GetMapping("/notificationsByStatus")
	public ResponseEntity<?> getEmpInterviewDetails(@RequestParam("emp_id") final int empId,
			@RequestParam("status") final String status) {

		List<Map<String, Object>> details = interviewRepository.getEmpInterviewNotification(empId);

		if (!details.isEmpty() && details != null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Notifications Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	
	@PutMapping(path = "/updateLocation")
	public ResponseEntity<?> updateLocation(@RequestParam("employer_id") final int employer_id,
			@RequestParam(value = "address") final String address, @RequestParam(value = "city") final String city,
			@RequestParam(value = "state", required = false) final String state, @RequestParam(value = "country", required = false) final String country,
			@RequestParam(value = "pin_code", required = false) final int code,@RequestParam(value = "area", required = false) final String area,
			@RequestParam(value = "latitude", required = false) final String latitude,
			@RequestParam(value = "longitude", required = false) final String longitude) {

		Optional<EmployerModel> details = employerRepository.findById(employer_id);

		if (details.isPresent()) {

			EmployerModel existing = details.get();
			existing.setAddress(address);
			existing.setCity(city);
			existing.setArea(area);
			existing.setState(state);
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
	
	@GetMapping("/placementPlans")
	public ResponseEntity<?> getPlacementPlans(
	    @RequestParam(value = "emp_id",required = false) final int empId,
	    @RequestParam("type") final String type) {

	        boolean Ctype = type.equalsIgnoreCase("Experienced");
	        
	    		List<PlansModel> list = plansRepository.findByExperiencedStatus(Ctype,true);
	    		if(list!=null) {
	            Map<String, Object> map = new HashMap<>();
	            map.put("status", "success");
	            map.put("message", "success");
	            map.put("code", 200);
	            map.put("data", list.get(0));
	            return new ResponseEntity<>(map, HttpStatus.OK);
	    		}
	    		else {
	    			HashMap<String, Object> map = new HashMap<>();
	    			map.put("code", 400);
	    			map.put("message", "No Plans Available");
	    			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
	    		}
	   
	}


}
