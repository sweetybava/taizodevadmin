package com.taizo.service;

import java.io.File; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.services.s3.model.*;
import com.taizo.DTO.CandidateAnalyticsFilterDTO;
import com.taizo.DTO.DateRange;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.Admin;
import com.taizo.model.CanInterviewsModel;
import com.taizo.model.CanLanguageModel;
import com.taizo.model.CanLeadModel;
import com.taizo.model.CandidateAnalyticsModel;
import com.taizo.model.CandidateCallsModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.CfgCanSources;
import com.taizo.model.EmployerModel;
import com.taizo.model.FacebookMetaLead;
import com.taizo.model.JobsModel;
import com.taizo.model.LeadModel;
import com.taizo.model.MidSeniorCandidateReportModel;
import com.taizo.model.MidSeniorLevelCandidateLeadModel;
import com.taizo.model.SampleVideosModel;
import com.taizo.model.UserModel;
import com.taizo.repository.*;
import com.taizo.utils.WorkbookUtil;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.HorizontalAlignment;

@Service("candidateService")
public class CandidateServiceImpl implements CandidateService {
	
	private static final Logger logger = LoggerFactory.getLogger(CandidateServiceImpl.class);

	
	@Autowired
	CandidateRepository candidateRepository;
	
	@Autowired
	CanLanguagesRepository canLanguagesRepository;

	@Autowired
	VideosRepository videosRepository;

	@Autowired
	UserRepository userRepository;	
	
	@Autowired
	CanLeadRepository canLeadRepository;
	
	@Autowired
	AdminRepository adminRepository;
	
	@Autowired
	CfgCanSourcesRepository cfgCanSourcesRepository;

	@Autowired
	CandidateCallsRepository candidateCallsRepository;
	
	@Autowired
	CanInterviewRepository canInterviewRepository;
	
	@Autowired
	MidSeniorLevelCandidateLeadRepository midSeniorLevelCandidateLeadRepository;
	
	 @Autowired
	 CandidateAnalyticsRepositroy candidateAnalyticsRepository; 
	
	@PersistenceContext
    private EntityManager entityManager;
	
	@Value("classpath:assets/TaizoReport.jpg")
	private Resource taizoReport;
	
    private AmazonS3 s3client;
    
    private String awsS3AudioBucket;

    @Value("${aws.user.endpointUrl}")
    private String endpointUrl;

    @Value("${aws.s3.audio.bucket}")
    private String bucketName;
    @Value("${aws.access.key.id}")
    private String accessKey;
    @Value("${aws.access.key.secret}")
    private String secretKey;
	@Value("${aws.s3.sample.video.bucket}")
	private String sampleVideoBucketName;
	@Value("${aws.s3.sample.video.bucket.folder}")
	private String sampleVideoBucketFolderName;
    @Value("${aws.s3.bucket.user.folder}")
    private String folderName;
  

