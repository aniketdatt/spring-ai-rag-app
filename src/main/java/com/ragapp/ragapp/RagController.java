package com.ragapp.ragapp;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;
    private final IngestionService ingestionService;

    public RagController(RagService ragService, IngestionService ingestionService) {
        this.ragService = ragService;
        this.ingestionService = ingestionService;
    }

    @PostMapping("/add")
    public String addDocument(@RequestBody String content) {
        ragService.addDocument(content);
        return "Document added successfully";
    }

    @PostMapping("/ingest")
    public String ingest(@RequestParam("file") MultipartFile file) throws IOException {
        Resource resource = file.getResource();
        ingestionService.ingest(resource);
        return "File ingested successfully: " + file.getOriginalFilename();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String query) {
        return ragService.chat(query);
    }
}
