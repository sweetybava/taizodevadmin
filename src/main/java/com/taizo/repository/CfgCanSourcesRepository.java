package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.taizo.model.CfgAreasModel;
import com.taizo.model.CfgCanSources;
import com.taizo.model.CityModel;
import com.taizo.model.CountryCitiesModel;

public interface CfgCanSourcesRepository extends JpaRepository<CfgCanSources,Long>{
	
    @Query("select a from CfgCanSources a where active = :b")
	List<CfgCanSources> findByActive(boolean b);


}
