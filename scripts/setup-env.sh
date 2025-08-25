#!/bin/bash

echo "========================================="
echo "LoadMapGuide Backend - 환경 변수 설정 스크립트"
echo "========================================="
echo

echo "1. 카카오 API 키 설정"
echo
read -p "카카오 REST API 키를 입력하세요: " KAKAO_API_KEY

if [ -z "$KAKAO_API_KEY" ]; then
    echo "에러: API 키가 입력되지 않았습니다."
    exit 1
fi

echo
echo "2. 환경 변수 설정 중..."

# 사용자의 shell 프로필 파일 감지
if [ -f ~/.zshrc ]; then
    PROFILE_FILE="$HOME/.zshrc"
elif [ -f ~/.bashrc ]; then
    PROFILE_FILE="$HOME/.bashrc"
elif [ -f ~/.bash_profile ]; then
    PROFILE_FILE="$HOME/.bash_profile"
else
    PROFILE_FILE="$HOME/.bashrc"
    touch "$PROFILE_FILE"
fi

# 기존 설정 제거 (있다면)
sed -i '/export KAKAO_REST_API_KEY=/d' "$PROFILE_FILE"

# 새 설정 추가
echo "export KAKAO_REST_API_KEY=\"$KAKAO_API_KEY\"" >> "$PROFILE_FILE"

echo
echo "✅ 환경 변수 설정 완료!"
echo
echo "💡 다음 단계:"
echo "   1. 현재 터미널에서 환경변수 로드: source $PROFILE_FILE"
echo "   2. 또는 새 터미널을 열어주세요"
echo "   3. ./gradlew bootRun 으로 애플리케이션을 실행하세요"
echo "   4. http://localhost:8080/api/health 에서 상태를 확인하세요"
echo
echo "📋 설정된 환경 변수:"
echo "   KAKAO_REST_API_KEY=$KAKAO_API_KEY"
echo "   프로필 파일: $PROFILE_FILE"
echo