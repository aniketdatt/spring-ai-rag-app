package com.ragapp.ragapp.ui;

import com.ragapp.ragapp.ChatService;
import com.ragapp.ragapp.IngestionService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.core.io.InputStreamResource;

import java.util.concurrent.CompletableFuture;

@Route("")
public class MainView extends VerticalLayout {

    private final IngestionService ingestionService;
    private final ChatService chatService;
    private final VectorStoreService vectorStoreService;

    private final Grid<VectorStoreEntry> grid;
    private final Div chatHistoryContainer;
    private final TextArea chatInput;

    public MainView(IngestionService ingestionService, ChatService chatService, VectorStoreService vectorStoreService) {
        this.ingestionService = ingestionService;
        this.chatService = chatService;
        this.vectorStoreService = vectorStoreService;

        // Initialize components FIRST before using them
        grid = new Grid<>(VectorStoreEntry.class, false);
        chatHistoryContainer = new Div();
        chatInput = new TextArea();

        // Configure main layout
        setSizeFull();
        setPadding(true);
        setSpacing(false);
        addClassName("main-view");

        // Header
        H2 header = new H2("🤖 RAG Application");
        header.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.SMALL);

        // Create TabSheet
        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        // Document Management Tab
        VerticalLayout documentTab = createDocumentManagementTab();
        tabSheet.add("📄 Document Management", documentTab);

        // AI Chat Tab
        VerticalLayout chatTab = createChatTab();
        tabSheet.add("💬 AI Chat", chatTab);

