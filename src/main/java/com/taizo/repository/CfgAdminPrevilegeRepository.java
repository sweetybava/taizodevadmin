package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.CfgAdminPrevilegeModel;

public interface CfgAdminPrevilegeRepository extends JpaRepository<CfgAdminPrevilegeModel, Long> {

	CfgAdminPrevilegeModel findByPrivilegeName(String privilegeName);

}
 