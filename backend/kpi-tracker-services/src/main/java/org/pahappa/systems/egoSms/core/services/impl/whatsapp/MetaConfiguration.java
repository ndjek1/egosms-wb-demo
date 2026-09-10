package org.pahappa.systems.egoSms.core.services.impl.whatsapp;

import org.springframework.stereotype.Component;
import java.io.*;
import java.net.URI;
import java.util.*;

@Component
public class MetaConfiguration {
    private final Properties fileValues = new Properties();
    private String loadedFile;
    public MetaConfiguration() {
        String explicit=firstNonBlank(System.getProperty("egosms.env.file"),System.getenv("EGOSMS_ENV_FILE"));
        if(explicit!=null&&loadIfPresent(new File(explicit)))return;
        if(walkForEnv(new File(System.getProperty("user.dir","."))))return;
        try{URI location=MetaConfiguration.class.getProtectionDomain().getCodeSource().getLocation().toURI();walkForEnv(new File(location));}catch(Exception ignored){}
    }
    public String graphBase(){return value("META_GRAPH_BASE_URL",value("META_API_BASE_URL","https://graph.facebook.com"));}
    public String version(){return value("META_API_VERSION",value("META_GRAPH_VERSION","v23.0"));}
    public String appId(){return required("META_APP_ID");} public String appSecret(){return required("META_APP_SECRET");} public String signupConfigId(){return required("META_EMBEDDED_SIGNUP_CONFIG_ID");}
    public String registrationPin(){return required("META_REGISTRATION_PIN");} public String encryptionKey(){return required("META_TOKEN_ENCRYPTION_KEY");} public String webhookVerifyToken(){return required("META_WEBHOOK_VERIFY_TOKEN");}
    public String loadedFile(){return loadedFile;}
    private boolean walkForEnv(File start){File directory=start!=null&&start.isFile()?start.getParentFile():start;for(int i=0;i<14&&directory!=null;i++,directory=directory.getParentFile())if(loadIfPresent(new File(directory,".env")))return true;return false;}
    private boolean loadIfPresent(File file){if(file==null||!file.isFile())return false;load(file);loadedFile=file.getAbsolutePath();return true;}
    private String required(String name){String v=value(name,null);if(v==null||v.trim().isEmpty()){String source=loadedFile==null?"No .env file was found":"Loaded "+loadedFile;throw new IllegalStateException(name+" is not configured. "+source+". You can also set -Degosms.env.file=/absolute/path/.env");}return v.trim();}
    private String value(String name,String fallback){String v=System.getenv(name);if(v==null||v.trim().isEmpty())v=fileValues.getProperty(name);return v==null||v.trim().isEmpty()?fallback:v.trim();}
    private String firstNonBlank(String one,String two){if(one!=null&&!one.trim().isEmpty())return one.trim();return two==null||two.trim().isEmpty()?null:two.trim();}
    private void load(File file){try{BufferedReader reader=new BufferedReader(new FileReader(file));String line;while((line=reader.readLine())!=null){line=line.trim();if(line.isEmpty()||line.startsWith("#"))continue;if(line.startsWith("export "))line=line.substring(7).trim();int split=line.indexOf('=');if(split<1)continue;String key=line.substring(0,split).trim(),v=line.substring(split+1).trim();if(v.length()>1&&((v.startsWith("\"")&&v.endsWith("\""))||(v.startsWith("'")&&v.endsWith("'"))))v=v.substring(1,v.length()-1);fileValues.setProperty(key,v);}reader.close();}catch(IOException e){throw new IllegalStateException("Could not read "+file.getAbsolutePath(),e);}}
}
