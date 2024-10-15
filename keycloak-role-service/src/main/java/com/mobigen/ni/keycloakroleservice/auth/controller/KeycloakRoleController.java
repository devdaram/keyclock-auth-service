package com.mobigen.ni.keycloakroleservice.auth.controller;

import com.mobigen.ni.keycloakroleservice.auth.domain.Message;
import com.mobigen.ni.keycloakroleservice.auth.domain.StatusCode;
import com.mobigen.ni.keycloakroleservice.auth.dto.ClientVO;
import com.mobigen.ni.keycloakroleservice.auth.dto.RealmRequest;
import com.mobigen.ni.keycloakroleservice.auth.dto.role.RealmRoleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * OPEN API Rest Keycloak Sample RoleController ver.1
 */
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Keycloak RoleController ver.1")
@RestController
@RequestMapping(value = "/nms/api/v1/auth")
public class KeycloakRoleController {

	@Value("${keycloak.auth-server-url}")
	private String authServerUrl;

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${keycloak.resource}")
	private String clientId;

	@Value("${keycloak.credentials.secret}")
	private String clientSecret;

	private final Keycloak keycloak;
	private final Keycloak keycloakMaster;

	@Operation(description = "Create a new Realm Role : 새로운 Realm의 권한(Role)을 추가합니다.")
	@PostMapping(path = "/new/realm/role")
	public ResponseEntity<Message> addRealmRole(@Parameter @RequestBody RealmRoleRequest realmRoleRequest) {

		Message<String> message = new Message<>();

		RealmResource realmResource = keycloak.realm(realm);

		if(realmRoleRequest == null){
			message.setResponse("fail");
			message.setMessage("파라미터 인자값이 없습니다.");
			message.setData("");

			return ResponseEntity.ok(message);
		}

		RoleRepresentation newRole = new RoleRepresentation();
		newRole.setName(realmRoleRequest.getRoleName());
		newRole.setDescription(realmRoleRequest.getDescription());
		//추가적으로 설정할 속성이 있다면 추가하면 됨.

		realmResource.roles().create(newRole);

		message.setResponse("success");
		message.setMessage("realm role 생성에 성공하였습니다.");
		message.setData(realmRoleRequest.toString());

		return ResponseEntity.ok(message);
	}

	@Operation(description = "Add a Realm Role to user: user에게 Realm의 권한을 부여합니다.")
	@PostMapping(path = "/realm/role/add/{userUniqueId}/{realmRoleName}")
	public ResponseEntity<Message> addRealmRoleToUser(@Parameter @PathVariable String realmRoleName, @Parameter @PathVariable String userUniqueId) {

		Message<String> message = new Message<>();

		RealmResource realmResource = keycloak.realm(realm);
		UserResource userResource = realmResource.users().get(userUniqueId);
		RoleRepresentation testerRealmRole = realmResource.roles().get(realmRoleName).toRepresentation();

		log.info("user realm role : {}", userResource.roles().realmLevel().listAll());
		log.info("get realm roles : {}", realmResource.roles().list());

		if(testerRealmRole == null){
			message.setResponse("fail");
			message.setMessage("해당 Realm에 " + realmRoleName + " 이 존재하지 않습니다.");
			message.setData("");

			return ResponseEntity.ok(message);
		}
		userResource.roles().realmLevel().add(Arrays.asList(testerRealmRole));

		log.info("After user realm role : {}", userResource.roles().realmLevel().listAll());

		message.setResponse("success");
		message.setMessage(userResource.toRepresentation().getUsername()+" 에게 Realm Role : " + realmRoleName + " 를 추가하였습니다.");
		message.setData("");

		return ResponseEntity.ok(message);
	}

	@Operation(description = "Remove a Realm Role to user: user에게 Realm의 권한을 제거합니다.")
	@PostMapping(path = "/realm/role/remove/{userUniqueId}/{realmRoleName}")
	public ResponseEntity<Message> removeRealmRoleToUser(@Parameter @PathVariable String realmRoleName, @Parameter @PathVariable String userUniqueId) {

		Message<String> message = new Message<>();

		RealmResource realmResource = keycloak.realm(realm);
		UserResource userResource = realmResource.users().get(userUniqueId);
		RoleRepresentation testerRealmRole = realmResource.roles().get(realmRoleName).toRepresentation();

		log.info("user realm role : {}", userResource.roles().realmLevel().listAll());
		log.info("get realm roles : {}", realmResource.roles().list());

		if(testerRealmRole == null){
			message.setResponse("fail");
			message.setMessage("해당 Realm에 " + realmRoleName + " 이 존재하지 않습니다.");
			message.setData("");

			return ResponseEntity.ok(message);
		}
		//userResource.roles().realmLevel().add(Arrays.asList(testerRealmRole));
		userResource.roles().realmLevel().remove(Arrays.asList(testerRealmRole));

		log.info("After user realm role : {}", userResource.roles().realmLevel().listAll());

		message.setResponse("success");
		message.setMessage(userResource.toRepresentation().getUsername()+" 에게 Realm Role : " + realmRoleName + " 를 제거하였습니다.");
		message.setData("");

		return ResponseEntity.ok(message);
	}

