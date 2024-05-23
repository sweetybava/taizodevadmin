package com.taizo.controller.landing;

import java.util.HashMap; 
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taizo.controller.gallabox.ExotelCallController;
import com.taizo.model.Admin;
import com.taizo.model.CityModel;
import com.taizo.model.EmpEnquiryModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.JobIndustryModel;
import com.taizo.model.LeadModel;
import com.taizo.model.WAAlert;
import com.taizo.repository.AdminRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.CityRepository;
import com.taizo.repository.EmpEnquiryRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.JobIndustryRepository;
import com.taizo.repository.LeadRepository;
import com.taizo.service.WAAlertService;

@CrossOrigin
@RestController
@RequestMapping("/landing")
public class EmpEnquiryController {
	
	
	@Autowired
	CityRepository cityRepository;
	
	@Autowired
	AdminRepository adminRepository;
	
	@Autowired
	CandidateRepository candidateRepository;
	
	@Autowired
	ExotelCallController exotelCallController;
	
	@Autowired
	WAAlertService waAlertService;
	
	@Autowired
	JobIndustryRepository jobIndustryRepository;
	
	@Autowired
	EmployerRepository employerRepository;
	
	@Autowired
	LeadRepository leadRepository;
	
	@Autowired
	EmpEnquiryRepository empEnquiryRepository;
	
	@Value("${spring.profiles.active}")
	private String activeProfile;
	
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
	
	 @PostMapping("/empEnquiry")
	    public ResponseEntity<?> createEmpEnquiry(@RequestBody EmpEnquiryModel empEnquiryModel) {
		 		 
		     String  companyName=empEnquiryModel.getCompanyName();  
		     String industry=empEnquiryModel.getIndustry();
		     String city=empEnquiryModel.getCity();
		     String name=empEnquiryModel.getContactPersonName();
		     String email=empEnquiryModel.getEmailId();
		     String mn=empEnquiryModel.getMobileNumber();
		     String type=empEnquiryModel.getType();
		     String designation=empEnquiryModel.getDesignation();
		 
	        try {
	            EmpEnquiryModel createdModel = empEnquiryRepository.save(empEnquiryModel);
	            EmployerModel employer=employerRepository.findByMobileNumber(Long.valueOf(mn));
	            
	            HashMap<String, String> empEnquiry = new HashMap<>();
	            if(employer!=null) {
	            	type="Registered";
	            }
	            else {
	            	type="new";
	            }
	            empEnquiry.put("type", type);
	            empEnquiry.put("company_name", companyName);
	            empEnquiry.put("industry", industry);
	            empEnquiry.put("city", city);
	            empEnquiry.put("contact_person_name", name);
	            empEnquiry.put("email", email);
	            empEnquiry.put("mobile_number", "91" + String.valueOf(mn));
	            empEnquiry.put("designation",designation);
				
				waAlertService.sendEmployerEnquiry(empEnquiry);
	         
				createdModel.setWaNotification(true);
				empEnquiryRepository.save(createdModel);
				
				if (activeProfile.equalsIgnoreCase("prod")) {
					   HashMap<String, String> empEnquirys = new HashMap<>();
			            if(employer!=null) {
			            	type="Registered";
			            }
			            else {
			            	type="new";
			            }
			            empEnquirys.put("type", type);
			            empEnquirys.put("company_name", companyName);
			            empEnquirys.put("industry", industry);
			            empEnquirys.put("city", city);
			            empEnquirys.put("contact_person_name", name);
			            empEnquirys.put("email", email);
			            empEnquirys.put("designation", designation);
			            empEnquirys.put("mobile_number", "91" + String.valueOf(mn));
			            exotelCallController.connectToAgentcall("+91" + String.valueOf(empEnquiryModel.getMobileNumber()), empEnquirys);
						
				}
	            
	            return new ResponseEntity<>(createdModel, HttpStatus.CREATED);
	        } catch (Exception e) {
	            Map<String, Object> response = new HashMap<>();
	            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
	            response.put("message", "Failed to create the empEnquiry");
	            response.put("error", e.getMessage());
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	        }

	    }
	
	@GetMapping("/checkMobileNo")
	public ResponseEntity<Map<String, Object>> checkMobileNumberPresence(@RequestParam long mobileNumber) {
		EmployerModel employer = employerRepository.findByMobileNumber(mobileNumber);

		HashMap<String, Object> responseMap = new HashMap<>();
		if (employer != null) {
			responseMap.put("status", "success");
			responseMap.put("message", "Mobile number is present.");
			responseMap.put("code", HttpStatus.OK.value());
			responseMap.put("data", employer);
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		} else {
			responseMap.put("status", "error");
			responseMap.put("code", HttpStatus.NOT_FOUND.value());
			responseMap.put("message", "Mobile number is not present.");
			return new ResponseEntity<>(responseMap, HttpStatus.NOT_FOUND);
		}
	}
	

	@PostMapping("/Testing")
    public ResponseEntity<?> createEmpEnquirys(@RequestBody EmpEnquiryModel empEnquiryModel) {
	 		 
	     String  companyName=empEnquiryModel.getCompanyName();  
	     String industry=empEnquiryModel.getIndustry();
	     String city=empEnquiryModel.getCity();
	     String name=empEnquiryModel.getContactPersonName();
	     String email=empEnquiryModel.getEmailId();
	     String mn=empEnquiryModel.getMobileNumber();
	     String type=empEnquiryModel.getType();
	 
        try {
            EmpEnquiryModel createdModel = empEnquiryRepository.save(empEnquiryModel);
            EmployerModel employer=employerRepository.findByMobileNumber(Long.valueOf(mn));
            
            
            HashMap<String, String> empEnquiry = new HashMap<>();
            if(employer!=null) {
            	type="Registered";
            }
            else {
            	type="new";
            }
            empEnquiry.put("type", type);
            empEnquiry.put("company_name", companyName);
            empEnquiry.put("industry", industry);
            empEnquiry.put("city", city);
            empEnquiry.put("contact_person_name", name);
            empEnquiry.put("email", email);
            empEnquiry.put("mobile_number", "91" + String.valueOf(mn));
			
			waAlertService.sendEmployerEnquirys(empEnquiry);
         
			createdModel.setWaNotification(true);
			empEnquiryRepository.save(createdModel);
			
			if (activeProfile.equalsIgnoreCase("local")) {
				   HashMap<String, String> empEnquirys = new HashMap<>();
		            if(employer!=null) {
		            	type="Registered";
		            }
		            else {
		            	type="new";
		            }
		            empEnquirys.put("type", type);
		            empEnquirys.put("company_name", companyName);
		            empEnquirys.put("industry", industry);
		            empEnquirys.put("city", city);
		            empEnquirys.put("contact_person_name", name);
		            empEnquirys.put("email", email);
		            empEnquirys.put("mobile_number", "91" + String.valueOf(mn));
		            exotelCallController.connectToAgentcalls("+91" + String.valueOf(empEnquiryModel.getMobileNumber()), empEnquirys);
					
			}
            
            return new ResponseEntity<>(createdModel, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Failed to create the empEnquiry");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

}

