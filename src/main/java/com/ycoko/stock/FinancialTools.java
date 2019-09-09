package com.ycoko.stock;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FileUtils;
import org.jsoup.helper.StringUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FinancialTools {
	
	class StockMeta{
		static final String tyDiff = "tyDiff";
		static final String hilowDiff = "hilowDiff";
		static final String closePrice = "closePrice";
		static final String turnoverRate = "turnoverRate";
		
	}
	@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
	static class Rank implements Serializable{
		private static final long serialVersionUID = 1L;
		String name;
		Double min;
		Double max;
		Double cur;
		Double rank;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Double getMin() {
			return min;
		}
		public void setMin(Double min) {
			this.min = min;
		}
		public Double getMax() {
			return max;
		}
		public void setMax(Double max) {
			this.max = max;
		}
		public Double getCur() {
			return cur;
		}
		public void setCur(Double cur) {
			this.cur = cur;
		}
		public Double getRank() {
			return rank;
		}
		public void setRank(Double rank) {
			this.rank = rank;
		}
		public Rank(String name, Double min, Double max, Double cur, Double rank) {
			super();
			this.name = name;
			this.min = min;
			this.max = max;
			this.cur = cur;
			this.rank = rank;
		}
		@Override
		public String toString() {
			return "Rank [name=" + name + ", min=" + min + ", max=" + max + ", cur=" + cur + ", rank=" + rank + "]";
		}
		public String toTable() {
			String rankcolor = bgbyRank();
			
			return "<table style='border:1px solid blue;width:100%;'><tr><td>"+name+"</td><td>cur="+String.format("%.4f", cur)+"</td><td>min="+String.format("%.4f", min)+"</td><td>max="+String.format("%.4f", max)+"</td><td style='background:"+rankcolor+";color:white;'>rank="+(rank<10?"0"+String.format("%.2f", rank):String.format("%.2f", rank))+"</td></tr></table>";
		}
		private String bgbyRank() {
			String rankcolor= "green";
			if(rank>=90){
				rankcolor= "red";
			}
			else if(rank>=80){
				rankcolor = "blue";
			}
			
			if(rank<=10){
				rankcolor= "black";
			}
			else if(rank<=20){
				rankcolor= "gray";
			}
			return rankcolor;
		}
		
	}
	public static void main(String[] args) throws Exception {
		
		
		calculateTodayAllStocks();
		
		Timer timer = new Timer();
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		
		String year = String.valueOf((date.getYear()+1900));
		String month = String.valueOf(date.getMonth()+1);
		String day = String.valueOf(date.getDate());
		String firstTimeDateStr = year+"-"+month+"-"+day+" 20:00:00";
		
		Date firstTimeDate = new Date();
		try {
			firstTimeDate = sdf.parse(firstTimeDateStr);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		
		timer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				
				try {
					System.out.println("Timer task scheduled");
					calculateTodayAllStocks();
					System.out.println("Timer task:[getIncrementalNewsForKeyWord] done "+new Date().toLocaleString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}}, firstTimeDate, 1000*60*60*24);
		
		
	}


	static class RankReport{
		static Map<String,String> nameMap = new HashMap<String,String>();
		static{
			nameMap.put("600000", "浦发银行");
			nameMap.put("600015", "华夏银行");
			nameMap.put("600016", "民生银行");
			nameMap.put("600036", "招商银行");
			nameMap.put("601009", "南京银行");
			nameMap.put("601166", "兴业银行");
			nameMap.put("601169", "北京银行");
			nameMap.put("601288", "农业银行");
			nameMap.put("601328", "交通银行");
			nameMap.put("601398", "工商银行");
			nameMap.put("601818", "光大银行");
			nameMap.put("601939", "建设银行");
			nameMap.put("601988", "中国银行");
			nameMap.put("601998", "中信银行");
		}
		public RankReport(String name,String code, String rankData, List<Rank> ranks) {
			super();
			this.name = name;
			this.code = code;
			this.rankData = rankData;
			this.ranks = ranks;
		}
		String name;
		String code;
		String rankData;
		List<Rank> ranks;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getRankData() {
			return rankData;
		}
		public void setRankData(String rankData) {
			this.rankData = rankData;
		}
		public List<Rank> getRanks() {
			return ranks;
		}
		public void setRanks(List<Rank> ranks) {
			this.ranks = ranks;
		}
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		
		
		
	}
	private static void calculateTodayAllStocks() throws AddressException, JsonProcessingException, IOException, MessagingException, Exception{
		File[] files= new File("/ycoko/work/others/stocks/dailyrank").listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.contains("baseline");
			}
		});
		
		List<RankReport> rrs = new ArrayList<>();
		for(File f:files){
			String code = f.getName().substring(0, f.getName().indexOf("."));
			rrs.add(calculateToday(code));
		}
		
		
		StringBuilder sb2 = new StringBuilder();
		sb2.append("<div style='width:100%;'>");
		sb2.append("<h2>银行综合排名</h2>");
		sb2.append("<table style='width:100%;'><tr style='backgroud:golden;'><td>股票</td><td>当前价格</td><td>日高低差</td><td>日涨跌额</td><td>换手率%</td><td>综合分</td></tr>");

		List<RankReport> rrsSorted1 = new ArrayList<>();
		TreeMap<Double,RankReport> rrss = new TreeMap<>();
		
		rrs.forEach(rr->{
			double sum = 0.0;
			for(Rank r:rr.ranks){
				sum +=r.rank;
			}
			double totalRank = 100-sum/rr.ranks.size();
			rrss.put(totalRank, rr);
		});
		rrsSorted1.addAll(rrss.values());
		
		//cross compare
		rrsSorted1.forEach(rr->{
			StringBuilder lineb = new StringBuilder("<tr>");
			lineb.append("<td style='border:1px solid blue;'>"+rr.name+"</td>");
			double sum = 0.0;
			for(Rank r:rr.ranks){
				lineb.append("<td style='border:1px solid blue;color:white;background:"+r.bgbyRank()+"'>"+String.format("%.2f", r.rank)+"</td>");
				sum +=r.rank;
			}
			Rank totalRank = new Rank("", 0d, 0d, 0d, sum/rr.ranks.size());
			lineb.append("<td style='border:1px solid blue;color:white;background:"+totalRank.bgbyRank()+"'>"+String.format("%.2f", totalRank.rank)+"</td>");
			lineb.append("</tr>");
			sb2.append(lineb);
		});
		sb2.append("</table></div>");
		
		StringBuilder sb = new StringBuilder();
		rrsSorted1.forEach(rr->sb.append("<div style='width:100%;'><h3>"+rr.name+"</h3><div style='width:100%;'>"+rr.rankData+"</div></div>"));
		
		
		sendMail(new String[]{"今日银行股分析"}, sb2.append(sb));
		
	}
	
	private static RankReport calculateToday(String code)
			throws IOException, Exception, JsonProcessingException, AddressException, MessagingException {
		File basefile = new File("/ycoko/work/others/stocks/dailyrank/"+code+".baseline.csv");
		List<String> lines = FileUtils.readLines(basefile);
//		List<String> lines = FileUtils.readLines(new File("C:/Users/84854/Downloads/stock/src/main/resources/600016.baseline.20110101-20190907.csv"));
		Map<String,List<Double>> historicalData = new HashMap<String,List<Double>>();
		Map<Integer,String> indexMap = new HashMap<Integer,String>();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(new Date());
		boolean containsTodayData = false;
		if(lines.size()>0){
			for(int i=0;i<lines.size();i++){
				String[] lineParts = lines.get(i).replace("-", "").replace("E","e").split(",");
				if(lineParts[1].equals(dateStr.replace("-", ""))){
					containsTodayData = true;
				}
				if(i==0){
					//process header
					for(int j = 0; j < lineParts.length; j++){
						indexMap.put(j,lineParts[j]);
					}
				}else{
					for(int j = 0; j < lineParts.length; j++){
						String head = indexMap.get(j);
						List<Double> data= historicalData.getOrDefault(head, new ArrayList<Double>());
						data.add(Double.parseDouble(lineParts[j])>100?0:Double.parseDouble(lineParts[j]));
						historicalData.put(head, data);
					}
					//Hi-low
					Double hilowDiff= Double.parseDouble(lineParts[4])-Double.parseDouble(lineParts[5]);
					//TClose-YClose
					Double tyDiff= Double.parseDouble(lineParts[6])-Double.parseDouble(lineParts[2]);
					//closePrice
					//turnoverRate
					List<Double> data= historicalData.getOrDefault("hilowDiff", new ArrayList<Double>());
					data.add(hilowDiff);
					historicalData.put("hilowDiff", data);
					
					List<Double> data2= historicalData.getOrDefault("tyDiff", new ArrayList<Double>());
					data2.add(tyDiff);
					historicalData.put("tyDiff", data2);
					
				}
			}
		}
		
		
		Iterator<Entry<String, List<Double>>>  iter = historicalData.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, List<Double>> entry =iter.next();
			Collections.sort(entry.getValue());
		}
		
		System.out.println("getting daily info from Dongfangcaifu for:"+code);
		DailyInfo info = new DongFangCaiFu().getDailyInfo(code);//600016
		
		
		List<Rank> all = new ArrayList<Rank>();
		Rank rankCurPrice = createItem(historicalData, "今日价格",StockMeta.closePrice, Double.parseDouble(info.currentPrice));
		Rank rankHiLowDiff = createItem(historicalData, "日高低差",StockMeta.hilowDiff, Double.parseDouble(info.highestPrice)-Double.parseDouble(info.lowestPrice));
		Rank rankTyDiff = createItem(historicalData, "日涨跌额",StockMeta.tyDiff, Double.parseDouble(info.currentPrice)-Double.parseDouble(info.yesterdayClosedPrice));
		Rank rankTurnoverRate = createItem(historicalData, "换手率(%)",StockMeta.turnoverRate, Double.parseDouble(info.exchangeRatePercent)*1.000001/100);
		rankTurnoverRate.cur = rankTurnoverRate.cur * 100;
		rankTurnoverRate.min = rankTurnoverRate.min * 100;
		rankTurnoverRate.max = rankTurnoverRate.max * 100;
		
		all.add(rankCurPrice);
		all.add(rankHiLowDiff);
		all.add(rankTyDiff);
		all.add(rankTurnoverRate);
		

