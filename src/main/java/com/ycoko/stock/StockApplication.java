package com.ycoko.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StockApplication {

	public static void main(String[] args) throws Exception{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					FinancialTools.main(args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		
		SpringApplication.run(StockApplication.class, args);
	}

}
