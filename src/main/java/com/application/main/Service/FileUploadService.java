package com.application.main.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.application.main.Repositories.DocumentDetailsRepository;
import com.application.main.Repositories.InvoiceRepository;
import com.application.main.Repositories.LoginUserRepository;
import com.application.main.Repositories.PoSummaryRepository;
import com.application.main.config.awsconfig.AwsConfigService.AWSClientConfigService;
import com.application.main.model.DocumentDetails;
import com.application.main.model.Invoice;
import com.application.main.model.PoSummary;
import com.application.main.model.NotificationModel.VendorPortalNotification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FileUploadService {

//	@Value("${bucket_name}")
	private String bucketName;
	@Value("${minio_url}")
	private String minioUrl;
	@Autowired
	private AWSClientConfigService s3client;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	@Autowired
	InvoiceRepository invoicerepository;
	
	@Autowired
	LoginUserRepository loginrepo;

	@Autowired
	PoSummaryRepository porepo;

	@Autowired
	DocumentDetailsRepository documentDetailsRepository;
	
	KafkaTemplate<String, Invoice> ktemplate;
	KafkaTemplate<String, VendorPortalNotification> notificationtemplate;

	public static final String ALGORITHM = "AES";
	public static final int KEY_SIZE = 256;

	public void createBucket(String token, String folderappend) {
		this.bucketName = "vendorportalfiles" + folderappend;
		try {
			AmazonS3 awsClient = s3client.awsClientConfiguration(token);
			if (!awsClient.doesBucketExistV2(bucketName))
				awsClient.createBucket(bucketName);
			System.out.println("bucket name : " + bucketName);
		} catch (AmazonS3Exception e) {
			System.out.println("CREATE BUCKET EXCEPTION");
			e.printStackTrace();
		}
	}

	public String getUserNameFromToken(String token) {
		String tokenBody = token.split("\\.")[1];
		Base64.Decoder decoder = Base64.getUrlDecoder();
		String payload = new String(decoder.decode(tokenBody));
		System.out.println("Getting Username from payload");
		return getFieldFromJson(payload, "preferred_username");
	}

	private String getFieldFromJson(String json, String fieldName) {
		String fieldValue = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(json);
			System.out.println(jsonNode.toPrettyString());
			fieldValue = jsonNode.get(fieldName).asText();
			System.out.println("------------------------------------");
			System.out.println(fieldName + " : " + fieldValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fieldValue;
	}

	public DocumentDetails uploadFile(String token, MultipartFile file, String id, String username)
			throws IOException, Exception {
		System.out.println("uploading file attached");
		System.out.println("Getting token for AWS Service : ");
		String fileName = id.concat("?#" + file.getOriginalFilename());
		AmazonS3 awsClient = s3client.awsClientConfiguration(token);

		if (file == null || file.isEmpty()) {
			System.out.println("File is null");
			return new DocumentDetails();
		}
		if (awsClient.doesObjectExist(bucketName, fileName)) {
			System.err.println("FILE WITH " + file.getOriginalFilename() + " Already Exists !");
			System.err.println("-------------Replacing file without permission-------------");
			awsClient.deleteObject(bucketName, fileName);
		}

		System.out.println("UPLOADING FILES ................");

		PutObjectResult res = awsClient.putObject(bucketName, fileName, file.getInputStream(), new ObjectMetadata());
		String invoiceFileUrl = bucketName + "XCIDHK2788k99BBSEEL99" + fileName;

		SecretKey secretkey = generateSecretKey();
		invoiceFileUrl = new EncDecService().encrypt(invoiceFileUrl, secretkey);
		String EncodedSecretKey = Base64.getEncoder().encodeToString(secretkey.getEncoded());
		HashMap<String, Object> response = new HashMap<>();
		response.put("Response", res);
		response.put("fileName", file.getOriginalFilename());
		response.put("generatedURL", invoiceFileUrl);

		DocumentDetails newdoc = new DocumentDetails(fileName, id, invoiceFileUrl, EncodedSecretKey);

//		awsClient.getObject(bucketName, fileName);
		System.err.println("---------- Upload Response Object START ------------");
		for (Map.Entry<String, Object> ele : response.entrySet()) {
			System.out.println(ele.getKey() + " : " + ele.getValue().toString());
		}
		System.err.println("---------- Upload Response Object END ------------");
		documentDetailsRepository.save(newdoc);
		return newdoc;

	}

	public SecretKey generateSecretKey() throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
		keyGenerator.init(KEY_SIZE);
		return keyGenerator.generateKey();
	}
}
	
