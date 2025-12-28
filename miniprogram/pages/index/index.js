// pages/index/index.js
const mockData = require('../../utils/mockData.js');
const app = getApp();

Page({
  data: {
    currentTab: 0,
    tasks: [],
    filteredTasks: [],
    refreshing: false
  },

  onShow() {
    this.loadTasks();
    app.checkLogin();
  },

  loadTasks() {
    // Simulate API call
    const allTasks = mockData.tasks;
    this.setData({ tasks: allTasks });
    this.filterTasks(this.data.currentTab);
  },

  switchTab(e) {
    const index = parseInt(e.currentTarget.dataset.index);
    this.setData({ currentTab: index });
    this.filterTasks(index);
  },

  filterTasks(tabIndex) {
    let filtered = [];
    if (tabIndex === 0) {
      filtered = this.data.tasks;
    } else if (tabIndex === 1) {
      filtered = this.data.tasks.filter(t => t.status === 'published');
    } else if (tabIndex === 2) {
      filtered = this.data.tasks.filter(t => t.status === 'ended');
    }
    this.setData({ filteredTasks: filtered });
  },

  onRefresh() {
    this.setData({ refreshing: true });
    setTimeout(() => {
      this.loadTasks();
      this.setData({ refreshing: false });
    }, 1000);
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/task-detail/index?id=${id}`,
    });
  }
})
