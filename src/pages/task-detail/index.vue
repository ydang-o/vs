<template>
  <view class="container">
    <view class="detail-card" v-if="task && task.proposal">
      <view class="header">
        <text class="title">{{ task.proposal.title }}</text>
        <text class="delegate-hint text-orange" v-if="task.voteTask && task.voteTask.delegateeName">
          (您已委托 {{ task.voteTask.delegateeName }} 投票)
        </text>
        <text class="delegate-hint text-blue" v-if="task.voteTask && task.voteTask.delegatorName">
          ({{ task.voteTask.delegatorName }} 委托您投票)
        </text>
        <view class="meta">
          <text>截止时间：{{ task.voteTask.endTime }}</text>
        </view>
      </view>
      
      <view class="content">
        <text class="desc">{{ task.proposal.content || '暂无详细描述' }}</text>
        <image 
          v-if="task.proposal.imageUrl" 
          class="detail-image" 
          :src="task.proposal.imageUrl" 
          mode="widthFix" 
          @click="previewImage(task.proposal.imageUrl)"
        ></image>
      </view>

      <!-- Statistics Area for All Users -->
      <view class="stats-card" v-if="statistics">
        <view class="stat-row main-stat">
          <view class="stat-item">
            <text class="stat-num">{{ statistics.voted }}</text>
            <text class="stat-label">已投票</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-num">{{ statistics.unvoted }}</text>
            <text class="stat-label">未投票</text>
          </view>
        </view>
        <view class="stat-row sub-stat">
          <view class="stat-item">
            <text class="stat-val text-green">{{ statistics.agree }}</text>
            <text class="stat-label">同意</text>
          </view>
          <view class="stat-item">
            <text class="stat-val text-red">{{ statistics.reject }}</text>
            <text class="stat-label">反对</text>
          </view>
          <view class="stat-item">
            <text class="stat-val text-gray">{{ statistics.abstain }}</text>
            <text class="stat-label">弃权</text>
          </view>
        </view>
      </view>
      
      <!-- Self Vote Area -->
        <view class="vote-area" v-if="task.voteTask.canVote && !isExpired">
          <view class="section-title">本人投票</view>
          <view class="btn-group">
            <button class="vote-btn agree" @click="handleVote(1, 'self')">同意</button>
            <button class="vote-btn reject" @click="handleVote(2, 'self')">反对</button>
            <button class="vote-btn abstain" @click="handleVote(3, 'self')">弃权</button>
          </view>
          <view class="delegate-create-area">
             <button class="delegate-create-btn" @click="goToDelegateCreate">委托他人投票</button>
          </view>
        </view>

        <!-- Delegate Vote Area(s) -->
        <view class="vote-area delegate-vote-area" v-for="(delegate, index) in delegateList" :key="index">
          <view class="section-title">代 {{ delegate.fromPartnerName }} 投票</view>
          
          <view class="btn-group" v-if="delegate.hasVoted !== 1">
            <button class="vote-btn agree" @click="handleVote(1, 'delegate', delegate)">同意</button>
            <button class="vote-btn reject" @click="handleVote(2, 'delegate', delegate)">反对</button>
            <button class="vote-btn abstain" @click="handleVote(3, 'delegate', delegate)">弃权</button>
          </view>
          <view class="result-text" v-else>
            <text>已完成代投</text>
          </view>
        </view>

        <!-- Old Delegate Area (Hidden) -->
        <!-- <view class="delegate-area" v-if="task.voteTask.canDelegateVote && !isExpired">
           <view class="section-title">委托投票</view>
           <button class="delegate-btn" @click="handleDelegateVote">处理委托投票</button>
        </view> -->
        
        <view class="result-area" v-if="(task.voteTask.hasVoted || isExpired) && !task.voteTask.hasPendingDelegatedVote">
          <text class="result-text" v-if="task.voteTask.hasVoted">您已完成本人投票</text>
          <text class="result-text" v-else-if="isExpired">投票已截止</text>
          <button class="view-result-btn" @click="viewResult">查看结果</button>
        </view>

        <!-- Admin View: Voter Details -->
        <view class="admin-area" v-if="isAdmin && task.voteTask.voterList && task.voteTask.voterList.length > 0">
          <view class="section-title">投票人详情 (管理员可见)</view>
          <view class="voter-list">
            <view class="voter-header">
              <text class="th">姓名</text>
              <text class="th">状态</text>
            </view>
            <view class="voter-item" v-for="(voter, idx) in task.voteTask.voterList" :key="idx">
              <text class="voter-name">{{ voter.realName || voter.name || voter.username || '未知' }}</text>
              <text class="voter-status" :class="getVoteColorClass(voter.voteOption)">
                {{ getVoteText(voter.voteOption) }}
              </text>
            </view>
          </view>
        </view>
    </view>
    
    <!-- Result Modal Popup -->
    <view v-if="showResultModal" class="modal-overlay" @click="closeResultModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">投票结果</text>
          <text class="close-btn" @click="closeResultModal">×</text>
        </view>
        
        <scroll-view class="modal-body" scroll-y="true">
          <!-- Task Status -->
          <view class="result-section">
            <text class="section-title">投票状态</text>
            <view class="status-info">
              <text :class="['status-text', voteStatistics.taskStatus === 2 ? 'status-ended' : 'status-progress']">
                {{ voteStatistics.taskStatus === 2 ? '已结束' : '进行中' }}
              </text>
            </view>
          </view>
          
          <!-- Progress Info -->
          <view class="result-section" v-if="voteStatistics.progress">
            <text class="section-title">投票进度</text>
            <view class="progress-stats">
              <view class="progress-stat-item">
                <text class="stat-label">总参与人数：</text>
                <text class="stat-value">{{ voteStatistics.progress.stat.partnerCount }}</text>
              </view>
              <view class="progress-stat-item">
                <text class="stat-label">已投票人数：</text>
                <text class="stat-value">{{ voteStatistics.progress.stat.votedCount }}</text>
              </view>
              <view class="progress-stat-item">
                <text class="stat-label">未投票人数：</text>
                <text class="stat-value">{{ voteStatistics.progress.stat.unvotedCount }}</text>
              </view>
              <view class="progress-stat-item">
                <text class="stat-label">投票进度：</text>
                <text class="stat-value">{{ voteStatistics.progress.stat.progress }}%</text>
              </view>
            </view>
            
            <view class="vote-distribution">
              <view class="vote-item">
                <text class="vote-option agree">同意：{{ voteStatistics.progress.stat.agreeCount }}</text>
                <text class="vote-ratio">({{ voteStatistics.progress.stat.agreeRatio }}%)</text>
              </view>
              <view class="vote-item">
                <text class="vote-option reject">反对：{{ voteStatistics.progress.stat.rejectCount }}</text>
                <text class="vote-ratio">({{ voteStatistics.progress.stat.rejectRatio }}%)</text>
              </view>
              <view class="vote-item">
                <text class="vote-option abstain">弃权：{{ voteStatistics.progress.stat.abstainCount }}</text>
                <text class="vote-ratio">({{ voteStatistics.progress.stat.abstainRatio }}%)</text>
              </view>
            </view>
          </view>
          
          <!-- Voting Records (only shown if not anonymous) -->
          <view class="result-section" v-if="voteStatistics.progress && !voteStatistics.progress.anonymous && voteStatistics.progress.records && voteStatistics.progress.records.length > 0">
            <text class="section-title">投票记录</text>
            <view class="records-list">
              <view class="record-item" v-for="(record, index) in voteStatistics.progress.records" :key="index">
                <view class="record-header">
                  <text class="record-name">{{ record.name }}</text>
                  <text :class="['record-vote', getVoteClass(record.voteOption)]">{{ record.voteOption }}</text>
                </view>
                <view class="record-details">
                  <text class="record-time" v-if="record.voteTime">投票时间：{{ record.voteTime }}</text>
                  <text class="record-status" v-if="record.isDelegate" :class="{ 'delegate-record': record.isDelegate }">委托投票</text>
                  <text class="record-status" v-if="record.delegateTo">委托给：{{ record.delegateTo }}</text>
                  <text class="record-status" v-if="record.delegateBy">受托于：{{ record.delegateBy }}</text>
                </view>
              </view>
            </view>
          </view>
          
          <!-- Anonymous Notice -->
          <view class="result-section" v-if="voteStatistics.progress && voteStatistics.progress.anonymous">
            <text class="section-title">投票记录</text>
            <view class="anonymous-notice">
              <text class="notice-text">本投票为匿名投票，不显示具体投票人信息</text>
            </view>
          </view>
          
          <!-- Final Result (only shown if task ended) -->
          <view class="result-section" v-if="voteStatistics.taskStatus === 2 && voteStatistics.result">
            <text class="section-title">最终结果</text>
            <view class="final-result">
              <view class="result-summary">
                <text class="result-status" :class="{ 'passed': voteStatistics.result.finalResult, 'failed': !voteStatistics.result.finalResult }">
                  {{ voteStatistics.result.finalResult ? '投票通过' : '投票未通过' }}
                </text>
              </view>
              
              <view class="result-description" v-if="voteStatistics.result.resultDesc">
                <text class="result-desc">{{ voteStatistics.result.resultDesc }}</text>
              </view>
              
              <view class="strategy-info">
                <text class="strategy-label">投票策略：</text>
                <text class="strategy-value">
                  {{ voteStatistics.result.voteStrategy === 1 ? '人数票' : 
                     voteStatistics.result.voteStrategy === 2 ? '出资票' : 
                     '组合策略' }}
                </text>
              </view>
              
              <view class="threshold-info">
                <text class="threshold-label">通过阈值：</text>
                <text class="threshold-value">{{ voteStatistics.result.passRate }}%</text>
              </view>
              
              <!-- People Vote Result (show only if strategy is people or combination) -->
              <view class="sub-result-section" v-if="voteStatistics.result.voteStrategy === 1 || voteStatistics.result.voteStrategy === 3">
                <text class="sub-result-title">人数票结果</text>
                <view class="sub-result-details">
                  <view class="result-item">
                    <text class="result-label">是否通过：</text>
                    <text class="result-value" :class="{ 'passed': voteStatistics.result.peoplePassed, 'failed': !voteStatistics.result.peoplePassed }">
                      {{ voteStatistics.result.peoplePassed ? '是' : '否' }}
                    </text>
                  </view>
                  <view class="result-item">
                    <text class="result-label">总票权数：</text>
                    <text class="result-value">{{ voteStatistics.result.peopleTotalVotes }}</text>
                  </view>
                  <view class="result-item">
                    <text class="result-label">同意票数：</text>
                    <text class="result-value">{{ voteStatistics.result.peopleAgreeVotes }}</text>
                  </view>
                  <view class="result-item">
                    <text class="result-label">反对票数：</text>
                    <text class="result-value">{{ voteStatistics.result.peopleRejectVotes }}</text>
                  </view>
                  <view class="result-item">
                    <text class="result-label">弃权票数：</text>
                    <text class="result-value">{{ voteStatistics.result.peopleAbstainVotes }}</text>
                  </view>
                  <view class="result-item">
                    <text class="result-label">同意率：</text>
                    <text class="result-value">{{ voteStatistics.result.peopleAgreeRatio }}%</text>
                  </view>
                </view>
              </view>
              
              <!-- Capital Vote Result (show only if strategy is capital or combination) -->
              <view class="sub-result-section" v-if="voteStatistics.result.voteStrategy === 2 || voteStatistics.result.voteStrategy === 3">
                <text class="sub-result-title">出资票结果</text>
                <view class="sub-result-details">
                  <view class="result-item">
                    <text class="result-label">是否通过：</text>
                    <text class="result-value" :class="{ 'passed': voteStatistics.result.capitalPassed, 'failed': !voteStatistics.result.capitalPassed }">
                      {{ voteStatistics.result.capitalPassed ? '是' : '否' }}
                    </text>
                  </view>
                  <view class="result-item">
                    <text class="result-label">总权重：</text>
                    <text class="result-value">{{ voteStatistics.result.capitalTotalRatio }}%</text>
                  </view>
                  <view class="result-item">
                    <text class="result-label">同意权重：</text>
                    <text class="result-value">{{ voteStatistics.result.capitalAgreeRatio }}%</text>
                  </view>
                  <view class="result-item">
                    <text class="result-label">反对权重：</text>
                    <text class="result-value">{{ voteStatistics.result.capitalRejectRatio }}%</text>
                  </view>
                  <view class="result-item">
                    <text class="result-label">弃权权重：</text>
                    <text class="result-value">{{ voteStatistics.result.capitalAbstainRatio }}%</text>
                  </view>
                  <view class="result-item">
                    <text class="result-label">同意得票率：</text>
                    <text class="result-value">{{ voteStatistics.result.capitalAgreeVoteRatio }}%</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onPullDownRefresh, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import request from '@/utils/request.js'
