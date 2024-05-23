package com.taizo.controller.webemp;

import java.sql.Timestamp;

import com.taizo.model.DraftJobsModel;
import com.taizo.model.EmpPlacementPlanDetailsModel;
import com.taizo.model.EmployerModel;

import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.model.JobLeadModel;
import com.taizo.model.PlansModel;
import com.taizo.repository.DraftJobsRepository;
import com.taizo.repository.EmpPlacementPlanDetailsRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.JobLeadRepository;
import com.taizo.repository.PlansRepository;
import com.taizo.service.DraftJobsService;
import com.taizo.service.JobLeadService;

@CrossOrigin
@RestController
@RequestMapping("/webEmployer")
public class WebJobLeadController {
	
	@Autowired
	private JobLeadRepository jobLeadRepository;
	
	@Autowired
	private JobLeadService jobLeadService;
	
	@Autowired
	private DraftJobsRepository draftJobsRepository;
	
	@Autowired
	private DraftJobsService  draftJobsService;
	
	@Autowired
	EmployerRepository employerRepository;
	
	@Autowired
	PlansRepository plansRepository;
	
	@Autowired
	EmpPlacementPlanDetailsRepository empPlacementPlanRepository;

	private static final Logger logger = LoggerFactory.getLogger(WebJobLeadController.class);
	
	

	@GetMapping(value = "/jobLeads/{employerId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<JobLeadModel>> getJobLeadsByEmployerId(@PathVariable int employerId) {
	    List<JobLeadModel> jobLeads = jobLeadService.getJobLeadsByEmployerId(employerId);
	    if (jobLeads.isEmpty()) {
	        return ResponseEntity.notFound().build();
	    }
	    return ResponseEntity.ok(jobLeads);
	}

