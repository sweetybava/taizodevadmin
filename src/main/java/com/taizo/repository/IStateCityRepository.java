package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.taizo.model.CfgStateCityModel;
import com.taizo.model.CfgStateModel;
import com.taizo.model.StateCityModel;

public interface IStateCityRepository extends JpaRepository<CfgStateCityModel,Long>{

	@Query("select e from CfgStateCityModel e  where e.stateId=?1 and e.active = ?2 order by jsOrderNo desc")
	List<CfgStateCityModel> findByStateId(int stateId,boolean active);

	Optional<CfgStateCityModel> findById(int parseInt);
	

	

}
