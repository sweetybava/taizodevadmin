package com.taizo.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.taizo.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface AdminService {
	
	Optional<Admin> login(String userName, String password);
	
	
	public void uploadSampleVideoFileToS3Bucket(MultipartFile file, String videoFileName) throws IOException;

	public void insertSampleVideo(String vid, String vlink, String vdesc, String vtitle);
	
	public void uploadJobVideoFileToS3Bucket(MultipartFile file, String videoFileName) throws IOException;

	public void insertJobVideo(String vid, String vlink);
	
	Page<AdminCallNotiModel> getAllAdminCallNotifications(Pageable pageable);
	
	List<Admin> getAllAdmins();
	
	 public Admin loginDetails(String emailId, String password);
	 
	 public List<CfgAdminRolesModel> getAdminRoles(Long adminId);	
	 
	 List<AdminRolesPrevilegeMappingModel> getPrivilegesByRoleId(Long roleId);


	List<CfgAdminPrevilegeModel> getAllPrivileges();


	Admin findAdminByEmail(String emailId);


	String fetchDataFromExotelApi( int page, int pageSize);


	Page<AdminCallNotiModel> fetchDataBySidsPaginated(List<String> sid, int page, int pageSize);

	

	String uploadProfilePic(MultipartFile pic, byte[] bytes);


	boolean numberExistsInModels(long number);


	Page<EmployerTimeline> findByEmpFilters(String eventName, Integer empId, Integer empLeadId, Date startDate,
											 Date endDate, Pageable pageable);


	List<Map<String, Object>> filterAdminAnalyticsList(Long adminId, String module, Timestamp createdOn, String dateFilter);


	Page<CandidateTimeLine> findByFilters(String eventName, Integer canId, Integer canLeadId, Long facebookId,
			Long midSeniorCan, Long midSeniorSorcingId, Date startDate, Date endDate, int page, int size);


	

}
