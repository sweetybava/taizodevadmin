package com.taizo.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.taizo.model.LeadModel;

public interface EmployerLeadRepository extends JpaRepository<LeadModel, Integer> {
    
	@Query("SELECT e FROM LeadModel e WHERE  e.mobileNumber = :mobileNumber AND e.fromAdmin = :fromAdmin ORDER BY e.createdTime DESC")
	Page<LeadModel> findBymobileNumber(@Param("mobileNumber")Long mobileNumber, boolean fromAdmin, Pageable pageable);
    
	@Query("SELECT e FROM LeadModel e WHERE  e.companyName = :companyName AND e.fromAdmin = :fromAdmin ORDER BY e.createdTime DESC")
	Page<LeadModel> findByCompanyName(String companyName, boolean fromAdmin, Pageable pageable);

	@Query("SELECT e FROM LeadModel e WHERE  e.industry = :industry AND e.fromAdmin = :fromAdmin ORDER BY e.createdTime DESC")
	Page<LeadModel> findByIndustry(String industry, boolean fromAdmin, Pageable pageable);

	@Query("SELECT e FROM LeadModel e WHERE  e.city = :city AND e.fromAdmin = :fromAdmin ORDER BY e.createdTime DESC")
	Page<LeadModel> findByCity(String city, boolean fromAdmin, Pageable pageable);

	@Query("SELECT e FROM LeadModel e WHERE e.createdTime BETWEEN :startDate AND :endDate AND e.fromAdmin = :fromAdmin ORDER BY e.createdTime DESC")
	Page<LeadModel> findByCreatedTime(
	         Date startDate,
	         Date endDate,
	         boolean fromAdmin,
	        Pageable pageable);

	Page<LeadModel> findAll(Specification<LeadModel> spec, Pageable pageable);

}
