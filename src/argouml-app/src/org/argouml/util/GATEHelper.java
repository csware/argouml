package org.argouml.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.argouml.application.Main;

public class GATEHelper {
    static private Cookie setUpCookie() {
        BasicClientCookie cookie = new BasicClientCookie("JSESSIONID",
                Main.sessionID);
        cookie.setPath("/");
        cookie.setVersion(1);
        cookie.setDomain(getGATEUrl().getHost());
        if (getGATEUrl().getProtocol().equals("https")) cookie.setSecure(true);
        return cookie;
    }

    static public URL getGATEUrl() {
        URL gateURL = null;
        try {
            gateURL = new URL(Main.servletPath);
        } catch (MalformedURLException e) {
        }
        return gateURL;
    }

    public static HttpEntity retrieveEntity(String servlet) {
        DefaultHttpClient client = new DefaultHttpClient();

        client.getCookieStore().addCookie(setUpCookie());
        try {
            HttpGet get = new HttpGet(Main.servletPath + servlet);
            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(
                    CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            System.out.println("executing request " + get.getRequestLine());
            HttpResponse response = client.execute(get);
            HttpEntity resEntity = response.getEntity();
            //System.out.println(response.getStatusLine());
            return resEntity;
        } catch (Exception e) {
        }
        return null;
    }

    public static String retrieve(String servlet) {
        HttpEntity resEntity = retrieveEntity(servlet);
        if (resEntity != null) {
            String result = "";
            try {
                result = EntityUtils.toString(resEntity);
            } catch (ParseException e) {
            } catch (IOException e) {
            }
            return result;
        } else {
            return "";
        }
    }


    public static void upload(String taskID, String sessionID, File file,
            String fileExtension, List<Integer> selectedPartners) throws ClientProtocolException, IOException,
        InterruptedException {
        URL gateURL = null;
        try {
            gateURL = new URL(Main.servletPath);
        } catch (MalformedURLException e) {
        }

        DefaultHttpClient client = new DefaultHttpClient();
        BasicClientCookie cookie = new BasicClientCookie("JSESSIONID",
                sessionID);

        cookie.setPath("/");
        cookie.setVersion(1);
        cookie.setDomain(gateURL.getHost());
        if (gateURL.getProtocol().equals("https")) cookie.setSecure(true);
        client.getCookieStore().addCookie(cookie);

        HttpPost post = new HttpPost(Main.servletPath
                + "/SubmitSolution?taskid=" + taskID);

        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(
                CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        MultipartEntity reqEntity2 = new MultipartEntity();

        for (Integer partnerID : selectedPartners) {
            reqEntity2.addPart("partnerid", new StringBody(String.valueOf(partnerID)));
        }

        FileBody bin = null;
        if (fileExtension.equals("png")) {
            bin = new FileBody(file, "loesung." + fileExtension, "image/png", "utf-8");
        } else {
            bin = new FileBody(file, "loesung." + fileExtension, "text/xml",
                    "utf-8");
        }

        // Wichtig. Timingproblem
        while (bin.getContentLength() != file.length()) {
            Thread.sleep(100);
        }
        // FileBody bin = new FileBody(file);
        reqEntity2.addPart("file", bin);

        post.setEntity(reqEntity2);
        System.out.println("executing request " + post.getRequestLine());
        HttpResponse response = client.execute(post);
        if (response.getFirstHeader("SID") != null) {
            Main.sID = response.getFirstHeader("SID").getValue();
        } else {
            throw new IOException("got no SID");
        }
        if (response.getFirstHeader("TID") != null)
            Main.testID = response.getFirstHeader("TID").getValue();

        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            System.out.println(EntityUtils.toString(resEntity));
        }
        if (resEntity != null) {
            resEntity.consumeContent();
        }
        httpclient.getConnectionManager().shutdown();
    }

    public static HttpEntity checkDate(String servlet) {
        DefaultHttpClient client = new DefaultHttpClient();

        client.getCookieStore().addCookie(setUpCookie());
        try {
            HttpHead head = new HttpHead(Main.servletPath + servlet);
            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(
                    CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            System.out.println("executing request " + head.getRequestLine());
            HttpResponse response = client.execute(head);
            HttpEntity resEntity = response.getEntity();
            //System.out.println(response.getStatusLine());
            return resEntity;
        } catch (Exception e) {
        }
        return null;
    }
}
