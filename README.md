# UoM COMP90015 Distributed Systems - Distributed Whiteboard

This document is used by team members to record requirements, technical framework and meeting minutes.

## Deadlines

#### Deadline 1
Milestone 1 (Progress Review): **Week 10, Tuesday (Oct. 8)** in our own tutorial.

+ 4 marks will be given for demonstrating progress in phase 1.  
    * Requirement A of Phase 1: 2 marks
        - Implement a client that allows a single user to draw all the expected elements (line, circle, rectangle, oval, freehand drawing, and erasing).
    * Requirement B of Phase 1: 2 marks
        - Implement the open, new, save, save as, and close functionality for a single client.


#### Deadline 2
Milestone 2 (Final Submission): **Week 12, Friday (Oct 25)** at 5:00pm.

Write a report that includes the system architecture, communication protocols and message formats, design diagrams (class and interaction), implementation details, new innovations, and a section that outlines the contribution of each member.

| Name & Std. No.  | Contribution area | Overall contribution (% out of 100) to Project |
| ------------- | ------------- | ------------- |
| ..  | Describe…  | 20%? (decide reasonably) |
| ...  | ... | 15%? |

+ Submit the following via LMS:**
    - Report in PDF format only.
    - The executable jar files used to run system’s clients/server(s)
    - Source files in a .ZIP or .TAR archive only.

## Requirements

An initial version of [guidelines](Project2-2019.pdf) can be found in this repo.

Shared whiteboards allow multiple users to draw simultaneously on a canvas. The system should support a range of features such as freehand drawing with the mouse, drawing lines and shapes such as circles and squares that can be moved and resized, and inserting text. In addition to these features, the implementation should include a simple chat window, that allows all the current users of the system to broadcast messages to each other.

#### Function List

+ GUI Elements:
    - Shapes
        * [x] Line
        * [x] Circle
        * [x] Rectangle
        * [x] Oval
    - [x] Free Draw
    - [x] Erase
    - [x] Text Inputting (Allow user to type text everywhere inside the whiteboard)
    - [x] Color Select (At least 16 colors)
    - [ ] Chat Window (Text based)
    - File Menu
        * [ ] New
        * [ ] Open
        * [ ] Save
        * [ ] SaveAs
        * [ ] Close
+ Clients:
    - **Unique Username** - Users must provide a username when joining the whiteboard. There should be a way of uniquely identifying users, either by enforcing unique usernames or automatically generating a unique identifier and associating it with each username.
    - **Equivalent Privileges** - All the users should see the same image of the whiteboard and should have the privilege of doing all the drawing operations.
    - **Live Users** - When displaying a whiteboard, the client user interface should show the usernames of other users who are currently editing the same whiteboard.
    - **State Acquisition** - Clients may connect and disconnect at any time. When a new client joins the system the client should obtain the current state of the whiteboard so that the same objects are always displayed to every active client.
    - **Manager to Manage** - Only the manager of the whiteboard should be allowed to create a new whiteboard, open a previously saved one, save the current one, and close the application.
+ Proposed Operational Model
    - The first user creates a whiteboard and becomes the whiteboard’s manager
    ```bash
    java CreateWhiteBoard <serverIPAddress> <serverPort> username
    ```
    - Other users can ask to join the whiteboard application any time by inputting server’s IP address and port number
    ```bash
    java JoinWhiteBoard <serverIPAddress> <serverPort> username
    ```
    - A notification will be delivered to the manager if any peer wants to join. The peer can join in only after the manager approves
    - **An online peer list** should be maintained and displayed
    - All the peers will see the identical image of the whiteboard, as well as have the privilege of doing all the operations (draw and erase)
    - Online peers can **choose to leave** whenever they want. **The manager can kick someone out** at any time.
    - When the **manager quits**, the application will be terminated. All the peers will get a message notifying them.



## Teamwork

#### Division of Responsibilities

+ Client 
    - Reyham Soenasto
    - Rudolph Almeida
    - Steven Peng
+ Server
    - Boyang Yue

#### Git Workflow