import { useShare } from '@/composables/useShare.js'

const task = ref(null)
const taskId = ref(null)
const isAdmin = ref(false)
const delegateList = ref([])
const showResultModal = ref(false)
const voteStatistics = ref({})

// Use global share with dynamic title
useShare({
  title: () => task.value?.proposal?.title ? `投票：${task.value.proposal.title}` : '任务详情'
})

const statistics = computed(() => {
  if (!task.value || !task.value.voteTask) return null
  
  const vt = task.value.voteTask
  
  let agree = vt.agreeCount || 0
  let reject = vt.opposeCount || vt.rejectCount || 0
  let abstain = vt.abstainCount || vt.waiverCount || 0
  
  // Calculate from list if explicit counts are missing
  if (agree === 0 && reject === 0 && abstain === 0 && vt.voterList && vt.voterList.length > 0) {
    vt.voterList.forEach(v => {
      if (v.voteOption === 1) agree++
      else if (v.voteOption === 2) reject++
      else if (v.voteOption === 3) abstain++
    })
  }
  
  const total = vt.totalCount || 0
  const voted = vt.votedCount || (agree + reject + abstain)
  const unvoted = total > voted ? total - voted : 0
  
  return {
    agree,
    reject,
    abstain,
    voted,
    unvoted,
    total
  }
})

