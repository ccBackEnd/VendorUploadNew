package com.application.main.Inboxmodel;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "InvoicesHistory")
public class InvoicesHistoryCollection {
	@Id
	private String id;
	private String invoiceid;
	private String invoicenumber;
	@Default
	private boolean isSent = true;
	@Default
	private boolean isRevert = false;
	private LocalDateTime forwardRevertDate;
	private String timeofarrival;
	private String dateofarrival;
	private InvoicesHistory invoicehistory;

	public void setDatetimeofHistory(String isoDate) {
		try {
			ZonedDateTime zdt = ZonedDateTime.parse(isoDate, DateTimeFormatter.ISO_DATE_TIME);
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
			this.dateofarrival = zdt.format(dateFormatter);
			this.timeofarrival = zdt.format(timeFormatter);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public InvoicesHistoryCollection(String invoiceid, String invoicenumber, LocalDateTime forwardRevertDate,
			InvoicesHistory invoicehistory) {
		super();
		this.invoiceid = invoiceid;
		this.invoicenumber = invoicenumber;
		this.forwardRevertDate = forwardRevertDate;
		this.invoicehistory = invoicehistory;
		setDatetimeofHistory(forwardRevertDate.toString());
	}

}
