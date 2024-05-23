package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import lombok.ToString;

@Entity
@Table(name = "user")
@ToString
public class UserModel
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private int id;
    
    @Column(name="mobile_number")
    private long mobileNumber;
    
    @Column(name="country_code")
    private String countryCode;
    
    @Column(name="password")
    private String password;
    
    @Column(name="first_name")
    private String firstName;
    
    @Column(name="last_name")
    private String lastName;
    
    @Column(name="profile_pic")
    private String profilePic;
    
    @Column(name="token")
    private String token;
    
    @Column(name="user_status")
    private String approvalStatus;
    
    @Column(name="deleted")
    private boolean deleted;
    
    

	public UserModel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UserModel(int id, long mobileNumber, @NotBlank(message = "password is mandatory") String password,
			@NotBlank(message = "firstName is mandatory") String firstName,
			@NotBlank(message = "lastName is mandatory") String lastName, String profilePic, String token,String approvalStatus) {
		super();
		this.id = id;
		this.mobileNumber = mobileNumber;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.profilePic = profilePic;
		this.token = token;
		this.approvalStatus = approvalStatus;

	}

	public int getId() {
		return id;
	}

	public long getMobileNumber() {
		return mobileNumber;
	}

	public String getPassword() {
		return password;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getProfilePic() {
		return profilePic;
	}

	public String getToken() {
		return token;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setMobileNumber(long mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

    
    
}
