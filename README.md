# Stronger Wanderer

给**原版坚守者**增加五种手臂技能。保留原版模型、骨骼、贴图、生命值、攻击力和通常的感知/战斗行为。客户端和服务端均需安装对应版本。

## 版本与构建

| Minecraft | 加载器 | Java | 定位 |
| --- | --- | --- | --- |
| 1.20.1 | Forge 47.4.10 | 17 | 主版本、默认编辑版本 |
| 1.21.1 | NeoForge 21.1.235 | 21 | 同步支持 |
| 26.1.2 | NeoForge 26.1.2.109 | 25 | 同步支持 |

“26.12”按 Minecraft **26.1.2** 实现。

工程采用 Stonecutter 0.9.7 共享源码；Forge 使用 Architectury Loom 1.11.458，NeoForge 使用 ModStitch 0.8.5 / ModDevGradle。Gradle 9.5.0 的守护进程使用 Java 25，节点编译分别使用 Java 17/21/25。没有 GeckoLib 或其他游戏模组依赖。

```powershell
.\gradlew.bat build
```

输出在 `versions/<版本节点>/build/libs/`。安装不带 `-sources` 的 JAR：

- `strongerwanderer-forge-0.1.0+1.20.1.jar`
- `strongerwanderer-neoforge-0.1.0+1.21.1.jar`
- `strongerwanderer-neoforge-0.1.0+26.1.2.jar`

## 技能

重力劈跃默认在剩余生命 **50%** 时解锁。普通/追加狮子斩和两种幻影突进在两个阶段均可用，不更换本体模型。

| 技能 | 流程 | 默认伤害，以原版攻击力 A 为基准 |
| --- | --- | --- |
| 普通狮子斩 | 近身抬臂 → 锁定地面 → 向前翻身 → 双臂重砸 | 抬臂 0.45A、重砸 1A |
| 追加狮子斩 | 普通狮子斩后重新锁定 → 更快的第二次翻身重砸 | 1A |
| 重力劈跃 | 四个幻影依次跳劈 → 本体跳劈 → 两次扩大的重力爆发 | 幻影各 0.2A、本体 1.2A、爆发各 0.45A |
| 正向连续幻影突进 | 四个幻影依次冲刺 → 本体紧随最后一个幻影伸臂冲刺 | 幻影各 0.2A、本体 0.9A |
| 侧向幻影突进 | 三个侧向错开的幻影突进 → 本体转身伸臂突进 | 幻影各 0.2A、本体 0.9A |

默认普通狮子斩在目标距本体不超过 12 格时，有 40% 概率追加。狮子斩和重力劈跃冷却 12 秒，两种突进各冷却 6 秒，从该招式结束时计算。

目标距离不超过 **6 格 × 范围倍率** 时，优先交替选择两种可用突进；都在冷却才选择其他技能。选招先于原版 Brain 的追近/普攻执行。新技能之间默认保留 1.5 秒原版 AI，近战时最多 0.6 秒；所有技能不可用时仍使用原版战斗逻辑。远处目标优先正向突进。

| 技能 | 准备到本体位移 | 本体命中后的剩余时间 | 首次分身出场 |
| --- | --- | --- | --- |
| 普通狮子斩 | 0.50 秒 | 1.20 秒；触发追加则更早衔接 | — |
| 追加狮子斩 | 0.25 秒 | 1.45 秒 | — |
| 重力劈跃 | 2.20 秒 | 2.15 秒，包含两次爆发；末次爆发后 0.50 秒 | 0.85 秒 |
| 正向连续幻影突进 | 1.95 秒 | 1.05 秒 | 0.40 秒 |
| 侧向幻影突进 | 1.35 秒 | 0.95 秒 | 0.20 秒 |

以上按每秒 20 tick。缩短准备和收招时间，保留本体位移时长、分身间隔和完整攻击段。

### 动画与命中

