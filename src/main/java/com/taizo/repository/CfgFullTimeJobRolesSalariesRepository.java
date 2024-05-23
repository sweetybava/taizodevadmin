package com.taizo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgFullTimeJobRoleSalaries;
@Repository
public interface CfgFullTimeJobRolesSalariesRepository extends JpaRepository<CfgFullTimeJobRoleSalaries, Long> {

    CfgFullTimeJobRoleSalaries findByJobRolesAndYearsOfExperience(String jobRole, String s);
}
