
package com.application.main.config.awsconfig;

import com.amazonaws.services.s3.AmazonS3;


public interface AWSClientConfig {

	
	public AmazonS3 awsClientConfiguration(String token);

}
