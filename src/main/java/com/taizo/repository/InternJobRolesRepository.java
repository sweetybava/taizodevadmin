package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.InternJobRolesModel;

@Repository
public interface InternJobRolesRepository extends JpaRepository<InternJobRolesModel, Long>{

	List<InternJobRolesModel> findByIndustryId(int industryId);

}