const isExpired = computed(() => {
  if (!task.value || !task.value.voteTask) return false
  const timeStr = task.value.voteTask.endTime
  if (!timeStr || timeStr === '进行中') return false
  try {
    const target = new Date(timeStr.replace(/-/g, '/')).getTime()
    return Date.now() > target
  } catch (e) {
    return false
  }
})

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

const fetchDelegateInfo = (id) => {
  request({
    url: `/user/h5/vote/delegate/detail/${id}`,
    method: 'GET'
  }).then(res => {
    console.log('Delegate info response:', res)
    if (res.code === 0 || res.code === 1 || res.code === 200) {
       const data = res.data
       if (!data) {
         delegateList.value = []
         return
       }
       
       let list = []
       if (Array.isArray(data)) {
          list = data
       } else if (data.list && Array.isArray(data.list)) {
          list = data.list
       } else if (data.records && Array.isArray(data.records)) {
          list = data.records
       } else if (typeof data === 'object' && (data.id || data.toPartnerId)) {
          // Single object fallback
          list = [data]
       }
       
       const userInfo = uni.getStorageSync('userInfo')
       // Try partnerId first, then id
       const currentUserId = userInfo.partnerId || userInfo.id
       
       console.log('Current User ID:', currentUserId)
       console.log('Raw Delegate List:', list)

       // Relaxed filtering: trust the API to return relevant delegations. 
       // Filter only by status (1=Active) and ensure fromPartnerId exists.
       // Previously filtered by toPartnerId == currentUserId, but this caused issues with ID mismatches (e.g. userId vs partnerId).
       delegateList.value = list.filter(d => d.status === 1 && d.fromPartnerId)
       console.log('Filtered Delegate List:', delegateList.value)
    }
  }).catch(err => {
     console.error('Fetch delegate info failed', err)
  })
}

