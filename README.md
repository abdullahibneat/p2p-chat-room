# Peer-to-peer Chat Room
![Main screen](https://i.imgur.com/Chp5wys.png)

## Overview
This coursework required the writing of software for a peer-to-peer chatroom where people can send messages and communicate with each other. I implemented a distributed, decentralized network using the Java programming language and the NetBeans IDE.
The provided brief listed specific requirements that were to be met through this project:

- Network must be peer-to-peer, allowing communication between all members
- New members only need to provide an ID, a port to listen to, and port and IP address of an existing member.
- The first member to join is the coordinator, and they must be notified about this.
- The network must be able to select a new coordinator when the previous coordinator becomes unavailable

The role of the coordinator is to maintain the state of the group (i.e. ensure all members are online) and notify everyone when a member leaves the chatroom.
