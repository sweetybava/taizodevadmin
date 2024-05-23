package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgAreasModel;
import com.taizo.model.DiplomaCourseModel;

@Repository
public interface DiplomaCourseRepository extends JpaRepository<DiplomaCourseModel, Long> {

    @Query("select a from DiplomaCourseModel a where jsActive = :b")
	List<DiplomaCourseModel> findByJSActive(boolean b);

    @Query("select a from DiplomaCourseModel a where empActive = :b")
	List<DiplomaCourseModel> findByEmpActive(boolean b);

}
