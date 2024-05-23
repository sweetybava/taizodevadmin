package com.taizo.controller.gallabox;

import java.awt.geom.Area;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.taizo.model.*;
import com.taizo.repository.*;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AbstractPutObjectRequest;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.itextpdf.io.exceptions.IOException;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.service.CandidateService;
import com.taizo.service.CloudWatchLogService;
import com.taizo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin
@RequestMapping("/chatbot")
public class GallaboxController {

	@Autowired
	CfgStateRepository cfgStateRepository;
	
	@Autowired
	AdminCallNotiRepository adminCallNotiRepository;
	
	@Autowired
	CfgCanAdminCityGroupingRepository cfgCanAdminCityGroupingRepository;
	
	@Autowired
	CanDocumentsRepository canDocumentsRepository;
	
	@Autowired
	FacebookMetaLeadRepository facebookMetaLeadRepository;
	
	@Autowired
	CfgCanAdminAreaRepository cfgCanAdminAreaRepository;

	@Autowired
	JobIndustryRepository jobIndustryRepository;

	@Autowired
	IStateCityRepository istateCityRepository;

	@Autowired
	JobRolesRepository jobRolesRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	LeadRepository leadRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	CandidateService candidateService;

	@Autowired
	CityRepository cityRepository;

	@Autowired
	IndiaStateRepository indiaStateRepository;

	@Autowired
	IndustryRepository industryRepository;

	@Autowired
	CandidateTimeLineRepository candidateTimeLineRepository;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	private UserService userService;

	@Autowired
	FullTimeGroupingRepository fullTimeGroupingRepository;

	@Autowired
	CanLanguagesRepository canLanguagesRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	CanLeadRepository canLeadRepository;

	@Autowired
	ExotelCallController exotelCallController;
	
	@Autowired
	CfgCanDocumentsRepository cfgCanDocumentsRepository;

	@Autowired
	CloudWatchLogService cloudWatchLogService; 
	
	 @PersistenceContext
	 private EntityManager entityManager;

	@Value("${firebase.js.api.key}")
	private String firebaseJSApiKey;
	
	@Value("${aws.access.key.id}")
	private String accessKey;
	@Value("${aws.access.key.secret}")
	private String awssecretKey;
	@Value("${aws.s3.audio.bucket}")
	private String bucketName;
	
	private AmazonS3 s3client;

	@Value("${firebase.deeplink.js.hostname}")
	private String firebaseJSHost;

	@Value("${firebase.jsapp.package.name}")
	private String firebaseJSPackage;

	@Value("${gallabox.campaign.url}")
	private String campaignUrl;
	@Value("${gallabox.auth.apiKey}")
	private String apiKey;
	@Value("${gallabox.auth.apiSecret}")
	private String apiSecret;
	@Value("${aws.user.resume.endpointUrl}")
	private String resumefolder;
	
	@Value("${aws.s3.bucket.user.resumes.folder}")
	private String folder;

