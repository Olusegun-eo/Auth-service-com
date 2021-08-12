package com.waya.wayaauthenticationservice.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Service;

import com.waya.wayaauthenticationservice.pojo.others.DevicePojo;

@Service
public class ReqIPUtils {

	public final String getClientIP(HttpServletRequest request) {
		final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
	}
	
	public DevicePojo GetDevice(Device device) {
		String deviceType, platform;

		if (device.isNormal()) {
			deviceType = "browser";
		} else if (device.isMobile()) {
			deviceType = "mobile";
		} else if (device.isTablet()) {
			deviceType = "tablet";
		} else {
			deviceType = "browser";
		}

		platform = device.getDevicePlatform().name();

		if (platform.equalsIgnoreCase("UNKNOWN")) {
			platform = "browser";
		}
		return new DevicePojo(deviceType, platform);
	}

}
