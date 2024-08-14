package com.application.main.controller;


import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.services.s3.AmazonS3;
import com.application.main.Repositories.DocDetailsRepository;
import com.application.main.Repositories.DocumentRepository;
import com.application.main.Repositories.InvoiceRepository;
import com.application.main.Repositories.PoSummaryRepository;
import com.application.main.Repositories.VendorUserRepository;
import com.application.main.URLCredentialModel.CipherEncDec;
import com.application.main.URLCredentialModel.DocDetails;
import com.application.main.awsconfig.AWSClientConfigService;
import com.application.main.awsconfig.AwsService;
import com.application.main.model.DocumentsMongo;
import com.application.main.model.PoDTO;
import com.application.main.model.PoSummary;
import com.application.main.model.VendorUserModel;
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

	@Autowired
	private AWSClientConfigService s3client;

	@Autowired
	DocDetailsRepository docdetailsrepository;

	private final MongoTemplate mongoTemplate;

	@Autowired
	DocumentRepository documentRepository;

	@Autowired
	VendorUserRepository vendoruserrepo;
	@Autowired
	InvoiceRepository invoiceRepository;

	@GetMapping("/version")
	public String version() {
		return "vendorPortalVersion: 1";
	}

	public POController(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}


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
		String username = s3service.getUserNameFromToken(token);
		s3service.createBucket(token, username);
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
		System.out.println("Po Creation Initiated : UserName -> " + username);

		try {
			LocalDate issuedt = LocalDate.parse(poIssueDate, formatter);
			LocalDate delt = LocalDate.parse(deliveryDate, formatter);
			DocDetails PoUploadObject = s3service.uploadFile(filePO, poNumber, "");
//			DocDetails doc = new DocDetails(filePO.getOriginalFilename(), poNumber, PoUploadObject.get("generatedURL").toString(),(SecretKey) PoUploadObject.get("secretkey"));

			PoSummary ps = new PoSummary(poNumber, description, issuedt, delt, deliveryPlant, deliveryTimelines, 0, eic,
					poAmount, receiver, username, PoUploadObject.getUrl());
			System.out.println("------------------------------------");
			ps.setUsername(username);
			System.err.println("PO File URL : " + PoUploadObject.getUrl());
			ps.setDoc(PoUploadObject);
			porepo.save(ps);
			System.err.println("Po Creation Successfully Ended ! ");
		} catch (Exception e) {
			System.out.println("Exception Found");
			e.printStackTrace();
			return ResponseEntity.ok(e);
		}
		return ResponseEntity.ok("Po Creation Successfully Ended ! , saved" + HttpStatus.ACCEPTED);
	}

	

	@GetMapping("/GetPo")
	public ResponseEntity<?> getPurchaseOrder(@RequestParam String ponumber) {
		System.out.println("Getting Purchase Order");
		Optional<PoSummary> po = porepo.findByPoNumber(ponumber);
		if (po == null)
			return ResponseEntity.ok(HttpStatus.NOT_FOUND);
		PoDTO podto = new PoDTO(po.get().getId(), po.get().getPoNumber(), po.get().getDescription(),
				po.get().getPoIssueDate(), po.get().getDeliveryDate(), po.get().getPoStatus(), po.get().getPoAmount(),
				po.get().getNoOfInvoices(), po.get().getDeliveryTimelines(), po.get().getDeliveryPlant(),
				po.get().getEic(), po.get().getReceiver(), po.get().getUrl());
		return ResponseEntity.ok(podto);

	}

	@GetMapping("/getAllRoles")
	public List<String> getAllRoles() {
		List<String> roles = Arrays.asList("HR", "Finance", "Admin", "Admin2");
		return roles;
	}

