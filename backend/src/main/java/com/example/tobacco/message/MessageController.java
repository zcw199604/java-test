package com.example.tobacco.message;

import com.example.tobacco.common.result.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(messageService.list(userId));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<String> read(@PathVariable Long id) {
        messageService.read(id);
        return ApiResponse.success("ok");
    }
}
