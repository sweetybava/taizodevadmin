package com.taizo.controller.employer;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.model.CertificateCoursesModel;
import com.taizo.model.CfgAllCourseModel;
import com.taizo.model.CfgItiCourseModel;
import com.taizo.model.CfgPGCourseModel;
import com.taizo.model.CfgUGCourseModel;
import com.taizo.model.DiplomaCourseModel;
import com.taizo.model.GraduationCoursesModel;
import com.taizo.model.KeySkillsModel;
import com.taizo.repository.CertificateCourseRepository;
import com.taizo.repository.CfgAllCoursesRepository;
import com.taizo.repository.CfgItiCourseRepository;
import com.taizo.repository.CfgPGCourseRepository;
import com.taizo.repository.CfgUGCourseRepository;
import com.taizo.repository.DiplomaCourseRepository;
import com.taizo.repository.GraduationCourseRepository;
import com.taizo.repository.KeySkillsRepository;

@CrossOrigin
@RestController
@RequestMapping("/employer")
public class EmpQualificationController {

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
	
	@Autowired
	CfgAllCoursesRepository cfgAllCourseRepository;
	
	@GetMapping(path = "/allCourses")
	public ResponseEntity<?> getAllCourses() {

		List<CfgAllCourseModel> details = cfgAllCourseRepository.findByEmpActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Courses Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/diplomaCourses")
	public ResponseEntity<?> getFullTimeJobRoless() {

		List<DiplomaCourseModel> details = diplomaCourseRepository.findByEmpActive(true);

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

		List<CfgUGCourseModel> details = cfgUGCourseRepository.findByEmpActive(true);

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

		List<CfgPGCourseModel> details = cfgPGCourseRepository.findByEmpActive(true);

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

		List<CfgItiCourseModel> details = cfgItiCourseRepository.findByEmpActive(true);

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

		List<CertificateCoursesModel> details = certificateCourseRepository.findAll();

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

		List<KeySkillsModel> details = keySkillsRepository.findEmpKeyskills();

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
