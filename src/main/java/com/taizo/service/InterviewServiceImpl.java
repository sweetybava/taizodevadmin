package com.taizo.service;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.CanInterviewNotificationModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.EmpInterviewNotificationModel;
import com.taizo.model.EmployerActivityModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.InterviewAddressesModel;
import com.taizo.model.InterviewsModel;
import com.taizo.model.JobsModel;
import com.taizo.repository.CanInterviewNotificationRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.EmpActivityRepository;
import com.taizo.repository.EmpInterviewNotificationRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.InterviewAddressRepository;
import com.taizo.repository.InterviewRepository;
import com.taizo.repository.JobRepository;



@Service("interviewService")
public class InterviewServiceImpl implements InterviewService {
	
	private static final Logger logger = LoggerFactory.getLogger(InterviewServiceImpl.class);


	@Autowired
	EmpInterviewNotificationRepository empInterviewNotRepository;

	@Autowired
	CanInterviewNotificationRepository canInterviewNotRepository;
	
	@Autowired
	EmpInterviewNotificationRepository empInterviewNotificationRepository;
	
	@Autowired
	InterviewRepository interviewRepository;
	
	@Autowired
	EmployerRepository employerRepository;
	
	@Autowired
    CandidateRepository candidateRepository;
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	InterviewAddressRepository interviewAddressRepository;

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	EmpActivityRepository empActivityRepository;

	@Override
	public Boolean saveInterview(Map<String, Object> requestBody) {
		// TODO Auto-generated method stub
		// "login" this is the name of your procedure
		StoredProcedureQuery query = entityManager.createStoredProcedureQuery("SaveInterviewDetails");

		// Declare the parameters in the same order
		query.registerStoredProcedureParameter("emp_id", Integer.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("job_id", Integer.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("can_id", Integer.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("scheduled_on", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("start_tm", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("end_tm", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("landmark", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("address", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("contact_person_name", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("mobile_number", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("latitude", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("longitude", String.class, ParameterMode.IN);
		
		 query.registerStoredProcedureParameter("Result_Param", Integer.class, ParameterMode.OUT);
		 query.registerStoredProcedureParameter("interview_id", Integer.class, ParameterMode.OUT);


		// Pass the parameter values
		query.setParameter("emp_id", requestBody.get("emp_id"));
		query.setParameter("job_id", requestBody.get("job_id"));
		query.setParameter("can_id", requestBody.get("can_id"));
		query.setParameter("scheduled_on", requestBody.get("scheduled_on"));
		query.setParameter("start_tm", requestBody.get("start_tm"));
		query.setParameter("end_tm", requestBody.get("end_tm"));
		query.setParameter("landmark", requestBody.get("landmark"));
		query.setParameter("address", requestBody.get("address"));
		query.setParameter("contact_person_name", requestBody.get("contact_person_name"));
		query.setParameter("mobile_number", requestBody.get("mobile_number"));
		query.setParameter("latitude", requestBody.get("latitude"));
		query.setParameter("longitude", requestBody.get("longitude"));

		// Execute query
		query.execute();
		
		
		/*
		 * //Get output parameters Integer outCode = (Integer) int count = ((Number)
		 * query.getOutputParameterValue("Result_Param")).intValue();
		 * System.out.println(count);
		 * 
		 * if (count == 1) { System.out.println("Interview added successfully.");
		 * 
		 * } else { System.out.println("Interview updated successfully."); }
		 */
        
		return true;
	}

	@Override
	public List<EmpInterviewNotificationModel> getAllCanNotifications(int candidateId) {
		// TODO Auto-generated method stub
		List<EmpInterviewNotificationModel> list = empInterviewNotRepository.getAllCanNotifications(candidateId);
		return list;
	}

	@Override
	public List<CanInterviewNotificationModel> getAllEmpNotifications(int empId) {
		// TODO Auto-generated method stub
		List<CanInterviewNotificationModel> list = canInterviewNotRepository.getAllEmpNotifications(empId);
		return list;
	}

	@Override
	public ResponseEntity<?> saveInterviewDetails(InterviewsModel interview, InterviewAddressesModel ia)
			throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		
		Optional<CandidateModel> can = candidateRepository.findById(interview.getCanId());
		Optional<JobsModel> job = jobRepository.findById(interview.getJobId());
		
		if (!job.isPresent()) {
			logger.debug("Job not found {}.",interview.getJobId());
			throw new ResourceNotFoundException("Interview not found.");
		}
		if (!can.isPresent()) {
			logger.debug("Candidate not found {}.",interview.getCanId());
			throw new ResourceNotFoundException("Interview not found.");
		}
		
		
		InterviewAddressesModel a = new InterviewAddressesModel();
		a.setEmpId(interview.getEmpId());
		a.setAddress(ia.getAddress());
		a.setLandmark(ia.getLandmark());
		a.setLatitude("Latitude");
		a.setLongitude("Longitude");
		
		a=interviewAddressRepository.save(a);
		
		InterviewsModel i = new InterviewsModel();
		i.setEmpId(interview.getEmpId());
		i.setCanId(interview.getCanId());
		i.setJobId(interview.getJobId());
		i.setAddressId(ia.getId());
		i.setScheduledDate(interview.getScheduledDate());
		i.setStartTime(interview.getStartTime());
		i.setContactPersonName(interview.getContactPersonName());
		i.setMobileNumber(interview.getMobileNumber());
		i.setDocuments(interview.getDocuments());
		
		interviewRepository.save(i);
		
		Optional<CandidateModel> CModel = candidateRepository.findById(interview.getCanId());

		
		EmployerActivityModel EA = new EmployerActivityModel();
		EA.setEmpId(interview.getEmpId());
		EA.setActivity(CModel.get().getFirstName()+" - "+CModel.get().getJobCategory() +" , <b>interview</b> has been scheduled!");
		empActivityRepository.save(EA);
		
		
		
        return null;
	}

	
	

	
	//https://www.appsdeveloperblog.com/calling-a-stored-procedure-in-spring-boot-rest-with-jpa/

}
