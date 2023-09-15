# PLANTODO
할 일 관리 웹 페이지

<br>

## Environments

<img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=OpenJDK&logoColor=white"/><img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">

<img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"><img src="https://img.shields.io/badge/hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white"><img src="https://img.shields.io/badge/flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white">

<img src="https://img.shields.io/badge/ec2-FF9900?style=for-the-badge&logo=ec2&logoColor=white"><img src="https://img.shields.io/badge/githubactions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"><img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">

<img src="https://img.shields.io/badge/swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white"><img src="https://img.shields.io/badge/intellijidea-000000?style=for-the-badge&logo=intellijidea&logoColor=white">


- Language : java 17
- Framework : SpringBoot 3.1.1 
- Database :
  - prod, dev : MySQL 8.0 (mysql-connector-j 8.1.0)
  - test : H2 (embedded)
- ORM : JPA (hibernate)
- DB migration : flyway 9.22.0
- Deployment : EC2, Github Actions, docker-compose 3.8
- Others :
  - IDE : IntelliJ Ultimate 2023.1.3
  - Docs : swagger-ui (springdoc 2.1.0)

<br>

## Features
- 회원가입 / 로그인 POST API, 일정 / 그룹 / 할일 CRUD HTTP API, 날짜 / 기간 단위로 조회하는 Collection 조회 API
  - 애플리케이션의 상태를 전이하기 위해 Spring HATEOAS를 적용
  - 정상적인 요청은 EntityModel로 감싼 응답 객체를, 비정상적인 요청은 커스텀 에러 객체를 ResponseEntity에 담아 리턴
  - 요청을 보낼 수 있는 Swagger-UI 기반 API 명세서를 제공

- JWT 토큰 방식의 인증을 사용
  - 회원가입, 로그인 외 모든 요청에 Bearer {토큰} 형태의 Authorization 헤더가 있는지 확인하고 토큰이 유효한지 검증
  - 요청이 들어오면 인터셉터에서 검증하고 올바른 요청이라면 이어서 처리
  - 올바른 요청이 아니라면 인터셉터에서 Error response 객체를 반환

<br>

## Architecture
![ptd-architecture-230910](https://github.com/yeonleaf/plantodo-renew/assets/91470133/3a1a2050-43d4-4034-b643-1cfd207af373)


<br>

## Link
[링크](https://plantodo.site/swagger-ui/index.html)

[API 명세서](https://documenter.getpostman.com/view/16796529/2s9Y5SWRLp)
