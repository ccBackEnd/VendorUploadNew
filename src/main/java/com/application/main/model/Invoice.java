package com.application.main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.application.main.URLCredentialModel.DocDetails;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Size;

@Document(collection = "Invoice")
public class Invoice {

	@Id

	private String id;
	@Size(min = 10, max = 10, message = "PO number or package number must be 10 digits")
	private String poNumber;
	private String termAndConditions;
	private String deliveryTimelines;
	@JsonFormat(pattern = "yyyy-mm-dd")
	private LocalDate validityDate;
	@JsonFormat(pattern = "yyyy-mm-dd")
	private LocalDate invoiceDate;
	private LocalDateTime currentDateTime;

	private String invoiceNumber;
	private String status;
	private String roleName;
	private List<DocDetails> supportingDocument;
	private List<DocDetails> invoiceFile;
	private String invoiceAmount;
	private String mobileNumber;
	private String email;
	private String deliveryPlant;
	private String username;
	private Set<String> remarks;
	private String searchItems;
	private String invoiceurl;
	private String alternateMobileNumber;
	private String alternateEmail;
	private String paymentType;
	private String receiver;
	private String claimedBy;
	private String eic;
	private String type;
	private boolean read = false;
	private String msmeCategory;
	private boolean claimed;

	private String createdBy;
	private String receievedBy;

	public Invoice(String deliveryPlant) {
		super();
		this.deliveryPlant = deliveryPlant;
	}

	public Invoice() {
		// TODO Auto-generated constructor stub
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInvoiceurl() {
		return invoiceurl;
	}

	public void setInvoiceurl(String invoiceurl) {
		this.invoiceurl = invoiceurl;
	}

	public String getPoNumber() {
		return poNumber;
	}

	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}

	public String getTermAndConditions() {
		return termAndConditions;
	}

	public void setTermAndConditions(String termAndConditions) {
		this.termAndConditions = termAndConditions;
	}

	public String getDeliveryTimelines() {
		return deliveryTimelines;
	}

	public void setDeliveryTimelines(String deliveryTimelines) {
		this.deliveryTimelines = deliveryTimelines;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public List<DocDetails> getSupportingDocument() {
		return supportingDocument;
	}

	public void setSupportingDocument(List<DocDetails> supportingDocument) {
		this.supportingDocument = supportingDocument;
	}

	public List<DocDetails> getInvoiceFile() {
		return invoiceFile;
	}

	public void setInvoiceFile(List<DocDetails> invoiceFile) {
		this.invoiceFile = invoiceFile;
	}

	public String getInvoiceAmount() {
		return invoiceAmount;
	}

	public void setInvoiceAmount(String invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public Set<String> getRemarks() {
		return remarks;
	}

	public void setRemarks(Set<String> remarks) {
		this.remarks = remarks;
	}

	public String getSearchItems() {
		return searchItems;
	}

	public void setSearchItems(String searchItems) {
		this.searchItems = searchItems;
	}

	public String getAlternateMobileNumber() {
		return alternateMobileNumber;
	}

	public void setAlternateMobileNumber(String alternateMobileNumber) {
		this.alternateMobileNumber = alternateMobileNumber;
	}

	public String getAlternateEmail() {
		return alternateEmail;
	}

	public void setAlternateEmail(String alternateEmail) {
		this.alternateEmail = alternateEmail;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getClaimedBy() {
		return claimedBy;
	}

	public void setClaimedBy(String claimedBy) {
		this.claimedBy = claimedBy;
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

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public String getMsmeCategory() {
		return msmeCategory;
	}

	public void setMsmeCategory(String msmeCategory) {
		this.msmeCategory = msmeCategory;
	}

	public boolean isClaimed() {
		return claimed;
	}

	public void setClaimed(boolean claimed) {
		this.claimed = claimed;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getReceievedBy() {
		return receievedBy;
	}

	public void setReceievedBy(String receievedBy) {
		this.receievedBy = receievedBy;
	}

	public LocalDate getValidityDate() {
		return validityDate;
	}

	public void setValidityDate(LocalDate validityDate) {
		this.validityDate = validityDate;
	}

	public LocalDate getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(LocalDate invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public LocalDateTime getCurrentDateTime() {
		return currentDateTime;
	}

	public void setCurrentDateTime(LocalDateTime currentDateTime) {
		this.currentDateTime = currentDateTime;
	}

	@Override
	public String toString() {
		return "Invoice [id=" + id + ", poNumber=" + poNumber + ", termAndConditions=" + termAndConditions
				+ ", deliveryTimelines=" + deliveryTimelines + ", validityDate=" + validityDate + ", invoiceNumber="
				+ invoiceNumber + ", invoiceDate=" + invoiceDate + ", status=" + status + ", roleName=" + roleName
				+ ", supportingDocument=" + supportingDocument + ", invoiceFile=" + invoiceFile + ", invoiceAmount="
				+ invoiceAmount + ", mobileNumber=" + mobileNumber + ", email=" + email + ", deliveryPlant="
				+ deliveryPlant + ", username=" + username + ", remarks=" + remarks + ", searchItems=" + searchItems
				+ ", alternateMobileNumber=" + alternateMobileNumber + ", alternateEmail=" + alternateEmail
				+ ", paymentType=" + paymentType + ", receiver=" + receiver + ", claimedBy=" + claimedBy + ", eic="
				+ eic + ", type=" + type + ", read=" + read + ", msmeCategory=" + msmeCategory + ", claimed=" + claimed
				+ ", createdBy=" + createdBy + ", receievedBy=" + receievedBy + ", currentDateTime=" + currentDateTime
				+ "]";
	}

}
