### HomeEntity

[![996.icu](https://img.shields.io/badge/link-996.icu-red.svg)](https://996.icu)
[![LICENSE](https://img.shields.io/badge/license-Anti%20996-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE)
[![DevelBuild](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.thelama.cn%2Fjob%2FHomeEntity-Devel%2F&label=Devel%20Build)](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.thelama.cn%2Fjob%2FHomeEntity-Devel%2F&label=Devel%20Build)
[![StableBuild](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.thelama.cn%2Fjob%2FHomeEntity%2F&label=Stable%20Build)](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.thelama.cn%2Fjob%2FHomeEntity%2F&label=Stable%20Build)
[![Server](https://hello-minecraft-api-production.up.railway.app/api/ping-svg/2704ba23.123nat.com:26588 )](https://hello-minecraft-api-production.up.railway.app/api/ping-svg/2704ba23.123nat.com:26588)
```
A wonderful minecraft server utils code in kotlin

Do you want join us? 
   Join Us on Telegram -> https://t.me/joinchat/SQh32oidbCM92uy
```
---
使用
---
```
注: 被<>括起来的参数代表该参数为必填
      被[]括起来的参数代表选填
      被|分割的参数代表选其一
      若有地方存在可支持但不支持的Tab补全则会用!Tab!进行标记
 
验证前:
  0. .l <密码> 登录
  1. .reg <密码> <重复密码> 注册，没注册过的请先注册!
验证后:
  0. /hent [<sync>] [...] 如果参数为空或无法处理则输出版本号
    sync <HomeEntity|HomeEntity-Devel> -> 从指定行项目更新插件
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
  8. /tpa <玩家名字> 发送一个传送到另一个玩家的请求
  9. /tphere <玩家名字> 发送一个传送另一个玩家到这里的请求
  
并非玩家直接使用的指令:
  **请不要直接使用这些指令，也不要问开发人员如何使用，你也没必要去钻研如何使用**
  /tpaccept <go|here> <uuid> 接受tpa/tphere请求
  /tpdeny <go|here> <uuid> 拒绝tpa/tphere请求
```
---
更新计划
---
+ 更改TelegramAPI支持库到 InsanusMokrassar/TelegramBotAPI ⭕
+ TPA ✅
+ 全部可配置化 ⭕
+ 更改版本检查方法到git commit ⭕
+ 外置登录 ⭕
+ TG玩家绑定 ⭕
+ TG执行指令 ⭕
+ TG访问后台 ⭕

---
贡献代码
---
首先，您需要一个Github账号，您需要熟练地使用Kotlin、Gradle、Git、Bukkit API以及熟悉Minecraft，否则您的Pull Request（后文简称PR）可能会遭到拒绝

接下来，您需要在Github中fork这个仓库并clone下来master分支

然后，在您的仓库对我们的代码进行一些修改

最后，打开主仓库页面的PR界面并新建一个PR，填写一些你想说的话和更新内容并提交。

**我们欢迎任何一个人对我们的项目进行贡献，感谢所有对此仓库做出过贡献的人 :)**