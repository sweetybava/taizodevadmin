package com.taizo.controller.admin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.taizo.model.Admin;
import com.taizo.model.CandidateTimeLine;
import com.taizo.repository.AdminRepository;
import com.taizo.repository.CandidateTimeLineRepository;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.utils.FreeMarkerUtils;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.itextpdf.io.exceptions.IOException;
import com.taizo.model.MidSeniorLevelCandidateLeadModel;
import com.taizo.model.MidSeniorSourcingModel;
import com.taizo.repository.MidSeniorLevelCandidateLeadRepository;
import com.taizo.repository.MidSeniorSourcingRepository;
import com.taizo.service.MidSeniorCandidateService;


@CrossOrigin
@RestController
@RequestMapping("/admin")
public class AdminMidSeniorController {
	
	 private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	MidSeniorSourcingRepository midSeniorSourcingRepository;
	
	@Autowired
	MidSeniorLevelCandidateLeadRepository midSeniorLevelCandidateLeadRepository;

	@Autowired
	CandidateTimeLineRepository candidateTimeLineRepository;

	@Autowired
	AdminRepository adminRepository;
	
	@Autowired
	MidSeniorCandidateService midSeniorCandidateService;

	@Autowired
	private FreeMarkerUtils freeMarkerUtils;

	@Autowired
	AmazonSESMailUtil amazonSESMailUtil;

	
	@Value("${aws.s3.bucket.user.resumes.folder}")
	private String folder;
	
	@Value("${aws.access.key.id}")
	private String accessKey;
	@Value("${aws.access.key.secret}")
	private String awssecretKey;
	@Value("${aws.s3.audio.bucket}")
	private String bucketName;
	
