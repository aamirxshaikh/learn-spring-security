package com.demo.springsecurity.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;

public class JwtUsernameAndPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;
    private final SecretKey key;

    public JwtUsernameAndPasswordAuthenticationFilter(AuthenticationManager authenticationManager, JwtConfig jwtConfig, SecretKey key) {
        this.authenticationManager = authenticationManager;
        this.jwtConfig = jwtConfig;
        this.key = key;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
//            map sent credentials to UsernameAndPasswordAuthenticationRequest

            UsernameAndPasswordAuthenticationRequest authenticationRequest = new ObjectMapper().readValue(request.getInputStream(), UsernameAndPasswordAuthenticationRequest.class);

//            validate credentials

            Authentication authenticate = new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword());

            return authenticationManager.authenticate(authenticate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
//        generate token

        String token = Jwts.builder()
                .setSubject(authResult.getName())
                .claim("authorities", authResult.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(java.sql.Date.valueOf(LocalDate.now().plusWeeks(1)))
                .signWith(key)
                .compact();

//        send token back to client

        response.addHeader(jwtConfig.getAuthorizationHeader(), jwtConfig.getTokenPrefix() + token);
    }
}
