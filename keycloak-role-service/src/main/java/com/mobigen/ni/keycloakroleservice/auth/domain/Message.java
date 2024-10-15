package com.mobigen.ni.keycloakroleservice.auth.domain;

import lombok.Data;

@Data
public class Message<T> {
	private String response;
	private String message;
	private StatusCode statusCode;
	private T data;
}
