package com.taizo.controller.candidate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.model.CanRatingsModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.CfgRatingCountsModel;
import com.taizo.model.EmpJobRatingsModel;
import com.taizo.model.FeedbackModel;
import com.taizo.model.JobsModel;
import com.taizo.repository.CanRatingsRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.CfgRatingCountsRepository;
import com.taizo.repository.CfgRatingReasonsRepository;
import com.taizo.repository.EmpJobRatingsRepository;
import com.taizo.repository.FeedbackRepository;

@CrossOrigin
@RestController
public class UserFeedbackController {

	@Autowired
	FeedbackRepository feedbackRepository;
	
	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	CfgRatingCountsRepository cfgRatingCountsRepository;

	@Autowired
	CfgRatingReasonsRepository cfgRatingReasonsRepository;

	@Autowired
	CanRatingsRepository canRatingsRepository;

	@GetMapping("/ratingQuestions")
	public ResponseEntity<?> ratingCounts(@RequestParam("can_id") final int canId,@RequestParam("rating") final int count) {

		
		Optional<CandidateModel> c = candidateRepository.findById(canId);
		String lan_key = c.get().getLanguageKey();
		List<Map<String, Object>> list = cfgRatingCountsRepository.findByModuleandCountTran("JS",count,lan_key);

		/*
		 * Object k = list.get(0).getOrDefault("question_bn",null);
		 * System.out.println(k.toString());
		 */
		
		if (!list.isEmpty()) {
			
			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", list);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "No Rating Available");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

	}

	@PostMapping("/appRating")
	public CanRatingsModel jobRating(@RequestBody CanRatingsModel model) {
		int canId = model.getCanId();
		CanRatingsModel e= canRatingsRepository.findByCanId(canId);
		if(e!=null) {
			e.setCanId(canId);
			e.setRatingId(model.getRatingId());
			e.setRatingCount(model.getRatingCount());
			e.setQuestion(model.getQuestion());
			e.setReasons(model.getReasons());
			canRatingsRepository.save(e);

		}else {
			canRatingsRepository.save(model);
		}
		return model;
	}
	
	@GetMapping("/homeFeedback")
	public ResponseEntity<?> homeFeedback(@RequestParam("can_id") final int canId) {

		CanRatingsModel e= canRatingsRepository.findByCanId(canId);			

			if(e!=null) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("show feedback", false);
				return new ResponseEntity<>(map, HttpStatus.OK);	

			} else {

					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 200);
					map.put("show feedback", true);
					map.put("image url", "https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/Job+published.png");
					//map.put("published icon", "https://taizo-common.s3.ap-south-1.amazonaws.com/Public/images/Job+published.png");
					map.put("skippable", false);
					map.put("title", "Feedback!");
					map.put("desc", "Please rate your Taizo experience");
					map.put("feedback desc", "Your feedback will help us in providing a better experience on Taizo.");
					return new ResponseEntity<>(map, HttpStatus.OK);
			}

	}

	@PostMapping("/feedback")
	public FeedbackModel createFeedback(@RequestBody FeedbackModel feedback) {
		feedbackRepository.save(feedback);
		return feedback;
	}

}
