<template>
  <div class="min-h-screen bg-[#050505] text-gray-300 font-sans selection:bg-blue-500 selection:text-white overflow-x-hidden relative">
    <!-- Loading Screen -->
    <transition name="fade-out">
      <div v-if="isLoading" class="fixed inset-0 z-[100] bg-[#050505] flex flex-col items-center justify-center">
        <div class="relative w-24 h-24 mb-8">
          <div class="absolute inset-0 border-t-4 border-blue-500 rounded-full animate-spin"></div>
          <div class="absolute inset-2 border-r-4 border-cyan-400 rounded-full animate-spin-reverse"></div>
          <div class="absolute inset-0 flex items-center justify-center">
            <img src="/logo.png" alt="Logo" class="w-10 h-10 animate-pulse" />
          </div>
        </div>
        <div class="text-2xl font-bold font-mono text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-cyan-300 animate-pulse">
          CoffeeViz
        </div>
        <div class="mt-4 w-48 h-1 bg-gray-800 rounded-full overflow-hidden">
          <div class="h-full bg-blue-500 animate-progress"></div>
        </div>
      </div>
    </transition>

    <!-- Navigation -->
    <nav class="fixed w-full z-50 transition-all duration-500" 
         :class="[
           isScrolled ? 'bg-[#050505]/80 backdrop-blur-md border-b border-white/5 py-2' : 'bg-transparent py-4',
           isLoading ? 'opacity-0 -translate-y-full' : 'opacity-100 translate-y-0'
         ]">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-20">
          <!-- Logo -->
          <div class="flex-shrink-0 flex items-center gap-3 cursor-pointer" @click="scrollTo('hero')">
            <img src="/logo.png" alt="CoffeeViz" class="h-10 w-10 animate-pulse-slow" />
            <span class="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-cyan-300 tracking-tight font-mono">
              CoffeeViz
            </span>
          </div>
          
          <!-- Desktop Menu -->
          <div class="hidden md:block">
            <div class="ml-10 flex items-baseline space-x-8">
              <a @click.prevent="scrollTo('features')" href="#features" class="hover:text-blue-400 transition-colors px-3 py-2 rounded-md text-sm font-medium cursor-pointer">功能特性</a>
              <a @click.prevent="scrollTo('preview')" href="#preview" class="hover:text-blue-400 transition-colors px-3 py-2 rounded-md text-sm font-medium cursor-pointer">演示预览</a>
              <a @click.prevent="scrollTo('pricing')" href="#pricing" class="hover:text-blue-400 transition-colors px-3 py-2 rounded-md text-sm font-medium cursor-pointer">订阅计划</a>
              
              <button @click="handleStart" class="ml-4 px-6 py-2.5 rounded-full bg-gradient-to-r from-blue-600 to-cyan-500 text-white font-semibold text-sm hover:shadow-[0_0_20px_rgba(6,182,212,0.5)] transition-all duration-300 transform hover:-translate-y-0.5 border border-transparent">
                <i class="fas" :class="userStore.isLoggedIn ? 'fa-tachometer-alt' : 'fa-rocket'"></i>
                <span class="ml-2">{{ userStore.isLoggedIn ? '进入控制台' : '立即开始' }}</span>
              </button>
            </div>
          </div>

          <!-- Mobile menu button -->
          <div class="md:hidden">
            <button @click="mobileMenuOpen = !mobileMenuOpen" class="text-gray-300 hover:text-white p-2">
              <i class="fas" :class="mobileMenuOpen ? 'fa-times' : 'fa-bars'"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- Mobile Menu -->
      <div v-show="mobileMenuOpen" class="md:hidden bg-[#0a0a0a] border-b border-white/10 absolute w-full">
        <div class="px-2 pt-2 pb-3 space-y-1 sm:px-3">
          <a @click.prevent="scrollTo('features'); mobileMenuOpen = false" href="#features" class="text-gray-300 hover:text-white block px-3 py-2 rounded-md text-base font-medium">功能特性</a>
          <a @click.prevent="scrollTo('preview'); mobileMenuOpen = false" href="#preview" class="text-gray-300 hover:text-white block px-3 py-2 rounded-md text-base font-medium">演示预览</a>
          <a @click.prevent="scrollTo('pricing'); mobileMenuOpen = false" href="#pricing" class="text-gray-300 hover:text-white block px-3 py-2 rounded-md text-base font-medium">订阅计划</a>
          <button @click="handleStart" class="w-full text-left mt-4 px-5 py-3 bg-blue-600/20 text-blue-400 font-bold border-l-4 border-blue-500">
            {{ userStore.isLoggedIn ? '进入控制台' : '登录 / 注册' }}
          </button>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <header id="hero" class="relative pt-32 pb-20 lg:pt-48 lg:pb-32 overflow-hidden min-h-screen flex items-center justify-center">
      <!-- Background Effects -->
      <div class="absolute inset-0 z-0 pointer-events-none overflow-hidden">
        <!-- Grid Background (CSS generated) -->
        <div class="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:24px_24px] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)]"></div>
        
        <!-- Moving Blobs -->
        <div class="absolute top-1/4 left-1/4 w-[600px] h-[600px] bg-blue-600/20 rounded-full blur-[120px] mix-blend-screen animate-blob"></div>
        <div class="absolute bottom-1/4 right-1/4 w-[500px] h-[500px] bg-cyan-500/10 rounded-full blur-[100px] mix-blend-screen animate-blob animation-delay-2000"></div>
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-indigo-500/10 rounded-full blur-[130px] mix-blend-screen animate-pulse-slow"></div>
        
        <!-- Floating Particles (CSS Only representation) -->
        <div class="absolute inset-0 opacity-30">
          <div v-for="n in 20" :key="n" 
               class="absolute rounded-full bg-blue-400 animate-float"
               :style="{
                 left: `${Math.random() * 100}%`,
                 top: `${Math.random() * 100}%`,
                 width: `${Math.random() * 4 + 1}px`,
                 height: `${Math.random() * 4 + 1}px`,
                 animationDelay: `${Math.random() * 5}s`,
                 animationDuration: `${Math.random() * 10 + 10}s`
               }">
          </div>
        </div>
      </div>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10 text-center transition-all duration-1000 transform"
           :class="isLoading ? 'opacity-0 translate-y-10' : 'opacity-100 translate-y-0'">
        <div class="inline-flex items-center px-4 py-2 rounded-full border border-blue-500/30 bg-blue-500/10 text-blue-300 text-sm font-medium mb-8 hover:scale-105 transition-transform cursor-default">
          <span class="flex h-2 w-2 rounded-full bg-blue-400 mr-2 animate-ping"></span>
          <span class="flex h-2 w-2 rounded-full bg-blue-400 mr-2 absolute"></span>
          v2.0 Kimi-2.5 深度驱动的架构生成
        </div>
        
        <h1 class="text-6xl md:text-8xl font-extrabold tracking-tight mb-8 leading-tight">
          <span class="block text-white mb-2 drop-shadow-lg">智能系统架构</span>
          <span class="bg-clip-text text-transparent bg-gradient-to-r from-blue-400 via-cyan-400 to-teal-300 filter drop-shadow-[0_0_25px_rgba(34,211,238,0.4)] animate-gradient-x">
            可视化与设计生成
          </span>
        </h1>
        
        <p class="mt-6 max-w-3xl mx-auto text-xl md:text-2xl text-gray-400 mb-12 leading-relaxed">
          将 SQL、文档或 <span class="text-white font-semibold border-b-2 border-blue-500/50">自然语言描述</span> 转化为精美的 ER 图与系统功能结构图。
        </p>
        
        <div class="flex flex-col sm:flex-row justify-center gap-6">
          <button @click="handleStart" class="group relative px-8 py-4 bg-white text-black font-bold rounded-xl transition-all transform hover:-translate-y-1 hover:shadow-[0_0_30px_rgba(255,255,255,0.3)] overflow-hidden">
            <div class="absolute inset-0 bg-gradient-to-r from-blue-400/20 to-cyan-400/20 translate-y-full group-hover:translate-y-0 transition-transform duration-300"></div>
            <span class="relative flex items-center text-lg">
              立即体验 <i class="fas fa-arrow-right ml-2 group-hover:translate-x-1 transition-transform"></i>
            </span>
          </button>
          
          <button @click="scrollTo('features')" class="px-8 py-4 bg-white/5 border border-white/10 text-white font-bold rounded-xl hover:bg-white/10 transition-all backdrop-blur-sm hover:border-white/30 text-lg">
            了解更多
          </button>
        </div>
        
        <!-- Stats with animated counter effect (simulated) -->
        <div class="mt-20 grid grid-cols-2 gap-8 md:grid-cols-4 border-t border-white/5 pt-10 bg-black/20 backdrop-blur-sm rounded-3xl p-6 mx-4 md:mx-0">
          <div v-for="(stat, index) in stats" :key="index" class="flex flex-col items-center group hover:transform hover:scale-105 transition-transform duration-300">
            <dt class="order-2 mt-2 text-sm font-medium text-gray-500 uppercase tracking-wider group-hover:text-blue-400 transition-colors">{{ stat.label }}</dt>
            <dd class="order-1 text-3xl md:text-4xl font-extrabold text-white group-hover:text-transparent group-hover:bg-clip-text group-hover:bg-gradient-to-r group-hover:from-blue-400 group-hover:to-cyan-300 transition-all">{{ stat.value }}</dd>
          </div>
        </div>
      </div>
    </header>

    <!-- Features Section -->
    <section id="features" class="py-24 bg-[#080808] relative">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-blue-400 font-semibold tracking-wide uppercase text-sm mb-3">核心能力</h2>
          <p class="mt-2 text-3xl leading-8 font-extrabold tracking-tight text-white sm:text-4xl">
            不仅仅是绘图工具
          </p>
          <p class="mt-4 max-w-2xl text-xl text-gray-400 mx-auto">
            全方位的数据库架构解决方案，从设计到协作，无缝衔接。
          </p>
        </div>

        <div class="grid grid-cols-1 gap-8 md:grid-cols-3">
          <div v-for="(feature, index) in features" :key="index" 
               class="relative group p-8 bg-[#111] rounded-2xl border border-white/5 hover:border-blue-500/50 transition-all duration-300 hover:-translate-y-2 hover:shadow-[0_10px_40px_-10px_rgba(59,130,246,0.2)] overflow-hidden">
            <div class="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-transparent opacity-0 group-hover:opacity-100 rounded-2xl transition-opacity"></div>
            <!-- Animated corner border -->
            <div class="absolute top-0 right-0 w-20 h-20 bg-gradient-to-bl from-blue-500/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 rounded-tr-2xl"></div>
            
            <div class="relative z-10">
              <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-blue-600 to-cyan-600 flex items-center justify-center mb-6 shadow-lg transform group-hover:scale-110 group-hover:rotate-6 transition-all duration-300">
                <i :class="feature.icon" class="text-2xl text-white group-hover:animate-bounce-short"></i>
              </div>
              <h3 class="text-xl font-bold text-white mb-3 group-hover:text-blue-400 transition-colors">{{ feature.title }}</h3>
              <p class="text-gray-400 leading-relaxed group-hover:text-gray-300 transition-colors">
                {{ feature.description }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Preview Section -->
    <section id="preview" class="py-24 relative overflow-hidden">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div class="lg:grid lg:grid-cols-12 lg:gap-16 items-center">
          <div class="lg:col-span-5 mb-10 lg:mb-0">
            <h2 class="text-3xl font-extrabold text-white sm:text-4xl mb-6">
              <span class="block text-blue-400 mb-2">AI 驱动</span>
              自然语言生成架构
            </h2>
            <p class="text-lg text-gray-400 mb-8">
              不再需要手动编写繁琐的 DDL。只需告诉 AI 您的业务需求，CoffeeViz 将自动为您生成规范的数据库模型、字段定义和表关系。
            </p>
            <ul class="space-y-4 mb-8">
              <li v-for="item in ['智能推断表关系', '自动生成字段注释', '遵循命名规范', '支持 MySQL/PostgreSQL']" :key="item" class="flex items-center text-gray-300">
                <i class="fas fa-check-circle text-cyan-400 mr-3"></i>
                {{ item }}
              </li>
            </ul>
            <button @click="handleStart" class="text-blue-400 font-semibold hover:text-blue-300 flex items-center group">
              探索 AI 功能 <i class="fas fa-arrow-right ml-2 group-hover:translate-x-1 transition-transform"></i>
            </button>
          </div>
          
          <div class="lg:col-span-7">
            <!-- Code Window Mockup -->
            <div class="rounded-xl bg-[#1E1E1E] border border-white/10 shadow-2xl overflow-hidden transform rotate-1 hover:rotate-0 transition-transform duration-500 group">
              <div class="flex items-center px-4 py-3 bg-[#252526] border-b border-white/5">
                <div class="flex space-x-2">
                  <div class="w-3 h-3 rounded-full bg-red-500 group-hover:bg-red-400 transition-colors"></div>
                  <div class="w-3 h-3 rounded-full bg-yellow-500 group-hover:bg-yellow-400 transition-colors"></div>
                  <div class="w-3 h-3 rounded-full bg-green-500 group-hover:bg-green-400 transition-colors"></div>
                </div>
                <div class="ml-4 text-xs text-gray-500 font-mono">ai-generate.sql</div>
              </div>
              <div class="p-6 font-mono text-sm overflow-hidden h-[300px] relative">
                <div class="flex h-full">
                  <div class="text-gray-600 select-none pr-4 text-right border-r border-white/10 mr-4 font-mono text-xs leading-6">
                    <div v-for="n in 12" :key="n">{{ n }}</div>
                  </div>
                  <div class="text-gray-300 w-full font-mono text-xs leading-6 whitespace-pre-wrap">
                    <span v-html="highlightedCode"></span><span class="animate-cursor inline-block w-2 h-4 bg-blue-400 align-middle ml-1"></span>
                  </div>
                </div>
                
                <!-- Replay Button Overlay -->
                <div v-if="showReplay" class="absolute inset-0 bg-black/50 flex items-center justify-center backdrop-blur-sm opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                  <button @click="startTyping" class="px-6 py-2 bg-blue-600 rounded-full text-white font-bold hover:bg-blue-500 transition-colors transform hover:scale-105">
                    <i class="fas fa-play mr-2"></i> 重新演示
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Pricing Section -->
    <section id="pricing" class="py-24 bg-[#080808]">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h2 class="text-3xl font-extrabold text-white sm:text-4xl mb-4">灵活的订阅计划</h2>
        <p class="text-gray-400 mb-16 max-w-2xl mx-auto">选择最适合您团队的方案，随时升级或取消。</p>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div v-for="plan in plans" :key="plan.name" 
               class="relative p-8 bg-[#111] rounded-2xl border transition-all duration-300 flex flex-col"
               :class="plan.popular ? 'border-blue-500 shadow-[0_0_30px_-10px_rgba(59,130,246,0.3)] scale-105 z-10' : 'border-white/5 hover:border-white/20'">
            
            <div v-if="plan.popular" class="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-gradient-to-r from-blue-600 to-cyan-500 text-white text-xs font-bold px-3 py-1 rounded-full">
              最受欢迎
            </div>

            <h3 class="text-xl font-bold text-white mb-2">{{ plan.name }}</h3>
            <div class="text-4xl font-extrabold text-white mb-6">
              {{ plan.price }}<span class="text-lg font-normal text-gray-500">/月</span>
            </div>
            
            <ul class="space-y-4 mb-8 flex-1 text-left">
              <li v-for="feature in plan.features" :key="feature" class="flex items-start">
                <i class="fas fa-check text-blue-500 mt-1 mr-3 text-xs"></i>
                <span class="text-gray-300 text-sm">{{ feature }}</span>
              </li>
            </ul>
            
            <button @click="handleStart" 
                    class="w-full py-3 rounded-xl font-bold transition-colors"
                    :class="plan.popular ? 'bg-blue-600 hover:bg-blue-700 text-white' : 'bg-white/10 hover:bg-white/20 text-white'">
              {{ plan.cta }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="bg-black border-t border-white/10 py-12">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex flex-col md:flex-row justify-between items-center">
          <div class="flex items-center gap-2 mb-4 md:mb-0">
            <img src="/logo.png" alt="CoffeeViz" class="h-8 w-8 grayscale opacity-70" />
            <span class="text-gray-500 font-mono">CoffeeViz &copy; 2026</span>
          </div>
          
          <div class="flex space-x-6">
            <a href="https://github.com/eighteenth-last/CoffeeViz.git" class="text-gray-500 hover:text-white transition-colors"><i class="fab fa-github text-xl"></i></a>
            <a href="#" class="text-gray-500 hover:text-white transition-colors"><i class="fab fa-twitter text-xl"></i></a>
            <a href="#" class="text-gray-500 hover:text-white transition-colors"><i class="fas fa-envelope text-xl"></i></a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const isScrolled = ref(false)

const handleStart = () => {
  if (userStore.isLoggedIn) {
    router.push('/dashboard')
  } else {
    router.push('/login')
  }
}
const isLoading = ref(true)
const mobileMenuOpen = ref(false)

// Typewriter Logic
const fullCode = `
<span class="text-purple-400">-- User Prompt:</span>
<span class="text-green-400">-- "设计一个高并发的电商秒杀系统，包含库存预扣减、订单异步处理。"</span>

<span class="text-blue-400">CREATE TABLE</span> seckill_items (
  id <span class="text-yellow-400">BIGINT PRIMARY KEY</span>,
  item_id <span class="text-yellow-400">BIGINT</span> NOT NULL,
  stock_count <span class="text-yellow-400">INT</span> CHECK(stock_count >= 0),
  start_time <span class="text-yellow-400">DATETIME</span>,
  end_time <span class="text-yellow-400">DATETIME</span>,
  version <span class="text-yellow-400">INT</span> DEFAULT 0
);

<span class="text-blue-400">CREATE TABLE</span> seckill_orders (
  id <span class="text-yellow-400">BIGINT PRIMARY KEY</span>,
  user_id <span class="text-yellow-400">BIGINT</span>,
  order_id <span class="text-yellow-400">BIGINT</span>,
  status <span class="text-yellow-400">TINYINT</span> COMMENT '0:Creating 1:Paid'
);`

const highlightedCode = ref('')
const showReplay = ref(false)
let typingInterval = null

const startTyping = () => {
  highlightedCode.value = ''
  showReplay.value = false
  let currentIndex = 0
  
  // Strip HTML tags for typing logic to avoid breaking tags mid-way, 
  // but here we are typing pre-formatted HTML string.
  // A simpler approach for HTML typing is to type chunks or just raw text then colorize.
  // For this demo, let's type char by char but fast-forward through tags.
  
  clearInterval(typingInterval)
  typingInterval = setInterval(() => {
    if (currentIndex >= fullCode.length) {
      clearInterval(typingInterval)
      showReplay.value = true
      return
    }

    // Check if we are at a tag
    if (fullCode[currentIndex] === '<') {
      const closingIndex = fullCode.indexOf('>', currentIndex)
      if (closingIndex !== -1) {
        highlightedCode.value += fullCode.substring(currentIndex, closingIndex + 1)
        currentIndex = closingIndex + 1
      } else {
        highlightedCode.value += fullCode[currentIndex]
        currentIndex++
      }
    } else {
      highlightedCode.value += fullCode[currentIndex]
      currentIndex++
    }
  }, 30)
}

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

const scrollTo = (id) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  
  // Simulate Loading
  setTimeout(() => {
    isLoading.value = false
    // Start typing after loading and a small delay
    setTimeout(startTyping, 1000)
  }, 2000)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  clearInterval(typingInterval)
})

