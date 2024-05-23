package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgEducationalModel;
@Repository
public interface CfgEducationalRepository extends JpaRepository<CfgEducationalModel, Long> {

}