        add(header, tabSheet);
    }

    private VerticalLayout createDocumentManagementTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(true);

        // Upload Section
        H3 uploadHeader = new H3("Upload PDF Documents");
        uploadHeader.addClassName(LumoUtility.Margin.Bottom.SMALL);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/pdf", ".pdf");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Html("<span>Drop PDF file here or click to browse</span>"));
        upload.addClassName("upload-component");

        // Upload success handler
        upload.addSucceededListener(event -> {
            try {
                ingestionService.ingest(new InputStreamResource(buffer.getInputStream()));

                // Show success notification
                Notification notification = Notification.show(
                        "✓ File uploaded successfully: " + event.getFileName(),
                        3000,
                        Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Refresh grid
                refreshGrid();

                // Clear upload to allow new upload without refresh
                upload.clearFileList();

            } catch (Exception e) {
                Notification notification = Notification.show(
                        "✗ Upload failed: " + e.getMessage(),
                        5000,
                        Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Upload failed handler
        upload.addFailedListener(event -> {
            Notification notification = Notification.show(
                    "✗ Upload failed: " + event.getReason().getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        // Vector Store Grid
        H3 gridHeader = new H3("Vector Store Embeddings");
        gridHeader.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.SMALL);

        grid.addColumn(VectorStoreEntry::content)
                .setHeader("Content")
                .setFlexGrow(2)
                .setResizable(true);

        grid.addColumn(VectorStoreEntry::embedding)
                .setHeader("Embedding (Preview)")
                .setFlexGrow(1)
                .setResizable(true);

        grid.addComponentColumn(entry -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteButton.addClickListener(e -> confirmAndDelete(entry));
            return deleteButton;
        }).setHeader("Actions").setWidth("100px").setFlexGrow(0);

        grid.addClassName("vector-grid");
        grid.setHeight("400px");

        // Initial load
        refreshGrid();

        // Refresh button
        Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshButton.addClickListener(e -> refreshGrid());

        layout.add(uploadHeader, upload, gridHeader, grid, refreshButton);
        return layout;
    }

    private VerticalLayout createChatTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(true);

        // Chat History Container
        H3 chatHeader = new H3("Chat with AI");
        chatHeader.addClassName(LumoUtility.Margin.Bottom.SMALL);

        chatHistoryContainer.addClassName("chat-history");
        chatHistoryContainer.setWidthFull();
        chatHistoryContainer.getStyle()
                .set("max-height", "500px")
                .set("overflow-y", "auto")
                .set("padding", "1rem")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "1rem");

        // Chat Input Section
        chatInput.setPlaceholder("Type your question here...");
        chatInput.setWidthFull();
        chatInput.setMinHeight("80px");
        chatInput.setMaxHeight("150px");
        chatInput.addClassName("chat-input");

        Button sendButton = new Button("Send", new Icon(VaadinIcon.PAPERPLANE));
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.addClassName("send-button");
        sendButton.addClickListener(e -> handleChatSend());

        HorizontalLayout inputLayout = new HorizontalLayout(chatInput, sendButton);
        inputLayout.setWidthFull();
        inputLayout.setAlignItems(Alignment.END);
        inputLayout.expand(chatInput);
        inputLayout.setSpacing(true);

        layout.add(chatHeader, chatHistoryContainer, inputLayout);
        layout.expand(chatHistoryContainer);

        return layout;
    }

    private void handleChatSend() {
        String query = chatInput.getValue();

        if (query == null || query.trim().isEmpty()) {
            Notification.show("Please enter a question", 2000, Notification.Position.MIDDLE);
            return;
        }

        // Add user message to chat
        addChatMessage(query, true);

        // Clear input
        chatInput.clear();

        // Create wrapper for loading indicator (same as chat messages)
        Div loadingWrapper = new Div();
        loadingWrapper.setWidthFull();
        loadingWrapper.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-start")
                .set("margin-bottom", "0.5rem");

        Div loadingDiv = new Div();
        loadingDiv.addClassName("ai-message"); // Add ai-message class here

        // Create horizontal layout for spinner and text
        HorizontalLayout loadingLayout = new HorizontalLayout();
        loadingLayout.setSpacing(true);
        loadingLayout.setAlignItems(Alignment.CENTER);

        // Add spinner
        ProgressBar spinner = new ProgressBar();
        spinner.setIndeterminate(true);
        spinner.setWidth("20px");

        // Add text
        Span loadingText = new Span("AI is thinking...");
        loadingText.getStyle().set("font-style", "italic");

        loadingLayout.add(spinner, loadingText);
        loadingDiv.add(loadingLayout);

        loadingDiv.getStyle()
                .set("padding", "0.75rem 1rem")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("max-width", "80%")
                .set("background-color", "var(--lumo-contrast-10pct)")
                .set("color", "var(--lumo-secondary-text-color)");

        loadingWrapper.add(loadingDiv);
        chatHistoryContainer.add(loadingWrapper);

        // Scroll to bottom immediately
        chatHistoryContainer.getElement().executeJs("this.scrollTop = this.scrollHeight");

        // Get current UI instance
        UI ui = UI.getCurrent();

        // Execute chat request asynchronously
        CompletableFuture.supplyAsync(() -> {
            try {
                return chatService.chat(query);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(response -> {
            // Update UI in a thread-safe manner
            ui.access(() -> {
                // Remove loading indicator wrapper
                chatHistoryContainer.remove(loadingWrapper);

                // Add AI response
                addChatMessage(response, false);

                // Scroll to bottom
                chatHistoryContainer.getElement().executeJs("this.scrollTop = this.scrollHeight");
            });
        }).exceptionally(throwable -> {
            // Handle errors in a thread-safe manner
            ui.access(() -> {
                chatHistoryContainer.remove(loadingDiv);
                Notification notification = Notification.show(
                        "Error: " + throwable.getMessage(),
                        5000,
                        Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            });
            return null;
        });
    }

    private void addChatMessage(String message, boolean isUser) {
        // Create a wrapper div for proper layout
        Div messageWrapper = new Div();
        messageWrapper.setWidthFull();
        messageWrapper.getStyle()
                .set("display", "flex")
                .set("justify-content", isUser ? "flex-end" : "flex-start")
                .set("margin-bottom", "0.5rem");

        Div messageDiv = new Div();
        messageDiv.addClassName(isUser ? "user-message" : "ai-message");

        // Style the message bubble
        messageDiv.getStyle()
                .set("padding", "0.75rem 1rem")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("max-width", "80%")
                .set("word-wrap", "break-word");

        if (isUser) {
            messageDiv.getStyle()
                    .set("background-color", "var(--lumo-primary-color)")
                    .set("color", "var(--lumo-primary-contrast-color)");
            messageDiv.setText(message);
        } else {
            messageDiv.getStyle()
                    .set("background-color", "var(--lumo-contrast-10pct)")
                    .set("color", "var(--lumo-body-text-color)");

            // Convert markdown to HTML for AI messages
            String htmlContent = MarkdownRenderer.toHtml(message);
            Html htmlComponent = new Html("<div>" + htmlContent + "</div>");
            messageDiv.add(htmlComponent);
        }

        messageWrapper.add(messageDiv);
        chatHistoryContainer.add(messageWrapper);
    }

    private void confirmAndDelete(VectorStoreEntry entry) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Document");
        dialog.setText("Are you sure you want to delete this entry? This action cannot be undone.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(event -> {
            boolean success = vectorStoreService.deleteById(entry.id());

            if (success) {
                Notification notification = Notification.show(
                        "✓ Document deleted successfully",
                        3000,
                        Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshGrid();
            } else {
                Notification notification = Notification.show(
                        "✗ Failed to delete document",
                        5000,
                        Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    private void refreshGrid() {
        try {
            grid.setItems(vectorStoreService.getVectorStoreEntries());
        } catch (Exception e) {
            Notification notification = Notification.show(
                    "Error loading embeddings: " + e.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
