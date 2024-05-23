package com.taizo.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CandidateCallModel;
import com.taizo.model.EmpInterviewNotificationModel;
import com.taizo.model.InterviewsModel;

@Repository
public interface EmpInterviewNotificationRepository extends JpaRepository<EmpInterviewNotificationModel, Long>, 
JpaSpecificationExecutor<EmpInterviewNotificationModel> {

@Query("select e from EmpInterviewNotificationModel e where e.canId = :canId")
List<EmpInterviewNotificationModel> getAllCanNotifications(@Param("canId") int candidateId);

@Query("select e from EmpInterviewNotificationModel e  where e.empId=?1 and e.dateTime > ?2")

Page<EmpInterviewNotificationModel> getEmployerInterviewNotificationCount(int eid, Date date, Pageable pageable);


}
