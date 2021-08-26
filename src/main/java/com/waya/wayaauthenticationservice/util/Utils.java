package com.waya.wayaauthenticationservice.util;

import com.google.crypto.tink.*;
import com.waya.wayaauthenticationservice.SpringApplicationContext;
import com.waya.wayaauthenticationservice.pojo.others.DevicePojo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

@Component
@Slf4j
public class Utils {

	@Value("${cipher.utils.value}")
	private String cipherValue;

	public byte[] getCipherValue() {
		return cipherValue.getBytes();
	}

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

	private static final String KEY_TYPE = "ECDSA_P256";
	private static String keysetFilename = "my_keyset.json";

	public void generateKeySet() throws Exception {
		KeysetHandle keysetHandle = KeysetHandle.generateNew(
				KeyTemplates.get(KEY_TYPE));
		/**
		 * write to a file.
		 */
		CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(
				new File(keysetFilename)));
		// or encrypt with a master Key and store tis key in GCP KMS
		//String masterKeyUri = "gcp-kms://projects/tink-examples/locations/global/keyRings/foo/cryptoKeys/bar";
		//keysetHandle.write(JsonKeysetWriter.withFile(new File(keysetFilename)),
		//			new GcpKmsClient().getAead(masterKeyUri));
	}

	private KeysetHandle getPrivateKey() throws Exception {
		KeysetHandle privateKeysetHandle = CleartextKeysetHandle.read(
				JsonKeysetReader.withFile(new File(keysetFilename)));

		return privateKeysetHandle;
	}

	public byte[] encryptData() throws Exception {
		PublicKeySign signer = getPrivateKey().getPrimitive(PublicKeySign.class);
		byte[] signature = signer.sign(getCipherValue());
		return signature;
	}

	public boolean verifySignedData(String data){

		boolean isCorrect = false;
		try{
			KeysetHandle publicKeysetHandle =
					getPrivateKey().getPublicKeysetHandle();
			PublicKeyVerify verifier = publicKeysetHandle.getPrimitive(PublicKeyVerify.class);
			byte[] signature = (byte[]) SpringApplicationContext.getBean("encryptSignature");
			verifier.verify(signature, data.getBytes());
			isCorrect = true;
		}catch(Exception e){
			log.error("An Error has occurred :: {}", e.getMessage());
		}
		log.info("Signature " + (isCorrect ? "correct" : "incorrect"));
		return isCorrect;
	}
}
