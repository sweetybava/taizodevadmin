package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.taizo.model.StateCityModel;

public interface StateCityRepository extends JpaRepository<StateCityModel,Long>{

	@Query("select e from StateCityModel e  where e.stateId=?1 order by jsOrderNo desc")
	List<StateCityModel> findByJSStateId(int stateId);
	
}
