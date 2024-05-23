package com.taizo.service;

import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taizo.model.EmployerApplication;
import com.taizo.repository.EmployerApplicationRepository;

@Service("employerApplicationService")
public class EmployerApplicationServiceImpl implements EmployerApplicationService {
	
	@Autowired
	EmployerApplicationRepository employerApplicationRepository;

	@Transactional
	@Override
	public EmployerApplication updateStatus(int employerId,int candidateId, int jobId, String status) {
		Optional<EmployerApplication> optional = employerApplicationRepository.findByEmployerIdAndJobId(employerId,candidateId,jobId);
		
		EmployerApplication existing = optional.get();
		
		existing.setStatus(status);
		
		existing = employerApplicationRepository.save(existing);		
		return existing;

	}


}
