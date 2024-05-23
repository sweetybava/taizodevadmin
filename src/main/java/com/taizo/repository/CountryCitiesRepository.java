package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.CountryCitiesModel;

public interface CountryCitiesRepository extends JpaRepository<CountryCitiesModel,Long>{

	List<CountryCitiesModel> findByCountryId(int countryId);

}
