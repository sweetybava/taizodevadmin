package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.taizo.model.CanLanguageModel;

@Repository
public interface CanLanguagesRepository extends JpaRepository<CanLanguageModel, Long>{

	List<CanLanguageModel> findByCandidateId(int candidate);
	
	@Transactional
	@Modifying
	@Query("DELETE from CanLanguageModel c WHERE c.id IN :id")
	void delete(@Param("id") List<Integer> list);

	List<CanLanguageModel> findById(int id);

	

	 
}
