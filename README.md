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

public class MyParams extends BaseRequestParams {
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
    
    public getPerson(String id) throws Exception {
      MyParams params = new MyParams();
      params.id = id;      
      return callService(OPERATION, params, PERSON);
    }
}
```

