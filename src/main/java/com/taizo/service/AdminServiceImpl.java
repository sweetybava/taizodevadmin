package com.taizo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.taizo.model.*;
import com.taizo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import okhttp3.*;
import org.json.JSONObject;
import org.json.XML;

@Service("adminService")
public class AdminServiceImpl implements AdminService {

	@Autowired
	AdminRepository adminRepository;
	
	@PersistenceContext
    private EntityManager entityManager;
	
	@Autowired
	VideosRepository videosRepository;
	
	@Autowired
	AdminCallNotiRepository adminCallNotiRepository;
	
	@Autowired
	AdminRolesMappingRepository adminRolesMappingRepository;
	
	@Autowired
	AdminRolesPrevilegeMappingRepository adminRolesPrevilegeMappingRepository;
	
	@Autowired
	CfgAdminPrevilegeRepository cfgAdminPrevilegeRepository;
	
	@Autowired
	CfgAdminRolesRepository cfgAdminRolesRepository;
	
	@Autowired
	AdminAnalyticsRepository adminAnalyticsRepository;
	
    private AmazonS3 s3client;

	@Autowired
	CanLeadRepository canLeadRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	CandidateTimeLineRepository candidateTimeLineRepository;
	
	@Autowired
	EmployerTimelineRepository employerTimeLineRepository;

    @Value("${aws.access.key.id}")
    private String accessKey;
    @Value("${aws.access.key.secret}")
    private String secretKey;
	@Value("${aws.s3.sample.video.bucket}")
	private String sampleVideoBucketName;
	@Value("${aws.s3.sample.video.bucket.folder}")
	private String sampleVideoBucketFolderName;
	@Value("${aws.s3.job.video.bucket}")
	private String jobVideoBucketName;
	@Value("${aws.s3.job.video.bucket.folder}")
	private String jobVideoBucketFolderName;
	
	@Value("${aws.s3.audio.bucket}")
	private String bucketName;
	@Value("${aws.s3.admin.bucket.folder}")
	private String folderName;
	
