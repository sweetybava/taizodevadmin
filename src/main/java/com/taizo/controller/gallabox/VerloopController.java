package com.taizo.controller.gallabox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.CanLeadModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.CfgFullTimeGroup;
import com.taizo.model.CfgStateCityModel;
import com.taizo.model.CfgStateModel;
import com.taizo.model.CityModel;
import com.taizo.model.DeeplinkRequest;
import com.taizo.model.DeeplinkSuffix;
import com.taizo.model.EmployerModel;
import com.taizo.model.FirebaseShortLink;
import com.taizo.model.IndiaStateModel;
import com.taizo.model.IndustryModel;
import com.taizo.model.JobIndustryModel;
import com.taizo.model.JobRolesModel;
import com.taizo.model.LeadModel;
import com.taizo.model.StateCityModel;
import com.taizo.model.UserModel;
import com.taizo.repository.CanLeadRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.CfgStateRepository;
import com.taizo.repository.CityRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.FullTimeGroupingRepository;
import com.taizo.repository.IStateCityRepository;
import com.taizo.repository.IndiaStateRepository;
import com.taizo.repository.IndustryRepository;
import com.taizo.repository.JobIndustryRepository;
import com.taizo.repository.JobRepository;
import com.taizo.repository.JobRolesRepository;
import com.taizo.repository.LeadRepository;
import com.taizo.repository.StateCityRepository;
import com.taizo.repository.UserRepository;
import com.taizo.service.CandidateService;
import com.taizo.service.UserService;

@RestController
@CrossOrigin
@RequestMapping("/chat")
public class VerloopController {

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	LeadRepository leadRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Autowired
	CandidateService candidateService;

	@Autowired
	CityRepository cityRepository;

	@Autowired
	IndiaStateRepository indiaStateRepository;

	@Autowired
	CfgStateRepository cfgStateRepository;

	@Autowired
	IndustryRepository industryRepository;

	@Autowired
	JobIndustryRepository jobIndustryRepository;

	@Autowired
	IStateCityRepository istateCityRepository;

	@Autowired
	JobRolesRepository jobRolesRepository;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	FullTimeGroupingRepository fullTimeGroupingRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	CanLeadRepository canLeadRepository;
	
	@Value("${firebase.js.api.key}")
	private String firebaseJSApiKey;

	@Value("${firebase.deeplink.js.hostname}")
	private String firebaseJSHost;

	@Value("${firebase.jsapp.package.name}")
	private String firebaseJSPackage;

	@PostMapping(path = "/employerDetails")
	public ResponseEntity<?> setEmployerDetails(@RequestParam("phone_number") final long mobileNumber,
			@RequestParam(value = "name", required = false) final String name,
			@RequestParam(value = "country_code", required = false) final String ccode,
			@RequestParam(value = "email_id", required = false) final String emailId,
			@RequestParam(value = "company_name", required = false) final String companyName) {
		Optional<LeadModel> details = leadRepository.findByMobileNumber(mobileNumber);
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
			leadRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			LeadModel emp = new LeadModel();

			emp.setMobileNumber(mobileNumber);
			emp.setMobileCountryCode(ccode);
			if (name != null && !name.isEmpty()) {
				emp.setName(name);
			}
			if (emailId != null && !emailId.isEmpty()) {
				emp.setEmailId(emailId);
			}
			if (companyName != null && !companyName.isEmpty()) {
				emp.setCompanyName(companyName);
			}
			emp.setRegistered(true);

			EmployerModel empDetails = employerRepository.findTopByMobileNumber(mobileNumber);
			if (empDetails != null) {
				emp.setRegisteredInApp(true);
			} else {
				emp.setRegisteredInApp(false);
			}

			leadRepository.save(emp);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}
	
