package com.waya.wayaauthenticationservice.util;

import java.net.InetAddress;

import javax.servlet.http.HttpServletRequest;

import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.waya.wayaauthenticationservice.pojo.DevicePojo;

@Service
public class ReqIPUtils {
	
	private final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };
	public final String getClientIP(HttpServletRequest request) {
        String ip = null;
        if (request == null) {
            if (RequestContextHolder.getRequestAttributes() == null) {
                return null;
            }
            request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        }

        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!ObjectUtils.isEmpty(ip))
            return ip;

        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (ipList != null && ipList.length() != 0 && !"unknown".equalsIgnoreCase(ipList)) {
                return ipList.split(",")[0];
            }
        }
        return request.getRemoteAddr();
    }
	
	public void getMessage() {
		
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
