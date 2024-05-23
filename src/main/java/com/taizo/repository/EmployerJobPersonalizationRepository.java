package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CandidateModel;
import com.taizo.model.EmployerJobPersonalizationModel;

@Repository
public interface EmployerJobPersonalizationRepository extends JpaRepository<EmployerJobPersonalizationModel, Long> {
	
	@Query("select c from EmployerJobPersonalizationModel c where c.active = :active")
	List<EmployerJobPersonalizationModel> findAllByActive(@Param("active") boolean active);

}
