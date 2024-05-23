package com.taizo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.taizo.model.JobLeadModel;

public interface JobLeadService {
	
	Optional<JobLeadModel> getJobLeadById(int id);
	
	List<JobLeadModel> getJobLeadsByEmployerId(int employerId);
	
	void deleteJobLead(int id);
	
    Page<JobLeadModel> findByEmployerId(int employerId, int page, int size);
    
    public Page<JobLeadModel> getAllJobLeads(Pageable pageable);

}
