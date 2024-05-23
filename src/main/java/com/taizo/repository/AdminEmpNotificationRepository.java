package com.taizo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.AdminEmpNotificationModel;


@Repository
public interface AdminEmpNotificationRepository extends JpaRepository<AdminEmpNotificationModel, Long> {
	

}
