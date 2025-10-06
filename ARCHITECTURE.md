# Arquitectura C4 — Introduction POC

Este documento agrupa los diagramas C4 en Mermaid y explica el flujo principal implementado por `SalesService.processSale` en el microservicio orquestador.

---

## Context (C4Context)

```mermaid
C4Context
  title Introduction POC - Context
  Enterprise_Boundary(b1, "Introduction POC") {
    Person(user, "Usuario", "Interacts with the SPA frontend")

    System(frontend, "Frontend (SPA)", "Vite + React")
    System(orchestrator, "Orchestrator Service", "Java Spring Boot - REST API")
    System(pdfs, "PDF Service", "Python - Generates invoice PDF")
    System(notification, "Notification Service", "Node.js - Sends emails")
    System_Ext(db, "Sales DB (SQLite)", "Local DB file used by Orchestrator")
  }

  Rel(user, frontend, "Uses")
  Rel(frontend, orchestrator, "Calls /api/sales → SalesController.processSale", "HTTP/JSON")
  Rel(orchestrator, db, "processSale step 1: saveSale(...) → persist sale", "JDBC/SQL")
  Rel(orchestrator, pdfs, "processSale step 2: generatePdf(...) (sync)", "HTTP/JSON")
  Rel(orchestrator, notification, "processSale step 3: sendInvoiceEmail(...) (async)", "HTTP/JSON")
  Rel(notification, user, "Sends email to", "SMTP")
```

---

## Containers (C4Container)

```mermaid
flowchart TB
    %% Containers
    subgraph system["Introduction POC System"]
        browser["Frontend SPA\n[Vite + React]\nRenders UI and calls APIs"]
        orchestrator["Orchestrator Service\n[Spring Boot]\nCoordinates flows"]
        pdfsvc["PDF Service\n[Python]\nGenerates PDF files"]
        notification["Notification Service\n[Node.js]\nSends emails"]
        sqldb[("Sales DB\n[SQLite]\nStores sales")]
    end

    %% Relationships
    browser -->|"HTTP/JSON\n/api/sales"| orchestrator
    orchestrator -->|"JDBC/SQL\nsaveSale()"| sqldb
    orchestrator -->|"HTTP POST\ngeneratePdf() [sync]"| pdfsvc
    orchestrator -->|"HTTP POST\nsendEmail() [async]"| notification
    notification -.->|"optional read"| sqldb

    %% Styling
    classDef container fill:#1168bd,stroke:#0b4884,color:#ffffff
    classDef database fill:#2c3e50,stroke:#2c3e50,color:#ffffff
    class browser,orchestrator,pdfsvc,notification container
    class sqldb database

    %% Note
    note["orchestrator-service/.../SalesService.java\nprocessSale(): save → pdf → email"]
    orchestrator -.- note
```

---

## Component (C4Component) — Orchestrator Service

```mermaid
flowchart TB
    %% Components
    subgraph orchestrator["Orchestrator Service [Spring Boot]"]
        direction TB
        api["SalesController\n[REST Controller]\nExposes /api/sales"]
        service["SalesService\n[Service]\nOrchestrates processing"]
        repo["SaleRepository\n[JPA Repository]\nPersists sales"]
        pdfClient["PdfServiceClient\n[HTTP Client]\nGenerates PDFs"]
        emailClient["EmailServiceClient\n[HTTP Client]\nSends emails"]
        security["ApiKeyFilter\n[Filter]\nAPI security"]
    end

    %% Relationships
    security -->|protects| api
    api -->|"processSale(request)"| service
    service -->|"1. saveSale()"| repo
    service -->|"2. generatePdf() [sync]"| pdfClient
    service -->|"3. sendEmail() [async]"| emailClient

    %% Styling
    classDef component fill:#85bbf0,stroke:#5d82a8,color:#000000
    classDef primary fill:#1168bd,stroke:#0b4884,color:#ffffff
    class api,security component
    class service,repo,pdfClient,emailClient primary

    %% Note
    note["orchestrator-service/.../SalesService.java\nprocessSale(): coordinates end-to-end flow"]
    service -.- note
```

---

## Flujo `processSale` (C4Code)

```mermaid
sequenceDiagram
    title SalesService.processSale Flow
    participant SC as SalesController
    participant SS as SalesService
    participant SR as SaleRepository
    participant PS as PDF Service
    participant NS as Notification Service
    participant DB as Sales DB

    SC->>+SS: processSale(SaleRequest)
    SS->>SR: saveSale(sale)
    SR->>DB: persist Sale entity
    DB-->>SR: sale saved
    SR-->>SS: saved Sale
    
    SS->>+PS: generatePdf(sale) [sync]
    PS-->>-SS: PDF bytes
    
    SS->>NS: sendInvoiceEmail(sale, pdf) [async/fire-and-forget]
    Note over SS,NS: Email sending continues independently
    
    SS-->>-SC: SaleResponse
```

El diagrama muestra el flujo orquestador implementado por `SalesService.processSale(SaleRequest)`:

1. **Persistencia**: La venta se guarda en la base de datos a través de `saveSale(...)` (serializa productos a JSON).
2. **PDF**: Se solicita al `pdf-service` la generación síncrona de la factura.
3. **Email**: Se envía una petición asíncrona al `notification-service` para el envío del email.

El flujo principal continúa incluso si el envío del email falla, ya que la venta persiste en base de datos.