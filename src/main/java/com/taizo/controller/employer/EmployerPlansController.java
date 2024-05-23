package com.taizo.controller.employer;

import java.io.ByteArrayOutputStream; 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.*;
import com.taizo.repository.*;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.service.CloudWatchLogService;
import com.taizo.service.PlansService;
import com.taizo.service.UserServiceImpl;
import com.taizo.service.WAAlertService;
import com.taizo.utils.FreeMarkerUtils;
import com.taizo.utils.TupleStore;

import freemarker.template.TemplateException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@CrossOrigin
@RestController
@RequestMapping("/employer")
public class EmployerPlansController {

	@Autowired
	PlansRepository plansRepository;

	@Autowired
	AmazonSESMailUtil amazonSESMailUtil;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	PlansService plansService;

	@Autowired
	EmployerTimelineRepository employerTimelineRepository;

	@Value("${firebase.emp.api.key}")
	private String firebaseEmpApiKey;

	@Value("${firebase.deeplink.emp.hostname}")
	private String firebaseEmpHost;

	@Value("${firebase.empapp.package.name}")
	private String firebaseEmpPackage;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	EmployerPaymentRepository employerPaymentRepository;

	@Autowired
	EmpActivityRepository empActivityRepository;
	
	@Autowired
	EmpPlansRepository empPlansRepository;

	@Autowired
	CloudWatchLogService cloudWatchLogService;
	
	@Autowired
	EmpPlacementPlanDetailsRepository empPlacementPlanRepository;
	
	@Autowired
	private JobLeadRepository jobLeadRepository;
	
	@Autowired
	private FreeMarkerUtils freeMarkerUtils;

	@Value("${razor.key.id}")
	private String KeyId;

	@Value("${razor.secret.key}")
	private String secretKey;

	@Value("${spring.profiles.active}")
	private String activeProfile;
	
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
	WAAlertService waAlertService;

	@Autowired
	CfgFullTimeJobRolesSalariesRepository cfgFullTimeJobRolesSalariesRepository;
	
	private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

