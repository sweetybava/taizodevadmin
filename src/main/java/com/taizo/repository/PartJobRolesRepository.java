package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.PartJobRolesModel;

@Repository
public interface PartJobRolesRepository extends JpaRepository<PartJobRolesModel, Long> {

	List<PartJobRolesModel> findByIndustryId(int industryId);

}
