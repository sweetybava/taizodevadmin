package com.taizo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.taizo.model.InterviewAddressesModel;
import com.taizo.model.InterviewsModel;

public interface InterviewAddressRepository extends JpaRepository<InterviewAddressesModel, Integer>{
	@Query("select i from InterviewAddressesModel i where i.id = :id")
	Optional<InterviewAddressesModel> findById(int id);

}
