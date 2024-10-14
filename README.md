# WayToday - Anonymous GPS tracker

[WayToday](https://way.today) is a service for tracking and sharing locations with two key differences: 
 - it allows users to share not only their current position but also track movement in real-time
 - it doesn’t require creating accounts or passwords, ensuring complete anonymity.

Additionally, WayToday was designed and implemented as a tool for drivers, making it extremely simple—its interface 
consists of just 2 buttons, yet it remains reliable: it monitors network status, providing visual and audio feedback 
on the GPS signal and mobile network status. The service only stores the last 500 positions with an adjustable interval, 
which are automatically deleted after 2 weeks of inactivity.

To view a location, the web service does not require the installation of additional applications, and the 
track link can be shared not only through text messengers but also verbally, by sharing a
unique 4-digit code from the tracker app.

Moreover, the web service allows tracking the movement of multiple tracker owners simultaneously.

## Android application
WayToday Android is an android application to

 - request tracking ID from the web-service (gRPC)
 - turn on and turn off GPS sensor
 - send the recorder GPS position (Kalman-filtered) to web-service
 - manage the interval of GPS position recording (gRPC)
 - monitor the status of GPS sygnal and mobile network
 - sahre the link to the current track

## Implementatio detatils

 The application uses [WayTodaySDK-Android](https://github.com/s4ysolutions/WayTodaySDK-Android) framework and
 provides UI for the tracking functionality. 

 For sake of the performance and battery life the code is writtine in Java, but also uses RxJava for
 simplifying UI updates.
