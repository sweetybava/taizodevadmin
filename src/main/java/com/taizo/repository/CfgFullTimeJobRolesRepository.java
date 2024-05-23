package com.taizo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.taizo.model.CfgFullTimeJobRoles;

public interface CfgFullTimeJobRolesRepository  extends JpaRepository<CfgFullTimeJobRoles, Long>{

    CfgFullTimeJobRoles findByJobRolesIgnoreCase(String jobCategory);

    Page<CfgFullTimeJobRoles> findByActiveTrue(Pageable pageable);
}