- 所有动画只调整原版身体、头部、双臂和双腿的 ModelPart 变换；没有新增骨骼、武器或模型；分身在原版贴图上叠加青蓝色染色与半透明效果。
- 普通/追加狮子斩、重力劈跃及跳劈分身沿前进方向前空翻。使用 ModelPart 正 X 轴旋转，并以同一角度补偿原版脚底旋转中心，围绕身体中心旋转；竖直移动由服务端实体路径承担。
- 近战改为抬臂、伸臂、双臂下砸；收招在解除技能控制前回到原版静止姿态。
- 突进检测**上一位置到当前位置的完整路径**，每个攻击段每个目标只命中一次。每个幻影有独立计时和命中记录。
- 技能伤害使用独立数据包伤害类型，仅绕过原版受伤间隔，以保证不同幻影可连续命中。护甲、盾牌、抗性和外部无敌逻辑仍由原版处理。
- 判定在竖直方向额外覆盖上下各 5 格；跳劈默认半径 3.5 格，重力重砸 5 格，两次爆发为 5.5/7 格，突进路径半径 2 格。
- 本体追击上限：跳劈 14 格、突进 22 格；幻影 24 格。范围倍率同时作用于这些值。
- 本体使用原版碰撞检测，不会主动穿墙；寻找落点时检查完整身体空间、世界边界、已加载区块与地面。没有安全落点时向原位置收缩。
- 幻影不执行 AI、不保存、不掉落、不阻挡其他实体。死亡或卸载后不会留下独立敌人。

### 特效

使用原版幽匿灵魂粒子、反向传送门粒子、声波冲击粒子和坚守者音效。幻影保留原版坚守者几何和骨骼，直接使用原版坚守者贴图，并叠加青蓝色染色与约 45% 不透明度；保留胸骨、皮肤纹理和触须的透明裁切，资源包也可替换该贴图。贴图、染色与透明度通过原版半透明自发光管线一次绘制；关闭独立的脉动发光叠层和阴影，避免遮住半透明效果。没有技能指示器，也没有自定义着色器或屏幕后处理。

## 配置

首次正常运行生成 `config/strongerwanderer-common.toml`，注释包含中英文。全部配置集中在这一个文件。

| 配置项 | 默认值 | 含义 |
| --- | --- | --- |
| `enabled` | true | 启用新技能 |
| `phaseTwoHealth` | 0.5 | 重力劈跃解锁血量比例 |
| `damageScale` | 1.0 | 新技能伤害倍率 |
| `rangeScale` | 1.0 | 命中范围和最大追击距离倍率 |
| `recoverySeconds` | 1.5 | 技能间隔秒数；近战最多 0.6 秒 |
| `skillCooldownSeconds` | 12 | 同种主招式冷却秒数；两种突进各使用一半 |
| `followupChance` | 0.4 | 追加狮子斩概率 |
| `particles` | 100 | 粒子密度百分比，0 关闭 |
| `announceSkills` | false | 播报技能名 |

## 调试与验收

管理员命令可定向触发技能，目标必须是可攻击的存活生物；创造/旁观玩家不能作为目标。出土、钻地等原版特殊姿态期间不能强制施放。

```mcfunction
/strongerwanderer lion_claw @e[type=minecraft:warden,limit=1,sort=nearest] @p
/strongerwanderer followup_claw @e[type=minecraft:warden,limit=1,sort=nearest] @p
/strongerwanderer gravity_leap @e[type=minecraft:warden,limit=1,sort=nearest] @p
/strongerwanderer forward_phantoms @e[type=minecraft:warden,limit=1,sort=nearest] @p
/strongerwanderer side_phantoms @e[type=minecraft:warden,limit=1,sort=nearest] @p
```

主版本的隔离运行验证：

```powershell
.\gradlew.bat :1.20.1-forge:runGameTestServer -PswGameTest
```

此验证使用 `build/gametest`，覆盖五种技能的伤害、实际追击、自然结束和幻影回收，以及满血近战自动优先选择并交替使用两种突进。GameTest 源码和测试场地不会进入默认构建产物。验证后使用普通 `build` 生成正式 JAR。

