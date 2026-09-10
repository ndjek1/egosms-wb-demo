package org.pahappa.systems.egoSms.core.services.impl.whatsapp;
import com.fasterxml.jackson.databind.*;import org.apache.http.client.fluent.Request;import org.apache.http.entity.ContentType;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Component;import java.net.URLEncoder;
@Component public class MetaGraphClient{
 private final ObjectMapper mapper=new ObjectMapper();@Autowired private MetaConfiguration config;
 public JsonNode get(String path,String token){try{Request r=Request.Get(url(path));if(token!=null)r.addHeader("Authorization","Bearer "+token);return response(r);}catch(Exception e){throw failure(e);}}
 public JsonNode post(String path,String token,Object body){try{return response(Request.Post(url(path)).addHeader("Authorization","Bearer "+token).bodyString(mapper.writeValueAsString(body),ContentType.APPLICATION_JSON));}catch(Exception e){throw failure(e);}}
 public JsonNode exchangeCode(String code){try{String path="/oauth/access_token?client_id="+encode(config.appId())+"&client_secret="+encode(config.appSecret())+"&code="+encode(code);return get(path,null);}catch(Exception e){throw failure(e);}}
 private JsonNode response(Request request)throws Exception{org.apache.http.HttpResponse r=request.execute().returnResponse();String body=org.apache.http.util.EntityUtils.toString(r.getEntity());if(r.getStatusLine().getStatusCode()>=300)throw new IllegalStateException("Meta API rejected the request: "+body);return mapper.readTree(body);}
 private String url(String path){return config.graphBase()+"/"+config.version()+path;}private String encode(String v)throws Exception{return URLEncoder.encode(v,"UTF-8");}private RuntimeException failure(Exception e){return e instanceof RuntimeException?(RuntimeException)e:new IllegalStateException("Meta API request failed",e);}
}
