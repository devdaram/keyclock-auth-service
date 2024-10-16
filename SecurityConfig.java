@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
    //configure 설정들...
	}

	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {

		/**
		 * Returning NullAuthenticatedSessionStrategy means app will not remember session
		 */

		return new NullAuthenticatedSessionStrategy();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		KeycloakAuthenticationProvider keycloakAuthenticationProvider =
				keycloakAuthenticationProvider();

		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());

		auth.authenticationProvider(keycloakAuthenticationProvider);
	}

	@Bean
	public FilterRegistrationBean<?> keycloakAuthenticationProcessingFilterRegistrationBean(
			KeycloakAuthenticationProcessingFilter filter) {

		FilterRegistrationBean<?> registrationBean = new FilterRegistrationBean<>(filter);

		registrationBean.setEnabled(false);
		return registrationBean;
	}

	@Bean
	public FilterRegistrationBean<?> keycloakPreAuthActionsFilterRegistrationBean(
			KeycloakPreAuthActionsFilter filter) {

		FilterRegistrationBean<?> registrationBean = new FilterRegistrationBean<>(filter);
		registrationBean.setEnabled(false);
		return registrationBean;
	}

	@Bean
	public FilterRegistrationBean<?> keycloakAuthenticatedActionsFilterBean(
			KeycloakAuthenticatedActionsFilter filter) {

		FilterRegistrationBean<?> registrationBean = new FilterRegistrationBean<>(filter);

		registrationBean.setEnabled(false);
		return registrationBean;
	}

	@Bean
	public FilterRegistrationBean<?> keycloakSecurityContextRequestFilterBean(
			KeycloakSecurityContextRequestFilter filter) {

		FilterRegistrationBean<?> registrationBean = new FilterRegistrationBean<>(filter);

		registrationBean.setEnabled(false);

		return registrationBean;
	}

	@Bean
	@Override
	@ConditionalOnMissingBean(HttpSessionManager.class)
	protected HttpSessionManager httpSessionManager() {
		return new HttpSessionManager();
	}
}
