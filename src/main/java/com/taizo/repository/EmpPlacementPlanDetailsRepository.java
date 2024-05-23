package com.taizo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.taizo.model.EmpPlacementPlanDetailsModel;
import com.taizo.model.PlansModel;

public interface EmpPlacementPlanDetailsRepository extends JpaRepository<EmpPlacementPlanDetailsModel,Long> {

	List<EmpPlacementPlanDetailsModel> findByEmployerIdAndActive(int employerId, boolean isActive);

	List<EmpPlacementPlanDetailsModel> findByActive( boolean isActive);
	
	@Query("SELECT e FROM EmpPlacementPlanDetailsModel e WHERE e.active = true AND e.employerId = :employerId ORDER BY e.createdTime DESC")
	Page<EmpPlacementPlanDetailsModel> findByEmployerId(
	    @Param("employerId") Integer employerId, 
	    Pageable pageable
	);
	@Query("SELECT e FROM EmpPlacementPlanDetailsModel e WHERE e.active = true ORDER BY e.createdTime DESC")
	Page<EmpPlacementPlanDetailsModel> findAllActive(Pageable pageable);

	
}
