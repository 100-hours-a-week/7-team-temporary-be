package molip.server.chat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import molip.server.chat.dto.request.ChatRoomCreateRequest;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.facade.ChatRoomCommandFacade;
import molip.server.chat.facade.ChatRoomQueryFacade;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.chat.service.ChatRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock private ChatRoomService chatRoomService;
    @Mock private ChatRoomParticipantService chatRoomParticipantService;
    @Mock private ChatRoomCommandFacade chatRoomCommandFacade;
    @Mock private ChatRoomQueryFacade chatRoomQueryFacade;

    @InjectMocks private ChatController chatController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc =
                MockMvcBuilders.standaloneSetup(chatController)
                        .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                        .build();
    }

    @Test
    void 채팅방_생성에_성공하면_성공_응답을_반환한다() throws Exception {
        // given
        ChatRoomCreateRequest request =
                new ChatRoomCreateRequest("삼전 적정가는 18만이다.", "삼전이 18만원이 적정가인가에 대한 토론방", 10);

        ChatRoom chatRoom =
                new ChatRoom(1L, request.title(), request.description(), request.maxParticipants());
        ReflectionTestUtils.setField(chatRoom, "id", 101L);

        given(chatRoomService.createChatRoom(any(), any(), any(), any())).willReturn(chatRoom);

        // when & then
        mockMvc.perform(
                        post("/chat-rooms")
                                .with(authenticatedUser(1L))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("채팅방이 생성되었습니다."))
                .andExpect(jsonPath("$.data.roomId").value(101L));
    }

    @Test
    void 채팅방_삭제에_성공하면_204를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/chat-rooms/{roomId}", 101L).with(authenticatedUser(1L)))
                .andExpect(status().isNoContent());

        then(chatRoomService).should(times(1)).deleteChatRoom(1L, 101L);
    }

    private RequestPostProcessor authenticatedUser(Long userId) {
        return request -> {
            User principal = new User(String.valueOf(userId), "", List.of());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal, null, principal.getAuthorities());

            SecurityContext context = SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);

            return request;
        };
    }
}
