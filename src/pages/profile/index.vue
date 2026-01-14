<template>
  <view class="container">
    <view class="user-card">
      <view class="avatar-area">
        <image class="avatar" src="/static/icons/profile-active.png" mode="aspectFit"></image>
      </view>
      <view class="info-area">
        <text class="username">{{ userInfo.username || '微信用户' }}</text>
        <text class="role-tag">{{ getRoleName(userInfo.role) }}</text>
      </view>
    </view>
    
    <view class="menu-list">
      <view class="menu-item" @click="goToMyVotes">
        <text>我的投票记录</text>
        <text class="arrow">></text>
      </view>
      <view class="menu-item" @click="goToSettings">
        <text>系统设置</text>
        <text class="arrow">></text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'

const userInfo = ref({})

onShow(() => {
  loadUserInfo()
})

onPullDownRefresh(() => {
  loadUserInfo()
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 500)
})

const loadUserInfo = () => {
  const info = uni.getStorageSync('userInfo')
  if (info) {
    userInfo.value = info
  }
}

const getRoleName = (role) => {
  const map = {
    'partner': '正式合伙人',
    'admin': '管理员'
  }
  return map[role] || '普通用户'
}

const goToMyVotes = () => {
  uni.navigateTo({
    url: '/pages/my-votes/index'
  })
}

const goToSettings = () => {
  uni.navigateTo({
    url: '/pages/settings/index'
  })
}
</script>

<style>
.container {
  padding: 30rpx;
  background-color: #F8F9FA;
  min-height: 100vh;
}

.user-card {
  background-color: #3B82F6;
  border-radius: 20rpx;
  padding: 40rpx;
  display: flex;
  align-items: center;
  color: white;
  margin-bottom: 40rpx;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  background-color: rgba(255, 255, 255, 0.2);
  margin-right: 30rpx;
}

.info-area {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
}

.role-tag {
  font-size: 24rpx;
  background-color: rgba(255, 255, 255, 0.2);
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  align-self: flex-start;
}

.menu-list {
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #F3F4F6;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background-color: #F9FAFB;
}

.arrow {
  color: #9CA3AF;
}

.logout-text {
  color: #EF4444;
}
</style>
