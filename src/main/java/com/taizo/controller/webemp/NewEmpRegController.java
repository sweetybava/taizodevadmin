package com.taizo.controller.webemp;

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
import com.taizo.controller.gallabox.ExotelCallController;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.service.CloudWatchLogService;
import com.taizo.service.EmployerService;
import com.taizo.service.WAAlertService;
import com.taizo.utils.TupleStore;

import org.json.JSONArray;
import org.json.JSONObject;
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
@RequestMapping("/webEmployer")
public class NewEmpRegController {

	private static final Logger logger = LoggerFactory.getLogger(NewEmpRegController.class);

	@Autowired
	EmployerCallRepository employerCallRepository;

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
	EmployerPaymentRepository employerPaymentRepository;
	
	@Autowired
	private ProFormaInvoicesRepository proFormaInvoiceRepository;
	
	@Autowired
	EmpPlacementPlanDetailsRepository empPlacementPlanRepository;

	@Autowired
	CloudWatchLogService cloudWatchLogService;
	
	@Autowired
	WAAlertService waAlertService;

	@Value("${aws.host.url}")
	private String baseUrl;
	
	@Value("${spring.profiles.active}")
	private String activeProfile;

	private Gson gson = new Gson();
	
	@Autowired
	ExotelCallController exotelCallController;

