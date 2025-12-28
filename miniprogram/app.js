// app.js
App({
  onLaunch() {
    // Check local storage for login info
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.globalData.userInfo = userInfo;
    }
  },
  globalData: {
    userInfo: null,
    // Mock user roles: 'admin', 'partner'
    // In a real app, this would come from the backend
  },
  
  // Helper to check if user is logged in
  checkLogin() {
    if (!this.globalData.userInfo) {
      wx.redirectTo({
        url: '/pages/login/index',
      });
      return false;
    }
    return true;
  }
})
