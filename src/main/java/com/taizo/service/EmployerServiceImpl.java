package com.taizo.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.CandidateModel;
import com.taizo.model.CfgEmployerDocumentsModel;
import com.taizo.model.EmpJobRatingsModel;
import com.taizo.model.EmpPlacementPlanDetailsModel;
import com.taizo.model.EmpProformaInvoiceModel;
import com.taizo.model.EmployerCallModel;
import com.taizo.model.EmployerDocumentsModel;
import com.taizo.model.EmployerFieldLead;
import com.taizo.model.EmployerModel;
import com.taizo.model.EmployerPaymentModel;
import com.taizo.model.LeadModel;
import com.taizo.model.PlansModel;
import com.taizo.repository.CfgEmployerDocumentsRepository;
import com.taizo.repository.EmpJobRatingsRepository;
import com.taizo.repository.EmpPlacementPlanDetailsRepository;
import com.taizo.repository.EmployerCallRepository;
import com.taizo.repository.EmployerDocumentsRepository;
import com.taizo.repository.EmployerFieldLeadRepository;
import com.taizo.repository.EmployerLeadRepository;
import com.taizo.repository.EmployerPaymentRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.LeadRepository;

@Service("employerService")
public class EmployerServiceImpl implements EmployerService {

	@Autowired
	EmployerRepository employerRepository;
	
	@Autowired
	EmployerFieldLeadRepository employerFieldLeadRepository;
	
	@Autowired
	EmployerLeadRepository employerLeadRepository;
	
	@Autowired
	LeadRepository leadRepository;
	
	@Autowired
	EmpPlacementPlanDetailsRepository empPlacementPlanDetailsRepository;
	
	@Autowired
	EmployerPaymentRepository employerpaymentRepository;
	
	@Autowired
	EmpJobRatingsRepository empJobRatingsRepository;
	
	@Autowired
	EmployerCallRepository employerCallRepository;

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
	
	@PersistenceContext
    private EntityManager entityManager;
	
	@Value("${aws.user.resume.endpointUrl}")
	private String resumefolder;
	
	@Value("${aws.s3.bucket.user.resumes.folder}")
	private String folder;

	private String awsS3AudioBucket;
	
	@Value("${aws.s3.region}")
	private String region;
	
	@Autowired
	CfgEmployerDocumentsRepository cfgEmployerDocumentsRepository;
	
	@Autowired
	EmployerDocumentsRepository employerDocumentsRepository;

