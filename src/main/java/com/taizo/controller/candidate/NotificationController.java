package com.taizo.controller.candidate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.taizo.model.AdminNotificationModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.JobApplicationModel;
import com.taizo.model.NotificationModel;
import com.taizo.model.NotificationViewModel;
import com.taizo.repository.AdminNotificationRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.JobApplicationRepository;
import com.taizo.repository.NotificationRepository;
import com.taizo.service.JobApplicationService;

@CrossOrigin
@RestController
public class NotificationController {

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	AdminNotificationRepository adminNotificationRepository;

	@Autowired
	JobApplicationRepository jobApplicationRepository;

	@Autowired
	JobApplicationService jobApplicationService;

	@Autowired
	EmployerRepository employerRepository;

	@GetMapping(path = "/employerNotifications")
	public ResponseEntity<?> getNotifications(@RequestParam("candidate_id") final int candidateId) {

		List<NotificationModel> details = notificationRepository.findByCandidateId(candidateId);
		List<NotificationViewModel> n = new ArrayList<NotificationViewModel>();

		if (!details.isEmpty()) {

			int empId;

			for (NotificationModel s : details) {

				String msg = s.getMessage();

				NotificationViewModel e = new NotificationViewModel();
				empId = s.getEmployerId();
				Optional<EmployerModel> emp = employerRepository.findById(empId);
				EmployerModel emp1 = emp.get();
				String cmName = emp1.getCompanyName();

				e.setMessage(msg);
				e.setCompanyName(cmName);

				n.add(e);

			}
			Collections.reverse(n);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", n);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "No messages");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping(path = "/adminnotifications")
	public ResponseEntity<?> getAdminNotifications() {

		List<AdminNotificationModel> details = adminNotificationRepository.findAll();

		if (!details.isEmpty()) {
			Collections.reverse(details);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "No messages Available");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(path = "/sendReplyNotification")
	public ResponseEntity<?> sendUserIndivitualNotification(@RequestParam("candidate_id") final int candidateId,
			@RequestParam("message") final String userMessage, @RequestParam("status") final String status,
			@RequestParam("job_id") final int jobId) {

		Optional<JobApplicationModel> details = jobApplicationRepository.findByCandidateIdAndJobIdAndStatus(candidateId,
				jobId, status);

		if (details.isPresent()) {
			jobApplicationService.updateReplyMessage(candidateId, jobId, userMessage);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Send Successfully");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Candidate Not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}

	}

	@SuppressWarnings("unused")
	@GetMapping(path = "/JobNotification")
	public ResponseEntity<?> getEmployerIndivitualNotification(@RequestParam("candidate_id") final int candidateId,
			@RequestParam("status") final String status, @RequestParam("job_id") final int jobId) {

		Optional<JobApplicationModel> details = jobApplicationRepository.findByCandidateIdAndJobIdAndStatus(candidateId,
				jobId, status);
		String umsg = null;
		String msg;
		if (details.isPresent()) {
			JobApplicationModel j = details.get();

			msg = j.getMessage();

			try {
				umsg = j.getUserMessage();
			} catch (Exception e) {

			}

			if (umsg != null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 400);
				map.put("message", "Message Not found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

			} else {
				if (!msg.isEmpty() && msg != null) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 200);
					map.put("message", msg);
					return new ResponseEntity<>(map, HttpStatus.OK);
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 400);
					map.put("message", "Message Not found");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

				}

			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Job Not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

}
