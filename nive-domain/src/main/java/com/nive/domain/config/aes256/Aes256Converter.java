package com.nive.domain.config.aes256;


import com.nive.domain.config.port.AesKeyPropertiesPolicy;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * @author nive
 * @class Aes256Converter
 * @desc 개인정보 암호화/복호화 처리를 위한 AES256Converter
 * 향후 데이터가 많이 쌓이거나 몇만건 조회 등이 이루어지는 경우 복호화 불가능한 암호화 처리 및 평문을 별도 컬럼으로 해시값으로 저장하도록 처리
 * @since 2025-09-23
 */
@Converter
@RequiredArgsConstructor
public class Aes256Converter implements AttributeConverter<String,String> {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final byte[] IV = new byte[16]; // 고정 IV

    //    SpringBeanContainer에 의해 자동으로 스프링 빈 주입 지원되지만 버전에 따라 static setter 방식으로 해야함.
    private final AesKeyPropertiesPolicy aesKeyProperties;

//    private static AesKeyProperties aesKeyProperties; // 스프링에서 주입받은 키를 담아둠
//
//    // Spring이 시작할 때 세팅
//    public static void setAesKeyProperties(AesKeyProperties properties) {
//        aesKeyProperties = properties;
//    }

    /*암호화한 값으로 비교*/
    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            if (attribute == null) return null;
            SecretKeySpec keySpec = new SecretKeySpec(aesKeyProperties.getAesKey().getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(IV));
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e) {
            throw new IllegalStateException("AES encryption error", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null) return null;
            SecretKeySpec keySpec = new SecretKeySpec(aesKeyProperties.getAesKey().getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(IV));
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (Exception e) {
            return dbData;
        }
    }
}
