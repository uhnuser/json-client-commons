package ca.uhn.json.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.uhn.model.json.Error;
import ca.uhn.model.json.Error.ErrorData;
import ca.uhn.model.json.Request;
import ca.uhn.model.json.BaseRequestParams;
import ca.uhn.model.json.exception.AuthorizationErrorException;
import ca.uhn.model.json.exception.InternalErrorException;
import ca.uhn.model.json.exception.InvalidParamsException;
import ca.uhn.model.json.exception.InvalidRequestException;
import ca.uhn.model.json.exception.LimitReachedException;
import ca.uhn.model.json.exception.MethodNotFoundException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonClient extends Client {
	private static final String JSON_RESULT = "result";

	private static class DateTimeSerializer implements JsonSerializer<Date> {
		public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
			SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
			return new JsonPrimitive(format.format(src));
		}
	}

	private static class DateTimeDeserializer implements JsonDeserializer<Date> {
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			try {
				String date = json.getAsJsonPrimitive().getAsString();
				if (date == null || date.length() == 0) return null;
				SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
				return format.parse(date);
			} catch (ParseException e) {
				throw new JsonParseException(e.getMessage());
			}
		}
	}

	public static Gson gson = new GsonBuilder()
								.setPrettyPrinting()
								.registerTypeAdapter(Date.class, new DateTimeSerializer())
								.registerTypeAdapter(Date.class, new DateTimeDeserializer())
								.registerTypeAdapter(java.sql.Date.class, new DateTimeSerializer())
								.registerTypeAdapter(java.sql.Date.class, new DateTimeDeserializer())
								.create();


	public JsonClient(String url, String clientId, String pass, String auditSourceId) {
		super(url, clientId, pass, auditSourceId);
	}

	protected void setDefaultParams(BaseRequestParams params) {
		params.setClientId(clientId);
		params.setClientPass(pass);
		params.setAuditSourceId(auditSourceId);
	}

	public static String toJsonString(Object o) {
		return gson.toJson(o).toString();
	}

	@SuppressWarnings("unchecked")
	public static <T> T fromJsonString(String jsonString, Type t) throws IOException, InternalErrorException, InvalidParamsException, InvalidRequestException, MethodNotFoundException {
		if(jsonString == null) return null;
		return (T)gson.fromJson(jsonString, t);
	}

	protected JsonElement callService(String operation, BaseRequestParams params) throws IOException, InternalErrorException, InvalidParamsException, InvalidRequestException, MethodNotFoundException, LimitReachedException, AuthorizationErrorException {
		if(params != null) setDefaultParams(params);
		Request request = new Request(operation, params);
		String data = gson.toJson(request);

		String jsonResponse = callService(data);
		JsonElement jsonTree = new JsonParser().parse(jsonResponse);
		JsonObject response = jsonTree.getAsJsonObject();

		ErrorData error = gson.fromJson(response.get("error"), Error.ErrorData.class);

		if (null != error) {
			String serviceError = "Received error from service: " + error.message;
			if (error.code == Error.INTERNAL_ERROR) {
				throw new InternalErrorException(serviceError);
			} else if (error.code == Error.INVALID_PARAMS) {
				throw new InvalidParamsException(serviceError);
			} else if (error.code == Error.INVALID_REQUEST) {
				throw new InvalidRequestException(serviceError);
			} else if (error.code == Error.METHOD_NOT_FOUND) {
				throw new MethodNotFoundException(serviceError);
			} else if (error.code == Error.LIMIT_REACHED_ERROR) {
				throw new LimitReachedException(serviceError);
			} else if (error.code == Error.AUTHORIZATION_ERROR) {
				throw new AuthorizationErrorException(serviceError);
			} else {
				throw new IOException(serviceError);
			}
		}

		return response.get(JSON_RESULT);
	}

	@SuppressWarnings("unchecked")
	protected <T> T callService(String operation, BaseRequestParams params, Type t) throws IOException, InternalErrorException, InvalidParamsException, InvalidRequestException, MethodNotFoundException, JsonParseException, LimitReachedException, AuthorizationErrorException {
		return (T)gson.fromJson(callService(operation, params), t);
	}

	public String toString() {
		return "{:url " + this.url + ", :user " + clientId + ", :pass " + pass + ", :auditSourceID " + auditSourceId + "}";
	}

}
