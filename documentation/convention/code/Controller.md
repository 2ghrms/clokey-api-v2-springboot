# 📌 Controller

> 최종 수정 일자 : 8월 28일
>
> 최종 수정자 : 나용준

### 1. Controller 명명 규칙

- 도메인 이름에 맞게 
- (도메인) + Controller

### 2. Controller Structure

상단부
```java
@RestController
@RequestMapping("/clothes")
@RequiredArgsConstructor
@Tag(name = "4. 옷 API", description = "옷 관련 API입니다.")
@Validated
public class ClothController {
```
- ```@RequestMapping```은 도메인의 API들이 prefix가 통일될 시에만 사용합니다.
- 도메인 내부의 API들의 주소가 달라져야할 경우 생략해 주세요.
- ```@Tag```는 기본적으로 명세서의 순서에 따라서 배치됩니다.
- ```@Validated```는 ```@Enum``` 또는 ```@PageSize``` 처럼 Custom Annotation이 적용되는 경우 사용해 주세요.

API 코드
```java
@PostMapping
@Operation(summary = "옷 생성", description = "새로운 옷을 생성합니다.")
public BaseResponse<ClothCreateResponse> createCloths(
        @Valid @RequestBody ClothCreateRequests request) {
    ClothCreateResponse response = clothService.createCloths(request);
    return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
}
```
- 위와 같은 구조로 코딩을 해주시면 됩니다.
- Controller와 Service 메서드의 이름은 모두 ```행위+도메인(대상)```으로 통일됩니다.
- 응답의 종류는 ```200,201,204```가 존재하며 204를 던질 경우 ```BaseResponse<Void>```룰 반환하세요.
- 기본적으로 PathVariable과 Query Parameter는 입력값 검증은 수행하지 않습니다. 필요하다면 Custom Annotation을 만들고 문서화 부탁드립니다.
- DTO 내부에 검증 메서드가 있는 경우에 ```@Valid```를 붙여서 사용해주세요.
