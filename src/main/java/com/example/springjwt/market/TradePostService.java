package com.example.springjwt.market;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradePostService {

    private final TradePostRepository tradePostRepository;
    private final UserRepository userRepository;

    public TradePost create(TradePostDTO dto, String username) {
        UserEntity user = userRepository.findByUsername(username);
        TradePost tradePost = dto.toEntity();
        tradePost.setUser(user); // 작성자 주입
        return tradePostRepository.save(tradePost);
    }
}