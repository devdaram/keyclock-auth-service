package com.mobigen.ni.keycloakroleservice.auth.dto;

import lombok.Data;

@Data
public class RealmRequest {
	private String name;
	private String displayName;
	private String FrontendURL;
	private boolean enable;
	private boolean userManagerAccessAllow;
	private boolean loginWithEmailAllowed;
	private String attributeName;
}
