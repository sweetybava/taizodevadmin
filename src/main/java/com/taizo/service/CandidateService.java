package com.taizo.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.taizo.DTO.CandidateAnalyticsFilterDTO;
import com.taizo.DTO.DateRange;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.CanLeadModel;
import com.taizo.model.CandidateAnalyticsModel;
import com.taizo.model.CandidateCallsModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.CfgCanSources;
import com.taizo.model.LeadModel;
import com.taizo.model.MidSeniorLevelCandidateLeadModel;
import com.taizo.model.SampleVideosModel;
import com.taizo.repository.CandidateRepository;

public interface CandidateService {

	public CandidateModel updateSkill(int userId, String skills, String skillVideoType);

	public CandidateModel updatePaymentStatus(int userId, String paymentStatus);

	public CandidateModel updateProfile3(int userId, Integer experienceYears,Integer experienceMonths, String candidateType, Integer overseasExpYears,Integer overseasExpMonths, String expCertificate,
			String certificateType, String license,String licenseType, String industry, String jobCategory, String city, String keySkill);

	public CandidateModel updateProfile1(int userId, String firstName, String lastName,
			String dateOfBirth,String age, String gender, String currentCountry, String currentState, String currentCity,
			String perCountry, String perState, String perCity,long whatsappNumber,String emailId);

	public CandidateModel updateApprovalStatus(int id, String status);

	public CandidateModel updateProfile4(int userId, String jobType,String industry, String jobRole, String candidateLocation,String prefLocation,
			String domesticLocation, String overseasLocation,String candidateType, Integer expYears,Integer expMonths);

	public void downloadData(HttpServletResponse response) throws IOException;

	public void downloadPdf(HttpServletResponse response) throws IOException;

	public CandidateModel updateDocument(int userId, String documentTitle, String document);

	public CandidateModel updateLicense(int userId, String licenseTitle, String license);

	public void searchData(HttpServletResponse response) throws IOException;

	public CandidateModel updateLanguageKey(int userId, String languageKey);

	public boolean deleteImage(String skillvideo);

	public String uploadFile(MultipartFile video, int id, byte[] bytes);

	public String uploadJobFile(MultipartFile video, int id, byte[] bytes);

	public void uploadSampleVideoFileToS3Bucket(MultipartFile file, String videoFileName) throws IOException;

	public void insertSampleVideo(String vid, String vlink, String vdesc, String vtitle);

	Page<SampleVideosModel> findPaginated(int pageNo, int pageSize);

	public CandidateModel get(int id) throws ResourceNotFoundException;

	public void deleteById(int id) throws ResourceNotFoundException;

	public List<Map<String, Object>> filtercandidate(String gender,String eligibility,int assignTo, long mobileNumber, String industry, String jobCategory,
			String specilization,String qualification, String candidateType, String skills, String prefLocation, int passed_out_year,
			int experience, int experience2,int pages,int pageSize,Date date,Date endDate);

	public List<CfgCanSources> getAllEntities();


    CandidateModel findById(String candidateId);
    
    public CandidateModel getCandidateDetailsById(int id);
    
    public CandidateModel getCandidateDetailsByNumber(long number) ;
    
    public Page<CanLeadModel> getAllCanLeadModels(Pageable pageable);
    
    Page<CanLeadModel> getCanLeadModelsByProfilePageNoAndMobileNumber( Pageable pageable);
    
    public List<CandidateCallsModel> getCallsByJid(int jid);
    
    public Page<CandidateCallsModel> getCallsByJidWithPagination(int jid, Pageable pageable);
    
    public Page<CandidateCallsModel> getAllCallsWithPagination(Pageable pageable);

	public Page<CanLeadModel> getCanLeadModelsByProfilePageNo(Integer profilePageNo, Pageable pageable);

	public Page<CanLeadModel> getCanLeadModelsMobileNumber(Long mobileNumber, Pageable pageable);

