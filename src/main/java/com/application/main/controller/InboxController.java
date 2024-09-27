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

import com.application.main.Repositories.InvoiceRepository;
import com.application.main.Repositories.LoginUserRepository;
import com.application.main.Repositories.InboxRepository.InvoiceHistoryRepository;
import com.application.main.Service.FileUploadService;
import com.application.main.Service.InboxService;
import com.application.main.model.Invoice;
import com.application.main.model.InvoiceDTO;
import com.application.main.model.InboxModel.InvoicesHistory;
import com.application.main.model.InboxModel.InvoicesHistoryCollection;
import com.application.main.model.NotificationModel.VendorPortalNotification;
import com.application.main.model.UserModel.UserDetails;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/call/vendor/Vendorportal")

public class InboxController {

	@Autowired
	InvoiceRepository invoiceRepository;

	@Autowired
	LoginUserRepository loginrepository;

	@Autowired
	FileUploadService s3service;

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

	
		// Retrieve the invoice from the database using the invoiceId
		return new InboxService().forwardinvoice(id, "Sent", request, fileinvoice , remarks);
		
	}

	@GetMapping("/Inbox/History")
	public ResponseEntity<?> getHistory(@RequestParam(value = "invoiceNumber") String invoiceNumber,
			@RequestHeader("id") String id) {
		return new InboxService().retrivehistory(invoiceNumber,id);
	}

	@GetMapping("/InboxData")
	public ResponseEntity<Page<InvoiceDTO>> getInvoicesByUsernameAndStatus(@RequestParam("username") String username,
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);

		// Retrieve invoices with status "reverted"
		Page<InvoiceDTO> invoicepage = invoiceRepository.findByUsernameAndStatusIgnoreCaseOrderByLatestRecievingDateDesc(username, "Reverted",
				pageable);
		if (!invoicepage.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK).body(invoicepage);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Page.empty());
		}
	}
}
