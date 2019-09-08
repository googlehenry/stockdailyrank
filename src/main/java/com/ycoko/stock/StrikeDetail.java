package com.ycoko.stock;

public class StrikeDetail {
	
	public String date;//代码
	public String code;//代码
	public String name;//名称
	
	public String orderIdx;
	public String time;
	public String price;
	public String amount;
	public String volume;
	public String type1;
	public String type2;
	public String dealCount;
	public String positionDiff;
	public String openPosition;
	public String closePosition;
	
	/*
	 * {"Time":"时间"}
{"Price":"价格"}
{"Amount":"成交额"}
{"Volume":"成交量"}
{"Type1":"交易类型1:平盘－0,内盘－1,外盘－2"}
{"Type2":"交易类型2:多开－1,空开－2,多平－3,空平－4,双开－5,多平－6,多换－7,空换－8"}
{"DealCount":"成交笔数"}
{"PositionDiff":"仓差"}
{"OpenPosition":"开仓量"}
{"ClosePosition":"平仓量"}
	 */
}
