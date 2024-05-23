package com.taizo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taizo.model.JobApplicationModel;
import com.taizo.repository.JobApplicationRepository;

@Service("jobApplicationService")
public class JobApplicationServiceImpl implements JobApplicationService {
	
	@Autowired
	JobApplicationRepository jobApplicationRepository;

	@Override
	public List<JobApplicationModel> findByStatus(int candidateId, String status) {
		// TODO Auto-generated method stub
		return jobApplicationRepository.findTopByStatus(candidateId,status);
	}

	@Override
	public JobApplicationModel updateMessage(int candidateId, int jobId, int employerId, String message) {
		// TODO Auto-generated method stub
		String status = "I";
		Optional<JobApplicationModel> details = jobApplicationRepository.findByCandidateIdAndJobIdAndStatus(candidateId, jobId,status);
		
		JobApplicationModel existing = details.get();

		existing.setEmployerId(employerId);
		existing.setMessage(message);
		existing.setUserMessage(null);
		existing = jobApplicationRepository.save(existing);		
		return existing;
	}

	@Override
	public JobApplicationModel updateReplyMessage(int candidateId, int jobId, String userMessage) {
		// TODO Auto-generated method stub
		String status = "I";
	Optional<JobApplicationModel> details = jobApplicationRepository.findByCandidateIdAndJobIdAndStatus(candidateId, jobId,status);

		JobApplicationModel existing = details.get();

		existing.setUserMessage(userMessage);
		existing = jobApplicationRepository.save(existing);		
		return existing;
		
	}


}
