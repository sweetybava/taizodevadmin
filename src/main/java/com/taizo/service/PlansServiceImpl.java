package com.taizo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taizo.model.LeadModel;
import com.taizo.model.PlansModel;
import com.taizo.repository.PlansRepository;

@Service("plansService")
public class PlansServiceImpl implements PlansService {

    @Autowired
    PlansRepository plansRepository;

    @Transactional
	@Modifying
	@Override
	public PlansModel update(int id, PlansModel plan) {
		// TODO Auto-generated method stub
		Optional<PlansModel> optional =  plansRepository.findById(id);
		
		PlansModel existing = optional.get();
		
		existing.setPlanName(plan.getPlanName());
		existing.setJobPosting(plan.getJobPosting());
		existing.setActiveJobs(plan.getActiveJobs());
		existing.setProfiles(plan.getProfiles());
		existing.setAmount(plan.getAmount());
		
		 plansRepository.save(existing);	
		 
		return existing;	
		}

	@Override
	public List<PlansModel> findActivePlans(boolean isActive) {
		
		return plansRepository.findByActive(isActive);
	}
    
}
