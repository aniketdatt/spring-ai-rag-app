package com.ragapp.ragapp.ui;

import com.ragapp.ragapp.ChatService;
import com.ragapp.ragapp.IngestionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import org.springframework.core.io.InputStreamResource;

@Route("")
public class MainView extends VerticalLayout {

    private final IngestionService ingestionService;
    private final ChatService chatService;
    private final VectorStoreService vectorStoreService;

    private final Grid<VectorStoreEntry> grid;

    public MainView(IngestionService ingestionService, ChatService chatService, VectorStoreService vectorStoreService) {
        this.ingestionService = ingestionService;
        this.chatService = chatService;
        this.vectorStoreService = vectorStoreService;

        // Upload Component
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/pdf");
        upload.addSucceededListener(event -> {
            ingestionService.ingest(new InputStreamResource(buffer.getInputStream()));
            refreshGrid();
        });

        // Grid
        grid = new Grid<>(VectorStoreEntry.class);
        grid.setColumns("content", "embedding");
        refreshGrid();

        // Chat Interface
        TextField chatField = new TextField("Chat");
        Button chatButton = new Button("Send");
        TextField responseField = new TextField("Response");
        responseField.setReadOnly(true);
        responseField.setWidth("100%");

        chatButton.addClickListener(e -> {
            String response = chatService.chat(chatField.getValue());
            responseField.setValue(response);
        });

        add(upload, grid, chatField, chatButton, responseField);
    }

    private void refreshGrid() {
        grid.setItems(vectorStoreService.getVectorStoreEntries());
    }
}
