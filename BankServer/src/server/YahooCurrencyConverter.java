package server;

import java.io.IOException;

import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

@SuppressWarnings("deprecation")
public class YahooCurrencyConverter implements CurrencyConverter {
    public float convert(String currencyFrom, String currencyTo) throws IOException {
        @SuppressWarnings("resource")
		HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://quote.yahoo.com/d/quotes.csv?s=" + currencyFrom + currencyTo + "=X&f=l1&e=.csv");
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        //String responseBody = httpclient.execute(httpGet, responseHandler);
        String responseBody = httpclient.execute(httpGet, responseHandler);
        httpclient.getConnectionManager().shutdown();
        return Float.parseFloat(responseBody);
    }

/*    public static void main(String[] args) {
        YahooCurrencyConverter ycc = new YahooCurrencyConverter();
        try {
            float current = ycc.convert("SGD", "USD");
            System.out.println(current);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
