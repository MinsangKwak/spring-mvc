package com.sparta.springmvc.request;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.sparta.springmvc.error.ApiError;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

/**
 * 같은 이름·나이를 경로·쿼리·폼·JSON으로 받아 어떤 애노테이션이 어떻게 읽는지 비교하는 컨트롤러.
 * <p>
 * 개별 인자({@code @PathVariable}·{@code @RequestParam})는 클래스의 {@code @Validated}로 메서드 검증을,
 * 객체 인자({@code @ModelAttribute}·{@code @RequestBody})는 {@code @Valid}로 Bean Validation을 거친다.
 * 두 경로는 던지는 예외가 달라 {@link com.sparta.springmvc.error.ApiExceptionHandler}에서 서로 다른 분기를 탄다.
 * 데이터를 저장하거나 변경하지 않는다.
 */
@Controller
@Validated
@RequestMapping("/hello/request")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "요청 바인딩 성공"),
    @ApiResponse(responseCode = "400", description = "필수값·범위·타입·JSON 문법 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "415", description = "요청 Content-Type 불일치",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
})
@Tag(name = "02 · 요청 바인딩", description = "같은 이름·나이를 경로, 쿼리, 폼, JSON으로 전달합니다. 이름은 1~40자, 나이는 0~150의 정수입니다.")
public class RequestController {
    @GetMapping("/form/html")
    @Hidden
    public String helloForm() { return "redirect:/#request-lab"; }

    @GetMapping(value = "/star/{name}/age/{age}", produces = "application/json")
    @ResponseBody
    @Operation(summary = "경로 변수를 객체 응답으로 확인")
    public BindingResponse path(@PathVariable("name") @NotBlank @Size(max = 40) String name, @PathVariable("age") @Min(0) @Max(150) Integer age) {
        return new BindingResponse("@PathVariable", name, age);
    }

    @GetMapping(value = "/form/param", produces = "application/json")
    @ResponseBody
    @Operation(summary = "쿼리 파라미터를 개별 인자로 받기")
    public BindingResponse query(@RequestParam("name") @NotBlank @Size(max = 40) String name, @RequestParam("age") @Min(0) @Max(150) Integer age) {
        return new BindingResponse("@RequestParam · query", name, age);
    }

    @PostMapping(value = "/form/param", consumes = "application/x-www-form-urlencoded", produces = "application/json")
    @ResponseBody
    @Operation(summary = "폼 데이터를 개별 인자로 받기")
    public BindingResponse form(@RequestParam("name") @NotBlank @Size(max = 40) String name, @RequestParam("age") @Min(0) @Max(150) Integer age) {
        return new BindingResponse("@RequestParam · form", name, age);
    }

    @PostMapping(value = "/form/model", consumes = "application/x-www-form-urlencoded", produces = "application/json")
    @ResponseBody
    @Operation(summary = "폼 데이터를 객체로 받기",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(implementation = Star.class))))
    public BindingResponse formModel(@io.swagger.v3.oas.annotations.Parameter(hidden = true) @Valid @ModelAttribute Star star) {
        return new BindingResponse("@ModelAttribute · form", star.getName(), star.getAge());
    }

    @GetMapping(value = "/form/param/model", produces = "application/json")
    @ResponseBody
    @Operation(summary = "쿼리 파라미터를 객체로 받기")
    public BindingResponse queryModel(@Valid @ModelAttribute Star star) {
        return new BindingResponse("@ModelAttribute · query", star.getName(), star.getAge());
    }

    @PostMapping(value = "/form/json", consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Operation(summary = "JSON 본문을 객체로 받기")
    public BindingResponse json(@Valid @RequestBody Star star) {
        return new BindingResponse("@RequestBody · JSON", star.getName(), star.getAge());
    }
}
