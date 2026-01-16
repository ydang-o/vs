<template>
  <view class="container">
    <view class="form-card">
      <view class="form-item">
        <text class="label">旧密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="formData.oldPassword" 
          placeholder="请输入旧密码"
        />
      </view>
      <view class="form-item">
        <text class="label">新密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="formData.newPassword" 
          placeholder="请输入新密码"
        />
      </view>
      <view class="form-item">
        <text class="label">确认新密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="formData.confirmPassword" 
          placeholder="请再次输入新密码"
        />
      </view>
    </view>

    <view class="btn-area">
      <button class="submit-btn" @click="handleSubmit" :loading="loading">确认修改</button>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { useShare } from '@/composables/useShare.js'

useShare({ title: '修改密码' })

onPullDownRefresh(() => {
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 500)
})

const loading = ref(false)
const formData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const handleSubmit = () => {
  if (!formData.oldPassword) {
    uni.showToast({ title: '请输入旧密码', icon: 'none' })
    return
  }
  if (!formData.newPassword) {
    uni.showToast({ title: '请输入新密码', icon: 'none' })
    return
  }
  if (formData.newPassword.length < 6) {
    uni.showToast({ title: '新密码长度不能少于6位', icon: 'none' })
    return
  }
  if (formData.newPassword !== formData.confirmPassword) {
    uni.showToast({ title: '两次输入的密码不一致', icon: 'none' })
    return
  }
  if (formData.oldPassword === formData.newPassword) {
    uni.showToast({ title: '新密码不能与旧密码相同', icon: 'none' })
    return
  }

  loading.value = true
  
  // Using a standard endpoint assumption for password update
  request({
    url: '/user/user/changePassword',
    method: 'PUT',
    data: {
      oldPassword: formData.oldPassword,
      newPassword: formData.newPassword
    }
  }).then(res => {
    loading.value = false
    // Handle backend bug where code 0 is returned for password error
    if ((res.code === 0 || res.code === 1 || res.code === 200) && res.msg !== '密码错误') {
      uni.showToast({ title: '修改成功，请重新登录', icon: 'none' })
      setTimeout(() => {
        // Clear token and redirect to login
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        uni.reLaunch({
          url: '/pages/login/index'
        })
      }, 1500)
    } else {
      uni.showToast({ title: res.msg || '修改失败', icon: 'none' })
    }
  }).catch(err => {
    loading.value = false
    uni.showToast({ title: err.msg || '请求失败', icon: 'none' })
  })
}
</script>

<style>
.container {
  padding: 30rpx;
  background-color: #F8F9FA;
  min-height: 100vh;
}

.form-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 0 30rpx;
  margin-bottom: 60rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1px solid #F3F4F6;
}

.form-item:last-child {
  border-bottom: none;
}

.label {
  width: 180rpx;
  font-size: 30rpx;
  color: #333;
}

.input {
  flex: 1;
  font-size: 30rpx;
  color: #333;
}

.submit-btn {
  background-color: #3B82F6;
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
}
</style>
