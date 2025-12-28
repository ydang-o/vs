// utils/auth.js
const mockData = require('./mockData.js');

const login = (username, password) => {
  return new Promise((resolve, reject) => {
    // Simulate network delay
    setTimeout(() => {
      const user = mockData.users.find(u => u.username === username && u.password === password);
      if (user) {
        wx.setStorageSync('userInfo', user);
        resolve({ success: true, user });
      } else {
        resolve({ success: false, message: '用户名或密码错误' });
      }
    }, 500);
  });
};

const logout = () => {
  wx.removeStorageSync('userInfo');
};

const getCurrentUser = () => {
  return wx.getStorageSync('userInfo');
};

module.exports = {
  login,
  logout,
  getCurrentUser
};
