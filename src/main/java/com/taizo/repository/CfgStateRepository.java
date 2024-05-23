package com.taizo.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.taizo.model.CfgStateModel;

public interface CfgStateRepository extends JpaRepository<CfgStateModel,Long>{

	List<CfgStateModel> findByCountryId(int countryId);
	
	@Query("select e from CfgStateModel e  where e.countryId=?1 and e.active = ?2 order by jsOrderNo desc")
	List<CfgStateModel> findByCountryIdAndStatus(int countryId,boolean active);

	Optional<CfgStateModel> findById(int state);
	

    @Query(value = "SELECT count(c.job_city)as count, j.city FROM jobs c" + 
    		"  left join  cfg_cities j on j.city COLLATE utf8mb4_0900_ai_ci = c.job_city where " + 
    		"  c.job_city COLLATE utf8mb4_0900_ai_ci = j.city group by c.job_city order by count desc ;", nativeQuery = true)
    List<Map<String, Object>> getData();



	

	


}
