package com.ibaseit.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebScrapingHttpClient2 {

    private HttpClient client = HttpClientBuilder.create().build();

    public static void main(String[] args) throws Exception {

	String url = "https://vimas.cynergydata.com/Vimas2.0/Login.aspx";
	HttpContext httpContext = new BasicHttpContext();
	httpContext.setAttribute(HttpClientContext.COOKIE_STORE,
		new BasicCookieStore());

	WebScrapingHttpClient2 http = new WebScrapingHttpClient2();

	// get request for login form
	String page = http.GetPageContent(url, httpContext);
	List<NameValuePair> postParams = http.getLoginFormParams(page);

	// post request for login form
	http.sendPostForLogin(postParams, url, httpContext);
	List<NameValuePair> postParamsLink = http.getExcelFormParams();

	// post request for excel download
	http.sendPostForExcel(postParamsLink, httpContext);

	System.out.println("Done");
    }

    private void sendPostForLogin(List<NameValuePair> postParams, String url,
	    HttpContext httpContext) throws Exception {

	HttpPost post = new HttpPost(url);
	post.setEntity(new UrlEncodedFormEntity(postParams));
	HttpResponse response = client.execute(post, httpContext);
	int responseCode = response.getStatusLine().getStatusCode();

	System.out.println("\nSending 'POST' request to URL : " + url);
	System.out.println("Post parameters : " + postParams);
	System.out.println("Response Code : " + responseCode);

	BufferedReader rd = new BufferedReader(new InputStreamReader(response
		.getEntity().getContent()));
	StringBuffer result = new StringBuffer();
	String line = "";
	while ((line = rd.readLine()) != null) {
	    result.append(line);
	}
	System.out.println(result.toString());
    }

    private String GetPageContent(String url, HttpContext httpContext)
	    throws Exception {

	HttpGet request = new HttpGet(url);
	HttpResponse response = client.execute(request, httpContext);
	int responseCode = response.getStatusLine().getStatusCode();

	System.out.println("\nSending 'GET' request to URL : " + url);
	System.out.println("Response Code : " + responseCode);

	BufferedReader rd = new BufferedReader(new InputStreamReader(response
		.getEntity().getContent()));
	StringBuffer result = new StringBuffer();
	String line = "";
	while ((line = rd.readLine()) != null) {
	    result.append(line);
	}
	return result.toString();
    }

    public List<NameValuePair> getLoginFormParams(String html)
	    throws UnsupportedEncodingException {

	System.out.println("Extracting form's data...");
	List<NameValuePair> paramList = new ArrayList<NameValuePair>();

	for (Element login : Jsoup.parse(html).getElementsByTag("form")) {
	    for (Element inputElement : login.getElementsByTag("input")) {
		String key = inputElement.attr("name");
		String value = inputElement.attr("value");
		if (key.equals("Username"))
		    value = "3899000002598314";
		else if (key.equals("Password"))
		    value = "2comcastic27";
		paramList.add(new BasicNameValuePair(key, value));
	    }
	}
	return paramList;
    }

    public List<NameValuePair> getExcelFormParams() {

	List<NameValuePair> paramList = new ArrayList<NameValuePair>();

	paramList
		.add(new BasicNameValuePair(
			"__VIEWSTATE",
			"/wEPDwUJNTgwMDk2MzUyZBgBBQlHcmlkVmlldzEPPCsADAEIAgFkO749DL8/p3L9GZ04yuFdeFgaC3TrVqo5VrI5PHufMnU="));
	paramList
		.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", "C7DA1541"));
	paramList
		.add(new BasicNameValuePair(
			"__EVENTVALIDATION",
			"/wEWCwLAm+mdDQLlqpO0DwLM76k/AqzEjO8KAremvIMPApm9ztsKAuKX7J8MAoro7rgGAvqm5LULAtmm/tsBArGw/wg/bT8CxTDf6h2FxmKZ++S2A2mNAAvELCa5bRhj/DQAOQ=="));
	paramList.add(new BasicNameValuePair("FromDate", "9/12/2016"));
	paramList.add(new BasicNameValuePair("ToDate", "10/20/2016"));
	paramList.add(new BasicNameValuePair("FilterByDate", "ReceivedDate"));
	paramList.add(new BasicNameValuePair("OrigRefNum", ""));
	paramList.add(new BasicNameValuePair("ButtonExport", "Export"));
	paramList.add(new BasicNameValuePair("ExportToExcel", "1"));
	paramList.add(new BasicNameValuePair("SelectedPageCtrl", "0"));
	paramList.add(new BasicNameValuePair("SortBy", ""));

	return paramList;
    }

    private Header[] sendPostForExcel(List<NameValuePair> postParams,
	    HttpContext httpContext) throws Exception {
	HttpPost post = new HttpPost(
		"https://vimas.cynergydata.com/Vimas2.0/Merchant/Report_Chargebacks.aspx");

	post.setEntity(new UrlEncodedFormEntity(postParams));
	HttpResponse response = client.execute(post, httpContext);
	Header[] headers = response.getAllHeaders();
	int responseCode = response.getStatusLine().getStatusCode();

	System.out
		.println("\nSending 'POST' request to URL : https://vimas.cynergydata.com/Vimas2.0/Merchant/Report_Chargebacks.aspx");
	System.out.println("Post parameters : " + postParams);
	System.out.println("Response Code : " + responseCode);

	HttpEntity entity1 = response.getEntity();
	if (entity1 != null) {
	    System.out.println("Entity isn't null");

	    InputStream is = entity1.getContent();
	    String filePath = "D:\\Downloads\\CHARGEBACK_SHEET1.xls";
	    FileOutputStream fos = new FileOutputStream(new File(filePath));

	    byte[] buffer = new byte[5600];
	    int inByte;
	    while ((inByte = is.read(buffer)) > 0)
		fos.write(buffer, 0, inByte);

	    is.close();
	    fos.close();
	}
	System.out.println("Excel Received..");
	return headers;

    }

}
