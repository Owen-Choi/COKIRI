
# Co-Kiri
끼리끼리 교환하며 새로운 가치를 만들어가는 물물교환 플랫폼
### Summary

저희 프로젝트 Co-kiri는 위치기반으로 근처에 있는 사용자들끼리 사용하지 않는 물건들을 다양한 관심사에 따라 카테고리끼리 교환할 수 있는 물물교환 플랫폼입니다.

- 집에 쌓여있는 책들을 다른 새로운 책들로 교환할 수 있다면 얼마나 좋을까요?
- 큰 용량의 식품이 항상 부담이었던 자취생들끼리 서로 식품을 교환할 수 있다면 얼마나 좋을까요?
- 보유하고 있는 기프트콘, 티켓, 교환권 등을 자신의 필요에 따라 더욱 유연하게 교환하고 사용할 수 있다면 얼마나 좋을까요?

그 외 의류, 유아동용품, 운동용품 뿐만 아니라 서비스/기술까지 다양한 카테고리에 따라 코끼리(Co-Kiri)와 함께 끼리끼리 교환하며 새로운 가치를 만들어가요.


### 배포 주소

> https://www.file-blocker.shop

### 화면 구성

> 홈 화면
> 
![home](https://github.com/F3F-T/COKIRI/assets/97940568/fadcbe71-9320-40d5-8826-d6aab086484a)

> 물물교환,끼리끼리
> 
![물물교환,끼리끼리](https://github.com/F3F-T/COKIRI/assets/97940568/03209643-4910-42c8-b39a-f3d27eece529)

> 게시글 업로드,수정,삭제
>
![게시글 업로드,수정,삭제](https://github.com/F3F-T/COKIRI/assets/97940568/92ef16ed-e309-4844-83b5-5712800a28ff)

> 마이페이지, 주소 설정, 관심 상품 등록
>
![마이페이지, 주소 설정, 관심 상품 등록](https://github.com/F3F-T/COKIRI/assets/97940568/6410b093-7e4b-42c4-9b43-bb986bae9375)

## Tech Stack


Frontend :  React, Typescript, Redux-toolkit, css module, bootstrap, aws amplify

Backend : JAVA, Spring Boot, JPA, QueryDSL, RestDocs, MySQL, Redis, AWS EC2, AWS S3, AWS RDS, Docker




***

## Conventions

### commit convention

`type`: **subject**

| 제목 및 태그 이름 | 설명 |
| --- | --- |
| Feat | 새로운 기능을 추가할 경우 |
| Fix | 버그를 고친 경우 |
| Design | CSS 등 사용자 UI 디자인 변경 |
| !BREAKING CHANGE | 커다란 API 변경의 경우 |
| !HOTFIX | 급하게 치명적인 버그를 고쳐야하는 경우 |
| Style | 코드 포맷 변경, 세미 콜론 누락, 코드 수정이 없는 경우 |
| Refactor | 프로덕션 코드 리팩토링 |
| Comment | 필요한 주석 추가 및 변경 |
| Docs | 문서를 수정한 경우 |
| Test | 테스트 추가, 테스트 리팩토링 (프로덕션 코드 변경 X) |
| Chore | 빌드 테스트 업데이트, 패키지 매니저를 설정하는 경우 (프로덕션 코드 변경 X) |
| Rename | 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우 |
| Remove | 파일을 삭제하는 작업만 수행한 경우 |

`body` (optional)

`footer` (optional)

***

#### Commit Convention 에시
```
Feat: 추가 로그인 함수

로그인 API 개발

Resolves: #123
Ref: #456
Related to: #48, #45
```
***

# PR convention

## PR 타입

ex)
- [ ] 기능 추가
- [ ] 기능 삭제
- [ ] 기능 수정
- [x] 의존성, 환경 변수, 빌드 관련 코드 업데이트

## 작업사항
- ex) 로그인 시, 구글 소셜 로그인 기능을 추가했습니다. + commit 태그
- ex) 회원가입 로직 수정했습니다.

