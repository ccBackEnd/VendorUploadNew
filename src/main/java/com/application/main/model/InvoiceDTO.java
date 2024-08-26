package com.application.main.model;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.annotation.Id;

import com.application.main.PaymentRepositories.PaymentDetailsRepository;
import com.application.main.Paymentmodel.PaymentDetailsVendor;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class InvoiceDTO extends Pageable {

	@Autowired
	PaymentDetailsRepository paymentrepo;
	
	@Id
	private String id;

	private String poNumber;
	private String invoiceNumber;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private String invoiceDate;
	private String status;
	private String deliveryPlant;
	private String mobileNumber;
	private PaymentDetailsVendor paymentdetails;
	private String eic;
	private String paymentType;
	private String invoiceurl;
	private String invoiceAmount;

	public InvoiceDTO(String id, String poNumber, String invoiceNumber , LocalDate invoiceDate,
			String status, String deliveryPlant, String mobileNumber, String eic,Optional<PaymentDetailsVendor> paymentdetails, String paymentType, String invoiceurl,
			String invoiceAmount) {
		super();
		this.id = id;
		this.poNumber = poNumber;
		this.invoiceNumber = invoiceNumber;
		if(!paymentdetails.isPresent()) this.paymentdetails = null;
		else this.paymentdetails = paymentdetails.get();
		this.invoiceDate = invoiceDate.toString();
		this.status = status;
		this.deliveryPlant = deliveryPlant;
		this.mobileNumber = mobileNumber;
		this.eic = eic;
		this.paymentType = paymentType;
		this.invoiceurl = invoiceurl;
		this.invoiceAmount = invoiceAmount;
	}

}
