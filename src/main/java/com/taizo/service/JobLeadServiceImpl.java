package com.taizo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.taizo.model.JobLeadModel;
import com.taizo.repository.JobLeadRepository;


@Service
public class JobLeadServiceImpl implements JobLeadService {
	
	@Autowired
	private JobLeadRepository jobLeadRepository;

	@Override
	public Optional<JobLeadModel> getJobLeadById(int id) {
		return jobLeadRepository.findById(id);
	}

	@Override
	public List<JobLeadModel> getJobLeadsByEmployerId(int employerId) {
		return jobLeadRepository.findByEmployerId(employerId);
	}

	@Override
	public void deleteJobLead(int id) {
		jobLeadRepository.deleteById(id);
	}

	@Override
	public Page<JobLeadModel> findByEmployerId(int employerId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());
	    return jobLeadRepository.findByEmployerId(employerId, pageable);
	}


	@Override
	public Page<JobLeadModel> getAllJobLeads(Pageable pageable) {
		 return jobLeadRepository.findAll(pageable);
	}

}
