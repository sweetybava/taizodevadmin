package com.taizo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taizo.model.AdminNotificationModel;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotificationModel, Long> {
	

}
