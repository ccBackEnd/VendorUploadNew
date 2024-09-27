
package com.application.main.config.awsconfig;

import com.amazonaws.auth.BasicSessionCredentials;

public interface AWSConfig {

	public BasicSessionCredentials client(String token);

}
