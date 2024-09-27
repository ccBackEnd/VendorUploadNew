package com.application.main.model.InboxModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	private String sentto;
	private String recievedfrom;
	@Default
	private boolean isSent = true;
	@Default
	private boolean isRevert = false;
	private LocalDateTime forwardRevertDate;
	private String timeofarrival;
	private String dateofarrival;
	private InvoicesHistory invoicehistory;
	
	@Transient
	final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	@Transient
	final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	public void setDatetimeofHistory(LocalDateTime isoDate) {
		try {
			this.dateofarrival = isoDate.format(dateFormatter);
			this.timeofarrival = isoDate.format(timeFormatter);
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
	}

}
