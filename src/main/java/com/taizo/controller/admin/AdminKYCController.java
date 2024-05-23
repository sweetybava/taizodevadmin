package com.taizo.controller.admin;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.JSONObject;
import java.text.DecimalFormat;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.MessagingException;
import javax.xml.bind.DatatypeConverter;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.gson.Gson;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import com.razorpay.*;
import com.taizo.controller.candidate.UserPaymentController;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.service.CloudWatchLogService;
import com.taizo.service.PlansService;
import com.taizo.service.WAAlertService;
import com.taizo.utils.FreeMarkerUtils;
import freemarker.template.TemplateException;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.*;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@CrossOrigin
@RequestMapping("/admin")
public class AdminKYCController {

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	EmpActivityRepository empActivityRepository;

	@Autowired
	AmazonSESMailUtil amazonSESMailUtil;

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Autowired
	CloudWatchLogService cloudWatchLogService;

	@Autowired
	PlansRepository plansRepository;

	@Autowired
	PlansService plansService;
	
	@Autowired
	AdminAnalyticsRepository adminAnalyticsRepository;

	@Autowired
	EmpProFormaInvoiceRepository empProFormaInvoiceRepository;

	@Autowired
	private FreeMarkerUtils freeMarkerUtils;

	@Value("${firebase.emp.api.key}")
	private String firebaseEmpApiKey;

	@Value("${firebase.deeplink.emp.hostname}")
	private String firebaseEmpHost;

	@Value("${firebase.empapp.package.name}")
	private String firebaseEmpPackage;

	@Value("${firebase.js.api.key}")
	private String firebaseJSApiKey;

	@Value("${firebase.deeplink.js.hostname}")
	private String firebaseJSHost;

	@Value("${firebase.jsapp.package.name}")
	private String firebaseJSPackage;

	@Value("${property.base.url}")
	private String baseUrl;

	@Autowired
	EmployerPaymentRepository employerPaymentRepository;

	@Autowired
	EmpPlansRepository empPlansRepository;
	
	@Autowired
	AdminRepository adminRepository;

	@Value("${razor.key.id}")
	private String KeyId;

	@Value("${razor.secret.key}")
	private String secretKey;

	private AmazonS3 s3client;

	@Value("${aws.endpointUrl}")
	private String endpointUrl;

	@Value("${aws.s3.audio.bucket}")
	private String bucketName;
	@Value("${aws.access.key.id}")
	private String accessKey;
	@Value("${aws.access.key.secret}")
	private String awssecretKey;

	@Value("${aws.s3.bucket.folder}")
	private String folderName;

	@Autowired
	LeadRepository leadRepository;

	@Autowired
	WAAlertService waAlertService;

	@Autowired
	CanLeadRepository canLeadRepository;
	@Autowired
	private CandidateRepository candidateRepository;
	@Autowired
	private UserPaymentRepository userPaymentRepository;
	@Autowired
	private ProFormaInvoicesRepository proFormaInvoiceRepository;

	@Autowired
	EmpPlacementPlanDetailsRepository empPlacementPlanRepository;

	@Autowired
	EmployerTimelineRepository employerTimelineRepository;

	private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

	private static final Logger logger = LoggerFactory.getLogger(UserPaymentController.class);

