[Русский текст](https://github.com/primesoftpro/exhibition-android/wiki)

# Online exhibition mobile app

The mobile application for exhibitions.
It contains all necessary information about the exhibition: events, description of events and the demonstrations, 
news, photos and exhibition samples, participants, delegates, videos and more. 
The application allows visitors to share contact information, save and share open information about the companies.
Special attention is given to the offline-operation with supporting the data synchronization with application backend.

Key features:

- events & details
- organizations catalog
- delegates / persons
- exhibition samples & details
- categories
- news (RSS)
- media gallery
- integration with personal calendar
- search
- share and save images, files
- offline database (syncronization with server)
- image caching

Mobile apps based on this project:
- Army-2015
- [Army-2016](https://play.google.com/store/apps/details?id=ru.gkpromtech.parkpatriot)

## Screenshots

<p align="center">
  <img src="/screenshots/Screenshot2.png" width="200"/>
  <img src="/screenshots/Screenshot1.png" width="200"/>
  <img src="/screenshots/Screenshot4.png" width="200"/>
  <img src="/screenshots/Screenshot3.png" width="400"/>
  <img src="/screenshots/Screenshot5.png" width="400"/>
</p>

## Prerequisites/SDK

- Android Studio 1.5+
- minSdkVersion 16

## Used libraries

- appcompat-v7
- support-v13
- [horizontallistview](https://github.com/sephiroth74/HorizontalVariableListView)

## How to use

### Step 1

You have to install backend that will provide Android application with all required content.

You may use your own backend or project [Exhibition-server](https://github.com/primesoftpro/exhibition-server).

### Step 2

Clone project and update application UI theme. All you need is to update resource files.

Styles (```styles.xml```):

- actionBarStyle
- colorPrimary
- colorPrimaryDark
- windowBackground
- textAppearanceSmall
- textAppearanceMedium
- textAppearanceLarge
- buttonStyle
- imageButtonStyle
- listViewStyle
 
Colors (```color.xml```):
 
- Exhibition.Primary
- Exhibition.Secondary
- Exhibition.Background
- ListViewOddRow

Launcher icon:

- minimap
 
### Step 3

Now we need to change a base URL that is used for talking with a backend and other static properties.

Please see class ```SharedData.java```

- SERVER_URL - Base server URL
- REST_SERVER_URL - Backend server URL
- EXTERNAL_DIR - Cache dir for images and video

### Step 4 (optional)

Prepare/modify offline database for application. Run script from ```android/app/tools```.

> ./gen_db_sqlite

Script requests remote database for data (key ```host```). You need to modify it on your own backend server.

### Step 5

Application also has Google Analytics. 
To track the statistics please modify key in class ```SharedData.java```.

>GOOGLE_ANALYTICS_TRACKING_ID = "UA-XXXXXXXX";


## License

Copyright 2016 primesoftpro

https://primesoftpro.ru

info@primesoftpro.ru

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
