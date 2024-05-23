package com.taizo.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.Admin;
import com.taizo.model.AdminCallNotiModel;
import com.taizo.model.EmployerActivityModel;
import com.taizo.model.EmployerModel;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>, JpaSpecificationExecutor<Admin> {

	Optional<Admin> findByUserName(String userName);

	@Query(value = "SELECT u FROM Admin u where u.userName = ?1 and u.password = ?2 ")
	Optional<Admin> login(String userName, String password);

	@Query(value = "{call AdminDashboardCount()}", nativeQuery = true)
	Map<String, Object> findCount();

	@Query(value = "SELECT * FROM emp_recent_activities u where Date(u.created_time) between ?1 and ?2 order by u.created_time desc", nativeQuery = true)
	Map<String, Object> findDashboardCount(String startDate, String endDate);

	@Query(value = "SELECT u FROM Admin u where u.emailId = ?1 and u.password = ?2 ")
	Optional<Admin> adminlogin(String emailId, String password);

	Admin findByEmailId(String emailId);

	Optional<Admin> findById(String adminId);
	
	 Admin findByEmailIdAndPassword(String email, String password);
	 
	 @Query(value = "{ CALL TaizoDB.adminDashboardEmployerAnalytics(:time) }", nativeQuery = true)
	 List<Map<String, Object>> findByEmpAnalytics(@Param("time") String time);
	 
	 @Query(value = "{ CALL TaizoDB.adminDashboardJobSeekerAnalytics(:time) }", nativeQuery = true)
	 List<Map<String, Object>> findByJSAnalytics(@Param("time") String time);
	 
	 @Query(value = "{ CALL TaizoDB.adminDashboardJobsAnalytics(:time) }", nativeQuery = true)
	 List<Map<String, Object>> findByJobsAnalytics(@Param("time") String time);

	Admin findByMobileNo(String mobileNo);

	Admin findByEmailIdAndIsAvailable(String emailId, boolean b);

	List<Admin> findByModule(String string);
	
	  @Query("SELECT a.mobileNo FROM Admin a WHERE a.id = :adminId")
	    String findMobileNumberByAdminId(Long adminId);
	  
	  Optional<Admin> findById(Long adminId);


	
 


}
