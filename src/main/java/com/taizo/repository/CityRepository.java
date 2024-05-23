package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.taizo.model.CityModel;

public interface CityRepository extends JpaRepository<CityModel,Long>{

	Optional<CityModel> findById(int city);

	@Query(value = "SELECT u FROM CityModel u where u.active = true order by jsOrderNo desc")
	List<CityModel> findAllByActive();
	
	@Query(value = "SELECT u FROM CityModel u where u.active = true order by empOrderNo desc")
	List<CityModel> findAllByEmpActive();

	Optional<CityModel> findByCity(String city);
	
}
