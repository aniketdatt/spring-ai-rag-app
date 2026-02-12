package com.ragapp.ragapp;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestionService {

    private final VectorStore vectorStore;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingest(Resource resource) {
        // 1. Read the PDF
        TikaDocumentReader documentReader = new TikaDocumentReader(resource);
        List<Document> documents = documentReader.get();

        // 2. Split into chunks
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        List<Document> chunks = textSplitter.apply(documents);

        // 3. Save to VectorStore
        vectorStore.add(chunks);
    }
}
