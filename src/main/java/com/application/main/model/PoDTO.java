package com.application.main.model;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

public class PoDTO {

	@Id
	private String id;
//	@Size(min = 10, max = 10, message = "PO number or package number must be 10 digits")
	private String poNumber;
	private String description;
	@JsonFormat( pattern = "dd-MM-yyyy")
	private String poIssueDate;
	@JsonFormat( pattern = "dd-MM-yyyy")
	private String deliveryDate;
	private String poStatus;
	private String poAmount;
	private int noOfInvoices;
	private String deliveryTimelines;
	private Set<String> deliveryPlant;
	private String eic;
	private String receiver;
	private String url;

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

	public String getPoIssueDate() {
		return poIssueDate;
	}

	public void setPoIssueDate(String poIssueDate) {
		this.poIssueDate = poIssueDate;
	}

	public String getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(String deliveryDate) {
		this.deliveryDate = deliveryDate;
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

	public String getDeliveryTimelines() {
		return deliveryTimelines;
	}

	public void setDeliveryTimelines(String deliveryTimelines) {
		this.deliveryTimelines = deliveryTimelines;
	}

	public Set<String> getDeliveryPlant() {
		return deliveryPlant;
	}

	public void setDeliveryPlant(Set<String> deliveryPlant) {
		this.deliveryPlant = deliveryPlant;
	}

	public String getEic() {
		return eic;
	}

	public void setEic(String eic) {
		this.eic = eic;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public PoDTO(String id , String poNumber, String description, LocalDate poIssueDate, LocalDate deliveryDate, String poStatus,
			String poAmount, int noOfInvoices, String deliveryTimelines, Set<String> deliveryPlant,
			String eic, String receiver,String savedurl ) {
		super();
		this.id = id;
		this.poNumber = poNumber;
		this.description = description;
		this.poIssueDate = poIssueDate.toString();
		this.deliveryDate = deliveryDate.toString();
		this.poStatus = poStatus;
		this.poAmount = poAmount;
		this.noOfInvoices = noOfInvoices;
		this.deliveryTimelines = deliveryTimelines;
		
		this.deliveryPlant = deliveryPlant;
		this.eic = eic;
		this.receiver = receiver;
		this.setUrl(savedurl);
		System.out.println(url);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


	@Override
	public String toString() {
		return "PoDTO [id=" + id + ", poNumber=" + poNumber + ", description=" + description + ", poIssueDate="
				+ poIssueDate + ", deliveryDate=" + deliveryDate + ", poStatus=" + poStatus + ", poAmount=" + poAmount
				+ ", noOfInvoices=" + noOfInvoices + ", deliveryTimelines=" + deliveryTimelines + ", deliveryPlant="
				+ deliveryPlant + ", eic=" + eic + ", receiver=" + receiver
				+ ", url=" + url + "]";
	}
	

}