[Forking Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/forking-workflow) & [Pull Request](https://www.atlassian.com/git/tutorials/making-a-pull-request)

## Technical Framework

#### Libraries Could be Used

- JavaFX (Built in Java 8): [JavaFX 13 at openjfx.io](https://openjfx.io), [JavaFX 8 at docs.oracle.com](https://docs.oracle.com/javase/8/javafx/api/index.html)
    - JFoenix (Material Design): [Official Website](http://www.jfoenix.com), [Github](https://github.com/jfoenixadmin/JFoenix)
- Java2D drawing package: [Docs at Oracle](https://docs.oracle.com/javase/tutorial/2d/index.html)

#### System Architecture

The API is available online right now! Use it by sending your request to **api.boyang.website/whiteboard/{URI}**

There are nine URIs you may request. Details are given below.

1. **Root**
    ```
    URL: http://api.boyang.website/whiteboard
    Method: GET
    Parameter(s): NULL
    Expected Return: {"message":"Hi there!"}
    ```
    The only function of this URI is to help you get your hands on RESTful API (or test your network connection). Your HTTP Request Headers may look like this:
    ```
    GET /whiteboard HTTP/1.1
    Host: api.boyang.website
    User-Agent: PostmanRuntime/7.17.1
    Accept: */*
    Accept-Encoding: gzip, deflate
    Connection: keep-alive
    ```
    The Response Headers should like this:
    ```
    HTTP/1.1 200 OK
    Date: Sun, 29 Sep 2019 13:26:48 GMT
    Content-Type: application/json
    Content-Length: 23
    Connection: keep-alive
    Server: cloudflare
    CF-RAY: 51de4a7b2911df1c-MEL
    ```
    Tips:

    + You should never put anything into the HTTP request body when you use GET method.
    + You could quickly determine if your request was successful by status code (200 OK)
    + The format of message from the server would always be JSON (except the access token).  

2. **AccessToken**
    ```
    URL: http://api.boyang.website/whiteboard/accesstoken
    Method: GET
    Parameter(s): User (in Request Headers), Code (in Request Headers)
    Expected Return: Access-Token (in Response Headers), {"message":"Successfully obtained accesstoken"}
    ```
    This URI is to enforce access control by generating [access token](https://en.wikipedia.org/wiki/Access_token). You are supposed to request for this URI before requesting for others (except Root). Just put two parameters into HTTP Request Headers: 

    + User: Username of current user.
    + Code: 471e61cd2878317b204b878dfc918d2b (which is the 32 bit MD5 hash of "frameless").

    For example, if you send request on behalf of user "test", Your HTTP Request Headers may look like this:
    ```
    GET /whiteboard/accesstoken HTTP/1.1
    Host: api.boyang.website
    Code: 471e61cd2878317b204b878dfc918d2b
    User: test
    User-Agent: PostmanRuntime/7.17.1
    Accept: */*
    Accept-Encoding: gzip, deflate
    Connection: keep-alive
    ```
    The Response Headers should like this:
    ```
    HTTP/1.1 200 OK
    Date: Sun, 29 Sep 2019 13:26:48 GMT
    Content-Type: application/json
    Content-Length: 47
    Connection: keep-alive
    Access-Token: 40ba5aeca40e537c0933e1bc287fb3d1
    Server: cloudflare
    CF-RAY: 51deda9a8d29df95-MEL
    ```
    You should read and save the Access-Token. Every time you request for other URIs you need to put it into HTTP Request Headers to identify yourself. If not, you would get an error message:
    ```
    401 Unauthorized
    {"message\":"Not a legal user"}
    ```
    Error message you may get:
    ```
    401 Unauthorized
    {"message":"Code is not correct"}
    ```

    Tips:

    + You could always get a new access token with the right code and if you request for a new one, it would be different every time for every user. So you do not need to save it permanently.
    + Why do this? Please refer to [OAuth 2.0](https://oauth.net/2/).

3. **Manager**

    *\@GET* : To find out who is the manager right now
    ```
    URL: http://api.boyang.website/whiteboard/manager
    Method: GET
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"result": managername, "message":"Request completed successfully"}
    ```

    Default manager is "null". In this case, "null" is regarded as a reserved word in the server and it could not be an user name.

    *\@POST*: To register the current user as the manager
    ```
    URL: http://api.boyang.website/whiteboard/manager
    Method: POST
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"message":"Request completed successfully"}
    ```
    Error message you may get:
    ```
    405 Method Not Allowed
    {"message":"Manager already exists"}
    ```

    *\@DELETE*: To unregister the current user as the manager
    ```
    URL: http://api.boyang.website/whiteboard/manager
    Method: DELETE
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"message":"Request completed successfully"}
    ```
    Error message you may get:
    ```
    403 Forbidden
    {"message":"Only manager can delete manager"}

    405 Method Not Allowed
    {"message":"Manager does not exist"}
    ```

    Tips:

    + At the very start, nobody is manager. But once a user register himself or herself as the manager, others could not register as the manager.

4. **OnlineUsers**

    *\@GET*: To get the list of all online users
    ```
    URL: http://api.boyang.website/whiteboard/onlineusers
    Method: GET
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"result": [onlineuserslist], "message":"Request completed successfully"}
    ```
    Error message you may get:
    ```
    404 Not Found
    {"result": "null", "message":"No online user exists"}
    ```

    *\@POST*: To register the current user as an online user
    ```
    URL: http://api.boyang.website/whiteboard/onlineusers
    Method: POST
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"message":"Request completed successfully"}
    ```

    *\@DELETE*: To unregister the current user as an online user
    ```
    URL: http://api.boyang.website/whiteboard/onlineusers
    Method: DELETE
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"message":"Request completed successfully"}
    ```

    Tips:

    + After you get the access token, just make yourself online. When you close the application, remember to make yourself offline.

5. **ActiveUsers**

    *\@GET*: To get the list of all active users
    ```
    URL: http://api.boyang.website/whiteboard/activeusers
    Method: GET
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"result": [activeuserslist], "message":"Request completed successfully"}
    ```
    Error message you may get:
    ```
    404 Not Found
    {"result": "null", "message":"No active user exists"}
    ```

    *\@POST*: To register some user as an active user
    ```
    URL: http://api.boyang.website/whiteboard/activeusers/{user}
    Method: POST
    Parameter(s): Access-Token (in Request Headers), user (in URL)
    Expected Return: {"message":"Request completed successfully"}
    ```
    Error message you may get:
    ```
    403 Forbidden
    {"message":"Only manager can activate users"}
    ```

    *\@DELETE*: To unregister some user as an active user
    ```
    URL: http://api.boyang.website/whiteboard/activeusers/{user}
    Method: DELETE
    Parameter(s): Access-Token (in Request Headers), user (in URL)
    Expected Return: {"message":"Request completed successfully"}
    ```
    Error message you may get:
    ```
    403 Forbidden
    {"message":"Only manager can kick users"}
    ```

    Tips:

    + For the user who is manager, he or she could active (let them join in the whiteboard) or deactive (kick them) others.
    + For the user who is not manager, he or she could deactive himself or herself (leave the whiteboard).
    + Manager should active himself or herself after becoming the manager.

6. **Canvas**

    *\@GET*: To get all data of the canvas
    ```
    URL: http://api.boyang.website/whiteboard/canvas
    Method: GET
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"result": [{"id": 1, "request": {yourjsonhere}}, {"id": 2, "request": {yourjsonhere}}, ...], "message":"Request completed successfully"} or
                     {"result": null, "message":"Request completed successfully"}
    ```

    Default canvas is "null". That means no canvas exists. Manager could initialize one by using POST method.

    Error message you may get:
    ```
    403 Forbidden
    {"message":"Not an active user"}
    ```

    *\@GET*: To get updated data of the canvas
    ```
    URL: http://api.boyang.website/whiteboard/canvas/{mid}
    Method: GET
    Parameter(s): Access-Token (in Request Headers), mid (in URL)
    Expected Return: {"result": [{"id": mid + 1, "request": {yourjsonhere}}, {"id": mid + 2, "request": {yourjsonhere}}, ...], "message":"Request completed successfully"} or
                     {"result": null, "message":"Request completed successfully"}
    ```

    This method would only return messages whose message id are behind "mid".

    Error message you may get:
    ```
    403 Forbidden
    {"message":"Not an active user"}
    ```

    *\@POST*: To initialize a canvas
    ```
    URL: http://api.boyang.website/whiteboard/canvas
    Method: POST
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"message":"Request completed successfully"}
    ```
    Error message you may get:
    ```
    403 Forbidden
    {"message":"Not an active user"}

    403 Forbidden
    {"message":"Only manager can create new cavans"}
    ```

    *\@PUT*: To update the canvas
    ```
    URL: http://api.boyang.website/whiteboard/canvas
    Method: PUT
    Parameter(s): Access-Token (in Request Headers), {your jsons tring} (in Request Body)
    Expected Return: {"message":"Request completed successfully"}
    ```
    Error message you may get:
    ```
    403 Forbidden
    {"message":"Not an active user"}
    ```

    *\@DELETE*: To delete the canvas
    ```
    URL: http://api.boyang.website/whiteboard/canvas
    Method: DELETE
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"message":"Request completed successfully"}
    ```
    Error message you may get:
    ```
    403 Forbidden
    {"message":"Not an active user"}

    403 Forbidden
    {"message":"Only manager can delete canvas"}
    ```

    Tips:

    + Manager could get, post, put and delete the canvas.
    + Other active users could only get and put the canvas.

7. **WaitingUsers**

    *\@GET*: To get the list of all users who are waiting for manager's approval
    ```
    URL: http://api.boyang.website/whiteboard/waitingusers
    Method: GET
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"result": [waitinguserslist], "message":"Request completed successfully"}
    ```
    Error message you may get:
    ```
    404 Not Found
    {"result": "null", "message":"No online user exists"}
    ```

    *\@POST*: To register the current user as a waiting user
    ```
    URL: http://api.boyang.website/whiteboard/waitingusers
    Method: POST
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"message":"Request completed successfully"}
    ```
    *\@DELETE*: To reject some user to be an active user
    ```
    URL: http://api.boyang.website/whiteboard/waitingusers/{user}
    Method: DELETE
    Parameter(s): Access-Token (in Request Headers), user (in URL)
    Expected Return: {"message":"Request completed successfully"}
    ```
    Error message you may get:
    ```
    403 Forbidden
    {"message":"Only manager can reject users"}
    ```

8. **UserState**

    *\@GET*: To get the state of current user
    ```
    URL: http://api.boyang.website/whiteboard/userstate
    Method: GET
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"result": "null|active|waiting|rejected|kicked", "message":"Request completed successfully"}
    ```

9. **ChatMessages**

    *\@GET*: To get all chat messages
    ```
    URL: http://api.boyang.website/whiteboard/chatmessages
    Method: GET
    Parameter(s): Access-Token (in Request Headers)
    Expected Return: {"result": [{"id": 1, "request": {yourjsonhere}}, {"id": 2, "request": {yourjsonhere}}, ...], "message":"Request completed successfully"} or
                     {"result": null, "message":"Request completed successfully"}
    ```

    Error message you may get:
    ```
    403 Forbidden
    {"message":"Not an active user"}
    ```

    *\@GET*: To get updated chat messages
    ```
    URL: http://api.boyang.website/whiteboard/chatmessages/{cid}
    Method: GET
    Parameter(s): Access-Token (in Request Headers), cid (in URL)
    Expected Return: {"result": [{"id": cid + 1, "request": {yourjsonhere}}, {"id": cid + 2, "request": {yourjsonhere}}, ...], "message":"Request completed successfully"} or
                     {"result": null, "message":"Request completed successfully"}
    ```

    This method would only return messages whose message id are behind "cid".

    Error message you may get:
    ```
    403 Forbidden
    {"message":"Not an active user"}
    ```

    *\@POST*: To add a new chat message
    ```
    URL: http://api.boyang.website/whiteboard/chatmessages
    Method: POST
    Parameter(s): Access-Token (in Request Headers), {your jsons tring} (in Request Body)
    Expected Return: {"message":"Request completed successfully"}
    ```
    Error message you may get:
    ```
    403 Forbidden
    {"message":"Not an active user"}
    ```

    Tips:

    + The chat messages could be regarded as a part of canvas. In other words, canvas should exist if you want to post new chat messages. If canvas is deleted, chat messages would be cleared too.

<br>

**Have fun and keep going!**