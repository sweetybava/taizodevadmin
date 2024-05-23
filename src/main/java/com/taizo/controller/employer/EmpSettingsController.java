package com.taizo.controller.employer;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.EmployerFeedbackModel;
import com.taizo.model.EmployerModel;
import com.taizo.repository.EmployerFeedbackRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.FeedbackRepository;

@CrossOrigin
@RestController
@RequestMapping("/employer")
public class EmpSettingsController {


	@Autowired
	EmployerFeedbackRepository employerFeedbackRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostMapping(path = "/feedback")
	public ResponseEntity<?> updateLocation(@RequestParam("emp_id") final int employer_id,
			@RequestParam("message") final String message) {

		Optional<EmployerModel> optional = employerRepository.findById(employer_id);

		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {

			EmployerFeedbackModel empfeedback = new EmployerFeedbackModel();
			empfeedback.setEmpId(employer_id);
			empfeedback.setMessage(message);
			empfeedback.setModule("App");
			employerFeedbackRepository.save(empfeedback);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	@PutMapping(path = "/changePassword")
	public ResponseEntity<?> updatePAssword(@RequestParam("emp_id") final int employer_id,
			@RequestParam("old_password") final String oldPass, @RequestParam("new_password") final String newPass)
			throws ResourceNotFoundException {

		Optional<EmployerModel> optional = employerRepository.findById(employer_id);

		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

		EmployerModel existing = optional.get();

		if (passwordEncoder.matches(oldPass, existing.getPassword())) {
			String pass = passwordEncoder.encode(newPass);
			existing.setPassword(pass);
			employerRepository.save(existing);
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Password changed successfully");
			return ResponseEntity.status(HttpStatus.OK).body(map);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Old Password is wrong.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
		}
	}

	@GetMapping(path = "/links")
	public ResponseEntity<?> getLinks() {
		HashMap<String, Object> detailMap = new HashMap<>();

		detailMap.put("Privacy Policy", "https://taizo-common.s3.ap-south-1.amazonaws.com/Policies/privacypolicy.html");
		detailMap.put("Terms of Service", "https://taizo-common.s3.ap-south-1.amazonaws.com/Policies/Termsofservice.html");
		
		HashMap<String, Object> map = new HashMap<>();
		map.put("status", "success");
		map.put("message", "success");
		map.put("code", 200);
		map.put("data", detailMap);
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@PutMapping(path = "/notificationPermissions")
	public ResponseEntity<?> notificationPermissions(@RequestParam("emp_id") final int employer_id,
			@RequestParam("push_noti") final boolean push,
			@RequestParam("email_noti") final boolean email,
			@RequestParam("whatsapp_noti") final boolean whatsapp,
			@RequestParam("noti_sound") final boolean notiSound) {

		Optional<EmployerModel> optional = employerRepository.findById(employer_id);

		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {
			EmployerModel existing = optional.get();
			existing.setPushNotification(push);
			existing.setEmailNotification(email);
			existing.setWhatsappNotification(whatsapp);
			existing.setNotificationSound(notiSound);
			
			employerRepository.save(existing);
			
		HashMap<String, Object> map = new HashMap<>();
		map.put("code", 200);
		map.put("message", "Updated successfully");
		return ResponseEntity.status(HttpStatus.OK).body(map);
		}

	}

}
