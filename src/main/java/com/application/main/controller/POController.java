package com.application.main.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.application.main.Repositories.DocumentRepository;
import com.application.main.Repositories.InvoiceRepository;
import com.application.main.Repositories.PoSummaryRepository;
import com.application.main.Repositories.UserRepository;
import com.application.main.awsconfig.AwsService;
import com.application.main.model.DocDetails;
import com.application.main.model.DocumentsMongo;
import com.application.main.model.Invoice;
import com.application.main.model.InvoiceDTO;
import com.application.main.model.PoDTO;
import com.application.main.model.PoSummary;
import com.application.main.model.UserClass;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/call/vendor/Vendorportal")
public class POController {

	@Autowired
	AwsService s3service;

	@Autowired
	PoSummaryRepository porepo;
	private final MongoTemplate mongoTemplate;

	@Autowired
	DocumentRepository documentRepository;

	@Autowired
	UserRepository userRepository;
	
	

	@GetMapping("/version")
	public String version() {
		return "vendorPortalVersion: 1";
	}

	public POController(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Autowired
	InvoiceRepository invoiceRepository;

	@GetMapping("/dateparsingcheck")
	public LocalDate parsed(@RequestParam String invoiceDate) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		LocalDate date = LocalDate.parse(invoiceDate, formatter);
		System.out.println(date);
		return date;
	}

	@GetMapping("/testing")
	public LocalDate testing() {
		return LocalDate.now();
	}

	private String getUserNameFromToken(String token) {

		String tokenBody = token.split("\\.")[1];
		Base64.Decoder decoder = Base64.getUrlDecoder();
		String payload = new String(decoder.decode(tokenBody));
		System.out.println("payload" + payload);
		return getFieldFromJson(payload, "preferred_username");
	}

	private String getFieldFromJson(String json, String fieldName) {
		String fieldValue = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(json);
			fieldValue = jsonNode.get(fieldName).asText();
			System.out.println(fieldName + " : " + fieldValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fieldValue;
	}

	public Page<?> convertStreamToPage(Stream<?> entityStream, int page, int size) {
		List<?> entityList = entityStream.collect(Collectors.toList());
		Pageable pageable = PageRequest.of(page, size);
		return new PageImpl<>(entityList, pageable, entityList.size());
	}

	@PostMapping("/createPO")
	public ResponseEntity<?> createPurchaseOrder(@RequestParam(value = "poNumber") String poNumber,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "poIssueDate") @DateTimeFormat(pattern = "yyyy-MM-dd") String poIssueDate,
			@RequestParam(value = "deliveryDate") @DateTimeFormat(pattern = "yyyy-MM-dd") String deliveryDate,
			@RequestParam(value = "poAmount") String poAmount,
			@RequestParam(value = "deliveryTimelines", required = false) String deliveryTimelines,
			@RequestParam(value = "deliveryPlant", required = false) String deliveryPlant,
			@RequestParam(value = "eic") String eic,
			@RequestParam(value = "receiver", required = false) String receiver,
			@RequestParam(value = "filePO", required = false) MultipartFile filePO, HttpServletRequest request)
			throws Exception {
		if (porepo.existsByPoNumber(poNumber))
			return ResponseEntity.ok("Po Number " + poNumber + " Already Exists");
		String token = request.getHeader("Authorization").replace("Bearer ", "");
		String username = getUserNameFromToken(token);
		s3service.createBucket(token, username);
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
		System.out.println("Po Creation Initiated : UserName -> " + username);

		try {
			LocalDate issuedt = LocalDate.parse(poIssueDate, formatter);
			LocalDate delt = LocalDate.parse(deliveryDate, formatter);
			String url = s3service.uploadFile(filePO);
			DocDetails doc = new DocDetails(filePO.getOriginalFilename(), poNumber, url);
			
//			backupfilerepo.save(new BackupfileUrls(poNumber , ))
			
			System.err.println("PO File URL : " + url);
			PoSummary ps = new PoSummary(poNumber, description, issuedt, delt, deliveryPlant, deliveryTimelines, 0, eic,
					poAmount, receiver, username, url);
			ps.setDoc(doc);
			porepo.save(ps);
			System.err.println("Po Creation Successfully Ended ! ");
		} catch (Exception e) {
			System.out.println("Exception Found");
			e.printStackTrace();
			return ResponseEntity.ok(e);
		}
		return ResponseEntity.ok("Po Creation Successfully Ended ! , saved" + HttpStatus.ACCEPTED);
	}

