package com.application.main.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "InvoiceInboxHistory")
public class InvoiceHistoryDatabase {
	@Id
	private String id;
	
	private String invoiceid;
	private String invoicenumber;
	private List<InvoicesHistory> invoicehistory;

}
