package com.application.main.model;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;

import com.application.main.PaymentRepositories.PaymentDetailsRepository;
import com.application.main.Paymentmodel.Paymentbreakup;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class InvoiceDTO {

	@Autowired
	PaymentDetailsRepository paymentrepo;
	
	@Id
	private String id;

	private String poNumber;
	private String invoiceNumber;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private String invoiceDate;
	private String latestRecieviedDate;
	private String latestforwardedDate;
	private String status;
	private String deliveryPlant;
	private String mobileNumber;
	private Paymentbreakup paymentdetails;
	private String eic;
	private String paymentType;
	private String invoiceurl;
	private String invoiceAmount;

//	public InvoiceDTO(String invoiceid, String poNumber, String invoiceNumber , LocalDate invoiceDate,
//			String status, String deliveryPlant, String mobileNumber, String eic,Paymentbreakup paymentdetails, String paymentType, String invoiceurl,
//			String invoiceAmount) {
//		super();
//		this.id = invoiceid;
//		this.poNumber = poNumber;
//		this.invoiceNumber = invoiceNumber;
//		
//		this.paymentdetails = paymentdetails;
//		this.invoiceDate = invoiceDate.toString();
//		this.status = status;
//		this.deliveryPlant = deliveryPlant;
//		this.mobileNumber = mobileNumber;
//		this.eic = eic;
//		this.paymentType = paymentType;
//		this.invoiceurl = invoiceurl;
//		this.invoiceAmount = invoiceAmount;
//	}
	
	public InvoiceDTO(String invoiceid, String poNumber, String invoiceNumber , LocalDate invoiceDate,
			String status, String deliveryPlant, String mobileNumber, String eic,Paymentbreakup paymentdetails, String paymentType, String invoiceurl,
			String invoiceAmount,LocalDate revertdate , LocalDate forwardeddate) {
		super();
		this.id = invoiceid;
		this.poNumber = poNumber;
		this.invoiceNumber = invoiceNumber;
		if(revertdate!=null) this.latestRecieviedDate = revertdate.toString();
		else this.latestRecieviedDate="";
		if(forwardeddate!=null) this.latestforwardedDate = forwardeddate.toString();
		else this.latestforwardedDate = "";
		this.invoiceDate = invoiceDate.toString();
		this.paymentdetails = paymentdetails;
		this.status = status;
		this.deliveryPlant = deliveryPlant;
		this.mobileNumber = mobileNumber;
		this.eic = eic;
		this.paymentType = paymentType;
		this.invoiceurl = invoiceurl;
		this.invoiceAmount = invoiceAmount;
	}

}
