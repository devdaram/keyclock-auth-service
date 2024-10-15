package com.mobigen.ni.keycloakroleservice.auth.dto;

import lombok.Data;

@Data
public class UserRequest {
	private String id; //keycloak unique id key
	private String email;
	private String password;
	private String userId;
	private String lastname;
	private String firstname;
	private String userNickName;
	private String createDate;
	private int statusCode;
	private String status;
}
