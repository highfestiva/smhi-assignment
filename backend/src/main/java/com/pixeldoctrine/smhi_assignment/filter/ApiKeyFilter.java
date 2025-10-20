package com.pixeldoctrine.smhi_assignment.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pixeldoctrine.smhi_assignment.dto.ApiKeyDTO;
import com.pixeldoctrine.smhi_assignment.repository.apikey.ApiKeyRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private ApiKeyRepository repo;

    public ApiKeyFilter(ApiKeyRepository repo) {
        this.repo = repo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // load the auth bearer key from the database
        String authHeader = request.getHeader("Authorization");
        ApiKeyDTO apiKeyData = (authHeader != null && authHeader.startsWith("Bearer ")) ?
                repo.findByApiKey(authHeader.substring(7)) : null;

        // check that key and request combination is valid
        if (isValidKeyForRequest(apiKeyData, request)) {
            var roles = apiKeyData.roles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            Authentication auth = new UsernamePasswordAuthenticationToken(apiKeyData, null, roles);
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response); // valid key, go right ahead
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"unauthorized\", \"message\": \"invalid API key\"}");
        }
    }

    private boolean isValidKeyForRequest(ApiKeyDTO apiKeyData, HttpServletRequest request) {
        if (apiKeyData == null || request.getRequestURI() == null) {
            return false;
        }
        // check paths, if you want it that granular
        // TODO: regexps are slow, consider something faster if system will be under heavy load
        return apiKeyData.permittedPathRegex().stream()
                .anyMatch(regex -> request.getRequestURI().matches(regex));
    }
}
