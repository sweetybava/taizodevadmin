package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "interviewAddresses")
@ToString
 @Getter
 @Setter
 
public class InterviewAddressesModel {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;
 
 @Column(name="emp_id")
 private int empId;
 
 @Column(name="latitude")
 private String latitude;
 
 @Column(name="longitude")
 private String longitude;
 
 @Column(name="address")
 private String address;
 
 @Column(name="landmark")
 private String landmark;
 
 
 @Column(name="active")
 private boolean active;
 

public int getId() {
	return id;
}

public void setId(int id) {
	this.id = id;
}

public int getEmpId() {
	return empId;
}

public void setEmpId(int empId) {
	this.empId = empId;
}

public String getLatitude() {
	return latitude;
}

public void setLatitude(String latitude) {
	this.latitude = latitude;
}

public String getLongitude() {
	return longitude;
}

public void setLongitude(String longitude) {
	this.longitude = longitude;
}

public String getAddress() {
	return address;
}

public void setAddress(String address) {
	this.address = address;
}

public String getLandmark() {
	return landmark;
}

public void setLandmark(String landmark) {
	this.landmark = landmark;
}

public boolean getActive() {
	return active;
}

public void setActive(boolean active) {
	this.active = active;
}
 

}
 