	@Autowired
	EmployerTimelineRepository employerTimelineRepository;

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
				emp.setFromApp(false);
				emp.setFromWeb(true);
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
	@Async
	private void regInitiated(String mob) { 
	   DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
	  String date = formatter.format(new Date()); 
	  SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa"); 
	  Calendar cal = Calendar.getInstance(); 
	  String time = simpleDateFormat1.format(cal.getTime());
	    
	  HashMap<String, String> reg = new HashMap<>();
	  reg.put("Mobile Number", mob); 
	  reg.put("Registration Attempted Date", date);
	  reg.put("Registration Attempted Time", time); 
	  reg.put("From", "Web");
	  
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
	        // Existing employer found in the database
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
	          

	        System.out.println("ylead" + leadMN.size());
	        if (leadMN.size() > 0) {
	            for (LeadModel l : leadMN) {
	                Optional<LeadModel> lead = leadRepository.findById(l.getId());
	                
	                // Check if the lead is present before proceeding
	                if (lead.isPresent()) {
	                    EmployerPaymentModel payment = employerPaymentRepository.findByLeadId(lead.get().getId());

	                    if (payment != null) {
	                        payment.setEmployerId(em.getId());
	                        employerPaymentRepository.save(payment);

	                        Optional<EmpProformaInvoiceModel> invoiceDetails = proFormaInvoiceRepository.findById(Long.valueOf(payment.getProformaInvoiceId()));

	                        if (invoiceDetails.isPresent()) {
	                            EmpProformaInvoiceModel invoice = invoiceDetails.get();
	                            invoice.setEmployerId(em.getId());
	                            proFormaInvoiceRepository.save(invoice);

	                            String jobDetails = invoice.getJobDetails();
	                            JSONArray jobs = new JSONArray(jobDetails);
	                            System.out.println(jobs);

	                            PlansModel plan;
	                            for (int i = 0; i < jobs.length(); i++) {
	                                JSONObject job = jobs.getJSONObject(i);
	                                boolean experienced = Boolean.parseBoolean(job.get("isExperienced").toString());

	                                if (experienced) {
	                                    plan = plansRepository.findByActiveAndIsExperienced(true, true);
	                                } else {
	                                    plan = plansRepository.findByActiveAndIsExperienced(true, false);
	                                }

	                                if (plan == null) {
	                                    // Handle the case where no suitable plan is found.
	                                    // You can log an error message or throw an exception as needed.
	                                }
	                                EmpPlacementPlanDetailsModel empPlacementPlanDetails = new EmpPlacementPlanDetailsModel();
	                                empPlacementPlanDetails.setPlanId(plan.getId());
	                                empPlacementPlanDetails.setPaymentId(payment.getId());
	                                empPlacementPlanDetails.setActive(true);
	                                empPlacementPlanDetails.setFromSource("Admin");
	                                empPlacementPlanDetails.setEmployerId(em.getId());
	                                empPlacementPlanDetails.setNoOfOpenings(Integer.parseInt(job.get("noOfOpenings").toString()));
	                                empPlacementPlanDetails.setIndustry((String) job.get("industry"));
	                                empPlacementPlanDetails.setJobCategory((String) job.get("jobCategory"));
	                                empPlacementPlanDetails.setIsExperienced(job.getBoolean("isExperienced"));
	                                empPlacementPlanDetails.setJobMinExp((Integer) job.get("jobMinExp"));
									empPlacementPlanDetails.setMinSalary((Integer) job.get("maxSalary"));
									empPlacementPlanDetails.setMaxSalary((Integer) job.get("minSalary"));
									empPlacementPlanDetails.setWorkHours((String) job.get("workHours"));
	                                System.out.println(empPlacementPlanDetails);
	                                empPlacementPlanRepository.save(empPlacementPlanDetails);
	                            }
	                            leadRepository.deleteById(l.getId());
	                        } else {
	                            // invoiceDetails object is not present.
	                            leadRepository.deleteById(l.getId());
	                        } 
	                    } else {
	                        // Handle the case where payment is null.
	                        leadRepository.deleteById(l.getId());
	                    }
	                }
	            }
	        }

	        List<LeadModel> leadEmail = leadRepository.findByEmailId(emp.getEmailId());
	        if (leadEmail.size() > 0) {
	            for (LeadModel l : leadEmail) {
	                System.out.println("zzzzz");
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
	        // No existing employer found in the database
	        String token = UUID.randomUUID().toString();

	        EmployerModel employer = new EmployerModel();
	        employer.setEmailId(emp.getEmailId());
	        employer.setMobileNumber(emp.getMobileNumber());
	        employer.setMobileCountryCode(emp.getMobileCountryCode());
	        if (leadMN.size() > 0) {
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
	        employer.setFromApp(false);
	        employer.setFromWeb(true);

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

	      //  Integer adminId = getAdminIdForCity(employer.getCity());
	        employer.setAssignTo(2);
	        if (employer.getAssignTo() == 2) {
	            waAlertService.sendEmployerLead(employer);
	        }

	        employerRepository.save(employer);

	        System.out.println("new" + leadMN.size());
	        if (leadMN.size() > 0) {
	            for (LeadModel l : leadMN) {
	                Optional<LeadModel> lead = leadRepository.findById(l.getId());
	                
	                // Check if the lead is present before proceeding
	                if (lead.isPresent()) {
	                    EmployerPaymentModel payment = employerPaymentRepository.findByLeadId(lead.get().getId());

	                    if (payment != null) {
	                        payment.setEmployerId(employer.getId());
	                        employerPaymentRepository.save(payment);

	                        Optional<EmpProformaInvoiceModel> invoiceDetails = proFormaInvoiceRepository.findById(Long.valueOf(payment.getProformaInvoiceId()));

	                        if (invoiceDetails.isPresent()) {
	                            EmpProformaInvoiceModel invoice = invoiceDetails.get();
	                            invoice.setEmployerId(employer.getId());
	                            proFormaInvoiceRepository.save(invoice);

	                            String jobDetails = invoice.getJobDetails();
	                            JSONArray jobs = new JSONArray(jobDetails);
	                            System.out.println(jobs);

	                            PlansModel plan;
	                            for (int i = 0; i < jobs.length(); i++) {
	                                JSONObject job = jobs.getJSONObject(i);
	                                boolean experienced = Boolean.parseBoolean(job.get("isExperienced").toString());

	                                if (experienced) {
	                                    plan = plansRepository.findByActiveAndIsExperienced(true, true);
	                                } else {
	                                    plan = plansRepository.findByActiveAndIsExperienced(true, false);
	                                }

	                                if (plan == null) {
	                                    // Handle the case where no suitable plan is found.
	                                    // You can log an error message or throw an exception as needed.
	                                }
	                                EmpPlacementPlanDetailsModel empPlacementPlanDetails = new EmpPlacementPlanDetailsModel();
	                                empPlacementPlanDetails.setPlanId(plan.getId());
	                                empPlacementPlanDetails.setPaymentId(payment.getId());
	                                empPlacementPlanDetails.setActive(true);
	                                empPlacementPlanDetails.setFromSource("Admin");
	                                empPlacementPlanDetails.setEmployerId(employer.getId());
	                                empPlacementPlanDetails.setNoOfOpenings(Integer.parseInt(job.get("noOfOpenings").toString()));
	                                empPlacementPlanDetails.setIndustry((String) job.get("industry"));
	                                empPlacementPlanDetails.setJobCategory((String) job.get("jobCategory"));
	                                empPlacementPlanDetails.setIsExperienced(job.getBoolean("isExperienced"));
									empPlacementPlanDetails.setJobMinExp((Integer) job.get("jobMinExp"));
									empPlacementPlanDetails.setMinSalary((Integer) job.get("maxSalary"));
									empPlacementPlanDetails.setMaxSalary((Integer) job.get("minSalary"));
									empPlacementPlanDetails.setWorkHours((String) job.get("workHours"));
	                                System.out.println(empPlacementPlanDetails);
	                                empPlacementPlanRepository.save(empPlacementPlanDetails);
	                            }
	                            leadRepository.deleteById(l.getId());
	                        } else {
	                            // invoiceDetails object is not present.
	                            leadRepository.deleteById(l.getId());
	                        }
	                    } else {
	                        // Handle the case where payment is null.
	                        leadRepository.deleteById(l.getId());
	                    }
	                }
	            }
	        }

	        List<LeadModel> leadEmail = leadRepository.findByEmailId(emp.getEmailId());
	        if (leadEmail.size() > 0) {
	            for (LeadModel l : leadEmail) {
	                System.out.println("hjhhjhgk");
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
	private Integer getAdminIdForCity(String city) {
	    Map<String, Integer> cityAdminMapping = new HashMap<>();
	    cityAdminMapping.put("Chennai", 2);
	    cityAdminMapping.put("Chengalpattu", 2);
	    cityAdminMapping.put("Coimbatore", 3);
	    cityAdminMapping.put("Hosur", 3);
	    cityAdminMapping.put("Kanchipuram", 2);
	    
	    return cityAdminMapping.getOrDefault(city, 2); 
	}
	
	

	@PutMapping("/companyContactDetails")
	public ResponseEntity<?> updateContactDetails(@RequestBody EmployerModel emp) {

		EmployerModel existing = employerRepository.findByEmailId(emp.getEmailId());
		if (existing != null) {

			existing.setContactPersonName(emp.getContactPersonName());
			existing.setPhoneCountryCode(emp.getPhoneCountryCode());
			existing.setPhone(Long.valueOf(emp.getPhone()));
			existing.setWhatsappNumber(emp.getWhatsappNumber());
			existing.setWebsiteUrl(emp.getWebsiteUrl());
			existing.setYearFounded(emp.getYearFounded());
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

			EmployerTimeline employerTimeline = new EmployerTimeline();
			Date currentDate1 = new Date();
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
			String formattedDate1 = dateFormat1.format(currentDate1);
			String eventDescription = "Registration on  <b>" + formattedDate1;
			employerTimeline.setEmpId(empId);
			employerTimeline.setEmpLeadId(0);
			employerTimeline.setEventName("Registration");
			employerTimeline.setEventDescription(eventDescription);
			employerTimelineRepository.save(employerTimeline);

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
		sdata.put("Location", existing.getCity() != null ? existing.getCity() : "");
		sdata.put("Contact Person Name", existing.getContactPersonName() != null ? existing.getContactPersonName() : "");
		sdata.put("Company Name", existing.getCompanyName() != null ? existing.getCompanyName() : "");
		sdata.put("Mobile Number", String.valueOf(existing.getMobileNumber()));
		sdata.put("Source", "Web");
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
