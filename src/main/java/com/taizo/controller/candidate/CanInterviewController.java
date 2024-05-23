package com.taizo.controller.candidate;

import java.net.URISyntaxException; 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.model.CanInterviewNotificationModel;
import com.taizo.model.CanInterviewsModel;
import com.taizo.model.CandidateCallModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.CfgInterRequiredDocModel;
import com.taizo.model.EmpInterviewNotificationModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.InterviewAddressesModel;
import com.taizo.model.InterviewDates;
import com.taizo.model.InterviewsModel;
import com.taizo.model.JobsModel;
import com.taizo.model.RescheduleInterviewModel;
import com.taizo.repository.CanInterviewNotificationRepository;
import com.taizo.repository.CanInterviewRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.EmpInterviewNotificationRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.InterviewAddressRepository;
import com.taizo.repository.InterviewRepository;
import com.taizo.repository.InterviewRequiredDocRepository;
import com.taizo.repository.JobRepository;
import com.taizo.repository.RescheduleInterviewRepository;
import com.taizo.service.InterviewService;

@CrossOrigin
@RestController
public class CanInterviewController {

	private static final Logger logger = LoggerFactory.getLogger(CanInterviewController.class);

	@Autowired
	InterviewService interviewService;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	InterviewRepository interviewRepository;

	@Autowired
	RescheduleInterviewRepository rescheduleInterviewRepository;

	@Autowired
	InterviewRequiredDocRepository interviewReqDocRepository;

	@Autowired
	CanInterviewNotificationRepository canInterviewNotificationRepository;

	@Autowired
	InterviewAddressRepository interviewAddressRepository;

	@Autowired
	CanInterviewRepository canInterviewRepository;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	EmployerRepository employerRepository;

	private Object notInterestedJobs;

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

	@GetMapping("/AllNotification")
	public ResponseEntity<?> getInterviewNotification(@RequestParam("candidate_id") final int candidateId) {

		List<Map<String, Object>> details = interviewRepository.getCanInterviewNotification(candidateId);

		if (!details.isEmpty() && details != null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Notifications Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping(path = "/interviewDetailsById")
	public ResponseEntity<?> getinterviewDetailsById(@RequestParam("interview_id") final int interviewId,
			@RequestParam("status") final String status) {

		Map<String, Object> details = interviewRepository.findInterviewByIdAndStatus(interviewId, status);

		if (!details.isEmpty() && details != null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Interview Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping("/candidateInterviewCardDetails")
	public ResponseEntity<?> getCanInterviewDetails(@RequestParam("candidate_id") final int candidateId,
			@RequestParam("status") final String status) {
		List<Map<String, Object>> details = interviewRepository.findCanInterviewByStatus(candidateId, status);

		if (!details.isEmpty() && details != null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Interviews Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path = "/updateCandidateInterviewStatus")
	public ResponseEntity<?> updateCandidateAddSkills(@RequestParam("candidate_id") final int candidateId,
			@RequestParam("interview_id") final int interviewId,
			@RequestParam("interview_status") final String status) {

		Optional<InterviewsModel> optional = interviewRepository.findById(interviewId);

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		String date = dtf.format(now);
		Date currentDate;

		if (optional.isPresent()) {

			InterviewsModel existing = optional.get();
			Optional<JobsModel> jobsModel = jobRepository.findById(existing.getJobId());
			Optional<CandidateModel> CModel = candidateRepository.findById(existing.getCanId());
			Optional<EmployerModel> EModel = employerRepository.findById(existing.getEmpId());
			Optional<InterviewAddressesModel> AModel = interviewAddressRepository.findById(existing.getAddressId());
			if (!existing.isRescheduled()) {

				existing.setStatus(status);
				try {
					currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
					existing.setAcceptedDate(currentDate);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}

				interviewRepository.save(existing);

				CanInterviewNotificationModel m = new CanInterviewNotificationModel();
				m.setInterviewId(interviewId);
				m.setCanId(existing.getCanId());
				m.setEmpId(existing.getEmpId());
				m.setScheduledDate(existing.getScheduled_on());
				m.setScheduledTime(existing.getStartTime());
				m.setStatus(status);
				if (status.equalsIgnoreCase("A")) {
					m.setNotes("Interview Accepted");

				} else {
					m.setNotes("Interview Rejected");
				}
				canInterviewNotificationRepository.save(m);

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "Interview Accepted");
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				Optional<RescheduleInterviewModel> reModel = rescheduleInterviewRepository
						.findByInterviewId(interviewId);
				if (reModel.isPresent()) {

					RescheduleInterviewModel existing1 = reModel.get();
					existing1.setStatus(status);
					try {
						currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
						existing1.setAcceptedDate(currentDate);
					} catch (ParseException e1) {
						e1.printStackTrace();
					}

					rescheduleInterviewRepository.save(existing1);

					InterviewsModel i = optional.get();

					CanInterviewNotificationModel m = new CanInterviewNotificationModel();
					m.setInterviewId(interviewId);
					m.setCanId(i.getCanId());
					m.setEmpId(i.getEmpId());
					m.setScheduledDate(existing1.getReScheduledOn());
					m.setScheduledTime(existing1.getStartTime());
					m.setStatus(status);
					if (status.equalsIgnoreCase("A")) {
						m.setNotes("Reschedule Interview Accepted");


					} else {
						m.setNotes("Reschedule Interview Rejected");
					}
					canInterviewNotificationRepository.save(m);

					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", "Accepted");
					return new ResponseEntity<>(map, HttpStatus.OK);
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 400);
					map.put("message", "Failed to update");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}
			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "User Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	@GetMapping("/notInterestedJobs")
	public ResponseEntity<?> getJobDetailsByCanId(@RequestParam("canId") int canId) {
	    List<CanInterviewsModel> canInterviewModels = canInterviewRepository.findByCanIdAndStatus(canId, "N");

	    List<JobsModel> jobDetailsList = new ArrayList<>();
	    for (CanInterviewsModel canInterviewModel : canInterviewModels) {
	        int jobId = canInterviewModel.getJobId();
	        Optional<JobsModel> job = jobRepository.findById(jobId);
	        
	        if (job.isPresent()) {
	            JobsModel jobDetails = job.get();
	            jobDetailsList.add(jobDetails);
	        }
	    }

	    if (!jobDetailsList.isEmpty()) {
	        Map<String, Object> response = new HashMap<>();
	        response.put("status", "success");
	        response.put("message", "Job details found");
	        response.put("result", jobDetailsList);
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    }

	    Map<String, Object> response = new HashMap<>();
	    response.put("status", "error");
	    response.put("message", "No job details found for the given canId");
	    response.put("result", null);
	    return new ResponseEntity<>(response, HttpStatus.OK);
	}






	
}
