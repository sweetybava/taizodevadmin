package com.taizo.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.CanInterviewNotificationModel;
import com.taizo.model.EmpInterviewNotificationModel;
import com.taizo.model.InterviewAddressesModel;
import com.taizo.model.InterviewsModel;
import com.taizo.model.JobsModel;
import com.taizo.model.RescheduleInterviewModel;

public interface InterviewService {

	Boolean saveInterview(Map<String, Object> requestBody);



	List<EmpInterviewNotificationModel> getAllCanNotifications(int candidateId);

	List<CanInterviewNotificationModel> getAllEmpNotifications(int empId);



	public ResponseEntity<?> saveInterviewDetails(InterviewsModel interview,InterviewAddressesModel ia)throws ResourceNotFoundException;



}