	private static final Logger logger = LoggerFactory.getLogger(EmployerServiceImpl.class);
	
	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		this.s3client = new AmazonS3Client(credentials);
	}
	
	@Autowired
	public void AmazonS3ClientServiceImpl(Region awsRegion, AWSCredentialsProvider awsCredentialsProvider,
			String awsS3AudioBucket) {
		this.s3client = AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(region)
				.build();
		this.awsS3AudioBucket = awsS3AudioBucket;
	}

	@Override
	public EmployerModel updateApprovalStatus(int id, String status) {
		// TODO Auto-generated method stub

		Optional<EmployerModel> optional = employerRepository.findById(id);
		if (!optional.isPresent()) {
		}
		EmployerModel existing = optional.get();
		existing.setApprovalStatus(status);

		existing = employerRepository.save(existing);

		return existing;
	}

	@Override
	public EmployerModel updateEmployer(int id, EmployerModel employer) throws ResourceNotFoundException {
		// TODO Auto-generated method stub

		Optional<EmployerModel> optional = employerRepository.findById(id);
		if (!optional.isPresent()) {
			throw new ResourceNotFoundException("Employer not found.");
		}
		EmployerModel e = optional.get();

		e.setCategory(employer.getCategory());
		e.setIndustry(employer.getIndustry());
		e.setCompanyName(employer.getCompanyName());
		e.setContactPersonName(employer.getContactPersonName());
		e.setMobileCountryCode(employer.getMobileCountryCode());
		e.setPhoneCountryCode(employer.getPhoneCountryCode());
		e.setMobileNumber(employer.getMobileNumber());
		e.setPhone(employer.getPhone());
		e.setAddress(employer.getAddress());
		e.setCity(employer.getCity());
		e.setState(employer.getState());
		e.setCountry(employer.getCountry());
		e.setPincode(employer.getPincode());
		e.setWebsiteUrl(employer.getWebsiteUrl());
		e.setLinkedlnURl(employer.getLinkedlnURl());
		e.setReference(employer.getReference());
		e.setRegProofNumber(employer.getRegProofNumber());
		e.setTaxNumber(employer.getTaxNumber());
		e.setTaxDoc(employer.getTaxDoc());

		e = employerRepository.save(e);

		return e;
	}

	@Override
	public Optional<EmployerModel> login(String emailId, String password) {
		// TODO Auto-generated method stub
		Optional<EmployerModel> employer = employerRepository.login(emailId, password);
		if (employer.isPresent()) {
			String token = UUID.randomUUID().toString();
			EmployerModel custom = employer.get();
			custom.setToken(token);
			employerRepository.save(custom);
			return employerRepository.findByToken(token);
		}
		return null;
	}

	@Override
	public Optional<EmployerModel> findLogout(String token) {
		Optional<EmployerModel> employer = employerRepository.findByToken(token);
		if (employer.isPresent()) {
			EmployerModel custom = employer.get();
			custom.setToken("");
			employerRepository.save(custom);

			return null;
		}
		return null;
	}

	@Override
	public Optional<EmployerModel> findEmployerByResetToken(String token) {
		// TODO Auto-generated method stub
		return employerRepository.findByToken(token);
	}

	@Override
	public void save(EmployerModel employer) {
		// TODO Auto-generated method stub
		employerRepository.save(employer);

	}

	@Override
	public String uploadCompanyLogo(MultipartFile photo, byte[] content) throws IOException {
		// TODO Auto-generated method stub
		String fileUrl = "";
		File file = null;
		try {
			file = new File("/tmp/" + photo);
		} catch (Exception e) {
			e.printStackTrace();

		} // file.canWrite();
		// file.canRead();
		FileOutputStream iofs = null;
		try {
			iofs = new FileOutputStream(file);
			iofs.write(content);
			iofs.close();

			String path = folderName + "/" + "CompanyLogo" + "/";
			String fileName = generateFileName(photo);
			String picpath = path + fileName;

			fileUrl = endpointUrl + "/" + "CompanyLogo" + "/" + fileName;
			uploadFileTos3bucket(picpath, file);
			file.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileUrl;
	}

	@Override
	public String uploadFile(MultipartFile multipartFile, byte[] content) {
		// TODO Auto-generated method stub
		String fileUrl = "";
		File file = null;
		try {
			file = new File("/tmp/" + multipartFile);
		} catch (Exception e) {
			e.printStackTrace();

		}
		FileOutputStream iofs = null;
		try {
			iofs = new FileOutputStream(file);
			iofs.write(content);
			iofs.close();

			String path = folderName + "/" + "RegistrationProof" + "/";
			String fileName = generateFileName(multipartFile);
			String proofpath = path + fileName;
			fileUrl = endpointUrl + "/" + "RegistrationProof" + "/" + fileName;
			uploadFileTos3bucket(proofpath, file);
			file.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileUrl;

	}

	private String generateFileName(MultipartFile multiPart) {
		return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
	}

	private void uploadFileTos3bucket(String fileName, File file) {
		s3client.putObject(
				new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
	}

	@Override
	public boolean deleteCompanyLogo(String image) {
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
	public EmployerModel updatePaymentStatus(int employerId, String paymentStatus) {
		// TODO Auto-generated method stub

		Optional<EmployerModel> optional = employerRepository.findById(employerId);
		if (!optional.isPresent()) {
		}
		EmployerModel existing = optional.get();
		existing.setPaymentStatus(paymentStatus);
		existing = employerRepository.save(existing);
		return existing;
	}

	@Override
	public EmployerModel updateDeactivationStatus(int employerId, String status) {
		// TODO Auto-generated method stub
		Optional<EmployerModel> optional = employerRepository.findById(employerId);
		if (!optional.isPresent()) {
		}
		EmployerModel existing = optional.get();
		existing.setDeactivated(true);
		existing = employerRepository.save(existing);
		return existing;
	}
	
	@Override
	public List<Map<String,Object>> filteremployer(String industry,String noOfEmployees, String city, int plan
			) {
		return employerRepository.filterEmployer(industry,noOfEmployees,city,plan);
	}

	@Override
	public Page<EmployerModel> getAllEmployers(Pageable pageable) {
		return employerRepository.findAll(pageable);
	}

	@Override
	public long getTotalEmployersCount() {
		 return employerRepository.count();
	}

	@Override
	public Optional<EmployerModel> findByIdOrMobileNumberOrWhatsappNumber(int id, long mobileNumber,
			long whatsappNumber) {
		 return employerRepository.findByIdOrMobileNumberOrWhatsappNumber(id, mobileNumber, whatsappNumber);
	}

	@Override
	public Page<EmployerModel> getAllEmployersOrderedByCreatedTimeDesc(Pageable pageable) {
		return employerRepository.findAllByOrderByCreatedTimeDesc(pageable);
    
	}

	@Override
	public Page<EmployerPaymentModel> findByEmployerIdOrderByCreatedTimeDesc(int employerId, Pageable pageable) {
		return employerpaymentRepository.findByEmployerIdOrderByCreatedTimeDesc(employerId, pageable);
    }

	@Override
	public Page<EmployerPaymentModel> getPaymentsByEmployerId(int employerId, Pageable pageable) {
		  return employerpaymentRepository.findByEmployerIdOrderByCreatedTimeDesc(employerId, pageable);
    
	}

	@Override
	public Page<EmployerPaymentModel> getAllPayments(Pageable pageable) {
		return employerpaymentRepository.findAllByOrderByCreatedTimeDesc(pageable);
	}

	@Override
	public long getTotalEmployerPaymentCount(int employerId) {
		return employerpaymentRepository.countByEmployerId(employerId);
	}

	@Override
	public long getTotalPaymentCount() {
		return employerpaymentRepository.count();
	}

	@Override
	public Page<EmpJobRatingsModel> findByRatingCount(int ratingCount, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());
	    return empJobRatingsRepository.findByRatingCount(ratingCount, pageable);

	}

	@Override
	public Page<EmpJobRatingsModel> getAllRatingCount(Pageable pageable) {
		// TODO Auto-generated method stub
		return empJobRatingsRepository.findAll(pageable);
	}

 
	@Override
	public Page<EmployerCallModel> getAllCallRegistry(Pageable pageable) {
        return employerCallRepository.findAll(pageable);
    }
	
	@Override
	public Page<EmployerCallModel> findByJid(int jId,  int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("callTime").descending());
		 return employerCallRepository.findByjId(jId,pageable);
	}

	@Override
	public Page<EmployerModel> findByCompanyNameContainingIgnoreCase(String companyName, Pageable pageable) {
		return employerRepository.findByCompanyNameContainingIgnoreCase(companyName, pageable);
	}

	@Override
	public List<?> getAnalyticsByTime(String time) {
		// TODO Auto-generated method stub
		return employerRepository.findByAnalystics(time);
	}

	@Override
	public List<Map<String, Object>> filterEmployer(Integer employerId, String companyName, String industry,
			String noOfEmployees, String city, String area,String contactNumber, Integer page, Integer pageSize, Date startDate,
			Date endDate) {
		 CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	        CriteriaQuery<EmployerModel> criteriaQuery = criteriaBuilder.createQuery(EmployerModel.class);
	        Root<EmployerModel> root = criteriaQuery.from(EmployerModel.class);
	        
	        List<Predicate> predicates = new ArrayList<>();
	        
	        if (employerId != 0) {
	        	predicates.add(criteriaBuilder.equal(root.get("id"),employerId));
	        }
	        if (companyName != null) {
	        	predicates.add(criteriaBuilder.like(root.get("companyName"), "%" + companyName.trim() + "%"));
	        }
	  
	        if (industry != null) {
	            predicates.add(criteriaBuilder.equal(root.get("industry"), industry));  
	        }
	        
	        if (noOfEmployees != null) {
	            predicates.add(criteriaBuilder.equal(root.get("noOfEmployees"), noOfEmployees));   
	        }
	        
	        if (city != null) {
	            predicates.add(criteriaBuilder.equal(root.get("city"), city));   
	        }
	        
	        if (area != null) {
	            predicates.add(criteriaBuilder.equal(root.get("area"), area));   
	        }
	        
	        
	        if (contactNumber != null) {
	            Predicate mobileNumberPredicate = criteriaBuilder.equal(root.get("mobileNumber"), contactNumber);
	            Predicate whatsappNumberPredicate = criteriaBuilder.equal(root.get("whatsappNumber"), contactNumber);
	            predicates.add(criteriaBuilder.or(mobileNumberPredicate, whatsappNumberPredicate));
	        }
	        
	        if (startDate != null && endDate != null) {
	        	predicates.add(criteriaBuilder.between(root.get("createdTime"), startDate, endDate));
	        }

	        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));
	        predicates.add(criteriaBuilder.equal(root.get("deactivated"), false));
	        
	        if (predicates != null && predicates.size() != 0) {
				Predicate[] predicate = predicates.toArray(new Predicate[0]);
				criteriaQuery.where(predicate);
			}
	        List<EmployerModel> resultList = entityManager.createQuery(criteriaQuery)
	                .setFirstResult((page - 1) * pageSize)
	                .setMaxResults(pageSize)
	                .getResultList();

	        List<Map<String, Object>> resultMaps = new ArrayList<>();

	        for (EmployerModel employer : resultList) {
	            Map<String, Object> resultMap = new HashMap<>();
	            resultMap.put("employer", employer);

	            resultMaps.add(resultMap);
	        }

	        return resultMaps;
	}

	@Override
	public Page<LeadModel> findByEmployerLead(String emailId,Boolean fromAdmin, Integer page, Integer pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdTime").descending());
	    return leadRepository.findByFromAdmin(emailId,fromAdmin, pageable);
	}

	@Override
	public Page<LeadModel> getAllLead(Pageable pageable) {
		// TODO Auto-generated method stub
		return leadRepository.findAll(pageable);
	}

	@Override
	public List<Map<String, Object>> filterProforma(Integer employerId, String companyName, Long mobileNumber,
			Integer page, Integer size, Date startDate, Date endDate) {
		
		 CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	        CriteriaQuery<EmpProformaInvoiceModel> criteriaQuery = criteriaBuilder.createQuery(EmpProformaInvoiceModel.class);
	        Root<EmpProformaInvoiceModel> root = criteriaQuery.from(EmpProformaInvoiceModel.class);
	        
	        List<Predicate> predicates = new ArrayList<>();
	        
	        if (employerId != 0) {
	        	predicates.add(criteriaBuilder.equal(root.get("employerId"),employerId));
	        }
	        if (companyName != null) {
	        	predicates.add(criteriaBuilder.like(root.get("companyName"), "%" + companyName.trim() + "%"));
	        }
	        
	        if(mobileNumber!=0) {
	        	predicates.add(criteriaBuilder.equal(root.get("mobileNumber"),mobileNumber));
	        }
	        
	        if (startDate != null && endDate != null) {
	        	predicates.add(criteriaBuilder.between(root.get("createdTime"), startDate, endDate));
	        }

	        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));   
	     
	        if (predicates != null && predicates.size() != 0) {
				Predicate[] predicate = predicates.toArray(new Predicate[0]);
				criteriaQuery.where(predicate);
			}
	        List<EmpProformaInvoiceModel> resultList = entityManager.createQuery(criteriaQuery)
	                .setFirstResult((page - 1) * size)
	                .setMaxResults(size)
	                .getResultList();

	        List<Map<String, Object>> resultMaps = new ArrayList<>();

	        for (EmpProformaInvoiceModel invoice : resultList) {
	            Map<String, Object> resultMap = new HashMap<>();
	            resultMap.put("employer", invoice);

	            resultMaps.add(resultMap);
	        }

	        return resultMaps;
	}

	@Override
	public long filterEmployerCount(int id, String companyName, String industry, String noOfEmployees, String city,
			String area, String contactNumber, int page, int size, Date createdTime, Date endDate) {
		// TODO Auto-generated method stub
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<EmployerModel> root = criteriaQuery.from(EmployerModel.class);

        List<Predicate> predicates = new ArrayList<>();
        
        if (id != 0) {
        	predicates.add(criteriaBuilder.equal(root.get("id"),id));
        }
        if (companyName != null) {
        	predicates.add(criteriaBuilder.like(root.get("companyName"), "%" + companyName.trim() + "%"));
        }
  
        if (industry != null) {
            predicates.add(criteriaBuilder.equal(root.get("industry"), industry));  
        }
        
        if (noOfEmployees != null) {
            predicates.add(criteriaBuilder.equal(root.get("noOfEmployees"), noOfEmployees));   
        }
        
        if (city != null) {
            predicates.add(criteriaBuilder.equal(root.get("city"), city));   
        }
        
        if (area != null) {
            predicates.add(criteriaBuilder.equal(root.get("area"), area));   
        }
        
        
        if (contactNumber != null) {
            Predicate mobileNumberPredicate = criteriaBuilder.equal(root.get("mobileNumber"), contactNumber);
            Predicate whatsappNumberPredicate = criteriaBuilder.equal(root.get("whatsappNumber"), contactNumber);
            predicates.add(criteriaBuilder.or(mobileNumberPredicate, whatsappNumberPredicate));
        }
        
        if (createdTime != null && endDate != null) {
        	predicates.add(criteriaBuilder.between(root.get("createdTime"), createdTime, endDate));
        }

        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));
        predicates.add(criteriaBuilder.equal(root.get("deactivated"), false));

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
	public long filterProformaCount(int employerId, String companyName, long mobileNumber, int page, int size,
			Date createdTime, Date endDate) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<EmpProformaInvoiceModel> root = criteriaQuery.from(EmpProformaInvoiceModel.class);

        List<Predicate> predicates = new ArrayList<>();
        
        if (employerId != 0) {
        	predicates.add(criteriaBuilder.equal(root.get("employerId"),employerId));
        }
        if (companyName != null) {
        	predicates.add(criteriaBuilder.like(root.get("companyName"), "%" + companyName.trim() + "%"));
        }
        
        if(mobileNumber!=0) {
        	predicates.add(criteriaBuilder.equal(root.get("mobileNumber"),mobileNumber));
        }
        
        if (createdTime != null && endDate != null) {
        	predicates.add(criteriaBuilder.between(root.get("createdTime"), createdTime, endDate));
        }
  
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));
        
        if (predicates != null && predicates.size() != 0) {
			Predicate[] predicate = predicates.toArray(new Predicate[0]);
			criteriaQuery.where(predicate);
        }
        
        criteriaQuery.select(criteriaBuilder.count(root)); // Count the results

        TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);

        long totalCount = query.getSingleResult();

        return totalCount;

	}

	@Override
	public Page<EmpPlacementPlanDetailsModel> getUnPublishedJobsByEmployer(Integer employerId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());
	    return empPlacementPlanDetailsRepository.findByEmployerId(employerId, pageable);
	}

	@Override
	public Page<EmpPlacementPlanDetailsModel> getAllunpublishedjob(Pageable pageable) {
		// TODO Auto-generated method stub
		return empPlacementPlanDetailsRepository.findAllActive(pageable);
	}

	@Override
	public Page<LeadModel> findByEmployerLeads(Long mobileNumber, boolean fromAdmin, Integer page, Integer pageSize) {
		// TODO Auto-generated method stub
		Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdTime").descending());
	    return employerLeadRepository.findBymobileNumber(mobileNumber,fromAdmin, pageable);
	}
	
	@Override
	public Page<LeadModel> findByCompanyName(String companyName, boolean fromAdmin, Integer page, Integer pageSize) {
		// TODO Auto-generated method stub
		Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdTime").descending());
	    return employerLeadRepository.findByCompanyName(companyName,fromAdmin, pageable);
	}
	
	@Override
	public Page<LeadModel> findByIndustry(String industry, boolean fromAdmin, Integer page, Integer pageSize) {
		// TODO Auto-generated method stub
		Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdTime").descending());
	    return employerLeadRepository.findByIndustry(industry,fromAdmin, pageable);
	}

	@Override
	public Page<LeadModel> findByCity(String city, boolean fromAdmin, Integer page, Integer pageSize) {
		// TODO Auto-generated method stub
		Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdTime").descending());
	    return employerLeadRepository.findByCity(city,fromAdmin, pageable);
	}
	
	@Override
	public Page<LeadModel> findByCreatedTime(Date startDate,Date endDate, boolean fromAdmin, Integer page, Integer pageSize) {
		// TODO Auto-generated method stub
		Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdTime").descending());
	    return employerLeadRepository.findByCreatedTime(startDate,endDate,fromAdmin, pageable);
	}

	@Override
	public String uploadLeadPhotoToS3Bucket1(MultipartFile multipartFile, String companyName, boolean enablePublicReadAccess) {
	    String fileUrl = "";

	    try {
	        String path = folder + "/" + companyName + "/" + "employerFieldLead" + "/";
	        String fileName = generateFileName(multipartFile);
	        String videopath = path + fileName;
	        fileUrl = resumefolder + "/" + companyName + "/" + "employerFieldLead" + "/" + fileName;

	        // creating the file in the server (temporarily)
	        File file = new File(System.getProperty("java.io.tmpdir") +
	                System.getProperty("file.separator") +
	                fileName);
	        FileOutputStream fos = new FileOutputStream(file);
	        fos.write(multipartFile.getBytes());
	        fos.close();

	        PutObjectRequest putObjectRequest = new PutObjectRequest(this.awsS3AudioBucket, videopath, file);

	        if (enablePublicReadAccess) {
	            putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
	        }

	        this.s3client.putObject(putObjectRequest);
	        
	        // removing the file created in the server
	        file.delete();

	        // Update leadImageLink in the database
	        EmployerFieldLead existingUser = employerFieldLeadRepository.findByCompanyName(companyName);
	        if (existingUser != null) {
	            existingUser.setLeadImageLink(fileUrl);
	            employerFieldLeadRepository.save(existingUser);
	        }

	    } catch (IOException | AmazonServiceException ex) {
	        logger.error("error [" + ex.getMessage() + "] occurred while uploading [" + fileUrl + "] ");
	        fileUrl = "error [" + ex.getMessage() + "] occurred while uploading [" + fileUrl + "] ";
	    }

	    return fileUrl;
	}

	@Override
	public List<Map<String, Object>> filteremployerFieldList(String companyName, String area, String city,
			Date createdTime,  Date endDate, int pages, int size) {
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EmployerFieldLead> criteriaQuery = criteriaBuilder.createQuery(EmployerFieldLead.class);
        Root<EmployerFieldLead> root = criteriaQuery.from(EmployerFieldLead.class);

        List<Predicate> predicates = new ArrayList<>();
        
        if (companyName != null) {
            predicates.add(criteriaBuilder.like(root.get("companyName"),"%"+ companyName+"%"));
        }
        if (area != null) {
            predicates.add(criteriaBuilder.like(root.get("area"),"%"+ area+"%"));
        }
        if (city != null) {
            predicates.add(criteriaBuilder.like(root.get("city"),"%"+ city+"%"));
        }
        if (createdTime != null && endDate != null) {
        	predicates.add(criteriaBuilder.between(root.get("createdTime"), createdTime, endDate));
        }
         criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));
        
        if (predicates != null && predicates.size() != 0) {
			Predicate[] predicate = predicates.toArray(new Predicate[0]);
			criteriaQuery.where(predicate);
		}
        
        List<EmployerFieldLead> resultList = entityManager.createQuery(criteriaQuery)
                .setFirstResult((pages - 1) * size)
                .setMaxResults(size)
                .getResultList();

        List<Map<String, Object>> resultMaps = new ArrayList<>();

        for (EmployerFieldLead employerFieldLead : resultList) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("employerFieldLead", employerFieldLead);

            resultMaps.add(resultMap);
        }

        return resultMaps;
	}

	@Override
	public long filteremployerFieldCount(String companyName, String area, String city, Date createdTime,
			 Date endDate) {
		// TODO Auto-generated method stub
		 CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
	        Root<EmployerFieldLead> root = criteriaQuery.from(EmployerFieldLead.class);

	        List<Predicate> predicates = new ArrayList<>();
	        
	        if (companyName != null) {
	            predicates.add(criteriaBuilder.like(root.get("companyName"),"%"+ companyName+"%"));
	        }
	        if (area != null) {
	            predicates.add(criteriaBuilder.like(root.get("area"),"%"+ area+"%"));
	        }
	        if (city != null) {
	            predicates.add(criteriaBuilder.like(root.get("city"),"%"+ city+"%"));
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
	public Page<LeadModel> findAll(Specification<LeadModel> spec, Pageable pageable) {
		// TODO Auto-generated method stub
		return employerLeadRepository.findAll(spec,pageable);
	}

	@Override
	public List<EmployerDocumentsModel> getDocumentsByEmpId(int empId) {
		// TODO Auto-generated method stub
		return employerDocumentsRepository.findByEmpIdAndActiveTrue(empId);
	}

	@Override
	public List<CfgEmployerDocumentsModel> getActiveDocuments() {
		// TODO Auto-generated method stub
		return cfgEmployerDocumentsRepository.findByActiveTrue();
	}


	
	
		}
