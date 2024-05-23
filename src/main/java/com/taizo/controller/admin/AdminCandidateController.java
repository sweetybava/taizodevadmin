package com.taizo.controller.admin;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Expression;
import javax.transaction.Transactional;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.exceptions.IOException;
import com.taizo.controller.employer.EmpWebHookController;
import com.taizo.controller.gallabox.ExotelCallController;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.AdminService;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.service.CandidateAnalyticsService;
import com.taizo.service.CandidateService;
import com.taizo.service.UserService;
import com.taizo.service.WAAlertService;
import com.taizo.utils.FreeMarkerUtils;

import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/admin")

public class AdminCandidateController {
	
	private AmazonSimpleEmailService sesClient = null;

    public void AmazonSESMailUtil(AmazonSimpleEmailService sesClient) {
        this.sesClient = sesClient;
    }
	
	   
	@Autowired
	CandidateService candidateService;
	
	@Autowired
	CfgEducationalRepository cfgEducationalRepository;
	
	@Autowired
	CalendlyMettingRepository calendlyMettingRepository;
	
	@Autowired
	AmazonSESMailUtil amazonSESMailUtil;
	
	@Autowired
	CandidateQualifiedRepository candidateQualifiedRepository;
	
	@Autowired
	MidSeniorCandidateReportRepository midSeniorCandidateReportRepository;
	
	@Autowired
	MidSeniorLevelCandidateLeadRepository midSeniorLevelCandidateLeadRepository;
	
	@Autowired
	CanDocumentsRepository canDocumentsRepository;
	
	@Autowired
	CfgCanDocumentsRepository cfgCanDocumentsRepository;
	
	@Autowired
	CfgCanTimelineEventsRepository cfgCanTimelineEventsRepository;
	
	@Autowired
	CanLanguagesRepository canLanguagesRepository;
	
	@Autowired
	CandidateRepository candidateRepository;
	
	@Autowired
	CanInterviewRepository canInterviewRepository;
	
	@Autowired
	UserRepository userRepository;

	@Autowired
	CandidateTimeLineRepository candidateTimeLineRepository;

	@Autowired
	AdminRepository adminRepository;
	
	@Autowired
	private FreeMarkerUtils freeMarkerUtils;

	@Autowired
	CanLeadRepository canLeadRepository;
	
	@Autowired
	AdminService adminService;

	@Autowired
	private UserService userService;

	@Autowired
	AdminAnalyticsRepository adminAnalyticsRepository;

	@Autowired
	WAAlertService waAlertService;

	@Autowired
	ExotelCallController exotelCallController;

	@Autowired
	FacebookMetaLeadRepository facebookMetaLeadRepository;
	
	@Autowired
	EmpWebHookController empWebHookController;
	
	@Autowired
	CfgCanAdminAreaRepository cfgCanAdminAreaRepository;
	
	@Autowired
	CfgCanAdminCityGroupingRepository cfgCanAdminCityGroupingRepository;
	
	@Autowired
	CandidateAnalyticsRepositroy candidateAnalyticsRepositroy;
	

	@Autowired
	CandidateAnalyticsService candidateAnalyticsService;
	
	@Value("${gallabox.auth.apiKey}")
	private String apiKey;
	@Value("${gallabox.auth.apiSecret}")
	private String apiSecret;
	@Value("${aws.user.resume.endpointUrl}")
	private String resumefolder;
	
	@Value("${aws.s3.bucket.user.resumes.folder}")
	private String folder;
	
	@Value("${aws.access.key.id}")
	private String accessKey;
	@Value("${aws.access.key.secret}")
	private String awssecretKey;
	@Value("${aws.s3.audio.bucket}")
	private String bucketName;
	
	private AmazonS3 s3client;
	
