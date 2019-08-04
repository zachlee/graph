package com.strade.resource;

import com.strade.dao.GraphDao;
import com.strade.domain.Textbook;
import com.strade.service.GraphService;
import io.javalin.Context;
import io.javalin.core.util.ContextUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

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
