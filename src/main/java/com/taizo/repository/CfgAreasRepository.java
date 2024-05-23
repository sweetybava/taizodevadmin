package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.taizo.model.CfgAreasModel;
import com.taizo.model.CityModel;
import com.taizo.model.CountryCitiesModel;

public interface CfgAreasRepository extends JpaRepository<CfgAreasModel,Long>{

	Optional<CfgAreasModel> findById(int area);
	
    @Query("select a from CfgAreasModel a where cityId = :cityId and active = :b")
	List<CfgAreasModel> findByCityIdandActvie(int cityId,boolean b);


}
