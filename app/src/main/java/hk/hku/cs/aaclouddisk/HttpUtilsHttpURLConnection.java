package hk.hku.cs.aaclouddisk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtilsHttpURLConnection {

    public static String BASE_URL= "http://123.207.6.234:8080/aacloud";

    /*
     * urlStr: Controller
     * parameters: KV pair
     * return: Json String or Page(xml/html)
     */
    public static String postByHttp(String urlStr, Map<String, String> parameters){
        StringBuilder sb = new StringBuilder();
        OutputStream outputStream = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try{
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(true);

            outputStream = connection.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            writer.write(transformParams(parameters));

            writer.flush();
            writer.close();
            outputStream.close();

            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String temp;
                while((temp = reader.readLine()) != null){
                    sb.append(temp);
                }
                reader.close();
            }else{
                return "connection error:" + connection.getResponseCode();
            }

            connection.disconnect();
        }catch (Exception e){
            return e.toString();
        }
        return sb.toString();
    }

    /**
     * transform map<K, V> to "key1=value1&key2=value2"
     * @param map Param Key Value Pair
     * @return String of Url Params
     * @throws UnsupportedEncodingException
     */
    private static String transformParams(Map<String, String> map) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        for(Map.Entry<String, String> entry: map.entrySet()){
            if(isFirst)
                isFirst = false;
            else
                sb.append("&");

            sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return sb.toString();
    }
}
