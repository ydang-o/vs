# 合伙人投票系统 - 项目交付与部署文档

本文档详细说明了项目的运行环境、配置方法及构建流程，用于项目交付和后续维护。

## 1. 项目概述
本项目是基于 **uni-app (Vue 3)** 开发的微信小程序，主要功能包括合伙人投票、任务详情查看、委托投票、个人中心管理等。

## 2. 环境要求
在运行或开发本项目前，请确保您的开发环境满足以下要求：

- **操作系统**: Windows / macOS
- **Node.js**: 建议版本 v16.x 或 v18.x
- **包管理器**: npm (v8+) 或 yarn
- **开发工具**: 
  - [HBuilderX](https://www.dcloud.io/hbuilderx.html) (推荐，uni-app 官方 IDE)
  - 或 [VS Code](https://code.visualstudio.com/) + 命令行工具
  - [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html) (用于小程序预览和上传)

## 3. 安装与运行

### 3.1 安装依赖
项目根目录下包含 `package.json`，首次使用需安装依赖：

```bash
# 在项目根目录执行
npm install
```

### 3.2 运行开发环境

#### 方式一：使用 HBuilderX (推荐)
1. 打开 HBuilderX，选择“文件” -> “打开目录”，选中项目根目录 `d:\work_boss\VotingSystem`。
2. 点击菜单栏“运行” -> “运行到小程序模拟器” -> “微信开发者工具”。
3. 此时会自动编译并打开微信开发者工具。

#### 方式二：使用命令行 (CLI)
```bash
# 运行微信小程序开发模式
npm run dev:mp-weixin
```
运行成功后，在微信开发者工具中导入 `dist/dev/mp-weixin` 目录即可预览。

## 4. 项目配置 (关键)

在交付或部署到正式环境前，请务必检查以下配置：

### 4.1 后端接口地址
目前项目的接口地址配置在 `src/utils/request.js` 文件中。

**文件路径**: [src/utils/request.js](src/utils/request.js)

```javascript
// 修改此处的 URL 为您的正式环境后端地址
export const BASE_URL = 'http://localhost:8080'; 
```

### 4.2 微信小程序 AppID
请确保 `manifest.json` 中的 AppID 与您微信后台申请的一致。

**文件路径**: [src/manifest.json](src/manifest.json)

```json
"mp-weixin" : {
    "appid" : "wx439e92e6e8fc24df", // 请替换为您真实的 AppID
    "setting" : {
        "urlCheck" : false
    }
}
```

## 5. 打包与发布

当开发完成准备上线时，需要进行生产环境打包。

### 5.1 构建生产包
```bash
npm run build:mp-weixin
```

### 5.2 上传代码
1. 构建完成后，生成的文件位于 `dist/build/mp-weixin` 目录。
2. 打开微信开发者工具，导入该目录。
3. 点击右上角的“上传”按钮，填写版本号和备注。
4. 登录 [微信公众平台](https://mp.weixin.qq.com/)，在“版本管理”中提交审核。

## 6. 目录结构说明

```
VotingSystem/
├── src/
│   ├── components/      # 通用组件
│   ├── composables/     # 组合式函数 (如 useShare.js)
│   ├── pages/           # 页面文件
│   │   ├── index/       # 首页 (投票列表)
│   │   ├── login/       # 登录与绑定
│   │   ├── task-detail/ # 任务详情
│   │   ├── ...
│   ├── static/          # 静态资源 (图片、图标)
│   ├── utils/           # 工具库 (request.js 等)
│   ├── App.vue          # 根组件
│   ├── main.js          # 入口文件
│   ├── manifest.json    # 应用配置文件 (AppID 等)
│   └── pages.json       # 页面路由配置
├── package.json         # 项目依赖配置
├── vite.config.js       # Vite 构建配置
└── DEPLOY_GUIDE.md      # 本文档
```

## 7. 常见问题
- **接口 401 错误**: 通常是因为 Token 过期，重新登录即可。
- **上传文件失败**: 请检查 `request.js` 或具体页面中的上传 URL 是否配置正确（部分上传接口可能在页面内单独定义，如 `delegate-create/index.vue`）。
- **真机调试无法连接**: 请确保手机和电脑在同一局域网（如果使用本地后端），或在微信开发者工具中勾选“不校验合法域名”。
