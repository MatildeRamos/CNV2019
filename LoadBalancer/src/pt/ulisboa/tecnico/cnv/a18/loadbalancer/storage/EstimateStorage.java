package pt.ulisboa.tecnico.cnv.a18.loadbalancer.storage;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import pt.ulisboa.tecnico.cnv.a18.storage.Request;

@DynamoDBTable(tableName = "EstimateStorage")
public class EstimateStorage {
    private int methodsPerArea;
    private Request request;
    private String strategy;
    private int area;


    public EstimateStorage(){}

    public EstimateStorage(Request request){
        this.request = request;
        this.strategy = request.getStrategy();
        this.area = request.getHeight() * request.getWidth();
        this.methodsPerArea = 0;

    }

    @DynamoDBHashKey
    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    @DynamoDBRangeKey
    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    @DynamoDBAttribute(attributeName = "methodsPerArea")
    public int getMethodsPerArea() {
        return methodsPerArea;
    }

    public void setMethodsPerArea(int methodsPerArea) {
        this.methodsPerArea = methodsPerArea;
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
