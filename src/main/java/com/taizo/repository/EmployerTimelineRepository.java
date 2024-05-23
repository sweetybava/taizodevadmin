package com.taizo.repository;

import com.taizo.model.CandidateTimeLine;
import com.taizo.model.EmployerTimeline;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmployerTimelineRepository extends JpaRepository<EmployerTimeline,Long> {
	
	 @Query("SELECT c FROM EmployerTimeline c " +
	           "WHERE (:eventName IS NULL OR c.eventName = :eventName) " +
	           "AND (:empId IS NULL OR c.empId = :empId) " +
			   "AND (:empLeadId IS NULL OR c.empLeadId = :empLeadId) " +
	           "AND (:startDate IS NULL OR c.createdTime >= :startDate) " +
	           "AND (:endDate IS NULL OR c.createdTime <= :endDate) " +
	           "ORDER BY c.createdTime DESC")
	 Page<EmployerTimeline> findEmpByFilters(String eventName, Integer empId, Integer empLeadId, Date startDate, Date endDate, Pageable pageable);


    
}
