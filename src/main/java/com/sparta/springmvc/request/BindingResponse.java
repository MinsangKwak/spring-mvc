package com.sparta.springmvc.request;

/**
 * 요청 바인딩 성공 응답. binding은 서버가 어떤 애노테이션으로 값을 읽었는지, name·age는 실제로 읽힌 값이다.
 * 화면은 이 세 필드를 그대로 표시하므로 이름을 바꾸면 lab.js와 테스트의 jsonPath가 함께 바뀐다.
 */
public record BindingResponse(String binding, String name, Integer age) {}