	public List<Map<String, Object>> filterCandidate(String gender, String industry, String category,
			String qualification, String canType, String skills, String prefLocation, Integer  Passed_out_year,
			Integer Experience, Integer maxExperience, Integer pages, Integer size, Date startDate,
			Date endDate);

	public long filterCandidateCount(String gender, String industry, String jobCategory, String qualification,
			String candidateType, String keySkill, String prefLocation, int passed_out_year, int experience,
			int maxExperience, Date createdTime, Date endDate);

	public CandidateModel getCandidateById(String candidateId);

	public List<Map<String, Object>> filterMetaDatas(Long id,int assignTo, String candidateName, String educationQualification,
			String jobCategory, String mobileNumber, boolean qualified, boolean notQualified, boolean notAttend,boolean noStatus, String experience, String preferredLocation,
			 String joining,  int pages, int size, Date createdTime, Date endDate);

	public long filterMetaLeadCount(Long id,int assignTo, String candidateName, String educationQualification, String jobCategory,
			String mobileNumber, boolean qualified, boolean notQualified, boolean notAttend, boolean noStatus, String experience, String preferredLocation,String joining,
			 Date createdTime, Date endDate);

	public Page<CanLeadModel> getCanLeadModelFromSource(String fromSource, Pageable pageable);

	public List<Map<String, Object>> filterCanInterview(int jobId,long contactNumber, int adminId, String interviewDate,
			String companyName, String interviewEndDate, int page, int size, Date createdTime, Date date,long candidateMobileNumber, String jobCategory,String city, String area, int interviewStatus);

	public long filterCanInterviewCount(int jobId, long contactNumber, int adminId, String interviewDate, String companyName,
			String interviewEndDate, Date createdTime, Date endDate,long candidateMobileNumber,String jobCategory, String city, String area, int interviewStatus);

	public Page<CanLeadModel> getgetCanLeadModelJobCategory(String jobCategory, Pageable pageable);

	public Page<CanLeadModel> getCanLeadModelsByExpYears(Integer expYearsMin, Integer expYearsMax, Pageable pageable);

	public Page<CanLeadModel> getCanLeadModelsByCreatedTime(Date createdTimeStart,
			Date createdTimeEnd, Pageable pageable);

	public Page<CanLeadModel> getCanLeadModelsByQualified(boolean b, Pageable pageable);

	public Page<CanLeadModel> getCanLeadModelsByNotQualified(boolean b, Pageable pageable);

	public Page<CanLeadModel> getCanLeadLeadModelByQualifiedAndNotQualified(boolean b, boolean c, Pageable pageable);

	public Page<CanLeadModel> getCanLeadModels(Specification<CanLeadModel> spec, Pageable pageable);

	public Page<MidSeniorLevelCandidateLeadModel> findAll(Specification<MidSeniorLevelCandidateLeadModel> spec, Pageable pageable);

	public Page<MidSeniorLevelCandidateLeadModel> getAllCandidates(Pageable pageable);

	public List<Map<String, Object>> filterCanLead(int profilePageNo, String fromSource, String jobCategory,
			long mobileNumber, int expYears, int expYears2, String qualificationStatus, String scheduledBy, Date createdTime,
			Date endDate, int page, int size);

	public long filterCanLeadCount(int profilePageNo, String fromSource, String jobCategory, long mobileNumber,
			int expYears, int expYears2, String qualificationStatus, String scheduledBy, Date createdTime, Date endDate);

	
	public CandidateAnalyticsFilterDTO getAnalyticsSummaryByAdminIdAndDateRange(Long adminId, Date startDate,
			Date endDate);

	public List<CandidateAnalyticsFilterDTO> getAnalyticsSummaryByDateRange(Date startDate, Date endDate);
	
	public List<Map<String, Object>> filterMidSenior(int page, int size);
	public long filterMidSeniorCount();

	public List<Map<String, Object>> filtercandidates(String companyName, int adminId, long contactNumber,
			Date createdTime, Date endDate, int page, int size);

	public long filterCandidatesCount(String companyName, int adminId, long contactNumber, Date createdTime,
			Date endDate);
	
}
