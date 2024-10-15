package com.mobigen.ni.keycloakroleservice.auth.dto;

import lombok.Data;

@Data
public class ClientVO {
	private String clientId;
	private String clientRolename;
	private String description;
	//private String protocol;
	//private String clientAuthenticatorType;
	//private boolean enable;

}
