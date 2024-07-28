# 로그인 기능 동작 설명

1. 클라이언트 요청
클라이언트(예: React 애플리케이션)가 사용자 이름과 비밀번호를 포함한 POST 요청을 `/api/login` 엔드 포인트로 전송한다.
2. 서버 수신
Spring Boot 애플리케이션이 요청을 수신한다. 요청은 디스패치 서블릿(DispatchServlet)에 의해 처리된다.
3. Spring Security 필터 체인 적용
Spring Security의 여러 필터가 요청을 가로채고, 보안 규칙을 적용한다.
4. 보안 설정 확인
'SecurifyConfig' 클래스에서 정의된 보안 규칙에 따라 요청이 처리된다.
5. CSRF 토큰 확인
클라이언트 요청에는 CSRF 토큰이 포함되어야 하며, Spring Security는 요청의 유효성을 검사한다.
6. 컨르롤러 메서드 호출
보안 설정을 통과한 요청은 'LoginController'의 'login'메서드로 전달된다. 이 메서드는 사용자 인증을 처리한다.
7. 데이터베이스 조회
'UserRepository'는 데이터베이스와 상호작용하여 사용자 정보를 조회한다.
8. 응답 생성 및 반환
9. 