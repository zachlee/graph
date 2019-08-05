package com.strade.resource;

import com.strade.service.GraphService;
import io.javalin.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;


public class GraphResourceTest {
	@Mock
	static HttpServletRequest request;
	@Mock
	static HttpServletResponse response;

	static Context testContext;

	static GraphService underTest;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Before
	public void resetContext() {
//		testContext = ContextUtil.init(request, response);
	}

	@Test
	public void addTextbookReturnsStatus200() throws IOException {

	}
}