	@PostMapping("/uploadInvoice")
	public ResponseEntity<?> createInvoice(@RequestParam("file") MultipartFile invoiceFile,
			@RequestPart(name = "supportingDocument", required = false) List<MultipartFile> supportingDocument,
			@RequestParam("poNumber") String poNumber,
			@RequestParam(value = "alternateMobileNumber", required = false) String alternateMobileNumber,
			@RequestParam(value = "alternateEmail", required = false) String alternateEmail,
			@RequestParam(value = "remarks", required = false) Set<String> remarks,
			@RequestParam("invoiceAmount") String invoiceAmount, @RequestParam("invoiceDate") String invoiceDate,
			@RequestParam("invoiceNumber") String invoiceNumber,
			@RequestParam(name = "validityDate", required = false) String validityDate,
			@RequestParam(name = "deliveryTimelines", required = false) String deliveryTimelines,
			@RequestParam(name = "deliveryPlant", required = false) String deliveryPlant,
			@RequestParam(value = "roleName", required = false) String roleName,
//			@RequestParam(value = "eic", required = false) String eic,
//			@RequestParam(name = "termAndConditions", required = false) String termAndConditions,
//			@RequestParam(name = "status", required = false) String status,
//			@RequestParam(name = "createdBy", required = false) String createdBy,
//			@RequestParam(name = "receievedBy", required = false) String receievedBy, 
			HttpServletRequest request) throws Exception {
		if (invoiceRepository.existsByInvoiceNumber(invoiceNumber))
			return ResponseEntity.ok("Invoice with Number " + invoiceNumber + " Already Exists");
		String receievedBy = roleName;
		String token = request.getHeader("Authorization").replace("Bearer ", "");
		String username = getUserNameFromToken(token);
		s3service.createBucket(token, username);

		String invoiceURL = s3service.uploadFile(invoiceFile);
		DocDetails invoicedetails = new DocDetails(invoiceFile.getOriginalFilename(),invoiceNumber, invoiceURL);
		List<DocDetails> suppDocNameList = new ArrayList<>();

		if (supportingDocument != null) {
			supportingDocument.forEach(document -> {
				try {
					String documentUrl = s3service.uploadFile(document);
					suppDocNameList.add(new DocDetails(document.getOriginalFilename(),invoiceNumber, documentUrl));
				} catch (IOException e) {
					System.out.println("Supporting docs exception");
					e.printStackTrace();
				}
			});
		}

		System.out.println("receievedBy: " + receievedBy);
		System.out.println("createdBy: " + username);
		String eic = porepo.findByPoNumber(poNumber).get().getEic();
		Map<String, Object> uploadMongoFile = s3service.uploadMongoFile(eic, roleName, poNumber, token,
				alternateMobileNumber, alternateEmail, remarks, invoiceAmount, invoiceDate, invoiceNumber, username,
				username, deliveryPlant, invoicedetails, suppDocNameList, "Paid", receievedBy);
		if (uploadMongoFile == null)
			return ResponseEntity.ok(HttpStatus.METHOD_FAILURE);
		return ResponseEntity.ok(uploadMongoFile);
	}

	@GetMapping("/GetPo")
	public ResponseEntity<?> getPurchaseOrder(@RequestParam String ponumber) {
		System.out.println("Getting Purchase Order");
		Optional<PoSummary> po = porepo.findByPoNumber(ponumber);
		if (po == null)
			return ResponseEntity.ok(HttpStatus.NOT_FOUND);
		PoDTO podto = new PoDTO(po.get().getId() ,po.get().getPoNumber(), po.get().getDescription(), po.get().getPoIssueDate(),
				po.get().getDeliveryDate(), po.get().getPoStatus(), po.get().getPoAmount(), po.get().getNoOfInvoices(),
				po.get().getDeliveryTimelines(), po.get().getDeliveryPlant(), po.get().getEic(),
				po.get().getReceiver(),po.get().getUrl());
		return ResponseEntity.ok(podto);

	}

	@GetMapping("/getAllRoles")
	public List<String> getAllRoles() {
		List<String> roles = Arrays.asList("HR", "Finance", "Admin", "Admin2");
		return roles;
	}

