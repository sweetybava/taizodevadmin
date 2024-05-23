package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.taizo.model.EmployerJobPrescreeningQuestionsModel;
import com.taizo.model.JobsModel;

public interface EmployerJobPrescreeningQuestionsRepository extends JpaRepository<EmployerJobPrescreeningQuestionsModel, Long>{
	
	
	
	
}
