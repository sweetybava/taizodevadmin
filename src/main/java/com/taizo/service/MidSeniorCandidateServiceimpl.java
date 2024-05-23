package com.taizo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.taizo.model.MidSeniorSourcingModel;
import com.taizo.repository.MidSeniorSourcingRepository;

@Service
public class MidSeniorCandidateServiceimpl implements MidSeniorCandidateService {
	
	@Autowired
	MidSeniorSourcingRepository midSeniorSourcingRepository;

	@Override
	public Page<MidSeniorSourcingModel> findAll(Specification<MidSeniorSourcingModel> spec, Pageable pageable) {
		// TODO Auto-generated method stub
		 return midSeniorSourcingRepository.findAll(spec, pageable);
	}



}
