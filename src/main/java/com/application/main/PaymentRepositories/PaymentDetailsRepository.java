package com.application.main.PaymentRepositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.application.main.Paymentmodel.Paymentbreakup;


public interface PaymentDetailsRepository extends MongoRepository<Paymentbreakup, String> {
	
	List<Paymentbreakup> findByPoNumber(String poNumber);
	Paymentbreakup findByInvoiceNumber(String invoiceNumber);
	List<Paymentbreakup> findByPaymentDateBetween(LocalDate paymentDatefrom , LocalDate paymentDateTo);
}