	@GetMapping("/poSummary/getSummary")
	public Page<PoDTO> getPobyUsername(@RequestParam(value = "username", required = false) String username,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
			) {
		Pageable pageable = PageRequest.of(page, size);
		Optional<Page<PoSummary>> polist = porepo.findByUsername(username,pageable);
		
		if (polist.isEmpty())
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "NO PO FOUND!", null);
		return polist.get().map(po -> new PoDTO(po.getId() , po.getPoNumber(), po.getDescription(), po.getPoIssueDate(),
				po.getDeliveryDate(), po.getPoStatus(), po.getPoAmount(), po.getNoOfInvoices(),
				po.getDeliveryTimelines(), po.getDeliveryPlant(), po.getEic(), po.getReceiver(),po.getUrl()));
	}

	@GetMapping("/getAllPo")
	public Page<PoDTO> getAllPo(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		System.out.println(page + " " + size);
		Pageable pageable = PageRequest.of(page, size, Sort.by("poIssueDate").descending());
		Page<PoSummary> poPage = porepo.findAll(pageable);

		return poPage.map(po -> new PoDTO(po.getId() ,po.getPoNumber(), po.getDescription(), po.getPoIssueDate(),
				po.getDeliveryDate(), po.getPoStatus(), po.getPoAmount(), po.getNoOfInvoices(),
				po.getDeliveryTimelines(), po.getDeliveryPlant(), po.getEic(), po.getReceiver(),po.getUrl()));
	}
	
	@GetMapping("/getAllPoNumber")
	public List<String> getAllPoNumber(@RequestParam(value = "ponumber") String poNumber) {
		List<PoSummary> polist= porepo.findAll();
		return polist.stream().flatMap(po->getAllPoNumber(poNumber).stream()).toList();
	}

	@GetMapping("/getAllInvoices")
	public ResponseEntity<?> getAllInvoices(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("invoicedate").descending());

		Page<Invoice> invoicepage = invoiceRepository.findAll(pageable);
		Page<InvoiceDTO> invoicedtopage = invoicepage
				.map(po -> new InvoiceDTO(po.getId(), po.getPoNumber(), po.getDeliveryTimelines(), po.getInvoiceDate(),po.getInvoiceAmount(),
						 po.getDeliveryPlant(), po.getMobileNumber(), po.getEic(), po.getPaymentType(), po.getInvoiceurl()));
		return ResponseEntity.ok(invoicedtopage);
	}

	@GetMapping("/email")
	public ResponseEntity<String> getEmailByEic(@RequestParam("eic") String eic) {
		UserClass user = userRepository.findByEic(eic);

		if (user != null) {
			return ResponseEntity.ok(user.getEmail());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("getInvoice")
	public ResponseEntity<?> getInvoicesBypoNumber(@RequestParam(value = "poNumber") String poNumber,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);
		System.err.println("get PO");
		Page<InvoiceDTO> invoice1 = invoiceRepository.findByPoNumber(poNumber, pageable);

//		if (!invoice1.isPresent())
//			return ResponseEntity.ok(HttpStatus.NOT_FOUND);
//
//		Invoice invoice = invoice1.get();
//		InvoiceDTO invoiceDTO = new InvoiceDTO();
//
//		invoiceDTO.setRoleName(invoice.getRoleName());
//		invoiceDTO.setEic(invoice.getEic());
//		invoiceDTO.setDeliveryPlant(invoice.getDeliveryPlant());
//		invoiceDTO.setMobileNumber(invoice.getMobileNumber());
//		invoiceDTO.setEmail(invoice.getEmail());
//		invoiceDTO.setPaymentType(invoice.getPaymentType());
//		invoiceDTO.setType(invoice.getType());
//		invoiceDTO.setMsmeCategory(invoice.getMsmeCategory());
//
//		System.out.println("::GET " + invoiceDTO);

		return ResponseEntity.status(HttpStatus.OK).body(invoice1);
	}

	@PostMapping("/updateClaimedStatus")
	public ResponseEntity<String> updateClaimedStatus(@RequestParam("id") String id,
			@RequestParam("claimed") boolean claimed, @RequestParam("username") String username) {
		Optional<Invoice> optionalInvoice = invoiceRepository.findById(id);
		if (optionalInvoice.isPresent()) {
			Invoice invoice = optionalInvoice.get();
			invoice.setClaimedBy(username);
			invoice.setClaimed(claimed);
			invoiceRepository.save(invoice);
			return ResponseEntity.status(HttpStatus.OK).body("Claimed status updated successfully.");
		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found.");
		}
	}

	@GetMapping("/invoices")
	public ResponseEntity<Page<InvoiceDTO>> getInvoicesByUsernameAndClaimedTrue(
			@RequestParam("username") String username,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<InvoiceDTO> invoice = invoiceRepository.findByUsernameAndClaimedIsTrue(username, pageable);

		if (!invoice.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK).body(invoice);
		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"No invoices found for the given username and claimed value");
		}
	}

	@GetMapping("getData")
	public Page<InvoiceDTO> getInvoicesByParameters(@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "poNumber", required = false) String poNumber,
			@RequestParam(value = "invoiceNumber", required = false) String invoiceNumber,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);
		if (poNumber == null && invoiceNumber == null && username == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide atleast one detail !!");
		}
		if (username != null && poNumber == null && invoiceNumber == null) {
			return invoiceRepository.findByUsername(username, pageable);
		}

		// Find by poNumber only
		if (poNumber != null && username == null && invoiceNumber == null) {
			return invoiceRepository.findByPoNumber(poNumber, pageable);
		}

		// Find by invoiceNumber only
		if (invoiceNumber != null && username == null && poNumber == null) {
			return invoiceRepository.findByInvoiceNumber(invoiceNumber, pageable);
		}

		// Find by poNumber and invoiceNumber
		if (poNumber != null && invoiceNumber != null) {
			return invoiceRepository.findByPoNumberAndInvoiceNumber(poNumber, invoiceNumber, pageable);
		}

		// Find by poNumber and username
		if (poNumber != null && username != null) {
			return invoiceRepository.findByUsernameAndPoNumber(username, poNumber, pageable);
		}

		// Find by username and invoiceNumber
		if (username != null && invoiceNumber != null) {
			return invoiceRepository.findByUsernameAndInvoiceNumber(username, invoiceNumber, pageable);
		}

		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "NO DATA FOUND");
	}

	@GetMapping("searchInvoices")
	public ResponseEntity<Page<InvoiceDTO>> searchInvoices(
			@RequestParam(value = "searchItems", required = false) String searchItems,
			@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Page<InvoiceDTO> invoicedtopage = null;
		Page<Invoice> invoicepage;
		List<Invoice> invoices = null;
		Pageable pageable = PageRequest.of(page, size);
		if (searchItems == null || searchItems.isEmpty()) { // Check if searchItems is null or empty
			return ResponseEntity.ok(invoiceRepository.findByUsername(username, pageable)); // Fetch data based on username only
		} else {
			Criteria criteria = new Criteria().andOperator(Criteria.where("username").is(username),
					new Criteria().orOperator(Criteria.where("poNumber").regex(searchItems),
							Criteria.where("deliveryTimelines").regex(searchItems),
							Criteria.where("invoiceNumber").regex(searchItems),
							Criteria.where("status").regex(searchItems),
							Criteria.where("mobileNumber").regex(searchItems),
							Criteria.where("deliveryPlant").regex(searchItems),
//							Criteria.where("remarks").regex(searchItems),
							Criteria.where("paymentType").regex(searchItems),
							Criteria.where("receiver").regex(searchItems),
							Criteria.where("claimedBy").regex(searchItems), Criteria.where("type").regex(searchItems),
							Criteria.where("msmeCategory").regex(searchItems)));

			invoices= mongoTemplate.find(Query.query(criteria), Invoice.class);
			System.out.println("---------------------------");
			invoices.forEach(System.out::println);
		}
		invoicepage = convertListToPage(invoices, page, size);
		
		invoicedtopage = invoicepage
				.map(po -> new InvoiceDTO(po.getId() , po.getPoNumber(), po.getDeliveryTimelines(), po.getInvoiceDate(),po.getInvoiceAmount(),
						 po.getDeliveryPlant(), po.getMobileNumber(), po.getEic(), po.getPaymentType(),po.getInvoiceurl()));
		invoicedtopage.forEach(System.out::println);
		return ResponseEntity.ok(invoicedtopage);
	}

	public Page<Invoice> convertListToPage(List<Invoice> invoiceList, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		int start = Math.min((int) pageable.getOffset(), invoiceList.size());
		int end = Math.min((start + pageable.getPageSize()), invoiceList.size());
		List<Invoice> subList = invoiceList.subList(start, end);
		return new PageImpl<>(subList, pageable, invoiceList.size());
	}

	@GetMapping("getDash")
	public ResponseEntity<Map<String, Object>> getInvoicesByUsername11(
			@RequestParam(value = "username") String username,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<InvoiceDTO> invoices = invoiceRepository.findByUsername(username, pageable);
		if (invoices.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No invoices found");
		}

		long approvedCount = invoices.stream().filter(invoice -> "approved".equalsIgnoreCase(invoice.getStatus()))
				.count();
		long inProgressCount = invoices.stream().filter(invoice -> "inProgress".equalsIgnoreCase(invoice.getStatus()))
				.count();
		long pendingCount = invoices.stream().filter(invoice -> "pending".equalsIgnoreCase(invoice.getStatus()))
				.count();
		long rejectedCount = invoices.stream().filter(invoice -> "rejected".equalsIgnoreCase(invoice.getStatus()))
				.count();

		Map<String, Object> response = new HashMap<>();

		long totalCount = invoices.getTotalElements(); // Use the size of the filtered list
		response.put("totalCount", totalCount);

		Map<String, Long> statusCounts = new HashMap<>();
		statusCounts.put("approved", approvedCount);
		statusCounts.put("pending", pendingCount);
		statusCounts.put("rejected", rejectedCount);
		statusCounts.put("inProgress", inProgressCount);
		response.put("statusCounts", statusCounts);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("totalDash")
	public ResponseEntity<Map<String, Object>> getInvoiceStatistics() {
		List<Invoice> invoices = invoiceRepository.findAll(); // Fetch all invoices

		if (invoices.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No invoices found");
		}

		long approvedCount = invoices.stream().filter(invoice -> "approved".equalsIgnoreCase(invoice.getStatus()))
				.count();

		long pendingCount = invoices.stream().filter(invoice -> "pending".equalsIgnoreCase(invoice.getStatus()))
				.count();

		long rejectedCount = invoices.stream().filter(invoice -> "rejected".equalsIgnoreCase(invoice.getStatus()))
				.count();

		Map<String, Object> response = new HashMap<>();

		long totalCount = invoices.size(); // Use the size of the list
		response.put("totalCount", totalCount);

		Map<String, Long> statusCounts = new HashMap<>();
		statusCounts.put("approved", approvedCount);
		statusCounts.put("pending", pendingCount);
		statusCounts.put("rejected", rejectedCount);
		response.put("statusCounts", statusCounts);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("getDashboard")
	public ResponseEntity<Map<String, Object>> getInvoicesByUsername(@RequestParam(value = "username") String username,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<InvoiceDTO> invoices = invoiceRepository.findByUsername(username, pageable);
		if (invoices.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No invoices found");
		}

		long numInvoices = invoices.getTotalElements();
		long inprogressInvoices = invoices.getTotalElements(); // This seems to be a placeholder; adjust logic if
																// needed.

		Map<String, Object> response = new HashMap<>();
		response.put("numInvoices", numInvoices);
		response.put("inprogressInvoices", inprogressInvoices);

		List<Map<String, Object>> modifiedInvoices = new ArrayList<>();
		for (InvoiceDTO invoice : invoices) {
			Map<String, Object> modifiedInvoice = new HashMap<>();
			modifiedInvoice.put("invoiceNumber", invoice.getInvoiceNumber()); // Add the MongoDB object ID
			modifiedInvoice.put("deliveryPlant", invoice.getDeliveryPlant());
//			modifiedInvoice.put("remarks", invoice.getRemarks());
			modifiedInvoice.put("poNumber", invoice.getPoNumber());
			modifiedInvoice.put("status", invoice.getStatus());
			modifiedInvoice.put("invoiceDate", invoice.getInvoiceDate());
			System.out.println("Invoice ID: " + invoice.getInvoiceNumber());
			System.out.println("Status: " + invoice.getStatus());
			System.out.println("InvoiceDate: " + invoice.getInvoiceDate());
			modifiedInvoices.add(modifiedInvoice);
		}

		response.put("invoices", modifiedInvoices);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PutMapping("/updateInvoice")
	public ResponseEntity<?> updateInvoice(@RequestHeader("remarks") Set<String> remarks,
			@RequestHeader("id") String invoiceId) {
		Optional<Invoice> invoiceOptional = invoiceRepository.findById(invoiceId);
		if (invoiceOptional.isEmpty() || !invoiceOptional.isPresent())
			return ResponseEntity.badRequest().body("invoice with this id does not exist");
		Invoice invoice = invoiceOptional.get();
		Set<String> existingremarks = invoice.getRemarks();
		if(existingremarks==null || existingremarks.isEmpty()) { 
			existingremarks = new HashSet<String>();
		}
		existingremarks.addAll(remarks);
		invoice.setRemarks(existingremarks);
		invoice.setReceiver(invoice.getUsername());
		invoiceRepository.save(invoice);
		return ResponseEntity.ok(invoice);
	}

	@GetMapping("/getInboxData")
	public ResponseEntity<Page<InvoiceDTO>> getAllInvoices(@RequestHeader("roleName") String roleName,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<InvoiceDTO> invoicedtopage;

		if ("admin".equals(roleName) || "admin2".equals(roleName)) {
			return ResponseEntity
					.ok(invoiceRepository.findByRoleNameAndTypeAndClaimed(roleName, "material", false, pageable));
		} else {
			// For other roles, filter by roleName only
			invoicedtopage = invoiceRepository.findByRoleName(roleName, pageable);
		}

		if (!invoicedtopage.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK).body(invoicedtopage);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}

	@GetMapping("/InboxData")
	public ResponseEntity<Page<InvoiceDTO>> getInvoicesByUsernameAndStatus(@RequestParam("username") String username,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);

		// Retrieve invoices with status "reverted"
		Page<InvoiceDTO> invoiceList = invoiceRepository.findByUsernameAndStatus(username, "reverted", pageable);

		if (!invoiceList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK).body(invoiceList);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}

	@GetMapping("/getClaimedInbox")
	public ResponseEntity<Page<InvoiceDTO>> getAllInvoices(@RequestHeader(required = false) Boolean claimed,
			@RequestHeader("claimedBy") String claimedBy,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);

		try {
			Page<InvoiceDTO> invoicedtopage = null;

			if (claimed != null && claimed && claimedBy != null) {
				// If claimed is true and claimedBy is provided, retrieve claimed invoices
				invoicedtopage = invoiceRepository.findByClaimedAndClaimedBy(true, claimedBy, pageable);
			}
			// Always return an empty list to hide data when claimed is not true
			return ResponseEntity.status(HttpStatus.OK).body(invoicedtopage);
		} catch (Exception e) {
			// Handle exceptions, log them, and return an error response
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@PostMapping("/revert")
	public ResponseEntity<?> revertInvoice(@RequestParam("id") String id,
			@RequestParam(value = "remarks", required = false) String remarks) {

		// Retrieve the invoice from the database using the invoiceId
		Optional<Invoice> invoiceOptional = invoiceRepository.findById(id);
		if (invoiceOptional.isEmpty() || !invoiceOptional.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		Invoice invoice = invoiceOptional.get();
		// Revert the invoice to the original user and update roleName with username
		try {
			invoice.setStatus("reverted");
			invoice.setRoleName(invoice.getUsername()); // Update roleName with the value of username

			// Update remarks if provided
			if (remarks != null) {
				// If remarks list is null, create a new ArrayList
				if (invoice.getRemarks() == null) {
					invoice.setRemarks(new HashSet<String>());
				}
				// Add the new remark to the list
				invoice.getRemarks().add(remarks);
			}

			Invoice revertedInvoice = invoiceRepository.save(invoice);
			return ResponseEntity.ok(revertedInvoice);
		} catch (Exception e) {
			e.printStackTrace(); // Log the exception
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reverting invoice");
		}
	}

	@DeleteMapping("/delete")
	public ResponseEntity<String> deleteInvoice(@RequestParam("id") String id) {
		// Assuming you have a method in your repository to delete an invoice by ID
		try {
			invoiceRepository.deleteById(id);
			return ResponseEntity.ok("Invoice deleted successfully");
		} catch (Exception e) {
			e.printStackTrace(); // Log the exception
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting invoice");
		}
	}

	@PostMapping("/forward")
	public ResponseEntity<?> forwardInvoice(@RequestParam("id") String id, @RequestParam("roleName") String roleName,
			@RequestParam("remarks") Set<String> remarks) {
		// Retrieve the invoice from the database using the invoiceId
		Optional<Invoice> invoiceOptional = invoiceRepository.findById(id);

		if (invoiceOptional.isEmpty())
			return ResponseEntity.notFound().build();

		Invoice invoice = invoiceOptional.get();

		// Log the roleName for debugging
		System.out.println("Role Name: " + roleName);

		// Update fields based on roleName
		try {
			if (roleName.startsWith("vi-")) {
				invoice.setStatus("pending");
			} else if (roleName.equalsIgnoreCase("finance") || roleName.equalsIgnoreCase("hr")) {
				invoice.setStatus("inProgress");
				invoice.setClaimed(false);
				invoice.setType("service");
				System.out.println("HR/Finance Role Detected");
			} else if (roleName.equalsIgnoreCase("admin") || roleName.equalsIgnoreCase("admin2")) {
				// Log for debugging
				System.out.println("Admin Role Detected");
				// Set specific fields for admin roles
				invoice.setClaimed(false);
				invoice.setType("material");
			}
			invoice.setRoleName(roleName);
			// Append the new remarks to the existing list
			if (remarks != null) {
				if (invoice.getRemarks() == null) {
					invoice.setRemarks(new HashSet<>());
				}
				invoice.getRemarks().addAll(remarks);
			}
			Invoice updatedInvoice = invoiceRepository.save(invoice);

			return ResponseEntity.ok(updatedInvoice);
		} catch (Exception e) {
			e.printStackTrace(); // Log the exception
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating invoice");
		}
	}

	@PostMapping("/forwarding")
	public ResponseEntity<?> forwardInvoice(@RequestParam("id") String id, @RequestParam("roleName") String roleName,
			@RequestParam("remarks") List<String> remarks,
			@RequestParam(value = "file", required = false) MultipartFile invoicefile,
			@RequestParam(value = "supportingDocument", required = false) MultipartFile supportingDocument) {

		// Retrieve the invoice from the database using the invoiceId
		Optional<Invoice> invoiceOptional = invoiceRepository.findById(id);
		if (invoiceOptional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Invoice invoice = invoiceOptional.get();

		// Log the roleName for debugging
		System.out.println("Role Name: " + roleName);

		// Update fields based on roleName
		try {
			if (roleName.startsWith("vi-")) {
				invoice.setStatus("pending");
			} else if (roleName.equalsIgnoreCase("finance") || roleName.equalsIgnoreCase("hr")) {
				invoice.setStatus("inProgress");
				System.out.println("HR/Finance Role Detected");
				invoice.setClaimed(false);
				invoice.setType("service");
			} else if (roleName.equalsIgnoreCase("admin") || roleName.equalsIgnoreCase("admin2")) {
				// Log for debugging
				System.out.println("Admin Role Detected");

				// Set specific fields for admin roles
				invoice.setClaimed(false);
				invoice.setType("material");
			}

			invoice.setRoleName(roleName);

			// Append the new remarks to the existing list
			if (remarks != null) {
				if (invoice.getRemarks() == null) {
					invoice.setRemarks(new HashSet<>());
				}
				invoice.getRemarks().addAll(remarks);
			}

			// Update the file in the database (modify based on your entity structure)
			List<DocDetails> newFiles = new ArrayList<>();
			if (invoicefile != null) {
				String newFileUrl = s3service.uploadFile(invoicefile);
				newFiles.add(new DocDetails(invoicefile.getOriginalFilename(),invoiceOptional.get().getInvoiceNumber(), newFileUrl));
			}

			invoice.getInvoiceFile().addAll(newFiles);

			// Update the supporting document in the database (modify based on your entity
			// structure)
			List<DocDetails> newSupportingDocuments = new ArrayList<>();
			if (supportingDocument != null) {
				String newDocUrl = s3service.uploadFile(supportingDocument);
				newSupportingDocuments.add(new DocDetails(supportingDocument.getOriginalFilename(),invoiceOptional.get().getInvoiceNumber(), newDocUrl));
			}

			// Append the new supporting documents to the existing list in the database
			// (modify based on your entity structure)
			invoice.getSupportingDocument().addAll(newSupportingDocuments);

			// Save the updated invoice to the database
			Invoice updatedInvoice = invoiceRepository.save(invoice);

			return ResponseEntity.ok(updatedInvoice);
		} catch (Exception e) {
			e.printStackTrace(); // Log the exception
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating invoice");
		}
	}

	@GetMapping("/getting")
	public ResponseEntity<Page<InvoiceDTO>> getInvoicesByReceiver(@RequestParam("receiver") String receiver,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);
		// Retrieve invoices with the given receiver username
		Page<InvoiceDTO> invoices = invoiceRepository.findByReceiver(receiver, pageable);

		if (invoices.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok(invoices);
	}

	@PostMapping("/upload-multiple")
	public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files,
			@RequestParam("username") String username, HttpServletRequest request) {
		List<String> fileNames = Collections.synchronizedList(new ArrayList<>());
		// Convert array into list
		List<MultipartFile> fileslist = Arrays.asList(files);
		System.err.println(request.getHeader("Authorization"));

		fileslist.parallelStream().filter(file -> !file.isEmpty()).map(file -> {
			try {
				DocumentsMongo document = new DocumentsMongo();
				document.setusername(username);
				document.setFilename(file.getOriginalFilename());

				ResponseEntity<String> url = s3service
						.uploadCompliance(request.getHeader("Authorization").replace("Bearer ", ""), file);
				document.setUrl(url.getBody());

				if (url.getStatusCodeValue() != 200) {
					return ResponseEntity.internalServerError().body("not uploaded successfully!!");
				}
				documentRepository.save(document);

				synchronized (fileNames) {
					fileNames.add(file.getOriginalFilename());
				}
				return ResponseEntity.ok("uploaded successfully!!");
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.internalServerError().body("not uploaded successfully!!");
			}
		}).filter(response -> response.getStatusCodeValue() != 200).findAny().ifPresent(response -> {
			// Handle the case where at least one file was not uploaded successfully
			throw new RuntimeException("At least one file was not uploaded successfully!!");
		});

//		fileslist.parallelStream().map(file->{
//			if(!file.isEmpty())
//		})
//		for (MultipartFile file : files) {
//			if (!file.isEmpty()) {
//				DocumentsMongo document = new DocumentsMongo();
//				document.setusername(username);
//				document.setFilename(file.getOriginalFilename());
//				ResponseEntity<String> url = s3service
//						.uploadCompliance(request.getHeader("Authorization").replace("Bearer ", ""), file);
//				document.setUrl(url.getBody());
//				if (url.getStatusCodeValue() != 200)
//					return ResponseEntity.internalServerError().body("not uploaded successfully!!");
//				documentRepository.save(document);
//				String fileName = file.getOriginalFilename();
//				fileNames.add(fileName);
//			}
//		}

		return ResponseEntity.ok("Files uploaded successfully: " + fileNames);
	}

	@GetMapping("getInvoiceCount")
	public ResponseEntity<Map<String, Long>> getInvoiceCount(@RequestParam(value = "username") String username,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<InvoiceDTO> invoicesByUsername = invoiceRepository.findByUsername(username, pageable);
		long invoiceCountByUsername = invoicesByUsername.getTotalElements();

		// Calculate inbox count based on username and status "reverted"
		long inboxCountReverted = invoiceRepository.countByUsernameAndStatus(username, "reverted");

		Map<String, Long> response = new HashMap<>();
		response.put("invoiceCount", invoiceCountByUsername);
		response.put("inboxCount", inboxCountReverted);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/get-compliances") // to get all the compliances by username, username is required in path
	public ResponseEntity<?> getCompliances(@RequestParam("username") String username) {
		List<DocumentsMongo> compliances = documentRepository.findAllByusername(username);
		return ResponseEntity.ok(compliances);
	}

	@GetMapping("poSearch")
	public ResponseEntity<?> searchPOByPrefix(@RequestParam(value = "poNumber") String poNumber,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);

		Page<InvoiceDTO> poList = invoiceRepository.findByPoNumberContaining(poNumber, pageable);
		return ResponseEntity.ok(poList);

	}

	@GetMapping("/deliveryPlants")
	public List<String> getAllDeliveryPlants() {
		List<Invoice> entities = invoiceRepository.findAll();
		return entities.stream().map(Invoice::getDeliveryPlant) // Replace with the actual method to get the //
																// deliveryPlant field
				.distinct().collect(Collectors.toList());
	}

	@GetMapping("/invoices-by-username")
	public ResponseEntity<Page<InvoiceDTO>> getInvoicesByUsername1(@RequestParam("username") String username,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<InvoiceDTO> invoices = invoiceRepository.findByUsername(username, pageable);

		if (!invoices.isEmpty()) {

			return ResponseEntity.status(HttpStatus.OK).body(invoices);
		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No invoices found for the given username");
		}
	}

}
