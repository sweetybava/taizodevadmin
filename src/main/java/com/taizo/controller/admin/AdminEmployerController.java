package com.taizo.controller.admin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityNotFoundException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import com.taizo.DTO.InvoiceFilterDTO;
import com.taizo.controller.admin.AdminDashboardController.CanInterviewFilterResponse;
import com.taizo.controller.gallabox.ExotelCallController;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.*;
import com.taizo.utils.FreeMarkerUtils;
import freemarker.template.TemplateException;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import com.taizo.repository.AdminAnalyticsRepository;
import com.taizo.repository.AdminRepository;
import com.taizo.repository.EmpActivityRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.LeadRepository;
import com.taizo.repository.PlansRepository;
import com.taizo.utils.FreeMarkerUtils;

import freemarker.template.TemplateException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.apache.commons.io.FilenameUtils;


@CrossOrigin
@RestController
@RequestMapping("/admin")
public class AdminEmployerController {
	
	private final Logger logger = LoggerFactory.getLogger(AdminEmployerController.class);

	@Autowired
	EmployerFieldLeadRepository employerFieldLeadRepository;

	@Autowired
	EmployerService employerService;

	@Autowired
	EmployerService employerservice;

	@Autowired
	EmpActivityRepository empActivityRepository;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	PlansRepository plansRepository;

	@Autowired
	private FreeMarkerUtils freeMarkerUtils;

	@Autowired
	LeadRepository leadRepository;

	@Autowired
	EmpEnquiryService enquiryService;

	@Autowired
	AmazonSESMailUtil amazonSESMailUtil;

	@Autowired
	AdminRepository adminRepository;

	@Autowired
	JobService jobService;

	@Autowired
	AdminService adminService;

	@Autowired
	ExotelCallController exotelCallController;

	@Autowired
	EmployerTimelineRepository employerTimelineRepository;
	
	@Autowired
	EmployerDocumentsRepository employerDocumentsRepository;
	
	@Autowired
	CfgEmployerDocumentsRepository cfgEmployerDocumentsRepository;

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Value("${property.base.url}")
	private String baseUrl;
	
	@Value("${aws.s3.audio.bucket}")
	private String bucketName;
	
	@Value("${aws.s3.region}")
	private String awsRegion;

	@Autowired
	AdminAnalyticsRepository adminAnalyticsRepository;

	@Autowired
	AdminKYCController adminKYCController;

	@Autowired
	EmployerPaymentRepository employerPaymentRepository;
	
	@Autowired
	private AmazonS3 s3client;


		
	

	@GetMapping(path = "/empDetails")
	public ResponseEntity<?> employerrDetails(@RequestParam("emp_id") final int id) {

		Optional<EmployerModel> emp = employerRepository.findById(id);
		if (emp != null) {
			EmployerModel existing = emp.get();
			existing.setPassword("");

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", existing);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

		}
	}

	public class EmpEnquiryFilterResponse {
		private long totalCount;
		private List<Map<String, Object>> empEnquiryList;

		public EmpEnquiryFilterResponse(long totalCount, List<Map<String, Object>> empEnquiryList) {
			this.totalCount = totalCount;
			this.empEnquiryList = empEnquiryList;
		}

		public long getTotalCount() {
			return totalCount;
		}

		public List<Map<String, Object>> getEmpEnquiryList() {
			return empEnquiryList;
		}
	}

	@PostMapping("/filterByEmpEnquiry")
	public ResponseEntity<EmpEnquiryFilterResponse> filterEmpEnquirys(@RequestBody EmpEnquiryModel empEnquiryModel) {
		List<Map<String, Object>> filterEmpEnquirys = enquiryService.filterEmpEnquiry(empEnquiryModel.getMobileNumber(),
				empEnquiryModel.getEmailId(), empEnquiryModel.getCompanyName(), empEnquiryModel.getPage(),
				empEnquiryModel.getSize(), empEnquiryModel.getCreatedTime(), empEnquiryModel.getEndDate());
		long totalCount = enquiryService.filterempEnquiryCount(empEnquiryModel.getMobileNumber(),
				empEnquiryModel.getEmailId(), empEnquiryModel.getCompanyName(), empEnquiryModel.getPage(),
				empEnquiryModel.getSize(), empEnquiryModel.getCreatedTime(), empEnquiryModel.getEndDate());
		EmpEnquiryFilterResponse response = new EmpEnquiryFilterResponse(totalCount, filterEmpEnquirys);

		return ResponseEntity.ok(response);
	}

	public class EmployerFilterResponse {
		private long totalCount;
		private List<Map<String, Object>> employerList;

		public EmployerFilterResponse(long totalCount, List<Map<String, Object>> employerList) {
			this.totalCount = totalCount;
			this.employerList = employerList;
		}

		public long getTotalCount() {
			return totalCount;
		}

		public List<Map<String, Object>> getEmployerList() {
			return employerList;
		}
	}

