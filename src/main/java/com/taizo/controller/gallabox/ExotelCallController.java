package com.taizo.controller.gallabox;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.taizo.model.Admin;
import com.taizo.model.AdminCallNotiModel;
import com.taizo.repository.AdminCallNotiRepository;
import com.taizo.repository.AdminRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.service.AdminService;
import com.taizo.service.AmazonSESMailUtil;
import com.taizo.service.NotificationService;

import okhttp3.Credentials;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@CrossOrigin
@RestController
@RequestMapping("/exotel")
public class ExotelCallController {
	
		public String customerNumber = "+916383465436";
		public String url = "http://my.exotel.com/";
		public String exotelsid = "taizo1";
		public String flow_id = "646723";
		public String apiid = "b3f23bf1a7b73c73e9fefde5e4501a609e1b8c7bdf692406";
		public String apitoken = "a25ea4fc5d397cdf54989d67248f3da52ba70c3211aa44e0";
		public static String agentNumber    = "+919600014728";
		
		@Autowired
		AmazonSESMailUtil amazonSESMailUtil;
		
		@Autowired
		AdminCallNotiRepository adminCallNotiRepository;
		
		@Autowired
		AdminRepository adminRepository;
		
		@Autowired
		NotificationService notificationService;
		
		@Autowired
		AdminService adminService;
		
