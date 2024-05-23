package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.taizo.model.JobRolesModel;

@Repository
public interface JobRolesRepository extends JpaRepository<JobRolesModel, Long>{

	@Query(value = "SELECT u FROM JobRolesModel u where u.industryId = ?1 and u.active = true order by u.jsOrderNo desc")
	List<JobRolesModel> findByIndustryId(int industryId);
	
	@Query(value = "SELECT u FROM JobRolesModel u where u.industryId = ?1 and u.active = true order by u.empOrderNo desc")
	List<JobRolesModel> findByEmpIndustryId(int industryId);

	@Query(value = "SELECT u FROM JobRolesModel u where u.id = ?1 and u.active = true")
	Optional<JobRolesModel> findById(int jobRole);

	@Query(value = "SELECT u FROM JobRolesModel u where u.active = ?1")
	List<JobRolesModel> findAll(boolean b);

	@Query(value = "SELECT u FROM JobRolesModel u where u.industryId = ?1 and u.jobRoles = ?2")
	Optional<JobRolesModel> findByIndustryIdandJobRole(int industryId, String jobrole);

}
