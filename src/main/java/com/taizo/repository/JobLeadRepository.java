package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.JobLeadModel;

public interface JobLeadRepository extends JpaRepository<JobLeadModel, Integer> {
	
	Optional<JobLeadModel> findById(int id);
	
	List<JobLeadModel> findByEmployerId(int employerId);
	
	 Page<JobLeadModel> findByEmployerId(int employerId, Pageable pageable);

}