const stats = [
  { label: '生成的图表', value: '50K+' },
  { label: '注册用户', value: '10,000+' },
  { label: '支持数据库', value: '15+' },
  { label: '好评率', value: '99%' },
]

const features = [
  {
    title: 'SQL 智能解析',
    description: '支持 MySQL, PostgreSQL, Oracle, SQL Server 等主流数据库脚本解析。自动识别复杂约束与关系。',
    icon: 'fas fa-code'
  },
  {
    title: 'JDBC 直连可视化',
    description: '通过 JDBC 连接直接读取数据库元数据。无需导出脚本，一键生成现有数据库的完整 ER 图。',
    icon: 'fas fa-database'
  },
  {
    title: 'AI 架构对话',
    description: '集成 DeepSeek R1 与 OpenAI o1 模型，通过自然语言描述业务需求，AI 自动为您设计数据库模型并生成 SQL。',
    icon: 'fas fa-robot'
  },
  {
    title: '团队协作共享',
    description: '创建团队，邀请成员加入。共享项目架构库，支持多人查看和版本管理，提升团队沟通效率。',
    icon: 'fas fa-users'
  },
  {
    title: '多格式导出',
    description: '支持导出高清 SVG, PNG 图片，以及 Mermaid 源码。方便嵌入文档、Wiki 或演示文稿中。',
    icon: 'fas fa-image'
  },
  {
    title: '系统结构图生成',
    description: '基于 DDL 聚类或 AI 文档解析，自动生成层级化的系统功能结构图 (WBS)，清晰展示模块关系。',
    icon: 'fas fa-sitemap'
  }
]

