package com.taizo.service;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.taizo.model.CloudwatchLogEventModel;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;


@Service
public class CloudWatchLogService {
	
	@Value("${aws.access.key.id}")
	private String accessKey;
	@Value("${aws.access.key.secret}")
	private String secretKey;
	@Value("${cloudwatch.loggroup.name}")
	private String logGroupName;
	@Value("${cloudwatch.loggroup.name.failure}")
	private String failureLogGroupName;
	@Value("${cloudwatch.candidate.logstream}")
	private String candidateLogStream;
	@Value("${cloudwatch.employer.logstream}")
	private String employerLogStream;

	@Async
    public void cloudLog(CloudwatchLogEventModel logEventModel, String module) {

        Region region = Region.AP_SOUTH_1;
        
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
        		  accessKey,
        		  secretKey);
		CloudWatchLogsClient logsClient = CloudWatchLogsClient.builder().region(region)
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
		
		if(module.equalsIgnoreCase("C")) {

        putCWLogEvents(logsClient, logGroupName, candidateLogStream,logEventModel) ;
		} else {
	        putCWLogEvents(logsClient, logGroupName, employerLogStream,logEventModel) ;

		}
        logsClient.close();
    }
    
	@Async
    public void cloudLogFailure(CloudwatchLogEventModel logEventModel, String module) {

        Region region = Region.AP_SOUTH_1;
        
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
        		  accessKey,
        		  secretKey);
		CloudWatchLogsClient logsClient = CloudWatchLogsClient.builder().region(region)
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

		if(module.equalsIgnoreCase("C")) {
        putCWLogEvents(logsClient, failureLogGroupName, candidateLogStream,logEventModel) ;
		} else {
	        putCWLogEvents(logsClient, failureLogGroupName, employerLogStream,logEventModel) ;
		}
        logsClient.close();
    }

    public static void putCWLogEvents(CloudWatchLogsClient logsClient, String logGroupName, String streamName, 
    		CloudwatchLogEventModel logEventModel) {

     try {
        DescribeLogStreamsRequest logStreamRequest = DescribeLogStreamsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamNamePrefix(streamName)
                .build();
        DescribeLogStreamsResponse describeLogStreamsResponse = logsClient.describeLogStreams(logStreamRequest);

        String sequenceToken = describeLogStreamsResponse.logStreams().get(0).uploadSequenceToken();

        // Build an input log message to put to CloudWatch.
        InputLogEvent inputLogEvent = InputLogEvent.builder()
                .message(logEventModel.toString())
                .timestamp(System.currentTimeMillis())
                .build();

        PutLogEventsRequest putLogEventsRequest = PutLogEventsRequest.builder()
                .logEvents(Arrays.asList(inputLogEvent))
                .logGroupName(logGroupName)
                .logStreamName(streamName)
                .sequenceToken(sequenceToken)
                .build();

        logsClient.putLogEvents(putLogEventsRequest);

     } catch (CloudWatchLogsException e) {
         System.err.println(e.awsErrorDetails().errorMessage());
         System.exit(1);
     }
    }
}
