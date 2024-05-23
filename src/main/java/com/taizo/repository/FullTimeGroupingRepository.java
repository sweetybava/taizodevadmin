package com.taizo.repository;


import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgFullTimeGroup;
import com.taizo.model.JobsModel;

@Repository
public interface FullTimeGroupingRepository extends JpaRepository<CfgFullTimeGroup, Long>, 
	JpaSpecificationExecutor<CfgFullTimeGroup> {

	@Query("select j from CfgFullTimeGroup j where j.groupName IN :jobCategory and j.active = true")
	ArrayList<CfgFullTimeGroup> findByJobCategory(@Param("jobCategory") List<String> jobCategory);
	
	@Query("select j from CfgFullTimeGroup j where j.groupName IN :jobCategory and j.active = true")
	List<CfgFullTimeGroup> findByJobRole(@Param("jobCategory") String jobCategory);
	
	@Query("select j from CfgFullTimeGroup j where j.groupId IN :groupId and j.active = true")
	ArrayList<CfgFullTimeGroup> findByGroupId( @Param("groupId")List<Integer> groupID);
	
	@Query("select j from CfgFullTimeGroup j where j.groupName = :jobCategory and j.industryId = :industryId and j.active = true")
	ArrayList<CfgFullTimeGroup> findByCategoryAndIndustry(@Param("jobCategory") String jobCategory,
			                                              @Param("industryId") int industryId);

	@Query("select j from CfgFullTimeGroup j where j.groupId = :groupId and j.id!=:id and j.active = true")
	ArrayList<CfgFullTimeGroup> findByGroupId(@Param("groupId") int groupId,@Param("id") Long id);

}
