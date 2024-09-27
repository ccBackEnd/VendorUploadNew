package com.application.main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder

@Document(collection = "Invoice")
public class Invoice {
	
	

	@Id
	private String id;
	private String poNumber;
	private String paymentType;
	private String deliveryPlant;
	private ArrayList<String> sentrevertidlist;
	
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime latestforwardDate;
	private String latestforwardTime;
	
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime latestRecievingDate;
	private String latestRecievedTime;
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate invoiceDate;
	
	private String invoiceTime;
	
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime currentDateTime;

	private String invoiceNumber;
	@Default
	private String status = "new";
	private List<StatusHistory> statushistory;
	private List<DocumentDetails> supportingDocument;
	private List<DocumentDetails> invoiceFile;
	private String invoiceAmount;
	private String mobileNumber;
	private String email;
	private String alternateMobileNumber;
	private String alternateEmail;
	private List<String> username;
	private Set<String> remarks;
	private String ses;
	private String invoiceurl;
	private String msmeCategory;
	private String eic;
	@Default
	private boolean read = false;

	private boolean isagainstLC;
	private boolean isGst;
	private boolean isTredExchangePayment;
	private String factoryunitnumber;
	private boolean isMDCCPayment;
	private String mdccnumber;
	private String sellerGst;
	private String buyerGst;
	private String bankaccountno;
	private String sender;
	private String reciever;
	@Default
	private int sentCount = 0;
	@Default
	private int revertCount =0;

	public Invoice(String deliveryPlant) {
		super();
		this.deliveryPlant = deliveryPlant;
	}
	public void setDatetimeofHistory(LocalDateTime isoDate , boolean isSent) {
		try {
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
			if(isSent) {
			this.latestforwardDate = isoDate;
			this.latestforwardTime = isoDate.format(timeFormatter);
			}
			else {
				this.latestRecievingDate = isoDate;
				this.latestRecievedTime = isoDate.format(timeFormatter);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

}
