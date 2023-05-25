package co.istad.mbanking.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final PasswordEncoder encoder;
    private final UserDetailsServiceImpl userDetailsService;

    // Define in-memory user
    /* @Bean
    public UserDetailsService userDetailsService(){
        UserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        UserDetails admin = User.builder()'
                .username("admin")
//                .password("{noop}123")
                .password(encoder.encode("123"))
                .roles("ADMIN")
                .build();
        UserDetails goldUser = User.builder()
                .username("gold")
                .password(encoder.encode("123"))
                //.password("{noop}123")
                .roles("ACCOUNT")
                .build();
        UserDetails user = User.builder()
                .username("user")
                .password(encoder.encode("123"))
                //.password("{noop}123")
                .roles("USER")
                .build();
        userDetailsManager.createUser(admin);
        userDetailsManager.createUser(user);
        userDetailsManager.createUser(goldUser);
        return userDetailsManager;
    }*/

    // Define user auth with jdbc db
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(encoder);
        return auth;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);
//        http.csrf(token -> token.disable());

        http.authorizeHttpRequests(request -> {
            //Authorize URL mapping
            request.requestMatchers("/api/v1/auth/**").permitAll();
            request.requestMatchers(HttpMethod.GET,"/api/v1/users/**").hasAuthority("SCOPE_admin:read");
            request.requestMatchers(HttpMethod.POST,"/api/v1/users/**").hasAuthority("SCOPE_admin:write");
            request.requestMatchers(HttpMethod.DELETE,"/api/v1/users/**").hasAuthority("SCOPE_admin:delete");
            request.requestMatchers(HttpMethod.PUT,"/api/v1/users/**").hasAuthority("SCOPE_admin:update");

            request.anyRequest().authenticated();
//                //config users
////                request.requestMatchers("/api/v1/users").hasAnyRole("ADMIN","SYSTEM");
////
////
////                //config files
////                request.requestMatchers(HttpMethod.POST, "/api/v1/files/**").authenticated();
////                request.requestMatchers(HttpMethod.GET,"/api/v1/files/find-all-files").authenticated();
////                request.requestMatchers(HttpMethod.GET,"/api/v1/files/**").authenticated();
////                request.requestMatchers(HttpMethod.DELETE,"/api/v1/files/**").hasAnyRole("ADMIN","SYSTEM");
////                request.requestMatchers(HttpMethod.DELETE,"/api/v1/files").hasAnyRole("ADMIN","SYSTEM");
////
////                //config account
////                request.requestMatchers("/api/v1/account").authenticated();
////
////                //config account-type
//////                request.requestMatchers(HttpMethod.GET,"/api/v1/account-types/").authenticated();
//////                request.requestMatchers("/api/v1/account-types/").hasAnyRole("SYSTEM","ADMIN");
////                request.requestMatchers("/api/v1/account-types/**").hasAnyRole("SYSTEM","ADMIN");
////
////                request.anyRequest().permitAll();

        });

        //Security mechanism
        //http.httpBasic();

        http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);

        //make api stateless

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }

    //generate keypair

    @Bean
    public KeyPair keyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        return keyPairGenerator.generateKeyPair();
    }

    @Bean
    public RSAKey rsaKey(KeyPair keyPair) {
        return new RSAKey.Builder((RSAPublicKey)keyPair.getPublic())
                .privateKey(keyPair.getPrivate())
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();

    }
    @Bean
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }




    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
        var jwkSet = new JWKSet(rsaKey);
        return new JWKSource<SecurityContext>() {
            @Override
            public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) {
                return jwkSelector.select(jwkSet);
            }
        };
    }


}



















