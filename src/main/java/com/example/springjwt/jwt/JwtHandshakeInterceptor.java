package com.example.springjwt.jwt;
import com.example.springjwt.jwt.JWTUtil;
import com.example.springjwt.User.UserRepository;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.util.List;
import java.util.Map;
import org.springframework.web.socket.server.HandshakeInterceptor;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtHandshakeInterceptor(JWTUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 헤더에서 Authorization 값 꺼냄
        List<String> authHeaders = request.getHeaders().get("Authorization");
        System.out.println("🛰️ [Interceptor] Authorization 헤더: " + authHeaders);
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwtToken = authHeader.substring(7);
                System.out.println("🔐 [Interceptor] 토큰 추출: " + jwtToken); // 🔥 로그 추가

                // 2️⃣ 토큰 유효성 검사
                if (!jwtUtil.isExpired(jwtToken)) {
                    String username = jwtUtil.getUsername(jwtToken);
                    System.out.println("✅ [Interceptor] 토큰 유효, 사용자: " + username); // 🔥 로그 추가

                    attributes.put("username", username); // 필요 시 추가
                    return true;
                } else {
                    System.out.println("❌ [Interceptor] 토큰 만료"); // 🔥
                }
            } else {
                System.out.println("❌ [Interceptor] Bearer 포맷 아님"); // 🔥
            }
        } else {
            System.out.println("❌ [Interceptor] Authorization 헤더 없음"); // 🔥
        }
        return false;// 실패하면 false 반환 (403 발생)
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}