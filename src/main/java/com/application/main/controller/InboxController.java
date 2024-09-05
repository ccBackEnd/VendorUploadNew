package com.application.main.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.application.main.Inboxmodel.InvoicesHistory;
import com.application.main.Inboxmodel.RecieveInvoiceDatabaseModel;
import com.application.main.Inboxmodel.SentInvoicesDatabaseModel;
import com.application.main.InboxmodelRepository.RecievedInvoiceRepository;
import com.application.main.InboxmodelRepository.SentInvoicesRepository;
import com.application.main.Repositories.InvoiceRepository;
import com.application.main.awsconfig.AwsService;
import com.application.main.model.Invoice;
import com.application.main.model.InvoiceDTO;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/call/vendor/Vendorportal")

public class InboxController {

	@Autowired
	InvoiceRepository invoiceRepository;

	@Autowired
	AwsService s3service;

	@Autowired
	SentInvoicesRepository sentinvrepo;

	@Autowired
	RecievedInvoiceRepository recieveinvrepo;

	@Autowired
	private KafkaTemplate<String, Invoice> kafkaInvoice;

	private final MongoTemplate mongoTemplate;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public InboxController(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@PostMapping("/forwardinvoice")
	public ResponseEntity<?> forwardInvoice(@RequestHeader("id") String id, @RequestParam("remarks") String remarks,
			@RequestParam(value = "fileinvoice", required = false) MultipartFile fileinvoice,
			HttpServletRequest request) throws IOException, Exception {

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
			if (fileinvoice != null) {
				url = s3service.uploadFile(token, fileinvoice, invoice.getInvoiceNumber(), username).getUrl();
			}
			InvoicesHistory historyinvoice = new InvoicesHistory(id, url, "forwarded", invoice.getInvoiceNumber(),
					LocalDate.now().toString(), remarks);
			SentInvoicesDatabaseModel sidm = new SentInvoicesDatabaseModel(null, id, invoice.getInvoiceNumber(),
					historyinvoice);
			sidm = sentinvrepo.save(sidm);
			invoice.setStatus("sent");
			invoice.setLatestforwardDate(LocalDate.now());
			LinkedHashMap<String, String> map = invoice.getSentinvoicesidlist();
			if (map == null)
				map = new LinkedHashMap<String, String>();
			map.put(LocalDateTime.now().toString(), sidm.getId());
			invoice.setSentinvoicesidlist(map);
			kafkaInvoice.send("ForwardedInvoice", invoice);

			if (remarks != null && !remarks.trim().isEmpty()) {
				Set<String> existingremarks = invoice.getRemarks();
				if (existingremarks.isEmpty() || existingremarks == null)
					invoice.setRemarks(Set.of(remarks));
				existingremarks.add(remarks);
				invoice.setRemarks(existingremarks);
			}

			invoice.setSentCount(invoice.getSentCount() + 1);
			Invoice updatedInvoice = invoiceRepository.save(invoice);
			return ResponseEntity.ok("Sucessfully Forwarded ");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(e);
		}
	}

	@PostMapping("/revert")
	public ResponseEntity<?> revertInvoice(@RequestParam("id") String id,
			@RequestParam(value = "remarks", required = false) String remarks,
			@RequestParam(value = "fileinvoice", required = false) MultipartFile fileinvoice,
			HttpServletRequest request) throws IOException, Exception {

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
			// Retrieve the invoice from the database using the invoiceId
			s3service.createBucket(token, "reverthistory");
			String url = invoice.getInvoiceurl();
			if (fileinvoice != null) {
				url = s3service.uploadFile(token, fileinvoice, invoice.getInvoiceNumber(), username).getUrl();
			}
			InvoicesHistory historyinvoice = new InvoicesHistory(id, url, "reverted", invoice.getInvoiceNumber(),
					LocalDate.now().toString(), remarks);
			RecieveInvoiceDatabaseModel ridm = new RecieveInvoiceDatabaseModel(null, id, invoice.getInvoiceNumber(),
					historyinvoice);
			ridm = recieveinvrepo.save(ridm);
			invoice.setStatus("Reverted");
			invoice.setLatestRecievingDate(LocalDate.now());

			LinkedHashMap<String, String> map = invoice.getRecieveinvoicesidlist();
			if (map == null)
				map = new LinkedHashMap<String, String>();
			map.put(LocalDateTime.now().toString(), ridm.getId());
			invoice.setRecieveinvoicesidlist(map);

			if (remarks != null && !remarks.trim().isEmpty()) {
				Set<String> existingremarks = invoice.getRemarks();
				if (existingremarks.isEmpty() || existingremarks == null)
					invoice.setRemarks(Set.of(remarks));
				existingremarks.add(remarks);
				invoice.setRemarks(existingremarks);
			}
			invoice.setRevertCount(invoice.getRevertCount() + 1);
			kafkaInvoice.send("RevertedInvoice", invoice);
			Invoice revertedInvoice = invoiceRepository.save(invoice);
			return ResponseEntity.ok("Reverted Successfully");
		} catch (Exception e) {
			e.printStackTrace(); // Log the exception
			return ResponseEntity.ok(e);
		}
	}

	@GetMapping("/Inbox/History/{invoiceNumber}")
	public ResponseEntity<?> getHistory(@PathVariable String invoiceNumber, @RequestHeader("id") String id) {

		List<SentInvoicesDatabaseModel> sentInvoices = sentinvrepo.findByInvoicenumber(invoiceNumber);
		List<InvoicesHistory> invhistorylist1 = sentInvoices.stream().map(SentInvoicesDatabaseModel::getSentinvoice) // Extract
																														// sentinvoice
																														// field
				.collect(Collectors.toList());

		List<RecieveInvoiceDatabaseModel> recievedInvoices = recieveinvrepo.findByInvoicenumber(invoiceNumber);
		List<InvoicesHistory> invhistorylist2 = recievedInvoices.stream()
				.map(RecieveInvoiceDatabaseModel::getRecievedinvoices) // Extract sentinvoice field
				.collect(Collectors.toList());
		Map<String, Object> response = new HashMap<>();

		response.put("sentinvoices", invhistorylist1);
		response.put("recievedinvoices", invhistorylist2);
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
		Page<InvoiceDTO> invoicepage = invoiceRepository.findByUsernameAndStatus(username, "Reverted", pageable);
		if (!invoicepage.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK).body(invoicepage);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Page.empty());
		}
	}
}
