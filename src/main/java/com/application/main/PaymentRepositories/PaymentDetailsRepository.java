package com.application.main.PaymentRepositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.Paymentmodel.PaymentDetailsVendor;


public interface PaymentDetailsRepository extends MongoRepository<PaymentDetailsVendor, String> {
	
	Optional<PaymentDetailsVendor> findByAccountnumber(String accountnumber);

}