	private List<CandidateModel> candidateList;
    
    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = new AmazonS3Client(credentials);
    }

	@Override
	public CandidateModel updateSkill(int userId, String skills, String skillVideoType) {
		// TODO Auto-generated method stub
		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		if (!optional.isPresent()) {
		}	
		CandidateModel existing = optional.get();
		existing.setSkills(skills);
		existing.setSkillVideoType(skillVideoType);
		existing = candidateRepository.save(existing);		
		return existing;
}

	@Override
	public CandidateModel updatePaymentStatus(int userId, String paymentStatus) {
		// TODO Auto-generated method stub
		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		if (!optional.isPresent()) {
		}	
		CandidateModel existing = optional.get();
		existing.setPaymentStatus(paymentStatus);;
		existing = candidateRepository.save(existing);		
		return existing;	}

	@Override
	public CandidateModel updateProfile1(int userId, String firstName, String lastName,
			String dateOfBirth, String age, String gender, String currentCountry, String currentState, String currentCity,
			String perCountry, String perState, String perCity,long whatsappNumber,String emailId) {
		// TODO Auto-generated method stub
		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		Optional<UserModel> optional1 = userRepository.findById(userId);

		if (!optional.isPresent() && !optional1.isPresent() ) {
		}
		
		CandidateModel existing = optional.get();
		existing.setFirstName(firstName);
		existing.setLastName(lastName);
		existing.setDateOfBirth(dateOfBirth);
		existing.setAge(age);
		existing.setGender(gender);
		existing.setCurrentCountry(currentCountry);
		existing.setCurrentState(currentState);
		existing.setCurrentCity(currentCity);
		existing.setPerCountry(perCountry);
		existing.setPerState(perState);
		existing.setPerCity(perCity);
		existing.setWhatsappNumber(whatsappNumber);
		existing.setEmailId(emailId);
		
		existing = candidateRepository.save(existing);	
		
		UserModel user = optional1.get();
		
		user.setFirstName(firstName);
		user.setLastName(lastName);
		
		user = userRepository.save(user);

		return existing;	
		}

	@Override
	public CandidateModel updateProfile3(int userId, Integer experienceYears,Integer experienceMonths,  String candidateType,Integer overseasExpYears,Integer overseasExpMonths, String expCertificate,
			String certificateType,String license,String licenseType,String industry,String jobCategory,String city,String keySkill) {
		// TODO Auto-generated method stub
		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		if (!optional.isPresent()) {
		}
		
		CandidateModel existing = optional.get();
		existing.setExperience(experienceYears);
		existing.setExpMonths(experienceMonths);
		existing.setCandidateType(candidateType);
		existing.setIndustry(industry);
		if(keySkill!=null && !keySkill.isEmpty()) {
		existing.setKeySkill(keySkill);
		}
		existing.setJobType("Full Time (8hrs to 10hrs)");
		existing.setJobCategory(jobCategory);
		existing.setOverseasExp(overseasExpYears);
		existing.setOverseasExpMonths(overseasExpMonths);
		existing.setExpCertificate(expCertificate);
		existing.setCertificateType(certificateType);
		existing.setLicense(license);
		existing.setLicenseType(licenseType);
		if(city!=null && !city.isEmpty()) {
			existing.setCity(city);
		}
		if(candidateType.equalsIgnoreCase("Fresher")) {
			existing.setExperienced(false);
		}else {
			existing.setExperienced(true);
		}
		try {
		if(jobCategory.equalsIgnoreCase("Trainee")) {
			existing.setCandidateType("Fresher");
			existing.setExperienced(false);
			existing.setExpInManufacturing(false);
		}else if(jobCategory.equalsIgnoreCase("Assembler")) {
			existing.setCandidateType("Fresher");
			existing.setExperienced(false);
			existing.setExpInManufacturing(false);
		}else if(jobCategory.equalsIgnoreCase("Graduate Trainee")) {
			existing.setCandidateType("Fresher");
			existing.setExperienced(false);
			existing.setExpInManufacturing(false);
		}else {
		if (!existing.isExperienced()) {
			existing.setCandidateType("Fresher");
			existing.setExperienced(false);
			existing.setExpInManufacturing(false);
		} else {
			existing.setCandidateType("Experienced");
			existing.setExperienced(true);
			existing.setExpInManufacturing(true);

		}
		}}catch(Exception e) {
			if (!existing.isExperienced()) {
				existing.setCandidateType("Fresher");
				existing.setExperienced(false);
				existing.setExpInManufacturing(false);
			} else {
				existing.setCandidateType("Experienced");
				existing.setExperienced(true);
				existing.setExpInManufacturing(true);

			}
		}
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		existing.setProfileLastUpdatedDt(dtf.format(now));
		
		existing = candidateRepository.save(existing);		

		return existing;
	}
	
	@Override
	public CandidateModel updateProfile4(int userId, String jobType,String industry, String jobRole, String candidateLocation, String prefLocation,
			String domesticLocation, String overseasLocation,String candidateType, Integer expYears,
										 Integer expMonths) {
		// TODO Auto-generated method stub
		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		if (!optional.isPresent()) {
		}
		
		CandidateModel existing = optional.get();
		existing.setJobType(jobType);
		existing.setIndustry(industry);
		existing.setJobCategory(jobRole);
		existing.setCandidateLocation(candidateLocation);
		existing.setPrefLocation(prefLocation);
		existing.setCity(domesticLocation);
		existing.setOverseasLocation(overseasLocation);
		existing.setCandidateType(candidateType);
		existing.setExperience(expYears);
		existing.setExpMonths(expMonths);
		
		if(candidateType.equalsIgnoreCase("Fresher")) {
			existing.setOverseasExp(0);
			existing.setOverseasExpMonths(0);
		}
	
		existing = candidateRepository.save(existing);		

		return existing;
	}	

	@Transactional
	@Override
	public CandidateModel updateApprovalStatus(int id, String status) {
		// TODO Auto-generated method stub
		Optional<CandidateModel> optional = candidateRepository.findByUserId(id);
		Optional<UserModel> optional1 = userRepository.findById(id);

		
		if (!optional.isPresent() && !optional1.isPresent()) {
		}
		
		CandidateModel existing = optional.get();
		UserModel existing1 = optional1.get();		
		existing.setApprovalStatus(status);		
		existing = candidateRepository.save(existing);	
	   existing1.setApprovalStatus(status);	
		existing1 = userRepository.save(existing1);	
		return existing;	}

	@Override
	public void downloadData(HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		List<CandidateModel> candidates = candidateRepository.findAll();
				
		Workbook workbook = new XSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Full Report");

	    String[] headers = new String[] {
	    		"User Name", "Candidate Type", "Job Type", "Country", "Job Category", "Age", 
	    		"Gender", "Mobile Number", "Email ID"
		};        
	    
	    Row headerRow = sheet.createRow(0);
	    for(int i = 0; i < headers.length; i++) {
	    	Cell cell = headerRow.createCell(i);
	    	cell.setCellValue(headers[i]);
	    }
	    
	    int i = 0;
	    for (CandidateModel c : candidates) {
	    	i++;
	    	int j = 0;
	    	
	    	Row row = sheet.createRow(i);

	    	WorkbookUtil.writeCell(row, j++, "" + c.getFirstName() + " "
	    			+ c.getLastName());
	    	WorkbookUtil.writeCell(row, j++, "" + c.getCandidateType());
	    	WorkbookUtil.writeCell(row, j++, "" + c.getJobType());
	    	WorkbookUtil.writeCell(row, j++, "" + c.getPrefCountry());
	    	WorkbookUtil.writeCell(row, j++, "" + c.getJobCategory());
	    	WorkbookUtil.writeCell(row, j++, "" + c.getAge());
	    	WorkbookUtil.writeCell(row, j++, "" + c.getGender());
	    	WorkbookUtil.writeCell(row, j++, "" + c.getMobileNumber());    
	    	WorkbookUtil.writeCell(row, j++, "" + c.getEmailId());        	       

	    	
	    }
	    workbook.write(response.getOutputStream());
	    workbook.close();
		
	}

	
	public void downloadPdf(HttpServletResponse response) throws IOException {

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER); 
        document.addPage(page);
                
        byte[] bdata = FileCopyUtils.copyToByteArray(taizoReport.getInputStream());        
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, bdata, "TaizoReport.jpg");
        
        PDPageContentStream cos = new PDPageContentStream(document, page);        
        PDRectangle mediaBox = page.getMediaBox();

        float startX = (mediaBox.getWidth() - pdImage.getWidth()) / 2;
        float startY = (mediaBox.getHeight() - pdImage.getHeight());
        cos.drawImage(pdImage, startX, (startY - 20));
        
        float margin = 50;
        float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
        float tableWidth = page.getMediaBox().getWidth() - (2 * margin);

        boolean drawContent = true;
        //float yStart = yStartNewPage;
        float bottomMargin = 50;
        float yPosition =  startY - pdImage.getHeight() + 20;

        BaseTable table = new BaseTable(yPosition, yStartNewPage,
            bottomMargin, tableWidth, margin, document, page, true, drawContent);

        be.quodlibet.boxable.Row<PDPage> headerRow = table.createRow(20);
        createPdfCell(headerRow, 18f, "Candidate");
		createPdfCell(headerRow, 9f, "Candidate Type");
		createPdfCell(headerRow, 18f, "Job Type");
		createPdfCell(headerRow, 18f, "Job Category");
		createPdfCell(headerRow, 9f, "Age");
		createPdfCell(headerRow, 18f, "Mobile Number");
		createPdfCell(headerRow, 8f, "Payment Status");
        
        List<CandidateModel> can = candidateRepository.findAll();   
        

        for (CandidateModel c : can) {
        	be.quodlibet.boxable.Row<PDPage> row = createPdfRow(table);
    		createPdfCell(row, 18f, String.format("%s %s", c.getFirstName(), c.getLastName()));
    		createPdfCell(row, 9f, c.getCandidateType());
    		createPdfCell(row, 18f, c.getJobType());
    		createPdfCell(row, 18f, c.getJobCategory());   	
    		createPdfCell(row, 9f, c.getAge());   		

    		createPdfCell(row, 18f, String.format("%s",c.getMobileNumber()));   	
    		createPdfCell(row, 8f, c.getPaymentStatus());   		


  		}

        table.draw();
        cos.close();
        document.save(response.getOutputStream());		
	}
	
	


	public be.quodlibet.boxable.Row<PDPage> createPdfRow(BaseTable table) {
		return table.createRow(16);
	}
	
	public void createPdfCell(be.quodlibet.boxable.Row<PDPage> row, float width, String string) {		
		be.quodlibet.boxable.Cell<PDPage> cell = row.createCell(width, string);
        cell.setFontSize(6);
        cell.setFont(PDType1Font.HELVETICA);
        cell.setAlign(HorizontalAlignment.LEFT);
	}

	@Override
	public CandidateModel updateDocument(int userId, String documentTitle, String document) {
		// TODO Auto-generated method stub
		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		if (!optional.isPresent()) {
		}	
		CandidateModel existing = optional.get();
		existing.setExpCertificate(document);
	
			existing.setCertificateType(documentTitle);
		
		existing = candidateRepository.save(existing);		
		return existing;	}

	@Override
	public CandidateModel updateLicense(int userId, String licenseTitle, String license) {
		// TODO Auto-generated method stub
		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		if (!optional.isPresent()) {
		}	
		CandidateModel existing = optional.get();
		existing.setLicense(license);
	
			existing.setLicenseType(licenseTitle);
		
		existing = candidateRepository.save(existing);		
		return existing;	}

	public void searchData(HttpServletResponse response) throws IOException {
		
	}

	@Override
	public CandidateModel updateLanguageKey(int userId, String languageKey) {
		// TODO Auto-generated method stub
		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);
		if (!optional.isPresent()) {
		}	
		CandidateModel existing = optional.get();
		existing.setLanguageKey(languageKey);
		existing = candidateRepository.save(existing);		
		return existing;	}

	@Override
	public boolean deleteImage(String skillvideo) {
		// TODO Auto-generated method stub
        String fileName = skillvideo.substring(skillvideo.lastIndexOf("/") + 1);
        try {
            DeleteObjectsRequest delObjReq = new DeleteObjectsRequest(bucketName).withKeys(fileName);

            s3client.deleteObjects(delObjReq);
            return true;
        } catch (SdkClientException s) {
            return false;
        }
	        }

	@Override
	public String uploadFile(MultipartFile video, int id, byte[] bytes) {
		// TODO Auto-generated method stub
        String fileUrl = "";
        File file = null;
        
        String pathToFile = "/tmp/";

        		  new File(pathToFile).mkdir();
        try {
        	
            file = new File(pathToFile + video);
            file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();

        }
        FileOutputStream iofs = null;
        try {
            iofs = new FileOutputStream(file);
            iofs.write(bytes);
            iofs.close();

            String path = folderName + "/" + id + "/" + "Skills" + "/";
            String fileName = generateFileName(video);
            String videopath = path + fileName;
            fileUrl = endpointUrl + "/" + id + "/" + "Skills" + "/" + fileName;

            uploadFileTos3bucket(videopath, file);
            file.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return fileUrl;	}	
	
	@Override
	public String uploadJobFile(MultipartFile video, int id, byte[] bytes) {
		// TODO Auto-generated method stub
        String fileUrl = "";
        File file = null;
        
        String pathToFile = "/tmp/";

        		  new File(pathToFile).mkdir();
        try {
        	
            file = new File(pathToFile + video);
            file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();

        }
        FileOutputStream iofs = null;
        try {
            iofs = new FileOutputStream(file);
            iofs.write(bytes);
            iofs.close();

            String path = folderName + "/" + id + "/" + "JobVideo" + "/";
            String fileName = generateFileName(video);
            String videopath = path + fileName;
            fileUrl = endpointUrl + "/" + id + "/" + "JobVideo" + "/" + fileName;

            uploadFileTos3bucket(videopath, file);
            file.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return fileUrl;	}	
	
    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }

    private void uploadFileTos3bucket(String fileName, File file) {

        s3client.putObject(
                new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
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

	@Override
	public Page<SampleVideosModel> findPaginated(int pageNo, int pageSize) {

		Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

		Page<SampleVideosModel> pagedResult = videosRepository.findAll(paging);
		return pagedResult;
	}

	
	@Transactional(readOnly = true)
	@Override
	public CandidateModel get(int id) throws ResourceNotFoundException {
		
		Optional<CandidateModel> optional = candidateRepository.findById(id);
		
		if (!optional.isPresent()) {
			logger.debug("Candidate not found with id {}.", id);
			throw new ResourceNotFoundException("Candidate not found."); 
		}
		Optional<UserModel> optional1 = userRepository.findById(optional.get().getUserId());

		optional.get().setProfilePic(optional1.get().getProfilePic());
		//optional.get().setKnownLanguages(details);
		
		return optional.get();
	}
	@Transactional
	@Override
	public void deleteById(int id) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		Optional<CanLeadModel> optional = canLeadRepository.findById(id);
		if (!optional.isPresent()) {
			throw new ResourceNotFoundException("data not found.");

		}
		canLeadRepository.deleteById(id);
	}


	@Override
	public List<Map<String, Object>> filtercandidate(String gender,String eligibility,int assignTo,long mobileNumber, String industry, String jobCategory,
			String specilization,String qualification, String candidateType, String skills, String prefLocation, int passed_out_year,
			int experience, int experience2,int pages,int pageSize,Date startDate,Date endDate) {
		// TODO Auto-generated method stub
		return candidateRepository.candidate(gender,eligibility,assignTo,mobileNumber,industry,jobCategory,specilization,qualification,candidateType,skills,prefLocation,passed_out_year,experience,experience2,pages,pageSize,startDate,endDate);
	}
	
	 public List<CfgCanSources> getAllEntities() {
	        return cfgCanSourcesRepository.findAll();
	    }

	@Override
	public CandidateModel findById(String candidateId) {
		return candidateRepository.findById(Integer.parseInt(candidateId)).get();
	}

	@Override
	public CandidateModel getCandidateDetailsById(int id) {
        return candidateRepository.findById(id).orElse(null);
	}

	@Override
	public CandidateModel getCandidateDetailsByNumber(long number) {
		String numberAsString = Long.toString(number);
		return candidateRepository.findByMobileNumberOrContactNumberOrWhatsappNumber(number, numberAsString, number);
	}


	@Override
	public Page<CanLeadModel> getAllCanLeadModels(Pageable pageable) {
		return canLeadRepository.findAll(pageable);
	}
	
	@Override
	  public Page<CanLeadModel> getCanLeadModelsByProfilePageNoAndMobileNumber( Pageable pageable) {
	        return canLeadRepository.findByProfilePageNoAndMobileNumber( pageable);
	    }
	

	@Override
	public List<CandidateCallsModel> getCallsByJid(int jid) {
		  return candidateCallsRepository.findAllByJidOrderByCallTimeDesc(jid);
	}

	@Override
	public Page<CandidateCallsModel> getCallsByJidWithPagination(int jid, Pageable pageable) {
		return candidateCallsRepository.findAllByJidOrderByCallTimeDesc(jid, pageable);
	}

	@Override
	public Page<CandidateCallsModel> getAllCallsWithPagination(Pageable pageable) {
		return candidateCallsRepository.findAllByOrderByCallTimeDesc(pageable);
	}

	public Page<CanLeadModel> getCanLeadModelsByProfilePageNo(Integer profilePageNo, Pageable pageable) {
        return canLeadRepository.findByProfilePageNo(profilePageNo, pageable);
    }

	@Override
	public Page<CanLeadModel> getCanLeadModelsMobileNumber(Long mobileNumber, Pageable pageable) {
		// TODO Auto-generated method stub
		return canLeadRepository.findByMobileNumber(mobileNumber, pageable);
	}
	
	
	 public List<Map<String, Object>> filterCandidate(
	            String gender,
	            String industry,
	            String category,
	            String qualification,
	            String canType,
	            String skills,
	            String prefLocation,
	            Integer  Passed_out_year,
	            Integer Experience,
	            Integer maxExperience,
	            Integer pages,
	            Integer size,
	            Date startDate,
	            Date endDate
	    ) {
	        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	        CriteriaQuery<CandidateModel> criteriaQuery = criteriaBuilder.createQuery(CandidateModel.class);
	        Root<CandidateModel> root = criteriaQuery.from(CandidateModel.class);

	        List<Predicate> predicates = new ArrayList<>();
	        

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

	        
	        if (industry != null && !industry.isEmpty()) {
	            predicates.add(criteriaBuilder.equal(root.get("industry"), industry));
	        }


        if (category != null) {
            predicates.add(criteriaBuilder.equal(root.get("jobCategory"), category));
        }
        
        if (qualification != null) {
            predicates.add(criteriaBuilder.equal(root.get("qualification"), qualification));
        }


        if (canType != null) {
            predicates.add(criteriaBuilder.equal(root.get("candidateType"), canType));
        }

        if (skills != null) {
            String[] skillList = skills.split(",");
            List<Predicate> skillPredicates = new ArrayList<>();
            for (String skill : skillList) {
                skillPredicates.add(criteriaBuilder.like(root.get("keySkill"), "%" + skill.trim() + "%"));
            }
            predicates.add(criteriaBuilder.or(skillPredicates.toArray(new Predicate[0])));
        }

        if (prefLocation != null) {
            String[] locationList = prefLocation.split(",");
            List<Predicate> locationPredicates = new ArrayList<>();
            
            for (String location : locationList) {
                locationPredicates.add(criteriaBuilder.like(root.get("prefLocation"), "%" + location.trim() + "%"));
            }
            
            Predicate locationsOrPredicate = criteriaBuilder.or(locationPredicates.toArray(new Predicate[0]));
            
            predicates.add(locationsOrPredicate);
        }


        if ( Passed_out_year != null &&  Passed_out_year > 0) {
            predicates.add(criteriaBuilder.equal(root.get("passed_out_year"),  Passed_out_year));
        }

      
        if (Experience != 0 || maxExperience != 0) {
            if (Experience != null && maxExperience != null && Experience <= maxExperience) {
                predicates.add(criteriaBuilder.between(root.get("experience"), Experience, maxExperience));
            } else if (Experience != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), Experience));
            } else if (maxExperience != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("experience"), maxExperience));
            }
        }
        
        if (startDate != null && endDate != null) {
        	predicates.add(criteriaBuilder.between(root.get("createdTime"), startDate, endDate));
        }
        
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));
        
        if (predicates != null && predicates.size() != 0) {
			Predicate[] predicate = predicates.toArray(new Predicate[0]);
			criteriaQuery.where(predicate);
		}
        
        List<CandidateModel> resultList = entityManager.createQuery(criteriaQuery)
                .setFirstResult((pages - 1) * size)
                .setMaxResults(size)
                .getResultList();

        List<Map<String, Object>> resultMaps = new ArrayList<>();

        for (CandidateModel candidate : resultList) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("candidate", candidate);

            resultMaps.add(resultMap);
        }

        return resultMaps;
    }

		@Override
		public long filterCandidateCount(String gender, String industry, String jobCategory, String qualification,
				String candidateType, String keySkill, String prefLocation, int passed_out_year, int experience,
				int maxExperience, Date createdTime, Date endDate) {
			// TODO Auto-generated method stub
			 CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		        Root<CandidateModel> root = criteriaQuery.from(CandidateModel.class);

		        List<Predicate> predicates = new ArrayList<>();
		        

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

		        
		        if (industry != null && !industry.isEmpty()) {
		            predicates.add(criteriaBuilder.equal(root.get("industry"), industry));
		        }


	        if (jobCategory != null) {
	            predicates.add(criteriaBuilder.equal(root.get("jobCategory"), jobCategory));
	        }
	        
	        if (qualification != null) {
	            predicates.add(criteriaBuilder.equal(root.get("qualification"), qualification));
	        }


	        if (candidateType != null) {
	            predicates.add(criteriaBuilder.equal(root.get("candidateType"), candidateType));
	        }

	        if (keySkill != null) {
	            String[] skillList = keySkill.split(",");
	            List<Predicate> skillPredicates = new ArrayList<>();
	            for (String skill : skillList) {
	                skillPredicates.add(criteriaBuilder.like(root.get("keySkill"), "%" + skill.trim() + "%"));
	            }
	            predicates.add(criteriaBuilder.or(skillPredicates.toArray(new Predicate[0])));
	        }

	        if (prefLocation != null) {
	            String[] locationList = prefLocation.split(",");
	            List<Predicate> locationPredicates = new ArrayList<>();
	            
	            for (String location : locationList) {
	                locationPredicates.add(criteriaBuilder.like(root.get("prefLocation"), "%" + location.trim() + "%"));
	            }
	            
	            Predicate locationsOrPredicate = criteriaBuilder.or(locationPredicates.toArray(new Predicate[0]));
	            
	            predicates.add(locationsOrPredicate);
	        }


	        if ( passed_out_year != 0 &&  passed_out_year > 0) {
	            predicates.add(criteriaBuilder.equal(root.get("passed_out_year"),  passed_out_year));
	        }

	      
	        if (experience != 0 || maxExperience != 0) {
	            if (experience != 0 && maxExperience != 0 && experience <= maxExperience) {
	                predicates.add(criteriaBuilder.between(root.get("experience"), experience, maxExperience));
	            } else if (experience != 0) {
	                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), experience));
	            } else if (maxExperience != 0) {
	                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("experience"), maxExperience));
	            }
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
		public CandidateModel getCandidateById(String candidateId) {
			CandidateModel candidateModel = candidateRepository.findById(Integer.parseInt(candidateId)).orElse(null);
			return candidateModel;

		}

		@Override
		public List<Map<String, Object>> filterMetaDatas(Long id, int assignTo, String candidateName, String educationQualification,
														 String jobCategory, String mobileNumber, boolean qualified, boolean notQualified, boolean notAttend,boolean noStatus,String experience,
														 String preferredLocation,String joining, int pages, int size, Date createdTime,
														 Date endDate) {

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<FacebookMetaLead> criteriaQuery = criteriaBuilder.createQuery(FacebookMetaLead.class);
			Root<FacebookMetaLead> root = criteriaQuery.from(FacebookMetaLead.class);

			List<Predicate> predicates = new ArrayList<>();

			if (id != null && id != 0) {
				predicates.add(criteriaBuilder.equal(root.get("id"), id));
			}

			if (assignTo != 0) {
			    predicates.add(criteriaBuilder.equal(root.get("assignTo"), assignTo));
			} 


			if (candidateName != null) {
				predicates.add(criteriaBuilder.like(root.get("candidateName"), "%" + candidateName + "%"));
			}

			if (educationQualification != null) {
				predicates.add(criteriaBuilder.like(root.get("educationQualification"), "%" + educationQualification + "%"));
			}

			if (jobCategory != null) {
				predicates.add(criteriaBuilder.like(root.get("jobCategory"), "%" + jobCategory + "%"));
			}

			if (mobileNumber != null) {
				predicates.add(criteriaBuilder.or(
						criteriaBuilder.like(root.get("mobileNumber"), "%" + mobileNumber + "%"),
						criteriaBuilder.like(root.get("whatsappNumber"), "%" + mobileNumber + "%")
				));
			}
			
			if (qualified) {
			    predicates.add(criteriaBuilder.isTrue(root.get("qualified")));
			    predicates.add(criteriaBuilder.isFalse(root.get("notQualified")));
		        predicates.add(criteriaBuilder.isFalse(root.get("isNotAttend")));
			}

			// Not Qualified
			if (notQualified) {
			    predicates.add(criteriaBuilder.isTrue(root.get("notQualified")));
			    predicates.add(criteriaBuilder.isFalse(root.get("isNotAttend")));
		        predicates.add(criteriaBuilder.isFalse(root.get("qualified")));
			}

			// Not Attend
			if (notAttend) {
			    predicates.add(criteriaBuilder.isTrue(root.get("isNotAttend")));
			    predicates.add(criteriaBuilder.isFalse(root.get("notQualified")));
		        predicates.add(criteriaBuilder.isFalse(root.get("qualified")));
			}
			if(noStatus) {
				 predicates.add(criteriaBuilder.isFalse(root.get("isNotAttend")));
				    predicates.add(criteriaBuilder.isFalse(root.get("notQualified")));
			        predicates.add(criteriaBuilder.isFalse(root.get("qualified")));
			}
			
			if (experience != null) {
				predicates.add(criteriaBuilder.equal(root.get("experience"), experience));
			}

			if (preferredLocation != null) {
				predicates.add(criteriaBuilder.or(
						criteriaBuilder.like(root.get("preferredLocation"), "%" + preferredLocation + "%"),
						criteriaBuilder.like(root.get("candidatePreferredLocation"), "%" + preferredLocation + "%")
				));
			}
			
			if("immediate_joining".equals(joining)) {
				 predicates.add(criteriaBuilder.equal(root.get("joiningAvailability"), joining));
			}
			else if ("i_will_join_within_1_week".equals(joining)) {
			    predicates.add(criteriaBuilder.equal(root.get("joiningAvailability"), joining));
			}
			else if("i_will_join_within_15_days".equals(joining)) {
				predicates.add(criteriaBuilder.equal(root.get("joiningAvailability"), joining));
			}
			else if("i_will_join_within_1_month".equals(joining)) {
				predicates.add(criteriaBuilder.equal(root.get("joiningAvailability"), joining));
			}
			else if("i_will_join_next_month".equals(joining)) {
				predicates.add(criteriaBuilder.equal(root.get("joiningAvailability"), joining));
			}
			else if("i_do_not_know".equals(joining)) {
				predicates.add(criteriaBuilder.equal(root.get("joiningAvailability"), joining));
			}
			

			if (createdTime != null && endDate != null) {
				predicates.add(criteriaBuilder.between(root.get("createdTime"), createdTime, endDate));
			}
			predicates.add(criteriaBuilder.equal(root.get("inActive"), false));
			criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));

			// Calculate the offset for pagination
			int offset = (pages - 1) * size;

			criteriaQuery.where(predicates.toArray(new Predicate[0]));

			List<FacebookMetaLead> resultList = entityManager.createQuery(criteriaQuery)
					.setFirstResult(offset)
					.setMaxResults(size)
					.getResultList();

			List<Map<String, Object>> resultMaps = new ArrayList<>();
			for (FacebookMetaLead facebookMetaLead : resultList) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("facebookMetaLead", facebookMetaLead);
				resultMaps.add(resultMap);
			}
			return resultMaps;
		}


		@Override
		public long filterMetaLeadCount(Long id, int assignTo, String candidateName, String educationQualification,
		                                String jobCategory, String mobileNumber, boolean qualified, boolean notQualified,
		                                boolean notAttend,boolean noStatus, String experience, String preferredLocation, String joining,
		                                Date createdTime, Date endDate) {

		    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		    CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		    Root<FacebookMetaLead> root = criteriaQuery.from(FacebookMetaLead.class);

		    List<Predicate> predicates = new ArrayList<>();

		    if (id != null && id != 0) {
		        predicates.add(criteriaBuilder.equal(root.get("id"), id));
		    }

		    if (assignTo != 0) {
		        predicates.add(criteriaBuilder.equal(root.get("assignTo"), assignTo));
		    } else if (assignTo == 0) {
		        criteriaQuery.select(criteriaBuilder.count(root));
		    }

		    if (candidateName != null) {
		        predicates.add(criteriaBuilder.like(root.get("candidateName"), "%" + candidateName + "%"));
		    }

		    if (educationQualification != null) {
		        predicates.add(criteriaBuilder.like(root.get("educationQualification"), "%" + educationQualification + "%"));
		    }

		    if (jobCategory != null) {
		        predicates.add(criteriaBuilder.like(root.get("jobCategory"), "%" + jobCategory + "%"));
		    }

		    if (mobileNumber != null) {
		        predicates.add(criteriaBuilder.or(
		                criteriaBuilder.like(root.get("mobileNumber"), "%" + mobileNumber + "%"),
		                criteriaBuilder.like(root.get("whatsappNumber"), "%" + mobileNumber + "%")
		        ));
		    }
		    if (qualified) {
			    predicates.add(criteriaBuilder.isTrue(root.get("qualified")));
			    predicates.add(criteriaBuilder.isFalse(root.get("notQualified")));
		        predicates.add(criteriaBuilder.isFalse(root.get("isNotAttend")));
			}

			// Not Qualified
			if (notQualified) {
			    predicates.add(criteriaBuilder.isTrue(root.get("notQualified")));
			    predicates.add(criteriaBuilder.isFalse(root.get("isNotAttend")));
		        predicates.add(criteriaBuilder.isFalse(root.get("qualified")));
			}

			// Not Attend
			if (notAttend) {
			    predicates.add(criteriaBuilder.isTrue(root.get("isNotAttend")));
			    predicates.add(criteriaBuilder.isFalse(root.get("notQualified")));
		        predicates.add(criteriaBuilder.isFalse(root.get("qualified")));
		    }
			
			if(noStatus) {
				 predicates.add(criteriaBuilder.isFalse(root.get("isNotAttend")));
				    predicates.add(criteriaBuilder.isFalse(root.get("notQualified")));
			        predicates.add(criteriaBuilder.isFalse(root.get("qualified")));
			}
			
		  
		    if (experience != null) {
		        predicates.add(criteriaBuilder.equal(root.get("experience"), experience));
		    }

		    if (preferredLocation != null) {
		        predicates.add(criteriaBuilder.or(
		                criteriaBuilder.like(root.get("preferredLocation"), "%" + preferredLocation + "%"),
		                criteriaBuilder.like(root.get("candidatePreferredLocation"), "%" + preferredLocation + "%")
		        ));
		    }

		    if ("immediate_joining".equals(joining)) {
		        predicates.add(criteriaBuilder.equal(root.get("joiningAvailability"), joining));
		    } else if ("i_will_join_within_1_week".equals(joining)) {
		        predicates.add(criteriaBuilder.equal(root.get("joiningAvailability"), joining));
		    } else if ("i_will_join_within_15_days".equals(joining)) {
		        predicates.add(criteriaBuilder.equal(root.get("joiningAvailability"), joining));
		    } else if ("i_will_join_within_1_month".equals(joining)) {
		        predicates.add(criteriaBuilder.equal(root.get("joiningAvailability"), joining));
		    } else if ("i_will_join_next_month".equals(joining)) {
		        predicates.add(criteriaBuilder.equal(root.get("joiningAvailability"), joining));
		    } else if ("i_do_not_know".equals(joining)) {
		        predicates.add(criteriaBuilder.equal(root.get("joiningAvailability"), joining));
		    }

		    if (createdTime != null && endDate != null) {
		        predicates.add(criteriaBuilder.between(root.get("createdTime"), createdTime, endDate));
		    }
		    predicates.add(criteriaBuilder.equal(root.get("inActive"), false));

		    // Handling the case where none of qualified, notQualified, and notAttend are true
		    Predicate finalPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));

		    criteriaQuery.where(finalPredicate);
		    criteriaQuery.select(criteriaBuilder.count(root));

		    TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);

		    return query.getSingleResult();
		}



		@Override
		public Page<CanLeadModel> getCanLeadModelFromSource(String fromSource, Pageable pageable) {
			// TODO Auto-generated method stub
			return canLeadRepository.findByFromSource(fromSource, pageable);
		}
		
		private Join<CanInterviewsModel, CandidateModel> joinCandidate(CriteriaBuilder criteriaBuilder, Root<CanInterviewsModel> root) {
		    return root.join("candidate", JoinType.LEFT);
		}

		private Join<CanInterviewsModel, JobsModel> joinJob(CriteriaBuilder criteriaBuilder, Root<CanInterviewsModel> root) {
		    return root.join("job", JoinType.LEFT);
		}

		@Override
		public List<Map<String, Object>> filterCanInterview(int jobId, long contactNumber, int adminId, String interviewDate, String companyName,String interviewEndDate, int page, int size, Date createdTime, Date endDate,long candidateMobileNumber,String jobCategory,String city,String area, int interviewStatus) {
		    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		    CriteriaQuery<CanInterviewsModel> criteriaQuery = criteriaBuilder.createQuery(CanInterviewsModel.class);
		    Root<CanInterviewsModel> root = criteriaQuery.from(CanInterviewsModel.class);
		    List<Predicate> predicates = new ArrayList<>();
		    
		    if (jobId != 0) {
		        predicates.add(criteriaBuilder.equal(root.get("jobId"), jobId));
		    }
		    if (contactNumber != 0) {
		        predicates.add(criteriaBuilder.equal(root.get("contactNumber"), contactNumber));
		    }
		    if (adminId != 0) {
		        predicates.add(criteriaBuilder.equal(root.get("adminId"), adminId));
		    }
		    if (interviewDate != null && interviewEndDate != null) {
		        // Add condition to check interviewDate and interviewEndDate
		        Predicate dateCondition = criteriaBuilder.between(root.get("interviewDate"), interviewDate, interviewEndDate);
		        Predicate dateCondition1 = criteriaBuilder.between(root.get("rescheduledDate"), interviewDate, interviewEndDate);

		        // Check if isRescheduled is true
		        Predicate isRescheduledCondition = criteriaBuilder.isTrue(root.get("isRescheduled"));

		        predicates.add(criteriaBuilder.or(
		                criteriaBuilder.and(isRescheduledCondition, dateCondition1), // Rescheduled interviews
		                criteriaBuilder.and(criteriaBuilder.isFalse(root.get("isRescheduled")), dateCondition) // Original interviews
		        ));
		    }

			
		    if (companyName != null) {
		        predicates.add(criteriaBuilder.like(root.get("companyName"), "%" + companyName + "%"));
		    }
		    if (createdTime != null && endDate != null) {
		        predicates.add(criteriaBuilder.between(root.get("createdTime"), createdTime, endDate));
		    }
		    
		    if (candidateMobileNumber != 0) {
		        // Fetch the candidate id based on the mobile number
		        Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
		        Root<CandidateModel> candidateRoot = subquery.from(CandidateModel.class);
		        subquery.select(candidateRoot.get("id")).where(criteriaBuilder.equal(candidateRoot.get("mobileNumber"), candidateMobileNumber));

		        // Add a condition to check if the canId matches the one in CanInterviewsModel
		        predicates.add(criteriaBuilder.in(root.get("canId")).value(subquery));
		    }

		    if (jobCategory != null) {
		        // Fetch the candidate id based on the mobile number
		        Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
		        Root<JobsModel> jobRoot = subquery.from(JobsModel.class);
		        subquery.select(jobRoot.get("id")).where(criteriaBuilder.like(jobRoot.get("jobCategory"), "%" + jobCategory + "%"));

		        // Add a condition to check if the jobId matches the one in CanInterviewsModel
		        predicates.add(criteriaBuilder.in(root.get("jobId")).value(subquery));
		    }


		    if (city != null) {
		        predicates.add(criteriaBuilder.like(root.get("city"), "%" + city + "%"));
		    }
		    
		    if (area != null) {
		        predicates.add(criteriaBuilder.like(root.get("area"), "%" + area + "%"));
		    }
		    if (interviewStatus >= 0 && interviewStatus <= 8 && interviewStatus!=6) {
		        predicates.add(criteriaBuilder.equal(root.get("interviewCurrentStatus"), interviewStatus));
		    } else if (interviewStatus == 6) {
		        predicates.add(criteriaBuilder.and(
		                criteriaBuilder.isFalse(root.get("isLeftTheCompany")),
		                criteriaBuilder.equal(root.get("interviewCurrentStatus"), interviewStatus)
		        ));
		    } 



		    predicates.add(criteriaBuilder.equal(root.get("status"), "I"));
		    predicates.add(criteriaBuilder.equal(root.get("isJoined"), false));
		    criteriaQuery.where(predicates.toArray(new Predicate[0]));
		    criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));
		    

		    int offset = (page - 1) * size;

		    List<CanInterviewsModel> resultList = entityManager.createQuery(criteriaQuery)
		            .setFirstResult(offset)
		            .setMaxResults(size)
		            .getResultList();

		    List<Map<String, Object>> resultMaps = new ArrayList<>();
		    for (CanInterviewsModel canInterview : resultList) {
		        Map<String, Object> resultMap = new HashMap<>();
		        resultMap.put("CanInterviewsModel", canInterview);

		        // Fetch related CandidateModel using a separate query
		        CandidateModel candidateModel = entityManager.find(CandidateModel.class, canInterview.getCanId());
		        resultMap.put("CandidateModel", candidateModel);

		        // Fetch related JobTable using a separate query
		        JobsModel jobTable = entityManager.find(JobsModel.class, canInterview.getJobId());
		        resultMap.put("JobModel", jobTable);
		        
		        EmployerModel employerModel = entityManager.find(EmployerModel.class, jobTable.getEmployerId());
		        resultMap.put("EmployerModel", employerModel);
                
		        
//		        Admin admin = entityManager.find(Admin.class, canInterview.getAdminId());
//		        resultMap.put("Admin", admin);

		        resultMaps.add(resultMap);
		    }

		    return resultMaps;
		}



		@Override
		public long filterCanInterviewCount(int jobId,long contactNumber, int adminId, String interviewDate, String companyName,String interviewEndDate,
				Date createdTime, Date endDate,long candidateMobileNumber,String jobCategory,String city,String area, int interviewStatus) {
			// TODO Auto-generated method stub
			 CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			    CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
			    Root<CanInterviewsModel> root = criteriaQuery.from(CanInterviewsModel.class);

			    List<Predicate> predicates = new ArrayList<>();
			    
			    if (jobId != 0) {
					predicates.add(criteriaBuilder.equal(root.get("jobId"), jobId));
				}
			    if(contactNumber!=0) {
					predicates.add(criteriaBuilder.equal(root.get("contactNumber"),contactNumber));
				}
				if (adminId != 0) {
					predicates.add(criteriaBuilder.equal(root.get("adminId"), adminId));
				}
				else if(adminId==0) {
			    	  criteriaQuery.select(criteriaBuilder.count(root));
			    }
				if (interviewDate != null && interviewEndDate != null) {
				    // Add condition to check interviewDate and interviewEndDate
				    Predicate dateCondition = criteriaBuilder.between(root.get("interviewDate"), interviewDate, interviewEndDate);
				    Predicate dateCondition1 = criteriaBuilder.between(root.get("rescheduledDate"), interviewDate, interviewEndDate);

				    // Check if isRescheduled is true
				    Predicate isRescheduledCondition = criteriaBuilder.isTrue(root.get("isRescheduled"));

				    predicates.add(criteriaBuilder.or(
				            criteriaBuilder.and(isRescheduledCondition, dateCondition1), // Rescheduled interviews
				            criteriaBuilder.and(criteriaBuilder.isFalse(root.get("isRescheduled")), dateCondition) // Original interviews
				    ));
				}


				
				if(companyName!=null) {
					predicates.add(criteriaBuilder.like(root.get("companyName"), "%" + companyName + "%"));
				}
				if (createdTime != null && endDate != null) {
					predicates.add(criteriaBuilder.between(root.get("createdTime"), createdTime, endDate));
				}
				
				 if (candidateMobileNumber != 0) {
				        // Fetch the candidate id based on the mobile number
				        Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
				        Root<CandidateModel> candidateRoot = subquery.from(CandidateModel.class);
				        subquery.select(candidateRoot.get("id")).where(criteriaBuilder.equal(candidateRoot.get("mobileNumber"), candidateMobileNumber));

				        // Add a condition to check if the canId matches the one in CanInterviewsModel
				        predicates.add(criteriaBuilder.in(root.get("canId")).value(subquery));
				    }

				 if (jobCategory != null) {
					    // Fetch the candidate id based on the mobile number
					    Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
					    Root<JobsModel> jobRoot = subquery.from(JobsModel.class);
					    subquery.select(jobRoot.get("id")).where(criteriaBuilder.like(jobRoot.get("jobCategory"), "%" + jobCategory + "%"));

					    // Add a condition to check if the jobId matches the one in CanInterviewsModel
					    predicates.add(criteriaBuilder.in(root.get("jobId")).value(subquery));
					}

				 
				 if (city != null) {
				        predicates.add(criteriaBuilder.like(root.get("city"), "%" + city + "%"));
				    }
				 
				 if (area != null) {
				        predicates.add(criteriaBuilder.like(root.get("area"), "%" + area + "%"));
				    }
				 if (interviewStatus >= 0 && interviewStatus <= 8 && interviewStatus!=6) {
					    predicates.add(criteriaBuilder.equal(root.get("interviewCurrentStatus"), interviewStatus));
					} else if (interviewStatus == 6) {
					    predicates.add(criteriaBuilder.and(
					            criteriaBuilder.isFalse(root.get("isLeftTheCompany")),
					            criteriaBuilder.equal(root.get("interviewCurrentStatus"), interviewStatus)
					    ));
					} 

				  
				 predicates.add(criteriaBuilder.equal(root.get("status"), "I"));
				 predicates.add(criteriaBuilder.equal(root.get("isJoined"), false));
				 criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));
			        
			        if (predicates != null && !predicates.isEmpty()) {
			            Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
			            criteriaQuery.where(predicateArray);
			        }
				   
				        criteriaQuery.select(criteriaBuilder.count(root));

				    TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);

				    long totalCount= query.getSingleResult();
				    return totalCount;
			
		}

		@Override
		public Page<CanLeadModel> getgetCanLeadModelJobCategory(String jobCategory, Pageable pageable) {
			// TODO Auto-generated method stub
			return canLeadRepository.findByjobCategory(jobCategory, pageable);
		}

		@Override
		public Page<CanLeadModel> getCanLeadModelsByExpYears(Integer expYearsMin, Integer expYearsMax,
				Pageable pageable) {
			// TODO Auto-generated method stub
			 return canLeadRepository.findByExpYearsBetween(expYearsMin, expYearsMax, pageable);
		}

		@Override
		public Page<CanLeadModel> getCanLeadModelsByCreatedTime(Date createdTimeStart,
				Date createdTimeEnd, Pageable pageable) {
			// TODO Auto-generated method stub
			return canLeadRepository.findByCreatedTimeBetween(createdTimeStart,createdTimeEnd,pageable);
		}

		@Override
		public Page<CanLeadModel> getCanLeadModelsByQualified(boolean b, Pageable pageable) {
			// TODO Auto-generated method stub
			return  canLeadRepository.findByQualified(b,pageable);
		}

		@Override
		public Page<CanLeadModel> getCanLeadModelsByNotQualified(boolean b, Pageable pageable) {
			// TODO Auto-generated method stub
			return  canLeadRepository.findByNotQualified(b,pageable);
		}

		@Override
		public Page<CanLeadModel> getCanLeadLeadModelByQualifiedAndNotQualified(boolean b, boolean c,
				Pageable pageable) {
			// TODO Auto-generated method stub
			return canLeadRepository.findByQualifiedAndNotQualified(b,c, pageable);
		}

		@Override
		public Page<CanLeadModel> getCanLeadModels(Specification<CanLeadModel> spec, Pageable pageable) {
			// TODO Auto-generated method stub
			return canLeadRepository.findAll(spec, pageable);
		}

		@Override
		public Page<MidSeniorLevelCandidateLeadModel> findAll(Specification<MidSeniorLevelCandidateLeadModel> spec, Pageable pageable) {
			// TODO Auto-generated method stub
			return midSeniorLevelCandidateLeadRepository.findAll(spec,pageable);
		}

		@Override
		public Page<MidSeniorLevelCandidateLeadModel> getAllCandidates(Pageable pageable) {
			// TODO Auto-generated method stub
			return midSeniorLevelCandidateLeadRepository.findAll(pageable);
		}
		
		@Override
		public List<Map<String, Object>> filterCanLead(int profilePageNo, String fromSource, String jobCategory,
				long mobileNumber, int expYears, int expYears2, String qualificationStatus, String scheduledBy, Date createdTime,
				Date endDate, int page, int size) {

		    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		    CriteriaQuery<CanLeadModel> criteriaQuery = criteriaBuilder.createQuery(CanLeadModel.class);
		    Root<CanLeadModel> root = criteriaQuery.from(CanLeadModel.class);
		    List<Predicate> predicates = new ArrayList<>();

		    // Add conditions based on the provided parameters
		    if (profilePageNo != 0) {
		        predicates.add(criteriaBuilder.equal(root.get("profilePageNo"), profilePageNo));
		    }
		    if (fromSource != null) {
		        Predicate sourcePredicate;
		        switch (fromSource) {
		            case "fromApp":
		                sourcePredicate = criteriaBuilder.equal(root.get("fromApp"), true);
		                break;
		            case "fromWA":
		                sourcePredicate = criteriaBuilder.equal(root.get("fromWA"), true);
		                break;
		            case "fromAdmin":
		                sourcePredicate = criteriaBuilder.equal(root.get("fromAdmin"), true);
		                break;
		            case "fromFbMetaLeadAd":
		                sourcePredicate = criteriaBuilder.equal(root.get("fromFbMetaLeadAd"), true);
		                break;
		            default:
		                sourcePredicate = null; // Handle unknown source values
		                break;
		        }
		        if (sourcePredicate != null) {
		            predicates.add(sourcePredicate);
		        }

		    }
		    if (jobCategory != null) {
		        predicates.add(criteriaBuilder.equal(root.get("jobCategory"), jobCategory));
		    }

		    if (mobileNumber != 0) {
		        predicates.add(criteriaBuilder.or(
		                criteriaBuilder.equal(root.get("mobileNumber"), mobileNumber),
		                criteriaBuilder.equal(root.get("whatsappNumber"), mobileNumber),
		                criteriaBuilder.equal(root.get("contactNumber"), mobileNumber)
		        ));
		    }

		    if (expYears != 0 && expYears2 != 0) {
		        predicates.add(criteriaBuilder.between(root.get("expYears"), expYears, expYears2));
		    }

		    if (qualificationStatus != null) {
		        if ("Qualified".equals(qualificationStatus)) {
		            predicates.add(criteriaBuilder.equal(root.get("qualified"), true));
		        } else if ("NotQualified".equals(qualificationStatus)) {
		            predicates.add(criteriaBuilder.equal(root.get("notQualified"), true));
		        } else {
		            predicates.add(criteriaBuilder.and(
		                    criteriaBuilder.equal(root.get("notQualified"), false),
		                    criteriaBuilder.equal(root.get("qualified"), false)
		            ));
		        }
		    }
		    if (scheduledBy != null) {
		        try {
		            Integer scheduledAdminId = Integer.parseInt(scheduledBy);

		            // Join CanLeadModel with Admin based on the @ManyToOne relationship
		            predicates.add(criteriaBuilder.equal(root.get("assignTo"), scheduledAdminId));
		        } catch (NumberFormatException e) {
		            // Handle the case where "scheduledBy" is not a valid Integer
		            // You might throw an exception, log an error, or handle it accordingly
		            e.printStackTrace(); // or log.error("Invalid adminId format", e);
		        }
		    }

		    if (createdTime != null && endDate != null) {
		        predicates.add(criteriaBuilder.between(root.get("createdTime"), createdTime, endDate));
		    }

		    // Build the final query
		    criteriaQuery.where(predicates.toArray(new Predicate[0]));
		    criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));

		    // Perform the query and pagination
		    TypedQuery<CanLeadModel> typedQuery = entityManager.createQuery(criteriaQuery);
		    typedQuery.setFirstResult((page - 1) * size);
		    typedQuery.setMaxResults(size);
		    List<CanLeadModel> resultList = typedQuery.getResultList();

		    // Process the result list and create the desired map structure
		    List<Map<String, Object>> resultMaps = new ArrayList<>();
		    for (CanLeadModel canLeadModel : resultList) {
		        Map<String, Object> resultMap = new HashMap<>();
		        resultMap.put("CanLeadModel", canLeadModel);

		        // Add additional related entities if needed

		        resultMaps.add(resultMap);
		    }

		    return resultMaps;
		}

		@Override
		public long filterCanLeadCount(int profilePageNo, String fromSource, String jobCategory,
		        long mobileNumber, int expYears, int expYears2, String qualificationStatus, String scheduledBy, Date createdTime,
		        Date endDate) {

		    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		    CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		    Root<CanLeadModel> root = criteriaQuery.from(CanLeadModel.class);
		    List<Predicate> predicates = new ArrayList<>();

		    // Add conditions based on the provided parameters
		    if (profilePageNo != 0) {
		        predicates.add(criteriaBuilder.equal(root.get("profilePageNo"), profilePageNo));
		    }

		    if (fromSource != null) {
		        Predicate sourcePredicate;
		        switch (fromSource) {
		            case "fromApp":
		                sourcePredicate = criteriaBuilder.equal(root.get("fromApp"), true);
		                break;
		            case "fromWA":
		                sourcePredicate = criteriaBuilder.equal(root.get("fromWA"), true);
		                break;
		            case "fromAdmin":
		                sourcePredicate = criteriaBuilder.equal(root.get("fromAdmin"), true);
		                break;
		            case "fromFbMetaLeadAd":
		                sourcePredicate = criteriaBuilder.equal(root.get("fromFbMetaLeadAd"), true);
		                break;
		            default:
		                sourcePredicate = null; // Handle unknown source values
		                break;
		        }
		        if (sourcePredicate != null) {
		            predicates.add(sourcePredicate);
		        }
		    }

		    if (jobCategory != null) {
		        predicates.add(criteriaBuilder.equal(root.get("jobCategory"), jobCategory));
		    }

		    if (mobileNumber != 0) {
		        predicates.add(criteriaBuilder.or(
		                criteriaBuilder.equal(root.get("mobileNumber"), mobileNumber),
		                criteriaBuilder.equal(root.get("whatsappNumber"), mobileNumber),
		                criteriaBuilder.equal(root.get("contactNumber"), mobileNumber)
		        ));
		    }

		    if (expYears != 0 && expYears2 != 0) {
		        predicates.add(criteriaBuilder.between(root.get("expYears"), expYears, expYears2));
		    }

		    if (qualificationStatus != null) {
		        if ("Qualified".equals(qualificationStatus)) {
		            predicates.add(criteriaBuilder.equal(root.get("qualified"), true));
		        } else if ("NotQualified".equals(qualificationStatus)) {
		            predicates.add(criteriaBuilder.equal(root.get("notQualified"), true));
		        } else {
		            predicates.add(criteriaBuilder.and(
		                    criteriaBuilder.equal(root.get("notQualified"), false),
		                    criteriaBuilder.equal(root.get("qualified"), false)
		            ));
		        }
		    }

		    if (scheduledBy != null) {
		        try {
		            Integer scheduledAdminId = Integer.parseInt(scheduledBy);

		            // Join CanLeadModel with Admin based on the @ManyToOne relationship
		            predicates.add(criteriaBuilder.equal(root.get("assignTo"), scheduledAdminId));
		        } catch (NumberFormatException e) {
		            // Handle the case where "scheduledBy" is not a valid Integer
		            // You might throw an exception, log an error, or handle it accordingly
		            e.printStackTrace(); // or log.error("Invalid adminId format", e);
		        }
		    }


		    if (createdTime != null && endDate != null) {
		        predicates.add(criteriaBuilder.between(root.get("createdTime"), createdTime, endDate));
		    }

		    criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));

		    if (predicates != null && !predicates.isEmpty()) {
		        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
		        criteriaQuery.where(predicateArray);
		    }

		    criteriaQuery.select(criteriaBuilder.count(root));

		    try {
		        TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);
		        return query.getSingleResult();
		    } catch (Exception e) {
		        e.printStackTrace(); // Log the exception details
		        return 0; // or handle the exception accordingly
		    }
		}


		@Override
		public CandidateAnalyticsFilterDTO getAnalyticsSummaryByAdminIdAndDateRange(Long adminId, Date startDate,
		        Date endDate) {
		    // TODO Auto-generated method stub
		    List<Object[]> results = candidateAnalyticsRepository.sumValuesByAdminIdAndDateRange(adminId, startDate, endDate);

		    CandidateAnalyticsFilterDTO response = new CandidateAnalyticsFilterDTO();
		    response.setAdminId(adminId);

		    if (results != null && !results.isEmpty() && results.get(0) != null && results.get(0).length == 13) {
		        Object[] result = results.get(0);

		        // Adding null checks for each result element
		        response.setFbMetaLeads(result[0] != null ? ((Number) result[0]).intValue() : 0);
		        response.setQualifiedFbMetaLeads(result[1] != null ? ((Number) result[1]).intValue() : 0);
		        response.setCanLeads(result[2] != null ? ((Number) result[2]).intValue() : 0);
		        response.setQualifiedCanLeads(result[3] != null ? ((Number) result[3]).intValue() : 0);
		        response.setTotalLeads(result[4] != null ? ((Number) result[4]).intValue() : 0);
		        response.setTotalQualifiedLeads(result[5] != null ? ((Number) result[5]).intValue() : 0);
		        response.setCanRegistration(result[6] != null ? ((Number) result[6]).intValue() : 0);
		        response.setCanRegistration(result[7] != null ? ((Number) result[7]).intValue() : 0);
		        response.setInterviewScheduled(result[8] != null ? ((Number) result[8]).intValue() : 0);
		        response.setInterviewAttended(result[9] != null ? ((Number) result[9]).intValue() : 0);
		        response.setCompanySelected(result[10] != null ? ((Number) result[10]).intValue() : 0);
		        response.setOfferAccepted(result[11] != null ? ((Number) result[11]).intValue() : 0);
		        response.setJoined(result[12] != null ? ((Number) result[12]).intValue() : 0);

		        // Log the results for debugging
		        System.out.println("Results: " + Arrays.toString(result));
		    } else {
		        // Handle the case when results are null or empty
		        // You may want to log a warning or handle it differently based on your requirements
		        System.out.println("No results found for adminId: " + adminId + ", startDate: " + startDate + ", endDate: " + endDate);
		    }

		    response.setStartDate(startDate);
		    response.setEndDate(endDate);

		    return response;
		}

		@Override
		public List<CandidateAnalyticsFilterDTO> getAnalyticsSummaryByDateRange(Date startDate, Date endDate) {
		    List<Object[]> results = candidateAnalyticsRepository.sumValuesByDateRange(startDate, endDate);

		    Map<Long, CandidateAnalyticsFilterDTO> summariesMap = new HashMap<>();

		    if (results != null && !results.isEmpty()) {
		        for (Object[] result : results) {
		            if (result != null && result.length == 13) {
		                CandidateAnalyticsFilterDTO summary = new CandidateAnalyticsFilterDTO();
		                summary.setAdminId(((Number) result[0]).longValue());
		                summary.setFbMetaLeads(result[1] != null ? ((Number) result[1]).intValue() : 0);
		                summary.setQualifiedFbMetaLeads(result[2] != null ? ((Number) result[2]).intValue() : 0);
		                summary.setCanLeads(result[3] != null ? ((Number) result[3]).intValue() : 0);
		                summary.setQualifiedCanLeads(result[4] != null ? ((Number) result[4]).intValue() : 0);
		                summary.setTotalLeads(result[5] != null ? ((Number) result[5]).intValue() : 0);
		                summary.setTotalQualifiedLeads(result[6] != null ? ((Number) result[6]).intValue() : 0);
		                summary.setCanRegistration(result[7] != null ? ((Number) result[7]).intValue() : 0);
		                summary.setInterviewScheduled(result[8] != null ? ((Number) result[8]).intValue() : 0);
		                summary.setInterviewAttended(result[9] != null ? ((Number) result[9]).intValue() : 0);
		                summary.setCompanySelected(result[10] != null ? ((Number) result[10]).intValue() : 0);
		                summary.setOfferAccepted(result[11] != null ? ((Number) result[11]).intValue() : 0);
		                summary.setJoined(result[12] != null ? ((Number) result[12]).intValue() : 0);
		                summary.setStartDate(startDate);
		                summary.setEndDate(endDate);

		                summariesMap.put(summary.getAdminId(), summary);
		            }
		        }
		    }

		    // Add summaries for adminIds not present in the results
		    List<Long> adminIdsInResults = results.stream()
		            .map(result -> ((Number) result[0]).longValue())
		            .collect(Collectors.toList());

		    List<Long> allAdminIds = getAllAdminIds(); 

		    for (Long adminId : allAdminIds) {
		        if (!adminIdsInResults.contains(adminId)) {
		            CandidateAnalyticsFilterDTO summary = new CandidateAnalyticsFilterDTO();
		            summary.setAdminId(adminId);
		            summary.setStartDate(startDate);
		            summary.setEndDate(endDate);
		            summariesMap.put(adminId, summary);
		        }
		    }

		    return new ArrayList<>(summariesMap.values());
		}
		public List<Long> getAllAdminIds() {
		    
		    return candidateAnalyticsRepository.findAllAdminIds();
		}

		@Override
		public List<Map<String, Object>> filterMidSenior(int page, int size) {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	        CriteriaQuery<MidSeniorCandidateReportModel> criteriaQuery = criteriaBuilder.createQuery(MidSeniorCandidateReportModel.class);
	        Root<MidSeniorCandidateReportModel> root = criteriaQuery.from(MidSeniorCandidateReportModel.class);
	        List<Predicate> predicates = new ArrayList<>();
	        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdTime")));
	        
	        if (predicates != null && predicates.size() != 0) {
				Predicate[] predicate = predicates.toArray(new Predicate[0]);
				criteriaQuery.where(predicate);
			}
	        
	        List<MidSeniorCandidateReportModel> resultList = entityManager.createQuery(criteriaQuery)
	                .setFirstResult((page - 1) * size)
	                .setMaxResults(size)
	                .getResultList();
	        List<Map<String, Object>> resultMaps = new ArrayList<>();
	        for (MidSeniorCandidateReportModel candidate : resultList) {
	            Map<String, Object> resultMap = new HashMap<>();
	            resultMap.put("MidSeniorCandidateReport", candidate);
	            resultMaps.add(resultMap);
	        }
	        return resultMaps;
		}
		@Override
		public long filterMidSeniorCount() {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
	        Root<MidSeniorCandidateReportModel> root = criteriaQuery.from(MidSeniorCandidateReportModel.class);
	        List<Predicate> predicates = new ArrayList<>();
	        
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
		public List<Map<String, Object>> filtercandidates(String companyName, int adminId, long contactNumber,
				Date startDate, Date endDate, int page, int size) {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	        CriteriaQuery<CanInterviewsModel> criteriaQuery = criteriaBuilder.createQuery(CanInterviewsModel.class);
	        Root<CanInterviewsModel> root = criteriaQuery.from(CanInterviewsModel.class);
	        List<Predicate> predicates = new ArrayList<>();
	        
	        if(companyName!=null) {
				predicates.add(criteriaBuilder.like(root.get("companyName"), "%" + companyName + "%"));
			}
	        if (contactNumber != 0) {
		        predicates.add(criteriaBuilder.or(
		        		criteriaBuilder.equal(root.get("contactNumber"), contactNumber)
		        ));
		    }
	        if (adminId != 0L) {
		        predicates.add(criteriaBuilder.equal(root.get("adminId"), adminId));
		    }
	        
	        if (startDate != null && endDate != null) {
		        predicates.add(criteriaBuilder.between(root.get("createdTime"), startDate, endDate));
		    }

	        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("joinedOn")));
	        predicates.add(criteriaBuilder.and(
		            criteriaBuilder.isFalse(root.get("isLeftTheCompany")),
	        criteriaBuilder.isTrue(root.get("isJoined"))));
	       
	        
	        if (predicates != null && predicates.size() != 0) {
				Predicate[] predicate = predicates.toArray(new Predicate[0]);
				criteriaQuery.where(predicate);
			}
	        
	        List<CanInterviewsModel> resultList = entityManager.createQuery(criteriaQuery)
	                .setFirstResult((page - 1) * size)
	                .setMaxResults(size)
	                .getResultList();
	        List<Map<String, Object>> resultMaps = new ArrayList<>();
	        for (CanInterviewsModel candidate : resultList) {
	            Map<String, Object> resultMap = new HashMap<>();
	            resultMap.put("candidateInterview", candidate);
	            resultMaps.add(resultMap);

	            CandidateModel candidateModel = entityManager.find(CandidateModel.class, candidate.getCanId());
		        resultMap.put("CandidateModel", candidateModel);
		        
		        JobsModel jobsModel = entityManager.find(JobsModel.class, candidate.getJobId());
		        resultMap.put("JobsModel", jobsModel);
		        
		        EmployerModel employerModel = entityManager.find(EmployerModel.class, jobsModel.getEmployerId());
		        resultMap.put("EmployerModel", employerModel);
	        }
	        return resultMaps;
		}

		@Override
		public long filterCandidatesCount(String companyName, int adminId, long contactNumber, Date startDate,
				Date endDate) {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
	        Root<CanInterviewsModel> root = criteriaQuery.from(CanInterviewsModel.class);
	        List<Predicate> predicates = new ArrayList<>();
	        
	        if(companyName!=null) {
				predicates.add(criteriaBuilder.like(root.get("companyName"), "%" + companyName + "%"));
			}
	        if (contactNumber != 0) {
		        predicates.add(criteriaBuilder.or(
		        		criteriaBuilder.equal(root.get("contactNumber"), contactNumber)
		        ));
		    }
	        if (adminId != 0) {
				predicates.add(criteriaBuilder.equal(root.get("adminId"), adminId));
			}
			else if(adminId==0) {
		    	  criteriaQuery.select(criteriaBuilder.count(root));
		    }
	        if (startDate != null && endDate != null) {
		        predicates.add(criteriaBuilder.between(root.get("createdTime"), startDate, endDate));
		    }
	        
	        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("joinedOn")));
	        predicates.add(criteriaBuilder.and(
		            criteriaBuilder.isFalse(root.get("isLeftTheCompany")),
	        criteriaBuilder.isTrue(root.get("isJoined"))));
	        
	        if (predicates != null && !predicates.isEmpty()) {
	            Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
	            criteriaQuery.where(predicateArray);
	        }
	        criteriaQuery.select(criteriaBuilder.count(root)); // Count the results
	        TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);
	        long totalCount = query.getSingleResult();
	        return totalCount;
		}
		
}