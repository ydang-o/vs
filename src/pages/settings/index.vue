<template>
  <view class="container">
    <view class="menu-list">
      <view class="menu-item" @click="goToChangePassword">
        <text>修改密码</text>
        <text class="arrow">></text>
      </view>
      <!-- Future settings can be added here -->
    </view>

    <view class="logout-area">
       <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const goToChangePassword = () => {
  uni.navigateTo({
    url: '/pages/change-password/index'
  })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        uni.reLaunch({
          url: '/pages/login/index'
        })
      }
    }
  })
}
</script>

<style>
.container {
  padding: 30rpx;
  background-color: #F8F9FA;
  min-height: 100vh;
}

.menu-list {
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 40rpx;
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

.logout-area {
  margin-top: 60rpx;
}

.logout-btn {
  background-color: #fff;
  color: #EF4444;
  font-size: 32rpx;
  border-radius: 16rpx;
  border: none;
}

.logout-btn::after {
  border: none;
}
</style>
