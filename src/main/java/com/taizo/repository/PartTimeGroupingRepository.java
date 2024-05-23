package com.taizo.repository;


import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgFullTimeGroup;
import com.taizo.model.CfgPartTimeGroup;

@Repository
public interface PartTimeGroupingRepository extends JpaRepository<CfgPartTimeGroup, Long>, 
	JpaSpecificationExecutor<CfgPartTimeGroup> {
	
	@Query("select j from CfgPartTimeGroup j where j.groupName IN :jobCategory")
	ArrayList<CfgPartTimeGroup> findByJobCategory(@Param("jobCategory") List<String> jobCategory);
	
	@Query("select j from CfgPartTimeGroup j where j.groupId IN :groupId")
	ArrayList<CfgPartTimeGroup> findByGroupId( @Param("groupId")List<Integer> groupID);

}
