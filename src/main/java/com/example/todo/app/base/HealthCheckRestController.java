package com.example.todo.app.base;

import static com.example.todo.Main.isPrepared;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("health")
public class HealthCheckRestController {

  @GetMapping("")
  public ResponseEntity get() {

    if (isPrepared) {
      return new ResponseEntity(HttpStatus.OK);
    }
    return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
  }

}
