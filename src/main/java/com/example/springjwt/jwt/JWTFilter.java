package com.example.springjwt.jwt;

import com.example.springjwt.User.UserRepository;
import com.example.springjwt.dto.CustomUserDetails;
import com.example.springjwt.User.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    public JWTFilter(JWTUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 인증 없이 접근 허용할 URI는 여기서 처리
        if (
                requestURI.equals("/join") ||
                requestURI.equals("/login") ||
                requestURI.equals("/api/recipes/search") ||
                requestURI.equals("/admin/join") ||
                requestURI.startsWith("/uploads")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더 읽기 및 로그 출력
        String authHeader = request.getHeader("Authorization");
        System.out.println("Received Authorization header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("token null");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("authorization now");
        String token = authHeader.split(" ")[1];

        if (jwtUtil.isExpired(token)) {
            System.out.println("token expired");
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);
        System.out.println("✅ JWT 추출 결과:");
        System.out.println("   - username: " + username);
        System.out.println("   - role: " + role);

        UserEntity userEntity = userRepository.findByUsername(username);
        // ✅ 차단된 회원이면 즉시 요청 거부
        if (userEntity != null && userEntity.isBlocked()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"차단된 회원입니다.\"}");
            System.out.println("차단된 유저입니다 username:" + username);
            return;
        }

        if (userEntity == null) {
            System.out.println("JWT에 해당하는 유저가 없습니다");
            filterChain.doFilter(request, response);
            return;
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
