<template>
  <div class="page-ai-generate flex flex-col h-[calc(100vh-144px)] relative overflow-hidden">
    
    <!-- Main Content Area (Results) -->
    <!-- Only visible when hasStarted is true -->
    <main class="flex-1 flex flex-col overflow-hidden relative z-0 pt-4">
        
        <!-- Result Split View Container -->
        <div v-if="hasStarted" class="flex-1 flex w-full h-full max-w-[1600px] mx-auto animate-fade-in relative transition-all duration-500 ease-in-out">
            
            <!-- Left: Text Output (Explanation & SQL) -->
            <div class="flex flex-col h-full bg-transparent overflow-hidden transition-all duration-500 ease-in-out"
                 :class="[showCanvas ? 'w-[30%] min-w-[400px]' : 'w-full max-w-[800px] mx-auto']">
                
                <div class="flex-1 overflow-y-auto p-5 space-y-6 custom-scrollbar" ref="outputContainer">
                    
                    <!-- User Prompt -->
                    <div v-if="currentPrompt" class="flex justify-end animate-fade-in">
                        <div class="bg-[#28292A] text-[#E3E3E3] px-5 py-3 rounded-2xl rounded-tr-sm max-w-[80%] leading-relaxed whitespace-pre-wrap border border-[#444746]">{{ currentPrompt }}</div>
                    </div>

                    <!-- Thinking Phase -->
                    <div v-if="isThinking" class="flex flex-col gap-3 p-0">
                        <div class="flex items-center gap-2">
                            <div class="w-6 h-6 rounded-full bg-gradient-to-tr from-blue-500 to-red-500 animate-spin"></div>
                            <span class="gemini-gradient-text font-medium text-sm">Thinking...</span>
                        </div>
                    </div>

                    <!-- Content Streaming Phase -->
                    <div v-if="!isThinking" class="space-y-8 pb-32">
                        
                        <!-- Explanation -->
                        <div class="animate-slide-up">
                            <div class="flex items-start gap-4">
                                <div class="w-8 h-8 rounded-full bg-white flex-shrink-0 flex items-center justify-center mt-1">
                                    <i class="fas fa-star text-transparent bg-clip-text bg-gradient-to-tr from-blue-600 to-red-600 text-xs"></i>
                                </div>
                                <div class="flex-1 text-[#E3E3E3] leading-7 text-[15px] markdown-body">
                                    <span v-html="renderedExplanation"></span><span v-if="streaming.explanation" class="cursor-blink"></span>
                                </div>
                            </div>
                        </div>

                        <!-- SQL Code -->
                        <div v-if="streamedSql" class="animate-slide-up pl-12">
                            <div class="gemini-card overflow-hidden mb-4">
                                <div class="flex items-center justify-between px-4 py-2 bg-[#28292A] border-b border-[#444746]">
                                    <span class="text-xs text-neutral-400 font-medium">SQL</span>
                                    <button @click="copySql" class="text-xs px-2 py-1 rounded hover:bg-[#3C4043] text-neutral-400 hover:text-white transition"><i class="fas fa-copy mr-1"></i> Copy</button>
                                </div>
                                <pre class="language-sql m-0 !bg-[#1E1F20] !p-4 text-xs font-mono"><code ref="sqlCodeBlock" class="language-sql">{{ streamedSql }}</code></pre>
                            </div>
                            
                            <!-- Open Canvas Chip (Visible if canvas is closed but data is ready) -->
                            <button v-if="!showCanvas && mermaidCode" @click="showCanvas = true" class="flex items-center gap-2 px-4 py-2 rounded-full bg-[#28292A] hover:bg-[#3C4043] border border-[#444746] transition-colors text-sm text-neutral-200">
                                <i class="fas fa-columns text-blue-400"></i> Open Canvas
                            </button>
                        </div>

                    </div>
                </div>
            </div>

            <!-- Right: Diagram Preview (Canvas Panel) -->
            <div class="flex flex-col h-full bg-transparent transition-all duration-500 ease-in-out overflow-hidden"
                 :class="[showCanvas ? 'flex-1 opacity-100 translate-x-0' : 'w-0 opacity-0 translate-x-10 absolute right-0']">
                 
                <div class="gemini-card w-full h-full flex flex-col overflow-hidden relative border border-[#444746] m-4 mt-0 ml-0 !rounded-2xl shadow-2xl">
                    
                    <!-- Canvas Header Toolbar -->
                    <div class="h-14 bg-[#1E1F20] border-b border-[#444746] flex items-center justify-between px-4 shrink-0">
                        <div class="flex items-center gap-3">
                            <i class="fas fa-bezier-curve text-blue-400"></i>
                            <span class="text-sm font-medium text-neutral-200">CoffeeViz 架构可视化生成器</span>
                            <div class="h-4 w-[1px] bg-[#444746] mx-1"></div>
                            <div class="flex gap-1">
                                <button class="w-8 h-8 flex items-center justify-center rounded hover:bg-[#3C4043] text-neutral-400 transition" title="Undo"><i class="fas fa-undo text-xs"></i></button>
                                <button class="w-8 h-8 flex items-center justify-center rounded hover:bg-[#3C4043] text-neutral-400 transition" title="Redo"><i class="fas fa-redo text-xs"></i></button>
                            </div>
                        </div>
                        
                        <div class="flex items-center gap-2">
                            <div class="flex bg-[#28292A] rounded-lg p-0.5 border border-[#444746] mr-2">
                                <button 
                                    @click="canvasView = 'code'" 
                                    class="px-3 py-1 rounded text-xs font-medium transition"
                                    :class="[canvasView === 'code' ? 'bg-[#3C4043] text-white' : 'text-neutral-400 hover:text-neutral-200']"
                                >代码</button>
                                <button 
                                    @click="canvasView = 'preview'" 
                                    class="px-3 py-1 rounded text-xs font-medium transition"
                                    :class="[canvasView === 'preview' ? 'bg-[#3C4043] text-white' : 'text-neutral-400 hover:text-neutral-200']"
                                >预览</button>
                            </div>
                            
                            <button @click="handleSave" class="w-8 h-8 flex items-center justify-center rounded hover:bg-[#3C4043] text-neutral-400 hover:text-white transition" title="Save to MinIO">
                                <i class="fas fa-cloud-upload-alt"></i>
                            </button>
                            <button @click="handleDownload" class="w-8 h-8 flex items-center justify-center rounded hover:bg-[#3C4043] text-neutral-400 hover:text-white transition" title="Download PNG">
                                <i class="fas fa-download"></i>
                            </button>
                            <button @click="handleCopy" class="w-8 h-8 flex items-center justify-center rounded hover:bg-[#3C4043] text-neutral-400 hover:text-white transition" title="Copy Image">
                                <i class="fas fa-copy"></i>
                            </button>
                            
                            <div class="h-4 w-[1px] bg-[#444746] mx-1"></div>
                            
                            <button @click="showCanvas = false" class="w-8 h-8 flex items-center justify-center rounded hover:bg-[#3C4043] text-neutral-400 hover:text-white transition" title="Close Canvas"><i class="fas fa-times"></i></button>
                        </div>
                    </div>

                    <!-- Diagram Canvas Area -->
                    <div class="flex-1 relative overflow-hidden flex items-center justify-center bg-[#1E1F20]">
                        
                        <!-- Empty / Waiting State -->
                        <div v-if="!mermaidCode && !loading" class="text-neutral-500 flex flex-col items-center">
                            <div class="w-12 h-12 rounded-full bg-[#28292A] flex items-center justify-center mb-3">
                                <i class="fas fa-project-diagram text-lg"></i>
                            </div>
                            <p class="text-xs font-medium">Visualization Preview</p>
                        </div>

                        <!-- Drawing State -->
                        <div v-if="streaming.diagram" class="flex flex-col items-center justify-center absolute inset-0 z-10 bg-[#1E1F20]/80">
                            <div class="w-8 h-8 rounded-full border-2 border-t-blue-500 border-r-purple-500 border-b-red-500 border-l-transparent animate-spin mb-3"></div>
                            <p class="gemini-gradient-text text-xs font-medium">Generating Diagram...</p>
                        </div>

                        <!-- Rendered Diagram (Preview Mode) -->
                        <div v-show="canvasView === 'preview' && mermaidCode" class="mermaid-container w-full h-full p-8 overflow-auto flex items-center justify-center" ref="mermaidContainer"></div>
                        
                        <!-- Mermaid Code (Code Mode) -->
                        <div v-show="canvasView === 'code' && mermaidCode" class="w-full h-full p-0 overflow-hidden bg-[#1E1F20]">
                            <textarea 
                                readonly 
                                class="w-full h-full bg-[#1E1F20] text-[#A8C7FA] font-mono text-sm p-6 resize-none outline-none border-none custom-scrollbar"
                                :value="mermaidCode"
                            ></textarea>
                        </div>

                    </div>
                </div>
            </div>

        </div>

    </main>

    <!-- Input Box Container -->
    <div 
        class="absolute z-50 transition-all duration-500 ease-in-out pointer-events-none"
        :class="[ 
            hasStarted 
                ? (showCanvas ? 'bottom-4 left-0 w-[30%] min-w-[400px] px-6' : 'bottom-4 left-1/2 -translate-x-1/2 w-full max-w-[800px] px-4') 
                : 'top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-2xl px-4'
        ]"
    >
        <div class="w-full pointer-events-auto relative">
            
            <!-- Welcome Title (Only visible when centered) -->
            <div 
                class="text-center input-transition overflow-hidden"
                :class="[ hasStarted ? 'max-h-0 opacity-0 mb-0' : 'max-h-60 opacity-100 mb-8' ]"
            >
                <h2 class="text-4xl font-bold mb-3">What do you want to build?</h2>
                <p class="text-neutral-400">描述业务需求，AI 将为您生成数据库设计与 ER 图</p>
            </div>

            <!-- Input Box -->
            <div 
                class="gemini-input rounded-3xl p-2 flex flex-col gap-2 relative group transition-all duration-300"
                :class="[ hasStarted ? '!bg-[#1E1F20] border-[#444746]' : '' ]"
            >
                <!-- Textarea -->
                <textarea 
                    v-model="prompt" 
                    @keydown.ctrl.enter="handleAction"
                    @input="autoResize"
                    ref="textareaRef"
                    rows="1" 
                    class="bg-transparent border-none outline-none text-white w-full px-4 py-2 resize-none placeholder-neutral-500 max-h-60 custom-scrollbar text-lg leading-relaxed"
                    placeholder="输入您的需求，例如：设计一个即时通讯系统..."
                ></textarea>

                <!-- Footer Actions -->
                <div class="flex justify-between items-center px-2 pb-0">
                    <!-- Quick Tags -->
                    <div class="flex gap-2 overflow-x-auto no-scrollbar">
                        <button @click="fillPrompt('设计一个电商订单系统，包含用户、商品、订单和支付')" class="text-xs px-3 py-1.5 rounded-full bg-white/5 hover:bg-white/10 text-neutral-400 hover:text-amber-400 transition border border-white/5 whitespace-nowrap">
                            <i class="fas fa-shopping-cart mr-1"></i> 电商系统
                        </button>
                        <button @click="fillPrompt('设计一个博客内容管理系统，支持多作者和评论')" class="text-xs px-3 py-1.5 rounded-full bg-white/5 hover:bg-white/10 text-neutral-400 hover:text-amber-400 transition border border-white/5 whitespace-nowrap">
                            <i class="fas fa-newspaper mr-1"></i> 博客系统
                        </button>
                        <button @click="fillPrompt('设计一个项目管理工具，支持看板和任务分配')" class="text-xs px-3 py-1.5 rounded-full bg-white/5 hover:bg-white/10 text-neutral-400 hover:text-amber-400 transition border border-white/5 whitespace-nowrap">
                            <i class="fas fa-tasks mr-1"></i> 项目管理
                        </button>
                    </div>

                    <!-- Send Button -->
                    <button 
                        @click="handleAction" 
                        :disabled="!loading && !prompt.trim()"
                        class="ml-3 w-10 h-10 rounded-xl bg-white text-black hover:bg-amber-400 hover:scale-105 transition-all flex items-center justify-center disabled:opacity-30 disabled:hover:bg-white disabled:hover:scale-100 disabled:cursor-not-allowed shadow-lg"
                    >
                        <i v-if="loading" class="fas fa-stop text-sm"></i>
                        <i v-else class="fas fa-arrow-up text-sm"></i>
                    </button>
                </div>
            </div>
            
            <!-- Footer Info (Only visible when centered) -->
            <div 
                class="text-center text-xs text-neutral-600 input-transition overflow-hidden"
                :class="[ hasStarted ? 'max-h-0 opacity-0 mt-0' : 'max-h-10 opacity-100 mt-4' ]"
            >
                <p>Powered by CoffeeViz AI • Supports Streaming & Mermaid Visualization</p>
            </div>
        </div>
    </div>

    <!-- Settings Modal -->
    <div v-if="showSettings" class="fixed inset-0 bg-black/80 backdrop-blur-sm z-[100] flex items-center justify-center fade-in">
        <div class="glass-card w-96 p-6 rounded-2xl">
            <h3 class="text-lg font-bold mb-4">设置</h3>
            <div class="space-y-4">
                <div>
                    <label class="block text-xs text-neutral-400 mb-1">Backend API URL</label>
                    <input v-model="settings.apiUrl" type="text" class="w-full bg-neutral-900 border border-neutral-700 rounded p-2 text-sm text-white focus:border-amber-500 outline-none">
                </div>

            </div>
            <div class="mt-6 flex justify-end">
                <button @click="showSettings = false" class="px-4 py-2 bg-neutral-800 hover:bg-neutral-700 rounded-lg text-sm font-bold transition">Close</button>
            </div>
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted, watch, computed } from 'vue';
import { useMessage } from 'naive-ui';