		@Autowired
		EmployerRepository employerRepository;
		
		
		@GetMapping(value = "/agentCall")
		public ExotelResponse connectToAgent(String to,String module,HashMap<String, String> data) {
			String fromNum = "+917418662176";
	
		  if(module.equalsIgnoreCase("Emp")) { 
			  fromNum = "+917806805801"; }
			 
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
					.addFormDataPart("From", fromNum)
					.addFormDataPart("To"  , to )
					.addFormDataPart("CallerId","04448135483")
					.addFormDataPart("Record","true")
					.build();

			String credentials = Credentials.basic(apiid, apitoken);

			Request request = new Request.Builder()
					.url(String.format(ExotelStrings.CONNECT_TO_AGENT_URL , exotelsid)).method("POST", body)
					.addHeader("Authorization", credentials).addHeader("Content-Type", "application/json").build();
			
			try {
				Response response = client.newCall(request).execute();
				Gson connect = new Gson();
				String res = null;
				try {
					res = response.body().string();
				} catch (IOException e) {
					e.printStackTrace();
				}

				ExotelResponse SuccessResponse = connect.fromJson(res, ExotelResponse.class);

				int status = response.code();

				if (status == 200) {
					ExotelSuccessResponse cust = connect.fromJson(res, ExotelSuccessResponse.class);
					String Sid = cust.Call.Sid;

					//ExotelSuccessResponse cust = new ExotelSuccessResponse(0);
				
					sendPush(data,Sid);
					return cust;
				} else {
					ExotelFailureResponse cust = connect.fromJson(res, ExotelFailureResponse.class);
					//ExotelFailureResponse cust = new ExotelFailureResponse(0);
					return cust;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			ExotelFailureResponse cust = new ExotelFailureResponse(0);
			return cust;
		}


		private void sendPush(HashMap<String, String> data, String sid) {
			
			AdminCallNotiModel a = new AdminCallNotiModel();
			a.setEventName(data.get("Event Name"));
			a.setType(data.get("Type"));
			a.setEventType(data.get("Event Type"));
			a.setSource(data.get("Source"));
			a.setContactPersonName(data.get("Contact Person Name"));
			a.setCompanyName(data.get("Company Name"));
			a.setMobileNumber(data.get("Mobile Number"));
			a.setIdType(data.get("ID Type"));
			a.setLocation(data.get("Location"));
			a.setPosition(data.get("Position"));
			a.setJobRole(data.get("Job Role"));
			a.setExp(data.get("Experience"));
			a.setJobStatus(data.get("Job Status"));
			a.setCandidateName(data.get("Candidate Name"));
			a.setInterviewDate(data.get("Interview Date"));
			a.setReferenceId(Integer.parseInt(data.get("ID")));
			a.setSid(sid);
			
			adminCallNotiRepository.save(a);
			data.put("Notification ID", String.valueOf(a.getId()));

		}

		@GetMapping(value = "/call")
		public ExotelResponse connectCustomerToFlow() {
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
					.addFormDataPart("From", customerNumber)
					.addFormDataPart("Url", url + exotelsid + "/exoml/start_voice/" + flow_id).build();

			String credentials = Credentials.basic(apiid, apitoken);

			Request request = new Request.Builder()
					.url(String.format(ExotelStrings.CONNECT_CUSTOMER_TO_FLOW_URL, exotelsid)).method("POST", body)
					.addHeader("Authorization", credentials).addHeader("Content-Type", "application/json").build();

			try {
				Response response = client.newCall(request).execute();
				Gson connect = new Gson();
				String res = null;
				try {
					res = response.body().string();
				} catch (IOException e) {
					e.printStackTrace();
				}

				ExotelResponse SuccessResponse = new ExotelResponse();

			    SuccessResponse = connect.fromJson(res, ExotelResponse.class);

				int status = response.code();

				if (status == 200) {
					ExotelSuccessResponse cust = new ExotelSuccessResponse(0);
					return cust;
				} else {
					ExotelFailureResponse cust = new ExotelFailureResponse(0);
					return cust;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			ExotelFailureResponse cust = new ExotelFailureResponse(0);
			return cust;
		}
	
		 public void connectToAgentWithAdminMobileNumber(String to, HashMap<String, String> data1, String adminMobileNumber) {
		        String fromNum = "+917418662176"; // Default value

		        if (!adminMobileNumber.isEmpty()) {
		            fromNum = "+91" + adminMobileNumber;
		        }

		        OkHttpClient client = new OkHttpClient().newBuilder().build();
		        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
		                .addFormDataPart("From", fromNum)
		                .addFormDataPart("To", to)
		                .addFormDataPart("CallerId", "04448135483")
		                .addFormDataPart("Record", "true")
		                .build();

		        String credentials = Credentials.basic(apiid, apitoken);

		        Request request = new Request.Builder()
		                .url(String.format(ExotelStrings.CONNECT_TO_AGENT_URL, exotelsid))
		                .method("POST", body)
		                .addHeader("Authorization", credentials)
		                .addHeader("Content-Type", "application/json")
		                .build();

		        try {
		            Response response = client.newCall(request).execute();
		            Gson connect = new Gson();
		            String res = null;
		            try {
		                res = response.body().string();
		            } catch (IOException e) {
		                e.printStackTrace();
		            }

		            ExotelResponse successResponse = connect.fromJson(res, ExotelResponse.class);

		            int status = response.code();

		            if (status == 200) {
		                ExotelSuccessResponse cust = connect.fromJson(res, ExotelSuccessResponse.class);
		                String Sid = cust.Call.Sid;
		                sendPush(data1, Sid);
		                // Handle successful response
		            } else {
		                ExotelFailureResponse cust = connect.fromJson(res, ExotelFailureResponse.class);
		                // Handle failure response
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		            // Handle exception
		        }
		 }


		public void connectToAgentcall(String string, HashMap<String, String> empEnquirys) {
			// TODO Auto-generated method stub
			String fromNum = "+919600014728";
			 
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
					.addFormDataPart("From", fromNum)
					.addFormDataPart("To"  , string )
					.addFormDataPart("CallerId","04448135483")
					.addFormDataPart("Record","true")
					.build();

			String credentials = Credentials.basic(apiid, apitoken);

			Request request = new Request.Builder()
					.url(String.format(ExotelStrings.CONNECT_TO_AGENT_URL , exotelsid)).method("POST", body)
					.addHeader("Authorization", credentials).addHeader("Content-Type", "application/json").build();
			
			try {
				Response response = client.newCall(request).execute();
				Gson connect = new Gson();
				String res = null;
				try {
					res = response.body().string();
				} catch (IOException e) {
					e.printStackTrace();
				}

				ExotelResponse SuccessResponse = connect.fromJson(res, ExotelResponse.class);

				int status = response.code();

				if (status == 200) {
					ExotelSuccessResponse cust = connect.fromJson(res, ExotelSuccessResponse.class);
					String Sid = cust.Call.Sid;

					//ExotelSuccessResponse cust = new ExotelSuccessResponse(0);
				
					sendPush(empEnquirys,Sid);
					return;
				} else {
					ExotelFailureResponse cust = connect.fromJson(res, ExotelFailureResponse.class);
					//ExotelFailureResponse cust = new ExotelFailureResponse(0);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			ExotelFailureResponse cust = new ExotelFailureResponse(0);
			return;
		}
		

		@GetMapping("/fetch-data")
		public ResponseEntity<?> fetchData(
		    @RequestParam(name = "sid", required = false) List<String> sid,
		    @RequestParam(defaultValue = "0") int page,
		    @RequestParam(defaultValue = "10") int pageSize) {

		    Page<AdminCallNotiModel> data;
		    HashMap<String, Object> map = new HashMap<>();

		    if (sid != null && !sid.isEmpty()) {
		        // When sid is provided, retrieve data filtered by sid
		        data = adminService.fetchDataBySidsPaginated(sid, page, pageSize);
		        map.put("status", "success");
			    map.put("message", "success");
			    map.put("code", 200);
			    map.put("data", data);
			    return new ResponseEntity<>(map, HttpStatus.OK);
		    } else {
		        // When sid is not provided or is empty, retrieve all data with pagination
		    	String data1 = adminService.fetchDataFromExotelApi( page, pageSize);
		    
		    map.put("status", "success");
		    map.put("message", "success");
		    map.put("code", 200);
		    map.put("data", data1);
		    return new ResponseEntity<>(map, HttpStatus.OK);
		    }
		    
		}


		public void connectToAgentcalls(String string, HashMap<String, String> empEnquirys) {
		    // Fetch the admin module's employer mobile numbers
		    List<Admin> adminList = adminRepository.findByModule("Emp");

		    if (adminList != null && !adminList.isEmpty()) {
		        OkHttpClient client = new OkHttpClient().newBuilder().build();

		        for (Admin admin : adminList) {
		            String fromNum = admin.getMobileNo();

		            // Rest of your code remains mostly unchanged
		            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
		                    .addFormDataPart("From", fromNum)
		                    .addFormDataPart("To", string)
		                    .addFormDataPart("CallerId", "04448135483")
		                    .addFormDataPart("Record", "true")
		                    .build();

		            String credentials = Credentials.basic(apiid, apitoken);

		            Request request = new Request.Builder()
		                    .url(String.format(ExotelStrings.CONNECT_TO_AGENT_URL, exotelsid))
		                    .method("POST", body)
		                    .addHeader("Authorization", credentials)
		                    .addHeader("Content-Type", "application/json")
		                    .build();

		            try {
		                Response response = client.newCall(request).execute();
		                Gson connect = new Gson();
		                String res = response.body().string();
		                int status = response.code();

		                if (status == 200) {
		                    ExotelSuccessResponse cust = connect.fromJson(res, ExotelSuccessResponse.class);
		                    String Sid = cust.Call.Sid;
		                    sendPush(empEnquirys, Sid);
		                    return; 
		                } 
		            } catch (Exception e) {
		                e.printStackTrace();
		            }

		            try {
		                Thread.sleep(20000); // 20 seconds
		            } catch (InterruptedException e) {
		                e.printStackTrace();
		            }
		        }
		    }

		    ExotelFailureResponse cust = new ExotelFailureResponse(0);
		}


		public String connectToEmployercalls(String string, String mobileNumber) {

		    if (string != null && mobileNumber != null) {
		        OkHttpClient client = new OkHttpClient().newBuilder().build();

		        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
		                .addFormDataPart("From", mobileNumber)
		                .addFormDataPart("To", string)
		                .addFormDataPart("CallerId", "04448135483")
		                .addFormDataPart("Record", "true")
		                .build();

		        String credentials = Credentials.basic(apiid, apitoken);

		        Request request = new Request.Builder()
		                .url(String.format(ExotelStrings.CONNECT_TO_AGENT_URL, exotelsid))
		                .method("POST", body)
		                .addHeader("Authorization", credentials)
		                .addHeader("Content-Type", "application/json")
		                .build();

		        // Uncomment the code to make the HTTP request
		        try {
		            Response response = client.newCall(request).execute();
		            // Handle the response as needed
		            if (response.isSuccessful()) {
		                // Handle success
		                return "Call initiated successfully";
		            } else {
		                // Handle failure
		                return "Call initiation failed";
		            }
		        } catch (IOException e) {
		            e.printStackTrace();
		            return "An error occurred while connecting to Exotel";
		        }
		    } else {
		        return "Mobile number not found";
		    }
		}


		public String connectToCandidateCalls(String string, String adminMobileNumber) {
			 if (string != null && adminMobileNumber != null) {
			        OkHttpClient client = new OkHttpClient().newBuilder().build();

			        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
			                .addFormDataPart("From", adminMobileNumber)
			                .addFormDataPart("To", string)
			                .addFormDataPart("CallerId", "04448135483")
			                .addFormDataPart("Record", "true")
			                .build();

			        String credentials = Credentials.basic(apiid, apitoken);

			        Request request = new Request.Builder()
			                .url(String.format(ExotelStrings.CONNECT_TO_AGENT_URL, exotelsid))
			                .method("POST", body)
			                .addHeader("Authorization", credentials)
			                .addHeader("Content-Type", "application/json")
			                .build();

			        // Uncomment the code to make the HTTP request
			        try {
			            Response response = client.newCall(request).execute();
			            // Handle the response as needed
			            if (response.isSuccessful()) {
			                // Handle success
			                return "Call initiated successfully";
			            } else {
			                // Handle failure
			                return "Call initiation failed";
			            }
			        } catch (IOException e) {
			            e.printStackTrace();
			            return "An error occurred while connecting to Exotel";
			        }
			    } else {
			        return "Mobile number not found";
			    }
		}


		public ExotelResponse connectMetaLeads(String to, Long adminId) {
			
			
				 Admin admin = adminRepository.findById(adminId).orElse(null);
			     String fromNum = "+91" + admin.getMobileNo();


			OkHttpClient client = new OkHttpClient().newBuilder().build();
				RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
						.addFormDataPart("From", fromNum)
						.addFormDataPart("To"  , to )
						.addFormDataPart("CallerId","04448135483")
						.addFormDataPart("Record","true")
						.build();

				String credentials = Credentials.basic(apiid, apitoken);

				Request request = new Request.Builder()
						.url(String.format(ExotelStrings.CONNECT_TO_AGENT_URL , exotelsid)).method("POST", body)
						.addHeader("Authorization", credentials).addHeader("Content-Type", "application/json").build();
				
				try {
			        Response response = client.newCall(request).execute();
			        String res = response.body().string();

			        if (response.isSuccessful()) {
			            Gson gson = new Gson();
			            return gson.fromJson(res, ExotelResponse.class);
			        } else {
			            // Handle failure response here
			            return new ExotelFailureResponse(response.code());
			        }
			    } catch (Exception e) {
			        e.printStackTrace();
			        // Handle exceptions here
			        return new ExotelFailureResponse(0);
			    }
			}
}
	
