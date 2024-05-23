package com.taizo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgCanAdminArea;
@Repository
public interface CfgCanAdminAreaRepository extends JpaRepository<CfgCanAdminArea, Long> {

	CfgCanAdminArea findByAreas(String area);

	List<CfgCanAdminArea> findByAssingnedToAdminId(int assingnedToAdminId);

	List<CfgCanAdminArea> findByCityIdAndActive(int cityId, boolean b);
	

}
