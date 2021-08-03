package com.waya.wayaauthenticationservice.util;

import com.waya.wayaauthenticationservice.SpringApplicationContext;

public class SecurityConstants {
    public static final long PASSWORD_RESET_EXPIRATION_TIME = 3600000;
    
    public static final String TOKEN_PREFIX = "serial ";
    public static final String HEADER_STRING = "Authorization";
    public static final String LOGIN_URL = "/api/users/login";  
    
	/**
	 * @return the secret
	 */
	public static String getSecret() {
		AppProperties appProperties = (AppProperties) SpringApplicationContext.getBean("appProperties");
		return appProperties.getTokenSecret();
	}

	/**
	 * @return the expiration
	 */
	public static Long getExpiration() {
		AppProperties appProperties = (AppProperties) SpringApplicationContext.getBean("appProperties");
		return appProperties.getTokenExpiryPeriod();
	}



//    private static final Key secret = MacProvider.generateKey(SignatureAlgorithm.HS256);
//    private static final byte[] secretBytes = secret.getEncoded();
//    @SuppressWarnings("unused")
//	private static final String base64SecretBytes = Base64.getEncoder().encodeToString(secretBytes);

}
