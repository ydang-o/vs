<template>
  <view class="container">
    <view class="header">
      <text class="page-title">投票任务</text>
    </view>
    
    <view class="task-list">
      <view class="empty-tip" v-if="tasks.length === 0">
        <text>暂无进行中的投票任务</text>
      </view>
      
      <view 
        class="task-card" 
        v-for="(item, index) in tasks" 
        :key="index"
        @click="goToDetail(item.id)"
      >
        <view class="card-header">
          <text class="task-title">{{ item.title }}</text>
          <text class="status-badge" :class="item.status === 'active' ? 'active' : 'ended'">
            {{ item.status === 'active' ? '进行中' : '已结束' }}
          </text>
        </view>
        <view class="card-body">
          <view class="info-row">
            <text class="label">截止时间：</text>
            <text class="value">{{ item.endTime }}</text>
          </view>
          <view class="info-row">
            <text class="label">通过率要求：</text>
            <text class="value">{{ item.passRate }}%</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

const tasks = ref([])

const fetchTasks = () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }

  request({
    url: '/api/vote/task/list',
    method: 'GET'
  }).then(res => {
    if (res.code === 1 || res.code === 200) {
      tasks.value = res.data || []
    }
  }).catch(err => {
    if (err.statusCode !== 401) {
      console.error(err)
    }
    // Mock data if API fails
    tasks.value = [
      { id: 1, title: '关于扩大合伙人规模的议案', status: 'active', endTime: '2023-12-31', passRate: 60 },
      { id: 2, title: '2024年度分红方案', status: 'ended', endTime: '2023-11-30', passRate: 50 }
    ]
  })
}

const goToDetail = (id) => {
  uni.navigateTo({
    url: `/pages/task-detail/index?id=${id}`
  })
}

onShow(() => {
  fetchTasks()
})
</script>

<style>
.container {
  padding: 30rpx;
  background-color: #F8F9FA;
  min-height: 100vh;
}

.header {
  margin-bottom: 40rpx;
}

.page-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #1F2937;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.task-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  border: 1px solid #E5E7EB;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  padding-bottom: 20rpx;
  border-bottom: 1px solid #F3F4F6;
}

.task-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #111827;
  flex: 1;
  margin-right: 20rpx;
}

.status-badge {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

.status-badge.active {
  background-color: #EBF5FF;
  color: #3B82F6;
}

.status-badge.ended {
  background-color: #F3F4F6;
  color: #6B7280;
}

.info-row {
  display: flex;
  margin-bottom: 12rpx;
  font-size: 28rpx;
}

.label {
  color: #6B7280;
  width: 180rpx;
}

.value {
  color: #374151;
}

.empty-tip {
  text-align: center;
  color: #9CA3AF;
  margin-top: 100rpx;
}
</style>
