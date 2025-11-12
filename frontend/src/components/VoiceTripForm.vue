<script setup lang="ts">
import { ref, reactive, watch, onMounted, onUnmounted } from 'vue';
import { useTripsStore } from '@/stores/trips';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Microphone, VideoPause } from '@element-plus/icons-vue';
import type { TripRequest } from '@/stores/trips';

// Web Speech API 类型声明
declare global {
  interface Window {
    SpeechRecognition: new () => SpeechRecognition;
    webkitSpeechRecognition: new () => SpeechRecognition;
  }
}

interface SpeechRecognition extends EventTarget {
  continuous: boolean;
  interimResults: boolean;
  lang: string;
  start(): void;
  stop(): void;
  onstart: (() => void) | null;
  onresult: ((event: SpeechRecognitionEvent) => void) | null;
  onerror: ((event: SpeechRecognitionErrorEvent) => void) | null;
  onend: (() => void) | null;
}

interface SpeechRecognitionEvent extends Event {
  resultIndex: number;
  results: SpeechRecognitionResultList;
}

interface SpeechRecognitionResultList {
  readonly length: number;
  item(index: number): SpeechRecognitionResult;
  [index: number]: SpeechRecognitionResult;
}

interface SpeechRecognitionResult {
  readonly isFinal: boolean;
  readonly length: number;
  item(index: number): SpeechRecognitionAlternative;
  [index: number]: SpeechRecognitionAlternative;
}

interface SpeechRecognitionAlternative {
  readonly transcript: string;
  readonly confidence: number;
}

interface SpeechRecognitionErrorEvent extends Event {
  error: string;
}

const emit = defineEmits<{
  createSuccess: [tripId: string]
}>();

const tripsStore = useTripsStore();
const router = useRouter();

// 语音识别文本
const voiceText = ref('');

// 语音识别相关状态
const isListening = ref(false);
const isSupported = ref(false);
const recognition = ref<SpeechRecognition | null>(null);
const interimTranscript = ref('');

// 检查浏览器是否支持Web Speech API
const checkSpeechRecognitionSupport = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (SpeechRecognition) {
    isSupported.value = true;
    initializeSpeechRecognition();
  } else {
    isSupported.value = false;
    ElMessage.warning('您的浏览器不支持语音识别功能，请使用Chrome、Edge等现代浏览器');
  }
};

// 初始化语音识别
const initializeSpeechRecognition = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  recognition.value = new SpeechRecognition();
  
  if (recognition.value) {
    recognition.value.continuous = true;
    recognition.value.interimResults = true;
    recognition.value.lang = 'zh-CN'; // 中文识别
    
    recognition.value.onstart = () => {
      isListening.value = true;
      interimTranscript.value = '';
    };
    
    recognition.value.onresult = (event) => {
      let finalTranscript = '';
      interimTranscript.value = '';
      
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const result = event.results[i];
        if (result && result[0]) {
          const transcript = result[0].transcript;
          if (result.isFinal) {
            finalTranscript += transcript;
          } else {
            interimTranscript.value += transcript;
          }
        }
      }
      
      if (finalTranscript) {
        // 追加内容而不是重定义，并在追加时添加空格分隔
        if (voiceText.value && !voiceText.value.endsWith(' ')) {
          voiceText.value += ' ';
        }
        voiceText.value += finalTranscript;
      }
    };
    
    recognition.value.onerror = (event) => {
      console.error('语音识别错误:', event.error);
      if (event.error === 'not-allowed') {
        ElMessage.error('请允许浏览器使用麦克风权限');
      } else if (event.error === 'audio-capture') {
        ElMessage.error('无法访问麦克风，请检查设备连接');
      } else {
        ElMessage.error('语音识别发生错误: ' + event.error);
      }
      stopListening();
    };
    
    recognition.value.onend = () => {
      isListening.value = false;
    };
  }
};

// 开始语音识别
const startListening = () => {
  if (!isSupported.value) {
    ElMessage.warning('浏览器不支持语音识别');
    return;
  }
  
  if (recognition.value && !isListening.value) {
    try {
      recognition.value.start();
    } catch (error) {
      console.error('启动语音识别失败:', error);
      ElMessage.error('启动语音识别失败');
    }
  }
};

// 停止语音识别
const stopListening = () => {
  if (recognition.value && isListening.value) {
    try {
      recognition.value.stop();
      isListening.value = false;
      interimTranscript.value = '';
      ElMessage.success('语音识别已停止');
    } catch (error) {
      console.error('停止语音识别失败:', error);
      // 强制停止
      isListening.value = false;
      interimTranscript.value = '';
    }
  }
};

// 切换语音识别状态
const toggleListening = () => {
  if (isListening.value) {
    stopListening();
  } else {
    startListening();
  }
};

