# Jenkins + Docker 설치 및 구성 가이드 (로컬 CD 파이프라인 테스트용)

## 📋 프로젝트 목적

이 프로젝트는 로컬 환경에서 CD 파이프라인 개선 작업을 테스트하기 위해 Jenkins를 Docker 컨테이너 위에 설치하고, Docker-in-Docker(dind) 환경을 구성하여 Jenkins 파이프라인에서 Docker 명령어를 안전하게 사용할 수 있도록 설정하는 환경을 제공합니다.

### 🎯 주요 목적
- **로컬 CD 파이프라인 테스트**: 실제 프로덕션 환경과 유사한 파이프라인을 로컬에서 안전하게 테스트
- **Input 스테이지 테스트**: nginx 서버를 통한 사용자 입력 기반 배포 워크플로우 검증
- **Docker 환경 연동**: Jenkins에서 Docker 명령어를 안전하게 실행할 수 있는 환경 구성

### 📁 포함된 구성 요소
- Jenkins 커스텀 이미지 (Blue Ocean 플러그인 포함)
- 실제 배포용 파이프라인 스크립트
- Input 파라미터 테스트용 파이프라인
- Azure Storage 연동 스크립트
- 웹 기반 테스트 환경 (nginx)

---

## 🛠️ 설치 및 구성

### 1. 환경 준비: Docker 네트워크 생성

```bash
docker network create jenkins
```

Jenkins와 Docker 데몬 컨테이너가 통신할 수 있는 전용 네트워크를 생성합니다.

**⚠️ 주의사항**: 기존에 `jenkins` 네트워크가 있다면 먼저 제거하세요.
```bash
docker network ls | grep jenkins
docker network rm jenkins  # 기존 네트워크가 있는 경우
```

---

### 2. Docker 데몬 컨테이너 실행 (docker:dind)

```bash
docker run \
--name jenkins-docker \
--rm \
--detach \
--privileged \
--network jenkins \
--network-alias docker \
--env DOCKER_TLS_CERTDIR=/certs \
--volume jenkins-docker-certs:/certs/client \
--volume jenkins-data:/var/jenkins_home \
--publish 2376:2376 \
docker:dind \
--storage-driver overlay2
```

- Docker 데몬을 컨테이너로 실행하여 네트워크와 TLS 보안 인증서로 관리합니다.
- Jenkins 컨테이너가 이 데몬에 원격 연결하여 Docker 명령을 실행할 수 있도록 합니다.

**⚠️ 주의사항**: 
- 기존에 `jenkins-docker` 컨테이너가 있다면 먼저 제거하세요.
- `jenkins-data`, `jenkins-docker-certs` 볼륨이 사용 중이라면 컨테이너를 먼저 중지하고 제거하세요.
```bash
docker ps -a | grep jenkins-docker
docker stop jenkins-docker && docker rm jenkins-docker  # 기존 컨테이너가 있는 경우
```

---

### 3. Jenkins 커스텀 이미지 빌드

**Dockerfile 특징:**
- Jenkins 공식 이미지 기반 (2.516.2-jdk21)
- Docker CLI 설치
- Blue Ocean 및 필수 Jenkins 플러그인 설치
  - blueocean: 현대적인 파이프라인 UI
  - docker-workflow: Docker 파이프라인 지원
  - json-path-api: JSON 데이터 처리

```bash
cd install
docker build -t myjenkins-blueocean:2.516.2-1 .
```

---

### 4. Jenkins 컨테이너 실행

```bash
docker run \
--name jenkins-blueocean \
--restart=on-failure \
--detach \
--network jenkins \
--env DOCKER_HOST=tcp://docker:2376 \
--env DOCKER_CERT_PATH=/certs/client \
--env DOCKER_TLS_VERIFY=1 \
--publish 8080:8080 \
--publish 50000:50000 \
--volume jenkins-data:/var/jenkins_home \
--volume jenkins-docker-certs:/certs/client:ro \
--volume /path/to/your/pipeline/scripts:/var/jenkins_home/pipelines:ro \
myjenkins-blueocean:2.516.2-1
```

**주요 설정:**
- `docker:dind` 컨테이너와 동일 네트워크에서 도커 데몬에 TLS 인증서로 원격 연결
- Jenkins 웹 UI (8080) 및 에이전트 통신 포트 (50000) 오픈
- Jenkins 홈 데이터 및 도커 인증서 볼륨 마운트
- 로컬 파이프라인 스크립트 디렉토리를 Jenkins 컨테이너 내 `/var/jenkins_home/pipelines`에 읽기 전용 마운트

**⚠️ 주의사항**: 
- 기존에 `jenkins-blueocean` 컨테이너가 있다면 먼저 제거하세요.
- 8080, 50000 포트가 이미 사용 중이라면 다른 포트로 변경하거나 기존 프로세스를 종료하세요.
```bash
docker ps -a | grep jenkins-blueocean
docker stop jenkins-blueocean && docker rm jenkins-blueocean  # 기존 컨테이너가 있는 경우

# 포트 사용 중인 프로세스 확인
lsof -i :8080
lsof -i :50000
```

---

## 📝 설치 완료 후 확인사항

### Jenkins 접속
- **웹 UI**: http://localhost:8080
- **초기 비밀번호**: `/var/jenkins_home/secrets/initialAdminPassword`에서 컨테이너 내 확인

### 주의사항
- 본 설치는 **로컬 CD 파이프라인 개선 작업을 테스트하기 위한 환경**입니다
- 파이프라인 스크립트 마운트 경로를 실제 로컬 경로로 수정해야 합니다
- Jenkins 데이터 초기화 시 `jenkins-data` 볼륨을 삭제하세요
- MacOS Homebrew 환경에서는 Docker가 VM 내에서 동작하므로 `/var/lib/docker` 경로가 없습니다
- 안정적인 Jenkins-Docker 연동을 위해 네트워크, 권한, TLS 인증서 설정에 유의하세요
