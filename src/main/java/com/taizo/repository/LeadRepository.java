package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CandidateModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.LeadModel;

@Repository
public interface LeadRepository extends JpaRepository<LeadModel, Integer> {

	@Query("select c from LeadModel c")
	List<LeadModel> findTopByMobileNumber(@Param("mobileNumber") long mobileNumber);

	@Query(value = "SELECT * FROM TaizoDB.emp_lead where mobile_number= :mobileNumber limit 1", nativeQuery = true)
	Optional<LeadModel> findByMobileNumber(@Param("mobileNumber") long mobileNumber);
	
	@Query(value = "SELECT * FROM TaizoDB.emp_lead where mobile_number= :mobileNumber", nativeQuery = true)
	List<LeadModel> findByMobileNumberList(@Param("mobileNumber") long mobileNumber);
	
	@Query(value = "SELECT * FROM TaizoDB.emp_lead where email_id= :emailId", nativeQuery = true)
	List<LeadModel> findByEmailId(@Param("emailId") String emailId);

	@Query("SELECT e FROM LeadModel e WHERE  e.emailId = :emailId AND e.fromAdmin = :fromAdmin ORDER BY e.createdTime DESC")
	Page<LeadModel> findByFromAdmin(@Param("emailId") String emailId,Boolean fromAdmin,Pageable pageable );

	Page<LeadModel> findAll(Pageable pageable);

	LeadModel findByMobileNumberOrEmailId(long mobileNumber, String searchValue);

	void deleteLeadByIdAndMobileNumber(int id, long mobileNumber);



}