//	@GetMapping("/poSummary/getSummary")
//	public Page<PoDTO> getPobyUsername(@RequestHeader(value = "userName", required = false) String username,
//			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
//		List<PoSummary> polist = porepo.findByUsername(username);
//		
//
//		if (polist.isEmpty())
//			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "NO PO FOUND!", null);
//		Page<PoSummary> purchaseorderpage = convertListToPage(polist, page, size);
//		 
//		 return purchaseorderpage.map(po -> new PoDTO(po.getId(), po.getPoNumber(), po.getDescription(),
//					po.getPoIssueDate(), po.getDeliveryDate(), po.getPoStatus(),po.getPoAmount(),
//					po.getNoOfInvoices(),po.getDeliveryTimelines(),po.getDeliveryPlant(),po.getEic(),po.getReceiver(),po.getUrl()));
//	}

	@GetMapping("/getAllPo")
	public Page<PoDTO> getAllPo(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		System.out.println(page + " " + size);
		Pageable pageable = PageRequest.of(page, size, Sort.by("poIssueDate").descending());
		Page<PoSummary> poPage = porepo.findAll(pageable);

		return poPage.map(po -> new PoDTO(po.getId(), po.getPoNumber(), po.getDescription(), po.getPoIssueDate(),
				po.getDeliveryDate(), po.getPoStatus(), po.getPoAmount(), po.getNoOfInvoices(),
				po.getDeliveryTimelines(), po.getDeliveryPlant(), po.getEic(), po.getReceiver(), po.getUrl()));
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
	
	

//	public Set<String> getAllPoNumber() {
//	    List<PoSummary> polist = porepo.findAll();
//	    return polist.stream()
//	                 .map(PoSummary::getPoNumber) // Assuming getPoNumber() returns a String
//	                 .collect(Collectors.toSet());
//	}


	@GetMapping("/email")
	public ResponseEntity<String> getEmailByEic(@RequestParam("eic") String eic) {
		VendorUserModel user = vendoruserrepo.findByEic(eic);

		if (user != null) {
			return ResponseEntity.ok(user.getVendoremail());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/poSummary/getSummary")
	public ResponseEntity<Page<PoDTO>> searchInvoices(
			@RequestHeader(value = "FilterBy" , required = false) String poStatus,
			@RequestHeader(value = "Fromdate") String fromdate,
			@RequestHeader(value = "Todate") String todate,			
			@RequestHeader(value = "searchItems", required = false) String searchItems,
			@RequestHeader(value = "username", required = true) String username,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "10", required = false) int size) {
		Page<PoDTO> poDTOpage = null;
		Page<PoSummary> purchaseorderpage;
		List<PoSummary> purchaseorders = null;
		try {
			Criteria criteria = new Criteria().where("username").is(username);
			if((poStatus ==null || poStatus.equalsIgnoreCase("All"))) {
				purchaseorders = porepo.findByUsername(username);
			}
			if (searchItems != null && !searchItems.isEmpty()) {
				System.out.println(searchItems);
				criteria = criteria.andOperator(criteria,
						new Criteria().orOperator(Criteria.where("poNumber").regex(searchItems),
							Criteria.where("deliveryTimelines").regex(searchItems),
							Criteria.where("description").regex(searchItems),
							Criteria.where("deliveryPlant").regex(searchItems),
							Criteria.where("poStatus").regex(searchItems),
							Criteria.where("eic").regex(searchItems),
							Criteria.where("remarks").regex(searchItems),
							Criteria.where("receiver").regex(searchItems),
							Criteria.where("invoiceobject.invoiceAmount").regex(searchItems),
							Criteria.where("invoiceobject.invoiceNumber").regex(searchItems),
							Criteria.where("poAmount").regex(searchItems),
							Criteria.where("createdBy").regex(searchItems)));
			}
			else if (poStatus != null && !poStatus.isEmpty())
					criteria = criteria.andOperator(Criteria.where("status").regex(poStatus));
			purchaseorders = mongoTemplate.find(Query.query(criteria), PoSummary.class);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate Fromdate = LocalDate.parse(fromdate, formatter);
			LocalDate Todate = LocalDate.parse(todate,formatter);
			
			List<PoSummary>  purchaseordersbydate = porepo.findByPoIssueDateBetween(Fromdate, Todate);
			if(!purchaseorders.isEmpty() && !purchaseordersbydate.isEmpty())
			purchaseorders = purchaseorders.stream().filter(obj1 -> purchaseordersbydate.stream().anyMatch(obj2-> obj2.getPoNumber().equals(obj1.getPoNumber()))).toList();
			
			purchaseorderpage = convertListToPage(purchaseorders, page, size);
			poDTOpage = convertPoAsPODTO(purchaseorderpage);
			poDTOpage.forEach(System.out::println);
			return ResponseEntity.ok(poDTOpage);
		}catch(Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
	}

	

	private Page<PoDTO> convertPoAsPODTO(Page<PoSummary> purchaseorderpage) {
		return purchaseorderpage.map(po -> new PoDTO(po.getId(), po.getPoNumber(), po.getDescription(),
				po.getPoIssueDate(), po.getDeliveryDate(), po.getPoStatus(),po.getPoAmount(),
				po.getNoOfInvoices(),po.getDeliveryTimelines(),po.getDeliveryPlant(),po.getEic(),po.getReceiver(),po.getUrl()));
	}

	private Page<PoSummary> convertListToPage(List<PoSummary> purchaseorders, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		int start = Math.min((int) pageable.getOffset(), purchaseorders.size());
		int end = Math.min((start + pageable.getPageSize()), purchaseorders.size());
		List<PoSummary> subList = purchaseorders.subList(start, end);
		return new PageImpl<>(subList, pageable, purchaseorders.size());
	}

	@GetMapping("/getFileURLObject")
	public ResponseEntity<?> getObject(@RequestHeader(value = "url") String url,
			@RequestHeader("Authorization") String token) throws Exception {
		
		token = token.replace("Bearer ", "");
		Optional<DocDetails> existingdoc = docdetailsrepository.findByUrl(url);
		if (!existingdoc.isPresent())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		
		String key = existingdoc.get().getBase64Encodedsecretkey();
		SecretKey secretkey = convertBase64ToSecretKey(key);
		
		url = new CipherEncDec().decrypt(url, secretkey);
		int index = url.indexOf("123");
		
		String bucketname = url.substring(0, index);
		String filename = url.substring(index + 3);

		AmazonS3 s3 = s3client.awsClientConfiguration(token);
		InputStream inputStream = s3.getObject(bucketname, filename).getObjectContent();
		byte arr[] = IOUtils.toByteArray(inputStream);
		ByteArrayResource resource = new ByteArrayResource(arr);
		System.out.println("url : " + url + " : FILENAME " + filename);
		return ResponseEntity
				.ok()
				.contentLength(arr.length)
				.header("Content-type", "application/octet-stream")
				.header("Content-disposition", "attachment; filename=\"" + filename + "\"")
				.body(resource);		
		
	}

	public static SecretKey convertBase64ToSecretKey(String base64Key) {
		byte[] decodedKey = Base64.getDecoder().decode(base64Key);
		SecretKey sec = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		System.err.println(sec);
		return sec;
	}

	

	@GetMapping("/get-compliances") // to get all the compliances by username, username is required in path
	public ResponseEntity<?> getCompliances(@RequestParam("username") String username) {
		List<DocumentsMongo> compliances = documentRepository.findAllByusername(username);
		return ResponseEntity.ok(compliances);
	}

	

}