	@Operation(description = "Add Realm : 새로운 Realm을 추가합니다.")
	@PostMapping(path = "/new/realm")
	public ResponseEntity<Message> addRealm(@Parameter @RequestBody RealmRequest realmRequest) {

		Message<String> message = new Message<>();

		RealmRepresentation newRealm = new RealmRepresentation();

		Map<String, String> attributes = new HashMap<>();
		attributes.put("Frontend URL", realmRequest.getFrontendURL());
		newRealm.setAttributes(attributes);

		newRealm.setRealm(realmRequest.getName());
		newRealm.setUserManagedAccessAllowed(realmRequest.isUserManagerAccessAllow());
		newRealm.setEnabled(true);
		newRealm.setDisplayName(realmRequest.getDisplayName());

		//create realm role
		keycloakMaster.realms().create(newRealm);

		message.setResponse("success");
		message.setMessage("realm role 생성에 성공하였습니다.");
		message.setData("");

		return ResponseEntity.ok(message);

	}

	//아예 삭제처리하는것보다 enable=false로 하는게 맞나? history 상으로도..?
	@Operation(description = "delete Realm Role : Realm의 권한(Role)을 삭제합니다.")
	@DeleteMapping(path = "/delete/realm/{realmName}/{roleName}")
	public ResponseEntity<Message> deleteRealmRole(@Parameter @PathVariable String realmName, @Parameter @PathVariable String roleName) {

		Message<String> message = new Message<>();

		//1. realm이 존재하지 않는 경우
		List<RealmRepresentation> list = keycloakMaster.realms().findAll();
		for(RealmRepresentation realmRes : list){
			if(realmRes.getRealm().toLowerCase(Locale.ROOT).equals(realmName.toLowerCase(Locale.ROOT))){
				message.setStatusCode(StatusCode.EXIST);
				break;
			}
		}
	/*.forEach(realmRes -> {
			String s = realmRes.getRealm().toLowerCase(Locale.ROOT);
			String ss = realmName.toLowerCase(Locale.ROOT);
			if(realmRes.getRealm().toLowerCase(Locale.ROOT).equals(realmName.toLowerCase(Locale.ROOT))){
				message.setStatusCode(StatusCode.EXIST);
				return;
			}
		});
	*/
		if(message.getStatusCode() != StatusCode.EXIST){
			message.setResponse("fail");
			message.setMessage("realm이 존재하지 않습니다.");
			return ResponseEntity.ok(message);
		}

		RealmRepresentation changedRealmRole = new RealmRepresentation();
		changedRealmRole.setEnabled(false);

		//RealmResource realmResource = keycloakMaster.realm(realm);
		//RoleRepresentation newRole = new RoleRepresentation();
		//keycloakMaster.realms()

		//realmResource.roles().deleteRole(realmRoleName);

		//keycloakMaster.realm(realmName).update(changedRealmRole);
		keycloakMaster.realm(realmName).roles().get(roleName).remove();

		message.setResponse("success");
		message.setMessage("realm 삭제(비활성화)에 성공하였습니다.");
		message.setData(realmName);

		return ResponseEntity.ok(message);

	}

	@Operation(description = "update Realm Role : Realm의 권한(Role)을 수정합니다.")
	@PutMapping(path = "/update/realm/{realmRoleName}")
	public ResponseEntity<Message> updateRealmRole(@Parameter @PathVariable String realmRoleName, @Parameter @RequestBody RealmRequest realmRequest) {

		Message<String> message = new Message<>();
		//List<String> attributeName = new ArrayList<>();

		//1. realm이 존재하지 않는 경우
		//keycloakMaster.realm(realmRoleName).

		//2. realm role이 존재하지 않는 경우

		/**
		 * 추가 항목
		 * 	https://www.keycloak.org/docs-api/18.0/javadocs/org/keycloak/representations/idm/RealmRepresentation.html#displayName
		 * 참조
		 */

		RealmRepresentation newRealm = new RealmRepresentation();
		newRealm.setRealm(realmRequest.getName());
		newRealm.setDisplayName(realmRequest.getDisplayName());

		Map<String, String> attributes = new HashMap<>();

		attributes.put(realmRequest.getAttributeName(), realmRequest.getAttributeName());

		newRealm.setAttributes(attributes);

		keycloakMaster.realm(realmRoleName).update(newRealm);

		message.setResponse("success");
		message.setMessage("realm role 수정에 성공하였습니다.");
		message.setData("");

		return ResponseEntity.ok(message);

	}

