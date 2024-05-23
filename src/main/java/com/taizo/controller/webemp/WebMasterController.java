package com.taizo.controller.webemp;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.model.BenefitsModel;
import com.taizo.model.CertificateCoursesModel;
import com.taizo.model.CfgAllCourseModel;
import com.taizo.model.CfgAreasModel;
import com.taizo.model.CfgEmpJobShiftTimings;
import com.taizo.model.CfgEmpPrescreeningQuestionsModel;
import com.taizo.model.CfgInterRequiredDocModel;
import com.taizo.model.CfgItiCourseModel;
import com.taizo.model.CfgPGCourseModel;
import com.taizo.model.CfgUGCourseModel;
import com.taizo.model.CityModel;
import com.taizo.model.DiplomaCourseModel;
import com.taizo.model.EmployerJobPersonalizationModel;
import com.taizo.model.GraduationCoursesModel;
import com.taizo.model.IndustryModel;
import com.taizo.model.JobCloseReasonModel;
import com.taizo.model.JobIndustryModel;
import com.taizo.model.JobRolesModel;
import com.taizo.model.KeySkillsModel;
import com.taizo.model.StateCityModel;
import com.taizo.repository.BenefitsRepository;
import com.taizo.repository.CertificateCourseRepository;
import com.taizo.repository.CfgAllCoursesRepository;
import com.taizo.repository.CfgAreasRepository;
import com.taizo.repository.CfgEmpPrescreeningQuestionsRepository;
import com.taizo.repository.CfgItiCourseRepository;
import com.taizo.repository.CfgPGCourseRepository;
import com.taizo.repository.CfgUGCourseRepository;
import com.taizo.repository.CityRepository;
import com.taizo.repository.DiplomaCourseRepository;
import com.taizo.repository.EmpJobShiftTimingsRepository;
import com.taizo.repository.EmployerJobPersonalizationRepository;
import com.taizo.repository.GraduationCourseRepository;
import com.taizo.repository.IndustryRepository;
import com.taizo.repository.InterviewRequiredDocRepository;
import com.taizo.repository.JobCloseReasonRepository;
import com.taizo.repository.JobIndustryRepository;
import com.taizo.repository.JobRolesRepository;
import com.taizo.repository.KeySkillsRepository;
import com.taizo.repository.StateCityRepository;

@CrossOrigin
@RestController
@RequestMapping("/webEmployer")
public class WebMasterController {

	@Autowired
	BenefitsRepository benefitsRepository;
	@Autowired
	IndustryRepository industryRepository;
	@Autowired
	EmpJobShiftTimingsRepository empJobShiftTimingsRepository;
	@Autowired
	JobRolesRepository jobRolesRepository;
	@Autowired
	JobIndustryRepository jobIndustryRepository;
	@Autowired
	StateCityRepository stateCityRepository;
	@Autowired
	InterviewRequiredDocRepository interviewReqDocRepository;
	@Autowired
	EmployerJobPersonalizationRepository employerJobPersonalizationRepository;
	@Autowired
	CfgEmpPrescreeningQuestionsRepository cfgEmpPrescreeningQuestionsRepository;
	
	@Autowired
	JobCloseReasonRepository jobCloseReasonRepository;
	
	@Autowired
	CityRepository cityRepository;
	
	@Autowired
	CfgAreasRepository cfgAreasRepository;
	
	@Autowired
	DiplomaCourseRepository diplomaCourseRepository;
	
	@Autowired
	CfgPGCourseRepository cfgPGCourseRepository;
	
	@Autowired
	CfgUGCourseRepository cfgUGCourseRepository;
	
	@Autowired
	CfgItiCourseRepository cfgItiCourseRepository;
	
	@Autowired
	CfgAllCoursesRepository cfgAllCourseRepository;

	@Autowired
	CertificateCourseRepository certificateCourseRepository;

	@Autowired
	GraduationCourseRepository graduationCourseRepository;

	@Autowired
	KeySkillsRepository keySkillsRepository;
	
