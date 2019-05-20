package pt.ulisboa.tecnico.cnv.a18.storage.db;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import pt.ulisboa.tecnico.cnv.a18.storage.Request;

@DynamoDBTable(tableName = "TableName")
public class RequestEntry {
    String key;
    Request request;

    public RequestEntry(Request request){
        this.key = request.getKey();
        this.request = request;
    }

    public RequestEntry(){ }

    @DynamoDBHashKey //TODO se ser erro ver isto
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @DynamoDBTypeConverted(converter = RequestConverter.class)
    @DynamoDBAttribute(attributeName = "request")
    public Request getRequestAttribute() {
        return request;
    }

    public void setRequestAttribute(Request request) {
        this.request = request;
    }

    // Converts the complex type DimensionType to a string and vice-versa.
    static public class RequestConverter implements DynamoDBTypeConverter<String, Request> {

        @Override
        public String convert(Request object) {
            Request request = object;
            String res = null;
            try {
                if (request != null) {
                    res = String.format("%s", request.getKey());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        public Request unconvert(String s) {

            Request request = new Request();
            try {
                if (s != null && s.length() != 0) {
                    String[] data = s.split("x");
                    request.setKey(data[0].trim());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return request;
        }
    }

}
