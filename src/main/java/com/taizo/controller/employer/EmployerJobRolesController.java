package com.taizo.controller.employer;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.model.InternJobRolesModel;
import com.taizo.model.JobRolesModel;
import com.taizo.model.PartJobRolesModel;
import com.taizo.repository.InternJobRolesRepository;
import com.taizo.repository.JobRolesRepository;
import com.taizo.repository.PartJobRolesRepository;

@CrossOrigin
@RestController
@RequestMapping("/employer")
public class EmployerJobRolesController {

	@Autowired
	JobRolesRepository jobRolesRepository;

	@Autowired
	PartJobRolesRepository partJobRolesRepository;

	@Autowired
	InternJobRolesRepository internJobRolesRepository;

	@GetMapping(path = "/fullTimeJobRoles")
	public ResponseEntity<?> getFullTimeJobRoles(@RequestParam(value = "industry_id",required=false,defaultValue="0")  int industryId) {

		List<JobRolesModel> details = jobRolesRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Roles Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/partTimeJobRoles")
	public ResponseEntity<?> getPartTimeJobRoles(@RequestParam("industry_id") final int industryId) {

		List<PartJobRolesModel> details = partJobRolesRepository.findByIndustryId(industryId);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Roles Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/internJobRoles")
	public ResponseEntity<?> getInternJobRoles(@RequestParam("industry_id") final int industryId) {

		List<InternJobRolesModel> details = internJobRolesRepository.findByIndustryId(industryId);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Roles Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

}
