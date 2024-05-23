package com.taizo.repository;

import com.taizo.model.AdminJobRolesMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminJobRolesMappingRepository extends JpaRepository<AdminJobRolesMapping,Long> {

    List<AdminJobRolesMapping> findByGroupId(Integer groupId);
}
