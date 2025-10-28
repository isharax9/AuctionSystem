# 🏆 Distributed Online Auction System

<div align="center">

![Java](https://img.shields.io/badge/Java-11+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Jakarta EE](https://img.shields.io/badge/Jakarta_EE-10-orange?style=for-the-badge&logo=eclipse&logoColor=white)
![GlassFish](https://img.shields.io/badge/GlassFish-7.x-blue?style=for-the-badge&logo=eclipse&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)
![Build](https://img.shields.io/badge/Build-Maven-red?style=for-the-badge&logo=apache-maven&logoColor=white)

**A robust, enterprise-grade auction system built with Jakarta EE, featuring real-time bidding, distributed messaging, and comprehensive session management.**

[Features](#-features) •
[Quick Start](#-quick-start) •
[Documentation](#-documentation) •
[Architecture](#-architecture) •
[Contributing](#-contributing) •
[License](#-license)

</div>

---

## 📋 Table of Contents

- [About The Project](#-about-the-project)
- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Configuration](#configuration)
  - [Running the Application](#running-the-application)
- [Usage](#-usage)
- [API Documentation](#-api-documentation)
- [Project Structure](#-project-structure)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Roadmap](#-roadmap)
- [Contributing](#-contributing)
- [License](#-license)
- [Contact](#-contact)
- [Acknowledgments](#-acknowledgments)

---

## 🎯 About The Project

The **Distributed Online Auction System** is a comprehensive enterprise application designed to facilitate online auctions with real-time bidding capabilities. Built using Jakarta EE 10 and modern Java technologies, it demonstrates best practices in distributed system design, message-driven architecture, and enterprise application development.

### Why This Project?

- 🎓 **Educational**: Perfect for learning enterprise Java development patterns
- 🏢 **Enterprise-Ready**: Built with production-grade technologies and patterns
- 🚀 **Scalable**: Designed with distributed architecture principles
- 🔄 **Real-Time**: WebSocket-based real-time bid notifications
- 🔒 **Secure**: Session management and user authentication

---

## ✨ Features

### Core Functionality

- 🏷️ **Auction Management**
  - Create, view, and manage auctions
  - Configurable auction duration (hours and minutes)
  - Automatic expiration handling
  - Auction history and tracking
  - Status management (Active, Ended, Expired, Cancelled)

- 💰 **Bidding System**
  - Real-time bid placement
  - Concurrent bid handling with thread safety
  - Bid validation and verification
  - Highest bid tracking
  - Complete bid history

- 👥 **User Management**
  - User registration and authentication
  - Email validation
  - Session management
  - User profile tracking
  - Activity monitoring

### Advanced Features

- 📡 **Real-Time Notifications**
  - WebSocket-based live updates
  - Instant bid notifications
  - Auction status updates
  - Multi-user broadcasting

- 🔄 **Distributed Messaging**
  - JMS (Jakarta Messaging Service) integration
  - Message-Driven Beans for asynchronous processing
  - Event-driven architecture
  - Decoupled service communication

- 🎛️ **Session Management**
  - Active session tracking
  - Singleton session manager
  - User activity monitoring
  - Session analytics

- 📊 **Statistics & Analytics**
  - Total auction count
  - Active/completed auction metrics
  - Bid statistics
  - User engagement metrics
  - Auction value tracking

### Technical Features

- ⚡ **High Performance**
  - Concurrent HashMap for thread-safe operations
  - Atomic operations for counters
  - Stateless EJB for scalability
  - Singleton pattern for shared resources

- 🧪 **Comprehensive Testing**
  - Unit tests with JUnit 5
  - Integration tests
  - Concurrency tests
  - Performance tests
  - Mock support with Mockito

- 🔧 **Developer-Friendly**
  - Clean code architecture
  - RESTful API design
  - Comprehensive logging
  - Error handling
  - Maven build system

---

## 🛠️ Technology Stack

### Backend Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 11+ | Core programming language |
| **Jakarta EE** | 10.0.0 | Enterprise application framework |
| **EJB** | 4.0.1 | Business logic components |
| **JMS** | 3.1.0 | Asynchronous messaging |
| **CDI** | 4.0.1 | Dependency injection |
| **Servlet API** | 6.0.0 | Web layer |
| **WebSocket** | - | Real-time communication |

### Frontend Technologies

| Technology | Purpose |
|------------|---------|
| **JSP** | Server-side rendering |
| **JavaScript** | Client-side logic |
| **CSS3** | Styling |
| **WebSocket API** | Real-time updates |

### Build & Testing

| Tool | Version | Purpose |
|------|---------|---------|
| **Maven** | 3.x | Build automation |
| **JUnit Jupiter** | 5.9.3 | Unit testing |
| **Mockito** | 5.3.1 | Mocking framework |
| **Testcontainers** | 1.18.3 | Integration testing |
| **JaCoCo** | 0.8.8 | Code coverage |

### Server

| Server | Version | Purpose |
|--------|---------|---------|
| **GlassFish** | 7.x | Application server |

---

## 🏗️ Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│  (Web Browser - JSP, JavaScript, WebSocket Client)          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
│              (Servlets, WebSocket Endpoints)                │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      Business Layer                          │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│   │ Auction EJB  │  │   Bid EJB    │  │  User EJB    │    │
│   │  (Stateless) │  │  (Stateless) │  │  (Stateless) │    │
│   └──────────────┘  └──────────────┘  └──────────────┘    │
│   ┌──────────────┐  ┌──────────────┐                       │
│   │ Session Mgr  │  │   Auction    │                       │
│   │ (Singleton)  │  │   Manager    │                       │
│   └──────────────┘  │ (Singleton)  │                       │
│                     └──────────────┘                        │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    Messaging Layer (JMS)                     │
│              ┌──────────────────────────┐                   │
│              │  Bid Notification MDB    │                   │
│              │  (Message Driven Bean)   │                   │
│              └──────────────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

#### 1. **Enterprise JavaBeans (EJB)**

- **AuctionServiceBean**: Manages auction lifecycle and operations
- **BidServiceBean**: Handles bid placement and validation
- **UserServiceBean**: User management and authentication
- **UserSessionManagerBean**: Session tracking (Singleton)
- **AuctionManagerSingleton**: Auction expiration monitoring (Singleton)
- **BidNotificationMDB**: Asynchronous bid notifications (Message-Driven Bean)

#### 2. **Data Transfer Objects (DTO)**

- **AuctionDTO**: Auction data transfer
- **BidDTO**: Bid information transfer
- **BidUpdateMessage**: JMS message structure

#### 3. **Entity Classes**

- **Auction**: Core auction entity with history tracking
- **Bid**: Bid entity with validation
- **User**: User entity with authentication

#### 4. **Web Layer**

- **AuctionServlet**: Main auction operations
- **SessionAdminServlet**: Session management admin interface
- **WebSocket Endpoints**: Real-time communication

### Design Patterns Used

| Pattern | Implementation | Purpose |
|---------|----------------|---------|
| **Singleton** | AuctionManagerSingleton, UserSessionManagerBean | Shared state management |
| **Stateless Session Bean** | AuctionServiceBean, BidServiceBean | Scalable business logic |
| **Message-Driven Bean** | BidNotificationMDB | Asynchronous processing |
| **Data Transfer Object** | AuctionDTO, BidDTO | Data encapsulation |
| **Front Controller** | AuctionServlet | Centralized request handling |
| **Observer** | WebSocket + JMS | Event notification |

---

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK)** 11 or higher
  ```bash
  java -version
  ```

- **Apache Maven** 3.6 or higher
  ```bash
  mvn -version
  ```

- **GlassFish Server** 7.x
  - Download from [GlassFish Downloads](https://glassfish.org/download)
  - Or use Docker:
    ```bash
    docker pull glassfish:7.0.0-jdk11
    ```

- **Git**
  ```bash
  git --version
  ```

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/isharax9/AuctionSystem.git
   cd AuctionSystem
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run tests**
   ```bash
   mvn test
   ```

4. **Package the application**
   ```bash
   mvn package
   ```
   This creates `target/AuctionSystem.war`

### Configuration

#### 1. GlassFish Setup

**Option A: Manual Setup**

1. Start GlassFish:
   ```bash
   cd $GLASSFISH_HOME/bin
   ./asadmin start-domain domain1
   ```

2. Access Admin Console:
   - URL: `http://localhost:4848`
   - Default credentials: admin/admin

3. Configure JMS Resources:
   ```bash
   ./asadmin create-jms-resource --restype jakarta.jms.Queue --property Name=BidNotificationQueue jms/BidNotificationQueue
   ./asadmin create-jms-resource --restype jakarta.jms.QueueConnectionFactory jms/BidNotificationQueueFactory
   ```

**Option B: Docker Setup**

```bash
docker run -d \
  -p 8080:8080 \
  -p 4848:4848 \
  --name glassfish \
  glassfish:7.0.0-jdk11
```

#### 2. Application Configuration

The application uses default configurations. To customize:

1. Edit `src/main/webapp/WEB-INF/web.xml` for servlet configuration
2. Modify connection pool settings in GlassFish admin console
3. Adjust session timeout in web.xml if needed

### Running the Application

1. **Deploy to GlassFish**
   
   **Using Admin Console:**
   - Navigate to `Applications` → `Deploy`
   - Select `target/AuctionSystem.war`
   - Click `Deploy`

   **Using Command Line:**
   ```bash
   cd $GLASSFISH_HOME/bin
   ./asadmin deploy /path/to/AuctionSystem/target/AuctionSystem.war
   ```

   **Using Maven (if configured):**
   ```bash
   mvn glassfish:deploy
   ```

2. **Access the Application**
   - Main application: `http://localhost:8080/AuctionSystem/`
   - Real-time notifications: `http://localhost:8080/AuctionSystem/real-time-notifications.html`
   - Session admin: `http://localhost:8080/AuctionSystem/session-admin`

3. **Verify Deployment**
   ```bash
   ./asadmin list-applications
   ```

---

## 📖 Usage

### Basic Auction Workflow

1. **Register/Login**
   - Create a user account
   - System validates email uniqueness

2. **Create Auction**
   ```java
   // Example: Create auction via API
   POST /AuctionSystem/auction
   {
     "title": "Vintage Watch",
     "description": "Classic 1960s Rolex",
     "startingPrice": 500.0,
     "durationHours": 2,
     "durationMinutes": 30
   }
   ```

3. **Place Bid**
   ```java
   // Example: Place bid via API
   POST /AuctionSystem/auction/{auctionId}/bid
   {
     "amount": 550.0,
     "username": "bidder123"
   }
   ```

4. **Monitor Auctions**
   - Real-time updates via WebSocket
   - View active auctions
   - Check auction history

5. **View Results**
   - Check auction winners
   - View bid history
   - See completed auctions

### Code Examples

#### Creating an Auction

```java
@Inject
private AuctionServiceRemote auctionService;

public void createAuction() {
    AuctionDTO auction = auctionService.createAuction(
        "Vintage Camera",
        "Rare 1950s film camera in excellent condition",
        250.0,
        3,  // hours
        0   // minutes
    );
}
```

#### Placing a Bid

```java
@Inject
private BidServiceRemote bidService;

public void placeBid(Long auctionId, String username, double amount) {
    BidDTO bid = bidService.placeBid(auctionId, username, amount);
    if (bid != null) {
        System.out.println("Bid placed successfully!");
    }
}
```

#### Real-Time Updates (JavaScript)

```javascript
// Connect to WebSocket
const socket = new WebSocket('ws://localhost:8080/AuctionSystem/auction-updates');

socket.onmessage = function(event) {
    const update = JSON.parse(event.data);
    console.log('New bid:', update.bidAmount);
    updateUI(update);
};
```

---

## 🔌 API Documentation

### Auction Endpoints

| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| `GET` | `/auction` | List all active auctions | - |
| `GET` | `/auction/{id}` | Get auction details | id (path) |
| `POST` | `/auction` | Create new auction | JSON body |
| `POST` | `/auction/{id}/bid` | Place a bid | id (path), JSON body |
| `GET` | `/auction/history` | Get completed auctions | page, size (query) |
| `POST` | `/auction/{id}/close` | Close auction | id (path) |

### Request/Response Examples

#### Create Auction

**Request:**
```json
POST /auction
Content-Type: application/json

{
  "title": "Antique Vase",
  "description": "Ming Dynasty ceramic vase",
  "startingPrice": 1000.0,
  "durationHours": 4,
  "durationMinutes": 30
}
```

**Response:**
```json
{
  "auctionId": 1,
  "title": "Antique Vase",
  "description": "Ming Dynasty ceramic vase",
  "startingPrice": 1000.0,
  "currentHighestBid": 1000.0,
  "currentHighestBidder": null,
  "startTime": "2024-01-15T10:00:00",
  "endTime": "2024-01-15T14:30:00",
  "active": true,
  "totalBidsCount": 0,
  "status": "ACTIVE"
}
```

#### Place Bid

**Request:**
```json
POST /auction/1/bid
Content-Type: application/json

{
  "username": "bidder123",
  "amount": 1100.0
}
```

**Response:**
```json
{
  "bidId": 1,
  "auctionId": 1,
  "username": "bidder123",
  "amount": 1100.0,
  "timestamp": "2024-01-15T10:15:00"
}
```

---

## 📂 Project Structure

```
AuctionSystem/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── auction/
│   │   │           ├── config/          # Configuration classes
│   │   │           │   └── WebSocketConfigurator.java
│   │   │           ├── dto/             # Data Transfer Objects
│   │   │           │   ├── AuctionDTO.java
│   │   │           │   ├── BidDTO.java
│   │   │           │   └── BidUpdateMessage.java
│   │   │           ├── ejb/             # Enterprise JavaBeans
│   │   │           │   ├── AuctionServiceBean.java
│   │   │           │   ├── AuctionServiceRemote.java
│   │   │           │   ├── AuctionManagerSingleton.java
│   │   │           │   ├── BidServiceBean.java
│   │   │           │   ├── BidServiceRemote.java
│   │   │           │   ├── BidNotificationMDB.java
│   │   │           │   ├── UserServiceBean.java
│   │   │           │   └── UserServiceRemote.java
│   │   │           ├── entity/          # Domain entities
│   │   │           │   ├── Auction.java
│   │   │           │   ├── Bid.java
│   │   │           │   └── User.java
│   │   │           ├── servlet/         # Web servlets
│   │   │           │   ├── AuctionServlet.java
│   │   │           │   └── SessionAdminServlet.java
│   │   │           └── session/         # Session management
│   │   │               ├── UserSessionManagerBean.java
│   │   │               ├── UserSessionManagerRemote.java
│   │   │               └── ActiveSessionInfo.java
│   │   ├── resources/                   # Application resources
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml              # Web application descriptor
│   │       ├── css/                     # Stylesheets
│   │       │   ├── enhanced-auction-styles.css
│   │       │   └── websocket-styles.css
│   │       ├── js/                      # JavaScript files
│   │       │   └── auction-websocket.js
│   │       ├── error/                   # Error pages
│   │       │   ├── 404.html
│   │       │   ├── 500.html
│   │       │   └── general.html
│   │       ├── index.jsp                # Main page
│   │       ├── real-time-notifications.html
│   │       └── favicon.ico
│   └── test/
│       └── java/
│           └── com/
│               └── auction/
│                   ├── ejb/             # EJB tests
│                   ├── entity/          # Entity tests
│                   ├── integration/     # Integration tests
│                   └── performance/     # Performance tests
├── target/                              # Build output
├── pom.xml                              # Maven configuration
├── README.md                            # This file
├── LICENSE                              # MIT License
├── CONTRIBUTING.md                      # Contribution guidelines
└── .gitignore                           # Git ignore rules
```

---

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuctionServiceBeanTest

# Run with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Test Categories

1. **Unit Tests** (`src/test/java/com/auction/ejb/`)
   - AuctionServiceBeanTest
   - BidServiceBeanTest
   - UserServiceBeanTest

2. **Entity Tests** (`src/test/java/com/auction/entity/`)
   - AuctionTest
   - BidTest
   - UserTest

3. **Integration Tests** (`src/test/java/com/auction/integration/`)
   - AuctionFlowIntegrationTest
   - ConcurrencyTest

4. **Performance Tests** (`src/test/java/com/auction/performance/`)
   - PerformanceTest

### Test Coverage

The project aims for >80% code coverage. Current coverage can be viewed in the JaCoCo report after running tests.

---

## 🚢 Deployment

### Production Deployment

1. **Build for Production**
   ```bash
   mvn clean package -Pprod
   ```

2. **Configure Production Server**
   - Set appropriate heap size
   - Configure connection pools
   - Enable production logging
   - Set up SSL/TLS certificates

3. **Deploy**
   ```bash
   ./asadmin deploy --force=true --name AuctionSystem target/AuctionSystem.war
   ```

4. **Post-Deployment Verification**
   ```bash
   ./asadmin list-applications
   curl http://your-server:8080/AuctionSystem/auction
   ```

### Docker Deployment

```dockerfile
FROM glassfish:7.0.0-jdk11

COPY target/AuctionSystem.war /opt/glassfish7/glassfish/domains/domain1/autodeploy/

EXPOSE 8080 4848

CMD ["asadmin", "start-domain", "--verbose"]
```

Build and run:
```bash
docker build -t auction-system .
docker run -d -p 8080:8080 -p 4848:4848 auction-system
```

### Environment Configuration

| Environment | URL | Database | JMS |
|-------------|-----|----------|-----|
| **Development** | localhost:8080 | In-Memory | Local Queue |
| **Staging** | staging.example.com | PostgreSQL | ActiveMQ |
| **Production** | www.example.com | PostgreSQL (HA) | ActiveMQ (Cluster) |

---

## 🗺️ Roadmap

### Current Version: 1.0.0

### Planned Features

- [ ] **Database Integration**
  - PostgreSQL/MySQL persistence
  - JPA/Hibernate implementation
  - Database migration scripts

- [ ] **Enhanced Security**
  - OAuth2/JWT authentication
  - Role-based access control (RBAC)
  - API rate limiting
  - HTTPS enforcement

- [ ] **Advanced Features**
  - Image upload for auctions
  - Payment gateway integration
  - Email notifications
  - SMS alerts
  - Advanced search and filtering

- [ ] **Performance Improvements**
  - Redis caching layer
  - Database query optimization
  - Load balancing support
  - CDN integration

- [ ] **Monitoring & Analytics**
  - Prometheus metrics
  - Grafana dashboards
  - ELK stack integration
  - Real-time analytics

- [ ] **Mobile Support**
  - Responsive design improvements
  - Progressive Web App (PWA)
  - Native mobile app APIs

- [ ] **DevOps**
  - CI/CD pipeline
  - Kubernetes deployment
  - Automated testing
  - Infrastructure as Code

### Completed Features ✅

- [x] Core auction management
- [x] Real-time bidding
- [x] User management
- [x] JMS integration
- [x] WebSocket support
- [x] Session management
- [x] Comprehensive testing
- [x] Maven build system

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**!

Please see our [Contributing Guidelines](CONTRIBUTING.md) for detailed information on:

- Code of Conduct
- Development workflow
- Coding standards
- Pull request process
- Testing requirements

### Quick Start for Contributors

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 isharax9

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files...
```

---

## 💬 Contact & Support

<div align="center">

### Get Help & Connect

</div>

| Platform | Link | Purpose |
|----------|------|---------|
| 📧 **Email** | [isharax9@gmail.com](mailto:isharax9@gmail.com) | Direct support & inquiries |
| 💬 **Telegram** | [@mac_knight141](https://t.me/mac_knight141) | Quick questions & community |
| 💼 **LinkedIn** | [isharax9](https://www.linkedin.com/in/isharax9/) | Professional networking |
| 📸 **Instagram** | [@mac_knight141](https://www.instagram.com/mac_knight141/) | Updates & behind-the-scenes |
| 🐦 **Twitter** | [@isharax9](https://twitter.com/isharax9) | Announcements & tech discussions |
| 🐙 **GitHub** | [@isharax9](https://github.com/isharax9) | Source code & issues |

### Support This Project

If you find this project helpful, please consider:
- ⭐ Starring the repository
- 🐛 Reporting bugs and issues
- 💡 Suggesting new features
- 📖 Improving documentation
- 🔀 Contributing code

---

## 🙏 Acknowledgments

- **Jakarta EE Community** - For the robust enterprise platform
- **GlassFish Team** - For the excellent application server
- **Maven** - For streamlined build management
- **JUnit & Mockito** - For comprehensive testing tools
- **All Contributors** - For making this project better

### Special Thanks

- Enterprise Java development community
- Stack Overflow contributors
- Open-source software maintainers

---

## 📊 Project Statistics

<div align="center">

![GitHub repo size](https://img.shields.io/github/repo-size/isharax9/AuctionSystem)
![GitHub code size](https://img.shields.io/github/languages/code-size/isharax9/AuctionSystem)
![GitHub language count](https://img.shields.io/github/languages/count/isharax9/AuctionSystem)
![GitHub top language](https://img.shields.io/github/languages/top/isharax9/AuctionSystem)

</div>

---

## 🔗 Related Projects

- [Enterprise Java Examples](https://github.com/javaee-samples)
- [Jakarta EE Tutorial](https://eclipse-ee4j.github.io/jakartaee-tutorial/)
- [GlassFish Documentation](https://glassfish.org/documentation)

---

<div align="center">

### ⭐ Star this repository if you find it helpful!

**Made with ❤️ by [isharax9](https://github.com/isharax9)**

</div>
