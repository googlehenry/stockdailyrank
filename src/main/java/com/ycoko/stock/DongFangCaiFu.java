package com.ycoko.stock;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class DongFangCaiFu {
	private Map<String,String> reqHeaders = new HashMap<String,String>();
	public DongFangCaiFu() {
		reqHeaders.put("Host", "nuff.eastmoney.com");
		reqHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0");
	}
	
	public DailyInfo getDailyInfo(String code) throws Exception{
		String prefix = "";
		if(code.startsWith("6")){
			prefix = "1";
		}else{
			prefix = "2";
		}
		String responseHtml = HttpUtil.getHtml("http://nuff.eastmoney.com/EM_Finance2015TradeInterface/JS.ashx?id="+code+"", "utf-8", reqHeaders);
		if(responseHtml.length()<60){
			responseHtml = HttpUtil.getHtml("http://nuff.eastmoney.com/EM_Finance2015TradeInterface/JS.ashx?id="+code+prefix+"", "utf-8", reqHeaders);
		}
		
		DailyInfo dailyInfo = new DailyInfo();
		if(responseHtml.length()>60){
			responseHtml = responseHtml.substring(responseHtml.indexOf("\"Value\":[")+"\"Value\":[".length(), responseHtml.lastIndexOf("]})")); 
			
			responseHtml = responseHtml.replace("\"", "");
			responseHtml = responseHtml.replace("[", "").replace("]", "");
			
			String[] dataArr = responseHtml.split(",");
			if(dataArr.length>49){
				dailyInfo.marketCode = dataArr[0];
				dailyInfo.code = dataArr[1];
				dailyInfo.name = dataArr[2];
				dailyInfo.limitTopPrice = dataArr[23];
				dailyInfo.limitBottomPrice = dataArr[24];
				dailyInfo.currentPrice = dataArr[25];
				dailyInfo.avgPrice = dataArr[26];
				dailyInfo.increasedPrice = dataArr[27];
				dailyInfo.todayOpenPrice = dataArr[28];
				dailyInfo.increasedPercent = dataArr[29];
				dailyInfo.highestPrice = dataArr[30];
				dailyInfo.totalDoneVolumnAmount = dataArr[31];
				dailyInfo.lowestPrice = dataArr[32];
				dailyInfo.yesterdayClosedPrice = dataArr[34];
				dailyInfo.totalDonePriceAmount = dataArr[35];
				dailyInfo.dealIncreaseRatio = dataArr[36];
				dailyInfo.exchangeRatePercent = dataArr[37];
				dailyInfo.priceEarningRatio = dataArr[38];
				dailyInfo.buySellRatio = dataArr[41];
				dailyInfo.buySellAmountDiff = dataArr[42];
				dailyInfo.totalMarketCapitalInTrade = dataArr[45];
				dailyInfo.totalMarketCapital = dataArr[46];
				dailyInfo.date = dataArr[49];
			}
			
		}
		return dailyInfo;
	}
	
	public TreeMap<String,String> getAllStocksMap() throws Exception{
		TreeMap<String,String> map = new TreeMap<String,String>();
		
		String responseHtml = HttpUtil.getHtml("http://quote.eastmoney.com/stocklist.html", "gb2312", reqHeaders);
		Document doc = Jsoup.parse(responseHtml);
		
		Elements elements = doc.select(".quotebody ul li a");
		
		for(Element stock:elements){
			String nameCode = stock.text();
			String name = nameCode.substring(0, nameCode.indexOf("("));
			String code = nameCode.substring(nameCode.indexOf("(")+1,nameCode.lastIndexOf(")"));
			
			map.put(code, name);
		}
		
		return map;
	}
	
	public static void main(String[] args) throws Exception {
		
		
		DailyInfo info = new DongFangCaiFu().getDailyInfo("600016");
		System.out.println("ok");
	}
}