	private AmazonS3 s3client;
	
	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.awssecretKey);
		this.s3client = new AmazonS3Client(credentials);
	}
	
	private AmazonSimpleEmailService sesClient = null;

    public void AmazonSESMailUtil(AmazonSimpleEmailService sesClient) {
        this.sesClient = sesClient;
    }
	


	@PutMapping("/midSeniorSourcing")
	public ResponseEntity <?> creteSourcing (@RequestBody MidSeniorSourcingModel midSeniorSourcingModel) {

		HashMap<String, String> map = new HashMap<>();

		try {
		Optional<Admin> admin = adminRepository.findById(midSeniorSourcingModel.getAdminId());		
		Admin a = admin.orElse(null);

	        if (a == null) {
	            map.put("code", "404");
	            map.put("message", "Admin not found");
	            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
	        }

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String formattedDate = dateFormat.format(currentDate);
		
		Optional<MidSeniorSourcingModel> midSeniorSourcingModel1 = midSeniorSourcingRepository.findByMobileNumber(midSeniorSourcingModel.getMobileNumber());

		MidSeniorLevelCandidateLeadModel midSeniorLevelCandidateLeadModel =midSeniorLevelCandidateLeadRepository.findByMobileNumber(midSeniorSourcingModel.getMobileNumber());

			
		
			if (midSeniorSourcingModel1.isPresent()) {
				
				MidSeniorSourcingModel existingMidSenior = midSeniorSourcingModel1.get();
				
				existingMidSenior.setMobileNumber(midSeniorSourcingModel.getMobileNumber());
				existingMidSenior.setFirstName(midSeniorSourcingModel.getFirstName());
				existingMidSenior.setLastName(midSeniorSourcingModel.getLastName());
				existingMidSenior.setEmailId(midSeniorSourcingModel.getEmailId());
				existingMidSenior.setAppliedJobrole(midSeniorSourcingModel.getAppliedJobrole());
				existingMidSenior.setJobrole(midSeniorSourcingModel.getJobrole());
				existingMidSenior.setExperienceInYears(midSeniorSourcingModel.getExperienceInYears());
				existingMidSenior.setExperienceInMonths(midSeniorSourcingModel.getExperienceInMonths());
				existingMidSenior.setSkills(midSeniorSourcingModel.getSkills());
				existingMidSenior.setCurrentLocation(midSeniorSourcingModel.getCurrentLocation());
				existingMidSenior.setPreferredJobLocation(midSeniorSourcingModel.getPreferredJobLocation());
				existingMidSenior.setAdminPreferredCompany(midSeniorSourcingModel.getAdminPreferredCompany());
				existingMidSenior.setAdminId(midSeniorSourcingModel.getAdminId());

				midSeniorSourcingRepository.save(existingMidSenior);

				CandidateTimeLine candidateTimeLine=new CandidateTimeLine();
				candidateTimeLine.setFacebookId(0L);
				candidateTimeLine.setCanId(0);
				candidateTimeLine.setSeniorCanId(0L);
				candidateTimeLine.setCanLeadId(0);
				candidateTimeLine.setEventName("Profile Update");
				candidateTimeLine.setEventDescription("profile updated by " +a.getUserName() + "</b> on " + formattedDate);
				candidateTimeLine.setSeniorCanLeadId(existingMidSenior.getId());
				candidateTimeLineRepository.save(candidateTimeLine);

				map.put("code", "200");
				map.put("message", "updated Successfully");
				return new ResponseEntity<>(map, HttpStatus.OK);
			}   else if (midSeniorLevelCandidateLeadModel != null) {
	            // If exists in midSeniorLevelCandidateLeadModel, do not allow create or update
	            map.put("code", "400");
	            map.put("message", "Error: Record already exists in midSeniorLevelCandidateLeadModel");
	            return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
	        }
			else {

				MidSeniorSourcingModel newMidSenior = new MidSeniorSourcingModel();

				newMidSenior.setMobileNumber(midSeniorSourcingModel.getMobileNumber());
				newMidSenior.setFirstName(midSeniorSourcingModel.getFirstName());
				newMidSenior.setLastName(midSeniorSourcingModel.getLastName());
				newMidSenior.setEmailId(midSeniorSourcingModel.getEmailId());
				newMidSenior.setAppliedJobrole(midSeniorSourcingModel.getAppliedJobrole());
				newMidSenior.setJobrole(midSeniorSourcingModel.getJobrole());
				newMidSenior.setExperienceInYears(midSeniorSourcingModel.getExperienceInYears());
				newMidSenior.setExperienceInMonths(midSeniorSourcingModel.getExperienceInMonths());
				newMidSenior.setSkills(midSeniorSourcingModel.getSkills());
				newMidSenior.setCurrentLocation(midSeniorSourcingModel.getCurrentLocation());
				newMidSenior.setPreferredJobLocation(midSeniorSourcingModel.getPreferredJobLocation());
				newMidSenior.setAdminPreferredCompany(midSeniorSourcingModel.getAdminPreferredCompany());
				newMidSenior.setAdminId(midSeniorSourcingModel.getAdminId());

				midSeniorSourcingRepository.save(newMidSenior);

				CandidateTimeLine candidateTimeLine=new CandidateTimeLine();
				candidateTimeLine.setFacebookId(0L);
				candidateTimeLine.setCanId(0);
				candidateTimeLine.setSeniorCanId(0L);
				candidateTimeLine.setCanLeadId(0);
				candidateTimeLine.setEventName("Lead Sourcing");
				candidateTimeLine.setEventDescription("Lead Sourced by " +a.getUserName() + "</b> on " + formattedDate);
				candidateTimeLine.setSeniorCanLeadId(newMidSenior.getId());
				candidateTimeLineRepository.save(candidateTimeLine);

				map.put("code", "200");
				map.put("message", "new Record Successfully");
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
		} catch (Exception e) {
			map.put("code", "400");
			map.put("message", "Error: " + e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);


		}
	}
		
		@PutMapping("/midSeniorSourcingResume")
		ResponseEntity<?> uploadResume(@RequestParam String mobileNumber,
		                              @RequestParam MultipartFile resume,
		                              @RequestParam Long adminId) throws IOException, java.io.IOException {
			
			 HashMap<String, Object> map = new HashMap<>();
			 
			Optional<MidSeniorSourcingModel> midSeniorSourcingModel = midSeniorSourcingRepository.findByMobileNumber(mobileNumber);

			Optional<Admin> admin = adminRepository.findById(adminId);
			
			Admin a = admin.orElse(null);
			  if (a == null) {
		            map.put("code", "404");
		            map.put("message", "Admin not found");
		            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
		        }
		    if (midSeniorSourcingModel.isPresent()) {
		    	MidSeniorSourcingModel candidate = midSeniorSourcingModel.get(); 

		        String existingResumeKey = candidate.getResumeLink();
		        try {
		            // Check if the candidate already has a resume
		            if (existingResumeKey != null && !existingResumeKey.isEmpty()) {
		                logger.info("Deleting existing resume with key: {}", existingResumeKey);
		                // Delete the existing resume from S3
		                try {
		                    s3client.deleteObject(bucketName, existingResumeKey); // Delete the old resume
		                    logger.info("Deleted existing resume successfully.");
		                } catch (AmazonServiceException e) {
		                    logger.error("S3 Delete Error: {}", e.getMessage(), e);
		                    // Handle the delete error if needed
		                    // Consider whether you want to stop execution or continue
		                }
		            }

		            String key = folder + "/" + mobileNumber + "/Resumes/" + generateFileName(resume);
		            String fileUrl = s3UploadFileAndReturnUrl(key, resume);

		            // Delete the temporary file
		            File tempFile = new File("/tmp/" + resume.getOriginalFilename());
		            if (tempFile.exists()) {
		                tempFile.delete();
		            }
		            
		            candidate.setResumeLink(fileUrl);
		            midSeniorSourcingRepository.save(candidate);
		            
		        	Date currentDate = new Date();
		    		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		    		String formattedDate = dateFormat.format(currentDate);
		            
		            CandidateTimeLine candidateTimeLine=new CandidateTimeLine();
					candidateTimeLine.setFacebookId(0L);
					candidateTimeLine.setCanId(0);
					candidateTimeLine.setSeniorCanId(0L);
					candidateTimeLine.setCanLeadId(0);
					candidateTimeLine.setEventName("Resume");
					candidateTimeLine.setEventDescription("Resume updated by " +a.getUserName() + "</b> on " + formattedDate);
					candidateTimeLine.setSeniorCanLeadId(candidate.getAdminId());
					candidateTimeLineRepository.save(candidateTimeLine);
		         
		           
		           
					map.put("statuscode", 200);
					map.put("message", " Resume Updated successfully");
					map.put("Details", candidate);
					return new ResponseEntity<>(map, HttpStatus.OK);
		        } catch (AmazonServiceException e) {
		            logger.error("S3 Error: {}", e.getMessage(), e);
		           
					map.put("statuscode", 400);
					map.put("message", "Updated failed");
					map.put("Details", candidate);
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		            
		        }
		    }

		    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Candidate not found");
		}
		
		 private String s3UploadFileAndReturnUrl(String key, MultipartFile file) throws IOException, java.io.IOException {
		        ObjectMetadata metadata = new ObjectMetadata();
		        metadata.setContentLength(file.getSize());
		        metadata.setContentType(file.getContentType());

		        try {
		            s3client.putObject(new PutObjectRequest(bucketName, key, file.getInputStream(), metadata)
		                    .withCannedAcl(CannedAccessControlList.PublicRead));
		            return s3client.getUrl(bucketName, key).toString();
		        } catch (AmazonServiceException e) {
		            logger.error("S3 Error: {}", e.getMessage(), e);
		            throw new AmazonServiceException("Failed to upload to S3: " + e.getMessage());
		        }
		    }
		   private String generateFileName(MultipartFile file) {
		        return UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
		    }
		   
		   @GetMapping("/midSeniorSourcingFilter")
		    public ResponseEntity<Map<String, Object>> midSeniorSourcingFilter(
		            @RequestParam(required = false) String firstName,
		            @RequestParam(required = false) long adminId,
		            @RequestParam(required = false) String lastName,
		            @RequestParam(required = false) String emailId,
		            @RequestParam(required = false) String mobileNumber,
		            @RequestParam(required = false) String appliedJobrole,
		            @RequestParam(required = false) String jobrole,
		            @RequestParam(required = false) String currentLocation,
		            @RequestParam(required = false) String preferredJobLocation,
		            @RequestParam(required = false) Boolean qualified,
		            @RequestParam(required = false) Boolean notQualified,
					@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") String createdTimeStart,
					@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") String createdTimeEnd,
		            @RequestParam(required = false, defaultValue = "0") Integer page,
		            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

		        Page<MidSeniorSourcingModel> result = null;
		        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdTime").descending());

		        Specification<MidSeniorSourcingModel> spec = Specification.where(null);

		        if (firstName != null) {
		            spec = spec.and((root, query, builder) -> builder.like(root.get("firstName"), "%" + firstName + "%"));
		        }
			   if (lastName != null) {
				   spec = spec.and((root, query, builder) -> builder.equal(root.get("lastName"), lastName));
			   }
			   if (emailId != null) {
				   spec = spec.and((root, query, builder) -> builder.equal(root.get("emailId"), emailId));
			   }
		        if (adminId != 0L) {
		            spec = spec.and((root, query, builder) -> builder.equal(root.get("adminId"), adminId));
		        }
			   if (mobileNumber != null) {
				   spec = spec.and((root, query, builder) -> builder.equal(root.get("mobileNumber"), mobileNumber));
			   }
			   if (appliedJobrole != null) {
				   spec = spec.and((root, query, builder) -> builder.equal(root.get("appliedJobrole"), appliedJobrole));
			   }
			   if (jobrole != null) {
				   spec = spec.and((root, query, builder) -> builder.equal(root.get("jobrole"), jobrole));
			   }
			   if (currentLocation != null) {
				   spec = spec.and((root, query, builder) -> builder.equal(root.get("currentLocation"), currentLocation));
			   }
			   if (preferredJobLocation != null) {
				   spec = spec.and((root, query, builder) -> builder.equal(root.get("preferredJobLocation"), preferredJobLocation));
			   }
			   if ( qualified!= null) {
				   spec = spec.and((root, query, builder) -> builder.equal(root.get("qualified"), qualified));
			   }

			   if (notQualified != null) {
				   spec = spec.and((root, query, builder) -> builder.equal(root.get("notQualified"), notQualified));
			   }
		        // Add other filters as needed based on the MidSeniorSourcingModel fields

			   if (createdTimeStart != null && createdTimeEnd != null) {
		            LocalDate startDate = LocalDate.parse(createdTimeStart);
		            LocalDate endDate = LocalDate.parse(createdTimeEnd);
		            Timestamp startDateTime = Timestamp.valueOf(startDate.atStartOfDay());
		            Timestamp endDateTime = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());
		            spec = spec.and((root, query, builder) -> builder.between(root.get("createdTime"), startDateTime, endDateTime));
		        }

			   // Retrieve data based on the filters
		        result = midSeniorCandidateService.findAll(spec, pageable);

		        if (result.isEmpty()) {
		            Map<String, Object> errorResponse = new HashMap<>();
		            errorResponse.put("code", 400);
		            errorResponse.put("message", "Not Found");
		            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		        }

		        Map<String, Object> successResponse = new HashMap<>();
		        successResponse.put("status", "success");
		        successResponse.put("message", "success");
		        successResponse.put("code", 200);
		        successResponse.put("data", result);
		        return ResponseEntity.ok(successResponse);
		    }

	@PutMapping("/midSeniorSourcingStatus")
	public ResponseEntity<?> updateMidSeniorStatus(@RequestParam int id,
												   @RequestParam long adminId,
												   @RequestParam boolean qualified,
												   @RequestParam boolean notQualified,
												   @RequestParam (required =false)String notes) throws IOException, MessagingException, TemplateException, java.io.IOException {

		Map<String, Object> map = new HashMap<>();

		Optional<MidSeniorSourcingModel> midSeniorSourcingModelOptional = midSeniorSourcingRepository.findById((long) id);
		Optional<Admin> adminOptional = adminRepository.findById(adminId);

		if (!midSeniorSourcingModelOptional.isPresent() || !adminOptional.isPresent()) {
			map.put("statuscode", 400);
			map.put("message", "Candidate or Admin not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

		MidSeniorSourcingModel midSeniorSourcingModel = midSeniorSourcingModelOptional.get();
		Admin admin = adminOptional.get();
		String emailId = midSeniorSourcingModel.getEmailId();

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String formattedDate = dateFormat.format(currentDate);

		midSeniorSourcingModel.setQualified(qualified);
		midSeniorSourcingModel.setNotQualified(notQualified);
		if(notes != null) {
		midSeniorSourcingModel.setNotes(notes);
		}
		midSeniorSourcingRepository.save(midSeniorSourcingModel);

		if (qualified && !notQualified) {
			// Candidate is qualified
			Map<String, Object> data = new HashMap<>();
			data.put("registrationLink", "https://profile.taizo.in/candidate-basic-details?from_id=" + adminId);
			data.put("candidateName", midSeniorSourcingModel.getFirstName() + " " + midSeniorSourcingModel.getLastName());
			data.put("signature", admin.getEmailSignature());

			String message = freeMarkerUtils.getHtml("MidSeniorRegisterationLink.html", data);

			amazonSESMailUtil.sendMidSeniorRegLinkWithoutAttachment(emailId, adminId, message);

			// Update timeline
			CandidateTimeLine candidateTimeLine = new CandidateTimeLine();
			candidateTimeLine.setCanLeadId(0);
			candidateTimeLine.setEventName("Qualified");
			candidateTimeLine.setEventDescription("Qualified By " + admin.getUserName() + " on " + formattedDate);
			candidateTimeLine.setCanId(0);
			candidateTimeLine.setFacebookId(0L);
			candidateTimeLine.setSeniorCanLeadId(midSeniorSourcingModel.getId());
			if (notes != null) {
				candidateTimeLine.setNotes(notes);
			}
			candidateTimeLineRepository.save(candidateTimeLine);


			 // move to MidSeniorLevelCandidateLeadModel table
			MidSeniorLevelCandidateLeadModel midSeniorLevelCandidateLeadModel = new MidSeniorLevelCandidateLeadModel();

			midSeniorLevelCandidateLeadModel.setAdminId(adminId);
			midSeniorLevelCandidateLeadModel.setFirstName(midSeniorSourcingModel.getFirstName());
			midSeniorLevelCandidateLeadModel.setLastName(midSeniorSourcingModel.getLastName());
			midSeniorLevelCandidateLeadModel.setEmailId(midSeniorSourcingModel.getEmailId());
			midSeniorLevelCandidateLeadModel.setMobileNumber(midSeniorSourcingModel.getMobileNumber());
			midSeniorLevelCandidateLeadModel.setExpInYears(midSeniorSourcingModel.getExperienceInYears());
			midSeniorLevelCandidateLeadModel.setExpInMonths(midSeniorSourcingModel.getExperienceInMonths());
			midSeniorLevelCandidateLeadModel.setPrefJobLocation(midSeniorSourcingModel.getPreferredJobLocation());
			midSeniorLevelCandidateLeadModel.setAdminPreferredCompany(midSeniorSourcingModel.getAdminPreferredCompany());
			midSeniorLevelCandidateLeadModel.setAppliedJobrole(midSeniorSourcingModel.getAppliedJobrole());
			midSeniorLevelCandidateLeadModel.setSkills(midSeniorSourcingModel.getSkills());
			midSeniorLevelCandidateLeadModel.setJobrole(midSeniorSourcingModel.getJobrole());
			midSeniorLevelCandidateLeadModel.setCurrentLocation(midSeniorSourcingModel.getCurrentLocation());
			midSeniorLevelCandidateLeadModel.setResumeLink(midSeniorSourcingModel.getResumeLink());

			midSeniorLevelCandidateLeadRepository.save(midSeniorLevelCandidateLeadModel);

			midSeniorSourcingRepository.delete(midSeniorSourcingModel);
			
			// Step 2: Find relevant records in candidateTimeLineRepository
			List<CandidateTimeLine> timelineRecords = candidateTimeLineRepository.findBySeniorCanLeadId((long) id);


			// Step 3: Update canLeadId in the found records
			for (CandidateTimeLine timelineRecord : timelineRecords) {
			    timelineRecord.setCanLeadId(0);
			    timelineRecord.setFacebookId(0L);
			    timelineRecord.setCanId(0);
			    timelineRecord.setSeniorCanLeadId(0L);
			    timelineRecord.setSeniorCanId(midSeniorLevelCandidateLeadModel.getId());
			}

			// Step 4: Save the updated records in candidateTimeLineRepository
			candidateTimeLineRepository.saveAll(timelineRecords);

			map.put("statuscode", 200);
			map.put("message", "Candidate qualified and email sent");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else if (!qualified && notQualified) {
			// Candidate is not qualified
			CandidateTimeLine candidateTimeLine = new CandidateTimeLine();
			candidateTimeLine.setCanLeadId(0);
			candidateTimeLine.setEventName("NotQualified");
			candidateTimeLine.setEventDescription("NotQualified By " + admin.getUserName() + " on " + formattedDate);
			candidateTimeLine.setCanId(0);
			candidateTimeLine.setFacebookId(0L);
			candidateTimeLine.setSeniorCanLeadId(midSeniorSourcingModel.getId());
			if (notes != null) {
				candidateTimeLine.setNotes(notes);
			}
			candidateTimeLineRepository.save(candidateTimeLine);

			map.put("statuscode", 200);
			map.put("message", "Candidate not qualified");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			// Both qualified and notQualified cannot be true at the same time
			map.put("statuscode", 400);
			map.put("message", "Invalid request. Candidate cannot be both qualified and not qualified at the same time.");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}


}
