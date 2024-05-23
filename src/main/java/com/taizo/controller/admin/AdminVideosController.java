package com.taizo.controller.admin;

import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.CandidateModel;
import com.taizo.model.JobsModel;
import com.taizo.repository.AdminRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.JobRepository;
import com.taizo.service.AdminService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@CrossOrigin
@RestController
public class AdminVideosController {

	private static final Logger logger = LogManager.getLogger(AdminVideosController.class);

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	AdminRepository adminRepository;

	@Autowired
	AdminService adminService;

	private String SAMPLEVIDEOPREFIX = "taizo_samplevideo_";
	private String EMPLOYERJOBVIDEOPREFIX = "employer_job_video_";

	@PostMapping(value = "/uploadSampleVideo")
	public ResponseEntity<?> uploadSampleVideo(@RequestParam(name = "sampleVideoFile") MultipartFile sampleVideo,
			@RequestParam(name = "videoTitle") String videoTitle, @RequestParam(name = "videoDesc") String videoDesc)
			throws ResourceNotFoundException, IOException {
		try {
			long currentTimeInMillis = System.currentTimeMillis();
			adminService.uploadSampleVideoFileToS3Bucket(sampleVideo,
					SAMPLEVIDEOPREFIX + Long.toString(currentTimeInMillis) + ".mp4");
			adminService.insertSampleVideo(Long.toString(currentTimeInMillis), "", videoDesc, videoTitle);

			List<CandidateModel> candidate = candidateRepository.findAll();

			for (CandidateModel c : candidate) {

				
			}

			return new ResponseEntity<>("200", HttpStatus.OK);
		} catch (Exception e) {
			logger.error("error [" + e.getMessage() + "] occurred while uploading [" + sampleVideo + "] sample video");
			return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(path = "/updateJobVideo")
	public ResponseEntity<?> addJobVideo(@RequestParam("employer_id") final int employerId,
			@RequestParam("job_id") final int jobId, @RequestPart(name = "job_video") MultipartFile jobVideo)
			throws IOException {

		JobsModel jobs = jobRepository.findByIdAndEmployer(jobId, employerId);
		if (jobs != null) {

			if (jobVideo != null && !jobVideo.isEmpty()) {

				try {

					adminService.uploadJobVideoFileToS3Bucket(jobVideo, EMPLOYERJOBVIDEOPREFIX + jobId + ".mp4");

				} catch (Exception e) {
					logger.error(
							"error [" + e.getMessage() + "] occurred while uploading [" + jobVideo + "] job video");
					return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
				}

				return new ResponseEntity<>("200", HttpStatus.OK);

			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "Video Field is required");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Job Or Employer Not found");

			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}

	}

}
