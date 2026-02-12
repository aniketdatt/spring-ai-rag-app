package com.ragapp.ragapp.ui;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VectorStoreService {

    private final JdbcTemplate jdbcTemplate;

    public VectorStoreService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<VectorStoreEntry> getVectorStoreEntries() {
        return jdbcTemplate.query(
                "SELECT content, embedding::text FROM vector_store",
                (rs, rowNum) -> {
                    String content = rs.getString("content");
                    String embeddingRaw = rs.getString("embedding");
                    return new VectorStoreEntry(content, parseAndFormatEmbedding(embeddingRaw));
                }
        );
    }

    private String parseAndFormatEmbedding(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "[]";
        }
        // PostgreSQL vector is returned as "[v1,v2,v3,...]"
        String cleaned = raw.replace("[", "").replace("]", "");
        String[] parts = cleaned.split(",");
        
        StringBuilder sb = new StringBuilder("[");
        int limit = Math.min(5, parts.length);
        for (int i = 0; i < limit; i++) {
            sb.append(parts[i].trim());
            if (i < limit - 1) {
                sb.append(", ");
            }
        }
        if (parts.length > 5) {
            sb.append(", ...");
        }
        sb.append("]");
        return sb.toString();
    }

}
