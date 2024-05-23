package com.taizo.controller.candidate;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.model.CertificateCoursesModel;
import com.taizo.model.CfgItiCourseModel;
import com.taizo.model.CfgPGCourseModel;
import com.taizo.model.CfgUGCourseModel;
import com.taizo.model.DiplomaCourseModel;
import com.taizo.model.GraduationCoursesModel;
import com.taizo.model.KeySkillsModel;
import com.taizo.repository.CertificateCourseRepository;
import com.taizo.repository.CfgItiCourseRepository;
import com.taizo.repository.CfgPGCourseRepository;
import com.taizo.repository.CfgUGCourseRepository;
import com.taizo.repository.DiplomaCourseRepository;
import com.taizo.repository.GraduationCourseRepository;
import com.taizo.repository.KeySkillsRepository;

@CrossOrigin
@RestController
public class QualificationController {

	@Autowired
	DiplomaCourseRepository diplomaCourseRepository;
	
	@Autowired
	CfgPGCourseRepository cfgPGCourseRepository;
	
	@Autowired
	CfgUGCourseRepository cfgUGCourseRepository;
	
	@Autowired
	CfgItiCourseRepository cfgItiCourseRepository;

	@Autowired
	CertificateCourseRepository certificateCourseRepository;

	@Autowired
	GraduationCourseRepository graduationCourseRepository;

	@Autowired
	KeySkillsRepository keySkillsRepository;

	@GetMapping(path = "/diplomaCourses")
	public ResponseEntity<?> getFullTimeJobRoless() {

		List<DiplomaCourseModel> details = diplomaCourseRepository.findByJSActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Diploma Course Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/UGCourses")
	public ResponseEntity<?> getUGCourses() {

		List<CfgUGCourseModel> details = cfgUGCourseRepository.findByJSActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "UG Courses Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	@GetMapping(path = "/PGCourses")
	public ResponseEntity<?> getPGCourses() {

		List<CfgPGCourseModel> details = cfgPGCourseRepository.findByJSActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "UG Courses Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	@GetMapping(path = "/ITICourses")
	public ResponseEntity<?> getITICourses() {

		List<CfgItiCourseModel> details = cfgItiCourseRepository.findByJSActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "UG Courses Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	@GetMapping(path = "/certificateCourses")
	public ResponseEntity<?> getPartTimeJobRoles() {

		List<CertificateCoursesModel> details = certificateCourseRepository.findByJsOrder();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Certificate Course Not Found");
			map.put("results", null);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/graduationCourses")
	public ResponseEntity<?> getFreelancerJobRoless() {

		List<GraduationCoursesModel> details = graduationCourseRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Graduation Course Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/keySkills")
	public ResponseEntity<?> getKeySkills() {

		List<KeySkillsModel> details = keySkillsRepository.findJsKeyskills();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Key Skills Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

}
