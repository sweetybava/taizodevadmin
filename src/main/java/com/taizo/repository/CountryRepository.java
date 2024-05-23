package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.taizo.model.CountryModel;

public interface CountryRepository extends JpaRepository<CountryModel,Long>{

	CountryModel findById(int country);

	@Query("select c from CountryModel c where c.specification = :specification")
	List<CountryModel> findBySpecification(@Param("specification") String specification);
}