	@PostMapping("/filterByEmployerDetails")
	public ResponseEntity<EmployerFilterResponse> filterEmployers(@RequestBody EmployerModel employer,
			@RequestParam(name = "contactNumber", required = false) String contactNumber) {
		List<Map<String, Object>> filteredEmployer = employerService.filterEmployer(employer.getId(),
				employer.getCompanyName(), employer.getIndustry(), employer.getNoOfEmployees(), employer.getCity(),
				employer.getArea(), contactNumber, employer.getPage(), employer.getSize(), employer.getCreatedTime(),
				employer.getEndDate());
		long totalCount = employerService.filterEmployerCount(employer.getId(), employer.getCompanyName(),
				employer.getIndustry(), employer.getNoOfEmployees(), employer.getCity(), employer.getArea(),
				contactNumber, employer.getPage(), employer.getSize(), employer.getCreatedTime(),
				employer.getEndDate());

		EmployerFilterResponse response = new EmployerFilterResponse(totalCount, filteredEmployer);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/empLeadFromAdmin")
	public ResponseEntity<Map<String, Object>> EmployerLeads(
	        @RequestParam(value = "email_id", required = false) String emailId,
	        @RequestParam(value = "mobile_number", required = false) Long mobileNumber,
	        @RequestParam(value = "company_name", required = false) String companyName,
	        @RequestParam(value = "industry", required = false) String industry,
	        @RequestParam(value = "city", required = false) String city,
	        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") String createdTimeStart,
	        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") String createdTimeEnd,
	        @RequestParam(required = false, defaultValue = "0") Integer page,
	        @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
	    boolean fromAdmin = true;

	    Page<LeadModel> result = null;
	    Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdTime").descending());

	    Specification<LeadModel> spec = Specifications.where(null);

	    if (fromAdmin && emailId != null && !emailId.isEmpty()) {
	        spec = spec.and((root, query, builder) -> builder.equal(root.get("emailId"), emailId));
	    }

	    if (fromAdmin && mobileNumber != null) {
	        spec = spec.and((root, query, builder) -> builder.equal(root.get("mobileNumber"), mobileNumber));
	    }

	    if (fromAdmin && companyName != null) {
	        spec = spec.and((root, query, builder) -> builder.like(root.get("companyName"), "%" + companyName + "%"));
	    }

	    if (fromAdmin && industry != null) {
	        spec = spec.and((root, query, builder) -> builder.equal(root.get("industry"), industry));
	    }

	    if (fromAdmin && city != null) {
	        spec = spec.and((root, query, builder) -> builder.equal(root.get("city"), city));
	    }

	    if (createdTimeStart != null && createdTimeEnd != null) {
	        LocalDate startDate = LocalDate.parse(createdTimeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	        LocalDate endDate = LocalDate.parse(createdTimeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

	        Date startDateTime = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	        Date endDateTime = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

	        spec = spec.and((root, query, builder) -> builder.between(root.get("createdTime"), startDateTime, endDateTime));
	    }
	    
	    spec = spec.and((root, query, builder) -> builder.equal(root.get("deactivated"), false));

	    result = employerService.findAll(spec, pageable);

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




	public class ProformaFilterResponse {
		private long totalCount;
		private List<Map<String, Object>> proformaList;

		public ProformaFilterResponse(long totalCount, List<Map<String, Object>> proformaList) {
			this.totalCount = totalCount;
			this.proformaList = proformaList;
		}

		public long getTotalCount() {
			return totalCount;
		}

		public List<Map<String, Object>> getProformaList() {
			return proformaList;
		}
	}

	@PostMapping("/filterByProformaInvoice")
	public ResponseEntity<ProformaFilterResponse> filterEmployerProforma(
			@RequestBody EmpProformaInvoiceModel proformaModel) {

		List<Map<String, Object>> filteredProforma = employerService.filterProforma(proformaModel.getEmployerId(),
				proformaModel.getCompanyName(), proformaModel.getMobileNumber(), proformaModel.getPage(),
				proformaModel.getSize(), proformaModel.getCreatedTime(), proformaModel.getEndDate());
		long totalCount = employerService.filterProformaCount(proformaModel.getEmployerId(),
				proformaModel.getCompanyName(), proformaModel.getMobileNumber(), proformaModel.getPage(),
				proformaModel.getSize(), proformaModel.getCreatedTime(), proformaModel.getEndDate());
		ProformaFilterResponse response = new ProformaFilterResponse(totalCount, filteredProforma);

		return ResponseEntity.ok(response);

	}

	@GetMapping("/placementPlans")
	public ResponseEntity<?> getPlacementPlans(@RequestParam(value = "emp_id", required = false) final int empId,
			@RequestParam("type") final String type) {

		boolean Ctype = type.equalsIgnoreCase("Experienced");

		List<PlansModel> list = plansRepository.findByExperiencedStatus(Ctype, true);
		if (list != null) {
			Map<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", list.get(0));
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "No Plans Available");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping("/sendIntroMail")
	public ResponseEntity<?> sendIntroMail(
	        @RequestParam int id,
	        @RequestParam Long adminId,
	        @RequestParam(required = false) String ccMail
	) throws IOException, TemplateException {

	    Optional<LeadModel> leadMN = leadRepository.findById(id);

	    if (!leadMN.isPresent()) {
	        return ResponseEntity.badRequest().body(createErrorResponse("Lead not found", 400));
	    }

	    LeadModel leademp = leadMN.get();

	    Optional<Admin> adminOptional = adminRepository.findById(adminId);

	    if (!adminOptional.isPresent()) {
	        return ResponseEntity.badRequest().body(createErrorResponse("Admin not found", 400));
	    }

	    Admin admin = adminOptional.get();
	    String signature = admin.getEmailSignature();

	    String companyName = leademp.getCompanyName();
	    String contactPersonName = leademp.getContactPersonName();

	    try {
	        sendIntroEmail(leademp, admin, companyName, contactPersonName, signature, ccMail);
	        return ResponseEntity.ok(createSuccessResponse("Intro email sent successfully", "success", 200));
	    } catch (IOException | TemplateException e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(createErrorResponse("Error sending intro email", 500));
	    }
	}
	
	

	@PostMapping("/employerLead")
    public ResponseEntity<?> updateLeadEmployers(
            @RequestBody LeadModel leademp,
            @RequestParam("admin_id") Long adminId,
            @RequestParam(required = false) String ccMail
    ) throws IOException, TemplateException {

        EmployerModel em = employerRepository.findByMobileNumber(leademp.getMobileNumber());
        List<LeadModel> leadMN = leadRepository.findByMobileNumberList(leademp.getMobileNumber());

        String emailId = leademp.getEmailId();
        String companyName = leademp.getCompanyName();
        String contactPersonName = leademp.getContactPersonName();

		Optional<Admin> adminOptional = adminRepository.findById(adminId);

        if (!adminOptional.isPresent()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Admin not found", 400));
        }

        Admin admin = adminOptional.get();
        String fromEmailId = admin.getEmailId();
        String signature = admin.getEmailSignature();
        String module = admin.getModule();

        if (em != null) {
            return ResponseEntity.badRequest().body(createErrorResponse("User with mobile number already registered in employer table", 400));
        } else if (!leadMN.isEmpty()) {
            LeadModel existingLead = leadMN.get(0);
            updateLeadDetails(existingLead, leademp, adminId);
        } else {
            leadRepository.save(leademp);
            saveEmployerTimeline(leademp, adminId);

            if (emailId != null && !emailId.isEmpty()) {
                sendIntroEmail(leademp, admin, companyName, contactPersonName, signature, ccMail);
                updateAdminAnalytics(adminId, module);
            }
        }

        return ResponseEntity.ok(createSuccessResponse(leademp, "success", 200));
    }

    private void updateLeadDetails(LeadModel existingLead, LeadModel leademp, Long adminId) {
        existingLead.setEmailId(leademp.getEmailId());
        existingLead.setMobileNumber(leademp.getMobileNumber());
        existingLead.setMobileCountryCode(leademp.getMobileCountryCode());
        existingLead.setFromAdmin(true);
        existingLead.setCompanyName(leademp.getCompanyName());
        existingLead.setContactPersonName(leademp.getContactPersonName());
        existingLead.setNotes(leademp.getNotes());
        existingLead.setAddress(leademp.getAddress());
        existingLead.setIndustry(leademp.getIndustry());
        existingLead.setCity(leademp.getCity());
        existingLead.setAssignTo(adminId);

        leadRepository.save(existingLead);
    }

    private void saveEmployerTimeline(LeadModel leademp, Long adminId) {
    	
    	Optional<Admin> adminOptional = adminRepository.findById(adminId);
    	Admin admin = adminOptional.get();
        EmployerTimeline employerTimeline = new EmployerTimeline();
        Date currentDate1 = new Date();
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate1 = dateFormat1.format(currentDate1);
        String eventDescription = "Lead Generation on <b>" + formattedDate1 + "</b> By <b>" + admin.getUserName()
                + "</b>";
        employerTimeline.setEmpId(0);
        employerTimeline.setEmpLeadId(leademp.getId());
        employerTimeline.setEventName("Lead Generation");
        employerTimeline.setEventDescription(eventDescription);
        employerTimelineRepository.save(employerTimeline);
    }

    private void sendIntroEmail( LeadModel leademp, Admin admin,String companyName,String contactPersonName,
                                 String signature,String ccMail) throws IOException, TemplateException {
        String emailId = leademp.getEmailId();
        Map<String, String> emailDataHM = new HashMap<>();
        emailDataHM.put("ContactPersonName", contactPersonName.trim());
        emailDataHM.put("ExecutiveName", admin.getUserName());
        emailDataHM.put("signature", signature);

        String message = freeMarkerUtils.getHtml1("EmpLeadIntro.html", emailDataHM);
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        ConverterProperties converterProperties = new ConverterProperties();
        converterProperties.setBaseUri("http://localhost:8000");
        HtmlConverter.convertToPdf(message, target, converterProperties);
        byte[] bytes = target.toByteArray();

        if (ccMail != null && !ccMail.isEmpty()) {
            // Pass ccMail only if it is provided
            amazonSESMailUtil.sendIntroEmail(emailId, admin.getId(), message, bytes, companyName, ccMail);
        } else {
            // Send email without ccMail
            amazonSESMailUtil.sendIntroEmail(emailId, admin.getId(), message, bytes, companyName, null);
        }
        leademp.setEmailNotification(true);
        leademp.setIntroEmailOn(new Date());
        leadRepository.save(leademp);

        Date currentDate1 = new Date();
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate1 = dateFormat1.format(currentDate1);

        EmployerTimeline employerTimeline1 = new EmployerTimeline();
        String eventDescription1 = "Intro Email send On  <b>" + formattedDate1 + "</b> By <b>"
                + admin.getUserName() + "</b>";
        employerTimeline1.setEmpId(0);
        employerTimeline1.setEmpLeadId(leademp.getId());
        employerTimeline1.setEventName("Intro Email");
        employerTimeline1.setEventDescription(eventDescription1);
        employerTimelineRepository.save(employerTimeline1);
    }

    private void updateAdminAnalytics(Long adminId, String module) {
        List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(Long.valueOf(adminId));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate currentDate = LocalDate.now();
        String formattedDate = currentDate.format(dateFormatter);

        if (!adminAnalyticsList.isEmpty()) {
            boolean dateMatch = false;

            for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
                LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
                if (currentDate.isEqual(createdOnDate)) {
                    dateMatch = true;
                    adminAnalytics.setEmpNewLeadCount(adminAnalytics.getEmpNewLeadCount() != null
                            ? adminAnalytics.getEmpNewLeadCount() + 1
                            : 1);
                }
            }

            if (!dateMatch) {
                AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
                newAdminAnalytics.setAdminId(Long.valueOf(adminId));
                newAdminAnalytics.setModule(module);
                newAdminAnalytics.setEmpNewLeadCount(1);
                adminAnalyticsList.add(newAdminAnalytics);
            }

            adminAnalyticsRepository.saveAll(adminAnalyticsList);
        } else {
            AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
            adminAnalytics.setAdminId(Long.valueOf(adminId));
            adminAnalytics.setModule(module);
            adminAnalytics.setEmpNewLeadCount(1);
            adminAnalyticsRepository.save(adminAnalytics);
        }
    }

    private Map<String, Object> createErrorResponse(String message, int code) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "error");
        map.put("message", message);
        map.put("code", code);
        return map;
    }

    private Map<String, Object> createSuccessResponse(Object data, String message, int code) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        map.put("message", message);
        map.put("code", code);
        map.put("data", data);
        return map;
    }

	@GetMapping("/leadDetails")
	public ResponseEntity<?> searchLeadByMobileNumberOrEmailId(@RequestParam String input) {
		long mobileNumber;
		try {
			mobileNumber = Long.parseLong(input);
		} catch (NumberFormatException e) {
			mobileNumber = -1;
		}

		LeadModel lead = leadRepository.findByMobileNumberOrEmailId(mobileNumber, input);

		if (lead != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", lead);
			return ResponseEntity.ok(map);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 404);
			map.put("message", "Lead not found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
		}
	}

	@PostMapping(path = "/employerRegistration", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> employerRegistration(@RequestBody EmployerModel emp,
			@RequestParam(value = "admin_id", required = false,defaultValue = "0") Long adminId)
			throws IOException {
		List<LeadModel> leadMN = leadRepository.findByMobileNumberList(emp.getMobileNumber());
		EmployerModel employerModel = employerRepository.findByMobileNumber(emp.getMobileNumber());
		EmployerModel empRegProof = employerRepository.findTopByregProofNumber(emp.getRegProofNumber());

		Optional<Admin> adminOptional = adminRepository.findById(adminId);
		Admin admin = adminOptional.get();

		String token = UUID.randomUUID().toString();

		if (employerModel == null && empRegProof == null) {
			employerModel = new EmployerModel();
			employerModel.setEmailId(emp.getEmailId());
			employerModel.setMobileNumber(emp.getMobileNumber());
			employerModel.setMobileCountryCode(emp.getMobileCountryCode());
			employerModel.setMnVerified(true);
			employerModel.setDeactivated(false);
			employerModel.setEmailVerified(false);
			employerModel.setCountry("India");
			employerModel.setEmailNotification(true);
			employerModel.setPushNotification(true);
			employerModel.setWhatsappNotification(true);
			employerModel.setNotificationSound(true);
			employerModel.setUsedFreeTrial("Yes");
			employerModel.setFromApp(false);
			employerModel.setFromWeb(false);
			employerModel.setFromAdmin(true);

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			employerModel.setLastLoginDate(dtf.format(now));

			employerModel.setIndustry(emp.getIndustry());
			employerModel.setCompanyName(emp.getCompanyName());

			if (emp.getCategory() != null) {
				employerModel.setCategory(emp.getCategory());
			} else {
				employerModel.setCategory("Company");
			}

			employerModel.setCompanyDetailsFilled(true);
			employerModel.setContactDetailsFilled(false);

			if (emp.getAddress() != null && !emp.getAddress().isEmpty()) {
				employerModel.setState(emp.getState());
				employerModel.setAddress(emp.getAddress());
				employerModel.setCity(emp.getCity());
				employerModel.setArea(emp.getArea());
				employerModel.setCountry(emp.getCountry());
				employerModel.setPincode(emp.getPincode());
				employerModel.setLatitude(emp.getLatitude());
				employerModel.setLongitude(emp.getLongitude());
			}

			employerModel.setMobileNumber(emp.getMobileNumber());
			employerModel.setMobileCountryCode(emp.getMobileCountryCode());
			employerModel.setToken(token);
			employerModel.setContactPersonName(emp.getContactPersonName());
			employerModel.setPhoneCountryCode(emp.getPhoneCountryCode());
			employerModel.setPhone(Long.valueOf(emp.getPhone()));
			employerModel.setWebsiteUrl(emp.getWebsiteUrl());
			employerModel.setYearFounded(emp.getYearFounded());
			employerModel.setWhatsappNumber(Long.valueOf(emp.getWhatsappNumber()));
			employerModel.setNoOfEmployees(emp.getNoOfEmployees());
			employerModel.setContactDetailsFilled(true);
			employerModel.setReference(emp.getReference());

			if (emp.getRegProofNumber() != null && !emp.getRegProofNumber().isEmpty()) {
				kycInitiated(employerModel);
			}

			String image = employerModel.getCompanyLogo();
			if (image != null && !image.isEmpty()) {
				boolean imageResult = employerservice.deleteCompanyLogo(image);
			}

			if (emp.getRegProofNumber() != null && !emp.getRegProofNumber().isEmpty()) {
				EmployerModel em1 = employerRepository.findTopByregProofNumber(emp.getRegProofNumber());
				if (em1 != null) {
					employerModel.setUsedFreeTrial("Yes");
				}
			}

			if (emp.getRegProofNumber() != null && !emp.getRegProofNumber().isEmpty()) {
				employerModel.setRegProofNumber(emp.getRegProofNumber());
				employerModel.setKycStatus("U");

				DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
				String date = formatter.format(new Date());
				SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
				Calendar cal = Calendar.getInstance();
				String time = simpleDateFormat1.format(cal.getTime());

			}
			if (adminId != 0)
				employerModel.setAssignTo(2);

			for (LeadModel lead : leadMN) {
				leadRepository.delete(lead);
			}

			employerRepository.save(employerModel);

			if (adminOptional.isPresent()) {
			EmployerTimeline employerTimeline = new EmployerTimeline();
			Date currentDate1 = new Date();
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
			String formattedDate1 = dateFormat1.format(currentDate1);
			String eventDescription = "Registration on  <b>" + formattedDate1 + "</b> By <b>" + admin.getUserName()
					+ "</b>";
			employerTimeline.setEmpId(employerModel.getId());
			employerTimeline.setEmpLeadId(0);
			employerTimeline.setEventName("Registration");
			employerTimeline.setEventDescription(eventDescription);
			employerTimelineRepository.save(employerTimeline);
			}

		} else if (emp != null || empRegProof != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "failed");
			map.put("message", "already register");
			map.put("code", 400);
			return ResponseEntity.badRequest().body(map);

		}

		HashMap<String, Object> map = new HashMap<>();
		map.put("status", "success");
		map.put("message", "success");
		map.put("code", 200);
		map.put("data", employerModel);
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	private void kycInitiated(EmployerModel existing) {

	}

	@PutMapping(path = "/employerRegistration")
	public ResponseEntity<?> updateProfile(@RequestBody EmployerModel emp) {

		Optional<EmployerModel> details = employerRepository.findById(emp.getId());

		if (details.isPresent()) {

			EmployerModel existing = details.get();

			if (emp.getIndustry() != null && !emp.getIndustry().isEmpty()) {
				existing.setIndustry(emp.getIndustry());
			}

			if (emp.getCategory() != null && !emp.getCategory().isEmpty()) {
				existing.setCategory(emp.getCategory());
			}

			if (emp.getCompanyName() != null && !emp.getCompanyName().isEmpty()) {
				existing.setCompanyName(emp.getCompanyName());
			}

			if (emp.getEmailId() != null && !emp.getEmailId().isEmpty()) {
				existing.setEmailId(emp.getEmailId());
			}

			if (emp.getCity() != null && !emp.getCity().isEmpty()) {
				existing.setArea(emp.getArea());
			}

			if (emp.getArea() != null && !emp.getArea().isEmpty()) {
				existing.setArea(emp.getArea());
			}

			if (emp.getAddress() != null && !emp.getAddress().isEmpty()) {
				existing.setAddress(emp.getAddress());
				existing.setCity(emp.getCity());
				existing.setState(emp.getState());
				existing.setCountry(emp.getCountry());
				existing.setPincode(emp.getPincode());
				existing.setLatitude(emp.getLatitude());
				existing.setLongitude(emp.getLongitude());
			}
			if (emp.getContactPersonName() != null && !emp.getContactPersonName().isEmpty()) {

				existing.setContactPersonName(emp.getContactPersonName());
			}

			if (emp.getWhatsappNumber() != 0) {
				existing.setWhatsappNumber(emp.getWhatsappNumber());
			}

			if (emp.getWebsiteUrl() != null && !emp.getWebsiteUrl().isEmpty()) {
				existing.setWebsiteUrl(emp.getWebsiteUrl());
			}

			if (emp.getMobileCountryCode() != null && !emp.getMobileCountryCode().isEmpty()) {
				existing.setMobileCountryCode(emp.getMobileCountryCode());
			}

			if (emp.getMobileNumber() != 0) {
				existing.setMobileNumber(Long.valueOf(emp.getMobileNumber()));
			}

			if (emp.getNoOfEmployees() != null && !emp.getNoOfEmployees().isEmpty()) {
				existing.setNoOfEmployees(emp.getNoOfEmployees());
			}

			if (emp.getPhoneCountryCode() != null && !emp.getPhoneCountryCode().isEmpty()) {
				existing.setPhoneCountryCode(emp.getPhoneCountryCode());

			}

			if (emp.getPhone() != 0) {
				existing.setPhone(Long.valueOf(emp.getPhone()));

			}

			if (emp.getYearFounded() != null && !emp.getYearFounded().isEmpty()) {
				existing.setYearFounded(emp.getYearFounded());
			}

			if (emp.getRegProofNumber() != null && !emp.getRegProofNumber().isEmpty()) {
				existing.setRegProofNumber(emp.getRegProofNumber());
			}

			if (emp.getReference() != null && !emp.getReference().isEmpty()) {
				existing.setReference(emp.getReference());
			}
			existing.setAssignTo(emp.getAssignTo());

			employerRepository.save(existing);

			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(existing.getId());
			EA.setActivity("Your <b>company profile</b> has been updated!");
			empActivityRepository.save(EA);

			EmployerTimeline employerTimeline = new EmployerTimeline();
			Date currentDate1 = new Date();
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
			String formattedDate1 = dateFormat1.format(currentDate1);
			String eventDescription = "Profile Updated by Admin on " + formattedDate1;
			employerTimeline.setEmpId(emp.getId());
			employerTimeline.setEmpLeadId(0);
			employerTimeline.setEventName("Profile Updated");
			employerTimeline.setEventDescription(eventDescription);
			employerTimelineRepository.save(employerTimeline);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "Saved Succesfully");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

	@PutMapping("/empLeadStatus")
	public ResponseEntity<Map<String, Object>> empUpdateStatus(@RequestParam int empLeadId, @RequestParam Long adminId,
			@RequestParam boolean qualified, @RequestParam boolean notQualified) {

		Optional<LeadModel> optionalLeadModel = leadRepository.findById(empLeadId);

		Map<String, Object> response = new HashMap<>();

		if (optionalLeadModel.isPresent()) {
			LeadModel leadModel = optionalLeadModel.get();
			Admin a = adminRepository.findById(adminId).orElse(null);

			if (qualified) {
				leadModel.setQualified(true);
				leadModel.setNotQualified(false);
				leadRepository.save(leadModel);

				List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository
						.findByAdminId(Long.valueOf(adminId));

				if (!adminAnalyticsList.isEmpty()) {
					// Check if the createdOn date is the same as the current date
					LocalDate currentDate = LocalDate.now();
					boolean dateMatch = false;

					for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
						LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
						if (currentDate.isEqual(createdOnDate)) {
							dateMatch = true;
							adminAnalytics.setEmpQualifiedCount(adminAnalytics.getEmpQualifiedCount() != null
									? adminAnalytics.getEmpQualifiedCount() + 1
									: 1);
						}
					}
					if (!dateMatch) {

						// If the dates are different for all records, insert a new record
						AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
						newAdminAnalytics.setAdminId(Long.valueOf(adminId));
						newAdminAnalytics.setModule(a.getModule());
						newAdminAnalytics.setEmpQualifiedCount(1);
						adminAnalyticsList.add(newAdminAnalytics);
					}

					adminAnalyticsRepository.saveAll(adminAnalyticsList);
				} else {

					// If there are no existing records for the adminId, insert a new record
					AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
					adminAnalytics.setAdminId(Long.valueOf(adminId));
					adminAnalytics.setModule(a.getModule());
					adminAnalytics.setEmpQualifiedCount(1);
					adminAnalyticsRepository.save(adminAnalytics);
				}
			} else if (notQualified) {
				leadModel.setQualified(false);
				leadModel.setNotQualified(true);
				leadRepository.save(leadModel);

				List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository
						.findByAdminId(Long.valueOf(adminId));

				if (!adminAnalyticsList.isEmpty()) {
					// Check if the createdOn date is the same as the current date
					LocalDate currentDate = LocalDate.now();
					boolean dateMatch = false;

					for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
						LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
						if (currentDate.isEqual(createdOnDate)) {
							dateMatch = true;
							adminAnalytics.setEmpNotQualifiedCount(adminAnalytics.getCanNotQualifiedCount() != null
									? adminAnalytics.getCanNotQualifiedCount() + 1
									: 1);
						}
					}
					if (!dateMatch) {

						// If the dates are different for all records, insert a new record
						AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
						newAdminAnalytics.setAdminId(Long.valueOf(adminId));
						newAdminAnalytics.setModule(a.getModule());
						newAdminAnalytics.setEmpNotQualifiedCount(1);
						adminAnalyticsList.add(newAdminAnalytics);
					}

					adminAnalyticsRepository.saveAll(adminAnalyticsList);
				} else {

					// If there are no existing records for the adminId, insert a new record
					AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
					adminAnalytics.setAdminId(Long.valueOf(adminId));
					adminAnalytics.setModule(a.getModule());
					adminAnalytics.setEmpNotQualifiedCount(1);
					adminAnalyticsRepository.save(adminAnalytics);
				}
			}

			response.put("statusCode", 200);
			response.put("message", "Status updated successfully");
			return ResponseEntity.ok(response);
		} else {
			response.put("statusCode", 400);
			response.put("message", "EmpLeadModel not found with ID: ");
			return ResponseEntity.badRequest().body(response);
		}
	}

	@PutMapping("/empStatus")
	public ResponseEntity<Map<String, Object>> candidateUpdateStatus(@RequestParam int empId,
			@RequestParam Long adminId, @RequestParam boolean qualified, @RequestParam boolean notQualified) {

		Optional<EmployerModel> optionalEmployerModel = employerRepository.findById(empId);

		Map<String, Object> response = new HashMap<>();

		if (optionalEmployerModel.isPresent()) {
			EmployerModel employerModel = optionalEmployerModel.get();
			Admin a = adminRepository.findById(adminId).orElse(null);

			if (qualified) {
				employerModel.setQualified(true);
				employerModel.setNotQualified(false);
				employerRepository.save(employerModel);
			} else if (notQualified) {
				employerModel.setQualified(false);
				employerModel.setNotQualified(true);
				employerRepository.save(employerModel);
			}

			response.put("statusCode", 200);
			response.put("message", "Status updated successfully");
			return ResponseEntity.ok(response);
		} else {
			response.put("statusCode", 400);
			response.put("message", "Employer not found with ID: ");
			return ResponseEntity.badRequest().body(response);
		}
	}

	@PostMapping("/employerCall")
	public String getMobileNumberByAdminId(@RequestParam Long adminId, @RequestParam int employerId) {

		String adminMobileNumber = adminRepository.findMobileNumberByAdminId(adminId);
		String employerMobileNumber = employerRepository.findByMobileNumberById(employerId);

		Map<String, Object> response = new HashMap<>();

		if (adminMobileNumber != null && employerMobileNumber != null) {
			// Use the provided mobileNumber query parameter, if available, or the admin's
			// mobile number
			String targetMobileNumber = employerMobileNumber != null ? employerMobileNumber : adminMobileNumber;

			response.put("code", 200);
			response.put("message", "call Initialed success");
			return exotelCallController.connectToEmployercalls("+91" + targetMobileNumber, adminMobileNumber);
		} else {
			response.put("code", 400);
			response.put("message", "call not  Initialed");
			return "Mobile number not found";
		}
	}

	@PostMapping("/invoice")
	public ResponseEntity<?> AdminId(@RequestParam int paymentId) {
		EmployerPaymentModel payment = employerPaymentRepository.findById(paymentId);
		int payments = payment.getId();
		int empId = payment.getEmployerId();
		int amount = payment.getAmount();
		String pMethod = payment.getTypeOfPurchase();

		if (payment != null) {
			try {
				// You can call the sendInvoice method from the AdminKYCController here
				adminKYCController.sendInvoices(empId, amount, payments, pMethod);
				return ResponseEntity.ok("Invoice sent successfully");
			} catch (Exception e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Failed to send the invoice: " + e.getMessage());
			}
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid paymentId or empId");
		}
	}

	@PostMapping("/employerTimeline")
	public Map<String, Object> createEmployerTimeLine(@RequestParam("empId") int empId,
			@RequestParam("adminId") Long adminId, @RequestParam("eventName") String eventName,
			@RequestParam(value = "notes", required = false) String notes) {

		Map<String, Object> response = new HashMap<>();

		EmployerModel emp = employerRepository.findById(empId).orElse(null);
		Admin a = adminRepository.findById(adminId).orElse(null);

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String formattedDate = dateFormat.format(currentDate);

		if (emp == null || a == null || eventName.isEmpty()) {
			response.put("status", 400);
			response.put("message", "Bad Request: Missing or invalid parameters");
		} else {
			EmployerTimeline employerTimeLine = new EmployerTimeline();
			employerTimeLine.setEmpId(empId);
			employerTimeLine.setEventName(eventName);

			if ("Follow up call".equals(eventName)) {
				employerTimeLine
						.setEventDescription("Follow up call by" + "<b>" + a.getUserName() + "</b> on " + formattedDate);

				List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository
						.findByAdminId(Long.valueOf(adminId));

				if (!adminAnalyticsList.isEmpty()) {
					// Check if the createdOn date is the same as the current date
					LocalDate currentDate1 = LocalDate.now();
					boolean dateMatch = false;

					for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
						LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
						if (currentDate1.isEqual(createdOnDate)) {
							dateMatch = true;
							adminAnalytics.setEmpFollowUpCount(adminAnalytics.getEmpFollowUpCount() != null
									? adminAnalytics.getEmpFollowUpCount() + 1
									: 1);
						}
					}
					if (!dateMatch) {

						// If the dates are different for all records, insert a new record
						AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
						newAdminAnalytics.setAdminId(Long.valueOf(adminId));
						newAdminAnalytics.setModule(a.getModule());
						newAdminAnalytics.setEmpFollowUpCount(1);
						adminAnalyticsList.add(newAdminAnalytics);
					}

					adminAnalyticsRepository.saveAll(adminAnalyticsList);
				} else {

					// If there are no existing records for the adminId, insert a new record
					AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
					adminAnalytics.setAdminId(Long.valueOf(adminId));
					adminAnalytics.setModule(a.getModule());
					adminAnalytics.setEmpFollowUpCount(1);
					adminAnalyticsRepository.save(adminAnalytics);
				}

			} else if ("Call Remainder".equals(eventName)) {
				employerTimeLine
						.setEventDescription("Call Remainder by" + "<b>" + a.getUserName() + "</b>" + formattedDate);
			} else if ("CSM Intro Call".equals(eventName)) {
				employerTimeLine
						.setEventDescription("CSM Intro Call by" + "<b>" + a.getUserName() + "</b>" + formattedDate);
			} else if ("CSM Follow Up Call".equals(eventName)) {
				employerTimeLine.setEventDescription(
						"CSM Follow Up Call by" + "<b>" + a.getUserName() + "</b> on " + formattedDate);
			} else if ("Inbound Call".equals(eventName)) {
				employerTimeLine
						.setEventDescription("Inbound Call" + "<b>" + a.getUserName() + "</b> on " + formattedDate);
			}
			if (notes != null && !notes.isEmpty()) {
				employerTimeLine.setNotes(notes);
			}

			employerTimelineRepository.save(employerTimeLine);

			response.put("status", 200);
			response.put("message", "Success");
			response.put("data", employerTimeLine);
		}

		return response;
	}

