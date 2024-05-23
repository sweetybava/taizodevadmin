package com.taizo.controller.employer;

import java.io.ByteArrayOutputStream; 
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import javax.mail.MessagingException;

import com.google.gson.Gson;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.taizo.controller.gallabox.ExotelCallController;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.service.CloudWatchLogService;
import com.taizo.service.EmployerService;
import com.taizo.service.PlansService;
import com.taizo.service.WAAlertService;
import com.taizo.utils.FreeMarkerUtils;
import com.taizo.utils.TupleStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import freemarker.template.TemplateException;

@CrossOrigin
@RestController
@RequestMapping("/employer")
public class NewRegistrationController {

	private static final Logger logger = LoggerFactory.getLogger(NewRegistrationController.class);

	@Autowired
	EmployerCallRepository employerCallRepository;
	
	@Autowired
	AdminRepository adminRepository;

	@Autowired
	AmazonSESMailUtil amazonSESMailUtil;
	
	@Autowired
	ExotelCallController exotelCallController;

	@Autowired
	private FreeMarkerUtils freeMarkerUtils;


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
	EmpActivityRepository empActivityRepository;

	@Autowired
	JobApplicationRepository jobApplicationRepository;

	@Autowired
	EmployerJobPersonalizationRepository employerJobPersonalizationRepository;

	@Autowired
	PlansRepository plansRepository;

	@Autowired
	LeadRepository leadRepository;
	
	@Autowired
	CfgCitiesRepository cfgCitiesRepository;

	@Autowired
	CloudWatchLogService cloudWatchLogService;

	@Value("${aws.host.url}")
	private String baseUrl;
	
	@Value("${spring.profiles.active}")
	private String activeProfile;

	private Gson gson = new Gson();
	
	@Autowired
	SalesLeadRepository salesLeadRepository;
	
