# Stronger Wanderer 开发约定

- 主版本为 Minecraft 1.20.1 / Forge 47.4.10；同时维护 1.21.1 / NeoForge 21.1.235 和 26.1.2 / NeoForge 26.1.2.109。
- 遵循同级 `MC-Dev-Skills/SKILL.md`。使用 Stonecutter 平铺版本树，公共 Java 源码放在 `src/main/java`，加载器与版本边界集中于 `platform`。
- 保留原版坚守者的模型、骨骼层级、贴图和基础属性。不添加 GeckoLib。动画通过原版 ModelPart 接口处理。
- 新技能使用手臂攻击。无技能范围指示器。特效优先使用原版粒子与渲染管线。
- 服务端决定技能、移动、目标和伤害；客户端只做动画表现。
- 配置只维护当前结构，不添加旧配置兼容、别名或自动迁移。
- 验证使用隔离目录，不改用户游戏配置和存档。仅执行与本次改动直接相关的必要验证。
- 正式交付只使用默认 `gradlew.bat build`；不额外打包第三方安装包。
- `src/gametest` 仅在 `-PswGameTest` 的 Forge 开发验证中启用，不允许进入正式 JAR。
