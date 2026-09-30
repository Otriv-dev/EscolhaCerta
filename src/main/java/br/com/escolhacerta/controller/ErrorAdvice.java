package br.com.escolhacerta.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.ui.Model;

@ControllerAdvice public class ErrorAdvice {
    @ExceptionHandler(MaxUploadSizeExceededException.class) @ResponseStatus(org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE) public String large(Model m) {
        m.addAttribute("error","O arquivo ultrapassa o limite de 5 MB.");
        return "error";
    }
}