	@PostMapping(path = "/newEmployerDetails")
	public ResponseEntity<?> setNewEmployerDetails(@RequestParam("phone_number") final String mobileNumber,
			@RequestParam(value = "name", required = false) final String name,
			@RequestParam(value = "email_id", required = false) final String emailId,
			@RequestParam(value = "company_name", required = false) final String companyName) {
		String num = mobileNumber.substring(mobileNumber.length()-10);
		String[] cc = mobileNumber.split(num);
		String countrycode = cc[0].substring(1);
		long mn = Long.parseLong(num);
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
			leadRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			LeadModel emp = new LeadModel();

			emp.setMobileNumber(mn);
			emp.setMobileCountryCode(countrycode);
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

	@PostMapping(path = "/userDetails")
	public ResponseEntity<?> setUserDetails(@RequestParam("phone_number") final long mobileNumber,
			@RequestParam(value = "name", required = false) final String name,
			@RequestParam(value = "country_code", required = false) final String ccode,
			@RequestParam(value = "looking_for_a_job", required = false) final String lookingJob,
			@RequestParam(value = "experience_in_manufacturing", required = false) final String expInMan,
			@RequestParam(value = "industry", required = false) final String industry,
			@RequestParam(value = "location", required = false) final String city,
			@RequestParam(value = "job_role", required = false) final String jobRole,
			@RequestParam(value = "state_of_job", required = false) final String state,
			@RequestParam(value = "job_role_experience", required = false) final String exp,
			@RequestParam(value = "language_key", required = false) final String language,
			@RequestParam(value = "WA_campaign", required = false) final String WACampaign) {

		CandidateModel can = candidateRepository.findByMobileNumber(mobileNumber);
		CanLeadModel details = canLeadRepository.findByMobileNumber(mobileNumber);
		Optional<IndustryModel> ind = industryRepository.findById(Integer.parseInt(industry));
		Optional<JobRolesModel> jobrol = jobRolesRepository.findById(Integer.parseInt(jobRole));
		Optional<CfgStateModel> sta = cfgStateRepository.findById(Integer.parseInt(state));
		Optional<CfgStateCityModel> cit = istateCityRepository.findById(Integer.parseInt(city));


		if (details != null) {
			CfgStateModel csta = sta.get();
			CfgStateCityModel ccit = cit.get();

			if (name != null && !name.isEmpty()) {
				details.setName(name);
			}
			if (lookingJob != null && !lookingJob.isEmpty()) {
				if (lookingJob.equalsIgnoreCase("yes")) {
					details.setLookingForaJob(true);
				} else {
					details.setLookingForaJob(false);
				}
			}

			if (expInMan != null && !expInMan.isEmpty()) {
				if (expInMan.equalsIgnoreCase("yes")) {
					details.setExpInManufacturing(true);
				} else {
					details.setExpInManufacturing(false);
				}
			}
			if (sta.isPresent()) {
				details.setState(csta.getState());
			}
			if (ind.isPresent()) {
				IndustryModel cind = ind.get();
				details.setIndustry(cind.getIndustry());
			}
			if (cit.isPresent()) {
				details.setCity(ccit.getCity());
			}
			if (jobrol.isPresent()) {
				JobRolesModel cjobrol = jobrol.get();
				details.setJobCategory(cjobrol.getJobRoles());
			}

			if (exp != null && !exp.isEmpty()) {
				details.setExpYears(Integer.parseInt(exp));
				details.setExpMonths(0);
			}
			if (exp.isEmpty() || exp.equalsIgnoreCase("0")) {
				// details.setCandidateType("Fresher");
				details.setExperienced(false);
			} else {
				// details.setCandidateType("Experienced");
				details.setExperienced(true);

			}

			if (WACampaign != null && !WACampaign.isEmpty()) {
				if (WACampaign.equalsIgnoreCase("yes")) {
					details.setWACampaign(true);
				} else {
					details.setWACampaign(false);
				}
			}
			if (language != null && !language.isEmpty()) {
				details.setLanguageKey(language);
			}

			canLeadRepository.save(details);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			CfgStateModel csta = sta.get();
			CfgStateCityModel ccit = cit.get();
			if(can!=null) {
				if (name != null && !name.isEmpty()) {
					can.setFirstName(name);
				}
				if (lookingJob != null && !lookingJob.isEmpty()) {
					if (lookingJob.equalsIgnoreCase("yes")) {
						can.setLookingForaJob(true);
					} else {
						can.setLookingForaJob(false);
					}
				}

				if (expInMan != null && !expInMan.isEmpty()) {
					if (expInMan.equalsIgnoreCase("yes")) {
						can.setExpInManufacturing(true);
					} else {
						can.setExpInManufacturing(false);
					}
				}
				if (sta.isPresent()) {
					can.setState(csta.getState());
				}
				if (ind.isPresent()) {
					IndustryModel cind = ind.get();
					can.setIndustry(cind.getIndustry());
				}
				if (cit.isPresent()) {
					can.setCity(ccit.getCity());
				}
				if (jobrol.isPresent()) {
					JobRolesModel cjobrol = jobrol.get();
					can.setJobCategory(cjobrol.getJobRoles());
				}
				if (exp != null && !exp.isEmpty()) {
					can.setExperience(Integer.parseInt(exp));
					can.setExpMonths(0);
				}
				if (exp == null || exp.equalsIgnoreCase("0")) {
					can.setExperienced(false);
				} else {
					can.setExperienced(true);
				}
				if (WACampaign != null && !WACampaign.isEmpty()) {
					if (WACampaign.equalsIgnoreCase("yes")) {
						can.setWACampaign(true);
					} else {
						can.setWACampaign(false);
					}
				}
				if (language != null && !language.isEmpty()) {
					can.setLanguageKey(language);
				}
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				return new ResponseEntity<>(map, HttpStatus.OK);
			
			 }else {
				 CanLeadModel candidate = new CanLeadModel();
					candidate.setMobileNumber(mobileNumber);
					candidate.setCountryCode(ccode);

					if (name != null && !name.isEmpty()) {
						candidate.setName(name);
					}
					if (lookingJob != null && !lookingJob.isEmpty()) {
						if (lookingJob.equalsIgnoreCase("yes")) {
							candidate.setLookingForaJob(true);
						} else {
							candidate.setLookingForaJob(false);
						}
					}

					if (expInMan != null && !expInMan.isEmpty()) {
						if (expInMan.equalsIgnoreCase("yes")) {
							candidate.setExpInManufacturing(true);
						} else {
							candidate.setExpInManufacturing(false);
						}
					}
					if (sta.isPresent()) {
						candidate.setState(csta.getState());
					}
					if (ind.isPresent()) {
						IndustryModel cind = ind.get();
						candidate.setIndustry(cind.getIndustry());
					}
					if (cit.isPresent()) {
						candidate.setCity(ccit.getCity());
					}
					if (jobrol.isPresent()) {
						JobRolesModel cjobrol = jobrol.get();
						candidate.setJobCategory(cjobrol.getJobRoles());
					}
					if (exp != null && !exp.isEmpty()) {
						candidate.setExpYears(Integer.parseInt(exp));
						candidate.setExpMonths(0);
					}
					if (exp == null || exp.equalsIgnoreCase("0")) {
						candidate.setExperienced(false);
					} else {
						candidate.setExperienced(true);
					}
					if (WACampaign != null && !WACampaign.isEmpty()) {
						if (WACampaign.equalsIgnoreCase("yes")) {
							candidate.setWACampaign(true);
						} else {
							candidate.setWACampaign(false);
						}
					}
					if (language != null && !language.isEmpty()) {
						candidate.setLanguageKey(language);
					}

					candidate.setFromWA(true);
					candidate.setAge(0);
					candidate.setProfilePageNo(0);

					canLeadRepository.save(candidate);

					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 200);
					return new ResponseEntity<>(map, HttpStatus.OK);
			 }

		}
	}
	
