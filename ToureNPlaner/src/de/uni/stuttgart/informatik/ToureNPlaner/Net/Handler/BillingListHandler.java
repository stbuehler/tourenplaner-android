package de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.BillingItem;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.JacksonManager;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.JacksonManager.ContentType;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Observer;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;

import java.io.InputStream;

public class BillingListHandler extends ConnectionHandler {
	private int limit;
	private int offset;

	public BillingListHandler(Observer listener, Session session, int limit, int offset) {
		super(listener, session);
		this.limit = limit;
		this.offset = offset;
	}


	@Override
	protected String getSuffix() {
		return "/listrequests?details=nojson&limit=" + limit + "&offset=" + offset;
	}

	@Override
	protected boolean isPost() {
		return false;
	}

	@Override
	protected Object handleInput(ContentType type, InputStream inputStream) throws Exception {
		ObjectMapper mapper = JacksonManager.getMapper(type);
		return BillingItem.parse(mapper.readValue(inputStream, JsonNode.class));
	}
}
