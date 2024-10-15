package com.mobigen.ni.keycloakroleservice.auth.dto;

import lombok.Data;

@Data
public class ClientRequest {
	private String cliendId;
	private String clientSecret;
}
