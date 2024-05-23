package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.FreeJobRolesModel;

@Repository
public interface FreeJobRolesRepository extends JpaRepository<FreeJobRolesModel, Long>{

}