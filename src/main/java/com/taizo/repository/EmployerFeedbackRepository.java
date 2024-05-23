package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.EmployerFeedbackModel;

@Repository
public interface EmployerFeedbackRepository extends JpaRepository<EmployerFeedbackModel, Long> {

}
