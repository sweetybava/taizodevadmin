package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgUGCourseModel;
import com.taizo.model.DiplomaCourseModel;

@Repository
public interface CfgUGCourseRepository extends JpaRepository<CfgUGCourseModel, Long> {
	
    @Query("select a from CfgUGCourseModel a where jsActive = :b")
	List<CfgUGCourseModel> findByJSActive(boolean b);
    
    @Query("select a from CfgUGCourseModel a where empActive = :b")
	List<CfgUGCourseModel> findByEmpActive(boolean b);

}
