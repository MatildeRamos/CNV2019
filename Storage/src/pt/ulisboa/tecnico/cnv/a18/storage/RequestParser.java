package pt.ulisboa.tecnico.cnv.a18.storage;

public class RequestParser {
    private String requestReceived;
    private Request request;
    String example = "http://localhost:8001/climb?w=512&h=512&x0=0&x1=512&y0=0&y1=512&xS=0&yS=0&s=BFS&i=datasets/RANDOM_HILL_512x512_2019-02-27_09-46-42.dat";

    public RequestParser(String requestReceived){
        this.requestReceived = requestReceived;
    }

    public Request parseRequest(String id){
        String[] parameters = requestReceived.split("&");
        Request request = new Request();
        request.setKey(id);
        int width = 0, height = 0;
        for (String p : parameters){
            String[] div = p.split("=");
                switch (div[0]){
                    case "h":
                        request.setHeight(Integer.parseInt(div[1]));
                        break;
                    case "w":
                        request.setWidth(Integer.parseInt(div[1]));
                        break;
                    case "x0":
                        request.setX0(Integer.parseInt(div[1]));
                        break;
                    case "x1":
                        request.setX1(Integer.parseInt(div[1]));
                        break;
                    case "y0":
                        request.setY0(Integer.parseInt(div[1]));
                        break;
                    case "y1":
                        request.setY1(Integer.parseInt(div[1]));
                        break;
                    case "s":
                        request.setStrategy(div[1]);
                        break;
                    case "i":
                        request.setImage(div[1]);
                        break;
                }
        }
        return request;
    }
}
