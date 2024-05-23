package com.taizo.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import javax.persistence.criteria.CriteriaBuilder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.appstream.model.Session;
import com.taizo.DTO.CandidateAnalyticsFilterDTO;
import com.taizo.model.CanLeadModel;
import com.taizo.model.CandidateAnalyticsModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.UserModel;

@Repository
public interface CandidateRepository extends JpaRepository<CandidateModel, Long>, JpaSpecificationExecutor<CandidateModel> {

	@Query("select c from CandidateModel c where c.userId = :userId")
	CandidateModel finduser(@Param("userId") int userId);

	Optional<CandidateModel> findByUserId(int userId);

	@Query("select c from CandidateModel c where c.userId = :userId and c.paymentStatus = :paymentStatus")
	CandidateModel findByPaymentStatus(@Param("userId") int userId, @Param("paymentStatus") String paymentStatus);

	Optional<CandidateModel> findById(int id);

	@Query("select c from CandidateModel c where c.approvalStatus <> :Declinedstatus and c.approvalStatus <> :ApprovedStatus or c.approvalStatus is null")
	List<CandidateModel> findApprovedCandidates(@Param("Declinedstatus") String Declinedstatus,
			@Param("ApprovedStatus") String ApprovedStatus);

	@Query("select c from CandidateModel c where c.jobType LIKE %:jobType% and c.jobCategory = COALESCE(NULLIF(:jobCategory, ''), c.jobCategory) and c.currentState LIKE COALESCE(NULLIF(:location, ''), c.currentState) and c.currentState LIKE COALESCE(NULLIF(:domesticLocation, ''), c.currentState)"
			+ " and c.qualification = COALESCE(NULLIF(:qualification, ''), c.qualification)  and c.specification = COALESCE(NULLIF(:specialization, ''), c.specification)"
			+ " and c.gender = COALESCE(NULLIF(:gender, ''), c.gender) and c.experience BETWEEN :minExp AND :maxExp and c.age BETWEEN :minAge AND :maxAge")
	List<CandidateModel> findAllCandidate(@Param("jobType") String jobType, @Param("jobCategory") String jobCategory,
			@Param("location") String location,@Param("domesticLocation") String domesticLocation,
			@Param("qualification") String qualifiation, @Param("specialization") String specialization,
			@Param("gender") String gender1,@Param("minExp") String minExp,@Param("maxExp") String maxExp,

			@Param("minAge") String minAge,@Param("maxAge") String maxAge);	
	
	@Query("select c from CandidateModel c where c.jobType LIKE %:jobType%")
	List<CandidateModel> findAllByJobType(@Param("jobType") String jobType);

	@Query("select c from CandidateModel c where c.candidateLocation = :canLocation")
	List<CandidateModel> findAllByCandidateLocation(@Param("canLocation") String canLocation);

	@Query("select c from CandidateModel c where c.userId = :userId and c.dateOfBirth = :dob")
	CandidateModel findDob(@Param("userId") int userId, @Param("dob") String dob);

	@Query("select c from CandidateModel c where c.userId = :userId and c.lastName = :fatherName")
	CandidateModel findFatherName(@Param("userId") int userId, @Param("fatherName") String fatherName);

	 CandidateModel findByMobileNumber(long mobileNumber); 

	@Query(value ="select c.id,c.firstName,c.jobType,c.industry,c.city,c.prefCountry,c.prefLocation,c.candidateLocation,c.jobCategory,c.age,c.gender,c.currentCountry,c.currentState,c.currentCity,c.mobileNumber,c.whatsappNumber,c.emailId,c.qualification,c.specification,c.experience,c.expMonths,c.overseasExp,c.overseasExpMonths from CandidateModel c  order by c.id desc")
	Page<CandidateModel> findAllcanDetails(Pageable pageable);

	
	//Whatsapp Notification
	@Query(value ="SELECT * FROM candidate e where FIND_IN_SET(:location, e.city) and e.candidate_type = 'Fresher' and e.stop_wa_campaign = false and e.wa_campaign = true", nativeQuery = true)
	List<CandidateModel> getFresherCandidates(@Param("location") String location);

	@Query(value ="SELECT * FROM candidate e where FIND_IN_SET(:location, e.city) and e.exp_in_years >=:jobMinExp AND e.exp_in_years <=:jobMaxExp and e.candidate_type = 'Experienced' and e.job_category = :jobCategory and e.industry = :industry and e.stop_wa_campaign = false and e.wa_campaign = true", nativeQuery = true)
	List<CandidateModel> getExperiencedCandidates(String industry, String jobCategory, int jobMinExp, int jobMaxExp,String location);

	@Query(value ="SELECT * FROM candidate e where FIND_IN_SET(:location, e.city) and e.exp_in_years >0 AND e.exp_in_years <=:jobMaxExp and e.candidate_type = 'Experienced' and e.job_category = :jobCategory and e.industry = :industry and e.stop_wa_campaign = false and e.wa_campaign = true", nativeQuery = true)
	List<CandidateModel> getOneYearExperiencedCandidates(String industry, String jobCategory,String location,int jobMaxExp);
	
