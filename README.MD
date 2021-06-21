# HomeEntity

[![996.icu](https://img.shields.io/badge/link-996.icu-red.svg)](https://996.icu)
[![LICENSE](https://img.shields.io/badge/license-Anti%20996-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE)
[![DevelBuild](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.thelama.cn%2Fjob%2FHomeEntity-Devel%2F&label=Devel%20Build)](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.thelama.cn%2Fjob%2FHomeEntity-Devel%2F&label=Devel%20Build)
[![StableBuild](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.thelama.cn%2Fjob%2FHomeEntity%2F&label=Stable%20Build)](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.thelama.cn%2Fjob%2FHomeEntity%2F&label=Stable%20Build)
[![Server](https://hello-minecraft-api-production.up.railway.app/api/ping-svg/2704ba23.123nat.com:26588 )](https://hello-minecraft-api-production.up.railway.app/api/ping-svg/2704ba23.123nat.com:26588)

A wonderful minecraft server utils, which is written in Kotlin

- Do you want to join us?
	- Telegram -> https://t.me/joinchat/SQh32oidbCM92uy
	- Discord -> https://discord.gg/SffQRJpfWW

## 使用

注:
- 被<>括起来的参数代表该参数为必填
- 被[]括起来的参数代表选填(可不填)
- 被|分割的参数代表选其一
- 若有地方存在可支持但不支持的Tab补全则会用!Tab!进行标记

#### 验证前可用

- .l <密码> *登录*
- .reg <密码> <重复密码> *注册，未注册过请先注册!*

#### 验证后可用

- /hent `[sync]` `[...]` *如果参数为空或无法处理则输出版本号*
	- sync `<HomeEntity|HomeEntity-Devel>` *从指定行项目更新插件*
- /warp `<名字|list|lookup|add|del>` *传送到一个设置的位置*
	- list *列出全部可以传送的warp点 (加关键词参数启用筛选器, 如 `/warp list lsp` )*
	- lookup *查询一个可以传送的点*
	- add *创建一个传送点*
	- del *删除一个传送点*
- /slime `<范围(-5 ~ 5)>` *查找附近的史莱姆区块*
- /session `<limit|remove|reset|maintainer|revoke>` `[...]` *会话管理 (普通玩家请勿尝试使用)*
	- limit *限制玩家数据包，玩家和服务器的通讯只留存 S16 S03 C31 C14 这四个数据包*
	- remove *解除限制，发送限制期间所有服务器发送的数据包*
	- reset `<密码>` *修改密码*
	- maintainer `<玩家名字>` *添加一名维护者*
	- revoke *删除一名维护者*
	- login *为一名玩家登录*
	- crash *让一名玩家崩端 (相当于该玩家本人执行 `/exit crash` )*
- /exit `[crash]` *退出服务器*
	- crash *崩端 (让客户端无响应, Linux 用户请切换 tty 使用 `pkill java` 结束游戏进程, Windows 用户基本无解 或许也可以试试 C+A+D ?)*
- /back *返回上一个地点 (只有当死亡、传送时才会记录上一个地点)*
- /show *给大家展示你的装备*
- !p `<玩家名字>` `[文本]` *加密通信，只有你和目标玩家能看到，服务器后台都看不见 (**不是/p**)*
- /tpa `<玩家名字>` *发送一个传送到另一个玩家的请求*
- /tphere `<玩家名字>` *发送一个传送另一个玩家 **到这里** 的请求*

#### 请勿使用

**请不要直接使用这些指令，也不要询问开发人员如何使用，正常游戏中没有必要使用**

- /tpaccept <go|here> <uuid> 接受tpa/tphere请求
- /tpdeny <go|here> <uuid> 拒绝tpa/tphere请求

## 更新计划

+ 更改 TelegramAPI 支持库到 InsanusMokrassar/TelegramBotAPI ⭕
+ TPA ✅
+ 全部可配置化 ⭕
+ 更改版本检查方法到 git commit ⭕
+ 外置第三方登录 ⭕
+ TG玩家绑定 ⭕
+ TG执行指令 ⭕
+ TG访问后台 ⭕

## 贡献代码

首先, 需要一个 GitHub 账号, 并熟悉 Kotlin / Gradle / Git / Bukkit API 基本用法以及熟悉 Minecraft 这款游戏,
否则 Pull Request（后文简称PR）可能会遭到拒绝.

接下来，在 GitHub 中 fork 这个仓库并将 `devel` 分支克隆到本地.

然后，在自己的仓库对代码进行一些修改.

最后，打开这个仓库的PR界面并新建一个PR，填写想说的话和更新内容并提交.

**我们欢迎任何一个人对我们的项目进行贡献，感谢所有对此仓库做出过贡献的人 :)**
