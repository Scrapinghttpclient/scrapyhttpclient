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

public class WebPageScraping {

	private HttpClient client = HttpClientBuilder.create().build();

	public static void main(String[] args) throws Exception {
		long startTotalTime = System.currentTimeMillis();
		String url = "https://merituspayment.com/merchants/frmLogin.aspx";

		// Session Handling
		CookieStore cookieStore = new BasicCookieStore();
		HttpContext httpContext = new BasicHttpContext();
		httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

		WebPageScraping http = new WebPageScraping();

		// Step1 : GET Request for login page
		String page = http.GetPageContent(url, httpContext);

		// Step2 : Setting Userid and password for login page
		List<NameValuePair> postParams = http.setFormParamsForLogin(page, "42162",
				"16Comcastic4");
		long startTime = System.currentTimeMillis();
		// Step3 : post request for login page
		http.sendPost(url, postParams, httpContext);

		long endTime = System.currentTimeMillis();
		System.out.println("Login Time : "+(endTime-startTime));
		// Step4 : Setting Form parameters for Charge Back xls download
		List<NameValuePair> postParamsLink = http.setFormDateParams();
		startTime = System.currentTimeMillis();
		// Step5 : Post Request for XLS download
		http.sendPostForExcel(postParamsLink, httpContext);
		endTime = System.currentTimeMillis();
		System.out.println("XLS Download Time : "+(endTime-startTime));
		System.out.println("Done");
		long endTotalTime = System.currentTimeMillis();
		System.out.println("Total Time : "+(endTotalTime - startTotalTime));
	}

