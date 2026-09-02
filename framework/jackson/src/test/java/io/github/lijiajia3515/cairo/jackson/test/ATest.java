package io.github.lijiajia3515.cairo.jackson.test;

import com.fasterxml.jackson.databind.ObjectMapper;

//@SpringBootTest
public class ATest {

	//	@Test
	public static void main(String[] args) {
		try {
			ObjectMapper om = new ObjectMapper();
			String s = om.writeValueAsString(new TestData());
			System.out.println(s);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
