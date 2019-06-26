package com.remxbot.bot.rest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class ErrorPage implements ErrorController {
    @RequestMapping("/error")
    public ErrorRepresentation errorPage(HttpServletRequest request, HttpServletResponse response) {
        var rep = new ErrorRepresentation(request);
        response.setStatus(rep.statusCode);
        return rep;
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    private class ErrorRepresentation {
        public String message;
        public int statusCode;
        public Throwable exception;

        public ErrorRepresentation(HttpServletRequest request) {
            var statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
            if (statusCode == null) {
                statusCode = 500;
            }
            this.statusCode = statusCode;
            message = (String) request.getAttribute("javax.servlet.error.message");
            exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
        }
    }
}
