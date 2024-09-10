package com.application.main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
	private String invoiceTime;
	
	public void setTime(LocalDate date) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		this.invoiceDate = date.format(dateFormatter);
		this.invoiceTime = date.format(timeFormatter);
	}
	
	public void setDatetimeofHistory(LocalDateTime isoDate , boolean isSent) {
		try {
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
			if(isSent) {
			this.latestforwardDate = isoDate.format(dateFormatter);
			this.latestforwardTime = isoDate.format(timeFormatter);
			}
			else {
				this.latestRecievingDate = isoDate.format(dateFormatter);
				this.latestRecievedTime = isoDate.format(timeFormatter);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public InvoiceDTO(String invoiceid, String poNumber, String invoiceNumber , LocalDate invoiceDate,
			String status, String deliveryPlant, String mobileNumber, String eic,Paymentbreakup paymentdetails, String paymentType, String invoiceurl,
			String invoiceAmount,LocalDateTime revertdate , LocalDateTime forwardeddate) {
		super();
		this.id = invoiceid;
		this.poNumber = poNumber;
		this.invoiceNumber = invoiceNumber;
		if(revertdate!=null) setDatetimeofHistory(revertdate, false);
		else this.latestRecievingDate="";
		if(forwardeddate!=null) setDatetimeofHistory(forwardeddate, true);
		else this.latestforwardDate=invoiceDate.toString();
		setTime(invoiceDate);
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
