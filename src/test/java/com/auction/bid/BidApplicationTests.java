package com.auction.bid;

import com.auction.bid.domain.product.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest

class BidApplicationTests {

	@Autowired
	private ProductService productService;

	@Test
	void register() {

	}

}
