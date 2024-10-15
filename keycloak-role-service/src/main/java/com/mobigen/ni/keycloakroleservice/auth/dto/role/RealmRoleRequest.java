package com.mobigen.ni.keycloakroleservice.auth.dto.role;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RealmRoleRequest {
	private String roleName;
	private String description;


	//private boolean compositeRoles;

	//attribute
	// users in role
}
