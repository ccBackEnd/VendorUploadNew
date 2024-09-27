package com.application.main.model.PaymentDetailsModel;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@Document(collection = "PoPayment")
public class Paymentbreakup {
	@Id
	private String id;

	private String poNumber;
	private String invoiceNumber;
	private LocalDate paymentDate;
	private List<TaxDetails> taxfees;
	private double originalPrice;
	private double transactionFees;
	private double calculatedPrice;
	private double discountOnPrice;
	private PayerDetails payerdetails;
	private RecieverDetails recieverdetails;


}