// Dynamic Load Helper
const loadScript = (src) => {
    return new Promise((resolve, reject) => {
        if (document.querySelector(`script[src="${src}"]`)) {
            resolve();
            return;
        }
        const script = document.createElement('script');
        script.src = src;
        script.onload = resolve;
        script.onerror = reject;
        document.head.appendChild(script);
    });
};

const loadStyle = (href) => {
    if (document.querySelector(`link[href="${href}"]`)) return;
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = href;
    document.head.appendChild(link);
};


const message = useMessage();
const prompt = ref('');
const currentPrompt = ref('');
const loading = ref(false);
const stopFlag = ref(false);
const hasStarted = ref(false); // Controls Layout State
const isThinking = ref(false); // Controls Thinking UI
const showSettings = ref(false);
const showCanvas = ref(false); // Controls the visibility of the right canvas panel
const canvasView = ref('preview'); // 'preview' | 'code'
const isMarkedLoaded = ref(false);

const streamedExplanation = ref('');
const streamedSql = ref('');
const mermaidCode = ref('');

const outputContainer = ref(null);
const sqlCodeBlock = ref(null);
const mermaidContainer = ref(null);
const textareaRef = ref(null);

const streaming = reactive({
    explanation: false,
    sql: false,
    diagram: false
});

const settings = reactive({
    apiUrl: 'http://localhost:8080/api/ai/generate'
});

