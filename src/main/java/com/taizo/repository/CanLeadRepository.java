package com.taizo.repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CanLeadModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.FacebookMetaLead;
import com.taizo.model.LeadModel;

@Repository
public interface CanLeadRepository extends JpaRepository<CanLeadModel, Long> {

	CanLeadModel findByMobileNumber(long mobileNumber);
	
	@Query(value = "SELECT u FROM CanLeadModel u where u.mobileNumber = ?1")
	Optional<CanLeadModel> findByMobNumber(long mobileNumber);

	void deleteByMobileNumber(long mobileNumber);
	
	@Query(value = "SELECT * FROM TaizoDB.can_lead where mobile_number= :mobileNumber", nativeQuery = true)
	List<CanLeadModel> findByMobileNumberList(@Param("mobileNumber") long mobileNumber);

	void deleteById(Long id);

	Optional<CanLeadModel> findById(int id);
	
	Page<CanLeadModel> findByProfilePageNoOrderByProfilePageNoDesc(int profilePageNo, Pageable pageable);
	 
	Page<CanLeadModel> findAll(Pageable pageable);

	Page<CanLeadModel> findByProfilePageNo(Integer profilePageNo, Pageable pageable);

	Page<CanLeadModel> findByProfilePageNoAndMobileNumber(Pageable pageable);

	Page<CanLeadModel> findByMobileNumber(Long mobileNumber, Pageable pageable);

	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CanLeadModel c WHERE c.mobileNumber = :number OR c.contactNumber = :number OR c.whatsappNumber = :number")
	boolean existsInCanLead(@Param("number") long number);
     
	 @Query("SELECT e FROM CanLeadModel e " +
	           "WHERE (e.fromAdmin = true AND ?1 = 'fromAdmin') OR " +
	           "(e.fromWA = true AND ?1 = 'fromWA') OR " +
	           "(e.fromApp = true AND ?1 = 'fromApp')OR" +
	           "(e.fromFbMetaLeadAd = true AND ?1 = 'fromFbMetaLeadAd')")
	Page<CanLeadModel> findByFromSource(String fromSource, Pageable pageable);

	Page<CanLeadModel> findByjobCategory(String jobCategory, Pageable pageable);

	Page<CanLeadModel> findByExpYearsBetween(Integer expYearsMin, Integer expYearsMax, Pageable pageable);

	Page<CanLeadModel> findByCreatedTimeBetween(Date createdTimeStart, Date createdTimeEnd,
			Pageable pageable);

	Page<CanLeadModel> findByQualified(boolean b, Pageable pageable);

	Page<CanLeadModel> findByNotQualified(boolean b, Pageable pageable);

	Page<CanLeadModel> findByQualifiedAndNotQualified(boolean b, boolean c, Pageable pageable);

	CanLeadModel findByWhatsappNumber(long mn);

	CanLeadModel findByContactNumber(String mobileNumber);

	Page<CanLeadModel> findAll(Specification<CanLeadModel> spec, Pageable pageable);

	Optional<CanLeadModel> findByMobileNumber(String mobileNumber);

	void save(CandidateModel candidateModel);

	void deleteById(int id);

	Optional<CanLeadModel> findFirstByCityOrderByCreatedTimeDesc(String preferredLocation);

	
}