// 清除语音文本
const clearVoiceText = () => {
  voiceText.value = '';
  interimTranscript.value = '';
  ElMessage.info('语音识别结果已清空');
};

// 组件挂载时检查支持情况
onMounted(() => {
  checkSpeechRecognitionSupport();
});

// 组件卸载时停止语音识别
onUnmounted(() => {
  if (recognition.value && isListening.value) {
    recognition.value.stop();
  }
});

// 日期格式化
const formatDate = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// 行程默认值（后续可从语音中智能提取）
const tripDefaults = reactive({
  title: '语音创建的行程',
  destination: '北京', // 默认目的地
  startDate: formatDate(new Date()),
  endDate: formatDate(new Date(Date.now() + 3 * 24 * 60 * 60 * 1000)), // 默认3天后
  budgetTotal: 5000,
  companionCount: 1,
  preferences: [] as string[]
});

// 加载状态
const loading = ref(false);
const showLoadingOverlay = ref(false);

// 智能提取行程信息（增强版本）
const extractTripInfoFromText = (text: string) => {
  if (!text) return;
  
  // 重置提取结果
  const extractedInfo = {
    destination: '',
    startDate: '',
    endDate: '',
    days: 0,
    budget: 0,
    travelers: 1,
    preferences: [] as string[]
  };
  
  // 1. 提取目的地 - 使用地名词典和模式匹配
  const destinationPatterns = [
    /去(?:\s*)([\u4e00-\u9fa5]{2,10})(?:\s*)(?:旅游|旅行|玩)/g,
    /到(?:\s*)([\u4e00-\u9fa5]{2,10})(?:\s*)(?:旅游|旅行|玩)/g,
    /想去(?:\s*)([\u4e00-\u9fa5]{2,10})(?:\s*)(?:旅游|旅行|玩)/g,
    /计划去(?:\s*)([\u4e00-\u9fa5]{2,10})(?:\s*)(?:旅游|旅行|玩)/g
  ];
  
  // 中国主要城市列表（扩充版）
  const majorCities = [
    // 直辖市
    '北京', '上海', '天津', '重庆',
    // 省会城市
    '广州', '深圳', '成都', '杭州', '西安', '南京', '武汉', '郑州', '长沙', '沈阳',
    '济南', '合肥', '太原', '南昌', '南宁', '昆明', '贵阳', '兰州', '银川', '西宁',
    '乌鲁木齐', '拉萨', '哈尔滨', '长春', '石家庄', '呼和浩特', '海口', '福州',
    // 经济特区及重要城市
    '厦门', '青岛', '大连', '宁波', '苏州', '无锡', '东莞', '佛山', '温州', '常州',
    '南通', '徐州', '扬州', '镇江', '泰州', '盐城', '淮安', '连云港', '宿迁',
    '绍兴', '嘉兴', '湖州', '金华', '衢州', '舟山', '台州', '丽水',
    '泉州', '漳州', '莆田', '三明', '南平', '龙岩', '宁德',
    '烟台', '潍坊', '济宁', '泰安', '临沂', '德州', '聊城', '滨州', '菏泽',
    '洛阳', '开封', '安阳', '新乡', '焦作', '濮阳', '许昌', '漯河', '三门峡',
    '宜昌', '襄阳', '荆州', '黄石', '十堰', '孝感', '荆门', '鄂州', '黄冈',
    '株洲', '湘潭', '衡阳', '邵阳', '岳阳', '常德', '张家界', '益阳', '郴州',
    '惠州', '珠海', '汕头', '佛山', '韶关', '湛江', '肇庆', '江门', '茂名',
    '桂林', '柳州', '梧州', '北海', '防城港', '钦州', '贵港', '玉林', '百色',
    '绵阳', '德阳', '南充', '宜宾', '广安', '达州', '雅安', '阿坝', '甘孜',
    '遵义', '六盘水', '安顺', '毕节', '铜仁', '黔西南', '黔东南', '黔南',
    '曲靖', '玉溪', '保山', '昭通', '丽江', '普洱', '临沧', '楚雄', '红河',
    '宝鸡', '咸阳', '渭南', '延安', '汉中', '榆林', '安康', '商洛',
    '齐齐哈尔', '牡丹江', '佳木斯', '大庆', '鸡西', '双鸭山', '伊春', '七台河',
    '芜湖', '蚌埠', '淮南', '马鞍山', '淮北', '铜陵', '安庆', '黄山', '滁州',
    '阜阳', '宿州', '六安', '亳州', '池州', '宣城',
    // 港澳台
    '香港', '澳门', '台北', '高雄', '台中', '台南', '新北', '桃园', '新竹', '基隆'
  ];
  
  // 世界主要国家列表
  const majorCountries = [
    // 亚洲
    '日本', '韩国', '新加坡', '马来西亚', '泰国', '越南', '菲律宾', '印度尼西亚', '印度',
    '斯里兰卡', '马尔代夫', '尼泊尔', '柬埔寨', '老挝', '缅甸', '文莱', '东帝汶',
    '土耳其', '以色列', '阿联酋', '沙特阿拉伯', '卡塔尔', '阿曼', '科威特', '巴林',
    '伊朗', '伊拉克', '叙利亚', '约旦', '黎巴嫩', '也门', '阿富汗', '巴基斯坦',
    // 欧洲
    '英国', '法国', '德国', '意大利', '西班牙', '葡萄牙', '荷兰', '比利时', '卢森堡',
    '瑞士', '奥地利', '瑞典', '挪威', '丹麦', '芬兰', '冰岛', '爱尔兰', '希腊',
    '俄罗斯', '波兰', '捷克', '匈牙利', '罗马尼亚', '保加利亚', '塞尔维亚', '克罗地亚',
    '斯洛伐克', '斯洛文尼亚', '爱沙尼亚', '拉脱维亚', '立陶宛', '乌克兰', '白俄罗斯',
    '摩纳哥', '安道尔', '圣马力诺', '梵蒂冈', '马耳他', '塞浦路斯',
    // 北美洲
    '美国', '加拿大', '墨西哥', '古巴', '牙买加', '巴哈马', '多米尼加', '海地',
    '哥斯达黎加', '巴拿马', '危地马拉', '洪都拉斯', '萨尔瓦多', '尼加拉瓜',
    // 南美洲
    '巴西', '阿根廷', '智利', '秘鲁', '哥伦比亚', '委内瑞拉', '厄瓜多尔', '玻利维亚',
    '乌拉圭', '巴拉圭', '圭亚那', '苏里南', '法属圭亚那',
    // 大洋洲
    '澳大利亚', '新西兰', '斐济', '巴布亚新几内亚', '所罗门群岛', '瓦努阿图',
    '萨摩亚', '汤加', '基里巴斯', '图瓦卢', '瑙鲁', '马绍尔群岛', '密克罗尼西亚',
    '帕劳', '库克群岛', '纽埃', '托克劳',
    // 非洲
    '埃及', '南非', '肯尼亚', '坦桑尼亚', '埃塞俄比亚', '摩洛哥', '突尼斯', '阿尔及利亚',
    '利比亚', '苏丹', '南苏丹', '尼日利亚', '加纳', '科特迪瓦', '塞内加尔', '马里',
    '布基纳法索', '尼日尔', '乍得', '喀麦隆', '刚果', '刚果民主共和国', '安哥拉',
    '赞比亚', '津巴布韦', '博茨瓦纳', '纳米比亚', '莫桑比克', '马达加斯加', '毛里求斯',
    '塞舌尔', '科摩罗', '圣多美和普林西比', '赤道几内亚', '加蓬', '几内亚', '几内亚比绍',
    '塞拉利昂', '利比里亚', '多哥', '贝宁', '冈比亚', '佛得角'
  ];
  
  // 尝试从模式中提取目的地
  for (const pattern of destinationPatterns) {
    const matches = [...text.matchAll(pattern)];
    if (matches.length > 0) {
      extractedInfo.destination = matches[0][1];
      break;
    }
  }
  
  // 如果没有从模式中提取到，尝试从城市列表中匹配
  if (!extractedInfo.destination) {
    for (const city of majorCities) {
      if (text.includes(city)) {
        extractedInfo.destination = city;
        console.log(`城市匹配：${city}`);
        break;
      }
    }
  }
  
  // 如果城市列表没有匹配到，尝试从国家列表中匹配
  if (!extractedInfo.destination) {
    for (const country of majorCountries) {
      if (text.includes(country)) {
        extractedInfo.destination = country;
        console.log(`国家匹配：${country}`);
        break;
      }
    }
  }
  
  // 2. 提取时间信息 - 增强的时间识别
  // 具体日期格式匹配
  const datePatterns = [
    // 完整日期格式：从2025年12月1日到12月3日
    /从(\d{4})年(\d{1,2})月(\d{1,2})[日号][到至](\d{4})年(\d{1,2})月(\d{1,2})[日号]/g,
    /从(\d{4})年(\d{1,2})月(\d{1,2})[日号][到至](\d{1,2})月(\d{1,2})[日号]/g,
    /从(\d{1,2})月(\d{1,2})[日号][到至](\d{1,2})月(\d{1,2})[日号]/g,
    
    // 完整日期格式：2025年12月1日到12月3日（支持日和号）- 按优先级排序
    // 1. 同一年份的日期范围：2025年12月1日到12月5日（最常见）
    /(\d{4})年(\d{1,2})月(\d{1,2})[日号][到至](\d{1,2})月(\d{1,2})[日号]/g,
    
    // 2. 完整年份范围：2025年12月1日到2026年12月5日（较少见）
    /(\d{4})年(\d{1,2})月(\d{1,2})[日号][到至](\d{4})年(\d{1,2})月(\d{1,2})[日号]/g,
    
    // 3. 无年份的月份日期范围：12月1日到12月5日
    /(\d{1,2})月(\d{1,2})[日号][到至](\d{1,2})月(\d{1,2})[日号]/g,
    
    // 单个日期格式
    /(\d{4})[年.-](\d{1,2})[月.-](\d{1,2})[日号]?/g, // 2024-01-15
    /(\d{1,2})[月.-](\d{1,2})[日号]?/g, // 1月15日
    /下[周星期](\d)/g, // 下周1
    /(\d+)月(\d+)号/g, // 1月15号
  ];
  
  // 中文数字转换函数
  const chineseToNumber = (chineseNum) => {
    const chineseNumbers = {
      '零': 0, '一': 1, '二': 2, '三': 3, '四': 4, '五': 5, '六': 6, '七': 7, '八': 8, '九': 9,
      '十': 10, '百': 100, '千': 1000, '万': 10000, '亿': 100000000
    };
    
    // 处理简单数字：一、二、三...
    if (chineseNum.length === 1 && chineseNumbers[chineseNum] !== undefined && chineseNumbers[chineseNum] < 10) {
      return chineseNumbers[chineseNum];
    }
    
    // 处理十以内的数字：十一、十二...
    if (chineseNum.length === 2 && chineseNum[0] === '十' && chineseNumbers[chineseNum[1]] !== undefined) {
      return 10 + chineseNumbers[chineseNum[1]];
    }
    
    // 处理十的倍数：二十、三十...
    if (chineseNum.length === 2 && chineseNumbers[chineseNum[0]] !== undefined && chineseNum[1] === '十') {
      return chineseNumbers[chineseNum[0]] * 10;
    }
    
    // 处理复杂数字：二十一、三十二...
    if (chineseNum.length === 3 && chineseNumbers[chineseNum[0]] !== undefined && chineseNum[1] === '十' && chineseNumbers[chineseNum[2]] !== undefined) {
      return chineseNumbers[chineseNum[0]] * 10 + chineseNumbers[chineseNum[2]];
    }
    
    // 默认返回0，表示无法转换
    return 0;
  };
  
  // 天数匹配 - 支持中文数字
  const dayPatterns = [
    /([零一二三四五六七八九十百千万亿]+|[\d]+)[-到至]([零一二三四五六七八九十百千万亿]+|[\d]+)天/g, // 三-五天 或 3-5天
    /([零一二三四五六七八九十百千万亿]+|[\d]+)天/g, // 三天 或 3天
    /([零一二三四五六七八九十百千万亿]+|[\d]+)日游/g, // 三日游 或 3日游
    /玩([零一二三四五六七八九十百千万亿]+|[\d]+)天/g, // 玩三天 或 玩3天
  ];
  
  // 处理具体日期信息
  for (const pattern of datePatterns) {
    const matches = [...text.matchAll(pattern)];
    if (matches.length > 0) {
      const match = matches[0];
      
      // 处理完整日期范围：2025年12月1日到12月3日（有年份）
      if (match.length >= 7) {
        const startYear = match[1] ? parseInt(match[1]) : new Date().getFullYear();
        const startMonth = parseInt(match[2]) - 1; // 月份从0开始
        const startDay = parseInt(match[3]);
        
        const endYear = match[4] ? parseInt(match[4]) : startYear;
        const endMonth = parseInt(match[5]) - 1;
        const endDay = parseInt(match[6]);
        
        const startDate = new Date(startYear, startMonth, startDay);
        const endDate = new Date(endYear, endMonth, endDay);
        
        extractedInfo.startDate = formatDate(startDate);
        extractedInfo.endDate = formatDate(endDate);
        extractedInfo.days = Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)) + 1;
        break;
      }
      
      // 处理月份日期范围：从12月15日到12月18日（无年份）
      if (match[1] && match[2] && match[3] && match[4]) {
        const currentYear = new Date().getFullYear();
        const startMonth = parseInt(match[1]) - 1;
        const startDay = parseInt(match[2]);
        const endMonth = parseInt(match[3]) - 1;
        const endDay = parseInt(match[4]);
        
        const startDate = new Date(currentYear, startMonth, startDay);
        const endDate = new Date(currentYear, endMonth, endDay);
        
        // 如果结束月份小于开始月份，说明跨年了
        if (endMonth < startMonth) {
          endDate.setFullYear(currentYear + 1);
        }
        
        extractedInfo.startDate = formatDate(startDate);
        extractedInfo.endDate = formatDate(endDate);
        extractedInfo.days = Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)) + 1;
        break;
      }
      
      // 处理单个日期（有年份）
      if (match[1] && match[2] && match[3]) {
        const year = match[1] ? parseInt(match[1]) : new Date().getFullYear();
        const month = parseInt(match[2]) - 1;
        const day = parseInt(match[3]);
        const date = new Date(year, month, day);
        
        // 如果是开始日期，设置开始日期
        if (!extractedInfo.startDate) {
          extractedInfo.startDate = formatDate(date);
        } else {
          // 如果是结束日期，设置结束日期
          extractedInfo.endDate = formatDate(date);
          if (extractedInfo.startDate) {
            const startDate = new Date(extractedInfo.startDate);
            extractedInfo.days = Math.ceil((date.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)) + 1;
          }
        }
      }
      
      // 处理单个日期（无年份）
      if (match[1] && match[2] && !match[3]) {
        const year = new Date().getFullYear();
        const month = parseInt(match[1]) - 1;
        const day = parseInt(match[2]);
        const date = new Date(year, month, day);
        
        // 如果是开始日期，设置开始日期
        if (!extractedInfo.startDate) {
          extractedInfo.startDate = formatDate(date);
        } else {
          // 如果是结束日期，设置结束日期
          extractedInfo.endDate = formatDate(date);
          if (extractedInfo.startDate) {
            const startDate = new Date(extractedInfo.startDate);
            extractedInfo.days = Math.ceil((date.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)) + 1;
          }
        }
      }
    }
  }
  
  // 处理天数信息（如果没有找到具体日期）
  if (!extractedInfo.startDate || !extractedInfo.endDate) {
    for (const pattern of dayPatterns) {
      const matches = [...text.matchAll(pattern)];
      if (matches.length > 0) {
        let startDays, endDays;
        
        // 处理中文数字或阿拉伯数字
        if (/[零一二三四五六七八九十百千万亿]/.test(matches[0][1])) {
          startDays = chineseToNumber(matches[0][1]);
        } else {
          startDays = parseInt(matches[0][1]);
        }
        
        if (matches[0][2]) {
          if (/[零一二三四五六七八九十百千万亿]/.test(matches[0][2])) {
            endDays = chineseToNumber(matches[0][2]);
          } else {
            endDays = parseInt(matches[0][2]);
          }
          // 范围天数，取平均值
          extractedInfo.days = Math.round((startDays + endDays) / 2);
        } else {
          extractedInfo.days = startDays;
        }
        
        console.log(`天数识别：${matches[0][0]} -> ${extractedInfo.days}天`);
        break;
      }
    }
  }
  
  // 3. 提取预算信息 - 增强的预算识别（支持中文数字）
  const budgetPatterns = [
    /([零一二三四五六七八九十百千万亿]+|[\d]+(?:\.\d+)?)[万w]?[元块]/g, // 五千元, 5000元, 1万元
    /预算(?:\s*)([零一二三四五六七八九十百千万亿]+|[\d]+(?:\.\d+)?)[万w]?[元块]?/g, // 预算五千, 预算5000
    /花费(?:\s*)([零一二三四五六七八九十百千万亿]+|[\d]+(?:\.\d+)?)[万w]?[元块]?/g, // 花费五千, 花费5000
    /人均(?:\s*)([零一二三四五六七八九十百千万亿]+|[\d]+(?:\.\d+)?)[万w]?[元块]?/g, // 人均五千, 人均5000
  ];
  
  for (const pattern of budgetPatterns) {
    const matches = [...text.matchAll(pattern)];
    if (matches.length > 0) {
      let budget;
      
      // 处理中文数字或阿拉伯数字
      if (/[零一二三四五六七八九十百千万亿]/.test(matches[0][1])) {
        budget = chineseToNumber(matches[0][1]);
      } else {
        budget = parseFloat(matches[0][1]);
      }
      
      // 处理"万"单位
      if (text.includes('万') || text.includes('w')) {
        budget *= 10000;
      }
      // 处理"千"单位
      if (text.includes('千') || text.includes('k')) {
        budget *= 1000;
      }
      
      // 如果是人均预算，需要根据出行人数计算总预算
      if (pattern.source.includes('人均')) {
        // 获取出行人数，如果没有识别到人数，默认1人
        const travelers = extractedInfo.travelers || 1;
        budget = budget * travelers;
        console.log(`人均预算识别：人均${matches[0][1]}元，${travelers}人，总预算${budget}元`);
      }
      
      extractedInfo.budget = budget;
      console.log(`预算识别：${matches[0][0]} -> ${budget}元`);
      break;
    }
  }
  
  // 4. 提取出行人数 - 支持中文数字
  const travelerPatterns = [
    /([零一二三四五六七八九十百千万亿]+|[\d]+)个人/g, // 三个人 或 3个人
    /([零一二三四五六七八九十百千万亿]+|[\d]+)人/g, // 三人 或 3人
    /([零一二三四五六七八九十百千万亿]+|[\d]+)个朋友/g, // 三个朋友 或 3个朋友
    /([零一二三四五六七八九十百千万亿]+|[\d]+)个同伴/g, // 三个同伴 或 3个同伴
    /([零一二三四五六七八九十百千万亿]+|[\d]+)个同学/g, // 三个同学 或 3个同学
  ];
  
  for (const pattern of travelerPatterns) {
    const matches = [...text.matchAll(pattern)];
    if (matches.length > 0) {
      let travelers;
      
      // 处理中文数字或阿拉伯数字
      if (/[零一二三四五六七八九十百千万亿]/.test(matches[0][1])) {
        travelers = chineseToNumber(matches[0][1]);
      } else {
        travelers = parseInt(matches[0][1]);
      }
      
      extractedInfo.travelers = travelers;
      console.log(`出行人数识别：${matches[0][0]} -> ${travelers}人`);
      break;
    }
  }
  
  // 5. 提取旅行偏好
  const preferenceKeywords = {
    '美食': '美食',
    '美食探索': '美食',
    '吃': '美食',
    '店': '美食',
    '自然': '自然风景',
    '风光': '自然风景',
    '景': '自然风景',
    '山': '自然风景',
    '水': '自然风景',
    '海': '自然风景',
    '俗': '文化',
    '文化': '文化',
    '人文': '文化',
    '漫': '文化',
    '历史': '历史古迹',
    '古迹': '历史古迹',
    '名胜': '历史古迹',
    '购物': '购物',
    '购物娱乐': '购物',
    '娱乐': '购物',
    '赶': '特种兵行程',
    '多': '特种兵行程',
    '急': '特种兵行程',
    '紧': '特种兵行程',
    '特种兵': '特种兵行程',
    '松': '轻松行程',
    '休闲': '轻松行程',
    '休闲度假': '轻松行程',
    '度假': '轻松行程',
    '轻松': '轻松行程',
    '主题乐园': '主题乐园',
    '乐园': '主题乐园',
    '艺术': '艺术展览', 
    '展览': '艺术展览',
    '亲子': '亲子',
    '孩': '亲子',
    '儿子': '亲子',
    '女儿': '亲子',
    '侣': '情侣',
    '爱人': '情侣',
    '爱情': '情侣',
    '浪漫': '情侣'
  };
  
  for (const [keyword, preference] of Object.entries(preferenceKeywords)) {
    if (text.includes(keyword) && !extractedInfo.preferences.includes(preference)) {
      extractedInfo.preferences.push(preference);
    }
  }
  
  // 6. 智能设置开始和结束日期（只有当没有识别到具体日期时）
  const today = new Date();
  if (extractedInfo.days > 0 && !extractedInfo.endDate) {
    if (!extractedInfo.startDate) {
      extractedInfo.startDate = formatDate(today);
    }
    const startDate = extractedInfo.startDate ? new Date(extractedInfo.startDate) : today;
    const endDate = new Date(startDate);
    endDate.setDate(startDate.getDate() + extractedInfo.days - 1); // 天数包含开始日期
    extractedInfo.endDate = formatDate(endDate);
  }
  
  // 7. 生成智能标题
  let title = '语音创建的行程';
  if (extractedInfo.destination) {
    if (extractedInfo.days > 0) {
      title = `${extractedInfo.startDate} ${extractedInfo.destination} ${extractedInfo.days}日游`;
    } else {
      title = `${extractedInfo.startDate} ${extractedInfo.destination}的行程`;
    }
  }
  
  // 更新行程默认值
  tripDefaults.title = title;
  tripDefaults.destination = extractedInfo.destination || '北京';
  
  // 智能设置开始和结束日期
  if (extractedInfo.startDate && extractedInfo.days > 0 && !extractedInfo.endDate) {
    // 基于开始日期和天数计算结束日期
    const startDate = new Date(extractedInfo.startDate);
    const endDate = new Date(startDate);
    endDate.setDate(startDate.getDate() + extractedInfo.days - 1); // 天数包含开始日期
    tripDefaults.startDate = extractedInfo.startDate;
    tripDefaults.endDate = formatDate(endDate);
  } else {
    // 使用默认逻辑
    tripDefaults.startDate = extractedInfo.startDate || formatDate(today);
    tripDefaults.endDate = extractedInfo.endDate || formatDate(new Date(today.getTime() + 3 * 24 * 60 * 60 * 1000)); // 默认3天
  }
  
  tripDefaults.budgetTotal = extractedInfo.budget || 5000;
  tripDefaults.companionCount = extractedInfo.travelers || 1;
  tripDefaults.preferences = extractedInfo.preferences;
  
  console.log('智能提取结果:', extractedInfo);
};

