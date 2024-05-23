package com.taizo.controller.candidate;

import java.io.IOException; 
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.taizo.exception.ErrorResponse;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.CanLeadModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.CloudwatchLogEventModel;
import com.taizo.model.UserModel;
import com.taizo.repository.CanLeadRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.UserRepository;
import com.taizo.service.CandidateService;
import com.taizo.service.CloudWatchLogService;
import com.taizo.service.UserService;
import com.taizo.service.UserServiceImpl;

@CrossOrigin
@RestController
public class UserController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	CanLeadRepository canLeadRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	CandidateService candidateService;

	@Autowired
	CloudWatchLogService cloudWatchLogService;


	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@PostMapping(path = "/CheckMobileNumber")
	public ResponseEntity<?> CheckMobileNo(@RequestParam("mobile_number") final long mobileNumber) {

		UserModel existingUser = userRepository.findByMobileNumber(mobileNumber);
		if (existingUser != null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Mobile Number Already Registered");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Mobile Number does not exists");
			return new ResponseEntity<>(map, HttpStatus.OK);

		}
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ResponseEntity<?> createUser(@RequestParam("first_name") final String firstName,
			@RequestParam("industry") final String industry, @RequestParam("password") final String password,
			@RequestParam("mobile_number") final long mobileNumber, @RequestParam("job_type") final String jobType,
			@RequestParam("country") final String prefCountry, @RequestParam("city") final String city,
			@RequestParam(value = "candidate_location", required = false) final String candidateLocation,
			@RequestParam("candidate_type") final String candidateType,
			@RequestParam(value = "pref_dom_location", required = false) final String prefDomLocation,
			@RequestParam(value = "pref_over_location", required = false) final String prefOverLocation,
			@RequestParam("job_category") final String jobCategory,
			@RequestParam(value = "student", required = false) final String student,
			@RequestParam(value = "pref_location", required = false) final String prefLocation,
			@RequestParam(value = "experience_years", required = false) final Integer experienceYears,
			@RequestParam(value = "experience_months", required = false) final Integer experienceMonths,
			@RequestParam(value = "device_token", required = false) String deviceToken,
			@RequestParam(value = "wa_campaign", required = false) boolean waCampaign,

			HttpServletRequest request) throws ResourceNotFoundException {

		UserModel userExists = userService.findByMobileNo(mobileNumber);
		if (userExists != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Mobile Number Already exists");
			map.put("results", null);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		} else {

			DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
			String date = formatter.format(new Date());
			SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
			Calendar cal = Calendar.getInstance();
			String time = simpleDateFormat1.format(cal.getTime());


			String pass = passwordEncoder.encode(password);

			UserModel user = new UserModel();

			user.setFirstName(firstName);
			user.setMobileNumber(mobileNumber);
			user.setPassword(pass);
			user.setCountryCode(prefCountry);
			user.setDeleted(false);

			String token = UUID.randomUUID().toString();
			user.setToken(token);

			userRepository.save(user);

			int userID = user.getId();

			CandidateModel candidate = new CandidateModel();

			candidate.setUserId(userID);
			candidate.setDeleted(false);
			candidate.setFirstName(firstName);
			candidate.setIndustry(industry);
			candidate.setMobileNumber(mobileNumber);
			candidate.setJobType(jobType);
			candidate.setCity(city);
			candidate.setPrefLocation(prefLocation);
			candidate.setState("Tamil Nadu");
			candidate.setCandidateLocation(candidateLocation);
			candidate.setDomesticLocation(prefDomLocation);
			candidate.setOverseasLocation(prefOverLocation);
			candidate.setCandidateType(candidateType);
			candidate.setJobCategory(jobCategory);
			candidate.setPrefCountry(prefCountry);
			candidate.setStudent(student);
			candidate.setExperience(experienceYears);
			candidate.setExpMonths(experienceMonths);

			candidate.setAmount(30);
			candidate.setPaymentStatus("Paid");
			candidate.setFcmToken(deviceToken);
			candidate.setRegistered(true);
			candidate.setWACampaign(waCampaign);
			candidate.setRegInApp(true);

			candidateRepository.save(candidate);

			CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
			logEventModel.setType("Profile");
			logEventModel.setMessage("success");
			logEventModel.setDescription(candidate.toString());

			try {
				cloudWatchLogService.cloudLog(logEventModel, "C");
			} catch (Exception e) {

			}

			Optional<CandidateModel> optional = candidateRepository.findByUserId(userID);
			if (!optional.isPresent()) {
				CloudwatchLogEventModel logEventModel1 = new CloudwatchLogEventModel();
				logEventModel1.setType("Profile");
				logEventModel1.setMessage("failure");
				logEventModel1.setDescription(user.toString());

				try {
					cloudWatchLogService.cloudLogFailure(logEventModel1, "C");
				} catch (Exception e) {

				}


				userService.deleteById(userID);

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "Failed to Register");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			} else {
				CandidateModel c = optional.get();
				// passing candidate id to user value
				user.setCountryCode(String.valueOf(c.getId()));

				List<CanLeadModel> leadMN = canLeadRepository.findByMobileNumberList(mobileNumber);

				if (leadMN.size() > 0) {
					for (CanLeadModel l : leadMN) {
						try {
							candidateService.deleteById(l.getId());
						} catch (ResourceNotFoundException e) {
							e.printStackTrace();
						}
					}
				}

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 201);
				map.put("message", "Registered Successfully");
				map.put("results", user);
				return new ResponseEntity<>(map, HttpStatus.CREATED);
			}
		}

	}

	@RequestMapping(value = "/mobileRegister", method = RequestMethod.POST)
	public ResponseEntity<?> createUserUsingMobNum(@RequestParam("mobile_number") final long mobileNumber,
			@RequestParam("country") final String prefCountry,
			@RequestParam(value = "device_token", required = false) String deviceToken, HttpServletRequest request)
			throws ResourceNotFoundException {

		UserModel userExists = userService.findByMobileNo(mobileNumber);
		if (userExists != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Mobile Number Already exists");
			map.put("results", null);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		} else {

			UserModel user = new UserModel();

			user.setMobileNumber(mobileNumber);
			user.setCountryCode(prefCountry);
			user.setDeleted(false);

			userRepository.save(user);

			int userID = user.getId();

			CandidateModel candidate = new CandidateModel();

			candidate.setUserId(userID);
			candidate.setDeleted(false);
			candidate.setMobileNumber(mobileNumber);
			candidate.setPrefCountry(prefCountry);
			candidate.setFcmToken(deviceToken);
			candidate.setRegistered(true);
			candidate.setRegInApp(true);

			candidateRepository.save(candidate);

			Optional<CandidateModel> optional = candidateRepository.findByUserId(userID);
			if (!optional.isPresent()) {
				CloudwatchLogEventModel logEventModel1 = new CloudwatchLogEventModel();
				logEventModel1.setType("Profile");
				logEventModel1.setMessage("failure");
				logEventModel1.setDescription(user.toString());

				try {
					cloudWatchLogService.cloudLogFailure(logEventModel1, "C");
				} catch (Exception e) {

				}

				userService.deleteById(userID);

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "Failed to Register");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			} else {
				CandidateModel c = optional.get();
				// passing candidate id to user value

				List<CanLeadModel> leadMN = canLeadRepository.findByMobileNumberList(mobileNumber);

				if (leadMN.size() > 0) {
					for (CanLeadModel l : leadMN) {
						try {
							candidateService.deleteById(l.getId());
						} catch (ResourceNotFoundException e) {
							e.printStackTrace();
						}
					}
				}

				user.setCountryCode(String.valueOf(c.getId()));

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 201);
				map.put("message", "Registered Successfully");
				map.put("results", user);
				return new ResponseEntity<>(map, HttpStatus.CREATED);
			}
		}

	}

	@RequestMapping(value = "/updateName", method = RequestMethod.PUT)
	public ResponseEntity<?> createUserUsingName(@RequestParam("mobile_number") final long mobileNumber,
			@RequestParam("first_name") final String firstName,
			@RequestParam(value = "wa_campaign", required = false) boolean waCampaign, HttpServletRequest request)
			throws ResourceNotFoundException {

		UserModel userExists = userService.findByMobileNo(mobileNumber);
		if (userExists != null) {

			userExists.setFirstName(firstName);
			String token = UUID.randomUUID().toString();
			userExists.setToken(token);

			userRepository.save(userExists);

			CandidateModel candidate = candidateRepository.finduser(userExists.getId());

			candidate.setFirstName(firstName);
			candidate.setState("Tamil Nadu");
			candidate.setAmount(30);
			candidate.setPaymentStatus("Paid");
			candidate.setWACampaign(waCampaign);

			candidateRepository.save(candidate);

			// passing candidate id to user value
			userExists.setCountryCode(String.valueOf(candidate.getId()));


			List<CanLeadModel> leadMN = canLeadRepository.findByMobileNumberList(mobileNumber);

			if (leadMN.size() > 0) {
				for (CanLeadModel l : leadMN) {
					try {
						candidateService.deleteById(l.getId());
					} catch (ResourceNotFoundException e) {
						e.printStackTrace();
					}
				}
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Registered Successfully");
			map.put("results", userExists);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Mobile Number does not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}

	}

	@PutMapping(path = "/updateProfilePic")
	public ResponseEntity<?> updateProfilePic(@RequestParam("user_id") final int id,
			@RequestParam("profile_pic") final String profilePic) {
		Optional<UserModel> optional = userRepository.findById(id);

		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Failed to Update");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {

			userService.updateProfilePic(id, profilePic);

			DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
			String date = formatter.format(new Date());
			SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
			Calendar cal = Calendar.getInstance();
			String time = simpleDateFormat1.format(cal.getTime());
			
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Successfully Updated");
			return new ResponseEntity<>(map, HttpStatus.OK);

		}
	}

	@PostMapping(value = "/filePic")
	public ResponseEntity<?> setPicture(@RequestParam("user_id") final int id,
			@RequestPart(value = "file") MultipartFile file) {

		Optional<UserModel> optional = userRepository.findById(id);

		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {

			if (file != null && !file.isEmpty()) {

				UserModel c = optional.get();

				String pic1 = c.getProfilePic();

				if (pic1 != null && !pic1.isEmpty()) {

					String fp = pic1.substring(49);

					this.userService.deleteFileFromS3Bucket(fp);

				}

			}

			String url = this.userService.uploadProfilePicToS3Bucket(file, id, true);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "file [" + file.getOriginalFilename() + "] uploading request submitted successfully.");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	@PostMapping(path = "/setProfilePic")
	public ResponseEntity<?> setProfilePic(@RequestParam("user_id") final int id,
			@RequestPart(name = "profile_pic") MultipartFile profilePic) throws IOException {
		Optional<UserModel> optional = userRepository.findById(id);

		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Failed to set Profile Pic");
			map.put("results", null);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {
			UserModel c = optional.get();

			if (profilePic != null && !profilePic.isEmpty()) {

				String pic = c.getProfilePic();

				if (pic != null && !pic.isEmpty()) {

					boolean imageResult = userService.deleteImage(pic);

				}

			}

			if (profilePic != null && !profilePic.isEmpty()) {
				String profile = userService.uploadFile(profilePic, id, profilePic.getBytes());
				if (profile != null && !profile.isEmpty()) {
					c.setProfilePic(profile);

					userRepository.save(c);

					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", "Successfully Updated");
					return new ResponseEntity<>(map, HttpStatus.OK);

				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 400);
					map.put("message", "Picture Not Saved");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Picture Not Saved");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@GetMapping(path = "/profilePic")
	public ResponseEntity<?> getProfilePic(@RequestParam("user_id") final int userId) {

		Optional<UserModel> details = userRepository.findById(userId);

		if (details.isPresent()) {

			UserModel existing = details.get();

			String pp = existing.getProfilePic();

			if (pp != null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "success");
				map.put("results", details);
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "Profile Pic is not Found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/employer/profilePic")
	public ResponseEntity<?> getUserProfilePic(@RequestParam("user_id") final int userId) {

		Optional<UserModel> details = userRepository.findById(userId);

		if (details.isPresent()) {

			UserModel existing = details.get();

			String pp = existing.getProfilePic();

			if (pp != null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("status", "success");
				map.put("message", "success");
				map.put("data", details);
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "Profile Pic is not Found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(path = "/login", consumes = "application/json")
	public ResponseEntity<?> userLogin(@RequestParam("mobile_number") final long mobileNumber,
			@RequestParam("password") final String password, @RequestParam("country") final String country) {

		UserModel existingUser = userRepository.findByMobileNumber(mobileNumber);
		if (existingUser != null) {

			if (passwordEncoder.matches(password, existingUser.getPassword())) {

				Optional<UserModel> check = userService.login(mobileNumber, existingUser.getPassword(), country);

				if (check != null) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", "Login Successfully");
					map.put("results", check);
					return new ResponseEntity<>(map, HttpStatus.OK);
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 400);
					map.put("message", "Incorrect Country Code");
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
			map.put("message", "Mobile Number does not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@GetMapping(path = "/otpLogin")
	public ResponseEntity<?> userOtpLogin(@RequestParam("mobile_number") final long mobileNumber) {

		UserModel existingUser = userRepository.findByMobileNumber(mobileNumber);
		if (existingUser != null) {
			CandidateModel details = candidateRepository.finduser(existingUser.getId());

			String token = UUID.randomUUID().toString();
			existingUser.setToken(token);
			userRepository.save(existingUser);

			details.setRegInApp(true);
			candidateRepository.save(details);

			existingUser.setCountryCode(String.valueOf(details.getId()));

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Login Successfully");
			map.put("results", existingUser);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Mobile Number does not exists");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@PostMapping("/logout")
	public ResponseEntity<?> getLogout(@RequestParam("token") final String token) {

		Optional<UserModel> customer = userRepository.findByToken(token);

		if (customer.isPresent()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Logout Successfully");
			map.put("results", userService.findLogout(token));
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
		HashMap<String, Object> map = new HashMap<>();
		map.put("statuscode", 400);
		map.put("message", "token mismatched");
		return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
	}

	@PutMapping(value = "/updatePassword")
	public ResponseEntity<?> updatePassword(@RequestParam("mobile_number") final long mobileNumber,
			@RequestParam("password") String password) {

		Optional<UserModel> optional = userRepository.findByMobNumber(mobileNumber);

		if (!optional.isPresent()) {
			ErrorResponse errorResponse = new ErrorResponse();
			errorResponse.setStatus(false);
			errorResponse.setMessage("Mobile Number does not exists");
			return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
		} else {

			UserModel existing = optional.get();

			String pass = passwordEncoder.encode(password);

			existing.setPassword(pass);

			userRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Successfully Updated");
			return new ResponseEntity<>(map, HttpStatus.OK);

		}
	}

	@PostMapping(path = "/dobValidation")
	public ResponseEntity<?> DOBValidation(@RequestParam("user_id") final int userId,
			@RequestParam("dob") final String dob) {

		CandidateModel details = candidateRepository.finduser(userId);
		if (details != null) {

			CandidateModel check = candidateRepository.findDob(userId, dob);

			if (check != null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", true);
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", false);
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User is not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@PostMapping(path = "/fatherNameValidation")
	public ResponseEntity<?> fatherNameValidation(@RequestParam("mobile_number") final long mobileNumber,
			@RequestParam("father_name") final String fatherName) {

		Optional<UserModel> optional = userRepository.findByMobNumber(mobileNumber);

		if (optional.isPresent()) {
			UserModel user = optional.get();
			CandidateModel details = candidateRepository.finduser(user.getId());
			if (details != null) {

				CandidateModel check = candidateRepository.findFatherName(user.getId(), fatherName);

				if (check != null) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", true);
					map.put("re", user);
					return new ResponseEntity<>(map, HttpStatus.OK);
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 400);
					map.put("message", false);
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

				}

			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "Candidate is not found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User is not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(value = "/deleteAccount")
	public ResponseEntity<?> deleteAccount(@RequestParam("user_id") final int userId, @RequestParam("pin") String pin) {

		Optional<UserModel> details1 = userRepository.findById(userId);
		CandidateModel details = candidateRepository.finduser(userId);

		if (details1.isPresent() && details != null) {

			UserModel user = details1.get();
			if (passwordEncoder.matches(pin, user.getPassword())) {
				user.setDeleted(true);
				details.setDeleted(true);
				userRepository.save(user);
				candidateRepository.save(details);

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Successfully Deleted");
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "Incorrect Password");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	@DeleteMapping(value = "/del")
	public Map<String, String> deleteFile(@RequestParam("file_names") String[] files) {

		try {
			List<String> fileNames = new ArrayList<>();

			Arrays.asList(files).stream().forEach(file -> {
				this.userService.deleteFileFromS3Bucket(file);
				fileNames.add(file);
			});

			Map<String, String> response = new HashMap<>();
			response.put("message", "file [" + fileNames + "] removing request submitted successfully.");

			return response;
		} catch (Exception e) {
			Map<String, String> response = new HashMap<>();
			response.put("message", "Fail to delete files!");
			logger.error("error [" + e.getMessage() + "] occurred while deleting [" + files + "] ");

			CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
			logEventModel.setType("Profile");
			logEventModel.setMessage("failure");
			logEventModel.setDescription("error [" + e.getMessage() + "] occurred while deleting [" + files + "] ");

			try {
				cloudWatchLogService.cloudLogFailure(logEventModel, "C");
			} catch (Exception e1) {

			}

			return response;
		}
	}

}