const renderedExplanation = computed(() => {
    if (!streamedExplanation.value) return '';
    if (isMarkedLoaded.value && window.marked) {
        return window.marked.parse(streamedExplanation.value);
    }
    return streamedExplanation.value;
});

// Watch for start to reset canvas
watch(hasStarted, (val) => {
    if (!val) showCanvas.value = false;
});

onMounted(async () => {
    // Dynamic Load Prism and Mermaid and Marked
    try {
        loadStyle('https://cdn.bootcdn.net/ajax/libs/prism/1.29.0/themes/prism-tomorrow.min.css');
        await Promise.all([
            loadScript('https://cdn.bootcdn.net/ajax/libs/mermaid/10.6.1/mermaid.min.js'),
            loadScript('https://cdn.bootcdn.net/ajax/libs/prism/1.29.0/prism.min.js'),
            loadScript('https://cdn.bootcdn.net/ajax/libs/marked/12.0.0/marked.min.js')
        ]);
        await loadScript('https://cdn.bootcdn.net/ajax/libs/prism/1.29.0/components/prism-sql.min.js');
        
        isMarkedLoaded.value = true;

        if (window.mermaid) {
            window.mermaid.initialize({ startOnLoad: false, theme: 'dark', securityLevel: 'loose' });
        }
    } catch (e) {
        console.error('Failed to load dependencies', e);
    }

    // Focus input on load
    if(textareaRef.value) textareaRef.value.focus();
});

