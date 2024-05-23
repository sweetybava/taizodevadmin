package com.taizo.controller.employer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.model.CanLanguageModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.LanguagesModel;
import com.taizo.repository.CanLanguagesRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.LanguagesRepository;

@CrossOrigin
@RestController
@RequestMapping("/employer")
public class EmployerLanguagesController {

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	LanguagesRepository languagesRepository;

	@Autowired
	CanLanguagesRepository canLanguagesRepository;

	@PersistenceContext
	EntityManager em;

	@GetMapping(path = "/languages")
	public ResponseEntity<?> getEmployerLanguages() {

		List<LanguagesModel> details = languagesRepository.findAll();

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
			map.put("message", "Languages Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping(path = "/candidateKnownLanguages")
	public ResponseEntity<?> getCandidateKnownLanguagess(@RequestParam("candidate_id") final int candidateId) {

		Optional<CandidateModel> optional = candidateRepository.findById(candidateId);
		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Candidate Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {

			List<CanLanguageModel> details = canLanguagesRepository.findByCandidateId(candidateId);
			if (!details.isEmpty()) {

				List<LanguagesModel> persons = null;
				Set<Integer> list = new HashSet();

				int j = 0;

				for (CanLanguageModel s : details) {

					j = s.getLanguageId();
					list.add(j);
				}

				persons = em.createQuery("SELECT j FROM LanguagesModel j WHERE j.id IN :ids").setParameter("ids", list)
						.getResultList();

				HashMap<String, Object> map = new HashMap<>();
				map.put("status", "success");
				map.put("code", 200);
				map.put("message", "success");
				map.put("data", persons);
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "No languages found");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		}

	}

}
