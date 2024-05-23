package com.taizo.controller.employer;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.model.BenefitsModel;
import com.taizo.model.CertificateCoursesModel;
import com.taizo.model.CfgAreasModel;
import com.taizo.model.CfgStateCityModel;
import com.taizo.model.CfgStateModel;
import com.taizo.model.CityModel;
import com.taizo.model.CountryCitiesModel;
import com.taizo.model.CountryModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.IndiaStateModel;
import com.taizo.model.IndustryModel;
import com.taizo.model.JobIndustryModel;
import com.taizo.model.JobRolesModel;
import com.taizo.model.JobsModel;
import com.taizo.model.KeySkillsModel;
import com.taizo.model.StateCityModel;
import com.taizo.repository.BenefitsRepository;
import com.taizo.repository.CertificateCourseRepository;
import com.taizo.repository.CfgAreasRepository;
import com.taizo.repository.CfgStateRepository;
import com.taizo.repository.CityRepository;
import com.taizo.repository.CountryCitiesRepository;
import com.taizo.repository.CountryRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.IStateCityRepository;
import com.taizo.repository.IndiaStateRepository;
import com.taizo.repository.IndustryRepository;
import com.taizo.repository.JobIndustryRepository;
import com.taizo.repository.JobRepository;
import com.taizo.repository.JobRolesRepository;
import com.taizo.repository.KeySkillsRepository;
import com.taizo.repository.StateCityRepository;

@CrossOrigin
@RestController
public class AddressController {

	@Autowired
	CountryRepository countryRepository;

	@Autowired
	CityRepository cityRepository;
	
	@Autowired
	CfgAreasRepository cfgAreasRepository;

	@Autowired
	IndiaStateRepository indiaStateRepository;

	@Autowired
	IndustryRepository industryRepository;

	@Autowired
	JobIndustryRepository jobIndustryRepository;

	@Autowired
	StateCityRepository stateCityRepository;
	
	@Autowired
	CountryCitiesRepository countryCitiesRepository;

	@Autowired
	BenefitsRepository benefitsRepository;
	
	@Autowired
	CfgStateRepository cfgStateRepository;
	
	@Autowired
	KeySkillsRepository keySkillsRepository;
	
	@Autowired
	CertificateCourseRepository certificateCourseRepository;
	
	@Autowired
	JobRolesRepository jobRolesRepository;
	
	@Autowired
	EmployerRepository employerRepository;
	
	@Autowired
	JobRepository jobRepository;
	
	@GetMapping(path = "/sample")
	public ResponseEntity<?> getSample() {

		List<Map<String, Object>> details = cfgStateRepository.getData();
		List<JobsModel> jobs = jobRepository.findJobExpiryinDay();
		List<EmployerModel> employers = employerRepository.findByPlanExpireOneDay();

		if (details.size()>0) {
			/*
			 * for(Map<String, Object> c : details) { String state =
			 * String.valueOf(c.get("city"));
			 * 
			 * Optional<CityModel> d = cityRepository.findByCity(state);
			 * 
			 * String co = String.valueOf((BigInteger) c.get("count"));
			 * 
			 * d.get().setEmpOrderNo(Integer.parseInt(co)); cityRepository.save(d.get());
			 * 
			 * System.out.println(d.get().getCity()); }
			 */

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("job", jobs);
			map.put("oneday", employers);

			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Countries Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/countries")
	public ResponseEntity<?> getCountries() {

		List<CountryModel> details = countryRepository.findAll();
		    
		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Countries Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/overseasCountries")
	public ResponseEntity<?> getOverseasCountries() {

		String specification = "O";

		List<CountryModel> details = countryRepository.findBySpecification(specification);
		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Countries Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/cities")
	public ResponseEntity<?> getCities() {

		List<CityModel> details = cityRepository.findAllByActive();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Cities Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
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
			map.put("message", "Areas Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping(path = "/employer/cities")
	public ResponseEntity<?> getStateCities(@RequestParam(value = "state_id", required = false) final int stateId) {

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
	
	@GetMapping(path = "/employer/areas")
	public ResponseEntity<?> getCitiesAreas(@RequestParam(value = "city_id", required = false) final int cityId) {

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

	@GetMapping(path = "/industries")
	public ResponseEntity<?> getIndustries() {

		List<IndustryModel> details = industryRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Industries Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/employer/industries")
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
			map.put("code", 400);
			map.put("message", "Industries Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/jobIndustries")
	public ResponseEntity<?> getJobIndustries() {

		List<JobIndustryModel> details = jobIndustryRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Industries Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/employer/jobIndustries")
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
			map.put("code", 400);
			map.put("message", "Industries Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/indiaStates")
	public ResponseEntity<?> getIndiaStates() {

		List<IndiaStateModel> details = indiaStateRepository.findByJSOrder();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "States Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/indiaSpecificStates")
	public ResponseEntity<?> getIndiaSpecificStates() {
		String specification = "D";

		List<IndiaStateModel> details = indiaStateRepository.findBySpecification(specification);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "States Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/indiaStateCities")
	public ResponseEntity<?> getIndiaStateCities(@RequestParam("state_id") final int stateId) {

		List<StateCityModel> details = stateCityRepository.findByJSStateId(stateId);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Cities Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/employer/indiaStateCities")
	public ResponseEntity<?> getIndiaStateCitiess(@RequestParam("state_id") final int stateId) {

		List<StateCityModel> details = stateCityRepository.findByJSStateId(31);

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
			map.put("message", "Cities Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/countryCities")
	public ResponseEntity<?> getCountryCities(@RequestParam("country_id") final int stateId) {

		List<CountryCitiesModel> details = countryCitiesRepository.findByCountryId(stateId);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Cities Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(path = "/employer/benefits")
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
			map.put("code", 400);
			map.put("message", "Benefits Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping(path = "/benefits")
	public ResponseEntity<?> getCanBenefits() {

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
			map.put("code", 400);
			map.put("message", "Benefits Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping(path = "/employer/states")
	public ResponseEntity<?> getIndiaSpecificStates(@RequestParam("country_id") final int countryId) {

		List<CfgStateModel> details = cfgStateRepository.findByCountryId(1);

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("states", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("states", details);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping(path = "/employer/indiaStates")
	public ResponseEntity<?> getEmpIndiaStates() {

		List<IndiaStateModel> details = indiaStateRepository.findAll();

		if (!details.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", details);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "States Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
}