	@GetMapping("/employerTimeline")
	public ResponseEntity<Page<EmployerTimeline>> getEmployerTimelineList(
			@RequestParam(required = false) String eventName, @RequestParam(required = false) Integer empId,
			@RequestParam(required = false) Integer empLeadId,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
			@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
			@RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "0") int page) {

		try {
			Page<EmployerTimeline> resultPage = adminService.findByEmpFilters(eventName, empId, empLeadId, startDate,
					endDate, PageRequest.of(page, size));

			return ResponseEntity.ok(resultPage);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/employerLeadTimeLine")
	public Map<String, Object> createEmployerLeadTimeLine(@RequestParam("empLeadId") int empLeadId,
			@RequestParam("adminId") Long adminId, @RequestParam("eventName") String eventName,
			@RequestParam(value = "notes", required = false) String notes) {

		Map<String, Object> response = new HashMap<>();

		LeadModel lead = leadRepository.findById(empLeadId).orElse(null);
		Admin a = adminRepository.findById(adminId).orElse(null);

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String formattedDate = dateFormat.format(currentDate);

		if (lead == null || a == null || eventName.isEmpty()) {
			response.put("status", 400);
			response.put("message", "Bad Request: Missing or invalid parameters");
		} else {
			EmployerTimeline employerTimeLine = new EmployerTimeline();
			employerTimeLine.setEmpLeadId(empLeadId);
			employerTimeLine.setEmpId(0);
			employerTimeLine.setEventName(eventName);

			if ("Intro Call".equals(eventName)) {
				employerTimeLine
						.setEventDescription("Intro call by <b>" + a.getUserName() + "</b> on " + formattedDate);
			}

			if (notes != null && !notes.isEmpty()) {
				employerTimeLine.setNotes(notes);
			}

			employerTimelineRepository.save(employerTimeLine);

			response.put("status", 200);
			response.put("message", "Success");
			response.put("data", employerTimeLine);
		}

		return response;
	}

	@PostMapping(value = "/employerFieldLead")
	public ResponseEntity<?> updateOrCreateEmployerFieldLead(@RequestParam("companyName") String companyName,
			@RequestParam("area") String area, @RequestParam("city") String city,
			@RequestParam(required = false, defaultValue = "0") long mobileNumber,
			@RequestParam(required = false, defaultValue = "0") long whatsappNumber,
			@RequestParam(required = false, defaultValue = "0") long alternateMobileNumber,
			@RequestParam(required = false) String emailId, @RequestParam(required = false) String latitude,
			@RequestParam(required = false) String longitude,
			@RequestParam(required = false, defaultValue = "0") Integer adminId,
			@RequestPart(required = false) MultipartFile file) {

		EmployerFieldLead existingUser = employerFieldLeadRepository.findByCompanyName(companyName);

		if (existingUser == null) {
			// Create a new user if not found
			EmployerFieldLead newUser = new EmployerFieldLead();
			newUser.setCompanyName(companyName);
			newUser.setArea(area);
			newUser.setCity(city);
			newUser.setMobileNumber(mobileNumber);
			newUser.setWhatsappNumber(whatsappNumber);
			newUser.setAlternateMobileNumber(alternateMobileNumber);
			newUser.setEmailId(emailId);
			newUser.setLattitude(latitude);
			newUser.setLongitude(longitude);
			newUser.setAdminId(adminId);

			if (file != null && !file.isEmpty()) {
				try {
					String url = this.employerService.uploadLeadPhotoToS3Bucket1(file, companyName, true);
					newUser.setLeadImageLink(url);
				} catch (AmazonServiceException ase) {
					// AmazonServiceException represents an error response from an AWS service
					ase.printStackTrace();
					logger.error("AmazonServiceException during file upload to S3: " + ase.getMessage());
				} catch (SdkClientException sce) {
					// SdkClientException represents a low-level, unexpected error that occurred in
					// the SDK
					sce.printStackTrace();
					logger.error("SdkClientException during file upload to S3: " + sce.getMessage());
				} catch (Exception e) {
					// Catch any other unexpected exceptions
					e.printStackTrace();
					logger.error("Unexpected exception during file upload to S3: " + e.getMessage());
				}
			}

			employerFieldLeadRepository.save(newUser);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "New profile created successfully");
			map.put("Image URL", newUser.getLeadImageLink());
			map.put("results", newUser);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			// Update existing user
			if (file != null && !file.isEmpty()) {
				try {
					String url = this.employerService.uploadLeadPhotoToS3Bucket1(file, companyName, true);
					existingUser.setLeadImageLink(url);
				} catch (Exception e) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("statuscode", 500);
					map.put("message", "Error uploading profile picture");
					return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}

			// Update other fields as needed
			existingUser.setArea(area);
			existingUser.setCity(city);
			existingUser.setMobileNumber(mobileNumber);
			existingUser.setWhatsappNumber(whatsappNumber);
			existingUser.setAlternateMobileNumber(alternateMobileNumber);
			existingUser.setEmailId(emailId);
			existingUser.setLattitude(latitude);
			existingUser.setLongitude(longitude);
			existingUser.setAdminId(adminId);

			employerFieldLeadRepository.save(existingUser);

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Profile details updated successfully");
			map.put("Image URL", existingUser.getLeadImageLink());
			map.put("results", existingUser);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	public class EmployerFieldLeadFilterResponse {
		private long totalCount;
		private List<Map<String, Object>> employerFieldList;

		public EmployerFieldLeadFilterResponse(long totalCount, List<Map<String, Object>> employerFieldList) {
			this.totalCount = totalCount;
			this.employerFieldList = employerFieldList;
		}

		public long getTotalCount() {
			return totalCount;
		}

		public List<Map<String, Object>> getemployerFieldList() {
			return employerFieldList;
		}
	}

	@PostMapping("/employerFeildList")
	public ResponseEntity<EmployerFieldLeadFilterResponse> getEmployerFieldLead(
			@RequestBody EmployerFieldLead employerFieldLead) {
		List<Map<String, Object>> empint = employerservice.filteremployerFieldList(employerFieldLead.getCompanyName(),
				employerFieldLead.getArea(), employerFieldLead.getCity(), employerFieldLead.getCreatedTime(),
				employerFieldLead.getEndDate(), employerFieldLead.getPages(), employerFieldLead.getSize());
		long totalCount = employerservice.filteremployerFieldCount(employerFieldLead.getCompanyName(),
				employerFieldLead.getArea(), employerFieldLead.getCity(), employerFieldLead.getCreatedTime(),
				employerFieldLead.getEndDate());
		EmployerFieldLeadFilterResponse response = new EmployerFieldLeadFilterResponse(totalCount, empint);
		return ResponseEntity.ok(response);
	}

	@GetMapping(value = "/employerFieldLead")
	public ResponseEntity<?> getEmployerFieldLeadByCompanyName(@RequestParam String companyName) {
		EmployerFieldLead existingUser = employerFieldLeadRepository.findByCompanyName(companyName);

		if (existingUser == null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "Company not found");
			return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "Company details retrieved successfully");
			map.put("results", existingUser);
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}
	
	@PostMapping("/sendSLA")
    public ResponseEntity<Map<String, String>> sendSLA(@RequestParam int id,
                                                        @RequestParam String recruitmentFeePercentage,
                                                        @RequestParam String recruitmentFeeType,
                                                        @RequestParam String paymentDuration,
                                                        @RequestParam String replacementDuration,
                                                        @RequestParam Long adminId,
                                                        @RequestParam(required = false) String ccMail,
                                                        @RequestParam String emailId) {
        Map<String, String> response = new HashMap<>();

        try {
            // Validate input parameters
            if (recruitmentFeePercentage == null || recruitmentFeeType == null ||
                    paymentDuration == null || replacementDuration == null) {
                response.put("code", "400");
                response.put("status", "error");
                response.put("message", "One or more required parameters are missing or empty");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<LeadModel> optionalLead = leadRepository.findById(id);
            Optional<Admin> optionalAdmin = adminRepository.findById(adminId);

            if (optionalLead.isPresent() && optionalAdmin.isPresent()) {
                LeadModel lead = optionalLead.get();
                Admin admin = optionalAdmin.get();

                // Email content
                Map<String, String> emailData = new HashMap<>();
                emailData.put("ContactPersonName", lead.getContactPersonName());
                emailData.put("CompanyName", lead.getCompanyName());
                emailData.put("signature", admin.getEmailSignature());

                String emailMessage = freeMarkerUtils.getHtml1("SLAEmailContent.html", emailData);
               

                Map<String, String> slaData = new HashMap<>();
                slaData.put("companyName", lead.getCompanyName());
                slaData.put("recruitmentFeePercentage", recruitmentFeePercentage);
                slaData.put("recruitmentFeeType", recruitmentFeeType);
                slaData.put("paymentDuration", paymentDuration);
                slaData.put("replacementDuration", replacementDuration);
                slaData.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

                String sla1 = freeMarkerUtils.getHtml1("SLA1.html", slaData);
                String sla2 = freeMarkerUtils.getHtml1("SLA3.html", slaData);

                // Convert HTML to PDF for SLA1
                byte[] pdfBytes1 = convertHtmlToPdf(sla1);
                // Convert HTML to PDF for SLA2
                byte[] pdfBytes2 = convertHtmlToPdf(sla2);

                // Merge SLA1 and SLA2 PDFs
                byte[] mergedPdfBytes = mergePdfs(pdfBytes1, pdfBytes2);

                if (amazonSESMailUtil != null) {
                    amazonSESMailUtil.sendSLA(emailId, admin.getId(),
                            emailMessage, mergedPdfBytes,
                            lead.getCompanyName(), ccMail);
                }

                // Update LeadModel
                lead.setRecruitmentFeePercentage(recruitmentFeePercentage);
                lead.setRecruitmentFeeType(recruitmentFeeType);
                lead.setPaymentDuration(paymentDuration);
                lead.setReplacementDuration(replacementDuration);
                lead.setSlaEmailNotification(true);
                lead.setSlaEmailOn(new Date());
                leadRepository.save(lead);

                response.put("code", "200");
                response.put("status", "success");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", "400");
                response.put("status", "error");
                response.put("message", "Lead or Admin not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            handleException("Invalid input parameters", e);
        } catch (Exception e) {
            handleException("An error occurred while processing the request", e);
        }

        response.put("status", "error");
        response.put("message", "An error occurred while processing the request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

	private static byte[] convertHtmlToPdf(String html) {
	    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
	        ConverterProperties converterProperties = new ConverterProperties();
	        HtmlConverter.convertToPdf(new ByteArrayInputStream(html.getBytes()), outputStream, converterProperties);
	        return outputStream.toByteArray();
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}



    private byte[] mergePdfs(byte[] pdfBytes1, byte[] pdfBytes2) throws IOException {
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.addSource(new ByteArrayInputStream(pdfBytes1));
        pdfMerger.addSource(new ByteArrayInputStream(pdfBytes2));
        ByteArrayOutputStream mergedPdfStream = new ByteArrayOutputStream();
        pdfMerger.setDestinationStream(mergedPdfStream);
        pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        return mergedPdfStream.toByteArray();
    }

    private void handleException(String message, Exception e) {
        // Handle the exception as needed, e.g., logging
        e.printStackTrace();
    }




	@GetMapping("/Companydetails")
	public ResponseEntity<?> getEmployerDetailsByCompanyName(@RequestParam(required =false) String companyName) {
	    try {
	        // Assuming EmployerRepository has a method to find by company name containing the provided string
	        List<EmployerModel> employers = employerRepository.findByCompanyName(companyName);

	        if (!employers.isEmpty()) {
	            // Create a list to store employer details (you can use a DTO for a cleaner representation)
	            List<Map<String, Object>> employerDetailsList = new ArrayList<>();

	            // Loop through each employer and extract necessary details
	            for (EmployerModel employer : employers) {
	                Map<String, Object> employerDetails = new HashMap<>();
	                employerDetails.put("id", employer.getId());
	                employerDetails.put("companyName", employer.getCompanyName());
	                // Add other details as needed

	                employerDetailsList.add(employerDetails);
	            }

	            return new ResponseEntity<>(employerDetailsList, HttpStatus.OK);
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No employers found for company name containing: " + companyName);
	        }
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the request");
	    }
	}
	
	@GetMapping("/empDocuments")
	public ResponseEntity<List<CfgEmployerDocumentsModel>> getActiveDocuments() {
        List<CfgEmployerDocumentsModel> activeDocuments = employerservice.getActiveDocuments();
        return new ResponseEntity<>(activeDocuments, HttpStatus.OK);
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
	
	 
	 @GetMapping("/empDoc")
	 public List<EmployerDocumentsModel> getDocumentsByEmpId(@RequestParam int empId) {
	        return employerService.getDocumentsByEmpId(empId);
	    }
	 
	 @PostMapping("/empDocUpload")
	 public ResponseEntity<Map<String, String>> handleFileUpload(
	         @RequestParam("file") MultipartFile file,
	         @RequestParam("adminId") Long adminId,
	         @RequestParam("empId") int empId,
	         @RequestParam("docTitle") String docTitle
	 ) {
	     Map<String, String> response = new HashMap<>();
	     Optional<Admin> a = adminRepository.findById(adminId);
	     Admin admin = a.get();
	     try {
	         // Check for null values
	         if (file == null || docTitle == null) {
	             response.put("code", "400");
	             response.put("status", "Invalid file or docTitle provided.");
	             return ResponseEntity.badRequest().body(response);
	         }

	         // Null check for s3client
	         if (s3client == null) {
	             logger.error("S3 client is null. Cannot upload a new document.");
	             response.put("code", "500");
	             response.put("status", "Error uploading the file. S3 client is null.");
	             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	         }

	         // Declare the key variable outside the if-else statements
	         String key;

	         // Conditionally set the key based on docTitle
	         if ("SLA from Taizo.in".equals(docTitle) || "SLA from Client".equals(docTitle)) {
	             key = "DevEmployer/SLA/" + docTitle + "_" + generateFileName(file);
	         } else if ("GST".equals(docTitle)) {
	             key = "DevEmployer/GST/" + docTitle + "_" + generateFileName(file);
	         } else if ("Vendor Form".equals(docTitle)) {
	             key = "DevEmployer/VendorForm/" + docTitle + "_" + generateFileName(file);
	         } 
	         else if ("Invoice".equals(docTitle)) {
	             key = "DevEmployer/Invoices/" + docTitle + "_" + generateFileName(file);
	         }
	             else {
	             // Handle other cases or provide a default key if needed
	             response.put("code", "400");
	             response.put("status", "Invalid docTitle provided.");
	             return ResponseEntity.badRequest().body(response);
	         }

	         // Upload the new document to S3
	         String fileUrl = s3UploadFileAndReturnUrl(key, file);

	         // Check if the document with the same docTitle and empId exists
	         List<EmployerDocumentsModel> existingDocuments = employerDocumentsRepository.findByEmpIdAndDocTitleAndActiveTrue(empId, docTitle);

	         for (EmployerDocumentsModel existingDoc : existingDocuments) {
	             // Update the active status to false
	             existingDoc.setActive(false);
	             // Save the updated document back to the database
	             employerDocumentsRepository.save(existingDoc);
	         }

	         Optional<CfgEmployerDocumentsModel> cfgOptional = cfgEmployerDocumentsRepository.findByDocTitle(docTitle);
	         CfgEmployerDocumentsModel cfg = cfgOptional.orElseThrow(() -> new RuntimeException("Configuration not found for docTitle: " + docTitle));

	         // Save the new document to the database
	         EmployerDocumentsModel document = new EmployerDocumentsModel();
	         document.setAdminId(adminId);
	         document.setEmpLeadId(0);
	         document.setEmpId(empId);
	         document.setDocLink(fileUrl);
	         document.setDocTitle(docTitle);
	         document.setDocKey(cfg.getDocKey());
	         document.setActive(true);

	         employerDocumentsRepository.save(document);
	         
	         EmployerTimeline employerTimeline = new EmployerTimeline();
	         Date currentDate = new Date();
	         SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
	         String formattedDate = dateFormat.format(currentDate);
	         String eventDescription = "Document Upload (" + docTitle + ") on <b>" + formattedDate + "</b> By <b>" + admin.getUserName()
	                 + "</b>";
	         employerTimeline.setEmpId(empId);
	         employerTimeline.setEmpLeadId(0);
	         employerTimeline.setEventName("Document Upload");
	         employerTimeline.setEventDescription(eventDescription);
	         employerTimelineRepository.save(employerTimeline);

	         response.put("code", "200");
	         response.put("status", "success");
	         return ResponseEntity.ok(response);

	     } catch (IOException e) {
	         // Log the exception using a logging framework
	         logger.error("Exception: {}", e.getMessage(), e);
	         response.put("code", "400");
	         response.put("status", "Error uploading the file.");
	         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	     }
	 }
	 
	 private String generateFileName(MultipartFile file) {
		    // Use a UUID to ensure a unique file name
		    String uuid = UUID.randomUUID().toString();
		    // Extract the file extension from the original file name
		    String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		    // Use Instant for better precision than System.currentTimeMillis()
		    Instant instant = Instant.now();
		    // Combine the UUID, timestamp, and file extension to create a unique S3 key
		    return uuid + "_" + instant.toEpochMilli() + "." + extension;
		}
	 
//	 @PostMapping("/generateSLAPdf")
//	    public ResponseEntity<byte[]> generateSLAPdf(@RequestParam int id,
//	                                                  @RequestParam String recruitmentFeePercentage,
//	                                                  @RequestParam String recruitmentFeeType,
//	                                                  @RequestParam String paymentDuration,
//	                                                  @RequestParam String replacementDuration,
//	                                                  @RequestParam Long adminId,
//	                                                  @RequestParam(required = false) String ccMail,
//	                                                  @RequestParam String emailId) {
//	        try {
//	            // Fetch the LeadModel by ID
//	            Optional<LeadModel> optionalLead = leadRepository.findById(id);
//	            if (!optionalLead.isPresent()) {
//	                return ResponseEntity.notFound().build();
//	            }
//	            LeadModel lead = optionalLead.get();
//
//	            // Extract company name and contact person name from the LeadModel
//	            String companyName = lead.getCompanyName();
//	            String contactPersonName = lead.getContactPersonName();
//
//	            // Construct the SLA HTML content
//	            Map<String, String> slaData = new HashMap<>();
//	            slaData.put("companyName", companyName);
//	            slaData.put("contactPersonName", contactPersonName);
//	            slaData.put("recruitmentFeePercentage", recruitmentFeePercentage);
//	            slaData.put("recruitmentFeeType", recruitmentFeeType);
//	            slaData.put("paymentDuration", paymentDuration);
//	            slaData.put("replacementDuration", replacementDuration);
//	            slaData.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//
//	            // Get SLA HTML content from template dynamically
//	            String slaEmailContent = freeMarkerUtils.getHtml1("SLAEmailContent.html", slaData);
//	            String slaHtmlContent = constructSlaHtmlContent(slaData);
//
//	            // Convert the HTML content to PDF
//	            byte[] pdfBytes = convertHtmlToPdf1(slaHtmlContent);
//
//	            // Set response headers for PDF download
//	            HttpHeaders headers = new HttpHeaders();
//	            headers.setContentType(MediaType.APPLICATION_PDF);
//	            headers.setContentDisposition(ContentDisposition.builder("attachment")
//	                    .filename("sla.pdf")
//	                    .build());
//
//	            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//	        }
//	    }
//
//	    private String constructSlaHtmlContent(Map<String, String> slaData) {
//	        // Construct the HTML content for the SLA using the provided data
//	        StringBuilder htmlBuilder = new StringBuilder();
//	        htmlBuilder.append("<html><head><title>SLA</title></head><body>");
//	        // Add SLA content using the slaData
//	        htmlBuilder.append("<h1>Service Level Agreement</h1>");
//	        htmlBuilder.append("<p>Company Name: ").append(slaData.get("companyName")).append("</p>");
//	        htmlBuilder.append("<p>Contact Person Name: ").append(slaData.get("contactPersonName")).append("</p>");
//	        htmlBuilder.append("<p>Recruitment Fee Percentage: ").append(slaData.get("recruitmentFeePercentage")).append("</p>");
//	        // Add other SLA details...
//	        htmlBuilder.append("</body></html>");
//	        return htmlBuilder.toString();
//	    }
//
//	    private byte[] convertHtmlToPdf1(String htmlContent) throws IOException, DocumentException {
//	        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//	            ITextRenderer renderer = new ITextRenderer();
//	            renderer.setDocumentFromString(htmlContent);
//	            renderer.layout();
//	            try {
//					renderer.createPDF(outputStream);
//				} catch (com.lowagie.text.DocumentException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//	            return outputStream.toByteArray();
//	        }
//	    }
}
