package com.taizo.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.xmlbeans.impl.jam.JParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.DTO.CandidateAnalyticsFilterDTO;
import com.taizo.model.CandidateAnalyticsModel;

@Repository
public interface CandidateAnalyticsRepositroy extends JpaRepository<CandidateAnalyticsModel, Long> {

	List<CandidateAnalyticsModel> findByAdminId(Long adminId);
	
	@Query("SELECT " +
	        "SUM(c.fbMetaLeads),SUM(c.qualifiedFbMetaLeads),SUM(c.canLeads),SUM(c.qualifiedCanLeads), " +
	        "SUM(c.totalLeads),SUM(c.totalQualifiedLeads),SUM(c.canRegistration),SUM(c.canRegistration), " +  // Fix the duplicate field
	        "SUM(c.interviewScheduled),SUM(c.interviewAttended),SUM(c.companySelected),SUM(c.offerAccepted),SUM(c.joined) " +
	        "FROM CandidateAnalyticsModel c " +
	        "WHERE c.adminId = :adminId AND c.createdTime BETWEEN :startDate AND :endDate " +
	        "GROUP BY c.adminId")
	List<Object[]> sumValuesByAdminIdAndDateRange(@Param("adminId") Long adminId,
	                                               @Param("startDate") Date startDate,
	                                               @Param("endDate") Date endDate);

    
    @Query("SELECT c.adminId, SUM(c.fbMetaLeads), SUM(c.qualifiedFbMetaLeads), SUM(c.canLeads), " +
            "SUM(c.qualifiedCanLeads), SUM(c.totalLeads), SUM(c.totalQualifiedLeads), " +
            "SUM(c.canRegistration), SUM(c.interviewScheduled), SUM(c.interviewAttended)," +
            "SUM(c.companySelected),SUM(c.offerAccepted),SUM(c.joined)" +
            "FROM CandidateAnalyticsModel c " +
            "WHERE c.createdTime BETWEEN :startDate AND :endDate " +
            "GROUP BY c.adminId")
    List<Object[]> sumValuesByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);



    @Query("SELECT DISTINCT c.adminId FROM CandidateAnalyticsModel c")
    List<Long> findAllAdminIds();

	
}
