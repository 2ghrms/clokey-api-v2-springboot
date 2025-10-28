# 📌 DTO

> 최종 수정 일자 : 8월 28일
>
> 최종 수정자 : 나용준

### 1. DTO 명명 규칙

사용자와 상호 작용하는 DTO

- 요청 : xxxRequest.java (api와 통일된 이름 사용)
- 응답 : xxxResponse.java (api와 통일된 이름 사용)

서버 내부 또는 외부 서버와 상호 작용

- xxx.Dto.java

### 2. DTO structure

- 기본적으로 Java의 Record를 사용합니다.

사용자와 상호 작용하는 DTO (request)

```java
public record ClothCreateRequest(
        @NotBlank(message = "옷의 이미지 주소는 비워둘 수 없습니다.")
                @Schema(description = "옷의 이미지 주소", example = "https://example.jpg")
                String clothImageUrl,
        @NotNull(message = "옷의 카테고리 ID는 비워둘 수 없습니다.")
                @Schema(description = "옷의 카테고리 ID", example = "1")
                Long categoryId) {}
```
- 다음과 같이 입력값 검증을 수행합니다. 비즈니스 로직과 관련해서 입력값 검증을 수행합니다.
- 기본적으로 ```@Schema```를 통해서 입력 인자에 대한 설명을 제공합니다. 가능하다면 example까지 작성하면 좋으나 필수는 아닙니다.
- String : ```@NotBlank```
- Long( 이외에도 null값 가능한 인자 ) : ```@NotNull```
- List : ```@NotEmpty```

⚠️ DTO에서는 입력값 검증과 관련된 검증만 수행되어야 하며, ```"DB에 존재하는가?"```와 같은 로직과 관련된 검증은 서비스 내부에서 수행합니다.


사용자와 상호 작용하는 DTO (response)
```java
public record ClothCreateResponse(
        @Schema(description = "생성된 옷 ID들", example = "[1,2,3,4]") List<Long> clothIds) {
    public static ClothCreateResponse from(List<Cloth> cloths) {
        return new ClothCreateResponse(cloths.stream().map(Cloth::getId).toList());
    }
}
```
- 기본적으로 ```@Schema```를 통해서 입력 인자에 대한 설명을 제공합니다. 가능하다면 example까지 작성하면 좋으나 필수는 아닙니다.
- 정적 펙토리 메서드를 통한 생성자를 사용합니다
- ```of``` : 구성 요소들로 만들어지는 경우
- ```from``` : 입력값을 바탕으로 추출해서 만들어지는 경우
- 이는 service에서 DTO응답을 만들 때 사용되며, test에서는 record가 기본으로 제공하는 생성자를 사용합니다.

서버 내부 통신용 DTO

```java
public record AccessTokenDto(Long memberId, MemberRole role, String tokenValue) {
    public static AccessTokenDto of(Long memberId, MemberRole role, String tokenValue) {
        return new AccessTokenDto(memberId, role, tokenValue);
    }
}
```

- 별도의 인자 설명 없이 정적 펙토리 메서드만 적용해서 사용해 주시면 됩니다.

⚠️ example 값 오류 : 배열이면 ```(1,2,3,4)```는 안됩니다 ```[1,2,3,4]``` 이렇게 작성되어야 합니다.
json 변환 오류가 발생할 수 있습니다.

### 3. Layered Dto

```java
public record TermAgreeRequest(
        @NotEmpty(message = "약관 동의 정보는 비워둘 수 없습니다.") @Valid @Schema(description = "약관 동의 정보 리스트")
                List<Payload> payloads) {
    @Schema(name = "TermAgreeRequestPayload")
    public record Payload(
            @NotNull(message = "약관 ID는 비워둘 수 없습니다.") @Schema(description = "약관 ID") Long termId,
            @NotNull(message = "약관 동의 여부는 비워둘 수 없습니다.")
                    @Schema(description = "약관 동의 여부", example = "true")
                    Boolean agreed) {}
}

```
- 다음과 같이 내부 내용들의 이름은 ```Payload```로 사용하겠습니다.
- ```@Valid```를 한 번 더 붙여야 내부 검증 로직이 작동합니다.
- @Schema로 Distinct한 이름을 붙여줘야 스웨거에서 잘 보입니다!