//		FileUtils.write(new File("C:/Users/84854/Downloads/"+dateStr+".dat"), new ObjectMapper().writeValueAsString(all));
		FileUtils.write(new File("/ycoko/work/others/stocks/dailyrank/report."+code+"."+dateStr+".dat"), new ObjectMapper().writeValueAsString(all));
//		String newLine = ",tradeDate,preClosePrice,openPrice,highestPrice,lowestPrice,closePrice,negMarketValue,turnoverValue,dealAmount,turnoverRate";
		if(!containsTodayData){
			String lastline = lines.get(lines.size()-1).replace("-", "").replace("E","e");
			String newLine2 = (lines.size()-1)+","+dateStr+","+info.yesterdayClosedPrice+","+info.todayOpenPrice+","+info.highestPrice+","+info.lowestPrice
					+","+info.currentPrice+",-1,-1,-1,"+String.format("%.4f", (Double.parseDouble(info.exchangeRatePercent)*1.000001/100))+"\n";
			if(!lastline.equals(newLine2)){
				FileUtils.write(basefile, newLine2, true);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		all.stream().forEach(item->sb.append(item.toTable()));
		RankReport rr = new RankReport(RankReport.nameMap.get(code)+code,code, sb.toString(),all);
		
		
		return rr;
//		sendMail(new String[]{"民生银行"+dateStr}, sb);
	}


	private static Rank createItem(Map<String, List<Double>> historicalData, String name,String key, Double cur) {
		int firstMatchIdx = 0;
		List<Double> sortedData =historicalData.get(key);
		for(int i = 0; i < sortedData.size(); i++){
			Double d = sortedData.get(i);
			if(d>cur){
				firstMatchIdx = i;
				break;
			}
		}
		
		double currentPosition = 1.000001*firstMatchIdx/sortedData.size()*100;
		Rank rank= new Rank(name,sortedData.get(0),sortedData.get(sortedData.size()-1),cur,currentPosition);
		return rank;
	}


	public static void sendMail(String[] keywords, StringBuilder content) throws AddressException, MessagingException {
		final Properties props = new Properties();
        /*
         * 可用的属性： mail.store.protocol / mail.transport.protocol / mail.host /
         * mail.user / mail.from
         */
        // 表示SMTP发送邮件，需要进行身份验证
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.163.com");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.port", "465");
//        props.put("mail.smtp.port", "587");
        props.put("mail.user", "babamamahenry@163.com");
        props.put("mail.password", "19881217565x");

        // 发件人的账号
        props.put("mail.user", "ycoko_com@163.com");
        // 访问SMTP服务时需要提供的密码
        props.put("mail.password", "2wsxZaq1");
        
        

        // 构建授权信息，用于进行SMTP进行身份验证
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // 用户名、密码
                String userName = props.getProperty("mail.user");
                String password = props.getProperty("mail.password");
                return new PasswordAuthentication(userName, password);
            }
        };
        // 使用环境属性和授权信息，创建邮件会话
        Session mailSession = Session.getInstance(props, authenticator);
        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        // 设置发件人
        InternetAddress form = new InternetAddress(
                props.getProperty("mail.user"));
        message.setFrom(form);

        // 设置收件人
        InternetAddress to = new InternetAddress("848549385@qq.com");
        message.setRecipient(RecipientType.TO, to);

        // 设置抄送
