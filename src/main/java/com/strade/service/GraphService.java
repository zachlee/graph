package com.strade.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strade.dao.GraphDao;
import com.strade.domain.Textbook;
import io.javalin.Context;

import java.io.IOException;

public class GraphService {

	private static ObjectMapper mapper = new ObjectMapper();
	static GraphDao graphDao = new GraphDao();

	public static void aboutPage( Context ctx ) { ctx.result( "studentrade-graph" ); }

	public static void addTextbook(Context context) throws IOException {
		Textbook textbook = mapper.readValue(context.body(), Textbook.class);
		graphDao.insertTextbook(textbook);
	}

	public static void removeTextbook(Context context){}

	public static void addTextbookRelationship(Context context){}

	public static void searchBook(Context context){}

	public static void transferBook(Context context){}

	public static void searchWishlist(Context context){}

	public static void addUser(Context context){}

	public static void removeUser(Context context){}
}
