package com.application.main.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.application.main.Repositories.DocumentDetailsRepository;
import com.application.main.Repositories.InvoiceRepository;
import com.application.main.Repositories.LoginUserRepository;
import com.application.main.Repositories.PoSummaryRepository;
import com.application.main.Repositories.PaymentRepository.PaymentDetailsRepository;
import com.application.main.Service.DigitalSignValidationService;
import com.application.main.Service.FileUploadService;
import com.application.main.Service.InvoiceService;
import com.application.main.model.DocumentDetails;
import com.application.main.model.Invoice;
import com.application.main.model.InvoiceDTO;
import com.application.main.model.PoSummary;
import com.application.main.model.UserModel.UserDetails;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/call/vendor/Vendorportal")
public class InvoiceController {

	@Autowired
	FileUploadService fileUploadService;

	@Autowired
	PaymentDetailsRepository paymentrepo;

	@Autowired
	PoSummaryRepository porepo;

	@Autowired
	DocumentDetailsRepository docdetailsrepository;

	@Autowired
	DigitalSignValidationService digitalSignVerificationObject;

	private final MongoTemplate mongoTemplate;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Autowired
	LoginUserRepository loginuserrepository;

	@Autowired
	InvoiceRepository invoiceRepository;
	
	@Autowired
	InvoiceService invoiceService;

	@GetMapping("/versioninvoice")
	public String version() {
		return "vendorPortalVersion: 1";
	}

