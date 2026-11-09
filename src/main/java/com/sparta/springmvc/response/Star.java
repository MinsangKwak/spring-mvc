package com.sparta.springmvc.response;

/**
 * 응답 비교 실습용 DTO. Jackson이 getter로 직렬화하는 평범한 객체이며 검증 제약이 없다.
 * 요청 실습의 {@link com.sparta.springmvc.request.Star}와 일부러 분리해 검증 제약이 응답 예제에 섞이지 않게 했다.
 */
public class Star {
    private String name;
    private int age;

    public Star() {}
    public Star(String name, int age) { this.name = name; this.age = age; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
