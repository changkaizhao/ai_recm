# Introduction

## 1.  Config data files
You can put all json data files in somewhere and mounting them to docker volume, but recommend putting them in **data** directory, and modify all files path in
`data/data.json`, indicates each datafilepath to the specific filepath which file located.

## 2. Log file 
You can find **log** file in `log` directory which located in `workspacedir`.



## 3. Sending Test message

You can use `http` `POST` method to send message to Solace queue.
·
>API:  10.194.102.62:8080/SolMessager/msg

requeset data form as below
```json
{
  "bet_acc_no": "3540498",
  "request_id": "REQUESTID_PLACEHOLDER",
  "meeting_id": "20210716",
  "betslip_display_num": 2,
  "betconfirmation_display_num": 3,
   "bettype_display_num":{
    "WIN_PLA_W-P":2,
    "QIN":2,
    "QPL":2,
    "QQP":2,
    "FCT":2,
    "TCE":2,
    "TRI":2,
    "FF":2,
    "QTT":2,
    "CWA":2
   },
  "betfilter": [
     {
        "meeting_id": "20210716",
        "race_id": "RACE_20210314_0001",
        "venue": "ST",
        "race_no": "1",
        "pool": {
          "pool_type": "WIN",
          "race_no": [
            "1"
          ],
          "sel": [
            "6"
          ]
        }
     },
     {
        "meeting_id": "20210716",
        "race_id": "RACE_20210314_0008",
        "venue": "ST",
        "race_no": "3",
        "pool": {
          "pool_type": "QPL",
          "race_no": [
            "3"
          ],
          "sel": [
            "3",
            "4"
          ]
        }
     }
  ],
  "race_no":7,
  "start_time": "2021-06-09T14:30:00+08:00"
}
```


[INFO ] 2022-10-17 17:02:10,683(237003) --> [Context_2_jms_1_ConsumerDispatcher] com.HKJC.Main$1.onMessage(Main.java:188
): Properties: JMS_Solace_DeliverToOneJMS_Solace_DeadMsgQueueEligibleJMS_Solace_ElidingEligibleSolace_JMS_Prop_IS_Reply_
Message

