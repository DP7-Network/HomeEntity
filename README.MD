### HomeEntity 

[![996.icu](https://img.shields.io/badge/link-996.icu-red.svg)](https://996.icu)
[![LICENSE](https://img.shields.io/badge/license-Anti%20996-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE)
[![Master](https://github.com/Lama3L9R/HomeEntity/actions/workflows/master.yml/badge.svg)](https://github.com/Lama3L9R/HomeEntity/actions/workflows/master.yml)

```
A wonderful minecraft server utils code in kotlin

Do you want join us? 
   Join Us on Telegram -> https://t.me/joinchat/SQh32oidbCM92uy
```
---
### 使用:
```
  注: 被<>括起来的参数代表该参数为必填
      被[]括起来的参数代表选填
      被|分割的参数代表选其一
      若有地方存在可支持但不支持的Tab补全则会用!Tab!进行标记

  1. /warp <名字|list|lookup|add|del> 传送到一个设置的位置
    list -> 列出全部可以传送的warp点
    lookup -> 查询一个可以传送的点
    add -> 创建一个传送点
    del -> 删除一个传送点
  2. /slime <范围(0 - 5 如果你想你可以试试填写负数，其实是一样的)> 查找附近的史莱姆区块
  3. /session <limit|remove|reset|maintainer|revoke> [...]
    limit -> 限制玩家数据包，玩家和服务器的通讯只留存 S16 S03 C31 C14 这四个数据包
    remove -> 接触限值，发送限制期间所有服务器发送的数据包
    reset <密码> -> 修改密码
    maintainer -> 添加一个维护者(HomeEntity中最高权限)
    revoke -> 删除一个维护者
    login -> 为一名玩家登录
    crash -> 让一名玩家未响应
  4. /exit [crash] 根据Maxel要求写了个退出服务器用的指令
    crash -> 让你崩端（大概吧，用的十几年前的崩端方法）
  5. /back 返回上一个地点 只有当死亡、传送时才会记录上一个地点
  6. /show 给大家展示你的装备
  7. !p <玩家名字> [文本] 加密通话，只有你和目标能看到，服务器后台都看不见（不是/p!!!!）

```

 