pipeline {
    agent any
    
    stages {
        stage('Start') {
            steps {
                echo '=== 파이프라인 시작 ==='
                echo '현재 시간: ' + new Date().format('yyyy-MM-dd HH:mm:ss')
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
                    
                    // 선택된 머신 정보로 txt 파일 생성
                    sh "echo 'Machine: ${machineName}' > selected_machine.txt"
                    sh "echo 'Time: ${new Date().format('yyyy-MM-dd HH:mm:ss')}' >> selected_machine.txt"
                    
                    echo "머신 정보가 selected_machine.txt 파일에 저장되었습니다."
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
