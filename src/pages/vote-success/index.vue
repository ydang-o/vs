<template>
  <view class="container">
    <view class="success-icon-area">
      <icon type="success" size="80" color="#10B981" />
      <text class="success-text">投票成功</text>
      <text class="sub-text">您的投票已成功提交</text>
    </view>
    
    <view class="btn-area">
      <button class="primary-btn" @click="goBack">返回上一页</button>
      <button class="secondary-btn" @click="goHome">返回首页</button>
    </view>
  </view>
</template>

<script setup>
const goBack = () => {
  uni.navigateBack()
}

const goHome = () => {
  uni.switchTab({
    url: '/pages/index/index'
  })
}
</script>

<style>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 40rpx;
  background-color: #fff;
  min-height: 100vh;
}

.success-icon-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 100rpx;
}

.success-text {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  margin-top: 40rpx;
  margin-bottom: 20rpx;
}

.sub-text {
  font-size: 28rpx;
  color: #666;
}

.btn-area {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.primary-btn {
  background-color: #3B82F6;
  color: #fff;
  border-radius: 44rpx;
  width: 80%;
}

.secondary-btn {
  background-color: #F3F4F6;
  color: #333;
  border-radius: 44rpx;
  width: 80%;
}
</style>
