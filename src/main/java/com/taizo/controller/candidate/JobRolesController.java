package com.taizo.controller.candidate;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.model.CfgCanSources;
import com.taizo.model.FreeJobRolesModel;
import com.taizo.model.InternJobRolesModel;
import com.taizo.model.JobRolesModel;
import com.taizo.model.PartJobRolesModel;
import com.taizo.repository.CfgCanSourcesRepository;
import com.taizo.repository.FreeJobRolesRepository;
import com.taizo.repository.InternJobRolesRepository;
import com.taizo.repository.JobRolesRepository;
import com.taizo.repository.PartJobRolesRepository;

@CrossOrigin
@RestController
public class JobRolesController {

	@Autowired
	JobRolesRepository jobRolesRepository;
	
	@Autowired
	CfgCanSourcesRepository cfgCanSourcesRepository;

	@Autowired
	PartJobRolesRepository partJobRolesRepository;

	@Autowired
	FreeJobRolesRepository freeJobRolesRepository;

	@Autowired
	InternJobRolesRepository internJobRolesRepository;

	@GetMapping(path = "/fullTimeJobRoless")
	public ResponseEntity<?> getFullTimeJobRoles(@RequestParam(value = "industry_id",required=false,defaultValue="0") final int industryId) {

		List<JobRolesModel> details = jobRolesRepository.findAll(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Roles Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/fullTimeJobRoles")
	public ResponseEntity<?> getFullTimeJobRoless() {

		List<JobRolesModel> details = jobRolesRepository.findAll(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Job Roles Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/partTimeJobRoles")
	public ResponseEntity<?> getPartTimeJobRoless() {

		List<PartJobRolesModel> details = partJobRolesRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Job Roles Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/freelancerJobRoles")
	public ResponseEntity<?> getFreelancerJobRoles() {

		List<FreeJobRolesModel> details = freeJobRolesRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Job Roles Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/internJobRoles")
	public ResponseEntity<?> getInternJobRoless() {

		List<InternJobRolesModel> details = internJobRolesRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Job Roles Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping(path = "/canSources")
	public ResponseEntity<?> getcanSources() {

		List<CfgCanSources> details = cfgCanSourcesRepository.findByActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Job Roles Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

}
