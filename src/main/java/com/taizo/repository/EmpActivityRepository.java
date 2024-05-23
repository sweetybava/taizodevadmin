package com.taizo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.taizo.model.CandidateCallsModel;
import com.taizo.model.CityModel;
import com.taizo.model.EmployerActivityModel;

public interface EmpActivityRepository extends JpaRepository<EmployerActivityModel,Long>{

    @Query(value = "SELECT * FROM emp_recent_activities u where u.emp_id=?1 and Date(u.created_time) between ?2 and ?3 order by u.created_time desc limit 30",nativeQuery = true)
    List<EmployerActivityModel> getEmpRecentActivity(Integer empId, String startDate, String endDate);
    
    @Query(value = "SELECT * FROM emp_recent_activities u where Date(u.created_time) between ?1 and ?2 order by u.created_time desc",nativeQuery = true)
    Page<EmployerActivityModel> getEmpAdminRecentActivity(String startDate, String endDate,Pageable pageable);

}