onLoad((options) => {
  if (options.id) {
    taskId.value = options.id
    const userInfo = uni.getStorageSync('userInfo')
    // As per user requirement: Only 'admin' username can see admin view
    if (userInfo && userInfo.username === 'admin') {
      isAdmin.value = true
    }
    fetchDetail(options.id)
  }
})

onPullDownRefresh(() => {
  if (taskId.value) {
    fetchDetail(taskId.value)
  } else {
    uni.stopPullDownRefresh()
  }
})

const fetchDetail = (id) => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }

  // Use the same endpoint for both Admin and User
  // Admin and User: /user/h5/vote/task/detail/{id} (id is VoteTask ID)
  const url = `/user/h5/vote/task/detail/${id}`

  request({
    url: url,
    method: 'GET'
  }).then(res => {
    uni.stopPullDownRefresh()
    console.log(`Fetching detail from endpoint:`, url)
    
    // Fetch delegate info separately
    fetchDelegateInfo(id)
    
    if (res.code === 0 || res.code === 1 || res.code === 200) {
      const payload = res.data || {}
      const BASE_URL = 'http://localhost:8080'
      const resolveImageUrl = (path) => {
        if (!path) return ''
        // If it contains the view controller, it's likely correct (check if needs base url)
        if (path.indexOf('/user/file/view') !== -1) {
             if (path.startsWith('http')) return path
             return BASE_URL + path
        }
        
        // If it starts with http (full URL) but missing controller
        if (path.startsWith('http')) {
             // If it matches our BASE_URL, strip it and fix
             if (path.startsWith(BASE_URL)) {
                 const relative = path.replace(BASE_URL, '')
                 return `${BASE_URL}/user/file/view?filePath=${relative}`
             }
             // External URL? Return as is
             return path
        }
        
        // Relative path, add base url and controller
        return `${BASE_URL}/user/file/view?filePath=${path}`
      }
      
      // Unified Response Mapping
      // Check if it's the new flat structure
      const isFlatStructure = payload.voteTaskId !== undefined && payload.title !== undefined
      
      if (isFlatStructure) {
          const stats = payload.voteStats || {}
          task.value = {
              proposal: {
                  title: payload.title,
                  content: payload.content || '暂无详细描述',
                  imageUrl: resolveImageUrl(payload.imageUrl)
              },
              voteTask: {
                  voteTaskId: payload.voteTaskId,
                  endTime: payload.endTime || '进行中',
                  canVote: payload.canVote,
                  canDelegateVote: payload.canDelegateVote,
                  hasVoted: payload.hasVoted,
                  voterList: payload.voteRecords || [],
                  // Statistics
                  agreeCount: stats.agreeCount || 0,
                  rejectCount: stats.rejectCount || 0,
                  abstainCount: stats.abstainCount || 0,
                  votedCount: stats.votedCount || 0,
                  totalCount: stats.partnerCount || 0,
                  // Delegate info not in flat structure, handled by fetchDelegateInfo
                  delegateeName: null,
                  delegatorName: null,
                  // New field for pending delegated votes
                  hasPendingDelegatedVote: payload.hasPendingDelegatedVote || false
              }
          }
      } else {
          // Old structure fallback or other variations
          const proposalData = payload.proposal || payload.Proposal
          const voteTaskData = payload.voteTask || payload.VoteTask
    
          task.value = {
            proposal: {
              title: proposalData ? proposalData.title : (payload.title || ''),
              content: proposalData ? (proposalData.content || '暂无详细描述') : (payload.content || '暂无详细描述'),
              imageUrl: resolveImageUrl(proposalData ? proposalData.imageUrl : payload.imageUrl)
            },
            voteTask: {
              voteTaskId: voteTaskData ? voteTaskData.voteTaskId : id,
              endTime: voteTaskData ? (voteTaskData.endTime || '进行中') : (payload.endTime || '进行中'),
              // Relaxed status check: allow if status is 1 or undefined (backward compatibility)
              canVote: voteTaskData ? ((voteTaskData.status === 1 || voteTaskData.status === undefined) && !voteTaskData.hasVoted) : (payload.canVote),
              canDelegateVote: voteTaskData ? voteTaskData.canDelegateVote : (payload.canDelegateVote),
              hasVoted: voteTaskData ? voteTaskData.hasVoted : (payload.hasVoted),
              votedCount: voteTaskData ? (voteTaskData.votedCount || 0) : 0,
              totalCount: voteTaskData ? (voteTaskData.totalCount || 0) : 0,
              voterList: voteTaskData ? (voteTaskData.voterList || []) : [],
              delegateeName: voteTaskData ? voteTaskData.delegateeName : payload.delegateeName,
              delegatorName: voteTaskData ? voteTaskData.delegatorName : payload.delegatorName,
              hasPendingDelegatedVote: (voteTaskData && voteTaskData.hasPendingDelegatedVote) || payload.hasPendingDelegatedVote || false
            }
          }
      }
      
      // If admin, fetch additional stats details (still needed for detailed voter list if not present in main detail)
      if (isAdmin.value) {
        fetchAdminStats(id)
      }
    }
  }).catch(err => {
    uni.stopPullDownRefresh()
    // Force HBuilderX re-compile
    if (err.statusCode !== 401) {
      console.error(err)
    }
    // Mock for error case or dev
    task.value = {
      proposal: {
        title: '关于扩大合伙人规模的议案',
        content: '为了适应公司快速发展的需求，提议引入新的战略合伙人...'
      },
      voteTask: {
        voteTaskId: id,
        endTime: '2023-12-31',
        canVote: true,
        canDelegateVote: true,
        hasVoted: false
      }
    }
  })
}

