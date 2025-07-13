``` mermaid
graph TD
    subgraph Clients
        C1[Web / Mobile App]
    end

    subgraph Keycloak
        KC[Keycloak Auth Server]
    end

    subgraph API Gateway
        AG[API Gateway Spring Cloud Gateway]
    end

    subgraph Order Service
        OSDB[(PostgreSQL)]
        OS[Order Service]
        OS --> OSDB
        OS -->|Publish: OrderPlaced| RMQ[(RabbitMQ)]
        RMQ -->|InventoryReserved / InventoryRejected| OS
    end

    subgraph Product Service
        PSDB[(PostgreSQL)]
        PS[Product Service]
        PS --> PSDB
        RMQ -->|OrderPlaced| PS
        PS -->|Publish: InventoryReserved / InventoryRejected| RMQ
    end

    C1 -->|Login Request| KC
    KC -->|Access Token JWT| C1
    C1 -->|Bearer JWT| AG
    AG -->|Forward Request| OS
    AG -->|Forward Request| PS

    style KC fill:#f9f,stroke:#333,stroke-width:2
    style RMQ fill:#fff3b0,stroke:#333,stroke-width:2,stroke-dasharray: 5 5
    style PS fill:#bfb,stroke:#333,stroke-width:2
    style OS fill:#fbf,stroke:#333,stroke-width:2
    style AG fill:#bbf,stroke:#333,stroke-width:2
```