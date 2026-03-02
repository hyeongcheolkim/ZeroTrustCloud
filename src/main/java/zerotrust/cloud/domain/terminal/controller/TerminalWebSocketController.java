package zerotrust.cloud.domain.terminal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import zerotrust.cloud.domain.terminal.service.TerminalExecService;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TerminalWebSocketController {

    private final TerminalExecService terminalExecService;
    private final SimpMessagingTemplate messagingTemplate;

    
    @MessageMapping("/terminal/{serverInstanceId}/start")
    public void startTerminal(@DestinationVariable Long serverInstanceId) {

        terminalExecService.startTerminalSession(serverInstanceId);
    }

    
    @MessageMapping("/terminal/{serverInstanceId}/input")
    public void handleTerminalInput(@DestinationVariable Long serverInstanceId, Map<String, String> payload) {



        String input = payload.get("input");
        if (input != null) {
            terminalExecService.sendInput(serverInstanceId, input);
        }
    }
}
