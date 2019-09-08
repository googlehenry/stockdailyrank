package com.ycoko.stock;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpUtil {

	
	public static String getHtmlExcep(String urlStr,String pageEncoding,Map<String,String> reqHeader) throws Exception{
		return getHtml(urlStr, pageEncoding, reqHeader);
	}
	public static String getHtml(String urlStr,String pageEncoding,Map<String,String> reqHeader) throws Exception{
		StringBuilder sb = new StringBuilder();
		
		try {
			URL url = new URL(urlStr);
			
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(20*1000);
			con.setReadTimeout(20*1000);
			//con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.72 Safari/537.36");
			if(reqHeader!=null){
				for(String header:reqHeader.keySet()){
					con.addRequestProperty(header, reqHeader.get(header));
				}
			}
			
			InputStream is = con.getInputStream();
			
			byte[] buf = new byte[2048];
			int len = 0; 
			
			while((len=is.read(buf))>-1){
				String data = new String(buf,0,len,pageEncoding);
				sb.append(data);
			}
		} catch (Exception e) {
//			getHtmlExcep(urlStr,pageEncoding,reqHeader);
			e.printStackTrace();
		}
		
		
		
		return sb.toString();
	}
	
}
