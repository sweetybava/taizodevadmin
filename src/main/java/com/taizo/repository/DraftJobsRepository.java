package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.DraftJobsModel;


public interface DraftJobsRepository extends JpaRepository<DraftJobsModel, Integer> {

	  List<DraftJobsModel> findByEmployerId(int employerId);
	  
	  Optional<DraftJobsModel> findByIdAndEmployerId(int id, int employerId);

	void deleteById(int id);
	
	  List<DraftJobsModel> findAllByOrderByCreatedTimeDesc();
	  
	    Page<DraftJobsModel> findByEmployerId(int employerId, Pageable pageable);

	 }
