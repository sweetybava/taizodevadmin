package com.taizo.repository;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CanInterviewsModel;
import com.taizo.model.InterviewsModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
@Repository
public interface CanInterviewRepository extends JpaRepository<CanInterviewsModel, Integer>, 
	JpaSpecificationExecutor<CanInterviewsModel> {

	@Query("select t from CanInterviewsModel t where t.canId = :canId")
	ArrayList<CanInterviewsModel> findByCanId(int canId);
	
	@Query("select t from CanInterviewsModel t where t.canId = :canId and t.jobId = :jobId")
	ArrayList<CanInterviewsModel> findByCanIdandJobId(int canId,int jobId);
	
	@Query("select t from CanInterviewsModel t where t.canId = :canId and status='I'")
	ArrayList<CanInterviewsModel> findByCanIdandStatus(int canId);

    @Query(value = "SELECT * FROM TaizoDB.can_interviews where active is true and Date(interview_scheduled_dt) = current_date()",nativeQuery = true)
    List<CanInterviewsModel> findInterviewReminderOnDay();
    
    @Query(value = "SELECT * FROM TaizoDB.can_interviews where active is true and Date(interview_scheduled_dt) = current_date()-2",nativeQuery = true)
    List<CanInterviewsModel> findBeforeInterviewReminder();
    
    List<CanInterviewsModel> findByCanIdAndStatus(int canId, String status);
    
    @Query("SELECT c FROM CanInterviewsModel c WHERE c.active = true")
    Page<CanInterviewsModel> findAllActive(Pageable pageable);
    
    @Query("SELECT c FROM CanInterviewsModel c WHERE c.jobId = :jobId AND c.active = true")
	Page<CanInterviewsModel> findByJobId(Integer jobId, Pageable pageable);


	Page<CanInterviewsModel> findByAdminId(int assignTo, Pageable pageable);
	
	@Query(value = "SELECT * FROM can_interviews WHERE job_id = :jobId AND interview_date BETWEEN :startDate AND :endDate", nativeQuery = true)
	Page<CanInterviewsModel> findByJobIdAndInterviewDateBetween(
	        @Param("jobId") int jobId,
	        @Param("startDate") LocalDateTime startDate,
	        @Param("endDate") LocalDateTime endDateTime,
	        Pageable pageable);
	

    long countByJobIdAndIsAttended(int jobId, boolean isAttended);

    long countByJobIdAndIsSelected(int jobId, boolean isSelected);

    long countByJobIdAndIsJoined(int jobId, boolean isJoined);

    long countByJobIdAndIsOfferRejected(int jobId, boolean isOfferRejected);

	long countByJobIdAndStatus(int id, String statusInterviewScheduled);

	long countByJobIdAndIsNotAttended(int id, boolean b);

	long countByJobIdAndIsNotSelected(int id, boolean b);

	Page<CanInterviewsModel> findByJobIdAndAttendedOnBetween(Integer jobId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Pageable pageable);

}
