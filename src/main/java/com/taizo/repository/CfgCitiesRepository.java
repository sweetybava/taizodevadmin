package com.taizo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgCities;
import com.taizo.model.LeadModel;
@Repository
public interface CfgCitiesRepository extends JpaRepository<CfgCities, Long> {

}
