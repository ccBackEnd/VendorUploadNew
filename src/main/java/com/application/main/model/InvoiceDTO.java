package com.application.main.model;

import java.time.LocalDate;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;

import com.fasterxml.jackson.annotation.JsonFormat;

public class InvoiceDTO extends Pageable {
	
		
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		private String poNumber;
		private String invoiceNumber;
		private String deliveryTimelines;
		@JsonFormat(pattern = "yyyy-MM-dd")
		private String invoiceDate;
		private String status;
		private String deliveryPlant;
	    private String mobileNumber;
	    private String eic;
	    private String paymentType;
	    private String invoiceurl;
	    private String invoiceAmount;
	    
	    
		public String getInvoiceDate() {
			return invoiceDate;
		}
		public void setInvoiceDate(String invoiceDate) {
			this.invoiceDate = invoiceDate;
		}
		public String getInvoiceAmount() {
			return invoiceAmount;
		}
		public void setInvoiceAmount(String invoiceAmount) {
			this.invoiceAmount = invoiceAmount;
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
		public String getPaymentType() {
			return paymentType;
		}
		public void setPaymentType(String paymentType) {
			this.paymentType = paymentType;
		}
		
		public InvoiceDTO(String poNumber, String deliveryTimelines, LocalDate invoicedate,
				String invoiceAmount ,
				 String deliveryPlant, String mobileNumber, String eic, String paymentType) {
			super();
			this.poNumber = poNumber;
			this.deliveryTimelines = deliveryTimelines;
			this.invoiceDate = invoicedate.toString();
			this.deliveryPlant = deliveryPlant;
			this.mobileNumber = mobileNumber;
			this.invoiceAmount = invoiceAmount;
			this.eic = eic;
			this.paymentType = paymentType;
		}
		
		public String getInvoiceNumber() {
			return invoiceNumber;
		}
		public void setInvoiceNumber(String invoiceNumber) {
			this.invoiceNumber = invoiceNumber;
		}
		public InvoiceDTO() {
			super();
			// TODO Auto-generated constructor stub
		}
		
		
	    
	    
			
}


