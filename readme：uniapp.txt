# 合伙人投票系统 - 微信小程序开发与部署手册

## 1. 项目概述
本项目为“合伙人投票系统”的微信小程序端，基于 **uni-app + Vue 3** 开发。
系统聚焦于合伙人投票决策，提供轻量化的议案查看、投票（含本人投票与委托投票）、结果查询等功能。

## 2. 技术栈
- **框架**: uni-app (Vue 3 Composition API)
- **开发工具**: HBuilderX
- **运行环境**: 微信小程序
- **UI风格**: 扁平化设计，主色调 #3B82F6

## 3. 目录结构
项目采用 HBuilderX 默认结构：
```
miniprogram/
├── pages/
│   ├── login/          # 登录页
│   ├── index/          # 首页（投票任务列表）
│   ├── task-detail/    # 任务详情页（核心投票逻辑）
│   ├── my-votes/       # 我的投票记录
│   ├── delegate-create/# 发起委托
│   ├── profile/        # 个人中心
│   ├── change-password/# 修改密码
│   └── vote-success/   # 投票成功页
├── static/             # 静态资源
├── utils/
│   └── request.js      # 网络请求封装
├── App.vue             # 应用入口
├── main.js             # Vue初始化
├── pages.json          # 路由与TabBar配置
└── manifest.json       # 应用配置
```

## 4. 快速开始 (开发环境)

### 4.1 环境准备
1. 安装 [HBuilderX](https://www.dcloud.io/hbuilderx.html) (推荐 Alpha 或最新正式版)。
2. 安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)。
3. 确保微信开发者工具已开启服务端口：
   - 打开微信开发者工具 -> 设置 -> 安全设置 -> 服务端口：**开启**。

### 4.2 导入项目
1. 打开 HBuilderX。
2. 菜单栏：文件 -> 打开目录 -> 选择项目根目录。

### 4.3 运行
1. 在 HBuilderX 中选中项目。
2. 菜单栏：运行 -> 运行到小程序模拟器 -> 微信开发者工具。
3. 首次运行可能需要指定微信开发者工具的安装路径。

## 5. 配置与部署 (重要)

本项目在发布前，**必须**配置后端接口地址。请按照以下步骤修改代码中的配置。

### 5.1 修改接口地址
为了保护数据安全，源码中的接口地址已设置为本地默认地址 (`http://127.0.0.1:8080`)。在部署或连接测试环境时，请务必更新以下文件中的 `BASE_URL`。

**涉及文件清单：**

1.  **全局接口请求配置**
    - **文件**: `src/utils/request.js`
    - **修改内容**:
      ```javascript
      // 将此处修改为您的真实后端服务地址
      export const BASE_URL = 'https://api.your-domain.com'; 
      ```

2.  **详情页图片资源路径**
    - **文件**: `src/pages/task-detail/index.vue`
    - **修改内容**:
      ```javascript
      // 搜索 BASE_URL 常量并修改
      const BASE_URL = 'https://api.your-domain.com'
      ```

3.  **文件上传接口**
    - **文件**: `src/pages/delegate-create/index.vue`
    - **修改内容**:
      ```javascript
      // 搜索 uni.uploadFile 中的 url 字段
      url: 'https://api.your-domain.com/user/file/upload',
      ```

> **注意**: 微信小程序要求所有请求域名必须配置 HTTPS（开发调试模式下可在开发者工具中勾选“不校验合法域名”）。

### 5.2 发行到微信小程序
1. 在 HBuilderX 中点击菜单栏：**发行** -> **小程序-微信**。
2. 输入您的微信小程序 AppID    VotingSystem\src\project.config.json。
3. 点击“发行”，HBuilderX 将进行编译和代码压缩。
4. 编译完成后，微信开发者工具会自动打开。
5. 在微信开发者工具中点击“上传”，填写版本号和备注。
6. 登录 [微信公众平台](https://mp.weixin.qq.com/)，在“版本管理”中提交审核。

## 6. 功能模块说明

### 6.1 登录 (Login)
- 支持账号密码登录。
- 登录成功后将 Token 存储在本地 Storage 中，用于后续接口鉴权。

### 6.2 任务列表 (Index)
- 展示“待办”和“已办”任务。
- 支持下拉刷新。
- **代投标识**: 列表项会通过紫色标签 `[代 XXX 投票]` 明确标识该任务是受他人委托。

### 6.3 任务详情 (Task Detail)
- **本人投票**: 若当前用户有投票权，直接显示同意/反对/弃权按钮。
- **委托投票**: 若当前用户接受了他人委托，会显示独立的“代 XXX 投票”区域。
- **逻辑分流**: 
  - 本人投票调用 `/vote/submit`。
  - 委托投票调用 `/vote/delegate/submit` (自动携带 `fromPartnerId`)。
- **结果查看**: 仅当本人及所有代投任务均完成后，才显示最终结果查看按钮。

### 6.4 发起委托 (Delegate Create)
- 用户可以上传证明材料（图片），指定受托人，将某个任务的投票权委托给他人。


