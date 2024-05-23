package com.taizo.service;

import java.io.File; 
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.taizo.controller.gallabox.ExotelCallController;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.CandidateModel;
import com.taizo.model.EmpPlacementPlanDetailsModel;
import com.taizo.model.EmployerActivityModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.JobsModel;
import com.taizo.model.LeadModel;
import com.taizo.model.PlansModel;
import com.taizo.model.WebJobsModel;
import com.taizo.repository.EmpActivityRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.FullTimeGroupingRepository;
import com.taizo.repository.IndustryRepository;
import com.taizo.repository.JobRepository;
import com.taizo.repository.LeadRepository;
import com.taizo.repository.PlansRepository;
import com.taizo.repository.WebJobsRepository;

@Transactional
@Service("jobService")
public class JobServiceImpl implements JobService {

	@Autowired
	JobRepository jobRepository;
	@Autowired
	WebJobsRepository webJobsRepository;
	
	@Autowired
	@Lazy
	NotificationService notificationService;

	@Autowired
	LeadRepository leadRepository;

	@Autowired
	PlansRepository plansRepository;
	
	@Autowired
	ExotelCallController exotelCallController;

	private AmazonS3 s3client;

	@Value("${aws.endpointUrl}")
	private String endpointUrl;

	@Value("${aws.s3.audio.bucket}")
	private String bucketName;
	@Value("${aws.access.key.id}")
	private String accessKey;
	@Value("${aws.access.key.secret}")
	private String secretKey;

	@Value("${aws.s3.bucket.folder}")
	private String folderName;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	CloudWatchLogService cloudWatchLogService;

	@Autowired
	EmpActivityRepository empActivityRepository;

	@PersistenceContext
	EntityManager em;
	
	@Autowired
	FullTimeGroupingRepository fullTimeGroupingRepository;

	@Autowired
	IndustryRepository industryRepository;

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Value("${firebase.js.api.key}")
	private String firebaseJSApiKey;

	@Value("${firebase.deeplink.js.hostname}")
	private String firebaseJSHost;

	@Value("${firebase.jsapp.package.name}")
	private String firebaseJSPackage;
	
	@Autowired
	private EntityManager entityManager;
	
	private Session getHibernateSession() {
		return entityManager.unwrap(Session.class);
	}
	
	

