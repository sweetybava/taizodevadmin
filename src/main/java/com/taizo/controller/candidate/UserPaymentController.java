package com.taizo.controller.candidate;

import java.net.URISyntaxException; 
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.taizo.model.CandidateModel;
import com.taizo.model.CloudwatchLogEventModel;
import com.taizo.model.PaymentHistoryModel;
import com.taizo.model.UserPaymentModel;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.EmpActivityRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.UserPaymentRepository;
import com.taizo.service.CloudWatchLogService;

@CrossOrigin
@RestController
public class UserPaymentController {

	@Autowired
	UserPaymentRepository userPaymentRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@Value("${razor.key.id}")
	private String KeyId;

	@Value("${razor.secret.key}")
	private String secretKey;

	@Autowired
	EmployerRepository employerRepository;

	@Autowired
	CloudWatchLogService cloudWatchLogService;

	private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

	private static final Logger logger = LoggerFactory.getLogger(UserPaymentController.class);

	@GetMapping("/paypalorderId")
	public String getEncodedResponse() {
		HashMap<String, Object> data = new HashMap();
		data.put("merchantTransactionId", "MT7850590068188104"); // String. Mandatory
		data.put("merchantId", "PGTESTPAYUAT"); // String. Mandatory
//merchantUserId - Mandatory if paymentInstrument.type is: PAY_PAGE, CARD, SAVED_CARD, TOKEN.
//merchantUserId - Optional if paymentInstrument.type is: UPI_INTENT, UPI_COLLECT, UPI_QR.
		data.put("amount", 200); // Long. Mandatory
		data.put("mobileNumber", "9952346948"); // String. Optional
		data.put("callbackUrl", "https://webhook.site/callback-url"); // String. Mandatory

		final byte[] authBytes = (data.toString()).getBytes(StandardCharsets.UTF_8);

		String s = new String(Base64.getEncoder().encode(authBytes));

		System.out.println(s);
		return s;
	}

