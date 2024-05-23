package com.taizo.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.taizo.exception.ResourceNotFoundException;
import com.taizo.model.CanLeadModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.UserModel;
import com.taizo.repository.CanLeadRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.UserRepository;

@Service("userService")
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	CanLeadRepository canLeadRepository;
	
	@Autowired
	CandidateRepository candidateRepository;

	private AmazonS3 s3client;

	@Value("${aws.user.endpointUrl}")
	private String endpointUrl;
	@Value("${aws.user.lead.endpointUrl}")
	private String leadEndpointUrl;
	@Value("${aws.s3.audio.bucket}")
	private String bucketName;
	@Value("${aws.access.key.id}")
	private String accessKey;
	@Value("${aws.access.key.secret}")
	private String secretKey;

	@Value("${aws.s3.bucket.user.folder}")
	private String folderName;
	
	@Value("${aws.s3.bucket.user.lead.folder}")
	private String LeadfolderName;

	@Value("${aws.s3.region}")
	private String region;
	
	@Value("${aws.user.resume.endpointUrl}")
	private String resumefolder;
	
	@Value("${aws.s3.bucket.user.resumes.folder}")
	private String folder;

	private String awsS3AudioBucket;

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		this.s3client = new AmazonS3Client(credentials);
	}

	@Autowired
	public void AmazonS3ClientServiceImpl(Region awsRegion, AWSCredentialsProvider awsCredentialsProvider,
			String awsS3AudioBucket) {
		this.s3client = AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(region)
				.build();
		this.awsS3AudioBucket = awsS3AudioBucket;
	}

	@Async
	public void uploadFileToS3Bucket(MultipartFile multipartFile, boolean enablePublicReadAccess) {
		String fileName = multipartFile.getOriginalFilename();

		try {
			// creating the file in the server (temporarily)
			File file = new File(fileName);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(multipartFile.getBytes());
			fos.close();

			PutObjectRequest putObjectRequest = new PutObjectRequest(this.awsS3AudioBucket, fileName, file);

			if (enablePublicReadAccess) {
				putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
			}
			this.s3client.putObject(putObjectRequest);
			// removing the file created in the server
			file.delete();
		} catch (IOException | AmazonServiceException ex) {
			logger.error("error [" + ex.getMessage() + "] occurred while uploading [" + fileName + "] ");
		}
	}

	@Override
	public void deleteFileFromS3Bucket(String pic) {
		try {

			s3client.deleteObject(new DeleteObjectRequest(awsS3AudioBucket, pic));

		} catch (AmazonServiceException ex) {
			logger.error("error [" + ex.getMessage() + "] occurred while removing [" + pic + "] ");
		}
	}

	@Override
	public UserModel findByMobileNo(long mobileNumber) {
		// TODO Auto-generated method stub
		return userRepository.findByMobileNumber(mobileNumber);
	}

	@Override
	public Optional<UserModel> login(long mobileNumber, String password, String country) {
		// TODO Auto-generated method stub

		Optional<UserModel> customer = userRepository.login(mobileNumber, password, country);
		if (customer.isPresent()) {
			String token = UUID.randomUUID().toString();
			UserModel custom = customer.get();
			custom.setToken(token);
			custom.getFirstName();
			userRepository.save(custom);
			return userRepository.findByToken(token);
		}

		return null;
	}

	@Override
	public Optional<UserModel> findLogout(String token) {
		Optional<UserModel> customer = userRepository.findByToken(token);
		if (customer.isPresent()) {
			UserModel custom = customer.get();
			custom.setToken("");
			userRepository.save(custom);

			return null;
		}
		return null;
	}

	@Override
	public Optional<User> findByToken(String token) {
		Optional<UserModel> userRegister = userRepository.findByToken(token);
		if (userRegister.isPresent()) {
			UserModel userRegister1 = userRegister.get();
			User user = new User(String.valueOf(userRegister1.getMobileNumber()), userRegister1.getPassword(), true,
					true, true, true, AuthorityUtils.createAuthorityList("USER"));
			return Optional.of(user);
		}
		return Optional.empty();
	}

	@Override
	public void updateProfilePic(int id, String profilePic) {
		Optional<UserModel> optional = userRepository.findById(id);
		if (!optional.isPresent()) {
		}

		UserModel existing = optional.get();
		existing.setProfilePic(profilePic);

		existing = userRepository.save(existing);
		return;
	}

	@Transactional
	@Override
	public void deleteById(int userID) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		Optional<UserModel> optional = userRepository.findById(userID);
		if (!optional.isPresent()) {
			throw new ResourceNotFoundException("user not found.");

		}
		userRepository.deleteById(userID);
	}
	
	@Transactional
	@Override
	public void deleteByMobileNumber(long mobileNumber) throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		Optional<CanLeadModel> optional = canLeadRepository.findByMobNumber(mobileNumber);
		if (!optional.isPresent()) {
			throw new ResourceNotFoundException("user not found.");

		}
		canLeadRepository.deleteByMobileNumber(mobileNumber);
	}

	@Override
	public boolean deleteImage(String pic) {
		// TODO Auto-generated method stub
		String fileName = pic.substring(pic.lastIndexOf("/") + 1);
		try {
			DeleteObjectsRequest delObjReq = new DeleteObjectsRequest(bucketName).withKeys(fileName);

			s3client.deleteObjects(delObjReq);
			return true;
		} catch (SdkClientException s) {
			return false;
		}
	}

	@Override
	public String uploadFile(MultipartFile profilePic, int id, byte[] bytes) {
		// TODO Auto-generated method stub
		String fileUrl = "";
		File file = null;

		String pathToFile = "/tmp/";

		new File(pathToFile).mkdir();
		try {

			file = new File(pathToFile + profilePic);
			file.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();

		}
		FileOutputStream iofs = null;
		try {
			iofs = new FileOutputStream(file);
			iofs.write(bytes);
			iofs.close();

			String path = folderName + "/" + id + "/" + "ProfilePhoto" + "/";
			String fileName = generateFileName(profilePic);
			String videopath = path + fileName;
			fileUrl = endpointUrl + "/" + id + "/" + "ProfilePhoto" + "/" + fileName;

			uploadFileTos3bucket(videopath, file);
			file.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileUrl;
	}

	private String generateFileName(MultipartFile multiPart) {
		return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
	}

	private void uploadFileTos3bucket(String fileName, File file) {

		s3client.putObject(
				new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
	}

	@Override
	public String uploadProfilePicToS3Bucket1(MultipartFile multipartFile, long mn, boolean enablePublicReadAccess) {
		String fileUrl = "";

		try {

			String path = LeadfolderName + "/" + mn + "/" + "ProfilePhoto" + "/";
			String fileName = generateFileName(multipartFile);
			String videopath = path + fileName;
			fileUrl = leadEndpointUrl + "/" + mn + "/" + "ProfilePhoto" + "/" + fileName;


			// creating the file in the server (temporarily)
			File file = new File(System.getProperty("java.io.tmpdir") +
                    System.getProperty("file.separator" ) +
                    fileName);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(multipartFile.getBytes());
			fos.close();

			PutObjectRequest putObjectRequest = new PutObjectRequest(this.awsS3AudioBucket, videopath, file);

			if (enablePublicReadAccess) {
				putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
			}
			this.s3client.putObject(putObjectRequest);
			// removing the file created in the server
			file.delete();

			CanLeadModel existingUser = canLeadRepository.findByMobileNumber(mn);
			if (existingUser != null) {
				existingUser.setProfilePic(fileUrl);
				existingUser.setProfilePageNo(2);
				canLeadRepository.save(existingUser);
			}


		} catch (IOException | AmazonServiceException ex) {
			logger.error("error [" + ex.getMessage() + "] occurred while uploading [" + fileUrl + "] ");
			fileUrl = "error [" + ex.getMessage() + "] occurred while uploading [" + fileUrl + "] ";
		}

		return fileUrl;
	}
	
	@Override
	public String uploadProfilePicToS3Bucket(MultipartFile multipartFile, int id, boolean enablePublicReadAccess) {
		String fileUrl = "";
		File file = null;
		try {
			file = new File("/tmp/" + multipartFile);
		} catch (Exception e) {
			e.printStackTrace();

		}

		FileOutputStream iofs = null;
		try {
			iofs = new FileOutputStream(file);
			iofs.write(multipartFile.getBytes());
			iofs.close();

			String path = folderName + "/" + id + "/" + "ProfilePhoto" + "/";

			String fileName = generateFileName(multipartFile);
			String imagepath = path + fileName;
			fileUrl = endpointUrl + "/" + id + "/" + "ProfilePhoto" + "/" + fileName;

			uploadFileTos3bucket(imagepath, file);
			file.delete();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

			Optional<UserModel> optional = userRepository.findById(id);
			UserModel c = optional.get();

			c.setProfilePic(fileUrl);

			userRepository.save(c);

		return fileUrl;
	}
	
	@Override
	public String uploadProfileResToS3Bucket1(MultipartFile multipartFile, long mn, boolean enablePublicReadAccess) {
		String fileUrl = "";

		try {

			String path = folder + "/" + mn + "/" + "Resumes" + "/";
			String fileName = generateFileName(multipartFile);
			String videopath = path + fileName;
			fileUrl = resumefolder + "/" + mn + "/" + "Resumes" + "/" + fileName;


			// creating the file in the server (temporarily)
			File file = new File(System.getProperty("java.io.tmpdir") +
                    System.getProperty("file.separator" ) +
                    fileName);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(multipartFile.getBytes());
			fos.close();

			PutObjectRequest putObjectRequest = new PutObjectRequest(this.awsS3AudioBucket, videopath, file);

			if (enablePublicReadAccess) {
				putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
			}
			this.s3client.putObject(putObjectRequest);
			// removing the file created in the server
			file.delete();

			CandidateModel existingUser = candidateRepository.findByMobileNumber(mn);
			if (existingUser != null) {
				existingUser.setResume(fileUrl);
				//existingUser.setProfilePageNo(2);
				candidateRepository.save(existingUser);
			}


		} catch (IOException | AmazonServiceException ex) {
			logger.error("error [" + ex.getMessage() + "] occurred while uploading [" + fileUrl + "] ");
			fileUrl = "error [" + ex.getMessage() + "] occurred while uploading [" + fileUrl + "] ";
		}

		return fileUrl;
	}
	

}