// 监听文本变化，自动提取行程信息
watch(voiceText, (newText) => {
  extractTripInfoFromText(newText);
});

// 创建行程
const handleCreateTrip = async () => {
  if (!voiceText.value.trim()) {
    ElMessage.warning('请先输入旅行需求');
    return;
  }
  
  try {
    loading.value = true;
    showLoadingOverlay.value = true;
    
    // 构建行程请求对象
    const tripRequest: TripRequest = {
      ...tripDefaults,
      request: voiceText.value // 将输入文本作为原始请求
    };
    
    // 调用创建行程API
    const trip = await tripsStore.createTrip(tripRequest);
    
    // 不再需要进度条，直接等待后端返回
    
    // 短暂延迟后关闭遮罩并显示成功消息，然后跳转到dashboard
    setTimeout(() => {
      showLoadingOverlay.value = false;
      if (trip) {
        ElMessage.success('旅行计划已生成');
        // 自动跳转到dashboard页面并刷新
        router.push('/dashboard').then(() => {
          window.location.reload();
        });
      } else {
        ElMessage.error(tripsStore.error || '行程创建失败');
      }
    }, 30000);
  } catch (error) {
    showLoadingOverlay.value = false;
    ElMessage.error('创建行程时发生错误');
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="voice-input-container">
    <div class="voice-input-section">
      <!-- 语音输入功能 -->
      <div v-if="isSupported" class="voice-controls">
        <div class="voice-status">
          <el-alert
            :title="isListening ? '正在聆听中...' : '点击麦克风开始语音输入'"
            :type="isListening ? 'success' : 'info'"
            :description="isListening ? '请说出您的旅行需求' : '支持中文语音输入，自动识别旅行信息'"
            show-icon
            :closable="false"
          ></el-alert>
        </div>
        
        <div class="voice-buttons">
          <!-- 整合的录音按钮 -->
          <el-button
            :type="isListening ? 'danger' : 'primary'"
            @click="toggleListening"
            size="large"
            circle
            class="microphone-btn"
            :loading="false"
            :title="isListening ? '点击停止录音' : '点击开始录音'"
          >
            <el-icon v-if="!isListening">
              <Microphone />
            </el-icon>
            <el-icon v-else>
              <VideoPause />
            </el-icon>
          </el-button>
          
          <el-button
            type="info"
            icon="Delete"
            @click="clearVoiceText"
            :disabled="!voiceText && !interimTranscript"
            size="large"
          >
            清空文本
          </el-button>
        </div>
        
        <!-- 实时识别结果显示 -->
        <div class="voice-result">
          <div class="result-title">
            <span>语音识别结果（可编辑）</span>
            <div class="result-actions">
              <el-tag v-if="isListening" type="warning" size="small">识别中...</el-tag>
              <el-button 
                type="text" 
                size="small" 
                @click="clearVoiceText"
                :disabled="!voiceText && !interimTranscript"
              >
                清空
              </el-button>
            </div>
          </div>
          <div class="result-text">
            <el-input
              v-if="voiceText"
              v-model="voiceText"
              type="textarea"
              :rows="3"
              placeholder="语音识别结果，您可以在此编辑内容"
              resize="none"
            ></el-input>
            <p v-if="interimTranscript" class="interim-text">{{ interimTranscript }}</p>
            <p v-if="!voiceText && !interimTranscript" class="placeholder-text">
              暂无识别结果，请点击"开始录音"按钮进行语音输入
            </p>
          </div>
        </div>
      </div>
      
      <!-- 不支持语音识别的提示 -->
      <div v-else class="unsupported-section">
        <el-alert
          title="浏览器不支持语音识别"
          type="warning"
          description="请使用Chrome、Edge等现代浏览器，或手动输入旅行需求"
          show-icon
        ></el-alert>
        <el-input
          v-model="voiceText"
          type="textarea"
          :rows="4"
          placeholder="请描述您的旅行需求，例如：'我想去北京玩5天，预算5000元，喜欢美食和文化景点'"
        ></el-input>
      </div>
    </div>
    
    <!-- 行程信息预览（可编辑） -->
    <div v-if="voiceText" class="trip-preview-section">
      <el-divider content-position="left">
        <span style="font-size: 18px; font-weight: 600;">行程信息预览</span>
      </el-divider>
      
      <div class="preview-header">
        <h3>{{ tripDefaults.title }}</h3>
        <el-tag v-if="tripDefaults.preferences.length > 0" type="success">
          {{ tripDefaults.preferences.length }} 个偏好已识别
        </el-tag>
      </div>
      
      <el-form label-width="100px" class="preview-form">
        <el-form-item label="行程标题">
          <el-input v-model="tripDefaults.title" placeholder="智能生成的行程标题"></el-input>
        </el-form-item>
        <el-form-item label="目的地">
          <el-input v-model="tripDefaults.destination" placeholder="自动提取的目的地"></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker
                v-model="tripDefaults.startDate"
                type="date"
                placeholder="开始日期"
                style="width: 100%"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker
                v-model="tripDefaults.endDate"
                type="date"
                placeholder="结束日期"
                style="width: 100%"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出行人数">
              <el-input-number
                v-model="tripDefaults.companionCount"
                :min="1"
                :max="10"
                controls-position="right"
                style="width: 100%"
              ></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总预算">
              <el-input-number
                v-model="tripDefaults.budgetTotal"
                :min="0"
                :step="100"
                controls-position="right"
                style="width: 100%"
              >
                <template #append>元</template>
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="旅行偏好">
          <el-select
            v-model="tripDefaults.preferences"
            multiple
            placeholder="请选择旅行偏好"
            collapse-tags
            style="width: 100%"
          >
            <el-option label="美食" value="美食"></el-option>
            <el-option label="购物" value="购物"></el-option>
            <el-option label="文化" value="文化"></el-option>
            <el-option label="自然风景" value="自然风景"></el-option>
            <el-option label="历史古迹" value="历史古迹"></el-option>
            <el-option label="主题乐园" value="主题乐园"></el-option>
            <el-option label="艺术展览" value="艺术展览"></el-option>
            <el-option label="特种兵行程" value="特种兵行程"></el-option>
            <el-option label="轻松行程" value="轻松行程"></el-option>
            <el-option label="亲子" value="亲子"></el-option>
            <el-option label="情侣" value="情侣"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="语音输入内容（可编辑）">
          <el-input
            v-model="voiceText"
            type="textarea"
            :rows="3"
            placeholder="语音识别结果，您可以在此编辑内容"
          ></el-input>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 提交按钮 -->
    <div class="action-buttons">
      <el-button
        type="primary"
        :loading="loading"
        @click="handleCreateTrip"
        :disabled="!voiceText.trim()"
        size="large"
      >
        创建行程
      </el-button>
    </div>
  </div>
  
  <!-- 全屏灰色遮罩 -->
  <div 
    v-if="showLoadingOverlay" 
    class="loading-overlay"
  >
    <div class="loading-content">
      <el-spinner size="large" />
      <p class="loading-text">正在生成旅行计划...</p>
    </div>
  </div>
</template>

<style scoped>
/* 全屏灰色遮罩样式 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-content {
  text-align: center;
  color: white;
}

.loading-text {
  margin-top: 16px;
  font-size: 16px;
}

.voice-input-container {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.voice-input-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8e8e8;
  margin-bottom: 32px;
}

.voice-controls {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.voice-status {
  text-align: center;
}

.voice-status .el-alert {
  margin-bottom: 0;
  border-radius: 8px;
}

.voice-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.microphone-btn {
  width: 100px;
  height: 100px;
  font-size: 24px;
  border-radius: 50%;
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.microphone-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(64, 158, 255, 0.4);
}

.microphone-btn:not(.is-loading) {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 8px 25px rgba(64, 158, 255, 0.3);
  }
  50% {
    box-shadow: 0 8px 25px rgba(64, 158, 255, 0.5);
  }
  100% {
    box-shadow: 0 8px 25px rgba(64, 158, 255, 0.3);
  }
}

.voice-result {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-radius: 12px;
  padding: 24px;
  border: 2px solid #e2e8f0;
  min-height: 160px;
}

.result-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 600;
  color: #2d3748;
  font-size: 16px;
}

.result-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-text {
  min-height: 80px;
  line-height: 1.6;
}

.result-text p {
  margin: 8px 0;
  font-size: 16px;
  color: #4a5568;
}

.result-text .el-textarea {
  width: 100%;
}

.result-text .el-textarea__inner {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  line-height: 1.5;
  resize: vertical;
  min-height: 80px;
}

.result-text .el-textarea__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.interim-text {
  color: #d69e2e !important;
  font-style: italic;
  opacity: 0.9;
  background: rgba(214, 158, 46, 0.1);
  padding: 8px 12px;
  border-radius: 6px;
  border-left: 3px solid #d69e2e;
}

.placeholder-text {
  color: #a0aec0;
  font-style: italic;
  text-align: center;
  padding: 20px;
  font-size: 14px;
}

.unsupported-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
  background: #ffffff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.unsupported-section .el-alert {
  margin-bottom: 0;
  border-radius: 8px;
}

.trip-preview-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8e8e8;
  margin-top: 32px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.preview-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 20px;
  font-weight: 600;
}

.preview-form {
  margin-top: 16px;
}

.submit-section {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.submit-section .el-button {
  width: 240px;
  height: 52px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.submit-section .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

/* 响应式设计 */
.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .voice-input-container {
    padding: 16px;
  }
  
  .voice-input-section,
  .unsupported-section,
  .trip-preview-section {
    padding: 24px;
  }
  
  .voice-buttons {
    flex-direction: column;
    gap: 12px;
  }
  
  .microphone-btn {
    width: 80px;
    height: 80px;
    font-size: 20px;
  }
  
  .result-title {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .result-actions {
    align-self: flex-end;
  }
}
</style>