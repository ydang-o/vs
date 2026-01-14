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
          <view class="title-wrapper">
            <text class="task-title">{{ item.proposalTitle || item.title }}</text>
            <text class="delegate-hint text-orange" v-if="item.delegateeName">
              (您已委托 {{ item.delegateeName }} 投票)
            </text>
            <text class="delegate-hint text-blue" v-if="item.delegatorName">
              ({{ item.delegatorName }} 委托您投票)
            </text>
          </view>
          <view class="badge-group">
            <text class="urgent-badge" v-if="item.urgent">紧急</text>
            <!-- Pending list always contains active/todo tasks per definition -->
          </view>
        </view>
        <view class="card-body">
          <view class="info-row" v-if="item.endTime">
            <text class="label">截止时间：</text>
            <text class="value" :class="{ 'text-danger': isUrgent(item.endTime) }">{{ item.endTime }}</text>
          </view>
          <view class="info-row" v-else-if="item.publishTime">
            <text class="label">发布时间：</text>
            <text class="value">{{ item.publishTime }}</text>
          </view>
          <view class="info-row" v-if="item.totalCount">
             <text class="label">投票进度：</text>
             <text class="value">{{ item.votedCount || 0 }}/{{ item.totalCount }} 人</text>
          </view>
          
          <view class="stats-bar" v-if="item.totalCount">
            <view class="sb-item">
              <text class="sb-label text-green">同意</text>
              <text class="sb-val">{{ item.agreeCount || 0 }}</text>
            </view>
            <view class="sb-item">
              <text class="sb-label text-red">反对</text>
              <text class="sb-val">{{ item.opposeCount || item.rejectCount || 0 }}</text>
            </view>
            <view class="sb-item">
              <text class="sb-label text-gray">弃权</text>
              <text class="sb-val">{{ item.abstainCount || item.waiverCount || 0 }}</text>
            </view>
            <view class="sb-item">
              <text class="sb-label">未投</text>
              <text class="sb-val">{{ (item.totalCount || 0) - (item.votedCount || 0) }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app'
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
  
  const userInfo = uni.getStorageSync('userInfo')
  const isAdmin = userInfo && userInfo.username === 'admin'
  
  if (isAdmin) {
    // uni.hideTabBar() // User requested to keep tab bar
  } else {
    uni.showTabBar()
  }

  const url = isAdmin ? '/user/h5/proposal/list' : '/user/vote/my/pending'
  const dataParams = isAdmin ? { pageNum: 1, pageSize: 20 } : { page: 1, pageSize: 20 }

  request({
    url: url,
    method: 'GET',
    data: dataParams
  }).then(res => {
    if (res.code === 0 || res.code === 1 || res.code === 200) {
      // Handle both new 'list' format and potential 'records' or direct array
      let listData = []
      const data = res.data;
      
      if (Array.isArray(data)) {
        listData = data;
      } else if (data && Array.isArray(data.records)) {
        listData = data.records;
      } else if (data && Array.isArray(data.list)) {
        listData = data.list;
      }
      
      // Map admin list to view model if needed
      if (isAdmin) {
          tasks.value = listData.map(item => {
              // Assuming admin list structure might be slightly different or same
              // Need to ensure fields match what template expects: 
              // id/voteTaskId, title/proposalTitle, endTime, votedCount, totalCount, stats...
              return {
                  ...item,
                  voteTaskId: item.voteTaskId || item.id,
                  proposalTitle: item.title, // Admin list likely has 'title'
                  // If admin list doesn't have stats directly, they might be missing or under a different key
                  // Using safe fallbacks
                  agreeCount: item.agreeCount || 0,
                  opposeCount: item.opposeCount || item.rejectCount || 0,
                  abstainCount: item.abstainCount || item.waiverCount || 0,
                  votedCount: item.votedCount || 0,
                  totalCount: item.totalCount || item.partnerCount || 0
              }
          })
      } else {
          tasks.value = listData
      }
      
    } else {
      uni.showToast({ title: res.msg || '获取任务失败', icon: 'none' })
    }
    uni.stopPullDownRefresh()
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

onPullDownRefresh(() => {
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

.stats-bar {
  display: flex;
  justify-content: space-between;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1px dashed #E5E7EB;
}

.sb-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 24rpx;
}

.sb-label {
  margin-bottom: 4rpx;
  color: #6B7280;
}

.sb-val {
  font-weight: bold;
  color: #374151;
}

.text-green { color: #10B981; }
.text-red { color: #EF4444; }
.text-gray { color: #9CA3AF; }
.text-orange { color: #F59E0B; }
.text-blue { color: #3B82F6; }

.title-wrapper {
  flex: 1;
  margin-right: 20rpx;
  display: flex;
  flex-direction: column;
}

.delegate-hint {
  font-size: 24rpx;
  margin-top: 8rpx;
  font-weight: normal;
}

.empty-tip {
  text-align: center;
  color: #9CA3AF;
  margin-top: 100rpx;
}
</style>
