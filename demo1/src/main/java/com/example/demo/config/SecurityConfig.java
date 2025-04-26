package com.example.demo.config;

import com.example.demo.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private MyUserDetailsService myUserDetailsService;
    @Autowired
    private JwtFilter jwtFilter;

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        return http
//                .csrf(customizer -> customizer.disable()) //daca facem sesiunile stateless, nu mai avem nevoie de csrf
//                .authorizeHttpRequests(request -> request
//                        // .requestMatchers("api/user/create","api/user/login").permitAll()
//                        // .requestMatchers("api/admin/create","api/admin/login").permitAll()
//                        // .requestMatchers("/api/admin/changeRole/").hasAuthority("ADMIN")
//                        // .requestMatchers("/api/user").hasAuthority("USER")
//                        .requestMatchers("/api/user/**").permitAll()
//                        .anyRequest().authenticated())
//                .httpBasic(Customizer.withDefaults()) //for Postman
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))//new session id for postman that works
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//                .build();
//    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(customizer -> customizer.disable())
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/moderator/**").hasAnyAuthority("admin", "moderator") // Permite doar admin și moderator
                        .requestMatchers("/api/user/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

      /*  Iterativ
      Customizer<CsrfConfigurer<HttpSecurity>> custCsrf = new Customizer<CsrfConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CsrfConfigurer<HttpSecurity> customizer) {
                customizer.disable();
            }
        };
        http.csrf(custCsrf);
        */


    /*@Bean
    public UserDetailsService userDetailsService(){
        UserDetails user1 = User
                .withDefaultPasswordEncoder()
                .username("user1")
                .password("u1@123")
                .roles("USER")
                .build();
        UserDetails user2 = User
                .withDefaultPasswordEncoder()
                .username("user2")
                .password("u2@123")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user1,user2);//nu verifica autentificarea
    }*/
    //AuthenticationObject(un-authenticated) -> AuthenticationProvider -> AuthenticationObject(authenticated)
    //folosit ca sa ma leg la baza de date
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(myUserDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