// Auto resize textarea
const autoResize = () => {
    const el = textareaRef.value;
    if(el) {
        el.style.height = 'auto';
        el.style.height = el.scrollHeight + 'px';
    }
};

const fillPrompt = (text) => {
    prompt.value = text;
    nextTick(() => {
        autoResize();
        if(textareaRef.value) textareaRef.value.focus();
    });
};

const sleep = (ms) => new Promise(r => setTimeout(r, ms));

const streamText = async (fullText, targetRef, type) => {
    streaming[type] = true;
    targetRef.value = '';
    
    const chunkSize = 3; 
    for (let i = 0; i < fullText.length; i += chunkSize) {
        if (stopFlag.value) break;
        targetRef.value += fullText.slice(i, i + chunkSize);
        
        if (outputContainer.value) {
            outputContainer.value.scrollTop = outputContainer.value.scrollHeight;
        }
        
        if (type === 'sql' && i % 50 === 0 && sqlCodeBlock.value && window.Prism) {
            window.Prism.highlightElement(sqlCodeBlock.value);
        }

        await sleep(10 + Math.random() * 20); 
    }
    
    if (type === 'sql' && sqlCodeBlock.value && window.Prism) {
        window.Prism.highlightElement(sqlCodeBlock.value);
    }
    streaming[type] = false;
};

