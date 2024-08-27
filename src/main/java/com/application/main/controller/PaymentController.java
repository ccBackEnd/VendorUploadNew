package com.application.main.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.application.main.PaymentRepositories.PaymentDetailsRepository;
import com.application.main.Paymentmodel.PaymentDetailsVendor;
import com.application.main.Repositories.DocDetailsRepository;
import com.application.main.awsconfig.AWSClientConfigService;
import com.application.main.awsconfig.AwsService;

@RestController
@RequestMapping("call/vendor/Vendorportal")
public class PaymentController {
	@Autowired
	AwsService s3service;
	
	@Autowired
	private AWSClientConfigService s3client;

	@Autowired
	DocDetailsRepository docdetailsrepository;
	
	@Autowired
	PaymentDetailsRepository paymentrepo;
	
	@PostMapping("payment/PaymentDetails")
	public ResponseEntity<?> PaymentEntryDetails(
			@RequestBody PaymentDetailsVendor pdv 
			){
		if(pdv==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		paymentrepo.save(pdv);
		return ResponseEntity.ok("");
		
	}
}
