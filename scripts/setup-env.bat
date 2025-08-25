@echo off
echo =========================================
echo LoadMapGuide Backend - 환경 변수 설정 스크립트
echo =========================================
echo.

echo 1. 카카오 API 키 설정
echo.
set /p KAKAO_API_KEY="카카오 REST API 키를 입력하세요: "

if "%KAKAO_API_KEY%"=="" (
    echo 에러: API 키가 입력되지 않았습니다.
    goto END
)

echo.
echo 2. 환경 변수 설정 중...
setx KAKAO_REST_API_KEY "%KAKAO_API_KEY%"

echo.
echo ✅ 환경 변수 설정 완료!
echo.
echo 💡 다음 단계:
echo    1. 새 터미널을 열어주세요 (환경변수 적용을 위해)
echo    2. gradle bootRun 또는 IDE에서 애플리케이션을 실행하세요
echo    3. http://localhost:8080/api/health 에서 상태를 확인하세요
echo.
echo 📋 설정된 환경 변수:
echo    KAKAO_REST_API_KEY=%KAKAO_API_KEY%
echo.

:END
pause