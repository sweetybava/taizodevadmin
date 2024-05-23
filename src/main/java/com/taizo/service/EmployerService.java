package com.taizo.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.CfgEmployerDocumentsModel;
import com.taizo.model.EmpJobRatingsModel;
import com.taizo.model.EmpPlacementPlanDetailsModel;
import com.taizo.model.EmployerCallModel;
import com.taizo.model.EmployerDocumentsModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.EmployerPaymentModel;
import com.taizo.model.JobLeadModel;
import com.taizo.model.LeadModel;
import com.taizo.model.PlansModel;

public interface EmployerService {
	
	
	public EmployerModel updateApprovalStatus(int id, String status);

	public EmployerModel updateEmployer(int id, EmployerModel employer) throws ResourceNotFoundException;

	public Optional<EmployerModel> login(String emailId, String password);

	Optional<EmployerModel> findLogout(String token);

	public Optional<EmployerModel> findEmployerByResetToken(String token);

	public void save(EmployerModel employer);

	public String uploadFile(MultipartFile file, byte[] bs);

	public String uploadCompanyLogo(MultipartFile photo, byte[] bs) throws IOException;

	public boolean deleteCompanyLogo(String pic);

	public EmployerModel updatePaymentStatus(int employerId, String paymentStatus);

	public EmployerModel updateDeactivationStatus(int employerId, String status);

	public List<Map<String, Object>> filteremployer(String industry, String noOfEmployees, String city, int plan);

	public Page<EmployerModel> getAllEmployers(Pageable pageable);

	public long getTotalEmployersCount();
	
	Page<EmployerModel> getAllEmployersOrderedByCreatedTimeDesc(Pageable pageable);
	
    Optional<EmployerModel> findByIdOrMobileNumberOrWhatsappNumber(int id, long mobileNumber, long whatsappNumber);

	public Page<EmployerPaymentModel> findByEmployerIdOrderByCreatedTimeDesc(int employerId, Pageable pageable);

	 public Page<EmployerPaymentModel> getPaymentsByEmployerId(int employerId, Pageable pageable);

	 public Page<EmployerPaymentModel> getAllPayments(Pageable pageable);
	 
	 public long getTotalEmployerPaymentCount(int employerId) ;
	 
	 public long getTotalPaymentCount();
	 
	 Page<EmpJobRatingsModel> findByRatingCount(int ratingCount, int page, int size);
	 
	 public Page<EmpJobRatingsModel> getAllRatingCount(Pageable pageable);

	 public Page<EmployerCallModel> getAllCallRegistry(Pageable pageable);

	public Page<EmployerCallModel> findByJid(int jId,  int page, int size);

	public Page<EmployerModel> findByCompanyNameContainingIgnoreCase(String companyName, Pageable pageable);

	public List<?> getAnalyticsByTime(String time);

	public List<Map<String, Object>> filterEmployer(Integer employerId, String companyName, String industry,
			String noOfEmployees, String city, String area,String contactNumber, Integer page, Integer size, Date startDate,
			Date endDate);

	public Page<LeadModel> findByEmployerLead(String emailId,Boolean fromAdmin, Integer page, Integer pageSize);

	public Page<LeadModel> getAllLead(Pageable pageable);

	public List<Map<String, Object>> filterProforma(Integer employerId, String companyName, Long mobileNumber,
			Integer page, Integer size, Date startDate, Date endDate);

	public long filterEmployerCount(int id, String companyName, String industry, String noOfEmployees, String city,
			String area, String contactNumber, int page, int size, Date createdTime, Date endDate);

	public long filterProformaCount(int employerId, String companyName, long mobileNumber, int page, int size,
			Date createdTime, Date endDate);

	public Page<EmpPlacementPlanDetailsModel> getUnPublishedJobsByEmployer( Integer employerId, int page, int size);

	public Page<EmpPlacementPlanDetailsModel> getAllunpublishedjob(Pageable pageable);

	public Page<LeadModel> findByEmployerLeads(Long mobileNumber, boolean fromAdmin, Integer page, Integer pageSize);

	public String uploadLeadPhotoToS3Bucket1(MultipartFile file, String companyName, boolean b);

	public List<Map<String, Object>> filteremployerFieldList(String companyName, String area, String city,
			Date createdTime, Date endDate, int pages, int size);

	public long filteremployerFieldCount(String companyName, String area, String city, Date createdTime,
			 Date endDate);

	public Page<LeadModel> findByCompanyName(String companyName, boolean fromAdmin, Integer page, Integer pageSize);

	public Page<LeadModel> findByIndustry(String industry, boolean fromAdmin, Integer page, Integer pageSize);

	public Page<LeadModel> findByCity(String city, boolean fromAdmin, Integer page, Integer pageSize);

	public Page<LeadModel> findByCreatedTime(Date startDateTime, Date endDateTime, boolean fromAdmin, Integer page,
			Integer pageSize);

	public Page<LeadModel> findAll(Specification<LeadModel> spec, Pageable pageable);

	public List<EmployerDocumentsModel> getDocumentsByEmpId(int empId);

	public List<CfgEmployerDocumentsModel> getActiveDocuments();

}
