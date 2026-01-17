package com.pairapp.security;

import com.pairapp.config.TelegramProperties;
import com.pairapp.exception.ApiException;
import com.pairapp.exception.ErrorCodes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@Component
public class TelegramInitDataValidator {
    private final TelegramProperties properties;

    public TelegramInitDataValidator(TelegramProperties properties) {
        this.properties = properties;
    }

    public Map<String, String> validate(String initData) {
        if (properties.botToken() == null || properties.botToken().isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, ErrorCodes.VALIDATION_ERROR,
                    "Telegram auth not configured");
        }
        long maxAgeSeconds = properties.authMaxAgeSeconds() != null ? properties.authMaxAgeSeconds() : 300L;
        Map<String, String> data = parseInitData(initData);
        String hash = data.remove("hash");
        if (hash == null || hash.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Invalid Telegram auth");
        }
        String dataCheckString = buildDataCheckString(data);
        String secretKey = sha256Hex(properties.botToken());
        String computedHash = hmacSha256Hex(dataCheckString, secretKey);
        if (!computedHash.equalsIgnoreCase(hash)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Invalid Telegram auth");
        }
        String authDateStr = data.get("auth_date");
        if (authDateStr == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Invalid Telegram auth");
        }
        long authDate = Long.parseLong(authDateStr);
        long now = Instant.now().getEpochSecond();
        if (now - authDate > maxAgeSeconds) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Telegram auth expired");
        }
        return data;
    }

    private Map<String, String> parseInitData(String initData) {
        Map<String, String> map = new TreeMap<>();
        for (String part : initData.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }
        return map;
    }

    private String buildDataCheckString(Map<String, String> data) {
        return data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.VALIDATION_ERROR,
                    "Telegram validation failed");
        }
    }

    private String hmacSha256Hex(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hexToBytes(key), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.VALIDATION_ERROR,
                    "Telegram validation failed");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
