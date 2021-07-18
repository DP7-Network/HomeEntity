# HomeEntity 

[![996.icu](https://img.shields.io/badge/link-996.icu-red.svg)](https://996.icu)
[![LICENSE](https://img.shields.io/badge/license-Anti%20996-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE)
[![Server](https://hello-minecraft-api-production.up.railway.app/api/ping-svg/2704ba23.123nat.com:26588 )](https://hello-minecraft-api-production.up.railway.app/api/ping-svg/2704ba23.123nat.com:26588)

Minecraft 服务器插件, DP7 Charmless 服务器专属 欢迎加入.DP7聊天群: [`Telegram`](https://t.me/joinchat/raRF9e8WOs84NzM1)

**关于服务器插件自动更新没了: 因为CI拥有者Lama3L9R家境贫寒没钱续费导致服务器无了. 正在尝试切换到免费的CI**

---
各种指令使用方法
---
注:
- 被<>括起来的参数代表该参数为必填
- 被[]括起来的参数代表选填(可不填)
- 被|分割的参数代表选其一
- 若有地方存在可支持但不支持的Tab补全则会用!Tab!进行标记

#### 验证前可用

- .l <密码> *登录*
- .reg <密码> <重复密码> *注册，未注册过请先注册!*

#### 验证后可用

- /prefix `[前缀|玩家名 <前缀>]` *设置玩家的前缀*
  - 前缀 *设置自己的前缀*
  - 玩家名 `<前缀>` *设置某个玩家的前缀*
- /cat `[reset]` *输出群猫猫的体重*
  - reset *拥有者 `Lama3L9R` 专用指令 用于重置猫猫*

- /feed *投喂猫猫*

- !Tab! /motd `[set|rm]` *如果参数为空则输出当前motd*
    - set `<行号> <内容...>` 设置motd某一行的内容, 支持&作为颜色符号
	- rm `<行号>` 清空motd某一行的内容

- /warp `<名字|find|detail|set|rm|set-des|list|share>` *输出一个记录点的位置 若名字颜色为绿色则为主世界 红色为地狱 紫色为末地*
	- find `<关键词>` *查找记录点,通过关键词查找 如 `/warp list lsp` )*
	- detail *查询一个记录点*
	- set `<名字> <x> <y> <z> [描述] [force]` *在指定位置创建一个记录点 携带force则直接覆盖已存在的记录点*
	- set `<名字> [描述] [force]` *创建一个记录点 携带force则直接覆盖已存在的记录点*
	- rm `<名字> [force]` *删除一个传送点*
	- set-des `<名字> <内容>` *设置一个记录点的描述信息*
	- list `[页码]` *列出全部记录点*
	- share `<名字>` *和大家分享一个你的记录点*

- /slime `<范围(-5 ~ 5)>` *查找附近的史莱姆区块*

- !Tab! /auth `<limit|remove|reset|login>` `[...]` *登陆验证控制指令 管理员专属*
	- limit `玩家名` *限制玩家数据包，玩家和服务器的通讯只留存 S16 S03 C31 C14 这四个数据包*
	- remove `玩家名` *解除限制，发送限制期间所有服务器发送的数据包*
	- reset `<密码>` *修改密码*
	- login *为一名玩家登录*

- !Tab! /admin `<maintainer|revoke|crash>` *管理员专属指令*
  - crash *让一名玩家崩端 (相当于该玩家本人执行 `/exit crash` )*
  - maintainer `<玩家名字>` *添加一名维护者*
  - revoke *删除一名维护者*

- /exit `[crash]` *退出服务器*
	- crash *崩端 (让客户端无响应, Linux 用户请切换 tty 使用 `pkill java` 结束游戏进程, Windows 用户基本无解 或许也可以试试 C+A+D ?)*\
	  1.17.1 Windows环境测试: 游戏会直接退出
	  
- /back *返回上一个地点 (只有当死亡、传送时才会记录上一个地点)*
  
- /show `[helmet|chestplate|leggings|boots|offhand|byid|inv]` *给大家展示你的装备*
  - helmet|chestplate|leggings|boots|offhand 展示不同部位的装备
  - inv `WIP`
  - byid **请不要直接使用!!**	
	
- !p `<玩家名字>` `[文本]` *加密通信，只有你和目标玩家能看到，服务器后台都看不见 (**不是/p**)*

- /tpa `<玩家名字> [confirm]` *发送一个传送到另一个玩家的请求*

- /tphere `<玩家名字> [confirm]` *发送一个传送另一个玩家 **到这里** 的请求*

- /relay `<on|off|status|toggle>` *控制聊天消息同步功能*
	- on *同步你的消息到 Telegram*
	- off *不同步你的消息到 Telegram 同步消息*
	- status *查看自己的消息同步状态*
	- toggle *切换 同步/不同步 自己的消息到 Telegram*
	- say *以Bot身份发送信息 管理员专属*
	- restart *重新启动Bot(强制杀死Bot运行所在CoroutineScope) 管理员专属*

#### 请勿使用

**请不要直接使用这些指令，也不要询问开发人员如何使用，正常游戏中没有必要直接使用**

- /tpaccept `<go|here>` `<uuid>` 接受tpa/tphere请求
- /tpdeny `<go|here>` `<uuid>` 拒绝tpa/tphere请求

#### 控制台专属
- /hent `[sync|crash]` `[...]` *如果参数为空或无法处理则输出版本号*
	- **暂时无法使用** sync `<HomeEntity|HomeEntity-Devel>` *从指定行项目更新插件*
	- crash 强制使JVM崩溃 **谨慎使用**
	
### 其他插件功能:
- @<玩家名> *给玩家发送一个提醒title, 在聊天区使用, 玩家名后请留一个空格*

tpa / tphere 计价方法: 
```
target -> 目标玩家
requester -> 请求玩家

              向上取整(平方根(平方(目标玩家x坐标 - 请求玩家x坐标) + 平方(目标玩家y坐标 - 请求玩家y坐标)) / 100)
传送扣除血量 = ceil(sqrt((target.x - requester.x).pow(2.0) + (target.z - requester.z).pow(2.0)) / 100)
```
![formula](https://latex.codecogs.com/gif.latex?TeleportCost%20%3D%20%5Clceil%20%5Csqrt%20%7B%28x_%7Btarget%7D%20&plus;%20x_%7Brequester%7D%29%5E2%20&plus;%20%28z_%7Btarget%7D%20&plus;%20z_%7Brequester%7D%29%5E2%7D%20%5Cdiv%20100%20%5Crceil)

## 更新计划

- 核心功能
  - 加急
	+ [ ] `WIP` 切换到免费的CI并恢复自动更新 -> pr:gh-actions-test to master
	+ [ ] 指令帮助
  + [x] 翻新warp功能, 适配群投票中决定的更新内容
  + [x] TPA成本
  + [ ] 全部可配置化
  + [ ] 更改版本检查方法到 git commit
  + [ ] `WIP` 外置第三方登录 -> [HomeEntity-Yggdrasil](https://github.com/DP7-Network/HomeEntity-Yggdrasil)
- RelayBot
  + [x] 更改 Telegram API 支持库到 InsanusMokrassar/TelegramBotAPI
  + [ ] TG玩家绑定

## 贡献代码

首先, 需要一个 GitHub 账号, 并熟悉 Kotlin / Gradle / Git / Bukkit API 基本用法以及熟悉 Minecraft 这款游戏,
否则 Pull Request（后文简称PR）可能会遭到拒绝.

接下来，在 GitHub 中 fork 这个仓库并将 `devel` 分支克隆到本地.

然后，在自己的仓库对代码进行一些修改.

最后，打开这个仓库的PR界面并新建一个PR，填写想说的话和更新内容并提交.

**我们欢迎任何一个人对我们的项目进行贡献，感谢所有对此仓库做出过贡献的人 :)**