	@PostMapping(path = "/newUserDetails")
	public ResponseEntity<?> setnewUserDetails(@RequestParam("phone_number") final String mobileNumber,
			@RequestParam(value = "name", required = false) final String name,
			@RequestParam(value = "experience_in_manufacturing", required = false) final String expInMan,
			@RequestParam(value = "industry", required = false) final String industry,
			@RequestParam(value = "location", required = false) final String city,
			@RequestParam(value = "job_role", required = false) final String jobRole,
			@RequestParam(value = "state_of_job", required = false) final String state,
			@RequestParam(value = "job_role_experience", defaultValue = "0") final int exp,
			@RequestParam(value = "language_key", required = false) final String language,
			@RequestParam(value = "WA_campaign", required = false) final String WACampaign) {

		String num = mobileNumber.substring(mobileNumber.length()-10);
		String[] cc = mobileNumber.split(num);
		String countrycode = cc[0].substring(1);
		long mn = Long.parseLong(num);
		CandidateModel can = candidateRepository.findByMobileNumber(mn);
		CanLeadModel details = canLeadRepository.findByMobileNumber(mn);

		if (details != null) {

			if (name != null && !name.isEmpty()) {
				details.setName(name);
			}

			if (expInMan != null && !expInMan.isEmpty()) {
				if (expInMan.equalsIgnoreCase("Experienced")) {
					details.setExpInManufacturing(true);
				} else {
					details.setExpInManufacturing(false);
				}
			}
			if (state!=null && !state.isEmpty()) {
				details.setState(state);
			}
			if (industry!=null && !industry.isEmpty()) {
				details.setIndustry(industry);
			}
			if (city!=null && !city.isEmpty()) {
				details.setCity(city);
			}
			if (jobRole!=null && !jobRole.isEmpty()) {
				details.setJobCategory(jobRole);
			}

			if (exp != 0) {
				details.setExpYears(exp);
				details.setExpMonths(0);
				details.setExperienced(true);
				details.setCandidateType("Experienced");
			}else {
				details.setExpYears(0);
				details.setExpMonths(0);
				details.setExperienced(false);
				details.setCandidateType("Fresher");
			}

			if (WACampaign != null && !WACampaign.isEmpty()) {
				if (WACampaign.equalsIgnoreCase("yes")) {
					details.setWACampaign(true);
				} else if (WACampaign.equalsIgnoreCase("ஆம்")) {
					details.setWACampaign(true);
				} else if (WACampaign.equalsIgnoreCase("हाँ")) {
					details.setWACampaign(true);
				} else {
					details.setWACampaign(false);
				}
			}
			if (language != null && !language.isEmpty()) {
				details.setLanguageKey(language);
			}

			canLeadRepository.save(details);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			if(can!=null) {
				if (name != null && !name.isEmpty()) {
					can.setFirstName(name);
				}

				if (expInMan != null && !expInMan.isEmpty()) {
					if (expInMan.equalsIgnoreCase("Experienced")) {
						can.setExpInManufacturing(true);
					} else {
						can.setExpInManufacturing(false);
					}
				}
				if (state!=null && !state.isEmpty()) {
					can.setState(state);
				}
				if (industry!=null && !industry.isEmpty()) {
					can.setIndustry(industry);
				}
				if (city!=null && !city.isEmpty()) {
					can.setCity(city);
				}
				if (jobRole!=null && !jobRole.isEmpty()) {
					can.setJobCategory(jobRole);
				}
				if (exp != 0) {
					can.setExperience(exp);
					can.setExpMonths(0);
					can.setExperienced(true);
					can.setCandidateType("Experienced");
				}else {
					can.setExperience(0);
					can.setExpMonths(0);
					can.setExperienced(false);
					can.setCandidateType("Fresher");
				}
				if (WACampaign != null && !WACampaign.isEmpty()) {
					if (WACampaign.equalsIgnoreCase("yes")) {
						can.setWACampaign(true);
					} else if (WACampaign.equalsIgnoreCase("ஆம்")) {
						can.setWACampaign(true);
					}  else if (WACampaign.equalsIgnoreCase("हाँ")) {
						can.setWACampaign(true);
					} else {
						can.setWACampaign(false);
					}
				}
				if (language != null && !language.isEmpty()) {
					can.setLanguageKey(language);
				}
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				return new ResponseEntity<>(map, HttpStatus.OK);
			
			 }else {
				 CanLeadModel candidate = new CanLeadModel();
					candidate.setMobileNumber(mn);
					candidate.setCountryCode(countrycode);

					if (name != null && !name.isEmpty()) {
						candidate.setName(name);
					}

					if (expInMan != null && !expInMan.isEmpty()) {
						if (expInMan.equalsIgnoreCase("Experienced")) {
							candidate.setExpInManufacturing(true);
						} else {
							candidate.setExpInManufacturing(false);
						}
					}
					if (state!=null && !state.isEmpty()) {
						candidate.setState(state);
					}
					if (industry!=null && !industry.isEmpty()) {
						candidate.setIndustry(industry);
					}
					if (city!=null && !city.isEmpty()) {
						candidate.setCity(city);
					}
					if (jobRole!=null && !jobRole.isEmpty()) {
						candidate.setJobCategory(jobRole);
					}
					if (exp != 0) {
						candidate.setExpYears(exp);
						candidate.setExpMonths(0);
						candidate.setExperienced(true);
						candidate.setCandidateType("Experienced");

					}else {
						candidate.setExpYears(0);
						candidate.setExpMonths(0);
						candidate.setExperienced(false);
						candidate.setCandidateType("Fresher");
					}
					if (WACampaign != null && !WACampaign.isEmpty()) {
						if (WACampaign.equalsIgnoreCase("yes")) {
							candidate.setWACampaign(true);
						} else if (WACampaign.equalsIgnoreCase("ஆம்")) {
							candidate.setWACampaign(true);
						}  else if (WACampaign.equalsIgnoreCase("हाँ")) {
							candidate.setWACampaign(true);
						} else {
							candidate.setWACampaign(false);
						}
					}
					if (language != null && !language.isEmpty()) {
						candidate.setLanguageKey(language);
					}

					candidate.setMnverified(true);
					candidate.setFromWA(true);
					candidate.setAge(0);
					candidate.setProfilePageNo(0);

					canLeadRepository.save(candidate);

					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 200);
					return new ResponseEntity<>(map, HttpStatus.OK);
			 }

		}
	}
	
	@GetMapping(path = "/newUserDetails")
	public ResponseEntity<?> getNewDetails(@RequestParam("phone_number") final String mobileNumber) {
		String num = mobileNumber.substring(mobileNumber.length()-10);
		long mn = Long.parseLong(num);

		CandidateModel details = candidateRepository.findByMobileNumber(mn);
		CanLeadModel lDetails = canLeadRepository.findByMobileNumber(mn);
		Optional<LeadModel> emp = leadRepository.findByMobileNumber(mn);
		EmployerModel empDetails = employerRepository.findTopByMobileNumber(mn);

		if (details != null) {

			String name = details.getFirstName();
			boolean regUser = details.isRegistered();
			String state = details.getState();
			String location = details.getCity();
			String industry = details.getIndustry();
			String jobRole = details.getJobCategory();
			int exp = details.getExperience();
			boolean lookJob = details.isLookingForaJob();
			boolean expInMan = details.isExpInManufacturing();
			boolean WACampaign = details.isWACampaign();
			int indID=0;
			int jobRoleID=0;
			String canType = details.getCandidateType();
			
			try {
			if(details.getCandidateType().equalsIgnoreCase("Experienced")) {
			 indID = industryRepository.findByIndustry(industry);
			Optional<JobRolesModel> jobrol = jobRolesRepository.findByIndustryIdandJobRole(indID, jobRole);
			jobRoleID = jobrol.get().getId();
			}
			}catch(Exception e) {
				canType="Fresher";
			}

			HashMap<Object, Object> data = new HashMap<>();
			data.put("Registered", regUser);
			data.put("Name", name);
			data.put("State", state);
			data.put("LocationForJob", location);
			data.put("Industry", industry);
			data.put("JobRole", jobRole);
			data.put("IndustryID", indID);
			data.put("JobRoleID", jobRoleID);
			data.put("CandidateType", canType);
			data.put("JobRoleExperience", exp);
			data.put("LookingForaJob", lookJob);
			data.put("ExperienceInManufacturing", expInMan);
			data.put("WACampaign", WACampaign);
			data.put("AppLink", "https://cutt.ly/taizojobs");
			data.put("UserType", "Job Seeker");

			return new ResponseEntity<>(data, HttpStatus.OK);
		} else if (lDetails != null) {
			String name = lDetails.getName();
			boolean regUser = true;
			String state = lDetails.getState();
			String location = lDetails.getCity();
			String industry = lDetails.getIndustry();
			String jobRole = lDetails.getJobCategory();
			int exp = lDetails.getExpYears();
			boolean lookJob = lDetails.isLookingForaJob();
			boolean expInMan = lDetails.isExpInManufacturing();
			boolean WACampaign = lDetails.isWACampaign();
			String canType = lDetails.getCandidateType();
			
			int indID=0;
			int jobRoleID=0;
			
			try {
			if(lDetails.getCandidateType().equalsIgnoreCase("Experienced")) {
			 indID = industryRepository.findByIndustry(industry);
			Optional<JobRolesModel> jobrol = jobRolesRepository.findByIndustryIdandJobRole(indID, jobRole);
			jobRoleID = jobrol.get().getId();
			}
			}catch(Exception e) {
				canType="Fresher";
			}

			HashMap<Object, Object> data = new HashMap<>();
			data.put("Registered", regUser);
			data.put("Name", name);
			data.put("State", state);
			data.put("LocationForJob", location);
			data.put("Industry", industry);
			data.put("JobRole", jobRole);
			data.put("IndustryID", indID);
			data.put("JobRoleID", jobRoleID);
			data.put("CandidateType", canType);
			data.put("JobRoleExperience", exp);
			data.put("LookingForaJob", lookJob);
			data.put("ExperienceInManufacturing", expInMan);
			data.put("WACampaign", WACampaign);
			data.put("AppLink", "https://cutt.ly/taizojobs");
			data.put("UserType", "Job Seeker");

			return new ResponseEntity<>(data, HttpStatus.OK);
		} else {

			if (empDetails != null) {
				HashMap<Object, Object> data = new HashMap<>();
				data.put("Registered", true);
				data.put("Name", empDetails.getName());
				data.put("CompanyName", empDetails.getCompanyName());
				data.put("EmailID", empDetails.getEmailId());
				data.put("WACampaign", empDetails.isWhatsappNotification());
				data.put("AppLink", "https://cutt.ly/taizoemployer");
				data.put("WebLink", "http://web.taizo.in/console/index.html");
				data.put("UserType", "Employer");

				return new ResponseEntity<>(data, HttpStatus.OK);
			} else {
				if (emp.isPresent()) {
					LeadModel l = emp.get();
					HashMap<Object, Object> data = new HashMap<>();
					data.put("Registered", l.isRegistered());
					data.put("Name", l.getName());
					data.put("CompanyName", l.getCompanyName());
					data.put("EmailID", l.getEmailId());
					data.put("WACampaign", true);
					data.put("AppLink", "https://cutt.ly/taizoemployer");
					data.put("WebLink", "http://web.taizo.in/console/index.html");
					data.put("UserType", "Employer");

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

	@GetMapping(path = "/userDetails")
	public ResponseEntity<?> getDetails(@RequestParam("phone_number") final long mobileNumber,
			@RequestParam(value = "country_code", required = false) final String ccode) {

		CandidateModel details = candidateRepository.findByMobileNumber(mobileNumber);
		CanLeadModel lDetails = canLeadRepository.findByMobileNumber(mobileNumber);
		Optional<LeadModel> emp = leadRepository.findByMobileNumber(mobileNumber);
		EmployerModel empDetails = employerRepository.findTopByMobileNumber(mobileNumber);

		if (details != null) {

			String name = details.getFirstName();
			boolean regUser = details.isRegistered();
			int assignTo=details.getAssignTo();
			String state = details.getState();
			String location = details.getCity();
			String industry = details.getIndustry();
			String jobRole = details.getJobCategory();
			int exp = details.getExperience();
			boolean lookJob = details.isLookingForaJob();
			boolean expInMan = details.isExpInManufacturing();
			boolean WACampaign = details.isWACampaign();

			HashMap<Object, Object> data = new HashMap<>();
			data.put("Registered", regUser);
			data.put("Name", name);
			data.put("AssignTo", assignTo);
			data.put("State", state);
			data.put("Location for job", location);
			data.put("Industry", industry);
			data.put("Job role", jobRole);
			data.put("Job role Experience", exp);
			data.put("Looking for a job", lookJob);
			data.put("Experience in manufacturing", expInMan);
			data.put("WA campaign", WACampaign);
			data.put("App Link", "https://cutt.ly/taizojobs");
			data.put("User Type", "Job Seeker");

			return new ResponseEntity<>(data, HttpStatus.OK);
		} else if (lDetails != null) {
			String name = lDetails.getName();
			boolean regUser = true;
			int assignTo=lDetails.getAssignTo();
			String state = lDetails.getState();
			String location = lDetails.getCity();
			String industry = lDetails.getIndustry();
			String jobRole = lDetails.getJobCategory();
			int exp = lDetails.getExpYears();
			boolean lookJob = lDetails.isLookingForaJob();
			boolean expInMan = lDetails.isExpInManufacturing();
			boolean WACampaign = lDetails.isWACampaign();

			HashMap<Object, Object> data = new HashMap<>();
			data.put("Registered", regUser);
			data.put("Name", name);
			data.put("AssignTo", assignTo);
			data.put("State", state);
			data.put("Location for job", location);
			data.put("Industry", industry);
			data.put("Job role", jobRole);
			data.put("Job role Experience", exp);
			data.put("Looking for a job", lookJob);
			data.put("Experience in manufacturing", expInMan);
			data.put("WA campaign", WACampaign);
			data.put("App Link", "https://cutt.ly/taizojobs");
			data.put("User Type", "Job Seeker");

			return new ResponseEntity<>(data, HttpStatus.OK);
		} else {

			if (empDetails != null) {
				HashMap<Object, Object> data = new HashMap<>();
				data.put("Registered", true);
				data.put("Name", empDetails.getName());
				data.put("AssignTo",empDetails.getAssignTo());
				data.put("Company Name", empDetails.getCompanyName());
				data.put("Email ID", empDetails.getEmailId());
				data.put("App Link", "https://cutt.ly/taizoemployer");
				data.put("Web Link", "http://web.taizo.in/console/index.html");
				data.put("User Type", "Employer");

				return new ResponseEntity<>(data, HttpStatus.OK);
			} else {
				if (emp.isPresent()) {
					LeadModel l = emp.get();
					HashMap<Object, Object> data = new HashMap<>();
					data.put("Registered", l.isRegistered());
					data.put("Name", l.getName());
					data.put("AssignTo", l.getAssignTo());
					data.put("Company Name", l.getCompanyName());
					data.put("Email ID", l.getEmailId());
					data.put("App Link", "https://cutt.ly/taizoemployer");
					data.put("Web Link", "http://web.taizo.in/console/index.html");
					data.put("User Type", "Employer");

					return new ResponseEntity<>(data, HttpStatus.OK);
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 400);
					map.put("message", "User Not Found");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}
			}
		}
	}

	@GetMapping(path = "/states")
	public ResponseEntity<?> getIndiaSpecificStates(@RequestParam("country_id") final int countryId) {

		List<CfgStateModel> details = cfgStateRepository.findByCountryIdAndStatus(countryId, true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("states", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("states", details);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/cities")
	public ResponseEntity<?> getCities(@RequestParam("state_id") final int stateId) {

		List<CfgStateCityModel> details = istateCityRepository.findByStateId(stateId,true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("cities", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("cities", details);
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
			map.put("industries", details);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/jobRoles")
	public ResponseEntity<?> getFullTimeJobRoles(@RequestParam("industry_id") final int industryId) {

		List<JobRolesModel> details = jobRolesRepository.findByIndustryId(industryId);

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

	@GetMapping(path = "/jobs")
	public ResponseEntity<?> getJobs(@RequestParam("user_status") final String status,
			@RequestParam(value = "industry_id", required = false, defaultValue = "0") int industryId,
			@RequestParam(value = "jobrole_id", required = false, defaultValue = "0") int jobrolesId,
			@RequestParam(value = "city_id", required = false) final String cityId,
			@RequestParam(value = "exp", required = false, defaultValue = "0") int exp,
			@RequestParam(value = "state_id", required = false) final int stateId) {

		List<Map<String, Object>> details = jobRepository.findByMatchedJobs(status, industryId, jobrolesId, cityId,
				exp);
		if (!details.isEmpty() && details != null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("jobSize",details.size());
			map.put("jobs", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			/*
			 * if(jobrolesId!=0) { Optional<JobRolesModel> role =
			 * jobRolesRepository.findByIndustryIdandJobRole(industryId,jobrolesId);
			 * 
			 * List<CfgFullTimeGroup> group =
			 * fullTimeGroupingRepository.findByJobRole(industryId);
			 * 
			 * }
			 */
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Jobs Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	
	@GetMapping(path = "/newJobs")
	public ResponseEntity<?> getNewJobs(@RequestParam("user_status") final String status,
			@RequestParam(value = "industry_id", required = false, defaultValue = "0") int industryId,
			@RequestParam(value = "jobrole_id", required = false, defaultValue = "0") int jobrolesId,
			@RequestParam(value = "city", required = false) final String city,
			@RequestParam(value = "exp", required = false, defaultValue = "0") int exp,
			@RequestParam(value = "state_id", required = false) final int stateId) {
		
		String st = null;
		if(status.equalsIgnoreCase("Fresher")) {
			st = "F";
		}else {
			st = "E";
		}
		ArrayList<HashMap<String, String>> al = new ArrayList<>();

		List<Map<String, Object>> details = jobRepository.findByMatchedJobs(st, industryId, jobrolesId, city,
				exp);
		if (!details.isEmpty() && details != null) {
			
			for (Map<String, Object> j : details) {
				int jid = (Integer)j.get("job_id");
				String link = getJobLink(jid);
					HashMap<String, String> count = new HashMap<>();
					count.put("job_id", String.valueOf(j.get("job_id")));
					count.put("industry", (String) j.get("industry"));
					count.put("job_category",(String) j.get("job_category"));
					count.put("job_city", (String) j.get("job_city"));
					count.put("salary", (String) j.get("salary"));
					count.put("min_salary", String.valueOf( j.get("min_salary")));
					count.put("max_salary", String.valueOf( j.get("max_salary")));
					count.put("company_name", (String) j.get("company_name"));
					count.put("job_min_exp", String.valueOf( j.get("job_min_exp")));
					count.put("job_max_exp", String.valueOf( j.get("job_max_exp")));
					count.put("state", (String) j.get("state"));
					count.put("mobile_number", (String) j.get("mobile_number"));
					count.put("jobLink", link);
					al.add(count);
				
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("jobSize",al.size());
			map.put("jobs", al);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Jobs Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	
	@GetMapping(path = "/existingUserJobs")
	public ResponseEntity<?> getExistingUserNewJobs(@RequestParam("user_status") final String status,
			@RequestParam(value = "industry_id", required = false, defaultValue = "0") int industryId,
			@RequestParam(value = "jobrole_id", required = false, defaultValue = "0") int jobrolesId,
			@RequestParam(value = "city", required = false) final String city,
			@RequestParam(value = "exp", required = false, defaultValue = "0") int exp,
			@RequestParam(value = "state_id", required = false, defaultValue = "0") final int stateId) {
		
		String st = null;
		if(status.equalsIgnoreCase("Fresher")) {
			st = "F";
		}else {
			st = "E";
		}
		ArrayList<HashMap<String, String>> al = new ArrayList<>();

		List<Map<String, Object>> details = jobRepository.findByMatchedJobs(st, industryId, jobrolesId, city,
				exp);
		if (!details.isEmpty() && details != null) {
			int index = 0;
			for (Map<String, Object> j : details) {
				int jid = (Integer)j.get("job_id");
				String link = getJobLink(jid);
				int limit = 4;

				index++;
				if ( index < limit) {

					HashMap<String, String> count = new HashMap<>();
					count.put("job_id", String.valueOf(j.get("job_id")));
					count.put("industry", (String) j.get("industry"));
					count.put("job_category",(String) j.get("job_category"));
					count.put("job_city", (String) j.get("job_city"));
					count.put("salary", (String) j.get("salary"));
					count.put("min_salary", String.valueOf( j.get("min_salary")));
					count.put("max_salary", String.valueOf( j.get("max_salary")));
					count.put("company_name", (String) j.get("company_name"));
					count.put("job_min_exp", String.valueOf( j.get("job_min_exp")));
					count.put("job_max_exp", String.valueOf( j.get("job_max_exp")));
					count.put("state", (String) j.get("state"));
					count.put("mobile_number", (String) j.get("mobile_number"));
					count.put("jobLink", link);
					al.add(count);
				}
				
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("jobSize",al.size());
			map.put("jobs", al);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Jobs Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	
	public String getJobLink(int jobId) {
		String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseJSApiKey;

			DeeplinkRequest dl = new DeeplinkRequest();
			dl.setLongDynamicLink(firebaseJSHost + "/?link=" + firebaseJSHost + "/jobDetails/" + 0 + "/"
					+ jobId + "&apn=" + firebaseJSPackage);

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
	
	@PutMapping(path = "/updateJSWAAlert")
	public ResponseEntity<?> updateJSWAAlert(@RequestParam("phone_number") final long mobileNumber,
			@RequestParam(value = "wa_notification") final String waNoti) {

		CandidateModel details = candidateRepository.findByMobileNumber(mobileNumber);
		CanLeadModel lDetails = canLeadRepository.findByMobileNumber(mobileNumber);

		if (details!=null) {
			if (waNoti != null && !waNoti.isEmpty()) {
				if (waNoti.equalsIgnoreCase("yes")) {
					details.setWACampaign(true);
				} else if (waNoti.equalsIgnoreCase("ஆம்")) {
					details.setWACampaign(true);
				}  else if (waNoti.equalsIgnoreCase("हाँ")) {
					lDetails.setWACampaign(true);
				} else {
					details.setWACampaign(false);
				}
			}
			candidateRepository.save(details);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "Saved Succesfully");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else if(lDetails!=null) {
			if (waNoti != null && !waNoti.isEmpty()) {
				if (waNoti.equalsIgnoreCase("yes")) {
					lDetails.setWACampaign(true);
				} else if (waNoti.equalsIgnoreCase("ஆம்")) {
					lDetails.setWACampaign(true);
				} else if (waNoti.equalsIgnoreCase("हाँ")) {
					lDetails.setWACampaign(true);
				} else {
					lDetails.setWACampaign(false);
				}
			}
			canLeadRepository.save(lDetails);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "Saved Succesfully");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

}
