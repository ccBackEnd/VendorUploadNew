package com.application.main.model.PaymentDetailsModel;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class PaymentDetailsDTO {
	
	private String poNumber;
	private String invoiceNumber;
	private String payeraccountnumber;
	private String recieveraccountumber;
	private double amountBeforeTax;
	private double transactionFees ;
	private List<TaxDetails> taxFees;
	private double discount;
	private String paymentMethod;
	private String paymentStatus;
	private LocalDate paymentDate;
	private String paymentReferenceNumber;
	private String paymentremarks;



	// Getters and Setters
	// Constructors
	// toString(), equals(), hashCode(), etc.
}
