Detector software is a quality monitoring tool for factory production processes. This software includes the MQTT functionality of the Scanner software, which has been adapted and added to the Detector software:
APP scans the label barcode on the body of a specific device, and checks whether the heartbeat field OEMID sent by BLE of this device corresponds to it.
After successful verification, APP will search the product table in this batch and mark "OK".
At the same time, APP releases to "FACTORY MQTT" the title and message of "heartbeat" of this product, and finally the initial heartbeat record of the cloud device "Digital Twin AVATAR". Heartbeat Recording. This function requires a GSM or WIFI signal on the mobile phone. When I i signal, the phone's internal MQTT will retain the message to know that there is a signal is released.
To prevent problems with unreliable mobile phone signals on the production floor, it is also recommended that a list of all products in this batch is kept within the working mobile phone. Each time a BLE message is received, the "BLE Detected" field of the corresponding product on the list is stamped with a time stamp. If several mobile phones are in use at the same time, the production QA staff agrees to consolidate them into a single table. The aggregated mobile phones are responsible for posting the "heartbeat" headers and messages of all products in sequence to "FACTORY MQTT" when a signal is available.
The QC department verifies the AVATAR heartbeat acquisition status of the devices in the batch to determine if any have been left behind.


Detector软件为工厂生产过程中质量监控工具。此软件包含Scanner软件有关MQTT的功能。Detector功能调整增加：
APP扫描具体设备机身上的标签条形码，同时核对此设备BLE发出的心跳字段OEMID是否对应。
核对成功后，APP在此批次中产品表格寻找，并标注”OK“
同时APP 向 ”FACTORY MQTT“发布对用此产品的”心跳“标题和报文，最终云端设备”数字双生AVATAR“的初始心跳记录。此功能需要手机有GSM或WIFI信号。无信号时，手机内部的MQTT会保留报文知道有信号时发布。
为防止生产车间手机信号不可靠的问题，同时建议在工作手机内保留一份此批次的全部产品清单。每次接收到一个BLE报文，同时对清单对应此产品的”BLE Detected"字段写入时间盖章。如果多部手机同时使用，
生产质检人员约定统一汇总到一份表格。汇总手机在有信号时负责依次向 ”FACTORY MQTT“发布全部产品的”心跳“标题和报文,
质检部门通过验证批次中设备AVATAR心跳获取状态，判断是否有遗落。
