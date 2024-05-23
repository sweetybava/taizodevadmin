package com.taizo.service;

import com.amazonaws.auth.AWSCredentials; 
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.SendTemplatedEmailRequest;
import com.amazonaws.services.simpleemail.model.SendTemplatedEmailResult;
import com.taizo.model.Admin;
import com.taizo.model.EmployerPaymentModel;
import com.taizo.repository.AdminRepository;
import com.taizo.utils.FreeMarkerUtils;
import com.taizo.utils.TupleStore;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Properties;



@Service("sesService")
public class AmazonSESMailUtil {

	private static final Logger logger = LoggerFactory.getLogger(AmazonSESMailUtil.class);

	@Value("${aws.access.key.id}")
	private String accessKey;

	@Value("${aws.access.key.secret}")
	private String secretKey;

	@Autowired
	Configuration configuration;

	private String senderEmail = "Taizo Notifications <info@taizo.in>";

	private AmazonSimpleEmailService sesClient;

	@Autowired
	AdminRepository adminRepository;

	@PostConstruct
	private void initializeSES() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		sesClient = AmazonSimpleEmailServiceClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.AP_SOUTH_1).build();
	}

	public void sendEmailSES(String templateName, TupleStore contact) {
		Destination destination = new Destination();
		List<String> toAddresses = new ArrayList<String>();
		toAddresses.add(contact.getKey());
		destination.setToAddresses(toAddresses);
		SendTemplatedEmailRequest templatedEmailRequest = new SendTemplatedEmailRequest();
		templatedEmailRequest.withDestination(destination);
		templatedEmailRequest.withTemplate(templateName);
		templatedEmailRequest.withTemplateData(contact.getValue());
		templatedEmailRequest.withSource(senderEmail);
		SendTemplatedEmailResult templatedEmailResult = sesClient.sendTemplatedEmail(templatedEmailRequest);
		logger.info(contact.toString());
		logger.info(templatedEmailResult.getMessageId());
	}

	public void sendEmailWithMultipleAttachments(String invoiceNum, String email, String emailContent, byte[] bytes) {

		Session session = Session.getInstance(new Properties(System.getProperties()));
		MimeMessage mimeMessage = new MimeMessage(session);

		try {
			mimeMessage.setSubject("Payment successful for Taizo Technologies Private Limited", "UTF-8");
			mimeMessage.setFrom(new InternetAddress("info@taizo.in"));
			mimeMessage.setRecipients(RecipientType.TO, email);

			MimeMultipart msgBody = new MimeMultipart("alternative");
			MimeBodyPart wrap = new MimeBodyPart();
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(emailContent, "text/html; charset=UTF-8");
			msgBody.addBodyPart(htmlPart);
			wrap.setContent(msgBody);

			MimeMultipart msg = new MimeMultipart("mixed");
			mimeMessage.setContent(msg);
			msg.addBodyPart(wrap);

			DataSource dataSource = new ByteArrayDataSource(bytes, "application/pdf");
			MimeBodyPart pdfBodyPart = new MimeBodyPart();
			pdfBodyPart.setDataHandler(new DataHandler(dataSource));
			pdfBodyPart.setFileName("Invoice-" + invoiceNum + ".pdf");
			msg.addBodyPart(pdfBodyPart);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			mimeMessage.writeTo(outputStream);
			RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

			SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
			sesClient.sendRawEmail(rawEmailRequest);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void sendEmailEmpAlert(String emailId, String subject, String emailContent, byte[] bytes) {
		// TODO Auto-generated method stub

		Session session = Session.getInstance(new Properties(System.getProperties()));
		MimeMessage mimeMessage = new MimeMessage(session);

		try {
			mimeMessage.setSubject(subject, "UTF-8");
			mimeMessage.setFrom(new InternetAddress("Taizo.in <info@taizo.in>"));
			mimeMessage.setRecipients(RecipientType.TO, emailId);


			MimeMultipart msgBody = new MimeMultipart("alternative");
			MimeBodyPart wrap = new MimeBodyPart();
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(emailContent, "text/html; charset=UTF-8");
			msgBody.addBodyPart(htmlPart);
			wrap.setContent(msgBody);
			MimeMultipart msg = new MimeMultipart("mixed");
			mimeMessage.setContent(msg);
			msg.addBodyPart(wrap);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			mimeMessage.writeTo(outputStream);
			RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

			SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
			sesClient.sendRawEmail(rawEmailRequest);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void sendEmailWithMultipleAttachments1(String emailId, Long adminId, String emailContent, byte[] pdfBytes, byte[] bytes) {
		Session session = Session.getInstance(new Properties(System.getProperties()));
		MimeMessage mimeMessage = new MimeMessage(session);

		String ccEmail = "k.saravanan@taizo.in";
		String cEmail = "vinoth.mani@taizo.in ";

		Optional<Admin> adminOptional = adminRepository.findById(adminId);

		Admin admin = adminOptional.get();
		String fromEmailId = admin.getEmailId();
		String name = admin.getUserName();

		String fromNameAndEmail = name + " <" + fromEmailId + ">";

		try {
			mimeMessage.setSubject(" Pro-forma Invoice | Your Hiring Partner🚀", "UTF-8");
			mimeMessage.setFrom(new InternetAddress(fromNameAndEmail));
			mimeMessage.setRecipients(RecipientType.TO, emailId);

			// Add CC (Carbon Copy) recipient
			mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail));

			// Add the additional CC recipient
			mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(fromEmailId));
			mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cEmail));

			MimeMultipart msgBody = new MimeMultipart("alternative");
			MimeBodyPart wrap = new MimeBodyPart();
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(emailContent, "text/html; charset=UTF-8");
			msgBody.addBodyPart(htmlPart);
			wrap.setContent(msgBody);

			MimeMultipart msg = new MimeMultipart("mixed");
			mimeMessage.setContent(msg);
			msg.addBodyPart(wrap);

			DataSource dataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");
			MimeBodyPart pdfBodyPart = new MimeBodyPart();
			pdfBodyPart.setDataHandler(new DataHandler(dataSource));
			pdfBodyPart.setFileName("Service Level Agreement.pdf");
			msg.addBodyPart(pdfBodyPart);

			// Attach the second PDF
			DataSource dataSource2 = new ByteArrayDataSource(bytes, "application/pdf");
			MimeBodyPart pdfBodyPart2 = new MimeBodyPart();
			pdfBodyPart2.setDataHandler(new DataHandler(dataSource2));
			pdfBodyPart2.setFileName("Taizo-proforma-invoice.pdff");
			msg.addBodyPart(pdfBodyPart2);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			mimeMessage.writeTo(outputStream);
			RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

			SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
			sesClient.sendRawEmail(rawEmailRequest);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void sendEmailEmpAlerts1(String emailId, Long adminId, String message, byte[] bytes, byte[] mergedPdfBytes) {
		// TODO Auto-generated method stub

		Session session = Session.getInstance(new Properties(System.getProperties()));
		MimeMessage mimeMessage = new MimeMessage(session);

		String ccEmail = "k.saravanan@taizo.in";
		String cEmail = "vinoth.mani@taizo.in ";

		Optional<Admin> adminOptional = adminRepository.findById(adminId);

		Admin admin = adminOptional.get();
		String fromEmailId = admin.getEmailId();
		String name = admin.getUserName();

		String fromNameAndEmail = name + " <" + fromEmailId + ">";


		try {
			mimeMessage.setSubject("Transforming Manufacturing Recruitment: Let's Work Together! -Taizo.in", "UTF-8");
			mimeMessage.setFrom(new InternetAddress(fromNameAndEmail));
			mimeMessage.setRecipients(RecipientType.TO, emailId);

			// Add CC (Carbon Copy) recipient
			mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail));

			// Add the additional CC recipient
			mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(fromEmailId));
			mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cEmail));

			MimeMultipart msgBody = new MimeMultipart("alternative");
			MimeBodyPart wrap = new MimeBodyPart();
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(message, "text/html; charset=UTF-8");
			msgBody.addBodyPart(htmlPart);
			wrap.setContent(msgBody);

			MimeBodyPart attachmentPart = new MimeBodyPart();
			attachmentPart.setFileName("SLA.pdf");
			attachmentPart.setContent(mergedPdfBytes, "application/pdf");

			MimeMultipart msg = new MimeMultipart("mixed");
			mimeMessage.setContent(msg);
			msg.addBodyPart(wrap);
			msg.addBodyPart(attachmentPart);


			// Set the content of the MimeMessage to the mixed multipart
			mimeMessage.setContent(msg);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			mimeMessage.writeTo(outputStream);
			RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

			SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(
					new RawMessage(ByteBuffer.wrap(outputStream.toByteArray())));
			sesClient.sendRawEmail(rawEmailRequest);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void sendIntroEmail(String emailId, Long adminId, String message, byte[] bytes, String companyName, String ccMail) {
		// TODO Auto-generated method stub

		Session session = Session.getInstance(new Properties(System.getProperties()));
		MimeMessage mimeMessage = new MimeMessage(session);

		String ccEmail = "k.saravanan@taizo.in";
		String cEmail = "vinoth.mani@taizo.in ";

		Optional<Admin> adminOptional = adminRepository.findById(adminId);

		Admin admin = adminOptional.get();
		String fromEmailId = admin.getEmailId();
		String name = admin.getUserName();

		String fromNameAndEmail = name + " <" + fromEmailId + ">";

		try {
			mimeMessage.setSubject(companyName + ": Together, let's transform hiring in the manufacturing industry!", "UTF-8");
			mimeMessage.setFrom(new InternetAddress(fromNameAndEmail));
			mimeMessage.setRecipients(Message.RecipientType.TO, emailId);

			// Add CC (Carbon Copy) recipient
			mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail));

			// Add the additional CC recipient
			mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(fromEmailId));
			mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cEmail));

			StringBuilder ccAddresses = new StringBuilder();
			ccAddresses.append(ccEmail).append(',');
			ccAddresses.append(fromEmailId).append(',');
			ccAddresses.append(cEmail);
			if (ccMail != null && !ccMail.isEmpty()) {
				ccAddresses.append(',').append(ccMail);
			}

			// Add CC (Carbon Copy) recipients
			mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccAddresses.toString()));

			MimeMultipart msgBody = new MimeMultipart("alternative");
			MimeBodyPart wrap = new MimeBodyPart();
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(message, "text/html; charset=UTF-8");
			msgBody.addBodyPart(htmlPart);
			wrap.setContent(msgBody);

			MimeMultipart msg = new MimeMultipart("mixed");
			mimeMessage.setContent(msg);
			msg.addBodyPart(wrap);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			mimeMessage.writeTo(outputStream);
			RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

			SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
			sesClient.sendRawEmail(rawEmailRequest);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	public void sendEmailWithMultipleAttachments1(String emailId, String emailContent, Long adminId, byte[] bytes1, byte[] bytes) {
		Session session = Session.getInstance(new Properties(System.getProperties()));
		MimeMessage mimeMessage = new MimeMessage(session);

		String ccEmail = "k.saravanan@taizo.in";
		String cEmail = "vinoth.mani@taizo.in ";

		Optional<Admin> adminOptional = adminRepository.findById(adminId);

		Admin admin = adminOptional.get();
		String fromEmailId = admin.getEmailId();
		String name = admin.getUserName();

		String fromNameAndEmail = name + " <" + fromEmailId + ">";

		try {
			mimeMessage.setSubject(" Pro-forma Invoice | Your Hiring Partner🚀", "UTF-8");
			mimeMessage.setFrom(new InternetAddress(fromNameAndEmail));
			mimeMessage.setRecipients(RecipientType.TO, emailId);

			// Add CC (Carbon Copy) recipients
			String ccEmails = ccEmail + "," + fromEmailId;
			String cEmails = cEmail + "," + fromEmailId;
			mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmails));
			mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cEmails));
			MimeMultipart msgBody = new MimeMultipart("alternative");
			MimeBodyPart wrap = new MimeBodyPart();
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(emailContent, "text/html; charset=UTF-8");
			msgBody.addBodyPart(htmlPart);
			wrap.setContent(msgBody);

			MimeMultipart msg = new MimeMultipart("mixed");
			mimeMessage.setContent(msg);
			msg.addBodyPart(wrap);


			// Attach the first PDF
			DataSource dataSource1 = new ByteArrayDataSource(bytes, "application/pdf");
			MimeBodyPart pdfBodyPart1 = new MimeBodyPart();
			pdfBodyPart1.setDataHandler(new DataHandler(dataSource1));
			pdfBodyPart1.setFileName("Proforma Invoice.pdf");
			msg.addBodyPart(pdfBodyPart1);

			// Attach the second PDF
			DataSource dataSource2 = new ByteArrayDataSource(bytes1, "application/pdf");
			MimeBodyPart pdfBodyPart2 = new MimeBodyPart();
			pdfBodyPart2.setDataHandler(new DataHandler(dataSource2));
			pdfBodyPart2.setFileName("Service Level Agreement.pdf");
			msg.addBodyPart(pdfBodyPart2);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			mimeMessage.writeTo(outputStream);
			RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

			SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
			sesClient.sendRawEmail(rawEmailRequest);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void sendEmailWithAttachment(int invoiceNo, String email, byte[] bytes) {
		try {
			// Create a new session with the necessary email properties
			Properties properties = System.getProperties();
			Session session = Session.getDefaultInstance(properties);

			// Create a MimeMessage
			MimeMessage mimeMessage = new MimeMessage(session);

			// Set the email subject
			mimeMessage.setSubject("Payment successful for Taizo Technologies Private Limited", "UTF-8");

			// Set the sender
			mimeMessage.setFrom(new InternetAddress("info@taizo.in"));

			// Set the recipient
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

			// Create the email body and attach the PDF
			Multipart multipart = new MimeMultipart();
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent("Your invoice is attached in this email.", "text/plain");
			multipart.addBodyPart(messageBodyPart);

			// Attach the PDF
			DataSource dataSource = new ByteArrayDataSource(bytes, "application/pdf");
			MimeBodyPart attachmentPart = new MimeBodyPart();
			attachmentPart.setDataHandler(new DataHandler(dataSource));
			attachmentPart.setFileName("Invoice-" + invoiceNo + ".pdf");
			multipart.addBodyPart(attachmentPart);

			// Set the email content
			mimeMessage.setContent(multipart);

			// Send the email via SES
			SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest()
					.withRawMessage(new RawMessage(ByteBuffer.wrap(getByteArray(mimeMessage))));
			sesClient.sendRawEmail(rawEmailRequest);

			// Handle success or log result
			System.out.println("Email sent with attachment.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private byte[] getByteArray(MimeMessage mimeMessage) throws MessagingException, IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		mimeMessage.writeTo(outputStream);
		return outputStream.toByteArray();
	}

	public void sendSLA(String emailId, Long adminId, String emailMessage, byte[] mergedPdfBytes, String companyName, String ccMail) {
	    try {
	        Optional<Admin> adminOptional = adminRepository.findById(adminId);

	        if (adminOptional.isPresent()) {
	            Admin admin = adminOptional.get();
	            String fromEmailId = admin.getEmailId();
	            String name = admin.getUserName();
	            String fromNameAndEmail = name + " <" + fromEmailId + ">";

	            Session session = Session.getInstance(new Properties(System.getProperties()));
	            MimeMessage mimeMessage = new MimeMessage(session);

	            mimeMessage.setSubject(companyName + " - Service Level Agreement", "UTF-8");
	            mimeMessage.setFrom(new InternetAddress(fromNameAndEmail));
	            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(emailId));
	            mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(fromEmailId));

	            // Add custom CC recipients if ccMail is provided
	            if (ccMail != null && !ccMail.isEmpty()) {
	                mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccMail));
	            }

	            // Create the main content of the email
	            MimeBodyPart mainContentPart = new MimeBodyPart();
	            MimeMultipart mainContent = new MimeMultipart("related");

	            // HTML part with the message content
	            MimeBodyPart htmlPart = new MimeBodyPart();
	            htmlPart.setContent(emailMessage, "text/html; charset=UTF-8");

	            // Attachment part for the PDF
	            MimeBodyPart attachmentPart = new MimeBodyPart();
	            attachmentPart.setFileName("SLA.pdf");
	            attachmentPart.setContent(mergedPdfBytes, "application/pdf");

	            mainContent.addBodyPart(htmlPart);
	            mainContent.addBodyPart(attachmentPart);
	            mainContentPart.setContent(mainContent);

	            // Create the complete email message
	            MimeMultipart completeMessage = new MimeMultipart("mixed");
	            completeMessage.addBodyPart(mainContentPart);

	            // Set the content of the MimeMessage to the mixed multipart
	            mimeMessage.setContent(completeMessage);

	            // Send the email
	            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	            mimeMessage.writeTo(outputStream);

	            RawMessage rawMessage = new RawMessage().withData(ByteBuffer.wrap(outputStream.toByteArray()));
	            SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest().withRawMessage(rawMessage);

	            sesClient.sendRawEmail(rawEmailRequest);

	        } else {
	            // Handle the case where Admin is not found
	            logger.error("Admin not found for ID: {}", adminId);
	        }
	    } catch (Exception ex) {
	        logger.error("Error in sending SLA email", ex);
	        // Handle the exception as needed
	    }
	}


	public void sendInterviewWithoutAttachment(String emailId, String contactPersonName, String jobCategory, String day,
											   String candidateName, String companyName, String emailMessage) throws MessagingException {

		String fromEmailId = "sowmiya.g@taizo.in";
		String ccEmail = "rahulsekar2000@gmail.com";
		Session session = Session.getInstance(new Properties(System.getProperties()));
		MimeMessage mimeMessage = new MimeMessage(session);

		try {
			mimeMessage.setSubject(companyName + " - Interview Scheduled", "UTF-8");
			mimeMessage.setFrom(new InternetAddress(fromEmailId));
			mimeMessage.setRecipients(Message.RecipientType.TO, emailId);

			mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail));

			// Create the email body part
			MimeBodyPart emailBodyPart = new MimeBodyPart();
			emailBodyPart.setContent(emailMessage, "text/html; charset=UTF-8");

			// Create the multipart message
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(emailBodyPart);

			// Set the content of the MimeMessage to the multipart message
			mimeMessage.setContent(multipart);

			// Send the email
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			mimeMessage.writeTo(outputStream);
			RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
			SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
			sesClient.sendRawEmail(rawEmailRequest);

			// Log or handle success
			System.out.println("Email sent successfully for candidate: " + candidateName);

		} catch (Exception ex) {
			// Log or handle exceptions
			ex.printStackTrace();
		}
	}


	public void sendInterviewDetailsEmail(String emailId, Long adminId, String message, byte[] bytes,
										  String companyName) {
		Session session = Session.getInstance(new Properties(System.getProperties()));
		MimeMessage mimeMessage = new MimeMessage(session);


		Optional<Admin> adminOptional = adminRepository.findById(adminId);

		Admin admin = adminOptional.get();
		String fromEmailId = admin.getEmailId();
		String name = admin.getUserName();

		String fromNameAndEmail = name + " <" + fromEmailId + ">";

		try {
			mimeMessage.setSubject(companyName + ": Interview Details", "UTF-8");
			mimeMessage.setFrom(new InternetAddress(fromNameAndEmail));
			mimeMessage.setRecipients(Message.RecipientType.TO, emailId);

			MimeMultipart msgBody = new MimeMultipart("alternative");
			MimeBodyPart wrap = new MimeBodyPart();
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(message, "text/html; charset=UTF-8");
			msgBody.addBodyPart(htmlPart);
			wrap.setContent(msgBody);

			MimeMultipart msg = new MimeMultipart("mixed");
			mimeMessage.setContent(msg);
			msg.addBodyPart(wrap);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			mimeMessage.writeTo(outputStream);
			RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

			SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
			sesClient.sendRawEmail(rawEmailRequest);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void sendInterviewDetailsEmail(String emailId, Long adminId, String message, MimeMessage mimeMessage) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			mimeMessage.writeTo(outputStream);
			byte[] bytes = outputStream.toByteArray();

			RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(bytes));
			SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
			sesClient.sendRawEmail(rawEmailRequest);
		} catch (MessagingException | IOException e) {
			e.printStackTrace();
			// Handle the exception appropriately based on your application's requirements
			throw new RuntimeException("Error sending email via Amazon SES");
		}
	}

	public void sendInvoice(String emailId, String ccMail, MultipartFile invoiceAttachment, String message,
							MimeMessage mimeMessage, Long adminId, String invoiceNumber) {
		try {
			String ccEmail = "k.saravanan@taizo.in";
			String cEmail = "vinoth.mani@taizo.in";

			Optional<Admin> adminOptional = adminRepository.findById(adminId);

			if (adminOptional.isPresent()) {
				Admin admin = adminOptional.get();
				String fromEmailId = admin.getEmailId();
				String name = admin.getUserName();
				String fromNameAndEmail = name + " <" + fromEmailId + ">";

				mimeMessage.setSubject( "Taizo.in - Invoice copy | " + invoiceNumber , "UTF-8");
				mimeMessage.setFrom(new InternetAddress(fromNameAndEmail));
				mimeMessage.setRecipients(Message.RecipientType.TO, emailId);

				// Add CC (Carbon Copy) recipient
				//mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail));

				// Add the additional CC recipient
				mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(fromEmailId));
				mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cEmail));

				// Create the email body part
				Multipart msgBody = new MimeMultipart();
				MimeBodyPart htmlPart = new MimeBodyPart();
				htmlPart.setContent(message, "text/html; charset=UTF-8");
				msgBody.addBodyPart(htmlPart);

				// Add the attachment
				MimeBodyPart attachmentPart = new MimeBodyPart();
				attachmentPart.setDataHandler(new DataHandler(new ByteArrayDataSource(invoiceAttachment.getBytes(), invoiceAttachment.getContentType())));
				attachmentPart.setFileName(invoiceAttachment.getOriginalFilename());
				msgBody.addBodyPart(attachmentPart);

				mimeMessage.setContent(msgBody);

				// Send the email
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				mimeMessage.writeTo(outputStream);

				RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
				SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
				sesClient.sendRawEmail(rawEmailRequest);

				// Log or handle success
				System.out.println("Email sent successfully to " + emailId);
			} else {
				// Handle the case where Admin is not found
				// Log an error and return from the method
				System.err.println("Admin not found for ID: " + adminId);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			// Handle exceptions as needed
		}
	}
	
	public void sendSLA1(String emailId, Long adminId, String message, byte[] emailAttachmentBytes, String companyName, String ccMail) {
	    try {
	        Optional<Admin> adminOptional = adminRepository.findById(adminId);

	        if (adminOptional.isPresent()) {
	            Admin admin = adminOptional.get();
	            String fromEmailId = admin.getEmailId();
	            String name = admin.getUserName();
	            String fromNameAndEmail = name + " <" + fromEmailId + ">";

	            // Construct the AWS SES client
	            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClient.builder()
	                    .withRegion(Regions.AP_SOUTH_1) 
	                    .build();
	            Session session = Session.getInstance(new Properties(System.getProperties()));
	            MimeMessage mimeMessage = new MimeMessage(session);

	            mimeMessage.setSubject(companyName + " - Service Level Agreement", "UTF-8");
	            mimeMessage.setFrom(new InternetAddress(fromNameAndEmail));
	            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(emailId));

	            // Add custom CC recipients if ccMail is provided
	            if (ccMail != null && !ccMail.isEmpty()) {
	                mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccMail));
	            }

	            // Create the main content of the email
	            MimeBodyPart mainContentPart = new MimeBodyPart();
	            MimeMultipart mainContent = new MimeMultipart("related");

	            // HTML part with the message content
	            MimeBodyPart htmlPart = new MimeBodyPart();
	            htmlPart.setContent(message, "text/html; charset=UTF-8");

	            // Attachment part for the PDF
	            MimeBodyPart attachmentPart = new MimeBodyPart();
	            attachmentPart.setFileName("SLA.pdf");
	            attachmentPart.setContent(emailAttachmentBytes, "application/pdf");

	            mainContent.addBodyPart(htmlPart);
	            mainContent.addBodyPart(attachmentPart);
	            mainContentPart.setContent(mainContent);

	            // Create the complete email message
	            MimeMultipart completeMessage = new MimeMultipart("mixed");
	            completeMessage.addBodyPart(mainContentPart);

	            // Set the content of the MimeMessage to the mixed multipart
	            mimeMessage.setContent(completeMessage);

	            // Send the email
	            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	            mimeMessage.writeTo(outputStream);

	            RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
				SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
				sesClient.sendRawEmail(rawEmailRequest);

	        } else {
	            // Handle the case where Admin is not found
	            logger.error("Admin not found for ID: {}", adminId);
	        }
	    } catch (Exception ex) {
	        logger.error("Error in sending SLA email", ex);
	        // Handle the exception as needed
	    }
	}

	public void sendMidSeniorRegLinkWithoutAttachment(String emailId, long adminId, String emailMessage) throws MessagingException {

	    Optional<Admin> adminOptional = adminRepository.findById(adminId);

	    if (adminOptional.isPresent()) {
	        Admin admin = adminOptional.get();
	        String fromEmailId = admin.getEmailId();
	        String name = admin.getUserName();

	        String fromNameAndEmail = name + " <" + fromEmailId + ">";

	        Session session = Session.getInstance(new Properties(System.getProperties()));
	        MimeMessage mimeMessage = new MimeMessage(session);

	        try {
	            mimeMessage.setSubject("Schedule an Online Technical Discussion with Taizo.in", "UTF-8");
	            mimeMessage.setFrom(new InternetAddress(fromEmailId));
	            mimeMessage.setRecipients(Message.RecipientType.TO, emailId);

	            // Create the email body part
	            MimeBodyPart emailBodyPart = new MimeBodyPart();
	            emailBodyPart.setContent(emailMessage, "text/html; charset=UTF-8");

	            // Create the multipart message
	            Multipart multipart = new MimeMultipart();
	            multipart.addBodyPart(emailBodyPart);

	            // Set the content of the MimeMessage to the multipart message
	            mimeMessage.setContent(multipart);

	            // Send the email
	            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	            mimeMessage.writeTo(outputStream);
	            RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
	            SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
	            sesClient.sendRawEmail(rawEmailRequest);

	            // Log or handle success
	            System.out.println("Email sent successfully for candidate: ");

	        } catch (Exception ex) {
	            // Log or handle exceptions
	            ex.printStackTrace();
	        }
	    }
	}


	




}

	

