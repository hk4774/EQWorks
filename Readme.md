# EQ-Works assignment

This is work sample given for the job position of Mobile Developer at EQ Works. The description of the work sample is [here](https://gist.github.com/woozyking/efecdb1d0e257b1300596957f4136f19).

## Features

- An standalone SDK in Kotlin for logging user's location
- Feature to deliver result on desired thread
- Error logging if API failes due to any result
- Retry mechansim if API fails.
- Minimal and can be used universally (Android OR Pure Java/Kotlin)
- [httpbin](https://httpbin.org/) has been used for API
- Unit test cases using JUnit4

## HOW TO USE - LIBRARY

- Import the ws-mobile-kotlin in your existing project(File -> New -> Import Module)
- In your application/main class, initialize the library as below:
    ``` Library.instance.setUp(YourCustomeOkhttpClient)```
- Pass users LocationEvent (lat, long, time(optional), extradata(optional)) object to the log function as below:
    ```Library.instance.log(LocationEvent object)```

## THIRD PARTY LIBRARIES - DEMO
- [OkHttp](https://square.github.io/okhttp/) - Open source project designed to be an efficient HTTP client developed by Square Inc.
- [Gson](https://github.com/google/gson) - Used to convert Java Objects into their JSON representation developed by Google


## HOW TO TEST

- Import EQWorks project in Android Studion IDE(File -> New -> Import Project)
- Open file MockViewmodelTest under com.huk.eqworks package
- Right click on work space and hit Run MockViewmodelTest
- Observe the result

## HOW TO USE - DEMO

- Import EQWorks project in Android Studion IDE(File -> New -> Import Project)
- From configurations, select app if not selected.
- Select the device you want to run on and click the run button next to device selection.
- Click on Send location button in application to log the location to the server.

## THIRD PARTY LIBRARIES - DEMO
- [CoLocation](https://github.com/patloew/CoLocation) - For getting location from all possible providers in Android using kotlin coroutines
- [OkHttp](https://square.github.io/okhttp/) - Open source project designed to be an efficient HTTP client developed by Square Inc.
- [Android Architecture Componnets](https://developer.android.com/topic/libraries/architecture) - Android architecture components for morder way of android development

## TODO

- Add security
- Handle various other methods such as PUT/DELETE/GET

## Acknowledgement
**Feel free to contact me if you have any question regarding code running.**