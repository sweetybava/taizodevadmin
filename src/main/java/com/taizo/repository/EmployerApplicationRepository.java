package com.taizo.repository;


import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.EmpInterviewNotificationModel;
import com.taizo.model.EmployerApplication;
import com.taizo.model.JobApplicationModel;

@Repository
public interface EmployerApplicationRepository extends JpaRepository<EmployerApplication, Long>{

	EmployerApplication findTopByEmployerId(int employerId);

	@Query("select j from EmployerApplication j where j.employerId = :employerId and j.candidateId = :candidateId and j.jobId = :jobId")
	EmployerApplication findByEmployerIdAndStatus(@Param("employerId") int employerId,@Param("candidateId") int candidateId,
			@Param("jobId") int jobId);

	@Query("select j from EmployerApplication j where j.employerId = :employerId and j.candidateId = :candidateId and j.jobId = :jobId")
	Optional<EmployerApplication> findByEmployerIdAndJobId(@Param("employerId") int employerId,@Param("candidateId") int candidateId,
			@Param("jobId") int jobId);

	


	




}
