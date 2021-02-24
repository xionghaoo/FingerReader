package com.ubtrobot.fingerreader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import android.util.Base64;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * 指尖文字识别 WebAPI 接口调用示例
 * 运行前：请先填写Appid、APIKey、APISecret
 * 运行方法：直接运行 main() 即可 
 * 结果： 控制台输出结果信息
 * 
 * 1.接口文档（必看）：https://www.xfyun.cn/doc/words/finger-word-discern/API.html
 * 2.错误码链接：https://www.xfyun.cn/document/error-code （错误码code为5位数字）
 * @author iflytek
 */

public class WebFingerOcr {
	private static final String TAG = "WebFingerOcr";

	// 指尖文字识别 webapi 接口地址
    private static final String WebFOCR_URL = "https://tyocr.xfyun.cn/v2/ocr"; //https url
	// 应用ID（到控制台获取）
	private static final String APPID = "602e2c80";
	// 接口APISercet（到控制台的指尖文字识别页面获取）
	private static final String API_SECRET = "ac6fbc58c0173ee28059bac4bce7eb5c";
	// 接口APIKey（到控制台的指尖文字识别页面获取）
	private static final String API_KEY = "1fdccba8f51414ebc64b68c6d86fc2fb";
	// 图片地址
	private static final String AUDIO_PATH = "resource\\finger\\4.jpg";

	public static void test(byte[] imageByteArray, ResultCallback resultCall) throws Exception {
		if (APPID.equals("") || API_KEY.equals("") || API_SECRET.equals("")) {
			System.out.println("Appid 或APIKey 或APISecret 为空！请打开demo代码，填写相关信息。");
			return;
		}
		String body = buildHttpBody(imageByteArray);
		Log.d(TAG, "指尖识别测试");
		//System.out.println("【指尖文字识别 WebAPI body】\n" + body);
		Map<String, String> header = buildHttpHeader(body);
		Map<String, Object> resultMap = HttpUtil.doPost2(WebFOCR_URL, header, body);
		if (resultMap != null) {
			String resultStr = resultMap.get("body").toString();
			Log.d(TAG, "【指尖文字识别 WebAPI 接口调用结果】\n" + resultStr);
			//以下仅用于调试
		    Gson json = new Gson();
	        ResponseData resultData = json.fromJson(resultStr, ResponseData.class);
	        int code = resultData.getCode();
	        if (resultData.getCode() != 0) {
	    		System.out.println("请前往https://www.xfyun.cn/document/error-code?code=" + code + "查询解决办法");
	    		resultCall.failure();
	        } else {
	        	resultCall.success(resultData);
			}
		} else {
			resultCall.failure();
			System.out.println("调用失败！请根据错误信息检查代码，接口文档：https://www.xfyun.cn/doc/words/photo-calculate-recg/API.html");
		}
	}
	

	/**
	 * 组装http请求头
	 */	
   public static Map<String, String> buildHttpHeader(String body) throws Exception {
		Map<String, String> header = new HashMap<String, String>();
        URL url = new URL(WebFOCR_URL);
        
        //时间戳
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date dateD = new Date();
        String date = format.format(dateD);
		//System.out.println("【指尖文字识别 WebAPI date】\n" + date);

		//对body进行sha256签名,生成digest头部，POST请求必须对body验证
		String digestBase64 = "SHA-256=" + signBody(body);
		//System.out.println("【指尖文字识别 WebAPI digestBase64】\n" + digestBase64);
        
		//hmacsha256加密原始字符串
        StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").//
        		append("date: ").append(date).append("\n").//
                append("POST ").append(url.getPath()).append(" HTTP/1.1").append("\n").//
                append("digest: ").append(digestBase64);
		//System.out.println("【指尖文字识别 WebAPI builder】\n" + builder);
		String sha = hmacsign(builder.toString(), API_SECRET);
		//System.out.println("【指尖文字识别 WebAPI sha】\n" + sha);
		
		//组装authorization
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", API_KEY, "hmac-sha256", "host date request-line digest", sha);
        //System.out.println("【指尖文字识别 WebAPI authorization】\n" + authorization);
		
        header.put("Authorization", authorization);
		header.put("Content-Type", "application/json");
		header.put("Accept", "application/json,version=1.0");
		header.put("Host", url.getHost());
		header.put("Date", date);
		header.put("Digest", digestBase64);
		//System.out.println("【指尖文字识别 WebAPI header】\n" + header);
		return header;
    }   

	/**
	 * 组装http请求体
	 */	
   public static String buildHttpBody(byte[] imageByteArray) throws Exception {
       JsonObject body = new JsonObject();
       JsonObject business = new JsonObject();
       JsonObject common = new JsonObject();
       JsonObject data = new JsonObject();
       //填充common
	   common.addProperty("app_id", APPID);
	   //填充business
	   business.addProperty("ent", "fingerocr");
	   business.addProperty("mode", "finger+ocr");
	   business.addProperty("method", "dynamic");
	   //business.addProperty("cut_w_scale", 5.0);
	   //business.addProperty("cut_h_scale", 2);
	   //business.addProperty("cut_shift", 1);
	   business.addProperty("resize_w", 1088);
	   business.addProperty("resize_h", 1632);
	   //填充data

//	   byte[] imageByteArray = FileUtil.read(AUDIO_PATH);
	   String imageBase64 = new String(Base64.encodeToString(imageByteArray, Base64.NO_WRAP));
	   data.addProperty("image", imageBase64);

	   //填充body
	   body.add("common", common);
	   body.add("business", business);
	   body.add("data", data);
       
       return body.toString();
   }
   
	/**
	 * 对body进行SHA-256加密
	 */	
	private static String signBody(String body) throws Exception {
		MessageDigest messageDigest;
		String encodestr = "";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(body.getBytes("UTF-8"));
			encodestr = Base64.encodeToString(messageDigest.digest(), Base64.NO_WRAP);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return encodestr;
	}
	
	/**
	 * hmacsha256加密
	 */
	private static String hmacsign(String signature, String apiSecret) throws Exception {
		Charset charset = Charset.forName("UTF-8");
		Mac mac = Mac.getInstance("hmacsha256");
		SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
		mac.init(spec);
		byte[] hexDigits = mac.doFinal(signature.getBytes(charset));
		return Base64.encodeToString(hexDigits, Base64.NO_WRAP);
	}
	
   public static class ResponseData {
       private int code;
       private String message;
       private String sid;
       private Object data;
       public int getCode() {
           return code;
       }
       public String getMessage() {
           return this.message;
       }
       public String getSid() {
           return sid;
       }
       public Object getData() {
           return data;
       }
   }

   	interface ResultCallback {
		public void success(ResponseData data);
		public void failure();
	}
}