	private String getCurrentTime() {
		LocalTime currentTime = LocalTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
		return currentTime.format(formatter);
	}

	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.awssecretKey);
		this.s3client = new AmazonS3Client(credentials);
	}

	@GetMapping(path = "/viewKYCDocuments")
	public ResponseEntity<?> getKYCVerificationData(@RequestParam("start_date") final String startDate,
													@RequestParam("end_date") final String endDate, @RequestParam("page") int start,
													@RequestParam("size") int length) {

		int page = start / length; // Calculate page number

		Pageable pageable = PageRequest.of(start, length, new Sort(Sort.Direction.DESC, "created_time"));

		Page<EmployerModel> employerModelList = employerRepository.getKYCUnderReviewEmployer(startDate, endDate,
				pageable);

		HashMap<String, Object> hm = new HashMap<>();
		hm.put("data", employerModelList.getContent());
		hm.put("start", start);
		hm.put("recordsTotal", employerModelList.getTotalElements());
		hm.put("recordsFiltered", employerModelList.getTotalElements());
		return new ResponseEntity<>(hm, HttpStatus.OK);
	}

	@PostMapping(path = "/verifyKyc")
	public ResponseEntity<?> verifyKyc(@RequestParam("eid") final int eid,
	                                   @RequestParam (value = "admin_id",required = false) Long adminId){
		Optional<EmployerModel> emp = employerRepository.findById(eid);
		Admin admin = adminRepository.findById(adminId).get();
		if (emp.isPresent()) {
			emp.get().setKycStatus("V");
			employerRepository.save(emp.get());

			EmployerModel existing = emp.get();
			Map<String, String> kycEmailData = new HashMap<String, String>();
			kycEmailData.put("name", existing.getContactPersonName());

			/*
			 * TupleStore tupleStore = new TupleStore();
			 * tupleStore.setKey(existing.getEmailId()); tupleStore.setValue(new
			 * Gson().toJson(kycEmailData));
			 * amazonSESMailUtil.sendEmailSES("EmployerKYCVerifiedtemplateV1", tupleStore);
			 */

			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(eid);
			EA.setActivity("KYC document has been verified!");
			empActivityRepository.save(EA);

			EmployerTimeline employerTimeline = new EmployerTimeline();
			Date currentDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
			String formattedDate1 = dateFormat.format(currentDate);
			String eventDescription = "KYC document has been verified on <b>" + formattedDate1;
			if (admin != null) {
				eventDescription += " by " + admin.getUserName();
			}
			employerTimeline.setEmpId(eid);
			employerTimeline.setEmpLeadId(0);
			employerTimeline.setEventName("KYC Verification");
			employerTimeline.setEventDescription(eventDescription);
			employerTimelineRepository.save(employerTimeline);

			return new ResponseEntity<>("kyc updated", HttpStatus.OK);
		}
		return new ResponseEntity<>("kyc not updated", HttpStatus.BAD_REQUEST);
	}

	@PostMapping(path = "/rejectKyc")
	public ResponseEntity<?> rejectKyc(@RequestParam("eid") final int eid) {
		Optional<EmployerModel> emp = employerRepository.findById(eid);
		if (emp.isPresent()) {
			emp.get().setKycStatus("R");
			employerRepository.save(emp.get());

			EmployerModel existing = emp.get();
			/*
			 * Map<String, String> kycEmailData = new HashMap<String, String>();
			 * kycEmailData.put("name", existing.getContactPersonName()); TupleStore
			 * tupleStore = new TupleStore(); tupleStore.setKey(existing.getEmailId());
			 * tupleStore.setValue(new Gson().toJson(kycEmailData));
			 * amazonSESMailUtil.sendEmailSES("EmployerKYCFailureTemplateV1", tupleStore);
			 */

			if (activeProfile.equalsIgnoreCase("prod")) {
				String cn = null;
				String name = existing.getCompanyName();
				String mn = String.valueOf(existing.getWhatsappNumber());

				if (!name.equalsIgnoreCase("null")) {
					cn = name;
				} else {
					cn = "User";
				}

				HashMap<String, String> d = new HashMap<>();
				d.put("mn", "91" + mn);
				d.put("name", cn);

				waAlertService.sendKYCFailedAlertToEmployer(d);
			}

			return new ResponseEntity<>("kyc rejected", HttpStatus.OK);
		}
		return new ResponseEntity<>("kyc not rejected", HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/empPayment", method = RequestMethod.POST)
	public ResponseEntity<?> createAdminEmployerPayment(@RequestParam("employer_id") final int employerId,
														@RequestParam("amount") final int amnt, @RequestParam("plan_id") final int planId,
														@RequestParam("payment_id") final String paymentId, @RequestParam("order_id") final String orderId,
														@RequestParam("status") final String status,
														@RequestParam(value = "job_count", defaultValue = "0") final int jobCount,
														@RequestParam(value = "plan_expiry_Days", defaultValue = "0") final int planExpiryDays,
														@RequestParam("no_of_openings") final int noOfOpenings,
														@RequestParam("no_of_job_category") final int noOfJobCategory)

			throws SignatureException, RazorpayException {
		EmployerPaymentModel emp = new EmployerPaymentModel();

		Optional<EmployerModel> optional = employerRepository.findById(employerId);
		PlansModel plan1 = null;
		RazorpayClient razorpay = new RazorpayClient(KeyId, secretKey);
		String name = "", email = "", cn = "";
		DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
		String date = formatter.format(new Date());
		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
		Calendar cal = Calendar.getInstance();
		String time = simpleDateFormat1.format(cal.getTime());
		name = optional.get().getContactPersonName();
		cn = optional.get().getCompanyName();

		if (status.equalsIgnoreCase("Payment Successfull")) {

			int amountPaid = amnt;
			try {
				Payment payment = razorpay.payments.fetch(paymentId);
				emp.setAmount(payment.get("amount"));
				emp.setStatus(payment.get("status"));
				emp.setCaptured(payment.get("captured"));
				emp.setNotes(payment.get("notes").toString());
				emp.setFromWeb(true);
				emp.setTypeOfPurchase(payment.get("method"));
				emp.setMobileNumber(Long.parseLong(payment.get("contact")));

			} catch (Exception e) {
				e.printStackTrace();
			}

			emp.setEmailId(optional.get().getEmailId());
			emp.setEmployerId(employerId);
			emp.setPlanId(planId);
			emp.setAmount(amountPaid);
			emp.setPaymentId(paymentId);
			emp.setOrderId(orderId);
			emp.setNumberOfJobCategory(noOfJobCategory);
			emp.setNumberOfOpenings(noOfOpenings);
			employerPaymentRepository.save(emp);

			EmployerActivityModel EA = new EmployerActivityModel();
			EA.setEmpId(employerId);
			EA.setActivity("Payment of " + "<b>INR " + amountPaid + "</b>" + " has been successful!");
			empActivityRepository.save(EA);

			Optional<PlansModel> empPlan = plansRepository.findById(planId);
			plan1 = empPlan.get();

			int expDays = 0;
			if (planId == 9) {
				expDays = planExpiryDays;
			} else {
				expDays = plan1.getPlanValidity();
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DATE, expDays);
			String output = sdf.format(c.getTime());

			if (optional.isPresent()) {

				EmployerModel existing = optional.get();

				name = existing.getContactPersonName();
				email = existing.getEmailId();

				existing.setPaymentStatus("Paid");
				existing.setPlan(planId);
				existing.setUsedFreeTrial("Yes");
				existing.setExpiryDate(output);
				if (planId == 9) {
					existing.setPlanJobCount(existing.getPlanJobCount() + noOfJobCategory);
				} else {
					existing.setPlanJobCount(plan1.getActiveJobs());
				}

				employerRepository.save(existing);

			}

			int paymentID = emp.getId();

			EmployerPaymentModel pay = employerPaymentRepository.findById(paymentID);
			pay.setSignature("Yes");
			employerPaymentRepository.save(pay);

			CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
			logEventModel.setType("Payment");
			logEventModel.setMessage("Signature Verified");
			logEventModel.setDescription(emp.toString());

			try {
				cloudWatchLogService.cloudLog(logEventModel, "E");
			} catch (Exception e) {

			}

			if (activeProfile.equalsIgnoreCase("prod")) {
				try {
					sendInvoice(employerId, amountPaid, paymentID, plan1.getPlanName());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TemplateException e) {
					e.printStackTrace();
				}
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", " Paid Successfully");
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {

			int amountPaid = amnt;

			try {
				Payment payment = razorpay.payments.fetch(paymentId);
				emp.setTypeOfPurchase(payment.get("method"));
				emp.setMobileNumber(Long.parseLong(payment.get("contact")));
				emp.setPaymentId(payment.get("id"));
				emp.setStatus(payment.get("status"));
				emp.setCaptured(payment.get("captured"));
				emp.setNotes(payment.get("notes").toString());
				emp.setFromWeb(true);
				emp.setReason(payment.get("error_reason") + " ," + payment.get("error_description"));
				// emp.setEmailId(payment.get("email"));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				emp.setStatus(status);
				emp.setFromWeb(true);
				emp.setReason(status);
				emp.setCaptured(false);
				emp.setMobileNumber(optional.get().getMobileNumber());

				e.printStackTrace();
			}
			emp.setEmailId(optional.get().getEmailId());
			emp.setEmployerId(employerId);
			emp.setAmount(amountPaid);
			emp.setOrderId(orderId);
			emp.setPlanId(planId);
			employerPaymentRepository.save(emp);

			Optional<PlansModel> empPlan = plansRepository.findById(planId);
			plan1 = empPlan.get();

			name = optional.get().getContactPersonName();
			email = optional.get().getEmailId();
			String company = optional.get().getCompanyName();

			CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
			logEventModel.setType("Payment");
			logEventModel.setMessage("Payment Failed");
			logEventModel.setDescription(emp.toString());
			HashMap<String, String> data = new HashMap<>();
			data.put("plan_id", String.valueOf(planId));
			logEventModel.setLogData(data);

			try {
				cloudWatchLogService.cloudLogFailure(logEventModel, "E");
			} catch (Exception e) {

			}

			String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseEmpApiKey;

			DeeplinkRequest dl = new DeeplinkRequest();
			dl.setLongDynamicLink(firebaseEmpHost + "/?link=" + firebaseEmpHost + "/pricing/" + employerId + "/"
					+ planId + "&apn=" + firebaseEmpPackage);

			// System.out.println(ex.getLongDynamicLink());
			DeeplinkSuffix c1 = new DeeplinkSuffix();
			c1.setOption("UNGUESSABLE");

			String json = new Gson().toJson(dl);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> req = new HttpEntity<String>(json.toString(), headers);

			RestTemplate restTemp = new RestTemplate();

			FirebaseShortLink response = null;
			try {
				response = restTemp.postForObject(url, req, FirebaseShortLink.class);
			} catch (Exception e) {

			}

			if (activeProfile.equalsIgnoreCase("prod")) {

				HashMap<String, String> d = new HashMap<>();
				d.put("mn", "91" + String.valueOf(optional.get().getWhatsappNumber()));
				d.put("webLink", "https://web.taizo.in/console/pricing");
				d.put("appLink", response.getShortLink());

				waAlertService.sendPaymentFailedAlertToEmployer(d);
			}

			if (status.equalsIgnoreCase("Payment Cancelled")) {
				EmployerActivityModel EA = new EmployerActivityModel();
				EA.setEmpId(employerId);
				EA.setActivity("Payment of INR " + "<b>" + amountPaid + "</b>" + " has been left incomplete!");
				empActivityRepository.save(EA);
			} else {
				EmployerActivityModel EA = new EmployerActivityModel();
				EA.setEmpId(employerId);
				EA.setActivity("Payment of " + "<b>INR " + amountPaid + "</b>" + " has failed!");
				empActivityRepository.save(EA);
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 500);
			map.put("message", "Payment Failed");
			return new ResponseEntity<>(map, HttpStatus.OK);

		}
	}

	@Async
	private void sendInvoice(int empId, int amount, int paymentID, String pMethod)
			throws IOException, TemplateException {
		// TODO Auto-generated method stub
		Optional<EmployerModel> d1 = employerRepository.findById(empId);
		EmployerModel d = d1.get();

		EmployerPaymentModel pay = employerPaymentRepository.findById(paymentID);

		DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		String date = formatter.format(pay.getCreatedTime());

		HashMap<String, String> data = new HashMap<>();
		data.put("Amount", String.valueOf(amount));
		data.put("PlanName", pMethod);
		data.put("PaymentMethod", pay.getTypeOfPurchase().toUpperCase());
		data.put("PaymentDate", date);

		EmployerPaymentModel p = employerPaymentRepository.findByInvoiceId();
		int invoiceNum = p.getInvoiceNo() + 1;

		double f = Double.parseDouble(new DecimalFormat("##.##").format(amount / 1.18));
		double GST = Double.parseDouble(new DecimalFormat("##.##").format((amount - f) / 2));

		int year = Calendar.getInstance().get(Calendar.YEAR) % 100;
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		String financialYear = null;
		if (month < 4) {
			financialYear = (year - 1) + "-" + year;
		} else {
			financialYear = year + "-" + (year + 1);
		}

		String invoiceNumber = String.format("%06d", invoiceNum);

		HashMap<String, String> emailDataHM = new HashMap<>();
		emailDataHM.put("CompanyName", d.getCompanyName() != null ? d.getCompanyName() : "");
		emailDataHM.put("InvoiceNo", "TZ/" + financialYear + "/" + invoiceNumber);
		emailDataHM.put("InvoiceDate", date);
		emailDataHM.put("Address", d.getAddress() != null ? d.getAddress() : "");
		emailDataHM.put("gstNum", d.getRegProofNumber() != null ? d.getRegProofNumber() : "");
		emailDataHM.put("Email", d.getEmailId() != null ? d.getEmailId() : "");
		emailDataHM.put("MobileNum", String.valueOf(d.getMobileNumber()));
		emailDataHM.put("Tax", String.valueOf(f));
		emailDataHM.put("GST", String.valueOf(GST));
		emailDataHM.put("TotalAmount", String.valueOf(amount));

		String message = freeMarkerUtils.getHtml1("invoice.html", emailDataHM);

		String html = freeMarkerUtils.getHtml1("PaymentSuccessful.html", data);

		ByteArrayOutputStream target = new ByteArrayOutputStream();

		ConverterProperties converterProperties = new ConverterProperties();
		converterProperties.setBaseUri(baseUrl);

		HtmlConverter.convertToPdf(message, target, converterProperties);

		byte[] bytes = target.toByteArray();

		pay.setInvoiceNo(invoiceNum);
		employerPaymentRepository.save(pay);

		amazonSESMailUtil.sendEmailWithMultipleAttachments(invoiceNumber, pay.getEmailId(), html, bytes);

	}

	public String getJobLink(int jobId) {
		String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseJSApiKey;

		DeeplinkRequest dl = new DeeplinkRequest();
		dl.setLongDynamicLink(firebaseJSHost + "/?link=" + firebaseJSHost + "/jobDetails/" + 0 + "/" + jobId + "&apn="
				+ firebaseJSPackage);

		DeeplinkSuffix c = new DeeplinkSuffix();
		c.setOption("UNGUESSABLE");

		String json = new Gson().toJson(dl);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> req = new HttpEntity<String>(json.toString(), headers);

		RestTemplate restTemp = new RestTemplate();

		FirebaseShortLink response = null;
		try {
			response = restTemp.postForObject(url, req, FirebaseShortLink.class);
		} catch (Exception e) {

		}
		return response.getShortLink();

	}

	@GetMapping(path = "/getJobLink")
	public ResponseEntity<?> getEmpJobLink(@RequestParam("job_id") final int jobId) {

		String jobLink = getJobLink(jobId);

		HashMap<String, Object> map = new HashMap<>();
		map.put("code", 200);
		map.put("message", "Success");
		map.put("jobLink", jobLink);
		return new ResponseEntity<>(map, HttpStatus.OK);

	}

	@GetMapping(path = "/downloadInvoice")
	public ResponseEntity<byte[]> getInvoice(@RequestParam("payment_id") final int paymentID)
			throws IOException, TemplateException {

		EmployerPaymentModel pay = employerPaymentRepository.findById(paymentID);

		Optional<EmployerModel> d1 = employerRepository.findById(pay.getEmployerId());
		EmployerModel d = d1.get();

		DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		String date = formatter.format(pay.getCreatedTime());
		double amount = pay.getAmount();

		int Grandtotal = pay.getAmount();

		int invoiceNum = pay.getInvoiceNo();

		double f = Double.parseDouble(new DecimalFormat("##.##").format(amount / 1.18));
		double GST = Double.parseDouble(new DecimalFormat("##.##").format((amount - f) / 2));

		int year = Calendar.getInstance().get(Calendar.YEAR) % 100;
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		String financialYear = null;
		if (month < 4) {
			financialYear = (year - 1) + "-" + year;
		} else {
			financialYear = year + "-" + (year + 1);
		}

		String invoiceNumber = String.format("%06d", invoiceNum);

		HashMap<String, String> emailDataHM = new HashMap<>();
		emailDataHM.put("CompanyName", d.getCompanyName() != null ? d.getCompanyName() : "");
		emailDataHM.put("InvoiceNo", "TZ/" + financialYear + "/" + invoiceNumber);
		emailDataHM.put("InvoiceDate", date);
		emailDataHM.put("Address", d.getAddress() != null ? d.getAddress() : "");
		emailDataHM.put("gstNum", d.getRegProofNumber() != null ? d.getRegProofNumber() : "");
		emailDataHM.put("Email", d.getEmailId() != null ? d.getEmailId() : "");
		emailDataHM.put("MobileNum", String.valueOf(d.getMobileNumber()));
		DecimalFormat df = new DecimalFormat("0.00");
		df.setDecimalSeparatorAlwaysShown(true);
		emailDataHM.put("Tax", df.format(f));
		DecimalFormat dff = new DecimalFormat("0.00");
		df.setDecimalSeparatorAlwaysShown(true);
		emailDataHM.put("GST", dff.format(GST));
		DecimalFormat df1 = new DecimalFormat("0.00");
		df.setDecimalSeparatorAlwaysShown(true);
		emailDataHM.put("TotalAmount", df1.format(amount));
		emailDataHM.put("GrandTotalAmount", String.valueOf(Grandtotal));

		String message = freeMarkerUtils.getHtml1("invoice.html", emailDataHM);

		ByteArrayOutputStream target = new ByteArrayOutputStream();

		ConverterProperties converterProperties = new ConverterProperties();
		converterProperties.setBaseUri(baseUrl);

		HtmlConverter.convertToPdf(message, target, converterProperties);

		DateFormat informat = new SimpleDateFormat("ddMMyyyy");
		String namedate = informat.format(pay.getCreatedTime());
		String filename = d.getCompanyName() + "_INR" + pay.getAmount() + "_" + namedate + ".pdf";

		byte[] bytes = target.toByteArray();
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + filename + "\"");

		return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(bytes);

	}

	@RequestMapping(value = "/userPayment", method = RequestMethod.POST)
	public ResponseEntity<?> createUserPayment(@RequestBody UserPaymentModel request) {

		Optional<CandidateModel> optional = candidateRepository.findByUserId(request.getUserId());
		if (!optional.isPresent()) {
			// User not found
			return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
		}

		UserPaymentModel user = new UserPaymentModel();

		// Calculate expiry date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, 1); // Adding 1 Year
		String expiryDate = sdf.format(c.getTime());
		user.setExpiryDate(expiryDate);

		user.setUserId(request.getUserId());
		user.setTypeOfPurchase(request.getTypeOfPurchase());
		user.setAmount(request.getAmount());
		user.setEmailId(request.getEmailId());
		user.setMobileNumber(request.getMobileNumber());
		user.setPaymentId(request.getPaymentId());
		user.setOrderId(request.getOrderId());
		user.setStatus(request.getStatus());

		if (request.getStatus().equalsIgnoreCase("Payment Successfull")) {
			user.setSignature("Yes");
			updateCandidateAndSendCleverTapEvent(optional.get(), request.getAmount(), request.getOrderId(), true);
		} else if (request.getStatus().equalsIgnoreCase("Payment Failed")) {
			user.setSignature("No");
			updateCandidateAndSendCleverTapEvent(optional.get(), request.getAmount(), request.getOrderId(), false);
		} else {
			return new ResponseEntity<>("Invalid Payment Status", HttpStatus.BAD_REQUEST);
		}

		userPaymentRepository.save(user);

		if (user.getSignature().equals("Yes")) {
			return new ResponseEntity<>("Payment Successfully", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Payment Failed", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void updateCandidateAndSendCleverTapEvent(CandidateModel candidate, int amount, String orderId,
													  boolean isPaymentSuccessful) {
		candidate.setPaymentStatus(isPaymentSuccessful ? "Paid" : "Unpaid");
		candidate.setAmount(candidate.getAmount() + amount);
		candidate.setJobLimit(candidate.getJobLimit() + 1);
		candidateRepository.save(candidate);

		DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
		String date = formatter.format(new Date());
		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
		Calendar cal = Calendar.getInstance();
		String time = simpleDateFormat1.format(cal.getTime());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, 1); // Adding 1 Year
		String output = sdf.format(c.getTime());

	}

	@PostMapping("/proFormaInvoice")
	public ResponseEntity<Map<String, Object>> sendProformaInvoice(@RequestBody EmpProformaInvoiceModel request){
		                                                         
		Long mobileNumber = request.getMobileNumber();
		String emailId = request.getEmailId();
		int originalAmount = request.getOriginalAmount();
		int invoiceAmount = request.getInvoiceAmount();
		String invoiceDate = request.getInvoiceDate();
		String companyName = request.getCompanyName();
		String contactPersonName = request.getContactPersonName();
		String gstNumber = request.getGSTNumber();
		String address = request.getAddress();
		double discountPercentage = request.getDiscountInPercentage();
		String jobDetails = request.getJobDetails();
		String noOfOpenings = request.getNoOfOpenings();
		String paymentLinkValidityDate = request.getPaymentLinkValidityDate();
		Long adminId = request.getAdminId();
		
		Optional<Admin> adminOptional = adminRepository.findById(adminId);
		
		Admin admin = adminOptional.get();
		String fromEmailId = admin.getEmailId();

		Map<String, Object> responseMap = new HashMap<>();

		try {
			EmployerModel employer = employerRepository.findByMobileNumber(mobileNumber);

			boolean isRegistered = (employer != null);
			// Check if the employer is registered
			double f = Double.parseDouble(new DecimalFormat("##.##").format(invoiceAmount / 1.18));
			double GST = Double.parseDouble(new DecimalFormat("##.##").format((invoiceAmount - f) / 2));

			// Parse the paymentLinkValidityDate into a java.util.Date object
			SimpleDateFormat PaymentvalidityDate = new SimpleDateFormat("dd-MM-yyyy");
			Date validityDate = PaymentvalidityDate.parse(paymentLinkValidityDate);

			// Format the validity date as a string
			String formattedValidityDate = PaymentvalidityDate.format(validityDate);

			// Convert java.util.Date to Unix time (in milliseconds)
			long unixTimeMillis = validityDate.getTime();

			// Convert Unix time to seconds (as required by Razorpay)
			long expireByInSeconds = unixTimeMillis / 1000;

			SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			SimpleDateFormat mysqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat desiredDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
			Date formattedInvoiceDate = null;
			try {
				formattedInvoiceDate = inputDateFormat.parse(invoiceDate);
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String formattedMySQLDate = mysqlDateFormat.format(formattedInvoiceDate);
			String formattedInvoiceDateString = desiredDateFormat.format(formattedInvoiceDate);
			
			// Format the discount percentage as a whole number
			DecimalFormat decimalFormat = new DecimalFormat("0");
			String formattedDiscountPercentage = decimalFormat.format(discountPercentage);

			EmpProformaInvoiceModel proFormaInvoice = new EmpProformaInvoiceModel();
			proFormaInvoice.setMobileNumber(mobileNumber);
			proFormaInvoice.setMobileCountryCode("91");
			proFormaInvoice.setInvoiceAmount(invoiceAmount);
			proFormaInvoice.setOriginalAmount(originalAmount);
			proFormaInvoice.setInvoiceDate(formattedMySQLDate);
			proFormaInvoice.setEmailId(emailId);
			proFormaInvoice.setAddress(address);
			proFormaInvoice.setCompanyName(companyName);
			proFormaInvoice.setContactPersonName(contactPersonName);
			proFormaInvoice.setDiscountInPercentage(discountPercentage);
			proFormaInvoice.setJobDetails(jobDetails);
			if (gstNumber != null && !gstNumber.isEmpty()) {
				proFormaInvoice.setGSTNumber(gstNumber);
			} else {
				proFormaInvoice.setGSTNumber("0");
			}
			if (employer != null) {
				proFormaInvoice.setEmployerId(employer.getId());
			} else {
				proFormaInvoice.setEmployerId(0);
			}
			proFormaInvoice.setAdminId(adminId);

			proFormaInvoiceRepository.save(proFormaInvoice);

			RazorpayClient razorpay = new RazorpayClient(KeyId, secretKey);

			int discountedAmountInPaise = (int) Math.round(invoiceAmount) * 100;

			// int disAMountInPaise =(int)(discountedAmountInPaise);

			JSONObject paymentLinkRequest = new JSONObject();
			paymentLinkRequest.put("amount", discountedAmountInPaise);
			paymentLinkRequest.put("currency", "INR");
			paymentLinkRequest.put("accept_partial", false);
			paymentLinkRequest.put("expire_by", expireByInSeconds);
			JSONObject notify = new JSONObject();
			notify.put("sms", true);
			notify.put("email", true);
			paymentLinkRequest.put("notify", notify);
			paymentLinkRequest.put("reminder_enable", true);
			paymentLinkRequest.put("description",companyName );

			int employerId = (employer != null) ? employer.getId() : 0;

			JSONObject notes = new JSONObject();
			notes.put("MobileNumber", mobileNumber);
			notes.put("CompanyName", companyName);
			notes.put("invoiceId", proFormaInvoice.getId());
			notes.put("adminId", adminId);
			//notes.put("email","info@taizo.in");
			if (emailId != null && !emailId.isEmpty()) {
				notes.put("email", emailId);
			} else {
				Optional<EmployerModel> employerEmail = null;
				if(employer!=null) {
					employerEmail = employerRepository.findById(employer.getId());
				}
				 if(employerEmail != null && employerEmail.isPresent())
				 {
					 notes.put("email", employerEmail.get().getEmailId());
				 }
				 else {
					 notes.put("email", "info@taizo.in");
				 }
			}
			if(employer != null)
			{
				notes.put("employerId", employerId);
				notes.put("leadId", 0);
			}
			else {
			// Try to find an existing lead by mobile number
			Optional<LeadModel> existingLeadOptional = leadRepository.findByMobileNumber(mobileNumber);

			if (existingLeadOptional.isPresent()) {
			    // Update the existing lead record
			    LeadModel existingLead = existingLeadOptional.get();
			    existingLead.setCompanyName(companyName);
			    existingLead.setContactPersonName(contactPersonName);
			    existingLead.setAddress(address);
			    existingLead.setEmailId(emailId);
				existingLead.setFromAdmin(true);
			    //existingLead.setMnverified(true);

			    leadRepository.save(existingLead);
			    notes.put("employerId", 0);
			    notes.put("leadId", existingLead.getId());
			} else {
			    // Create a new lead record
			    LeadModel newLead = new LeadModel();

			    newLead.setMobileNumber(mobileNumber);
				//newLead.setMnverified(true);
			    newLead.setCompanyName(companyName);
			    newLead.setContactPersonName(contactPersonName);
			    newLead.setAddress(address);
			    newLead.setEmailId(emailId);
				newLead.setFromAdmin(true);

			    leadRepository.save(newLead);

				EmployerTimeline employerTimeline = new EmployerTimeline();
				Date currentDate1 = new Date();
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
				String formattedDate1 = dateFormat1.format(currentDate1);
				String eventDescription = "Lead Generation On <b>" + formattedDate1 + "</b> By <b>" + admin.getUserName() + "</b>";
				employerTimeline.setEmpId(0);
				employerTimeline.setEmpLeadId(newLead.getId());
				employerTimeline.setEventName("Lead Generation");
				employerTimeline.setEventDescription(eventDescription);
				employerTimelineRepository.save(employerTimeline);
			    notes.put("employerId", 0);
			    notes.put("leadId", newLead.getId());
			}
			}

			notes.put("noOfOpenings", noOfOpenings);
			paymentLinkRequest.put("notes", notes);
			paymentLinkRequest.put("callback_url", baseUrl + "admin/invoicePaymentDetails");
			paymentLinkRequest.put("callback_method", "get");

			String referenceId = generateUniqueReferenceId();
			if (referenceId.length() <= 40) {
				paymentLinkRequest.put("reference_id", referenceId);
			} else {
				throw new IllegalArgumentException("Reference ID exceeds the maximum character limit of 40.");
			}

			PaymentLink payment = razorpay.paymentLink.create(paymentLinkRequest);

			proFormaInvoice.setPaymentLinkId(payment.get("id"));
			proFormaInvoice.setPaymentLink(payment.get("short_url").toString());
			proFormaInvoice.setPaymentReferenceId(referenceId);
			proFormaInvoice.setNotes(String.valueOf(notes));
			proFormaInvoice.setPaymentLinkValidityDate(paymentLinkValidityDate);
			proFormaInvoiceRepository.save(proFormaInvoice);


			if (isRegistered) {
				// Send email and WhatsApp alerts if emailId is present
				if (emailId != null && !emailId.isEmpty()) {
					HashMap<String, String> data = new HashMap<>();
					data.put("amount", String.valueOf(invoiceAmount));
					data.put("name", contactPersonName);
					data.put("no_of_openings", noOfOpenings);
					data.put("discount_percentage", String.valueOf(formattedDiscountPercentage));
					if (discountPercentage > 0) {
						data.put("discount_txt_visible", "");
						data.put("offer_price", String.valueOf(invoiceAmount));
						data.put("validity_date", formattedValidityDate);
					} else {
						data.put("discount_txt_visible", "d-none");
						data.put("offer_price", String.valueOf(invoiceAmount));
						data.put("validity_date", formattedValidityDate);
					}

					data.put("payment_link", payment.get("short_url").toString());

					Map<String, String> emailDataHM = new HashMap<>();
					emailDataHM.put("CompanyName", companyName);
					emailDataHM.put("InvoiceDate", formattedInvoiceDateString);
					emailDataHM.put("Address", address);
					emailDataHM.put("Email", emailId);
					emailDataHM.put("MobileNum", String.valueOf(mobileNumber));
					emailDataHM.put("Tax", String.valueOf(f));
					emailDataHM.put("GST", String.valueOf(GST));
					emailDataHM.put("gstNumber", gstNumber);
					emailDataHM.put("TotalAmount", String.valueOf(invoiceAmount));

					String message = freeMarkerUtils.getHtml1("ProFormaInvoice.html", emailDataHM);
					
					Map<String, String> slaData = new HashMap<>();
					slaData.put("companyName", companyName);
				
					Map<String, String> slaData1= new HashMap<>();
					slaData1.put("companyName", companyName);
					slaData1.put("fresherAmount","1999");
					slaData1.put("experiencedAmount","2999");
					slaData1.put("date",invoiceDate);
					
					String sla1 = freeMarkerUtils.getHtml1("SLA1.html", slaData);
					String sla2 = freeMarkerUtils.getHtml1("SLA2.html", slaData1);
					
					// Convert HTML to PDF for SLA1
					ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
					ConverterProperties converterProperties1 = new ConverterProperties();
					converterProperties1.setBaseUri(baseUrl);
					HtmlConverter.convertToPdf(sla1, baos1, converterProperties1);
					byte[] pdfBytes1 = baos1.toByteArray();

					// Convert HTML to PDF for SLA2
					ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
					ConverterProperties converterProperties2 = new ConverterProperties();
					converterProperties2.setBaseUri(baseUrl);
					HtmlConverter.convertToPdf(sla2, baos2, converterProperties2);
					byte[] pdfBytes2 = baos2.toByteArray();

				
					// Initialize PDFMergerUtility
					PDFMergerUtility pdfMerger = new PDFMergerUtility();

					// Add SLA1 and SLA2 PDFs to be merged
					pdfMerger.addSource(new ByteArrayInputStream(pdfBytes1));
					pdfMerger.addSource(new ByteArrayInputStream(pdfBytes2));

					// Set the destination stream for the merged PDF
					ByteArrayOutputStream mergedPdfStream = new ByteArrayOutputStream();
					pdfMerger.setDestinationStream(mergedPdfStream);

					// Merge the PDFs
					pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());


					String[] SRC = {sla1,sla2};

					ConverterProperties properties = new ConverterProperties();
					properties.setBaseUri(baseUrl);
			        ByteArrayOutputStream baos = new ByteArrayOutputStream();
			        PdfWriter writer = new PdfWriter(baos);
			        PdfDocument pdf = new PdfDocument(writer);
			        PdfMerger merger = new PdfMerger(pdf);

			        for (String html : SRC) {
			        PdfDocument temp = new PdfDocument(new PdfWriter(baos));
			        HtmlConverter.convertToPdf(html, temp, properties);
			        temp = new PdfDocument(new PdfReader(new ByteArrayInputStream(baos.toByteArray())));
			        merger.merge(temp, 1, temp.getNumberOfPages());
			        temp.close();
			        }
			        pdf.close();
			        
			     // Get the merged PDF as a byte array
			        byte[] mergedPdfBytes = mergedPdfStream.toByteArray();


				
					// Convert HTML to PDF
					ByteArrayOutputStream target = new ByteArrayOutputStream();
					ConverterProperties converterProperties = new ConverterProperties();
					converterProperties.setBaseUri(baseUrl);
					HtmlConverter.convertToPdf(message, target, converterProperties);
					byte[] bytes = target.toByteArray();

					// Generate HTML email content
					String emailContent = freeMarkerUtils.getHtml1("ProFormaInvoiceEmailContent.html", data);

					if (activeProfile.equalsIgnoreCase("prod")) {
					// Send email with attachments
					amazonSESMailUtil.sendEmailWithMultipleAttachments1(emailId,adminId, emailContent, mergedPdfBytes,bytes);
					}
					proFormaInvoice.setEmailNotification(true);
					proFormaInvoiceRepository.save(proFormaInvoice);

					EmployerTimeline employerTimeline = new EmployerTimeline();
					Date currentDate1 = new Date();
					SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
					String formattedDate1 = dateFormat1.format(currentDate1);
					String eventDescription = "Proforma Invoice send on  <b>" + formattedDate1 + "</b> By <b>" + admin.getUserName() + "</b>";
					employerTimeline.setEmpId(employer.getId());
					employerTimeline.setEmpLeadId(0);
					employerTimeline.setEventName("Proforma Invoice");
					employerTimeline.setEventDescription(eventDescription);
					employerTimelineRepository.save(employerTimeline);

					File file = null;
					try {
						// Choose an appropriate path for the temporary PDF file
						file = File.createTempFile("proFormaInvoice", ".pdf");
						FileOutputStream iofs = new FileOutputStream(file);
						iofs.write(bytes);
						iofs.close();

						String path = folderName + "/" + "ProFormaInvoices" + "/";

						DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
						String namedate = dateFormat.format(new Date());
						String fileName = removeSpacesAndConcatenate(companyName, "_INR", String.valueOf(invoiceAmount),
								namedate, ".pdf");
						String imagepath = path + fileName;

						uploadFileTos3bucket(imagepath, file);
						file.delete();

						String s3BucketLink = folderName + "/" + "ProFormaInvoices" + "/" + fileName;
						System.out.println(s3BucketLink);

						if (discountPercentage <= 0)  {

							HashMap<String, String> waData = new HashMap<>();
							waData.put("mn", "91" + String.valueOf(mobileNumber));
							waData.put("contact_person_name", contactPersonName);
							waData.put("plan_amount", String.valueOf(invoiceAmount));
							waData.put("no_of_openings", String.valueOf(noOfOpenings));
							waData.put("payment_link", payment.get("short_url").toString());
							waData.put("s3_bucket_link", s3BucketLink);

							if (activeProfile.equalsIgnoreCase("prod")) {
								waAlertService.sendProFormaInvoiceAlert(waData);
							}
							proFormaInvoice.setEmailNotification(true);
							proFormaInvoice.setWaNotification(true);
							proFormaInvoiceRepository.save(proFormaInvoice);
						}
						else {

							// Send different WhatsApp template for discounted invoices
							HashMap<String, String> waDataDiscounted = new HashMap<>();
							waDataDiscounted.put("mn", "91" + String.valueOf(mobileNumber));
							waDataDiscounted.put("contact_person_name", contactPersonName);
							waDataDiscounted.put("plan_amount", String.valueOf(originalAmount));
							waDataDiscounted.put("offer_price",String.valueOf(invoiceAmount));
							waDataDiscounted.put("no_of_openings", String.valueOf(noOfOpenings));
							waDataDiscounted.put("validity_date", formattedValidityDate);
							waDataDiscounted.put("discount_percentage",String.valueOf(formattedDiscountPercentage));
							waDataDiscounted.put("payment_link", payment.get("short_url").toString());
							waDataDiscounted.put("s3_bucket_link", s3BucketLink);

							if (activeProfile.equalsIgnoreCase("prod")) {
								waAlertService.sendProFormaInvoiceDiscountAlert(waDataDiscounted);
							}
							proFormaInvoice.setWaNotification(true);
							proFormaInvoiceRepository.save(proFormaInvoice);
						}

					} catch (Exception e) {

						handleException(responseMap, e, e.getMessage());
					}

				}

				// Send WhatsApp alert only if emailId is not present
				else {

					Map<String, String> emailDataHM = new HashMap<>();
					emailDataHM.put("CompanyName", companyName);
					emailDataHM.put("InvoiceDate", formattedInvoiceDateString);
					emailDataHM.put("Address", address);
					emailDataHM.put("Email", emailId);
					emailDataHM.put("MobileNum", String.valueOf(mobileNumber));
					emailDataHM.put("Tax", String.valueOf(f));
					emailDataHM.put("GST", String.valueOf(GST));
					emailDataHM.put("gstNumber", gstNumber);
					emailDataHM.put("TotalAmount", String.valueOf(invoiceAmount));

					String message = freeMarkerUtils.getHtml1("ProFormaInvoice.html", emailDataHM);

					// Convert HTML to PDF
					ByteArrayOutputStream target = new ByteArrayOutputStream();
					ConverterProperties converterProperties = new ConverterProperties();
					converterProperties.setBaseUri(baseUrl);
					HtmlConverter.convertToPdf(message, target, converterProperties);
					byte[] bytes = target.toByteArray();

					File file = null;

					// Choose an appropriate path for the temporary PDF file
					file = File.createTempFile("proFormaInvoice", ".pdf");
					FileOutputStream iofs = new FileOutputStream(file);
					iofs.write(bytes);
					iofs.close();

					String path = folderName + "/" + "ProFormaInvoices" + "/";

					DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
					String namedate = dateFormat.format(new Date());
					String fileName = removeSpacesAndConcatenate(companyName, "_INR", String.valueOf(invoiceAmount),
							namedate, ".pdf");
					String imagepath = path + fileName;

					uploadFileTos3bucket(imagepath, file);
					file.delete();

					String s3BucketLink = folderName + "/" + "ProFormaInvoices" + "/" + fileName;
					System.out.println(s3BucketLink);

					if (discountPercentage <= 0)  {
						HashMap<String, String> waData = new HashMap<>();
						waData.put("mn", "91" + String.valueOf(mobileNumber));
						waData.put("contact_person_name", contactPersonName);
						waData.put("plan_amount", String.valueOf(invoiceAmount));
						waData.put("no_of_openings", String.valueOf(noOfOpenings));
						waData.put("payment_link", payment.get("short_url").toString());
						waData.put("s3_bucket_link", s3BucketLink);

						if (activeProfile.equalsIgnoreCase("prod")) {
							waAlertService.sendProFormaInvoiceAlert(waData);
						}
						proFormaInvoice.setEmailNotification(false);
						proFormaInvoice.setWaNotification(true);
						proFormaInvoiceRepository.save(proFormaInvoice);
					}
					else {
						// Send different WhatsApp template for discounted invoices
						HashMap<String, String> waDataDiscounted = new HashMap<>();
						waDataDiscounted.put("mn", "91" + String.valueOf(mobileNumber));
						waDataDiscounted.put("contact_person_name", contactPersonName);
						waDataDiscounted.put("plan_amount", String.valueOf(originalAmount));
						waDataDiscounted.put("offer_price",String.valueOf(invoiceAmount));
						waDataDiscounted.put("discount_percentage",String.valueOf(formattedDiscountPercentage));
						waDataDiscounted.put("no_of_openings", String.valueOf(noOfOpenings));
						waDataDiscounted.put("validity_date", formattedValidityDate);
						waDataDiscounted.put("payment_link", payment.get("short_url").toString());
						waDataDiscounted.put("s3_bucket_link", s3BucketLink);

						if (activeProfile.equalsIgnoreCase("prod")) {
							waAlertService.sendProFormaInvoiceDiscountAlert(waDataDiscounted);
						}
						proFormaInvoice.setEmailNotification(false);
						proFormaInvoice.setWaNotification(true);
						proFormaInvoiceRepository.save(proFormaInvoice);
					}

				}
			}
			// new User send email & waAlert
			else {
				// Send email and WhatsApp alerts if emailId is present
				if (emailId != null && !emailId.isEmpty()) {
					HashMap<String, String> data = new HashMap<>();
					data.put("amount", String.valueOf(invoiceAmount));
					data.put("name", contactPersonName);
					data.put("no_of_openings", noOfOpenings);
					data.put("discount_percentage", String.valueOf(formattedDiscountPercentage));
					if (discountPercentage > 0) {
						data.put("discount_txt_visible", "");
						data.put("offer_price", String.valueOf(invoiceAmount));
						data.put("validity_date", formattedValidityDate);
					} else {
						data.put("discount_txt_visible", "d-none");
						data.put("offer_price", String.valueOf(invoiceAmount));
						data.put("validity_date", formattedValidityDate);
					}

					data.put("payment_link", payment.get("short_url").toString());

					Map<String, String> emailDataHM = new HashMap<>();
					emailDataHM.put("CompanyName", companyName);
					emailDataHM.put("InvoiceDate", formattedInvoiceDateString);
					emailDataHM.put("Address", address);
					emailDataHM.put("Email", emailId);
					emailDataHM.put("MobileNum", String.valueOf(mobileNumber));
					emailDataHM.put("Tax", String.valueOf(f));
					emailDataHM.put("GST", String.valueOf(GST));
					emailDataHM.put("gstNumber", gstNumber);
					emailDataHM.put("TotalAmount", String.valueOf(invoiceAmount));

					String message = freeMarkerUtils.getHtml1("ProFormaInvoice.html", emailDataHM);

					// Convert HTML to PDF
					ByteArrayOutputStream target = new ByteArrayOutputStream();
					ConverterProperties converterProperties = new ConverterProperties();
					converterProperties.setBaseUri(baseUrl);
					HtmlConverter.convertToPdf(message, target, converterProperties);
					byte[] bytes = target.toByteArray();

					// Generate HTML email content
					String emailContent = freeMarkerUtils.getHtml1("ProFormaInvoiceEmailContent.html", data);
					
					Map<String, String> slaData = new HashMap<>();
					slaData.put("companyName", companyName);
				
					Map<String, String> slaData1= new HashMap<>();
					slaData1.put("companyName", companyName);
					slaData1.put("fresherAmount","1999");
					slaData1.put("experiencedAmount","2999");
					slaData1.put("date",invoiceDate);
					
					String  sla1 = freeMarkerUtils.getHtml1("SLA1.html", slaData);
					String sla2 = freeMarkerUtils.getHtml1("SLA2.html", slaData1);
					
					// Convert HTML to PDF for SLA1
					ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
					ConverterProperties converterProperties1 = new ConverterProperties();
					converterProperties1.setBaseUri(baseUrl);
					HtmlConverter.convertToPdf(sla1, baos1, converterProperties1);
					byte[] pdfBytes1 = baos1.toByteArray();

					// Convert HTML to PDF for SLA2
					ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
					ConverterProperties converterProperties2 = new ConverterProperties();
					converterProperties2.setBaseUri(baseUrl);
					HtmlConverter.convertToPdf(sla2, baos2, converterProperties2);
					byte[] pdfBytes2 = baos2.toByteArray();

				
					// Initialize PDFMergerUtility
					PDFMergerUtility pdfMerger = new PDFMergerUtility();

					// Add SLA1 and SLA2 PDFs to be merged
					pdfMerger.addSource(new ByteArrayInputStream(pdfBytes1));
					pdfMerger.addSource(new ByteArrayInputStream(pdfBytes2));

					// Set the destination stream for the merged PDF
					ByteArrayOutputStream mergedPdfStream = new ByteArrayOutputStream();
					pdfMerger.setDestinationStream(mergedPdfStream);

					// Merge the PDFs
					pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());


					String[] SRC = {sla1,sla2};

					ConverterProperties properties = new ConverterProperties();
					properties.setBaseUri(baseUrl);
			        ByteArrayOutputStream baos = new ByteArrayOutputStream();
			        PdfWriter writer = new PdfWriter(baos);
			        PdfDocument pdf = new PdfDocument(writer);
			        PdfMerger merger = new PdfMerger(pdf);

			        for (String html : SRC) {
			        PdfDocument temp = new PdfDocument(new PdfWriter(baos));
			        HtmlConverter.convertToPdf(html, temp, properties);
			        temp = new PdfDocument(new PdfReader(new ByteArrayInputStream(baos.toByteArray())));
			        merger.merge(temp, 1, temp.getNumberOfPages());
			        temp.close();
			        }
			        pdf.close();
			        
			     // Get the merged PDF as a byte array
			        byte[] mergedPdfBytes = mergedPdfStream.toByteArray();


			        if (activeProfile.equalsIgnoreCase("prod")) {
					// Send email with attachments
					amazonSESMailUtil.sendEmailWithMultipleAttachments1(emailId,adminId,emailContent, mergedPdfBytes,bytes);
			        }
					proFormaInvoice.setEmailNotification(true);
					proFormaInvoiceRepository.save(proFormaInvoice);

					EmployerTimeline employerTimeline = new EmployerTimeline();
					Date currentDate1 = new Date();
					SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
					String formattedDate1 = dateFormat1.format(currentDate1);
					String eventDescription = "Proforma Invoice sent on <b>" + formattedDate1 + "</b> By <b>" + admin.getUserName() + "</b";
					employerTimeline.setEventName("Proforma Invoice");
					employerTimeline.setEventDescription(eventDescription);

					if (employer != null) {
					    employerTimeline.setEmpId(employer.getId());
					} else {
					    employerTimeline.setEmpId(0);
					}

					// Assuming you retrieve leadModel from your repository or another source
					LeadModel leadModel = leadRepository.findByMobileNumber(mobileNumber).orElse(null);

					if (leadModel != null) {
					    employerTimeline.setEmpLeadId(leadModel.getId());
					} else {
					    employerTimeline.setEmpLeadId(0);
					}

					employerTimelineRepository.save(employerTimeline);



					File file = null;
					try {
						// Choose an appropriate path for the temporary PDF file
						file = File.createTempFile("proFormaInvoice", ".pdf");
						FileOutputStream iofs = new FileOutputStream(file);
						iofs.write(bytes);
						iofs.close();

						String path = folderName + "/" + "ProFormaInvoices" + "/";

						DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
						String namedate = dateFormat.format(new Date());
						String fileName = removeSpacesAndConcatenate(companyName, "_INR", String.valueOf(invoiceAmount),
								namedate, ".pdf");
						String imagepath = path + fileName;

						uploadFileTos3bucket(imagepath, file);
						file.delete();

						String s3BucketLink = folderName + "/" + "ProFormaInvoices" + "/" + fileName;
						System.out.println(s3BucketLink);

						if (discountPercentage <= 0)  {

							HashMap<String, String> waData = new HashMap<>();
							waData.put("mn", "91" + String.valueOf(mobileNumber));
							waData.put("contact_person_name", contactPersonName);
							waData.put("plan_amount", String.valueOf(invoiceAmount));
							waData.put("no_of_openings", String.valueOf(noOfOpenings));
							waData.put("payment_link", payment.get("short_url").toString());
							waData.put("s3_bucket_link", s3BucketLink);

							if (activeProfile.equalsIgnoreCase("prod")) {
								waAlertService.sendProFormaInvoiceAlert(waData);
							}
							proFormaInvoice.setWaNotification(true);
							proFormaInvoiceRepository.save(proFormaInvoice);
						}
						else {

							// Send different WhatsApp template for discounted invoices
							HashMap<String, String> waDataDiscounted = new HashMap<>();
							waDataDiscounted.put("mn", "91" + String.valueOf(mobileNumber));
							waDataDiscounted.put("contact_person_name", contactPersonName);
							waDataDiscounted.put("plan_amount", String.valueOf(originalAmount));
							waDataDiscounted.put("offer_price",String.valueOf(invoiceAmount));
							waDataDiscounted.put("no_of_openings", String.valueOf(noOfOpenings));
							waDataDiscounted.put("validity_date", formattedValidityDate);
							waDataDiscounted.put("discount_percentage",String.valueOf(formattedDiscountPercentage));
							waDataDiscounted.put("payment_link", payment.get("short_url").toString());
							waDataDiscounted.put("s3_bucket_link", s3BucketLink);

							if (activeProfile.equalsIgnoreCase("prod")) {
								waAlertService.sendProFormaInvoiceDiscountAlert(waDataDiscounted);
							}
							proFormaInvoice.setWaNotification(true);
							proFormaInvoiceRepository.save(proFormaInvoice);
						}

					} catch (Exception e) {

						handleException(responseMap, e, e.getMessage());
					}

				}

				// Send WhatsApp alert only if emailId is not present
				else {
					
					

					Map<String, String> emailDataHM = new HashMap<>();
					emailDataHM.put("CompanyName", companyName);
					emailDataHM.put("InvoiceDate", formattedInvoiceDateString);
					emailDataHM.put("Address", address);
					emailDataHM.put("Email", emailId);
					emailDataHM.put("MobileNum", String.valueOf(mobileNumber));
					emailDataHM.put("Tax", String.valueOf(f));
					emailDataHM.put("GST", String.valueOf(GST));
					emailDataHM.put("gstNumber", gstNumber);
					emailDataHM.put("TotalAmount", String.valueOf(invoiceAmount));

					String message = freeMarkerUtils.getHtml1("ProFormaInvoice.html", emailDataHM);
					
					
					
					

					// Convert HTML to PDF
					ByteArrayOutputStream target = new ByteArrayOutputStream();
					ConverterProperties converterProperties = new ConverterProperties();
					converterProperties.setBaseUri(baseUrl);
					HtmlConverter.convertToPdf(message, target, converterProperties);
					byte[] bytes1 = target.toByteArray();

					File file = null;

					// Choose an appropriate path for the temporary PDF file
					file = File.createTempFile("proFormaInvoice", ".pdf");
					FileOutputStream iofs = new FileOutputStream(file);
					iofs.write(bytes1);
					iofs.close();

					String path = folderName + "/" + "ProFormaInvoices" + "/";

					DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
					String namedate = dateFormat.format(new Date());
					String fileName = removeSpacesAndConcatenate(companyName, "_INR", String.valueOf(invoiceAmount),
							namedate, ".pdf");
					String imagepath = path + fileName;

					uploadFileTos3bucket(imagepath, file);
					file.delete();

					String s3BucketLink = folderName + "/" + "ProFormaInvoices" + "/" + fileName;
					System.out.println(s3BucketLink);
					
					

					if (discountPercentage <= 0)  {
						HashMap<String, String> waData = new HashMap<>();
						waData.put("mn", "91" + String.valueOf(mobileNumber));
						waData.put("contact_person_name", contactPersonName);
						waData.put("plan_amount", String.valueOf(invoiceAmount));
						waData.put("no_of_openings", String.valueOf(noOfOpenings));
						waData.put("payment_link", payment.get("short_url").toString());
						waData.put("s3_bucket_link", s3BucketLink);

						if (activeProfile.equalsIgnoreCase("prod")) {
							waAlertService.sendProFormaInvoiceAlert(waData);
						}
						proFormaInvoice.setEmailNotification(false);
						proFormaInvoice.setWaNotification(true);
						proFormaInvoiceRepository.save(proFormaInvoice);
					}
					else {
						// Send different WhatsApp template for discounted invoices
						HashMap<String, String> waDataDiscounted = new HashMap<>();
						waDataDiscounted.put("mn", "91" + String.valueOf(mobileNumber));
						waDataDiscounted.put("contact_person_name", contactPersonName);
						waDataDiscounted.put("plan_amount", String.valueOf(originalAmount));
						waDataDiscounted.put("offer_price",String.valueOf(invoiceAmount));
						waDataDiscounted.put("discount_percentage",String.valueOf(formattedDiscountPercentage));
						waDataDiscounted.put("no_of_openings", String.valueOf(noOfOpenings));
						waDataDiscounted.put("validity_date", formattedValidityDate);
						waDataDiscounted.put("payment_link", payment.get("short_url").toString());
						waDataDiscounted.put("s3_bucket_link", s3BucketLink);

						if (activeProfile.equalsIgnoreCase("prod")) {
							waAlertService.sendProFormaInvoiceDiscountAlert(waDataDiscounted);
						}
						proFormaInvoice.setEmailNotification(false);
						proFormaInvoice.setWaNotification(true);
						proFormaInvoiceRepository.save(proFormaInvoice);
					}

				}
			}

			responseMap.put("status", "success");
			responseMap.put("message", "ProForma Invoice sent successfully");
			responseMap.put("code", 200);

		} catch (IOException e) {
			handleException(responseMap, e, e.getMessage());
		} catch (Exception e) {
			handleException(responseMap, e, e.getMessage());
		}

		return ResponseEntity.status((int) responseMap.get("code")).body(responseMap);
	}


	private void handleException(Map<String, Object> responseMap, Exception e, String errorMessage) {
		e.printStackTrace();
		responseMap.put("status", "error");
		responseMap.put("message", errorMessage);
		responseMap.put("code", 500);
	}

	private void uploadFileTos3bucket(String fileName, File file) {
		s3client.putObject(
				new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
	}

	private String generateUniqueReferenceId() {
		String randomComponent = String.format("%06d", new Random().nextInt(1000000));
		return randomComponent;
	}

	private String removeSpacesAndConcatenate(String... parts) {
		String concatenated = String.join("", parts);
		return concatenated.replaceAll("\\s+", "");
	}

	@GetMapping("/invoicePaymentDetails")
	public String getPaymentByReferenceId(@RequestParam("razorpay_payment_link_id") String linkId,
										  @RequestParam("razorpay_payment_id") String payid,
										  @RequestParam("razorpay_payment_link_reference_id") String refId,
										  @RequestParam("razorpay_payment_link_status") String status,
										  @RequestParam("razorpay_signature") String signature) throws RazorpayException {

		signatureVerification(refId, payid, status, linkId, signature);

		return "ProformaPaymentSuccess";
	}

	private void signatureVerification(String refId, String payid, String status, String linkId, String signature)
			throws RazorpayException {
		RazorpayClient razorpay = new RazorpayClient(KeyId, secretKey);

		JSONObject options = new JSONObject();
		options.put("payment_link_reference_id", refId);
		options.put("razorpay_payment_id", payid);
		options.put("payment_link_status", status);
		options.put("payment_link_id", linkId);
		options.put("razorpay_signature", signature);

		try {

			boolean pStatus = Utils.verifyPaymentLink(options, secretKey);
			if (pStatus) {
				PaymentLink paymentDetails = razorpay.paymentLink.fetch(linkId);
				JSONObject notes = paymentDetails.toJson().getJSONObject("notes");
				int empId = Integer.parseInt(notes.get("employerId").toString());
				long adminId = Long.parseLong(notes.get("adminId").toString());
				int leadId = Integer.parseInt(notes.get("leadId").toString());
				long invoiceId = Long.parseLong(notes.get("invoiceId").toString());
				JSONArray paymentArr = paymentDetails.toJson().getJSONArray("payments");
				JSONObject payments = paymentArr.getJSONObject(0);
				int amount = Integer.parseInt(payments.get("amount").toString());
				int amountPaid = amount / 100;
				String paymentID = payments.get("payment_id").toString();
				String paymentMethod = payments.get("method").toString();
				String orderID = paymentDetails.get("order_id").toString();
				String emailID = notes.get("email").toString();
				//int noOfOpenings = Integer.parseInt(notes.get("noOfOpenings").toString());


				Optional<EmployerModel> optional = employerRepository.findById(empId);

				if (optional.isPresent()) {
					EmployerModel existing = optional.get();
					EmployerPaymentModel pay = new EmployerPaymentModel();

					Optional<EmpProformaInvoiceModel> invoiceDetails = proFormaInvoiceRepository.findById(invoiceId);
					String jobDetails = invoiceDetails.get().getJobDetails();
					JSONArray jobs = new JSONArray(jobDetails);

					PlansModel plans;

					plans = plansRepository.findByActiveAndIsExperienced(true, false);

					try {
						Payment payment = razorpay.payments.fetch(paymentID);
						pay.setStatus(payment.get("status"));
						pay.setCaptured(payment.get("captured"));
						pay.setNotes(payment.get("notes").toString());
						pay.setFromAdmin(true);
						pay.setTypeOfPurchase(payment.get("method"));
						pay.setMobileNumber(Long.parseLong(payment.get("contact")));
						//pay.setEmailId(payment.get("email"));

					} catch (Exception e) {
						e.printStackTrace();
					}

					pay.setEmailId(emailID);
					pay.setEmployerId(empId);
					pay.setAmount(amountPaid);
					pay.setPaymentId(paymentID);
					pay.setOrderId(orderID);
					pay.setNumberOfOpenings(0);
					pay.setNumberOfJobCategory(0);
					pay.setSignature("Yes");
					pay.setAdminId((int) adminId);
					pay.setPaidOn(String.valueOf(LocalDateTime.now()));
					pay.setProformaInvoiceId((int) invoiceId);
					employerPaymentRepository.save(pay);
					
					if (activeProfile.equalsIgnoreCase("prod")) {
						try {
							sendInvoice(empId, amountPaid, pay.getId(), plans.getPlanName());
						} catch (IOException e) {
							e.printStackTrace();
						} catch (TemplateException e) {
							e.printStackTrace();
						}
					}

					if(invoiceDetails.isPresent())
					{
						EmpProformaInvoiceModel empProformaInvoiceModel = invoiceDetails.get();
						empProformaInvoiceModel.setPaid(true);
						empProFormaInvoiceRepository.save(empProformaInvoiceModel);

					}
					
					List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(Long.valueOf(adminId));

					if (!adminAnalyticsList.isEmpty()) {
					    // Check if the createdOn date is the same as the current date
					    LocalDate currentDate = LocalDate.now();
					    boolean dateMatch = false;

					    for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
					        Integer empNoOfPayment = adminAnalytics.getEmpNoOfPayment();

					        // Check if empNoOfPayment is not null
					        if (empNoOfPayment != null) {
					            LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
					            if (currentDate.isEqual(createdOnDate)) {
					                // If the dates are the same, increment noOfCalls by 1
					                adminAnalytics.setEmpNoOfPayment(empNoOfPayment + 1);
					                dateMatch = true;
					            }
					        }
					    }

					    if (!dateMatch) {
					        Optional<Admin> admin = adminRepository.findById(Long.valueOf(adminId));
					        Admin a = admin.get();
					        String module = a.getModule();
					        // If the dates are different for all records, insert a new record
					        AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
					        newAdminAnalytics.setAdminId(Long.valueOf(adminId));
					        newAdminAnalytics.setModule(module);
					        newAdminAnalytics.setEmpNoOfPayment(1);
					        adminAnalyticsList.add(newAdminAnalytics);
					    }

					    adminAnalyticsRepository.saveAll(adminAnalyticsList);
					} else {
					    Optional<Admin> admin = adminRepository.findById(Long.valueOf(adminId));
					    Admin a = admin.get();
					    String module = a.getModule();
					    // If there are no existing records for the adminId, insert a new record
					    AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
					    adminAnalytics.setAdminId(Long.valueOf(adminId));
					    adminAnalytics.setModule(module);
					    adminAnalytics.setEmpNoOfPayment(1);
					    adminAnalyticsRepository.save(adminAnalytics);
					}


					EmployerActivityModel EA = new EmployerActivityModel();
					EA.setEmpId(empId);
					EA.setActivity("Payment of " + "<b>INR " + amountPaid + "</b>" + " has been successful!");
					empActivityRepository.save(EA);

					existing.setPaymentStatus("Paid");
					/*
					 * existing.setPlan(planId); existing.setUsedFreeTrial("Yes");
					 * existing.setExpiryDate(output); if (planId == 9) {
					 * existing.setPlanJobCount(existing.getPlanJobCount()+noOfJobCategory); } else
					 * { existing.setPlanJobCount(plan1.getActiveJobs()); }
					 */

					employerRepository.save(existing);

					for (int i = 0; i < jobs.length(); i++) {
						JSONObject job = jobs.getJSONObject(i);
						PlansModel plan;
						boolean experienced = Boolean.parseBoolean(job.get("isExperienced").toString());

						if (experienced) {
							plan = plansRepository.findByActiveAndIsExperienced(true, true);
						} else {
							plan = plansRepository.findByActiveAndIsExperienced(true, false);
						}

						if (plan == null) {
							throw new IllegalArgumentException("No suitable plan found");
						}
						EmpPlacementPlanDetailsModel empPlacementPlanDetails = new EmpPlacementPlanDetailsModel();
						empPlacementPlanDetails.setPlanId(plan.getId());
						empPlacementPlanDetails.setPaymentId(pay.getId());
						empPlacementPlanDetails.setActive(true);
						empPlacementPlanDetails.setFromSource("Admin");
						empPlacementPlanDetails.setEmployerId(empId);
						empPlacementPlanDetails.setNoOfOpenings(Integer.parseInt(job.get("noOfOpenings").toString()));
						empPlacementPlanDetails.setIndustry((String) job.get("industry"));
						empPlacementPlanDetails.setJobCategory((String) job.get("jobCategory"));
						empPlacementPlanDetails.setIsExperienced(job.getBoolean("isExperienced"));
						empPlacementPlanDetails.setJobMinExp((Integer) job.get("jobMinExp"));
						empPlacementPlanDetails.setMinSalary((Integer) job.get("maxSalary"));
						empPlacementPlanDetails.setMaxSalary((Integer) job.get("minSalary"));
						empPlacementPlanDetails.setWorkHours((String) job.get("workHours"));
						empPlacementPlanRepository.save(empPlacementPlanDetails);
					}
				} else {
					if (leadId != 0) {
						EmployerPaymentModel pay = new EmployerPaymentModel();

						PlansModel plans;

						plans = plansRepository.findByActiveAndIsExperienced(true, false);

						try {
							Payment payment = razorpay.payments.fetch(paymentID);
							pay.setStatus(payment.get("status"));
							pay.setCaptured(payment.get("captured"));
							pay.setNotes(payment.get("notes").toString());
							pay.setFromAdmin(true);
							pay.setTypeOfPurchase(payment.get("method"));
							pay.setMobileNumber(Long.parseLong(payment.get("contact")));
							//pay.setEmailId(payment.get("email"));

						} catch (Exception e) {
							e.printStackTrace();
						}

						pay.setEmailId(emailID);
						pay.setEmployerId(0);
						pay.setLeadId(leadId);
						pay.setProformaInvoiceId((int) invoiceId);
						pay.setAmount(amountPaid);
						pay.setPaymentId(paymentID);
						pay.setOrderId(orderID);
						pay.setNumberOfOpenings(0);
						pay.setNumberOfJobCategory(0);
						pay.setSignature("Yes");
						pay.setAdminId((int) adminId);
						pay.setPaidOn(String.valueOf(LocalDateTime.now()));
						employerPaymentRepository.save(pay);
						
						if (activeProfile.equalsIgnoreCase("prod")) {
							try {
								sendInvoice(empId, amountPaid, pay.getId(), plans.getPlanName());
							} catch (IOException e) {
								e.printStackTrace();
							} catch (TemplateException e) {
								e.printStackTrace();
							}
						}
						
						List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(Long.valueOf(adminId));

						if (!adminAnalyticsList.isEmpty()) {
						    // Check if the createdOn date is the same as the current date
						    LocalDate currentDate = LocalDate.now();
						    boolean dateMatch = false;

						    for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
						        Integer empNoOfPayment = adminAnalytics.getEmpNoOfPayment();

						        // Check if empNoOfPayment is not null
						        if (empNoOfPayment != null) {
						            LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
						            if (currentDate.isEqual(createdOnDate)) {
						                // If the dates are the same, increment noOfCalls by 1
						                adminAnalytics.setEmpNoOfPayment(empNoOfPayment + 1);
						                dateMatch = true;
						            }
						        }
						    }

						    if (!dateMatch) {
						        Optional<Admin> admin = adminRepository.findById(Long.valueOf(adminId));
						        Admin a = admin.get();
						        String module = a.getModule();
						        // If the dates are different for all records, insert a new record
						        AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
						        newAdminAnalytics.setAdminId(Long.valueOf(adminId));
						        newAdminAnalytics.setModule(module);
						        newAdminAnalytics.setEmpNoOfPayment(1);
						        adminAnalyticsList.add(newAdminAnalytics);
						    }

						    adminAnalyticsRepository.saveAll(adminAnalyticsList);
						} else {
						    Optional<Admin> admin = adminRepository.findById(Long.valueOf(adminId));
						    Admin a = admin.get();
						    String module = a.getModule();
						    // If there are no existing records for the adminId, insert a new record
						    AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
						    adminAnalytics.setAdminId(Long.valueOf(adminId));
						    adminAnalytics.setModule(module);
						    adminAnalytics.setEmpNoOfPayment(1);
						    adminAnalyticsRepository.save(adminAnalytics);
						}
						
					}
				}

			}
		} catch (RazorpayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	@GetMapping("/activePlans")
	public ResponseEntity<Map<String, Object>> Plans(
			@RequestParam(required = false, defaultValue = "0") Integer page,
			@RequestParam(required = false, defaultValue = "10") Integer size
	) {
		boolean isActive = true;

		List<PlansModel> activePlans = plansService.findActivePlans(isActive);

		if (activePlans.isEmpty()) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("code", 404);
			errorResponse.put("message", "Active plans not found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}

		Map<String, Object> successResponse = new HashMap<>();
		successResponse.put("status", "success");
		successResponse.put("message", "success");
		successResponse.put("code", 200);
		successResponse.put("data", activePlans);
		return ResponseEntity.ok(successResponse);
	}
	
	@PostMapping("/employerPayment")
	public ResponseEntity<Map<String, Object>> createAdminEmployerPayment(
	        @RequestParam("employer_id") final int employerId,
	        @RequestParam(value = "payment_id", required = false) final String paymentId,
	        @RequestParam("job_details") final String jobDetails,
	        @RequestParam(value = "cheque_no", required = false) final String chequeNo,
	        @RequestParam(value = "payment_method", required = false) final String paymentMethod,
	        @RequestParam(value = "amount", required = false) final Integer amount,
	        @RequestParam(value = "cheque_date", required = false) final String chequeDate,
	        @RequestParam(value = "admin_id", required = false, defaultValue = "0") Integer adminId)
	        throws SignatureException, RazorpayException {

	    Map<String, Object> response = new HashMap<>();

	    EmployerPaymentModel emp = new EmployerPaymentModel();
	    Optional<EmployerModel> optional = employerRepository.findById(employerId);

	    if (!optional.isPresent()) {
	        response.put("code", 400);
	        response.put("error", "Employer not found with ID: " + employerId);
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	    }

	    JSONArray jobs = new JSONArray(jobDetails);
	    PlansModel plans = plansRepository.findByActiveAndIsExperienced(true, false);

	    int amountPaid = 0;

	    Optional<Admin>admin = adminRepository.findById(Long.valueOf(adminId));
        
        Admin a =admin.get();
        String module = a.getModule();
        
	    try {
	        if ("payment".equalsIgnoreCase(paymentMethod)) {
	            RazorpayClient razorpay = new RazorpayClient(KeyId, secretKey);
	            Payment payment = razorpay.payments.fetch(paymentId);

	            if (payment == null || !"captured".equals(payment.get("status"))) {
	                response.put("code", 400);
	                response.put("error", "Invalid or unpaid payment.");
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	            }

	            String orderID = payment.get("order_id").toString();
	            int amount1 = Integer.parseInt(payment.get("amount").toString());
	            amountPaid = amount1 / 100;

	            emp.setStatus(payment.get("status"));
	            emp.setCaptured(payment.get("captured"));
	            emp.setPaidOn(String.valueOf(LocalDateTime.now()));
	            emp.setNotes(payment.get("notes").toString());
	            emp.setFromAdmin(true);
	            emp.setTypeOfPurchase(payment.get("method"));
	            emp.setMobileNumber(Long.parseLong(payment.get("contact")));

	            emp.setEmailId(optional.get().getEmailId());
	            emp.setEmployerId(employerId);
	            emp.setAmount(amountPaid);
	            emp.setPaymentId(paymentId);
	            emp.setOrderId(orderID);
	            emp.setSignature("Yes");
	            emp.setAdminId(adminId != null ? adminId : 0);

	            employerPaymentRepository.save(emp);
	            
	            if (activeProfile.equalsIgnoreCase("prod")) {
	                try {
	                    sendInvoice(employerId, amountPaid, emp.getId(), plans.getPlanName());
	                } catch (IOException e) {
	                    e.printStackTrace();
	                } catch (TemplateException e) {
	                    e.printStackTrace();
	                }
	            }
	        } else if ("cheque".equalsIgnoreCase(paymentMethod)) {
	            emp.setEmailId(optional.get().getEmailId());
	            emp.setEmployerId(employerId);
	            emp.setAmount(amountPaid);
	            emp.setSignature("Yes");
	            emp.setChequeNo(chequeNo);
	            emp.setChequeDate(chequeDate);
	            emp.setFromAdmin(true);
	            emp.setAmount(amount);
	            emp.setTypeOfPurchase("cheque");
	            emp.setCaptured(true);
	            emp.setStatus("captured");
	            emp.setPaidOn(String.valueOf(LocalDateTime.now()));
	            emp.setMobileNumber(optional.get().getMobileNumber());

	            emp.setAdminId(adminId != null ? adminId : 0);

	            employerPaymentRepository.save(emp);
	            
	            if (activeProfile.equalsIgnoreCase("prod")) {
	                try {
	                    sendInvoice(employerId, amountPaid, emp.getId(), plans.getPlanName());
	                } catch (IOException e) {
	                    e.printStackTrace();
	                } catch (TemplateException e) {
	                    e.printStackTrace();
	                }
	            }
	        } else {
	            emp.setEmailId(optional.get().getEmailId());
	            emp.setEmployerId(employerId);
	            emp.setSignature("Yes");
	            emp.setAmount(amountPaid);
	            emp.setFromAdmin(true);
	            emp.setAmount(amount);
	            emp.setTypeOfPurchase("cash");
	            emp.setCaptured(true);
	            emp.setStatus("captured");
	            emp.setPaidOn(String.valueOf(LocalDateTime.now()));
	            emp.setMobileNumber(optional.get().getMobileNumber());

	            emp.setAdminId(adminId != null ? adminId : 0);

	            employerPaymentRepository.save(emp);
	            
	            if (activeProfile.equalsIgnoreCase("prod")) {
	                try {
	                    sendInvoice(employerId, amountPaid, emp.getId(), plans.getPlanName());
	                } catch (IOException e) {
	                    e.printStackTrace();
	                } catch (TemplateException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
			EmployerTimeline employerTimeline = new EmployerTimeline();
			Date currentDate1 = new Date();
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
			String formattedDate1 = dateFormat1.format(currentDate1);
			String eventDescription = "Payment of <b>INR " + amountPaid + "</b> on <b>" + formattedDate1 + "</b> By <b>" + a.getUserName() + "</b>";
			employerTimeline.setEmpId(employerId);
			employerTimeline.setEmpLeadId(0);
			employerTimeline.setEventName("Payment");
			employerTimeline.setEventDescription(eventDescription);
			employerTimelineRepository.save(employerTimeline);

			List<AdminAnalyticsModel> adminAnalyticsList = adminAnalyticsRepository.findByAdminId(Long.valueOf(adminId));

			// Assuming that adminAnalytics is an instance of AdminAnalyticsModel


			if (!adminAnalyticsList.isEmpty()) {
				// Check if the createdOn date is the same as the current date
				LocalDate currentDate = LocalDate.now();
				boolean dateMatch = false;

				for (AdminAnalyticsModel adminAnalytics : adminAnalyticsList) {
					LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
					if (currentDate.isEqual(createdOnDate)) {

						// If the dates are the same, increment noOfCalls by 1
						Integer empNoOfPayment = adminAnalytics.getEmpNoOfPayment();
						int empNoOfPaymentValue = (empNoOfPayment != null) ? empNoOfPayment.intValue() : 0;
						adminAnalytics.setEmpNoOfPayment(empNoOfPaymentValue + 1);

						dateMatch = true;
					}
				}

				if (!dateMatch) {

					// If the dates are different for all records, insert a new record
					AdminAnalyticsModel newAdminAnalytics = new AdminAnalyticsModel();
					newAdminAnalytics.setAdminId(Long.valueOf(adminId));
					newAdminAnalytics.setModule(module);
					newAdminAnalytics.setEmpNoOfPayment(1);
					adminAnalyticsList.add(newAdminAnalytics);
				}

				adminAnalyticsRepository.saveAll(adminAnalyticsList);
			} else {

				// If there are no existing records for the adminId, insert a new record
				AdminAnalyticsModel adminAnalytics = new AdminAnalyticsModel();
				adminAnalytics.setAdminId(Long.valueOf(adminId));
				adminAnalytics.setModule(module);
				adminAnalytics.setEmpNoOfPayment(1);
				adminAnalyticsRepository.save(adminAnalytics);
			}

	        for (int i = 0; i < jobs.length(); i++) {
	            JSONObject job = jobs.getJSONObject(i);
	            PlansModel plan;
	            boolean experienced = Boolean.parseBoolean(job.get("isExperienced").toString());

	            if (experienced) {
	                plan = plansRepository.findByActiveAndIsExperienced(true, true);
	            } else {
	                plan = plansRepository.findByActiveAndIsExperienced(true, false);
	            }

	            if (plan == null) {
	                response.put("error", "No suitable plan found");
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	            }
	            EmpPlacementPlanDetailsModel empPlacementPlanDetails = new EmpPlacementPlanDetailsModel();
	            empPlacementPlanDetails.setPlanId(plan.getId());
	            empPlacementPlanDetails.setPaymentId(emp.getId());
	            empPlacementPlanDetails.setActive(true);
	            empPlacementPlanDetails.setFromSource("Admin");
	            empPlacementPlanDetails.setEmployerId(employerId);
	            empPlacementPlanDetails.setNoOfOpenings(Integer.parseInt(job.get("noOfOpenings").toString()));
	            empPlacementPlanDetails.setIndustry((String) job.get("industry"));
	            empPlacementPlanDetails.setJobCategory((String) job.get("jobCategory"));
	            empPlacementPlanDetails.setIsExperienced(job.getBoolean("isExperienced"));
	            empPlacementPlanRepository.save(empPlacementPlanDetails);
	        }
	        EmployerActivityModel EA = new EmployerActivityModel();
	        EA.setEmpId(employerId);
	        EA.setActivity("Payment of <b>INR " + amountPaid + "</b> has been successful!");
	        empActivityRepository.save(EA);

	        response.put("code", 200);
	        response.put("success", "Successfully processed.");
	        return ResponseEntity.status(HttpStatus.CREATED).body(response);
	    } catch (Exception e) {
	        // Handle other exceptions
	        response.put("error", "Internal server error");
	        logger.error("An error occurred: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	public void sendInvoices(int empId, int amount, int payments, String pMethod) throws IOException, TemplateException {
		 EmployerPaymentModel payment=employerPaymentRepository.findById(empId);
		 EmployerModel employer=employerRepository.findById(empId).get();

		 String email = employer.getEmailId();
	    PlansModel pay = plansRepository.findByAmount(amount);
	  
	    List<EmployerModel> employers = employerRepository.findAll();

	    DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
	    String date = formatter.format(pay.getCreatedTime());

	    HashMap<String, String> data = new HashMap<>();
	    data.put("Amount", String.valueOf(amount));
	    data.put("PlanName", pay.getPlanName());
	    data.put("PaymentMethod", pMethod);
	    data.put("PaymentDate", date);


	    double f = Double.parseDouble(new DecimalFormat("##.##").format(amount / 1.18));
	    double GST = Double.parseDouble(new DecimalFormat("##.##").format((amount - f) / 2));

	    int year = Calendar.getInstance().get(Calendar.YEAR) % 100;
	    int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
	    String financialYear = null;

	    if (month < 4) {
	        financialYear = (year - 1) + "-" + year;
	    } else {
	        financialYear = year + "-" + (year + 1);
	    }


	    HashMap<String, String> emailDataHM = new HashMap<>();
	  
	    emailDataHM.put("CompanyName", employer.getCompanyName() != null ? employer.getCompanyName() : "");
	    emailDataHM.put("InvoiceNo", "TZ/" + financialYear + "/" + payment.getInvoiceNo());
	    emailDataHM.put("InvoiceDate", date);
	    emailDataHM.put("Address", employer.getAddress() != null ? employer.getAddress() : "");
	    emailDataHM.put("gstNum", employer.getRegProofNumber() != null ? employer.getRegProofNumber() : "");
	    emailDataHM.put("Email", employer.getEmailId() != null ? employer.getEmailId() : "");
	    emailDataHM.put("MobileNum", String.valueOf(employer.getMobileNumber()));
	    emailDataHM.put("Tax", String.valueOf(f));
	    emailDataHM.put("GST", String.valueOf(GST));
	    emailDataHM.put("TotalAmount", String.valueOf(amount));

	    String message = freeMarkerUtils.getHtml1("invoice.html", emailDataHM);

	    ByteArrayOutputStream target = new ByteArrayOutputStream();
	    ConverterProperties converterProperties = new ConverterProperties();
	    converterProperties.setBaseUri(baseUrl);

	    HtmlConverter.convertToPdf(message, target, converterProperties);

	    byte[] bytes = target.toByteArray();

	    amazonSESMailUtil.sendEmailWithAttachment(payment.getInvoiceNo(), email, bytes);
	 
	}




}