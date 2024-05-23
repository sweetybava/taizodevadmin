package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CanLanguageModel;
import com.taizo.model.CfgLanguagesModel;
import com.taizo.model.EmployerJobPersonalizationModel;


@Repository
public interface CfgLanguagesRepository extends JpaRepository<CfgLanguagesModel, Long> {

	CfgLanguagesModel findById(int id);

	@Query("select c from CfgLanguagesModel c where c.active = :active")
	List<CfgLanguagesModel> findAllByActive(@Param("active") boolean active);

	static List<CfgLanguagesModel> findById(List<CanLanguageModel> details) {
		// TODO Auto-generated method stub
		return null;
	}

	


}
