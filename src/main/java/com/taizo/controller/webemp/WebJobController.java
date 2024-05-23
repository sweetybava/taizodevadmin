package com.taizo.controller.webemp;

import java.net.URISyntaxException; 
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.CandidateCallModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.EmpInterviewNotificationModel;
import com.taizo.model.EmployerActivityModel;
import com.taizo.model.EmployerCallModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.InterviewsModel;
import com.taizo.model.JobApplicationModel;
import com.taizo.model.JobsModel;
import com.taizo.model.UserModel;
import com.taizo.repository.CandidateCallRepository;
import com.taizo.repository.EmpActivityRepository;
import com.taizo.repository.EmployerCallRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.InterviewRepository;
import com.taizo.repository.JobApplicationRepository;
import com.taizo.repository.JobRepository;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.service.JobService;
import com.taizo.utils.TupleStore;

@CrossOrigin
@RestController
@RequestMapping("/webEmployer")
public class WebJobController {

	private static final Logger logger = LoggerFactory.getLogger(WebJobController.class);

	@Autowired
	JobRepository jobRepository;

	@Autowired
	JobService jobService;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	EmployerCallRepository employerCallRepository;
	@Autowired
	CandidateCallRepository candidateCallRepository;

	@Autowired
	JobApplicationRepository jobApplicationRepository;

	@Autowired
	InterviewRepository interviewRepository;

	@Autowired
	EmpActivityRepository empActivityRepository;

	@Autowired
	private AmazonSESMailUtil amazonSESMailUtil;

	@GetMapping("/jobs")
	public ResponseEntity<?> getEmployerJobs(@RequestParam("emp_id") final int employerId,
			@RequestParam("status") final String status, @RequestParam("page") int pgNo,
			@RequestParam("size") int length) throws ResourceNotFoundException {

		if (status.equalsIgnoreCase("C")) {

			Page<JobsModel> Cjobs = jobService.findEmployerClosedJobs(employerId,status, pgNo, length);
			HashMap<String, Object> hm = new HashMap<>();
			hm.put("data", Cjobs.getContent());
			hm.put("start", pgNo);
			hm.put("recordsTotal", Cjobs.getTotalElements());
			hm.put("recordsFiltered", Cjobs.getContent().size());
			return new ResponseEntity<>(hm, HttpStatus.OK);
		} else if (status.equalsIgnoreCase("D")) {

			Page<JobsModel> Cjobs = jobService.findEmployerDraftJobs(employerId, status, pgNo, length);
			HashMap<String, Object> hm = new HashMap<>();
			hm.put("data", Cjobs.getContent());
			hm.put("start", pgNo);
			hm.put("recordsTotal", Cjobs.getTotalElements());
			hm.put("recordsFiltered", Cjobs.getContent().size());
			return new ResponseEntity<>(hm, HttpStatus.OK);
		} else {
			Page<JobsModel> jobs = jobService.findEmployerJobs(employerId, status, pgNo, length);

			HashMap<String, Object> hm = new HashMap<>();
			hm.put("data", jobs.getContent());
			hm.put("start", pgNo);
			hm.put("recordsTotal", jobs.getTotalElements());
			hm.put("recordsFiltered", jobs.getContent().size());
			return new ResponseEntity<>(hm, HttpStatus.OK);
		}

	}

	@GetMapping("/jobCounts")
	public ResponseEntity<?> getEmployerJobCounts(@RequestParam("emp_id") final int employerId)
			throws ResourceNotFoundException {

		int page = 1 / 1000; // Calculate page number
		Pageable pageable = PageRequest.of(page, 1000, new Sort(Sort.Direction.DESC, "createdTime"));
		Page<JobsModel> Cjobs = jobRepository.findEmpClosedJob(employerId,"C", pageable);
		Page<JobsModel> Djobs = jobRepository.findEmpDraftJob(employerId, "D", pageable);
		Page<JobsModel> jobs = jobRepository.findEmpJob(employerId, "O", pageable);

		HashMap<String, Object> hm = new HashMap<>();
		hm.put("postedJobCount", jobs.getContent().size());
		hm.put("savedJobCount", Djobs.getContent().size());
		hm.put("closedJobCount", Cjobs.getContent().size());

		return new ResponseEntity<>(hm, HttpStatus.OK);

	}

