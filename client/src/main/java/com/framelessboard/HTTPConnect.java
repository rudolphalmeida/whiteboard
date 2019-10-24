package com.framelessboard;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HTTPConnect {

    public CloseableHttpClient httpclient ;;
    public String url = "http://api.boyang.website/whiteboard";
    public String token;
    public String Code = "471e61cd2878317b204b878dfc918d2b";
    //Code: 471e61cd2878317b204b878dfc918d2b (which is the 32 bit MD5 hash of "frameless")
    public String username;
    public String currentMananger;
    public JSONArray onlineUserList;
    public JSONArray activeUserList;
    public JSONArray waitingUserList;
    public boolean isManager = false;
    public int currentCanvas = -1;
    public int currentChat = -1;


    private DrawController drawController;
    private Artist artist;
    private Chat chat;



    public Thread updateThread;
    public Task<Void> updateTask;
    boolean stopUpdate = false;

    HTTPConnect(){
        httpclient = HttpClients.createDefault();

        updateThread = getUpdateThread();
    }

    public void setUsername(String username){
        if (username != null){
            this.username = username;
        }else{
            System.out.println("User name illegal");
        }
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }
    public void setDrawController(DrawController drawController){
        this.drawController = drawController;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public void testConnect(){
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try{
            response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getToken(){
        //Get the access token.
        try{
            String uri = url + "/accesstoken";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("User", username)
                    .setHeader("Code", Code)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
                token = response.getFirstHeader("Access-Token").getValue();
                System.out.println("Access-Token: "+ token);
            }
            else {
                System.out.println(request.toString());
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getManager(){
        //To find out who is the manager right now
        try{
            String uri = url + "/manager";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                //System.out.println(content);
                currentMananger = content.getString("result");
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void postMananger(){
        //To register the current user as the manager
        if (currentMananger == null){
            System.out.println("Can not get current manager, retry");
        }
        else if (currentMananger.equals("null")){
            System.out.println("You are the first one");
            //Set yourself the mananger
            try{
                String uri = url + "/manager";
                HttpUriRequest request = RequestBuilder.post()
                        .setUri(uri)
                        .setHeader("Access-Token", token)
                        .build();
                CloseableHttpResponse response = httpclient.execute(request);
                if (response.getStatusLine().getStatusCode() == 200){
                    JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    isManager = true;
                    System.out.println(content);
                }
                else {
                    System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                    JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    System.out.println(content);
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else if (username.equals(currentMananger)){
            isManager = true;
            System.out.println("You are the mananger");
        }else{
            System.out.println("There is a mananger:" + currentMananger);
        }
    }

    public void deleteManager(){
        //To unregister the current user as the manager
        if (username.equals(currentMananger)){
            try{
                String uri = url + "/manager";
                HttpUriRequest request = RequestBuilder.delete()
                        .setUri(uri)
                        .setHeader("Access-Token", token)
                        .build();
                CloseableHttpResponse response = httpclient.execute(request);
                if (response.getStatusLine().getStatusCode() == 200){
                    JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    System.out.println(content);
                }
                else {
                    System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                    JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    System.out.println(content);
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getOnlineUsers(){
        //To get the list of all online users
        try{
            String uri = url + "/onlineusers";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
                if (content.get("result").equals("null")){
                    System.out.println("No thing new");
                }else{
                    onlineUserList = content.getJSONArray("result");
                    System.out.println(onlineUserList);
                }
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                onlineUserList = new JSONArray();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerOnline(){
        //To register the current user as an online user
        try{
            String uri = url + "/onlineusers";
            HttpUriRequest request = RequestBuilder.post()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteOnline(){
        //To unregister the current user as an online user
        try{
            String uri = url + "/onlineusers";
            HttpUriRequest request = RequestBuilder.delete()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getActiveUser(){
        //To get the list of all active users
        try{
            String uri = url + "/activeusers";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
                activeUserList = content.getJSONArray("result");
                System.out.println(activeUserList);
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
                activeUserList = new JSONArray();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerActive(String user){
        //To register some user as an active user
        try{
            String uri = url + "/activeusers/" + user;
            HttpUriRequest request = RequestBuilder.post()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteActive(String user){
        //To unregister some user as an active user
        try{
            String uri = url + "/activeusers/" + user;
            HttpUriRequest request = RequestBuilder.delete()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteActiveSelf(){
        try{
            String uri = url + "/activeusers/" + username;
            HttpUriRequest request = RequestBuilder.delete()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getCanvas(){
        //To get all data of the canvas
        try{
            String uri = url + "/canvas";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
                if (content.get("result").equals(null)){
                    System.out.println("No thing new");
                }
                else{
                    JSONArray result = content.getJSONArray("result");
                    System.out.println(result);
                    if (result.length() != 0){
                        return result;
//                        for (int i = 0; i < result.length(); i++){
//                            JSONObject drawing = result.getJSONObject(i).getJSONObject("request");
//                            System.out.println(drawing);
//                        }
                    }
                }
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public JSONArray getCanvas(int mid){
        //To get updated data of the canvas
        try{
            String uri = url + "/canvas/" + mid ;
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
                if (content.get("result").equals(null)){
                    //Should not exist
                    System.out.println("No canvas exists");
                }
                else{
                    JSONArray result = content.getJSONArray("result");
                    System.out.println(result);
                    if (result.length() != 0){
                        return result;
//                        for (int i = 0; i < result.length(); i++){
//                            JSONObject drawing = result.getJSONObject(i).getJSONObject("request");
//                            System.out.println(drawing);
//                        }
                    }
                }
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }


    public void postCanvas(){
        //To initialize a canvas
        try{
            String uri = url + "/canvas";
            HttpUriRequest request = RequestBuilder.post()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void putCanvas(JSONObject json){
        //To update the canvas
        try{
            String uri = url + "/canvas";
            HttpUriRequest request = RequestBuilder.put()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .setEntity(new StringEntity(json.toString(), "UTF-8"))
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteCanvas(){
        //To delete the canvas
        try{
            String uri = url + "/canvas";
            HttpUriRequest request = RequestBuilder.delete()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getWaitingUsers(){
        //To get the list of all users who are waiting for manager's approval
        try{
            String uri = url + "/waitingusers";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
                waitingUserList = content.getJSONArray("result");
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
                waitingUserList = new JSONArray();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void postWaitingUsers(){
        //To register the current user as a waiting user
        try{
            String uri = url + "/waitingusers";
            HttpUriRequest request = RequestBuilder.post()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteWaitingUsers(String username){
        //To reject some user to be an active user
        try{
            String uri = url + "/waitingusers/" + username;
            HttpUriRequest request = RequestBuilder.delete()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserState(){
        //To reject some user to be an active user
        try{
            String uri = url + "/userstate";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
                String state = content.getString("result");
                return state;
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONArray getChatMessages(){
        //To reject some user to be an active user
        try{
            String uri = url + "/chatmessages";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
                if (content.get("result").equals(null)){
                    //Should not exist
                    System.out.println("No canvas exists");
                }
                else{
                    JSONArray result = content.getJSONArray("result");
                    System.out.println(result);
                    if (result.length() != 0){
                        return result;
//                        for (int i = 0; i < result.length(); i++){
//                            JSONObject drawing = result.getJSONObject(i).getJSONObject("request");
//                            System.out.println(drawing);
//                        }
                    }
                }
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public JSONArray getChatMessages(int mid){
        //To reject some user to be an active user
        try{
            String uri = url + "/chatmessages/" + mid;
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
                if (content.get("result").equals(null)){
                    //Should not exist
                    System.out.println("No canvas exists");
                }
                else{
                    JSONArray result = content.getJSONArray("result");
                    System.out.println(result);
                    if (result.length() != 0){
                        return result;
//                        for (int i = 0; i < result.length(); i++){
//                            JSONObject drawing = result.getJSONObject(i).getJSONObject("request");
//                            System.out.println(drawing);
//                        }
                    }
                }
            }
            else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }


    public void postChatMessages(JSONObject text){
        //To reject some user to be an active user
        try{
            String uri = url + "/chatmessages";
            HttpUriRequest request = RequestBuilder.post()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .setEntity(new StringEntity(text.toString(), "UTF-8"))
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200){
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
            else {
                System.out.println(request.toString());

                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void establishConnect(String username){
        testConnect();
        setUsername(username);
        getToken();
        getManager();
        postMananger();
        registerOnline();
        if (isManager){
            //Register self as active
            registerActive(username);
            //postCanvas();
        }
        else{
            //Send waiting request
            postWaitingUsers();
        }

    }

    public void reconnect(){
        //getToken();

        //getManager();
        //postMananger();
        //registerOnline();
        System.out.println("reconnect");
        currentCanvas = -1;
        currentChat = -1;
        if (isManager){
            //Register self as active
            registerActive(username);
        }
        else{
            //Send waiting request
            postWaitingUsers();
        }
        System.out.println("restart");
        stopUpdate = false;
        updateThread = getUpdateThread();
        updateThread.start();
    }

    public void userLogout(HTTPConnect myHTTPConnect){
        myHTTPConnect.deleteOnline();
        myHTTPConnect.deleteActiveSelf();
    }

    public void sendCanvas(JSONObject json){
        new Thread(() -> {
            putCanvas(json);
        }).start();
    }

    public void sendText(String text){
        JSONObject JSONText = new JSONObject();
        JSONText.put("text", text);
        JSONText.put("username", username);
        System.out.println(JSONText);
        new Thread(() -> {
            postChatMessages(JSONText);
        }).start();
    }


    public Thread getUpdateThread(){
        Thread thread = new Thread(){
            @Override
            public synchronized void run() {
                while (!stopUpdate){
                    //Check if active
                    System.out.println("Check active");
                    String state = getUserState();
                    if (state.equals("kicked") | state.equals("rejected")){
                        try {
                            drawController.switchToLogin();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    //Updat Canvas
                    System.out.println("update canvas");
                    JSONArray canvasResult = null;
                    if (currentCanvas == -1){
                        canvasResult = getCanvas();
                    }
                    else{
                        canvasResult = getCanvas(currentCanvas);
                    }
                    if (canvasResult.length()>0){
                        for (int i = 0; i < canvasResult.length(); i++){
                            JSONObject drawing = canvasResult.getJSONObject(i).getJSONObject("request");
                            currentCanvas = canvasResult.getJSONObject(i).getInt("id");
                            String objectType = drawing.getString("Object");
                            JSONObject action = drawing.getJSONObject("Action");
                            drawAction(objectType, action);
                            //System.out.println(drawing);
                        }
                    }


                    //Update Chat
                    System.out.println("update chat");
                    JSONArray chatResult = null;
                    if (currentChat == -1){
                        try {
                            chatResult = getChatMessages();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    else{
                        chatResult = getChatMessages(currentChat);
                    }
                    if (chatResult.length()>0){
                        for (int i = 0; i < chatResult.length(); i++){
                            JSONObject chating = chatResult.getJSONObject(i).getJSONObject("request");
                            currentChat = chatResult.getJSONObject(i).getInt("id");
                            String text = chating.getString("text");
                            String username = chating.getString("username");
                            String message = username + ": \n" + text;
                            Platform.runLater(()-> {
                                        drawController.receiveMessage(message);
                                        });
                            //System.out.println(drawing);
                        }
                    }

                    //Update Waiting User
                    if (isManager){
                        System.out.println("update waiting user");
                        getWaitingUsers();
                        System.out.println(waitingUserList);
                        if (!drawController.isBusy){
                            if (waitingUserList != null && !waitingUserList.isEmpty()){
                                Platform.runLater(()->{
                                    //modify your javafx app here.
                                    for (int i = 0; i < waitingUserList.length(); i++){
                                        drawController.waitingUser(waitingUserList.getString(i));
                                        System.out.println(waitingUserList.getString(i));
                                    }
                                });
                            }
                        }
                    }


                    //Update Active User
                    System.out.println("update active user");
                    getActiveUser();
                    getOnlineUsers();
                    registerOnline();
                    System.out.println(activeUserList);
                    System.out.println(onlineUserList);
                    ArrayList<String> activeAndOnlineUserList = new ArrayList<String>();
                    if (activeUserList.length()>0 && onlineUserList.length()>0){
                        for (int i=0; i < activeUserList.length(); i++){
                            for (int j=0; j < onlineUserList.length(); j++){
                                if (activeUserList.get(i).equals(onlineUserList.get(j))){
                                    activeAndOnlineUserList.add((String) activeUserList.get(i));
                                }
                            }
                        }
                    }
                    Platform.runLater(()->{
                        //modify your javafx app here.
                        drawController.clearUsers();
                        System.out.println("Check");
                        if (activeAndOnlineUserList.size()>0){
                            for (int i=0; i < activeAndOnlineUserList.size(); i++){
                                drawController.receiveUser(activeAndOnlineUserList.get(i));
                            }
                        }
                    });


                    //Wait for 100ms
                    try {
                        this.wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        //thread.start();
        return thread;
    }

    public void stopUpdateThread(){
        stopUpdate = true;
    }

    public void drawAction(String objectType, JSONObject action){
        switch (objectType){
            case "TEXT":
                artist.drawJSONText(action);
                break;
            case "CIRCLE":
                artist.drawJSONCircle(action);
                break;
            case "LINE":
                artist.drawJSONLine(action);
                break;
            case "RECTANGLE":
                artist.drawJSONRectangle(action);
                break;
            case "ELLIPSE":
                artist.drawJSONEllipse(action);
                break;
            case "IMAGE":
                artist.drawJSONImage(action);
                break;
            case "FREEHAND":
            case "ERASER":
                artist.drawJSONFreeHand(action);
                break;
            case "FILL":
                Platform.runLater(()->{
                    //modify your javafx app here.
                    artist.drawJSONFill(action);
                });
                break;
        }
    }




    public static void main( String[] args ) throws InterruptedException {
        HTTPConnect myHTTPConnect = new HTTPConnect();
        myHTTPConnect.establishConnect("rudolph1");
        myHTTPConnect.postCanvas();
        myHTTPConnect.getOnlineUsers();
        System.out.println(myHTTPConnect.onlineUserList);
        //myHTTPConnect.deleteCanvas();

        System.out.println(myHTTPConnect.getUserState());
        myHTTPConnect.getActiveUser();
        System.out.println(myHTTPConnect.activeUserList);
        myHTTPConnect.getOnlineUsers();
        System.out.println(myHTTPConnect.onlineUserList);
        myHTTPConnect.getCanvas(0);
//        JSONObject testJSON = new JSONObject();
//        testJSON.put("Object", "IMAGE");
//        JSONObject testAction = new JSONObject();
//        pngBase64 png = new pngBase64();
//        String test = null;
//
//        test = png.pngToString("C:/Users/IF/Pictures/fbk.png");
//        //System.out.println(test);
//
//        testAction.put("image", test);
//        testJSON.put("Action", testAction);
//        //System.out.println(testAction);
//        //myHTTPConnect.sendCanvas(testJSON);
//        try {
//            myHTTPConnect.getCanvas();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        myHTTPConnect.postChatMessages(testJSON);
        //myHTTPConnect.getChatMessages(0);

        //myHTTPConnect.deleteCanvas();



//        HTTPConnect newHTTPConnect = new HTTPConnect();
//        newHTTPConnect.establishConnect("bc");
//
//        myHTTPConnect.getActiveUser();
//        myHTTPConnect.getWaitingUsers();
//        System.out.println("Register bc as active");
//        myHTTPConnect.registerActive("bc");
//        myHTTPConnect.getActiveUser();
//        myHTTPConnect.getWaitingUsers();





    }

}
