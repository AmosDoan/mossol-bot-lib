# MOSSOL-BOT-LIB 
[![Build Status](https://travis-ci.org/AmosDoan/line_bot_mossol.svg?branch=master)](https://travis-ci.org/AmosDoan/line_bot_mossol)

* This project is started to support the integrated, cross platform Bot Framework.
* Currently, we support LINE messenger & Slack
  * LINE Bot logic is implemented by using [Armeria](https://line.github.io/armeria/), open source from LINE Corp.
  * Slack Bot logic is implemented by using [JBot](https://github.com/rampatra/jbot) (Would be replaced with our own implementation)
* Messaging data is managed by [CentralDogma](https://line.github.io/centraldogma/), open source from LINE Corp.

* This project is consists of 2 parts
  * Library - basic library to create your own bot
  * Bot - bot example by using the library 

## Library

* Basically, we provide connection logic with LINE Bot server & Slack Server
* There are some default messaging controllers provide below logic :
  * SimpleMatcherService - Exact match with receiving message and send mapping reply message
    * The message information to be matched and sent is managed by CentralDogma 
  * RegexMatcherService - Regex match with receiving message and send mapping reply message
    * The message information to be matched and sent is managed by CentralDogma 
* MenuServiceHandler - There are one special service logic to recommend menu

### How to use

* You can add your own matcher and message to be sent on CentralDogma 
* If you want to create your own function for messaging, you create just your own service bean implement MatcherService interface

### How to import 

* We provide maven central repository 


#### Maven

```
<dependency>
  <groupId>net.mossol.bot</groupId>
  <artifactId>line_bot_mossol-lib</artifactId>
  <version>0.0.3.2</version>
</dependency>
```

#### Gradle

```
implementation 'net.mossol.bot:line_bot_mossol-lib:0.0.3.2'
```

### How to build

```bash
$ ./gradlew build
```


## Mossol Bot 

* This bot is the example by using mossol-bot-lib
* Below is description for Mossol-Bot

* 귀여운 메뉴 추천 BOT Mossol
* Mossol이는 메뉴 결정장애가 있는 라인 직원들을 위해 태어났습니다 
    * 안녕이 포함된 문구 => Mossol이가 인사로 화답해줍니다
    * 메뉴후보 => 서현역 근처에서 갈만한 리스트를 보여줍니다
    * 메뉴골라줘 => 랜덤으로 서현역 근처 메뉴리스트에서 하나를 점지해줍니다 
    * 일본메뉴후보 => 일본 메뉴 리스트를 보여줍니다
    * 일본메뉴골라줘 => 랜덤으로 일본 메뉴 하나를 점지해줍니다
    * 메뉴추가 [메뉴명] => 메뉴를 리스트에 추가합니다
    * 메뉴삭제 [메뉴명] => 메뉴를 리스트에서 삭제합니다 
    * 일본메뉴추가 [메뉴명] => 메뉴를 일본 메뉴 리스트에 추가합니다
    * 일본메뉴삭제 [메뉴명] => 메뉴를 일본 메뉴 리스트에서 삭제합니다

* Data 저장소
    * Bot Mossol에서 사용되는 Data는 다음 Repo에서 관리됩니다.
    * https://github.com/AmosDoan/line_bot_mossol_central_dogma