	@Query(value ="SELECT * FROM candidate e where FIND_IN_SET(:location, e.city) and e.exp_in_years >=:exp and e.candidate_type = 'Experienced' and e.job_category = :jobCategory and e.industry = :industry and e.stop_wa_campaign = false and e.wa_campaign = true", nativeQuery = true)
	List<CandidateModel> getExperiencedRelatedCandidates(String industry, String jobCategory, String location,int exp);

	//Test Whatsapp Notification
	@Query(value ="SELECT * FROM candidate e where e.candidate_id=40 and FIND_IN_SET(:location, e.city) and e.candidate_type = 'Fresher' and e.stop_wa_campaign = false and e.wa_campaign = true", nativeQuery = true)
	List<CandidateModel> getAdminFresherCandidates(@Param("location") String location);

	//Test Whatsapp Notification
	@Query(value ="SELECT * FROM candidate e where e.candidate_id=40 and FIND_IN_SET(:location, e.city) and e.exp_in_years >=:jobMinExp AND e.exp_in_years <=:jobMaxExp and e.candidate_type = 'Experienced' and e.job_category = :jobCategory and e.industry = :industry and e.stop_wa_campaign = false and e.wa_campaign = true", nativeQuery = true)
	List<CandidateModel> getAdminExperiencedCandidates(String industry, String jobCategory, int jobMinExp, int jobMaxExp,String location);
	
	@Query(value ="SELECT * FROM candidate e where e.candidate_id=40 and FIND_IN_SET(:location, e.city) and e.exp_in_years >0 AND e.exp_in_years <=:jobMaxExp and e.candidate_type = 'Experienced' and e.job_category = :jobCategory and e.industry = :industry and e.stop_wa_campaign = false and e.wa_campaign = true", nativeQuery = true)
	List<CandidateModel> getAdminOneYearExperiencedCandidates(String industry, String jobCategory,String location,int jobMaxExp);

	@Query(value ="SELECT * FROM candidate e where e.candidate_id=40 and FIND_IN_SET(:location, e.city) and e.exp_in_years >=:exp and e.candidate_type = 'Experienced' and e.job_category = :jobCategory and e.industry = :industry and e.stop_wa_campaign = false and e.wa_campaign = true", nativeQuery = true)
	List<CandidateModel> getAdminExperiencedRelatedCandidates(String industry, String jobCategory, String location,int exp);

	@Query(value = "{ CALL TaizoDB.whatsapp_alert() }", nativeQuery = true)
	List<Map<String, Object>> sendWhatsappAlert();
	
	//Whatsapp Notification
	@Query(value =" select * from candidate where candidate_type='Experienced' and job_category=\"Trainee\" and qualification='Diploma' and specialization!='Diploma in Civil Engineering'" + 
			" and FIND_IN_SET(passed_out_year,REPLACE('2021,2022', ', ', ',')) > 0 and age>17 and age<25", nativeQuery = true)
	List<CandidateModel> getJSAlert();
	
	
	

	@Query(value = "{ CALL TaizoDB.Admin_Filters_candidate(:gender,:eligibility,:admin_id,:mobile_number,:industry,:category,:specialization,:qualification,:can_type,:skills,:pref_city,:year_of_passedout,:min_experience,:max_experience,:pages,:pageSize,:created_time,:endDate) }", nativeQuery = true)
	List<Map<String, Object>> candidate(
	    @Param("gender") String gender,                                           
	    @Param("eligibility")String eligibility,
	    @Param("admin_id")int assignTo,
	    @Param("mobile_number")long mobileNumber,
	    @Param("industry") String industry,
	    @Param("category") String category,                   
	    @Param("specialization")String specialization,
	    @Param("qualification") String qualification,
	    @Param("can_type") String canType,
	    @Param("skills") String skills,
	    @Param("pref_city") String prefCity,
	    @Param("year_of_passedout") int yearOfPassedOut,
	    @Param("min_experience") int minExperience,
	    @Param("max_experience") int maxExperience,
	    @Param("pages")int pages,
	    @Param("pageSize")int pageSize,
	    @Param("created_time")Date startDate,
	    @Param("endDate")Date endDate
	);
	CandidateModel findById(String candidateId);
	
	CandidateModel findByMobileNumberOrContactNumberOrWhatsappNumber(long mobileNumber, String contactNumber, long whatsappNumber);


	 @Query("SELECT a.mobileNumber FROM CandidateModel a WHERE a.id = :candidateId")
	String findByMobileNumberById(int candidateId);

	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CandidateModel c WHERE c.mobileNumber = :number OR c.whatsappNumber = :number")
	boolean existsInCandidate(@Param("number") long number);

	CandidateModel findByWhatsappNumber(long mn);

	CandidateModel findByContactNumber(String mobileNumber);
	
	 Optional<CandidateModel> findByMobileNumber(String mobileNumber);

	Optional<CandidateModel> findById(CandidateModel canId);

    @Query("SELECT c FROM CandidateModel c WHERE c.id IN :ids")
    List<CandidateModel> findAllById(@Param("ids") List<Integer> ids);

    


}




