---
version: alpha
name: "Spring MVC Lab"
description: "요청과 응답을 나란히 비교하는 한국어 HTTP 실습 도구"
colors:
  ink: "#172d46"
  muted: "#52677e"
  paper: "#f4f7fb"
  surface: "#ffffff"
  line: "#ced8e4"
  blue: "#2159c7"
  blue-hover: "#16449e"
  blue-wash: "#eaf0ff"
  green: "#146b54"
  red: "#ab303b"
  focus: "#7341c4"
  scroll-thumb: "#8496ad"
  scroll-track: "#eaf0f6"
  scroll-hover: "#52677e"
  scroll-active: "#172d46"
typography:
  body:
    fontFamily: '"Noto Sans KR", "Malgun Gothic", "Apple SD Gothic Neo", system-ui, sans-serif'
  code:
    fontFamily: '"Cascadia Code", "SFMono-Regular", Consolas, monospace'
rounded:
  DEFAULT: "10px"
spacing:
  section-gap: "30px"
  page-max: "1600px"
components:
  button:
    height: "46px"
  field:
    height: "44px"
  inspector:
    height: "280px"
---

# Spring MVC Lab Design System

## Overview

브라우저 개발자 도구의 네트워크 검사기를 참고한 한국어 학습 도구다. 대상은 요청 바인딩을 학습하는 개발자와 구현 범위를 확인하는 채용 담당자다. 현재 사용자 요청이 제품 요구사항의 근거이며, 실제 서비스의 업무·인증 정책은 없다.

핵심은 왼쪽의 요청 설정과 오른쪽의 실제 응답을 같은 화면에서 비교하는 것이다. 서버 응답을 꾸며내거나 정적 예시를 실제 응답처럼 표시하지 않는다. 마케팅 배너, 장식 이미지, 애니메이션은 사용하지 않는다.

한국어 화면과 영문 프로토콜 용어를 병기한다. 데스크톱 비교 작업을 우선하되 760px 이하에서는 요청과 응답을 세로로 배치한다. Swagger UI는 별도 도구이며 해당 제품의 기본 스타일과 조작 방식을 유지한다.

CSS의 :root 변수가 실행 시 토큰의 원본이다. 이 문서는 같은 이름의 colors 키를 --키 CSS 변수에 대응한다. typography.body/code는 --font-body/code, rounded.DEFAULT는 --radius에 대응한다. 색상 변경 시 CSS와 이 문서를 함께 변경하고 tools/ui/check-ui.mjs로 차이를 검사한다.

## Colors

파란색은 요청과 주요 동작, 초록색은 성공, 빨간색은 실패를 나타낸다. 상태는 반드시 숫자·문구와 함께 표시한다. 흰 요청 패널과 옅은 청색 응답 패널로 경계를 구분한다. 포커스는 focus 색의 3px 외곽선이다. 라이트 테마만 제공하며 강제 색상 모드에서는 시스템 스크롤바와 포커스 색을 사용한다.

## Typography

한국어 본문은 시스템에 설치된 한국어 산세리프 글꼴을 사용한다. 외부 글꼴 다운로드에 의존하지 않는다. HTTP 메서드, URL, 헤더, JSON에는 고정폭 글꼴을 사용한다. 본문 15px, 주요 제목 32~48px, 패널 제목 17px를 기준으로 한다. 긴 URL과 JSON은 줄바꿈해 페이지 밖으로 넘치지 않게 한다.

## Layout

최대 폭 1600px, 탐색 영역 230px, 본문은 유동 폭이다. 요청·응답을 동일한 폭으로 배치하고 문서 스크롤을 사용한다. 1100px 이하에서는 여백과 탐색 폭을 줄이고 760px 이하에서는 탐색 버튼을 가로로, 패널을 세로로 배치한다. 결과 영역과 오류 안내 영역의 최소 높이를 확보한다.

## Elevation & Depth

패널·필드·버튼의 경계선과 배경색으로 계층을 표현한다. 그림자와 겹치는 모달은 사용하지 않는다.

## Shapes

주 패널 10px, 버튼 7px, 필드 5px 모서리를 사용한다. 기본 HTML select의 팝업 크기와 조작은 운영체제에 맡긴다.

## Components

요청 폼·전송 상태·응답 패널은 lab.js가 관리하는 단일 구현을 공유한다. HTML 응답 화면도 lab.css의 버튼·본문·패널 규칙을 사용한다.

| Capability | Canonical owner | Source of truth | Allowed variants | Verification |
|---|---|---|---|---|
| Form | index.html + lab.js | 서버 검증 제약 | 요청 바인딩·메서드·응답 비교 | MockMvc + 브라우저 실습 |
| Select/Listbox | 기본 HTML select | index.html | 운영체제 기본 팝업 | 키보드·좁은 화면 |
| Scrollbar | lab.css 전역 규칙 | :root 토큰 | 강제 색상 모드 | computed style |
| Toast | response-message live region | lab.js | 대기·전송·성공·실패 | 브라우저 실습 |

폼은 novalidate로 브라우저 팝업을 끄고 서버의 검증 결과를 필드에 연결한다. 필수값·범위·형식 오류는 응답 패널에 남기며 오류 필드에 aria-invalid와 안내 문구를 적용한다. 잘못된 요청을 관찰하는 도구이므로 클라이언트가 서버 검증 전에 오류 입력을 차단하지 않는다.

전송 시 설정과 주제 변경을 잠가 중복 요청과 이전 응답 덮어쓰기를 막는다. 10초 시간 제한 후 재시도할 수 있다. 입력은 브라우저 저장소에 저장하지 않는다. 주제 변경 시 이전 응답을 초기화한다. 상태 안내는 role=status로 읽히며 결과 본문은 textContent로 출력한다.

기본 동작 이름은 요청 보내기, Swagger UI 열기, 실습 홈으로다. 클릭 대상은 버튼·링크를 사용하고 hover·focus·active·disabled 상태를 제공한다. 장식 아이콘 대신 텍스트와 방향 기호만 사용한다. 모션은 추가하지 않으며 reduced-motion을 존중한다.

## Do's and Don'ts

- 요청·응답의 실제 Content-Type과 상태 코드를 함께 보여준다.
- 서버 오류 뒤에도 입력값과 응답 본문을 유지한다.
- 예제 API를 데이터가 영속 저장되는 CRUD로 설명하지 않는다.
- 서버에서 받은 문자열을 HTML로 삽입하지 않는다.