	@Operation(description = "Add Client : 새로운 Client를 생성합니다.")
	@PostMapping(path = "/new/client")
	public ResponseEntity<Message> addClient(@Parameter @RequestBody ClientVO clientVO) {

		RealmResource realmResource = keycloak.realm(realm);

		ClientRepresentation newClient = new ClientRepresentation();

		newClient.setClientId(clientVO.getClientId());
		//newClient.setProtocol(clientVO.getProtocol());
		//newClient.setClientAuthenticatorType(clientVO.getClientAuthenticatorType());

		//keycloakMaster.realm("master")
		realmResource.clients().create(newClient);

		Message<String> message = new Message<>();
		message.setResponse("success");
		message.setMessage("client 생성에 성공하였습니다.");
		message.setData("");

		return ResponseEntity.ok(message);
	}

	@Operation(description = "Add Client Role : Client의 권한(Role)을 추가합니다.")
	@PostMapping(path = "/new/client/role")
	public ResponseEntity<Message> addClientRole(@Parameter @RequestBody ClientVO clientVO) {

		Message<String> message = new Message<>();
		RealmResource realmResource = keycloak.realm(realm);

		if(clientVO == null || clientVO.equals("")){
			message.setResponse("fail");
			message.setMessage("파라미터 인자가 없습니다.");
			message.setData("");
			return ResponseEntity.ok(message);
		}

        List<ClientRepresentation> clientRepList = realmResource.clients().findByClientId(clientVO.getClientId());

        if(clientRepList.size() == 0){
            message.setResponse("fail");
            message.setMessage("해당 client는 존재하지 않습니다.(NPE)");
            message.setData("");
            return ResponseEntity.ok(message);
        }



		RoleRepresentation newClientRole = new RoleRepresentation();
		newClientRole.setName(clientVO.getClientRolename());
		newClientRole.setDescription(clientVO.getDescription());
		newClientRole.setClientRole(true);

		realmResource.clients().findByClientId(clientVO.getClientId()).forEach(e -> realmResource.clients().get(e.getId()).roles().create(newClientRole));
		message.setResponse("success");
		message.setMessage("client role 생성에 성공하였습니다.");
		message.setData("");

		return ResponseEntity.ok(message);
	}

	@Operation(description = "Add Client role to User : user에게 Client의 권한(Role)을 부여합니다.")
	@PostMapping(path = "/client/role/add/{userUniqueId}/{clientId}/{clientRoleName}")
	public ResponseEntity<Message> addClientRoleToUser(@Parameter @PathVariable String clientRoleName, @Parameter @PathVariable String clientId, @Parameter @PathVariable String userUniqueId) {

		Message<String> message = new Message<>();
		RealmResource realmResource = keycloak.realm(realm);

		UserResource userResource = realmResource.users().get(userUniqueId);

		ClientRepresentation clientRep = realmResource.clients().findByClientId(clientId).get(0);
		if(realmResource.clients().findByClientId(clientId).size() == 0){
			message.setResponse("fail");
			message.setMessage(userResource.toRepresentation().getUsername()+" 에게 Client Role : " + clientRoleName + " 가 존재하지 않습니다.");
			message.setData("");

			return ResponseEntity.ok(message);
		}

		RoleRepresentation userClientRole = realmResource.clients().get(clientRep.getId()).roles().get(clientRoleName).toRepresentation();

		userResource.roles()
				.clientLevel(clientRep.getId()).add(Arrays.asList(userClientRole));

		message.setResponse("success");
		message.setMessage(userResource.toRepresentation().getUsername()+" 에게 Realm Role : " + clientRoleName + " 를 추가하였습니다.");
		message.setData("");

		return ResponseEntity.ok(message);
	}

