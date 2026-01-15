<template>
  <view class="container">
    <view class="tabs">
      <view class="tab-item" :class="{ active: currentTab === 0 }" @click="switchTab(0)">待投票</view>
      <view class="tab-item" :class="{ active: currentTab === 1 }" @click="switchTab(1)">已投票</view>
    </view>
    
    <scroll-view scroll-y class="list-content" @scrolltolower="loadMore">
      <view class="empty-tip" v-if="list.length === 0 && !loading">
        <text>暂无数据</text>
      </view>
      
      <view 
        class="task-card" 
        v-for="(item, index) in list" 
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
          <text class="status-badge" :class="getStatusClass(item)" v-if="currentTab === 1">
            {{ getStatusText(item) }}
          </text>
          <text class="urgent-badge" v-if="currentTab === 0 && item.urgent">紧急</text>
        </view>
        <view class="card-body">
           <view class="info-row">
            <text class="label" v-if="currentTab === 0">截止时间：</text>
            <text class="label" v-else>投票时间：</text>
            <text class="value">{{ currentTab === 0 ? item.endTime : item.voteTime }}</text>
          </view>
           <view class="info-row" v-if="currentTab === 1">
            <text class="label">我的投票：</text>
            <text class="value highlight" :class="getVoteColorClass(item.myVoteOption)">
              {{ getVoteOptionText(item.myVoteOption) }}
            </text>
          </view>
        </view>
      </view>
      
      <view class="loading-more" v-if="loading">
        <text>加载中...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

const currentTab = ref(0)
const list = ref([])
const page = ref(1)
const loading = ref(false)
const hasMore = ref(true)

const isUrgent = (timeStr) => {
  if (!timeStr || timeStr === '进行中') return false
  try {
    const target = new Date(timeStr.replace(/-/g, '/')).getTime()
    if (isNaN(target)) return false
    const now = Date.now()
    const diff = target - now
    return diff > 0 && diff <= 10800000
  } catch (e) {
    return false
  }
}

const switchTab = (index) => {
  if (currentTab.value === index) return
  currentTab.value = index
  page.value = 1
  list.value = []
  hasMore.value = true
  fetchData()
}

onPullDownRefresh(() => {
  page.value = 1
  list.value = []
  hasMore.value = true
  // Reset loading to allow fetchData to run if it was stuck
  loading.value = false
  fetchData().finally(() => {
    uni.stopPullDownRefresh()
  })
})

const fetchData = () => {
  if (loading.value || !hasMore.value) return
  
  loading.value = true
  const isPending = currentTab.value === 0
  const url = isPending ? '/user/vote/my/pending' : '/user/vote/my/voted'
  
  return request({
    url: url,
    method: 'GET',
    data: {
      page: page.value,
      pageSize: 20
    },
    header: {
      'content-type': 'application/x-www-form-urlencoded'
    }
  }).then(res => {
    loading.value = false
    if (res.code === 0 || res.code === 1 || res.code === 200) {
      let records = [];
      
      // Handle different response structures based on "my voted" API docs and typical patterns
      // API docs say Response data is object, schema is generic. 
      // Assuming standard pagination structure or direct list.
      if (res.data && Array.isArray(res.data.records)) {
         records = res.data.records;
      } else if (res.data && Array.isArray(res.data.list)) {
         records = res.data.list;
      } else if (Array.isArray(res.data)) {
         records = res.data;
      }
      
      // Check for hasMore
      if (records.length < 20) {
        hasMore.value = false
      }
      
      if (page.value === 1) {
        list.value = records
      } else {
        list.value = [...list.value, ...records]
      }
      
      if (hasMore.value) {
        page.value++
      }
    } else {
        // Error
        hasMore.value = false
    }
  }).catch(err => {
    loading.value = false
    console.error(err)
    hasMore.value = false
  })
}

const loadMore = () => {
  fetchData()
}

const goToDetail = (id) => {
  uni.navigateTo({
    url: `/pages/task-detail/index?id=${id}`
  })
}

const getStatusClass = (item) => {
  // Logic might differ based on API response fields
  return (item.status === 'active' || item.status === 1) ? 'active' : 'ended'
}

const getStatusText = (item) => {
  return '已投票'
}

const getVoteOptionText = (val) => {
  const map = { 1: '同意', 2: '反对', 3: '弃权' }
  return map[val] || '未知'
}

const getVoteColorClass = (val) => {
    if (val === 1) return 'text-green'
    if (val === 2) return 'text-red'
    return 'text-gray'
}

onShow(() => {
  page.value = 1
  hasMore.value = true
  list.value = [] // Clear list on show to refresh or keep cache? Better refresh.
  fetchData()
})
</script>

<style>
.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #F8F9FA;
}

.tabs {
  display: flex;
  background-color: #fff;
  padding: 20rpx 0;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}

.tab-item {
  flex: 1;
  text-align: center;
  font-size: 30rpx;
  color: #666;
  position: relative;
  padding-bottom: 20rpx;
}

.tab-item.active {
  color: #3B82F6;
  font-weight: bold;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 6rpx;
  background-color: #3B82F6;
  border-radius: 3rpx;
}

.list-content {
  flex: 1;
  padding: 0 30rpx;
  box-sizing: border-box;
}

.task-card {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.02);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.task-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  flex: 1;
  margin-right: 20rpx;
}

.status-badge {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background-color: #E5E7EB;
  color: #666;
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
  font-size: 26rpx;
  margin-top: 10rpx;
}

.label {
  color: #888;
  width: 140rpx;
}

.value {
  color: #444;
}

.highlight {
  font-weight: bold;
}

.text-danger {
  color: #EF4444 !important;
  font-weight: bold;
}

.text-green { color: #10B981; }
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
.text-red { color: #EF4444; }
.text-gray { color: #9CA3AF; }

.empty-tip {
  text-align: center;
  color: #999;
  padding-top: 100rpx;
}

.loading-more {
  text-align: center;
  padding: 20rpx;
  color: #999;
  font-size: 24rpx;
}
</style>