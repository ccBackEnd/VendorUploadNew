package com.application.main.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.application.main.Repositories.DocumentDetailsRepository;
import com.application.main.Repositories.InvoiceRepository;
import com.application.main.Repositories.LoginUserRepository;
import com.application.main.Repositories.PoSummaryRepository;
import com.application.main.Repositories.NotificationRepository.NotificationRepository;
import com.application.main.model.DocumentDetails;
import com.application.main.model.Invoice;
import com.application.main.model.PoSummary;
import com.application.main.model.StatusHistory;
import com.application.main.model.NotificationModel.VendorPortalNotification;

@Service
public class InvoiceService {

	Logger logApp = LoggerFactory.getLogger(InvoiceService.class);

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	@Autowired
	InvoiceRepository invoicerepository;

	@Autowired
	LoginUserRepository loginrepo;

	@Autowired
	PoSummaryRepository porepo;

	@Autowired
	DocumentDetailsRepository documentDetailsRepository;

	@Autowired
	NotificationRepository notificationRepository;

	KafkaTemplate<String, Invoice> ktemplate;
	KafkaTemplate<String, VendorPortalNotification> notificationtemplate;

	public ResponseEntity<?> createInvoice(String username, String msmecategory, String poNumber, String paymentType,
			String deliveryPlant, String invoiceDate, String invoiceNumber, String invoiceAmount, String mobileNumber,
			String email, String alternateMobileNumber, String alternateEmail, String remarks, String ses,
			boolean isagainstLC, boolean isGst, boolean isTredExchangePayment, String factoryunitnumber,
			boolean isMDCCPayment, String mdccnumber, String sellerGst, String buyerGst, String bankaccountno,
			DocumentDetails invoicedetails, List<DocumentDetails> suppDocNameList) throws IOException {
		System.out.println("Invoice creation Initiated");
		Invoice invoice = new Invoice();
		invoice.setPoNumber(poNumber);

		StatusHistory statushistory = new StatusHistory(LocalDateTime.now(), "Sent", "Created and Sent Initially");
		List<StatusHistory> statuslist = new ArrayList<>();
		statuslist.add(statushistory);
		invoice.setStatushistory(statuslist);
		invoice.setStatus("Sent");

		invoice.setInvoiceurl(invoicedetails.getUrl());
		invoice.setEic(porepo.findByPoNumber(poNumber).get().getEic());
		invoice.setDeliveryPlant(deliveryPlant);
		// INVOICE SENDER AND RECIEVER
		invoice.setSender(username);
		invoice.setReciever(invoice.getEic());

		List<DocumentDetails> invoiceobjectaslist = new ArrayList<>();
		if (invoicedetails != null)
			invoiceobjectaslist.add(invoicedetails);
		invoice.setInvoiceFile(invoiceobjectaslist);
		invoice.setSupportingDocument(suppDocNameList);
		invoice.setAlternateMobileNumber(alternateMobileNumber);
		invoice.setAlternateEmail(alternateEmail);

		Set<String> remarksset = new HashSet<String>();
		remarksset.add(remarks);
		invoice.setRemarks(remarksset);
		invoice.setInvoiceAmount(invoiceAmount);

		// Invoice DATE
		System.out.println("------- Invoice referenced with this invoice date :-  " + invoiceDate);
		LocalDate date = LocalDate.parse(invoiceDate, formatter);
		invoice.setInvoiceDate(date);
		invoice.setInvoiceNumber(invoiceNumber);
		invoice.setCurrentDateTime(LocalDateTime.now());
		System.err.println("-------------Invoice with : " + invoiceNumber + " Saved Successfully-------------------");

		List<String> usernamelist = new ArrayList<>();
		usernamelist.add(username);
		String eicusername = loginrepo.findByUsername(invoice.getEic()).get().getUsername();
		usernamelist.add(eicusername);
		invoice.setUsername(usernamelist);
		invoice = invoicerepository.save(invoice);
		updatepurchaseorder(poNumber, invoice);

		System.out.println("Invoice with details :-> \n" + invoice.toString() + " is saved succesfully");
		String notificationmessage = "Invoice with " + invoiceNumber + " & with amount = " + invoiceAmount
				+ " is generated";
		VendorPortalNotification vendornotification = new VendorPortalNotification(null, invoice.getEic(),
				LocalDateTime.now(), invoiceNumber, invoicedetails.getName(), username, notificationmessage, "unread",
				null);
		System.out.println("Notification Initiated...");
		vendornotification = notificationRepository.save(vendornotification);
		logApp.info("Notification Sending...");
		notificationtemplate.send("vendorportalnotification", vendornotification);
		logApp.info("Notification Sent Properly...");
		Map<String, Object> responseData = new HashMap<>();
		System.err.println("---------------------------------");
		Set<String> remarksSet = new HashSet<>();
		remarksSet.addAll(remarksset);
		responseData.put("Remarks", remarksSet);
		responseData.put("InvoiceAmount", invoiceAmount);
		responseData.put("created_for_Username", username);
		responseData.put("deliveryPlant", deliveryPlant);
		responseData.put("InvoiceNumber", invoiceNumber);
		ktemplate.send("Invoiceinbox", invoice);

		return ResponseEntity.ok(responseData);
	}

	private void updatepurchaseorder(String poNumber, Invoice invoice) {
		try {
			Optional<PoSummary> po = porepo.findByPoNumber(poNumber);
			Map<String, String> invoicemap = new HashMap<>();
			if (po.isPresent()) {
				logApp.info("Fetching PurchaseOrder Against Invoice with poNumber : " + poNumber);
				PoSummary poObject = po.get();
				if (poObject.getInvoiceidlist() != null && !poObject.getInvoiceidlist().isEmpty()) {
					invoicemap = poObject.getInvoiceidlist();
				}
					invoicemap.put(invoice.getId(), invoice.getInvoiceNumber());
					poObject.setInvoiceidlist(invoicemap);
					poObject.setNoOfInvoices(poObject.getNoOfInvoices() + 1);
				System.out.println("No. of Invoices in Referenced Po with poNumber : " + poNumber + " is : "
						+ poObject.getNoOfInvoices());
				porepo.save(poObject);
			} else {
				logApp.info("PURCHASE ORDER NOT FOUND...");
			}
//			return ResponseEntity.ok(Map.of("Error Found , PO is Not found or Not accesible", HttpStatus.SC_CONFLICT));
		} catch (Exception e) {
			throw e;
		}
	}
}
