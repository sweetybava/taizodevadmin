package com.taizo.controller.webemp;

import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.AdminNotificationModel;
import com.taizo.model.CfgRatingCountsModel;
import com.taizo.model.EmpJobRatingsModel;
import com.taizo.model.EmployerFeedbackModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.JobsModel;
import com.taizo.model.PlansModel;
import com.taizo.repository.CfgRatingCountsRepository;
import com.taizo.repository.CfgRatingReasonsRepository;
import com.taizo.repository.EmpJobRatingsRepository;
import com.taizo.repository.EmployerFeedbackRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.FeedbackRepository;
import com.taizo.repository.JobRepository;

@CrossOrigin
@RestController
@RequestMapping("/webEmployer")
public class WebFeedbackController {

	@Autowired
	CfgRatingCountsRepository cfgRatingCountsRepository;

	@Autowired
	CfgRatingReasonsRepository cfgRatingReasonsRepository;

	@Autowired
	EmpJobRatingsRepository empJobRatingsRepository;

	@Autowired
	JobRepository jobRepository;

	@GetMapping("/ratingCounts")
	public ResponseEntity<?> ratingCounts() {

		List<CfgRatingCountsModel> list = cfgRatingCountsRepository.findByModule("Emp");
		if (!list.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("image url", "https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/Job+Closed.png");
			map.put("show feedback", true);
			map.put("skippable", false);
			map.put("title", "Job Successfully Closed!");
			map.put("desc", "Please rate your Taizo experience in hiring a {role} for your business.");
			map.put("feedback desc", "Your feedback will help us in providing a better hiring experience on Taizo.");
			map.put("data", list);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "No Rating Available");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

	}

	@GetMapping("/homeFeedback")
	public ResponseEntity<?> homeFeedback(@RequestParam("emp_id") final int empId) {

		JobsModel detail = jobRepository.findByRecentJob(empId);
		if (detail != null) {

			EmpJobRatingsModel e = empJobRatingsRepository.findByJobIdandEmpId(empId, detail.getId());

			if (e != null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("show feedback", false);
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {
				if (detail.getJobStatus().equalsIgnoreCase("C")) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 200);
					map.put("job_id", detail.getId());
					map.put("image url",
							"https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/Job+Closed.png");
					map.put("show feedback", true);
					map.put("skippable", false);
					map.put("limit exceeded", false);
					map.put("title", "Job Successfully Closed!");
					map.put("desc", "Please rate your Taizo experience in hiring a " + detail.getJobCategory()
							+ " for your business.");
					map.put("feedback desc",
							"Your feedback will help us in providing a better hiring experience on Taizo.");
					return new ResponseEntity<>(map, HttpStatus.OK);
				} else if (detail.getCanResponseCount() <= 0) {

					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 200);
					map.put("job_id", detail.getId());
					map.put("show feedback", true);
					map.put("limit exceeded", true);
					map.put("image url",
							"https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/Job+Expired.png");
					map.put("skippable", false);
					map.put("title", "Candidate Response limit has reached its limit!");
					map.put("desc", "The number of candidate responses for your job posting " + detail.getJobCategory()
							+ " has reached its limit.");
					map.put("feedback desc",
							"Your feedback will help us in providing a better hiring experience on Taizo.");
					return new ResponseEntity<>(map, HttpStatus.OK);

				} else {

					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 200);
					map.put("show feedback", false);
					return new ResponseEntity<>(map, HttpStatus.OK);
				}
			}
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("show feedback", false);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

	}

	@GetMapping("/homeFeedbackRating")
	public ResponseEntity<?> homeFeedbackRating(@RequestParam("rating_count") final int ratingCount) {

		CfgRatingCountsModel list = cfgRatingCountsRepository.findByModuleandCount("Emp", ratingCount);
		if (list != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", list);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "No Rating Available");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

	}

	@PostMapping("/jobRating")
	public EmpJobRatingsModel jobRating(@RequestBody EmpJobRatingsModel model) {
		int empId = model.getEmpId();
		int jobId = model.getJobId();
		EmpJobRatingsModel e = empJobRatingsRepository.findByJobIdandEmpId(empId, jobId);
		if (e != null) {
			e.setEmpId(empId);
			e.setJobId(jobId);
			e.setRatingId(model.getRatingId());
			e.setRatingCount(model.getRatingCount());
			e.setQuestion(model.getQuestion());
			e.setReasons(model.getReasons());
			e.setFromWeb(true);
			empJobRatingsRepository.save(e);

		} else {
			empJobRatingsRepository.save(model);
		}
		return model;
	}
}
