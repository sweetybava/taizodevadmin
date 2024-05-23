package com.taizo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.taizo.model.MidSeniorSourcingModel;

public interface MidSeniorCandidateService {

	Page<MidSeniorSourcingModel> findAll(Specification<MidSeniorSourcingModel> spec, Pageable pageable);

}
