package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgItiCourseModel;
import com.taizo.model.CfgUGCourseModel;
import com.taizo.model.DiplomaCourseModel;

@Repository
public interface CfgItiCourseRepository extends JpaRepository<CfgItiCourseModel, Long> {

    @Query("select a from CfgItiCourseModel a where jsActive = :b")
	List<CfgItiCourseModel> findByJSActive(boolean b);
    
    @Query("select a from CfgItiCourseModel a where empActive = :b")
	List<CfgItiCourseModel> findByEmpActive(boolean b);
}
