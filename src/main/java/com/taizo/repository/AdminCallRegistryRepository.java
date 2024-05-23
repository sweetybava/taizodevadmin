package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.AdminCallRegistryModel;

public interface AdminCallRegistryRepository extends JpaRepository<AdminCallRegistryModel, Long> {

}
