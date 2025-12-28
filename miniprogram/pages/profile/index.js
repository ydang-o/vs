// pages/profile/index.js
const app = getApp();
const auth = require('../../utils/auth.js');

Page({
  data: {
    userInfo: null
  },
  onShow() {
    this.setData({
      userInfo: app.globalData.userInfo
    });
  },
  handleLogout() {
    auth.logout();
    app.globalData.userInfo = null;
    wx.reLaunch({
      url: '/pages/login/index',
    });
  }
})
