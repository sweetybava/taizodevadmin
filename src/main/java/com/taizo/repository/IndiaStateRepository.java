package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.taizo.model.IndiaStateModel;

public interface IndiaStateRepository extends JpaRepository<IndiaStateModel,Long>{

	IndiaStateModel findById(int state);

	@Query("select c from IndiaStateModel c where c.specification = :specification")
	List<IndiaStateModel> findBySpecification(@Param("specification") String specification);

	@Query("select e from IndiaStateModel e order by jsOrderNo desc")
	List<IndiaStateModel> findByJSOrder();
	
}
