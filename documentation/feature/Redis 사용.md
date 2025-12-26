# 📌 공통 적용 사항

> 최종 수정 일자 : 9월 17일
>
> 최종 수정자 : 나용준 

### 🔑 Redis key 

- 현재 ```Lettuce Template```이 적용되어 있습니다.
- 간단한 경우 ```Redis Hash```를 사용해도 좋습니다. 
- 다만, key가 중복되지 않게 관리해야 하기 떄문에 레디스 캐싱 사항은 문서화 부탁드립니다.

  | Type           | 형식                               | 설명                                                                 |
  |----------------|------------------------------------|----------------------------------------------------------------------|
  | Refresh Token  | refreshToken:{memberId}            | domain 모듈의 auth 디렉토리의 RefreshToken.java에서 확인              |
  | DailyCoordinate| dailyCoordinate:{memberId}:{date}  | 오늘의 코디 ID를 저장, TTL은 자정까지로 설정되어 자동 만료됨          |
