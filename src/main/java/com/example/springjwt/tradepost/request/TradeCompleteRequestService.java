package com.example.springjwt.tradepost.request;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.tradepost.TradePost;
import com.example.springjwt.tradepost.TradePostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeCompleteRequestService {

    private final TradePostRepository tradePostRepository;
    private final UserRepository userRepository;
    private final TradeCompleteRequestRepository requestRepository;

    @Transactional
    public void createRequest(Long postId, String username) {
        TradePost post = tradePostRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException("거래글이 존재하지 않습니다."));
        UserEntity user = userRepository.findByUsername(username);

        boolean alreadyExists = requestRepository.existsByTradePostAndRequester(post, user);
        if (alreadyExists) {
            throw new IllegalStateException("이미 거래완료 요청을 보냈습니다.");
        }

        TradeCompleteRequest request = TradeCompleteRequest.of(post, user);
        requestRepository.save(request);
    }

    public List<UserEntity> getRequesters(Long postId) {
        List<TradeCompleteRequest> requests = requestRepository.findByTradePost_TradePostId(postId);
        return requests.stream()
                .map(TradeCompleteRequest::getRequester)
                .collect(Collectors.toList());
    }
}
