package com.application.main.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.application.main.Repositories.InvoiceRepository;
import com.application.main.Repositories.LoginUserRepository;
import com.application.main.Repositories.InboxRepository.InvoiceHistoryRepository;
import com.application.main.model.Invoice;
import com.application.main.model.StatusHistory;
import com.application.main.model.InboxModel.InvoicesHistory;
import com.application.main.model.InboxModel.InvoicesHistoryCollection;
import com.application.main.model.NotificationModel.VendorPortalNotification;
import com.application.main.model.UserModel.UserDetails;

import jakarta.servlet.http.HttpServletRequest;
@Service
public class InboxService {

	@Autowired
	FileUploadService s3service;

	@Autowired
	InvoiceRepository invoiceRepository;

	@Autowired
	LoginUserRepository loginrepository;

	@Autowired
	InvoiceHistoryRepository invoiceHistoryRepository;
	@Autowired
	private KafkaTemplate<String, Invoice> kafkaInvoice;

	@Autowired
	KafkaTemplate<String, VendorPortalNotification> notificationtemplate;
	private String id;
	private String invoiceNumber;
	private String fileName;
	private String username;

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public ResponseEntity<?> forwardinvoice(String id, String status, HttpServletRequest request,
			MultipartFile fileinvoice, String remarks) {
		System.out.println("--- Forwarding Initiation -------");
		System.out.println("Remarks : " + remarks);
		Optional<Invoice> invoiceOptional = invoiceRepository.findById(id);

		if (invoiceOptional.isEmpty() || !invoiceOptional.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		Invoice invoice = invoiceOptional.get();
		this.invoiceNumber = invoice.getInvoiceNumber();
		this.id = id;

		// Revert the invoice to the original user and update roleName with username
		try {
			String token = request.getHeader("Authorization").replace("Bearer ", "");
			this.username = s3service.getUserNameFromToken(token);
			s3service.createBucket(token, username + "history");
			String url = invoice.getInvoiceurl();
			String fileName = invoiceNumber;
			if (fileinvoice != null) {
				fileName = fileinvoice.getOriginalFilename();
				url = s3service.uploadFile(token, fileinvoice, invoiceNumber, username).getUrl();
			}
			this.fileName = fileName;

			InvoicesHistory historyinvoice = new InvoicesHistory(fileName, url, invoiceNumber, remarks);
			InvoicesHistoryCollection ihc = new InvoicesHistoryCollection(id, invoiceNumber, LocalDateTime.now(), null);
			ihc.setDatetimeofHistory(LocalDateTime.now());
			sendNotificationandStatus(ihc, historyinvoice, invoice, status, remarks);

//			Invoice updatedInvoice =
			invoiceRepository.save(invoice);
			return ResponseEntity.ok("Sucessfully Forwarded ");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(e);
		}
	}

	private boolean sendNotificationandStatus(InvoicesHistoryCollection ihc, InvoicesHistory historyinvoice,
			Invoice invoice, String status, String remarks) {

		Optional<UserDetails> user = loginrepository.findByUsername(username);
		String notificationmessage = "Invoice status referenced with " + invoiceNumber + " has beem changed !";
		VendorPortalNotification vendornotification = new VendorPortalNotification(null, invoice.getReciever(),
				LocalDateTime.now(), invoiceNumber, fileName, invoice.getSender(), notificationmessage, "unread", null);
		if (user.get().isEic() == true) {
			ihc.setSent(false);
			ihc.setRevert(true);
			status = "Reverted";
			invoice.setRevertCount(invoice.getRevertCount() + 1);
			invoice.setDatetimeofHistory(LocalDateTime.now(), false);
		} else {
			ihc.setSent(true);
			ihc.setRevert(false);
			invoice.setSentCount(invoice.getSentCount() + 1);
			invoice.setDatetimeofHistory(LocalDateTime.now(), true);
		}
		StatusHistory sh = new StatusHistory(LocalDateTime.now(), status, remarks);
		List<StatusHistory> statushistorylist = invoice.getStatushistory();
		statushistorylist.add(sh);
		invoice.setStatushistory(statushistorylist);
		ihc.setSentto(ihc.getRecievedfrom());
		ihc.setRecievedfrom(username);
		invoice.setSender(username);
		invoice.setReciever(invoice.getSender());
		historyinvoice.setStatus(status);
		invoice.setStatus(status);
		ihc.setInvoicehistory(historyinvoice);
		ihc = invoiceHistoryRepository.save(ihc);
		notificationtemplate.send("vendorportalnotification", vendornotification);
		invoice.setStatus(status);
		ArrayList<String> sentrevertlist = invoice.getSentrevertidlist();
		if (sentrevertlist == null || sentrevertlist.size() == 0)
			sentrevertlist = new ArrayList<>();
		sentrevertlist.add(ihc.getId());
		invoice.setSentrevertidlist(sentrevertlist);
		CompletableFuture<SendResult<String, Invoice>> s = kafkaInvoice.send("Invoiceinbox", invoice);
		if (!s.isDone())
			return false;

		if (remarks != null && !remarks.trim().isEmpty()) {
			Set<String> existingremarks = new HashSet<>();
			existingremarks = invoice.getRemarks();
			existingremarks.add(remarks);
			invoice.setRemarks(existingremarks);
		}
		invoice.setSentCount(invoice.getSentCount() + 1);
		return true;
	}

	public ResponseEntity<?> retrivehistory(String invoiceNumber, String id) {
		this.invoiceNumber = invoiceNumber;
		this.id = id;
		List<InvoicesHistoryCollection> invoicesretrieved = invoiceHistoryRepository
				.findByInvoicenumberOrderByForwardRevertDate(invoiceNumber);
		Map<String, Object> response = new HashMap<>();
		response.put("history", invoicesretrieved);
		if (response.get("history") == null)
			return ResponseEntity.ok("");
		else
			return ResponseEntity.ok(response);
	}
	
	private boolean canTransition(String currentStatus, String newStatus) {
        Map<String, List<String>> validTransitions = new HashMap<>();
        validTransitions.put("Sent", Arrays.asList("On Hold", "Approved"));
        validTransitions.put("On Hold", Arrays.asList("Approved", "Sent"));
        validTransitions.put("Approved", Arrays.asList("On Hold", "Sent"));

        return validTransitions.containsKey(currentStatus) && 
               validTransitions.get(currentStatus).contains(newStatus);
    }
}