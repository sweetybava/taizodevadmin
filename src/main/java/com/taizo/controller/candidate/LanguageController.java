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

import com.taizo.model.CfgLanguagesModel;
import com.taizo.repository.CfgLanguagesRepository;

@CrossOrigin
@RestController
public class LanguageController {
	
	@Autowired
	CfgLanguagesRepository cfgLanguagesRepository;
	
	@GetMapping(path = "/languageDetails")
	public ResponseEntity<?> getLanguageDetails() {

		List<CfgLanguagesModel> details = cfgLanguagesRepository.findAllByActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "language Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/languageLabels")
	public ResponseEntity<?> getLanguageLabels(@RequestParam("language_id") final int id) {
		CfgLanguagesModel details = cfgLanguagesRepository.findById(id);
		if (details!=null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Language Not Found");
			map.put("results", null);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

}
