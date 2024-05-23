package com.taizo.model;

import lombok.ToString;

@ToString
public class NotificationViewModel {

    private String message;
    private String companyName;
    //private String message;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
    
    

}
