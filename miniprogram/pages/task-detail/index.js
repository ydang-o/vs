// pages/task-detail/index.js
const mockData = require('../../utils/mockData.js');

Page({
  data: {
    task: null,
    hasVoted: false,
    myVote: null
  },

  onLoad(options) {
    const id = parseInt(options.id);
    this.loadTask(id);
  },

  loadTask(id) {
    const task = mockData.tasks.find(t => t.id === id);
    if (task) {
      this.setData({ task });
    }
  },

  handleVote(e) {
    const type = e.currentTarget.dataset.type;
    let typeName = '';
    switch(type) {
      case 'agree': typeName = '同意'; break;
      case 'oppose': typeName = '反对'; break;
      case 'abstain': typeName = '弃权'; break;
    }

    wx.showModal({
      title: '确认投票',
      content: `您确认投“${typeName}”票吗？提交后不可修改。`,
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '提交中' });
          setTimeout(() => {
            wx.hideLoading();
            this.setData({
              hasVoted: true,
              myVote: type
            });
            wx.showToast({
              title: '投票成功',
              icon: 'success'
            });
          }, 1000);
        }
      }
    });
  },

  handleDelegate() {
    wx.showToast({
      title: '跳转至委托流程',
      icon: 'none'
    });
  }
})
