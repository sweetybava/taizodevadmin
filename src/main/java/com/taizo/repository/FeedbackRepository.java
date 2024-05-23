package com.taizo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.EmployerModel;
import com.taizo.model.FeedbackModel;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackModel, Long> {

	
	

}
