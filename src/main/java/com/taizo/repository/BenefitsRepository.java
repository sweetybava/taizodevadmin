package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.BenefitsModel;

public interface BenefitsRepository extends JpaRepository<BenefitsModel,Long>{

}
