# json-client-commons

a library for creating JSON RPC clients in Java

## Installation

The artifact can be installed using Maven
```bash
mvn install
```

## Usage

To make a JSON RPC client you will need to do the following things:

Extend `ca.uhn.model.json.BaseRequestParams` with a class containing the parameters for your request:
```java
import ca.uhn.model.json.BaseRequestParams;

public class RequestParams extends BaseRequestParams {
  public String id;  
}
```
Create a bean into which the result will be deserialized
```java
import java.util.Date;

public class Person {  
  public String name;
  public Integer height;
  public Date age;
}
```

Extend `ca.uhn.json.client.JsonClient` with your class

```java
import java.util.Date;
import ca.uhn.json.client.JsonClient;

public class MyClient extends JsonClient {
    
    //specify the type into which the response will be deserialized
    private static final Type PERSON = new TypeToken<Person>(){}.getType();
    //specify the name of the operation which will be called on the service
    private static final String OPERATION = "getPerson";
    
    public MyClient(String url) {
      super(url);
    }
    
    public Person getPerson(String id) throws Exception {
      RequestParams params = new RequestParams();
      params.id = id;      
      return callService(OPERATION, params, PERSON);
    }

    public static void main(String[] args) throws Exception {
  	  System.out.println(new MyClient("http://localhost:8080/test-service/json-service").getPerson("123"));
	}
}
```
The client will generate the following JSON when callService executes:
```javascript
POST /test-service/json-service HTTP/1.1
User-Agent: Jakarta Commons-HttpClient/3.1
Host: localhost:8080
Content-Length: 82
Content-Type: application/json; charset=UTF-8

{
  "jsonrpc": "2.0",
  "method": "getPerson",
  "params": {
    "id": "123"
  }
}
```
The service should reply with a response which looks like the following:
```javascript
HTTP/1.1 200 OK
X-Powered-By: Servlet/2.5
Server: Sun GlassFish Enterprise Server v2.1.1
Content-Type: application/json;charset=ISO-8859-1
Transfer-Encoding: chunked
Date: Wed, 10 Oct 2012 20:59:36 GMT

{
  "jsonrpc": "2.0",
  "result": {
    "name": "John",
    "height": 180,
    "age": "Oct 10, 2012 04:59:36 PM"
  }
}
```