	@Operation(description = "Remove Client role to User : user에게 Client의 권한(Role)을 제거합니다.")
	@PostMapping(path = "/client/role/remove/{userUniqueId}/{clientId}/{clientRoleName}")
	public ResponseEntity<Message> removeClientRoleToUser(@Parameter @PathVariable String clientRoleName, @Parameter @PathVariable String clientId, @Parameter @PathVariable String userUniqueId) {

		Message<String> message = new Message<>();
		RealmResource realmResource = keycloak.realm(realm);

		UserResource userResource = realmResource.users().get(userUniqueId);

		ClientRepresentation clientRep = realmResource.clients().findByClientId(clientId).get(0);
		if(realmResource.clients().findByClientId(clientId).size() == 0){
			message.setResponse("fail");
			message.setMessage(userResource.toRepresentation().getUsername()+" 에게 Client Role : " + clientRoleName + " 가 존재하지 않습니다.");
			message.setData("");

			return ResponseEntity.ok(message);
		}

		RoleRepresentation userClientRole = realmResource.clients().get(clientRep.getId()).roles().get(clientRoleName).toRepresentation();

//		userResource.roles()
//				.clientLevel(clientRep.getId()).add(Arrays.asList(userClientRole));

		userResource.roles().clientLevel(clientRep.getId()).remove(Arrays.asList(userClientRole));

		message.setResponse("success");
		message.setMessage(userResource.toRepresentation().getUsername()+" 에게 Realm Role : " + clientRoleName + " 를 제거하였습니다.");
		message.setData("");

		return ResponseEntity.ok(message);
	}

	@Operation(description = "update Client Role : Client의 권한(Role)을 수정합니다.")
	@PutMapping(path = "/update/client/{clientRoleName}")
	public ResponseEntity<Message> updateClientRole(@Parameter @PathVariable String clientRoleName ,@Parameter @RequestBody ClientVO clientVO) {

		Message message = new Message();

		RealmResource realmResource = keycloak.realm(realm);
        List<ClientRepresentation> clientRepList = realmResource.clients().findByClientId(clientVO.getClientId());

        if(clientRepList.size() == 0){
            message.setResponse("fail");
            message.setMessage("해당 client는 존재하지 않습니다.(NPE)");
            message.setData("");
            return ResponseEntity.ok(message);
        }

		RoleRepresentation changedClientRole = new RoleRepresentation();
		changedClientRole.setName(clientVO.getClientRolename());
		changedClientRole.setDescription(clientVO.getDescription());

        realmResource.clients().findByClientId(clientVO.getClientId())
				.forEach(clientRepresentation -> {
					if(realmResource.clients().get(clientRepresentation.getId()).roles().list(clientRoleName,false).size() == 0){
						message.setResponse("fail");
						message.setMessage("해당 client role은 존재하지 않습니다.");
						message.setData(clientRoleName);
						return;
					}
						realmResource.clients().get(clientRepresentation.getId()).roles().get(clientRoleName).update(changedClientRole);
				});

		if(message.getMessage() != null){
			return ResponseEntity.ok(message);
		}

        message.setResponse("success");
        message.setMessage("client role 수정에 성공하였습니다.");
        message.setData("");

		return ResponseEntity.ok(message);
	}

	@Operation(description = "delete Client Role : Client의 권한(Role)을 삭제합니다.")
	@DeleteMapping(path = "/delete/client/{clientRoleName}/{clientId}")
	public ResponseEntity<Message> deleteClientRole(@Parameter @PathVariable String clientRoleName, @Parameter @PathVariable String clientId) {

		Message<String> message = new Message<>();
		RealmResource realmResource = keycloak.realm(realm);

		List<ClientRepresentation> clientRepList = realmResource.clients().findByClientId(clientId);
		//1. clinetId 가 존재하지 않는경우
		if(clientRepList.size() == 0){
			message.setResponse("fail");
			message.setMessage("해당 client는 존재하지 않습니다.");
			message.setData("");
			return ResponseEntity.ok(message);
		}

		//2. client role이 존재하지 않는 경우
		realmResource.clients().findByClientId(clientId)
				.forEach(clientRepresentation -> {
					if(realmResource.clients().get(clientRepresentation.getId()).roles().list(clientRoleName,false).size() == 0){
						message.setResponse("fail");
						message.setMessage("해당 client role은 존재하지 않습니다.");
						message.setData(clientRoleName);
						return;
					}
					realmResource.clients().get(clientRepresentation.getId()).roles().get(clientRoleName).remove();
				});

		if(message.getMessage() != null){
			return ResponseEntity.ok(message);
		}

		message.setResponse("success");
		message.setMessage("client role 삭제에 성공하였습니다.");
		message.setData("");

		return ResponseEntity.ok(message);
	}
}