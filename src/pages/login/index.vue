<template>
  <view class="container">
    <view class="logo-area">
      <image class="logo" src="/static/icons/task-active.png" mode="aspectFit"></image>
      <text class="title">合伙人投票系统</text>
    </view>
    
    <view class="login-form">
      <view class="welcome-text">欢迎回来</view>
      <view class="sub-text">请使用微信授权登录</view>
      
      <button 
        class="login-btn" 
        type="primary" 
        @click="handleLogin"
        :loading="loading"
      >
        微信一键登录
      </button>

      <!-- Phone Number Button (Optional for now, depending on backend logic) -->
      <!-- <button 
        class="login-btn phone-btn" 
        open-type="getPhoneNumber" 
        @getphonenumber="handlePhoneNumber"
      >
        手机号授权登录
      </button> -->
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request.js'

const loading = ref(false)

// Placeholder for phone number handling if needed later
// const handlePhoneNumber = (e) => {
//   if (e.detail.code) {
//     // Send code to backend to bind phone
//   }
// }

const handleLogin = () => {
  loading.value = true
  
  uni.login({
    provider: 'weixin',
    success: (loginRes) => {
      if (loginRes.code) {
        request({
          url: '/user/user/login',
          method: 'POST',
          data: {
            code: loginRes.code
          }
        }).then(res => {
          loading.value = false
          if (res.code === 1 || res.code === 200) {
            console.log('Login Response Data:', res.data)
            const { token, id, openid, ...userInfo } = res.data
            
            if (!token) {
              console.error('Token is missing in response:', res.data)
              uni.showToast({ title: '登录失败：Token缺失', icon: 'none' })
              return
            }

            // Remove role check as it is not in the API response definition
            // The backend seems to control access purely via token validity/claims
            
            uni.setStorageSync('token', token)
            console.log('Token stored in storage:', uni.getStorageSync('token')) // Verify storage
            uni.setStorageSync('userInfo', { id, openid, ...userInfo }) // Store all user info fields
            
            uni.showToast({
              title: '登录成功',
              icon: 'success'
            })
            
            setTimeout(() => {
              uni.switchTab({
                url: '/pages/index/index'
              })
            }, 1500)
          } else {
             // Handle backend error codes explicitly
             console.error('Login Failed with code:', res.code, res.msg)
             uni.showToast({
               title: res.msg || '登录失败',
               icon: 'none',
               duration: 3000
             })
          }
        }).catch(err => {
          loading.value = false
          console.error('Login Request Failed:', err)
          uni.showToast({
            title: '登录请求失败: ' + (err.errMsg || JSON.stringify(err)),
            icon: 'none',
            duration: 3000
          })
        })
      } else {
        loading.value = false
        console.error('WeChat Login Code Missing:', loginRes)
        uni.showToast({
          title: '微信登录失败: 未获取到code',
          icon: 'none'
        })
      }
    },
    fail: (err) => {
      loading.value = false
      console.error('Uni Login Failed:', err)
      uni.showToast({
        title: '无法调用微信登录: ' + (err.errMsg || JSON.stringify(err)),
        icon: 'none',
        duration: 3000
      })
    }
  })
}
</script>

<style>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 40rpx;
  min-height: 100vh;
  background-color: #fff;
}

.logo-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 120rpx;
}

.logo {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 30rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.login-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.welcome-text {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.sub-text {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 80rpx;
}

.login-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: #3B82F6;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 500;
  margin-bottom: 40rpx;
}
</style>
