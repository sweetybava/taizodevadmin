package com.taizo.controller.webemp;

import java.net.URISyntaxException; 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.CanLanguageModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.EmployerActivityModel;
import com.taizo.model.EmployerApplication;
import com.taizo.model.EmployerModel;
import com.taizo.model.JobsModel;
import com.taizo.model.LanguagesModel;
import com.taizo.repository.CanLanguagesRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.EmpActivityRepository;
import com.taizo.repository.EmployerApplicationRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.JobRepository;
import com.taizo.service.CandidateService;
import com.taizo.service.JobService;

@CrossOrigin
@RestController
@RequestMapping("/webEmployer")
public class WebCandidateController {

	@PersistenceContext
	EntityManager em;

	@Autowired
	CandidateService candidateService;

	@Autowired
	EmployerApplicationRepository employerApplicationRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	EmpActivityRepository empActivityRepository;

	@Autowired
	CandidateRepository candidateRepository;
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	CanLanguagesRepository canLanguagesRepository;

	@GetMapping(value = "/candidate/{id}")
	public ResponseEntity<CandidateModel> get(@PathVariable int id) {
	    Optional<CandidateModel> candidate = candidateRepository.findById(id);

	    return candidate.map(ResponseEntity::ok)
	                    .orElseGet(() -> ResponseEntity.notFound().build());
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping(path = "/candidateKnownLanguages")
	public ResponseEntity<?> getCandidateKnownLanguages(@RequestParam("can_id") final int canId) throws ResourceNotFoundException {

		Optional<CandidateModel> optional = candidateRepository.findById(canId);
		if (!optional.isPresent()) {
			throw new ResourceNotFoundException("Candidates not found.");
		} else {


			List<CanLanguageModel> details = canLanguagesRepository.findByCandidateId(optional.get().getId());
			if (!details.isEmpty()) {

				List<LanguagesModel> persons = null;
				Set<Integer> list = new HashSet();

				int j = 0;
			  String lan = null;
				List<String> lang = new ArrayList<String>();


				for (CanLanguageModel s : details) {
					j = s.getLanguageId();
					list.add(j);
				}

				persons = em.createQuery("SELECT j FROM LanguagesModel j WHERE j.id IN :ids").setParameter("ids", list)
						.getResultList();
				
				if(persons!=null && !persons.isEmpty()) {
				for (LanguagesModel s : persons) {

					lan = s.getLanguages();
					lang.add(lan);
				}
				}
		        String commaseparatedlist = lang.toString();
		        
		        commaseparatedlist
		            = commaseparatedlist.replace("[", "")
		                  .replace("]", "")
		                  .replace(" ", "");

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", "success");
				map.put("results", commaseparatedlist);
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {
				throw new ResourceNotFoundException("No languages found.");
			}
		}

	}

	@GetMapping(path = "/appliedCanDetails")
	public ArrayList<HashMap<String, String>> getAppliedCanDetails(@RequestParam("emp_id") final int empId,
			@RequestParam(value = "job_category", required = false) final String jobCategory,
			@RequestParam(value = "location", required = false) final String location,
			@RequestParam(value = "exp", required = false) final int exp,
			@RequestParam("start_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
			@RequestParam("end_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
			@RequestParam("page") final int pageNumber, @RequestParam("size") final int pageSize)
			throws ResourceNotFoundException {
		String status = "I";

		List<Object[]> results = null;

		results = em.createQuery(
				" SELECT j.id as jobId,c.id as candidateId,c.firstName,c.jobCategory,c.age,c.experience,"
				+ "c.currentState,c.currentCity,ja.appliedTime,u.profilePic FROM  JobApplicationModel ja "
						+ "left join JobsModel j on  j.id = ja.jobId "
						+ "left join EmployerModel e on e.id = j.employerId "
						+ "left join CandidateModel c on c.id  =ja.candidateId "
						+ "left join UserModel u on  u.id = c.userId "
						+ "where j.employerId = :emp_id and ja.status =:applied and (c.currentState=:location or :location IS NULL) "
						+ "and (c.jobCategory=:jobCategory or :jobCategory IS NULL) "
						+ "and (c.experience=:exp or :exp = 0)" + "and ja.appliedTime between :startdate and :enddate",
				Object[].class).setParameter("emp_id", empId).setParameter("applied", status)
				.setParameter("startdate", startDate).setParameter("enddate", endDate).setParameter("enddate", endDate)
				.setParameter("jobCategory", jobCategory).setParameter("location", location).setParameter("exp", exp)
				.setFirstResult((pageNumber - 1) * pageSize).setMaxResults(pageSize).getResultList();

		if (results.isEmpty()) {
			throw new ResourceNotFoundException("Candidates not found.");
		}
		Collections.reverse(results);

		ArrayList<HashMap<String, String>> al = new ArrayList<>();
		for (Object[] row : results) {
			HashMap<String, String> count = new HashMap<>();
			count.put("jobId", String.valueOf(row[0]));
			count.put("candidateId", String.valueOf(row[1]));
			count.put("firstName", String.valueOf(row[2]));
			count.put("jobCategory", String.valueOf(row[3]));
			count.put("age", String.valueOf(row[4]));
			count.put("experience", String.valueOf(row[5]));
			count.put("currentState", String.valueOf(row[6]));
			count.put("currentCity", String.valueOf(row[7]));
			count.put("appliedTime", String.valueOf(row[8]));
			count.put("profilePic", String.valueOf(row[9]));

			al.add(count);
		}

		return al;
	}

	@GetMapping(path = "/interestedCanDetails")
	public ArrayList<HashMap<String, String>> getInterestedCanDetails(@RequestParam("emp_id") final int empId,
			@RequestParam(value = "can_type", required = false) final String canType,
			@RequestParam(value = "job_category", required = false) final String jobCategory,
			@RequestParam(value = "location", required = false) final String location,
			@RequestParam(value = "exp", required = false) final int exp,
			@RequestParam("start_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
			@RequestParam("end_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
			@RequestParam("page") final int pageNumber, @RequestParam("size") final int pageSize)
			throws ResourceNotFoundException, ParseException {
		String status = "I";

		List<Object[]> results = null;

		results = em.createQuery(
				" SELECT j.id as jobId,c.id as candidateId,c.firstName,c.jobCategory,c.age,c.experience,"
				+ "c.currentState,c.currentCity,ja.appliedTime,u.profilePic,c.mobileNumber,c.whatsappNumber,c.languageKey,j.jobCategory as jobRole,a.status, "
				+ "c.industry,c.candidateType FROM  JobApplicationModel ja "
						+ "left join JobsModel j on  j.id = ja.jobId "
						+ "left join EmployerModel e on e.id = j.employerId "
						+ "left join CandidateModel c on c.id  =ja.candidateId "
						+ "left join UserModel u on  u.id = c.userId "
						+ "left join EmployerApplication a on j.employerId = a.employerId and j.id = a.jobId and c.id=a.candidateId "
						+ "where j.employerId = :emp_id and ja.status =:applied and (c.currentState=:location or :location IS NULL) "
						+ "and (j.jobCategory=:jobCategory or :jobCategory IS NULL) "
						+ "and (c.candidateType=:canType or :canType IS NULL) "
						+ "and (c.experience=:exp or :exp = 0)" + "and ja.appliedTime between :startdate and :enddate ",
				Object[].class).setParameter("emp_id", empId).setParameter("applied", status)
				.setParameter("startdate", startDate).setParameter("enddate", endDate)
				.setParameter("jobCategory", jobCategory).setParameter("canType", canType).setParameter("location", location).setParameter("exp", exp)
				.setFirstResult((pageNumber - 1) * pageSize).setMaxResults(pageSize).getResultList();

		if (results.isEmpty()) {
			throw new ResourceNotFoundException("Candidates not found.");
		}
		Collections.reverse(results);

		ArrayList<HashMap<String, String>> al = new ArrayList<>();
		for (Object[] row : results) {
			HashMap<String, String> count = new HashMap<>();
			count.put("jobId", String.valueOf(row[0]));
			count.put("candidateId", String.valueOf(row[1]));
			count.put("firstName", String.valueOf(row[2]));
			count.put("jobCategory", String.valueOf(row[3]));
			count.put("age", String.valueOf(row[4]));
			count.put("experience", String.valueOf(row[5]));
			count.put("currentState", String.valueOf(row[6]));
			count.put("currentCity", String.valueOf(row[7]));
			count.put("time", String.valueOf(row[8]));
			count.put("profilePic", String.valueOf(row[9]));
			count.put("mobileNumber", String.valueOf(row[10]));
			count.put("whatsappNumber", String.valueOf(row[11]));
			count.put("languageKey", String.valueOf(row[12]));
			count.put("jobRole", String.valueOf(row[13]));
			count.put("status", String.valueOf(row[14]));
			count.put("industry", String.valueOf(row[15]));
			count.put("candidateType", String.valueOf(row[16]));

			al.add(count);
		}

		return al;
	}
	
	@GetMapping(path = "/calledCanDetails")
	public ArrayList<HashMap<String, String>> getCalledCanDetails(@RequestParam("emp_id") final int empId,
			@RequestParam(value = "can_type", required = false) final String canType,
			@RequestParam(value = "job_category", required = false) final String jobCategory,
			@RequestParam(value = "location", required = false) final String location,
			@RequestParam(value = "exp", required = false) final int exp,
			@RequestParam("start_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
			@RequestParam("end_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
			@RequestParam("page") final int pageNumber, @RequestParam("size") final int pageSize)
			throws ResourceNotFoundException, ParseException {

		List<Object[]> results = null;


		results = em.createQuery(
				" SELECT j.id as jobId,c.id as candidateId,c.firstName,c.jobCategory,c.age,c.experience,c.currentState,c.currentCity,ja.callTime,u.profilePic,c.mobileNumber,c.whatsappNumber,c.languageKey,j.jobCategory as jobRole"
				+ " ,a.status,c.industry,c.candidateType FROM  CandidateCallModel ja "
						+ "left join EmployerModel e on  e.id = ja.empId "
						+ "left join CandidateModel c on c.id = ja.cId " + "left join JobsModel j on j.id  =ja.jId "
						+ "left join UserModel u on  u.id = c.userId "
						+ "left join EmployerApplication a on j.employerId = a.employerId and j.id = a.jobId and c.id=a.candidateId "
						+ "where ja.callTime between :startdate and :enddate " + "and ja.empId = :emp_id  and (c.currentState=:location or :location IS NULL) "
								+ "and (j.jobCategory=:jobCategory or :jobCategory IS NULL) and (c.candidateType=:canType or :canType IS NULL) and (c.experience=:exp or :exp = 0)",
				Object[].class).setParameter("emp_id", empId).setParameter("startdate", startDate)
				.setParameter("enddate", endDate).setParameter("jobCategory", jobCategory).setParameter("canType", canType)
				.setParameter("location", location).setParameter("exp", exp).setFirstResult((pageNumber - 1) * pageSize)
				.setMaxResults(pageSize).getResultList();

		if (results.isEmpty()) {
			throw new ResourceNotFoundException("Candidates not found.");
		}
		Collections.reverse(results);

		ArrayList<HashMap<String, String>> al = new ArrayList<>();
		for (Object[] row : results) {
			HashMap<String, String> count = new HashMap<>();
			count.put("jobId", String.valueOf(row[0]));
			count.put("candidateId", String.valueOf(row[1]));
			count.put("firstName", String.valueOf(row[2]));
			count.put("jobCategory", String.valueOf(row[3]));
			count.put("age", String.valueOf(row[4]));
			count.put("experience", String.valueOf(row[5]));
			count.put("currentState", String.valueOf(row[6]));
			count.put("currentCity", String.valueOf(row[7]));
			count.put("callTime", String.valueOf(row[8]));
			count.put("profilePic", String.valueOf(row[9]));
			count.put("mobileNumber", String.valueOf(row[10]));
			count.put("whatsappNumber", String.valueOf(row[11]));
			count.put("languageKey", String.valueOf(row[12]));
			count.put("jobRole", String.valueOf(row[13]));
			count.put("status", String.valueOf(row[14]));
			count.put("industry", String.valueOf(row[15]));
			count.put("candidateType", String.valueOf(row[16]));

			al.add(count);
		}

		return al;
	}

	@PostMapping(path = "/canScreeningStatus")
	public EmployerApplication setInterestedJobs(@RequestParam("emp_id") final int employerId,
			@RequestParam("can_id") final int candidateId, @RequestParam("job_id") final int jobId,
			@RequestParam("status") final String status) {

		EmployerApplication details = employerApplicationRepository.findByEmployerIdAndStatus(employerId, candidateId,
				jobId);

		if (details != null) {
			details.setStatus(status);
			employerApplicationRepository.save(details);

			return details;

		} else {
			EmployerApplication e = new EmployerApplication();

			e.setEmployerId(employerId);
			e.setCandidateId(candidateId);
			e.setJobId(jobId);
			e.setStatus(status);
			employerApplicationRepository.save(e);

			return e;
		}

	}
	
	@GetMapping(path = "/empCandidateStatus")
	public ResponseEntity<?> empcandidateStatus(@RequestParam("emp_id") final int employerId,
			@RequestParam("job_id") final int jobId, @RequestParam("can_id") final int candidateId) throws ResourceNotFoundException {
		EmployerApplication details = employerApplicationRepository.findByEmployerIdAndStatus(employerId, candidateId,
				jobId);

		if (details != null) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "success");
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			throw new ResourceNotFoundException("Candidate status not found.");
		}

	}
	
	@GetMapping(path = "/empFilterJobCategories")
	public ResponseEntity<?> empJobCategories(@RequestParam("emp_id") final int employerId) {
		EmployerModel em = employerRepository.findById(employerId).get();
		List<Map<String, Object>> details = jobRepository.getEmpMostJobCategories(employerId);

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
			map.put("message", "Employer not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	

}