const fetchAdminStats = (id) => {
  request({
    url: `/user/h5/vote/task/detail/${id}`,
    method: 'GET'
  }).then(res => {
    if (res.code === 0 || res.code === 1 || res.code === 200) {
       const data = res.data || {}
       if (task.value && task.value.voteTask) {
         // Merge voter list from admin endpoint
         if (data.voteRecords) {
           task.value.voteTask.voterList = data.voteRecords
         }
         // Merge stats
         const stats = data.voteStats
         if (stats) {
           task.value.voteTask.agreeCount = stats.agreeCount || 0
           task.value.voteTask.rejectCount = stats.rejectCount || 0
           task.value.voteTask.abstainCount = stats.abstainCount || 0
           task.value.voteTask.votedCount = stats.votedCount || 0
           task.value.voteTask.totalCount = stats.partnerCount || 0
         }
       }
    }
  }).catch(err => {
    console.error('Failed to fetch admin stats:', err)
  })
}

const getVoteText = (option) => {
  // If option is already a string (e.g. "同意"), return it
  if (typeof option === 'string' && option) return option
  // If option is null/undefined/empty string, it means not voted
  if (!option) return '未投票'
  
  const map = {
    1: '同意',
    2: '反对',
    3: '弃权'
  }
  return map[option] || '未知'
}

