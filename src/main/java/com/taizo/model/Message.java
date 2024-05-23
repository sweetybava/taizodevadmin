package com.taizo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Message {

	private String type;
	private String messageId;
	private String message;
	private String subscribeURL;
	private String subject;
	private String timestamp;
	private String topicArn;
	private String token;
	private String signature;
	private String signatureVersion;
	private String signingCertURL;
	private String unsubscribeURL;
}