const renderMermaid = async (code) => {
    if (!mermaidContainer.value || !window.mermaid) return;
    streaming.diagram = true;
    mermaidContainer.value.innerHTML = '';
    
    await sleep(800); // Fake delay for "Drawing..." effect

    try {
        const { svg } = await window.mermaid.render('mermaid-graph-' + Date.now(), code);
        mermaidContainer.value.innerHTML = svg;
    } catch (e) {
        console.error('Mermaid render error:', e);
        mermaidContainer.value.innerHTML = '<div class="text-red-500">Error rendering diagram</div>';
    } finally {
        streaming.diagram = false;
    }
};

const stop = () => {
    stopFlag.value = true;
    loading.value = false;
    isThinking.value = false;
    Object.keys(streaming).forEach(k => streaming[k] = false);
};

const getDiagramBlob = async () => {
    if (!mermaidContainer.value) return null;
    const svgEl = mermaidContainer.value.querySelector('svg');
    if (!svgEl) return null;
    
    // Serialize SVG
    const svgData = new XMLSerializer().serializeToString(svgEl);
    
    // Create Canvas
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const img = new Image();
    
    // Get dimensions (use viewBox or bounding client rect)
    const rect = svgEl.getBoundingClientRect();
    const viewBoxAttr = svgEl.getAttribute('viewBox');
    let width = rect.width;
    let height = rect.height;
    
    if (viewBoxAttr) {
        const viewBox = viewBoxAttr.split(/\s+|,/).map(Number);
        if (viewBox.length === 4) {
            width = viewBox[2];
            height = viewBox[3];
        }
    }
    
    // Scale for better quality
    const scale = 2;
    canvas.width = width * scale;
    canvas.height = height * scale;
    
    const svgBlob = new Blob([svgData], { type: 'image/svg+xml;charset=utf-8' });
    const url = URL.createObjectURL(svgBlob);
    
    return new Promise((resolve) => {
        img.onload = () => {
            // Fill background
            ctx.fillStyle = '#1E1F20'; 
            ctx.fillRect(0, 0, canvas.width, canvas.height);
            
            ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
            canvas.toBlob((blob) => {
                URL.revokeObjectURL(url);
                resolve(blob);
            }, 'image/png');
        };
        img.onerror = () => {
            URL.revokeObjectURL(url);
            resolve(null);
        };
        img.src = url;
    });
};

