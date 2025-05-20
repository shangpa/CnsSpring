package com.example.springjwt.chat;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.market.TradePost;
import com.example.springjwt.market.TradePostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final TradePostRepository tradePostRepository;

    public ChatRoom createOrGetRoom(Long postId, Long senderId, Long ownerId) {
        String roomKey = makeRoomKey(senderId, ownerId, postId);

        return chatRoomRepository.findByRoomKey(roomKey)
                .orElseGet(() -> {
                    TradePost post = tradePostRepository.findById(postId).orElseThrow();
                    UserEntity sender = userRepository.findById(senderId.intValue()).orElseThrow();
                    UserEntity owner = userRepository.findById(ownerId.intValue()).orElseThrow();

                    ChatRoom newRoom = ChatRoom.builder()
                            .roomKey(roomKey)
                            .post(post)
                            .userA(sender)
                            .userB(owner)
                            .build();

                    return chatRoomRepository.save(newRoom);
                });
    }

    private String makeRoomKey(Long a, Long b, Long postId) {
        Long min = Math.min(a, b);
        Long max = Math.max(a, b);
        return min + "-" + max + "-" + postId;
    }
}
