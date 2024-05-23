package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgCanDocuments;
@Repository
public interface CfgCanDocumentsRepository extends JpaRepository<CfgCanDocuments, Long> {

	CfgCanDocuments findByDocTitleAndActive(String docTitle, boolean active);

}
