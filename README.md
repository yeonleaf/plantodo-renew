# PLANTODO
할 일 관리 웹 페이지

## Environments

<img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=OpenJDK&logoColor=white"/><img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/intellijidea-000000?style=for-the-badge&logo=intellijidea&logoColor=white"><img src="https://img.shields.io/badge/swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white">
<img src="https://img.shields.io/badge/hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white"><img src="https://img.shields.io/badge/mariadb-003545?style=for-the-badge&logo=mariadb&logoColor=white">

- Language : java 17
- IDE : IntelliJ Ultimate 2021.2
- Framework : SpringBoot 3.1.1 
- Database : MariaDB 10.11 (java-client 2.7.2)
- ORM : JPA (hibernate)

## Features
- 회원가입, 로그인
- 일정 관리
- 할 일 관리

## Quick Start

현재 개발 단계이나 swagger ui를 통해 api를 테스트할 수 있습니다.

### Prerequisite
0. JDK 17 설치
1. [MariaDB 10.11 설치](https://mariadb.org/download/?t=mariadb&p=mariadb&r=11.1.0&os=windows&cpu=x86_64&pkg=msi&m=blendbyte) (설치 중 root 계정에 대해 비밀번호를 설정하고 꼭 기억할 것)

2. MYSQL client (MariaDB 10.11 (X64)) 실행

3. root 비밀번호를 입력해서 로그인했다면 DB 생성 `create database [db이름]`

### Processes
1. 프로젝트를 로컬에 clone
2. Settings/Build, Execution, Development/Gradle Projects/Gradle/Gradle - JVM을 Project SDK 17로 설정
3. Settings/Build, Execution, Development/Gradle Projects/Gradle/Gradle - Build and run using과 Run tests using을 모두 intelliJ로 설정
4. `/src/main` 에 `resource`라는 이름의 폴더 만들기
5. `/src/main/resource`에 `application.yaml` 파일 생성
6. 아래의 내용을 붙여넣기
```
spring:
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://localhost:3306/(1)
    username: root
    password: (2)
  jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MariaDBDialect
    open-in-view: false

server:
  servlet:
    context-path: /

springdoc:
  override-with-generic-response: false
```
7. application.yaml 내용 수정
- `(1)` : Prerequisite 3번에서 설정한 db이름
- `(2)` : Prerequisite 1번에서 설정한 root 비밀번호

8. intelliJ에서 application run
9. http://localhost:8080/swagger-ui.html