	private Header[] sendPost(String url, List<NameValuePair> postParams,
			HttpContext httpContext) throws Exception {

		HttpPost post = new HttpPost(url);

		post.setEntity(new UrlEncodedFormEntity(postParams));

		HttpResponse response = client.execute(post, httpContext);

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

	public List<NameValuePair> setFormParamsForLogin(String html,
			String username, String password) throws UnsupportedEncodingException {

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

	public List<NameValuePair> setFormDateParams() {
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair("__EVENTTARGET",
				"ctl00$ContentPlaceHolder1$btnExpExcelChargebackDay"));
		paramList.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
		paramList.add(new BasicNameValuePair("__LASTFOCUS", ""));
		paramList
				.add(new BasicNameValuePair(
						"__VIEWSTATE",
						"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"));
		paramList.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", "78FC0077"));
		paramList.add(new BasicNameValuePair("__VIEWSTATEENCRYPTED", ""));
		paramList
				.add(new BasicNameValuePair(
						"__PREVIOUSPAGE",
						"z-bjoV_pmfCLfDctEXPThYp83YjxdASmWV1tJivrxSWJv1Oe9Dde3BLbBzEubqM4ZGZMe9DUCCOw1Kns8ewhPClqhNLVtUMHpDTg0oC-qd2_QD4puYZlzvyslRGb4oHIb44zemiJzg085VlGkDTRKAJWDW7JggQ81K7TBDMEsAQ1"));
		paramList
				.add(new BasicNameValuePair(
						"ContentPlaceHolder1_wdcFromDate_clientState",
						"|0|012016-08-18-0-0-0-0||[[[[]],[],[]],[{},[]],\"012016-08-18-0-0-0-0\"]"));
		// paramList.add(new
		// BasicNameValuePair("ContentPlaceHolder1_wdcFromDate_clientState","|0|012016-10-19-0-0-0-0||[[[[]],[],[]],[{},[]],\"012016-10-19-0-0-0-0\"]"));
		paramList
				.add(new BasicNameValuePair(
						"ContentPlaceHolder1_wdcToDate_clientState",
						"|0|012016-10-19-0-0-0-0||[[[[]],[],[]],[{},[]],\"012016-10-19-0-0-0-0\"]"));
		paramList.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$ddlCBType",
				"-1"));
		paramList.add(new BasicNameValuePair(
				"ctl00$ContentPlaceHolder1$ddlTransType", "-1"));
		paramList.add(new BasicNameValuePair(
				"ctl00$ContentPlaceHolder1$ddlCardType", "-1"));
		paramList.add(new BasicNameValuePair(
				"ContentPlaceHolder1_wceTransAmount_clientState",
				"|0|01||[[[[]],[],[]],[{},[]],\"01\"]"));
		paramList.add(new BasicNameValuePair("ContentPlaceHolder1_wceTransAmount",
				""));
		paramList.add(new BasicNameValuePair(
				"ContentPlaceHolder1_wteAuthCode_clientState",
				"|0|01||[[[[]],[],[]],[{},[]],\"01\"]"));
		paramList
				.add(new BasicNameValuePair("ContentPlaceHolder1_wteAuthCode", ""));
		paramList.add(new BasicNameValuePair(
				"ctl00$ContentPlaceHolder1$tbxLastFour", ""));
		paramList.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$rdExport",
				"1"));
		paramList.add(new BasicNameValuePair(
				"ctl00$ContentPlaceHolder1$cboPageSize", "10"));
		paramList
				.add(new BasicNameValuePair(
						"_ig_def_dp_cal_clientState",
						"[[[[null,3,null,null,\"430px\",\"260px\",1,null,null,1,null,3]],[[[[[null,\"Adjustment Details\",null]],[[[[[]],[],[]],[{},[]],null],[[[[]],[],[]],[{},[]],null],[[[[]],[],[]],[{},[]],null]],[]],[{},[]],null],[[[[null,null,null,null]],[],[]],[{},[]],null]],[]],[{},[]],\"3,3,,,430px,260px,0\"]"));
		paramList
				.add(new BasicNameValuePair(
						"ContentPlaceHolder1_window2_clientState",
						"[[[[null,3,null,null,\"430px\",\"260px\",1,null,null,1,null,3]],[[[[[null,\"Adjustment Details\",null]],[[[[[]],[],[]],[{},[]],null],[[[[]],[],[]],[{},[]],null],[[[[]],[],[]],[{},[]],null]],[]],[{},[]],null],[[[[null,null,null,null]],[],[]],[{},[]],null]],[]],[{},[]],\"3,3,,,430px,260px,0\"]"));
		paramList.add(new BasicNameValuePair("_ig_def_dp_cal_clientState",
				"[[null,[],null],[{},[]],\"01,2016,10\"]"));
		paramList
				.add(new BasicNameValuePair(
						"ctl00$_IG_CSS_LINKS_",
						"~/App_Themes/Blue/Blue.css|../../ig_res/Default/ig_monthcalendar.css|../../ig_res/Default/ig_dialogwindow.css|../../ig_res/Default/ig_texteditor.css|../../ig_res/Default/ig_shared.css"));
		return paramList;
	}

	private Header[] sendPostForExcel(List<NameValuePair> postParams,
			HttpContext httpContext) throws Exception {

		HttpPost post = new HttpPost(
				"https://merituspayment.com/merchants/web/SecureReportForms/frmChargebackDetail.aspx?ct=0&dt=0&rd=0");
		post.setEntity(new UrlEncodedFormEntity(postParams));
		HttpResponse response = client.execute(post, httpContext);

		Header[] headers = response.getAllHeaders();

		int responseCode = response.getStatusLine().getStatusCode();

		System.out
				.println("\nSending 'POST' request to URL : https://merituspayment.com/merchants/web/SecureReportForms/frmChargebackDetail.aspx?ct=0&dt=0&rd=0");
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);

		HttpEntity entity1 = response.getEntity();

		if (entity1 != null) {
			System.out.println("Entity isn't null");

			InputStream is = entity1.getContent();
			String filePath = "D:\\Downloads\\CHARGEBACK_SHEET.xls";
			FileOutputStream fos = new FileOutputStream(new File(filePath));

			byte[] buffer = new byte[5600];
			int inByte;
			while ((inByte = is.read(buffer)) > 0)
				fos.write(buffer, 0, inByte);

			is.close();
			fos.close();
		}

		return headers;

	}

}