### 已验证与待验收

- 三版本 Java 编译和默认构建通过。幻影原版贴图、青蓝色染色、半透明与纹理透明裁切已在 Forge 1.20.1 实机画面中核对。
- Forge 1.20.1 独立世界验证通过：缩短后的五种技能均命中并移动到目标附近，完整收招后清除状态，无遗留幻影；满血近战自动优先选择正向突进，并在收招间隔后衔接侧向突进。
- 两个 NeoForge 版本执行过加载器/设置初始化；26.1.2 的 FML 启动器在 `--initSettings` 提前退出后报告缺少服务器线程，因此这项检查不视为完整运行通过。
- **仍需手动验收**：三个版本的实际画面、翻身/收招观感、不同高低差与复杂障碍、多人网络延迟，以及 NeoForge 完整战斗运行。

## 开发结构

```text
src/main/java/com/tonywww/strongerwanderer/
  combat/            技能时间轴、选招、地形、扫掠判定、特效
  client/            原版 ModelPart 姿态与渲染快照接口
  platform/          加载器入口、配置、注册、版本接口、幻影实体
    mixin/           Warden AI/移动接入和客户端动画接入
src/main/resources/  模组元数据、语言、技能伤害类型
src/gametest/        主版本隔离集成验证
versions/            三个版本的构建节点
```

技能时间点参考 Elder-Bosses 中狮子斩、重力劈跃和幻影突进的机制，重新设计为坚守者的原版手臂动作；没有复制原作模型、骨骼、贴图或动画资产。版本依赖来自 [Forge 官方发布页](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html) 与 [NeoForge 官方 Maven](https://maven.neoforged.net/releases/net/neoforged/neoforge/)。

## CurseForge 发布

发布流程参考同级 JEI-Optimize，使用 [Mod Publish Plugin 2.1.1](https://modmuss50.github.io/mod-publish-plugin/platforms/curseforge/)。默认 `build` 只生成 JAR；发布命令在构建后上传三个版本对应的正式 JAR。Forge 使用重映射产物，NeoForge 使用默认 JAR；均标注准确的 Minecraft、加载器、Java 版本及客户端/服务端支持。

```powershell
.\scripts\setup-publish.ps1              # 检查 JDK 与凭据环境
.\scripts\dryrun.ps1                     # 默认 build + 完整发布试运行，不上传
.\scripts\setup-publish.ps1 -ProjectId 1234567  # 获得本项目 ID 后填写，替换示例数字
.\scripts\publish.ps1                    # 构建、列出文件、确认后上传
.\scripts\publish.ps1 -Yes               # 无交互发布，可用于自动化
```

当前 `curseforge.projectId` 留空。项目建立后填写数字 ID；空 ID 会阻止真实发布，但不影响普通构建和试运行。`curseforge.releaseType` 支持 `release`、`beta`、`alpha`；版本取自 `mod.version`，更新日志取自 `CHANGELOG.md`，也可由 `CHANGELOG` 环境变量覆盖。

Token 复用用户级 `%USERPROFILE%/.gradle/gradle.properties` 中的 `curseforge.token`，或优先使用 `CURSEFORGE_TOKEN` 环境变量。凭据不写入项目文件，也不作为命令行参数传递。试运行使用固定占位凭据，不上传或验证真实凭据的远端权限。

脚本自动寻找 JDK 25；也可传入 `-JavaHome "C:/path/to/jdk-25"`。仅对本次命令设置 `JAVA_HOME`，结束后恢复。Gradle 守护进程固定为 Java 25，游戏编译工具链仍为 17 / 21 / 25。发布任务串行上传，禁止携带 GameTest 验证内容的构建。

直接使用 Gradle 的等价试运行命令为 `gradlew.bat publishAllVersions -Ppublish.dryRun=true`；试运行报告位于各版本的 `build/publishMods` 目录。
