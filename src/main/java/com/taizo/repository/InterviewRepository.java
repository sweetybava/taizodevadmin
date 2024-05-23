package com.taizo.repository;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.InterviewsModel;

@Repository
public interface InterviewRepository extends JpaRepository<InterviewsModel, Integer>, 
	JpaSpecificationExecutor<InterviewsModel> {

	@Query("select t from InterviewsModel t where t.id = :id")
	Optional<InterviewsModel> findById(int id);
	
	@Query(value = "CALL SaveInterviewDetails(:id);", nativeQuery = true)
	int findCarsAfterYear(@Param("id") Integer id);
	
	@Query(value = "{call GetInterviewDetailsById(:id)}", nativeQuery = true)
	Map<String, Object> findInterviewByID(@Param("id")int interviewId);
	
	@Query(value = "{call GetCanInterviewDetailsById(:id,:status)}", nativeQuery = true)
	Map<String, Object> findInterviewByIdAndStatus(@Param("id")int interviewId,@Param("status") String status);

	@Query(value = "{call GetEmpInterviewDetailsByJobId(:empId,:jobId,:startDate,:endDate)}", nativeQuery = true)
	List<Map<String, Object>> findjobScheduledInterviewDetails(@Param("empId")int empId,@Param("jobId")int jobId,
			@Param("startDate") String startdate,@Param("endDate") String endDate);
	
	@Query(value = "{call GetEmpInterestedProfileDetails(:empId,:jobId,:startDate,:endDate)}", nativeQuery = true)
	List<Map<String, Object>> findjobAppliedCanDetails(@Param("empId")int empId,@Param("jobId")int jobId,
			@Param("startDate") String startdate,@Param("endDate") String endDate);

	@Query(value = "{call GetEmpInterviewDetailsByDate(:empId,:startDate,:endDate,:jobRole)}", nativeQuery = true)
	List<Map<String, Object>> getinterviewDetailsByDate(@Param("empId")int empId,@Param("startDate") String startdate,@Param("endDate") String endDate,
			@Param("jobRole") String jobRole);

	@Query(value = "{call GetCanInterviewDetailsByStatus(:candidateId,:status)}", nativeQuery = true)
	List<Map<String, Object>> findCanInterviewByStatus(@Param("candidateId")int candidateId,@Param("status") String status);

	@Query(value = "{call CanInterviewNotificationDetails(:candidateId)}", nativeQuery = true)
	List<Map<String, Object>> getCanInterviewNotification(@Param("candidateId")int candidateId);
	
	@Query(value = "{call EmpInterviewNotificationDetails(:employerId)}", nativeQuery = true)
	List<Map<String, Object>> getEmpInterviewNotification(@Param("employerId")int employerId);

	@Query("select t from InterviewsModel t where t.jobId = ?1")
	Page<InterviewsModel> getCanInterviewScheduledNotificationCount(int id, PageRequest of);


	


}
