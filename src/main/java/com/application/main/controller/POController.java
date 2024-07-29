package com.application.main.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	public String version(){
		return "vendorPortalVersion: 1";
	}
	

	public POController(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Autowired
	InvoiceRepository invoiceRepository;

	@PostMapping("/createPO")
	public ResponseEntity<?> createPurchaseOrder(@RequestBody PoDTO podto) {
		System.err.println("999999999999999999999999999999");
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
		LocalDate issuedt = LocalDate.parse(podto.getPoIssueDate(),formatter);
		LocalDate delt = LocalDate.parse(podto.getDeliveryDate(),formatter);
		
		PoSummary ps = new PoSummary(podto.getPoNumber(), podto.getDescription(), issuedt,
				delt, podto.getDeliveryPlant(), podto.getDeliveryTimelines(),
				podto.getNoOfInvoices(), podto.getPoStatus(), podto.getEic(), podto.getPaymentType(),
				podto.getPoAmount(), podto.getReceiver());

		porepo.save(ps);
		return ResponseEntity.ok("saved");

	}

	@GetMapping("/GetPo")
	public ResponseEntity<?> getPurchaseOrder(@RequestParam String ponumber) {
		Optional<PoSummary> po = porepo.findByPoNumber(ponumber);
		if(po==null) return ResponseEntity.ok(HttpStatus.NOT_FOUND);
		PoDTO podto = new PoDTO(po.get().getPoNumber(), po.get().getDescription(), po.get().getPoIssueDate(), po.get().getDeliveryDate(), po.get().getPoStatus(), po.get().getPoAmount(),po.get().getNoOfInvoices(), po.get().getDeliveryTimelines(), po.get().getDeliveryPlant(), po.get().getPaymentType(), po.get().getEic(), po.get().getReceiver());
		return ResponseEntity.ok(podto);
			
	}

	@GetMapping("/getAllRoles")
	public List<String> getAllRoles() {
		List<String> roles = Arrays.asList("hr", "finance", "admin", "admin2");
		return roles;
	}

	@GetMapping("/getAllPo")
	public Page<PoDTO> getAllPo(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("poIssueDate").descending());

		Page<PoSummary> poPage = porepo.findAll(pageable);

		return poPage.map(po -> new PoDTO(po.getPoNumber(), po.getDescription(), po.getPoIssueDate(),
				po.getDeliveryDate(), po.getPoStatus(), po.getPoAmount(), po.getNoOfInvoices(),
				po.getDeliveryTimelines(), po.getDeliveryPlant(), po.getPaymentType(), po.getEic(), po.getReceiver()));
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

	@PostMapping("/uploadInvoice")
	public ResponseEntity<?> createInvoice(
			@RequestParam("poNumber") String poNumber,
			@RequestParam("file") MultipartFile invoiceFile,
			@RequestPart(name = "supportingDocument", required = false) List<MultipartFile> supportingDocument,
			@RequestParam(value = "roleName", required = false) String roleName,
			@RequestParam(value = "eic", required = false) String eic,
			@RequestParam(value = "alternateMobileNumber", required = false) String alternateMobileNumber,
			@RequestParam(value = "alternateEmail", required = false) String alternateEmail,
			@RequestParam(value = "remarks", required = false) Set<String> remarks,
			@RequestParam("invoiceAmount") String invoiceAmount,
			@RequestParam("invoiceDate") String invoiceDate,
			@RequestParam("invoiceNumber") String invoiceNumber,
			@RequestParam(name = "validityDate", required = false) String validityDate,
			@RequestParam(name = "termAndConditions", required = false) String termAndConditions,
			@RequestParam(name = "status", required = false) String status,
			@RequestParam(name = "deliveryTimelines", required = false) String deliveryTimelines,
			@RequestParam(name = "deliveryPlant", required = false) String deliveryPlant,
			@RequestParam(name = "createdBy", required = false) String createdBy,
			@RequestParam(name = "receievedBy", required = false) String receievedBy, HttpServletRequest request)
			throws Exception {

//		if(supportingDocument.contains(invoiceFile)) return ResponseEntity.ok("InvoiceFile and Supporting Document cant be Same");
		System.err.println("----------------------------------------------");
		if (receievedBy == null || receievedBy.isEmpty()) {
			receievedBy = roleName;
		}
		String username = "";
		if (createdBy == null || createdBy.isEmpty()) {
			username = request.getHeader("preferred_username");
			createdBy = username;
		}

		String token = request.getHeader("Authorization").replace("Bearer ", "");
		System.err.println(token);
		s3service.createBucket(token);

		List<String> invoiceFileUrlList = new ArrayList<>();
		String url = s3service.uploadFile(invoiceFile);
		DocDetails invoicedetails = new DocDetails(invoiceFile.getOriginalFilename(), url);
		invoiceFileUrlList.add(url);
		// ----------------------------------
		List<DocDetails> suppDocNameList = new ArrayList<>();
		List<String> supportingDocumentUrls = new ArrayList<>();

		if (supportingDocument != null) {
			supportingDocument.forEach(document -> {
				try {
					String documentUrl = s3service.uploadFile(document);
					supportingDocumentUrls.add(documentUrl);
					suppDocNameList.add(new DocDetails(document.getOriginalFilename(), documentUrl));
				} catch (IOException e) {
					// Handle the exception, e.g., log it or rethrow it
					e.printStackTrace();
					// Optionally, you can handle the exception differently based on your
					// application's needs
				}
			});
		}

		System.out.println("receievedBy: " + receievedBy);
		System.out.println("createdBy" + createdBy);
		
		Map<String, Object> uploadMongoFile = s3service.uploadMongoFile(invoiceFileUrlList, supportingDocumentUrls,
				eic, roleName, poNumber, token, alternateMobileNumber, alternateEmail, remarks, invoiceAmount,
				invoiceDate, invoiceNumber, username, createdBy, deliveryPlant, invoicedetails, suppDocNameList, status,
				receievedBy);
		if (uploadMongoFile == null)
			return ResponseEntity.ok(HttpStatus.METHOD_FAILURE);
		return ResponseEntity.ok(uploadMongoFile);
	}

	@GetMapping("/getAllInvoices")
	public ResponseEntity<?> getAllInvoices(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("invoicedate").descending());

//		Page<InvoiceDTO> invoicepage = invoiceRepository.findAll(pageable).map()
		Page<Invoice> invoicepage = invoiceRepository.findAll(pageable);
		Page<InvoiceDTO> invoicedtopage = invoicepage.map(po->new InvoiceDTO(po.getPoNumber(), po.getDeliveryTimelines(), po.getInvoiceDate(),
				po.getClaimedBy(), po.getRoleName(), po.getDeliveryPlant(), po.getMobileNumber(), po.getEic(),
				po.getType(), po.getMsmeCategory(), po.getPaymentType()));
//		List<InvoiceDTO> invoicelist = invoiceRepository.findAll().stream().map(po->new InvoiceDTO(po.getPoNumber(), po.getDeliveryTimelines(), po.getInvoiceDate(),
//				po.getClaimedBy(), po.getRoleName(), po.getDeliveryPlant(), po.getMobileNumber(), po.getEic(),
//				po.getType(), po.getMsmeCategory(), po.getPaymentType())).collect(Collectors.toList());
//		System.out.println(invlist);
//		System.out.println(invoicedtopage);
		return ResponseEntity.ok(invoicedtopage);
//		return ResponseEntity.ok(invoicepage);
	}
	
	@GetMapping("/dateparsingcheck")
	public LocalDate parsed(@RequestParam String  invoiceDate) {
		
		   DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	        LocalDate date = LocalDate.parse(invoiceDate, formatter);
	        System.out.println(date);
	        return date;
	}
	@GetMapping("getInvoice")
	public ResponseEntity<InvoiceDTO> getInvoicesBypoNumber(@RequestParam(value = "poNumber") String poNumber) {
		System.err.println("get PO");
		Invoice invoice = invoiceRepository.findByPoNumber(poNumber);

		if (invoice != null) {
			InvoiceDTO invoiceDTO = new InvoiceDTO();

			invoiceDTO.setRoleName(invoice.getRoleName());
			invoiceDTO.setEic(invoice.getEic());
			invoiceDTO.setDeliveryPlant(invoice.getDeliveryPlant());
			invoiceDTO.setMobileNumber(invoice.getMobileNumber());
			invoiceDTO.setEmail(invoice.getEmail());
			invoiceDTO.setPaymentType(invoice.getPaymentType());
			invoiceDTO.setType(invoice.getType());
			invoiceDTO.setMsmeCategory(invoice.getMsmeCategory());

			System.out.println("::GET " + invoiceDTO);

			return ResponseEntity.status(HttpStatus.OK).body(invoiceDTO);
		}

		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No invoices found");
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
	public ResponseEntity<List<Invoice>> getInvoicesByUsernameAndClaimedTrue(
			@RequestParam("username") String username) {
		List<Invoice> invoice = invoiceRepository.findByUsernameAndClaimedIsTrue(username);

		if (!invoice.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK).body(invoice);
		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"No invoices found for the given username and claimed value");
		}
	}

	@GetMapping("getData")
	public ResponseEntity<List<Invoice>> getInvoicesByParameters(
			@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "poNumber", required = false) String poNumber,
			@RequestParam(value = "invoiceNumber", required = false) String invoiceNumber) {

		if (poNumber == null && invoiceNumber == null) {
			if (username == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide atleast one detail !!");
			}
			// If only username is provided, return all invoices with that username
			if (username != null) {
				List<Invoice> invoicesByUsername = invoiceRepository.findByUsername(username);
				if (invoicesByUsername.isEmpty()) {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND,
							"No invoices found for the given username.");
				}
				return new ResponseEntity<>(invoicesByUsername, HttpStatus.OK);
			}
		}

		// If poNumber is provided, search by username and poNumber
		if (poNumber != null) {
			List<Invoice> invoicesBypoNumber = invoiceRepository.findByUsernameAndPoNumber(username, poNumber);
			if (!invoicesBypoNumber.isEmpty()) {
				return new ResponseEntity<>(invoicesBypoNumber, HttpStatus.OK);
			}
		}

		// If invoiceNumber is provided, search by username and invoiceNumber
		if (invoiceNumber != null) {
			List<Invoice> invoicesByinvoiceNumber = invoiceRepository.findByUsernameAndInvoiceNumber(username,
					invoiceNumber);
			if (!invoicesByinvoiceNumber.isEmpty()) {
				return new ResponseEntity<>(invoicesByinvoiceNumber, HttpStatus.OK);
			}
		}

		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No invoices found");
	}

	@GetMapping("searchInvoices")
	public ResponseEntity<List<Invoice>> searchInvoices(
			@RequestParam(value = "searchItems", required = false) String searchItems,
			@RequestParam(value = "username", required = true) String username) {
		List<Invoice> invoices = new ArrayList<>();

		if (searchItems == null || searchItems.isEmpty()) { // Check if searchItems is null or empty
			invoices.addAll(invoiceRepository.findByUsername(username)); // Fetch data based on username only
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

			invoices.addAll(mongoTemplate.find(Query.query(criteria), Invoice.class));
		}

		return ResponseEntity.ok(invoices);
	}

	@GetMapping("getDash")
	public ResponseEntity<Map<String, Object>> getInvoicesByUsername11(
			@RequestParam(value = "username") String username) {
		List<Invoice> invoices = invoiceRepository.findByUsername(username);
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

		long totalCount = invoices.size(); // Use the size of the filtered list
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
	public ResponseEntity<Map<String, Object>> getInvoicesByUsername(
			@RequestParam(value = "username") String username) {
		List<Invoice> invoices = invoiceRepository.findByUsername(username);
		if (invoices.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No invoices found");
		}

		int numInvoices = invoices.size();
		int inprogressInvoices = invoices.size(); // This seems to be a placeholder; adjust logic if needed.

		Map<String, Object> response = new HashMap<>();
		response.put("numInvoices", numInvoices);
		response.put("inprogressInvoices", inprogressInvoices);

		List<Map<String, Object>> modifiedInvoices = new ArrayList<>();
		for (Invoice invoice : invoices) {
			Map<String, Object> modifiedInvoice = new HashMap<>();
			modifiedInvoice.put("id", invoice.getId()); // Add the MongoDB object ID
			modifiedInvoice.put("deliveryPlant", invoice.getDeliveryPlant());
			modifiedInvoice.put("remarks", invoice.getRemarks());
			modifiedInvoice.put("poNumber", invoice.getPoNumber());
			modifiedInvoice.put("status", invoice.getStatus());
			modifiedInvoice.put("validityDate", invoice.getValidityDate());
			System.out.println("Invoice ID: " + invoice.getId());
			System.out.println("Status: " + invoice.getStatus());
			System.out.println("Validity Date: " + invoice.getValidityDate());
			modifiedInvoices.add(modifiedInvoice);

		}

		response.put("invoices", modifiedInvoices);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping("/updateInvoice")
	public ResponseEntity<?> updateInvoice(@RequestHeader("remarks") Set<String> remarks,
			@RequestHeader("id") String invoiceId) {
		Optional<Invoice> invoiceOptional = invoiceRepository.findById(invoiceId);
		if (invoiceOptional.isEmpty())
			return ResponseEntity.badRequest().body("invoice with this id does not exist");
		Invoice invoice = invoiceOptional.get();
		invoice.setRemarks(remarks);
		invoice.setReceiver(invoice.getUsername());
		invoiceRepository.save(invoice);
		return ResponseEntity.ok(invoice);
	}

	@GetMapping("/getInboxData")
	public ResponseEntity<List<Invoice>> getAllInvoices(@RequestHeader("roleName") String roleName) {

		List<Invoice> invoiceList;

		if ("admin".equals(roleName) || "admin2".equals(roleName)) {
			invoiceList = invoiceRepository.findByRoleNameAndTypeAndClaimed(roleName, "material", false);
		} else {
			// For other roles, filter by roleName only
			invoiceList = invoiceRepository.findByRoleName(roleName);
		}

		if (!invoiceList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK).body(invoiceList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(new ArrayList<>());
		}
	}

	@GetMapping("/InboxData")
	public ResponseEntity<List<Invoice>> getInvoicesByUsernameAndStatus(@RequestParam("username") String username) {

		// Retrieve invoices with status "reverted"
		List<Invoice> invoiceList = invoiceRepository.findByUsernameAndStatus(username, "reverted");

		if (!invoiceList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK).body(invoiceList);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(new ArrayList<>());
		}
	}

	@GetMapping("/getClaimedInbox")
	public ResponseEntity<List<Invoice>> getAllInvoices(@RequestHeader(required = false) Boolean claimed,
			@RequestHeader("claimedBy") String claimedBy) {

		try {
			List<Invoice> invoiceList = new ArrayList<>();

			if (claimed != null && claimed && claimedBy != null) {
				// If claimed is true and claimedBy is provided, retrieve claimed invoices
				invoiceList = invoiceRepository.findByClaimedAndClaimedBy(true, claimedBy);
			}

			// Always return an empty list to hide data when claimed is not true
			return ResponseEntity.status(HttpStatus.OK).body(invoiceList);
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
		if (invoiceOptional.isEmpty()) {
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
				newFiles.add(new DocDetails(invoicefile.getOriginalFilename(), newFileUrl));
			}

			invoice.getInvoiceFile().addAll(newFiles);

			// Update the supporting document in the database (modify based on your entity
			// structure)
			List<DocDetails> newSupportingDocuments = new ArrayList<>();
			if (supportingDocument != null) {
				String newDocUrl = s3service.uploadFile(supportingDocument);
				newSupportingDocuments.add(new DocDetails(supportingDocument.getOriginalFilename(), newDocUrl));
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
	public ResponseEntity<List<Invoice>> getInvoicesByReceiver(@RequestParam("receiver") String receiver) {
		// Retrieve invoices with the given receiver username
		List<Invoice> invoices = invoiceRepository.findByReceiver(receiver);

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
	public ResponseEntity<Map<String, Integer>> getInvoiceCount(@RequestParam(value = "username") String username) {

		List<Invoice> invoicesByUsername = invoiceRepository.findByUsername(username);
		int invoiceCountByUsername = invoicesByUsername.size();

		// Calculate inbox count based on username and status "reverted"
		long inboxCountReverted = invoiceRepository.countByUsernameAndStatus(username, "reverted");

		Map<String, Integer> response = new HashMap<>();
		response.put("invoiceCount", invoiceCountByUsername);
		response.put("inboxCount", (int) inboxCountReverted);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/get-compliances") // to get all the compliances by username, username is required in path
	public ResponseEntity<?> getCompliances(@RequestParam("username") String username) {
		List<DocumentsMongo> compliances = documentRepository.findAllByusername(username);
		return ResponseEntity.ok(compliances);
	}

	@GetMapping("poSearch")
	public ResponseEntity<?> searchPOByPrefix(@RequestParam(value = "poNumber") String poNumber) {

		List<String> ans = new ArrayList<>();

		List<Invoice> poList = invoiceRepository.findByPoNumberContaining(poNumber);

		for (Invoice invoice : poList) {
			ans.add(invoice.getPoNumber());
		}

		return ResponseEntity.ok(ans);

	}

	@GetMapping("/deliveryPlants")
	public List<String> getAllDeliveryPlants() {
		List<Invoice> entities = invoiceRepository.findAll();
		return entities.stream().map(Invoice::getDeliveryPlant) // Replace with the actual method to get the //
																// deliveryPlant field
				.distinct().collect(Collectors.toList());
	}

	@GetMapping("/invoices-by-username")
	public ResponseEntity<List<Invoice>> getInvoicesByUsername1(@RequestParam("username") String username) {
		List<Invoice> invoices = invoiceRepository.findByUsername(username);

		if (!invoices.isEmpty()) {

			return ResponseEntity.status(HttpStatus.OK).body(invoices);
		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No invoices found for the given username");
		}
	}

}
