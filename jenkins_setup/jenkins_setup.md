# Jenkins 웹 초기 설정 가이드

## 📋 개요

이 문서는 Docker로 Jenkins를 설치한 후 웹 브라우저에서 Jenkins 초기 설정을 완료하는 과정을 단계별로 설명합니다.

## 🚀 1단계: Jenkins 접속 및 초기 비밀번호 확인

### 1.1 Jenkins 웹 UI 접속
브라우저에서 `http://localhost:8080`으로 접속합니다.

### 1.2 초기 관리자 비밀번호 확인
Jenkins 초기 설정 화면에서 초기 비밀번호를 입력해야 합니다. 다음 명령어로 비밀번호를 확인하세요:

```bash
# Jenkins 컨테이너 내에서 초기 비밀번호 확인
docker exec jenkins-blueocean cat /var/jenkins_home/secrets/initialAdminPassword
```

## 🔧 2단계: Jenkins 초기 설정

### 2.1 플러그인 설치 선택
- 초기 비밀번호 입력 후 다음 중 하나를 선택합니다.
    - Install suggested plugins: Jenkins에서 추천하는 기본 플러그인들을 자동으로 설치
        - Git plugin
        - Pipeline plugin
        - Blue Ocean plugin
        - Docker plugin
        - Credentials plugin
    - Select plugins to install: 필요한 플러그인만 선택적으로 설치
- Install suggested plugins로 설치

### 2.2 플러그인 설치 대기
선택한 플러그인들이 자동으로 다운로드되고 설치됩니다. 이 과정은 인터넷 속도에 따라 몇 분에서 10분 정도 소요될 수 있습니다.

### 2.3 Stage View 플러그인 확인 (빌드 진행상황 시각화)

**플러그인 목적**: 빌드 진행상황을 stage별로 시각적으로 표시하여 모니터링 편의성 향상

**기본 설치 상태**: Jenkins 기본 설치에 포함되어 있음 (별도 설치 불필요)

**Stage View 확인 방법**:
1. **Jenkins 대시보드**에서 **빌드 실행**
2. **빌드 번호** 클릭하여 빌드 상세 페이지 접속
3. **"Stage View"** 탭에서 stage별 진행상황 확인

**Stage View에서 보이는 정보**:
- **Stage별 진행상황**: 각 stage의 실행/대기/완료/실패 상태
- **실행 시간**: 각 stage의 소요 시간
- **실행 순서**: stage 간의 의존성 및 실행 순서
- **빌드 히스토리**: 이전 빌드들과의 비교

**Blue Ocean과의 차이점**:
- **Stage View**: 기본 Jenkins UI에서 제공하는 기본적인 stage 진행상황
- **Blue Ocean**: 더 현대적이고 시각적인 파이프라인 UI (권장)

## 👤 3단계: 관리자 계정 생성

### 3.1 관리자 정보 입력
플러그인 설치 완료 후 관리자 계정 정보를 입력합니다:

**필수 입력 항목:**
- **Username**: 관리자 사용자명 (예: `admin`, `jenkins-admin`)
- **Password**: 안전한 비밀번호 (8자 이상, 특수문자 포함 권장)
- **Full name**: 전체 이름 (예: `Jenkins Administrator`)
- **E-mail address**: 이메일 주소 (선택사항이지만 권장)

**비밀번호 보안 권장사항:**
- 최소 8자 이상
- 대문자, 소문자, 숫자, 특수문자 조합
- 개인정보나 단순한 단어 사용 금지

### 3.2 Jenkins URL 설정
Jenkins 인스턴스의 URL을 설정합니다:
- **기본값**: `http://localhost:8080/`
- **변경이 필요한 경우**: 프록시나 리버스 프록시를 사용하는 경우

## 🎯 4단계: Jenkins 준비 완료

### 4.1 시작 페이지 확인
모든 설정이 완료되면 Jenkins 시작 페이지가 표시됩니다.

### 4.2 기본 대시보드 확인
- 왼쪽 메뉴에 기본 메뉴들이 표시됩니다
- "Welcome to Jenkins!" 메시지가 표시됩니다

## 👥 5단계: 사용자 계정 생성 (선택사항)

### 5.1 사용자 계정 생성
**경로**: `Manage Jenkins` → `Security` -> `Users` → `Create User`

**입력 항목:**
- **Username**: 사용자명
- **Password**: 비밀번호
- **Full name**: 전체 이름
- **E-mail address**: 이메일 주소

### 5.2 권한 설정
**경로**: `Manage Jenkins` → `Security` -> `Security`

