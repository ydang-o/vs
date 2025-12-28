// pages/login/index.js
const auth = require('../../utils/auth.js');
const app = getApp();

Page({
  data: {
    username: '',
    password: '',
    loading: false
  },

  onLoad() {
    // If already logged in, redirect
    if (app.globalData.userInfo) {
      this.redirectBasedOnRole(app.globalData.userInfo);
    }
  },

  handleLogin() {
    if (!this.data.username || !this.data.password) {
      wx.showToast({
        title: '请输入账号密码',
        icon: 'none'
      });
      return;
    }

    this.setData({ loading: true });

    auth.login(this.data.username, this.data.password).then(res => {
      this.setData({ loading: false });
      if (res.success) {
        if (res.user.role === 'admin') {
          wx.showModal({
            title: '提示',
            content: '管理员请登录PC端管理系统进行操作',
            showCancel: false
          });
          return;
        }

        app.globalData.userInfo = res.user;
        wx.showToast({
          title: '登录成功',
          icon: 'success'
        });
        setTimeout(() => {
          this.redirectBasedOnRole(res.user);
        }, 1500);
      } else {
        wx.showToast({
          title: res.message,
          icon: 'none'
        });
      }
    });
  },

  handleWechatLogin() {
    wx.showToast({
      title: '模拟微信登录成功',
      icon: 'none'
    });
    // Mock auto login as partner1
    this.setData({
      username: 'partner1',
      password: '123'
    });
    this.handleLogin();
  },

  redirectBasedOnRole(user) {
    if (user.role === 'partner') {
      wx.switchTab({
        url: '/pages/index/index',
      });
    }
    // Admin is handled in handleLogin
  }
})
