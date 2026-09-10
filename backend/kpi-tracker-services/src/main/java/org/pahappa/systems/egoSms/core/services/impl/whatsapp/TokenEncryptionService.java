package org.pahappa.systems.egoSms.core.services.impl.whatsapp;
import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Service;
import javax.crypto.*;import javax.crypto.spec.*;import java.nio.charset.StandardCharsets;import java.security.*;import java.util.*;
@Service public class TokenEncryptionService{
 @Autowired private MetaConfiguration config;
 public String encrypt(String plain){try{byte[] iv=new byte[12];new SecureRandom().nextBytes(iv);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key(),new GCMParameterSpec(128,iv));return Base64.getEncoder().encodeToString(iv)+"."+Base64.getEncoder().encodeToString(c.doFinal(plain.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException("Could not encrypt Meta token",e);}}
 public String decrypt(String stored){try{String[] p=stored.split("\\.",2);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.getDecoder().decode(p[0])));return new String(c.doFinal(Base64.getDecoder().decode(p[1])),StandardCharsets.UTF_8);}catch(Exception e){throw new IllegalStateException("Could not decrypt Meta token",e);}}
 private SecretKey key()throws Exception{return new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(config.encryptionKey().getBytes(StandardCharsets.UTF_8)),"AES");}
}
