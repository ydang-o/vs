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
       <button class="unbind-btn" @click="handleUnbind">解除微信绑定并退出</button>
       <button class="logout-btn" @click="handleLogout">仅退出登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { useShare } from '@/composables/useShare.js'

useShare({ title: '系统设置' })

onPullDownRefresh(() => {
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 500)
})

const goToChangePassword = () => {
  uni.navigateTo({
    url: '/pages/change-password/index'
  })
}

const handleUnbind = () => {
  uni.showModal({
    title: '提示',
    content: '确定要解除微信绑定并退出吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '处理中' })
        request({
          url: '/user/user/unbindWechat',
          method: 'POST' // Assuming POST for state-changing action
        }).then(res => {
          uni.hideLoading()
          if (res.code === 0 || res.code === 1 || res.code === 200) {
            uni.showToast({ title: '已解绑并退出' })
            setTimeout(() => {
              clearSessionAndRedirect()
            }, 1000)
          } else {
            uni.showToast({ title: res.msg || '解绑失败', icon: 'none' })
          }
        }).catch(err => {
          uni.hideLoading()
          // Even if network fails, we might want to clear local session? 
          // But safer to let user retry or just clear local if 401.
          uni.showToast({ title: '请求失败', icon: 'none' })
        })
      }
    }
  })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        clearSessionAndRedirect()
      }
    }
  })
}

const clearSessionAndRedirect = () => {
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')
  uni.reLaunch({
    url: '/pages/login/index'
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
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.unbind-btn {
  background-color: #fff;
  color: #EF4444;
  font-size: 32rpx;
  border-radius: 16rpx;
  width: 100%;
}

.logout-btn {
  background-color: #F3F4F6;
  color: #666;
  font-size: 32rpx;
  border-radius: 16rpx;
  width: 100%;
}

.unbind-btn::after, .logout-btn::after {
  border: none;
}
</style>
