package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.taizo.model.CityModel;
import com.taizo.model.KeySkillsModel;

public interface KeySkillsRepository extends JpaRepository<KeySkillsModel,Long>{

	@Query(value = "SELECT u FROM KeySkillsModel u order by empOrderNo desc")
	List<KeySkillsModel> findEmpKeyskills();
	
	@Query(value = "SELECT u FROM KeySkillsModel u order by jsOrderNo desc")
	List<KeySkillsModel> findJsKeyskills();
	
	Optional<KeySkillsModel> findBySkill(String skill);


}