**권한 설정 옵션:**
- **Logged-in users can do anything**: 로그인한 사용자는 모든 작업 가능
- **Matrix-based security**: 세부적인 권한 설정
- **Project-based Matrix Authorization Strategy**: 프로젝트별 권한 설정

## 🏗️ 6단계: 첫 번째 Job 생성

### 6.1 새 Job 생성
**경로**: Jenkins 대시보드 → `New Item` 또는 `새로운 Item`

### 6.2 Job 유형 선택
**권장 Job 유형:**
- **Pipeline**: Jenkinsfile을 사용한 파이프라인 (가장 유연함)
- **Freestyle project**: 간단한 빌드 작업
- **Multibranch Pipeline**: 여러 브랜치를 자동으로 감지

### 6.3 Job 설정
**기본 설정:**
- **Name**: Job 이름 (예: `test-pipeline`)
- **Description**: Job 설명 (선택사항)

**Pipeline 설정 (Pipeline Job 선택 시):**
- **Definition**: Pipeline script from SCM 또는 Pipeline script
- **Script**: Pipeline 스크립트 입력

**Jenkins 웹 UI에서 설정하는 방법:**

### **1. 동시 빌드 제어**
- **General** 섹션에서 **"Do not allow concurrent builds"** 체크박스 선택
- **Build Environment** 섹션에서 **"Abort the build if it's stuck"** 체크박스 선택
- **Timeout minutes** 입력창에 `10` 입력

### **2. 빌드 보관 정책**
- **General** 섹션에서 **"Discard old builds"** 체크박스 선택
- **Days to keep builds** 입력창에 `7` 입력
- **Max # of builds to keep** 입력창에 `10` 입력

### **3. 파라미터화된 빌드**
- **General** 섹션에서 **"This project is parameterized"** 체크박스 선택
- **Add Parameter** 버튼 클릭 → **Choice Parameter** 선택
  - **Name**: `DEPLOY_TARGET` 입력
  - **Choices**: `MACHINE1`, `MACHINE2`, `ALL` 각 줄에 입력
  - **Description**: `배포할 환경을 선택하세요` 입력
- **Add Parameter** 버튼 클릭 → **String Parameter** 선택
  - **Name**: `VERSION` 입력
  - **Default Value**: `1.0.0` 입력
  - **Description**: `배포할 버전을 입력하세요` 입력

### 6.4 간단한 테스트 Pipeline 예시
```groovy
pipeline {
    agent any
    
    stages {
        stage('Hello') {
            steps {
                echo 'Hello World!'
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building application...'
                sh 'pwd'
                sh 'ls -al'
                // 빌드 결과물 생성
                sh 'echo "Build artifact" > artifact.txt'
                sh 'ls -al'
            }
        }
    }
    post {
        success {
            echo 'Build succeeded!'
            archiveArtifacts artifacts: '**/*.txt', fingerprint: true
            cleanWs()
        }
        failure {
            echo 'Build failed!'
            cleanWs()
        }
    }
}
```

## 🔍 7단계: 설정 확인 및 테스트

### 7.1 Job 실행 테스트
1. 생성한 Job 선택
2. `Build Now` 클릭
3. 빌드 히스토리에서 실행 결과 확인

### 7.2 로그 확인
- 빌드 번호 클릭
- `Console Output` 클릭하여 상세 로그 확인

### 7.3 Blue Ocean 확인
- `Open Blue Ocean` 클릭
- 현대적인 파이프라인 UI 확인

## 🛠️ 8단계: 추가 설정 (권장사항)

### 8.1 Jenkins URL 설정 확인
**경로**: `Manage Jenkins` → `Configure System`
- **Jenkins URL**: `http://localhost:8080/` 확인

### 8.2 Git 설정 (Git 사용 시)
**경로**: `Manage Jenkins` → `Configure System`
- **Git installations**: Git 경로 설정
- **GitHub**: GitHub 연동 설정 (필요시)

### 8.3 Credentials 설정
**경로**: `Manage Jenkins` → `Manage Credentials`
- **System**: 시스템 레벨 인증 정보
- **Global**: 전역 인증 정보
- **Stores scoped to**: 특정 범위의 인증 정보

## 📚 추가 리소스

- **Jenkins 공식 문서**: https://www.jenkins.io/doc/
- **Blue Ocean 가이드**: https://jenkins.io/projects/blueocean/
- **Pipeline 문법**: https://jenkins.io/doc/book/pipeline/syntax/
