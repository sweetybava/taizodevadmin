package com.taizo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.MidSeniorSourcingModel;

import java.util.Optional;

public interface MidSeniorSourcingRepository extends JpaRepository<MidSeniorSourcingModel, Long> {

    Optional<MidSeniorSourcingModel> findByMobileNumber(String mobileNumber);

	Page<MidSeniorSourcingModel> findAll(Specification<MidSeniorSourcingModel> spec, Pageable pageable);

    void deleteById(int id);
}
