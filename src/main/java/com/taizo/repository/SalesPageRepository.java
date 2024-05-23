package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.SalesModel;
@Repository
public interface SalesPageRepository extends JpaRepository<SalesModel, Long> {


	List<SalesModel> findByAdminIdAndActive(int adminId, boolean active);
}
