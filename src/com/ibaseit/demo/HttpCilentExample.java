package com.ibaseit.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HttpCilentExample {

    private String cookies;
    private HttpClient client = HttpClientBuilder.create().build();
    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {

	String url = "https://merituspayment.com/merchants/frmLogin.aspx";
	String nextUrl = "https://merituspayment.com/merchants/web/SecureHomeForms/frmHome.aspx";

	// make sure cookies is turn on
	CookieHandler.setDefault(new CookieManager());

	HttpCilentExample http = new HttpCilentExample();

	//get req for login url
	String page = http.GetPageContent(url);

	List<NameValuePair> postParams = http.getFormParams(page, "42162","16Comcastic4");

	//post req for login form
	Header[] headersForLogin=http.sendPost(url, postParams);
	
	for (Header header : headersForLogin) {
		System.out.println("Key : " + header.getName()
                       + " ,   Value : " + header.getValue());

	}
	
	//get req for home page confirmation
	//String result = http.GetPageContent(nextUrl);
	//System.out.println(result);
	
	//get req for Chargeback Detail link
	String link = http.GetPageContent("https://merituspayment.com/merchants/web/SecureReportForms/frmChargebackDetail.aspx?ct=0&dt=0&rd=0&");
	System.out.println(link);
	List<NameValuePair> postParamsLink = http.getFormDateParams(link);
	
	//post req for excel
	Header[] headersForSearch=http.sendPostForExcel(postParamsLink);
	for (Header header : headersForSearch) {
		System.out.println("Key : " + header.getName()
                       + " ,   Value : " + header.getValue());

	}

	System.out.println("Done");
    }

    private Header[] sendPost(String url, List<NameValuePair> postParams)
	    throws Exception {

	HttpPost post = new HttpPost(url);

	// add header
	post.setHeader("Host", "merituspayment.com");
	post.setHeader("User-Agent", USER_AGENT);
	post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
	post.setHeader("Accept-Language", "en-US,en;q=0.8");
	post.setHeader("Cookie", getCookies());
	post.setHeader("Connection", "keep-alive");
	post.setHeader("Referer",
		"https://merituspayment.com/merchants/frmLogin.aspx");
	post.setHeader("Content-Type", "application/x-www-form-urlencoded");

	post.setEntity(new UrlEncodedFormEntity(postParams));

	HttpResponse response = client.execute(post);

	Header[] headers = response.getAllHeaders();

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
	return headers;
    }

    private String GetPageContent(String url) throws Exception {

	HttpGet request = new HttpGet(url);

	request.setHeader("User-Agent", USER_AGENT);
	request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	request.setHeader("Accept-Language", "en-US,en;q=0.8");

	HttpResponse response = client.execute(request);
	int responseCode = response.getStatusLine().getStatusCode();

	System.out.println("\nSending 'GET' request to URL : " + url);
	System.out.println("Response Code : " + responseCode);

	BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

	StringBuffer result = new StringBuffer();
	String line = "";
	while ((line = rd.readLine()) != null) {
	    result.append(line);
	}

	// set cookies
	setCookies(response.getFirstHeader("Set-Cookie") == null ? "" : response.getFirstHeader("Set-Cookie").toString());

	return result.toString();

    }

    public List<NameValuePair> getFormParams(String html, String username,
	    String password) throws UnsupportedEncodingException {

	System.out.println("Extracting form's data...");

	Document doc = Jsoup.parse(html);

	// Form id
	Element loginform = doc.getElementById("form1");
	Elements inputElements = loginform.getElementsByTag("input");

	List<NameValuePair> paramList = new ArrayList<NameValuePair>();

	for (Element inputElement : inputElements) {
	    String key = inputElement.attr("name");
	    String value = inputElement.attr("value");

	    if (key.equals("ctl00$ContentPlaceHolder1$txtLoginID"))
		value = username;
	    else if (key.equals("ctl00$ContentPlaceHolder1$txtPassword"))
		value = password;
	    
	    paramList.add(new BasicNameValuePair(key, value));

	}

	return paramList;
    }
    
    public List<NameValuePair> getFormDateParams(String html) throws UnsupportedEncodingException {

	System.out.println("Extracting form's data...");

	Document doc = Jsoup.parse(html);

	// Form id
	Element loginform = doc.getElementById("form1");
	Elements inputElements = loginform.getElementsByTag("input");

	List<NameValuePair> paramList = new ArrayList<NameValuePair>();

	for (Element inputElement : inputElements) {
	    String key = inputElement.attr("name");
	    String value = inputElement.attr("value");

	    if (key.equals("ContentPlaceHolder1_wdcFromDate_clientState"))
		value = "|0|012016-10-3-0-0-0-0||[[[[]],[],[]],[{},[]],\"012016-10-3-0-0-0-0\"]";
	    else if (key.equals("ContentPlaceHolder1_wdcToDate_clientState"))
		value = "|0|012016-10-18-0-0-0-0||[[[[]],[],[]],[{},[]],\"012016-10-18-0-0-0-0\"]";
	    else if (key.equals("ctl00$ContentPlaceHolder1$ddlCBType"))
		value = "-1";
	    else if (key.equals("ctl00$ContentPlaceHolder1$ddlTransType"))
		value = "-1";
	    else if (key.equals("ctl00$ContentPlaceHolder1$ddlCardType"))
		value = "-1";
	    else if (key.equals("ContentPlaceHolder1_wceTransAmount_clientState"))
		value = "|0|01||[[[[]],[],[]],[{},[]],\"01\"]";
	    else if (key.equals("ContentPlaceHolder1_wceTransAmount"))
		value = "";
	    else if (key.equals("ContentPlaceHolder1_wteAuthCode_clientState"))
		value = "|0|01||[[[[]],[],[]],[{},[]],\"01\"]";
	    else if (key.equals("ContentPlaceHolder1_wteAuthCodee"))
		value = "";
	    else if (key.equals("ctl00$ContentPlaceHolder1$tbxLastFour"))
		value = "";
	    else if (key.equals("ctl00$ContentPlaceHolder1$rdExport"))
		value = "0";
	    else if (key.equals("ctl00$ContentPlaceHolder1$cboPageSize"))
		value = "10";
	    else if (key.equals("ContentPlaceHolder1_window2_clientState"))
		value = "[[[[null,3,null,null,\"430px\",\"260px\",1,null,null,1,null,3]],[[[[[null,\"Adjustment Details\",null]],[[[[[]],[],[]],[{},[]],null],[[[[]],[],[]],[{},[]],null],[[[[]],[],[]],[{},[]],null]],[]],[{},[]],null],[[[[null,null,null,null]],[],[]],[{},[]],null]],[]],[{},[]],\"3,3,,,430px,260px,0\"]";
	   // else if (key.equals("_ig_def_dp_cal_clientState"))
		//value = "[[null,[],null],[{},[]],\"01,2016,10\"]";
	  //  else if (key.equals("ctl00$_IG_CSS_LINKS_"))
	//	value = "~/App_Themes/Blue/Blue.css|../../ig_res/Default/ig_monthcalendar.css|../../ig_res/Default/ig_dialogwindow.css|../../ig_res/Default/ig_texteditor.css|../../ig_res/Default/ig_shared.css";

	    paramList.add(new BasicNameValuePair(key, value));

	}

	return paramList;
    }

    
    private Header[] sendPostForExcel(List<NameValuePair> postParams)
	    throws Exception {

	HttpPost post = new HttpPost("https://merituspayment.com/merchants/web/SecureReportForms/frmChargebackDetail.aspx?ct=0&dt=0&rd=0");

	// add header
	post.setHeader("Host", "merituspayment.com");
	post.setHeader("User-Agent", USER_AGENT);
	post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
	post.setHeader("Accept-Encoding", "gzip, deflate, br");
	post.setHeader("Accept-Language", "en-US,en;q=0.8");
	post.setHeader("Cookie", getCookies());
	post.setHeader("Connection", "keep-alive");
	//post.setHeader("Content-Length", "13045");
	post.setHeader("Referer",
		"https://merituspayment.com/merchants/web/SecureReportForms/frmChargebackDetail.aspx?ct=0&dt=0&rd=0");
	post.setHeader("Upgrade-Insecure-Requests", "1");
	post.setHeader("Content-Type", "application/x-www-form-urlencoded");

	post.setEntity(new UrlEncodedFormEntity(postParams));

	HttpResponse response = client.execute(post);
	response.setHeader("Content-Type", "application/ms-excel; charset=utf-8");

	Header[] headers = response.getAllHeaders();

	int responseCode = response.getStatusLine().getStatusCode();

	System.out.println("\nSending 'POST' request to URL : https://merituspayment.com/merchants/web/SecureReportForms/frmChargebackDetail.aspx?ct=0&dt=0&rd=0");
	System.out.println("Post parameters : " + postParams);
	System.out.println("Response Code : " + responseCode);

	BufferedReader rd = new BufferedReader(new InputStreamReader(response
		.getEntity().getContent()));

	StringBuffer result = new StringBuffer();
	String line = "";
	while ((line = rd.readLine()) != null) {
	    result.append(line);
	}

	
	System.out.println("result for excel ;=;=;=;= "+result.toString());
	System.out.println();
	return headers;
    }
    public String getCookies() {
	return cookies;
    }

    public void setCookies(String cookies) {
	this.cookies = cookies;
    }

}
