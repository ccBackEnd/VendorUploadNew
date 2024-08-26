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
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.application.main.PaymentRepositories.PaymentDetailsRepository;
import com.application.main.Repositories.DocDetailsRepository;
import com.application.main.Repositories.DocumentRepository;
import com.application.main.Repositories.InvoiceRepository;
import com.application.main.Repositories.PoSummaryRepository;
import com.application.main.Repositories.VendorUserRepository;
import com.application.main.URLCredentialModel.DocDetails;
import com.application.main.awsconfig.AwsService;
import com.application.main.model.Invoice;
import com.application.main.model.InvoiceDTO;
import com.application.main.model.PoSummary;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/call/vendor/Vendorportal")
public class InvoiceController {

	@Autowired
	AwsService s3service;

	@Autowired
	PaymentDetailsRepository paymentrepo;

	@Autowired
	PoSummaryRepository porepo;

	@Autowired
	DocDetailsRepository docdetailsrepository;

	private final MongoTemplate mongoTemplate;

	@Autowired
	DocumentRepository documentRepository;

	@Autowired
	VendorUserRepository vendoruserrepo;

	@Autowired
	InvoiceRepository invoiceRepository;

	@GetMapping("/versioninvoice")
	public String version() {
		return "vendorPortalVersion: 1";
	}