	@GetMapping(path = "/benefits")
	public ResponseEntity<?> getBenefits() {

		List<BenefitsModel> details = benefitsRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Benefits Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
			
		}
	}

	@GetMapping(path = "/industries")
	public ResponseEntity<?> getEmployerIndustries() {

		List<IndustryModel> details = industryRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Industries Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	@GetMapping(path = "/jobIndustries")
	public ResponseEntity<?> getEmployerJobIndustries() {

		List<JobIndustryModel> details = jobIndustryRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Industries Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	@GetMapping(path = "/jobShiftTimings")
	public ResponseEntity<?> getJobShiftTimings(@RequestParam("shift_type") final String shiftType) {

		List<CfgEmpJobShiftTimings> details = empJobShiftTimingsRepository.findByShiftType(shiftType);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "success");
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Shift timings not available");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	@GetMapping(path = "/fullTimeJobRoles")
	public ResponseEntity<?> getFullTimeJobRoles(@RequestParam( value = "industry_id", required = false , defaultValue = "0") Integer industryId) {

		List<JobRolesModel> details = jobRolesRepository.findAll(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Job Roles Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	@GetMapping(path = "/cities")
	public ResponseEntity<?> getIndiaStateCitiess(@RequestParam(value = "state_id", required = false) final int stateId) {

		//List<StateCityModel> details = stateCityRepository.findByStateId(stateId);
		List<CityModel> details = cityRepository.findAllByEmpActive();


		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Cities Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/areas")
	public ResponseEntity<?> getIndianCitiesArea(@RequestParam(value = "city_id", required = false) final int cityId) {

		//List<StateCityModel> details = stateCityRepository.findByStateId(stateId);
		List<CfgAreasModel> details = cfgAreasRepository.findByCityIdandActvie(cityId,true);


		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Area Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

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
			map.put("statuscode", 200);
			map.put("message", "interviewRequiredDoc Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/getJobPersonalization")
	public ResponseEntity<?> getJobPersonalization() {

		List<EmployerJobPersonalizationModel> details = employerJobPersonalizationRepository.findAllByActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Personalization Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/getPrescreeningQuestions")
	public ResponseEntity<?> getPreQuesions(@RequestParam("status") final String status) {

		List<CfgEmpPrescreeningQuestionsModel> details = cfgEmpPrescreeningQuestionsRepository.finByType(status);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Prescreening Questions");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/jobCloseReasons")
	public ResponseEntity<?> getjobCloseReasons() {

		List<JobCloseReasonModel> details = jobCloseReasonRepository.findAll();

		if (!details.isEmpty()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Job Close Reasons Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/allCourses")
	public ResponseEntity<?> getAllCourses() {

		List<CfgAllCourseModel> details = cfgAllCourseRepository.findByEmpActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Courses Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	
	@GetMapping(path = "/diplomaCourses")
	public ResponseEntity<?> getFullTimeJobRoless() {

		List<DiplomaCourseModel> details = diplomaCourseRepository.findByEmpActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Diploma Course Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	@GetMapping(path = "/UGCourses")
	public ResponseEntity<?> getUGCourses() {

		List<CfgUGCourseModel> details = cfgUGCourseRepository.findByEmpActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "UG Courses Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}
	@GetMapping(path = "/PGCourses")
	public ResponseEntity<?> getPGCourses() {

		List<CfgPGCourseModel> details = cfgPGCourseRepository.findByEmpActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "UG Courses Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}
	@GetMapping(path = "/ITICourses")
	public ResponseEntity<?> getITICourses() {

		List<CfgItiCourseModel> details = cfgItiCourseRepository.findByEmpActive(true);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "UG Courses Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}
	@GetMapping(path = "/certificateCourses")
	public ResponseEntity<?> getPartTimeJobRoles() {

		List<CertificateCoursesModel> details = certificateCourseRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Certificate Course Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	@GetMapping(path = "/graduationCourses")
	public ResponseEntity<?> getFreelancerJobRoless() {

		List<GraduationCoursesModel> details = graduationCourseRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Graduation Course Not Found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}
	
	@GetMapping(path = "/keySkills")
	public ResponseEntity<?> getKeySkills() {

		List<KeySkillsModel> details = keySkillsRepository.findEmpKeyskills();

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
			map.put("message", "Key Skills Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	

}
