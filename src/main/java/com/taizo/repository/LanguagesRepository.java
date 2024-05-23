package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.LanguagesModel;

@Repository
public interface LanguagesRepository extends JpaRepository<LanguagesModel, Long> {

}