const plans = [
  {
    name: 'Free',
    price: '¥0',
    features: ['3 个架构库', '每月 10 次架构图生成', '每月 50 次 SQL 解析', '基础 ER 图 & MySQL 支持', '社区支持'],
    cta: '免费开始',
    popular: false
  },
  {
    name: 'Pro',
    price: '¥29',
    features: ['20 个架构库', '每月 100 次架构图生成', '每月 40 次 AI 设计', 'JDBC 实时连接 & 多数据库', '高清导出 (SVG/PNG) & 优先支持'],
    cta: '立即升级',
    popular: true
  },
  {
    name: 'Team',
    price: '¥99',
    features: ['无限架构库 & 无限架构图', '每月 150 次 AI 设计', '10 人团队协作 & 版本控制', 'API 集成 & 私有部署支持', '专属客户经理'],
    cta: '联系销售',
    popular: false
  }
]
</script>

<style scoped>
.animate-blob {
  animation: blob 7s infinite;
}
.animation-delay-2000 {
  animation-delay: 2s;
}
.animation-delay-4000 {
  animation-delay: 4s;
}
@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}

.animate-pulse-slow {
  animation: pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fade-in-up 0.8s ease-out forwards;
}

.animate-cursor {
  animation: blink 1s step-end infinite;
}

.animate-float {
  animation: float 20s infinite linear;
}

.animate-spin-reverse {
  animation: spin 1.5s linear infinite reverse;
}

.animate-progress {
  animation: progress 2s ease-in-out infinite;
}

.animate-gradient-x {
  background-size: 200% 200%;
  animation: gradient-x 5s ease infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

@keyframes float {
  0% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(30px, -50px) rotate(10deg); }
  66% { transform: translate(-20px, 20px) rotate(-5deg); }
  100% { transform: translate(0, 0) rotate(0deg); }
}

@keyframes progress {
  0% { width: 0%; transform: translateX(-100%); }
  50% { width: 100%; transform: translateX(0); }
  100% { width: 100%; transform: translateX(100%); }
}

@keyframes gradient-x {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes bounce-short {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20%); }
}

.animate-bounce-short {
  animation: bounce-short 1s ease-in-out infinite;
}

.animation-delay-100 { animation-delay: 0.1s; }
.animation-delay-200 { animation-delay: 0.2s; }
.animation-delay-300 { animation-delay: 0.3s; }
.animation-delay-400 { animation-delay: 0.4s; }
</style>
