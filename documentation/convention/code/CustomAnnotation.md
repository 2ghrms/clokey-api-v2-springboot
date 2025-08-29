# 📌 Custom Annoation

> 최종 수정 일자 : 8월 28일
>
> 최종 수정자 : 나용준

현재 Custom Annotation은 비즈니스 ```로직과 관련이 없는``` 최소한의 Annotation만을 사용합니다.

- ```@PageSize```
- ```@Enum```

⚠️ ```@Enum```과 관련된 주의 사항

- 현재 ```@Enum```은 DTO 내부에서만 사용 가능합니다. 사용할 경우 DTO에 ```@JsonCreater```와 함께 사용해야합니다.
- Controller에서 사용하려면 Controller에 너무나 많은 설정이 필요에서는 현재 사용하지 못합니다.
- 추후 과제로, ```@Enum```을 Controller에서 사용할 수 있는 방법을 남겨두겠습니다.
