package com.application.main.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.application.main.Inboxmodel.InvoicesHistory;
import com.application.main.Inboxmodel.InvoicesHistoryCollection;
import com.application.main.InboxmodelRepository.InvoiceHistoryRepository;
import com.application.main.NotificationService.VendorPortalNotification;
import com.application.main.Repositories.InvoiceRepository;
import com.application.main.Repositories.LoginUserRepository;
import com.application.main.awsconfig.AwsService;
import com.application.main.credentialmodel.UserDTO;
import com.application.main.model.Invoice;
import com.application.main.model.InvoiceDTO;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/call/vendor/Vendorportal")

public class InboxController {

	@Autowired
	InvoiceRepository invoiceRepository;

	@Autowired
	LoginUserRepository loginrepository;

	@Autowired
	AwsService s3service;

	@Autowired
	InvoiceHistoryRepository invhistoryrepo;

	@Autowired
	private KafkaTemplate<String, Invoice> kafkaInvoice;
	KafkaTemplate<String, VendorPortalNotification> notificationtemplate;

	@SuppressWarnings("unused")
	private final MongoTemplate mongoTemplate;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public InboxController(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@PostMapping("/forwardinvoice")
	public ResponseEntity<?> forwardInvoice(@RequestHeader("id") String id,
			@RequestParam(value = "remarks") String remarks,
			@RequestParam(value = "fileinvoice", required = false) MultipartFile fileinvoice,
			HttpServletRequest request) throws IOException, Exception {

		System.out.println("--- Forwarding Initiation -------");
		System.out.println(remarks);
		System.out.println("------------------------------");
		String status = "Sent";
		// Retrieve the invoice from the database using the invoiceId
		Optional<Invoice> invoiceOptional = invoiceRepository.findById(id);

		if (invoiceOptional.isEmpty() || !invoiceOptional.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		Invoice invoice = invoiceOptional.get();
		// Revert the invoice to the original user and update roleName with username
		try {
			String token = request.getHeader("Authorization").replace("Bearer ", "");
			String username = s3service.getUserNameFromToken(token);
			s3service.createBucket(token, username + "history");
			String url = invoice.getInvoiceurl();
			String fileName = invoice.getInvoiceNumber();
			if (fileinvoice != null) {
				fileName = fileinvoice.getOriginalFilename();
				url = s3service.uploadFile(token, fileinvoice, invoice.getInvoiceNumber(), username).getUrl();
			}
			Optional<UserDTO> user = loginrepository.findByUsername(username);

			InvoicesHistory historyinvoice = new InvoicesHistory(fileName, url, invoice.getInvoiceNumber(), remarks);
			InvoicesHistoryCollection ihc = new InvoicesHistoryCollection(id, invoice.getInvoiceNumber(),
					LocalDateTime.now(), null);
			ihc.setDatetimeofHistory(LocalDateTime.now());
			String notificationmessage = "Invoice status referenced with " + invoice.getInvoiceNumber()
					+ " has beem changed !";
			VendorPortalNotification vendornotification = new VendorPortalNotification(null, invoice.getEic(),
					LocalDateTime.now(), invoice.getInvoiceNumber(), fileName, username, notificationmessage, "unread",
					null);
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
			invoice.setReciever(invoice.getSender());
			ihc.setSentto(ihc.getRecievedfrom());
			ihc.setRecievedfrom(username);
			invoice.setSender(username);
			historyinvoice.setStatus(status);
			invoice.setStatus(status);
			ihc.setInvoicehistory(historyinvoice);
			ihc = invhistoryrepo.save(ihc);
			notificationtemplate.send("vendorportalnotification", vendornotification);
			invoice.setStatus(status);
			ArrayList<String> sentrevertlist = invoice.getSentrevertidlist();
			if (sentrevertlist == null || sentrevertlist.size() == 0)
				sentrevertlist = new ArrayList<>();
			sentrevertlist.add(ihc.getId());
			invoice.setSentrevertidlist(sentrevertlist);
			kafkaInvoice.send("Invoiceinbox", invoice);

			if (remarks != null && !remarks.trim().isEmpty()) {
				Set<String> existingremarks = invoice.getRemarks();
				if (existingremarks.isEmpty() || existingremarks == null)
					invoice.setRemarks(Set.of(remarks));
				existingremarks.add(remarks);
				invoice.setRemarks(existingremarks);
			}

			invoice.setSentCount(invoice.getSentCount() + 1);
//			Invoice updatedInvoice =
					invoiceRepository.save(invoice);
			return ResponseEntity.ok("Sucessfully Forwarded ");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(e);
		}
	}

	@GetMapping("/Inbox/History")
	public ResponseEntity<?> getHistory(@RequestParam(value = "invoiceNumber") String invoiceNumber,
			@RequestHeader("id") String id) {

		List<InvoicesHistoryCollection> invoicesretrieved = invhistoryrepo
				.findByInvoicenumberOrderByForwardRevertDateDesc(invoiceNumber);
//		List<InvoicesHistory> invhistorylist1 = sentInvoices.stream().map(InvoicesHistoryCollection::getInvoicehistory) // Extract
//				.collect(Collectors.toList());
		Map<String, Object> response = new HashMap<>();

		response.put("history", invoicesretrieved);
		if (response.isEmpty() || response == null)
			return ResponseEntity.ok("");
		return ResponseEntity.ok(response);
	}

	@GetMapping("/InboxData")
	public ResponseEntity<Page<InvoiceDTO>> getInvoicesByUsernameAndStatus(@RequestParam("username") String username,
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);

		// Retrieve invoices with status "reverted"
		Page<InvoiceDTO> invoicepage = invoiceRepository.findByUsernameAndStatusIgnoreCase(username, "Reverted",
				pageable);
		if (!invoicepage.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK).body(invoicepage);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Page.empty());
		}
	}
}
