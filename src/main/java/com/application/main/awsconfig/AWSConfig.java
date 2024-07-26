
package com.application.main.awsconfig;

import com.amazonaws.auth.BasicSessionCredentials;

public interface AWSConfig {

	public BasicSessionCredentials client(String token);

}
