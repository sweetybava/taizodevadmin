package com.taizo.controller.admin;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.annotation.MultipartConfig;

import org.apache.http.HttpStatus;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import com.taizo.model.Admin;
import com.taizo.model.CandidateModel;
import com.taizo.model.EmployerInvoiceModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.EmployerTimeline;
import com.taizo.model.JobsModel;
import com.taizo.model.LeadModel;
import com.taizo.repository.AdminRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.EmployerInvoiceRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.EmployerTimelineRepository;
import com.taizo.repository.JobRepository;
import com.taizo.repository.LeadRepository;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.utils.FreeMarkerUtils;

import freemarker.template.TemplateException;


@CrossOrigin
@RestController
@RequestMapping("/admin")
public class AdminEmailController {
	
	private final Logger logger = LoggerFactory.getLogger(AdminEmailController.class);
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	AmazonSESMailUtil amazonSESMailUtil;

	@Autowired
	AdminRepository adminRepository;
	
	@Autowired
	private FreeMarkerUtils freeMarkerUtils;
	
	@Autowired
	CandidateRepository candidateRepository;
	
	@Autowired
	EmployerInvoiceRepository employerInvoiceRepository;
	
	@Autowired
	EmployerTimelineRepository employerTimelineRepository;
	
	@Autowired
	LeadRepository leadRepository;
	
	@Value("${property.base.url}")
	private String baseUrl;
	
	@Autowired
	EmployerRepository employerRepository;
	
	@PostMapping("/sendInterviewDetails")
	public ResponseEntity<Map<String, Object>> sendInterviewDetails(
	        @RequestParam int jobId,
	        @RequestParam Long adminId,
	        @RequestParam String emailId,
	        @RequestParam List<Integer> candidateIds) {

	    Map<String, Object> response = new HashMap<>();

	    // Step 1: Check if email address is valid
	    if (!isValidEmail(emailId)) {
	        response.put("code", "400");
	        response.put("message", "Invalid email address format");
	        return ResponseEntity.badRequest().body(response);
	    }

	    Optional<JobsModel> job = jobRepository.findById(jobId);

	    if (!job.isPresent()) {
	        response.put("code", "400");
	        response.put("message", "Job not found");
	        return ResponseEntity.badRequest().body(response);
	    }

	    JobsModel jobModel = job.get();
	    String companyName = jobModel.getCompanyName();
	    String jobCategory = jobModel.getJobCategory();
	    String contactPersonName = jobModel.getContactPersonName();

	    // Find candidates by their IDs
	    List<CandidateModel> candidates = candidateRepository.findAllById(candidateIds);

	    if (candidates.isEmpty()) {
	        response.put("code", "400");
	        response.put("message", "No candidates found for the given IDs");
	        return ResponseEntity.badRequest().body(response);
	    }

		List<String> candidateNames = candidates.stream()
				.map(candidate -> candidate.getFirstName() + " " + candidate.getLastName().charAt(0))
				.collect(Collectors.toList());


		List<String> resumeLinks = candidates.stream()
	            .map(candidate -> candidate.getResume())
	            .collect(Collectors.toList());

	    // Step 2: Check if emailId is not empty or null
	    if (emailId != null && !emailId.isEmpty()) {
	        try {
	            // Step 3: Send interview details
	            sendInterviewDetails(jobCategory, companyName, adminId, emailId, candidateNames, contactPersonName, resumeLinks);

	            response.put("code", "200");
	            response.put("message", "Interview details sent successfully");
	            return ResponseEntity.ok(response);
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.put("code", "400");
	            response.put("message", "Error sending interview details");
	            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body(response);
	        }
	    } else {
	        response.put("code", "400");
	        response.put("message", "Invalid emailId");
	        return ResponseEntity.badRequest().body(response);
	    }
	}

	private boolean isValidEmail(String email) {
	    // Regular expression for a valid email address
	    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
	    return email.matches(emailRegex);
	}

