package com.application.main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;

import com.application.main.Repositories.PaymentRepository.PaymentDetailsRepository;
import com.application.main.model.PaymentDetailsModel.Paymentbreakup;
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
	private String latestforwardDate;
	private String latestforwardTime;
	
	private String latestRecievingDate;
	private String latestRecievedTime;
	private String status;
	private String deliveryPlant;
	private String mobileNumber;
	private Paymentbreakup paymentdetails;
	private String eic;
	private String paymentType;
	private String invoiceurl;
	private String invoiceAmount;
	
	final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	
//	public InvoiceDTO(String invoiceid, String poNumber, String invoiceNumber , LocalDate invoiceDate,
//			String status, String deliveryPlant, String mobileNumber, String eic,Paymentbreakup paymentdetails, String paymentType, String invoiceurl,
//			String invoiceAmount,LocalDateTime revertdate , LocalDateTime forwardeddate ) {
//		super();
//		this.id = invoiceid;
//		this.poNumber = poNumber;
//		this.invoiceNumber = invoiceNumber;
//		if(revertdate!=null) setDatetimeofHistory(revertdate, false);
//		else this.latestRecievingDate="";
//		if(forwardeddate!=null) setDatetimeofHistory(forwardeddate, true);
//		else this.latestforwardDate=invoiceDate.toString();
//		setTime(invoiceDate);
//		this.paymentdetails = paymentdetails;
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
			String invoiceAmount,LocalDateTime revertdate , LocalDateTime forwardeddate , String forwardtime,String recievingtime) {
		super();
		this.id = invoiceid;
		this.poNumber = poNumber;
		this.invoiceNumber = invoiceNumber;
		
		if(forwardeddate!=null)this.latestforwardDate = forwardeddate.format(dateFormatter);
		this.latestforwardTime = forwardtime;
		if(revertdate!=null)this.latestRecievingDate = revertdate.format(dateFormatter);
		this.latestRecievedTime = recievingtime;
		if(invoiceDate!=null)this.invoiceDate = invoiceDate.format(dateFormatter);
//		this.invoiceTime = invoiceDate.format(timeFormatter);
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
