package com.taizo.repository;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.InterviewsModel;
import com.taizo.model.RescheduleInterviewModel;

@Repository
public interface RescheduleInterviewRepository extends JpaRepository<RescheduleInterviewModel, Integer>, 
	JpaSpecificationExecutor<RescheduleInterviewModel> {

	@Query("select t from RescheduleInterviewModel t where t.id = :id")
	Optional<RescheduleInterviewModel> findById(int id);

	Optional<RescheduleInterviewModel> findByInterviewId(int interviewId);

	
}
