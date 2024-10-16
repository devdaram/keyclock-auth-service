@Slf4j
@Configuration
@Data
public class KeycloakConfig {

	@Value("${keycloak.realm}")
	private String realm;
	@Value("${keycloak.bearer-only}")
	private Boolean bearerOnly;
	@Value("${keycloak.auth-server-url}")
	private String authServerUrl;
	@Value("${keycloak.ssl-required}")
	private String sslRequired;
	@Value("${keycloak.resource}")
	private String clientId;
	@Value("${keycloak.credentials.secret}")
	private String clientSecret;
	@Value("${keycloak.confidential-port}")
	private int confidentialPort;

	@Bean
	public Keycloak keycloak() {
		return KeycloakBuilder.builder()
			.serverUrl(authServerUrl)
			.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
			.realm(realm)
			.clientId(clientId)
			.clientSecret(clientSecret)
			.resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
			.build();
	}

	@Bean
	public Keycloak keycloakMaster() {
		return KeycloakBuilder.builder()
				.serverUrl(authServerUrl)
				.realm("master")
				.username("admin")
				.password("admin")
				.clientId("admin-cli")
				.resteasyClient(
						new ResteasyClientBuilder()
								.connectionPoolSize(10).build()
				).build();
	}
}

