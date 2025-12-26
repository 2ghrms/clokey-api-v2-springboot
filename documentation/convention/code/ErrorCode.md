# 📌 ErrorCode

> 최종 수정 일자 : 8월 28일
>
> 최종 수정자 : 나용준

### 1. ErrorCode 명명 규칙

- 도메인 이름에 맞게
- (도메인) + ErrorCode

### 1. ErrorCode Structure

```java
@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    AUTH_NOT_PARSABLE(500, "AUTO_5001", "인증 정보 파싱에 실패했습니다."),

    AUTH_NOT_EXIST(401, "AUTH_4011", "인증 정보가 존재하지 않습니다."),
    INVALID_REFRESH_TOKEN(401, "AUTH_4012", "유효하지 않은 리프레시 토큰입니다. 재로그인 해주세요."),

    REFRESH_TOKEN_NOT_FOUND(404, "AUTH_4041", "리프레시 토큰을 찾지 못했습니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
```
- ```ErrorHandler```와 연관된 구조이니 implement 구조를 수정하고 싶다면 ```ErrorHandler```를 수정해야합니다.
- 기본적으로 에러 코드는 기본 에러 코드를 제외하고 ```도메인_번호```를 사용합니다.
- 예를 들어, 404번대 에러라면 ```EXAMPLE_4041```이렇게 시작하면 됩니다.
- 또한 보기 좋게 넘버 별로 묶어서 보관해 주세요 (위의 예시 참고).
- 유저 친화적인 에러 메시지 작성 부탁드립니다.
