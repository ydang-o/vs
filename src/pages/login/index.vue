<template>
  <view class="container">
    <view class="logo-area">
      <image class="logo" src="/static/icons/task-active.png" mode="aspectFit"></image>
      <text class="title">合伙人投票系统</text>
    </view>
    
    <!-- WeChat Login Area -->
    <view class="login-form" v-if="!showLoginForm">
      <view class="welcome-text">欢迎回来</view>
      <view class="sub-text">请使用微信授权登录</view>
      
      <button 
        class="login-btn" 
        type="primary" 
        @click="handleWxLogin"
        :loading="loading"
      >
        微信一键登录
      </button>
      
      <view class="switch-mode" @click="showLoginForm = true">
        <text>首次登录 / 账号绑定</text>
      </view>
    </view>

    <!-- Account Login/Bind Area -->
    <view class="login-form" v-else>
      <view class="welcome-text">账号绑定</view>
      <view class="sub-text">首次登录请验证身份绑定微信</view>

      <view class="input-group">
        <input 
          class="input-item" 
          type="text" 
          v-model="formData.username" 
          placeholder="请输入用户名" 
        />
        <input 
          class="input-item" 
          type="password" 
          v-model="formData.password" 
          placeholder="请输入密码" 
        />
      </view>

      <button 
        class="login-btn" 
        type="primary" 
        @click="handleAccountBind"
        :loading="loading"
      >
        验证并绑定
      </button>
      
      <view class="switch-mode" @click="showLoginForm = false">
        <text>返回微信登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { useShare } from '@/composables/useShare.js'

useShare()

onPullDownRefresh(() => {
  // If showing login form, maybe clear fields? Or just stop refresh.
  // For now just stop refresh to allow user to see spinner and know it's "refreshed" (even if nothing happens)
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 500)
})

const loading = ref(false)
const showLoginForm = ref(false)
const formData = reactive({
  username: '',
  password: ''
})

// Handle WeChat Login
const handleWxLogin = () => {
  // Clear existing token to prevent backend from treating this as a "Bind to current user" request
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')
  
  loading.value = true
  
  uni.login({
    provider: 'weixin',
    success: (loginRes) => {
      if (loginRes.code) {
        request({
          url: '/user/user/wxLogin',
          method: 'POST',
          data: {
            code: loginRes.code
          }
        }).then(res => {
          loading.value = false
          if ((res.code === 0 || res.code === 1 || res.code === 200) && res.data) {
            handleLoginSuccess(res.data)
          } else {
            // Assume failure means not bound or other error
            uni.showToast({ title: res.msg || '未绑定账号，请先验证', icon: 'none' })
            showLoginForm.value = true
          }
        }).catch(err => {
          loading.value = false
          
          // If 401 (Unauthorized/Not Bound) or 404, switch to form
          // Note: Some environments might wrap the error differently, so check multiple properties
          if (err.statusCode === 401 || err.code === 401 || err.statusCode === 404 || err.code === 404) {
             console.warn('WX Login: User not bound or not found (Expected for new users)', err)
             uni.showToast({ title: '未找到绑定账号，请先验证', icon: 'none' })
             showLoginForm.value = true
          } else {
             console.error('WX Login Failed:', err)
             uni.showToast({ title: '登录失败，请重试', icon: 'none' })
          }
        })
      } else {
        loading.value = false
        uni.showToast({ title: '获取微信授权码失败', icon: 'none' })
      }
    },
    fail: (err) => {
      loading.value = false
      uni.showToast({ title: '微信登录调用失败', icon: 'none' })
    }
  })
}

// Handle Account Verification and Binding
const handleAccountBind = () => {
  // Clear any existing token to avoid interference
  uni.removeStorageSync('token')

  if (!formData.username || !formData.password) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }

  loading.value = true

  uni.login({
    provider: 'weixin',
    success: (loginRes) => {
      if (!loginRes.code) {
        loading.value = false
        uni.showToast({ title: '获取微信授权码失败', icon: 'none' })
        return
      }

      request({
        url: '/user/user/preLogin',
        method: 'POST',
        data: {
          username: formData.username,
          password: formData.password,
          code: loginRes.code
        }
      }).then(res => {
        if (res.code === 0 && !res.data) {
          loading.value = false
          uni.showToast({ title: res.msg || '账号或密码不正确', icon: 'none' })
          return
        }

        if (res.code === 0 || res.code === 1 || res.code === 200) {
          const userId = res.data && res.data.id
          if (!userId) {
            loading.value = false
            uni.showToast({ title: '账号验证失败', icon: 'none' })
            return
          }

          uni.login({
            provider: 'weixin',
            success: (bindLoginRes) => {
              if (bindLoginRes.code) {
                request({
                  url: '/user/user/wxLogin',
                  method: 'POST',
                  data: {
                    code: bindLoginRes.code,
                    userId: userId
                  }
                }).then(bindRes => {
                  loading.value = false
                  if (bindRes.code === 0 || bindRes.code === 1 || bindRes.code === 200) {
                    uni.showToast({ title: '绑定成功', icon: 'success' })
                    handleLoginSuccess(bindRes.data)
                  } else {
                    uni.showToast({ title: bindRes.msg || '绑定失败', icon: 'none' })
                  }
                }).catch(err => {
                  loading.value = false
                  uni.showToast({ title: err.msg || '绑定请求失败', icon: 'none' })
                })
              } else {
                loading.value = false
                uni.showToast({ title: '获取微信授权码失败', icon: 'none' })
              }
            },
            fail: () => {
              loading.value = false
              uni.showToast({ title: '微信授权失败', icon: 'none' })
            }
          })
        } else {
          loading.value = false
          uni.showToast({ title: res.msg || '账号验证失败', icon: 'none' })
        }
      }).catch(err => {
        loading.value = false
        if (err.statusCode === 401 || err.code === 401) {
          uni.showToast({ title: '用户名或密码错误', icon: 'none' })
        } else {
          uni.showToast({ title: err.msg || '验证请求失败', icon: 'none' })
        }
      })
    },
    fail: () => {
      loading.value = false
      uni.showToast({ title: '微信授权失败', icon: 'none' })
    }
  })
}

const handleLoginSuccess = (data) => {
  if (!data) {
    uni.showToast({ title: '登录失败：服务端未返回数据', icon: 'none' })
    return
  }
  const { token, id, openid, ...userInfo } = data
            
  if (!token) {
    uni.showToast({ title: '登录失败：Token缺失', icon: 'none' })
    return
  }

  uni.setStorageSync('token', token)
  uni.setStorageSync('userInfo', { id, openid, ...userInfo })
  
  uni.showToast({
    title: '登录成功',
    icon: 'success'
  })
  
  setTimeout(() => {
    uni.switchTab({
      url: '/pages/index/index'
    })
  }, 1500)
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
  margin-bottom: 80rpx;
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
  margin-bottom: 60rpx;
}

.input-group {
  width: 100%;
  margin-bottom: 40rpx;
}

.input-item {
  width: 100%;
  height: 96rpx;
  background-color: #f5f7fa;
  border-radius: 48rpx;
  margin-bottom: 24rpx;
  padding: 0 40rpx;
  box-sizing: border-box;
  font-size: 30rpx;
  color: #333;
}

.login-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: #3B82F6;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 500;
  margin-bottom: 30rpx;
}

.switch-mode {
  padding: 20rpx;
}

.switch-mode text {
  font-size: 28rpx;
  color: #666;
  text-decoration: underline;
}
</style>