	private void sendInterviewDetails(String jobCategory, String companyName, Long adminId, String emailId,
	                                  List<String> candidateNames, String contactPersonName, List<String> resumeLinks)
	        throws IOException, TemplateException {

	    Optional<Admin> adminOptional = adminRepository.findById(adminId);
	    Admin admin = adminOptional.orElseThrow(() -> new RuntimeException("Admin not found"));

	    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
	    LocalDate currentDate = LocalDate.now();
	    String formattedDate = dtf.format(currentDate);

	    if (emailId != null && !emailId.isEmpty()) {
	        try {
	            HashMap<String, Object> data = new HashMap<>();
	            data.put("companyName", companyName);
	            data.put("jobCategory", jobCategory);
	            data.put("candidateNames", candidateNames);
	            data.put("contactPersonName", contactPersonName);
	            data.put("signature", admin.getEmailSignature());
	            data.put("Day", formattedDate);

	            String message = freeMarkerUtils.getHtml("EmployerInterviewDetails.html", data);

	            // Create MimeMessage and other necessary objects
	            Session session = Session.getInstance(new Properties(System.getProperties()));
	            MimeMessage mimeMessage = new MimeMessage(session);
	            Multipart msgBody = new MimeMultipart();

	            MimeBodyPart htmlPart = new MimeBodyPart();
	            htmlPart.setContent(message, "text/html; charset=UTF-8");
	            msgBody.addBodyPart(htmlPart);

	            // Attach resumes to the email
	            for (int i = 0; i < resumeLinks.size(); i++) {
	                String resumeLink = resumeLinks.get(i);
	                String candidateName = candidateNames.get(i);

	                try {
	                    MimeBodyPart attachments = new MimeBodyPart();
	                    DataSource source = new URLDataSource(new URL(resumeLink));
	                    attachments.setDataHandler(new DataHandler(source));
	                    attachments.setFileName(candidateName + "_Resume.pdf");
	                    msgBody.addBodyPart(attachments);
	                } catch (MalformedURLException e) {
	                    e.printStackTrace();
	                    // Handle invalid URL or any other exceptions related to resume attachment
	                }
	            }

	            // Change the subject line
	            mimeMessage.setSubject("Candidate Profile(s) for " + jobCategory +
	                    " position scheduled on " + "   " + formattedDate + " - Taizo.in", "UTF-8");

	            String ccEmail = "k.saravanan@taizo.in";

	            String cMail  = "vinoth.mani@taizo.in";

	            mimeMessage.setFrom(new InternetAddress(admin.getEmailId()));
	            mimeMessage.setRecipients(Message.RecipientType.TO, emailId);
	            mimeMessage.setContent(msgBody);

	            mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail));

	            // Add the additional CC recipient
	            mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(admin.getEmailId()));
	            mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cMail));

	            // Send email using Amazon SES or other email sending mechanism
	            amazonSESMailUtil.sendInterviewDetailsEmail(emailId, adminId, message, mimeMessage);

	        } catch (IOException | TemplateException | MessagingException e) {
	            e.printStackTrace();
	            throw new RuntimeException("Error generating or sending interview details");
	        }
	    }
	}

	
	 @PostMapping("/sendInvoice")
	    public ResponseEntity<?> sendInvoiceWithAWSSES(@RequestParam int empId,
	                                                   @RequestParam String contactPersonName,
	                                                   @RequestParam Long adminId,
	                                                   @RequestParam String invoiceNumber,
	                                                   @RequestParam String invoiceDate,
	                                                   @RequestParam Long invoiceAmount,
	                                                   @RequestParam String dueDate,
	                                                   @RequestParam String emailId,
	                                                   @RequestParam (required = false)String ccMail,
	                                                   @RequestParam MultipartFile invoiceAttachment){

	        try {
	            Optional<Admin> adminOptional = adminRepository.findById(adminId);
	            Admin admin = adminOptional.orElseThrow(() -> new RuntimeException("Admin not found"));
	            
	            EmployerInvoiceModel invoice = new EmployerInvoiceModel();
	            invoice.setEmpId(empId);
	            invoice.setEmailId(emailId);
	            invoice.setInvoiceAmount(invoiceAmount);
	            invoice.setDueDate(dueDate);
	            invoice.setInvoiceDate(invoiceDate);
	            invoice.setInvoiceNo(invoiceNumber);
	            invoice.setAdminId(adminId);
	            employerInvoiceRepository.save(invoice);

	            if (emailId != null && !emailId.isEmpty()) {

	            	 LocalDate parsedInvoiceDate = LocalDate.parse(invoiceDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
	                 String formattedInvoiceDate = parsedInvoiceDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
	                 
	                 LocalDate parsedInvoiceDate1 = LocalDate.parse(dueDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
	                 String formattedInvoiceDate1 = parsedInvoiceDate1.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));

	                HashMap<String, Object> data = new HashMap<>();
	                data.put("contactPersonName", contactPersonName);
	                data.put("invoiceAmount", invoiceAmount);
	                data.put("invoiceDate", formattedInvoiceDate);
	                data.put("invoiceNumber", invoiceNumber);
	                data.put("dueDate", formattedInvoiceDate1);
	                data.put("signature", admin.getEmailSignature());

	                String message = freeMarkerUtils.getHtml("InvoiceEmailContent.html", data);

	                // Create MimeMessage and other necessary objects
	                Session session = Session.getInstance(new Properties(System.getProperties()));
	                MimeMessage mimeMessage = new MimeMessage(session);
	                Multipart msgBody = new MimeMultipart();

	                MimeBodyPart htmlPart = new MimeBodyPart();
	                htmlPart.setContent(message, "text/html; charset=UTF-8");
	                msgBody.addBodyPart(htmlPart);

	                // Add the attachment
	                MimeBodyPart attachmentPart = new MimeBodyPart();
	                attachmentPart.setDataHandler(new DataHandler(new ByteArrayDataSource(invoiceAttachment.getBytes(), invoiceAttachment.getContentType())));
	                attachmentPart.setFileName(invoiceAttachment.getOriginalFilename());
	                msgBody.addBodyPart(attachmentPart);

	             
	                // Add CC recipients if ccMail is provided
	                if (ccMail != null && !ccMail.isEmpty()) {
	                    mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccMail));
	                }


	                if (ccMail != null && !ccMail.isEmpty()) {
	                    // Pass ccMail only if it is provided
	                	 amazonSESMailUtil.sendInvoice(emailId, ccMail, invoiceAttachment, message ,mimeMessage, adminId,invoiceNumber);
	                } else {
	                    // Send email without ccMail
	                	 amazonSESMailUtil.sendInvoice(emailId, null, invoiceAttachment, message ,mimeMessage, adminId,invoiceNumber);
	                }
	                // Call the method from AmazonSESMailUtil to send the email using AWS SES
	               
	           
	                EmployerTimeline employerTimeline = new EmployerTimeline();
	                Date currentDate1 = new Date();
	                SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
	                String formattedDate1 = dateFormat1.format(currentDate1);
	                String eventDescription = "Invoice Generation on <b>" + formattedDate1 + "</b> By <b>" + admin.getUserName()
	                        + "</b>";
	                employerTimeline.setEmpId(empId);
	                employerTimeline.setEmpLeadId(0);
	                employerTimeline.setEventName("Invoice Generation");
	                employerTimeline.setEventDescription(eventDescription);
	                employerTimelineRepository.save(employerTimeline);

	                Map<String, Object> response = new HashMap<>();
	                response.put("code", 200);
	                response.put("message", "Email sent successfully");
	                return ResponseEntity.ok(response);
	            } else {
	            	    Map<String, Object> response = new HashMap<>();
		                response.put("code", 400);
		                response.put("message", "Email ID is required");
		                return ResponseEntity.ok(response);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", e.getMessage());
                return ResponseEntity.ok(response);
	           
	        }
	    }
	 
	 @PostMapping("/empIntroMail")
		public ResponseEntity<?> sendIntroMailToEmployer(
		        @RequestParam int id,
		        @RequestParam Long adminId,
		        @RequestParam(required = false) String ccMail
		) throws IOException, TemplateException {

		    Optional<EmployerModel> employer = employerRepository.findById(id);

		    if (!employer.isPresent()) {
		        return ResponseEntity.badRequest().body(createErrorResponse("Lead not found", 400));
		    }

		    EmployerModel emp = employer.get();

		    Optional<Admin> adminOptional = adminRepository.findById(adminId);

		    if (!adminOptional.isPresent()) {
		        return ResponseEntity.badRequest().body(createErrorResponse("Admin not found", 400));
		    }

		    Admin admin = adminOptional.get();
		    String signature = admin.getEmailSignature();

		    String companyName = emp.getCompanyName();
		    String contactPersonName = emp.getContactPersonName();

		    try {
		        sendEmpIntroEmail(emp, admin, companyName, contactPersonName, signature, ccMail);
		        return ResponseEntity.ok(createSuccessResponse("Intro email sent successfully", "success", 200));
		    } catch (IOException | TemplateException e) {
		        return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
		                .body(createErrorResponse("Error sending intro email", 500));
		    }
		}
		private void sendEmpIntroEmail(EmployerModel emp, Admin admin,String companyName,String contactPersonName,
									 String signature,String ccMail) throws IOException, TemplateException {
			String emailId = emp.getEmailId();
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
			emp.setEmailNotification(true);
			emp.setIntroEmailOn(new Date());
			employerRepository.save(emp);

			Date currentDate1 = new Date();
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
			String formattedDate1 = dateFormat1.format(currentDate1);

			EmployerTimeline employerTimeline1 = new EmployerTimeline();
			String eventDescription1 = "Intro Email send On  <b>" + formattedDate1 + "</b> By <b>"
					+ admin.getUserName() + "</b>";
			employerTimeline1.setEmpId(emp.getId());
			employerTimeline1.setEmpLeadId(0);
			employerTimeline1.setEventName("Intro Email");
			employerTimeline1.setEventDescription(eventDescription1);
			employerTimelineRepository.save(employerTimeline1);
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
		 
		 @PostMapping("/sendSLAToEmployer")
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

		            Optional<EmployerModel> optionalLead = employerRepository.findById(id);
		            Optional<Admin> optionalAdmin = adminRepository.findById(adminId);

		            if (optionalLead.isPresent() && optionalAdmin.isPresent()) {
		                EmployerModel emp = optionalLead.get();
		                Admin admin = optionalAdmin.get();

		                // Email content
		                Map<String, String> emailData = new HashMap<>();
		                emailData.put("ContactPersonName", emp.getContactPersonName());
		                emailData.put("CompanyName", emp.getCompanyName());
		                emailData.put("signature", admin.getEmailSignature());

		                String emailMessage = freeMarkerUtils.getHtml1("SLAEmailContent.html", emailData);
		               

		                Map<String, String> slaData = new HashMap<>();
		                slaData.put("companyName", emp.getCompanyName());
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
		                            emp.getCompanyName(), ccMail);
		                }

		                // Update LeadModel
		                emp.setRecruitmentFeePercentage(recruitmentFeePercentage);
		                emp.setRecruitmentFeeType(recruitmentFeeType);
		                emp.setPaymentDuration(paymentDuration);
		                emp.setReplacementDuration(replacementDuration);
		                emp.setSlaEmailNotification(true);
		                emp.setSlaEmailOn(new Date());
		                employerRepository.save(emp);

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
		        return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(response);
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
			
		 
		 
		


	 











}

