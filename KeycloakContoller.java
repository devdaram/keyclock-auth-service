public class KeycloakCertUserController {

	@Value("${keycloak.auth-server-url}")
	private String authServerUrl;

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${keycloak.resource}")
	private String clientId;

	@Value("${keycloak.credentials.secret}")
	private String clientSecret;

	private final Keycloak keycloak;

	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


	@Operation(description = "Get Users : 사용자를 조회합니다.")
	@GetMapping(path = "/list")
	public ResponseEntity<Message> getUsersList (@Parameter @QueryParam SearchParameterRequest searchParameterRequest) {

		RealmResource realmResource = keycloak.realm(realm);
		UsersResource usersResource = realmResource.users();

		List<UserRepresentation> response = null;
		List<UserRequest> userList = new ArrayList<>();

		if(response == null || response.size() == 0){
			Message<List<UserRequest>> message = new Message<>();
			message.setResponse("fail");
			message.setMessage("조회된 사용자가 없습니다.");
			message.setData(userList);

			return ResponseEntity.ok(message);
		}

		if((searchParameterRequest.getUserId() == null || searchParameterRequest.getUserId().equals("")) &&
				(searchParameterRequest.getUserEmail() == null || searchParameterRequest.getUserEmail().equals("")) &&
				(searchParameterRequest.getUserNickName() == null || searchParameterRequest.getUserNickName().equals(""))) {
			response = usersResource.list();
		}

		if(searchParameterRequest.getUserId() != null && !searchParameterRequest.getUserId().equals("")){
			response  = usersResource.searchByAttributes("username:" + searchParameterRequest.getUserId());
		}

		if(searchParameterRequest.getUserEmail() != null && !searchParameterRequest.getUserEmail().equals("")){
			response  = usersResource.searchByAttributes("email:" + searchParameterRequest.getUserEmail());
		}

		//like 검색이 되지 않음 (정확히 일치해야 값이 나옴)
		if(searchParameterRequest.getUserNickName() != null && !searchParameterRequest.getUserNickName().equals("")){
			response  = usersResource.searchByAttributes("userNickName:" + searchParameterRequest.getUserNickName());
		}


		response.stream().forEach(user -> {
			UserRequest userRequest = new UserRequest();

			userRequest.setId(user.getId());
			userRequest.setEmail(user.getEmail());
			userRequest.setUserId(user.getUsername());
			userRequest.setLastname(user.getLastName());
			userRequest.setFirstname(user.getFirstName());
			userRequest.setUserNickName(user.getAttributes().get("userNickName").get(0));

			long timeStamp = user.getCreatedTimestamp();
			Date date = new Date(timeStamp);
			String formattedDate = format.format(date);

			userRequest.setCreateDate(formattedDate);

			userList.add(userRequest);
		});

		Message<List<UserRequest>> message = new Message<>();
		message.setResponse("success");
		message.setMessage("정상적으로 조회되었습니다.");
		message.setData(userList);

		return ResponseEntity.ok(message);
	}

	@Operation(description = "DoubleCheck User : 사용자 중복체크를 합니다.")
	@GetMapping(path = "/check")
	public ResponseEntity<Message> doubleCheckUser(@Parameter @RequestParam String userId) {

		RealmResource realmResource = keycloak.realm(realm);
		UsersResource usersResource = realmResource.users();
		List<UserRepresentation> users = usersResource.search(userId, true);

		Message<List<UserRepresentation>> message = new Message<>();

		if(users != null && users.size() > 0){
			message.setResponse("fail");
			message.setMessage("중복되는 아이디(username)가 존재합니다.");
			message.setData(users);
			return ResponseEntity.ok(message);
		}

		message.setResponse("success");
		message.setMessage("등록가능한 아이디 입니다.");
		message.setData(users);

		return ResponseEntity.ok(message);
	}


	@Operation(description = "Add User : 사용자를 추가합니다.")
	@PostMapping(path = "/add")
	public ResponseEntity<Message> addUser(@Parameter @RequestHeader(value = "Authorization", required=false) String Authorization, @Parameter @RequestBody UserRequest userRequest) {

		UserRepresentation user = new UserRepresentation();
		List<String> userAttributeName = new ArrayList<>();

		user.setEnabled(true);
		user.setUsername(userRequest.getUserId());
		user.setFirstName(userRequest.getFirstname());
		user.setLastName(userRequest.getLastname());
		user.setEmail(userRequest.getEmail());

		Map<String, List<String>> attributes = new HashMap<>();
		userAttributeName.add(userRequest.getUserNickName());

		attributes.put("userNickName", userAttributeName);

		user.setAttributes(attributes);

		RealmResource realmResource = keycloak.realm(realm);
		UsersResource usersResource = realmResource.users();

		Response response = usersResource.create(user);

		userRequest.setStatusCode(response.getStatus());
		userRequest.setStatus(response.getStatusInfo().toString());

		Message<Response> message = new Message<>();

		if (response.getStatus() == 201) {

			String userId = CreatedResponseUtil.getCreatedId(response);

			log.info("Created userId {}", userId);

			// create password credential
			CredentialRepresentation passwordCred = new CredentialRepresentation();
			passwordCred.setTemporary(false);
			passwordCred.setType(CredentialRepresentation.PASSWORD);
			passwordCred.setValue(userRequest.getPassword());

			UserResource userResource = usersResource.get(userId);

			userResource.resetPassword(passwordCred);
			message.setResponse("success");
			message.setMessage("정상적으로 계정이 생성되었습니다.");
			message.setData(response);

			return ResponseEntity.ok(message);
		}

		message.setResponse("fail");
		message.setMessage("계정 생성에 실패하였습니다.");

		return ResponseEntity.ok(message);
	}

	@Operation(description= "Update User : 사용자에 정보를 수정합니다.")
	@PutMapping(path = "/update")
	public ResponseEntity<Message> updateUser(@Parameter @RequestBody UserRequest userRequest) { 

		RealmResource realmResource = keycloak.realm(realm);
		UserResource usersResource = realmResource.users().get(userRequest.getId());

		UserRepresentation user = new UserRepresentation();
		List<String> userAttributeName = new ArrayList<>();

		user.setEnabled(true);
		user.setId(userRequest.getId());
		user.setUsername(userRequest.getUserId());
		user.setEmail(userRequest.getEmail());

		Map<String, List<String>> attributes = new HashMap<>();
		userAttributeName.add(userRequest.getUserNickName());

		attributes.put("userNickName", userAttributeName);

		user.setAttributes(attributes);

		Message<UserRepresentation> message = new Message<>();

		try {
			usersResource.update(user);

			// create password credential
			CredentialRepresentation passwordCred = new CredentialRepresentation();
			passwordCred.setTemporary(false);
			passwordCred.setType(CredentialRepresentation.PASSWORD);
			passwordCred.setValue(userRequest.getPassword());

			message.setResponse("success");
			message.setMessage("계정 수정에 성공하였습니다.");
			message.setData(user);

		}catch (Exception e){
			log.error("Error for updating user info : {}",e.getMessage());

			message.setResponse("fail");
			message.setMessage("계정 수정에 실패하였습니다.");
		}

		return ResponseEntity.ok(message);
	}

	@Operation(description= "Delete User : 사용자를 삭제합니다.")
	@DeleteMapping(path = "/delete/{id}")
	public ResponseEntity<Message> deleteUser(@Parameter @PathVariable String id){

		RealmResource realmResource = keycloak.realm(realm);
		UsersResource usersResource = realmResource.users();

		Response response = usersResource.delete(id);

		Message<Response> message = new Message<>();

		if(response.getStatus() == 201) {
			message.setResponse("success");
			message.setMessage("계정 삭제에 성공하였습니다.");
			message.setData(response);

			return ResponseEntity.ok(message);
		}

		message.setResponse("fail");
		message.setMessage("계정 삭제에 실패하였습니다.");
		message.setData(response);

		return ResponseEntity.ok(message);
	}

	@Operation(description= "Get Token for User : 사용자에 대한 인증토큰을 받아옵니다.")
	@PostMapping(path = "/signin")
	public ResponseEntity<Message> signin(@Parameter @RequestBody UserRequest userRequest) {

		Map<String, Object> clientCredentials = new HashMap<>();
		clientCredentials.put("secret", clientSecret);
		clientCredentials.put(OAuth2Constants.GRANT_TYPE, OAuth2Constants.PASSWORD);

		Configuration configuration =
				new Configuration(authServerUrl , realm, clientId, clientCredentials, null);
		AuthzClient authzClient = AuthzClient.create(configuration);

		AccessTokenResponse response =
				authzClient.obtainAccessToken(userRequest.getEmail(), userRequest.getPassword());

		//fail의 경우도 필요
		Message<AccessTokenResponse> message = new Message<>();
		message.setResponse("success");
		message.setData(response);


		return ResponseEntity.ok(message);
	}

	@Operation(description= "Get Token from Client : client에 대한 인증토큰을 받아옵니다.")
	@PostMapping(path = "/sasignin")
	public ResponseEntity<Message> signsain(@RequestBody ClientRequest clientRequest) {

		AccessTokenResponse response = keycloak.tokenManager().getAccessToken();

		//fail의 경우도 필요
		Message<AccessTokenResponse> message = new Message<>();
		message.setResponse("success");
		message.setData(response);

		return ResponseEntity.ok(message);
	}

	@Operation(description = "Sample for Unprotected API")
	@GetMapping(value = "/unprotected-data")
	public String getUnprotectedData() {
		return "Hello, this api is not protected.";
	}


	@Operation(description = "Sample for Protected API(Get preferred name)")
	@GetMapping(value = "/protected-data")
	public String getProtectedData(@RequestHeader String Authorization) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String result = "Can't find PreferredUsername";
		if (authentication != null) {
			if (authentication.getPrincipal() instanceof KeycloakPrincipal) {
				KeycloakPrincipal<KeycloakSecurityContext> kp = (KeycloakPrincipal<KeycloakSecurityContext>) authentication.getPrincipal();
				log.debug("getPreferredUsername String [{}]", kp.getKeycloakSecurityContext().getToken().getPreferredUsername());
				result = String.format("PreferredUsername : %s", kp.getKeycloakSecurityContext().getToken().getPreferredUsername());
			}
		}
		return result;
	}

}
