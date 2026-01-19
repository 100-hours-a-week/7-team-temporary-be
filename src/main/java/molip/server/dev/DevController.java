package molip.server.dev;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DevController {
    @GetMapping("/")
    public String healthCheck() {
        return "hello world";
    }
}
