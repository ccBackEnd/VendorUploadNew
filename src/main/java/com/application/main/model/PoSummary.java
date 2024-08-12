package com.application.main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.application.main.URLCredentialModel.DocDetails;
import com.fasterxml.jackson.annotation.JsonFormat;

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
	private int noOfInvoices;
	private String deliveryTimelines;
	private String deliveryPlant;
	private String eic;
	private String receiver;
	// private List<String> supportingDocument;
	private List<Invoice> invoiceobject;
	private String username;
	private String type;
	private String url;
	private DocDetails doc;
	
	public DocDetails getDoc() {
		return doc;
	}

	public void setDoc(DocDetails doc) {
		this.doc = doc;
	}

	LocalDateTime currentDateTime;
	private String invoiceNumber;
	@JsonFormat(pattern = "dd-MM-yyyy")
	private LocalDate invoiceDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPoNumber() {
		return poNumber;
	}

	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPoStatus() {
		return poStatus;
	}

	public void setPoStatus(String poStatus) {
		this.poStatus = poStatus;
	}

	public String getPoAmount() {
		return poAmount;
	}

	public void setPoAmount(String poAmount) {
		this.poAmount = poAmount;
	}

	public int getNoOfInvoices() {
		return noOfInvoices;
	}

	public void setNoOfInvoices(int noOfInvoices) {
		this.noOfInvoices = noOfInvoices;
	}

	public List<Invoice> getInvoiceobject() {
		return invoiceobject;
	}

	public void setInvoiceobject(List<Invoice> invoiceobject) {
		this.invoiceobject = invoiceobject;
	}

	public String getDeliveryTimelines() {
		return deliveryTimelines;
	}

	public void setDeliveryTimelines(String deliveryTimelines) {
		this.deliveryTimelines = deliveryTimelines;
	}

	public String getDeliveryPlant() {
		return deliveryPlant;
	}

	public void setDeliveryPlant(String deliveryPlant) {
		this.deliveryPlant = deliveryPlant;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getEic() {
		return eic;
	}

	public void setEic(String eic) {
		this.eic = eic;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LocalDateTime getCurrentDateTime() {
		return currentDateTime;
	}

	public void setCurrentDateTime(LocalDateTime currentDateTime) {
		this.currentDateTime = currentDateTime;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public PoSummary() {
	}

	public LocalDate getPoIssueDate() {
		return poIssueDate;
	}

	public void setPoIssueDate(LocalDate poIssueDate) {
		this.poIssueDate = poIssueDate;
	}

	public LocalDate getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(LocalDate deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public LocalDate getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(LocalDate invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public PoSummary(String poNumber, String deliveryTimelines, String username) {
		this.poNumber = poNumber;
		this.deliveryTimelines = deliveryTimelines;
		this.username = username;
	}

	public PoSummary(String poNumber, String description, LocalDate poIssueDate, LocalDate deliveryDate,
			String deliveryPlant, String deliveryTimelines, int noOfInvoices, String eic,
			 String poAmount, String receiver, String username, String savedurl) {
		// TODO Auto-generated constructor stub
		this.poNumber = poNumber;
		this.description = description;
		this.poIssueDate = poIssueDate;
		this.deliveryDate = deliveryDate;
		this.poStatus = "New Purchase Order : Pending Status";
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