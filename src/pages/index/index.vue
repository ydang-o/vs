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
        @click="goToDetail(item.voteTaskId || item.id)"
      >
        <view class="card-header">
          <text class="task-title">{{ item.proposalTitle || item.title }}</text>
          <view class="badge-group">
            <text class="urgent-badge" v-if="item.urgent">紧急</text>
            <!-- Pending list always contains active/todo tasks per definition -->
          </view>
        </view>
        <view class="card-body">
          <view class="info-row">
            <text class="label">截止时间：</text>
            <text class="value" :class="{ 'text-danger': isUrgent(item.endTime) }">{{ item.endTime }}</text>
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

const isUrgent = (timeStr) => {
  if (!timeStr || timeStr === '进行中') return false
  try {
    // Replace - with / for iOS compatibility
    const target = new Date(timeStr.replace(/-/g, '/')).getTime()
    if (isNaN(target)) return false
    const now = Date.now()
    const diff = target - now
    // Less than 3 hours (10800000ms) and not expired
    return diff > 0 && diff <= 10800000
  } catch (e) {
    return false
  }
}

const fetchTasks = () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }

  request({
    url: '/user/vote/my/pending',
    method: 'GET',
    data: {
      page: 1,
      pageSize: 20
    }
  }).then(res => {
    if (res.code === 0 || res.code === 1 || res.code === 200) {
      // Handle both new 'list' format and potential 'records' or direct array
      const data = res.data;
      if (Array.isArray(data)) {
        tasks.value = data;
      } else if (data && Array.isArray(data.list)) {
        tasks.value = data.list;
      } else if (data && Array.isArray(data.records)) {
        tasks.value = data.records;
      } else {
        tasks.value = [];
      }
      
      // Sort by endTime descending (Latest deadline first) as requested
      if (tasks.value.length > 0) {
        tasks.value.sort((a, b) => {
          const timeA = new Date(a.endTime.replace(/-/g, '/')).getTime() || 0
          const timeB = new Date(b.endTime.replace(/-/g, '/')).getTime() || 0
          return timeB - timeA
        })
      }
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

.badge-group {
  display: flex;
  gap: 10rpx;
}

.urgent-badge {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  background-color: #FEE2E2;
  color: #EF4444;
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

.text-danger {
  color: #EF4444 !important;
  font-weight: bold;
}

.empty-tip {
  text-align: center;
  color: #9CA3AF;
  margin-top: 100rpx;
}
</style>
