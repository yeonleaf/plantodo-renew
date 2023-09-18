# PLANTODO
일정 내에 할 일을 생성해서 관리할 수 있는 투두리스트 API입니다.

할 일을 생성할 때 반복 옵션을 다양하게 설정할 수 있습니다. (반복 없음, 매일 반복, X일마다 반복, X요일마다 반복)<br>
이 중 반복이 걸린 할 일은 할 일 그룹으로 관리되며 그룹 단위로 생성, 수정, 삭제를 할 수 있습니다.<br>
날짜, 기간 단위로 생성한 일정이나 그룹의 할 일을 필터링해서 조회할 수 있습니다.<br>

프론트엔드 코드 없이 HTTP API로만 구성되어 있으며 대신 요청을 보낼 수 있는 Swagger-UI 기반 [링크](https://plantodo.site/swagger-ui/index.html)를 제공합니다.

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
- 회원가입 / 로그인 POST API
- 일정 / 그룹 / 할일 CRUD HTTP API
- 날짜 / 기간 단위로 조회하는 Collection 조회 API

<br>

## Architecture
![ptd-architecture-230910](https://github.com/yeonleaf/plantodo-renew/assets/91470133/3a1a2050-43d4-4034-b643-1cfd207af373)


<br>

## Link
[링크](https://plantodo.site/swagger-ui/index.html)

[API 명세서](https://documenter.getpostman.com/view/16796529/2s9Y5SWRLp)