const getVoteClass = (option) => {
  if (typeof option === 'string') {
    if (option === '同意') return 'agree'
    if (option === '反对') return 'reject'
    if (option === '弃权') return 'abstain'
    return ''
  }
  
  const map = {
    1: 'agree',
    2: 'reject',
    3: 'abstain'
  }
  return map[option] || ''
}

const getVoteColorClass = (option) => {
  // Handle string options
  if (typeof option === 'string') {
    if (option === '同意') return 'text-green'
    if (option === '反对') return 'text-red'
    if (option === '弃权') return 'text-gray'
    return 'text-gray' // Default/Unvoted
  }
  
  const map = {
    1: 'text-green',
    2: 'text-red',
    3: 'text-gray'
  }
  return map[option] || 'text-gray'
}

const previewImage = (url) => {
  if (!url) return
  uni.previewImage({
    urls: [url]
  })
}

const handleVote = (option, type, delegateData) => {
  // Determine if it's a delegate vote based on data presence
  const isDelegate = delegateData && delegateData.fromPartnerId && delegateData.fromPartnerName
  
  const optionText = getVoteText(option)
  let title = '确认投票'
  let content = `确认投"${optionText}"吗？提交后不可修改。`
  
  if (isDelegate) {
      title = '确认代投'
      content = `确认代 ${delegateData.fromPartnerName} 投"${optionText}"吗？`
  }
  
  uni.showModal({
    title: title,
    content: content,
    success: (res) => {
      if (res.confirm) {
        submitVote(option, isDelegate ? 'delegate' : 'self', delegateData)
      }
    }
  })
}

const submitVote = (option, type, delegateData) => {
  uni.showLoading({ title: '提交中' })
  
  let url = '/user/h5/vote/submit'
  // Ensure numeric types for API
  const postData = {
    voteTaskId: Number(taskId.value),
    voteOption: Number(option)
  }
  
  // Double check if it's a delegate vote based on data presence
  const isDelegate = type === 'delegate' || (delegateData && delegateData.fromPartnerId)
  
  if (isDelegate) {
      url = '/user/h5/vote/delegate/submit'
      if (delegateData && delegateData.fromPartnerId) {
          postData.fromPartnerId = Number(delegateData.fromPartnerId)
      } else {
          uni.hideLoading()
          uni.showToast({ title: '缺少委托人信息', icon: 'none' })
          return
      }
  }
  
  console.log(`Submitting vote to ${url}:`, postData)
  
  request({
    url: url,
    method: 'POST',
    data: postData
  }).then(res => {
    uni.hideLoading()
    // Relaxed check logic aligned with delegate-create:
    // code=1: Success (even if data is null)
    // code=200: Success
    // code=0: Success ONLY if data exists (to filter out business errors with code=0 like "Already delegated")
    if (res.code === 1 || res.code === 200 || (res.code === 0 && res.data)) {
      uni.showToast({ title: '投票成功' })
      setTimeout(() => {
          fetchDetail(taskId.value)
      }, 1000)
    } else {
      uni.showToast({ title: res.msg || '投票失败', icon: 'none' })
    }
  }).catch(() => {
    uni.hideLoading()
  })
}

const checkIsUrgent = (target) => {
  try {
    const now = Date.now()
    const diff = target - now
    return diff > 0 && diff <= 10800000
  } catch (e) {
    return false
  }
}

const goToDelegateCreate = () => {
  uni.navigateTo({
    url: `/pages/delegate-create/index?taskId=${taskId.value}`
  })
}

