# ConversationalIST

A mobile messaging application focused on speed, security and location features for Android Operating Systems.

![ConversationalIST2](https://user-images.githubusercontent.com/78174997/168442370-834eda47-5517-472b-b0ca-ecc2ae1b5069.png)

## Introduction

ConversationalIST is at powerful messaging application which explores several key aspects of mobile development, including location awareness, judicious use of limited resources, and social behavior.

ConversationalIST user communication is centered around chatrooms, which users can create, join, participate in, or leave. Once a user has access to a chatroom, is able ton participate in a shared conversation and review older chat history, even retroactively prior to joining. Conversations can include a variety of media to aid in user communication, including text, photos, files, and geographic locations.

---

## ConversationalIST Live Video

https://user-images.githubusercontent.com/78174997/178368246-95d9eb45-3127-4990-ae99-dbc6d6afe503.mp4

---

# Table of Contents
1. [Introduction](#introduction)
2. [ConversationalIST Live Video](#conversationalist-live-video)
3. [Mobile Interface Design](#mobile-interface-design)
4. [Features](#features) 
5. [Extra Features](#extra-features)
6. [Processing and Saving Data](#processing-and-saving-data)
7. [Description of Client-Server Protocols](#description-of-client-server-protocols)
8. [Moderation and resource management](#moderation-and-resource-management)
9. [Context Awareness and Privacy](#context-awareness-and-privacy)
10. [Caching](#caching)
11. [Optimizations](#optimizations)
12. [Getting Started](#getting-started)
13. [Prerequisites](#prerequisites)
14. [Used Technologies](#used-technologies)
15. [Authors](#authors)
16. [Information about the project statement and idea](#information-about-the-project-statement-and-idea)

---

## Mobile Interface Design 

The activity wireframe of ConversionalIST is visible on the image below

![wireframe](https://user-images.githubusercontent.com/78174997/178365395-bdc31117-4f63-4a96-a10c-e89d778cbed7.jpg)

---

## Features

| Feature                          | Definition                
|:--------------------------------:|:---------------------------------
|Pick Username                     | Allow users to pick a unique username or handle to identify themselves in a conversation. This user ID is used to clearly mark their contributions to the shared conversation, along with additional meta-data like timestamps. 
|Create new chatrooms or join one  | Users can create new chatrooms or join existing ones. The users have easy access to a list of chatrooms they have joined, allowing them to easily check which have unread messages and to quickly switch between them.
|Watch chatroom content            | After selecting a chatroom, users should be able to see the content published there. Chatrooms can be seen as living append-only documents in that once a user has access to the chatroom, they are able to see all of its past content, including content submitted before they joined
|Ability to submit media           |   Users are able to submit a variety of simple media, including: Simple text messages. Photos taken from the phone’s camera. Geographic locations, to be shown as an embedded map with a button to request directions to the given location (e.g. by opening up Google Maps or other similar application). The user sharing a location is able to specify the location by selecting it on a map, searching an address, or using the phone’s current location.
| Notify user via a Notification   | If a message is sent to a chatroom the user has access to it, but if the user is not actively watching, will be notified via a Notification. Tapping the notification takes them directly to the specific chatroom to see the new messages.
| Messages Sync Across Devices Promptly | Messages are synchronized across mobile devices
| Efficient Message Retrieval        | Messages are received efficiently
| Download via Cellular Data or WiFi | Download Images on Request with Cellular Data / Automatically with WiFi
| Data Caching                       | Data is cached on mobile devices to improve efficiency
| Cache Pre-loading                  | Data is cached and pre-loaded


<img src="https://user-images.githubusercontent.com/78174997/168443250-d94029eb-9c45-43df-872c-7cb44c7fe0cc.jpg"/>


## Extra Features

| Feature                            | Definition                
|:----------------------------------:|:---------------------------------
| Securing Communications            | Securing Communications, Check Trust in Server
| Recommendations                    | Compute Most Likely Chatroom Pairings, List Sorted Suggestions 
| UI Adaptability: Light/Dark Theme  | UI Works Well in Light and Dark Mode
| UI Adaptability: Rotation          | UI Adaptability: Rotation

---

## Processing and Saving Data

ConversationalIST supports a number of features that build on explicit data sharing and crowd-sourcing among multiple devices. To enable such functionality we have a back-end service that holds and processes shared data (e.g. chatrooms) and that each device communicates with to synchronize its state.

The back-end service is implemented via a RESTful service. The Server uses MongoDB to save Users(id, name), Rooms(id, name, roomType, latitude, longitude, radius), Messages(id,sender, roomID, message, createdAt, isPhoto), Photos(id, messageID, file), Subscriptions(roomID, userID).

We additionally use Firebase Realtime Database to store the user's profile data, such as profile picture and bio.
The Client uses SQLite to save Rooms(id, name, isGeoFenced, latitude, longitude, radius), Messages(id, sender, roomID, message, createdAt, isPhoto), and photos are saved in the internal storage of the app.

---

## Description of Client-Server Protocols

The communication between the client and the server is done through HTTPS, using the methods GET and POST.
Both the server and the app use firebase cloud messaging(FCM), in order to, in case of the server to send notifications, and in the app receive notifications. 

When a user adds a room, it will save it in the local database, send the pair userID roomID to the server,and subscribe the topic in FCM with the roomID, so when a user sends a message to a room, it will send to the server, and the server stores it and then sends a notification to the roomID FCM topic.

In order to send a photo, the app first will send a message with the isPhoto flag, and with the server’s response(messageID) the app will then upload the image to the server. When a message is received with the flag isPhoto, the app will only make the photo’s request to the server when the app needs to display it, taking into account that if the user is on a metered connection it will only download if requested by him.

In the case of a geo-fenced room, when a notification is received, the data will be stored but it will only send a notification if the user is within the room's location. In the main activity, where the chatrooms are listed, the geo-fenced rooms that the users joined will only be listed if the user is within the room’s location. In order to add a geo-fenced room will only be possible if the user is within the room’s location.

In order to get recommendations, the app will send a request to the server with the userID, the server will then calculate 6 rooms to recommend according to the project statement and after it will be displayed the recommendations.

###  Other Relevant Design Features

Although a login and registration system were not designed, profiles were developed for the users, where each user can upload a
profile picture and add a bio. In addition, a Dark Mode/Light Mode was implemented to flow with the rotation of the device.

---

## Moderation and resource management

In ConversationalIST, conversations are sync across users and devices in a timely manner while also using the network efficiently. When the user is actively viewing a chatroom, we ensure that any new content shows up quickly. If the user disengages from the application, we use more efficient messaging to save network resources, even if at the expense of increased latency.

ConversationalIST avoids using resources unnecessarily. As conversations in a chatroom grow overtime, users are less likely to scroll up to older messages. We avoid wasting resources by only downloading data related to UI elements as they become visible to the user.

Particularly large content is further optimized to avoid costly metered data. In ConversationalIST photos represent a hefty data cost so, to optimize network usage,
we show a placeholder for them when the user is on a metered connection, retrieving the image only when the user taps it. If, on the other hand, the user is on WiFi, automatically retrieve photos when visible.

---

## Context Awareness and Privacy

Access to chatrooms is restricted to keep conversations private or in context. Three modes are supported:

<img src="https://user-images.githubusercontent.com/78174997/168442705-11ed6c3c-e359-4742-afa3-5df646fe3f4a.png" width="470" height="300"/>

| Mode                             | Context Awareness and Privacy Level                
|:--------------------------------:|:---------------------------------
|Public                            |  Any user can search for the chatroom by name and join.
|Private                           | Users must first use an Android App Link to join the chatroom. Anyone already in the chatroom can share these links using the Android simple data sharing API (share via SMS, social networks, E-Mail, etc.)
|Geo-fenced                        | Users can only participate (join/view) in the chatroom when located within a specific region defined by the chatroom creator. The creator picks a point (on a map, by address, or using their current location) and a radius. Other users within that radius can then search and join the chatroom and participate. Leaving the area blocks access, even for those who have already joined. Re-entering allows access once again, without needing to rejoin.

---

## Caching

Often users have only spotty data-connections with metered data. As such, communication between the ConversationalIST application and its back-end server are optimized to use the network judiciously and to compensate for short term outages. On one hand we avoid downloading the same content multiple times when it could
reasonably be avoided, on the other we minimize disruption during a momentary outage.

To address this challenge, a cache is used to store content as it is retrieved from the server.
With this in place, repeated downloads of images are minimized and any content recently viewed will be available offline if needed.

Further cache is optimized through careful pre-loading when the user connects to WiFi. As WiFi data is virtually free, the opportunity to load the most recent content
(the content immediately visible when opening the chatroom without scrolling) is used for each chatroom the user has access to. This way, later when the user no longer has WiFi, they can still browse all of their chatrooms with minimal data usage

---

## Optimizations

Additional optimizations implemented, to save power and improve usability:

- In order to save power, the geo-fenced rooms will only be displayed in the chatroom list if the user is within the room’s location, the same happens with the notifications, so that when a notification is received from a geo-fenced room the data will be saved, and the notification will only be displayed if the user is within the room’s location.

- In order to improve usability, whenever a message is received and the user is in the indicated room it will show an indicator that can be pressed to auto-scroll to the bottom of the chatroom.

- In order to save resources, when a chatroom is opened for the first time it will only fetch the latest 30 messages, but if the user scrolls to the top it will fetch 30 more and consequentially more.

- In order to save resources, the class that interacts with the database and the class that communicates with the server are singletons.

---

## Getting Started

The following instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

- Clone this repository
- Import app to `Android Studio`
- Set up your favorite Emulator or install the app directly on your mobile device
- Run the app

---

## Prerequisites

- [Android Studio](https://developer.android.com/studio) - Unified environment to build Android Apps;

---

## Used Technologies

* [Java](https://openjdk.java.net/) - Programming Language;
* [Android Studio](https://developer.android.com/studio) - Unified environment to build Android Apps;
* [SQLite](https://www.sqlite.org/index.html) - SQL database engine;
* [Mongodb](https://www.mongodb.com/) - NoSQL database;
* [Firebase](https://firebase.google.com/) - App development platform;

---

## Authors

* **André Proença** - [GitHub](https://github.com/AndreProenza)
* **Bernardo Várzea** - [GitHub](https://github.com/bernacv)
* **António Martins** - [GitHub](https://github.com/AL-CT)

---

## Information about the project statement and idea 

Project statement and idea were provided by [Instituto Superior Técnico](https://tecnico.ulisboa.pt/en/), however the system was developed by us.

---
