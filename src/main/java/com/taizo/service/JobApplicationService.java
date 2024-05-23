package com.taizo.service;

import java.util.List;

import com.taizo.model.JobApplicationModel;

public interface JobApplicationService {

	List<JobApplicationModel> findByStatus(int candidateId,String status);

	JobApplicationModel updateMessage(int candidateId, int jobId, int employerId, String message);

	JobApplicationModel updateReplyMessage(int candidateId, int jobId, String userMessage);

}
