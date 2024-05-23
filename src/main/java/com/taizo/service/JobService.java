package com.taizo.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.EmpPlacementPlanDetailsModel;
import com.taizo.model.JobsModel;
import com.taizo.model.WebJobsModel;

public interface JobService {

	public void delete(int id) throws ResourceNotFoundException;

	public JobsModel updateApprovalStatus(int id, String status);

	public String uploadVideo(MultipartFile video, int id, byte[] bs);

	public String uploadFile(MultipartFile image, int id, byte[] bs);

	public boolean deleteImage(String pic);

	public String getJobEmailAddress(int jobId);

	public JobsModel saveJobDetails(JobsModel jobs) throws ResourceNotFoundException;

	public JobsModel saveJobPersonalization(JobsModel jobs) throws ResourceNotFoundException;

	public JobsModel saveJobPost(JobsModel jobs) throws ResourceNotFoundException;

	public JobsModel saveJobAdditionalDetails(JobsModel jobs) throws ResourceNotFoundException;
	
	public JobsModel saveJobReviewPost(JobsModel jobs) throws ResourceNotFoundException;
	
	public JobsModel postDraftjob(JobsModel jobs) throws ResourceNotFoundException;


	public JobsModel getById(int id) throws ResourceNotFoundException;

	public JobsModel updateJobDetails(JobsModel jobs) throws ResourceNotFoundException;

	Page<JobsModel> findEmployerJobs(int employerId, String status, int pgNo, int length)
			throws ResourceNotFoundException;
	Page<JobsModel> findEmployerDraftJobs(int employerId, String status, int pgNo, int length)
			throws ResourceNotFoundException;

	Page<JobsModel> findEmployerClosedJobs(int employerId, String status, int pgNo, int length) throws ResourceNotFoundException;

	public WebJobsModel saveWebJobDetails(WebJobsModel jobs) throws ResourceNotFoundException;

	public JobsModel postWebJobDetails(int empId, int jobId) throws ResourceNotFoundException;

	public JobsModel openPostJob(int empId, int jobId,boolean freetrial) throws ResourceNotFoundException;
	
	List<Map<String, Object>> filterJobs(String priority, int employerId,String gender,String companyName, String jobLocation, String area, String industry,
            String jobCategory, String benefits, String keyskills, String qualification,
            int salary, int maxSalary, int jobExp, int jobMaxExp,int pages,int pageSize,int assignTo, Date createdTime,Date endDate);


	public List<Map<String, Object>> getJobConfigDataAsObjects();

	public JobsModel getJobDetailsById(String jobId);


    JobsModel findById(String jobId);

	public List<Map<String, Object>> filterJob(Integer employerId, String gender, String companyName, String city,
			String area, String industry, String jobCategory, String benefits, String skills, String qualification,
			Integer minSalary, Integer maxSalary, Integer minExperience, Integer maxExperience, Integer page,
			Integer pageSize, Date startDate, Date endDate);

	public long filterjobCount(int employerId, String gender, String companyName, String jobLocation, String area,
			String industry, String jobCategory, String benefits, String keyskills, String qualification, int salary,
			int maxSalary, int jobExp, int jobMaxExp, int pages, int size, Date createdTime, Date endDate);

	public Page<JobsModel> getPublishedJobs(String mobileNumber, int page, int size);

	public Page<JobsModel> getAllpublishedjob(Pageable pageable);
	
}