	@GetMapping("/orderId")
	public ResponseEntity<?> getOrderId(@RequestParam("amount") final int amount) throws RazorpayException {

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
				cloudWatchLogService.cloudLog(logEventModel, "C");
			} catch (Exception e) {

			}

		} catch (RazorpayException e) {
			errorMsg = e.getMessage();
			logger.error("error [" + e.getMessage() + "] occurred while generating user payment order id");

			CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
			logEventModel.setType("Payment");
			logEventModel.setMessage("failure");
			logEventModel
					.setDescription("error [" + e.getMessage() + "] occurred while generating user payment order id");

			try {
				cloudWatchLogService.cloudLogFailure(logEventModel, "C");
			} catch (Exception e1) {

			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", errorMsg);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

		if (!id.isEmpty() && id != null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("OrderId", id);
			map.put("KeyId", KeyId);
			map.put("SecretKey", secretKey);

			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", errorMsg);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/userPayment", method = RequestMethod.POST)
	public ResponseEntity<?> createUserPayment(@RequestParam("user_id") final int userId,
			@RequestParam("type_of_purchase") final String typeOfPurchase, @RequestParam("amount") final int amount,
			@RequestParam("payment_id") final String paymentId, @RequestParam("order_id") final String orderId,
			@RequestParam(value = "email_id", required = false) String emailId, @RequestParam("signature") final String signature,
			@RequestParam("mobile_number") final long mobileNumber, @RequestParam("status") final String status,
			@RequestParam(value = "device_token", required = false) String token) {

		UserPaymentModel user = new UserPaymentModel();
		Optional<CandidateModel> optional = candidateRepository.findByUserId(userId);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, 1); // Adding 1 Year
		String output = sdf.format(c.getTime());

		DateFormat formatter = new SimpleDateFormat("EEEE dd, MMM yyyy");
		String date = formatter.format(new Date());
		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aa");
		Calendar cal = Calendar.getInstance();
		String time = simpleDateFormat1.format(cal.getTime());


		if (status.equalsIgnoreCase("Payment Successfull")) {

			user.setExpiryDate(output);
			user.setUserId(userId);
			user.setTypeOfPurchase(typeOfPurchase);
			user.setAmount(amount);
			user.setEmailId(emailId);
			user.setMobileNumber(mobileNumber);
			user.setPaymentId(paymentId);
			user.setOrderId(orderId);
			user.setStatus(status);

			userPaymentRepository.save(user);

			CloudwatchLogEventModel logEventModel = new CloudwatchLogEventModel();
			logEventModel.setType("Payment");
			logEventModel.setMessage("success");
			logEventModel.setDescription(user.toString());

			try {
				cloudWatchLogService.cloudLog(logEventModel, "C");
			} catch (Exception e) {

			}
			int paymentID = user.getId();

			UserPaymentModel pay = userPaymentRepository.findById(paymentID);

			String generated_signature = null;
			try {
				generated_signature = hmac_sha256(orderId + "|" + paymentId, secretKey);
			} catch (SignatureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("Failed to generate HMAC payment signature : [" + e.getMessage() + "]");

				CloudwatchLogEventModel logEventModel1 = new CloudwatchLogEventModel();
				logEventModel1.setType("Payment");
				logEventModel1.setMessage("failure");
				logEventModel1.setDescription(
						"Failed to generate HMAC payment signature : [" + e.getMessage() + "for" + user + "]");

				try {
					cloudWatchLogService.cloudLogFailure(logEventModel1, "C");
				} catch (Exception e1) {

				}

			}

			if (generated_signature.equals(signature)) {
				// payment is successful
				pay.setSignature("Yes");
				userPaymentRepository.save(pay);

				String name = null;

				if (optional.isPresent()) {

					CandidateModel existing = optional.get();
					name = existing.getFirstName();

					existing.setPaymentStatus("Paid");

					int amnt = existing.getAmount();
					int totalamnt = amnt + amount;

					existing.setAmount(totalamnt);
					existing.setJobLimit(existing.getJobLimit() + 1);
					candidateRepository.save(existing);

				}

				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 200);
				map.put("message", " Paid Successfully");
				map.put("result", user);
				return new ResponseEntity<>(map, HttpStatus.OK);
			} else {
				pay.setSignature("No");
				userPaymentRepository.save(pay);


				HashMap<String, Object> map = new HashMap<>();
				map.put("statuscode", 500);
				map.put("message", "Payment is Invalid");
				return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);

			}
		} else {
			/*
			 * RazorpayClient razorpay = new RazorpayClient(KeyId, secretKey);
			 * 
			 * try { Order order = razorpay.Orders.fetch(orderId);
			 * System.out.println(order.toString()); } catch (RazorpayException e1) { //
			 * TODO Auto-generated catch block e1.printStackTrace(); }
			 */

			user.setExpiryDate(output);
			user.setUserId(userId);
			user.setTypeOfPurchase(typeOfPurchase);
			user.setAmount(amount);
			user.setEmailId(emailId);
			user.setMobileNumber(mobileNumber);
			user.setOrderId(orderId);
			user.setStatus(status);

			userPaymentRepository.save(user);


			HashMap<String, Object> map = new HashMap<>();
			map.put("code", 500);
			map.put("message", "Payment Failed");
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}

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

	@GetMapping("/paymentHistory")
	public ResponseEntity<?> getUserPaymentHistory(@RequestParam("user_id") final int userId) {

		List<UserPaymentModel> his = userPaymentRepository.findPaymentHistory(userId);

		int amount = 0;
		int applyLimit = 0;
		Date date = null;
		String status = null;
		List<PaymentHistoryModel> history = new ArrayList<PaymentHistoryModel>();

		if (!his.isEmpty()) {

			for (UserPaymentModel user : his) {
				PaymentHistoryModel tm = new PaymentHistoryModel();

				amount = user.getAmount();
				applyLimit = user.getAmount() / 5;
				date = user.getCreatedTime();
				status = user.getStatus();

				tm.setAmount(amount);
				tm.setApplyLimit(applyLimit);
				tm.setDate(date);
				tm.setStatus(status);

				history.add(tm);
			}

			Collections.reverse(history);
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 200);
			map.put("message", "success");
			map.put("results", history);
			return new ResponseEntity<>(map, HttpStatus.OK);
		} else {
			HashMap<String, Object> map = new HashMap<>();
			map.put("statuscode", 400);
			map.put("message", "No History Available");
			map.put("results", null);
			return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
		}

	}

}
