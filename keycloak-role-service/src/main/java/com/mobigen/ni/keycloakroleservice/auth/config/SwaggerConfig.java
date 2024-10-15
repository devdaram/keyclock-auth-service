package com.mobigen.ni.keycloakroleservice.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;


@OpenAPIDefinition(info = @Info(title = "My App",
		description = "Some long and useful description", version = "v1"), security = @SecurityRequirement(name = "security_auth"))
@SecurityScheme(name = "security_auth", type = SecuritySchemeType.OAUTH2,
		flows = @OAuthFlows(password = @OAuthFlow(
				authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}"
				, tokenUrl = "${springdoc.oAuthFlow.tokenUrl}", scopes = {
				@OAuthScope(name = "profile", description = "read scope") })))
public class SwaggerConfig {

}

