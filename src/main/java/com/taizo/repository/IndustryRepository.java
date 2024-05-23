package com.taizo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.taizo.model.IndustryModel;

public interface IndustryRepository extends JpaRepository<IndustryModel,Integer>{

    @Query("select id from IndustryModel where industry = :industry")
    int findByIndustry(String industry) ;

	Optional<IndustryModel> findById(int industry);





}
