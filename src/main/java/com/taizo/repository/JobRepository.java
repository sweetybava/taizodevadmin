package com.taizo.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.taizo.model.CandidateCallsModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.JobsModel;

@Repository
public interface JobRepository extends JpaRepository<JobsModel, Long>,JpaSpecificationExecutor<JobsModel>{
	


	@Query("select j from JobsModel j where j.jobCategory IN :jobCategory")
	ArrayList<JobsModel> findByCountryAndCategory(@Param("jobCategory") List<String> jobCategory);

	JobsModel getById(int id);

	@Query("select j from JobsModel j where j.id = :id")
	ArrayList<JobsModel> findByJobId(@Param("id") int jobId);

	Optional<JobsModel> findById(int id);

	void deleteById(int id);

	@Query("select j from JobsModel j where j.approvalStatus <> :Declinedstatus or j.approvalStatus is null and j.jobStatus = :status AND j.canResponseCount > 0")
	List<JobsModel> findApprovedCandidates(@Param("Declinedstatus") String Declinedstatus,@Param("status") String status);
	
	@Transactional
	@Modifying
	@Query("update JobsModel j set j.approvalStatus = :approvalStatus where j.id = :id")
	void findByApprovalStatus(@Param("id") int id,@Param("approvalStatus") String status);
	
	@Query("select j from JobsModel j where j.employerId = :employerId and j.jobStatus = :status and j.canResponseCount > 0 ORDER BY j.createdTime DESC")
	List<JobsModel> findEmployerJobs(@Param("employerId") int employerId,@Param("status") String status);

	//JobsModel findByIdAndEmployerId(int id, int employerId,String jobStatus);
	@Query("select j from JobsModel j where j.employerId = :employerId and j.jobStatus = :status and j.canResponseCount > 0 ORDER BY j.createdTime DESC")
	List<JobsModel> findEmployerDashboardJobs(@Param("employerId") int employerId,@Param("status") String status);
	
	@Query("select j from JobsModel j where  j.id = :id and j.employerId = :employerId and j.jobStatus = :status")
	JobsModel findByIdAndEmployerId(@Param("id") int id,@Param("employerId") int employerId,@Param("status") String status);

	@Query("select j from JobsModel j where j.employerId = :employerId and j.jobStatus = 'C' and j.inActive = false")
	List<JobsModel> findEmployerClosedJobs(@Param("employerId") int employerId);

	@Query("select j from JobsModel j where  j.id = :id and j.employerId = :employerId")
	JobsModel findByIdAndEmployer(@Param("id") int id,@Param("employerId") int employerId);

	@Query("select j from JobsModel j where j.jobType LIKE %:jobType% and j.jobStatus = :status and j.canResponseCount > 0 ORDER BY j.createdTime DESC")
	List<JobsModel> findByJobType(@Param("jobType") String jobType,@Param("status") String status);

	@Query(value = "{ CALL TaizoDB.chatMatchedJobsDetails(:status,:industryId,:jobrolesId,:city,:exp) }", nativeQuery = true)
	List<Map<String, Object>> findByMatchedJobs(@Param("status") String status,@Param("industryId")int industryId,@Param("jobrolesId") int jobrolesId,
			@Param("city") String city,@Param("exp") int exp);
	
	@Query(value = "{ CALL TaizoDB.chatNewMatchedJobsDetails(:status,:industry,:jobrole,:city,:exp,:jobids,:qualification) }", nativeQuery = true)
	List<Map<String, Object>> findByChatMatchedJobs(@Param("status") String status,@Param("industry")String industry,@Param("jobrole") String jobrole,
			@Param("city") String city,@Param("exp") int exp,@Param("jobids") String jobids,@Param("qualification") String qualification);
	
	@Query(value = "{ CALL TaizoDB.canAppMatchedJobsDetails(:status,:industry,:jobrole,:city,:exp,:jobids,:qualification) }", nativeQuery = true)
	List<Map<String, Object>> findByAppMatchedJobs(@Param("status") String status,@Param("industry")String industry,@Param("jobrole") String jobrole,
			@Param("city") String city,@Param("exp") int exp,@Param("jobids") String jobids,@Param("qualification") String qualification);
	
	
	@Query(value = "{call GetEmpInterestedProfileDetails(:empId,:jobId)}", nativeQuery = true)
	List<Map<String, Object>> findUsingId(@Param("empId")int empId,@Param("jobId")int jobId);


	void save(Optional<JobsModel> details);

	@Query("select j from JobsModel j where j.employerId = :employerId and j.inActive = false and j.jobStatus = :status")
	Page<JobsModel> findEmpJob(@Param("employerId") int employerId,@Param("status") String status,Pageable pageable);

