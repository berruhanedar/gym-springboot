package com.berruhanedar.app.gym_springboot.security;

import com.berruhanedar.app.gym_springboot.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(jwtService, userDetailsService, tokenBlacklistService);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenBearerHeaderIsMissing() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService, tokenBlacklistService);
    }

    @Test
    void shouldAuthenticateValidActiveUserAndPreserveAuthorities() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.extractUsername("valid-token")).thenReturn("trainer.user");
        when(userDetailsService.loadUserByUsername("trainer.user"))
                .thenReturn(User.withUsername("trainer.user")
                        .password("encoded")
                        .authorities(new SimpleGrantedAuthority("ROLE_TRAINER"))
                        .build());

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("trainer.user");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_TRAINER");
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldRejectBlacklistedToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer blocked-token");
        when(tokenBlacklistService.isBlacklisted("blocked-token")).thenReturn(true);

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void shouldRejectDisabledUser() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.extractUsername("valid-token")).thenReturn("disabled.user");
        when(userDetailsService.loadUserByUsername("disabled.user"))
                .thenReturn(User.withUsername("disabled.user")
                        .password("encoded")
                        .authorities(List.of())
                        .disabled(true)
                        .build());

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void shouldRejectMalformedToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer malformed-token");
        when(jwtService.extractUsername("malformed-token"))
                .thenThrow(new IllegalArgumentException("invalid token"));

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }
}