//        InternetAddress cc = new InternetAddress("luo_aaaaa@yeah.net");
//        message.setRecipient(RecipientType.CC, cc);

        // 设置密送，其他的收件人不能看到密送的邮件地址
//        InternetAddress bcc = new InternetAddress("aaaaa@163.com");
//        message.setRecipient(RecipientType.CC, bcc);

        // 设置邮件标题
        message.setSubject("股票消息:"+StringUtil.join(Arrays.asList(keywords), ",")+"["+new Date().toLocaleString()+"]");
       
//        // 设置邮件的内容体
//        message.setContent(content.toString(), "text/html;charset=UTF-8");
//
//        // 发送邮件
//        Transport.send(message);
        
        // MiniMultipart类是一个容器类，包含MimeBodyPart类型的对象
        Multipart mainPart = new MimeMultipart();
        // 创建一个包含HTML内容的MimeBodyPart
        BodyPart html = new MimeBodyPart();
        // 设置HTML内容
        html.setContent(content.toString(), "text/html; charset=utf-8");
        mainPart.addBodyPart(html);
        // 将MiniMultipart对象设置为邮件内容
        message.setContent(mainPart);
        Transport.send(message);
	}

//	private static StringBuilder getNews(String keyword) throws Exception {
//		String urlStr = "http://news.baidu.com/ns?ct=1&rn=20&ie=utf-8&bs="+URLEncoder.encode(keyword)+"&rsv_bp=1&sr=0&cl=2&f=8&prevct=no&tn=news&word="+URLEncoder.encode(keyword);
//			
//		String html = SentencePatternUtil.getHtml(urlStr,null);
//		Document doc = Jsoup.parse(html);
//		Elements results = doc.select(".result .c-title a");
//		
//		StringBuilder content = new StringBuilder(); 
//				
//		boolean hasNews = false;
//		content.append("<h2 style='color:red;'>"+keyword+"相关百度搜索新闻</h2>");
//		for(Element result:results){
//			
//			String label = result.text();
//			String href = result.attr("href");
//			if(Tools.notEmpty(href) && !news.containsValue(href) && !news.containsKey(label)){
//				content.append("<a href='"+href+"'>"+label+"</a><br>");
//				news.put(label.trim(), href);
//				hasNews = true;
//			}
//		}
//		if(hasNews){
//			content.append("<p>--已更新</p>");
//		}else{
//			content.setLength(0);
//		}
//		return content;
//	}
}

 
 
 