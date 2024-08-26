package com.application.main.PaymentRepositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.Paymentmodel.PayerDetails;

public interface PayerDetailsRepository extends MongoRepository<PayerDetails, String> {

}
