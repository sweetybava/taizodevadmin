package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgPGCourseModel;
import com.taizo.model.CfgUGCourseModel;
import com.taizo.model.DiplomaCourseModel;

@Repository
public interface CfgPGCourseRepository extends JpaRepository<CfgPGCourseModel, Long> {

    @Query("select a from CfgPGCourseModel a where jsActive = :b")
	List<CfgPGCourseModel> findByJSActive(boolean b);
    
    @Query("select a from CfgPGCourseModel a where empActive = :b")
	List<CfgPGCourseModel> findByEmpActive(boolean b);
}
