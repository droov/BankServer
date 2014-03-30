package server;

import java.io.IOException;

import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

/*
 * Class that obtains live forex rates from Yahoo  
 */
@SuppressWarnings("deprecation")
public class CurrencyConverter {
	// Method to return the exchange rate between two currencies
	public float convertCurrency(String sourceCurrency,
			String destinationCurrency) throws IOException {
		@SuppressWarnings("resource")
		HttpClient httpclient = new DefaultHttpClient();
		// Make a httpget request to yahoo's server with source and destination
		// currencies in URL
		HttpGet httpGet = new HttpGet("http://quote.yahoo.com/d/quotes.csv?s="
				+ sourceCurrency + destinationCurrency + "=X&f=l1&e=.csv");
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		// Obtain response from website
		String responseBody = httpclient.execute(httpGet, responseHandler);
		httpclient.getConnectionManager().shutdown();
		return Float.parseFloat(responseBody);
	}
}
