package com.taizo.service;


import com.taizo.model.EmployerApplication;

public interface EmployerApplicationService {

	EmployerApplication updateStatus(int employerId, int candidateId, int jobId, String status);

}
