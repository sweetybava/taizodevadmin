package com.taizo.controller.candidate;

import java.net.URISyntaxException; 
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

import com.taizo.model.*;
import com.taizo.repository.CandidateTimeLineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.taizo.exception.ResourceNotFoundException;
import com.taizo.repository.CanLeadRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.UserRepository;
import com.taizo.service.CandidateService;
import com.taizo.service.CloudWatchLogService;
import com.taizo.service.UserService;

@CrossOrigin
@RestController
public class NewRegController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired 
	CanLeadRepository canLeadRepository;

	@Autowired
	private UserService userService;

	@Autowired
	CandidateService candidateService;

	@Autowired
	CloudWatchLogService cloudWatchLogService;

	@Autowired
	CandidateTimeLineRepository candidateTimeLineRepository;


	@GetMapping(path = "/MobileNumberExists")
	public ResponseEntity<?> CheckMobileNo(@RequestParam("mobile_number") final long mobileNumber,
			@RequestParam("country_code") final String country, @RequestParam("language") final String language,
			@RequestParam("device_token") final String token) {

		CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mobileNumber);
		if (existingUser != null) {
			int page = existingUser.getProfilePageNo();

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("verified", existingUser.isMnverified());
			map.put("pageNo", page);
			map.put("message", "Mobile Number Not Registered");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		} else {

			UserModel existing = userRepository.findByMobileNumber(mobileNumber);
			if (existing != null) {

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Mobile Number Already Registered");
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {
				CanLeadModel user = new CanLeadModel();

				user.setMobileNumber(mobileNumber);
				user.setCountryCode(country);
				user.setLanguageKey(language);
				user.setFcmToken(token);
				user.setMnverified(false);
				user.setAge(0);
				user.setExpYears(0);
				user.setExpMonths(0);
				user.setProfilePageNo(0);
				user.setFromApp(true);
				user.setFromWA(false);

				canLeadRepository.save(user);

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("verified", false);
				map.put("pageNo", 0);
				map.put("message", "Mobile Number does not exists");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			}

		}
	}

	@PutMapping(path = "/MNVerified")
	public ResponseEntity<?> CheckMobileNoVerified(@RequestParam("mobile_number") final long mobileNumber,
			@RequestParam("verified") final boolean verified) {

		CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mobileNumber);
		if (existingUser != null) {
			existingUser.setMnverified(verified);
			canLeadRepository.save(existingUser);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("verified", existingUser.isMnverified());
			map.put("pageNo", existingUser.getProfilePageNo());
			map.put("message", "Verified Successfully");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("verified", false);
			map.put("pageNo", 0);
			map.put("message", "Mobile Number does not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@PutMapping(path = "/basicDetails")
	public ResponseEntity<?> basicDetails(@RequestParam("mobile_number") final long mobileNumber,
			@RequestParam("name") final String name, @RequestParam("dob") final String dob,
			@RequestParam("age") final int age, @RequestParam("gender") final String gender,
			@RequestParam("state") final String state, @RequestParam("city") final String city) {

		CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mobileNumber);
		if (existingUser != null) {
			existingUser.setName(name);
			existingUser.setDateOfBirth(dob);
			existingUser.setGender(gender);
			existingUser.setCountry("India");
			existingUser.setState(state);
			existingUser.setCity(city);
			existingUser.setAge(age);
			existingUser.setAssignTo(1);
			existingUser.setProfilePageNo(1);
			canLeadRepository.save(existingUser);
			
			CandidateTimeLine candidateTimeLine = new CandidateTimeLine();
			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
			String formattedDate = dateFormat.format(currentDate);
			String eventDescription = "Lead Generation From App "+ formattedDate;
			candidateTimeLine.setCanId(0);
			candidateTimeLine.setEventName("Lead generation");
			candidateTimeLine.setEventDescription(eventDescription);
			candidateTimeLineRepository.save(candidateTimeLine);

			DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
			String date = formatter.format(new Date());
			SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
			Calendar cal = Calendar.getInstance();
			String time = simpleDateFormat1.format(cal.getTime());

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Updated Successfully");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Mobile Number does not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}
	
	@PutMapping(path = "/profilePicture")
	public ResponseEntity<?> updatePic(@RequestParam("mobile_number") final long mobileNumber,
			@RequestParam("profile_pic") final String profilePic) {

		CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mobileNumber);
		if (existingUser != null) {
			existingUser.setProfilePic(profilePic);
			existingUser.setProfilePageNo(2);
			canLeadRepository.save(existingUser);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Updated Successfully");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Mobile Number does not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}
	
	@PostMapping(value = "/userProfilePic")
	public ResponseEntity<?> setPicture(@RequestParam("mobile_number") final long mobileNumber,
			@RequestPart(value = "file") MultipartFile file) {

		CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mobileNumber);

		if (existingUser == null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {

			if (file != null && !file.isEmpty()) {


			String url = this.userService.uploadProfilePicToS3Bucket1(file, mobileNumber, true);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("Image URL", url);
			map.put("message", "file [" + file.getOriginalFilename() + "] uploading request submitted successfully.");
			return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "File not found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		}
	}

	@PutMapping(path = "/educationDetails")
	public ResponseEntity<?> educationDetails(@RequestParam("mobile_number") final long mobileNumber,
			@RequestParam("qualification") final String quali,
			@RequestParam(value = "specialization", required = false) final String speciali,
			@RequestParam(value = "known_languages", required = false) final String known,
			@RequestParam(value = "student", required = false) final String student,
			@RequestParam(value = "passed_out_year", defaultValue = "0") final int year,
			@RequestParam(value = "passed_out_month", defaultValue = "0") final int month,
			@RequestParam(value = "cerification_courses", required = false) final String certi,
			@RequestParam(value = "pf_esi_account", required = false) final String pfAccount,
			@RequestParam(value = "arrears", required = false) final String arrears) {

		CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mobileNumber);
		if (existingUser != null) {
			existingUser.setQualification(quali);
			existingUser.setSpecification(speciali);
			existingUser.setKnownLanguages(known);
			existingUser.setCourses(certi);
			existingUser.setStudent(student);
			existingUser.setPassed_out_year(year);
			existingUser.setPassed_out_month(month);
			existingUser.setPfEsiAccount(pfAccount);
			existingUser.setIsHavingArrear(arrears);
			existingUser.setProfilePageNo(3);

			canLeadRepository.save(existingUser);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Updated Successfully");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Mobile Number does not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@PutMapping(path = "/workDetails")
	public ResponseEntity<?> workDetails(@RequestBody CanLeadModel canLead) {

		CanLeadModel existingUser = canLeadRepository.findByMobileNumber(canLead.getMobileNumber());
		if (existingUser != null) {
			existingUser.setExpInManufacturing(canLead.isExpInManufacturing());
			existingUser.setExperienced(canLead.isExperienced());
			existingUser.setIndustry(canLead.getIndustry());
			existingUser.setJobCategory(canLead.getJobCategory());
			existingUser.setKeySkill(canLead.getKeySkill());
			existingUser.setExpYears(canLead.getExpYears());
			existingUser.setExpMonths(canLead.getExpMonths());
			existingUser.setProfilePageNo(4);

			if (canLead.isExperienced()) {
				existingUser.setCandidateType("Experienced");
			} else {
				existingUser.setCandidateType("Fresher");
			}

			canLeadRepository.save(existingUser);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Updated Successfully");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Mobile Number does not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@PutMapping(path = "/jobLocation")
	public ResponseEntity<?> updateJobLocation(@RequestParam("mobile_number") final long mobileNumber,
			@RequestParam("job_location") final String location,
			@RequestParam(value = "reference", required = false) final String reference,
			@RequestParam("wa_campaign") final boolean waCampaign) {

		CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mobileNumber);
		if (existingUser != null) {
			existingUser.setJobLocation(location);
			existingUser.setProfilePageNo(5);
			existingUser.setWACampaign(waCampaign);
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
			candidate.setDeleted(false);
			candidate.setFirstName(existingUser.getName());
			candidate.setMobileNumber(existingUser.getMobileNumber());
			candidate.setWhatsappNumber(existingUser.getMobileNumber());
			candidate.setContactNumber(String.valueOf(existingUser.getMobileNumber()));
			candidate.setContactNumber(existingUser.getContactNumber());

			candidate.setDateOfBirth(existingUser.getDateOfBirth());
			candidate.setAge(String.valueOf(existingUser.getAge()));
			candidate.setGender(existingUser.getGender());
			candidate.setPrefCountry("India");
			candidate.setState(existingUser.getState());
			candidate.setCity(existingUser.getJobLocation());
			candidate.setCurrentCountry(existingUser.getCountry());
			candidate.setCurrentState(existingUser.getState());
			candidate.setCurrentCity(existingUser.getCity());
			candidate.setAssignTo(existingUser.getAssignTo());
			candidate.setQualification(existingUser.getQualification());
			candidate.setSpecification(existingUser.getSpecification());
			candidate.setCertificationCourses("Certification Courses");
			candidate.setCertificationSpecialization(existingUser.getCourses());

			candidate.setJobType("Full Time (8hrs to 10hrs)");
			candidate.setCandidateLocation("Domestic");
			candidate.setIndustry(existingUser.getIndustry());
			candidate.setJobCategory(existingUser.getJobCategory());
			candidate.setKeySkill(existingUser.getKeySkill());
			candidate.setExperience(existingUser.getExpYears());
			candidate.setExpMonths(existingUser.getExpMonths());

			candidate.setAmount(0);
			//candidate.setJobLimit(2);
			//candidate.setDiscountAmount(50);
			//candidate.setPaymentStatus("Paid");
			candidate.setFcmToken(existingUser.getFcmToken());
			candidate.setLanguageKey(existingUser.getLanguageKey());
			candidate.setRegistered(true);
			candidate.setProfileFilled(true);
			candidate.setWACampaign(existingUser.isWACampaign());
			candidate.setRegInApp(true);
			candidate.setLookingForaJob(existingUser.isLookingForaJob());
			candidate.setFromApp(existingUser.isFromApp());
			candidate.setFromWA(existingUser.isFromWA());
			candidate.setReference(reference);
			candidate.setStudent(existingUser.getStudent());
			candidate.setPassed_out_year(existingUser.getPassed_out_year());
			candidate.setPassed_out_month(existingUser.getPassed_out_month());

			try {
			if(existingUser.getJobCategory().equalsIgnoreCase("Trainee")) {
				candidate.setCandidateType("Fresher");
				candidate.setExperienced(false);
				candidate.setExpInManufacturing(false);
			}else if(existingUser.getJobCategory().equalsIgnoreCase("Assembler")) {
				candidate.setCandidateType("Fresher");
				candidate.setExperienced(false);
				candidate.setExpInManufacturing(false);
			}else if(existingUser.getJobCategory().equalsIgnoreCase("Graduate Trainee")) {
				candidate.setCandidateType("Fresher");
				candidate.setExperienced(false);
				candidate.setExpInManufacturing(false);
			}else {
			if (!existingUser.isExperienced()) {
				candidate.setCandidateType("Fresher");
				candidate.setExperienced(false);
				candidate.setExpInManufacturing(false);
			} else {
				candidate.setCandidateType("Experienced");
				candidate.setExperienced(true);
				candidate.setExpInManufacturing(true);

			}
			}}catch(Exception e) {
				if (!existingUser.isExperienced()) {
					candidate.setCandidateType("Fresher");
					candidate.setExperienced(false);
					candidate.setExpInManufacturing(false);
				} else {
					candidate.setCandidateType("Experienced");
					candidate.setExperienced(true);
					candidate.setExpInManufacturing(true);

				}
			}
			String languages = existingUser.getKnownLanguages();
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
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			candidate.setProfileLastUpdatedDt(dtf.format(now));
			
			candidateRepository.save(candidate);

			CandidateTimeLine candidateTimeLine = new CandidateTimeLine();
			candidateTimeLine.setCanId(candidate.getId());
			candidateTimeLine.setEventName("Registration");
			candidateTimeLine.setEventDescription("<b>" +candidate.getFirstName()+ "</b> New Candidate Registered on"+candidate.getCreatedTime());
			candidateTimeLineRepository.save(candidateTimeLine);

			Optional<CandidateModel> optional = candidateRepository.findByUserId(userID);
			if (!optional.isPresent()) {
				CloudwatchLogEventModel logEventModel1 = new CloudwatchLogEventModel();
				logEventModel1.setType("Profile");
				logEventModel1.setMessage("failure");
				logEventModel1.setDescription(user.toString()+candidate.toString());

				try {
					cloudWatchLogService.cloudLog(logEventModel1, "C");
				} catch (Exception e) {

				}

				try {
					userService.deleteById(userID);
				} catch (ResourceNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "Please Try Again Later");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			} else {
				DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
				String date = formatter.format(new Date());
				SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
				Calendar cal = Calendar.getInstance();
				String time = simpleDateFormat1.format(cal.getTime());

				CandidateModel c = optional.get();
				// passing candidate id to user value
	
			List<CanLeadModel> leadMN = canLeadRepository.findByMobileNumberList(existingUser.getMobileNumber());

			if (leadMN.size() > 0) {
				for (CanLeadModel l : leadMN) {
					try {
						candidateService.deleteById(l.getId());
					} catch (ResourceNotFoundException e) {
						e.printStackTrace();
					}
				}
			}

			user.setCountryCode(String.valueOf(candidate.getId()));

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", user);
			return new ResponseEntity<>(map, HttpStatus.OK);
			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Mobile Number does not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}
	
	@GetMapping(path = "/getLeadDetails")
	public ResponseEntity<?> getLeadDetails(@RequestParam("mobile_number") final long mobileNumber) {
		CandidateModel details = candidateRepository.findByMobileNumber(mobileNumber);
		CanLeadModel lDetails = canLeadRepository.findByMobileNumber(mobileNumber);
		
		if (lDetails != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", lDetails);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}else if (details != null) {
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

}
