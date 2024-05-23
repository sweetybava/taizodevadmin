package com.taizo.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.User;
import org.springframework.web.multipart.MultipartFile;

import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.UserModel;

public interface UserService {

	UserModel findByMobileNo(long l);

	Optional<UserModel> findLogout(String token);

	Optional<User> findByToken(String token);

	void updateProfilePic(int id, String profilePic);

	Optional<UserModel> login(long mobileNumber, String password, String country);

	public void deleteById(int userID) throws ResourceNotFoundException;

	boolean deleteImage(String pic);

	String uploadFile(MultipartFile profilePic, int id, byte[] bytes);

	void uploadFileToS3Bucket(MultipartFile multipartFile, boolean enablePublicReadAccess);

	void deleteFileFromS3Bucket(String fileName);

	String uploadProfilePicToS3Bucket(MultipartFile pic, int id, boolean enablePublicReadAccess);
	String uploadProfilePicToS3Bucket1(MultipartFile pic, long mobileNumber, boolean enablePublicReadAccess);

	public void deleteByMobileNumber(long mobileNumber) throws ResourceNotFoundException;

	String uploadProfileResToS3Bucket1(MultipartFile file, long mobileNumber, boolean b);
}
