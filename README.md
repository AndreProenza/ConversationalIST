# ConversationalIST

A mobile text-based chat application for Android Operating Systems.

![ConversationalIST2](https://user-images.githubusercontent.com/78174997/168442370-834eda47-5517-472b-b0ca-ecc2ae1b5069.png)

## Introduction

ConversationalIST is at powerful messaging application which explores several key aspects of mobile development, including location awareness, judicious use of limited resources, and social behavior.

ConversationalIST user communication is centered around chatrooms, which users can create, join, participate in, or leave. Once a user has access to a chatroom, they can participate in the shared conversation and review older chat history, even retroactively prior to joining. Conversations can include a variety of media to aid in user communication, including text, photos, files, polls, and geographic locations.

---

# Table of Contents
1. [Introduction](#introduction)
2. [Features](#features) 
3. [Extra Features](#extra-features)
4. [Processing and Saving Data](#processing-and-saving-data)
5. [Moderation and resource management](#moderation-and-resource-management)
6. [Context Awareness and Privacy](#context-awareness-and-privacy)
7. [Caching](#caching)
8. [Getting Started](#getting-started)
9. [Prerequisites](#prerequisites)
10. [Setup](#setup)
11. [Used Technologies](#used-technologies)
12. [References](#references)
13. [Authors](#authors)
14. [Information about the project statement and idea](#information-about-the-project-statement-and-idea)

---

## Features

| Feature                          | Definition                
|:--------------------------------:|:---------------------------------
|Pick Username                     | Allow users to pick a unique username or handle to identify themselves in a conversation. This user ID should then be used to clearly mark their contributions to the shared conversation, along with additional meta-data like timestamps. 
|Create new chatrooms or join one  | Users can create new chatrooms or join existing ones. The user should have easy access to a list of chatrooms they have joined, allowing them to easily check which have unread messages and to quickly switch between them.
|Watch chatroom content            | After selecting a chatroom, users should be able to see the content published there. Chatrooms can be seen as living append-only documents in that once a user has access to the chatroom, they should be able to see all of its past content, including content submitted before they joined
|Ability to submit media           |   Users should be able to submit a variety of simple media, including: Simple text messages.Photos taken from the phone’s camera. Geographic locations, to be shown as an embedded map with a button to request directions to the given location (e.g. by opening up Google Maps or other similar application). The user sharing a location should be able to specify the location by selecting it on a map, searching an address, or using the phone’s current location.
| Notify user via a Notification   | If a message is sent to a chatroom the user has access to but the user is not actively watching, notify them via a Notification. Tapping the notification should take them directly to the specific chatroom to see the new messages.

<img src="https://user-images.githubusercontent.com/78174997/168443250-d94029eb-9c45-43df-872c-7cb44c7fe0cc.jpg"/>


## Extra Features

| Feature                          | Definition                
|:--------------------------------:|:---------------------------------
| Extra feature  1                 | Feature content
| Extra feature  2                 | Feature content
| Extra feature  3                 | Feature content

---

## Processing and Saving Data

ConversationalIST supports a number of features that build on explicit data sharing and crowd-sourcing among multiple devices. To enable such functionality we have a back-end service that holds and processes shared data (e.g. chatrooms) and that each device communicates with to synchronize its state.

The back-end service is implemented via a RESTful service and the data is persisted in MongoDB database.

---

## Moderation and resource management

In ConversationalIST, conversations are sync across users and devices in a timely manner while also using the network efficiently. When the user is actively viewing a chatroom, we ensure that any new content shows up quickly. If the user disengages from the application, we use more efficient messaging to save network resources, even if at the expense of increased latency.

ConversationalIST avoids using resources unnecessarily. As conversations in a chatroom grow overtime, users are less likely to scroll up to older messages. We avoid wasting resources by only downloading data related to UI elements as they become visible to the
user.

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

## Getting Started

The following instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

---

## Prerequisites

- Prerequisite 1
- Prerequisite 2
- Prerequisite 3

---

## Setup

- Instruction 1
- Instruction 2
- Instruction 3

---

## Used Technologies

* [Java](https://openjdk.java.net/) - Programming Language;
* [Android Studio](https://developer.android.com/studio) - Unified environment to build Android Apps;

---

## References

- [Some ref](someref)

---

## Authors

* **André Proença** - [GitHub](https://github.com/AndreProenza)
* **Bernardo** - [GitHub](https://github.com/bernacv)
* **Name2** - [GitHub]()

---

## Information about the project statement and idea 

Project statement and idea were provided by [Instituto Superior Técnico](https://tecnico.ulisboa.pt/en/), however the system was developed by us.

---
