def allServers = [
        [name: "vm-az01-prd-navupd-was-01", port: 111, host: "10.111.11.111", appPath: "TEST11", user: "tomcat", password: ""],
        [name: "vm-az01-prd-navupd-was-02", port: 222, host: "10.222.22.222", appPath: "TEST21", user: "tomcat", password: ""]
]

// TARGET_VM 파라미터에 따라 선택된 서버만 필터링
def servers = allServers.findAll { it.name.contains(params.TARGET_VM) }


pipeline {
    agent any
    
    stages {
        stage('Start') {
            steps {
                echo '=== 파이프라인 시작 ==='
                echo '현재 시간: ' + new Date().format('yyyy-MM-dd HH:mm:ss')
            }
        }

        stage('Print Deployment Target') {
            steps {
                script {
                    echo "=== 배포 대상 서버 선택 ==="
                    echo "선택된 TARGET_VM: ${params.TARGET_VM}"
                    servers.each { server ->
                        echo "배포 대상 서버: ${server.name} (${server.host}:${server.port}) - ${server.appPath}"
                    }

                    if (servers.isEmpty()) {
                        error "TARGET_VM 파라미터와 일치하는 이름의 서버가 없습니다."
                    }
                }
            }
        }

        stage('Continue or Stop') {
            steps {
                script {
                    def continueChoice = input(
                        message: '다음 단계로 진행하시겠습니까?',
                        parameters: [
                            choice(
                                name: 'CONTINUE_OR_STOP',
                                choices: ['CONTINUE', 'STOP'],
                                description: '선택하세요'
                            )
                        ]
                    )
                    
                    echo "사용자 선택: ${continueChoice}"
                    
                    // 환경변수로 저장하여 다른 스테이지에서 사용 가능하도록 함
                    env.CONTINUE_CHOICE = continueChoice
                    
                    if (continueChoice == 'STOP') {
                        error "사용자가 파이프라인을 중단했습니다."
                    }
                    
                    echo "계속 진행합니다..."
                }
            }
        }
        
        stage('Select Machine') {
            steps {
                script {
                    echo '=== 사용자 입력: 배포할 머신 선택 ==='
                    
                    // 머신 선택 input 스텝
                    def machineName = input(
                        message: '배포할 머신을 선택하세요',
                        parameters: [
                            choice(
                                name: 'MACHINE_NAME',
                                choices: ['machine-01', 'machine-02'],
                                description: '머신을 선택하세요'
                            )
                        ]
                    )
                    
                    echo "선택된 머신: ${machineName}"
                    
                    // 환경변수로 저장하여 다른 스테이지에서 사용 가능하도록 함
                    env.MACHINE_NAME = machineName
                    
                    // 선택된 머신 정보로 txt 파일 생성
                    sh "echo 'Machine: ${machineName}' > selected_machine.txt"
                    sh "echo 'Time: ${new Date().format('yyyy-MM-dd HH:mm:ss')}' >> selected_machine.txt"
                    
                    echo "머신 정보가 selected_machine.txt 파일에 저장되었습니다."
                }
            }
        }
        
        stage('Variable Scope Test') {
            steps {
                script {
                    echo '=== 변수 스코프 테스트 ==='
                    echo '이전 스테이지에서 입력받은 값들을 확인합니다.'
                    
                    // 이전 스테이지의 변수들이 접근 가능한지 테스트
                    try {
                        echo "continueChoice 변수 접근 시도..."
                        echo "continueChoice: ${continueChoice}"
                    } catch (Exception e) {
                        echo "continueChoice 변수에 접근할 수 없습니다: ${e.message}"
                    }
                    
                    try {
                        echo "machineName 변수 접근 시도..."
                        echo "machineName: ${machineName}"
                    } catch (Exception e) {
                        echo "machineName 변수에 접근할 수 없습니다: ${e.message}"
                    }
                    
                    // 환경변수로 저장된 값 확인
                    echo "=== 환경변수로 저장된 값들 ==="
                    echo "env.CONTINUE_CHOICE: ${env.CONTINUE_CHOICE}"
                    echo "env.MACHINE_NAME: ${env.MACHINE_NAME}"
                    
                    // 환경변수를 사용한 추가 작업
                    if (env.CONTINUE_CHOICE == 'CONTINUE') {
                        echo "✅ 사용자가 계속 진행을 선택했습니다."
                    } else {
                        echo "❌ 사용자가 파이프라인을 중단했습니다."
                    }
                    
                    if (env.MACHINE_NAME) {
                        echo "✅ 선택된 머신: ${env.MACHINE_NAME}"
                        // 환경변수를 사용한 파일 생성
                        sh "echo 'Final Machine: ${env.MACHINE_NAME}' > final_machine.txt"
                        sh "echo 'Continue Choice: ${env.CONTINUE_CHOICE}' >> final_machine.txt"
                    }
                }
            }
        }
        
        stage('Final') {
            steps {
                script {
                    echo '=== 파이프라인 완료 ==='
                    echo '모든 단계가 성공적으로 완료되었습니다.'
                    
                    // 생성된 파일 확인
                    sh 'ls -la *.txt'
                    sh 'cat selected_machine.txt'
                }
            }
        }
    }
    
    post {
        success {
            echo '=== 성공 ==='
            archiveArtifacts artifacts: '*.txt', fingerprint: true
            cleanWs()
        }
        failure {
            echo '=== 실패 ==='
            echo '파이프라인이 실패했습니다.'
            cleanWs()
        }
    }
}
