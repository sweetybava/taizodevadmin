package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.CfgEmployerDocumentsModel;

public interface CfgEmployerDocumentsRepository extends JpaRepository<CfgEmployerDocumentsModel , Long> {

	 List<CfgEmployerDocumentsModel> findByActiveTrue();

	 Optional<CfgEmployerDocumentsModel> findByDocTitle(String docTitle);



}