	@Value("${aws.admin.endpointUrl}")
	private String endpointUrl;

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = new AmazonS3Client(credentials);
    }

	@Override
	public Optional<Admin> login(String username, String password) {
		Optional<Admin> customer = adminRepository.login(username, password);
		if (customer.isPresent()) {
			Admin custom = customer.get();
			custom.getUserName();
			return null;
		}

		return null;
	}
	
	public void uploadSampleVideoFileToS3Bucket(MultipartFile multipartFile, String videoFileName) throws IOException {
		ObjectMetadata data = new ObjectMetadata();
		data.setContentType(multipartFile.getContentType());
		data.setContentLength(multipartFile.getSize());
		s3client.putObject(sampleVideoBucketName+"/"+sampleVideoBucketFolderName, videoFileName, multipartFile.getInputStream(), data);
	}

	public void insertSampleVideo(String vid, String vlink, String vdesc, String vtitle) {
		SampleVideosModel sampleVideosModel = new SampleVideosModel();
		sampleVideosModel.setId(vid);
		sampleVideosModel.setV_link(vlink);
		sampleVideosModel.setV_desc(vdesc);
		sampleVideosModel.setV_title(vtitle);
		videosRepository.save(sampleVideosModel);
	}
	
	public void uploadJobVideoFileToS3Bucket(MultipartFile multipartFile, String videoFileName) throws IOException {
		ObjectMetadata data = new ObjectMetadata();
		data.setContentType(multipartFile.getContentType());
		data.setContentLength(multipartFile.getSize());
		s3client.putObject(jobVideoBucketName+"/"+jobVideoBucketFolderName, videoFileName, multipartFile.getInputStream(), data);
	}

	public void insertJobVideo(String vid, String vlink) {
		/*
		 * SampleVideosModel sampleVideosModel = new SampleVideosModel();
		 * sampleVideosModel.setId(vid); sampleVideosModel.setV_link(vlink);
		 * sampleVideosModel.setV_desc(vdesc); sampleVideosModel.setV_title(vtitle);
		 * videosRepository.save(sampleVideosModel);
		 */
	}

	@Override
	public Page<AdminCallNotiModel> getAllAdminCallNotifications(Pageable pageable) {
		 return adminCallNotiRepository.findAll(pageable);
	}

	@Override
	public List<Admin> getAllAdmins() {
		 return adminRepository.findAll();
	}

	@Override
	public Admin loginDetails(String emailId, String password) {
		 Admin admin = adminRepository.findByEmailIdAndPassword(emailId, password);
	        if (admin == null) {
	            throw new RuntimeException("Invalid credentials");
	        }
	        return admin;
	}

	@Override
	public List<CfgAdminRolesModel> getAdminRoles(Long adminId) {
		   Admin admin = adminRepository.findById(adminId).orElse(null);
	        if (admin == null) {
	            throw new RuntimeException("Admin not found");
	        }
	        List<CfgAdminRolesModel> roles = new ArrayList<>();
	        List<AdminRolesMappingModel> roleMappings = adminRolesMappingRepository.findByAdmin(admin);
	        for (AdminRolesMappingModel mapping : roleMappings) {
	            roles.add(mapping.getRoleId());
	        }
	        return roles;
	}

	@Override
	public List<AdminRolesPrevilegeMappingModel> getPrivilegesByRoleId(Long roleId) {
		 return adminRolesPrevilegeMappingRepository.findByRoleId_Id(roleId);
	}

	@Override
	public List<CfgAdminPrevilegeModel> getAllPrivileges() {
        return cfgAdminPrevilegeRepository.findAll();

	}

	@Override
	public Admin findAdminByEmail(String emailId) {
		return adminRepository.findByEmailId(emailId);
	}

	public String fetchDataFromExotelApi( int page, int pageSize) {
		
		String apiKey = "b3f23bf1a7b73c73e9fefde5e4501a609e1b8c7bdf692406";
		String apiToken = "a25ea4fc5d397cdf54989d67248f3da52ba70c3211aa44e0";
			String credentials = apiKey + ":" + apiToken;
			String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes());
			
			
	        OkHttpClient client = new OkHttpClient();

	        // Create a dynamic URL with query parameters
	        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.exotel.com/v1/Accounts/taizo1/Calls").newBuilder();
	     

	    /*    if (sid != null && !sid.isEmpty()) {
	        String[] sidArray = sid.split(",");

	        for (String sids : sidArray) {
	            // Clear any previous query parameters
	            urlBuilder.removeAllQueryParameters("Sid");

	            // Add sid parameter for the current sid
	            urlBuilder.addQueryParameter("Sid", sid);*/
	        

	        // Add page and pageSize parameters
	        urlBuilder.addQueryParameter("Page", String.valueOf(page));
	        urlBuilder.addQueryParameter("PageSize", String.valueOf(pageSize));
	        

	        String url = urlBuilder.build().toString();

	        Request request = new Request.Builder()
	            .url(url)
	            .get()
	            .addHeader("Authorization", "Basic "+base64Credentials)
	            .build();

	        try (Response response = client.newCall(request).execute()) {
	            System.out.print(response);
	            if (response.isSuccessful()) {
	                // Handle and parse the response body here
	                String xmlResponse = response.body().string();

	                // Convert the XML to JSON
	                JSONObject jsonObject = XML.toJSONObject(xmlResponse);
	                String jsonResponse = jsonObject.toString();

	                return jsonResponse;
	            } else {
	                // Handle unsuccessful response (e.g., error handling)
	                return "Error: " + response.code();
	            }
	        } catch (Exception e) {
	            // Handle exceptions
	            e.printStackTrace();
	            return "Error: " + e.getMessage();
	        }
	    }
	
	

	@Override
	 public Page<AdminCallNotiModel> fetchDataBySidsPaginated(List<String> sid, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return adminCallNotiRepository.findBySidIn(sid, pageable);
	}

	@Override
	public String uploadProfilePic(MultipartFile pic, byte[] bytes) {
        String fileUrl = "";
        File file = null;

        // Define the directory path
        String tmpDirectoryPath = "/tmp/";

        try {
            // Create the directory if it doesn't exist
            File tmpDirectory = new File(tmpDirectoryPath);
            if (!tmpDirectory.exists()) {
                tmpDirectory.mkdirs();
            }

            // Create the file in the directory
            file = new File(tmpDirectory, generateFileName(pic));

            // Create a FileOutputStream
            try (FileOutputStream iofs = new FileOutputStream(file)) {
                iofs.write(bytes);
                iofs.close();

                String path = folderName + "/" + "ProfilePictures" + "/";
                String fileName = generateFileName(pic);
                String picpath = path + fileName;

                fileUrl = endpointUrl + "/" + "ProfilePictures" + "/" + fileName;
                uploadFileToS3Bucket(picpath, file);
                file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileUrl;
    }

    private void uploadFileToS3Bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
    }

    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }

	@Override
	public boolean numberExistsInModels(long number) {
		 boolean existsInCanLead = canLeadRepository.existsInCanLead(number);
	        boolean existsInCandidate = candidateRepository.existsInCandidate(number);

	        
	        return existsInCanLead || existsInCandidate;
	}


	@Override
	public Page<EmployerTimeline> findByEmpFilters(String eventName, Integer empId, Integer empLeadId, Date startDate,
													Date endDate, Pageable pageable) {
		
		return employerTimeLineRepository.findEmpByFilters(eventName, empId,empLeadId, startDate, endDate, pageable);
	}

	//@Override
	public List<Map<String, Object>> filterAdminAnalyticsList(Long adminId, String module, Timestamp createdOn, String dateFilter) {
	    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	    CriteriaQuery<AdminAnalyticsModel> criteriaQuery = criteriaBuilder.createQuery(AdminAnalyticsModel.class);
	    Root<AdminAnalyticsModel> root = criteriaQuery.from(AdminAnalyticsModel.class);

	    List<Predicate> predicates = new ArrayList<>();

	    // Filter by adminId if it's not equal to 1
	    if (adminId != 1) {
	        predicates.add(criteriaBuilder.equal(root.get("adminId"), adminId));
	    }

	    // Filter by module
	    if ("Employer".equals(module) || "JobSeeker".equals(module)) {
	        predicates.add(criteriaBuilder.equal(root.get("module"), module));
	    }

	    // Filter by createdOn based on the date range
	    if ("today".equals(dateFilter)) {
	        LocalDateTime today = LocalDateTime.now();
	        LocalDateTime startDateTime = today.with(LocalTime.MIN);
	        LocalDateTime endDateTime = today.with(LocalTime.MAX);

	        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdOn"), Timestamp.valueOf(startDateTime)));
	        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdOn"), Timestamp.valueOf(endDateTime)));
	    } else if ("oneweek".equals(dateFilter)) {
	        LocalDateTime today = LocalDateTime.now();
	        LocalDateTime startDateTime = today.minusWeeks(1).with(LocalTime.MIN);
	        LocalDateTime endDateTime = today.with(LocalTime.MAX);

	        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdOn"), Timestamp.valueOf(startDateTime)));
	        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdOn"), Timestamp.valueOf(endDateTime)));
	    } else if ("onemonth".equals(dateFilter)) {
	        LocalDateTime today = LocalDateTime.now();
	        LocalDateTime startDateTime = today.minusMonths(1).with(LocalTime.MIN);
	        LocalDateTime endDateTime = today.with(LocalTime.MAX);

	        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdOn"), Timestamp.valueOf(startDateTime)));
	        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdOn"), Timestamp.valueOf(endDateTime)));
	    }

	    // Build the final predicate
	    Predicate finalPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
	    criteriaQuery.where(finalPredicate);

	    List<Map<String, Object>> formattedResult = entityManager.createQuery(criteriaQuery)
	    	    .getResultList()
	    	    .stream()
	    	    .map(model -> {
	    	        Map<String, Object> map = new HashMap<>();
	    	        BeanInfo beanInfo;
	    	        try {
	    	            beanInfo = Introspector.getBeanInfo(model.getClass());
	    	            for (PropertyDescriptor propertyDesc : beanInfo.getPropertyDescriptors()) {
	    	                String propertyName = propertyDesc.getName();
	    	                if (!propertyName.equals("class")) {  // Exclude the "class" property
	    	                    Object propertyValue = propertyDesc.getReadMethod().invoke(model);
	    	                    map.put(propertyName, propertyValue);
	    	                }
	    	            }
	    	        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
	    	            e.printStackTrace();  // Handle the exception appropriately
	    	        }

	    	        return map;
	    	    })
	    	    .collect(Collectors.toList());


	    return formattedResult;
	}

	@Override
	public Page<CandidateTimeLine> findByFilters(String eventName, Integer canId, Integer canLeadId, Long facebookId,
	        Long seniorCanId, Long seniorCanLeadId, Date startDate,
	        Date endDate, int page, int size) {
	    return candidateTimeLineRepository.findByFilters(eventName, canId, canLeadId, facebookId,
	               Optional.ofNullable(seniorCanId),
	               Optional.ofNullable(seniorCanLeadId),
	               startDate, endDate, PageRequest.of(page, size));
	}




}