	@Autowired
	WAAlertService waAlertService;
	
	
	@PostMapping("/salesLead")
	public ResponseEntity<?> createSalesLead(@RequestBody SalesLeadModel sales) {
		EmployerModel existing = employerRepository.findTopByMobileNumber(sales.getMobileNumber());

		SalesLeadModel lead = new SalesLeadModel();
			lead.setEmailId(sales.getEmailId());
			lead.setMobileNumber(sales.getMobileNumber());
			lead.setMobileCountryCode(sales.getMobileCountryCode());
			lead.setCompanyName(sales.getCompanyName());
			lead.setContactPersonName(sales.getContactPersonName());
			lead.setBusinessType(sales.getBusinessType());
			lead.setLocation(sales.getLocation());
			if(existing!=null) {
				lead.setRegisteredInApp(true);
				lead.setEmpId(existing.getId());
				existing.setCategory(sales.getBusinessType());
				employerRepository.save(existing);
				
			}
			if(sales.getEmpId()!=0) {
				lead.setEmpId(sales.getEmpId());
				lead.setRegisteredInApp(true);
			}
			salesLeadRepository.save(lead);
			
			if (activeProfile.equalsIgnoreCase("prod")) {
				HashMap<String, String> emailDataHM = new HashMap<>();
				emailDataHM.put("CompanyName", sales.getCompanyName() != null ? sales.getCompanyName() : "");
				emailDataHM.put("SalesTeamMember", "Saravanan");
				emailDataHM.put("BusinessType", sales.getBusinessType() != null ? sales.getBusinessType() : "");
				emailDataHM.put("ContactName", sales.getContactPersonName() != null ? sales.getContactPersonName() : "");
				emailDataHM.put("ContactEmail", sales.getEmailId() != null ? sales.getEmailId() : "");
				emailDataHM.put("ContactNumber", String.valueOf(sales.getMobileNumber()));
			String message = null;
			try {
				message = freeMarkerUtils.getHtml1("SalesLead.html", emailDataHM);

				ByteArrayOutputStream target = new ByteArrayOutputStream();

				ConverterProperties converterProperties = new ConverterProperties();
				converterProperties.setBaseUri("http://localhost:8000");

				HtmlConverter.convertToPdf(message, target, converterProperties);

				byte[] bytes = target.toByteArray();
				String subject = "Sales Lead";
				String email = "k.saravanan001@gmail.com";

				amazonSESMailUtil.sendEmailEmpAlert(email, subject, message, bytes);
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			}
			if (activeProfile.equalsIgnoreCase("prod")) {
			HashMap<String, String> sdata = new HashMap<>();
			sdata.put("Event Name", "Employer Alert");
			sdata.put("Event Type", "Sales Lead");
			sdata.put("Type", "Employer");
			sdata.put("Contact Person Name", sales.getContactPersonName() != null ? sales.getContactPersonName() : "");
			sdata.put("Company", sales.getCompanyName() != null ? sales.getCompanyName() : "");
			sdata.put("Mobile Number", String.valueOf(sales.getMobileNumber()));
			sdata.put("Location", sales.getLocation());
			sdata.put("Source", "App");
			sdata.put("ID Type", "Emp ID");
			sdata.put("ID", String.valueOf(sales.getEmpId()));

			exotelCallController.connectToAgent("+91" + String.valueOf(sales.getMobileNumber()),"Emp",sdata);
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "Lead created");
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@PostMapping("/newRegister")
	public ResponseEntity<?> createNewEmployer(@RequestParam("mobile_number") final long mn,
			@RequestParam("country_code") final String ccode) {
		Optional<LeadModel> details = leadRepository.findByMobileNumber(mn);
		EmployerModel em = employerRepository.findByMobileNumber(mn);

		if (em != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Mobile Number Already exists");
			map.put("data", em);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {
			if (details.isPresent()) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("status", "success");
				map.put("message", "success");
				map.put("code", 200);
				map.put("data", details.get());
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				LeadModel emp = new LeadModel();

				emp.setMobileNumber(mn);
				emp.setMobileCountryCode(ccode);
				emp.setMnverified(false);
				emp.setRegistered(false);
				emp.setRegisteredInApp(false);
				emp.setFromApp(true);
				emp.setFromWeb(false);
				emp.setFromFacebook(false);
				emp.setFromWhatsapp(false);
				emp.setFromWebBot(false);

				leadRepository.save(emp);

				HashMap<String, Object> map = new HashMap<>();
				map.put("status", "success");
				map.put("message", "success");
				map.put("code", 200);
				map.put("data", emp);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		}

	}

	@PutMapping("/mnVerified")
	public ResponseEntity<?> leadmnVerified(@RequestParam("mobile_number") final long mn,
			@RequestParam("verified") final boolean verified) {
		Optional<LeadModel> details = leadRepository.findByMobileNumber(mn);
		if (details.isPresent()) {
			LeadModel emp = details.get();

			emp.setMnverified(verified);
			
			regInitiated(String.valueOf(mn));

			leadRepository.save(emp);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "Updated Successfully");
			map.put("code", 200);
			map.put("data", emp);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Mobile Number not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping("/empMNVerified")
	public ResponseEntity<?> MainmnVerified(@RequestParam("mobile_number") final long mn,
			@RequestParam("verified") final boolean verified) {
		EmployerModel em = employerRepository.findByMobileNumber(mn);
		if (em != null) {
			if(!em.isDeactivated()) {
			String token = UUID.randomUUID().toString();
			em.setMnVerified(verified);
			em.setToken(token);

			employerRepository.save(em);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "Updated Successfully");
			map.put("code", 200);
			map.put("data", em);
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
			map.put("message", "Mobile Number not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping("/companyBasicDetails")
	public ResponseEntity<?> updateLeadEmployer(@RequestBody EmployerModel emp) {

	    List<LeadModel> leadMN = leadRepository.findByMobileNumberList(emp.getMobileNumber());
	    EmployerModel em = employerRepository.findByEmailId(emp.getEmailId());

	    if (em != null) {
	        // Existing employer found

	        String token = UUID.randomUUID().toString();

	        em.setMobileNumber(emp.getMobileNumber());
	        em.setMobileCountryCode(emp.getMobileCountryCode());
	        em.setToken(token);

	        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	        LocalDateTime now = LocalDateTime.now();
	        em.setLastLoginDate(dtf.format(now));

	        em.setIndustry(emp.getIndustry());
	        em.setCompanyName(emp.getCompanyName());
	        if (emp.getCategory() != null) {
	            em.setCategory(emp.getCategory());
	        } else {
	            em.setCategory("Company");
	        }
	        em.setCompanyDetailsFilled(true);
	        em.setContactDetailsFilled(false);

	        if (emp.getAddress() != null && !emp.getAddress().isEmpty()) {
	            em.setState(emp.getState());
	            em.setAddress(emp.getAddress());
	            em.setCity(emp.getCity());
	            em.setArea(emp.getArea());
	            em.setCountry(emp.getCountry());
	            em.setPincode(emp.getPincode());
	            em.setLatitude(emp.getLatitude());
	            em.setLongitude(emp.getLongitude());
	        }
	        em.setAssignTo(2);

	        employerRepository.save(em);

	        if (!leadMN.isEmpty()) {
	            for (LeadModel l : leadMN) {
	                leadRepository.deleteById(l.getId());
	            }
	        }

	        List<LeadModel> leadEmail = leadRepository.findByEmailId(emp.getEmailId());
	        if (!leadEmail.isEmpty()) {
	            for (LeadModel l : leadEmail) {
	                leadRepository.deleteById(l.getId());
	            }
	        }

	        HashMap<String, Object> map = new HashMap<>();
	        map.put("status", "success");
	        map.put("message", "success");
	        map.put("code", 200);
	        map.put("data", em);
	        return new ResponseEntity<>(map, HttpStatus.OK);

	    } else {
	        // New employer

	        String token = UUID.randomUUID().toString();

	        EmployerModel employer = new EmployerModel();
	        employer.setEmailId(emp.getEmailId());
	        employer.setMobileNumber(emp.getMobileNumber());
	        employer.setMobileCountryCode(emp.getMobileCountryCode());
	        
	        if (!leadMN.isEmpty()) {
	            employer.setMnVerified(leadMN.get(0).isMnverified());
	        }

	        employer.setToken(token);
	        employer.setDeactivated(false);
	        employer.setEmailVerified(false);
	        employer.setCountry("India");
	        employer.setEmailNotification(true);
	        employer.setPushNotification(true);
	        employer.setWhatsappNotification(true);
	        employer.setNotificationSound(true);
	        employer.setUsedFreeTrial("Yes");
	        employer.setFromApp(true);
	        employer.setFromWeb(false);

	        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	        LocalDateTime now = LocalDateTime.now();
	        employer.setLastLoginDate(dtf.format(now));

	        employer.setIndustry(emp.getIndustry());
	        employer.setCompanyName(emp.getCompanyName());
	        if (emp.getCategory() != null) {
	            employer.setCategory(emp.getCategory());
	        } else {
	            employer.setCategory("Company");
	        }
	        employer.setCompanyDetailsFilled(true);
	        employer.setContactDetailsFilled(false);

	        if (emp.getAddress() != null && !emp.getAddress().isEmpty()) {
	            employer.setState(emp.getState());
	            employer.setAddress(emp.getAddress());
	            employer.setCity(emp.getCity());
	            employer.setArea(emp.getArea());
	            employer.setCountry(emp.getCountry());
	            employer.setPincode(emp.getPincode());
	            employer.setLatitude(emp.getLatitude());
	            employer.setLongitude(emp.getLongitude());
	        }
	        employer.setAssignTo(2);
	        
	        if (employer.getAssignTo() == 2) {
	            waAlertService.sendEmployerLead(employer);
	        }

	        employerRepository.save(employer);

	        if (!leadMN.isEmpty()) {
	            for (LeadModel l : leadMN) {
	                leadRepository.deleteById(l.getId());
	            }
	        }

	        List<LeadModel> leadEmail = leadRepository.findByEmailId(emp.getEmailId());
	        if (!leadEmail.isEmpty()) {
	            for (LeadModel l : leadEmail) {
	                leadRepository.deleteById(l.getId());
	            }
	        }

	        HashMap<String, Object> map = new HashMap<>();
	        map.put("status", "success");
	        map.put("message", "success");
	        map.put("code", 200);
	        map.put("data", employer);
	        return new ResponseEntity<>(map, HttpStatus.OK);
	    }
	}

		

	@PutMapping("/companyContactDetails")
	public ResponseEntity<?> updateContactDetails(@RequestBody EmployerModel emp) {

		EmployerModel existing = employerRepository.findByEmailId(emp.getEmailId());
		if (existing != null) {

			existing.setContactPersonName(emp.getContactPersonName());
			existing.setPhoneCountryCode(emp.getPhoneCountryCode());
			existing.setPhone(Long.valueOf(emp.getPhone()));
			existing.setWebsiteUrl(emp.getWebsiteUrl());
			existing.setYearFounded(emp.getYearFounded());
			existing.setWhatsappNumber(Long.valueOf(emp.getWhatsappNumber()));
			existing.setNoOfEmployees(emp.getNoOfEmployees());
			existing.setContactDetailsFilled(true);

			employerRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", existing);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Email ID does not exist");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping("/companyOtherDetails")
	public ResponseEntity<?> kycUpdate(@RequestParam("emp_id") final int empId,
			@RequestParam(value = "reg_proof", required = false) final String regProof,
			@RequestParam(value = "reference", required = false) final String reference,
			@RequestPart(value = "logo", required = false) MultipartFile logo)
			throws TemplateException, MessagingException, IOException {

		Optional<EmployerModel> details = employerRepository.findById(empId);
		Admin a= new Admin();

		if (details != null && details.isPresent()) {

			String token = UUID.randomUUID().toString();

			EmployerModel existing = details.get();

			existing.setToken(token);
			existing.setReference(reference);

			if (regProof != null && !regProof.isEmpty()) {
				kycInitiated(existing);
			}

			String image = existing.getCompanyLogo();
			if (image != null && !image.isEmpty()) {
				boolean imageResult = employerService.deleteCompanyLogo(image);
			}

			if (logo != null && !logo.isEmpty()) {
				String ProfilePhoto = this.employerService.uploadCompanyLogo(logo, logo.getBytes());
				if (ProfilePhoto != null && !ProfilePhoto.isEmpty()) {
					existing.setCompanyLogo(ProfilePhoto);
				}
			}

			if (regProof != null && !regProof.isEmpty()) {
				EmployerModel em = employerRepository.findTopByregProofNumber(regProof);
				if (em != null) {
					existing.setUsedFreeTrial("Yes");
				}
			}

			if (regProof != null && !regProof.isEmpty()) {
				existing.setRegProofNumber(regProof);
				existing.setKycStatus("U");

				DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
				String date = formatter.format(new Date());
				SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
				Calendar cal = Calendar.getInstance();
				String time = simpleDateFormat1.format(cal.getTime());

			}

			
			employerRepository.save(existing);

			sendRegNoti(existing, regProof);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "Updated Successfully");
			map.put("data", existing);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@Async
	private void sendRegNoti(EmployerModel existing, String regProof) {
		// TODO Auto-generated method stub

		DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
		String date = formatter.format(new Date());
		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
		Calendar cal = Calendar.getInstance();
		String time = simpleDateFormat1.format(cal.getTime());

		if (activeProfile.equalsIgnoreCase("prod")) {
		HashMap<String, String> sdata = new HashMap<>();
		sdata.put("Event Name", "Employer Alert");
		sdata.put("Event Type", "Employer Registration Completed");
		sdata.put("Type", "Employer");
		sdata.put("Contact Person Name", existing.getContactPersonName() != null ? existing.getContactPersonName() : "");
		sdata.put("Company Name", existing.getCompanyName() != null ? existing.getCompanyName() : "");
		sdata.put("Location", existing.getCity() != null ? existing.getCity() : "");
		sdata.put("Mobile Number", String.valueOf(existing.getMobileNumber()));
		sdata.put("Source", "App");
		sdata.put("ID Type", "Emp ID");
		sdata.put("ID", String.valueOf(existing.getId()));

		exotelCallController.connectToAgent("+91" + String.valueOf(existing.getMobileNumber()),"Emp",sdata);
		}

		EmployerActivityModel EA1 = new EmployerActivityModel();
		EA1.setEmpId(existing.getId());
		EA1.setActivity("<b>" + existing.getCompanyName() + "</b>" + " has been registered successfully!");
		empActivityRepository.save(EA1);
		if (regProof != null && !regProof.isEmpty()) {
			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(existing.getId());
			EA.setActivity("<b>KYC</b> has been updated!");
			empActivityRepository.save(EA);
		}
	}

	private void kycInitiated(EmployerModel existing) { // TODO Auto-generated
	  
	  
	  }

	@PostMapping("/register")
	public ResponseEntity<?> createEmployer(@RequestParam("email_id") final String emailId,
			@RequestParam("password") final String password) throws TemplateException, MessagingException, IOException {
		EmployerModel employerExists = employerRepository.findByEmailId(emailId);
		if (employerExists != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Email ID Already exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		} else {

			String token = UUID.randomUUID().toString();
			String pass = passwordEncoder.encode(password);

			EmployerModel employer = new EmployerModel();
			employer.setEmailId(emailId);
			employer.setPassword(pass);
			employer.setToken(token);
			employer.setDeactivated(false);
			employer.setEmailVerified(false);
			employer.setCountry("India");
			employer.setCompanyDetailsFilled(false);
			employer.setContactDetailsFilled(false);
			employer.setEmailNotification(true);
			employer.setPushNotification(true);
			employer.setWhatsappNotification(true);
			employer.setNotificationSound(true);
			employer.setUsedFreeTrial("No");
			employer.setFromWeb(false);
			employer.setRegisteredInApp(true);
			employer.setRegistered(true);

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			employer.setLastLoginDate(dtf.format(now));

			employerRepository.save(employer);

			//regInitiated(emailId);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "Register Successfully");
			map.put("code", 200);
			map.put("data", employer);
			return new ResponseEntity<>(map, HttpStatus.CREATED);
		}
	}

	@PostMapping("/registerCompanyDetails")
	public ResponseEntity<?> updateEmployer(@RequestBody EmployerModel emp) {

		EmployerModel employerExists = employerRepository.findByEmailId(emp.getEmailId());
		if (employerExists != null) {

			employerExists.setIndustry(emp.getIndustry());
			employerExists.setCompanyName(emp.getCompanyName());
			employerExists.setCategory("Company");
			employerExists.setCompanyDetailsFilled(true);

			if (emp.getAddress() != null && !emp.getAddress().isEmpty()) {
				employerExists.setAddress(emp.getAddress());
				employerExists.setCity(emp.getCity());
				employerExists.setState(emp.getState());
				employerExists.setArea(emp.getArea());
				employerExists.setCountry(emp.getCountry());
				employerExists.setPincode(emp.getPincode());
				employerExists.setLatitude(emp.getLatitude());
				employerExists.setLongitude(emp.getLongitude());
			}

			int resetCode = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);

			employerExists.setResetCode(String.valueOf(resetCode));

			employerRepository.save(employerExists);

			HashMap<String, String> emailDataHM = new HashMap<>();
			emailDataHM.put("code", employerExists.getResetCode());
			emailDataHM.put("emailId", employerExists.getEmailId());

			TupleStore tupleStore = new TupleStore();
			tupleStore.setKey(emp.getEmailId());
			tupleStore.setValue(gson.toJson(emailDataHM));
			amazonSESMailUtil.sendEmailSES("EmployerEmailValidationTemplate", tupleStore);

			HashMap<String, String> emailData = new HashMap<>();
			emailData.put("name", employerExists.getContactPersonName());
			emailData.put("companyName", employerExists.getCompanyName());

			/*
			 * TupleStore tupleStore1 = new TupleStore();
			 * tupleStore1.setKey(employerExists.getEmailId());
			 * tupleStore1.setValue(gson.toJson(emailData));
			 * amazonSESMailUtil.sendEmailSES("EmployerRegistrationTemplateV1",
			 * tupleStore1);
			 */

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
			map.put("code", 400);
			map.put("message", "Email ID does not exist");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
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
			employer.setGoogleUserName(name);
			employer.setToken(token);
			employer.setDeactivated(false);
			employer.setEmailVerified(true);
			employer.setCompanyDetailsFilled(false);
			employer.setContactDetailsFilled(false);
			employer.setUsedFreeTrial("No");
			employer.setFromWeb(false);
			employer.setRegistered(true);
			employer.setRegisteredInApp(true);
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

			// regInitiated(emailId);

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "Register Successfully");
			map.put("code", 200);
			map.put("data", employer);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	@Async
	private void regInitiated(String mob) { 
	   DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
	  String date = formatter.format(new Date()); 
	  SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa"); 
	  Calendar cal = Calendar.getInstance(); 
	  String time = simpleDateFormat1.format(cal.getTime());
	   
	  
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
			map.put("data", employerExists);
			return new ResponseEntity<>(map, HttpStatus.CREATED);

		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Email ID does not exist");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(path = "/empLogin")
	public ResponseEntity<?> empLogin(@RequestParam("email_id") final String emailId,
			@RequestParam("password") final String password) {

		EmployerModel employerExists = employerRepository.findByEmailId(emailId);
		if (employerExists != null) {

			if (passwordEncoder.matches(password, employerExists.getPassword())) {

				Optional<EmployerModel> check = employerRepository.login(emailId, password);

				String token = UUID.randomUUID().toString();
				employerExists.setToken(token);
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime now = LocalDateTime.now();
				employerExists.setLastLoginDate(dtf.format(now));
				employerRepository.save(employerExists);

				if (check != null) {


					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", "Login Successfully");
					map.put("results", employerExists);
					return new ResponseEntity<>(map, HttpStatus.OK);
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 400);
					map.put("message", "Incorrect Details");
					map.put("results", null);
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

				}
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "Incorrect Password");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Email ID does not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@GetMapping(path = "/employer")
	public ResponseEntity<?> empDetails(@RequestParam("email_id") final String emailId) {

		EmployerModel emp = employerRepository.findByEmailId(emailId);
		if (emp != null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", emp);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
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

		HashMap<String, Object> detailMap = new HashMap<>();

		detailMap.put("count", countDetails);
		detailMap.put("RecentActivity", activities);
		detailMap.put("invoiceDetails", invoiceDetails);

		HashMap<String, Object> map = new HashMap<>();
		map.put("status", "success");
		map.put("message", "success");
		map.put("code", 200);
		map.put("data", detailMap);

		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@PostMapping(path = "/forgot")
	public ResponseEntity<?> requestForgot(@RequestParam("email_id") final String emailId,
			@RequestParam("status") final String status) {

		EmployerModel emp = employerRepository.findByEmailId(emailId);
		if (emp != null) {

			int resetCode = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);

			emp.setResetCode(String.valueOf(resetCode));
			employerRepository.save(emp);
			if (status.equalsIgnoreCase("R")) {
				HashMap<String, String> emailDataHM = new HashMap<>();
				emailDataHM.put("code", emp.getResetCode());
				emailDataHM.put("emailId", emp.getEmailId());

				TupleStore tupleStore = new TupleStore();
				tupleStore.setKey(emailId);
				tupleStore.setValue(gson.toJson(emailDataHM));
				amazonSESMailUtil.sendEmailSES("EmployerEmailValidationTemplate", tupleStore);

			} else {

				HashMap<String, String> emailDataHM = new HashMap<>();
				emailDataHM.put("code", emp.getResetCode());
				emailDataHM.put("companyName", emp.getCompanyName());

				TupleStore tupleStore = new TupleStore();
				tupleStore.setKey(emailId);
				tupleStore.setValue(gson.toJson(emailDataHM));
				amazonSESMailUtil.sendEmailSES("EmployerResetPasswordTemplate", tupleStore);
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", emp);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@GetMapping(path = "/reset")
	public ResponseEntity<?> reset(@RequestParam("email_id") final String emailId,
			@RequestParam("code") final String code) {

		EmployerModel employerExists = employerRepository.findByEmailId(emailId);
		if (employerExists != null) {

			Optional<EmployerModel> check = employerRepository.resetcheck(emailId, code);

			if (check != null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Code valid");
				map.put("results", check);
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "Incorrect code");
				map.put("results", null);
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Email ID does not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@PostMapping(path = "/reset")
	public ResponseEntity<?> resetPassword(@RequestParam("email_id") final String emailId,
			@RequestParam(value = "password") final String password) {

		EmployerModel employerExists = employerRepository.findByEmailId(emailId);
		if (employerExists != null) {

			String pass = passwordEncoder.encode(password);

			employerExists.setPassword(pass);
			employerRepository.save(employerExists);

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

	@GetMapping(path = "/employerExists")
	public ResponseEntity<?> getEmployerExistsStatus(@RequestParam("email_id") final String emailId) {

		EmployerModel existingUser = employerRepository.findByEmailId(emailId);
		if (existingUser != null) {

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
	
	@PutMapping(path = "/loggedIn")
	public ResponseEntity<?> lastLoggedIn(@RequestParam("emp_id") final int empId) {
		Optional<EmployerModel> existingUser = employerRepository.findById(empId);
		
		EmployerModel emp = existingUser.get();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		emp.setLastLoginDate(dtf.format(now));
		
		employerRepository.save(emp);
		
		HashMap<String, Object> map = new HashMap<>();
		map.put("status", "success");
		map.put("message", "success");
		map.put("code", 200);
		map.put("data", emp);
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

}
