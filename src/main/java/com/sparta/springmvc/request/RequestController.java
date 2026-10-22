package com.sparta.springmvc.request;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

@Controller
@Validated
@RequestMapping("/hello/request")
public class RequestController {
    @GetMapping("/form/html")
    public String helloForm() { return "redirect:/#request-lab"; }

    @GetMapping("/star/{name}/age/{age}")
    @ResponseBody
    public BindingResponse path(@PathVariable("name") @NotBlank @Size(max = 40) String name, @PathVariable("age") @Min(0) @Max(150) Integer age) {
        return new BindingResponse("@PathVariable", name, age);
    }

    @GetMapping("/form/param")
    @ResponseBody
    public BindingResponse query(@RequestParam("name") @NotBlank @Size(max = 40) String name, @RequestParam("age") @Min(0) @Max(150) Integer age) {
        return new BindingResponse("@RequestParam · query", name, age);
    }

    @PostMapping(value = "/form/param", consumes = "application/x-www-form-urlencoded")
    @ResponseBody
    public BindingResponse form(@RequestParam("name") @NotBlank @Size(max = 40) String name, @RequestParam("age") @Min(0) @Max(150) Integer age) {
        return new BindingResponse("@RequestParam · form", name, age);
    }

    @PostMapping(value = "/form/model", consumes = "application/x-www-form-urlencoded")
    @ResponseBody
    public BindingResponse formModel(@Valid @ModelAttribute Star star) {
        return new BindingResponse("@ModelAttribute · form", star.getName(), star.getAge());
    }

    @GetMapping("/form/param/model")
    @ResponseBody
    public BindingResponse queryModel(@Valid @ModelAttribute Star star) {
        return new BindingResponse("@ModelAttribute · query", star.getName(), star.getAge());
    }

    @PostMapping(value = "/form/json", consumes = "application/json")
    @ResponseBody
    public BindingResponse json(@Valid @RequestBody Star star) {
        return new BindingResponse("@RequestBody · JSON", star.getName(), star.getAge());
    }
}
