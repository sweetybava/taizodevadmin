package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgAllCourseModel;
import com.taizo.model.CfgPGCourseModel;
import com.taizo.model.CfgUGCourseModel;
import com.taizo.model.DiplomaCourseModel;

@Repository
public interface CfgAllCoursesRepository extends JpaRepository<CfgAllCourseModel, Long> {
    
    @Query("select a from CfgAllCourseModel a where empActive = :b")
	List<CfgAllCourseModel> findByEmpActive(boolean b);
}