	@Query("select j from JobsModel j where j.employerId = :employerId and j.inActive = false and j.deleted = false and j.jobStatus = :status")
	Page<JobsModel> findEmpDraftJob(@Param("employerId") int employerId,@Param("status") String status,Pageable pageable);
	
	@Query("select j from JobsModel j where j.employerId = :employerId and j.inActive = false and j.jobStatus = :status")
	Page<JobsModel> findEmpClosedJob(@Param("employerId") int employerId,@Param("status") String status,Pageable pageable);

	@Query("SELECT DISTINCT j FROM JobsModel j left join EmployerModel emp on j.employerId = emp.id" + 
			" where j.id Not IN (:jobIDs) and j.inActive = false  AND j.jobExp = 0 AND j.jobStatus = 'O' AND j.canResponseCount > 0 ORDER BY j.adminTopPriority desc, FIELD(emp.category, 'Staffing Services') desc, j.jobPostedTime desc")
	Page<JobsModel> findCanAllMatchedJobs(List<Integer> jobIDs,Pageable pageable);
	
	@Query("SELECT DISTINCT j FROM JobsModel j left join EmployerModel emp on j.employerId = emp.id" + 
			" where j.id!=1140 and j.jobStatus = 'O' and j.inActive = false AND j.id Not IN (:jobIDs) AND j.jobExp = 0 AND j.jobLocation IN (:cityList) AND j.canResponseCount > 0 ORDER BY j.adminTopPriority desc, FIELD(emp.category, 'Staffing Services') desc, j.jobPostedTime desc")
	Page<JobsModel> findCanAllFilteredJobs(List<Integer> jobIDs, List<String> cityList,Pageable pageable);
	
	@Query("SELECT DISTINCT j FROM JobsModel j left join EmployerModel emp on j.employerId = emp.id " + 
			"left join CandidateModel c on c.jobCategory = j.jobCategory where j.jobStatus = 'O' "
			+ "AND c.jobCategory = :jobRole AND c.industry = :industry and j.inActive = false AND j.jobExp <=:exp AND j.jobMaxExp >=:exp AND j.id Not IN (:jobIDs) AND j.jobLocation IN (:cityList) AND j.canResponseCount > 0 ORDER BY j.adminTopPriority desc, FIELD(emp.category, 'Staffing Services') desc, j.jobPostedTime desc")
	Page<JobsModel> findCanAllExperiencedJobs(List<Integer> jobIDs,int exp, String jobRole, String industry,List<String> cityList,Pageable pageable);

	@Query("SELECT DISTINCT j FROM JobsModel j left join EmployerModel emp on j.employerId = emp.id " + 
			"left join CandidateModel c on c.jobCategory = j.jobCategory where j.jobStatus = 'O' "
			+ "AND c.jobCategory = :jobRole and j.inActive = false AND j.jobExp <=:exp AND j.jobMaxExp >=:exp AND j.id Not IN (:jobIDs) AND j.jobLocation IN (:cityList) AND j.canResponseCount > 0")
	List<JobsModel> findCanMatchedExperiencedJobs(List<Integer> jobIDs,int exp, String jobRole, List<String> cityList);
	
	@Query("SELECT DISTINCT j FROM JobsModel j left join EmployerModel emp on " + 
			"j.employerId = emp.id left join CfgFullTimeGroup cfg on " + 
			"trim(lower(cfg.groupName)) = trim(lower(:jobRole)) where " + 
			"j.id Not IN (:jobIDs) AND j.jobStatus = " + 
			"'O' AND j.jobExp >=:exp AND j.inActive = false AND j.canResponseCount > 0")
	Page<JobsModel> findExperiencedRelatedJobs(String jobRole, List<Integer> jobIDs,int exp, Pageable pageable);
	
	@Query("SELECT DISTINCT j FROM JobsModel j left join EmployerModel emp on " + 
			"j.employerId = emp.id where " + 
			"j.id Not IN (:jobIDs) AND j.jobLocation IN (:cityList) AND j.jobCategory IN (:jobRoles) AND j.jobStatus = " + 
			"'O' AND j.jobExp >=:exp AND j.inActive = false AND j.canResponseCount > 0 ORDER BY j.adminTopPriority desc, FIELD(emp.category, 'Staffing Services') desc, j.jobPostedTime desc")
	List<JobsModel> findNewExperiencedRelatedJobs(List<String> jobRoles, List<Integer> jobIDs,List<String> cityList,int exp, Pageable pageable);

    @Query(value = "SELECT j.job_category,COUNT(*) as count FROM jobs j where employer_id=:empId and j.job_status IN ('O','C') GROUP BY j.job_category " + 
    		"ORDER BY count DESC Limit 3;", nativeQuery = true)
    List<Map<String, Object>> getEmpJobCategories(int empId);

