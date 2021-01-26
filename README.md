# Chat App

### Features Supported by the App
- Login and registration for users
- Token generation for email verification
- Usage of RESTful login using login tokens
- Authorization Header Verification
- Group chatting/ One-to-one chatting
- Ability to fetch messages
- Email notification for messages received


### Running Instructions
> Run this app by heading to __Routes.scala__ and running it (Routes is an object defined, you may run it directly).

### External Dependencies

##### Akka Essentials
- Akka Actors 2.5.32
- Akka Stream 2.5.32
- Akka HTTP 10.2.2

##### MongoDB Dependencies for DB Operations
- Mongo Scala Driver 2.9.0
- Alpakka MongoDB 2.0.2

##### Spray JSON Dependency for implicit data type conversions
- Akka HTTP Spray JSON 10.2.2

##### JWT dependency for token generation
- Authentikat JWT 0.4.5
- Nimbus Jose JWT 9.3

##### Scala Test Dependency
- Scala Test 3.2.2

##### SMTP dependency for sending mails
- Daddykotex Courier 3.0.0-M2

##### Scala Logging
- Scala Logging 3.9.2
- Logback Classic 1.2.3
- Logback Core 1.2.3

##### Encryption Library
- Scalacrypt 0.5-SNAPSHOT

### Plugins Applied
- SBT CPD