	private static final Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		this.s3client = new AmazonS3Client(credentials);
	}

	@Transactional
	@Override
	public void delete(int id) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		Optional<JobsModel> optional = jobRepository.findById(id);
		if (!optional.isPresent()) {
			throw new ResourceNotFoundException("Job not found.");

		}
		jobRepository.deleteById(id);

	}

	@Transactional
	@Modifying
	@Override
	public JobsModel updateApprovalStatus(int id, String status) {
		// TODO Auto-generated method stub
		Optional<JobsModel> optional = jobRepository.findById(id);

		if (!optional.isPresent()) {
		}

		jobRepository.findByApprovalStatus(id, status);

		return null;
	}

	@Override
	public String uploadFile(MultipartFile multipartFile, int id, byte[] content) {
		// TODO Auto-generated method stub
		String fileUrl = "";
		File file = null;
		try {
			file = new File("/tmp/" + multipartFile);
		} catch (Exception e) {
			e.printStackTrace();

		}
		// file.canWrite();
		// file.canRead();
		FileOutputStream iofs = null;
		try {
			iofs = new FileOutputStream(file);
			iofs.write(content);
			iofs.close();

			String path = folderName + "/" + "Jobs" + "/" + id + "/" + "JobImages" + "/";

			String fileName = generateFileName(multipartFile);
			String imagepath = path + fileName;
			fileUrl = endpointUrl + "/" + "Jobs" + "/" + id + "/" + "JobImages" + "/" + fileName;

			uploadFileTos3bucket(imagepath, file);
			file.delete();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileUrl;
	}

	@Override
	public String uploadVideo(MultipartFile video, int id, byte[] content) {
		// TODO Auto-generated method stub

		String fileUrl = "";
		File file = null;
		try {
			file = new File("/tmp/" + video);
		} catch (Exception e) {
			e.printStackTrace();

		}
		FileOutputStream iofs = null;
		try {
			iofs = new FileOutputStream(file);
			iofs.write(content);
			iofs.close();

			String path = folderName + "/" + "Jobs" + "/" + id + "/" + "JobVideos" + "/";
			String fileName = generateFileName(video);
			String videopath = path + fileName;
			fileUrl = endpointUrl + "/" + "Jobs" + "/" + id + "/" + "JobVideos" + "/" + fileName;

			uploadFileTos3bucket(videopath, file);
			file.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileUrl;
	}

	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}

	private String generateFileName(MultipartFile multiPart) {
		return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
	}

	private void uploadFileTos3bucket(String fileName, File file) {
		s3client.putObject(
				new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
	}

	@Override
	public boolean deleteImage(String image) {
		// TODO Auto-generated method stub
		String fileName = image.substring(image.lastIndexOf("/") + 1);

		try {
			DeleteObjectsRequest delObjReq = new DeleteObjectsRequest(bucketName).withKeys(fileName);
			s3client.deleteObjects(delObjReq);
			return true;
		} catch (SdkClientException s) {
			return false;
		}

	}

	@Override
	public String getJobEmailAddress(int jobId) {
		ArrayList<JobsModel> al = jobRepository.findByJobId(jobId);
		return al.get(0).getEmailId();
	}

	@Override
	public JobsModel saveJobDetails(JobsModel jobs) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		Optional<EmployerModel> optional = employerRepository.findById(jobs.getEmployerId());

		if (!optional.isPresent()) {
			logger.debug("Employer not found with id {}.", jobs.getEmployerId());
			throw new ResourceNotFoundException("Employer not found.");
		}

		EmployerModel empData = optional.get();

		/*
		 * int planId = empData.getPlan(); Optional<PlansModel> empPlan =
		 * plansRepository.findById(planId); PlansModel plan = empPlan.get();
		 */

		JobsModel job = new JobsModel();
		job.setJobType("Full Time");
		job.setIndustry(jobs.getIndustry());
		job.setJobCategory(jobs.getJobCategory());
		job.setSalaryCurrency("INR");
		job.setSalary(jobs.getSalary());
		job.setMaxSalary(jobs.getMaxSalary());
		job.setJobLocationAddr(jobs.getJobLocationAddr());
		job.setJobLatitude(jobs.getJobLatitude());
		job.setJobLongitude(jobs.getJobLongitude());
		job.setJobCountry(jobs.getJobCountry());
		job.setState(jobs.getState());
		job.setArea(jobs.getArea());
		job.setJobLocation(jobs.getJobLocation());
		job.setPersonalization(jobs.getPersonalization());

		job.setJobExp(jobs.getJobExp());
		int e = jobs.getJobExp();
		int max = 0;
		int maxEx = jobs.getJobMaxExp();
		if (e >= 1 && e <= 3) {
			max = 5;
		} else if (e == 4) {
			max = 8;
		} else if (e >= 5 && e <= 8) {
			max = 10;
		} else if (e == 9) {
			max = 15;
		} else if (e >= 10) {
			max = 20;
		} else if (e == 0 && maxEx == 1) {
			max = 1;
		} else {
		}

		job.setJobMaxExp(max);
		if (jobs.getPersonalization().equalsIgnoreCase("1")) {
			job.setContactPersonName(jobs.getContactPersonName());
			job.setMobileNumber(jobs.getMobileNumber());
			job.setIsViewContactPersonName("true");
			job.setIsViewMobileNumber("true");
			job.setIsViewEmailId("false");

		} else if (jobs.getPersonalization().equalsIgnoreCase("2")) {
			job.setEmailId(jobs.getEmailId());
			job.setIsViewEmailId("true");
			job.setIsViewContactPersonName("false");
			job.setIsViewMobileNumber("false");
		} else if (jobs.getPersonalization().equalsIgnoreCase("6")) {
			job.setWstartDate(jobs.getWstartDate());
			job.setWendDate(jobs.getWendDate());
			job.setWstartTime(jobs.getWstartTime());
			job.setWendTime(jobs.getWendTime());
			job.setWdocRequired(jobs.getWdocRequired());
			job.setWaddress(jobs.getWaddress());
			job.setWalkinLatitude(jobs.getWalkinLatitude());
			job.setWalkinLongitude(jobs.getWalkinLongitude());
			job.setContactPersonName(jobs.getContactPersonName());
			job.setMobileNumber(jobs.getMobileNumber());
			job.setIsViewContactPersonName("true");
			job.setIsViewMobileNumber("true");
			job.setIsViewEmailId("false");

		} else {

		}

		job.setWhatsappNoti(jobs.isWhatsappNoti());
		job.setCompanyName(empData.getCompanyName());
		job.setJobStatus(jobs.getJobStatus());
		job.setFromWeb(true);
		job.setEmployerId(jobs.getEmployerId());
		job.setInActive(false);

		jobRepository.save(job);

		return job;
	}

	@Override
	public JobsModel postWebJobDetails(int empId, int jobId) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		Optional<EmployerModel> optional = employerRepository.findById(empId);

		if (!optional.isPresent()) {
			logger.debug("Employer not found with id {}.", empId);
			throw new ResourceNotFoundException("Employer not found.");
		}
		Optional<WebJobsModel> optional1 = webJobsRepository.findById(jobId);
		if (!optional1.isPresent()) {
			throw new ResourceNotFoundException("Job not found.");

		}
		WebJobsModel jobs = optional1.get();

		EmployerModel empData = optional.get();

		int planId = empData.getPlan();
		Optional<PlansModel> empPlan = plansRepository.findById(planId);
		PlansModel plan = empPlan.get();

		JobsModel job = new JobsModel();
		job.setJobType("Full Time");
		job.setIndustry(jobs.getIndustry());
		job.setJobCategory(jobs.getJobCategory());
		job.setSalaryCurrency("INR");
		job.setSalary(jobs.getMinSalary());
		job.setMaxSalary(jobs.getMaxSalary());
		job.setJobLocationAddr(jobs.getJobLocationAddr());
		job.setJobLatitude(jobs.getJobLatitude());
		job.setJobLongitude(jobs.getJobLongitude());
		job.setJobCountry(jobs.getJobCountry());
		job.setState(jobs.getState());
		job.setArea(jobs.getArea());
		job.setJobLocation(jobs.getJobCity());
		job.setPersonalization(jobs.getPersonalization());

		job.setJobExp(jobs.getJobExp());
		job.setJobMaxExp(jobs.getJobExp());

		if (jobs.getPersonalization().equalsIgnoreCase("1")) {
			job.setContactPersonName(jobs.getContactPersonName());
			job.setMobileNumber(jobs.getMobileNumber());
			job.setIsViewContactPersonName("true");
			job.setIsViewMobileNumber("true");
			job.setIsViewEmailId("false");

		} else if (jobs.getPersonalization().equalsIgnoreCase("2")) {
			job.setEmailId(jobs.getEmailId());
			job.setIsViewEmailId("true");
			job.setIsViewContactPersonName("false");
			job.setIsViewMobileNumber("false");
		} else if (jobs.getPersonalization().equalsIgnoreCase("6")) {
			job.setWstartDate(jobs.getStartDate());
			job.setWendDate(jobs.getEndDate());
			job.setWstartTime(jobs.getStartTime());
			job.setWendTime(jobs.getEndTime());
			job.setWdocRequired(jobs.getDocRequired());
			job.setWaddress(jobs.getWaddress());
			job.setWalkinLatitude(jobs.getWalkinLatitude());
			job.setWalkinLongitude(jobs.getWalkinLongitude());
			job.setContactPersonName(jobs.getContactPersonName());
			job.setMobileNumber(jobs.getMobileNumber());
			job.setIsViewContactPersonName("true");
			job.setIsViewMobileNumber("true");
			job.setIsViewEmailId("false");

		} else {

		}

		job.setWhatsappNoti(true);
		job.setCompanyName(empData.getCompanyName());
		job.setJobStatus("O");
		job.setFromWeb(true);
		job.setEmployerId(jobs.getEmployerId());
		job.setInActive(false);

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		String date = dtf.format(now);
		Date currentDate;
		try {
			currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
			job.setJobPostedTime(currentDate);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		int jobPostValidity = plan.getJobPostValidity();

		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, jobPostValidity);
		String output = sdf1.format(c.getTime());

		job.setExpiryDate(output);

		if (empData.getContactPersonName() == null) {
			empData.setContactPersonName(job.getContactPersonName());
			employerRepository.save(empData);
		}

		if (empData.getMobileNumber() == 0) {
			try {
				empData.setMobileNumber(Long.parseLong(job.getMobileNumber()));
				empData.setMobileCountryCode("91");
				employerRepository.save(empData);
				Optional<LeadModel> em = leadRepository.findByMobileNumber(Long.valueOf(job.getMobileNumber()));
				if (em.isPresent()) {
					LeadModel l = em.get();
					l.setRegisteredInApp(true);
					leadRepository.save(l);
				}
			} catch (Exception e) {

			}
		}

		try {
			if (empData.getAddress() == null && empData.getAddress().isEmpty()) {
				empData.setAddress(jobs.getJobLocationAddr());
				empData.setLatitude(jobs.getJobLatitude());
				empData.setLongitude(jobs.getJobLongitude());
				empData.setCountry(jobs.getJobCountry());
				empData.setState(jobs.getState());
				empData.setCity(jobs.getJobCity());
				empData.setArea(jobs.getArea());

				employerRepository.save(empData);
			}
		} catch (Exception e) {

		}
		jobRepository.save(job);

		int jobUniqID = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);

		job.setEmpJobId(String.valueOf(jobUniqID));
		job = jobRepository.save(job);

		EmployerActivityModel EA = new EmployerActivityModel();
		EA.setEmpId(job.getEmployerId());
		EA.setActivity("Your job " + "<b>" + job.getJobCategory() + "</b>" + " has been published!");
		empActivityRepository.save(EA);

		Integer jobUpdateCount = optional.get().getPlanJobCount();
		jobUpdateCount -= 1;
		optional.get().setPlanJobCount(jobUpdateCount);
		employerRepository.save(optional.get());

		DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
		String pdate = formatter.format(new Date());
		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
		Calendar cal = Calendar.getInstance();
		String time = simpleDateFormat1.format(cal.getTime());


		if (activeProfile.equalsIgnoreCase("prod")) {
			notificationService.sendNotification(job, empData);
		}

		return job;
	}

	@Override
	public JobsModel openPostJob(int empId, int jobId,boolean freetrial) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		Optional<EmployerModel> optional = employerRepository.findById(empId);

		if (!optional.isPresent()) {
			logger.debug("Employer not found with id {}.", empId);
			throw new ResourceNotFoundException("Employer not found.");
		}
		Optional<JobsModel> optional1 = jobRepository.findById(jobId);
		if (!optional1.isPresent()) {
			throw new ResourceNotFoundException("Job not found.");

		}
		JobsModel jobs = optional1.get();

		EmployerModel empData = optional.get();

		int planId = empData.getPlan();
		Optional<PlansModel> empPlan = plansRepository.findById(planId);
		PlansModel plan = empPlan.get();

		jobs.setJobStatus("O");

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		String date = dtf.format(now);
		Date currentDate;
		try {
			currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
			jobs.setJobPostedTime(currentDate);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		int jobPostValidity = plan.getJobPostValidity();

		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, jobPostValidity);
		String output = sdf1.format(c.getTime());

		jobs.setExpiryDate(output);
		jobs.setCanResponseCount(plan.getProfiles());
		jobs.setTotalCanResponse(plan.getProfiles());
		if(freetrial) {
			jobs.setFreetrialJob(true);
		}

		if (empData.getContactPersonName() == null) {
			empData.setContactPersonName(jobs.getContactPersonName());
			employerRepository.save(empData);
		}

		if (empData.getMobileNumber() == 0) {
			try {
				empData.setMobileNumber(Long.parseLong(jobs.getMobileNumber()));
				empData.setMobileCountryCode("91");
				employerRepository.save(empData);
				Optional<LeadModel> em = leadRepository.findByMobileNumber(Long.valueOf(jobs.getMobileNumber()));
				if (em.isPresent()) {
					LeadModel l = em.get();
					l.setRegisteredInApp(true);
					leadRepository.save(l);
				}
			} catch (Exception e) {

			}
		}

		jobRepository.save(jobs);

		int jobUniqID = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);

		jobs.setEmpJobId(String.valueOf(jobUniqID));
		jobs.setInActive(false);
		jobs.setWhatsappNoti(true);
		jobs.setFromWeb(true);

		jobs = jobRepository.save(jobs);

		EmployerActivityModel EA = new EmployerActivityModel();
		EA.setEmpId(jobs.getEmployerId());
		EA.setActivity("Your job " + "<b>" + jobs.getJobCategory() + "</b>" + " has been published!");
		empActivityRepository.save(EA);

		Integer jobUpdateCount = optional.get().getPlanJobCount();
		jobUpdateCount -= 1;
		optional.get().setPlanJobCount(jobUpdateCount);

		employerRepository.save(optional.get());

		DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
		String pdate = formatter.format(new Date());
		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
		Calendar cal = Calendar.getInstance();
		String time = simpleDateFormat1.format(cal.getTime());

		if (activeProfile.equalsIgnoreCase("prod")) {
		String jobStatus = "Paid";
		String eventName = "Job Published";
		if(freetrial) {
			jobStatus = "Free Trial";
			eventName = "Free Trial Published";
		}
		
		HashMap<String, String> data1 = new HashMap<>();
		data1.put("Event Name", "Job Alert");
		data1.put("Event Type", eventName);
		data1.put("Type", "Job");
		data1.put("Company Name", jobs.getCompanyName());
		data1.put("Contact Person Name", jobs.getContactPersonName());
		data1.put("Position", jobs.getJobCategory());
		data1.put("Location", jobs.getJobLocation() != null ? jobs.getJobLocation() : "");
		data1.put("Experience", String.valueOf(jobs.getJobExp())+" to "+String.valueOf(jobs.getJobMaxExp()));
		data1.put("Source", "Web");
		data1.put("Mobile Number", String.valueOf(jobs.getMobileNumber()));
		data1.put("Job Status", jobStatus);
		data1.put("ID Type", "Job ID");
		data1.put("ID", String.valueOf(jobs.getId()));

		exotelCallController.connectToAgent("+91" + String.valueOf(jobs.getMobileNumber()),"Emp",data1);
		
			notificationService.sendNotification(jobs, empData);
		}

		return jobs;
	}

	@Override
	public JobsModel postDraftjob(JobsModel jobs) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		Optional<EmployerModel> empoptional = employerRepository.findById(jobs.getEmployerId());

		if (!empoptional.isPresent()) {
			logger.debug("Employer not found with id {}.", jobs.getEmployerId());
			throw new ResourceNotFoundException("Employer not found.");
		}
		Optional<JobsModel> optional = jobRepository.findById(jobs.getId());

		// TODO Auto-generated method stub
		if (!empoptional.isPresent()) {
			logger.debug("Job not found with id {}.", jobs.getEmployerId());
			throw new ResourceNotFoundException("Job not found.");
		}

		EmployerModel empData = empoptional.get();

		/*
		 * int planId = empData.getPlan(); Optional<PlansModel> empPlan =
		 * plansRepository.findById(planId); PlansModel plan = empPlan.get();
		 */

		JobsModel job = optional.get();
		job.setJobType("Full Time");
		job.setIndustry(jobs.getIndustry());
		job.setJobCategory(jobs.getJobCategory());
		job.setSalaryCurrency("INR");
		job.setSalary(jobs.getSalary());
		job.setMaxSalary(jobs.getMaxSalary());
		job.setJobLocationAddr(jobs.getJobLocationAddr());
		job.setJobLatitude(jobs.getJobLatitude());
		job.setJobLongitude(jobs.getJobLongitude());
		job.setJobCountry(jobs.getJobCountry());
		job.setState(jobs.getState());
		job.setArea(jobs.getArea());
		job.setJobLocation(jobs.getJobLocation());
		job.setPersonalization(jobs.getPersonalization());

		job.setJobExp(jobs.getJobExp());
		job.setJobMaxExp(jobs.getJobExp());

		if (jobs.getPersonalization().equalsIgnoreCase("1")) {
			job.setContactPersonName(jobs.getContactPersonName());
			job.setMobileNumber(jobs.getMobileNumber());
			job.setIsViewContactPersonName("true");
			job.setIsViewMobileNumber("true");
			job.setIsViewEmailId("false");

		} else if (jobs.getPersonalization().equalsIgnoreCase("2")) {
			job.setEmailId(jobs.getEmailId());
			job.setIsViewEmailId("true");
			job.setIsViewContactPersonName("false");
			job.setIsViewMobileNumber("false");
		} else if (jobs.getPersonalization().equalsIgnoreCase("6")) {
			job.setWstartDate(jobs.getWstartDate());
			job.setWendDate(jobs.getWendDate());
			job.setWstartTime(jobs.getWstartTime());
			job.setWendTime(jobs.getWendTime());
			job.setWdocRequired(jobs.getWdocRequired());
			job.setWaddress(jobs.getWaddress());
			job.setWalkinLatitude(jobs.getWalkinLatitude());
			job.setWalkinLongitude(jobs.getWalkinLongitude());
			job.setContactPersonName(jobs.getContactPersonName());
			job.setMobileNumber(jobs.getMobileNumber());
			job.setIsViewContactPersonName("true");
			job.setIsViewMobileNumber("true");
			job.setIsViewEmailId("false");

		} else {

		}

		job.setWhatsappNoti(jobs.isWhatsappNoti());
		job.setCompanyName(empData.getCompanyName());
		job.setJobStatus(jobs.getJobStatus());
		job.setFromWeb(true);
		job.setEmployerId(jobs.getEmployerId());
		job.setInActive(false);

		jobRepository.save(job);

		return job;
	}

	@Override
	public JobsModel saveJobPost(JobsModel jobs) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		Optional<EmployerModel> optional = employerRepository.findById(jobs.getEmployerId());

		if (!optional.isPresent()) {
			logger.debug("Employer not found with id {}.", jobs.getEmployerId());
			throw new ResourceNotFoundException("Employer not found.");
		}
		JobsModel job = new JobsModel();
		job.setJobType("Full Time");
		job.setIndustry(jobs.getIndustry());
		job.setJobCategory(jobs.getJobCategory());
		job.setSalaryCurrency("INR");
		job.setSalary(jobs.getSalary());
		job.setMaxSalary(jobs.getMaxSalary());
		job.setJobLocationAddr(jobs.getJobLocationAddr());
		job.setJobLatitude(jobs.getJobLatitude());
		job.setJobLongitude(jobs.getJobLongitude());
		job.setJobCountry(jobs.getJobCountry());
		job.setState(jobs.getState());
		job.setArea(jobs.getArea());
		job.setJobLocation(jobs.getJobLocation());
		job.setPersonalization(jobs.getPersonalization());

		job.setJobExp(jobs.getJobExp());
		job.setJobMaxExp(jobs.getJobExp());

		try {
			if (jobs.getPersonalization().equalsIgnoreCase("1")) {
				job.setContactPersonName(jobs.getContactPersonName());
				job.setMobileNumber(jobs.getMobileNumber());
				job.setIsViewContactPersonName("true");
				job.setIsViewMobileNumber("true");
				job.setIsViewEmailId("false");

			} else if (jobs.getPersonalization().equalsIgnoreCase("2")) {
				job.setEmailId(jobs.getEmailId());
				job.setIsViewEmailId("true");
				job.setIsViewContactPersonName("false");
				job.setIsViewMobileNumber("false");
			} else if (jobs.getPersonalization().equalsIgnoreCase("6")) {
				job.setWstartDate(jobs.getWstartDate());
				job.setWendDate(jobs.getWendDate());
				job.setWstartTime(jobs.getWstartTime());
				job.setWendTime(jobs.getWendTime());
				job.setWdocRequired(jobs.getWdocRequired());
				job.setWaddress(jobs.getWaddress());
				job.setWalkinLatitude(jobs.getWalkinLatitude());
				job.setWalkinLongitude(jobs.getWalkinLongitude());
				job.setContactPersonName(jobs.getContactPersonName());
				job.setMobileNumber(jobs.getMobileNumber());
				job.setIsViewContactPersonName("true");
				job.setIsViewMobileNumber("true");
				job.setIsViewEmailId("false");

			} else {

			}
		} catch (Exception e) {

		}

		job.setWhatsappNoti(jobs.isWhatsappNoti());
		// job.setCompanyName(empData.getCompanyName());
		job.setJobStatus(jobs.getJobStatus());
		job.setEmployerId(jobs.getEmployerId());

		int jobUniqID = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);

		job.setEmpJobId(String.valueOf(jobUniqID));
		job.setInActive(false);

		job = jobRepository.save(job);

		EmployerActivityModel EA = new EmployerActivityModel();
		EA.setEmpId(jobs.getEmployerId());
		EA.setActivity("Your job " + "<b>" + jobs.getJobCategory() + "</b>" + " has been saved!");
		empActivityRepository.save(EA);

		return job;

	}

	@Override
	public JobsModel saveJobReviewPost(JobsModel jobs) throws ResourceNotFoundException {
		Optional<JobsModel> optional = jobRepository.findById(jobs.getId());

		// TODO Auto-generated method stub
		if (!optional.isPresent()) {
			logger.debug("Job not found with id {}.", jobs.getEmployerId());
			throw new ResourceNotFoundException("Job not found.");
		}
		JobsModel job = optional.get();
		job.setJobType("Full Time");
		job.setIndustry(jobs.getIndustry());
		job.setJobCategory(jobs.getJobCategory());
		job.setSalaryCurrency("INR");
		job.setSalary(jobs.getSalary());
		job.setMaxSalary(jobs.getMaxSalary());
		job.setJobLocationAddr(jobs.getJobLocationAddr());
		job.setJobLatitude(jobs.getJobLatitude());
		job.setJobLongitude(jobs.getJobLongitude());
		job.setJobCountry(jobs.getJobCountry());
		job.setState(jobs.getState());
		job.setArea(jobs.getArea());
		job.setJobLocation(jobs.getJobLocation());
		job.setPersonalization(jobs.getPersonalization());

		job.setJobExp(jobs.getJobExp());
		job.setJobMaxExp(jobs.getJobExp());
		job.setWhatsappNoti(jobs.isWhatsappNoti());
		job.setInActive(false);

		if (jobs.getPersonalization().equalsIgnoreCase("1")) {
			job.setContactPersonName(jobs.getContactPersonName());
			job.setMobileNumber(jobs.getMobileNumber());
			job.setIsViewContactPersonName("true");
			job.setIsViewMobileNumber("true");
			job.setIsViewEmailId("false");

		} else if (jobs.getPersonalization().equalsIgnoreCase("2")) {
			job.setEmailId(jobs.getEmailId());
			job.setIsViewEmailId("true");
			job.setIsViewContactPersonName("false");
			job.setIsViewMobileNumber("false");
		} else if (jobs.getPersonalization().equalsIgnoreCase("6")) {
			job.setWstartDate(jobs.getWstartDate());
			job.setWendDate(jobs.getWendDate());
			job.setWstartTime(jobs.getWstartTime());
			job.setWendTime(jobs.getWendTime());
			job.setWdocRequired(jobs.getWdocRequired());
			job.setWaddress(jobs.getWaddress());
			job.setWalkinLatitude(jobs.getWalkinLatitude());
			job.setWalkinLongitude(jobs.getWalkinLongitude());
			job.setContactPersonName(jobs.getContactPersonName());
			job.setMobileNumber(jobs.getMobileNumber());
			job.setIsViewContactPersonName("true");
			job.setIsViewMobileNumber("true");
			job.setIsViewEmailId("false");

		} else {

		}

		job = jobRepository.save(job);

		return job;

	}

	@Override
	public JobsModel saveJobPersonalization(JobsModel jobs) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		Optional<JobsModel> optional = jobRepository.findById(jobs.getId());
		if (!optional.isPresent()) {
			throw new ResourceNotFoundException("Job not found.");

		}
		JobsModel existing = optional.get();
		existing.setPersonalization(jobs.getPersonalization());
		existing.setContactPersonName(jobs.getContactPersonName());
		existing.setMobileNumber(jobs.getMobileNumber());
		existing.setEmailId(jobs.getEmailId());
		existing.setWstartDate(jobs.getWstartDate());
		existing.setWendDate(jobs.getWendDate());
		existing.setWstartTime(jobs.getWstartTime());
		existing.setWendTime(jobs.getWendTime());
		existing.setWdocRequired(jobs.getWdocRequired());
		existing.setWaddress(jobs.getWaddress());
		existing.setWalkinLatitude(jobs.getWalkinLatitude());
		existing.setWalkinLongitude(jobs.getWalkinLongitude());

		existing = jobRepository.save(existing);

		return existing;
	}

	@Override
	public JobsModel saveJobAdditionalDetails(JobsModel jobs) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		Optional<JobsModel> optional = jobRepository.findById(jobs.getId());
		if (!optional.isPresent()) {
			throw new ResourceNotFoundException("Job not found.");

		}
		JobsModel existing = optional.get();
		existing.setWorkHours(jobs.getWorkHours());
		existing.setOt(jobs.getOt());
		existing.setShiftType(jobs.getShiftType());
		existing.setShiftTimings(jobs.getShiftTimings());
		existing.setBenefits(jobs.getBenefits());
		existing.setAdditionalDetailsFilled(true);

		existing = jobRepository.save(existing);
		return existing;
	}

	@Override
	public JobsModel getById(int id) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		Optional<JobsModel> optional = jobRepository.findById(id);
		if (!optional.isPresent()) {
			logger.debug("Job not found with id {}.", id);
			throw new ResourceNotFoundException("Job not found.");
		}

		return optional.get();
	}

	@Override
	public JobsModel updateJobDetails(JobsModel jobs) throws ResourceNotFoundException {
		Optional<JobsModel> optional = jobRepository.findById(jobs.getId());

		// TODO Auto-generated method stub
		if (!optional.isPresent()) {
			logger.debug("Job not found with id {}.", jobs.getEmployerId());
			throw new ResourceNotFoundException("Job not found.");
		}

		JobsModel j = optional.get();
		j.setIndustry(jobs.getIndustry());
		j.setJobCategory(jobs.getJobCategory());
		j.setSalary(jobs.getSalary());
		j.setMaxSalary(jobs.getMaxSalary());
		j.setJobExp(jobs.getJobExp());
		j.setJobMaxExp(jobs.getJobExp());
		j.setJobLocationAddr(jobs.getJobLocationAddr());
		j.setJobLatitude(jobs.getJobLatitude());
		j.setJobLongitude(jobs.getJobLongitude());
		j.setJobCountry(jobs.getJobCountry());
		j.setState(jobs.getState());
		j.setJobLocation(jobs.getJobLocation());
		j.setArea(jobs.getArea());
		j.setInActive(false);

		j = jobRepository.save(j);

		return j;
	}

	@Override
	public Page<JobsModel> findEmployerJobs(int employerId, String status, int pgNo, int length)
			throws ResourceNotFoundException {
		int page = pgNo / length; // Calculate page number
		Pageable pageable = PageRequest.of(page, length, new Sort(Sort.Direction.DESC, "jobPostedTime"));

		Page<JobsModel> pagedResult = jobRepository.findEmpJob(employerId, status, pageable);

		if (pagedResult.isEmpty()) {
			throw new ResourceNotFoundException("Job not found.");
		}

		return pagedResult;
	}

	@Override
	public Page<JobsModel> findEmployerDraftJobs(int employerId, String status, int pgNo, int length)
			throws ResourceNotFoundException {
		int page = pgNo / length; // Calculate page number
		Pageable pageable = PageRequest.of(page, length, new Sort(Sort.Direction.DESC, "createdTime"));

		Page<JobsModel> pagedResult = jobRepository.findEmpDraftJob(employerId, status, pageable);

		if (pagedResult.isEmpty()) {
			throw new ResourceNotFoundException("Job not found.");
		}

		return pagedResult;
	}

	@Override
	public Page<JobsModel> findEmployerClosedJobs(int employerId,String status, int pgNo, int length)
			throws ResourceNotFoundException {
		int page = pgNo / length; // Calculate page number
		Pageable pageable = PageRequest.of(page, length, new Sort(Sort.Direction.DESC, "createdTime"));
		Page<JobsModel> pagedResult = jobRepository.findEmpClosedJob(employerId,status, pageable);

		if (pagedResult.isEmpty()) {
			throw new ResourceNotFoundException("Job not found.");
		}

		return pagedResult;
	}

	@Override
	public WebJobsModel saveWebJobDetails(WebJobsModel jobs) throws ResourceNotFoundException {
		// TODO Auto-generated method stub

		WebJobsModel job = new WebJobsModel();
		job.setIndustry(jobs.getJobCategory());
		job.setJobCategory(jobs.getIndustry());
		job.setSalaryCurrency("INR");
		job.setMinSalary(jobs.getMinSalary());
		job.setMaxSalary(jobs.getMaxSalary());
		job.setJobExp(jobs.getJobExp());
		job.setJobLocationAddr(jobs.getJobLocationAddr());
		job.setJobLatitude(jobs.getJobLatitude());
		job.setJobLongitude(jobs.getJobLongitude());
		job.setJobCountry(jobs.getJobCountry());
		job.setState(jobs.getState());
		job.setArea(jobs.getArea());
		job.setJobCity(jobs.getJobCity());
		job.setCompanyName(jobs.getCompanyName());

		job = webJobsRepository.save(job);

		return job;
	}

	@Override
	public List<Map<String, Object>> filterJobs(String priority,int employerId, String gender,String companyName, String jobLocation, String area, String industry,
	            String jobCategory, String benefits, String keyskills, String qualification,int adminId,
	            int salary, int maxSalary, int jobExp, int jobMaxExp,int pages,int pageSize,Date createdTime,Date endDate) {

	    return jobRepository.filterJobs(priority,employerId, gender,companyName, jobLocation, area, industry, jobCategory, benefits, keyskills,
	                                    qualification,adminId, salary, maxSalary, jobExp, jobMaxExp,pages,pageSize,createdTime,endDate);
	}



	

	@Override
	public List<Map<String, Object>> getJobConfigDataAsObjects() {
		// TODO Auto-generated method stub
		return jobRepository.getJobConfigDataAsObjects();
	}

	@Override
	public JobsModel getJobDetailsById(String jobId) {
		return jobRepository.findById(Integer.parseInt(jobId)).orElse(null);
	}

	@Override
	public JobsModel findById(String jobId) {
		return jobRepository.findById(Integer.parseInt(jobId)).get();
	}

	@Override
	public List<Map<String, Object>> filterJob(Integer employerId, String gender, String companyName, String city,
			String area, String industry, String jobCategory, String benefits, String skills, String qualification,
			Integer minSalary, Integer maxSalary, Integer minExperience, Integer maxExperience, Integer page,
			Integer size, Date startDate, Date endDate) {
		
		 CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	        CriteriaQuery<JobsModel> criteriaQuery = criteriaBuilder.createQuery(JobsModel.class);
	        Root<JobsModel> root = criteriaQuery.from(JobsModel.class);
	        
	        List<Predicate> predicates = new ArrayList<>();
	        
	        if (employerId != 0) {
	        	predicates.add(criteriaBuilder.equal(root.get("employerId"),employerId));
	        }
	        
	        if (gender != null) {
	            Predicate genderPredicate;
	            if ("male".equalsIgnoreCase(gender)) {
	                genderPredicate = criteriaBuilder.equal(root.get("gender"), "male");
	            } else if ("female".equalsIgnoreCase(gender)) {
	                genderPredicate = criteriaBuilder.equal(root.get("gender"), "female");
	            } else if ("both".equalsIgnoreCase(gender)) {
	                genderPredicate = root.get("gender").in("male", "female", "Prefer not to say");
	            } else {
	                // Handle other cases if needed
	                genderPredicate = criteriaBuilder.conjunction();
	            }
	            predicates.add(genderPredicate);
	        }
	        
	        if (companyName != null) {
	        	predicates.add(criteriaBuilder.like(root.get("companyName"), "%" + companyName.trim() + "%"));
	        }
	        
	        if (city != null) {
	            predicates.add(criteriaBuilder.equal(root.get("jobLocation"), city));   
	        }
	        
	        if (area != null) {
	            predicates.add(criteriaBuilder.equal(root.get("area"), area));   
	        }
	        
	        if (industry != null) {
	            predicates.add(criteriaBuilder.equal(root.get("industry"), industry));  
	        }
	        
	        if (jobCategory != null) {
	            predicates.add(criteriaBuilder.equal(root.get("jobCategory"), jobCategory));
	        }
	        
	        if (benefits != null) {
	            String[] benefitList = benefits.split(",");
	            List<Predicate> benefitPredicates = new ArrayList<>();
	            
	            for (String benefit : benefitList) {
	                benefitPredicates.add(criteriaBuilder.like(root.get("benefits"), "%" + benefit.trim() + "%"));
	            }
	            
	            predicates.add(criteriaBuilder.or(benefitPredicates.toArray(new Predicate[0])));
	        }

	        
	        if (skills != null) {
	            String[] skillList = skills.split(",");
	            List<Predicate> skillPredicates = new ArrayList<>();
	            for (String skill : skillList) {
	                skillPredicates.add(criteriaBuilder.like(root.get("keyskills"), "%" + skill.trim() + "%"));
	            }
	            predicates.add(criteriaBuilder.or(skillPredicates.toArray(new Predicate[0])));
	        }
	        
	        if (qualification != null) {
	            String[] qualificationList = qualification.split(",");
	            List<Predicate> qualificationPredicates = new ArrayList<>();
	            
	            for (String qual : qualificationList) {
	                qualificationPredicates.add(criteriaBuilder.equal(root.get("qualification"), qual.trim()));
	            }
	            
	            predicates.add(criteriaBuilder.or(qualificationPredicates.toArray(new Predicate[0])));
	        }

	        
	        if (minSalary != 0 || maxSalary != 0) {
	            Predicate minSalaryPredicate=(criteriaBuilder.between(root.get("salary"), minSalary, maxSalary));
	            Predicate maxSalaryPredicate=(criteriaBuilder.between(root.get("maxSalary"), minSalary, maxSalary));

	            predicates.add(criteriaBuilder.and(minSalaryPredicate, maxSalaryPredicate));
	         }
	        
	        if (minExperience != 0 || maxExperience != 0) {
	            Predicate minExpPredicate = criteriaBuilder.between(root.get("jobExp"), minExperience, maxExperience);
	            Predicate maxExpPredicate = criteriaBuilder.between(root.get("jobMaxExp"), minExperience, maxExperience);
	            
	            predicates.add(criteriaBuilder.and(minExpPredicate, maxExpPredicate));
	        }

	        
	        if (startDate != null && endDate != null) {
	        	predicates.add(criteriaBuilder.between(root.get("createdTime"), startDate, endDate));
	        }

	        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));
	        
	        if (predicates != null && predicates.size() != 0) {
				Predicate[] predicate = predicates.toArray(new Predicate[0]);
				criteriaQuery.where(predicate);
			}
	        List<JobsModel> resultList = entityManager.createQuery(criteriaQuery)
	                .setFirstResult((page - 1) * size)
	                .setMaxResults(size)
	                .getResultList();

	        List<Map<String, Object>> resultMaps = new ArrayList<>();

	        for (JobsModel job : resultList) {
	            Map<String, Object> resultMap = new HashMap<>();
	            resultMap.put("job", job);

	            String message = getMessageBasedOnCreatedTime(job.getCreatedTime());
	            resultMap.put("message", message);
	            
	            resultMaps.add(resultMap);
	        }

	        return resultMaps;
	}

	private String getMessageBasedOnCreatedTime(Date createdTime) {
	    // Calculate the difference between the current time and createdTime
	    long currentTimeMillis = System.currentTimeMillis();
	    long createdTimeMillis = createdTime.getTime();
	    long differenceMillis = currentTimeMillis - createdTimeMillis;

	    // Calculate the difference in days
	    long differenceDays = differenceMillis / (1000 * 60 * 60 * 24);

	    // Determine the message based on the difference in days
	    if (differenceDays <= 15) {
	        return "Low";
	    } else if (differenceDays <= 30) {
	        return "Medium";
	    } else {
	        return "High";
	    }
	}
	
	
	@Override
	public long filterjobCount(int employerId, String gender, String companyName, String jobLocation, String area,
			String industry, String jobCategory, String benefits, String keyskills, String qualification, int salary,
			int maxSalary, int jobExp, int jobMaxExp, int pages, int size, Date createdTime, Date endDate) {
		// TODO Auto-generated method stub
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<JobsModel> root = criteriaQuery.from(JobsModel.class);

        List<Predicate> predicates = new ArrayList<>();
        
        if (employerId != 0) {
        	predicates.add(criteriaBuilder.equal(root.get("employerId"),employerId));
        }
        
        if (gender != null) {
            Predicate genderPredicate;
            if ("male".equalsIgnoreCase(gender)) {
                genderPredicate = criteriaBuilder.equal(root.get("gender"), "male");
            } else if ("female".equalsIgnoreCase(gender)) {
                genderPredicate = criteriaBuilder.equal(root.get("gender"), "female");
            } else if ("both".equalsIgnoreCase(gender)) {
                genderPredicate = root.get("gender").in("male", "female", "Prefer not to say");
            } else {
                // Handle other cases if needed
                genderPredicate = criteriaBuilder.conjunction();
            }
            predicates.add(genderPredicate);
        }
        
        if (companyName != null) {
        	predicates.add(criteriaBuilder.like(root.get("companyName"), "%" + companyName.trim() + "%"));
        }
        
        if (jobLocation != null) {
            predicates.add(criteriaBuilder.equal(root.get("jobLocation"), jobLocation));   
        }
        
        if (area != null) {
            predicates.add(criteriaBuilder.equal(root.get("area"), area));   
        }
        
        if (industry != null) {
            predicates.add(criteriaBuilder.equal(root.get("industry"), industry));  
        }
        
        if (jobCategory != null) {
            predicates.add(criteriaBuilder.equal(root.get("jobCategory"), jobCategory));
        }
        
        if (benefits != null) {
            String[] benefitList = benefits.split(",");
            List<Predicate> benefitPredicates = new ArrayList<>();
            
            for (String benefit : benefitList) {
                benefitPredicates.add(criteriaBuilder.like(root.get("benefits"), "%" + benefit.trim() + "%"));
            }
            
            predicates.add(criteriaBuilder.or(benefitPredicates.toArray(new Predicate[0])));
        }

        
        if (keyskills != null) {
            String[] skillList = keyskills.split(",");
            List<Predicate> skillPredicates = new ArrayList<>();
            for (String skill : skillList) {
                skillPredicates.add(criteriaBuilder.like(root.get("keyskills"), "%" + skill.trim() + "%"));
            }
            predicates.add(criteriaBuilder.or(skillPredicates.toArray(new Predicate[0])));
        }
        
        if (qualification != null) {
            String[] qualificationList = qualification.split(",");
            List<Predicate> qualificationPredicates = new ArrayList<>();
            
            for (String qual : qualificationList) {
                qualificationPredicates.add(criteriaBuilder.equal(root.get("qualification"), qual.trim()));
            }
            
            predicates.add(criteriaBuilder.or(qualificationPredicates.toArray(new Predicate[0])));
        }

        
        if (salary != 0 || maxSalary != 0) {
            Predicate minSalaryPredicate=(criteriaBuilder.between(root.get("salary"), salary, maxSalary));
            Predicate maxSalaryPredicate=(criteriaBuilder.between(root.get("maxSalary"), salary, maxSalary));

            predicates.add(criteriaBuilder.and(minSalaryPredicate, maxSalaryPredicate));
         }
        
        if (jobExp != 0 || jobMaxExp != 0) {
            Predicate minExpPredicate = criteriaBuilder.between(root.get("jobExp"), jobExp, jobMaxExp);
            Predicate maxExpPredicate = criteriaBuilder.between(root.get("jobMaxExp"), jobExp, jobMaxExp);
            
            predicates.add(criteriaBuilder.and(minExpPredicate, maxExpPredicate));
        }

        
        if (createdTime != null && endDate != null) {
        	predicates.add(criteriaBuilder.between(root.get("createdTime"), createdTime, endDate));
        }
        
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));
        
        if (predicates != null && !predicates.isEmpty()) {
            Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
            criteriaQuery.where(predicateArray);
        }

        criteriaQuery.select(criteriaBuilder.count(root)); // Count the results

        TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);

        long totalCount = query.getSingleResult();

        return totalCount;

	}

	@Override
	public Page<JobsModel> getPublishedJobs(String mobileNumber, int page, int size) {
		// TODO Auto-generated method stub
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());
	    return jobRepository.findByCompanyName(mobileNumber, pageable);
	}

	@Override
	public Page<JobsModel> getAllpublishedjob(Pageable pageable) {
		// TODO Auto-generated method stub
		return jobRepository.findAll(pageable);
	}


}