    @Query(value = "SELECT * FROM jobs j where j.employer_id=:empId and j.job_category=:jobRole order by j.job_id desc limit 1;", nativeQuery = true)
    JobsModel getJobByJobCategory(int empId,String jobRole);

    @Query(value = "SELECT * FROM TaizoDB.jobs where employer_id=:empId and job_status IN ('O','C') and inactive = false order by job_id desc limit 1;", nativeQuery = true)
	JobsModel findByRecentJob(int empId);

    @Query(value = "SELECT * FROM TaizoDB.jobs where expiry_date = DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY) and job_status ='O'" + 
    		" and inactive is false and whatsapp_notification is true",nativeQuery = true)
	List<JobsModel> findJobExpiryinDay();

    @Query(value = "SELECT j.job_category,j.job_id,COUNT(*) as count FROM jobs j where employer_id=:empId and j.job_status IN ('O','C') GROUP BY j.job_category " + 
    		"ORDER BY count DESC;", nativeQuery = true)
    List<Map<String, Object>> getEmpMostJobCategories(int empId);
	
    @Query(value = "SELECT * FROM jobs j where j.employer_id=:empId and j.job_category=:jobRole and j.job_status='O' and j.inactive = false and j.can_response_count > 0;", nativeQuery = true)
	List<JobsModel> findByJobDetails(int empId,String jobRole);

	@Query(value = "{ CALL TaizoDB.GetEmpInterviewDetailsByFilter(:empId,:startDate,:endDate,:jobCategory) }", nativeQuery = true)
	List<Map<String, Object>> findByScheduledInterviewDetails(int empId, Date startDate, Date endDate, String jobCategory);

	@Query(value = "{ CALL TaizoDB.GetNewInterviewDetailsById(:interviewId,:status) }", nativeQuery = true)
	Map<String, Object> findByNewInterviewDetails(int interviewId, String status);
	
	@Query(value = "{ CALL TaizoDB.ChatStaffingJobs(:age,:state,:city,:passed_out_year,:qualification,:specialization) }", nativeQuery = true)
	List<Map<String, Object>> findByStaffingMatchedJobs( int age,String state, String city, int passed_out_year, String qualification, String specialization);
	
	@Query(value = "{ CALL TaizoDB.AppStaffingJobs(:age,:state,:city,:passed_out_year,:qualification,:specialization) }", nativeQuery = true)
	List<JobsModel> findByAppStaffingMatchedJobs( int age,String state, String city, int passed_out_year, String qualification, String specialization);

	@Query(value = "{ CALL TaizoDB.Admin_Filters_Jobs(:priority,:employerId, :gender,:company_name, :city, :area, :industry, :job_category, :benefits, :skills, :qualification, :admin_id, :min_salary, :max_salary, :min_experience, :max_experience,:pages,:pageSize,:created_time,:endDate) }", nativeQuery = true)
    List<Map<String, Object>> filterJobs(
    		@Param("priority")String priority,
            @Param("employerId") int employerId,
            @Param("gender") String gender,
            @Param("company_name") String companyName,
            @Param("city") String jobLocation,
            @Param("area") String area,
            @Param("industry") String industry,
            @Param("job_category") String jobCategory,
            @Param("benefits") String benefits,
            @Param("skills") String keyskills,
            @Param("qualification") String qualification,
            @Param("admin_id")int adminId,
            @Param("min_salary") int salary,
            @Param("max_salary") int maxSalary,
            @Param("min_experience") int jobExp,
            @Param("max_experience") int jobMaxExp,
            @Param("pages")int pages,
            @Param("pageSize")int pageSize,
            @Param("created_time")Date createdTime,
            @Param("endDate")Date endDate
    );
	
	@Query(nativeQuery = true, value = "CALL Admin_cfg_jobs()")
    List<Object[]> getJobConfigData();

    default List<Map<String, Object>> getJobConfigDataAsObjects() {
        List<Object[]> result = getJobConfigData();
        List<Map<String, Object>> convertedResult = new ArrayList<>();

        for (Object[] row : result) {
            Map<String, Object> obj = new HashMap<>();
            obj.put("options", row[0]);
            obj.put("category", row[1]);
            convertedResult.add(obj);
        }

        return convertedResult;
    }
	@Query("SELECT e FROM JobsModel e WHERE e.jobStatus='O' AND e.mobileNumber = :mobileNumber ORDER BY e.createdTime DESC")
	Page<JobsModel> findByCompanyName( @Param("mobileNumber") String mobileNumber, 
		    Pageable pageable);

	JobsModel findByEmployerId(int employerId);

	
}