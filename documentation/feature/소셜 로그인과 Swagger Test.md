# 📌 소셜 로그인과 Swagger에서 로그인

> 최종 수정 일자 : 8월 28일
>
> 최종 수정자 : 나용준

OIDC 기반 소셜 로그인을 사용하, 별도의 회원 가입 API는 없습니다

### ⭐ 회원 가입 로직
- http://{도메인}/oauth2/authorization/kakao
- http://{도메인}/oauth2/authorization/apple

인증 요청을 하면 자동으로 회원가입까지 진행되며 Jwt Access token과 refresh Token이 발급됩니다.
이미 존재하는 계정이라면 중복으로 진행되지는 않고 새로운 Token들을 발급합니다!

이때, ```NickName```은 임의로 생성을 해서 온보딩 없이 간편하게 회원가입이 가능합니다.
이 부분은 ```UniqueUtil```에서 처리합니다.

### ⭐로컬 스웨거에서 로그인 하는 방법

1. 로컬에서 앱을 실행해 주세요

2. 본인이 가입하고 싶은 방법에 따라 다음과 같은 주소를 입력합니다.

- http://localhost:8080/oauth2/authorization/kakao
- http://localhost:8080/oauth2/authorization/apple

3. 위의 주소에서 로그인을 처리하면 토큰이 날라옵니다 (예시 사진)

   <img width="159" height="117" alt="스크린샷 2025-08-16 오후 4 09 03" src="https://github.com/user-attachments/assets/31b48d71-be6e-442c-9ce8-2ca61a74a124" />

4. 위의 토큰을 보관하고 예전 처럼 Swagger에 넣어서 테스트 하기 !

- 단, Member 필드에 저장되진 않습니다.
- 따라서, 개인 보관 하셔야 합니다.
