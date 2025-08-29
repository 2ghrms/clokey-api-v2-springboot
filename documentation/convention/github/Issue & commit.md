# 📌 Commit & Issue

> 최종 수정 일자 : 8월 28일
>
> 최종 수정자 : 나용준

# 📌 Commit 

| Type | **의미** |
| --- | --- |
| ✨feat | 새로운 기능 추가 |
| 🔨fix | 버그, 오류 수정 |
| ✅chore | 동작에 영향 없는 코드 or 변경 없는 변경사항(주석 추가 등) ex) .gitIgnore |
| 📝docs | README나 WIKI 등의 문서 수정 |
| ♻️refactor | 코드 리팩토링 ex) 형식변경 |
| 💡test | 테스트 코드, 리팩토링 테스트 코드 추가 |
| 🔥hotfix | 급하게 치명적인 버그를 고쳐야 하는 경우 |

- 자주 Commit을 하는 것은 좋은 습관이나 너무 많이 하면 PR에 길게 달려서 일정 단위로 커밋 부탁드립니다.
- ex) 저는 개인적으로 다음과 같은 방법을 씁니다.
- feat: 기능 구현 완료 
- test: controller test 작성
- test: service test 작성

## ⭐ git 명령어를 통해서 commit 해야만 Spotless가 적용됩니다!

### ✅ Tips

- 최초 테스트 작성은 ```test```, 그 이후에 수정하거나 고치는건 ```refactor```과 ```fix```등을 사용합니다.
- API 기능 구현이 아닌 ```CI/CD```, ```git-flow```는 Issue 자체는 Chore로 파고 내부 구현은 ```feature``` 로 커밋합니다.

# 📌 Issue

- Issue Template은 ```.github``` 아래에 작성이 되어있고 상황에 맞게 사용하시면 됩니다.
- Issue의 종류 또한 Commit의 상황과 비슷하게 정해서 사용하시면 됩니다.

