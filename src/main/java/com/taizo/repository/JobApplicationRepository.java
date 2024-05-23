package com.taizo.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CandidateCallModel;
import com.taizo.model.JobApplicationModel;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplicationModel, Long>{


	JobApplicationModel findTopByCandidateId(int candidateId);

	@Query("select j from JobApplicationModel j where j.candidateId = :candidateId and j.status = :status")
	List<JobApplicationModel> findTopByStatus(@Param("candidateId") int candidateId,@Param("status") String status);

	@Query("select j from JobApplicationModel j where j.candidateId = :candidateId and j.jobId = :jobId")
	Optional<JobApplicationModel> findByCandidateIdAndJobId(@Param("candidateId") int candidateId,@Param("jobId") int jobId);

	List<JobApplicationModel> findByJobId(int jobId);

	@Query("select j from JobApplicationModel j where j.candidateId = :candidateId and j.jobId = :jobId and j.status = :status")
	Optional<JobApplicationModel> findByCandidateIdAndJobIdAndStatus(@Param("candidateId") int candidateId,@Param("jobId") int jobId,@Param("status") String status);
	
	@Query("select j from JobApplicationModel j where j.jobId = :jobId and j.status = :status")
	List<JobApplicationModel> findByJobIdAndStatus(@Param("jobId") int jobId,@Param("status") String status);

	@Query("select j from JobApplicationModel j where j.employerId = ?1 and j.appliedTime > ?2")
	Page<JobApplicationModel> getJobApplicationCount(Integer empId, Date date, Pageable pageable);

	@Query("select j from JobApplicationModel j where j.jobId = ?1  and j.status = ?2")
	Page<JobApplicationModel> getCanAppliedNotificationCount(int id,  String status, PageRequest of);

	void deleteById(int id);

	Optional<JobApplicationModel> findTopByCandidateIdAndJobId(int candidateId, int jobId);
	
	

}
