package pt.ulisboa.tecnico.cnv.a18.storage.db;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import pt.ulisboa.tecnico.cnv.a18.storage.Request;

@DynamoDBTable(tableName = "MetricsStorage")
public class MetricsStorage{
    private String key;
    private Request request;
    private long methodsNumber;
    private int area;
    private long estimatedMethodsNumber;
    private String strategy;


    public MetricsStorage(Request request){
        this.key = request.getKey();
        this.request = request;
        this.area = request.getHeight() * request.getWidth();
        this.strategy = request.getStrategy();

    }

    public MetricsStorage(){}

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

    @DynamoDBAttribute(attributeName = "area")
    public void setArea(int area) {
        this.area = area;
    }

    public int getArea() {
        return area;
    }

    @DynamoDBAttribute(attributeName = "methodsNumber")
    public long getMethodsNumber() {
        return methodsNumber;
    }

    public void setMethodsNumber(long methodsNumber) {
        this.methodsNumber = methodsNumber;
    }

    @DynamoDBAttribute(attributeName = "estimatedMethodsNumber")
    public long getEstimatedMethodsNumber() {
        return estimatedMethodsNumber;
    }

    public void setEstimatedMethodsNumber(long estimatedMethodsNumber) {
        this.estimatedMethodsNumber = estimatedMethodsNumber;
    }

    @DynamoDBAttribute(attributeName = "strategy")
    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
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
