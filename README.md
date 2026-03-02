# 🛡️ Zero Trust Cloud Terminal

클라우드 멀티 테넌트 환경에서 관리자(Admin)가 가상 인스턴스(Kubernetes Pod)를 동적으로 프로비저닝하고, 인가된 사용자(User)가 웹 브라우저를 통해 안전하게 SSH 터미널에 접속할 수 있도록 지원하는 **Zero Trust Network Access (ZTNA) 기반 Web SSH 플랫폼**입니다.

## 시연장면
![시연장면](https://github.com/user-attachments/assets/aaa65c8f-f05c-4d08-884c-52fb2c34cacc)


## 🚀 아키텍처 및 핵심 로직
- **단일 진입점 제어:** 사용자는 오직 Spring Server(웹 서버)를 통해서만 터미널에 접근할 수 있으며, K8s 내부의 컨테이너와 직접 통신할 수 없습니다.
- **동적 프로비저닝 (Fabric8):** 관리자가 서버 생성을 요청하면 Kubernetes API를 호출하여 즉시 Ubuntu 기반 터미널 Pod를 생성합니다.
- **Web SSH 스트리밍:** 프론트엔드(xterm.js)와 백엔드는 WebSocket(STOMP)으로 연결되며, 백엔드와 K8s Pod 사이는 Fabric8 Exec Watch를 통해 터미널 I/O를 스트리밍합니다.
- **분산 환경 대응 (Redis Pub/Sub):** 터미널의 입력(Input) 스트림을 Redis Pub/Sub으로 관리하여, 다중 인스턴스 환경에서도 안정적인 세션 유지가 가능하도록 설계되었습니다.

## 🛠 기술 스택

### Backend
- **Language & Framework:** Java 21, Spring Boot 3.4.3
- **Build Tool:** Gradle
- **Authentication:** Spring Security + OAuth2 Resource Server (Keycloak 연동)
- **Database:** Spring Data JPA, H2 Database (In-Memory)
- **Communication:** Spring WebSocket
- **Infrastructure SDK:** Fabric8 Kubernetes Client (v6.13.4), Spring Data Redis

### Frontend
- HTML5, Vanilla JS, Bootstrap 5
- **xterm.js** (Web Terminal UI)
- **Keycloak JS Adapter** (SSO Login)
- SockJS & Stomp.js (WebSocket 통신)

### Infrastructure
- Kubernetes (Minikube or Docker Desktop K8s)
- Docker & Docker Compose (Keycloak, Redis 실행용)

---

## ✨ 핵심 기능

### 👨‍💻 Admin (관리자)
- **서버 프로비저닝:** K8s 클러스터(Namespace: `ztna-terminals`)에 신규 Ubuntu 터미널 Pod 동적 생성.
- **권한 제어 (RBAC):** 생성된 서버에 대해 특정 User에게 접근 권한(FULL_ACCESS, READ_ONLY) 부여.
- **서버 및 유저 조회:** 시스템에 등록된 전체 서버 목록 및 동기화된 유저 목록 조회.

### 👤 User (사용자)
- **SSO 로그인:** Keycloak을 통한 안전한 인증 및 JWT 토큰 기반 인가.
- **대시보드:** 관리자로부터 접근 권한을 부여받은 서버 목록만 제한적으로 조회.
- **Web 터미널 접속:** 권한이 있는 서버 클릭 시, 브라우저 상에서 즉시 K8s Pod의 Bash 쉘(Bash Shell)에 접속 및 명령어 실행.

---

## 🚦 Getting Started (로컬 실행 방법)

### 1. 사전 요구사항 (Prerequisites)
- Java 21 이상 설치
- Docker & Docker Compose 설치
- 로컬 Kubernetes 환경 (Docker Desktop 내장 K8s 또는 Minikube) 구동 중일 것

### 2. 인프라 실행 (Keycloak & Redis)
프로젝트 루트 디렉토리에서 `docker-compose`를 사용하여 Keycloak(인증 서버)과 Redis(Pub/Sub)를 실행합니다.
```bash
docker-compose up -d
```
메인 spring 백엔드 서버를 실행후 :8081포트의 /index.html에 접속합니다
