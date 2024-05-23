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

import com.taizo.model.BenefitsModel;
import com.taizo.model.CfgEmpJobShiftTimings;
import com.taizo.model.CfgInterRequiredDocModel;
import com.taizo.model.CfgStateCityModel;
import com.taizo.model.IndustryModel;
import com.taizo.model.JobIndustryModel;
import com.taizo.model.JobRolesModel;
import com.taizo.model.StateCityModel;
import com.taizo.repository.BenefitsRepository;
import com.taizo.repository.EmpJobShiftTimingsRepository;
import com.taizo.repository.IStateCityRepository;
import com.taizo.repository.IndustryRepository;
import com.taizo.repository.InterviewRequiredDocRepository;
import com.taizo.repository.JobIndustryRepository;
import com.taizo.repository.JobRolesRepository;
import com.taizo.repository.StateCityRepository;

@CrossOrigin
@RestController
@RequestMapping("/web")
public class WebController {
	
	@Autowired
	BenefitsRepository benefitsRepository;
	@Autowired
	IndustryRepository industryRepository;
	@Autowired
	EmpJobShiftTimingsRepository empJobShiftTimingsRepository;
	@Autowired
	JobRolesRepository jobRolesRepository;
	@Autowired
	JobIndustryRepository jobIndustryRepository;
	@Autowired
	IStateCityRepository stateCityRepository;
	@Autowired
	InterviewRequiredDocRepository interviewReqDocRepository;
	
	
	
	
	
	
	@GetMapping(path = "/employer/benefits")
	public ResponseEntity<?> getBenefits() {

		List<BenefitsModel> details = benefitsRepository.findAll();

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
			map.put("message", "Benefits Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/employer/industries")
	public ResponseEntity<?> getEmployerIndustries() {

		List<IndustryModel> details = industryRepository.findAll();

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
			map.put("message", "Industries Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	
      @GetMapping(path = "/employer/jobIndustries")
	public ResponseEntity<?> getEmployerJobIndustries() {

		List<JobIndustryModel> details = jobIndustryRepository.findAll();

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
			map.put("message", "Industries Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/jobShiftTimings")
	public ResponseEntity<?> getJobShiftTimings(@RequestParam("shift_type") final String shiftType) {

		List<CfgEmpJobShiftTimings> details = empJobShiftTimingsRepository.findByShiftType(shiftType);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "success");
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Shift timings not available");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	@GetMapping(path = "/fullTimeJobRoles")
	public ResponseEntity<?> getFullTimeJobRoles(@RequestParam(value = "industry_id",required=false,defaultValue="0") final int industryId){

		List<JobRolesModel> details = jobRolesRepository.findAll(true);

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
	@GetMapping(path = "/employer/indiaStateCities")
	public ResponseEntity<?> getIndiaStateCitiess(@RequestParam("state_id") final int stateId) {

		List<CfgStateCityModel> details = stateCityRepository.findByStateId(stateId,true);

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
			map.put("message", "Cities Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	@GetMapping(path = "/interviewRequiredDoc")
	public ResponseEntity<?> getInterviewRequiredDoc() {

		List<CfgInterRequiredDocModel> details = interviewReqDocRepository.findAllByActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "interviewRequiredDoc Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}



}
