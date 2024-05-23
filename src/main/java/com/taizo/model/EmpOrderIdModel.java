package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@ToString
public class EmpOrderIdModel {
	
    private String OrderId;
    private String KeyId;
	public String getOrderId() {
		return OrderId;
	}
	public String getKeyId() {
		return KeyId;
	}
	public void setOrderId(String orderId) {
		OrderId = orderId;
	}
	public void setKeyId(String keyId) {
		KeyId = keyId;
	}

	
	}
