package hk.hku.cs.aaclouddisk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class HttpUtilsHttpURLConnection {

    public static String BASE_URL= "http://123.207.6.234:8080/aacloud";

    /*
     * urlStr: 网址
     * parameters: 参数 KV pair
     * return: Json String or Page(xml or html)
     */
    public static String postByHttp(String urlStr, Map<String, String> parameters){
        StringBuilder sb = new StringBuilder();
        try{
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(true);

            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
            writer.write(transformParams(parameters));

            writer.flush();
            writer.close();
            outputStream.close();

            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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

    /*
     * urlStr: 网址
     * file: file to upload
     * return: Json String or Page(xml or html)
     */
    public static String uploadByHttp(String urlStr, File file){
        StringBuilder sb = new StringBuilder();
        try{
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(true);

            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
//            writer.write(transformParams(parameters));

            writer.flush();
            writer.close();
            outputStream.close();

            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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
     * @param map
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String transformParams(Map<String,String> map) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        for(Map.Entry<String, String> entry: map.entrySet()){
            if(isFirst)
                isFirst = false;
            else
                sb.append("&");

            sb.append(URLEncoder.encode(entry.getKey(),"UTF-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(),"UTF-8"));
        }
        return sb.toString();
    }
}