	@GetMapping(value = "/job/{id}")
	public ResponseEntity<?> getNotiCount(@PathVariable("id") int id) throws ResourceNotFoundException {

		String status = "I";

		Page<JobApplicationModel> jobApplicationModel = jobApplicationRepository.getCanAppliedNotificationCount(id,
				status, PageRequest.of(0, 100));

		Page<InterviewsModel> InterviewsModel = interviewRepository.getCanInterviewScheduledNotificationCount(id,
				PageRequest.of(0, 100));

		Page<CandidateCallModel> employerCallModel = candidateCallRepository.getCanCallNotificationCount(id,
				PageRequest.of(0, 100));

		HashMap<String, Long> count = new HashMap<>();

		count.put("CandidatesAppliedCount", jobApplicationModel.getTotalElements());
		count.put("InterviewScheduledCount", InterviewsModel.getTotalElements());
		count.put("CandidatesCalledCount", employerCallModel.getTotalElements());

		ArrayList<HashMap<String, Long>> al = new ArrayList<>();
		al.add(count);
		HashMap<String, Object> map = new HashMap<>();
		map.put("jobOverview", al);
		map.put("jobDetails", jobService.getById(id));
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@PutMapping(path = "/closeJob")
	public ResponseEntity<?> closeJob(@RequestParam("emp_id") final int employerId,
			@RequestParam("job_id") final int jobId, @RequestParam("expiry_date") final String expiryDate,
			@RequestParam(value = "reason", required = false) final String reason) throws ResourceNotFoundException {

		JobsModel jobs = jobRepository.findByIdAndEmployer(jobId, employerId);

		if (jobs != null) {
			Optional<JobsModel> optional = jobRepository.findById(jobId);
			JobsModel existing = optional.get();
			existing.setExpiryDate(expiryDate);
			existing.setJobStatus("C");
			existing.setReasonForClose(reason);
			jobRepository.save(existing);

			Optional<EmployerModel> emp = employerRepository.findById(employerId);

			EmployerModel em = emp.get();

			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(employerId);
			EA.setActivity("Your job " + "<b>" + existing.getJobCategory() + "</b>" + " has been closed!");
			empActivityRepository.save(EA);

			DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
			String date = formatter.format(new Date());
			SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
			Calendar cal = Calendar.getInstance();
			String time = simpleDateFormat1.format(cal.getTime());

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "successfully saved");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			throw new ResourceNotFoundException("Job not found.");
		}

	}

	@GetMapping(path = "/empJobCategories")
	public ResponseEntity<?> empJobCategories(@RequestParam("emp_id") final int employerId,
			@RequestParam(value = "job_category", required = false) final String jobRole) {
		EmployerModel em = employerRepository.findById(employerId).get();

		if (em != null) {
			if (jobRole != null && !jobRole.isEmpty()) {
				JobsModel detail = jobRepository.getJobByJobCategory(employerId, jobRole);
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("status", "success");
				map.put("message", "success");
				map.put("data", detail);
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {
				List<Map<String, Object>> details = jobRepository.getEmpJobCategories(employerId);
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("status", "success");
				map.put("message", "success");
				map.put("data", details);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping(value = "/deleteJob")
	public ResponseEntity<?> deleteAccount(@RequestParam("emp_id") final int employerId,
			@RequestParam("job_id") final int jobId) throws ResourceNotFoundException {
		JobsModel jobs = jobRepository.findByIdAndEmployer(jobId, employerId);

		if (jobs != null) {

			jobs.setDeleted(true);
			jobRepository.save(jobs);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "Successfully deleted");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			throw new ResourceNotFoundException("Job not found.");
		}
	}
}
