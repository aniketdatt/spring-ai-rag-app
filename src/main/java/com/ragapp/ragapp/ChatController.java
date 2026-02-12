package com.ragapp.ragapp;

import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;
    private final IngestionService ingestionService;

    public ChatController(ChatService chatService, IngestionService ingestionService) {
        this.chatService = chatService;
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    public String ingest(@RequestParam("file") MultipartFile file) throws IOException {
        Resource resource = file.getResource();
        ingestionService.ingest(resource);
        return "File ingested successfully: " + file.getOriginalFilename();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String query) {
        return chatService.chat(query);
    }
}
