package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.CalendlyMettingModel;
@Repository
public interface CalendlyMettingRepository extends JpaRepository<CalendlyMettingModel, Long> {
	
	List<CalendlyMettingModel> findByUserEmailOrderByCreatedTimeDesc(String userEmail);

}
