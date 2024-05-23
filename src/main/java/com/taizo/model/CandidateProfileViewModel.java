package com.taizo.model;

import java.util.Date;
import java.util.List;

import lombok.ToString;

@ToString
public class CandidateProfileViewModel {
	
    private int id;
    private int userId;	  
    private String profilePic;	 
    private List<LanguagesModel> languages;
    private String firstName;   
    private String lastName;   
    private String jobType;  
    private String student;
    private String age;
    private String prefCountry;  
    private String prefLocation;   
    private String candidateLocation;  
    private String domesticLocation;  
    private String overseasLocation;    
    private String jobCategory;   
    private String dateOfBirth;   
    private String gender;   
    private String currentCountry;   
    private String currentState;  
    private String currentCity;   
    private String perCountry;   
    private String perState;    
    private String perCity;    
    private long mobileNumber;   
    private long whatsappNumber;   
    private String emailId;   
    private String qualification;   
    private String certificationCourses;    
    private String certificationSpecialization;


    private String specification;   
    private String candidateType;  
    private Integer experience;
    private Integer experienceMonths;

    private Integer overseasExp;
    private Integer overseasExpMonths;

    private List<String> expCertificate;   
    private List<String> expCertificateType;    

    private List<String> license;   
    private List<String> licenseType;    

    private String paymentStatus;
    private String skills; 
    private String skillVideoType;
    private String approvalStatus;
    private Date appliedTime;



	public Date getCallTime() {
		return callTime;
	}

	public void setCallTime(Date callTime) {
		this.callTime = callTime;
	}

	private Date callTime;
    
    
	public CandidateProfileViewModel(List<CandidateProfileViewModel> sListt) {
		// TODO Auto-generated constructor stub
	}
	public CandidateProfileViewModel() {
		// TODO Auto-generated constructor stub
	}
	public int getId() {
		return id;
	}
	public int getUserId() {
		return userId;
	}
	public String getProfilePic() {
		return profilePic;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public String getJobType() {
		return jobType;
	}
	public String getStudent() {
		return student;
	}
	public String getPrefCountry() {
		return prefCountry;
	}
	public String getPrefLocation() {
		return prefLocation;
	}
	public String getCandidateLocation() {
		return candidateLocation;
	}
	public String getDomesticLocation() {
		return domesticLocation;
	}
	public String getOverseasLocation() {
		return overseasLocation;
	}
	public String getJobCategory() {
		return jobCategory;
	}
	public String getDateOfBirth() {
		return dateOfBirth;
	}
	public String getGender() {
		return gender;
	}
	public String getCurrentCountry() {
		return currentCountry;
	}
	public String getCurrentState() {
		return currentState;
	}
	public String getCurrentCity() {
		return currentCity;
	}
	public String getPerCountry() {
		return perCountry;
	}
	public String getPerState() {
		return perState;
	}
	public String getPerCity() {
		return perCity;
	}
	public long getMobileNumber() {
		return mobileNumber;
	}
	public long getWhatsappNumber() {
		return whatsappNumber;
	}
	public String getEmailId() {
		return emailId;
	}
	public String getQualification() {
		return qualification;
	}
	public String getSpecification() {
		return specification;
	}
	public String getCandidateType() {
		return candidateType;
	}
	public Integer getExperience() {
		return experience;
	}
	public Integer getOverseasExp() {
		return overseasExp;
	}
	public List<String> getExpCertificate() {
		return expCertificate;
	}
	public List<String> getLicense() {
		return license;
	}
	public String getPaymentStatus() {
		return paymentStatus;
	}
	public String getSkills() {
		return skills;
	}
	public String getApprovalStatus() {
		return approvalStatus;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	public void setStudent(String student) {
		this.student = student;
	}
	public void setPrefCountry(String prefCountry) {
		this.prefCountry = prefCountry;
	}
	public void setPrefLocation(String prefLocation) {
		this.prefLocation = prefLocation;
	}
	public void setCandidateLocation(String candidateLocation) {
		this.candidateLocation = candidateLocation;
	}
	public void setDomesticLocation(String domesticLocation) {
		this.domesticLocation = domesticLocation;
	}
	public void setOverseasLocation(String overseasLocation) {
		this.overseasLocation = overseasLocation;
	}
	public void setJobCategory(String jobCategory) {
		this.jobCategory = jobCategory;
	}
	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public void setCurrentCountry(String currentCountry) {
		this.currentCountry = currentCountry;
	}
	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}
	public void setCurrentCity(String currentCity) {
		this.currentCity = currentCity;
	}
	public void setPerCountry(String perCountry) {
		this.perCountry = perCountry;
	}
	public void setPerState(String perState) {
		this.perState = perState;
	}
	public void setPerCity(String perCity) {
		this.perCity = perCity;
	}
	public void setMobileNumber(long mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public void setWhatsappNumber(long whatsappNumber) {
		this.whatsappNumber = whatsappNumber;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public void setQualification(String qualification) {
		this.qualification = qualification;
	}
	public void setSpecification(String specification) {
		this.specification = specification;
	}
	public void setCandidateType(String candidateType) {
		this.candidateType = candidateType;
	}
	public void setExperience(Integer experience) {
		this.experience = experience;
	}
	public void setOverseasExp(Integer overseasExp) {
		this.overseasExp = overseasExp;
	}
	public void setExpCertificate(List<String> items) {
		this.expCertificate = items;
	}
	public void setLicense(List<String> licen) {
		this.license = licen;
	}
	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	public void setSkills(String skills) {
		this.skills = skills;
	}
	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public String getAge() {
		return age;
	}
	public String getSkillVideoType() {
		return skillVideoType;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public void setSkillVideoType(String skillVideoType) {
		this.skillVideoType = skillVideoType;
	}
	public List<LanguagesModel> getLanguages() {
		return languages;
	}
	public void setLanguages(List<LanguagesModel> persons) {
		this.languages = persons;
	}
	public String getCertificationCourses() {
		return certificationCourses;
	}
	public void setCertificationCourses(String certificationCourses) {
		this.certificationCourses = certificationCourses;
	}
	public String getCertificationSpecialization() {
		return certificationSpecialization;
	}
	public void setCertificationSpecialization(String certificationSpecialization) {
		this.certificationSpecialization = certificationSpecialization;
	}
	public Integer getExperienceMonths() {
		return experienceMonths;
	}
	public void setExperienceMonths(Integer experienceMonths) {
		this.experienceMonths = experienceMonths;
	}
	public Integer getOverseasExpMonths() {
		return overseasExpMonths;
	}
	public void setOverseasExpMonths(Integer overseasExpMonths) {
		this.overseasExpMonths = overseasExpMonths;
	}
	public List<String> getExpCertificateType() {
		return expCertificateType;
	}
	public void setExpCertificateType(List<String> expcerType) {
		this.expCertificateType = expcerType;
	}
	public List<String> getLicenseType() {
		return licenseType;
	}
	public void setLicenseType(List<String> licenType) {
		this.licenseType = licenType;
	}

	public Date getAppliedTime() {
		return appliedTime;
	}

	public void setAppliedTime(Date appliedTime) {
		this.appliedTime = appliedTime;
	}


    
    


    

}
