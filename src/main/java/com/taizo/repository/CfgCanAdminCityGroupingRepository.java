package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.CfgCanAdminCityGrouping;

public interface CfgCanAdminCityGroupingRepository extends JpaRepository<CfgCanAdminCityGrouping, Long> {

	CfgCanAdminCityGrouping findFirstByCityNameAndAdminIdGreaterThanOrderByAdminIdAsc(String preferredLocation,
			int prevAdminId);

	CfgCanAdminCityGrouping findFirstByCityNameOrderByAdminIdAsc(String preferredLocation);

	List<CfgCanAdminCityGrouping> findAllByCityNameOrderByAdminIdAsc(String preferredLocation);

	CfgCanAdminCityGrouping findFirstByCityNameAndAdminIdGreaterThanAndActiveOrderByAdminIdAsc(String preferredLocation,
			int prevAdminId, boolean b);

	CfgCanAdminCityGrouping findFirstByCityNameAndActiveOrderByAdminIdAsc(String preferredLocation, boolean b);


	List<CfgCanAdminCityGrouping> findByAdminId(int id);
}
