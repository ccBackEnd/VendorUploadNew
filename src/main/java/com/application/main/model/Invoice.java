package com.application.main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.application.main.credentialmodel.DocDetails;
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
	private LinkedHashMap<LocalDateTime,String> recieveinvoicesidlist;
	private LinkedHashMap<LocalDateTime,String> sentinvoicesidlist;
	private LocalDate latestRecievingDate;
	private LocalDate latestforwardDate;
	@JsonFormat(pattern = "yyyy-mm-dd")
	private LocalDate invoiceDate;
	private LocalDateTime currentDateTime;

	private String invoiceNumber;
	@Default
	private String status = "new";
	private List<DocDetails> supportingDocument;
	private List<DocDetails> invoiceFile;
	private String invoiceAmount;
	private String mobileNumber;
	private String email;
	private String alternateMobileNumber;
	private String alternateEmail;
	private String username;
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
	
	@Default
	private int sentCount = 0;
	@Default
	private int revertCount =0;

	public Invoice(String deliveryPlant) {
		super();
		this.deliveryPlant = deliveryPlant;
	}

}
