package com.mobigen.ni.certauthservice;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@RequiredArgsConstructor
public class RealmRoleCreateTest {
	private final Keycloak keycloak;
	@Value("${keycloak.realm}")
	private String realm;

	@Test
	public void Test1() {
		RealmResource realmResource = keycloak.realm(realm);
		//assertThat()
	}
}
