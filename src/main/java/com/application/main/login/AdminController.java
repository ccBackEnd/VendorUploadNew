package com.application.main.login;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.application.main.Repositories.VendorUserRepository;
import com.application.main.exception.CustomException;
import com.application.main.model.UserDTO;
import com.application.main.model.VendorUserModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/call/vendor/Vendorportal")
public class AdminController {

	private static final Logger log = LoggerFactory.getLogger(AdminController.class);

	@Autowired
	VendorUserRepository vendoruserrepo;

	@GetMapping("/xsc/ok")
	private String x() {
		return "Ok";
	}

	@PostMapping("/xsc/okk")
	private String xjk() {
		return "Ok";
	}

	@GetMapping("/checkRole")
	public HttpStatus checkUserRole(@RequestParam String role, HttpServletRequest request) {
		String token = (String) request.getHeader("Authorization").replace("Bearer ", "").trim();
		if (getRolesFromToken(token).contains(role))
			return HttpStatus.valueOf("ADMIN ROLE ACCEPTED").ACCEPTED;
		return HttpStatus.NOT_ACCEPTABLE;
	}

	@PostMapping("/admin/addUser")
	private ResponseEntity<?> addUser( @RequestParam(value = "username") String username,
	        @RequestParam(value = "firstName") String firstName,
	        @RequestParam(value = "lastName") String lastName,
//	        @RequestParam(value = "accesslevel") String accesslevel,
	        @RequestParam(value = "password") String password,
	        @RequestParam(value = "assignedrole") Set<String> assignedrole,
	        @RequestParam(value = "eicdepartments", required = false) Set<String> eicdepartments,
	        HttpServletRequest request) throws Exception {
		
		if(vendoruserrepo.existsByUsernameAndFirstNameAndLastName(username,firstName,lastName)) return ResponseEntity.ok(HttpStatus.FOUND).ok("Already a user !");
		System.out.println("----------------CREATION OF USER OBJECT STARTED-------------------");
		VendorUserModel uservum = new VendorUserModel(username,firstName ,lastName,password,assignedrole,eicdepartments);
		String token = request.getHeader("Authorization").replace("Bearer ", "").trim();
		
		try {
				RestTemplate restTemplate = new RestTemplate();
				HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					headers.setBearerAuth(token);

				Map<String, Object> user = new HashMap<>();
				System.out.println(username + " " + assignedrole);
					user.put("username", username);
					user.put("firstName", firstName);
					user.put("lastName", lastName);
//					user.put("accesslevel", userDTO.getAccesslevel());
					user.put("enabled", true);					
				Map<String, Object> credential = new HashMap<>();
					credential.put("type", "password");
					credential.put("value", password);
					credential.put("temporary", false);
					
					List<Map<String,Object>> credentials = new ArrayList<>();
					credentials.add(credential);
					user.put("credentials", credentials);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(user, headers);
		String keycloakUrl = "http://keycloak1-test.apps.ocp4.pacosta.com" + "/admin/realms/" + "master" + "/users";
		// Create user in keycloak
		ResponseEntity<?> response = restTemplate.exchange(keycloakUrl, HttpMethod.POST, requestEntity, Map.class);
		vendoruserrepo.save(uservum);
		return response;
		}
		catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(e.toString());
		}
		}

	@GetMapping("/getUserRoles")
	public ResponseEntity<HashMap<String, Object>> getUserRoles(HttpServletRequest request) {
		try {
			String username = request.getHeader("userName");
			HashMap<String, Object> Objectjsonuser = new HashMap<String, Object>();
			try {
				Optional<VendorUserModel> vuser =vendoruserrepo.findByUsername(username);
				log.info("**********Fetching User Roles**********");
				Objectjsonuser.put("status", HttpStatus.OK);
				Objectjsonuser.put("data", vuser.get() != null ? Arrays.asList(vuser.get()) : null);
				return ResponseEntity.ok(Objectjsonuser);
			} catch (Exception e) {
				e.printStackTrace();
				Objectjsonuser.put("error", e.getMessage());
				if (e.getMessage() == null) {
					throw new CustomException("No data present", HttpStatus.INTERNAL_SERVER_ERROR);
				}
//			System.out.println("errrrrrrrrr  "+e.getLocalizedMessage()+ "\n nnnnnnnnn+ "+e.getMessage());
				throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (CustomException e) {
			log.error(e.getMessage());
			throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {

			log.error(e.getMessage());
			throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private static final String client_secret = "7NKM1Xn7mwe4FFk5tgqMWJXWAmjQx97O";
	private static final String client_id = "costa_client";
	private static String realm = "master";

	@PostMapping("/getadmintoken")
	public String getToken(@RequestParam String username, @RequestParam String password) throws Exception {
		System.err.println("-------------------------------");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/x-www-form-urlencoded");

		String requestBody = "grant_type=password&client_id=" + client_id + "&client_secret=" + client_secret
				+ "&username=" + username + "&password=" + password;
		HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
		String keycloakBaseUrl = "http://keycloak1-test.apps.ocp4.pacosta.com/realms/";
		String keycloakUrl = keycloakBaseUrl + realm + "/protocol/openid-connect/token";
		RestTemplate restTemplate1 = new RestTemplate();
		ResponseEntity<Map> response = restTemplate1.exchange(keycloakUrl, HttpMethod.POST, request, Map.class);
		if (!(response.getStatusCode().is2xxSuccessful())) {
			throw new Exception();
		}
		System.out.println(response.getBody().get("access_token").toString());
		return response.getBody().get("access_token").toString();
	}

	@GetMapping("/gettokenroles")
	public List<String> getRolesFromToken(@RequestParam String token) {
		List<String> roles = new ArrayList<>();
		try {
			String tokenBody = token.split("\\.")[1];
			Base64.Decoder decoder = Base64.getUrlDecoder();
			String payload = new String(decoder.decode(tokenBody));
			System.out.println("Payload: " + payload);
			JsonNode jsonNode = new ObjectMapper().readTree(payload).path("realm_access").path("roles");
			if (jsonNode.isArray()) {
				roles = new ObjectMapper().convertValue(jsonNode, List.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.singletonList(HttpStatus.NOT_FOUND.toString());
		}
		return roles;
	}

//	@GetMapping("/updatePOI")
//	public ResponseEntity<?> updateClaroscrimeuser(){
//		Iterable<ClarosUsers> cusers= cUserRepo.findAll();
//		cusers.forEach(cuser->cuser.setDefaultview(true));
//		cusers.forEach(cuser->cuser.setAccessUserIdList(userRepo.findAll().stream().parallel().map(UserDTO::getId).toList()));
//		return null;
//	}

//	 public void assignRoleToUser(String userId, String roleName) {
//	        String accessToken =getToken("admin", "admin");
//	        RestTemplate restTemplate = new RestTemplate();
//
//	        HttpHeaders headers = new HttpHeaders();
//	        headers.setBearerAuth(accessToken);
//	        headers.setContentType(MediaType.APPLICATION_JSON);
//
//	        // Get role representation
//	        ResponseEntity<List<Map>> response = restTemplate.exchange(
//	            authServerUrl + "/admin/realms/" + realm + "/roles/" + roleName,
//	            HttpMethod.GET,
//	            new HttpEntity<>(headers),
//	            new ParameterizedTypeReference<List<Map>>() {
//	            });
//
//	        Map role = response.getBody().get(0);
//
//	        // Assign role to user
//	        HttpEntity<Map> roleRequest = new HttpEntity<>(role, headers);
//	        restTemplate.postForEntity(
//	            authServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm",
//	            roleRequest,
//	            Void.class
//	        );
//	    }

}
