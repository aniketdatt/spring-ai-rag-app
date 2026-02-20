package com.ragapp.ragapp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public ChatService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Classifies whether the user query is a knowledge/document question
     * or a casual/greeting message using a quick LLM call.
     */
    private boolean isKnowledgeQuery(String query) {
        String classificationPrompt = """
                Classify the following user message as either "KNOWLEDGE" or "CASUAL".

                - KNOWLEDGE: The user is asking a question that requires looking up information from documents, policies, or stored knowledge.
                - CASUAL: The user is sending a greeting, small talk, or a message that does not require any document lookup (e.g., "hi", "hello", "how are you", "thanks", "bye").

                Respond with ONLY the single word: KNOWLEDGE or CASUAL

                User message: %s
                """
                .formatted(query);

        String classification = chatClient.prompt()
                .user(classificationPrompt)
                .call()
                .content();

        return classification != null && classification.trim().toUpperCase().contains("KNOWLEDGE");
    }

    public ChatResponse chat(String query) {
        // Step 1: Classify the query
        if (!isKnowledgeQuery(query)) {
            // Casual/greeting message — respond directly without RAG context
            String directResponse = chatClient.prompt()
                    .user(query)
                    .call()
                    .content();
            return ChatResponse.withoutReferences(directResponse);
        }

        // Step 2: Knowledge query — retrieve similar documents
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.query(query).withTopK(3));

        // Collect document content excerpts for references
        List<String> sourceExcerpts = similarDocuments.stream()
                .map(Document::getContent)
                .toList();

        String context = String.join("\n\n---\n\n", sourceExcerpts);

        // Step 3: Construct the prompt with context
        String prompt = """
                Answer the question based on the following context. Use the information from the context to provide a helpful and accurate answer.

                Context:
                %s

                Question: %s

                Provide a clear, well-formatted answer. If the context does not contain enough information to fully answer the question, say so and provide what you can.
                """
                .formatted(context, query);

        String answer = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // Step 4: Return answer with references
        return ChatResponse.withReferences(answer, sourceExcerpts);
    }
}
