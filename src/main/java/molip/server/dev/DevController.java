package molip.server.dev;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DevController {

    private final LiveKitProbeService liveKitProbeService;

    @GetMapping("/")
    public String healthCheck() {
        return "hello world";
    }

    @GetMapping("/dev/livekit/ping")
    public Map<String, Object> liveKitPing() {
        return liveKitProbeService.ping();
    }
}
