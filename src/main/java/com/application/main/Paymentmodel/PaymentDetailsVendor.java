package com.application.main.Paymentmodel;

import java.math.BigDecimal;
import java.time.LocalDate;

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

public class PaymentDetailsVendor {
	@Id
	private String paymentId;

	private String poNumber;
	private String invoiceNumber;
	private String accountnumber;
	private LocalDate paymentDate;
	private BigDecimal paymentAmount;
	private String paymentMethod;
	private String paymentStatus;
	private String paymentReferenceNumber;

	private PayerDetails payerdetails;
	private RecieverDetails receiverDetails;
	private BigDecimal transactionFees;
	private String remarks;

	// Getters and Setters
	// Constructors
	// toString(), equals(), hashCode(), etc.
}