const viewResult = () => {
  uni.showLoading({ title: '加载中' });
  
  request({
    url: `/user/vote/task/stat/${taskId.value}`,
    method: 'GET'
  }).then(res => {
    uni.hideLoading();
    if (res.code === 0 || res.code === 1 || res.code === 200) {
      voteStatistics.value = res.data;
      showResultModal.value = true;
      console.log('Vote statistics:', res.data);
    } else {
      uni.showToast({ title: res.msg || '获取结果失败', icon: 'none' });
    }
  }).catch(err => {
    uni.hideLoading();
    console.error('Failed to fetch vote statistics:', err);
    uni.showToast({ title: '获取结果失败', icon: 'none' });
  });
}

const closeResultModal = () => {
  showResultModal.value = false;
}
</script>

<style>
.container {
  padding: 30rpx;
}

.stats {
  margin-left: 20rpx;
  color: #3B82F6;
  font-weight: 500;
}

.stats-card {
  background-color: #F3F4F6;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-top: 20rpx;
}

.stat-row {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.main-stat {
  padding-bottom: 20rpx;
  border-bottom: 1px solid #E5E7EB;
  margin-bottom: 20rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-divider {
  width: 1px;
  height: 60rpx;
  background-color: #D1D5DB;
}

.stat-num {
  font-size: 36rpx;
  font-weight: bold;
  color: #111827;
}

.stat-val {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 4rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #6B7280;
}

.detail-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #111;
  margin-bottom: 20rpx;
  display: block;
}

.meta {
  color: #666;
  font-size: 24rpx;
  padding-bottom: 30rpx;
  border-bottom: 1px solid #eee;
}

.content {
  padding: 40rpx 0;
  min-height: 200rpx;
}

.desc {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.detail-image {
  width: 100%;
  margin-top: 20rpx;
  border-radius: 8rpx;
}

.action-area {
  margin-top: 40rpx;
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  color: #333;
}

.btn-group {
  display: flex;
  justify-content: space-around;
}

.vote-btn {
  width: 180rpx;
  font-size: 28rpx;
}

.delegate-create-area {
  margin-top: 20rpx;
}

.delegate-create-btn {
  background-color: #fff;
  color: #3B82F6;
  font-size: 28rpx;
  border: 1px solid #3B82F6;
  width: 100%;
}

.delegate-btn {
  background-color: #3B82F6;
  color: white;
  font-size: 28rpx;
  width: 100%;
}

.text-danger {
  color: #EF4444 !important;
  font-weight: bold;
}

.agree {
  background-color: #10B981;
  color: white;
}

.reject {
  background-color: #EF4444;
  color: white;
}

.abstain {
  background-color: #9CA3AF;
  color: white;
}

.view-result-btn {
  margin-top: 20rpx;
  background-color: #fff;
  color: #3B82F6;
  border: 1px solid #3B82F6;
  font-size: 28rpx;
  width: 100%;
}

.admin-area {
  margin-top: 40rpx;
  padding-top: 30rpx;
  border-top: 1px solid #eee;
}

/* Admin List Styles */
.admin-list-container {
  margin-top: 24rpx;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  height: 600rpx; /* Fixed height for scroll area */
}

.list-header {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  color: #111827;
  flex-shrink: 0;
}

.list-content {
  flex: 1;
  overflow-y: auto;
}

/* Hide scrollbar for Chrome, Safari and Opera */
.list-content::-webkit-scrollbar {
  display: none;
}

/* Hide scrollbar for IE, Edge and Firefox */
.list-content {
  -ms-overflow-style: none;  /* IE and Edge */
  scrollbar-width: none;  /* Firefox */
}

.voter-item {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1px dashed #E5E7EB;
  font-size: 28rpx;
}

.voter-item:last-child {
  border-bottom: none;
}

.voter-name {
  color: #374151;
}

.text-green { color: #10B981; }
.text-red { color: #EF4444; }
.text-gray { color: #9CA3AF; }
.text-orange { color: #F59E0B; }
.text-blue { color: #3B82F6; }
.delegate-hint {
  font-size: 26rpx;
  margin-bottom: 10rpx;
  display: block;
  font-weight: 500;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 16rpx;
  width: 90%;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  border-bottom: 1px solid #eee;
}

.modal-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.close-btn {
  font-size: 32rpx;
  color: #999;
  line-height: 1;
  cursor: pointer;
}

.modal-body {
  padding: 15rpx;
  flex: 1;
  max-height: 60vh;
}

.result-section {
  margin-bottom: 15rpx;
}

.result-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
  display: block;
}

.status-info {
  padding: 10rpx;
  background-color: #f8f9fa;
  border-radius: 6rpx;
  margin-bottom: 10rpx;
}

.status-text {
  font-size: 24rpx;
  font-weight: bold;
}

.status-progress {
  color: #f59e0b;
}

.status-ended {
  color: #10b981;
}

.progress-stats {
  background-color: #f8f9fa;
  border-radius: 6rpx;
  padding: 10rpx;
  margin-bottom: 10rpx;
}

.progress-stat-item {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 5rpx;
  font-size: 22rpx;
}

.progress-stat-item:last-child {
  margin-bottom: 0;
}

.stat-label {
  color: #666;
  flex-shrink: 0;
  margin-right: 8rpx;
}

.stat-value {
  color: #333;
  font-weight: 500;
  flex-shrink: 0;
}

.vote-distribution {
  background-color: #f8f9fa;
  border-radius: 6rpx;
  padding: 10rpx;
}

.vote-item {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: 5rpx;
}

.vote-item:last-child {
  margin-bottom: 0;
}

.vote-option {
  font-weight: 500;
  flex-shrink: 0;
  margin-right: 8rpx;
}

.vote-option.agree {
  color: #10B981;
}

.vote-option.reject {
  color: #EF4444;
}

.vote-option.abstain {
  color: #9CA3AF;
}

.vote-ratio {
  color: #666;
  font-size: 20rpx;
  flex-shrink: 0;
}

.records-list {
  background-color: #f8f9fa;
  border-radius: 6rpx;
  padding: 10rpx;
}

.record-item {
  padding: 8rpx 0;
  border-bottom: 1px solid #e9ecef;
}

.record-item:last-child {
  border-bottom: none;
}

.record-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: 4rpx;
}

.record-name {
  font-weight: 500;
  color: #333;
  flex: 1;
  margin-right: 8rpx;
  font-size: 22rpx;
}

.record-vote {
  font-weight: bold;
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
  font-size: 20rpx;
  text-align: right;
  min-width: 50rpx;
}

.record-vote.agree {
  color: #10B981;
  background-color: rgba(16, 185, 129, 0.1);
}

.record-vote.reject {
  color: #EF4444;
  background-color: rgba(239, 68, 68, 0.1);
}

.record-vote.abstain {
  color: #9CA3AF;
  background-color: rgba(156, 163, 175, 0.1);
}

.record-vote.not-voted {
  color: #6B7280;
  background-color: rgba(107, 114, 128, 0.1);
}

.record-details {
  font-size: 20rpx;
  color: #666;
  padding-left: 8rpx;
}

.record-status {
  display: block;
  margin-top: 2rpx;
}

.delegate-record {
  color: #f59e0b;
}

.anonymous-notice {
  background-color: #f8f9fa;
  border-radius: 6rpx;
  padding: 10rpx;
  text-align: center;
}

.notice-text {
  font-size: 22rpx;
  color: #666;
}

.final-result {
  background-color: #f8f9fa;
  border-radius: 6rpx;
  padding: 10rpx;
}

.result-summary {
  text-align: center;
  margin-bottom: 10rpx;
  padding-bottom: 8rpx;
  border-bottom: 1px solid #e9ecef;
}

.result-status {
  font-size: 24rpx;
  font-weight: bold;
}

.result-status.passed {
  color: #10b981;
}

.result-status.failed {
  color: #ef4444;
}

.result-description {
  text-align: center;
  margin-bottom: 10rpx;
  padding-bottom: 8rpx;
  border-bottom: 1px solid #e9ecef;
}

.result-desc {
  font-size: 22rpx;
  color: #333;
}

.strategy-info, .threshold-info {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 8rpx;
  padding: 4rpx 0;
  font-size: 22rpx;
}

.strategy-label, .threshold-label {
  color: #666;
  flex-shrink: 0;
  margin-right: 8rpx;
}

.strategy-value, .threshold-value {
  color: #333;
  font-weight: 500;
  flex-shrink: 0;
}

.sub-result-section {
  margin-top: 10rpx;
  padding-top: 8rpx;
  border-top: 1px solid #e9ecef;
}

.sub-result-title {
  font-size: 22rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 6rpx;
  display: block;
}

.sub-result-details {
  background-color: #fff;
  border-radius: 6rpx;
  padding: 8rpx;
}

.result-item {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 4rpx;
  font-size: 20rpx;
}

.result-label {
  color: #666;
  flex-shrink: 0;
  margin-right: 8rpx;
}

.result-value {
  color: #333;
  font-weight: 500;
  flex-shrink: 0;
}

.result-value.passed {
  color: #10b981;
}

.result-value.failed {
  color: #ef4444;
}
</style>