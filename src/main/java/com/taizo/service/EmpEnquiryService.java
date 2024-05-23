package com.taizo.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.taizo.model.EmpEnquiryModel;


public interface EmpEnquiryService {

	EmpEnquiryModel createEmpEnquiry(EmpEnquiryModel empEnquiryModel);

	List<Map<String, Object>> filterEmpEnquiry(String mobileNumber,String emailId,String companyName, int page, int size, Date createdTime, Date endDate);

	long filterempEnquiryCount(String mobileNumber,String emailId,String companyName, int page, int size, Date createdTime, Date endDate);

}