	private Object candidate;
	
	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.awssecretKey);
		this.s3client = new AmazonS3Client(credentials);
	}

	@GetMapping(path = "/cities")
	public ResponseEntity<?> getCities(@RequestParam("phone_number") final String mobileNumber,
									   @RequestParam("state_id") final int stateId) {

		List<CfgStateCityModel> details = istateCityRepository.findByStateId(stateId, true);

		if (!details.isEmpty()) {

			long mn = Long.parseLong(mobileNumber);
			CandidateModel can = candidateRepository.findByMobileNumber(mn);
			CanLeadModel lead = canLeadRepository.findByMobileNumber(mn);
			if (can != null) {
				String cities = can.getCity();
				if (cities != null && !cities.isEmpty()) {
					String[] res = cities.split("[,]", 0);
					for (String city : res) {
						for (int i = 0; i < details.size(); i++) {
							CfgStateCityModel obj = details.get(i);
							if (obj.getCity().equals(city)) {
								details.remove(i);
							}
						}

					}
				}
			} else if (lead != null) {
				String cities = lead.getCity();
				if (cities != null && !cities.isEmpty()) {
					String[] res = cities.split("[,]", 0);
					for (String city : res) {
						for (int i = 0; i < details.size(); i++) {
							CfgStateCityModel obj = details.get(i);
							if (obj.getCity().equals(city)) {
								details.remove(i);
							}
						}
					}
				}
			}

			if (!details.isEmpty()) {

				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("cities", details);
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				List<CfgStateCityModel> details1 = istateCityRepository.findByStateId(stateId, true);
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("cities", details1);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Cities Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/industries")
	public ResponseEntity<?> getJobIndustries() {

		List<JobIndustryModel> details = jobIndustryRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("industries", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Industries Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/jobRoles")
	public ResponseEntity<?> getFullTimeJobRoles(@RequestParam(value="industry_id", required=false, defaultValue = "0") Integer industryId) {

		List<JobRolesModel> details = jobRolesRepository.findAll(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("jobRoles", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Roles Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	@PostMapping(path = "/userDetails")
	public ResponseEntity<?> setnewUserDetails(@RequestParam("phone_number") final String mobileNumber,
											   @RequestParam(value = "country_code", required = false) final String ccode,
											   @RequestParam(value = "name", required = false) final String name,
											   @RequestParam(value = "experience_in_manufacturing", required = false) final String expInMan,
											   @RequestParam(value = "industry", required = false) final String industry,
											   @RequestParam(value = "city", required = false) final String city,
											   @RequestParam(value = "prefArea", required = false) final String prefArea,
											   @RequestParam(value = "job_role", required = false) final String jobRole,
											   @RequestParam(value = "job_role_experience", defaultValue = "0") final int exp,
											   @RequestParam(value = "language_key", required = false) final String language) {

		long mn = Long.parseLong(mobileNumber);
		CandidateModel can = candidateRepository.findByMobileNumber(mn);
		CanLeadModel details = canLeadRepository.findByMobileNumber(mn);

		if (details != null) {

			details.setName(name);

			if (expInMan != null && !expInMan.isEmpty()) {
				if (expInMan.equalsIgnoreCase("Experienced")) {
					details.setExpInManufacturing(true);
				} else {
					details.setExpInManufacturing(false);
				}
				if (industry != null && !industry.isEmpty()) {
					details.setIndustry(industry);
				}
				if (jobRole != null && !jobRole.isEmpty()) {
					details.setJobCategory(jobRole);
				}

				if (exp != 0) {
					details.setExpYears(exp);
					details.setExpMonths(0);
					details.setExperienced(true);
					details.setCandidateType("Experienced");
					if (jobRole.equalsIgnoreCase("Trainee")) {
						details.setCandidateType("Fresher");
						details.setExperienced(false);
						details.setExpInManufacturing(false);
					}
					if (jobRole.equalsIgnoreCase("Assembler")) {
						details.setCandidateType("Fresher");
						details.setExperienced(false);
						details.setExpInManufacturing(false);
					}
					if (jobRole.equalsIgnoreCase("Graduate Trainee")) {
						details.setCandidateType("Fresher");
						details.setExperienced(false);
						details.setExpInManufacturing(false);
					}

				} else {
					details.setExpYears(0);
					details.setExpMonths(0);
					details.setExperienced(false);
					details.setCandidateType("Fresher");
				}
			}
			// if (state!=null && !state.isEmpty()) {
			details.setState("Tamil Nadu");
			details.setWACampaign(true);
			// }

			if (city != null && !city.isEmpty()) {
				details.setCity(city);
			}
			if(prefArea!=null && !prefArea.isEmpty()) {
				details.setCity(prefArea);
			}
			 

			if (language != null && !language.isEmpty()) {
				details.setLanguageKey(language);
			}

			canLeadRepository.save(details);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			if (can != null) {
				can.setFirstName(name);

				if (expInMan != null && !expInMan.isEmpty()) {
					if (expInMan.equalsIgnoreCase("Experienced")) {
						can.setExpInManufacturing(true);
					} else {
						can.setExpInManufacturing(false);
					}
					if (industry != null && !industry.isEmpty()) {
						can.setIndustry(industry);
					}
					if (jobRole != null && !jobRole.isEmpty()) {
						can.setJobCategory(jobRole);
					}
					if (exp != 0) {
						can.setExperience(exp);
						can.setExpMonths(0);
						can.setExperienced(true);
						can.setCandidateType("Experienced");
						if (jobRole.equalsIgnoreCase("Trainee")) {
							can.setCandidateType("Fresher");
							can.setExperienced(false);
							can.setExpInManufacturing(false);
						}
						if (jobRole.equalsIgnoreCase("Assembler")) {
							can.setCandidateType("Fresher");
							can.setExperienced(false);
							can.setExpInManufacturing(false);
						}
						if (jobRole.equalsIgnoreCase("Graduate Trainee")) {
							can.setCandidateType("Fresher");
							can.setExperienced(false);
							can.setExpInManufacturing(false);
						}
					} else {
						can.setExperience(0);
						can.setExpMonths(0);
						can.setExperienced(false);
						can.setCandidateType("Fresher");
					}
				}
				can.setState("Tamil Nadu");
				can.setWACampaign(true);

				if (city != null && !city.isEmpty()) {
					can.setCity(city);
				}
				if(prefArea!=null && !prefArea.isEmpty()) {
					can.setCity(prefArea);
				}
				 

				if (language != null && !language.isEmpty()) {
					can.setLanguageKey(language);
				}
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {
				CanLeadModel candidate = new CanLeadModel();
				candidate.setMobileNumber(mn);
				candidate.setCountryCode(ccode);

				if (name != null && !name.isEmpty()) {
					candidate.setName(name);
				}

				if (expInMan != null && !expInMan.isEmpty()) {
					if (expInMan.equalsIgnoreCase("Experienced")) {
						candidate.setExpInManufacturing(true);
					} else {
						candidate.setExpInManufacturing(false);
					}
					if (industry != null && !industry.isEmpty()) {
						candidate.setIndustry(industry);
					}
					if (jobRole != null && !jobRole.isEmpty()) {
						candidate.setJobCategory(jobRole);
					}
					if (exp != 0) {
						candidate.setExpYears(exp);
						candidate.setExpMonths(0);
						candidate.setExperienced(true);
						candidate.setCandidateType("Experienced");
						if (jobRole.equalsIgnoreCase("Trainee")) {
							candidate.setCandidateType("Fresher");
							candidate.setExperienced(false);
							candidate.setExpInManufacturing(false);
						}
						if (jobRole.equalsIgnoreCase("Assembler")) {
							candidate.setCandidateType("Fresher");
							candidate.setExperienced(false);
							candidate.setExpInManufacturing(false);
						}
						if (jobRole.equalsIgnoreCase("Graduate Trainee")) {
							candidate.setCandidateType("Fresher");
							candidate.setExperienced(false);
							candidate.setExpInManufacturing(false);
						}
					} else {
						candidate.setExpYears(0);
						candidate.setExpMonths(0);
						candidate.setExperienced(false);
						candidate.setCandidateType("Fresher");
					}
				}
				candidate.setState("Tamil Nadu");

				if (city != null && !city.isEmpty()) {
					candidate.setCity(city);
				}
				
				if(prefArea!=null && !prefArea.isEmpty()) {
					candidate.setCity(prefArea);
				}
				 

				if (language != null && !language.isEmpty()) {
					candidate.setLanguageKey(language);
				}

				candidate.setMnverified(true);
				candidate.setFromWA(true);
				candidate.setWACampaign(true);
				candidate.setAge(0);
				candidate.setPassed_out_year(0);
				candidate.setPassed_out_month(0);
				candidate.setProfilePageNo(0);

				canLeadRepository.save(candidate);

				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		}
	}

	@PutMapping(path = "/updateUserDetails")
	public ResponseEntity<?> updateUserDetails(@RequestParam("phone_number") final String mobileNumber,
											   @RequestParam(value = "currently_working", required = false) final String curWorking,
											   @RequestParam(value = "reason", required = false) final String reason,
											   @RequestParam(value = "immediate_joiner", required = false) final String immeJoiner) {

		long mn = Long.parseLong(mobileNumber);
		CandidateModel can = candidateRepository.findByMobileNumber(mn);
		CanLeadModel details = canLeadRepository.findByMobileNumber(mn);

		if (can != null) {
			if (curWorking != null && !curWorking.isEmpty()) {
				if (curWorking.equalsIgnoreCase("Yes")) {
					can.setCurrentlyworking(true);
					can.setReason_for_jobchange(reason);
				} else {
					can.setCurrentlyworking(false);
					can.setReason_for_unemployment(reason);
				}
			}
			if (immeJoiner != null && !immeJoiner.isEmpty()) {
				if (immeJoiner.equalsIgnoreCase("Yes")) {
					can.setImmediateJoiner(true);
				} else {
					can.setImmediateJoiner(false);
				}
			}
			candidateRepository.save(can);
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else if (details != null) {
			if (curWorking != null && !curWorking.isEmpty()) {
				if (curWorking.equalsIgnoreCase("Yes")) {
					details.setCurrentlyworking(true);
					details.setReason_for_jobchange(reason);
				} else {
					details.setCurrentlyworking(false);
					details.setReason_for_unemployment(reason);
				}
			}
			if (immeJoiner != null && !immeJoiner.isEmpty()) {
				if (immeJoiner.equalsIgnoreCase("Yes")) {
					details.setImmediateJoiner(true);
				} else {
					details.setImmediateJoiner(false);
				}
			}
			canLeadRepository.save(details);
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@SuppressWarnings("unchecked")
	@PutMapping(value = "/updateProfileDetails")
	public ResponseEntity<?> updateS3UserDetails(@RequestBody CanLeadModel c) {

		CandidateModel can = candidateRepository.findByMobileNumber(c.getMobileNumber());
		CanLeadModel details = canLeadRepository.findByMobileNumber(c.getMobileNumber());

		if (can != null) {
			can.setFirstName(c.getName());
			can.setAge(String.valueOf(c.getAge()));
			can.setGender(c.getGender());
			can.setCurrentCity(c.getCity());
			can.setCurrentState(c.getState());
			can.setState("Tamil Nadu");

			can.setDateOfBirth(c.getDateOfBirth());
			can.setQualification(c.getQualification());
			can.setSpecification(c.getSpecification());
			can.setCertificationSpecialization(c.getCourses());
			can.setStudent(c.getStudent());
			can.setPassed_out_year(c.getPassed_out_year());
			can.setPassed_out_month(c.getPassed_out_month());
			can.setKeySkill(c.getKeySkill());
			can.setPfEsiAccount(c.getPfEsiAccount());
			can.setIsHavingArrear(c.getIsHavingArrear());
			can.setReference(c.getReference());
			can.setCity(c.getPrefLocation());

			if (c.getExpYears() != 0) {
				can.setExperience(c.getExpYears());
				can.setExpMonths(c.getExpMonths());
				can.setExperienced(true);
				can.setCandidateType("Experienced");
				can.setExpInManufacturing(true);
				can.setIndustry(c.getIndustry());
				can.setJobCategory(c.getJobCategory());

				if (c.getJobCategory().equalsIgnoreCase("Trainee")) {
					can.setCandidateType("Fresher");
					can.setExperienced(false);
					can.setExpInManufacturing(false);
				}
				if (c.getJobCategory().equalsIgnoreCase("Assembler")) {
					can.setCandidateType("Fresher");
					can.setExperienced(false);
					can.setExpInManufacturing(false);
				}
				if (c.getJobCategory().equalsIgnoreCase("Graduate Trainee")) {
					can.setCandidateType("Fresher");
					can.setExperienced(false);
					can.setExpInManufacturing(false);
				}
			} else {
				can.setExperience(0);
				can.setExpMonths(0);
				can.setExpInManufacturing(false);
				can.setExperienced(false);
				can.setCandidateType("Fresher");
			}
			if (c.getKnownLanguages() != null) {

				List<CanLanguageModel> lang = canLanguagesRepository.findByCandidateId(can.getId());

				if (!lang.isEmpty()) {

					List<Integer> list = new ArrayList();

					int j = 0;

					for (CanLanguageModel s : lang) {

						j = s.getId();
						list.add(j);
					}

					canLanguagesRepository.delete(list);
				}

				List<Integer> x = Arrays.stream(c.getKnownLanguages().split(",")).map(Integer::parseInt)
						.collect(Collectors.toList());

				for (int f : x) {
					CanLanguageModel f1 = new CanLanguageModel();

					f1.setLanguageId(f);
					can.getLanguages().add(f1);
					f1.setCandidate(can);
					candidateRepository.save(can);
				}
			}
			can.setProfileFilled(true);

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			can.setProfileLastUpdatedDt(dtf.format(now));

			candidateRepository.save(can);

			updateGallaboxContacts(can);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else if (details != null) {

			UserModel user = new UserModel();

			user.setFirstName(c.getName());
			user.setMobileNumber(c.getMobileNumber());
			user.setCountryCode(c.getCountryCode());
			// user.setProfilePic(c.getProfilePic());

			user.setDeleted(false);

			String token = UUID.randomUUID().toString();
			user.setToken(token);

			userRepository.save(user);

			int userID = user.getId();

			CandidateModel candidate = new CandidateModel();

			candidate.setUserId(userID);
			candidate.setDeleted(false);
			candidate.setFirstName(c.getName());
			candidate.setMobileNumber(details.getMobileNumber());
			candidate.setWhatsappNumber(details.getMobileNumber());

			candidate.setDateOfBirth(c.getDateOfBirth());
			candidate.setAge(String.valueOf(c.getAge()));
			candidate.setGender(c.getGender());
			candidate.setPrefCountry("India");
			candidate.setState("Tamil Nadu");
			candidate.setCity(details.getCity());
			candidate.setCurrentCountry("India");
			candidate.setCurrentState(c.getState());
			candidate.setCurrentCity(c.getCity());

			candidate.setQualification(c.getQualification());
			candidate.setSpecification(c.getSpecification());
			candidate.setCertificationCourses("Certification Courses");
			candidate.setCertificationSpecialization(c.getCourses());

			candidate.setJobType("Full Time (8hrs to 10hrs)");
			candidate.setCandidateLocation("Domestic");
			candidate.setKeySkill(c.getKeySkill());

			candidate.setAmount(0);
			// candidate.setDiscountAmount(50);
			// candidate.setPaymentStatus("Paid");
			candidate.setLanguageKey(details.getLanguageKey());
			candidate.setRegistered(true);
			candidate.setWACampaign(true);
			candidate.setLookingForaJob(details.isLookingForaJob());
			candidate.setFromApp(false);
			candidate.setFromWA(true);
			candidate.setReference(c.getReference());
			candidate.setStudent(c.getStudent());
			candidate.setPassed_out_year(c.getPassed_out_year());
			candidate.setPassed_out_month(c.getPassed_out_month());
			candidate.setPfEsiAccount(c.getPfEsiAccount());
			candidate.setIsHavingArrear(c.getIsHavingArrear());

			if (c.getExpYears() != 0) {
				candidate.setExperience(c.getExpYears());
				candidate.setExpMonths(c.getExpMonths());
				candidate.setExperienced(true);
				candidate.setCandidateType("Experienced");
				candidate.setExpInManufacturing(true);
				candidate.setIndustry(c.getIndustry());
				candidate.setJobCategory(c.getJobCategory());

				if (c.getJobCategory().equalsIgnoreCase("Trainee")) {
					candidate.setCandidateType("Fresher");
					candidate.setExperienced(false);
					candidate.setExpInManufacturing(false);
				}
				if (c.getJobCategory().equalsIgnoreCase("Assembler")) {
					candidate.setCandidateType("Fresher");
					candidate.setExperienced(false);
					candidate.setExpInManufacturing(false);
				}
				if (c.getJobCategory().equalsIgnoreCase("Graduate Trainee")) {
					candidate.setCandidateType("Fresher");
					candidate.setExperienced(false);
					candidate.setExpInManufacturing(false);
				}
			} else {
				candidate.setExperience(0);
				candidate.setExpMonths(0);
				candidate.setExpInManufacturing(false);
				candidate.setExperienced(false);
				candidate.setCandidateType("Fresher");
			}

			if (details.isCurrentlyworking()) {
				candidate.setCurrentlyworking(true);
				candidate.setReason_for_jobchange(details.getReason_for_jobchange());
			} else {
				candidate.setCurrentlyworking(false);
				candidate.setReason_for_unemployment(details.getReason_for_unemployment());
			}
			candidate.setImmediateJoiner(details.isImmediateJoiner());

			String languages = c.getKnownLanguages();
			if (languages != null) {
				List<Integer> x = Arrays.stream(languages.split(",")).map(Integer::parseInt)
						.collect(Collectors.toList());

				for (int f : x) {
					CanLanguageModel f1 = new CanLanguageModel();

					f1.setLanguageId(f);
					candidate.getLanguages().add(f1);
					f1.setCandidate(candidate);
					candidateRepository.save(candidate);
				}
			}
			candidate.setProfileFilled(true);
			int limit = 2;
			if (candidate.getCandidateType().equalsIgnoreCase("Fresher")) {
				limit = 1;
			}
			candidate.setJobLimit(limit);
			candidate.setUsedFreeTrial(true);

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			candidate.setProfileLastUpdatedDt(dtf.format(now));

			candidateRepository.save(candidate);

			Optional<CandidateModel> optional = candidateRepository.findByUserId(userID);
			if (!optional.isPresent()) {

				try {
					userService.deleteById(userID);
				} catch (ResourceNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			} else {

				List<CanLeadModel> leadMN = canLeadRepository.findByMobileNumberList(details.getMobileNumber());

				if (leadMN.size() > 0) {
					for (CanLeadModel l : leadMN) {
						try {
							candidateService.deleteById(l.getId());
						} catch (ResourceNotFoundException e) {
							e.printStackTrace();
						}
					}
				}

				updateGallaboxContacts(candidate);

				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@Async
	private void updateGallaboxContacts(CandidateModel can) {
		List<String> numbers = new ArrayList<String>();
		numbers.add("+91" + can.getMobileNumber());

		GallaboxContactUpdate con = new GallaboxContactUpdate();
		con.setName(can.getFirstName());
		con.setPhone(numbers);
		GallboxContactFields val = new GallboxContactFields();
		val.setLanguage_key(can.getLanguageKey());
		val.setCity(can.getCity());
		val.setExp_in_manufacturing(can.getCandidateType());
		val.setIndustry(can.getIndustry());
		val.setJob_role(can.getJobCategory());
		val.setExp_in_years(String.valueOf(can.getExperience()));
		con.setFieldValues(val);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("apiKey", apiKey);
		headers.add("apiSecret", apiSecret);

		String jsonString = new com.google.gson.Gson().toJson(con);

		HttpEntity<String> request = new HttpEntity<String>(jsonString.toString(), headers);

		RestTemplate restTemplate = new RestTemplate();

		try {
			Object c = restTemplate.postForObject("https://server.gallabox.com/devapi/contacts/upsert", request,
					Object.class);
		} catch (Exception e) {
		}

	}

	@PutMapping(path = "/updateProfilePic")
	public ResponseEntity<?> updateProfilePic(@RequestParam("phone_number") final long mobileNumber,
											  @RequestPart(value = "pic", required = false) MultipartFile pic) {
		UserModel optional = userRepository.findByMobileNumber(mobileNumber);

		if (optional != null) {
			if (pic != null && !pic.isEmpty()) {

				String pic1 = optional.getProfilePic();

				if (pic1 != null && !pic1.isEmpty()) {

					String fp = pic1.substring(49);

					this.userService.deleteFileFromS3Bucket(fp);

				}
				String url = this.userService.uploadProfilePicToS3Bucket(pic, optional.getId(), true);
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Successfully Updated");
				map.put("url", url);
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "Failed to Update");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Failed to Update");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/profileDetails")
	public ResponseEntity<?> getProfileDetails(@RequestParam("phone_number") final String mobileNumber) {
		long mn = Long.parseLong(mobileNumber);
		CandidateModel details = candidateRepository.findByMobileNumber(mn);
		CanLeadModel lDetails = canLeadRepository.findByMobileNumber(mn);

		if (details != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "success");
			map.put("profile", details); // Add the entire details object to the response
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else if (lDetails != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "success");
			map.put("profile", lDetails); // Add the entire lDetails object to the response
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "User not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/candidateProfileStage")
	public ResponseEntity<?> getProfileStage(@RequestParam("phone_number") final String mobileNumber) {
		boolean profilefilled=true;
		boolean profilenotfilled=false;
		long mn = Long.parseLong(mobileNumber);
		CanLeadModel lDetails = canLeadRepository.findByMobileNumber(mn);
		CandidateModel details=candidateRepository.findByMobileNumber(mn);
		if (lDetails != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "success");
			map.put("profileFilled", profilenotfilled);
			map.put("profile", lDetails); // Add the entire lDetails object to the response
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
		else if(details!=null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "success");
			map.put("profileFilled", profilefilled);
			map.put("profile", details); // Add the entire lDetails object to the response
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
		else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "User not found in Lead Table");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/userDetails")
	public ResponseEntity<?> getNewDetails(@RequestParam("phone_number") final String mobileNumber) {
		long mn = Long.parseLong(mobileNumber);
		CandidateModel details = candidateRepository.findByMobileNumber(mn);
		CanLeadModel lDetails = canLeadRepository.findByMobileNumber(mn);
		Optional<LeadModel> emp = leadRepository.findByMobileNumber(mn);
		EmployerModel empDetails = employerRepository.findTopByMobileNumber(mn);
		FacebookMetaLead facebookMetaLead=facebookMetaLeadRepository.findByMobileNumber(mobileNumber);
		

		String profileFilled = "No";

		if(facebookMetaLead!=null) {
			String name = facebookMetaLead.getCandidateName();
			String location = facebookMetaLead.getPreferredLocation();
			String area=facebookMetaLead.getArea();
			String industry = facebookMetaLead.getIndustry();
			String jobRole = facebookMetaLead.getJobCategory();
			String exp = facebookMetaLead.getExperience();
			int AssignTo = facebookMetaLead.getAssignTo();
			
			HashMap<Object, Object> data = new HashMap<>();
			data.put("name", name);
			data.put("preferredLocation", location);
			data.put("prefArea", area);
			data.put("industry", industry);
			data.put("jobRole", jobRole);
			data.put("jobRoleExperience", exp);
			data.put("userType", "Job Seeker");
			data.put("profileFilled", profileFilled);
			data.put("assignTo", AssignTo);
			return new ResponseEntity<>(data, HttpStatus.OK);
		}
		else if (details != null) {

			String name = details.getFirstName();
			String state = details.getState();
			String location = details.getCity();
			int assignTo=details.getAssignTo();
			String area=details.getPrefArea();
			String industry = details.getIndustry();
			String jobRole = details.getJobCategory();
			int exp = details.getExperience();
			boolean expInMan = details.isExpInManufacturing();
			boolean WACampaign = details.isWACampaign();
			int indID = 0;
			int jobRoleID = 0, ProfilePageNo = 0;
			String canType = details.getCandidateType();

			try {
				if (details.getCandidateType().equalsIgnoreCase("Experienced")) {
					indID = industryRepository.findByIndustry(industry);
					Optional<JobRolesModel> jobrol = jobRolesRepository.findByIndustryIdandJobRole(indID, jobRole);
					jobRoleID = jobrol.get().getId();
				}
			} catch (Exception e) {
				canType = "Fresher";
			}
			if (details.isProfileFilled()) {
				profileFilled = "Yes";
			}
			try {
				if (canType != null) {
					ProfilePageNo = 1;
				}
			} catch (Exception e) {
			}
			HashMap<Object, Object> data = new HashMap<>();
			data.put("Name", name);
			data.put("State", state);
			data.put("city", location);
			data.put("assignTo", assignTo);
			data.put("prefArea", area);
			data.put("Industry", industry);
			data.put("JobRole", jobRole);
			data.put("IndustryID", indID);
			data.put("JobRoleID", jobRoleID);
			data.put("CandidateType", canType);
			data.put("JobRoleExperience", exp);
			data.put("ExperienceInManufacturing", expInMan);
			data.put("WACampaign", WACampaign);
			data.put("userType", "Job Seeker");
			data.put("ProfileFilled", profileFilled);
			data.put("LanguageKey", details.getLanguageKey());
			data.put("ProfilePageNo", ProfilePageNo);
			return new ResponseEntity<>(data, HttpStatus.OK);
		} else if (lDetails != null) {
			String name = lDetails.getName();
			String state = lDetails.getState();
			String location = lDetails.getCity();
			int assignTo=lDetails.getAssignTo();
			String area=lDetails.getPrefArea();
			String industry = lDetails.getIndustry();
			String jobRole = lDetails.getJobCategory();
			int exp = lDetails.getExpYears();
			boolean expInMan = lDetails.isExpInManufacturing();
			boolean WACampaign = lDetails.isWACampaign();
			String canType = lDetails.getCandidateType();

			int indID = 0;
			int jobRoleID = 0;
			int pageNo = 0;

			try {
				if (lDetails.getCandidateType().equalsIgnoreCase("Experienced")) {
					indID = industryRepository.findByIndustry(industry);
					Optional<JobRolesModel> jobrol = jobRolesRepository.findByIndustryIdandJobRole(indID, jobRole);
					jobRoleID = jobrol.get().getId();
				}
			} catch (Exception e) {
				canType = "Fresher";
			}
			try {
				if (canType != null) {
					pageNo = 1;
				}
			} catch (Exception e) {
			}
			HashMap<Object, Object> data = new HashMap<>();
			data.put("Name", name);
			data.put("State", state);
			data.put("City", location);
			data.put("assignTo", assignTo);
			data.put("prefArea", area);
			data.put("Industry", industry);
			data.put("JobRole", jobRole);
			data.put("IndustryID", indID);
			data.put("JobRoleID", jobRoleID);
			data.put("CandidateType", canType);
			data.put("JobRoleExperience", exp);
			data.put("ExperienceInManufacturing", expInMan);
			data.put("WACampaign", WACampaign);
			data.put("UserType", "Job Seeker");
			data.put("ProfileFilled", "No");
			data.put("ProfilePageNo", pageNo);
			data.put("LanguageKey", lDetails.getLanguageKey());
			return new ResponseEntity<>(data, HttpStatus.OK);
		} else {

			if (empDetails != null) {
				HashMap<Object, Object> data = new HashMap<>();
				data.put("Name", empDetails.getName());
				data.put("CompanyName", empDetails.getCompanyName());
				data.put("AssignTo", empDetails.getAssignTo());
				data.put("EmailID", empDetails.getEmailId());
				data.put("WACampaign", empDetails.isWhatsappNotification());
				data.put("UserType", "Employer");
				data.put("ProfileFilled", "No");
				data.put("ProfilePageNo", 0);
				data.put("LanguageKey", "en");
				return new ResponseEntity<>(data, HttpStatus.OK);
			} else {
				if (emp.isPresent()) {
					LeadModel l = emp.get();
					HashMap<Object, Object> data = new HashMap<>();
					data.put("Name", l.getName());
					data.put("CompanyName", l.getCompanyName());
					data.put("EmailID", l.getEmailId());
					data.put("assignTo", l.getAssignTo());
					data.put("WACampaign", true);
					data.put("UserType", "Employer");
					data.put("ProfileFilled", "No");
					data.put("ProfilePageNo", 0);
					data.put("LanguageKey", "en");
					return new ResponseEntity<>(data, HttpStatus.OK);
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 400);
					map.put("message", "User Not Found");
					map.put("Registered", false);
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}
			}
		}
	}

	@PostMapping(path = "/employerDetails")
	public ResponseEntity<?> setNewEmployerDetails(@RequestParam("phone_number") final String whatsappNum,
												   @RequestParam(value = "country_code", required = false) final String ccode,
												   @RequestParam(value = "name", required = false) final String name,
												   @RequestParam(value = "email_id", required = false) final String emailId,
												   @RequestParam(value = "company_name", required = false) final String companyName,
												   @RequestParam(value = "contact_number", required = false) final String contactNum) {
		long mn = Long.parseLong(contactNum);
		Optional<LeadModel> details = leadRepository.findByMobileNumber(mn);
		if (details.isPresent()) {
			LeadModel existing = details.get();

			if (name != null && !name.isEmpty()) {
				existing.setName(name);
			}
			if (emailId != null && !emailId.isEmpty()) {
				existing.setEmailId(emailId);
			}
			if (companyName != null && !companyName.isEmpty()) {
				existing.setCompanyName(companyName);
			}
			if (whatsappNum != null && !whatsappNum.isEmpty()) {
				long cn = Long.parseLong(whatsappNum);
				existing.setWhatsappNumber(cn);
			}

			leadRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			LeadModel emp = new LeadModel();

			emp.setMobileNumber(mn);
			emp.setMobileCountryCode(ccode);
			emp.setMnverified(true);
			if (name != null && !name.isEmpty()) {
				emp.setName(name);
			}
			if (emailId != null && !emailId.isEmpty()) {
				emp.setEmailId(emailId);
			}
			if (companyName != null && !companyName.isEmpty()) {
				emp.setCompanyName(companyName);
			}
			if (whatsappNum != null && !whatsappNum.isEmpty()) {
				long cn = Long.parseLong(whatsappNum);
				emp.setWhatsappNumber(cn);
			}
			emp.setRegistered(true);

			EmployerModel empDetails = employerRepository.findTopByMobileNumber(mn);
			if (empDetails != null) {
				emp.setRegisteredInApp(true);
			} else {
				emp.setRegisteredInApp(false);
			}
			emp.setFromWhatsapp(true);

			leadRepository.save(emp);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	@GetMapping(path = "/registeredStatus")
	public ResponseEntity<?> checkRegisteredDetails(@RequestParam("phone_number") final String mobileNumber) {
		long mn = Long.parseLong(mobileNumber);
		CandidateModel details = candidateRepository.findByMobileNumber(mn);
		CanLeadModel lDetails = canLeadRepository.findByMobileNumber(mn);
		CandidateModel contact=candidateRepository.findByContactNumber(mobileNumber);
		CanLeadModel contacts=canLeadRepository.findByContactNumber(mobileNumber);
		CandidateModel whatsapp=candidateRepository.findByWhatsappNumber(mn);
		CanLeadModel whatsapps=canLeadRepository.findByWhatsappNumber(mn);
		if (details != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("registered", true);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else if (lDetails != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("registered", true);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
		 else if (contact != null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("registered", true);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		 else if (contacts != null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("registered", true);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		 else if (whatsapp != null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("registered", true);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		 else if (whatsapps != null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("registered", true);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}

		else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "User Not Found");
			map.put("registered", false);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path = "/updateLanguageKey")
	public ResponseEntity<?> updateLanguage(@RequestParam("phone_number") final String mobileNumber,
											@RequestParam("language_key") final String languageKey) {
		long mn = Long.parseLong(mobileNumber);
		CandidateModel details = candidateRepository.findByMobileNumber(mn);
		CanLeadModel lDetails = canLeadRepository.findByMobileNumber(mn);

		if (details != null) {
			details.setLanguageKey(languageKey);
			candidateRepository.save(details);
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else if (lDetails != null) {
			lDetails.setLanguageKey(languageKey);
			canLeadRepository.save(lDetails);
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/callAgent")
	public ResponseEntity<?> needHelpCall(@RequestParam("phone_number") final String mobileNumber,
										  @RequestParam("event") final String event) {
		long mn = Long.parseLong(mobileNumber);
		CandidateModel details = candidateRepository.findByMobileNumber(mn);
		CanLeadModel lDetails = canLeadRepository.findByMobileNumber(mn);

		if (details != null) {
			HashMap<String, String> sdata = new HashMap<>();
			sdata.put("Event Name", "Jobseeker Alert");
			sdata.put("Event Type", event);
			sdata.put("Type", "Jobseeker");
			sdata.put("Candidate Name", details.getFirstName() != null ? details.getFirstName() : "");
			sdata.put("Job Role", details.getJobCategory() != null ? details.getJobCategory() : "Fresher");
			sdata.put("Mobile Number", mobileNumber);
			sdata.put("Source", "Whatsapp");
			sdata.put("ID Type", "Can ID");
			sdata.put("ID", String.valueOf(details.getId()));

			exotelCallController.connectToAgent("+91" + mobileNumber, "JS", sdata);
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else if (lDetails != null) {
			HashMap<String, String> sdata = new HashMap<>();
			sdata.put("Event Name", "Jobseeker Alert");
			sdata.put("Event Type", event);
			sdata.put("Type", "Jobseeker");
			sdata.put("Candidate Name", lDetails.getName() != null ? lDetails.getName() : "");
			sdata.put("Job Role", lDetails.getJobCategory() != null ? lDetails.getJobCategory() : "Fresher");
			sdata.put("Mobile Number", mobileNumber);
			sdata.put("Source", "Whatsapp");
			sdata.put("ID Type", "Can Lead ID");
			sdata.put("ID", String.valueOf(lDetails.getId()));

			exotelCallController.connectToAgent("+91" + mobileNumber, "JS", sdata);
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/empCallAgent")
	public ResponseEntity<?> needEmployerHelpCall(@RequestParam("phone_number") final String mobileNumber,
												  @RequestParam("event") final String event) {
		long mn = Long.parseLong(mobileNumber);
		Optional<LeadModel> lDetails = leadRepository.findByMobileNumber(mn);
		EmployerModel details = employerRepository.findTopByMobileNumber(mn);

		if (details != null) {
			HashMap<String, String> sdata = new HashMap<>();
			sdata.put("Event Name", "Employer Alert");
			sdata.put("Event Type", event);
			sdata.put("Type", "Employer");
			sdata.put("Contact Person Name",
					details.getContactPersonName() != null ? details.getContactPersonName() : "");
			sdata.put("Company", details.getCompanyName() != null ? details.getCompanyName() : "");
			sdata.put("Mobile Number", mobileNumber);
			sdata.put("Source", "Whatsapp");
			sdata.put("ID Type", "Emp ID");
			sdata.put("ID", String.valueOf(details.getId()));

			exotelCallController.connectToAgent("+91" + mobileNumber, "Emp", sdata);
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else if (lDetails != null) {
			HashMap<String, String> sdata = new HashMap<>();
			sdata.put("Event Name", "Employer Alert");
			sdata.put("Event Type", event);
			sdata.put("Type", "Employer");
//			sdata.put("Contact Person Name", lDetails.getContactPersonName() != null ? lDetails.getContactPersonName() : "");
//			sdata.put("Company", lDetails.getCompanyName() != null ? lDetails.getCompanyName() : "");
			sdata.put("Mobile Number", mobileNumber);
			sdata.put("Source", "Whatsapp");
			sdata.put("ID Type", "Emp Lead ID");
			sdata.put("ID", String.valueOf(lDetails.get().getId()));

			exotelCallController.connectToAgent("+91" + mobileNumber, "Emp", sdata);
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path = "/updateBasicDetails")
	public ResponseEntity<?> basicDetails(@RequestParam("mobileNumber") final long mobileNumber,
			  @RequestBody CanLeadModel canLeadModel) {

	    String name = canLeadModel.getName();
	    String lastName=canLeadModel.getLastName();
	    String dob = canLeadModel.getDateOfBirth();
	    int age = canLeadModel.getAge();
	    String gender = canLeadModel.getGender();
	    String state = canLeadModel.getState();
	    String city = canLeadModel.getCity();
	    String conNumber = canLeadModel.getContactNumber();
	   
	    String mobileNumberString = String.valueOf(mobileNumber);
		FacebookMetaLead fb = facebookMetaLeadRepository.findByMobileNumber(mobileNumberString);
	    CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mobileNumber);
	    CandidateModel candidate = candidateRepository.findByMobileNumber(mobileNumber);
	    
	    if(candidate != null)
	    {
	    	Map<String, Object> response = new HashMap<>();
	        response.put("statusCode", 400);
	        response.put("message", "Already Registered");
	        return ResponseEntity.ok(response);	
	    }

	    if(fb!=null) {
			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 400);
			response.put("message", "Already Registered in Fb Meta Lead");
			return ResponseEntity.ok(response);
		}
	    if (existingUser != null) {
	        // User already exists, update their details
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
	        existingUser.setWhatsappNumber(canLeadModel.getMobileNumber());
	        existingUser.setContactNumber(conNumber);
	        existingUser.setAssignTo(1);

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
	        newUser.setWhatsappNumber(canLeadModel.getMobileNumber());
	        newUser.setContactNumber(conNumber);
	        newUser.setAssignTo(1);

	        newUser.setProfilePageNo(1);
	        canLeadRepository.save(newUser);
	        AdminCallNotiModel ac=new AdminCallNotiModel();
	        ac.setEventName("Jobseeker Alert");
            ac.setType("Jobseeker");
            ac.setEventType("CanLead Jobseeker Register");
            ac.setSource("fb");
            ac.setMobileNumber(String.valueOf(mobileNumber));
            ac.setIdType("Jobseeker ID");
            ac.setReferenceId(Math.toIntExact(existingUser.getId()));
            ac.setCandidateName(name);
            adminCallNotiRepository.save(ac);


	        Map<String, Object> response = new HashMap<>();
	        response.put("statusCode", 200);
	        response.put("message", "User Registered Successfully");
	        return ResponseEntity.ok(response);
	    }
	}

	
	@PutMapping(path = "/updateEducationDetails")
	public ResponseEntity<?> updateEducationDetails(@RequestParam("mobileNumber") final long mobileNumber,
													@RequestBody CanLeadModel canLeadModel) {

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
			setAssignToBasedOnPrefArea(existingUser);
			existingUser.setProfilePageNo(3);

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

	@PutMapping(path = "/updateOtherDetails")
	public ResponseEntity<?> updateOtherDetails(@RequestParam("mobileNumber") long mobileNumber, @RequestBody CanLeadModel canLeadModel) {
		CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mobileNumber);
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
			user.setCountryCode(existingUser.getCountryCode());
			user.setProfilePic(existingUser.getProfilePic());
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
			candidate.setFromWA(true);
			candidate.setReference(existingUser.getReference());
			candidate.setStudent(existingUser.getStudent());
			candidate.setPassed_out_year(existingUser.getPassed_out_year());

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

			CandidateTimeLine candidateTimeLine = new CandidateTimeLine();

			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
			String formattedDate = dateFormat.format(currentDate);
			String eventDescription = "Updated On"+ formattedDate;
			candidateTimeLine.setCanId(canLeadModel.getId());
			candidateTimeLine.setEventName("Profile updated");
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
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@PutMapping("/resumeUpload")
	public ResponseEntity<String> updateResume(
	        @RequestParam long mobileNumber,
	        @RequestParam MultipartFile file) throws IOException, java.io.IOException {
	    CandidateModel candidateOptional = candidateRepository.findByMobileNumber(mobileNumber);

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
	  
	            candidate.setResume(fileUrl);
	            candidateRepository.save(candidate);
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
	   
	   @GetMapping(path = "/assignFacebookMetaLeads")
		public ResponseEntity<?> setnewAssignFacebook(@RequestParam("mobile_number") final String mobileNumber){
		   FacebookMetaLead facebookMetaLead=facebookMetaLeadRepository.findByMobileNumber(mobileNumber);
		   
		   if(facebookMetaLead!=null) {
			   int assignToValue = facebookMetaLead.getAssignTo();
			   HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Retrieve successfully");
				map.put("assignTo", assignToValue);
				return new ResponseEntity<>(map, HttpStatus.OK);    
		   }
		   HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "File not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
	   }
	   
	   @GetMapping("/city")
	   public ResponseEntity<?> getDetails(
	       @RequestParam(required = false) String preferredLocation,
	       @RequestParam(required = false) long mn,
	       @RequestParam(required = false) String name) {

	       int nextActiveAdminId = findNextAdminId(preferredLocation);

	       // Convert mn to Long
	       String mobileNumberString = String.valueOf(mn);
	       FacebookMetaLead facebookMeta = facebookMetaLeadRepository.findByMobileNumber(mobileNumberString);

	       // Convert mn to Long
	       CanLeadModel can = canLeadRepository.findByMobileNumber(mn);

	       // Convert mn to Long
	       CandidateModel candidate = candidateRepository.findByMobileNumber(mn);

	       if (facebookMeta == null && can == null && candidate == null) {
	           CanLeadModel lead = new CanLeadModel();
	           lead.setAssignTo(nextActiveAdminId);
	           lead.setMobileNumber(mn);
	           lead.setName(name);
	           lead.setCity(preferredLocation);
	           canLeadRepository.save(lead);
	       }

	       return ResponseEntity.ok().build(); // You should return an appropriate response
	   }

	   private Integer findNextAdminId(String preferredLocation) {
		    // Get the most recent FacebookMetaLead entry for the specified preferredLocation
		    Optional<CanLeadModel> latestEntryOptional = canLeadRepository.findFirstByCityOrderByCreatedTimeDesc(preferredLocation);
		    int prevAdminId = 0; // Default value

		    if (latestEntryOptional.isPresent()) {
		        // If an entry is found, extract the assignTo (adminId) from the latest entry
		        prevAdminId = latestEntryOptional.get().getAssignTo();
		    }

		    try {
		        // Find the next adminId for the specified preferredLocation and prevAdminId
		        CfgCanAdminCityGrouping cfgCanAdminCityGrouping = cfgCanAdminCityGroupingRepository.findFirstByCityNameAndAdminIdGreaterThanOrderByAdminIdAsc(preferredLocation, prevAdminId);

		        if (cfgCanAdminCityGrouping != null) {
		            return cfgCanAdminCityGrouping.getAdminId();
		        } else {
		            // Handle the case where there is no next adminId for the specified preferredLocation
		            // Restart the loop from the first adminId for that city
		            CfgCanAdminCityGrouping firstAdminForCity = cfgCanAdminCityGroupingRepository.findFirstByCityNameOrderByAdminIdAsc(preferredLocation);

		            if (firstAdminForCity != null) {
		                return firstAdminForCity.getAdminId();
		            } else {
		                return 1; // If no adminId is found, return a default value (e.g., 1)
		            }
		        }
		    } catch (Exception e) {
		        // Handle exceptions (log, rethrow, or return a default value)
		        e.printStackTrace();
		        return 1; // Return a default value in case of an exception
		    }
		}

}		


