package com.ragapp.ragapp;

import java.util.List;

/**
 * Response DTO that bundles the AI answer with source document references.
 */
public record ChatResponse(String answer, List<Reference> references) {

    /**
     * Represents a source document excerpt used to generate the answer.
     */
    public record Reference(String content) {
    }

    /**
     * Creates a ChatResponse with no references (e.g., for greetings).
     */
    public static ChatResponse withoutReferences(String answer) {
        return new ChatResponse(answer, List.of());
    }

    /**
     * Creates a ChatResponse with references from document excerpts.
     */
    public static ChatResponse withReferences(String answer, List<String> sourceExcerpts) {
        List<Reference> refs = sourceExcerpts.stream()
                .map(Reference::new)
                .toList();
        return new ChatResponse(answer, refs);
    }
}
