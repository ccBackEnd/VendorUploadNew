
package com.application.main.config.awsconfig.AwsConfigService;

import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.application.main.config.awsconfig.AWSClientConfig;




@Service
public class AWSClientConfigService implements AWSClientConfig{

	@Value("${minio.rest-url}")
	private String baseUrl;
	
	@Value("${minio.rest-port}")
	private String port;
	
	//	AWS Services
	@Autowired
	private AWSConfigService awsConfigService;
	@Autowired
	private AWSS3ContentService awsS3ContentService; 

	@Override
	public AmazonS3 awsClientConfiguration(String token)
	{
		System.out.println("------------------------AWS CLIENT CONFIGURATION STARTS------------------------");
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		clientConfiguration.setSignerOverride("AWSS3V4SignerType");
		BasicSessionCredentials response = awsConfigService.client(token);
		AmazonS3 awsClient = awsS3ContentService.client();
		System.out.println("------------------------CLIENT CREATED SUCCESSFULLY-----------------------------");
		awsClient = AmazonS3ClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(baseUrl+":"+port,
						Regions.US_EAST_1.getName()))
				.withClientConfiguration(clientConfiguration)
				.withCredentials(new AWSStaticCredentialsProvider(response)).build();
		return awsClient;
	}
}