	public InvoiceController(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
	
	Logger logApp = LoggerFactory.getLogger(InvoiceController.class);

	@GetMapping("/uploadInvoice/poSearch")
	public Set<?> getAllPoNumber(@RequestParam(value = "ponumber") String ponumber) {
		List<PoSummary> polist = porepo.findByPoNumberContaining(ponumber);
		Set<String> poNumberlist = new HashSet<>();
		for (PoSummary p : polist) {
			poNumberlist.add(p.getPoNumber());
		}
		return poNumberlist;
	}

	@GetMapping("/getdeliveryplants")
	public Set<String> deliveryplants(@RequestHeader("poNumber") String poNumber) {
		return porepo.findByPoNumber(poNumber).get().getDeliveryPlant();
	}

//	@GetMapping("/getInvoiceHistory")
//	public ResponseEntity<?> gethistory(@RequestParam(value = "invoiceNo" , required = true) String invoiceno,
//			@RequestParam(value = "status" , required = false) String status){
//		List<InvoicesHistory> invoicelist;
//		if(status==null) {
//			invoicelist = invrepo.findByInvoiceNoOrderByInvoicedate(invoiceno);
//		}
//		else invoicelist = invrepo.findByInvoiceNoAndStatus(invoiceno, status);
//		return ResponseEntity.ok(invoicelist.so);
//		
//	}

	@DeleteMapping("/deleteSingleInvoice")
	public ResponseEntity<String> deleteInvoice(@RequestHeader("id") String id,
			@RequestHeader("poNumber") String poNumber) {
		// Assuming you have a method in your repository to delete an invoice by ID
		try {
			Optional<Invoice> invoice = invoiceRepository.findById(id);
			if (!invoice.isPresent())
				return ResponseEntity.ok("Not found any Invoice with Reference Id : " + id);
			PoSummary po = porepo.findByPoNumber(poNumber).get();
			Map<String, String> invoicemap = po.getInvoiceidlist();
			invoicemap.remove(id);
			po.setInvoiceidlist(invoicemap);
			porepo.save(po);
			invoiceRepository.deleteById(id);
			return ResponseEntity.ok("Invoice deleted successfully");
		} catch (Exception e) {
			e.printStackTrace(); // Log the exception
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting invoice");
		}
	}

	@PostMapping("/uploadInvoice")
	public ResponseEntity<?> createInvoice(@RequestParam(value = "poNumber") String poNumber,
			@RequestParam(value = "paymentType", required = false) String paymentType,
			@RequestParam(value = "deliveryPlant") String deliveryPlant,
			@RequestParam(value = "invoiceDate") String invoiceDate,
			@RequestParam(value = "invoiceNumber") String invoiceNumber,
			@RequestParam(value = "invoiceAmount", required = false) String invoiceAmount,
			@RequestParam(value = "mobileNumber") String mobileNumber, @RequestParam(value = "email") String email,
			@RequestParam(value = "alternateMobileNumber", required = false) String alternateMobileNumber,
			@RequestParam(value = "alternateEmail", required = false) String alternateEmail,
			@RequestParam(value = "remarks", required = false) String remarks,
			@RequestParam(value = "ses", required = false) String ses,
			@RequestParam(value = "isagainstLC") boolean isagainstLC, @RequestParam(value = "isGst") boolean isGst,
			@RequestParam(value = "isTredExchangePayment", required = false) boolean isTredExchangePayment,
			@RequestParam(value = "factoryunitnumber", required = false) String factoryunitnumber,
			@RequestParam(value = "isMDCCPayment", required = false) boolean isMDCCPayment,
			@RequestParam(value = "mdccnumber", required = false) String mdccnumber,
			@RequestParam(value = "sellerGst", required = false) String sellerGst,
			@RequestParam(value = "buyerGst", required = false) String buyerGst,
			@RequestParam(value = "bankaccountno", required = false) String bankaccountno,
			@RequestParam(value = "file") MultipartFile invoiceFile,
			@RequestParam(name = "supportingDocument", required = false) List<MultipartFile> supportingDocument,
			HttpServletRequest request) throws Exception {

		String token = request.getHeader("Authorization").replace("Bearer ", "");
		String username = fileUploadService.getUserNameFromToken(token);
		logApp.info("---------------Invoice Creation Initiated !!!------------  ");
		boolean signed = digitalSignVerificationObject.verify(invoiceFile);
		if (!signed) {
			logApp.error("Digital Signed not verified");
			return ResponseEntity.ok(HttpStatus.METHOD_FAILURE).ok("Please upload a valid digitally signed invoice");
		}
		else logApp.info("Digitally Signed Invoice");
		fileUploadService.createBucket(token, username);
		DocumentDetails InvoiceuploadResponse = fileUploadService.uploadFile(token, invoiceFile, invoiceNumber, username);
		List<DocumentDetails> suppDocNameList = new ArrayList<>();
		if (supportingDocument != null) {
			System.out.println("-------UPLOADING SUPPORTING DOCUMENTS--------");
			supportingDocument.forEach(document -> {
				try {
					int i = 0;
					DocumentDetails documentUploadObject = fileUploadService.uploadFile(token, document,
							invoiceNumber.concat("SDoc" + String.valueOf(i++)), username);
					suppDocNameList.add(documentUploadObject);
				} catch (Exception e) {
					System.out.println("Supporting docs exception");
					e.printStackTrace();
				}
			});
		}
//		String msmecategory = porepo.findByPoNumber(poNumber).orElseGet(null).getMsmecategoy();
		String msmecategory = null;
		return invoiceService.createInvoice(username, msmecategory, poNumber, paymentType,
				deliveryPlant, invoiceDate, invoiceNumber, invoiceAmount, mobileNumber, email, alternateMobileNumber,
				alternateEmail, remarks, ses, isagainstLC, isGst, isTredExchangePayment, factoryunitnumber,
				isMDCCPayment, mdccnumber, sellerGst, buyerGst, bankaccountno, InvoiceuploadResponse, suppDocNameList);
	}

	@GetMapping("/getEicUsers")
	public List<UserDetails> getEicUsernames() {
		return loginuserrepository.findByEic(true);
	}

	@GetMapping("searchInvoices")
	public ResponseEntity<?> searchInvoices(@RequestHeader(value = "Filterby") String invoiceStatus,
			@RequestHeader(value = "Fromdate") String fromdate, @RequestHeader(value = "Todate") String todate,
			@RequestHeader(value = "searchItems", required = false) String searchItems,
			@RequestHeader(value = "username", required = true) String username,
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size) {
		Page<InvoiceDTO> invoicedtopage = null;
		Page<Invoice> invoicepage;
		List<Invoice> invoices = null;
		try {
			Criteria criteria = new Criteria().where("username").is(username);
			if (!invoiceStatus.equalsIgnoreCase("All")) {
				Pattern statusPattern = Pattern.compile(invoiceStatus, Pattern.CASE_INSENSITIVE);
				criteria = criteria.and("status").regex(statusPattern);
			}
			if (searchItems != null && !searchItems.isEmpty()) {
				System.out.println(searchItems);
				criteria = criteria.andOperator(new Criteria().orOperator(Criteria.where("poNumber").regex(searchItems),
						Criteria.where("invoiceNumber").regex(searchItems), Criteria.where("status").regex(searchItems),
						Criteria.where("mobileNumber").regex(searchItems),
						Criteria.where("deliveryPlant").regex(searchItems),
						Criteria.where("roleName").regex(searchItems), Criteria.where("paymentType").regex(searchItems),
						Criteria.where("receievedBy").regex(searchItems), Criteria.where("docId").regex(searchItems),
						Criteria.where("eic").regex(searchItems), Criteria.where("msmeCategory").regex(searchItems)));
			}
			invoices = mongoTemplate.find(Query.query(criteria), Invoice.class);
			List<Invoice> invoices1 = invoiceRepository.findByInvoiceDateBetween(LocalDate.parse(fromdate, formatter),
					LocalDate.parse(todate, formatter));
			invoices = invoices.stream().filter(obj1 -> invoices1.stream()
					.anyMatch(obj2 -> obj2.getInvoiceNumber().equals(obj1.getInvoiceNumber()))).toList();
			if (invoices.isEmpty())
				return ResponseEntity.ok(null).ok("No Data found !");
			invoicepage = convertListToPage(invoices, page, size);
			if (invoicepage == null)
				return ResponseEntity.ok(null).ok("No Data found !");
			invoicedtopage = convertInvoicetoInvoiceDTO(invoicepage);
			invoicedtopage.forEach(System.out::println);
			return ResponseEntity.ok(invoicedtopage).ok(invoicedtopage.getTotalElements() + " no. of Invoices found");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@GetMapping("InvoiceSearchbyPONumber")
	public ResponseEntity<?> searchPOByPrefix(@RequestParam(value = "poNumber") String poNumber,
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<InvoiceDTO> poList = invoiceRepository.findByPoNumberContaining(poNumber, pageable);
		return ResponseEntity.ok(poList);
	}

//	@PostMapping("/forwarding")
//	public ResponseEntity<?> forwardInvoice(@RequestParam("id") String id, @RequestParam("roleName") String roleName,
//			@RequestParam("remarks") List<String> remarks,
//			@RequestParam(value = "file", required = false) MultipartFile invoicefile,
//			@RequestParam(value = "supportingDocument", required = false) MultipartFile supportingDocument) {
//
//		// Retrieve the invoice from the database using the invoiceId
//		Optional<Invoice> invoiceOptional = invoiceRepository.findById(id);
//		if (invoiceOptional.isEmpty()) {
//			return ResponseEntity.notFound().build();
//		}
//
//		Invoice invoice = invoiceOptional.get();
//
//		// Log the roleName for debugging
//		System.out.println("Role Name: " + roleName);
//
//		// Update fields based on roleName
//		try {
//			if (roleName.startsWith("vi-")) {
//				invoice.setStatus("pending");
//			} else if (roleName.equalsIgnoreCase("finance") || roleName.equalsIgnoreCase("hr")) {
//				invoice.setStatus("inProgress");
//				System.out.println("HR/Finance Role Detected");
//			} else if (roleName.equalsIgnoreCase("admin") || roleName.equalsIgnoreCase("admin2")) {
//				// Log for debugging
//				System.out.println("Admin Role Detected");
//
//				// Set specific fields for admin roles
//			}
//
//			// Append the new remarks to the existing list
//			if (remarks != null) {
//				if (invoice.getRemarks() == null) {
//					invoice.setRemarks(new HashSet<>());
//				}
//				invoice.getRemarks().addAll(remarks);
//			}
//
//			// Update the file in the database (modify based on your entity structure)
//			List<DocumentDetails> newFiles = new ArrayList<>();
//			if (invoicefile != null) {
//				DocumentDetails FileUploadResponse = fileUploadService.uploadFile(invoicefile,
//						invoiceOptional.get().getInvoiceNumber(), invoiceOptional.get().getUsername());
//				newFiles.add(FileUploadResponse);
//			}
//
//			invoice.getInvoiceFile().addAll(newFiles);
//
//			// Update the supporting document in the database (modify based on your entity
//			// structure)
//			List<DocumentDetails> newSupportingDocuments = new ArrayList<>();
//			if (supportingDocument != null) {
//				DocumentDetails DocumentUploadResponse = fileUploadService.uploadFile(supportingDocument,
//						invoiceOptional.get().getInvoiceNumber(), invoiceOptional.get().getUsername());
//				newSupportingDocuments.add(DocumentUploadResponse);
//			}
//
//			// Append the new supporting documents to the existing list in the database
//			// (modify based on your entity structure)
//			invoice.getSupportingDocument().addAll(newSupportingDocuments);
//
//			// Save the updated invoice to the database
//			Invoice updatedInvoice = invoiceRepository.save(invoice);
//
//			return ResponseEntity.ok(updatedInvoice);
//		} catch (Exception e) {
//			e.printStackTrace(); // Log the exception
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating invoice");
//		}
//	}

	@GetMapping("getInvoiceCount")
	public ResponseEntity<Map<String, Long>> getInvoiceCount(
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size,
			HttpServletRequest request) {

		String token = request.getHeader("Authorization").replace("Bearer ", "");
		String username = fileUploadService.getUserNameFromToken(token);
		Page<InvoiceDTO> invoicesByUsername = convertInvoicetoInvoiceDTO(
				convertListToPage(invoiceRepository.findByUsernameContaining(username), page, size));
		long invoiceCountByUsername = invoicesByUsername.getTotalElements();
		// Calculate inbox count based on username and status "reverted"
		long inboxCountReverted = invoiceRepository.countByUsernameContainingAndStatus(username, "reverted");
		Map<String, Long> response = new HashMap<>();
		response.put("InvoiceCountTotalof_" + username, invoiceCountByUsername);
		response.put("inboxCount", inboxCountReverted);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("getData")
	public Page<InvoiceDTO> getInvoicesByParameters(@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "poNumber", required = false) String poNumber,
			@RequestParam(value = "invoiceNumber", required = false) String invoiceNumber,
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);
		if (poNumber == null && invoiceNumber == null && username == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide atleast one detail !!");
		}
		if (username != null && poNumber == null && invoiceNumber == null) {
			return convertInvoicetoInvoiceDTO(
					convertListToPage(invoiceRepository.findByUsernameContaining(username), page, size));
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
			return invoiceRepository.findByUsernameContainingAndPoNumber(username, poNumber, pageable);
		}

		// Find by username and invoiceNumber
		if (username != null && invoiceNumber != null) {
			return invoiceRepository.findByUsernameContainingAndInvoiceNumber(username, invoiceNumber, pageable);
		}

		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "NO DATA FOUND");
	}

	@GetMapping("/getDash")
	public ResponseEntity<Map<String, ?>> getInvoicesByUsername11(
			@RequestHeader(value = "Username", required = true) String username,
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size) {
		Page<InvoiceDTO> invoices = convertInvoicetoInvoiceDTO(
				convertListToPage(invoiceRepository.findByUsernameContaining(username), page, size));
		if (invoices == null || invoices.isEmpty()) {
			return ResponseEntity.ok(Map.of("Error", "No Data Found"));
		}

		long totalCount = invoices.getTotalElements(); // Use the size of the filtered list
		long approvedCount = invoices.stream().filter(invoice -> "approved".equalsIgnoreCase(invoice.getStatus()))
				.count();
		long pendingCount = invoices.stream().filter(invoice -> "pending".equalsIgnoreCase(invoice.getStatus()))
				.count();
		long inProgressCount = invoices.stream().filter(invoice -> "reverted".equalsIgnoreCase(invoice.getStatus()))
				.count() + pendingCount;
		long rejectedCount = invoices.stream().filter(invoice -> "rejected".equalsIgnoreCase(invoice.getStatus()))
				.count();
		if (inProgressCount > totalCount)
			inProgressCount = totalCount;
		Map<String, Long> statusCounts = new HashMap<>();

		statusCounts.put("TotalInvoices", totalCount);
		statusCounts.put("Approved", approvedCount);
		statusCounts.put("Pending", pendingCount);
		statusCounts.put("Rejected", rejectedCount);
		statusCounts.put("inProgress", inProgressCount);

		return ResponseEntity.status(HttpStatus.OK).body(statusCounts);
	}

	@GetMapping("totalDash")
	public ResponseEntity<Map<String, ?>> getInvoiceStatistics() {
		List<Invoice> invoices = invoiceRepository.findAll(); // Fetch all invoices
		if (invoices.isEmpty()) {
			ResponseEntity.ok(Map.of("Error", "No Data found"));
		}

		long totalCount = invoices.size(); // Use the size of the list
		long approvedCount = invoices.stream().filter(invoice -> "approved".equalsIgnoreCase(invoice.getStatus()))
				.count();
		long pendingCount = invoices.stream().filter(invoice -> "pending".equalsIgnoreCase(invoice.getStatus()))
				.count();
		long rejectedCount = invoices.stream().filter(invoice -> "rejected".equalsIgnoreCase(invoice.getStatus()))
				.count();

		Map<String, Long> statusCounts = new HashMap<>();
		statusCounts.put("totalCount", totalCount);
		statusCounts.put("completed", totalCount - pendingCount);
		statusCounts.put("approved", approvedCount);
		statusCounts.put("pending", pendingCount);
		statusCounts.put("rejected", rejectedCount);

		return ResponseEntity.status(HttpStatus.OK).body(statusCounts);
	}

	@GetMapping("getDashboard")
	public ResponseEntity<Map<String, Object>> getInvoicesByUsername(
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size,
			HttpServletRequest request) {
		String token = request.getHeader("Authorization").replace("Bearer ", "");
		String username = fileUploadService.getUserNameFromToken(token);
		Page<InvoiceDTO> invoices = convertInvoicetoInvoiceDTO(
				convertListToPage(invoiceRepository.findByUsernameContaining(username), page, size));
		if (invoices.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No invoices found");
		}

		long numInvoices = invoices.getTotalElements();
		long inprogressInvoices = invoices.getTotalElements(); // This seems to be a placeholder; adjust logic if

		Map<String, Object> response = new HashMap<>();
		response.put("numInvoices", numInvoices);
		response.put("inprogressInvoices", inprogressInvoices);

		List<Map<String, Object>> modifiedInvoices = new ArrayList<>();
		for (InvoiceDTO invoice : invoices) {
			Map<String, Object> modifiedInvoice = new HashMap<>();
			modifiedInvoice.put("invoiceNumber", invoice.getInvoiceNumber()); // Add the MongoDB object ID
			modifiedInvoice.put("deliveryPlant", invoice.getDeliveryPlant());
			modifiedInvoice.put("url", invoice.getInvoiceurl());
			modifiedInvoice.put("poNumber", invoice.getPoNumber());
			modifiedInvoice.put("status", invoice.getStatus());
			modifiedInvoice.put("invoiceDate", invoice.getInvoiceDate());
			modifiedInvoice.put("payment details", invoice.getPaymentdetails());
			System.out.println("Invoice ID: " + invoice.getInvoiceNumber());
			System.out.println("Status: " + invoice.getStatus());
			System.out.println("InvoiceDate: " + invoice.getInvoiceDate());
			System.out.println("Payment Details: " + invoice.getPaymentdetails());
			modifiedInvoices.add(modifiedInvoice);
		}

		response.put("invoices", modifiedInvoices);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/getAllInvoices")
	public ResponseEntity<?> getAllInvoices(@RequestParam(defaultValue = "0", required = false) int page,
			@RequestParam(defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("invoicedate").descending());
		Page<Invoice> invoicepage = invoiceRepository.findAll(pageable);
		Page<InvoiceDTO> invoicedtopage = convertInvoicetoInvoiceDTO(invoicepage);
		return ResponseEntity.ok(invoicedtopage);
	}

	@PostMapping("/updateClaimedStatus")
	public ResponseEntity<String> updateClaimedStatus(@RequestParam("id") String id,
			@RequestParam("claimed") boolean claimed, @RequestParam("username") String username) {
		Optional<Invoice> optionalInvoice = invoiceRepository.findById(id);
		if (optionalInvoice.isPresent()) {
			Invoice invoice = optionalInvoice.get();
			invoiceRepository.save(invoice);
			return ResponseEntity.status(HttpStatus.OK).body("Claimed status updated successfully.");
		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found.");
		}
	}

//
//	@GetMapping("/getClaimedInbox")
//	public ResponseEntity<Page<InvoiceDTO>> getAllInvoices(@RequestHeader(required = false) Boolean claimed,
//			@RequestHeader("claimedBy") String claimedBy,
//			@RequestHeader(value = "pageNumber",defaultValue = "0",required = false) int page,
//			@RequestHeader(value = "pageSize",defaultValue = "10",required = false) int size) {
//		Pageable pageable = PageRequest.of(page, size);
//
//		try {
//			Page<InvoiceDTO> invoicedtopage = null;
//
//			if (claimed != null && claimed && claimedBy != null) {
//				// If claimed is true and claimedBy is provided, retrieve claimed invoices
//				invoicedtopage = invoiceRepository.findByClaimedAndClaimedBy(true, claimedBy, pageable);
//			}
//			// Always return an empty list to hide data when claimed is not true
//			return ResponseEntity.status(HttpStatus.OK).body(invoicedtopage);
//		} catch (Exception e) {
//			// Handle exceptions, log them, and return an error response
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//		}
//	}
	public Page<Invoice> convertListToPage(List<Invoice> invoiceList, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		int start = Math.min((int) pageable.getOffset(), invoiceList.size());
		int end = Math.min((start + pageable.getPageSize()), invoiceList.size());
		List<Invoice> subList = invoiceList.subList(start, end);
		return new PageImpl<>(subList, pageable, invoiceList.size());
	}

	public Page<InvoiceDTO> convertInvoicetoInvoiceDTO(Page<Invoice> invoicepage) {

		return invoicepage.map(invoiceobject -> new InvoiceDTO(invoiceobject.getId(), invoiceobject.getPoNumber(),
				invoiceobject.getInvoiceNumber(), invoiceobject.getInvoiceDate(), invoiceobject.getStatus(),
				invoiceobject.getDeliveryPlant(), invoiceobject.getMobileNumber(), invoiceobject.getEic(),
				paymentrepo.findByInvoiceNumber(invoiceobject.getInvoiceNumber()), invoiceobject.getPaymentType(),
				invoiceobject.getInvoiceurl(), invoiceobject.getInvoiceAmount(), invoiceobject.getLatestRecievingDate(),
				invoiceobject.getLatestforwardDate(), invoiceobject.getLatestforwardTime(),
				invoiceobject.getLatestRecievedTime()));
	}

}
