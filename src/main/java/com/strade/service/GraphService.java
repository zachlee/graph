package com.strade.service;

import io.javalin.Context;

public class GraphService {
	public static void aboutPage( Context ctx ) { ctx.result( "studentrade-graph" ); }
}
