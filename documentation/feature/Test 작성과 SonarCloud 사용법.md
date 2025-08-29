# 📌 Test 작성과 SonarCloud 사용법

> 최종 수정 일자 : 8월 28일
>
> 최종 수정자 : 나용준 

# 📌 Sonar Cloud

### 1.개요

- sonarcloud가 PR이 요청된 경우 코드 품질 검사를 진행합니다.
- https://sonarcloud.io/project/overview?id=Clokey-dev_clokey-server
- SonarCloud 주소는 다음과 같고 admin 권한으로 초대드립니다. (추가 멤버가 들어오면 진행 해야함)
- 무료 요금제의 경우에는 Test Coverage가 80으로 고정됩니다.
- 이로 인해서 테스트가 필요하지 않는 상황에도 PR merge를 SonarCloud에서 막을 수 있습니다. 
- (예를 들어 S3 통신 로직 또는 Auth 로직 처럼 직접 테스트를 하는 것이 좋은 상황)


<img width="600" alt="스크린샷 2025-07-30 오후 7 56 28" src="https://github.com/user-attachments/assets/d745c8a0-42c4-4d08-89e6-88a1de829d09" />

- 이런 경우 Administration > GeneralSetting
  
<img width="600"  alt="스크린샷 2025-07-30 오후 7 56 42" src="https://github.com/user-attachments/assets/a45dc7d7-4709-46a7-a77c-716a1421637a" />
  
- Analysis Scope에서 사진과 같이 수동 추가 부탁드립니다.
- 이런 방법으로 테스트 불편 사항을 조정하고 유연하게 사용할 수 있습니다.

### 2.안내 사항

- 이외에 기본적으로 적용되는 ```JaCoCo```보고서와 ```SonarCloud```세팅은 루트 경로의 ```build.gradle```을 잠고해 주세요.

# 📌 Test Code

## 1. Serivce Test

### ✅ 개요 

- 서비스 레이어 테스트는 기본적으로 통합 테스트를 사용합니다.
- SonarCloud의 CI 검사 시간을 줄이기 위해서 하나의 테스트를 사용하며, 상속 받아서 사용합니다.

<img width="323" height="162" alt="스크린샷 2025-08-28 오후 7 09 15" src="https://github.com/user-attachments/assets/4a41aad0-bbba-4720-b91a-f64384e50c79" />

- Test는 application-test.yml을 적용합니다. 
- 테스트 관련 세팅은 이 곳에서 해주시면 됩니다.

### ✅ 현재 사용자 관련 Mocking

```java
@MockitoBean private MemberUtil memberUtil;
```

- 다음과 같이 MemberUtil을 모킹해서 서비스 로직 내부에서 던져지는 Member를 설정합니다.

```java
Member member =
        Member.createMember(
                "testEmail1",
                "testClokeyId1",
                "testNickName1",
                OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));
memberRepository.save(member);
given(memberUtil.getCurrentMember()).willReturn(member1);
```

- 그리고 ```@BeforeEach```에서 다음과 같이 세팅 해주면 됩니다.

### ✅ 테스트 의존성을 줄이기 위한 Convention

- 테스트를 너무 독립적으로 작성하면 작성 소요 시간이 너무 길어지고,
- 의존성이 너무 크면 유지 보수에서 테스트를 활용하는 활용도가 떨어집니다.
- 따라서 API 하나 마다 ```@Nested```로 묶어주고 독립적인 ```@BeforeEach```를 사용합니다.

```java
@Nested
class 댓글을_작성할_때 {
}
```

제목은 ```상황+~때```로 끝내주세요!

### ✅ 기타 Convention 및 주의 사항

- 외부 서버와 통신이 필요하거나 테스트가 어려운 ```"부분"```은 필요에 따라 ```@MockitoBean```또는 ```@MockitoSpyBean```을 활용합니다.
- 테스트는 일반적으로 ```성공 테스트``` 하나와 ```존재하는 예외 테스트```를 전부 해주시면 됩니다.

다음과 같은 명명 부탁드립니다.
- 성공 : ```유효한 요청이면 ~ 한다```
- 실패 : ```~이면 예외가 발생한다```

다음으로는 주의 사항입니다

- given-when-then 같은 기본적인 테스트 컨벤션은 개별적으로 잘 지켜주시면 됩니다.
- 테스트에 필요한 사전 준비 작업은 최소화 해주세요!

### ⚠️ 왜 Transaction Test는 사용하지 않나요?

- Transaction 롤백을 사용하면 유용하긴 하지만 Service에 ```@Transaction```을 누락한 경우에 테스트가 정상 실행이 될 수 있다는 단점이 있습니다.
- 따라서, 별도의 ```DB Cleaner```를 도입해서 사용합니다. 이는 ```Integration Test```를 상속 받을 경우 자동으로 처리됩니다.

### ⚠️ Transaction을 사용하지 않는 테스트와 OSIV를 끄기 때문에 생기는 주의 사항

- OSIV란 트랜잭션 밖에서도 ```Lazy Loading```을 활용할 수 있는 기능입니다. 
- 하지만, ```Connection Pool```의 효율성을 위해서 이를 off로 사용하고 있습니다.
- 위의 상황과 Test에서 Transaction을 키지 않는 관계로 Lazy Loading이 안되는 문제가 있습니다.

예를 들어서, 앨범이라는 엔티티에 Participant라는 필드가 존재한다면..
```java
Album album =
transactionUtil.getResult(
    () -> {
        Album loadedAlbum = albumRepository.findById(1L).get();
        loadedAlbum.getParticipants().get(0);
        return loadedAlbum;
    });
```
- 위와 같은 방식으로 미리 로딩을 해서 가져오면 ```album.getParticipant()``` 같은 기능이 작동합니다.
- Fetch Join과 같이 미리 가져온다고 이해하시면 좋을 것 같습니다.


### ✅ Redis 관련 테스트를 하는 경우

- 로컬 테스트를 할 때 로컬의 Redis를 사용하게 되는데, 롤백이 따로 되지 않아서 이를 청소하기 위한 ```RedisCleaner```를 만들었습니다.
- Redis가 사용되는 경우가 제한적이어서 필요한 테스트에서 ```@AfterEach```로 호출해 주시면 됩니다.

ex)
```java
@AfterEach
void cleanUp() {
    redisCleaner.flushAll();
}
```

## 2. Controller Test

### ✅ 개요

- Controller 레이어 테스트는 단위 테스트 MockMvcTest를 사용합니다.
- BDD Mockito에 맞게 테스트 코드를 설계해 주시면 됩니다.
- ⭐ 서비스 로직에 대한 응답 에러까지는 작성하지 않고 ```입력값 검증```까지만 테스트합니다.

### ✅ example

```java
@Test
void 옷의_카테고리_ID를_비워두면_예외가_발생한다() throws Exception {
    // given
    ClothCreateRequests request =
            new ClothCreateRequests(
                    List.of(new ClothCreateRequest("testClothImageUrl", null)));

    // when & then
    ResultActions perform =
            mockMvc.perform(
                    post("/clothes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));

    perform.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.isSuccess").value(false))
            .andExpect(jsonPath("$.code").value("COMMON400"))
            .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
            .andExpect(jsonPath("$.result.categoryId").value("옷의 카테고리 ID는 비워둘 수 없습니다."));
}
```
### ✅ Nested Naming Convention

```java
@Nested
class 댓글을_작성을_요청할_때 {
}
```

- ```~상황을 요청할 때``` 로 통일해 주세요.
- 이외에 메서드 이름은 Service와 동일합니다.
