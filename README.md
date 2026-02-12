# Spring AI RAG App

A Retrieval-Augmented Generation (RAG) application built with **Java 25**, **Spring Boot 3.4**, **Spring AI**, and **Vaadin**. This application allows users to upload PDF documents, ingest them into a vector database, and chat with the content using a local LLM (Ollama).

## 🚀 Features

*   **Document Ingestion**: Upload PDF files via the UI.
*   **Vector Storage**: Automatically splits text and stores embeddings in **PostgreSQL (pgvector)**.
*   **RAG Chat**: Chat with your documents using **Ollama**.
*   **Modern UI**: Built with **Vaadin** for a seamless single-page experience.
*   **Visualization**: View raw vector embeddings stored in the database.

## 🛠️ Tech Stack

*   **Language**: Java 25
*   **Framework**: Spring Boot 3.4.2
*   **AI Integration**: Spring AI (Ollama, Tika, PgVector)
*   **Database**: PostgreSQL + pgvector extension
*   **UI**: Vaadin Flow
*   **LLM**: Ollama (running locally)

## ⚙️ Prerequisites & Setup

### 1. Database (PostgreSQL + pgvector)
You need a PostgreSQL instance with the `pgvector` extension enabled. The easiest way is to use Docker.

**Run this command:**
```bash
docker run -d \
  --name ragdb \
  -p 5432:5432 \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=ragdb \
  ankane/pgvector
```

### 2. LLM (Ollama)
This application uses Ollama to run the LLM locally.

1.  **Download & Install Ollama**: [https://ollama.com/](https://ollama.com/)
2.  **Start Ollama**: Ensure the Ollama service is running (usually on port `11434`).
3.  **Pull the Required Models**:
    This application is configured to use specific models for chat and embeddings. Run the following commands to download them:

    *   **Chat Model** (`gemma3:4b`):
        ```bash
        ollama pull gemma3:4b
        ```
    *   **Embedding Model** (`nomic-embed-text:v1.5`):
        ```bash
        ollama pull nomic-embed-text:v1.5
        ```

### 3. Java
Ensure you have **Java 25** installed.

## 🏃‍♂️ How to Run

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/<your-username>/spring-ai-rag-app.git
    cd spring-ai-rag-app
    ```

2.  **Run the application**:
    ```bash
    ./mvnw spring-boot:run
    ```

3.  **Access the UI**:
    Open your browser and navigate to: **http://localhost:8080**

## 📝 Usage

1.  **Upload**: Use the "Upload" button to select a PDF file.
2.  **Verify**: Check the Grid to see the ingested content and embeddings.
3.  **Chat**: Type a question in the chat box (e.g., "What is this document about?") and get an answer based on the uploaded context.
