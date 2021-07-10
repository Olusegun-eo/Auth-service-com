package com.waya.wayaauthenticationservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.util.ReqIPUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

@Slf4j
public class LoggableDispatcherServlet extends DispatcherServlet {

    private static final long serialVersionUID = 2453821271976611591L;

    @Autowired
    ReqIPUtils reqUtil;

    @Override
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long startTime = System.currentTimeMillis();
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }
        HandlerExecutionChain handler = null;
        try {
            handler = getHandler(request);
            super.doDispatch(request, response);

        } catch (HttpMediaTypeNotSupportedException ex) {
            ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            String str = convertObjectToJson(errorResponse);
            PrintWriter pr = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            pr.write(str);
        } catch (Exception ex) {
            ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().write(convertObjectToJson(errorResponse));
            String str = convertObjectToJson(errorResponse);
            PrintWriter pr = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            pr.write(str);
        } finally{
            log(request, response, handler, System.currentTimeMillis() - startTime);
            updateResponse(response);
        }
    }

    private void log(HttpServletRequest request, HttpServletResponse response, HandlerExecutionChain handler,
                     long timeTaken) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        int status = response.getStatus();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("httpStatus", status);
        jsonObject.addProperty("path", request.getRequestURI());
        jsonObject.addProperty("httpMethod", request.getMethod());
        jsonObject.addProperty("timeTakenMs", timeTaken);
        jsonObject.addProperty("clientIP", reqUtil.getClientIP(request));
        jsonObject.addProperty("javaMethod", handler == null ? "null" : handler.toString());
        jsonObject.addProperty("response", getResponsePayload(response));

        if (status > 299) {
            String requestData = null;
            try {
                jsonObject.addProperty("request", request.getReader().readLine());
                //requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                requestData = getRequestData(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
            jsonObject.addProperty("requestBody", requestData);
            jsonObject.addProperty("requestParams", request.getQueryString());
        }
        String json = gson.toJson(jsonObject);
        log.info(json);
    }

    private String getResponsePayload(HttpServletResponse response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response,
                ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                int length = Math.min(buf.length, 5120);
                try {
                    return new String(buf, 0, length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException ex) {
                    log.error("Error Occurred in Encoding Response Body: {}", ex.getMessage());
                } catch (Exception ex) {
                    log.error("Error Occurred in Encoding Response Body: {}", ex.getMessage());
                }
            }
        }
        return "[unknown]";
    }

    private String getRequestData(final HttpServletRequest request) throws UnsupportedEncodingException {
        String payload = null;
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
            }
        }
        return payload;
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response,
                ContentCachingResponseWrapper.class);
        assert responseWrapper != null;
        responseWrapper.copyBodyToResponse();
    }

    public String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
