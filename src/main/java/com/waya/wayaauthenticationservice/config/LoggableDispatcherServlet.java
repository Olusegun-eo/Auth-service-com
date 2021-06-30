package com.waya.wayaauthenticationservice.config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.waya.wayaauthenticationservice.util.ReqIPUtils;

import lombok.extern.slf4j.Slf4j;

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
		HandlerExecutionChain handler = getHandler(request);
		try {
			super.doDispatch(request, response);
		} finally {
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
		jsonObject.addProperty("javaMethod", handler.toString());
		jsonObject.addProperty("response", getResponsePayload(response));
		jsonObject.addProperty("session", request.getSession().getId());
		if (status > 299) {
			String requestBody = null;
			try {
				jsonObject.addProperty("request", request.getReader().readLine());
				requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			jsonObject.addProperty("requestBody", requestBody);
			jsonObject.addProperty("requestParams", request.getQueryString());

			String json = gson.toJson(jsonObject);
			log.info(json);
		}
		logger.info(jsonObject);

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

	private void updateResponse(HttpServletResponse response) throws IOException {
		ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response,
				ContentCachingResponseWrapper.class);
		assert responseWrapper != null;
		responseWrapper.copyBodyToResponse();
	}
}