const handleSave = async () => {
    if (!mermaidCode.value) return;
    
    const blob = await getDiagramBlob();
    if (!blob) {
        message.error('Failed to generate image from diagram');
        return;
    }
    
    const formData = new FormData();
    formData.append('file', blob, `diagram-${Date.now()}.png`);
    
    try {
        const token = localStorage.getItem('token');
        const headers = {};
        if (token) {
            headers['Authorization'] = token; 
        }
        
        const res = await fetch('http://localhost:8080/api/file/upload', {
            method: 'POST',
            headers: headers,
            body: formData
        });
        
        const data = await res.json();
        if (data.code === 200) {
            message.success('Saved successfully! URL: ' + data.data);
        } else {
            message.error('Save failed: ' + (data.message || 'Unknown error'));
        }
    } catch (e) {
        console.error(e);
        message.error('Save error: ' + e.message);
    }
};

const handleDownload = async () => {
    if (!mermaidCode.value) return;
    const blob = await getDiagramBlob();
    if (!blob) return;
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `coffeeviz-diagram-${Date.now()}.png`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
};

const handleCopy = async () => {
    if (!mermaidCode.value) return;
    try {
        const blob = await getDiagramBlob();
        if (!blob) return;
        await navigator.clipboard.write([
            new ClipboardItem({ 'image/png': blob })
        ]);
        message.success('Image copied to clipboard');
    } catch (e) {
        console.error(e);
        message.error('Copy failed: ' + e.message);
    }
};

const handleAction = () => {
    if (loading.value) {
        stop();
    } else {
        generate();
    }
};

