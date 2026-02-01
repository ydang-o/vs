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
      <view class="stats-card" v-if="task && task.voteTask">
        <!-- Strategy 1 or 3: Headcount Participation -->
        <view class="stat-row main-stat" v-if="(currentVoteStrategy === 1 || currentVoteStrategy === 3) && effectiveItemType !== 2">
          <text class="section-label" v-if="currentVoteStrategy === 3 && !effectiveItemType" style="font-size: 24rpx; color: #666; margin-bottom: 10rpx; display: block; width: 100%;">1. 人数票统计</text>
          <view class="stat-item">
            <text class="stat-num">{{ task.voteTask.votedCount || 0 }}</text>
            <text class="stat-label">已投票</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-num">{{ (task.voteTask.totalCount || 0) - (task.voteTask.votedCount || 0) }}</text>
            <text class="stat-label">未投票</text>
          </view>
        </view>

        <!-- Strategy 1 or 3: Headcount Result Details -->
        <view v-if="(currentVoteStrategy === 1 || currentVoteStrategy === 3) && effectiveItemType !== 2">
           <view class="stat-row sub-stat">
            <view class="stat-item">
              <text class="stat-val text-green">{{ task.voteTask.agreeCount || 0 }}</text>
              <text class="stat-label">同意</text>
            </view>
            <view class="stat-item">
              <text class="stat-val text-red">{{ task.voteTask.rejectCount || 0 }}</text>
              <text class="stat-label">反对</text>
            </view>
            <view class="stat-item">
              <text class="stat-val text-gray">{{ task.voteTask.abstainCount || 0 }}</text>
              <text class="stat-label">弃权</text>
            </view>
          </view>
        </view>

        <view class="divider" v-if="currentVoteStrategy === 3 && effectiveItemType !== 1 && effectiveItemType !== 2" style="height: 1px; background: #eee; margin: 16rpx 0;"></view>

        <!-- Strategy 2 or 3: Capital Participation -->
        <view class="stat-row main-stat" v-if="(currentVoteStrategy === 2 || currentVoteStrategy === 3) && effectiveItemType !== 1">
          <text class="section-label" v-if="currentVoteStrategy === 3 && !effectiveItemType" style="font-size: 24rpx; color: #666; margin-bottom: 10rpx; display: block; width: 100%;">2. 出资票统计</text>
          <view class="stat-item">
            <text class="stat-num">{{ task.voteTask.capitalVotedCount || 0 }}</text>
            <text class="stat-label">{{ (currentVoteStrategy === 3 && !effectiveItemType) ? '已投(出资)' : '已投票' }}</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-num">{{ (task.voteTask.totalCount || 0) - (task.voteTask.capitalVotedCount || 0) }}</text>
            <text class="stat-label">{{ (currentVoteStrategy === 3 && !effectiveItemType) ? '未投(出资)' : '未投票' }}</text>
          </view>
        </view>

        <!-- Strategy 2 or 3: Capital Result Details -->
        <view v-if="(currentVoteStrategy === 2 || currentVoteStrategy === 3) && effectiveItemType !== 1">
           <view class="stat-row sub-stat">
            <view class="stat-item">
              <text class="stat-val text-green">{{ task.voteTask.capitalAgreeCount || 0 }}</text>
              <text class="stat-label">{{ (currentVoteStrategy === 3 && !effectiveItemType) ? '同意(出资)' : '同意' }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-val text-red">{{ task.voteTask.capitalRejectCount || 0 }}</text>
              <text class="stat-label">{{ (currentVoteStrategy === 3 && !effectiveItemType) ? '反对(出资)' : '反对' }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-val text-gray">{{ task.voteTask.capitalAbstainCount || 0 }}</text>
              <text class="stat-label">{{ (currentVoteStrategy === 3 && !effectiveItemType) ? '弃权(出资)' : '弃权' }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- Self Vote Area -->
        <view class="vote-area" v-if="task.voteTask.canVote && !isExpired">
          <view class="section-title">本人投票</view>
          <view class="section-title" v-if="!effectiveItemType">
            {{ effectiveItemType === 2 ? '2.出资票' : effectiveItemType === 1 ? '1.人数票' : '投票' }}
          </view>
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
          <view class="section-title" v-if="delegate.hasVoted !== 1 && !effectiveItemType">
            {{ effectiveItemType === 2 ? '2.出资票' : effectiveItemType === 1 ? '1.人数票' : '投票' }}
          </view>
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
          <view class="result-status-box">
            <text class="result-status">{{ voteStatusText }}</text>
          </view>
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
            
            <!-- Area 1: Headcount (Strategy 1 or 3) -->
            <view class="strategy-group" v-if="currentVoteStrategy === 1 || currentVoteStrategy === 3">
              <text class="section-subtitle" v-if="currentVoteStrategy === 3">1. 人数票统计</text>
              <view class="progress-stats">
                <view class="progress-stat-item">
                  <text class="stat-label">总人数：</text>
                  <text class="stat-value">{{ voteStatistics.progress.stat.partnerCount }}</text>
                </view>
                <view class="progress-stat-item">
                  <text class="stat-label">已投：</text>
                  <text class="stat-value">{{ voteStatistics.progress.stat.votedCount }}</text>
                </view>
                <view class="progress-stat-item">
                  <text class="stat-label">未投：</text>
                  <text class="stat-value">{{ voteStatistics.progress.stat.unvotedCount }}</text>
                </view>
                <view class="progress-stat-item">
                  <text class="stat-label">进度：</text>
                  <text class="stat-value">{{ voteStatistics.progress.stat.progress }}%</text>
                </view>
              </view>
              
              <view class="visual-vote-distribution">
                <text class="distribution-title" v-if="currentVoteStrategy !== 3">投票分布</text>
                <view class="vote-bar-item">
                  <view class="bar-row">
                    <text class="bar-option">同意</text>
                    <text class="bar-count">{{ voteStatistics.progress.stat.agreeCount }}人</text>
                  </view>
                  <view class="bar-container">
                    <view class="bar-fill agree-fill" :style="{ width: calculateBarWidth(voteStatistics.progress.stat.agreeCount, voteStatistics.progress.stat.votedCount) + '%' }"></view>
                  </view>
                  <text class="bar-ratio">({{ voteStatistics.progress.stat.agreeRatio }}%)</text>
                </view>
                <view class="vote-bar-item">
                  <view class="bar-row">
                    <text class="bar-option">反对</text>
                    <text class="bar-count">{{ voteStatistics.progress.stat.rejectCount }}人</text>
                  </view>
                  <view class="bar-container">
                    <view class="bar-fill reject-fill" :style="{ width: calculateBarWidth(voteStatistics.progress.stat.rejectCount, voteStatistics.progress.stat.votedCount) + '%' }"></view>
                  </view>
                  <text class="bar-ratio">({{ voteStatistics.progress.stat.rejectRatio }}%)</text>
                </view>
                <view class="vote-bar-item">
                  <view class="bar-row">
                    <text class="bar-option">弃权</text>
                    <text class="bar-count">{{ voteStatistics.progress.stat.abstainCount }}人</text>
                  </view>
                  <view class="bar-container">
                    <view class="bar-fill abstain-fill" :style="{ width: calculateBarWidth(voteStatistics.progress.stat.abstainCount, voteStatistics.progress.stat.votedCount) + '%' }"></view>
                  </view>
                  <text class="bar-ratio">({{ voteStatistics.progress.stat.abstainRatio }}%)</text>
                </view>
              </view>
            </view>

            <!-- Area 2: Capital (Strategy 2 or 3) -->
            <view class="strategy-group" v-if="currentVoteStrategy === 2 || currentVoteStrategy === 3">
              <view class="divider" v-if="currentVoteStrategy === 3" style="height: 1px; background: #eee; margin: 30rpx 0;"></view>
              <text class="section-subtitle" v-if="currentVoteStrategy === 3">2. 出资票统计</text>
              <view class="progress-stats">
                <view class="progress-stat-item">
                  <text class="stat-label">已投人数：</text>
                  <text class="stat-value">{{ voteStatistics.progress.stat.capitalVotedCount }}</text>
                </view>
                <view class="progress-stat-item">
                  <text class="stat-label">进度：</text>
                  <text class="stat-value">{{ voteStatistics.progress.stat.capitalProgress }}%</text>
                </view>
              </view>
              
              <view class="visual-vote-distribution">
                <text class="distribution-title" v-if="currentVoteStrategy !== 3">投票分布 (出资)</text>
                <view class="vote-bar-item">
                  <view class="bar-row">
                    <text class="bar-option">同意</text>
                    <text class="bar-count">{{ voteStatistics.progress.stat.capitalAgreeCount }}</text>
                  </view>
                  <view class="bar-container">
                    <view class="bar-fill agree-fill" :style="{ width: calculateBarWidth(voteStatistics.progress.stat.capitalAgreeCount, voteStatistics.progress.stat.capitalVotedCount) + '%' }"></view>
                  </view>
                  <text class="bar-ratio">({{ voteStatistics.progress.stat.capitalAgreeRatio }}%)</text>
                </view>
                <view class="vote-bar-item">
                  <view class="bar-row">
                    <text class="bar-option">反对</text>
                    <text class="bar-count">{{ voteStatistics.progress.stat.capitalRejectCount }}</text>
                  </view>
                  <view class="bar-container">
                    <view class="bar-fill reject-fill" :style="{ width: calculateBarWidth(voteStatistics.progress.stat.capitalRejectCount, voteStatistics.progress.stat.capitalVotedCount) + '%' }"></view>
                  </view>
                  <text class="bar-ratio">({{ voteStatistics.progress.stat.capitalRejectRatio }}%)</text>
                </view>
                <view class="vote-bar-item">
                  <view class="bar-row">
                    <text class="bar-option">弃权</text>
                    <text class="bar-count">{{ voteStatistics.progress.stat.capitalAbstainCount }}</text>
                  </view>
                  <view class="bar-container">
                    <view class="bar-fill abstain-fill" :style="{ width: calculateBarWidth(voteStatistics.progress.stat.capitalAbstainCount, voteStatistics.progress.stat.capitalVotedCount) + '%' }"></view>
                  </view>
                  <text class="bar-ratio">({{ voteStatistics.progress.stat.capitalAbstainRatio }}%)</text>
                </view>
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
const voteStatusText = ref('')
const voteStatistics = ref({})
const initialStrategy = ref(null)
const initialItemType = ref(null)
const selfPeopleOption = ref(null)
const selfCapitalOption = ref(null)
const delegateSelections = ref({})

const isAdminUser = (info) => {
  return !!info && (info.role === 'admin' || info.username === 'admin' || info.isAdmin === true)
}

// Use global share with dynamic title
useShare({
  title: () => task.value?.proposal?.title ? `投票：${task.value.proposal.title}` : '任务详情'
})

const statistics = computed(() => {
  if (!task.value || !task.value.voteTask) return null
  
  const vt = task.value.voteTask
  
  let agree = vt.agreeCount || 0
  let reject = vt.rejectCount || 0
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

const isComboStrategy = computed(() => {
  const strategy = task.value && task.value.voteTask ? task.value.voteTask.voteStrategy : null
  
  // Explicit strategy check: only 3 is combo
  if (Number(strategy) === 3) return true

  // Inference from voter list (safer check)
  // Only infer if we find explicit capital votes (1=Agree, 2=Reject, 3=Abstain)
  // This avoids false positives where voteOptionCapital might be present but 0/null
  if (task.value && task.value.voteTask && task.value.voteTask.voterList) {
    const hasCapitalVotes = task.value.voteTask.voterList.some(v => {
        const val = Number(v.voteOptionCapital)
        return val === 1 || val === 2 || val === 3
    })
    if (hasCapitalVotes) {
        console.log('Inferred isComboStrategy from voterList (found valid capital votes)')
        return true
    }
  }
  
  return false
})

const effectiveItemType = computed(() => {
  if (initialItemType.value !== null && initialItemType.value !== undefined) {
    return Number(initialItemType.value)
  }
  const vt = task.value && task.value.voteTask ? task.value.voteTask : null
  if (vt && vt.itemType !== undefined && vt.itemType !== null) return Number(vt.itemType)
  if (vt && vt.voteType !== undefined && vt.voteType !== null) return Number(vt.voteType)
  return null
})

const currentVoteStrategy = computed(() => {
  if (voteStatistics.value && voteStatistics.value.result && voteStatistics.value.result.voteStrategy) {
    return Number(voteStatistics.value.result.voteStrategy)
  }
  if (task.value && task.value.voteTask) {
    return Number(task.value.voteTask.voteStrategy)
  }
  return Number(initialStrategy.value)
})

const canSubmitSelfCombo = computed(() => {
  return isComboStrategy.value && selfPeopleOption.value && selfCapitalOption.value
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

// Calculate bar width percentage
const calculateBarWidth = (count, total) => {
  if (total === 0) return 0;
  return Math.round((count / total) * 100);
}

onLoad((options) => {
  if (options.id) {
    taskId.value = options.id
    if (options.voteStrategy !== undefined && options.voteStrategy !== null) {
      const nextStrategy = Number(options.voteStrategy)
      if (!Number.isNaN(nextStrategy)) {
        initialStrategy.value = nextStrategy
      }
    }
    if (options.itemType !== undefined && options.itemType !== null) {
      const nextItemType = Number(options.itemType)
      if (!Number.isNaN(nextItemType)) {
        initialItemType.value = nextItemType
      }
    }
    const userInfo = uni.getStorageSync('userInfo')
    isAdmin.value = isAdminUser(userInfo)
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
      const BASE_URL = 'http://119.29.249.72:8080'
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
      
      console.log('Payload:', payload)
      console.log('isFlatStructure:', isFlatStructure)

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
                  itemType: payload.itemType || payload.voteType,
                  voteStrategy: (payload.voteStrategy !== undefined ? payload.voteStrategy : (payload.vote_strategy !== undefined ? payload.vote_strategy : payload.strategy)) || initialStrategy.value,
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
              itemType: (voteTaskData && (voteTaskData.itemType || voteTaskData.voteType)) || payload.itemType || payload.voteType,
              voteStrategy: (voteTaskData && (voteTaskData.voteStrategy !== undefined ? voteTaskData.voteStrategy : (voteTaskData.vote_strategy !== undefined ? voteTaskData.vote_strategy : voteTaskData.strategy))) || payload.voteStrategy || payload.vote_strategy || payload.strategy || initialStrategy.value,
              votedCount: voteTaskData ? (voteTaskData.votedCount || 0) : 0,
              totalCount: voteTaskData ? (voteTaskData.totalCount || 0) : 0,
              voterList: voteTaskData ? (voteTaskData.voterList || []) : [],
              delegateeName: voteTaskData ? voteTaskData.delegateeName : payload.delegateeName,
              delegatorName: voteTaskData ? voteTaskData.delegatorName : payload.delegatorName,
              hasPendingDelegatedVote: (voteTaskData && voteTaskData.hasPendingDelegatedVote) || payload.hasPendingDelegatedVote || false
            }
          }
      }
      console.log('Mapped Task:', task.value)

      
      // If admin, fetch additional stats details (still needed for detailed voter list if not present in main detail)
      if (isAdmin.value) {
        fetchAdminStats(id)
      }
      fetchVoteStatus(id)
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
        hasVoted: false,
        voteStrategy: 1
      }
    }
  })
}

const fetchVoteStatus = (id) => {
  request({
    url: `/user/vote/task/stat/${id}`,
    method: 'GET'
  }).then(res => {
    if (res.code === 0 || res.code === 1 || res.code === 200) {
      const data = res.data || {}
      voteStatistics.value = data
      
      // Update task status if changed
      if (task.value && task.value.voteTask) {
          if (data.taskStatus !== undefined) task.value.voteTask.status = data.taskStatus
          // Try to recover voteStrategy if missing from detail
          if (data.voteStrategy !== undefined && data.voteStrategy !== null) {
              const nextStrategy = Number(data.voteStrategy)
              const currentStrategy = Number(task.value.voteTask.voteStrategy)
              if (!Number.isNaN(nextStrategy)) {
                  if (nextStrategy === 3) {
                      task.value.voteTask.voteStrategy = nextStrategy
                  } else if (!currentStrategy) {
                      task.value.voteTask.voteStrategy = nextStrategy
                  }
              }
          }
      }

      if (data.taskStatus === 3 || data.result) {
        const finalResult = data.result && data.result.finalResult
        voteStatusText.value = finalResult ? '投票通过' : '投票不通过'
      } else {
        voteStatusText.value = '投票中'
      }
    }
  }).catch(() => {
    voteStatusText.value = isExpired.value ? '投票已截止' : '投票中'
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
           // Capital Stats
           task.value.voteTask.capitalAgreeCount = stats.capitalAgreeCount || 0
           task.value.voteTask.capitalRejectCount = stats.capitalRejectCount || 0
           task.value.voteTask.capitalAbstainCount = stats.capitalAbstainCount || 0
           task.value.voteTask.capitalVotedCount = stats.capitalVotedCount || 0
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

const getComboBtnClass = (scope, type, val, baseClass, partnerId) => {
  let selectedVal = null
  if (scope === 'self') {
    if (type === 'people') selectedVal = selfPeopleOption.value
    else if (type === 'capital') selectedVal = selfCapitalOption.value
  } else if (scope === 'delegate' && partnerId) {
    const sel = delegateSelections.value[partnerId]
    if (sel) {
        selectedVal = sel[type]
    }
  }
  
  // If selected matches current button value, use solid style
  if (selectedVal === val) {
    return `vote-btn ${baseClass}`
  }
  // If nothing is selected in this group, use outline (neutral) style? 
  // Or just use outline for everything unselected.
  return `vote-btn ${baseClass}-outline`
}

const previewImage = (url) => {
  if (!url) return
  uni.previewImage({
    urls: [url]
  })
}

const selectSelfOption = (type, option) => {
  if (type === 'people') {
    selfPeopleOption.value = option
  } else if (type === 'capital') {
    selfCapitalOption.value = option
  }
}

const selectDelegateOption = (fromPartnerId, type, option) => {
  if (!fromPartnerId) return
  const current = delegateSelections.value[fromPartnerId] || {}
  delegateSelections.value = {
    ...delegateSelections.value,
    [fromPartnerId]: {
      ...current,
      [type]: option
    }
  }
}

const canSubmitDelegateCombo = (fromPartnerId) => {
  if (!isComboStrategy.value) return false
  const sel = delegateSelections.value[fromPartnerId]
  return !!(sel && sel.people && sel.capital)
}

const resetSelections = (type, delegateData) => {
  if (type === 'self') {
    selfPeopleOption.value = null
    selfCapitalOption.value = null
    return
  }
  if (delegateData && delegateData.fromPartnerId) {
    const next = { ...delegateSelections.value }
    delete next[delegateData.fromPartnerId]
    delegateSelections.value = next
  }
}

const handleVote = (option, type, delegateData) => {
  // Determine if it's a delegate vote based on data presence
  const isDelegate = !!(delegateData && delegateData.fromPartnerId)
  
  const optionText = getVoteText(option)
  let title = '确认投票'
  let content = `确认投"${optionText}"吗？提交后不可修改。`
  
  if (isDelegate) {
      const delegateName = delegateData.fromPartnerName || delegateData.delegatorName || delegateData.delegatorRealName || '他人'
      title = '确认代投'
      content = `确认代 ${delegateName} 投"${optionText}"吗？`
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

const handleComboSubmit = (type, delegateData) => {
  if (!isComboStrategy.value) return
  let peopleOption = null
  let capitalOption = null

  if (type === 'delegate') {
    if (!delegateData || !delegateData.fromPartnerId) {
      uni.showToast({ title: '缺少委托人信息', icon: 'none' })
      return
    }
    const sel = delegateSelections.value[delegateData.fromPartnerId] || {}
    peopleOption = sel.people
    capitalOption = sel.capital
  } else {
    peopleOption = selfPeopleOption.value
    capitalOption = selfCapitalOption.value
  }

  if (!peopleOption || !capitalOption) {
    uni.showToast({ title: '请先选择人数票和出资票', icon: 'none' })
    return
  }

  const peopleText = getVoteText(peopleOption)
  const capitalText = getVoteText(capitalOption)
  const isDelegate = type === 'delegate'
  const delegateName = isDelegate && delegateData ? (delegateData.fromPartnerName || delegateData.delegatorName || delegateData.delegatorRealName || '他人') : ''

  const title = isDelegate ? '确认代投' : '确认投票'
  const content = isDelegate
    ? `确认代 ${delegateName} 投票吗？\n人数票：${peopleText}\n出资票：${capitalText}`
    : `确认投票吗？\n人数票：${peopleText}\n出资票：${capitalText}`

  uni.showModal({
    title,
    content,
    success: (res) => {
      if (res.confirm) {
        submitVote(peopleOption, isDelegate ? 'delegate' : 'self', delegateData, capitalOption)
      }
    }
  })
}

const submitVote = (option, type, delegateData, optionCapital) => {
  uni.showLoading({ title: '提交中' })
  
  let url = '/user/h5/vote/submit'
  const postData = {
    voteTaskId: Number(taskId.value)
  }
  const isDelegate = type === 'delegate' || (delegateData && delegateData.fromPartnerId)
  const itemType = effectiveItemType.value
  if (itemType === 1) {
    postData.voteOption = Number(option)
  } else if (itemType === 2) {
    const capitalValue = optionCapital !== undefined && optionCapital !== null ? optionCapital : option
    postData.voteOptionCapital = Number(capitalValue)
  } else {
    if (!isDelegate && isComboStrategy.value && (optionCapital === undefined || optionCapital === null)) {
      uni.hideLoading()
      uni.showToast({ title: '组合投票需同时提交出资票', icon: 'none' })
      return
    }
    postData.voteOption = Number(option)
    if (optionCapital !== undefined && optionCapital !== null) {
      postData.voteOptionCapital = Number(optionCapital)
    }
  }
  
  // Double check if it's a delegate vote based on data presence
  
  if (isDelegate) {
      url = '/user/h5/vote/delegate/submit'
      if (delegateData && delegateData.fromPartnerId) {
          postData.fromPartnerId = Number(delegateData.fromPartnerId)
          postData.isDelegate = true
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
      resetSelections(isDelegate ? 'delegate' : 'self', delegateData)
      setTimeout(() => {
          fetchDetail(taskId.value)
      }, 1000)
    } else {
      if (res.msg && res.msg.includes('组合投票必须包含出资投票选项')) {
         if (task.value && task.value.voteTask) {
             console.log('Auto-correcting voteStrategy to 3 based on server error')
             task.value.voteTask.voteStrategy = 3
             uni.showToast({ title: '已切换至组合投票模式，请重新选择', icon: 'none', duration: 2000 })
         } else {
             uni.showToast({ title: res.msg || '投票失败', icon: 'none' })
         }
      } else {
         uni.showToast({ title: res.msg || '投票失败', icon: 'none' })
      }
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
  margin: 20rpx 0 24rpx;
}

.stat-row {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.main-stat {
  padding-bottom: 24rpx;
  border-bottom: 1px solid #E5E7EB;
  margin-bottom: 24rpx;
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

.header {
  margin-bottom: 30rpx;
  border-bottom: 1px solid #F3F4F6;
  padding-bottom: 20rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #111827;
  margin-bottom: 24rpx;
  display: block;
}

.delegate-hint {
  font-size: 28rpx;
  margin-bottom: 16rpx;
  display: block;
}

.meta {
  font-size: 24rpx;
  color: #9CA3AF;
  margin-top: 10rpx;
}

.content {
  margin-bottom: 40rpx;
}

.desc {
  font-size: 30rpx;
  color: #4B5563;
  line-height: 1.8;
  display: block;
  margin-bottom: 20rpx;
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
  margin: 30rpx 0 24rpx;
  color: #333;
}

.section-subtitle {
  font-size: 26rpx;
  font-weight: bold;
  margin: 20rpx 0 16rpx;
  color: #4B5563;
}

.result-text {
  font-size: 28rpx;
  color: #6B7280;
  margin: 0 0 24rpx;
  line-height: 1.7;
}

.result-status-box {
  border: 1px solid #E5E7EB;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  background-color: #F9FAFB;
  margin-top: 24rpx;
}

.result-status {
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
  display: block;
  text-align: center;
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

.agree-outline {
  background-color: #ECFDF5;
  color: #10B981;
  border: 1px solid #10B981;
}

.reject-outline {
  background-color: #FEF2F2;
  color: #EF4444;
  border: 1px solid #EF4444;
}

.abstain-outline {
  background-color: #F3F4F6;
  color: #6B7280;
  border: 1px solid #9CA3AF;
}

.result-area {
  margin-top: 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.result-text {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 20rpx;
  text-align: center;
  padding: 10rpx 0;
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

/* Visual Vote Distribution Styles */
.visual-vote-distribution {
  background-color: #f8f9fa;
  border-radius: 6rpx;
  padding: 10rpx;
  margin-top: 10rpx;
}

.distribution-title {
  font-size: 24rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.vote-bar-item {
  margin-bottom: 12rpx;
}

.vote-bar-item:last-child {
  margin-bottom: 0;
}

.bar-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4rpx;
  font-size: 22rpx;
}

.bar-option {
  font-weight: 500;
  color: #333;
  flex: 1;
}

.bar-count {
  font-weight: 500;
  color: #333;
  text-align: right;
  min-width: 60rpx;
  margin-left: 10rpx;
}

.bar-container {
  width: 100%;
  height: 24rpx;
  background-color: #e9ecef;
  border-radius: 12rpx;
  overflow: hidden;
  margin-bottom: 4rpx;
}

.bar-fill {
  height: 100%;
  border-radius: 12rpx;
  transition: width 0.3s ease;
}

.agree-fill {
  background-color: #10B981;
}

.reject-fill {
  background-color: #EF4444;
}

.abstain-fill {
  background-color: #9CA3AF;
}

.bar-ratio {
  font-size: 20rpx;
  color: #666;
  text-align: right;
  display: block;
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