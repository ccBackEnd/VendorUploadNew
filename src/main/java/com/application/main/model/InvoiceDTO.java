package com.application.main.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class InvoiceDTO {
		private String poNumber;
//		private String termAndConditions;
		private String deliveryTimelines;
		@JsonFormat(pattern = "dd/MM/yyyy")
		private String invoicedate;
		
		private String claimedBy;
		private String roleName;
		private String deliveryPlant;
	    private String mobileNumber;
	    private String eic;
	    private String type;
	    private String msmeCategory;
	    private boolean claimed;
	    private String email;
	    private String paymentType;
	    private String invoiceurl;
	    
	    
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
		public String getDeliveryTimelines() {
			return deliveryTimelines;
		}
		public void setDeliveryTimelines(String deliveryTimelines) {
			this.deliveryTimelines = deliveryTimelines;
		}
		
		public String getInvoicedate() {
			return invoicedate;
		}
		public void setInvoicedate(String invoicedate) {
			this.invoicedate = invoicedate;
		}
		public String getClaimedBy() {
			return claimedBy;
		}
		public void setClaimedBy(String claimedBy) {
			this.claimedBy = claimedBy;
		}
		public String getRoleName() {
			return roleName;
		}
		public void setRoleName(String roleName) {
			this.roleName = roleName;
		}
		public String getDeliveryPlant() {
			return deliveryPlant;
		}
		public void setDeliveryPlant(String deliveryPlant) {
			this.deliveryPlant = deliveryPlant;
		}
		public String getMobileNumber() {
			return mobileNumber;
		}
		public void setMobileNumber(String mobileNumber) {
			this.mobileNumber = mobileNumber;
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
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getPaymentType() {
			return paymentType;
		}
		public void setPaymentType(String paymentType) {
			this.paymentType = paymentType;
		}
		public InvoiceDTO(String poNumber, String deliveryTimelines, LocalDate invoicedate, String claimedBy,
				String roleName, String deliveryPlant, String mobileNumber, String eic, String type,
				String msmeCategory, String paymentType) {
			super();
			this.poNumber = poNumber;
			this.deliveryTimelines = deliveryTimelines;
			this.invoicedate = invoicedate.toString();
			this.claimedBy = claimedBy;
			this.roleName = roleName;
			this.deliveryPlant = deliveryPlant;
			this.mobileNumber = mobileNumber;
			this.eic = eic;
			this.type = type;
			this.msmeCategory = msmeCategory;
			this.paymentType = paymentType;
		}
		public InvoiceDTO() {
			super();
			// TODO Auto-generated constructor stub
		}
		
		
	    
	    
			
}