	 @PersistenceContext
	 private EntityManager entityManager;

	
//	@GetMapping(value = "/candidate/{id}")
//	public CandidateModel get(@PathVariable("id") int id) throws ResourceNotFoundException {
//		return candidateService.get(id);
//	}
//	
	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.awssecretKey);
		this.s3client = new AmazonS3Client(credentials);
	}
	
	public class CandidateFilterResponse {
	    private long totalCount;
	    private List<Map<String, Object>> candidateList;

	    public CandidateFilterResponse(long totalCount, List<Map<String, Object>> candidateList) {
	        this.totalCount = totalCount;
	        this.candidateList = candidateList;
	    }

	    public long getTotalCount() {
	        return totalCount;
	    }

	    public List<Map<String, Object>> getCandidateList() {
	        return candidateList;
	    }
	}
	
	@PostMapping("/filterByCandidateDetails")
	public ResponseEntity<CandidateFilterResponse> filterCandidates(
	        @RequestBody CandidateModel candidateModel
	) {
	    List<Map<String, Object>> filteredCandidates = candidateService.filterCandidate(
	            candidateModel.getGender(), candidateModel.getIndustry(),
	            candidateModel.getJobCategory(), candidateModel.getQualification(),
	            candidateModel.getCandidateType(), candidateModel.getKeySkill(),
	            candidateModel.getPrefLocation(), candidateModel.getPassed_out_year(),
	            candidateModel.getExperience(), candidateModel.getMaxExperience(),
	            candidateModel.getPages(), candidateModel.getSize(),
	            candidateModel.getCreatedTime(), candidateModel.getEndDate()
	    );
	    long totalCount = candidateService.filterCandidateCount(
	            candidateModel.getGender(), candidateModel.getIndustry(),
	            candidateModel.getJobCategory(), candidateModel.getQualification(),
	            candidateModel.getCandidateType(), candidateModel.getKeySkill(),
	            candidateModel.getPrefLocation(), candidateModel.getPassed_out_year(),
	            candidateModel.getExperience(), candidateModel.getMaxExperience(),
	            candidateModel.getCreatedTime(), candidateModel.getEndDate()
	    );

	    CandidateFilterResponse response = new CandidateFilterResponse(totalCount, filteredCandidates);

	    return ResponseEntity.ok(response);
	}
	
		

	public class MetaLeadsFilterResponse{
		 private long totalCount;
		    private List<Map<String, Object>> MetaLeadsList;
		    
		    public MetaLeadsFilterResponse(long totalCount, List<Map<String, Object>> MetaLeadsList) {
		        this.totalCount = totalCount;
		        this.MetaLeadsList = MetaLeadsList;
		    }
		    public long getTotalCount() {
		        return totalCount;
		    }

		    public List<Map<String, Object>> getMetaLeadsList() {
		        return MetaLeadsList;
		    }
	}
	
	@PostMapping("/filterByMetaLeads")
	public ResponseEntity<MetaLeadsFilterResponse> filterMetaLeads(
	        @RequestBody FacebookMetaLead facebookMetaLead
	) {
		List<Map<String, Object>> filteredMetaData =candidateService.filterMetaDatas(
				facebookMetaLead.getId(),facebookMetaLead.getAssignTo(),facebookMetaLead.getCandidateName(),facebookMetaLead.getEducationQualification(),
				facebookMetaLead.getJobCategory(),facebookMetaLead.getMobileNumber(),facebookMetaLead.isQualified(),facebookMetaLead.isNotQualified(),facebookMetaLead.isNotAttend(),facebookMetaLead.isNoStatus(),
				facebookMetaLead.getExperience(),facebookMetaLead.getPreferredLocation(),facebookMetaLead.getJoiningAvailability(),
				facebookMetaLead.getPages(),facebookMetaLead.getSize(),facebookMetaLead.getCreatedTime(),facebookMetaLead.getEndDate()

				);
		 long totalCount = candidateService.filterMetaLeadCount(
				 facebookMetaLead.getId(),facebookMetaLead.getAssignTo(),facebookMetaLead.getCandidateName(),facebookMetaLead.getEducationQualification(),
					facebookMetaLead.getJobCategory(),facebookMetaLead.getMobileNumber(),facebookMetaLead.isQualified(),facebookMetaLead.isNotQualified(),facebookMetaLead.isNotAttend(),facebookMetaLead.isNoStatus(),
					facebookMetaLead.getExperience(),facebookMetaLead.getPreferredLocation(),facebookMetaLead.getJoiningAvailability(),
					facebookMetaLead.getCreatedTime(),facebookMetaLead.getEndDate()
					);
		 MetaLeadsFilterResponse response = new MetaLeadsFilterResponse(totalCount, filteredMetaData);

		    return ResponseEntity.ok(response);
		
	}
	
	@GetMapping("candidates/{idOrNumber}")
	public ResponseEntity<?> getCandidateDetailsByIdOrNumber(@PathVariable("idOrNumber") String idOrNumber) {
	    try {
	        // Try parsing the input as an integer (ID)
	        int id = Integer.parseInt(idOrNumber);
	        CandidateModel candidate = candidateService.getCandidateDetailsById(id);
	        
	        if (candidate != null) {
	            return ResponseEntity.ok(candidate);
	        } else {
	            // Custom message for ID not found
	            Map<String, Object> response = new HashMap<>();
	            response.put("statusCode", 404);
	            response.put("message", "Candidate with ID " + id + " not found.");
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	        }
	    } catch (NumberFormatException e) {
	        // If parsing as an integer fails, try parsing as a long (number)
	        try {
	            long number = Long.parseLong(idOrNumber);
	            CandidateModel candidate = candidateService.getCandidateDetailsByNumber(number);
	            
	            if (candidate != null) {
	                return ResponseEntity.ok(candidate);
	            } else {
	                // Custom message for mobile number not found
	                Map<String, Object> response = new HashMap<>();
	                response.put("statusCode", 404);
	                response.put("message", "Candidate with mobile number " + number + " not found.");
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	            }
	        } catch (NumberFormatException ex) {
	            // If both parsing attempts fail, return a 400 Bad Request response
	            return ResponseEntity.badRequest().build();
	        }
	    }
	}


	
	@PostMapping("/candidateRegister")
	public ResponseEntity<?> candidateRegister(@RequestBody CandidateModel candidateModel,
	                                           @RequestParam Long adminId) {
	    try {
	        if (adminId == null) {
	            return ResponseEntity.badRequest().body("adminId is required");
	        }

	        CandidateModel existingUser = candidateRepository.findByMobileNumber(candidateModel.getMobileNumber());

	        if (existingUser != null) {
	            return ResponseEntity.badRequest().body("Mobile Number already exists");
	        }
	        Optional<Admin> adminOptional = adminRepository.findById(adminId);

	        Admin admin = adminOptional.get();

	        // Create a new user and save it to the database
	        UserModel user = new UserModel();
	        user.setFirstName(candidateModel.getFirstName());
	        user.setMobileNumber(candidateModel.getMobileNumber());
	        user.setCountryCode("91");
	        String token = UUID.randomUUID().toString();
	        user.setToken(token);
	        userRepository.save(user);

	        // Create a new candidate and set properties
	        CandidateModel newUser = new CandidateModel();
	        newUser.setUserId(user.getId());
	        newUser.setFirstName(candidateModel.getFirstName());
	        newUser.setDateOfBirth(candidateModel.getDateOfBirth());
	        newUser.setMobileNumber(candidateModel.getMobileNumber());
	        newUser.setContactNumber(candidateModel.getContactNumber());
	        newUser.setWhatsappNumber(candidateModel.getMobileNumber());
	        newUser.setGender(candidateModel.getGender());
	        newUser.setCurrentCountry("India");
	        newUser.setState(candidateModel.getState());
	        newUser.setCity(candidateModel.getPrefLocation());
	        newUser.setCurrentCity(candidateModel.getCity());
	        newUser.setAssignTo(adminId.intValue());
	        newUser.setAge(candidateModel.getAge());
	        newUser.setFromWA(false);
	        newUser.setFromApp(false);
	        newUser.setReason_for_unemployment(candidateModel.getReason_for_unemployment());
	        newUser.setReason_for_jobchange(candidateModel.getReason_for_jobchange());
	        newUser.setImmediateJoiner(candidateModel.isImmediateJoiner());
	        newUser.setQualification(candidateModel.getQualification());
	        newUser.setSpecification(candidateModel.getSpecification());
	        newUser.setStudent(candidateModel.getStudent());
	        newUser.setPassed_out_year(candidateModel.getPassed_out_year());
	        newUser.setIsHavingArrear(candidateModel.getIsHavingArrear());
	        newUser.setExpInManufacturing(candidateModel.isExpInManufacturing());
	        newUser.setExperienced(candidateModel.isExperienced());
	        newUser.setIndustry(candidateModel.getIndustry());
	        newUser.setJobCategory(candidateModel.getJobCategory());
	        newUser.setExperience(candidateModel.getExperience());
	        newUser.setExpMonths(candidateModel.getExpMonths());
	        newUser.setPrefLocation(candidateModel.getPrefLocation());
	        newUser.setPfEsiAccount(candidateModel.getPfEsiAccount());
	        newUser.setKnownLanguages(candidateModel.getKnownLanguages());
	        newUser.setCertificationCourses(candidateModel.getCertificationCourses());
	        newUser.setCertificationSpecialization(candidateModel.getCertificationSpecialization());
	        newUser.setKeySkill(candidateModel.getKeySkill());
	        newUser.setReference(candidateModel.getReference());
	        newUser.setFromAdmin(true);
	        newUser.setRegistered(true);
	        newUser.setProfileFilled(true);
	        newUser.setPrefCountry("India");
	        newUser.setLanguageKey("en");
	        newUser.setCertificationCourses("Certification Courses");
	        newUser.setJobType("Full Time (8hrs to 10hrs)");
	        newUser.setCandidateLocation("Domestic");
	        newUser.setWACampaign(true);
	        newUser.setAssignTo(adminId.intValue());

	        // Check experience and adjust properties accordingly
	        if (newUser.getExperience() > 0 || newUser.getExpMonths() > 0) {
	            newUser.setCandidateType("Experienced");
	            newUser.setExpInManufacturing(true);
	        } else {
	            newUser.setExperience(0);
	            newUser.setExpMonths(0);
	            newUser.setCandidateType("Fresher");
	        }

	        // Save the candidate to the database
	        candidateRepository.save(newUser);

	        // Create a candidate timeline entry
	        CandidateTimeLine candidateTimeLine = new CandidateTimeLine();
	        Date currentDate = new Date();
	        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
	        String formattedDate = dateFormat.format(currentDate);
	        String eventDescription = "Registered On <b> " + formattedDate + "</b> By <b>" + admin.getUserName() + "</b>";
	        candidateTimeLine.setCanId(newUser.getId());
	        candidateTimeLine.setEventName("Registration");
	        candidateTimeLine.setCanLeadId(0);
	        candidateTimeLine.setFacebookId(0l);
	        candidateTimeLine.setEventDescription(eventDescription);
	        candidateTimeLineRepository.save(candidateTimeLine);

	        Map<String, Object> response = new HashMap<>();
	        response.put("statusCode", 200);
	        response.put("message", "Success");
	        response.put("data", newUser);
	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	    
	        return ResponseEntity.status(500).body("Internal Server Error");
	    }
	}



	@PutMapping(path = "/updateCandidateRegister")
	public ResponseEntity<?> updateCandidateRegister(@RequestParam("canId") int candidateId,
			                                         @RequestBody CandidateModel candidateModel,
	                                                 @RequestParam Long adminId) {
		// Check if the candidate with the given ID exists
		CandidateModel existingUser = candidateRepository.findById(candidateId).orElse(null);

		Admin a = adminRepository.findById(adminId).get();

		if (existingUser != null) {
			// Update the existing candidate with the data from the request body
			existingUser.setFirstName(candidateModel.getFirstName());
			existingUser.setLastName(candidateModel.getLastName());
			existingUser.setDateOfBirth(candidateModel.getDateOfBirth());
			existingUser.setContactNumber(candidateModel.getContactNumber());
			existingUser.setMobileNumber(candidateModel.getMobileNumber());
			existingUser.setGender(candidateModel.getGender());
			existingUser.setCurrentCountry("India");
			existingUser.setState(candidateModel.getState());
			existingUser.setCity(candidateModel.getCity());
			existingUser.setAge(candidateModel.getAge());
			existingUser.setFromWA(false);
			existingUser.setFromApp(false);
			existingUser.setAssignTo(existingUser.getAssignTo());
			existingUser.setReason_for_unemployment(candidateModel.getReason_for_unemployment());
			existingUser.setReason_for_jobchange(candidateModel.getReason_for_jobchange());
			existingUser.setImmediateJoiner(candidateModel.isImmediateJoiner());
			existingUser.setWhatsappNumber(candidateModel.getWhatsappNumber());
			existingUser.setQualification(candidateModel.getQualification());
			existingUser.setSpecification(candidateModel.getSpecification());
			existingUser.setStudent(candidateModel.getStudent());
			existingUser.setPassed_out_year(candidateModel.getPassed_out_year());
			existingUser.setIsHavingArrear(candidateModel.getIsHavingArrear());
			existingUser.setExpInManufacturing(candidateModel.isExpInManufacturing());
			existingUser.setExperienced(candidateModel.isExperienced());
			existingUser.setIndustry(candidateModel.getIndustry());
			existingUser.setJobCategory(candidateModel.getJobCategory());
			existingUser.setExperience(candidateModel.getExperience());
			existingUser.setExpMonths(candidateModel.getExpMonths());
			existingUser.setPrefLocation(candidateModel.getPrefLocation());
			existingUser.setPfEsiAccount(candidateModel.getPfEsiAccount());
			existingUser.setKnownLanguages(candidateModel.getKnownLanguages());
			existingUser.setCertificationCourses(candidateModel.getCertificationCourses());
			existingUser.setCertificationSpecialization(candidateModel.getCertificationSpecialization());
			existingUser.setKeySkill(candidateModel.getKeySkill());
			existingUser.setReference(candidateModel.getReference());
			existingUser.setFromAdmin(true);
			existingUser.setRegistered(true);
			existingUser.setProfileFilled(true);
			existingUser.setPrefCountry("India");
			existingUser.setLanguageKey("en");
			existingUser.setCertificationCourses("Certification Courses");
			existingUser.setJobType("Full Time (8hrs to 10hrs)");
			existingUser.setCandidateLocation("Domestic");
			existingUser.setWACampaign(true);

			if (existingUser.getExperience() > 0 || existingUser.getExpMonths() > 0) {
				existingUser.setExperience(existingUser.getExperience());
				existingUser.setExpMonths(existingUser.getExpMonths());
				existingUser.setCandidateType("Experienced");
				existingUser.setExpInManufacturing(true);
				existingUser.setIndustry(existingUser.getIndustry());
				existingUser.setJobCategory(existingUser.getJobCategory());
			} else {
				existingUser.setExperience(0);
				existingUser.setExpMonths(0);
				existingUser.setCandidateType("Fresher");

				if (existingUser.getJobCategory() != null) {
					switch (existingUser.getJobCategory().toLowerCase()) {
					case "trainee":
					case "assembler":
					case "graduate trainee":
						existingUser.setExpInManufacturing(false);
						break;
					default:
						break;
					}
				}
			}
//			String languages = existingUser.getKnownLanguages();
//			if (languages != null) {
//				List<Integer> x = Arrays.stream(languages.split(",")).map(Integer::parseInt)
//						.collect(Collectors.toList());
//
//				for (int f : x) {
//					CanLanguageModel f1 = new CanLanguageModel();
//					f1.setLanguageId(f);
//					existingUser.getLanguages().add(f1);
//					f1.setCandidate(existingUser);
//					candidateRepository.save(existingUser);
//				}
//			}

			// Save the updated candidate to the database
			candidateRepository.save(existingUser);

			CandidateTimeLine candidateTimeLine = new CandidateTimeLine();

			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
			String formattedDate = dateFormat.format(currentDate);
			String eventDescription = "Profile Updated By " + a.getUserName() + " on " + formattedDate;
			candidateTimeLine.setCanId(candidateId);
			candidateTimeLine.setEventName("Profile updated");
			candidateTimeLine.setEventDescription(eventDescription);
			candidateTimeLineRepository.save(candidateTimeLine);


			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 200);
			response.put("message", "Candidate updated successfully");
			return ResponseEntity.ok(response);
		} else {
			// Candidate with the given ID does not exist
			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 404);
			response.put("message", "Candidate not found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}
	@PostMapping("/canTimeLine")
	public Map<String, Object> createCandidateTimeLine(
	        @RequestParam("canId") int canId,
	        @RequestParam("adminId") Long adminId,
	        @RequestParam("eventName") String eventName,
	        @RequestParam(value = "notes", required = false) String notes) {

	    Map<String, Object> response = new HashMap<>();

	    CandidateModel can = candidateRepository.findById(canId).orElse(null);
	    Admin a = adminRepository.findById(adminId).orElse(null);

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String formattedDate = dateFormat.format(currentDate);

	    if (can == null || a == null || eventName.isEmpty()) {
	        response.put("status", 400);
	        response.put("message", "Bad Request: Missing or invalid parameters");
	    } else {
	        CandidateTimeLine candidateTimeLine = new CandidateTimeLine();
	        candidateTimeLine.setCanId(canId);
	        candidateTimeLine.setEventName(eventName);

	        if ("CSS Intro call".equals(eventName)) {
				candidateTimeLine.setEventDescription("Intro call by <b>" + a.getUserName() + "</b> on " + formattedDate);
			} else if ("Follow up call".equals(eventName)) {
				candidateTimeLine.setEventDescription("Follow up call by"+"<b>" + a.getUserName() + "</b> on"+formattedDate);

				List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(Long.valueOf(adminId));

				if (!adminAnalyticsList.isEmpty()) {
					// Check if the createdOn date is the same as the current date
					LocalDate currentDate1 = LocalDate.now();
					boolean dateMatch = false;

					for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
						LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
						if (currentDate1.isEqual(createdOnDate)) {
							dateMatch = true;
							adminAnalytics.setCanFollowUpCalls(
									adminAnalytics.getCanFollowUpCalls() != null
											? adminAnalytics.getCanFollowUpCalls() + 1
											: 1
							);
						}
					}
					if (!dateMatch) {

						// If the dates are different for all records, insert a new record
						AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
						newAdminAnalytics.setAdminId(Long.valueOf(adminId));
						newAdminAnalytics.setModule(a.getModule());
						newAdminAnalytics.setCanFollowUpCalls(1);
						adminAnalyticsList.add(newAdminAnalytics);
					}

					adminAnalyticsRepository.saveAll(adminAnalyticsList);
				} else {

					// If there are no existing records for the adminId, insert a new record
					AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
					adminAnalytics.setAdminId(Long.valueOf(adminId));
					adminAnalytics.setModule(a.getModule());
					adminAnalytics.setCanFollowUpCalls(1);
					adminAnalyticsRepository.save(adminAnalytics);
				}
	        } else if ("Not qualified".equals(eventName)) {
				candidateTimeLine.setEventDescription("Not qualified by"+"<b>" + a.getUserName() + "</b>"+formattedDate);
	        }

	        if (notes != null && !notes.isEmpty()) {
	            candidateTimeLine.setNotes(notes);
	        }

	        candidateTimeLineRepository.save(candidateTimeLine);

	        response.put("status", 200);
	        response.put("message", "Success");
	        response.put("data", candidateTimeLine);
	    }

	    return response;
	}

	@PostMapping(path = "/canLead")
	public ResponseEntity<?> basicDetails(@RequestBody CanLeadModel canLeadModel,
	                                      @RequestParam Long adminId) {

	    try {
	        // Retrieve existing user and candidate
	    	String mobileNumberStr = String.valueOf(canLeadModel.getMobileNumber());
	        FacebookMetaLead fb = facebookMetaLeadRepository.findByMobileNumber(mobileNumberStr);
	    	//FacebookMetaLead fb=facebookMetaLeadRepository.findByMobileNumber(canLeadModel.getMobileNumber());
	        CanLeadModel existingUser = canLeadRepository.findByMobileNumber(canLeadModel.getMobileNumber());
	        CandidateModel candidate = candidateRepository.findByMobileNumber(canLeadModel.getMobileNumber());

	        // Retrieve admin by adminId
	        Admin admin = adminRepository.findById(adminId).orElse(null);

	        // Validate admin existence
	        if (admin == null) {
	            Map<String, Object> response = new HashMap<>();
	            response.put("statusCode", 400);
	            response.put("message", "Invalid adminId");
	            return ResponseEntity.badRequest().body(response);
	        }

	        LocalDate currentDate = LocalDate.now();

	        // Check if the candidate is already registered
	        if (candidate != null) {
	            Map<String, Object> response = new HashMap<>();
	            response.put("statusCode", 400);
	            response.put("message", "Already Registered");
	            return ResponseEntity.ok(response);
	        }
	        else if(fb!=null) {
	        	 Map<String, Object> response = new HashMap<>();
		            response.put("statusCode", 400);
		            response.put("message", "Already Registered");
		            return ResponseEntity.ok(response);
	        }

	        CanLeadModel userToUpdate = existingUser;

	        // If the user does not exist, create a new user
	        if (existingUser == null) {
	            userToUpdate = new CanLeadModel();
	        }
	        // Update user details
	        userToUpdate.setMobileNumber(canLeadModel.getMobileNumber());
	        userToUpdate.setName(canLeadModel.getName());
	        userToUpdate.setLastName(canLeadModel.getLastName());
	        userToUpdate.setGender(canLeadModel.getGender());
	        userToUpdate.setCountry("India");
	        userToUpdate.setExperienced(canLeadModel.isExperienced());
	        userToUpdate.setState(canLeadModel.getState());
	        userToUpdate.setPrefArea(canLeadModel.getPrefArea());
	        userToUpdate.setDateOfBirth(canLeadModel.getDateOfBirth());
	        userToUpdate.setExpMonths(canLeadModel.getExpMonths());
	        userToUpdate.setExpYears(canLeadModel.getExpYears());
	        userToUpdate.setWhatsappNumber(canLeadModel.getWhatsappNumber());
	        userToUpdate.setFromAdmin(true);
	        userToUpdate.setFromWA(false);
	        userToUpdate.setFromApp(false);
	        userToUpdate.setAssignTo(adminId.intValue());

	        userToUpdate.setProfilePageNo(0);
	        canLeadRepository.save(userToUpdate);

	        // Create a timeline entry
	        createTimelineEntry(admin, userToUpdate, currentDate);

	        //Canlead Count
	        candidateAnalyticsService.CanLeadcount(adminId,LocalDate.now());
	        //total lead Count
	        candidateAnalyticsService.TotalLeadscount(adminId,LocalDate.now());
	    

	        // Response
	        Map<String, Object> response = new HashMap<>();
	        response.put("statusCode", 200);
	        response.put("message", existingUser != null ? "Updated Successfully" : "Successfully");
	        return ResponseEntity.ok(response);

	    } catch (Exception e) {
	        // Handle exceptions
	        Map<String, Object> response = new HashMap<>();
	        response.put("statusCode", 500);
	        response.put("message", "Internal Server Error");
	        return ResponseEntity.status(500).body(response);
	    }
	}

	// Common method to create a timeline entry
	private void createTimelineEntry(Admin admin, CanLeadModel user, LocalDate currentDate) {
	    CandidateTimeLine candidateTimeLine = new CandidateTimeLine();
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
	    String formattedDate = currentDate.format(formatter);
	    String eventDescription = "Lead Generation by " + admin.getUserName() + " On " + formattedDate;

	    candidateTimeLine.setCanId(0);
	    candidateTimeLine.setFacebookId(0L);
	    candidateTimeLine.setCanLeadId(user.getId());
	    candidateTimeLine.setEventName("Lead generation");
	    candidateTimeLine.setEventDescription(eventDescription);

	    candidateTimeLineRepository.save(candidateTimeLine);
	}

	// Common method to update admin analytics
	private void updateAdminAnalytics(Long adminId, Admin admin, LocalDate currentDate) {
	    List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(adminId);

	    if (!adminAnalyticsList.isEmpty()) {
	        boolean dateMatch = false;

	        for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
	            LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
	            if (currentDate.isEqual(createdOnDate)) {
	                dateMatch = true;
	                adminAnalytics.setCanNewLeadCount(
	                        adminAnalytics.getCanNewLeadCount() != null
	                                ? adminAnalytics.getCanNewLeadCount() + 1
	                                : 1
	                );
	            }
	        }

	        if (!dateMatch) {
	            // If no matching date found, insert a new record for the current date
	            AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
	            newAdminAnalytics.setAdminId(adminId);
	            newAdminAnalytics.setModule(admin.getModule());
	            newAdminAnalytics.setCanNewLeadCount(1);
	            adminAnalyticsRepository.save(newAdminAnalytics);
	        } else {
	            // If matching date found, update the existing records
	            adminAnalyticsRepository.saveAll(adminAnalyticsList);
	        }
	    } else {
	        // If there are no existing records for the adminId, insert a new record for the current date
	        AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
	        newAdminAnalytics.setAdminId(adminId);
	        newAdminAnalytics.setModule(admin.getModule());
	        newAdminAnalytics.setCanNewLeadCount(1);
	        adminAnalyticsRepository.save(newAdminAnalytics);
	    }
	}
	
	private void setAssignToBasedOnPrefArea(CanLeadModel canLeadModel) {
	    String prefArea = canLeadModel.getPrefArea();
	    
	    // Assuming there is a repository method to find CfgCanAdminArea by prefArea
	    CfgCanAdminArea cfgCanAdminArea = (prefArea != null) ? cfgCanAdminAreaRepository.findByAreas(prefArea) : null;

	    if (cfgCanAdminArea != null) {
	        int adminId = findAdminIdByAssignedToAdminId(cfgCanAdminArea.getAssingnedToAdminId());
	        canLeadModel.setAssignTo(adminId);
	    } else {
	        // Default admin ID if no match is found
	        canLeadModel.setAssignTo(1);
	    }
	}

	private int findAdminIdByAssignedToAdminId(int assignedToAdminId) {
	    List<CfgCanAdminArea> adminJobRolesMapping = cfgCanAdminAreaRepository.findByAssingnedToAdminId(assignedToAdminId);
	    if (adminJobRolesMapping != null && !adminJobRolesMapping.isEmpty()) {
	        // Assuming you want to get the admin ID from the mapping
	        return adminJobRolesMapping.get(0).getAssingnedToAdminId(); 
	    } else {
	        return 0; // Default value if no match is found
	    }
	}

	
	@GetMapping("/numberCheck")
	public ResponseEntity<Map<String, Object>> checkNumberExists(@RequestParam long number) {
	    Map<String, Object> response = new HashMap<>();

	    boolean exists = adminService.numberExistsInModels(number);

	    if (exists) {
	        response.put("statusCode", 200);
	        response.put("message", "Number exists in CanLeadModel or CandidateModel");
	        return ResponseEntity.ok(response);
	    } else {
	        response.put("statusCode", 400);
	        response.put("message", "Number does not exist in CanLeadModel or CandidateModel");
	        return ResponseEntity.badRequest().body(response);
	    }
	}

	 @GetMapping("/candidateTimeline")
	    public ResponseEntity<Page<CandidateTimeLine>> getCandidateTimelineList(
	            @RequestParam(required = false) String eventName,
	            @RequestParam(required = false) Integer canId,
				@RequestParam(required = false)Integer canLeadId,
				@RequestParam(required = false)Long facebookId,
				@RequestParam(required = false)Long midSeniorCanId,
				@RequestParam(required = false)Long midSeniorSorcingId,
	            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
	            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
	            @RequestParam(defaultValue = "20") int size,
	            @RequestParam(defaultValue = "0") int page) {

	        try {
	        	Page<CandidateTimeLine> resultPage = adminService.findByFilters(eventName, canId, canLeadId,
                        facebookId, midSeniorCanId, midSeniorSorcingId,
                        startDate, endDate, page, size);
	            return ResponseEntity.ok(resultPage);
	        } catch (Exception e) {
	            return ResponseEntity.badRequest().build();
	        }
	    }

	@PutMapping("/canLeadStatus")
	public ResponseEntity<Map<String, Object>> updateStatus(@RequestParam int canLeadId,
															@RequestParam Long adminId,
															@RequestParam boolean qualified,
															@RequestParam boolean notQualified,
															@RequestParam (required = false)String notes) {

		Optional<CanLeadModel> optionalCanLeadModel = canLeadRepository.findById(canLeadId);
		CandidateTimeLine candidateTimeLine=new CandidateTimeLine();
		Date currentDates = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String formattedDate = dateFormat.format(currentDates);


		Map<String, Object> response = new HashMap<>();

		if (optionalCanLeadModel.isPresent()) {
			CanLeadModel canLeadModel = optionalCanLeadModel.get();
			Admin a = adminRepository.findById(adminId).orElse(null);

			if (qualified) {
				canLeadModel.setQualified(true);
				canLeadModel.setNotQualified(false);
				canLeadRepository.save(canLeadModel);
				
				candidateTimeLine.setCanLeadId(canLeadModel.getId());
				candidateTimeLine.setEventName("Qualified");
				candidateTimeLine.setEventDescription("Qualified By" +" "+a.getUserName() + "</b> on " + formattedDate);
				candidateTimeLine.setCanId(0);
				candidateTimeLine.setFacebookId(0L);
				if(notes != null) {
				candidateTimeLine.setNotes(notes);
				}
				candidateTimeLineRepository.save(candidateTimeLine);

				if (a != null) {
					long mobileNumber = canLeadModel.getMobileNumber();
					String wa = String.valueOf(mobileNumber);

					empWebHookController.sendMetaAlert(wa);
				}
				//Qualified CanLead Counts
				candidateAnalyticsService.QualifiedCanLeadcount(adminId,LocalDate.now());
				//Total Qualified Counts
				candidateAnalyticsService.TotalQualifiedLeadsCounts(adminId,LocalDate.now());

			} else if (notQualified) {
				canLeadModel.setQualified(false);
				canLeadModel.setNotQualified(true);
				canLeadRepository.save(canLeadModel);
				candidateTimeLine.setCanLeadId(canLeadModel.getId());
				candidateTimeLine.setEventName("NotQualified");
				candidateTimeLine.setEventDescription("NotQualified By " +a.getUserName() + "</b> on " + formattedDate);
				candidateTimeLine.setCanId(0);
				candidateTimeLine.setFacebookId(0L);
				candidateTimeLine.setNotes(notes);
				candidateTimeLineRepository.save(candidateTimeLine);


				List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(Long.valueOf(adminId));

				if (!adminAnalyticsList.isEmpty()) {
					// Check if the createdOn date is the same as the current date
					LocalDate currentDate = LocalDate.now();
					boolean dateMatch = false;

					for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
						LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
						if (currentDate.isEqual(createdOnDate)) {
							dateMatch = true;
							adminAnalytics.setCanLeadNotQualifiedCount(
									adminAnalytics.getCanLeadNotQualifiedCount() != null
											? adminAnalytics.getCanLeadNotQualifiedCount() + 1
											: 1
							);
						}
					}
					if (!dateMatch) {

						// If the dates are different for all records, insert a new record
						AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
						newAdminAnalytics.setAdminId(Long.valueOf(adminId));
						newAdminAnalytics.setModule(a.getModule());
						newAdminAnalytics.setCanLeadNotQualifiedCount(1);
						adminAnalyticsList.add(newAdminAnalytics);
					}

					adminAnalyticsRepository.saveAll(adminAnalyticsList);
				} else {

					// If there are no existing records for the adminId, insert a new record
					AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
					adminAnalytics.setAdminId(Long.valueOf(adminId));
					adminAnalytics.setModule(a.getModule());
					adminAnalytics.setCanLeadNotQualifiedCount(1);
					adminAnalyticsRepository.save(adminAnalytics);
				}
			}

			response.put("statusCode", 200);
			response.put("message", "Status updated successfully");
			return ResponseEntity.ok(response);
		} else {
			response.put("statusCode", 400);
			response.put("message", "CanLead not found with ID: ");
			return ResponseEntity.badRequest().body(response);
		}
	}

	@PutMapping("/canStatus")
	public ResponseEntity<Map<String, Object>> candidateUpdateStatus(@RequestParam int canId,
															@RequestParam Long adminId,
															@RequestParam boolean qualified,
															@RequestParam boolean notQualified,
															@RequestParam(required=false) String notes) {

		Optional<CandidateModel> optionalcandidateModel = candidateRepository.findById(canId);
		CandidateTimeLine candidateTimeLine=new CandidateTimeLine();
		Date currentDates = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String formattedDate = dateFormat.format(currentDates);

		Map<String, Object> response = new HashMap<>();

		if (optionalcandidateModel.isPresent()) {
			CandidateModel candidateModel = optionalcandidateModel.get();
			Admin a = adminRepository.findById(adminId).orElse(null);

			if (qualified) {
				candidateModel.setQualified(true);
				candidateModel.setNotQualified(false);
				candidateRepository.save(candidateModel);
				candidateTimeLine.setCanId(candidateModel.getId());
				candidateTimeLine.setEventName("Qualified");
				candidateTimeLine.setEventDescription("Qualified By " +" "+a.getUserName() + "</b> on " + formattedDate);
				candidateTimeLine.setCanLeadId(0);
				candidateTimeLine.setFacebookId(0L);
				if(notes != null) {
				candidateTimeLine.setNotes(notes);
				}
				candidateTimeLineRepository.save(candidateTimeLine);
				
				if (a != null) {
					long mobileNumber = candidateModel.getMobileNumber();
					String wa = String.valueOf(mobileNumber);

					empWebHookController.sendMetaAlert(wa);
				}

				List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(Long.valueOf(adminId));

				if (!adminAnalyticsList.isEmpty()) {
					// Check if the createdOn date is the same as the current date
					LocalDate currentDate = LocalDate.now();
					boolean dateMatch = false;

					for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
						LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
						if (currentDate.isEqual(createdOnDate)) {
							dateMatch = true;
							adminAnalytics.setCanQualifiedCount(
									adminAnalytics.getCanQualifiedCount() != null
											? adminAnalytics.getCanQualifiedCount() + 1
											: 1
							);
						}
					}
					if (!dateMatch) {

						// If the dates are different for all records, insert a new record
						AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
						newAdminAnalytics.setAdminId(Long.valueOf(adminId));
						newAdminAnalytics.setModule(a.getModule());
						newAdminAnalytics.setCanQualifiedCount(1);
						adminAnalyticsList.add(newAdminAnalytics);
					}

					adminAnalyticsRepository.saveAll(adminAnalyticsList);
				} else {

					// If there are no existing records for the adminId, insert a new record
					AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
					adminAnalytics.setAdminId(Long.valueOf(adminId));
					adminAnalytics.setModule(a.getModule());
					adminAnalytics.setCanQualifiedCount(1);
					adminAnalyticsRepository.save(adminAnalytics);
				}
				
			} else if (notQualified) {
				candidateModel.setQualified(false);
				candidateModel.setNotQualified(true);
				candidateRepository.save(candidateModel);
				candidateTimeLine.setCanId(candidateModel.getId());
				candidateTimeLine.setEventName("NotQualified");
				candidateTimeLine.setEventDescription("NotQualified By " +" "+a.getUserName() + "</b> on " + formattedDate);
				candidateTimeLine.setCanLeadId(0);
				candidateTimeLine.setFacebookId(0L);
				if(notes != null) {
				candidateTimeLine.setNotes(notes);
				}
				candidateTimeLineRepository.save(candidateTimeLine);
				
				if (a != null) {
					long mobileNumber = candidateModel.getMobileNumber();
					String wa = String.valueOf(mobileNumber);

					empWebHookController.sendMetaAlert(wa);
				}

				List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(Long.valueOf(adminId));

				if (!adminAnalyticsList.isEmpty()) {
					// Check if the createdOn date is the same as the current date
					LocalDate currentDate = LocalDate.now();
					boolean dateMatch = false;

					for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
						LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
						if (currentDate.isEqual(createdOnDate)) {
							dateMatch = true;
							adminAnalytics.setCanNotQualifiedCount(
									adminAnalytics.getCanNotQualifiedCount() != null
											? adminAnalytics.getCanNotQualifiedCount() + 1
											: 1
							);
						}
					}
					if (!dateMatch) {

						// If the dates are different for all records, insert a new record
						AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
						newAdminAnalytics.setAdminId(Long.valueOf(adminId));
						newAdminAnalytics.setModule(a.getModule());
						newAdminAnalytics.setCanNotQualifiedCount(1);
						adminAnalyticsList.add(newAdminAnalytics);
					}

					adminAnalyticsRepository.saveAll(adminAnalyticsList);
				} else {

					// If there are no existing records for the adminId, insert a new record
					AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
					adminAnalytics.setAdminId(Long.valueOf(adminId));
					adminAnalytics.setModule(a.getModule());
					adminAnalytics.setCanNotQualifiedCount(1);
					adminAnalyticsRepository.save(adminAnalytics);
				}
			}

			response.put("statusCode", 200);
			response.put("message", "Status updated successfully");
			return ResponseEntity.ok(response);
		} else {
			response.put("statusCode", 400);
			response.put("message", "Candidate not found with ID: ");
			return ResponseEntity.badRequest().body(response);
		}
	}

	@PutMapping(path = "/updateBasicDetails")
	public ResponseEntity<?> basicDetails(@RequestParam("mobileNumber") final long mobileNumber,
										  @RequestBody CanLeadModel canLeadModel){

		String name = canLeadModel.getName();
		String lastName=canLeadModel.getLastName();
		String dob = canLeadModel.getDateOfBirth();
		int age = canLeadModel.getAge();
		String gender = canLeadModel.getGender();
		String state = canLeadModel.getState();
		String city = canLeadModel.getCity();
		String conNumber = canLeadModel.getContactNumber();
		long whatsappNumber=canLeadModel.getWhatsappNumber();

		
		String mobileNumberString = String.valueOf(mobileNumber);
		FacebookMetaLead fb = facebookMetaLeadRepository.findByMobileNumber(mobileNumberString);
		boolean active = fb != null && fb.isInActive();
		CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mobileNumber);
		CandidateModel candidate = candidateRepository.findByMobileNumber(mobileNumber);

		if(candidate != null)
		{
			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 400);
			response.put("message", "Already Registered");
			return ResponseEntity.ok(response);
		}
		if (fb != null && !active) {
			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 400);
			response.put("message", "Already Registered in Fb Meta Lead");
			return ResponseEntity.ok(response);
		}

		if (existingUser != null) {
			existingUser.setName(name);
			existingUser.setLastName(lastName);
			existingUser.setDateOfBirth(dob);
			existingUser.setGender(gender);
			existingUser.setCountry("India");
			existingUser.setState(state);
			existingUser.setCity(city);
			existingUser.setAge(age);
			existingUser.setFromWA(true);
			existingUser.setFromApp(false);
			existingUser.setWhatsappNumber(whatsappNumber);
			existingUser.setContactNumber(conNumber);
			existingUser.setAssignTo(0);

			existingUser.setProfilePageNo(1);
			canLeadRepository.save(existingUser);

			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 200);
			response.put("message", "Updated Successfully");
			return ResponseEntity.ok(response);
		} else {
			// User is not registered, register them
			CanLeadModel newUser = new CanLeadModel();
			newUser.setMobileNumber(mobileNumber);
			newUser.setName(name);
			newUser.setLastName(lastName);
			newUser.setDateOfBirth(dob);
			newUser.setGender(gender);
			newUser.setCountry("India");
			newUser.setState(state);
			newUser.setCity(city);
			newUser.setAge(age);
			newUser.setFromWA(true);
			newUser.setFromApp(false);
			newUser.setWhatsappNumber(whatsappNumber);
			newUser.setContactNumber(conNumber);
			newUser.setAssignTo(0);

			newUser.setProfilePageNo(1);
			canLeadRepository.save(newUser);

			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 200);
			response.put("message", "User Registered Successfully");
			return ResponseEntity.ok(response);
		}
	}

	@PutMapping(path = "/updateEducationDetails")
	public ResponseEntity<?> updateEducationDetails(@RequestParam("mobileNumber") final long mobileNumber,
													@RequestBody CanLeadModel canLeadModel){

		CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mobileNumber);
		if (existingUser != null) {
			existingUser.setQualification(canLeadModel.getQualification());
			existingUser.setSpecification(canLeadModel.getSpecification());
			existingUser.setStudent(canLeadModel.getStudent());
			existingUser.setPassed_out_year(canLeadModel.getPassed_out_year());
			existingUser.setIsHavingArrear(canLeadModel.getIsHavingArrear());
			existingUser.setProfilePageNo(2);

			canLeadRepository.save(existingUser);

			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 200);
			response.put("message", "Updated Successfully");
			return ResponseEntity.ok(response);
		} else {
			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 400);
			response.put("message", "Mobile Number does not exist");
			return ResponseEntity.badRequest().body(response);
		}
	}

	@PutMapping(path = "/updateWorkDetails")
	public ResponseEntity<?> workDetails(@RequestParam("mobileNumber") final long mobileNumber,
										 @RequestBody CanLeadModel canLeadModel) {

		CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mobileNumber);
		if (existingUser != null) {
			existingUser.setExpInManufacturing(canLeadModel.isExpInManufacturing());
			existingUser.setExperienced(canLeadModel.isExperienced());
			existingUser.setIndustry(canLeadModel.getIndustry());
			existingUser.setJobCategory(canLeadModel.getJobCategory());
			existingUser.setExpYears(canLeadModel.getExpYears());
			existingUser.setExpMonths(canLeadModel.getExpMonths());
			existingUser.setPrefLocation(canLeadModel.getPrefLocation());
			existingUser.setPrefArea(canLeadModel.getPrefArea());
			existingUser.setPfEsiAccount(canLeadModel.getPfEsiAccount());
			existingUser.setProfilePageNo(3);
			setAssignToBasedOnPrefArea(existingUser);

			canLeadRepository.save(existingUser);

			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 200);
			response.put("message", "Updated Successfully");
			return ResponseEntity.ok(response);
		} else {
			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 400);
			response.put("message", "Mobile Number does not exist");
			return ResponseEntity.badRequest().body(response);
		}
	}

	@Transactional
	@PutMapping(path = "/updateOtherDetails")
	public ResponseEntity<?> updateOtherDetails(@RequestParam("mobileNumber") long mobileNumber, @RequestBody CanLeadModel canLeadModel,
	                                             @RequestParam Long adminId) throws ResourceNotFoundException {
		CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mobileNumber);
		Admin admin = adminRepository.findById(adminId).get();
		if (existingUser != null) {
			existingUser.setKnownLanguages(canLeadModel.getKnownLanguages());
			existingUser.setCourses(canLeadModel.getCourses());
			existingUser.setKeySkill(canLeadModel.getKeySkill());
			existingUser.setReference(canLeadModel.getReference());
			existingUser.setProfilePageNo(4);
			canLeadRepository.save(existingUser);
			
			

			UserModel user = new UserModel();

			user.setFirstName(existingUser.getName());
			user.setMobileNumber(existingUser.getMobileNumber());
			if(existingUser.getCountryCode()!=null) {
			user.setCountryCode(existingUser.getCountryCode());
			}
			if(existingUser.getProfilePic()!=null) {
			user.setProfilePic(existingUser.getProfilePic());
			}
			user.setDeleted(false);

			String token = UUID.randomUUID().toString();
			user.setToken(token);

			userRepository.save(user);

			int userID = user.getId();

			CandidateModel candidate = new CandidateModel();

			candidate.setUserId(userID);
			// Set candidate details
			candidate.setDeleted(false);
			candidate.setFirstName(existingUser.getName());
			candidate.setLastName(existingUser.getLastName());
			candidate.setMobileNumber(existingUser.getMobileNumber());
			candidate.setWhatsappNumber(existingUser.getMobileNumber());
			candidate.setContactNumber(existingUser.getContactNumber());
			candidate.setDateOfBirth(existingUser.getDateOfBirth());
			candidate.setAge(String.valueOf(existingUser.getAge()));
			candidate.setGender(existingUser.getGender());
			candidate.setPrefCountry("India");
			candidate.setState(existingUser.getState());
			candidate.setCity(existingUser.getPrefLocation());
			candidate.setPrefArea(existingUser.getPrefArea());
			candidate.setAssignTo(existingUser.getAssignTo());
			candidate.setCurrentCity(existingUser.getCity());
			candidate.setQualification(existingUser.getQualification());
			candidate.setSpecification(existingUser.getSpecification());
			candidate.setCertificationSpecialization(existingUser.getCourses());
			candidate.setCertificationCourses("Certification Courses");
			candidate.setJobType("Full Time (8hrs to 10hrs)");
			candidate.setCandidateLocation("Domestic");
			candidate.setIndustry(existingUser.getIndustry());
			candidate.setJobCategory(existingUser.getJobCategory());
			candidate.setKeySkill(existingUser.getKeySkill());
			candidate.setCandidateType(existingUser.getCandidateType());
			candidate.setExperience(existingUser.getExpYears());
			candidate.setExpMonths(existingUser.getExpMonths());
			candidate.setIsHavingArrear(existingUser.getIsHavingArrear());
			candidate.setPfEsiAccount(existingUser.getPfEsiAccount());
			candidate.setFcmToken(existingUser.getFcmToken());
			candidate.setLanguageKey(existingUser.getLanguageKey());
			candidate.setRegistered(true);
			candidate.setProfileFilled(true);
			candidate.setWACampaign(true);
			candidate.setRegInApp(false);
			candidate.setCurrentlyworking(existingUser.isCurrentlyworking());
			candidate.setLookingForaJob(existingUser.isLookingForaJob());
			candidate.setImmediateJoiner(existingUser.isImmediateJoiner());
			candidate.setReason_for_unemployment(existingUser.getReason_for_unemployment());
			candidate.setReason_for_jobchange(existingUser.getReason_for_jobchange());
			candidate.setFromApp(false);
			candidate.setFromWA(false);
			candidate.setFromAdmin(true);
			candidate.setReference(existingUser.getReference());
			candidate.setStudent(existingUser.getStudent());
			candidate.setPassed_out_year(existingUser.getPassed_out_year());
			candidate.setAssignTo(Math.toIntExact(adminId));
			
			// Assuming you have the existing candidate ID and lead ID
		
			if (existingUser.getExpYears() > 0 || existingUser.getExpMonths() > 0) {
				candidate.setExperience(existingUser.getExpYears());
				candidate.setExpMonths(existingUser.getExpMonths());
				candidate.setCandidateType("Experienced");
				candidate.setExpInManufacturing(true);
				candidate.setIndustry(existingUser.getIndustry());
				candidate.setJobCategory(existingUser.getJobCategory());
			} else {
				candidate.setExperience(0);
				candidate.setExpMonths(0);
				candidate.setExpInManufacturing(false);
				candidate.setCandidateType("Fresher");

				if(existingUser.getJobCategory()!=null) {
					switch (existingUser.getJobCategory().toLowerCase()) {
						case "trainee":
						case "assembler":
						case "graduate trainee":
							candidate.setExpInManufacturing(false);
							break;
						default:
							break;
					}

				}


			}
			

			String languages = existingUser.getKnownLanguages();

			if (languages != null && !languages.isEmpty()) {
				List<CanLanguageModel> languageModels = new ArrayList<>();

				String[] languageIds = languages.split(",");

				for (String languageIdStr : languageIds) {
					try {
						int languageId = Integer.parseInt(languageIdStr.trim());

						CanLanguageModel languageModel = new CanLanguageModel();
						languageModel.setLanguageId(languageId);
						languageModel.setCandidate(candidate);

						languageModels.add(languageModel);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}


				
				// Save all valid language models to the repository in a single batch
				if (!languageModels.isEmpty()) {
					candidate.getLanguages().addAll(languageModels);
					candidateRepository.save(candidate);
					
					// Flush and clear the Hibernate session to persist changes
	                entityManager.flush();
	                entityManager.clear();
				}
			}


			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			candidate.setProfileLastUpdatedDt(dtf.format(now));

			candidateRepository.save(candidate);
			
			candidateAnalyticsService.canRegistrationCounts(adminId,LocalDate.now());
			
			// Create a candidate timeline entry
			CandidateTimeLine candidateTimeLine = new CandidateTimeLine();
			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
			String formattedDate = dateFormat.format(currentDate);
			String eventDescription = "Registered On <b>" + formattedDate + "</b> By <b>" + admin.getUserName() + "</b>";
			candidateTimeLine.setCanId(candidate.getId());
			candidateTimeLine.setEventName("Registration");
			candidateTimeLine.setEventDescription(eventDescription);
			candidateTimeLineRepository.save(candidateTimeLine);


			Optional<CandidateModel> optional = candidateRepository.findByUserId(userID);
			if (optional.isPresent()) {
				DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
				String date = formatter.format(new Date());
				SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
				Calendar cal = Calendar.getInstance();
				String time = simpleDateFormat1.format(cal.getTime());

				CandidateModel c = optional.get();
			}

			 List<CanLeadModel> leadMN = canLeadRepository.findByMobileNumberList(existingUser.getMobileNumber());

		        // Delete all leads associated with the mobile number
		        if (!leadMN.isEmpty()) {
		            for (CanLeadModel lead : leadMN) {
		              //  candidateTimeLineRepository.deleteByCanLeadId(lead.getId());
		            	// Step 1: Delete record from facebookMetaLeadRepository
		                canLeadRepository.deleteById(lead.getId());
						// Step 2: Find relevant records in candidateTimeLineRepository
		                List<CandidateTimeLine> timelineRecords = candidateTimeLineRepository.findByCanLeadId(lead.getId());

						// Step 3: Update canLeadId in the found records
						for (CandidateTimeLine timelineRecord : timelineRecords) {
							timelineRecord.setCanId(candidate.getId());
						    timelineRecord.setCanLeadId(0);
						   
						}
		            }
		        }
		        

			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 200);
			response.put("message", "Updated Successfully");
			response.put("data",candidate);
			return ResponseEntity.ok(response);
		} else {
			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 400);
			response.put("message", "Mobile Number does not exist");
			return ResponseEntity.badRequest().body(response);
		}
	}

	@PutMapping(value = "/updateUserProfilePic")
	public ResponseEntity<?> updateProfilePicture(
			@RequestParam("mobileNumber") final long mobileNumber,
			@RequestPart(value = "file") MultipartFile file) {

		UserModel existingUser = userRepository.findByMobileNumber(mobileNumber);

		if (existingUser == null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {
			if (file != null && !file.isEmpty()) {
				try {
					String url = this.userService.uploadProfilePicToS3Bucket1(file, mobileNumber, true);
					existingUser.setProfilePic(url);
				} catch (Exception e) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 500);
					map.put("message", "Error uploading profile picture");
					return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "File not found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}

			userRepository.save(existingUser);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Profile picture and details updated successfully");
			map.put("Image URL", existingUser.getProfilePic());
			map.put("results", existingUser);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	@PutMapping(value = "/resumeUpload")
	public ResponseEntity<?> updateProfileResume(
			 @RequestParam long mobileNumber,
		        @RequestParam MultipartFile file) throws IOException, java.io.IOException {
		    CandidateModel candidateOptional = candidateRepository.findByMobileNumber(mobileNumber);
		if (candidateOptional == null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {
			if (file != null && !file.isEmpty()) {
				try {
					String url = this.userService.uploadProfileResToS3Bucket1(file, mobileNumber, true);
					candidateOptional.setResume(url);
				} catch (Exception e) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 500);
					map.put("message", "Error uploading profile picture");
					return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "File not found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}

			candidateRepository.save(candidateOptional);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", " Resume and details updated successfully");
			map.put("Document URL", candidateOptional.getResume());
			map.put("results", candidateOptional);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	
	@PutMapping("/moveTOAssign")
	public ResponseEntity<?>updateAssignTo(@RequestParam Integer canleadId,
										   @RequestParam Long adminId){
		CanLeadModel canLeadModel = canLeadRepository.findById(canleadId).get();
		HashMap<String, Object> map = new HashMap<>();
		if(canLeadModel!=null)
		{
			canLeadModel.setAssignTo(Math.toIntExact(adminId));
			canLeadRepository.save(canLeadModel);
		}
		map.put("code",200);
		map.put("message","updated Successfully");
		return new ResponseEntity<>(map,HttpStatus.OK);
	}
	
	@PutMapping("/moveTOCandidate")
	public ResponseEntity<?> updateAssignToCandidate(@RequestParam Integer candidateId,
	                                                  @RequestParam Long adminId) {
	    Optional<CandidateModel> optionalCandidateModel = candidateRepository.findById(candidateId);
	    
	    HashMap<String, Object> map = new HashMap<>();

	    if (optionalCandidateModel.isPresent()) {
	        CandidateModel candidateModel = optionalCandidateModel.get();
	        candidateModel.setAssignTo(Math.toIntExact(adminId));
	        candidateRepository.save(candidateModel);

	        map.put("code", 200);
	        map.put("message", "Updated Successfully");
	        return new ResponseEntity<>(map, HttpStatus.OK);
	    } else {
	        map.put("code", 404);
	        map.put("message", "Candidate not found");
	        return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
	    }
	}
	
	@PutMapping("/moveTOCandidateLead")
	public ResponseEntity<?> updateAssignToCandidateLead(@RequestParam Integer candidateLeadId,
	                                                  @RequestParam Long adminId) {
	    Optional<CanLeadModel> optionalCandidateModel = canLeadRepository.findById(candidateLeadId);
	    
	    HashMap<String, Object> map = new HashMap<>();

	    if (optionalCandidateModel.isPresent()) {
	        CanLeadModel candidateModel = optionalCandidateModel.get();
	        candidateModel.setAssignTo(Math.toIntExact(adminId));
	        canLeadRepository.save(candidateModel);

	        map.put("code", 200);
	        map.put("message", "Updated Successfully");
	        return new ResponseEntity<>(map, HttpStatus.OK);
	    } else {
	        map.put("code", 404);
	        map.put("message", "Candidate not found");
	        return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
	    }
	}


	@PostMapping("/candidateCall")
	public String getCandidateMobileNumberByAdminId(
			@RequestParam Long adminId,
			@RequestParam int candidateId) {

		String adminMobileNumber = adminRepository.findMobileNumberByAdminId(adminId);
		String candidateMobileNumber=candidateRepository.findByMobileNumberById(candidateId);

		Map<String, Object> response = new HashMap<>();

		if (adminMobileNumber != null && candidateMobileNumber!=null) {
			// Use the provided mobileNumber query parameter, if available, or the admin's mobile number
			String targetMobileNumber = candidateMobileNumber != null ? candidateMobileNumber : adminMobileNumber;

			response.put("code",200);
			response.put("message","call Initialed success");
			return exotelCallController.connectToCandidateCalls(
					"+91" + targetMobileNumber, adminMobileNumber);
		} else {
			response.put("code",400);
			response.put("message","call not  Initialed");
			return "Mobile number not found";
		}
	}

	@PostMapping("/canLeadTimeLine")
	public Map<String, Object> createCandidateLeadTimeLine(
			@RequestParam("canLeadId") int canLeadId,
			@RequestParam("adminId") Long adminId,
			@RequestParam("eventName") String eventName,
			@RequestParam(value = "notes", required = false) String notes) {

		Map<String, Object> response = new HashMap<>();

		CanLeadModel can = canLeadRepository.findById(canLeadId).orElse(null);
		Admin a = adminRepository.findById(adminId).orElse(null);

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String formattedDate = dateFormat.format(currentDate);

		if (can == null || a == null || eventName.isEmpty()) {
			response.put("status", 400);
			response.put("message", "Bad Request: Missing or invalid parameters");
		} else {
			CandidateTimeLine candidateTimeLine = new CandidateTimeLine();
			candidateTimeLine.setCanLeadId(canLeadId);
			candidateTimeLine.setCanId(0);
			candidateTimeLine.setNotes(notes);
			candidateTimeLine.setEventName(eventName);

			if ("CSS Intro call".equals(eventName)) {
				candidateTimeLine.setEventDescription("Intro call by <b>" + a.getUserName() + "</b> on " + formattedDate);

				List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(Long.valueOf(adminId));

				if (!adminAnalyticsList.isEmpty()) {
					// Check if the createdOn date is the same as the current date
					LocalDate currentDate1 = LocalDate.now();
					boolean dateMatch = false;

					for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
						LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
						if (currentDate1.isEqual(createdOnDate)) {
							dateMatch = true;
							adminAnalytics.setCanIntroCall(
									adminAnalytics.getCanIntroCall() != null
											? adminAnalytics.getCanIntroCall() + 1
											: 1
							);
						}
					}
					if (!dateMatch) {

						// If the dates are different for all records, insert a new record
						AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
						newAdminAnalytics.setAdminId(Long.valueOf(adminId));
						newAdminAnalytics.setModule(a.getModule());
						newAdminAnalytics.setCanIntroCall(1);
						adminAnalyticsList.add(newAdminAnalytics);
					}

					adminAnalyticsRepository.saveAll(adminAnalyticsList);
				} else {

					// If there are no existing records for the adminId, insert a new record
					AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
					adminAnalytics.setAdminId(Long.valueOf(adminId));
					adminAnalytics.setModule(a.getModule());
					adminAnalytics.setCanIntroCall(1);
					adminAnalyticsRepository.save(adminAnalytics);
				}
			} else if ("Not qualified".equals(eventName)) {
				candidateTimeLine.setEventDescription("Not qualified by"+"<b>" + a.getUserName() + "</b>"+formattedDate);
			}

			if (notes != null && !notes.isEmpty()) {
				candidateTimeLine.setNotes(notes);
			}

			candidateTimeLineRepository.save(candidateTimeLine);

			response.put("status", 200);
			response.put("message", "Success");
			response.put("data", candidateTimeLine);
		}

		return response;
	}
	
	@PostMapping("/fbTimeLine")
	public Map<String, Object> createFbTimeLine(
	        @RequestParam Long fbId,
	        @RequestParam Long adminId,
	        @RequestParam String eventName,
	        @RequestParam(required = false) String notes) {

	    Map<String, Object> response = new HashMap<>();

	    FacebookMetaLead can = facebookMetaLeadRepository.findById(fbId).orElse(null);
	    Admin a = adminRepository.findById(adminId).orElse(null);

	    // Adjust the next line based on how cfgcan is retrieved from your repository
	    CfgCanTimelineEvents cfgcan = cfgCanTimelineEventsRepository.findByEventName(eventName);

	    Date currentDate = new Date();
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
	    String formattedDate = dateFormat.format(currentDate);

	    if (can == null || a == null || cfgcan == null) {
	        response.put("status", 400);
	        response.put("message", "Bad Request: Missing or invalid parameters");
	    } else {
	        CandidateTimeLine candidateTimeLine = new CandidateTimeLine();
	        candidateTimeLine.setCanLeadId(0);
	        candidateTimeLine.setFacebookId(fbId);
	        candidateTimeLine.setCanId(0);
	        candidateTimeLine.setNotes(notes);
	        candidateTimeLine.setEventName(cfgcan.getEventName());

	        if (cfgcan.getEventName() != null) {
	            candidateTimeLine.setEventDescription(
	                    cfgcan.getEventName() + "by" + a.getUserName() + "</b> on " + formattedDate);

	            List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(adminId);

	            if (!adminAnalyticsList.isEmpty()) {
	                // Check if the createdOn date is the same as the current date
	                LocalDate currentDate1 = LocalDate.now();
	                boolean dateMatch = false;

	                for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
	                    LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
	                    if (currentDate1.isEqual(createdOnDate)) {
	                        dateMatch = true;

	                        // Update Analyticscount based on the eventName
	                        updateAnalyticsCount(adminAnalytics, cfgcan.getEventName());

	                        adminAnalyticsRepository.save(adminAnalytics);
	                    }
	                }
	                if (!dateMatch) {
	                    // If the dates are different for all records, insert a new record
	                    AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
	                    newAdminAnalytics.setAdminId(adminId);
	                    newAdminAnalytics.setModule(a.getModule());
	                    newAdminAnalytics.setAnalyticscount(1);

	                    // Update Analyticscount based on the eventName
	                    updateAnalyticsCount(newAdminAnalytics, cfgcan.getEventName());

	                    adminAnalyticsList.add(newAdminAnalytics);
	                    adminAnalyticsRepository.saveAll(adminAnalyticsList);
	                }
	            } else {
	                // If there are no existing records for the adminId, insert a new record
	                AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
	                adminAnalytics.setAdminId(adminId);
	                adminAnalytics.setModule(a.getModule());
	                adminAnalytics.setAnalyticscount(1);

	                // Update Analyticscount based on the eventName
	                updateAnalyticsCount(adminAnalytics, cfgcan.getEventName());

	                adminAnalyticsRepository.save(adminAnalytics);
	            }
	        }

	        if (notes != null && !notes.isEmpty()) {
	            candidateTimeLine.setNotes(notes);
	        }

	        candidateTimeLineRepository.save(candidateTimeLine);

	        response.put("status", 200);
	        response.put("message", "Success");
	        response.put("data", candidateTimeLine);
	    }

	    return response;
	}

	// Add this method to your service or repository class
	private void updateAnalyticsCount(AdminAnalyticsModel adminAnalytics, String eventName) {
	    switch (eventName) {
	        case "Not attended":
	            adminAnalytics.setNotAttendedCount(
	                    adminAnalytics.getNotAttendedCount() != null
	                            ? adminAnalytics.getNotAttendedCount() + 1
	                            : 1
	            );
	            break;
	        case "Wrong Person":
	            adminAnalytics.setWrongPersonCount(
	                    adminAnalytics.getWrongPersonCount() != null
	                            ? adminAnalytics.getWrongPersonCount() + 1
	                            : 1
	            );
	            break;
	        case "Number blocked":
	            adminAnalytics.setNumberBlockedCount(
	                    adminAnalytics.getNumberBlockedCount() != null
	                            ? adminAnalytics.getNumberBlockedCount() + 1
	                            : 1
	            );
	            break;
	        case "Not reachable":
	            adminAnalytics.setNotReachableCount(
	                    adminAnalytics.getNotReachableCount() != null
	                            ? adminAnalytics.getNotReachableCount() + 1
	                            : 1
	            );
	            break;
	        case "Switch off":
	            adminAnalytics.setSwitchOffCount(
	                    adminAnalytics.getSwitchOffCount() != null
	                            ? adminAnalytics.getSwitchOffCount() + 1
	                            : 1
	            );
	            break;
	        // Add more cases if needed
	        default:
	            // Handle unknown event names
	            break;
	    }
	}




	@PutMapping("/fbMetaLeads")
	public ResponseEntity<?> updateStatus(@RequestParam Long id,
										  @RequestParam Long adminId,
										  @RequestParam boolean qualified,
										  @RequestParam boolean notQualified,
										  @RequestParam(required=false) boolean notAttend,
										  @RequestParam(required=false) String notes
	) {
		Optional<FacebookMetaLead> fbOptional = facebookMetaLeadRepository.findById(id);
		Admin a = adminRepository.findById(adminId).get();
		CandidateTimeLine candidateTimeLine=new CandidateTimeLine();
		Date currentDates = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String formattedDate = dateFormat.format(currentDates);


		if (fbOptional.isPresent()) {
			FacebookMetaLead fb = fbOptional.get();
			
			

			if (qualified && !notQualified) {
				fb.setQualified(true);
				fb.setNotQualified(false);
				fb.setInActive(true);
				facebookMetaLeadRepository.save(fb);
				
				// Create CanLeadModel
				CanLeadModel canLeadModel = new CanLeadModel();
				canLeadModel.setMobileNumber(Long.parseLong(fb.getMobileNumber()));
				canLeadModel.setWhatsappNumber(Long.parseLong(fb.getWhatsappNumber()));
				canLeadModel.setName(fb.getCandidateName());
				canLeadModel.setQualification(fb.getEducationQualification());
				canLeadModel.setJobCategory(fb.getJobCategory());
				canLeadModel.setQualified(true);
				canLeadModel.setFromFbMetaLeadAd(true);
				canLeadModel.setIndustry(fb.getIndustry());
				canLeadModel.setCurrentlyworking(fb.isCurrentlyWorking());
				canLeadModel.setJoiningAvailability(fb.getJoiningAvailability());
				if (adminId != null) {
					canLeadModel.setAssignTo(Math.toIntExact(adminId));
				}
				canLeadModel.setPrefLocation(fb.getPreferredLocation()!= null && !fb.getPreferredLocation().isEmpty() ? fb.getPreferredLocation() : fb.getCandidatePreferredLocation());
				canLeadRepository.save(canLeadModel);
				// Step 1: Delete record from facebookMetaLeadRepository
			//	facebookMetaLeadRepository.deleteById(id);

				// Step 2: Find relevant records in candidateTimeLineRepository
				List<CandidateTimeLine> timelineRecords = candidateTimeLineRepository.findByFacebookId(id);

				// Step 3: Update canLeadId in the found records
				for (CandidateTimeLine timelineRecord : timelineRecords) {
				    timelineRecord.setCanLeadId(canLeadModel.getId());
				    timelineRecord.setFacebookId(0L);
				    timelineRecord.setCanId(0);
				}

				// Step 4: Save the updated records in candidateTimeLineRepository
				candidateTimeLineRepository.saveAll(timelineRecords);

				
				candidateTimeLine.setFacebookId(0L);
				candidateTimeLine.setEventName("Qualified");
				candidateTimeLine.setEventDescription("Qualified by " +a.getUserName() + "</b> on " + formattedDate);
				candidateTimeLine.setCanId(0);
				candidateTimeLine.setCanLeadId(canLeadModel.getId());
				candidateTimeLine.setNotes(notes);
				candidateTimeLineRepository.save(candidateTimeLine);
				
				candidateAnalyticsService.QualifiedfbMetaLeadcounts(adminId,LocalDate.now());
				
			} else if (!qualified && notQualified) {
				fb.setQualified(false);
				fb.setNotQualified(true);
				facebookMetaLeadRepository.save(fb);
				candidateTimeLine.setFacebookId(fb.getId());
				candidateTimeLine.setEventName("NotQualified");
				candidateTimeLine.setEventDescription("NotQualified By " +a.getUserName() + "</b> on " + formattedDate);
				candidateTimeLine.setCanId(0);
				candidateTimeLine.setCanLeadId(0);
				candidateTimeLine.setNotes(notes);
				candidateTimeLineRepository.save(candidateTimeLine);

				List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(Long.valueOf(adminId));

				if (!adminAnalyticsList.isEmpty()) {
					// Check if the createdOn date is the same as the current date
					LocalDate currentDate = LocalDate.now();
					boolean dateMatch = false;

					for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
						LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
						if (currentDate.isEqual(createdOnDate)) {
							dateMatch = true;
							adminAnalytics.setCanLeadNotQualifiedCount(
									adminAnalytics.getCanLeadNotQualifiedCount() != null
											? adminAnalytics.getCanLeadNotQualifiedCount() + 1
											: 1
							);
						}
					}
					if (!dateMatch) {

						// If the dates are different for all records, insert a new record
						AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
						newAdminAnalytics.setAdminId(Long.valueOf(adminId));
						newAdminAnalytics.setModule(a.getModule());
						newAdminAnalytics.setCanLeadNotQualifiedCount(1);
						adminAnalyticsList.add(newAdminAnalytics);
					}

					adminAnalyticsRepository.saveAll(adminAnalyticsList);
				} else {

					// If there are no existing records for the adminId, insert a new record
					AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
					adminAnalytics.setAdminId(Long.valueOf(adminId));
					adminAnalytics.setModule(a.getModule());
					adminAnalytics.setCanLeadNotQualifiedCount(1);
					adminAnalyticsRepository.save(adminAnalytics);
				}
				
			} 
			else if(notAttend) {
				fb.setNotAttend(true);
				facebookMetaLeadRepository.save(fb);
				}
			else {
				Map<String, Object> response = new HashMap<>();
				response.put("code", HttpStatus.BAD_REQUEST.value());
				response.put("message", "Please provide either 'qualified' or 'notQualified' as true");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}



			Map<String, Object> response = new HashMap<>();
			response.put("code", HttpStatus.OK.value());
			response.put("message", "Success");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			Map<String, Object> response = new HashMap<>();
			response.put("code", HttpStatus.BAD_REQUEST.value());
			response.put("message", "Entity not found");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	@PutMapping("/midSeniorCandidate")
	public ResponseEntity<?> updateSeniorCan(@RequestParam(required=false) Long id,
										  @RequestParam(required=false) Long adminId,
										  @RequestParam(required=false) boolean qualified,
										  @RequestParam(required=false) boolean notQualified,
										  @RequestParam(required=false) String notes
	) {
		Optional<MidSeniorLevelCandidateLeadModel> fbOptional = midSeniorLevelCandidateLeadRepository.findById(id);
		Admin a = adminRepository.findById(adminId).get();
		CandidateTimeLine candidateTimeLine=new CandidateTimeLine();
		Date currentDates = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String formattedDate = dateFormat.format(currentDates);
		MidSeniorLevelCandidateLeadModel midCanLeadModel = fbOptional.get();
		int convertedAdminId = adminId.intValue();

		if (fbOptional.isPresent()) {
			MidSeniorLevelCandidateLeadModel fb = fbOptional.get();

			if (qualified && !notQualified) {
				fb.setQualified(true);
				fb.setNotQualified(false);
				fb.setAdminId((long) convertedAdminId);
				midSeniorLevelCandidateLeadRepository.save(fb);

				// Step 2: Find relevant records in candidateTimeLineRepository
				List<CandidateTimeLine> timelineRecords = candidateTimeLineRepository.findBySeniorCanId(id);

				// Step 3: Update canLeadId in the found records
				for (CandidateTimeLine timelineRecord : timelineRecords) {
				    timelineRecord.setSeniorCanId(midCanLeadModel.getId());
				    timelineRecord.setFacebookId(0L);
				    timelineRecord.setCanLeadId(0);
				    timelineRecord.setCanId(0);
				}

				// Step 4: Save the updated records in candidateTimeLineRepository
				candidateTimeLineRepository.saveAll(timelineRecords);

				
				candidateTimeLine.setFacebookId(0L);
				candidateTimeLine.setEventName("Qualified");
				candidateTimeLine.setEventDescription("Qualified by " +a.getUserName() + "</b> on " + formattedDate);
				candidateTimeLine.setCanId(0);
				candidateTimeLine.setSeniorCanId(midCanLeadModel.getId());
				candidateTimeLine.setCanLeadId(0);
				candidateTimeLine.setNotes(notes);
				candidateTimeLineRepository.save(candidateTimeLine);
			} else if (!qualified && notQualified) {
				fb.setQualified(false);
				fb.setNotQualified(true);
				fb.setAdminId((long) convertedAdminId);
				midSeniorLevelCandidateLeadRepository.save(fb);
				candidateTimeLine.setFacebookId(0L);
				candidateTimeLine.setSeniorCanId(midCanLeadModel.getId());
				candidateTimeLine.setEventName("NotQualified");
				candidateTimeLine.setEventDescription("NotQualified By " +a.getUserName() + "</b> on " + formattedDate);
				candidateTimeLine.setCanId(0);
				candidateTimeLine.setCanLeadId(0);
				candidateTimeLine.setNotes(notes);
				candidateTimeLineRepository.save(candidateTimeLine);
			} else {
				Map<String, Object> response = new HashMap<>();
				response.put("code", HttpStatus.BAD_REQUEST.value());
				response.put("message", "Please provide either 'qualified' or 'notQualified' as true");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}



			Map<String, Object> response = new HashMap<>();
			response.put("code", HttpStatus.OK.value());
			response.put("message", "Success");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			Map<String, Object> response = new HashMap<>();
			response.put("code", HttpStatus.BAD_REQUEST.value());
			response.put("message", "Entity not found");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}



//	@GetMapping("/Languages")
//	public ResponseEntity<Map<String, Object>> getLanguagesForCandidate(@RequestParam int candidateId) {
//	    List<CanLanguageModel> canLanguage = canLanguagesRepository.findByCandidateId(candidateId);
//
//	    if (canLanguage != null) {
//	        Map<String, Object> response = new HashMap<>();
//	        response.put("code", HttpStatus.OK.value());
//	        response.put("message", "Success");
//	        response.put("response", canLanguage);
//	        return new ResponseEntity<>(response, HttpStatus.OK);
//	    } else {
//	        Map<String, Object> response = new HashMap<>();
//	        response.put("code", HttpStatus.BAD_REQUEST.value());
//	        response.put("message", "Languages not found");
//	        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//	    }
//	}
	
	@PutMapping("/metaLeadAssignTo")
	public ResponseEntity<?>updateMetaLeadAssignTo(@RequestParam Integer metaLeadId,
										   @RequestParam Long adminId){
		FacebookMetaLead facebookMetaLead = facebookMetaLeadRepository.findById(Long.valueOf(metaLeadId)).get();
		HashMap<String, Object> map = new HashMap<>();
		if(facebookMetaLead!=null)
		{
			facebookMetaLead.setAssignTo(Math.toIntExact(adminId));
			facebookMetaLeadRepository.save(facebookMetaLead);
		}
		map.put("code",200);
		map.put("message","updated Successfully");
		return new ResponseEntity<>(map,HttpStatus.OK);
	}
	
	
	 @GetMapping(path = "/candidate")
		public ResponseEntity<?> getCandidateDetails(@RequestParam long  mobileNumber){
		CandidateModel candidateModel=candidateRepository.findByMobileNumber(mobileNumber);
		   
		   if(candidateModel!=null) {
			   HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Retrieve successfully");
				map.put("assignTo", candidateModel);
				return new ResponseEntity<>(map, HttpStatus.OK);    
		   }
		   HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Retreive not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
	   }
	 private final Logger logger = LoggerFactory.getLogger(getClass());
	 
	 @PutMapping("/docUpload")
		public ResponseEntity<String> updateResume(
		        @RequestParam long mobileNumber,
		        @RequestParam String docType,
		        @RequestParam MultipartFile file,
		        @RequestParam(required=false) Long adminId) throws IOException, java.io.IOException {
		    CandidateModel candidateOptional = candidateRepository.findByMobileNumber(mobileNumber);
		    CanDocuments canDocuments= new CanDocuments();
		    CfgCanDocuments cfgCanDocuments=cfgCanDocumentsRepository.findByDocTitleAndActive(docType, true); 

		    if (candidateOptional != null) {
		        CandidateModel candidate = candidateOptional; // Assign to a new variable

		        String existingResumeKey = candidate.getResume();

		        try {
		            // Check if the candidate already has a resume
		            if (existingResumeKey != null && !existingResumeKey.isEmpty()) {
		                logger.info("Deleting existing resume with key: {}", existingResumeKey);
		                // Delete the existing resume from S3
		                try {
		                    s3client.deleteObject(bucketName, existingResumeKey); // Delete the old resume
		                    logger.info("Deleted existing resume successfully.");
		                } catch (AmazonServiceException e) {
		                    logger.error("S3 Delete Error: {}", e.getMessage(), e);
		                    // Handle the delete error if needed
		                    // Consider whether you want to stop execution or continue
		                }
		            }

		            String key = folder + "/" + mobileNumber + "/Resumes/" + generateFileName(file);
		            String fileUrl = s3UploadFileAndReturnUrl(key, file);

		            // Delete the temporary file
		            File tempFile = new File("/tmp/" + file.getOriginalFilename());
		            if (tempFile.exists()) {
		                tempFile.delete();
		            }
		            
		            canDocuments.setDocLink(fileUrl);
		            candidateOptional.getDocuments().add(canDocuments);
		            canDocuments.setDocTitle(cfgCanDocuments.getDocTitle());
		            canDocuments.setDocKey(cfgCanDocuments.getDocKey());
		            canDocuments.setDocuments(candidate);
		            canDocuments.setAdminId(adminId);
		            canDocumentsRepository.save(canDocuments);
		            if(docType.equals("Resume")) {
		            candidate.setResume(fileUrl);
		            candidateRepository.save(candidate);
		            }
		            return ResponseEntity.status(HttpStatus.OK).body("Resume updated successfully");
		        } catch (AmazonServiceException e) {
		            logger.error("S3 Error: {}", e.getMessage(), e);
		            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                    .body("S3 upload/delete failed: " + e.getMessage());
		        }
		    }

		    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Candidate not found");
		}

		 private String s3UploadFileAndReturnUrl(String key, MultipartFile file) throws IOException, java.io.IOException {
		        ObjectMetadata metadata = new ObjectMetadata();
		        metadata.setContentLength(file.getSize());
		        metadata.setContentType(file.getContentType());

		        try {
		            s3client.putObject(new PutObjectRequest(bucketName, key, file.getInputStream(), metadata)
		                    .withCannedAcl(CannedAccessControlList.PublicRead));
		            return s3client.getUrl(bucketName, key).toString();
		        } catch (AmazonServiceException e) {
		            logger.error("S3 Error: {}", e.getMessage(), e);
		            throw new AmazonServiceException("Failed to upload to S3: " + e.getMessage());
		        }
		    }
		   private String generateFileName(MultipartFile file) {
		        return UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
		    }
		   
		   @GetMapping("/listCanDocuments")
		   public List<CfgCanDocuments> getAllDocuments() {
		        // Use the repository method to retrieve all documents.
		        return cfgCanDocumentsRepository.findAll();
		    }
		   
		   @GetMapping("/listMidSenior")
		   public Optional<MidSeniorLevelCandidateLeadModel> getAllMidList(@RequestParam long midCandidateId) {
		        // Use the repository method to retrieve all documents.
		        return midSeniorLevelCandidateLeadRepository.findById(midCandidateId);
		    }
		   
		   @GetMapping("/regStatusMidSenior")
		   public ResponseEntity<Map<String, Object>> getRegMidList(@RequestParam String mobileNumber) {
		       MidSeniorLevelCandidateLeadModel result = midSeniorLevelCandidateLeadRepository.findByMobileNumberAndWhatsappNumber(mobileNumber,mobileNumber);

		       if (result != null) {
		           Map<String, Object> successResponse = new HashMap<>();
		           successResponse.put("status", "success");
		           successResponse.put("message", "success");
		           successResponse.put("code", 200);
		           successResponse.put("data", result);
		           return ResponseEntity.ok(successResponse);
		       } else {
		           Map<String, Object> errorResponse = new HashMap<>();
		           errorResponse.put("code", 404);
		           errorResponse.put("message", "Not Found");
		           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		       }
		   }



		   
		   @GetMapping("/canDocument")
		   public ResponseEntity<List<CanDocuments>> getDocumentsByCandidate(@RequestParam int candidateId) {
		       List<CanDocuments> documents = canDocumentsRepository.findByDocumentsId(candidateId);
		       return ResponseEntity.ok(documents);
		   }
		   
		   
		   @GetMapping("/canLeads")
		   public ResponseEntity<Optional<CanLeadModel>> getCandidateLead(@RequestParam int candidateId) {
		       Optional<CanLeadModel> documents = canLeadRepository.findById(candidateId);
		       return ResponseEntity.ok(documents);
		   }
		   
		   @GetMapping("/eventDetails")
		   public ResponseEntity<List<CandidateTimeLine>> getCanEventDetails(
		           @RequestParam(required = false) Long facebookId,
		           @RequestParam(required = false) int canLeadId) {

		       List<CandidateTimeLine> canInterviews;

		       if (facebookId != null) {
		           canInterviews = candidateTimeLineRepository.findByFacebookId(facebookId);
		       } else if (canLeadId != 0) {
		           canInterviews = candidateTimeLineRepository.findByCanLeadId(canLeadId);
		       } else {
		           // Handle the case where neither facebookId nor canLeadId is provided.
		           // You might want to return an error response or handle it differently based on your requirements.
		           return ResponseEntity.badRequest().build();
		       }

		       return ResponseEntity.ok(canInterviews);
		   }

		   
		   @GetMapping("/assignArea")
		   public ResponseEntity<List<CfgCanAdminArea>> getActiveAreasByCityId(@RequestParam int cityId) {
		       List<CfgCanAdminArea> activeAreas = cfgCanAdminAreaRepository.findByCityIdAndActive(cityId, true);

		       if (activeAreas.isEmpty()) {
		           // No active areas for the given cityId
		           return ResponseEntity.noContent().build();
		       } else {
		           return ResponseEntity.ok(activeAreas);
		       }
		   }


		   @PutMapping("/updateMetaLeads")
		   public ResponseEntity<HashMap<String, Object>> updateMeta(@RequestBody FacebookMetaLead updatedMetaLead) {
			   HashMap<String, Object> response = new HashMap<>();
		       try {
		           // Check if the provided ID is valid
		           Long id = updatedMetaLead.getId();
		            if (id == null) {
		                response.put("status", "error");
		                response.put("message", "ID is required for updating.");
		                return ResponseEntity.badRequest().body(response);
		            }
		           // Check if the meta lead with the provided ID exists
		           Optional<FacebookMetaLead> facebookmeta = facebookMetaLeadRepository.findById(id);
		           if (facebookmeta.isPresent()) {
		               FacebookMetaLead existingMetaLead = facebookmeta.get();

		               // Update only the non-null fields from the provided data
		               if (updatedMetaLead.getCandidateName() != null) {
		                   existingMetaLead.setCandidateName(updatedMetaLead.getCandidateName());
		               }
		               if (updatedMetaLead.getMobileNumber() != null) {
		                   existingMetaLead.setMobileNumber(updatedMetaLead.getMobileNumber());
		               }
		               if (updatedMetaLead.getWhatsappNumber() != null) {
		                   existingMetaLead.setWhatsappNumber(updatedMetaLead.getWhatsappNumber());
		               }
		               if (updatedMetaLead.getEducationQualification() != null) {
		                   existingMetaLead.setEducationQualification(updatedMetaLead.getEducationQualification());
		               }
		               if (updatedMetaLead.getJobCategory() != null) {
		                   existingMetaLead.setJobCategory(updatedMetaLead.getJobCategory());
		               }
		               if (updatedMetaLead.getExperience() != null) {
		                   existingMetaLead.setExperience(updatedMetaLead.getExperience());
		               }
		               if (updatedMetaLead.getPreferredLocation() != null) {
		                   existingMetaLead.setPreferredLocation(updatedMetaLead.getPreferredLocation());
		               }
		               if (updatedMetaLead.getArea() != null) {
		                   existingMetaLead.setArea(updatedMetaLead.getArea());
		               }
		               if (updatedMetaLead.getFormId() != null) {
		                   existingMetaLead.setFormId(updatedMetaLead.getFormId());
		               }
		               if (updatedMetaLead.getResourcePlatform() != null) {
		                   existingMetaLead.setResourcePlatform(updatedMetaLead.getResourcePlatform());
		               }

		               facebookMetaLeadRepository.save(existingMetaLead);
		               // Save the updated meta lead
		    
		               response.put("status", "success");
		                response.put("message", "Meta lead updated successfully.");
		                response.put("data", existingMetaLead);
		                return ResponseEntity.ok(response);
		            } else {
		                response.put("status", "error");
		                response.put("message", "Meta lead with ID " + id + " not found.");
		                return ((BodyBuilder) ResponseEntity.notFound()).body(response);
		            }
		        } catch (Exception e) {
		            // Handle exceptions
		            response.put("status", "error");
		            response.put("message", "Failed to update meta lead: " + e.getMessage());
		            return ResponseEntity.badRequest().body(response);
		        }
		   }
		   
		   @DeleteMapping("/deleteResume")
		   public ResponseEntity<String> deleteResume(@RequestParam long id) {
		       try {
		           // Retrieve candidate document by ID
		           CanDocuments canDoc = canDocumentsRepository.findById(id).orElse(null);

		           if (canDoc != null) {
		               // Get candidate ID from candidate document
		               int canId = canDoc.getDocuments().getId();
		               String doc = canDoc.getDocLink();

		               // Retrieve candidate by ID
		               Optional<CandidateModel> candidateOptional = candidateRepository.findById(canId);

		               if (candidateOptional.isPresent()) {
		                   CandidateModel can = candidateOptional.get();

		                   // Get the existing resume key
		                   String existingResumeKey = can.getResume();

		                   // Check if the candidate has a resume
		                   if (doc != null && !doc.isEmpty()) {
		                       logger.info("Deleting resume with key: {}", doc);

		                       // Delete the resume from S3
		                       try {
		                           s3client.deleteObject(bucketName, doc);
		                           logger.info("Deleted resume successfully.");

		                           // Set the candidate's resume field to null
		                           if (existingResumeKey != null && !existingResumeKey.isEmpty()) {
		                               can.setResume(null); // Set the resume to null
		                               candidateRepository.save(can);
		                               logger.info("Candidate's resume field set to null.");
		                           }

		                           // Delete the candidate document
		                           canDocumentsRepository.deleteById(id);
		                           logger.info("Candidate document deleted successfully.");

		                           return ResponseEntity.status(HttpStatus.OK).body("Resume deleted successfully");
		                       } catch (AmazonServiceException e) {
		                           logger.error("S3 Delete Error: {}", e.getMessage(), e);
		                           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                                   .body("S3 delete failed: " + e.getMessage());
		                       }
		                   } else {
		                       return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No resume found for the candidate");
		                   }
		               } else {
		                   return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Candidate not found");
		               }
		           } else {
		               return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Candidate document not found");
		           }
		       } catch (Exception e) {
		           logger.error("Error deleting resume: {}", e.getMessage(), e);
		           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                   .body("Error deleting resume: " + e.getMessage());
		       }
		   }


		   @PutMapping(path = "/basicMidLevelLead")
		   public ResponseEntity<?> basicDetailsMidSenior(@RequestBody MidSeniorLevelCandidateLeadModel midSeniorLevel) {
		       String mobileNumber = midSeniorLevel.getMobileNumber();
		       String whatsappNumber = midSeniorLevel.getWhatsappNumber();
		       MidSeniorLevelCandidateLeadModel midSeniorLevelCandidateLeadModel = midSeniorLevelCandidateLeadRepository.findByMobileNumberAndWhatsappNumber(mobileNumber, whatsappNumber);

		       if (midSeniorLevelCandidateLeadModel != null) {
		           // If candidate lead exists, update only the provided fields
		           if (midSeniorLevel.getFirstName() != null) {
		               midSeniorLevelCandidateLeadModel.setFirstName(midSeniorLevel.getFirstName());
		           }
		           if (midSeniorLevel.getLastName() != null) {
		               midSeniorLevelCandidateLeadModel.setLastName(midSeniorLevel.getLastName());
		           }
		           if (midSeniorLevel.getEmailId() != null) {
		               midSeniorLevelCandidateLeadModel.setEmailId(midSeniorLevel.getEmailId());
		           }
		           if (midSeniorLevel.getMobileNumber() != null) {
		               midSeniorLevelCandidateLeadModel.setMobileNumber(midSeniorLevel.getMobileNumber());
		           }
		           if (midSeniorLevel.getEducationalQualification() != null) {
		               midSeniorLevelCandidateLeadModel.setEducationalQualification(midSeniorLevel.getEducationalQualification());
		           }
		           if (midSeniorLevel.getPrefJobLocation() != null) {
		               midSeniorLevelCandidateLeadModel.setPrefJobLocation(midSeniorLevel.getPrefJobLocation());
		           }
		           if (midSeniorLevel.getWhatsappNumber() != null) {
		               midSeniorLevelCandidateLeadModel.setWhatsappNumber(midSeniorLevel.getWhatsappNumber());
		           }

		           midSeniorLevelCandidateLeadRepository.save(midSeniorLevelCandidateLeadModel);

		           HashMap<String, Object> map = new HashMap<>();
		           map.put("statuscode", 200);
		           map.put("message", "Updated successfully");
		           map.put("assignTo", midSeniorLevelCandidateLeadModel);
		           return new ResponseEntity<>(map, HttpStatus.OK);
		       } else {
		           // If candidate lead does not exist, create a new one
		    	   midSeniorLevel.setFirstName(midSeniorLevel.getFirstName());
				   midSeniorLevel.setLastName(midSeniorLevel.getLastName());
				   midSeniorLevel.setEmailId(midSeniorLevel.getEmailId());
				   midSeniorLevel.setMobileNumber(midSeniorLevel.getMobileNumber());
				   midSeniorLevel.setEducationalQualification(midSeniorLevel.getEducationalQualification());
				   midSeniorLevel.setPrefJobLocation(midSeniorLevel.getPrefJobLocation());
				   midSeniorLevel.setWhatsappNumber(midSeniorLevel.getWhatsappNumber());
				   midSeniorLevel.setAdminId(midSeniorLevel.getAdminId());
				   midSeniorLevel.setProfilePageNo(1);
				   midSeniorLevelCandidateLeadRepository.save(midSeniorLevel);

		           HashMap<String, Object> map = new HashMap<>();
		           map.put("statuscode", 200);
		           map.put("message", "Register successfully");
		           map.put("assignTo", midSeniorLevel);
		           return new ResponseEntity<>(map, HttpStatus.OK);
		       }
		   }

		   @PutMapping(path = "/workDetailsMidLevelLead")
			public ResponseEntity<?> OtherDetailsMidSenior(@RequestBody MidSeniorLevelCandidateLeadModel midSeniorLevel) {
			   String mobileNumber=midSeniorLevel.getMobileNumber();
			   String whatsappNumber=midSeniorLevel.getWhatsappNumber();
			   MidSeniorLevelCandidateLeadModel existing=midSeniorLevelCandidateLeadRepository.findByMobileNumberAndWhatsappNumber(mobileNumber,whatsappNumber);
			   if(existing!=null) {
				   existing.setJobCategory(midSeniorLevel.getJobCategory());
				   existing.setExpInYears(midSeniorLevel.getExpInYears());
				   existing.setExpInMonths(midSeniorLevel.getExpInMonths());
				   existing.setNoticePeriod(midSeniorLevel.getNoticePeriod());
				   existing.setCurrentSalary(midSeniorLevel.getCurrentSalary());
				   existing.setExpectedSalary(midSeniorLevel.getExpectedSalary());
				   existing.setProfilePageNo(2);
				   existing.setExpInManufacturing(midSeniorLevel.isExpInManufacturing());
				   existing.setCurrentlyWorking(midSeniorLevel.isCurrentlyWorking());
				   midSeniorLevelCandidateLeadRepository.save(existing);
				   
				   HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", "Updated successfully");
					map.put("assignTo", existing);
					return new ResponseEntity<>(map, HttpStatus.OK);
			   }
			   return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Record not found");
			   }
		   
		 
		   
		   @PutMapping("/updateProfileResumeMidSenior")
			public ResponseEntity<?> ResumeUpload(
			        @RequestParam long mobileNumber,
			        @RequestParam(required=false) String linkedinUrl,
			        @RequestParam MultipartFile file) throws IOException, java.io.IOException {
			   MidSeniorLevelCandidateLeadModel candidateOptional = midSeniorLevelCandidateLeadRepository.findByMobileNumber(String.valueOf(mobileNumber));

			    if (candidateOptional != null) {
			    	MidSeniorLevelCandidateLeadModel candidate = candidateOptional; // Assign to a new variable

			        String existingResumeKey = candidate.getResumeLink();

			        try {
			            // Check if the candidate already has a resume
			            if (existingResumeKey != null && !existingResumeKey.isEmpty()) {
			                logger.info("Deleting existing resume with key: {}", existingResumeKey);
			                // Delete the existing resume from S3
			                try {
			                    s3client.deleteObject(bucketName, existingResumeKey); // Delete the old resume
			                    logger.info("Deleted existing resume successfully.");
			                } catch (AmazonServiceException e) {
			                    logger.error("S3 Delete Error: {}", e.getMessage(), e);
			                    // Handle the delete error if needed
			                    // Consider whether you want to stop execution or continue
			                }
			            }

			            String key = folder + "/" + mobileNumber + "/Resumes/" + generateFileName(file);
			            String fileUrl = s3UploadFileAndReturnUrl(key, file);

			            // Delete the temporary file
			            File tempFile = new File("/tmp/" + file.getOriginalFilename());
			            if (tempFile.exists()) {
			                tempFile.delete();
			            }
			         
			            candidate.setResumeLink(fileUrl);
			            candidate.setRegistered(true);
			            candidate.setProfilePageNo(3);
			            candidate.setLinkedinUrl(linkedinUrl);
			            midSeniorLevelCandidateLeadRepository.save(candidate);
			            
			            HashMap<String, Object> map = new HashMap<>();
						map.put("statuscode", 200);
						map.put("message", " Resume Updated successfully");
						map.put("Details", candidate);
						return new ResponseEntity<>(map, HttpStatus.OK);
			        } catch (AmazonServiceException e) {
			            logger.error("S3 Error: {}", e.getMessage(), e);
			            HashMap<String, Object> map = new HashMap<>();
						map.put("statuscode", 400);
						map.put("message", "Updated failed");
						map.put("Details", candidate);
						return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			            
			        }
			    }

			    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Candidate not found");
			}
		   
		   @GetMapping("/canMidSeniorFilter")
		   public ResponseEntity<Map<String, Object>> SeniorMidLeads(
		           @RequestParam(required = false) String educationQualification,
		           @RequestParam(required = false) String mobileNumber,
		           @RequestParam(required = false) String emailId,
		           @RequestParam(required = false) Integer status,
		           @RequestParam(required = false) String preferredJobLocation,
		           @RequestParam(required = false) Boolean experienceInManufacturing,
		           @RequestParam(required = false) String jobCategory,
		           @RequestParam(required = false) Integer minExperience,
		           @RequestParam(required = false) Integer maxExperience,
		           @RequestParam(required = false) Boolean currentlyWorking,
		           @RequestParam(required = false) String joiningDate,
		           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") String createdTimeStart,
		           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") String createdTimeEnd,
		           @RequestParam(required = false, defaultValue = "0") Integer page,
		           @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

		       Page<MidSeniorLevelCandidateLeadModel> result;
		       Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdTime").descending());

		       Specification<MidSeniorLevelCandidateLeadModel> spec = Specifications.where(null);

		       if (educationQualification != null) {
		           spec = spec.and((root, query, builder) -> builder.like(root.get("educationalQualification"), "%" + educationQualification + "%"));
		       }

		       if (mobileNumber != null) {
		           spec = spec.and((root, query, builder) ->
		                   builder.or(
		                           builder.equal(root.get("mobileNumber"), mobileNumber),
		                           builder.equal(root.get("whatsappNumber"), mobileNumber)
		                   )
		           );
		       }

		       if (emailId != null) {
		           spec = spec.and((root, query, builder) -> builder.like(root.get("emailId"), "%" + emailId + "%"));
		       }

		       if (status != null) {
		           spec = spec.and((root, query, builder) -> builder.equal(root.get("status"), status));
		       }

		       if (preferredJobLocation != null) {
		           spec = spec.and((root, query, builder) -> builder.like(root.get("prefJobLocation"), "%" + preferredJobLocation + "%"));
		       }

		       if (experienceInManufacturing != null) {
		           spec = spec.and((root, query, builder) -> {
		               if (experienceInManufacturing) {
		                   return builder.isTrue(root.get("expInManufacturing"));
		               } else {
		                   return builder.isFalse(root.get("expInManufacturing"));
		               }
		           });
		       }

		       if (jobCategory != null) {
		           spec = spec.and((root, query, builder) -> builder.like(root.get("jobCategory"), "%" + jobCategory + "%"));
		       }

		       if (minExperience != null && maxExperience != null) {
		           spec = spec.and((root, query, builder) ->
		                   builder.between(root.get("expInYears"), minExperience, maxExperience)
		           );
		       } else if (minExperience != null) {
		           spec = spec.and((root, query, builder) ->
		                   builder.greaterThanOrEqualTo(root.get("expInYears"), minExperience)
		           );
		       } else if (maxExperience != null) {
		           spec = spec.and((root, query, builder) ->
		                   builder.lessThanOrEqualTo(root.get("expInYears"), maxExperience)
		           );
		       }

		       if (currentlyWorking != null) {
		           spec = spec.and((root, query, builder) -> {
		               if (currentlyWorking) {
		                   return builder.isTrue(root.get("isCurrentlyWorking"));
		               } else {
		                   return builder.isFalse(root.get("isCurrentlyWorking"));
		               }
		           });
		       }

		       if (joiningDate != null) {
		           spec = spec.and((root, query, builder) -> builder.equal(root.get("joiningDate"), joiningDate));
		       }

		       if (createdTimeStart != null && createdTimeEnd != null) {
		    	    LocalDate startDate = LocalDate.parse(createdTimeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		    	    LocalDate endDate = LocalDate.parse(createdTimeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		    	    Timestamp startDateTime = Timestamp.valueOf(startDate.atStartOfDay());
		    	    Timestamp endDateTime = Timestamp.valueOf(endDate.atStartOfDay());

		    	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		    	    if (status == 0) {
		    	        spec = spec.and((root, query, builder) -> builder.between(root.get("createdTime"), startDateTime, endDateTime));
		    	        pageable = PageRequest.of(page, pageSize, Sort.by("createdTime").descending());
		    	    } else if (status == 1) {
		    	        spec = spec.and((root, query, builder) -> {
		    	            Expression<String> screeningAtAsString = builder.function("DATE_FORMAT", String.class, root.get("screeningAt"), builder.literal("%Y-%m-%d %H:%i:%s"));
		    	            return builder.between(screeningAtAsString, startDateTime.toString(), endDateTime.toString());
		    	        });
		    	        pageable = PageRequest.of(page, pageSize, Sort.by("screeningAt").descending());
		    	    } else if (status == 2) {
		    	        spec = spec.and((root, query, builder) -> {
		    	            Expression<String> shortlistedAtAsString = builder.function("DATE_FORMAT", String.class, root.get("shortlistedAt"), builder.literal("%Y-%m-%d %H:%i:%s"));
		    	            return builder.between(shortlistedAtAsString, startDateTime.toString(), endDateTime.toString());
		    	        });
		    	        pageable = PageRequest.of(page, pageSize, Sort.by("shortlistedAt").descending());
		    	    }
		    	}


		       // Applying sorting based on the status
		       if (status != null) {
		           if (status == 1) {
		               pageable = PageRequest.of(page, pageSize, Sort.by("screeningAt").descending());
		           } else if (status == 2) {
		               pageable = PageRequest.of(page, pageSize, Sort.by("shortlistedAt").descending());
		           } else {
		               pageable = PageRequest.of(page, pageSize, Sort.by("createdTime").descending());
		           }
		       }

		       // Check if no filters are provided and return all records
		       if (spec.equals(Specifications.where(null))) {
		           result = candidateService.getAllCandidates(pageable);
		       } else {
		           result = candidateService.findAll(spec, pageable);
		       }

		       if (result.isEmpty()) {
		           Map<String, Object> errorResponse = new HashMap<>();
		           errorResponse.put("code", 400);
		           errorResponse.put("message", "Not Found");
		           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		       }

		       Map<String, Object> successResponse = new HashMap<>();
		       successResponse.put("status", "success");
		       successResponse.put("message", "success");
		       successResponse.put("code", 200);
		       successResponse.put("data", result);
		       return ResponseEntity.ok(successResponse);
		   }


		   @PostMapping("/sendEmailInterview")
		    public ResponseEntity<Map<String, String>> sendEmail(
		            @RequestParam(required = false) String contactPersonName,
		            @RequestParam(required = false) String companyName,
		            @RequestParam(required = false) String emailId,
		            @RequestParam(required = false) String jobCategory,
		            @RequestParam(required = false) String day,
		            @RequestParam(required = false) String candidateNames,
		            @RequestParam(required = false) MultipartFile signature,
		            @RequestParam(required = false) MultipartFile dynamicVideoLink,
		            @RequestParam(required = false) MultipartFile dynamicGifLink) {

		        Map<String, String> response = new HashMap<>();

		        try {
		            // Other validations and logic

		            // Split candidate names into a list
		            List<String> namesList = Arrays.asList(candidateNames.split(","));

		            // Process each candidate separately
		            for (String candidateName : namesList) {
		                // Check if all required documents are null
		                if (signature == null && dynamicVideoLink == null && dynamicGifLink == null) {
		                    // Generate HTML content dynamically for each candidate
		                    String emailContent = generateEmailContent(contactPersonName, companyName, jobCategory,
		                            day, candidateName, signature, dynamicVideoLink, dynamicGifLink, emailId);

		                    // Send email for each candidate
		                    executeInterview(emailId, contactPersonName, jobCategory, day, candidateName, companyName,
		                            dynamicVideoLink, dynamicGifLink, signature, emailContent, null);

		                    // Log or handle success for each candidate
		                    System.out.println("Email sent successfully for candidate: " + candidateName);
		                } else {
		                    // Log or handle case where at least one document is present for the candidate
		                    System.out.println("Documents present for candidate: " + candidateName);
		                }
		            }

		            response.put("code", "200");
		            response.put("status", "success");
		            return ResponseEntity.ok(response);

		        } catch (Exception e) {
		            // Log the exception for debugging
		            e.printStackTrace();

		            // Handle exceptions
		            response.put("code", "500");
		            response.put("status", "error");
		            response.put("message", "An error occurred while processing the request");
		            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		        }
		    }

		    private String generateEmailContent(String contactPersonName, String companyName, String jobCategory,
		                                        String day, String candidateName, MultipartFile signature,
		                                        MultipartFile dynamicVideoLink, MultipartFile dynamicGifLink, String emailId)
		            throws IOException, TemplateException {

		        // Create a map with dynamic values
		        Map<String, Object> emailData = new HashMap<>();
		        emailData.put("ContactPersonName", contactPersonName);
		        emailData.put("company_name", companyName);
		        emailData.put("job_catagory", jobCategory);
		        emailData.put("Day", day);
		        emailData.put("candidate_name", candidateName);

		        // Get HTML content from FreeMarker template
		        String emailMessage = null;
				try {
					emailMessage = freeMarkerUtils.getHtml("EmployerInterviewDetails.html", emailData);
				} catch (java.io.IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TemplateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		        return emailMessage;
		    }

		    private void executeInterview(String emailId, String contactPersonName, String jobCategory,
		                                  String day, String candidateName, String companyName,
		                                  MultipartFile dynamicVideoLink, MultipartFile dynamicGifLink,
		                                  MultipartFile signature, String emailMessage, byte[] emailAttachmentBytes)
		            throws IOException {

		        if (emailId != null && !emailId.isEmpty()) {
		            // Send the email without attachments
		            try {
		                sendInterviewWithoutAttachment(emailId, contactPersonName, jobCategory, day, candidateName,
		                        companyName, emailMessage);
		            } catch (MessagingException e) {
		                // Handle the exception appropriately
		                e.printStackTrace();
		            }

		            // Log or handle success
		            System.out.println("Email sent successfully for candidate: " + candidateName);
		        } else {
		            // Handle case where emailId is empty or null
		        }
		    }

		    public void sendInterviewWithoutAttachment(String emailId, String contactPersonName, String jobCategory, String day,
			        String candidateName, String companyName, String emailMessage) throws MessagingException {

			    String fromEmailId = "sowmiya.g@taizo.in";
			    String ccEmail = "rahulsekar2000@gmail.com";
			    Session session = Session.getInstance(new Properties(System.getProperties()));
			    MimeMessage mimeMessage = new MimeMessage(session);

			    try {
			        mimeMessage.setSubject(companyName + " - Interview Scheduled", "UTF-8");
			        mimeMessage.setFrom(new InternetAddress(fromEmailId));
			        mimeMessage.setRecipients(Message.RecipientType.TO, emailId);

			        mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail));

			        // Create the email body part
			        MimeBodyPart emailBodyPart = new MimeBodyPart();
			        emailBodyPart.setContent(emailMessage, "text/html; charset=UTF-8");

			        // Create the multipart message
			        Multipart multipart = new MimeMultipart();
			        multipart.addBodyPart(emailBodyPart);

			        // Set the content of the MimeMessage to the multipart message
			        mimeMessage.setContent(multipart);

			        // Send the email
			        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			        mimeMessage.writeTo(outputStream);
			        RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
			        SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
			        sesClient.sendRawEmail(rawEmailRequest);

			        // Log or handle success
			        System.out.println("Email sent successfully for candidate: " + candidateName);

			    } catch (Exception ex) {
			        // Log or handle exceptions
			        ex.printStackTrace();
			    }
			}
		 

		   // Add the following method to generate PDF bytes from HTML content
		   private byte[] generatePdfBytesFromHtml(String htmlContent) {
		       // Implement the logic to convert HTML to PDF and return the byte array
		       // You can use libraries like iText, Flying Saucer, etc.
		       // Example using iText:
		       ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		       // Replace the following with your logic to convert HTML to PDF
		       // PdfWriter writer = new PdfWriter(outputStream);
		       // HtmlConverter.convertToPdf(htmlContent, writer);
		       return outputStream.toByteArray();
		   }
		   
		   @PutMapping(path = "/basicMidLevelSenior")
			public ResponseEntity<?> basicMidSenior(@RequestBody MidSeniorLevelCandidateLeadModel midSeniorLevel) {
			   String mobileNumber=midSeniorLevel.getMobileNumber();
			   String whatsappNumber=midSeniorLevel.getWhatsappNumber();
			   MidSeniorLevelCandidateLeadModel midSeniorLevelCandidateLeadModel=midSeniorLevelCandidateLeadRepository.findByMobileNumberAndWhatsappNumber(mobileNumber,whatsappNumber);
			   if(midSeniorLevelCandidateLeadModel==null) {
			   midSeniorLevel.setFirstName(midSeniorLevel.getFirstName()); 
			   midSeniorLevel.setLastName(midSeniorLevel.getLastName());
			   midSeniorLevel.setEmailId(midSeniorLevel.getEmailId());
			   midSeniorLevel.setMobileNumber(midSeniorLevel.getMobileNumber());
			   midSeniorLevel.setEducationalQualification(midSeniorLevel.getEducationalQualification());
			   midSeniorLevel.setPrefJobLocation(midSeniorLevel.getPrefJobLocation());
			   midSeniorLevel.setWhatsappNumber(midSeniorLevel.getWhatsappNumber());
			   midSeniorLevel.setAdminId(midSeniorLevel.getAdminId());
			   midSeniorLevel.setJobCategory(midSeniorLevel.getJobCategory());
			   midSeniorLevel.setExpInYears(midSeniorLevel.getExpInYears());
			   midSeniorLevel.setExpInMonths(midSeniorLevel.getExpInMonths());
			   midSeniorLevel.setNoticePeriod(midSeniorLevel.getNoticePeriod());
			   midSeniorLevel.setCurrentSalary(midSeniorLevel.getCurrentSalary());
			   midSeniorLevel.setExpectedSalary(midSeniorLevel.getExpectedSalary());
			   midSeniorLevel.setExpInManufacturing(midSeniorLevel.isExpInManufacturing());
			   midSeniorLevel.setCurrentlyWorking(midSeniorLevel.isCurrentlyWorking());
			   midSeniorLevelCandidateLeadRepository.save(midSeniorLevel);
			   
			   HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Register successfully");
				map.put("assignTo", midSeniorLevel);
				return new ResponseEntity<>(map, HttpStatus.OK); 
		   
		   } else {
				   midSeniorLevelCandidateLeadModel.setFirstName(midSeniorLevel.getFirstName());
				   midSeniorLevelCandidateLeadModel.setLastName(midSeniorLevel.getLastName());
				   midSeniorLevelCandidateLeadModel.setEmailId(midSeniorLevel.getEmailId());
				   midSeniorLevelCandidateLeadModel.setMobileNumber(midSeniorLevel.getMobileNumber());
				   midSeniorLevelCandidateLeadModel.setEducationalQualification(midSeniorLevel.getEducationalQualification());
				   midSeniorLevelCandidateLeadModel.setPrefJobLocation(midSeniorLevel.getPrefJobLocation());
				   midSeniorLevelCandidateLeadModel.setWhatsappNumber(midSeniorLevel.getWhatsappNumber());
				   midSeniorLevelCandidateLeadModel.setAdminId(midSeniorLevel.getAdminId());
				   midSeniorLevelCandidateLeadModel.setJobCategory(midSeniorLevel.getJobCategory());
				   midSeniorLevelCandidateLeadModel.setExpInYears(midSeniorLevel.getExpInYears());
				   midSeniorLevelCandidateLeadModel.setExpInMonths(midSeniorLevel.getExpInMonths());
				   midSeniorLevelCandidateLeadModel.setNoticePeriod(midSeniorLevel.getNoticePeriod());
				   midSeniorLevelCandidateLeadModel.setCurrentSalary(midSeniorLevel.getCurrentSalary());
				   midSeniorLevelCandidateLeadModel.setExpectedSalary(midSeniorLevel.getExpectedSalary());
				   midSeniorLevelCandidateLeadModel.setExpInManufacturing(midSeniorLevel.isExpInManufacturing());
				   midSeniorLevelCandidateLeadModel.setCurrentlyWorking(midSeniorLevel.isCurrentlyWorking());

		           midSeniorLevelCandidateLeadRepository.save(midSeniorLevelCandidateLeadModel);
			   }
			return null;
		   }
		   
		   @GetMapping("/midSeniorDetails")
		  public ResponseEntity<?>getMidsenior(@RequestParam(required=false) String mobileNumber){
			MidSeniorLevelCandidateLeadModel mn=midSeniorLevelCandidateLeadRepository.findByMobileNumber(mobileNumber);
			 HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Retrieve successfully");
				map.put("assignTo", mn);
				return new ResponseEntity<>(map, HttpStatus.OK); 
		   }
		   
		   @PutMapping("/reportGenerationMidSenior")
		   public ResponseEntity<?> basicMidSeniorReport(@RequestBody MidSeniorCandidateReportModel midSeniorCandidateReport, @RequestParam(required = false) long id) {
		       Optional<MidSeniorLevelCandidateLeadModel> midSeniorLevel = midSeniorLevelCandidateLeadRepository.findById(id);

		       List<MidSeniorCandidateReportModel> existingReports = midSeniorCandidateReportRepository.findByMidSeniorId(id);

		       if (midSeniorLevel.isPresent()) {
		           if (existingReports == null || existingReports.isEmpty()) {
		               // If no existing report is found, create a new one
		               midSeniorCandidateReport.setCandidateName(midSeniorCandidateReport.getCandidateName());
		               midSeniorCandidateReport.setTitles(midSeniorCandidateReport.getTitles());
		               midSeniorCandidateReport.setAge(midSeniorCandidateReport.getAge());
		               midSeniorCandidateReport.setLookingFor(midSeniorCandidateReport.getLookingFor());
		               midSeniorCandidateReport.setYearsOfExperience(midSeniorCandidateReport.getYearsOfExperience());
		               midSeniorCandidateReport.setPreviousDesignation(midSeniorCandidateReport.getPreviousDesignation());
		               midSeniorCandidateReport.setQualification(midSeniorCandidateReport.getQualification());
		               midSeniorCandidateReport.setCoreSkillSetMatchingJd(midSeniorCandidateReport.getCoreSkillSetMatchingJd());
		               midSeniorCandidateReport.setSkills(midSeniorCandidateReport.getSkills());
		               midSeniorCandidateReport.setCertifications(midSeniorCandidateReport.getCertifications());
		               midSeniorCandidateReport.setTaizoSuggestion(midSeniorCandidateReport.getTaizoSuggestion());
		               midSeniorCandidateReport.setTaizoScore(midSeniorCandidateReport.getTaizoScore());
		               midSeniorCandidateReport.setMidSeniorId(id);
		               midSeniorCandidateReport.setReport(true);
		               midSeniorCandidateReportRepository.save(midSeniorCandidateReport);
		               
		               // Set isReport to true in MidSeniorLevelCandidateLeadModel
		               MidSeniorLevelCandidateLeadModel levelModel = midSeniorLevel.get();
		               levelModel.setReport(true);
		               midSeniorLevelCandidateLeadRepository.save(levelModel);

		               HashMap<String, Object> map = new HashMap<>();
		               map.put("statuscode", 200);
		               map.put("message", "Register successfully");
		               map.put("assignTo", midSeniorCandidateReport);
		               return new ResponseEntity<>(map, HttpStatus.OK);
		           } else {
		               // If an existing report is found, update it
		               MidSeniorCandidateReportModel existingReport = existingReports.get(0);

		               // Update the fields based on the new data
		               if(midSeniorCandidateReport.getCandidateName()!=null) {
		               existingReport.setCandidateName(midSeniorCandidateReport.getCandidateName());
		               }
		               if(midSeniorCandidateReport.getTitles()!=null) {
		               existingReport.setTitles(midSeniorCandidateReport.getTitles());
		               }
		               if(midSeniorCandidateReport.getAge()!=0) {
		               existingReport.setAge(midSeniorCandidateReport.getAge());
		               }
		               if(midSeniorCandidateReport.getLookingFor()!=null) {
		               existingReport.setLookingFor(midSeniorCandidateReport.getLookingFor());
		               }
		               if(midSeniorCandidateReport.getYearsOfExperience()!=0) {
		               existingReport.setYearsOfExperience(midSeniorCandidateReport.getYearsOfExperience());
		               }
		               if(midSeniorCandidateReport.getPreviousDesignation()!=null) {
		               existingReport.setPreviousDesignation(midSeniorCandidateReport.getPreviousDesignation());
		               }
		               if(midSeniorCandidateReport.getQualification()!=null) {
		               existingReport.setQualification(midSeniorCandidateReport.getQualification());
		               }
		               if(midSeniorCandidateReport.getCoreSkillSetMatchingJd()!=null) {
		               existingReport.setCoreSkillSetMatchingJd(midSeniorCandidateReport.getCoreSkillSetMatchingJd());
		               }
		               if(midSeniorCandidateReport.getSkills()!=null) {
		               existingReport.setSkills(midSeniorCandidateReport.getSkills());
		               }
		               if(midSeniorCandidateReport.getCertifications()!=null) {
		               existingReport.setCertifications(midSeniorCandidateReport.getCertifications());
		               }
		               if(midSeniorCandidateReport.getTaizoSuggestion()!=null) {
		               existingReport.setTaizoSuggestion(midSeniorCandidateReport.getTaizoSuggestion());
		               }
		               if(midSeniorCandidateReport.getTaizoScore()!=null) {
		               existingReport.setTaizoScore(midSeniorCandidateReport.getTaizoScore());
		               }

		               midSeniorCandidateReportRepository.save(existingReport);
		               
		               

		               // Set isReport to true in MidSeniorLevelCandidateLeadModel
		               MidSeniorLevelCandidateLeadModel levelModel = midSeniorLevel.get();
		               levelModel.setReport(true);
		               midSeniorLevelCandidateLeadRepository.save(levelModel);


		               HashMap<String, Object> map = new HashMap<>();
		               map.put("statuscode", 200);
		               map.put("message", "Update successful");
		               map.put("updatedReport", existingReport);
		               return new ResponseEntity<>(map, HttpStatus.OK);
		           }
		       } else {
		           HashMap<String, Object> map = new HashMap<>();
		           map.put("statuscode", 404);
		           map.put("message", "Candidate lead not found");
		           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
		       }
		   }

		   
		   @GetMapping("/reportGenerationDetails")
		   public ResponseEntity<?>getReportGeneration(@RequestParam (required=false) long id){
			   List<MidSeniorCandidateReportModel> details=midSeniorCandidateReportRepository.findByMidSeniorId(id);
			   HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Retreive successfully");
				map.put("MidSeniorCandidateReport", details);
				return new ResponseEntity<>(map, HttpStatus.OK); 
		   }
		   
		   public class CandidateFilterResponses {
			    private long totalCount;
			    private List<Map<String, Object>> candidateList;
			    public CandidateFilterResponses(long totalCount, List<Map<String, Object>> candidateList) {
			        this.totalCount = totalCount;
			        this.candidateList = candidateList;
			    }
			    public long getTotalCount() {
			        return totalCount;
			    }
			    public List<Map<String, Object>> getCandidateList() {
			        return candidateList;
			    }
			}
			
			@PostMapping("/candidateJoined")
			public ResponseEntity<CandidateFilterResponses> filterCandidateJoined(@RequestBody CanInterviewsModel candidatemod) {
				List<Map<String, Object>> candidate = candidateService.filtercandidates(candidatemod.getCompanyName(),candidatemod.getAdminId(),candidatemod.getContactNumber(),candidatemod.getCreatedTime(),candidatemod.getEndDate(),candidatemod.getPage(),candidatemod.getSize());
				long totalCount = candidateService.filterCandidatesCount(candidatemod.getCompanyName(),candidatemod.getAdminId(),candidatemod.getContactNumber(),candidatemod.getCreatedTime(),candidatemod.getEndDate());
				  CandidateFilterResponses response = new CandidateFilterResponses(totalCount, candidate);
				    return ResponseEntity.ok(response);
			}
			
			public class MidSeniorReportResponse{
				private long totalCount;
				private List<Map<String,Object>> MidSeniorReportList;
				
				public MidSeniorReportResponse(long totalCount,List<Map<String,Object>> MidSeniorReportList) {
					this.totalCount=totalCount;
					this.MidSeniorReportList=MidSeniorReportList;
				}
				 public long getTotalCount() {
				        return totalCount;
				    }
				    public List<Map<String, Object>> getMidSeniorReportList() {
				        return MidSeniorReportList;
				    }
			}
			
			@PostMapping("/midSeniorReport")
			public ResponseEntity<MidSeniorReportResponse> filterMidSeniorReport(@RequestBody MidSeniorCandidateReportModel candidatemod) {
				List<Map<String, Object>> candidate = candidateService.filterMidSenior(candidatemod.getPage(),candidatemod.getSize());
				long totalCount = candidateService.filterMidSeniorCount();
				MidSeniorReportResponse response = new MidSeniorReportResponse(totalCount, candidate);
				    return ResponseEntity.ok(response);
			}
			
			 @PostMapping("/moveToScreening")
			    public ResponseEntity<?> moveToScreening(@RequestParam(required = false) long id,
			    		                                 @RequestParam String screeningDate,
			    		                                 @RequestParam String screeningTime,
			    		                                 @RequestParam String meetingLink) {
			        HashMap<String, Object> map = new HashMap<>();

			        try {
			            Optional<MidSeniorLevelCandidateLeadModel> optionalModel = midSeniorLevelCandidateLeadRepository.findById(id);
			            if (optionalModel.isPresent()) {
			                MidSeniorLevelCandidateLeadModel midSeniorModel = optionalModel.get();
			                midSeniorModel.setStatus(1);
			                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			                midSeniorModel.setScreeningAt(LocalDateTime.now().format(formatter));
			                midSeniorModel.setQualified(true);
			                midSeniorModel.setScreeningDate(screeningDate);
			                midSeniorModel.setScreeningTime(screeningTime);
			                midSeniorModel.setMeetingLink(meetingLink);
			                midSeniorLevelCandidateLeadRepository.save(midSeniorModel);

			                map.put("code", 200);
			                map.put("message", "Updated Successfully");
			                return new ResponseEntity<>(map, HttpStatus.OK);
			            } else {
			                map.put("code", 400);
			                map.put("message", "ID not found");
			                return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			            }
			        } catch (Exception e) {
			            map.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
			            map.put("message", "Internal Server Error");
			            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
			        }
			    }
			 
			 @PostMapping("/moveToShortlisted")
			    public ResponseEntity<?> moveToShortlisted(@RequestParam(required = false) long id) {
			        HashMap<String, Object> map = new HashMap<>();

			        try {
			            Optional<MidSeniorLevelCandidateLeadModel> optionalModel = midSeniorLevelCandidateLeadRepository.findById(id);
			            MidSeniorLevelCandidateLeadModel midSeniorModel = optionalModel.get();
			            int status=midSeniorModel.getStatus();
			            if (optionalModel.isPresent() && status==1){
			                midSeniorModel.setStatus(2);
			                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			                midSeniorModel.setShortlistedAt(LocalDateTime.now().format(formatter));
			                midSeniorModel.setQualified(true);
			                midSeniorLevelCandidateLeadRepository.save(midSeniorModel);

			                map.put("code", 200);
			                map.put("message", "Updated Successfully");
			                return new ResponseEntity<>(map, HttpStatus.OK);
			            } else {
			                map.put("code", 400);
			                map.put("message", "ID not found or screening not moved");
			                return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
			            }
			        } catch (Exception e) {
			            map.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
			            map.put("message", "Internal Server Error");
			            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
			        }
			    }

			 @PutMapping("/seenStatus")
			 public ResponseEntity<?> updateSeenStatus(@RequestParam Long id, @RequestParam boolean isSeen) {
			     HashMap<String, String> response = new HashMap<>();

			     try {
			         Optional<FacebookMetaLead> facebookMetaLead = facebookMetaLeadRepository.findById(id);

			         if (facebookMetaLead.isPresent()) {
			             FacebookMetaLead fb = facebookMetaLead.get();
			             fb.setSeen(isSeen);
			             facebookMetaLeadRepository.save(fb);

			             response.put("code", "200");
			             response.put("status", "Status Updated");
			             return new ResponseEntity<>(response, HttpStatus.OK);
			         } else {
			             response.put("code", "404");
			             response.put("status", "Lead not found");
			             return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			         }
			     } catch (Exception e) {
			         response.put("code", "400");
			         response.put("status", "Error while Uploading status");
			         return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			     }
			 }

			  @PostMapping("/qualifiedCandidate")
			  public ResponseEntity<?> qualifiedCandidate(@RequestBody CandidateQualifiedModel candidateQualifiedModel,
					  @RequestParam(required=false) long mn,@RequestParam(required=false) String qualified,
					  @RequestParam(required=false) Long adminId){
				  HashMap<String, Object> map = new HashMap<>();
				  FacebookMetaLead fb = facebookMetaLeadRepository.findByMobileNumber(String.valueOf(mn));
				  CanLeadModel canLead = canLeadRepository.findByMobileNumber(Long.parseLong(String.valueOf(mn)));
				  CandidateModel can = candidateRepository.findByMobileNumber(Long.parseLong(String.valueOf(mn)));
				  Long facebookId = (fb != null) ? fb.getId() : null;
				boolean active = fb != null && fb.isInActive();
				long candidateLeadId = (canLead != null) ? canLead.getId() : 0;
				long candidateId=(can !=null)?can.getId():0;
				if(fb !=null && !active || canLead!=null || can!=null) {
				  try {
					 candidateQualifiedModel.setAppliedJobrole(candidateQualifiedModel.getAppliedJobrole());
					 candidateQualifiedModel.setExperienced(candidateQualifiedModel.isExperienced());
					 candidateQualifiedModel.setCurrentlyWorking(candidateQualifiedModel.isCurrentlyWorking());
					 if(!candidateQualifiedModel.isExperienced()) {
					     candidateQualifiedModel.setPreferredJobLocation(candidateQualifiedModel.getPreferredJobLocation());
					        candidateQualifiedModel.setHavingJobLocation(candidateQualifiedModel.isHavingJobLocation());
					        candidateQualifiedModel.setCanSuitableJobLocation(candidateQualifiedModel.getCanSuitableJobLocation());
					        candidateQualifiedModel.setEducation(candidateQualifiedModel.getEducation());
					        candidateQualifiedModel.setSpecialization(candidateQualifiedModel.getSpecialization());
					        candidateQualifiedModel.setIsMechanicalRelatedDegree(candidateQualifiedModel.getIsMechanicalRelatedDegree());
					        candidateQualifiedModel.setIsCourseCompleted(candidateQualifiedModel.getIsCourseCompleted());
					        candidateQualifiedModel.setSkillsCertifications(candidateQualifiedModel.getSkillsCertifications());
					        candidateQualifiedModel.setCurrentCandidateLocation(candidateQualifiedModel.getCurrentCandidateLocation());
					        candidateQualifiedModel.setCurrentStayType(candidateQualifiedModel.getCurrentStayType());
					        candidateQualifiedModel.setReadyToRelocate(candidateQualifiedModel.isReadyToRelocate());
					        candidateQualifiedModel.setExpectedSalary(candidateQualifiedModel.getExpectedSalary());
					        candidateQualifiedModel.setSalaryExpectationAdminPreference(candidateQualifiedModel.getSalaryExpectationAdminPreference());
					        candidateQualifiedModel.setWorkForSuggestedSalary(candidateQualifiedModel.isWorkForSuggestedSalary());
					        candidateQualifiedModel.setReadyForShifts(candidateQualifiedModel.isReadyForShifts());
					        candidateQualifiedModel.setNeedAccommodation(candidateQualifiedModel.isNeedAccommodation());
					        candidateQualifiedModel.setNeedTransport(candidateQualifiedModel.isNeedTransport());
					        candidateQualifiedModel.setHavingUpdatedCV(candidateQualifiedModel.isHavingUpdatedCV());
					        if(fb !=null && !active) {
					        	candidateQualifiedModel.setFbMetaLeadId(facebookId);
					        }
					        else if(canLead !=null) {
					        	candidateQualifiedModel.setCanLeadId(candidateLeadId);
					        }
					        else if(can !=null) {
					        	candidateQualifiedModel.setCandidateId(candidateId);
					        }
					        candidateQualifiedRepository.save(candidateQualifiedModel);
					        if(fb !=null && !active) {
					        	fb.setQualified(true);
								fb.setNotQualified(false);
								fb.setInActive(true);
								facebookMetaLeadRepository.save(fb);
								
						
								CanLeadModel canLeadModel = new CanLeadModel();
								canLeadModel.setMobileNumber(Long.parseLong(fb.getMobileNumber()));
								canLeadModel.setWhatsappNumber(Long.parseLong(fb.getWhatsappNumber()));
								canLeadModel.setName(fb.getCandidateName());
								canLeadModel.setQualification(fb.getEducationQualification());
								canLeadModel.setJobCategory(fb.getJobCategory());
								canLeadModel.setQualified(true);
								canLeadModel.setFromFbMetaLeadAd(true);
								canLeadModel.setIndustry(fb.getIndustry());
								canLeadModel.setCurrentlyworking(fb.isCurrentlyWorking());
								canLeadModel.setJoiningAvailability(fb.getJoiningAvailability());
								if (adminId != null) {
									canLeadModel.setAssignTo(Math.toIntExact(adminId));
								}
								canLeadModel.setPrefLocation(fb.getPreferredLocation()!= null && !fb.getPreferredLocation().isEmpty() ? fb.getPreferredLocation() : fb.getCandidatePreferredLocation());
								canLeadRepository.save(canLeadModel);
								// Step 1: Delete record from facebookMetaLeadRepository
							//	facebookMetaLeadRepository.deleteById(id);
								// Step 2: Find relevant records in candidateTimeLineRepository
								List<CandidateTimeLine> timelineRecords = candidateTimeLineRepository.findByFacebookId(facebookId);
								// Step 3: Update canLeadId in the found records
								for (CandidateTimeLine timelineRecord : timelineRecords) {
								    timelineRecord.setCanLeadId(canLeadModel.getId());
								    timelineRecord.setFacebookId(0L);
								    timelineRecord.setCanId(0);
								}
								// Step 4: Save the updated records in candidateTimeLineRepository
								candidateTimeLineRepository.saveAll(timelineRecords);
								Admin a = adminRepository.findById(adminId).get();
								CandidateTimeLine candidateTimeLine=new CandidateTimeLine();
								Date currentDates = new Date();
								SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
								String formattedDate = dateFormat.format(currentDates);
								candidateTimeLine.setFacebookId(0L);
								candidateTimeLine.setEventName("Qualified");
								candidateTimeLine.setEventDescription("Qualified by " +a.getUserName() + "</b> on " + formattedDate);
								candidateTimeLine.setCanId(0);
								candidateTimeLine.setCanLeadId(canLeadModel.getId());
								candidateTimeLineRepository.save(candidateTimeLine);
								candidateQualifiedModel.setCanLeadId(canLeadModel.getId());  
								candidateQualifiedRepository.save(candidateQualifiedModel);
								
					        }
					        else if(canLead !=null) {
					        	CanLeadModel canLeadModel = new CanLeadModel();
					        	Admin a = adminRepository.findById(adminId).get();
								CandidateTimeLine candidateTimeLine=new CandidateTimeLine();
								Date currentDates = new Date();
								SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
								String formattedDate = dateFormat.format(currentDates);
							    	canLeadModel.setAssignTo(Integer.parseInt(String.valueOf(adminId)));
									canLeadModel.setQualified(true);
									canLeadModel.setNotQualified(false);
									canLeadRepository.save(canLeadModel);
									
									candidateTimeLine.setCanLeadId(canLeadModel.getId());
									candidateTimeLine.setEventName("Qualified");
									candidateTimeLine.setEventDescription("Qualified By" +" "+a.getUserName() + "</b> on " + formattedDate);
									candidateTimeLine.setCanId(0);
									candidateTimeLine.setFacebookId(0L);
									candidateTimeLineRepository.save(candidateTimeLine);
					        }
					        
					          
					        map.put("statuscode", 200);
				               map.put("message", "created successfully for fresher candidate");
				               map.put("updatedReport", candidateQualifiedModel);
				               return new ResponseEntity<>(map, HttpStatus.OK);
					 }
					 else if(candidateQualifiedModel.isExperienced() && candidateQualifiedModel.isCurrentlyWorking()) {
						 candidateQualifiedModel.setJobrole(candidateQualifiedModel.getJobrole());
					        candidateQualifiedModel.setIndustry(candidateQualifiedModel.getIndustry());
					        candidateQualifiedModel.setExperienceInMonth(candidateQualifiedModel.getExperienceInMonth());
					        candidateQualifiedModel.setExperienceInYear(candidateQualifiedModel.getExperienceInYear());
					        candidateQualifiedModel.setOverallExperience(candidateQualifiedModel.isOverallExperience());
					        candidateQualifiedModel.setSkillsCertifications(candidateQualifiedModel.getSkillsCertifications());
					        candidateQualifiedModel.setCompanyName(candidateQualifiedModel.getCompanyName());
					        candidateQualifiedModel.setCompanyLocation(candidateQualifiedModel.getCompanyLocation());
					        candidateQualifiedModel.setJobType(candidateQualifiedModel.getJobType());
					        candidateQualifiedModel.setJobWorkHours(candidateQualifiedModel.getJobWorkHours());
					        candidateQualifiedModel.setNoticePeriod(candidateQualifiedModel.getNoticePeriod());
					        candidateQualifiedModel.setImmediateJoiner(candidateQualifiedModel.isImmediateJoiner());
					        candidateQualifiedModel.setPreferredJobLocation(candidateQualifiedModel.getPreferredJobLocation());
					        candidateQualifiedModel.setHavingJobLocation(candidateQualifiedModel.isHavingJobLocation());
					        candidateQualifiedModel.setCanSuitableJobLocation(candidateQualifiedModel.getCanSuitableJobLocation());
					        candidateQualifiedModel.setEducation(candidateQualifiedModel.getEducation());
					        candidateQualifiedModel.setSpecialization(candidateQualifiedModel.getSpecialization());
					        candidateQualifiedModel.setIsMechanicalRelatedDegree(candidateQualifiedModel.getIsMechanicalRelatedDegree());
					        candidateQualifiedModel.setIsCourseCompleted(candidateQualifiedModel.getIsCourseCompleted());
					        candidateQualifiedModel.setReasonForJobChange(candidateQualifiedModel.getReasonForJobChange());
					        candidateQualifiedModel.setCurrentCandidateLocation(candidateQualifiedModel.getCurrentCandidateLocation());
					        candidateQualifiedModel.setCurrentStayType(candidateQualifiedModel.getCurrentStayType());
					        candidateQualifiedModel.setReadyToRelocate(candidateQualifiedModel.isReadyToRelocate());
					        candidateQualifiedModel.setTakeHomeSalary(candidateQualifiedModel.getTakeHomeSalary());
					        candidateQualifiedModel.setAdminSuggestedSalary(candidateQualifiedModel.getAdminSuggestedSalary());
					        candidateQualifiedModel.setHavingSalaryProof(candidateQualifiedModel.isHavingSalaryProof());
					        candidateQualifiedModel.setSalaryProofDocumentType(candidateQualifiedModel.getSalaryProofDocumentType());
					        candidateQualifiedModel.setExpectedSalary(candidateQualifiedModel.getExpectedSalary());
					        candidateQualifiedModel.setSalaryExpectationAdminPreference(candidateQualifiedModel.getSalaryExpectationAdminPreference());
					        candidateQualifiedModel.setWorkForSuggestedSalary(candidateQualifiedModel.isWorkForSuggestedSalary());
					        candidateQualifiedModel.setReadyForShifts(candidateQualifiedModel.isReadyForShifts());
					        candidateQualifiedModel.setNeedAccommodation(candidateQualifiedModel.isNeedAccommodation());
					        candidateQualifiedModel.setNeedTransport(candidateQualifiedModel.isNeedTransport());
					        candidateQualifiedModel.setHavingUpdatedCV(candidateQualifiedModel.isHavingUpdatedCV());
					        if(fb !=null && !active) {
					        	candidateQualifiedModel.setFbMetaLeadId(facebookId);
					        }
					        else if(canLead !=null) {
					        	candidateQualifiedModel.setCanLeadId(candidateLeadId);
					        }
					        else if(can !=null) {
					        	candidateQualifiedModel.setCandidateId(candidateId);
					        }
					        candidateQualifiedRepository.save(candidateQualifiedModel);
					        
					        if(fb !=null && !active) {
					        	fb.setQualified(true);
								fb.setNotQualified(false);
								fb.setInActive(true);
								facebookMetaLeadRepository.save(fb);
								
								CanLeadModel canLeadModel = new CanLeadModel();
								canLeadModel.setMobileNumber(Long.parseLong(fb.getMobileNumber()));
								canLeadModel.setWhatsappNumber(Long.parseLong(fb.getWhatsappNumber()));
								canLeadModel.setName(fb.getCandidateName());
								canLeadModel.setQualification(fb.getEducationQualification());
								canLeadModel.setJobCategory(fb.getJobCategory());
								canLeadModel.setQualified(true);
								canLeadModel.setFromFbMetaLeadAd(true);
								canLeadModel.setIndustry(fb.getIndustry());
								canLeadModel.setCurrentlyworking(fb.isCurrentlyWorking());
								canLeadModel.setJoiningAvailability(fb.getJoiningAvailability());
								if (adminId != null) {
									canLeadModel.setAssignTo(Math.toIntExact(adminId));
								}
								canLeadModel.setPrefLocation(fb.getPreferredLocation()!= null && !fb.getPreferredLocation().isEmpty() ? fb.getPreferredLocation() : fb.getCandidatePreferredLocation());
								canLeadRepository.save(canLeadModel);
								// Step 1: Delete record from facebookMetaLeadRepository
							//	facebookMetaLeadRepository.deleteById(id);
								// Step 2: Find relevant records in candidateTimeLineRepository
								List<CandidateTimeLine> timelineRecords = candidateTimeLineRepository.findByFacebookId(facebookId);
								// Step 3: Update canLeadId in the found records
								for (CandidateTimeLine timelineRecord : timelineRecords) {
								    timelineRecord.setCanLeadId(canLeadModel.getId());
								    timelineRecord.setFacebookId(0L);
								    timelineRecord.setCanId(0);
								}
								// Step 4: Save the updated records in candidateTimeLineRepository
								candidateTimeLineRepository.saveAll(timelineRecords);
								Admin a = adminRepository.findById(adminId).get();
								CandidateTimeLine candidateTimeLine=new CandidateTimeLine();
								Date currentDates = new Date();
								SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
								String formattedDate = dateFormat.format(currentDates);
								candidateTimeLine.setFacebookId(0L);
								candidateTimeLine.setEventName("Qualified");
								candidateTimeLine.setEventDescription("Qualified by " +a.getUserName() + "</b> on " + formattedDate);
								candidateTimeLine.setCanId(0);
								candidateTimeLine.setCanLeadId(canLeadModel.getId());
								candidateTimeLineRepository.save(candidateTimeLine);
								candidateQualifiedModel.setCanLeadId(canLeadModel.getId());  
								candidateQualifiedRepository.save(candidateQualifiedModel);
								
					        }
					        else if(canLead !=null) {
					        	CanLeadModel canLeadModel = new CanLeadModel();
					        	Admin a = adminRepository.findById(adminId).get();
								CandidateTimeLine candidateTimeLine=new CandidateTimeLine();
								Date currentDates = new Date();
								SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
								String formattedDate = dateFormat.format(currentDates);
									canLeadModel.setQualified(true);
									canLeadModel.setNotQualified(false);
									canLeadRepository.save(canLeadModel);
									
									candidateTimeLine.setCanLeadId(canLeadModel.getId());
									candidateTimeLine.setEventName("Qualified");
									candidateTimeLine.setEventDescription("Qualified By" +" "+a.getUserName() + "</b> on " + formattedDate);
									candidateTimeLine.setCanId(0);
									candidateTimeLine.setFacebookId(0L);
									candidateTimeLineRepository.save(candidateTimeLine);
					        }
					        
					        map.put("statuscode", 200);
				               map.put("message", "created successfully for Working Candidate");
				               map.put("updatedReport", candidateQualifiedModel);
				               return new ResponseEntity<>(map, HttpStatus.OK);
					 }
					 else if(candidateQualifiedModel.isExperienced() && !candidateQualifiedModel.isCurrentlyWorking()) {
						 candidateQualifiedModel.setJobrole(candidateQualifiedModel.getJobrole());
						 candidateQualifiedModel.setIndustry(candidateQualifiedModel.getIndustry());
					        candidateQualifiedModel.setExperienceInMonth(candidateQualifiedModel.getExperienceInMonth());
					        candidateQualifiedModel.setExperienceInYear(candidateQualifiedModel.getExperienceInYear());
					        candidateQualifiedModel.setOverallExperience(candidateQualifiedModel.isOverallExperience());
					        candidateQualifiedModel.setSkillsCertifications(candidateQualifiedModel.getSkillsCertifications());
					        candidateQualifiedModel.setCompanyName(candidateQualifiedModel.getCompanyName());
					        candidateQualifiedModel.setCompanyLocation(candidateQualifiedModel.getCompanyLocation());
					        candidateQualifiedModel.setJobType(candidateQualifiedModel.getJobType());
					        candidateQualifiedModel.setJobWorkHours(candidateQualifiedModel.getJobWorkHours());
					        candidateQualifiedModel.setNoticePeriod(candidateQualifiedModel.getNoticePeriod());
					        candidateQualifiedModel.setImmediateJoiner(candidateQualifiedModel.isImmediateJoiner());
					        candidateQualifiedModel.setPreferredJobLocation(candidateQualifiedModel.getPreferredJobLocation());
					        candidateQualifiedModel.setHavingJobLocation(candidateQualifiedModel.isHavingJobLocation());
					        candidateQualifiedModel.setCanSuitableJobLocation(candidateQualifiedModel.getCanSuitableJobLocation());
					        candidateQualifiedModel.setEducation(candidateQualifiedModel.getEducation());
					        candidateQualifiedModel.setSpecialization(candidateQualifiedModel.getSpecialization());
					        candidateQualifiedModel.setIsMechanicalRelatedDegree(candidateQualifiedModel.getIsMechanicalRelatedDegree());
					        candidateQualifiedModel.setIsCourseCompleted(candidateQualifiedModel.getIsCourseCompleted());
					        candidateQualifiedModel.setReasonForJobChange(candidateQualifiedModel.getReasonForJobChange());
					        candidateQualifiedModel.setCurrentCandidateLocation(candidateQualifiedModel.getCurrentCandidateLocation());
					        candidateQualifiedModel.setCurrentStayType(candidateQualifiedModel.getCurrentStayType());
					        candidateQualifiedModel.setReadyToRelocate(candidateQualifiedModel.isReadyToRelocate());
					        candidateQualifiedModel.setTakeHomeSalary(candidateQualifiedModel.getTakeHomeSalary());
					        candidateQualifiedModel.setAdminSuggestedSalary(candidateQualifiedModel.getAdminSuggestedSalary());
					        candidateQualifiedModel.setHavingSalaryProof(candidateQualifiedModel.isHavingSalaryProof());
					        candidateQualifiedModel.setSalaryProofDocumentType(candidateQualifiedModel.getSalaryProofDocumentType());
					        candidateQualifiedModel.setExpectedSalary(candidateQualifiedModel.getExpectedSalary());
					        candidateQualifiedModel.setSalaryExpectationAdminPreference(candidateQualifiedModel.getSalaryExpectationAdminPreference());
					        candidateQualifiedModel.setWorkForSuggestedSalary(candidateQualifiedModel.isWorkForSuggestedSalary());
					        candidateQualifiedModel.setReadyForShifts(candidateQualifiedModel.isReadyForShifts());
					        candidateQualifiedModel.setNeedAccommodation(candidateQualifiedModel.isNeedAccommodation());
					        candidateQualifiedModel.setNeedTransport(candidateQualifiedModel.isNeedTransport());
					        candidateQualifiedModel.setHavingUpdatedCV(candidateQualifiedModel.isHavingUpdatedCV());
					        if(fb !=null && !active) {
					        	candidateQualifiedModel.setFbMetaLeadId(facebookId);
					        }
					        else if(canLead !=null) {
					        	candidateQualifiedModel.setCanLeadId(candidateLeadId);
					        }
					        else if(can !=null) {
					        	candidateQualifiedModel.setCandidateId(candidateId);
					        }
					        candidateQualifiedRepository.save(candidateQualifiedModel);
					        
					        if(fb !=null && !active) {
					        	fb.setQualified(true);
								fb.setNotQualified(false);
								fb.setInActive(true);
								facebookMetaLeadRepository.save(fb);
								
								CanLeadModel canLeadModel = new CanLeadModel();
								canLeadModel.setMobileNumber(Long.parseLong(fb.getMobileNumber()));
								canLeadModel.setWhatsappNumber(Long.parseLong(fb.getWhatsappNumber()));
								canLeadModel.setName(fb.getCandidateName());
								canLeadModel.setQualification(fb.getEducationQualification());
								canLeadModel.setJobCategory(fb.getJobCategory());
								canLeadModel.setQualified(true);
								canLeadModel.setFromFbMetaLeadAd(true);
								canLeadModel.setIndustry(fb.getIndustry());
								canLeadModel.setCurrentlyworking(fb.isCurrentlyWorking());
								canLeadModel.setJoiningAvailability(fb.getJoiningAvailability());
								if (adminId != null) {
									canLeadModel.setAssignTo(Math.toIntExact(adminId));
								}
								canLeadModel.setPrefLocation(fb.getPreferredLocation()!= null && !fb.getPreferredLocation().isEmpty() ? fb.getPreferredLocation() : fb.getCandidatePreferredLocation());
								canLeadRepository.save(canLeadModel);
								// Step 1: Delete record from facebookMetaLeadRepository
							//	facebookMetaLeadRepository.deleteById(id);
								// Step 2: Find relevant records in candidateTimeLineRepository
								List<CandidateTimeLine> timelineRecords = candidateTimeLineRepository.findByFacebookId(facebookId);
								// Step 3: Update canLeadId in the found records
								for (CandidateTimeLine timelineRecord : timelineRecords) {
								    timelineRecord.setCanLeadId(canLeadModel.getId());
								    timelineRecord.setFacebookId(0L);
								    timelineRecord.setCanId(0);
								}
								// Step 4: Save the updated records in candidateTimeLineRepository
								candidateTimeLineRepository.saveAll(timelineRecords);
								Admin a = adminRepository.findById(adminId).get();
								CandidateTimeLine candidateTimeLine=new CandidateTimeLine();
								Date currentDates = new Date();
								SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
								String formattedDate = dateFormat.format(currentDates);
								candidateTimeLine.setFacebookId(0L);
								candidateTimeLine.setEventName("Qualified");
								candidateTimeLine.setEventDescription("Qualified by " +a.getUserName() + "</b> on " + formattedDate);
								candidateTimeLine.setCanId(0);
								candidateTimeLine.setCanLeadId(canLeadModel.getId());
								candidateTimeLineRepository.save(candidateTimeLine);
								candidateQualifiedModel.setCanLeadId(canLeadModel.getId());  
								candidateQualifiedRepository.save(candidateQualifiedModel);
								
					        }
					        else if(canLead !=null) {
					        	CanLeadModel canLeadModel = new CanLeadModel();
					        	Admin a = adminRepository.findById(adminId).get();
								CandidateTimeLine candidateTimeLine=new CandidateTimeLine();
								Date currentDates = new Date();
								SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
								String formattedDate = dateFormat.format(currentDates);
									canLeadModel.setQualified(true);
									canLeadModel.setNotQualified(false);
									canLeadRepository.save(canLeadModel);
									
									candidateTimeLine.setCanLeadId(canLeadModel.getId());
									candidateTimeLine.setEventName("Qualified");
									candidateTimeLine.setEventDescription("Qualified By" +" "+a.getUserName() + "</b> on " + formattedDate);
									candidateTimeLine.setCanId(0);
									candidateTimeLine.setFacebookId(0L);
									candidateTimeLineRepository.save(candidateTimeLine);
					        }
					        
					        map.put("statuscode", 200);
				               map.put("message", "created successfully for not Working Candidate");
				               map.put("updatedReport", candidateQualifiedModel);
				               return new ResponseEntity<>(map, HttpStatus.OK);
					 }
				  
					 else {
				            // Handle the case when none of the conditions are met
				            map.put("statuscode", 400);
				            map.put("message", "Invalid input");
				            return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				        }
				  }
				  catch (Exception e) {
				        // Handle any exceptions that may occur
				        map.put("statuscode", 500);
				        map.put("message", "Internal Server Error");
				        return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
				    }
				}
				return null;
				  
			  }

  
  @GetMapping("/canlendlyDetails")
  public ResponseEntity<?> CanlendlyDetails(@RequestParam String emailId) {
      List<CalendlyMettingModel> calendlyList = calendlyMettingRepository.findByUserEmailOrderByCreatedTimeDesc(emailId);
      HashMap<String, Object> map = new HashMap<>();

      if (!calendlyList.isEmpty()) {
          map.put("statuscode", 200);
          map.put("message", "Retrieve Successfully");
          map.put("data", calendlyList);
          return new ResponseEntity<>(map, HttpStatus.OK);
      } else {
          map.put("statuscode", 404); // 404 indicates "Not Found"
          map.put("message", "Data not found for the specified email");
          return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
      }
  }
  
  @GetMapping("/educationalDetails")
  public ResponseEntity<List<CfgEducationalModel>> getAllEducationalModels() {
      List<CfgEducationalModel> educationalModels = cfgEducationalRepository.findAll();
      if (educationalModels.isEmpty()) {
          return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
      } else {
          return ResponseEntity.ok(educationalModels);
      }
  }

			
}