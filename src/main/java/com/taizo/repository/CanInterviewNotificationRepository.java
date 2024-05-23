package com.taizo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CanInterviewNotificationModel;

@Repository
public interface CanInterviewNotificationRepository extends JpaRepository<CanInterviewNotificationModel, Long>, 
JpaSpecificationExecutor<CanInterviewNotificationModel> {

@Query("select e from CanInterviewNotificationModel e where e.empId = :empId")
List<CanInterviewNotificationModel> getAllEmpNotifications(@Param("empId") int empId);

}