	public InvoiceController(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

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
	public Set<String> deliveryplants(@RequestParam String poNumber) {
		return porepo.findByPoNumber(poNumber).get().getDeliveryPlant();
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
			@RequestParam("file") MultipartFile invoiceFile,
			@RequestParam(name = "supportingDocument", required = false) List<MultipartFile> supportingDocument,
			HttpServletRequest request) throws Exception {

		if (invoiceRepository.existsByInvoiceNumber(invoiceNumber))
			return ResponseEntity.ok("Invoice with Number " + invoiceNumber + " Already Exists");
		String token = request.getHeader("Authorization").replace("Bearer ", "");
		String username = s3service.getUserNameFromToken(token);

		s3service.createBucket(token, username);
		DocDetails InvoiceuploadResponse = s3service.uploadFile(invoiceFile, invoiceNumber, username);
		List<DocDetails> suppDocNameList = new ArrayList<>();

		if (supportingDocument != null) {
			supportingDocument.forEach(document -> {
				try {
					int i = 0;
					DocDetails documentUploadObject = s3service.uploadFile(document,
							invoiceNumber.concat("SDoc" + String.valueOf(i++)), username);
					suppDocNameList.add(documentUploadObject);
				} catch (Exception e) {
					System.out.println("Supporting docs exception");
					e.printStackTrace();
				}
			});
		}

		String msmecategory = porepo.findByPoNumber(poNumber).get().getMsmecategoy();
		Map<String, Object> uploadMongoFile = s3service.uploadMongoFile(username, msmecategory, poNumber, paymentType,
				deliveryPlant, invoiceDate, invoiceNumber, invoiceAmount, mobileNumber, email, alternateMobileNumber,
				alternateEmail, remarks, ses, isagainstLC, isGst, isTredExchangePayment, factoryunitnumber,
				isMDCCPayment, mdccnumber, sellerGst, buyerGst, bankaccountno, InvoiceuploadResponse, suppDocNameList);
		if (uploadMongoFile == null)
			return ResponseEntity.ok(HttpStatus.METHOD_FAILURE);
		return ResponseEntity.ok(uploadMongoFile).ok(HttpStatus.OK)
				.ok("Invoice Successfully Uploaded with referenced PO");
	}

//	@PostMapping("/uploadInvoice")
//	public ResponseEntity<?> createInvoice(
//			@RequestParam("file") MultipartFile invoiceFile,
//			@RequestParam(name = "supportingDocument", required = false) List<MultipartFile> supportingDocument,
//			@RequestParam("poNumber") String poNumber,
//			@RequestParam(value = "alternateMobileNumber", required = false) String alternateMobileNumber,
//			@RequestParam(value = "alternateEmail", required = false) String alternateEmail,
//			@RequestParam(value = "remarks", required = false) Set<String> remarks,
//			@RequestParam("invoiceAmount") String invoiceAmount, @RequestParam("invoiceDate") String invoiceDate,
//			@RequestParam("invoiceNumber") String invoiceNumber,
//			@RequestParam(name = "validityDate", required = false) String validityDate,
//			@RequestParam(name = "deliveryTimelines", required = false) String deliveryTimelines,
//			@RequestParam(name = "deliveryPlant", required = false) String deliveryPlant,
//			@RequestParam(value = "roleName", required = false) String roleName,
////			@RequestParam(value = "eic", required = false) String eic,
////			@RequestParam(name = "termAndConditions", required = false) String termAndConditions,
////			@RequestParam(name = "status", required = false) String status,
////			@RequestParam(name = "createdBy", required = false) String createdBy,
////			@RequestParam(name = "receievedBy", required = false) String receievedBy, 
//			HttpServletRequest request) throws Exception {
//		if (invoiceRepository.existsByInvoiceNumber(invoiceNumber))
//			return ResponseEntity.ok("Invoice with Number " + invoiceNumber + " Already Exists");
//		String receievedBy = roleName;
//		String token = request.getHeader("Authorization").replace("Bearer ", "");
//		String username = s3service.getUserNameFromToken(token);
//		s3service.createBucket(token, username);
//		
//		DocDetails InvoiceuploadResponse = s3service.uploadFile(invoiceFile, invoiceNumber, username);
//		List<DocDetails> suppDocNameList = new ArrayList<>();
//		
//		if (supportingDocument != null) {
//			supportingDocument.forEach(document -> {
//				try {
//					int i=0;
//					DocDetails documentUploadObject = s3service.uploadFile(document, invoiceNumber.concat("SDoc" + String.valueOf(i++)), username);
//					suppDocNameList.add(documentUploadObject);
//				} catch (Exception e) {
//					System.out.println("Supporting docs exception");
//					e.printStackTrace();
//				}
//			});
//		}
//		
//		System.out.println("receievedBy: " + receievedBy);
//		System.out.println("createdBy: " + username);
//		String eic = porepo.findByPoNumber(poNumber).get().getEic();
//		Map<String, Object> uploadMongoFile = s3service.uploadMongoFile(eic, roleName, poNumber, token,
//				alternateMobileNumber, alternateEmail, remarks, invoiceAmount, invoiceDate, invoiceNumber, username,
//				username, deliveryPlant, InvoiceuploadResponse, suppDocNameList, "Paid", receievedBy);
//		if (uploadMongoFile == null)
//			return ResponseEntity.ok(HttpStatus.METHOD_FAILURE);
//		return ResponseEntity.ok(uploadMongoFile);
//	}

	public Page<InvoiceDTO> convertInvoicetoInvoiceDTO(Page<Invoice> invoicepage) {

		return invoicepage.map(invoiceobject -> new InvoiceDTO(invoiceobject.getId(), invoiceobject.getPoNumber(),
				invoiceobject.getInvoiceNumber(), invoiceobject.getInvoiceDate(), invoiceobject.getStatus(),
				invoiceobject.getDeliveryPlant(), invoiceobject.getMobileNumber(), invoiceobject.getEic(),
				paymentrepo.findByAccountnumber(invoiceobject.getBankaccountno()), invoiceobject.getPaymentType(),
				invoiceobject.getInvoiceurl(), invoiceobject.getInvoiceAmount()));
	}

	@GetMapping("searchInvoices")
	public ResponseEntity<Page<InvoiceDTO>> searchInvoices(
			@RequestHeader(value = "FilterBy", required = false) String invoiceStatus,
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

			if (invoiceStatus == null || invoiceStatus.equalsIgnoreCase("All")) {
				invoices = invoiceRepository.findByUsername(username);
			}
			if (searchItems != null && !searchItems.isEmpty()) {
				System.out.println(searchItems);
				criteria = criteria.andOperator(criteria,
						new Criteria().orOperator(Criteria.where("poNumber").regex(searchItems),
								Criteria.where("invoiceNumber").regex(searchItems),
								Criteria.where("status").regex(searchItems),
//											Criteria.where("deliveryTimelines").regex(searchItems),
								Criteria.where("mobileNumber").regex(searchItems),
								Criteria.where("deliveryPlant").regex(searchItems),
								Criteria.where("roleName").regex(searchItems),
								Criteria.where("paymentType").regex(searchItems),
								Criteria.where("receievedBy").regex(searchItems),
								Criteria.where("docId").regex(searchItems), Criteria.where("eic").regex(searchItems),
								Criteria.where("msmeCategory").regex(searchItems)));
			} else if (invoiceStatus != null && !invoiceStatus.isEmpty())
				criteria = criteria.andOperator(Criteria.where("status").regex(invoiceStatus));
			invoices = mongoTemplate.find(Query.query(criteria), Invoice.class);
			System.out.println(invoices);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate Fromdate = LocalDate.parse(fromdate, formatter);
			LocalDate Todate = LocalDate.parse(todate, formatter);
			List<Invoice> invoices1 = invoiceRepository.findByInvoiceDateBetween(Fromdate, Todate);
			if (!invoices.isEmpty() && !invoices1.isEmpty()) {
				invoices = invoices.stream().filter(obj1 -> invoices1.stream()
						.anyMatch(obj2 -> obj2.getInvoiceNumber().equals(obj1.getInvoiceNumber()))).toList();
			}
			invoicepage = convertListToPage(invoices, page, size);
			invoicedtopage = convertInvoicetoInvoiceDTO(invoicepage);
			invoicedtopage.forEach(System.out::println);
			return ResponseEntity.ok(invoicedtopage);

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

	@GetMapping("/deliveryPlants")
	public List<String> getAllDeliveryPlants() {
		List<Invoice> entities = invoiceRepository.findAll();
		return entities.stream().map(Invoice::getDeliveryPlant) // Replace with the actual method to get the //
																// deliveryPlant field
				.distinct().collect(Collectors.toList());
	}

	@GetMapping("/invoices-by-username")
	public ResponseEntity<Page<InvoiceDTO>> getInvoicesByUsername1(@RequestParam("username") String username,
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size) {
		Page<InvoiceDTO> invoices = convertInvoicetoInvoiceDTO(
				convertListToPage(invoiceRepository.findByUsername(username), page, size));
		if (invoices.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No invoices found for the given username");
		return ResponseEntity.status(HttpStatus.OK).body(invoices);
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
				System.out.println("HR/Finance Role Detected");
			} else if (roleName.equalsIgnoreCase("admin") || roleName.equalsIgnoreCase("admin2")) {
				// Log for debugging
				System.out.println("Admin Role Detected");
				// Set specific fields for admin roles
			}
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
			} else if (roleName.equalsIgnoreCase("admin") || roleName.equalsIgnoreCase("admin2")) {
				// Log for debugging
				System.out.println("Admin Role Detected");

				// Set specific fields for admin roles
			}

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
				DocDetails FileUploadResponse = s3service.uploadFile(invoicefile,
						invoiceOptional.get().getInvoiceNumber(), invoiceOptional.get().getUsername());
				newFiles.add(FileUploadResponse);
			}
//--------------------------------
//--------------------------------
//--------------------------------
//--------------------------------
//--------------------------------
//--------------------------------
//--------------------------------
//--------------------------------
//--------------------------------
//--------------------------------
			invoice.getInvoiceFile().addAll(newFiles);

			// Update the supporting document in the database (modify based on your entity
			// structure)
			List<DocDetails> newSupportingDocuments = new ArrayList<>();
			if (supportingDocument != null) {
				DocDetails DocumentUploadResponse = s3service.uploadFile(supportingDocument,
						invoiceOptional.get().getInvoiceNumber(), invoiceOptional.get().getUsername());
				newSupportingDocuments.add(DocumentUploadResponse);
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

//	@PostMapping("/upload-multiple")
//	public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files, HttpServletRequest request) {
//		
//		String token = request.getHeader("Authorization").replace("Bearer ", "");
//		String username = getUserNameFromToken(token);
//		List<String> fileNames = Collections.synchronizedList(new ArrayList<>());
//		// Convert array into list
//		List<MultipartFile> fileslist = Arrays.asList(files);
//		System.err.println(request.getHeader("Authorization"));
//
//		fileslist.parallelStream().filter(file -> !file.isEmpty()).map(file -> {
//			try {
//				DocumentsMongo document = new DocumentsMongo();
//				document.setusername(username);
//				document.setFilename(file.getOriginalFilename());
//
//				ResponseEntity<HashMap<String,Object>> url = s3service
//						.uploadCompliance(token, file,username);
//				
//				document.setUrl(url);
//
//				if (url.getStatusCodeValue() != 200) {
//					return ResponseEntity.internalServerError().body("not uploaded successfully!!");
//				}
//				documentRepository.save(document);
//
//				synchronized (fileNames) {
//					fileNames.add(file.getOriginalFilename());
//				}
//				return ResponseEntity.ok("uploaded successfully!!");
//			} catch (Exception e) {
//				e.printStackTrace();
//				return ResponseEntity.internalServerError().body("not uploaded successfully!!");
//			}
//		}).filter(response -> response.getStatusCodeValue() != 200).findAny().ifPresent(response -> {
//			// Handle the case where at least one file was not uploaded successfully
//			throw new RuntimeException("At least one file was not uploaded successfully!!");
//		});

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
//
//		return ResponseEntity.ok("Files uploaded successfully: " + fileNames);
//	}

	@GetMapping("getInvoiceCount")
	public ResponseEntity<Map<String, Long>> getInvoiceCount(@RequestParam(value = "username") String username,
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size) {
		Page<InvoiceDTO> invoicesByUsername = convertInvoicetoInvoiceDTO(
				convertListToPage(invoiceRepository.findByUsername(username), page, size));
		long invoiceCountByUsername = invoicesByUsername.getTotalElements();

		// Calculate inbox count based on username and status "reverted"
		long inboxCountReverted = invoiceRepository.countByUsernameAndStatus(username, "reverted");

		Map<String, Long> response = new HashMap<>();
		response.put("invoiceCount", invoiceCountByUsername);
		response.put("inboxCount", inboxCountReverted);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	public Page<Invoice> convertListToPage(List<Invoice> invoiceList, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		int start = Math.min((int) pageable.getOffset(), invoiceList.size());
		int end = Math.min((start + pageable.getPageSize()), invoiceList.size());
		List<Invoice> subList = invoiceList.subList(start, end);
		return new PageImpl<>(subList, pageable, invoiceList.size());
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
					convertListToPage(invoiceRepository.findByUsername(username), page, size));
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

	@GetMapping("getDash")
	public ResponseEntity<Map<String, Object>> getInvoicesByUsername11(
			@RequestParam(value = "username") String username,
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size) {
		Page<InvoiceDTO> invoices = convertInvoicetoInvoiceDTO(
				convertListToPage(invoiceRepository.findByUsername(username), page, size));
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
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size) {
		Page<InvoiceDTO> invoices = convertInvoicetoInvoiceDTO(
				convertListToPage(invoiceRepository.findByUsername(username), page, size));
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
		if (existingremarks == null || existingremarks.isEmpty()) {
			existingremarks = new HashSet<String>();
		}
		existingremarks.addAll(remarks);
		invoice.setRemarks(existingremarks);
		invoiceRepository.save(invoice);
		return ResponseEntity.ok(invoice);
	}

	@GetMapping("/getAllInvoices")
	public ResponseEntity<?> getAllInvoices(@RequestParam(defaultValue = "0", required = false) int page,
			@RequestParam(defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("invoicedate").descending());

		Page<Invoice> invoicepage = invoiceRepository.findAll(pageable);
		Page<InvoiceDTO> invoicedtopage = convertInvoicetoInvoiceDTO(invoicepage);
		return ResponseEntity.ok(invoicedtopage);
	}

	@GetMapping("getInvoice")
	public ResponseEntity<?> getInvoicesBypoNumber(@RequestParam(value = "poNumber") String poNumber,
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size) {
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
			invoiceRepository.save(invoice);
			return ResponseEntity.status(HttpStatus.OK).body("Claimed status updated successfully.");
		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found.");
		}
	}

//	@GetMapping("/getInboxData")
//	public ResponseEntity<Page<InvoiceDTO>> getAllInvoices(@RequestHeader("roleName") String roleName,
//			@RequestHeader(value = "pageNumber",defaultValue = "0",required = false) int page,
//			@RequestHeader(value = "pageSize",defaultValue = "10",required = false) int size) {
//		Pageable pageable = PageRequest.of(page, size);
//		Page<InvoiceDTO> invoicedtopage;
//
//		if ("admin".equals(roleName) || "admin2".equals(roleName)) {
//			return ResponseEntity
//					.ok(invoiceRepository.findByRoleNameAndTypeAndClaimed(roleName, "material", false, pageable));
//		} else {
//			// For other roles, filter by roleName only
//			invoicedtopage = invoiceRepository.findByRoleName(roleName, pageable);
//		}
//
//		if (!invoicedtopage.isEmpty()) {
//			return ResponseEntity.status(HttpStatus.OK).body(invoicedtopage);
//		} else {
//			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
//		}
//	}

	@GetMapping("/InboxData")
	public ResponseEntity<Page<InvoiceDTO>> getInvoicesByUsernameAndStatus(@RequestParam("username") String username,
			@RequestHeader(value = "pageNumber", defaultValue = "0", required = false) int page,
			@RequestHeader(value = "pageSize", defaultValue = "10", required = false) int size) {
		Pageable pageable = PageRequest.of(page, size);

		// Retrieve invoices with status "reverted"
		Page<InvoiceDTO> invoiceList = invoiceRepository.findByUsernameAndStatus(username, "reverted", pageable);

		if (!invoiceList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK).body(invoiceList);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
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

}
