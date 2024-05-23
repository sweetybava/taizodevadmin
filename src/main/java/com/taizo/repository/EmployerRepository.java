package com.taizo.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import com.taizo.model.EmployerActivityModel;
import com.taizo.model.EmployerCallModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.EmployerPaymentModel;
import com.taizo.model.PlansModel;

@Repository
public interface EmployerRepository extends JpaRepository<EmployerModel, Long>, JpaSpecificationExecutor<EmployerModel> {

    @Query("select e from EmployerModel e where e.approvalStatus <> :Declinedstatus and e.approvalStatus <> :ApprovedStatus or e.approvalStatus is null")
    List<EmployerModel> findApprovedEmployers(@Param("Declinedstatus") String Declinedstatus, @Param("ApprovedStatus") String ApprovedStatus);

    Optional<EmployerModel> findById(int id);

    EmployerModel findByEmailId(String emailId);
    EmployerModel findByMobileNumber(long mobileNumber);
    EmployerModel findTopByMobileNumber(long mobileNumber);

    @Query(value = "SELECT e FROM EmployerModel e where e.emailId = ?1 and e.password = ?2 ")
    Optional<EmployerModel> login(String emailId, String password);

    @Query(value = "SELECT *" +
            " FROM employer e where e.kyc_status != 'V' and Date(e.created_time) between ?1 and ?2 order by e.created_time desc",nativeQuery = true)
    Page<EmployerModel> getKYCUnderReviewEmployer(String startDate, String endDate,Pageable pageable);

    Optional<EmployerModel> findByToken(String token);

    void deleteById(int id);

    @Query("select c from EmployerModel c where c.category IN :category and c.city = COALESCE(NULLIF(:city, ''), c.city)"
            + " and c.plan = COALESCE(NULLIF(:plan, ''), c.plan)")
    List<EmployerModel> findAllEmployer(@Param("category") List<String> category, @Param("city") String city,
                                        @Param("plan") int plan);

    @Query(value = "SELECT e.emailId,e.id FROM EmployerModel e where e.kycStatus = 'V'")
	List<EmployerModel> findByKycStatus();
    
    @Query(value = "SELECT * FROM TaizoDB.employer where free_plan_expiry_date = DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY) and plan=5" + 
    		" and deactivated is false and whatsapp_notification is true",nativeQuery = true)
    List<EmployerModel> findByFreePlanExpireOneDay();
    
    @Query(value = "SELECT * FROM TaizoDB.employer where free_plan_expiry_date = DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY) and plan=5" + 
    		" and deactivated is false and whatsapp_notification is true",nativeQuery = true)
	List<EmployerModel> findByFreePlanExpired();
    
    @Query(value = "SELECT * FROM TaizoDB.employer where plan_expiry_date = DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY) and plan!=5" + 
    		" and deactivated is false and whatsapp_notification is true",nativeQuery = true)
	List<EmployerModel> findByPlanExpireOneDay();
    
	@Query(value = "{call emp_dashboard_count(:employerId,:startDate,:endDate)}", nativeQuery = true)
	Map<String, Object> getEmpDashboardDetails(@Param("employerId")int employerId,@Param("startDate") String startDate,
			@Param("endDate") String endDate);
	
	@Query(value = "{call emp_invoice(:employerId,:startDate,:endDate)}", nativeQuery = true)
	List<Map<String, Object>> getEmpInvoiceDetails(@Param("employerId")int employerId,@Param("startDate") String startDate,
			@Param("endDate") String endDate);
	
    @Query(value = "SELECT e FROM EmployerModel e where e.emailId = ?1 and e.resetCode = ?2 ")
    Optional<EmployerModel> resetcheck(String emailId, String resetCode);
    
    
    @Query(value = "SELECT e.id, e.contactPersonName, e.companyName, e.mobileNumber, e.regProofNumber, e.kycStatus," +
    "e.id FROM EmployerModel e where e.regProofNumber is not null order by e.id desc")
	Page<EmployerModel> getKYCDetail(Pageable pageable);
    
    
    @Query(value = "SELECT e.id,e.contactPersonName, e.companyName,e.industry," + 
    "e.regProofNumber,e.kycStatus,e.emailId,e.mobileNumber,e.paymentStatus,e.plan," +
    		"e.expiryDate,e.createdTime FROM EmployerModel e order by e.id desc")
	Page<EmployerModel> findAllEmpDetails(Pageable pageable);
	
	@Query(value = "{ CALL TaizoDB.EmpCallAndAppliedDetails(:EmpID,:status) }", nativeQuery = true)
	List<Map<String, Object>> getEmpCallAndAppliedNotification(@Param("EmpID")int EmpID,@Param("status") String status);
	
	@Query(value = "{ CALL TaizoDB.EmpCallAndAppliedDetailsByDate(:EmpID,:status,:startDate,:endDate,:jobRole) }", nativeQuery = true)
	List<Map<String, Object>> getEmpCallAndAppliedNotificationByDate(@Param("EmpID")int EmpID,@Param("status") String status,
			@Param("startDate") String startdate,@Param("endDate") String endDate,@Param("jobRole") String jobRole);
	
	@Query(value = "{ CALL TaizoDB.EmpCallAndAppliedDetailsByJobId(:EmpID,:status,:jobId,:startDate,:endDate) }", nativeQuery = true)
	List<Map<String, Object>> getEmpCallAndAppliedNotificationByJobId(@Param("EmpID")int EmpID,@Param("status") String status,
			@Param("jobId")int jobId,@Param("startDate") String startdate,@Param("endDate") String endDate);
	
    EmployerModel findTopByregProofNumber(String regProofNumber);

	@Query(value = "{ CALL TaizoDB.whatsapp_alert() }", nativeQuery = true)
	List<Map<String, Object>> sendWhatsappAlert();
	
	@Query(value = "{ CALL TaizoDB.Admin_Filters_Company(:Industry,:No_of_employees,:Location,:Current_plan)}",nativeQuery = true)
	List<Map<String, Object>> filterEmployer(
			@Param("Industry")String industry,
			@Param("No_of_employees")String noOfEmployees,
			@Param("Location")String city, 
			@Param("Current_plan")int plan);

	 Optional<EmployerModel> findByIdOrMobileNumberOrWhatsappNumber(int id, long mobileNumber, long whatsappNumber);

	Page<EmployerModel> findAllByOrderByCreatedTimeDesc(Pageable pageable);

	Optional<EmployerModel> findByAssignTo(int assignTo);
	
	Page<EmployerModel> findByCompanyNameContainingIgnoreCase(String companyName, Pageable pageable);

	
	@Query(value = "{ CALL TaizoDB.emp_dashboard_analytics(:time) }", nativeQuery = true)
	List<Map<String, Object>> findByAnalystics(@Param("time") String time);

	@Query("SELECT a.mobileNumber FROM EmployerModel a WHERE a.id = :employerId")
	String findByMobileNumberById(int employerId);


    @Query("SELECT e FROM EmployerModel e WHERE e.companyName LIKE %:companyName%")
    List<EmployerModel> findByCompanyName(String companyName);

	

}
