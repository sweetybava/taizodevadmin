package com.taizo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.CandidateTimeLine;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CandidateTimeLineRepository extends JpaRepository<CandidateTimeLine, Long> {

	@Query("SELECT c FROM CandidateTimeLine c " +
		       "WHERE (:eventName IS NULL OR c.eventName = :eventName) " +
		       "AND (:canId IS NULL OR c.canId = :canId) " +
		       "AND (:canLeadId IS NULL OR c.canLeadId = :canLeadId) " +
		       "AND (:facebookId IS NULL OR c.facebookId = :facebookId) " +
		       "AND (:seniorCanId IS NULL OR c.seniorCanId = :seniorCanId) " +
		       "AND (:seniorCanLeadId IS NULL OR c.seniorCanLeadId = :seniorCanLeadId) " + // Corrected property name
		       "AND (:startDate IS NULL OR c.createdTime >= :startDate) " +
		       "AND (:endDate IS NULL OR c.createdTime <= :endDate) " +
		       "ORDER BY c.createdTime DESC")
		Page<CandidateTimeLine> findByFilters(String eventName, Integer canId, Integer canLeadId, Long facebookId,
		                                      Optional<Long> seniorCanId, Optional<Long> seniorCanLeadId, // Corrected property name
		                                      Date startDate, Date endDate, Pageable pageable);




	 List<CandidateTimeLine> findByCanLeadId(int id);

	List<CandidateTimeLine> findByFacebookId(Long id);


	List<CandidateTimeLine> findBySeniorCanId(Long id);

	List<CandidateTimeLine> findBySeniorCanLeadId(long id);
}