	@PostMapping(path = "/jobLeads")
	public ResponseEntity<?> createJobLeads(@RequestBody JobLeadModel jobLead) {
		try {
			EmployerModel employer = employerRepository.findById(jobLead.getEmployerId()).orElse(null);
			if (employer != null) {
				jobLead.setAssignTo(employer.getAssignTo());
			}
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			jobLead.setCreatedTime(currentTime);

			jobLead.setEmployerId(jobLead.getEmployerId());
			jobLead.setJobCategory(jobLead.getJobCategory());
			jobLead.setJobIndustry(jobLead.getJobIndustry());
			jobLead.setNoOfOpenings(jobLead.getNoOfOpenings());
			jobLead.setAmount(jobLead.getAmount());

			String experience = jobLead.getExperience();
	        if (experience != null) {
	            if (experience.equalsIgnoreCase("fresher")) {
	                jobLead.setExperienced(false);
	                jobLead.setJobMinExp(0);
	                jobLead.setJobMaxExp(0);
	            } else if (experience.equalsIgnoreCase("0-1")) {
	                jobLead.setExperienced(true);
	                jobLead.setJobMinExp(0);
	                jobLead.setJobMaxExp(1);
	            } else {
	                int experienceValue = Integer.parseInt(experience);
	                jobLead.setExperienced(true);
	                jobLead.setJobMinExp(experienceValue);
	                jobLead.setJobMaxExp(experienceValue);
	            }
	        }

			jobLead.setMaxSalary(jobLead.getMaxSalary());
			jobLead.setMinSalary(jobLead.getMinSalary());
			jobLead.setWorkHours(jobLead.getWorkHours());

			jobLeadRepository.save(jobLead);

			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 200);
			response.put("message", "Job lead created successfully");
			response.put("jobLeadId", jobLead.getId());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 400);
			response.put("message", "An error occurred while creating the job lead");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	@PostMapping(path = "/jobLead")
	public ResponseEntity<?> createJobLead(@RequestBody JobLeadModel jobLead) {
		try {
			EmployerModel employer = employerRepository.findById(jobLead.getEmployerId()).orElse(null);
			if (employer != null) {
				jobLead.setAssignTo(employer.getAssignTo());
			}
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			jobLead.setCreatedTime(currentTime);

			jobLead.setEmployerId(jobLead.getEmployerId());
			jobLead.setJobCategory(jobLead.getJobCategory());
			jobLead.setJobIndustry(jobLead.getJobIndustry());
			jobLead.setNoOfOpenings(jobLead.getNoOfOpenings());
			jobLead.setExperienced(jobLead.getExperienced());
			jobLead.setAmount(jobLead.getAmount());

			jobLeadRepository.save(jobLead);

			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 200);
			response.put("message", "Job lead created successfully");
			response.put("jobLeadId", jobLead.getId());

			return ResponseEntity.ok(response);
		} catch (Exception e) {

			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 400);
			response.put("message", "An error occurred while creating the job lead");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	@PutMapping("/jobLead")
	public ResponseEntity<?> updateJobLead(@RequestParam("id") final int id, @RequestBody JobLeadModel jobLead) {
		int employerId = jobLead.getEmployerId();
		String jobCategory = jobLead.getJobCategory();
		String jobIndustry = jobLead.getJobIndustry();
		int noOfOpenings = jobLead.getNoOfOpenings();
		int amount = jobLead.getAmount();

		Optional<JobLeadModel> existingJobLeadOptional = jobLeadRepository.findById(id);
		if (existingJobLeadOptional.isPresent()) {
			JobLeadModel existingJobLead = existingJobLeadOptional.get();

			existingJobLead.setEmployerId(employerId);
			existingJobLead.setJobCategory(jobCategory);
			existingJobLead.setJobIndustry(jobIndustry);
			existingJobLead.setNoOfOpenings(noOfOpenings);
			existingJobLead.setAmount(amount);
			existingJobLead.setExperienced(jobLead.getExperienced());

			jobLeadRepository.save(existingJobLead);

			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 200);
			response.put("message", "Updated Successfully");
			return ResponseEntity.ok(response);
		} else {
			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 400);
			response.put("message", "Error");
			return ResponseEntity.badRequest().body(response);
		}
	}


	@PutMapping("/jobLeads")
	public ResponseEntity<?> updateJobLeads(@RequestParam("id") final int id, @RequestBody JobLeadModel jobLead) {
		int employerId = jobLead.getEmployerId();
		String jobCategory = jobLead.getJobCategory();
		String jobIndustry = jobLead.getJobIndustry();
		int noOfOpenings = jobLead.getNoOfOpenings();
		int amount = jobLead.getAmount();

		Optional<JobLeadModel> existingJobLeadOptional = jobLeadRepository.findById(id);
		if (existingJobLeadOptional.isPresent()) {
			JobLeadModel existingJobLead = existingJobLeadOptional.get();

			String experience = jobLead.getExperience();
	        if (experience != null) {
	            if (experience.equalsIgnoreCase("fresher")) {
	            	existingJobLead.setExperienced(false);
	            	existingJobLead.setJobMinExp(0);
	            	existingJobLead.setJobMaxExp(0);
	            } else if (experience.equalsIgnoreCase("0-1")) {
	            	existingJobLead.setExperienced(true);
	            	existingJobLead.setJobMinExp(0);
	            	existingJobLead.setJobMaxExp(1);
	            } else {
	                int experienceValue = Integer.parseInt(experience);
	                existingJobLead.setExperienced(true);
	                existingJobLead.setJobMinExp(experienceValue);
	                existingJobLead.setJobMaxExp(experienceValue);
	            }
	        }

			existingJobLead.setEmployerId(employerId);
			existingJobLead.setJobCategory(jobCategory);
			existingJobLead.setJobIndustry(jobIndustry);
			existingJobLead.setNoOfOpenings(noOfOpenings);
			existingJobLead.setAmount(amount);
			existingJobLead.setMaxSalary(jobLead.getMaxSalary());
			existingJobLead.setMinSalary(jobLead.getMinSalary());
			existingJobLead.setWorkHours(jobLead.getWorkHours());

			jobLeadRepository.save(existingJobLead);

			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 200);
			response.put("message", "Updated Successfully");
			return ResponseEntity.ok(response);
		} else {
			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 400);
			response.put("message", "Error");
			return ResponseEntity.badRequest().body(response);
		}
	}


	@DeleteMapping("/jobLead/{id}")
	    public ResponseEntity<Map<String, Object>> deleteJobLead(@PathVariable int id) {
	        Optional<JobLeadModel> jobLeadOptional = jobLeadService.getJobLeadById(id);
	        Map<String, Object> response = new HashMap<>();
	        
	        if (!jobLeadOptional.isPresent()) {
	            response.put("status", HttpStatus.BAD_REQUEST.value());
	            response.put("message", "Job lead with ID " + id + " not found.");
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	        }

	        jobLeadService.deleteJobLead(id);
	        response.put("status", HttpStatus.OK.value());
	        response.put("message", "Job lead with ID " + id + " has been deleted.");
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    }
	



}



