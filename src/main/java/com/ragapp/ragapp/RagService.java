package com.ragapp.ragapp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    public void addDocument(String content) {
        Document document = new Document(content, Map.of());
        vectorStore.add(List.of(document));
    }

    public String chat(String query) {
        // Retrieve similar documents
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(query).withTopK(2));

        String context = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));

        // Construct the prompt with context
        String prompt = """
                Answer the question based on the following context:
                %s

                Question: %s
                """.formatted(context, query);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
