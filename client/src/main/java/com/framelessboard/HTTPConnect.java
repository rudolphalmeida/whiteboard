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

    public CloseableHttpClient httpclient;
    ;
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


    Thread updateThread;
    public Task<Void> updateTask;
    private boolean stopUpdate = false;

    HTTPConnect() {
        httpclient = HttpClients.createDefault();

        updateThread = getUpdateThread();
    }

    public void setUsername(String username) {
        if (username != null) {
            this.username = username;
        } else {
            System.out.println("User name illegal");
        }
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public void setDrawController(DrawController drawController) {
        this.drawController = drawController;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public void testConnect() {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getToken() {
        //Get the access token.
        try {
            String uri = url + "/accesstoken";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("User", username)
                    .setHeader("Code", Code)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                token = response.getFirstHeader("Access-Token").getValue();
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getManager() {
        //To find out who is the manager right now
        try {
            String uri = url + "/manager";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                currentMananger = content.getString("result");
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postMananger() {
        //To register the current user as the manager
        if (currentMananger.equals("null")) {
            //Set yourself the manager
            try {
                String uri = url + "/manager";
                HttpUriRequest request = RequestBuilder.post()
                        .setUri(uri)
                        .setHeader("Access-Token", token)
                        .build();
                CloseableHttpResponse response = httpclient.execute(request);
                if (response.getStatusLine().getStatusCode() == 200) {
                    JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    isManager = true;
                } else {
                    JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (username.equals(currentMananger)) {
            isManager = true;
        }
    }

    public void deleteManager() {
        //To unregister the current user as the manager
        if (username.equals(currentMananger)) {
            try {
                String uri = url + "/manager";
                HttpUriRequest request = RequestBuilder.delete()
                        .setUri(uri)
                        .setHeader("Access-Token", token)
                        .build();
                CloseableHttpResponse response = httpclient.execute(request);
                if (response.getStatusLine().getStatusCode() == 200) {
                    JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                } else {
                    JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getOnlineUsers() {
        //To get the list of all online users
        try {
            String uri = url + "/onlineusers";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                if (content.get("result").equals("null")) {
                } else {
                    onlineUserList = content.getJSONArray("result");
                }
            } else {
                onlineUserList = new JSONArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerOnline() {
        //To register the current user as an online user
        try {
            String uri = url + "/onlineusers";
            HttpUriRequest request = RequestBuilder.post()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteOnline() {
        //To unregister the current user as an online user
        try {
            String uri = url + "/onlineusers";
            HttpUriRequest request = RequestBuilder.delete()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getActiveUser() {
        //To get the list of all active users
        try {
            String uri = url + "/activeusers";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                activeUserList = content.getJSONArray("result");
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                activeUserList = new JSONArray();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerActive(String user) {
        //To register some user as an active user
        try {
            String uri = url + "/activeusers/" + user;
            HttpUriRequest request = RequestBuilder.post()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteActive(String user) {
        //To unregister some user as an active user
        try {
            String uri = url + "/activeusers/" + user;
            HttpUriRequest request = RequestBuilder.delete()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteActiveSelf() {
        try {
            String uri = url + "/activeusers/" + username;
            HttpUriRequest request = RequestBuilder.delete()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONArray getCanvas() {
        //To get all data of the canvas
        try {
            String uri = url + "/canvas";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                content.get("result");
                JSONArray result = content.getJSONArray("result");
                if (result.length() != 0) {
                    return result;
                }
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    private JSONArray getCanvas(int mid) {
        //To get updated data of the canvas
        try {
            String uri = url + "/canvas/" + mid;
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                if (content.get("result").equals("null")) {
                    return new JSONArray();
                }
                JSONArray result = content.getJSONArray("result");
                if (result.length() != 0) {
                    return result;
                }
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }


    void postCanvas() {
        //To initialize a canvas
        try {
            String uri = url + "/canvas";
            HttpUriRequest request = RequestBuilder.post()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void putCanvas(JSONObject json) {
        //To update the canvas
        try {
            String uri = url + "/canvas";
            HttpUriRequest request = RequestBuilder.put()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .setEntity(new StringEntity(json.toString(), "UTF-8"))
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void deleteCanvas() {
        //To delete the canvas
        try {
            String uri = url + "/canvas";
            HttpUriRequest request = RequestBuilder.delete()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getWaitingUsers() {
        //To get the list of all users who are waiting for manager's approval
        try {
            String uri = url + "/waitingusers";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
                waitingUserList = content.getJSONArray("result");
            } else {
                System.out.println(request.toString());
                System.out.print("Status Code:" + response.getStatusLine().getStatusCode());
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.out.println(content);
                waitingUserList = new JSONArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postWaitingUsers() {
        //To register the current user as a waiting user
        try {
            String uri = url + "/waitingusers";
            HttpUriRequest request = RequestBuilder.post()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteWaitingUsers(String username) {
        //To reject some user to be an active user
        try {
            String uri = url + "/waitingusers/" + username;
            HttpUriRequest request = RequestBuilder.delete()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserState() {
        //To reject some user to be an active user
        try {
            String uri = url + "/userstate";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                String state = content.getString("result");
                return state;
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONArray getChatMessages() {
        //To reject some user to be an active user
        try {
            String uri = url + "/chatmessages";
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                content.get("result");
                JSONArray result = content.getJSONArray("result");
                if (result.length() != 0) {
                    return result;
                }
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public JSONArray getChatMessages(int mid) {
        //To reject some user to be an active user
        try {
            String uri = url + "/chatmessages/" + mid;
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                if (content.get("result").equals("null")) {
                    return new JSONArray();
                }
                JSONArray result = content.getJSONArray("result");
                if (result.length() != 0) {
                    return result;
                }
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public void postChatMessages(JSONObject text) {
        //To reject some user to be an active user
        try {
            String uri = url + "/chatmessages";
            HttpUriRequest request = RequestBuilder.post()
                    .setUri(uri)
                    .setHeader("Access-Token", token)
                    .setEntity(new StringEntity(text.toString(), "UTF-8"))
                    .build();
            CloseableHttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            } else {
                JSONObject content = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void establishConnect(String username) {
        testConnect();
        setUsername(username);
        getToken();
        getManager();
        postMananger();
        registerOnline();
        if (isManager) {
            //Register self as active
            registerActive(username);
            postCanvas();
        } else {
            //Send waiting request
            postWaitingUsers();
        }

    }

    void reconnect() {
        currentCanvas = -1;
        currentChat = -1;
        if (isManager) {
            //Register self as active
            registerActive(username);
        } else {
            //Send waiting request
            postWaitingUsers();
        }
        stopUpdate = false;
        updateThread = getUpdateThread();
        updateThread.start();
    }

    public void userLogout(HTTPConnect myHTTPConnect) {
        myHTTPConnect.deleteOnline();
        myHTTPConnect.deleteActiveSelf();
    }

    public void sendCanvas(JSONObject json) {
        new Thread(() -> {
            putCanvas(json);
        }).start();
    }

    public void sendText(String text) {
        JSONObject JSONText = new JSONObject();
        JSONText.put("text", text);
        JSONText.put("username", username);
        new Thread(() -> {
            postChatMessages(JSONText);
        }).start();
    }


    Thread getUpdateThread() {
        //thread.start();
        return new Thread() {
            @Override
            public synchronized void run() {
                while (!stopUpdate) {
                    //Check if active
                    String state = getUserState();
                    if (state.equals("kicked") | state.equals("rejected")) {
                        try {
                            drawController.switchToLogin();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    //Update Canvas
                    JSONArray canvasResult = null;
                    if (currentCanvas == -1) {
                        canvasResult = getCanvas();
                    } else {
                        canvasResult = getCanvas(currentCanvas);
                    }
                    if (canvasResult.length() > 0) {
                        for (int i = 0; i < canvasResult.length(); i++) {
                            JSONObject drawing = canvasResult.getJSONObject(i).getJSONObject("request");
                            currentCanvas = canvasResult.getJSONObject(i).getInt("id");
                            String objectType = drawing.getString("Object");
                            JSONObject action = drawing.getJSONObject("Action");
                            drawAction(objectType, action);
                        }
                    }


                    //Update Chat
                    JSONArray chatResult = null;
                    if (currentChat == -1) {
                        try {
                            chatResult = getChatMessages();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        chatResult = getChatMessages(currentChat);
                    }

                    if (chatResult != null && chatResult.length() > 0) {
                        for (int i = 0; i < chatResult.length(); i++) {
                            JSONObject chating = chatResult.getJSONObject(i).getJSONObject("request");
                            currentChat = chatResult.getJSONObject(i).getInt("id");
                            String text = chating.getString("text");
                            String username1 = chating.getString("username");
                            String message = username1 + ": \n" + text;
                            Platform.runLater(() -> {
                                drawController.receiveMessage(message);
                            });
                        }
                    }

                    //Update Waiting User
                    if (isManager) {
                        getWaitingUsers();
                        if (!drawController.isBusy) {
                            if (waitingUserList != null && !waitingUserList.isEmpty()) {
                                Platform.runLater(() -> {
                                    //modify your javafx app here.
                                    for (int i = 0; i < waitingUserList.length(); i++) {
                                        drawController.waitingUser(waitingUserList.getString(i));
                                    }
                                });
                            }
                        }
                    }


                    //Update Active User
                    getActiveUser();
                    getOnlineUsers();
                    registerOnline();
                    ArrayList<String> activeAndOnlineUserList = new ArrayList<String>();
                    if (activeUserList.length() > 0 && onlineUserList.length() > 0) {
                        for (int i = 0; i < activeUserList.length(); i++) {
                            for (int j = 0; j < onlineUserList.length(); j++) {
                                if (activeUserList.get(i).equals(onlineUserList.get(j))) {
                                    activeAndOnlineUserList.add((String) activeUserList.get(i));
                                }
                            }
                        }
                    }
                    Platform.runLater(() -> {
                        //modify your javafx app here.
                        drawController.clearUsers();
                        if (activeAndOnlineUserList.size() > 0) {
                            for (String s : activeAndOnlineUserList) {
                                drawController.receiveUser(s);
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
    }

    void stopUpdateThread() {
        stopUpdate = true;
    }

    private void drawAction(String objectType, JSONObject action) {
        switch (objectType) {
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
                Platform.runLater(() -> {
                    //modify your javafx app here.
                    artist.drawJSONFill(action);
                });
                break;
        }
    }


    public static void main(String[] args) throws InterruptedException {
        HTTPConnect myHTTPConnect = new HTTPConnect();
        myHTTPConnect.establishConnect("rudolph1");
        myHTTPConnect.postCanvas();
        myHTTPConnect.getOnlineUsers();

        myHTTPConnect.getActiveUser();
        myHTTPConnect.getOnlineUsers();
        myHTTPConnect.getCanvas(0);
    }

}