const generate = async () => {
    const textToSend = prompt.value.trim();
    if (!textToSend) return;
    
    currentPrompt.value = textToSend;
    prompt.value = '';
    
    stopFlag.value = false;
    loading.value = true;
    hasStarted.value = true; // Trigger layout change immediately
    isThinking.value = true; // Show thinking state
    
    // Reset outputs
    streamedExplanation.value = '';
    streamedSql.value = '';
    mermaidCode.value = '';

    // Reset textarea height after send
    if(textareaRef.value) {
         textareaRef.value.style.height = 'auto';
    }

    try {
        let data;
        
        // 1. Thinking Phase (Simulate Backend Call or Mock)
        // const startTime = Date.now();
        
        const token = localStorage.getItem('token');
        if (!token) {
            throw new Error('未登录，请先登录系统');
        }

        const res = await fetch(settings.apiUrl, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': token
            },
            body: JSON.stringify({
                prompt: textToSend,
                dbType: 'mysql',
                namingStyle: 'snake_case'
            })
        });
        
        const json = await res.json();
        if (json.code === 200) {
            data = json.data;
        } else {
            throw new Error(json.message || 'API Error');
        }

        // 2. Output Phase
        if (stopFlag.value) return;
        isThinking.value = false; // Hide thinking, start streaming
        loading.value = false; // Enable input again

        // Stream Explanation
        await streamText(data.aiExplanation || data.explanation, streamedExplanation, 'explanation');
        if (stopFlag.value) return;
        
        // Stream SQL
        await streamText(data.sqlDdl, streamedSql, 'sql');
        if (stopFlag.value) return;

        // 3. Draw Phase - Open Canvas
        showCanvas.value = true;
        
        // Sanitize Mermaid Code from LLM
        // 1. Remove Markdown code blocks
        // 2. Remove backticks from entity names (Fixes Parse error: Expecting 'EOF', 'SPACE'..., got '`')
        let cleanMermaid = data.mermaidCode
            .replace(/```mermaid/g, '')
            .replace(/```/g, '')
            .trim();
        
        // Remove backticks around entity names (e.g. `User` -> User)
        cleanMermaid = cleanMermaid.replace(/`([^`]+)`/g, '$1');
        
        mermaidCode.value = cleanMermaid;
        await renderMermaid(cleanMermaid);

    } catch (error) {
        console.error(error);
        alert('Error: ' + error.message);
        loading.value = false;
        isThinking.value = false;
    } finally {
         // prompt.value = ''; 
    }
};

const copySql = () => {
    navigator.clipboard.writeText(streamedSql.value);
    // Minimal feedback logic can be added here
};

const toggleSettings = () => {
    showSettings.value = !showSettings.value;
};
</script>

<style scoped>
/* Gemini Style Overrides */
.gemini-card {
    background-color: #1E1F20;
    border-radius: 1.5rem; /* rounded-3xl */
    border: 1px solid transparent;
}

.gemini-input {
    background-color: #1E1F20;
    border-radius: 2rem; /* Pill shape */
    border: 1px solid transparent;
    transition: all 0.2s ease;
}

.gemini-input:focus-within {
    background-color: #28292A;
    box-shadow: none;
}

.gemini-gradient-text {
    background: linear-gradient(90deg, #4285F4, #9B72CB, #D96570);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

/* Custom Scrollbar - Thinner and darker */
::-webkit-scrollbar {
    width: 8px;
    height: 8px;
}
::-webkit-scrollbar-track {
    background: transparent; 
}
::-webkit-scrollbar-thumb {
    background: #444746; 
    border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
    background: #5e5e5e; 
}

/* Cursor blinking for streaming effect */
.cursor-blink::after {
    content: '●'; /* Gemini uses a circle or just a cursor */
    font-size: 0.8em;
    animation: blink 1s step-end infinite;
    color: #4285F4;
    margin-left: 2px;
}
@keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
}

/* Animations */
.fade-enter-active, .fade-leave-active {
    transition: opacity 0.5s ease;
}
.fade-enter-from, .fade-leave-to {
    opacity: 0;
}

.slide-up-enter-active, .slide-up-leave-active {
    transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from, .slide-up-leave-to {
    transform: translateY(20px);
    opacity: 0;
}

.input-transition {
    transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

/* Markdown Styles */
.markdown-body h1, .markdown-body h2, .markdown-body h3 {
    margin-top: 1.5em;
    margin-bottom: 0.5em;
    font-weight: 600;
    color: #E3E3E3;
}
.markdown-body h2 { font-size: 1.25em; }
.markdown-body h3 { font-size: 1.1em; }
.markdown-body p { margin-bottom: 1em; }
.markdown-body ul { list-style-type: disc; padding-left: 1.5em; margin-bottom: 1em; }
.markdown-body ol { list-style-type: decimal; padding-left: 1.5em; margin-bottom: 1em; }
.markdown-body li { margin-bottom: 0.25em; }
.markdown-body strong { font-weight: 700; color: #fff; }
.markdown-body code { 
    background-color: rgba(255,255,255,0.1); 
    padding: 0.2em 0.4em; 
    border-radius: 4px; 
    font-family: monospace; 
    font-size: 0.9em;
}
.markdown-body pre {
    background-color: #1E1F20;
    padding: 1em;
    border-radius: 8px;
    overflow-x: auto;
    margin-bottom: 1em;
}
</style>