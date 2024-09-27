package com.application.main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "poSummary")
public class PoSummary {
	@Id
	private String id;
//	@Size(min = 10, max = 10, message = "PO number or package number must be 10 digits")
	private String poNumber;
	private String description;
	@JsonFormat(pattern = "yyyy-mm-dd")
	private LocalDate poIssueDate;
	@JsonFormat(pattern = "yyyy-mm-dd")
	private LocalDate deliveryDate;
	private String poStatus;
	private String poAmount;
	private String deliveryTimelines;
	private Set<String> deliveryPlant;
	private String eic;
	private String receiver;
	// private List<String> supportingDocument;
	Map<String , String> invoiceidlist;
	private int noOfInvoices;
	private String username;
	private String type;
	private String url;
	private DocumentDetails doc;
	private String msmecategoy;
	
	
	public DocumentDetails getDoc() {
		return doc;
	}

	public void setDoc(DocumentDetails doc) {
		this.doc = doc;
	}

	LocalDateTime currentDateTime;
	private String invoiceNumber;
	@JsonFormat(pattern = "dd-MM-yyyy")
	private LocalDate invoiceDate;

	

	public void setCurrentDateTime(LocalDateTime currentDateTime) {
		this.currentDateTime = currentDateTime;
	}

	public PoSummary(String poNumber, String deliveryTimelines, String username) {
		this.poNumber = poNumber;
		this.deliveryTimelines = deliveryTimelines;
		this.username = username;
	}

	public PoSummary(String poStatus ,String poNumber, String description, LocalDate poIssueDate, LocalDate deliveryDate,
			Set<String> deliveryPlant, String deliveryTimelines, int noOfInvoices, String eic,
			 String poAmount, String receiver, String username, String savedurl) {
		// TODO Auto-generated constructor stub
		this.poNumber = poNumber;
		this.description = description;
		this.poIssueDate = poIssueDate;
		this.deliveryDate = deliveryDate;
		this.poStatus = poStatus;
		this.poAmount = poAmount;
		this.noOfInvoices = noOfInvoices;
		this.deliveryTimelines = deliveryTimelines;
		this.deliveryPlant = deliveryPlant;
		this.eic = eic;
		this.receiver = receiver;
		this.url = savedurl;
		this.username = username;
		System.out.println(url);
	}
}