	private static final Logger logger = LoggerFactory.getLogger(EmployerPlansController.class);
	
	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.awssecretKey);
		this.s3client = new AmazonS3Client(credentials);
	}

	@GetMapping("/plans")
	public HashMap<String, Object> getPlans() {

		List<PlansModel> list = plansRepository.findAll();
		if (!list.isEmpty()) {
			list.remove(0);
			list.subList(3, 5).clear();

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", list);
			return map;
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "No Plans Available");
			return map;
		}

	}

	@GetMapping("/empPlans")
	public ResponseEntity<?> getEmpPlans(@RequestParam("emp_id") final int empId,
			@RequestParam("job_count") final int jobCount) throws ResourceNotFoundException {

		EmpPlansModel plan = empPlansRepository.findByJobCount(jobCount);
		
		if(plan!=null) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("status", "success");
		map.put("message", "success");
		map.put("code", 200);
		map.put("data", plan);
		return new ResponseEntity<>(map, HttpStatus.OK);
		}else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "No Plans Available");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}

	}

	@GetMapping("/subscriptionPlans")
	public ResponseEntity<?> getSubPlans() {

		List<PlansModel> list = plansRepository.findAllByActive(true);
		if (!list.isEmpty()) {

			HashMap<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("message", "success");
			map.put("code", 200);
			map.put("data", list);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "No Plans Available");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}

	}
	
	@GetMapping("/placementPlans")
	public ResponseEntity<?> getPlacementPlans(
	    @RequestParam("emp_id") final int empId,
	    @RequestParam("type") final String type) {

	        boolean Ctype = type.equalsIgnoreCase("Experienced");
	        
	    		List<PlansModel> list = plansRepository.findByExperiencedStatus(Ctype,true);
	    		if(list!=null) {
	            Map<String, Object> map = new HashMap<>();
	            map.put("status", "success");
	            map.put("message", "success");
	            map.put("code", 200);
	            map.put("data", list.get(0));
	            return new ResponseEntity<>(map, HttpStatus.OK);
	    		}
	    		else {
	    			HashMap<String, Object> map = new HashMap<>();
	    			map.put("code", 400);
	    			map.put("message", "No Plans Available");
	    			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
	    		}
	   
	}



	@PutMapping(path = "/selectedPlan")
	public ResponseEntity<?> setSelectedPlan(@RequestParam("employer_id") final int employerId,
			@RequestParam("plan_id") final int planId) {

		Optional<EmployerModel> optional = employerRepository.findById(employerId);

		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "Employer is not found");
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {

			EmployerModel existing = optional.get();
			Optional<PlansModel> empPlan = plansRepository.findById(planId);
			PlansModel plan1 = empPlan.get();

			int expDays = plan1.getPlanValidity();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DATE, expDays);
			String output = sdf.format(c.getTime());

			existing.setPaymentStatus("Paid");
			existing.setPlan(planId);
			existing.setExpiryDate(output);
			existing.setUsedFreeTrial("Yes");
			existing.setPlanJobCount(plan1.getActiveJobs());
			existing.setFreePlanExpiryDate(output);

			employerRepository.save(existing);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "Successfully Updated");
			return new ResponseEntity<>(map, HttpStatus.OK);

		}
	}

	@GetMapping("/orderId")
	public ResponseEntity<?> getEmployerOrderId(@RequestParam("amount") final int amount) throws RazorpayException {

		RazorpayClient razorpay = new RazorpayClient(KeyId, secretKey);
		String id = null;
		String errorMsg = null;
		try {
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", amount); // amount in paise
			orderRequest.put("currency", "INR");
			orderRequest.put("payment_capture", true);

			Order order = razorpay.orders.create(orderRequest);
			JSONObject jsonObject = new JSONObject(String.valueOf(order));
			id = jsonObject.getString("id");

			CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
			logEventModel.setType("Payment");
			logEventModel.setMessage("success");
			logEventModel.setDescription(orderRequest.toString());

			try {
				cloudWatchLogService.cloudLog(logEventModel, "E");
			} catch (Exception e) {

			}

		} catch (RazorpayException e) {
			errorMsg = e.getMessage();
			logger.error("error [" + e.getMessage() + "] occurred while creating order id");

			CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
			logEventModel.setType("Payment");
			logEventModel.setMessage("failure");
			logEventModel.setDescription(
					"error [" + e.getMessage() + "] occurred while generating employer payment order id");

			try {
				cloudWatchLogService.cloudLogFailure(logEventModel, "E");
			} catch (Exception e1) {

			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", errorMsg);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

		if (!id.isEmpty() && id != null) {

			EmpOrderIdModel model = new EmpOrderIdModel();
			model.setOrderId(id);
			model.setKeyId(KeyId);

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("message", "success");
			map.put("status", "success");
			map.put("data", model);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", errorMsg);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/employerPayment", method = RequestMethod.POST)
	public ResponseEntity<?> createEmployerPayment(
			@RequestParam("employer_id") final int employerId,
			@RequestParam("amount") final int amount,
			@RequestParam(value = "plan_id",required = false,defaultValue = "0") final int planId,
			@RequestParam("payment_id") final String paymentId,
			@RequestParam("order_id") final String orderId,
			@RequestParam(value = "email_id", required = false) final String emailId,
			@RequestParam("mobile_number") final String mobNum,
			@RequestParam("status") final String status,
			@RequestParam("signature") final String signature,
			@RequestParam(value = "job_count", required = false,defaultValue = "0") final int jobCount,
			@RequestParam(value = "plan_expiry_Days", defaultValue = "0") final int planExpiryDays,
			@RequestParam(value = "no_of_openings",required = false,defaultValue = "0") final int noOfOpenings,
			@RequestParam(value = "no_of_job_category",required = false,defaultValue = "0") final int noOfJobCategory
	) throws SignatureException, RazorpayException {
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

			int amountPaid = amount / 100;

			try {
				Payment payment = razorpay.payments.fetch(paymentId);
				emp.setStatus(payment.get("status"));
				emp.setCaptured(payment.get("captured"));
				emp.setPaidOn(String.valueOf(LocalDateTime.now()));
				emp.setNotes(payment.get("notes").toString());
				emp.setFromWeb(false);
				emp.setTypeOfPurchase(payment.get("method"));
				emp.setMobileNumber(Long.parseLong(payment.get("contact")));
				// emp.setEmailId(payment.get("email"));

			} catch (Exception e) {
				e.printStackTrace();
			}

			emp.setEmailId(optional.get().getEmailId());
			emp.setEmployerId(employerId);
			emp.setPlanId(planId);
			emp.setAmount(amountPaid);
			emp.setPaymentId(paymentId);
			emp.setOrderId(orderId);
			emp.setNumberOfOpenings(noOfOpenings);
			emp.setNumberOfJobCategory(noOfJobCategory);
			emp.setAdminId(1);
			emp.setPaidOn(String.valueOf(LocalDateTime.now()));
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
					existing.setPlanJobCount(existing.getPlanJobCount()+noOfJobCategory);
				} else {
					existing.setPlanJobCount(plan1.getActiveJobs());
				}

				employerRepository.save(existing);

			}

			int paymentID = emp.getId();

			EmployerPaymentModel pay = employerPaymentRepository.findById(paymentID);

			String generated_signature = hmac_sha256(orderId + "|" + paymentId, secretKey);

			if (generated_signature.equals(signature)) {
				// payment is successful

				pay.setSignature("Yes");
				employerPaymentRepository.save(pay);

				// try {
				// String invoiceUrl = sendInvoice(emp.getId(),amountPaid);
				

				CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
				logEventModel.setType("Payment");
				logEventModel.setMessage("Signature Verified");
				logEventModel.setDescription(emp.toString());

				try {
					cloudWatchLogService.cloudLog(logEventModel, "E");
				} catch (Exception e) {
					e.printStackTrace();
				}

				
				if (activeProfile.equalsIgnoreCase("prod")) { 
					  try { 
						  sendInvoice(employerId,amountPaid, paymentID, plan1.getPlanName()); 
						  } 
					  catch (IOException e) 
					  {
				          e.printStackTrace(); 
				  }catch (TemplateException e) { 
					  e.printStackTrace(); 
					  }
					  
					}

				EmployerTimeline employerTimeline = new EmployerTimeline();

				Date currentDate = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
				String formattedDate1 = dateFormat.format(currentDate);

				String eventDescription = "Payment of <b>INR " + amountPaid + "</b> on <b>" + formattedDate1 + "</b> from App";

				employerTimeline.setEmpId(employerId);
				employerTimeline.setEmpLeadId(0);
				employerTimeline.setEventName("Payment");
				employerTimeline.setEventDescription(eventDescription);

				employerTimelineRepository.save(employerTimeline);
				  
				

				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 200);
				map.put("status", "success");
				map.put("message", " Paid Successfully");
				map.put("paymentId",pay.getId());
				return new ResponseEntity<>(map, HttpStatus.OK);

			} else {
				pay.setSignature("No");
				employerPaymentRepository.save(pay);

				Map<String, String> kycEmailData1 = new HashMap<String, String>();
				kycEmailData1.put("name", optional.get().getContactPersonName());
				kycEmailData1.put("orderId", (orderId != null) ? orderId : "#");

				logger.info(kycEmailData1.toString());

				TupleStore tupleStore = new TupleStore();
				tupleStore.setKey(optional.get().getEmailId());
				tupleStore.setValue(new Gson().toJson(kycEmailData1));
				amazonSESMailUtil.sendEmailSES("EmployerPaymentFailedV1", tupleStore);

				CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
				logEventModel.setType("Payment");
				logEventModel.setMessage("Signature is invalid");
				logEventModel.setDescription(emp.toString());
				HashMap<String, String> data = new HashMap<>();
				data.put("plan_id", String.valueOf(planId));
				logEventModel.setLogData(data);

				try {
					cloudWatchLogService.cloudLogFailure(logEventModel, "E");
				} catch (Exception e) {
					e.printStackTrace();
				}

				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 500);
				map.put("message", "Payment is Invalid");
				return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			int amountPaid = amount / 100;

			try {
				Payment payment = razorpay.payments.fetch(paymentId);
				emp.setTypeOfPurchase(payment.get("method"));
				emp.setMobileNumber(Long.parseLong(payment.get("contact")));
				emp.setPaymentId(payment.get("id"));
				emp.setStatus(payment.get("status"));
				emp.setCaptured(payment.get("captured"));
				emp.setNotes(payment.get("notes").toString());
				emp.setFromWeb(false);
				emp.setReason(payment.get("error_reason") + " ," + payment.get("error_description"));
				// emp.setEmailId(payment.get("email"));

			} catch (Exception e) {
				emp.setStatus(status);
				emp.setFromWeb(true);
				emp.setReason(status);
				emp.setCaptured(false);
				emp.setMobileNumber(Long.parseLong(mobNum));

				e.printStackTrace();
			}
			emp.setEmailId(optional.get().getEmailId());
			emp.setEmployerId(employerId);
			emp.setAmount(amountPaid);
			emp.setOrderId(orderId);
			emp.setPlanId(planId);
			emp.setNumberOfOpenings(noOfOpenings);
			emp.setNumberOfJobCategory(noOfJobCategory);
			emp.setAdminId(1);
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
				e.printStackTrace();
			}

			String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + firebaseEmpApiKey;

			DeeplinkRequest dl = new DeeplinkRequest();
			dl.setLongDynamicLink(firebaseEmpHost + "/?link=" + firebaseEmpHost + "/pricing/" + employerId + "/"
					+ planId + "&apn=" + firebaseEmpPackage);

			DeeplinkSuffix c1 = new DeeplinkSuffix();
			c1.setOption("UNGUESSABLE");

			String json = new com.google.gson.Gson().toJson(dl);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> req = new HttpEntity<String>(json.toString(), headers);

			RestTemplate restTemp = new RestTemplate();

			FirebaseShortLink response = null;
			try {
				response = restTemp.postForObject(url, req, FirebaseShortLink.class);
			} catch (Exception e) {
				e.printStackTrace();
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
	private void sendInvoice(int empId, int amount, int paymentID, String pMethod) throws IOException, TemplateException {
		// TODO Auto-generated method stub
		Optional<EmployerModel> d1 = employerRepository.findById(empId);
		EmployerModel d = d1.get();

		EmployerPaymentModel pay = employerPaymentRepository.findById(paymentID);

		DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		String date = formatter.format(pay.getCreatedTime());

		HashMap<String, String> data = new HashMap<>();
		data.put("Amount", String.valueOf(amount));
		data.put("OrderID", pay.getOrderId());
		data.put("PlanName", pMethod);
		data.put("PaymentMethod", pay.getTypeOfPurchase().toUpperCase());
		data.put("PaymentDate", date);

		EmployerPaymentModel p = employerPaymentRepository.findByInvoiceId();
		int invoiceNum = p.getInvoiceNo() + 1;

		double f =Double.parseDouble(new DecimalFormat("##.##").format(amount/1.18));
		double GST =Double.parseDouble(new DecimalFormat("##.##").format((amount-f)/2));
		
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
		converterProperties.setBaseUri("http://localhost:8000");

		HtmlConverter.convertToPdf(message, target, converterProperties);

		byte[] bytes = target.toByteArray();
		
		pay.setInvoiceNo(invoiceNum);
		employerPaymentRepository.save(pay);

		amazonSESMailUtil.sendEmailWithMultipleAttachments(invoiceNumber,pay.getEmailId(),html, bytes);

		File file = null;
		try {
			file = new File("/tmp/" + bytes);
		} catch (Exception e) {
			e.printStackTrace();

		} // file.canWrite(); // file.canRead();
		FileOutputStream iofs = null;
		try {
			iofs = new FileOutputStream(file);
			iofs.write(bytes);
			iofs.close();

			String path = folderName + "/" + "Invoices" + "/";

			DateFormat informat = new SimpleDateFormat("ddMMyyyy_HHmmss");
			String namedate = informat.format(pay.getCreatedTime());
			String fileName = d.getCompanyName()+"_INR"+pay.getAmount()+"_"+namedate+".pdf";
			
			String imagepath = path + fileName;

			uploadFileTos3bucket(imagepath, file);
			file.delete();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void uploadFileTos3bucket(String fileName, File file) {
		s3client.putObject(
				new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
	}


	@Async
	private void paymentInitiated(Optional<EmployerModel> optional, Optional<PlansModel> ep, int amountPaid,
			String orderId) {
		DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
		String date = formatter.format(new Date());
		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
		Calendar cal = Calendar.getInstance();
		String time = simpleDateFormat1.format(cal.getTime());

	}

	public static String hmac_sha256(String data, String secret) throws java.security.SignatureException {
		// TODO Auto-generated method stub
		{
			String result;
			try {

				// get an hmac_sha256 key from the raw secret bytes
				SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA256_ALGORITHM);

				// get an hmac_sha256 Mac instance and initialize with the signing key
				Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
				mac.init(signingKey);

				// compute the hmac on input data bytes
				byte[] rawHmac = mac.doFinal(data.getBytes());

				// base64-encode the hmac
				result = DatatypeConverter.printHexBinary(rawHmac).toLowerCase();

			} catch (Exception e) {
				throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
			}
			return result;
		}
	}

	@GetMapping(value = "/getEmployerPlanStatus") // 2
	public ResponseEntity<?> getEmployerPlanStatus(@RequestParam("employer_id") int id) throws ParseException {

		Optional<EmployerModel> optional = employerRepository.findById(id);
		// employer 4 details will come

		if (!optional.isPresent()) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer is not found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		} else {

			EmployerModel existing = optional.get();

			int planID = existing.getPlan();
			// plan id

			if (planID != 0) {

				String date = existing.getExpiryDate();
				if (date != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					sdf.setLenient(false);
					Date expiry = sdf.parse(date);

					String cDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

					Date curDate = sdf.parse(cDate);

					boolean expired = expiry.before(curDate);

					Optional<PlansModel> plansModel = plansRepository.findById(planID);
					PlansModel plan = plansModel.get();

					if (expired == true) {

						/*
						 * EmployerActivityModel EA = new EmployerActivityModel(); EA.setEmpId(id);
						 * EA.setActivity("Your plan " +plan.getPlanName()+ " expired!");
						 * empActivityRepository.save(EA);
						 */

						HashMap<String, Object> map = new HashMap<>();
						map.put("code", 400);
						map.put("message", "Plan Expired");
						return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);

					} else {

						if (existing.getPlanJobCount() > 0) {
							HashMap<String, Object> map = new HashMap<>();
							map.put("code", 200);
							map.put("status", "success");
							map.put("message", "Plan is Selected");
							return new ResponseEntity<>(map, HttpStatus.OK);
						} else {
							HashMap<String, Object> map = new HashMap<>();
							map.put("code", 400);
							map.put("message", "Active Jobs Limit is Exceeded");
							return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
						}

					}
				} else {
					HashMap<String, Object> map = new HashMap<>();
					map.put("code", 400);
					map.put("message", "Plan is not selected");
					return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
				}

			} else {
				HashMap<String, Object> map = new HashMap<>();
				map.put("code", 400);
				map.put("message", "Plan is not selected");
				return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
			}
		}
	}

	@GetMapping("/walletDetails")
	public ResponseEntity<?> getEmployerWalletDetails(@RequestParam("employer_id") final int employerId) {

		List<EmployerPaymentModel> his = employerPaymentRepository.findEmployerPaymentHistory(employerId);
		EmployerPaymentModel recent = employerPaymentRepository.findEmployerPayment(employerId);
		Optional<EmployerModel> details = employerRepository.findById(employerId);

		int amount = 0, planID = 0;
		int jobPost = 0, activeJobPost = 0, jobPosted, balance, planExpiry = 0, JobPostCount;
		String planName = null, planExpiryDate = null;
		int jobPostValidity = 0;

		EmpCustomPaymentModel payModel = new EmpCustomPaymentModel();

		if (details.isPresent()) {

			EmployerModel existing = details.get();
			int planId = existing.getPlan();
			if (planId != 0) {
				PlansModel plan = plansRepository.findById(planId).get();
				if(recent!=null) {
				amount = recent.getAmount();
				}
				planID = plan.getId();
				planName = plan.getPlanName();
				jobPost = plan.getJobPosting();
				activeJobPost = plan.getActiveJobs();
				planExpiry = plan.getPlanValidity();
				planExpiryDate = existing.getExpiryDate();
				jobPostValidity = plan.getJobPostValidity();

			}

			String status = "O";

			List<JobsModel> jobs = jobRepository.findEmployerJobs(employerId, status);

			jobPosted = jobs.size();

			payModel.setPlanName(planName);
			payModel.setPlanId(planID);
			payModel.setAmount(amount);
			payModel.setJobPost(jobPost);
			payModel.setActiveJobPost(activeJobPost);
			payModel.setPlanExpiry(planExpiry);
			payModel.setJobPosted(jobPosted);
			payModel.setBalance(existing.getPlanJobCount());

			payModel.setJobPostValidity(jobPostValidity);
			if (!his.isEmpty()) {
				Collections.reverse(his);
				payModel.setPaymentList(his);
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 200);
			map.put("status", "success");
			map.put("message", "success");
			map.put("data", payModel);
			return new ResponseEntity<>(map, HttpStatus.OK);

		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 400);
			map.put("message", "Employer Not Found");
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}
	
	@GetMapping("/downloadInvoice")
	public ResponseEntity<byte[]> downloadInvoice(@RequestParam("employer_id") final int empId,
			@RequestParam("payment_id") final int paymentID)
			throws IOException, TemplateException {
		
		Optional<EmployerModel> d1 = employerRepository.findById(empId);
		EmployerModel d = d1.get();
		EmployerPaymentModel pay = employerPaymentRepository.findById(paymentID);

		DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		String date = formatter.format(pay.getCreatedTime());

		double amount = pay.getAmount();
		
		int Grandtotal=pay.getAmount();

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
		converterProperties.setBaseUri("http://localhost:8000");

		HtmlConverter.convertToPdf(message, target, converterProperties);

		byte[] bytes = target.toByteArray();

		pay.setInvoiceNo(invoiceNum);
		employerPaymentRepository.save(pay);

		 return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(bytes);

	}
	
	@PostMapping(path = "/placementPlan")
	public ResponseEntity<?> placementPlanDetails(@RequestParam(name = "jobLeadIds") List<Integer> jobLeadIds,
	                                              @RequestParam("payment_id") int paymentId) {
	    try {
	        List<Integer> createdIds = new ArrayList<>();
	        List<Integer> deletedIds = new ArrayList<>();
	        List<Integer> notFoundIds = new ArrayList<>();

	        boolean allIdsValid = true;

	        for (int jobLeadId : jobLeadIds) {
	            JobLeadModel jobLead = jobLeadRepository.findById(jobLeadId).orElse(null);

	            if (jobLead == null) {
	                notFoundIds.add(jobLeadId);
	                allIdsValid = false;
	                continue;
	            }

	            PlansModel plan;

	            if (jobLead.getExperienced()) {
	                plan = plansRepository.findByActiveAndIsExperienced(true, true);
	            } else {
	                plan = plansRepository.findByActiveAndIsExperienced(true, false);
	            }

	            if (plan == null) {
	                throw new IllegalArgumentException("No suitable plan found");
	            }

	            EmpPlacementPlanDetailsModel empPlacementPlanDetails = new EmpPlacementPlanDetailsModel();

	            empPlacementPlanDetails.setPlanId(plan.getId());
	            empPlacementPlanDetails.setActive(true);
	            empPlacementPlanDetails.setPaymentId(paymentId);
	            empPlacementPlanDetails.setEmployerId(jobLead.getEmployerId());
	            empPlacementPlanDetails.setNoOfOpenings(jobLead.getNoOfOpenings());
	            empPlacementPlanDetails.setJobCategory(jobLead.getJobCategory());
	            empPlacementPlanDetails.setIndustry(jobLead.getJobIndustry());
	            empPlacementPlanDetails.setIsExperienced(jobLead.getExperienced());
				empPlacementPlanDetails.setMaxSalary(jobLead.getMaxSalary());
				empPlacementPlanDetails.setMinSalary(jobLead.getMinSalary());
				empPlacementPlanDetails.setJobMinExp(jobLead.getJobMinExp());
				empPlacementPlanDetails.setWorkHours(jobLead.getWorkHours());
	            empPlacementPlanDetails.setFromSource("Web");

	            EmpPlacementPlanDetailsModel savedEmpPlacementPlan = empPlacementPlanRepository.save(empPlacementPlanDetails);

	            jobLeadRepository.deleteById(jobLeadId);
	            createdIds.add(Math.toIntExact(savedEmpPlacementPlan.getId()));
	        }

	        if (!allIdsValid) {
	            Map<String, Object> response = new HashMap<>();
	            response.put("statusCode", 400);
	            response.put("message", "Some jobLeadIds are invalid");
	            response.put("notFoundIds", notFoundIds);
	            return ResponseEntity.badRequest().body(response);
	        }

	        Map<String, Object> response = new HashMap<>();
	        response.put("statusCode", 200);
	        response.put("message", "Operation completed");
	        response.put("createdIds", createdIds);
	        response.put("deletedIds", deletedIds);

	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        Map<String, Object> response = new HashMap<>();
	        response.put("statusCode", 500);
	        response.put("message", "An error occurred while processing the data");

	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	
	@GetMapping("/placementPlanDetails")
	public ResponseEntity<?> getActivePlacementPlansByEmployerId(@RequestParam int employerId) {
	    List<EmpPlacementPlanDetailsModel> plans = empPlacementPlanRepository.findByEmployerIdAndActive(employerId, true);

	    if (plans.isEmpty()) {
	        Map<String, Object> response = new HashMap<>();
	        response.put("statusCode", 404); 
	        response.put("message", "No active placement plans found for employer ID: " + employerId);
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	    } else {
	        return ResponseEntity.ok(plans);
	    }
	} 
	
	@GetMapping("/placementPlanDetailsById/{id}")
	public HashMap<String, Object> getplacementPlanDetailsById(@PathVariable Long id) {
	    HashMap<String, Object> response = new HashMap<>();

	    Optional<EmpPlacementPlanDetailsModel> optionalPlacementPlanDetails = empPlacementPlanRepository.findById(id);
	    if (optionalPlacementPlanDetails.isPresent()) {
	        EmpPlacementPlanDetailsModel placementPlanDetails = optionalPlacementPlanDetails.get();
	        response.put("statusCode", 200);
	        response.put("data", placementPlanDetails);
	    } else {
	        response.put("statusCode", 404);
	        response.put("message", "EmpPlacementPlanDetails with ID " + id + " not found");
	    }

	    return response;
	}

	@GetMapping("/planAlert")
	public ResponseEntity<Map<String, Object>> checkSalary(
			@RequestParam(required = false) String jobRole,
			@RequestParam(required = false) String experience,
			@RequestParam(required = false) Integer amount,
			@RequestParam(required = false) Integer workHours
	) {
		Map<String, Object> response = new HashMap<>();
		boolean jobAlert = false;
		boolean workHoursAlert = false;

		int yearsExp = 0;

		if ("fresher".equalsIgnoreCase(experience)) {
			yearsExp = 0;
		} else if ("0-1".equals(experience)) {
			yearsExp = 1;
		} else {
			try {
				yearsExp = Integer.parseInt(experience);
			} catch (NumberFormatException e) {
				response.put("code", "400");
				response.put("message", "Invalid input for years of experience");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		}

		CfgFullTimeJobRoleSalaries job = cfgFullTimeJobRolesSalariesRepository.findByJobRolesAndYearsOfExperience(jobRole, String.valueOf(yearsExp));

		if (job == null) {
			response.put("code", "404");
			response.put("message", "Job role and years of experience not found");
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}

		int minSalary = Integer.parseInt(job.getMinSalary());

		String note = "";

		if (yearsExp == 0) {
			note = "Alert: Job Post Restrictions (Fresher's)\n\n" +
					"Fresher candidates looking for <b>" + jobRole + "</b> expect a minimum salary of Rs. <b>" + minSalary + "</b> that meets industry standards. Offering a lower salary reduces the chances of finding suitable candidates. Therefore, we currently do not work for job openings with salary less than industry standards. If you would like to revise your Salary and proceed, click 'Edit and Proceed'\n\n" +
					"Note: Please be aware that Taizo.in is committed to maintaining the quality of our platform and ensuring both employers and job seekers have a positive experience. We reserve the right to deactivate your account, and any payment made for job posting may not be refunded if we find out that the salary details mentioned during the interview process are different.\n\nThank you for your understanding.";
			if (amount != null && amount < minSalary) {
				jobAlert = true;
			}
		} else {
			note = "Alert: Job Post Restrictions (Experienced)\n\n" +
					"Candidates with <b>" + yearsExp + " year(s) experience</b> in <b>" + jobRole + "</b> expect a minimum salary of Rs. <b>" + minSalary + "</b> that meets industry standards. Offering a lower salary reduces the chances of finding suitable candidates. Therefore, we currently do not work for job openings with salary less than industry standards. If you would like to revise your Salary and proceed, click 'Edit and Proceed'\n\n" +
					"Note: Please be aware that Taizo.in is committed to maintaining the quality of our platform and ensuring both employers and job seekers have a positive experience. We reserve the right to deactivate your account, and any payment made for job posting may not be refunded if we find out that the salary details mentioned during the interview process are different.\n\nThank you for your understanding.";
			if (amount != null && amount < minSalary) {
				jobAlert = true;
			}
		}

		if (workHours != null && workHours > 10) {
			workHoursAlert = true;
		}
		if (jobAlert) {
			response.put("code", "400");
			response.put("message", "Alert");
			response.put("alertNote", note);
		} else if (workHoursAlert) {
			response.put("code", "400");
			response.put("message", "Alert");
			response.put("alertNote", "Alert: Job Post Restrictions for Work Hours\n\n" +
					"Candidates prefer roles with work hours between 8 to 10 (excluding OT). Longer work hours reduce the chances of finding suitable candidates. Therefore, we currently do not work for job openings with work hours more than 10 hours (excluding OT). If you would like to revise your work hours and proceed, click 'Edit and Proceed'\n\n" +
					"Note: Please be aware that Taizo.in is committed to maintaining the quality of our platform and ensuring both employers and job seekers have a positive experience. We reserve the right to deactivate your account, and any payment made for job posting may not be refunded if we find out that the work hours details mentioned during the interview process are different.\n\nThank you for your understanding.");
		} else {
			response.put("code", "200");
			response.put("message", "true");
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}


}
