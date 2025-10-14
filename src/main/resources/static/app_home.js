/* app_home.js */
'use strict';

/* ==========================
   전역 엘리먼트 / 기본 설정
========================== */
const app = document.getElementById('app');
let currentView = 'home';
let homeHTML = ''; // Store initial home HTML
if (app) {
  app.dataset.currentView = 'home';
  // Save the initial home view HTML
  homeHTML = app.innerHTML;
}

// 엔드포인트 (백엔드 실제 API와 매칭)
const QUOTE_API = '/api/v1/affirmations/main';  // 문제와 톤을 바탕으로 생성한 '읽을 문구'를 이 서버에서 불러옴
const TRANSCRIPT_API = '/api/v1/speech/logs';   // 어떤 사용자가 무슨 문구를 읽었는지 이 서버로 전송
const ASR_API = '/api/v1/speech/recognize';

/* ==========================
   뷰 전환 / 라우팅
========================== */
document.addEventListener('click', (e) => {
  const btn = e.target.closest('button');
  if (!btn) return;

  const view = btn.dataset.view;

  // 읽기 시작! → 현재 문장 저장 후 read 뷰 AJAX 로드
  if (view === 'read') {
    const currentQuoteEl =
      document.getElementById('quoteText') || document.querySelector('.bubble > div');
    if (currentQuoteEl) {
      localStorage.setItem('currentQuote', currentQuoteEl.innerHTML);
    }

    // Track sentence source: custom sentences don't need bookmark button
    const source = app.dataset.currentView || 'home';
    localStorage.setItem('sentenceSource', source);

    loadView('read');
    return;
  }

  if (view) {
    e.preventDefault();
    loadView(view);
  }
});

// view → 파일 매핑
function viewToUrl(viewName){
  switch(viewName){
    case 'bookmark': return 'views/bookmark.html';
    case 'custom'  : return 'views/custom.html';
    case 'read'    : return 'views/read.html';
    case 'correct'    : return 'views/correct.html';
    default        : return null;
  }
}

async function loadView(viewName){
  const targetView = viewName || 'home';

  // Save music state when leaving home view (for ALL non-home destinations)
  if (bgMusic && currentView === 'home' && viewName !== 'home' && viewName) {
    const wasPlaying = !bgMusic.paused;
    sessionStorage.setItem('musicWasPlaying', wasPlaying ? 'true' : 'false');
  }

  await transitionOut();

  try {
    let html;

    // 'home' view: restore saved HTML instead of fetching
    if (viewName === 'home' || !viewName) {
      html = homeHTML;
    } else {
      // Other views: fetch from URL
      const url = viewToUrl(viewName);
      if (!url) {
        throw new Error('Invalid view name: ' + viewName);
      }
      const res = await fetch(url, { cache: 'no-store' });
      if (!res.ok) throw new Error('HTTP ' + res.status);
      html = await res.text();
    }

    app.innerHTML = html;

    currentView = targetView;
    app.dataset.currentView = targetView;

    // Auto-pause background music during speech recognition to avoid interference
    if (bgMusic && musicIcon && musicToggle) {
      if (viewName === 'read') {
        // Pause music when entering read view (recording voice)
        bgMusic.pause();
        musicIcon.innerHTML = `<polygon points="6,4 20,12 6,20" />`;
        musicToggle.setAttribute('aria-label', '음악 재생');
      } else if (viewName === 'home' || !viewName) {
        // Restore music state when returning to home
        const wasPlaying = sessionStorage.getItem('musicWasPlaying') === 'true';
        if (wasPlaying) {
          bgMusic.play().catch(err => console.log('Music autoplay prevented:', err));
          musicIcon.innerHTML = `
            <rect x="6" y="4" width="4" height="16"></rect>
            <rect x="14" y="4" width="16"></rect>
          `;
          musicToggle.setAttribute('aria-label', '음악 일시정지');
        }
      }
    }

    // 각 뷰별 초기화
    if (viewName === 'read') {
      const saved = localStorage.getItem('currentQuote');
      const target = app.querySelector('#readQuote');
      if (target) {
        target.innerHTML = saved && saved.trim() ? saved : '문장을 불러오지 못했습니다.';
      }
      initReadVoice();
    }
    if (viewName === 'bookmark') {
      initBookmarkView();
    }
    if (viewName === 'custom') {
      initCustomView();
    }
    if (viewName === 'correct') {
      initCorrectView();
    }
    if (viewName === 'home' || !viewName) {
      // Check if affirmations cache was cleared (e.g., after completing a reading)
      const cached = sessionStorage.getItem('generated_affirmations');
      if (!cached) {
        // Cache was cleared, fetch new affirmations
        loadInitialQuote();
      } else {
        // Restore current affirmation display
        const quoteEl = document.getElementById('quoteText');
        if (quoteEl && GENERATED_AFFIRMATIONS.length > 0) {
          const currentIdx = affirmationIdx > 0 ? affirmationIdx - 1 : GENERATED_AFFIRMATIONS.length - 1;
          quoteEl.innerHTML = GENERATED_AFFIRMATIONS[currentIdx % GENERATED_AFFIRMATIONS.length].replace(/\n/g, '<br/>');
        }
      }
    }

    transitionIn();
  } catch (err) {
    app.innerHTML = `
      <section style="padding:24px">
        <div class="bubble">
          뷰 <b>${viewName}</b>를 불러오는 중 오류가 발생했어요.
        </div>
      </section>`;
    console.error('Load failed:', err);
    currentView = targetView;
    app.dataset.currentView = targetView;
    transitionIn();
  }
}

function transitionOut(){
  return new Promise((resolve)=>{
    app.classList.add('leaving');
    app.classList.remove('entered','entering');
    setTimeout(resolve, 250);
  });
}
function transitionIn(){
  app.classList.add('entering');
  requestAnimationFrame(()=>{
    requestAnimationFrame(()=>{
      app.classList.remove('leaving','entering');
      app.classList.add('entered');
    });
  });
}

/* ==========================
   헤더: 쿠키 → 이름/아바타
========================== */
function getCookie(name) {
  const m = document.cookie.match(
    new RegExp('(?:^|; )' + name.replace(/([.$?*|{}()[\]\/+^])/g, '\\$1') + '=([^;]*)')
  );
  return m ? decodeURIComponent(m[1]) : null;
}

async function setUsernameFromAPI(){
  const el = document.getElementById('username');
  if (!el) return;
  
  try {
    const response = await fetch('/api/users/me/info', {
      credentials: 'same-origin'
    });
    
    if (response.ok) {
      const data = await response.json();
      el.textContent = data.nickname ? `${data.nickname}님` : 'USER님';
    } else {
      el.textContent = 'USER님';
    }
  } catch (error) {
    console.error('Failed to fetch user info:', error);
    el.textContent = 'USER님';
  }
}
function setAvatarFromCookie(){
  const avatarKeys = ['user_avatar','profileImage','avatar'];
  let url = null;
  for (const k of avatarKeys){
    url = getCookie(k);
    if (url) break;
  }
  const avatarEl = document.getElementById('profileAvatar');
  if (!avatarEl) return;

  if (url && /^https?:\/\//i.test(url)){
    avatarEl.innerHTML = '';
    const img = document.createElement('img');
    img.src = url;
    img.alt = '프로필 이미지';
    img.style.width = '100%';
    img.style.height = '100%';
    img.style.borderRadius = '50%';
    img.style.objectFit = 'cover';
    avatarEl.appendChild(img);
  }
}

// 초기화(헤더)
(function initHeader(){
  setUsernameFromAPI();
  setAvatarFromCookie();
})();

/* ==========================
   음악 재생/일시정지 토글
========================== */
// Background music: Nature sounds (birds chirping, stream flowing, etc.)
const musicToggle = document.getElementById('musicToggle');
const musicIcon = document.getElementById('musicIcon');
const bgMusic = document.getElementById('bgMusic');

// Set background music volume to 50% for pleasant ambiance
if (bgMusic) {
  bgMusic.volume = 0.5;
}

if (musicToggle && musicIcon && bgMusic) {
  musicToggle.addEventListener('click', () => {
    if (bgMusic.paused) {
      bgMusic.play();
      musicIcon.innerHTML = `
        <rect x="6" y="4" width="4" height="16"></rect>
        <rect x="14" y="4" width="4" height="16"></rect>
      `;
      musicToggle.setAttribute('aria-label', '음악 일시정지');
    } else {
      bgMusic.pause();
      musicIcon.innerHTML = `<polygon points="6,4 20,12 6,20" />`;
      musicToggle.setAttribute('aria-label', '음악 재생');
    }
  });
}

/* ==========================
   NEXT: 서버 문장 로드 + 폴백
========================== */
// Generated affirmations from API (will be populated on load)
let GENERATED_AFFIRMATIONS = [];

// True fallback quotes (only used if API completely fails)
const FALLBACK_QUOTES = [
  '"어려워도 괜찮아.<br/>나는 희망을 찾을 수 있을 거야!"',
  '"지금의 나도 충분히 잘하고 있어."',
  '"어둠 속에서도 작은 빛은 늘 있어."'
];
let affirmationIdx = 0;

function getQuoteEl(){
  return document.getElementById('quoteText');
}

function setQuote(text){
  const el = getQuoteEl();
  if (!el) return;
  el.innerHTML = text.replace(/\n/g, '<br/>');
}
function showNextAffirmation(){
  // Use generated affirmations if available, otherwise fallback quotes
  const quotes = GENERATED_AFFIRMATIONS.length > 0 ? GENERATED_AFFIRMATIONS : FALLBACK_QUOTES;
  setQuote(quotes[affirmationIdx]);
  affirmationIdx = (affirmationIdx + 1) % quotes.length;

  // Save current index to sessionStorage
  if (GENERATED_AFFIRMATIONS.length > 0) {
    sessionStorage.setItem('affirmation_idx', String(affirmationIdx));
  }
}
function fetchWithTimeout(url, opts={}, ms=10000){
  return Promise.race([
    fetch(url, opts),
    new Promise((_, rej) => setTimeout(() => rej(new Error('timeout')), ms))
  ]);
}

// ✅ 초기 진입 시 자동으로 한 번 불러오기
async function loadInitialQuote(){
  console.log('loadInitialQuote() called'); // Debug log

  // 먼저 sessionStorage에서 캐시된 affirmations 확인
  const cached = sessionStorage.getItem('generated_affirmations');
  const cachedIdx = sessionStorage.getItem('affirmation_idx');

  if (cached) {
    try {
      GENERATED_AFFIRMATIONS = JSON.parse(cached);
      affirmationIdx = cachedIdx ? parseInt(cachedIdx, 10) : 0;

      if (GENERATED_AFFIRMATIONS.length > 0) {
        setQuote(GENERATED_AFFIRMATIONS[affirmationIdx % GENERATED_AFFIRMATIONS.length]);
        console.log('Loaded cached affirmations:', GENERATED_AFFIRMATIONS); // Debug log
        return;
      }
    } catch (e) {
      console.warn('Failed to parse cached affirmations:', e);
    }
  }

  // 캐시가 없으면 API 호출
  setQuote('불러오는 중…');

  try {
    const res = await fetchWithTimeout(QUOTE_API, {
      method: 'GET',
      headers: { 'Accept': 'application/json' },
      cache: 'no-store',
      credentials: 'same-origin'
    });
    if (!res.ok) throw new Error('bad-status ' + res.status);
    const data = await res.json().catch(() => ({}));

    // Extract the 3 generated affirmations from MainAffirmationResponseDto
    console.log('API response:', data); // Debug log

    if (data && (data.affirmation1 || data.affirmation2 || data.affirmation3)) {
      GENERATED_AFFIRMATIONS = [
        data.affirmation1,
        data.affirmation2,
        data.affirmation3
      ].filter(Boolean); // Remove any null/undefined values

      // sessionStorage에 저장
      sessionStorage.setItem('generated_affirmations', JSON.stringify(GENERATED_AFFIRMATIONS));
      sessionStorage.setItem('affirmation_idx', '0');

      console.log('Generated affirmations:', GENERATED_AFFIRMATIONS); // Debug log

      // Set the first affirmation
      if (GENERATED_AFFIRMATIONS.length > 0) {
        setQuote(GENERATED_AFFIRMATIONS[0]);
        affirmationIdx = 1; // Next will be affirmation2
        console.log('Set first affirmation:', GENERATED_AFFIRMATIONS[0]); // Debug log
        return;
      }
    }

    // Fallback if no affirmations in response
    showNextAffirmation();
  } catch (e) {
    showNextAffirmation();
    console.error('initial quote load failed:', e);
  }
}

// Next 버튼용 - 생성된 affirmations 순환하거나 새로 로드
async function loadNextQuote(btn){
  btn.disabled = true;
  btn.setAttribute('aria-busy', 'true');
  
  // If we have generated affirmations, cycle through them first
  if (GENERATED_AFFIRMATIONS.length > 0) {
    showNextAffirmation();
    btn.disabled = false;
    btn.removeAttribute('aria-busy');
    return;
  }
  
  // Otherwise, try to load new affirmations from API
  try {
    const res = await fetchWithTimeout(QUOTE_API, {
      method: 'GET',
      headers: { 'Accept': 'application/json' },
      cache: 'no-store',
      credentials: 'same-origin'
    });
    if (!res.ok) throw new Error('bad-status ' + res.status);
    const data = await res.json().catch(() => ({}));
    
    // Extract the 3 generated affirmations from MainAffirmationResponseDto
    if (data && (data.affirmation1 || data.affirmation2 || data.affirmation3)) {
      GENERATED_AFFIRMATIONS = [
        data.affirmation1,
        data.affirmation2,
        data.affirmation3
      ].filter(Boolean);

      // sessionStorage에 저장
      sessionStorage.setItem('generated_affirmations', JSON.stringify(GENERATED_AFFIRMATIONS));

      if (GENERATED_AFFIRMATIONS.length > 0) {
        setQuote(GENERATED_AFFIRMATIONS[0]);
        affirmationIdx = 1;
        sessionStorage.setItem('affirmation_idx', '1');
        return;
      }
    }
    
    showNextAffirmation();
  } catch (e) {
    showNextAffirmation();
    console.error('quote load failed:', e);
  } finally {
    btn.disabled = false;
    btn.removeAttribute('aria-busy');
  }
}

// next 버튼 핸들러(유지)
document.addEventListener('click', (e) => {
  const btn = e.target.closest('button.next');
  if (!btn) return;
  if (currentView !== 'home') return;
  loadNextQuote(btn);
});

// 🔔 페이지 로드(혹은 스크립트 실행) 시 즉시 1회 호출
// 스크립트를 <head defer>로 넣었거나 </body> 직전에 넣었다면 아래 한 줄이면 충분
loadInitialQuote();
// 만약 타이밍 이슈가 있다면 아래처럼 바꿔도 됨:
// window.addEventListener('DOMContentLoaded', loadInitialQuote);


/* ==========================
   음성 인식 / 서버 전송 유틸
========================== */
function getSessionId(){
  let sid = localStorage.getItem('voice_session_id');
  if (!sid) {
    sid = 'vs_' + Math.random().toString(36).slice(2) + Date.now().toString(36);
    localStorage.setItem('voice_session_id', sid);
  }
  return sid;
}

const TRANSCRIPT_QUEUE_KEY = 'pending_transcripts_v1';
function queuePush(payload){
  const arr = JSON.parse(localStorage.getItem(TRANSCRIPT_QUEUE_KEY) || '[]');
  arr.push(payload);
  localStorage.setItem(TRANSCRIPT_QUEUE_KEY, JSON.stringify(arr));
}
async function flushQueue(){
  const arr = JSON.parse(localStorage.getItem(TRANSCRIPT_QUEUE_KEY) || '[]');
  if (!arr.length) return;
  const rest = [];
  for (const p of arr) {
    try {
      await postJSON(TRANSCRIPT_API, p);
    } catch {
      rest.push(p);
    }
  }
  localStorage.setItem(TRANSCRIPT_QUEUE_KEY, JSON.stringify(rest));
}
window.addEventListener('online', flushQueue);

async function postJSON(url, body, tries=2){
  const res = await fetch(url, {
    method: 'POST',
    headers: {'Content-Type':'application/json'},
    credentials: 'same-origin',
    cache: 'no-store',
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    if (tries > 0) return postJSON(url, body, tries-1);
    throw new Error('bad-status ' + res.status);
  }
  return res.json().catch(() => ({}));
}

async function sendTranscript({ text, is_final, quote, extra }){
  const payload = {
    text,
    is_final: !!is_final,
    quote: quote || null,
    user: getCookie('user_name') || getCookie('username') || null,
    session_id: getSessionId(),
    ts: new Date().toISOString(),
    ...extra
  };

  if (!navigator.onLine) { queuePush(payload); return; }

  try {
    await postJSON(TRANSCRIPT_API, payload);
  } catch (e) {
    queuePush(payload);
  }
}

/* ==========================
   read 전용: 음성 인식 초기화 (클라이언트 비교 + 분기)
========================== */
function initReadVoice(){
  const btn = app.querySelector('#micToggle');          // 꽃 버튼
  const flower = app.querySelector('#flowerIcon');      // <img id="flowerIcon" ...>
  const transcriptEl = app.querySelector('#transcript');
  const readQuoteRaw = (app.querySelector('#readQuote')?.innerText || '').trim();
  if (!btn) return;

  let isListening = false;
  let currentRetryCount = 0;
  let mediaRecorder;
  let audioChunks = [];
  let activeStream = null;

  // ---------- 유틸: 텍스트 정규화 & 유사도 ----------
  const normalize = (s) => {
    if (!s) return '';
    return s
      .replace(/["""']/g, '')        // 따옴표 제거
      .replace(/<br\s*\/?>/gi, ' ')  // (예방적) BR 제거
      .replace(/\s+/g, ' ')          // 공백 정리
      .replace(/[.,!?;:()\[\]{}~\-_/\\]/g, '') // 구두점 제거(필요시 조정)
      .trim()
      .toLowerCase()
      .normalize('NFKC');
  };

  // 레벤슈타인 거리
  const levenshtein = (a, b) => {
    const m = a.length, n = b.length;
    if (m === 0) return n;
    if (n === 0) return m;
    const dp = new Array(n + 1);
    for (let j = 0; j <= n; j++) dp[j] = j;
    for (let i = 1; i <= m; i++) {
      let prev = dp[0]; dp[0] = i;
      for (let j = 1; j <= n; j++) {
        const tmp = dp[j];
        dp[j] = Math.min(
          dp[j] + 1,                   // deletion
          dp[j - 1] + 1,               // insertion
          prev + (a[i - 1] === b[j - 1] ? 0 : 1) // substitution
        );
        prev = tmp;
      }
    }
    return dp[n];
  };

  const isMatch = (expected, spoken) => {
    const A = normalize(expected);
    const B = normalize(spoken);
    if (!A || !B) return false;
    if (A === B) return true;
    // 포함(부분 일치) 허용
    if (A.includes(B) || B.includes(A)) return true;
    // 유사도 90% 이상 허용
    const dist = levenshtein(A, B);
    const maxLen = Math.max(A.length, B.length);
    const sim = 1 - dist / Math.max(1, maxLen);
    return sim >= 0.9;
  };

  // ---------- 모달 ----------
  function showResultModal(ok, onRetry){
    // 기존 모달 제거
    const old = document.getElementById('read-result-modal');
    if (old) old.remove();

    const wrap = document.createElement('div');
    wrap.id = 'read-result-modal';
    wrap.style.cssText = `
      position: fixed; inset: 0; z-index: 9999;
      background: rgba(0,0,0,.45); display: grid; place-items: center;
    `;
    wrap.innerHTML = `
      <div style="background:#fff; color:#222; width:min(90vw,360px); border-radius:16px; padding:18px; box-shadow:0 10px 30px rgba(0,0,0,.25); text-align:center;">
        <div style="font-weight:700; font-size:1rem; margin-bottom:8px;">
          ${ok ? '정확해요! 잘 읽었어요 🌟' : '조금만 더 정확히 읽어볼까요?'}
        </div>
        ${ok ? '' : '<div style="font-size:.92rem; color:#555; margin-bottom:14px;">다시 시도하거나 홈으로 이동할 수 있어요.</div>'}
        <div style="display:flex; gap:8px; justify-content:center; margin-top:6px; flex-wrap:wrap;">
          ${ok ? `
            <button id="modal-ok" style="padding:8px 14px; border:none; background:#1a7a29; color:#fff; border-radius:999px; cursor:pointer;">계속</button>
          ` : `
            <button id="modal-retry" style="padding:8px 14px; border:none; background:#1a7a29; color:#fff; border-radius:999px; cursor:pointer;">다시 시도</button>
            <button id="modal-home"  style="padding:8px 14px; border:1px solid #ddd; background:#fff; color:#333; border-radius:999px; cursor:pointer;">홈으로</button>
          `}
        </div>
      </div>
    `;
    document.body.appendChild(wrap);

    if (ok) {
      wrap.querySelector('#modal-ok').addEventListener('click', () => {
        wrap.remove();
        // 일치면 correct.html로
        loadView('correct');
      });
    } else {
      wrap.querySelector('#modal-retry').addEventListener('click', () => {
        wrap.remove();
        onRetry && onRetry();
      });
      wrap.querySelector('#modal-home').addEventListener('click', () => {
        // 홈으로 (초기 화면 복귀)
        window.location.reload();
      });
    }
  }

  // ---------- 상태(반짝임) ----------
  const setState = (on) => {
    isListening = on;
    if (flower) flower.classList.toggle('glowing', on);
  };

  // ---------- 폴백: Web Speech API ----------
  function tryWebSpeechFallback() {
    const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SR) {
      showResultModal(false, () => setState(false));
      console.warn('음성 인식을 지원하지 않는 브라우저입니다.');
      return;
    }

    const recog = new SR();
    recog.lang = 'ko-KR';
    recog.interimResults = true;
    recog.continuous = true;

    let finalText = '';

    recog.onresult = (e) => {
      for (let i = e.resultIndex; i < e.results.length; i++) {
        const t = e.results[i][0].transcript;
        if (e.results[i].isFinal) finalText += t + ' ';
      }
      if (transcriptEl) transcriptEl.textContent = finalText.trim();
    };

    recog.onend = () => {
      const ok = isMatch(readQuoteRaw, finalText);
      setState(false);
      showResultModal(ok, () => { finalText = ''; tryWebSpeechFallback(); setState(true); });
    };

    try {
      recog.start();
      setState(true);
    } catch (error) {
      console.error('Web Speech API fallback failed:', error);
      showResultModal(false, () => setState(false));
    }
  }

  // ---------- Clova STT API 우선 ----------
  btn.addEventListener('click', async () => {
    if (!isListening) {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        activeStream = stream;
        audioChunks = [];

        const options = { mimeType: 'audio/webm;codecs=opus' };
        mediaRecorder = new MediaRecorder(stream, options);
        mediaRecorder.ondataavailable = (e) => audioChunks.push(e.data);

        mediaRecorder.onstop = async () => {
          if (activeStream) {
            activeStream.getTracks().forEach(track => track.stop());
            activeStream = null;
          }
          const audioBlob = new Blob(audioChunks, { type: 'audio/wav' });
          
          console.log('🎤 Audio recorded:', {
            size: audioBlob.size,
            type: audioBlob.type,
            originalSentence: readQuoteRaw,
            apiEndpoint: ASR_API
          });
          
          try {
            const formData = new FormData();
            formData.append('audioFile', audioBlob, 'speech.wav');
            formData.append('originalSentence', readQuoteRaw);
            formData.append('retryCount', currentRetryCount.toString());
            
            console.log('📡 Sending to Clova STT... (retry count:', currentRetryCount, ')');
            const response = await fetch(ASR_API, {
              method: 'POST',
              body: formData,
              credentials: 'same-origin'
            });
            
            console.log('📥 Response:', response.status, response.statusText);
            
            if (response.ok) {
              const result = await response.json();
              console.log('✅ Clova result:', result);
              
              if (result.success) {
                // 성공: 재시도 카운트 리셋
                currentRetryCount = 0;
                setState(false);
                showResultModal(true, () => { setState(false); btn.click(); });
              } else if (result.needRetry && !result.maxRetryReached) {
                // 재시도 필요: 카운트 증가 후 재시도 버튼 표시
                currentRetryCount = result.retryCount || (currentRetryCount + 1);
                console.log('🔄 Retry needed. New retry count:', currentRetryCount);
                setState(false);
                showResultModal(false, () => { 
                  setState(false); 
                  // 재시도 클릭 시 음성 녹음 다시 시작
                  setTimeout(() => btn.click(), 100);
                });
              } else {
                // 최대 재시도 도달 또는 기타 실패
                currentRetryCount = 0; // 리셋
                setState(false);
                showResultModal(false, () => setState(false));
              }
            } else {
              const errorText = await response.text();
              console.error('❌ HTTP Error:', response.status, errorText);
              setState(false);
              showResultModal(false, () => setState(false));
            }
          } catch (error) {
            console.error('❌ Clova STT failed, trying Web Speech API fallback:', error);
            tryWebSpeechFallback();
          }
        };

        mediaRecorder.onerror = (e) => {
          console.error('MediaRecorder error:', e);
          if (activeStream) {
            activeStream.getTracks().forEach(track => track.stop());
            activeStream = null;
          }
          setState(false);
        };        
        mediaRecorder.start();
        setState(true);
      } catch (error) {
        console.error('Microphone access failed, trying Web Speech API fallback:', error);
        tryWebSpeechFallback();
      }
    } else {
      setState(false);
      if (mediaRecorder && mediaRecorder.state !== 'inactive') {
        mediaRecorder.stop();
      }
    }
  });
}

/* ─────────────────────────────────────────────
   correct 뷰: 문장 주입 + 북마크 토글 + 랜덤 응원 + 홈버튼
───────────────────────────────────────────── */

// 서버 엔드포인트: 백엔드 실제 API와 매칭
const BOOKMARK_EXISTS_API = '/api/v1/bookmarks/check?sentence=';  // 해당 문구가 북마크에 존재하는지 체크하는 서버
const BOOKMARK_ADD_API    = '/api/v1/bookmarks/add';             // 해당 문구를 북마크에 등록하는 서버
const BOOKMARK_DEL_API    = '/api/v1/bookmarks/remove';          // 해당 문구를 북마크에서 지우는 서버

/* 🔐 JWT 토큰 → Authorization 헤더 자동 부착 공통 래퍼 */
function getJwtToken() {
  // 우선순위: localStorage → sessionStorage → (읽을 수 있는) 쿠키
  const ls = localStorage.getItem('jwt') || localStorage.getItem('access_token');
  if (ls) return ls;

  const ss = sessionStorage.getItem('jwt') || sessionStorage.getItem('access_token');
  if (ss) return ss;

  // 쿠키명이 token일 때 (HttpOnly면 JS로 못 읽음)
  const m = document.cookie.match(/(?:^|;\s*)token=([^;]+)/);
  if (m) return decodeURIComponent(m[1]);

  return null;
}

async function fetchJSONWithAuth(url, { method='GET', headers={}, body=null } = {}) {
  const token = getJwtToken();
  const h = new Headers(headers);
  if (token && !h.has('Authorization')) {
    h.set('Authorization', `Bearer ${token}`);
  }

  const init = { method, headers: h, cache: 'no-store' };
  if (body && typeof body === 'object' && !(body instanceof FormData)) {
    h.set('Content-Type', 'application/json');
    init.body = JSON.stringify(body);
  } else if (body) {
    init.body = body;
  }

  const res = await fetch(url, init);
  if (res.status === 401 || res.status === 403) throw new Error(`auth-failed ${res.status}`);
  if (!res.ok) {
    const txt = await res.text().catch(()=> '');
    throw new Error(`HTTP ${res.status} ${txt}`);
  }
  try { return await res.json(); } catch { return {}; }
}

/* SVG 아이콘 스왑 */
function setBookmarkIcon(active){
  const icon = app.querySelector('#bookmarkIcon');
  if (!icon) return;
  if (active) {
    // 활성: 꽉 찬 별
    icon.innerHTML = '<path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z" fill="#f4c430"></path>';
  } else {
    // 비활성: 테두리 별
    icon.innerHTML = '<path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z" fill="none" stroke="#f4c430" stroke-width="2" stroke-linejoin="round"></path>';
  }
}

/* JWT 인증 붙여서 서버와 통신 */
async function checkBookmark(text){
  const url = BOOKMARK_EXISTS_API + encodeURIComponent(text);
  try {
    const data = await fetchJSONWithAuth(url, { method: 'GET' });

    if (typeof data === 'boolean') return data;
    if (data && typeof data === 'object') {
      if (typeof data.isBookmarked === 'boolean') return data.isBookmarked;
      if (typeof data.bookmarked === 'boolean') return data.bookmarked;
      if (typeof data.exists === 'boolean') return data.exists;
    }
    return false;
  } catch (e) {
    console.error('checkBookmark failed:', e);
    return false; // 실패 시 기본값
  }
}

async function addBookmark(text){
  await fetchJSONWithAuth(BOOKMARK_ADD_API, {
    method: 'POST',
    body: { sentence: text, tone: 'normal' } // 백엔드 BookmarkRequestDto 맞춤
  });
}

async function removeBookmark(text){
  // 백엔드 API는 query parameter 사용
  const url = BOOKMARK_DEL_API + '?sentence=' + encodeURIComponent(text);
  await fetchJSONWithAuth(url, {
    method: 'DELETE'
  });
}

function pickRandomEncourage(){
  const LINES = [
    '멋져요! 오늘도 해냈네요!',
    '성공적으로 해냈어요! 긍정 에너지가 오늘 하루를 이끌어갈 거예요.',
    '훌륭해요! 마음속 긍정의 씨앗이 무럭무럭 자라고 있어요.',
    '해냈군요! 오늘도 스스로에게 좋은 습관을 선물했어요.',
    '성공! 당신이 믿는 대로 이루어질 거예요.',
    '잘했어요! 지금 한 마디가 당신을 더 강하게 만듭니다.',
    '축하해요! 오늘을 위한 긍정 에너지를 가득 채웠습니다.',
    '자신감이 느껴져요! 오늘도 하루를 멋지게 만들어봐요.',
    '성공! 이 에너지가 잠재의식 속에 깊이 새겨졌어요.',
    '잘했어요! 다음 문장도 기대되는데요?',
    '정말 좋아요! 듣는 저도 기분이 좋아지네요.',
    '성공! 이제 이 느낌을 그대로 즐겨보세요.',
    '퍼펙트! 오늘도 스스로에게 칭찬 한 번!',
    '성공이에요! 목소리에 힘이 실려 있네요.',
    '완벽해요! 목소리가 확신에 가득 차 있네요.'
  ];
  return LINES[Math.floor(Math.random()*LINES.length)];
}

function initCorrectView(){
  // 1) 문장 주입 (read에서 저장한 값 재사용)
  const saved = localStorage.getItem('currentQuote');
  const target = app.querySelector('#correctQuote');
  if (target) {
    target.innerHTML = saved && saved.trim() ? saved : '문장을 불러오지 못했습니다.';
  }

  // 2) 랜덤 응원 문구
  const encEl = app.querySelector('#encourageText');
  if (encEl) encEl.textContent = pickRandomEncourage();

  // 3) 홈으로 - 읽기 성공 후 새 affirmations 생성
  const homeBtn = app.querySelector('#goHome');
  if (homeBtn) {
    homeBtn.addEventListener('click', () => {
      // Clear sessionStorage to force new affirmation generation
      sessionStorage.removeItem('generated_affirmations');
      sessionStorage.removeItem('affirmation_idx');
      // Use loadView instead of reload to preserve music state
      loadView('home');
    });
  }

  // 4) 북마크 초기 상태 + 토글
  // ⚠️ Custom sentences don't need bookmark button (already saved)
  const sentenceSource = localStorage.getItem('sentenceSource') || 'home';
  const toggleBtn = app.querySelector('#bookmarkToggle');

  if (sentenceSource === 'custom') {
    // Hide bookmark button entirely for custom sentences
    if (toggleBtn) {
      toggleBtn.style.display = 'none';
    }
    return; // Exit early - no bookmark logic needed
  }

  const quotePlain = (target?.innerText || '').trim();
  if (!toggleBtn || !quotePlain) return;

  // 기본은 해제 상태로 표시
  toggleBtn.setAttribute('aria-pressed', 'false');
  setBookmarkIcon(false);

  // 초기 상태 체크
  (async () => {
    const exists = await checkBookmark(quotePlain);
    toggleBtn.setAttribute('aria-pressed', exists ? 'true' : 'false');
    setBookmarkIcon(exists);
  })();

  // 토글
  toggleBtn.addEventListener('click', async () => {
    const nowActive = toggleBtn.getAttribute('aria-pressed') === 'true';

    // 낙관적 UI
    toggleBtn.setAttribute('aria-pressed', nowActive ? 'false' : 'true');
    setBookmarkIcon(!nowActive);

    try {
      if (nowActive) {
        await removeBookmark(quotePlain);  // 삭제
      } else {
        await addBookmark(quotePlain);     // 등록
      }
    } catch (e) {
      console.error('bookmark toggle failed:', e);
      // 실패 시 롤백
      toggleBtn.setAttribute('aria-pressed', nowActive ? 'true' : 'false');
      setBookmarkIcon(nowActive);

      // 간단 토스트
      const old = document.getElementById('bm-toast'); if (old) old.remove();
      const toast = document.createElement('div');
      toast.id = 'bm-toast';
      toast.textContent = '북마크 동기화에 실패했어요. 네트워크/로그인을 확인해 주세요.';
      toast.style.cssText = 'position:fixed;left:50%;bottom:24px;transform:translateX(-50%);background:#222;color:#fff;padding:10px 14px;border-radius:999px;font-size:.9rem;z-index:9999;';
      document.body.appendChild(toast);
      setTimeout(()=>toast.remove(), 2000);
    }
  });
}

function normalizeSentence(entry) {
  if (entry == null) return '';
  if (typeof entry === 'string') return entry;
  if (typeof entry === 'object') {
    if (typeof entry.sentence === 'string') return entry.sentence;
    if (typeof entry.text === 'string') return entry.text;
    if (typeof entry.content === 'string') return entry.content;
  }
  return String(entry ?? '');
}

function initBookmarkView(){
  const quoteEl = app.querySelector('#quoteText');      // 북마크 문장을 표시할 곳
  const nextBtn = app.querySelector('.bubble .next');   // 다음 북마크
  const ctaEl   = app.querySelector('.cta');            // 버튼 영역
  const readBtn = app.querySelector('[data-view="read"]');

  if (!quoteEl) return;

  // 상태
  let bookmarks = []; // 북마크 항목 배열
  let idx = 0;

  // 렌더
  const render = () => {
    if (!bookmarks.length) {
      quoteEl.innerHTML = '저장된 북마크가 없어요.';
      if (nextBtn) nextBtn.disabled = true;

      // 👉 읽기 버튼 대신 홈 버튼 표시
      if (ctaEl) {
        ctaEl.innerHTML = `
          <button type="button" data-view="home">홈으로</button>
        `;
      }
      return;
    }

    if (nextBtn) nextBtn.disabled = (bookmarks.length <= 1);

    const current = bookmarks[idx] ?? null;
    const text = current && typeof current.sentence === 'string' ? current.sentence.trim() : '';
    quoteEl.innerHTML = text ? text.replace(/\n/g, '<br/>') : '';

      // 👉 저장된 북마크가 있으면 읽기/홈 버튼으로 복원
      if (ctaEl) {
        ctaEl.innerHTML = `
          <button type="button" data-view="read" style="padding:8px 12px;">읽기 시작!</button>
          <button type="button" data-view="home" style="padding:8px 12px; margin-left:8px;">홈으로</button>
        `;
      }

  };

  // 북마크 불러오기
  (async () => {
    quoteEl.innerHTML = '불러오는 중…';

    try {
      const data = await fetchJSONWithAuth(BOOKMARK_LIST_ME_API, { method: 'GET' });
      const items = Array.isArray(data?.items) ? data.items
                  : Array.isArray(data)        ? data
                  : [];

      bookmarks = items.map(x => ({
        id: x.id ?? x.bookmarkId ?? x._id ?? null,
        sentence: normalizeSentence(x),
        tone: typeof x.tone === 'string' && x.tone.trim().length ? x.tone : 'normal',
        bookmarked: (x.isBookmarked === true) || (x.bookmarked === true),
      })).filter(entry => entry.sentence.length);

    } catch (e) {
      console.error('bookmark load failed:', e);
      bookmarks = [];
    } finally {
      render();
    }
  })();

  // 다음 버튼: 북마크 순환
  if (nextBtn) {
    nextBtn.addEventListener('click', (event) => {
      event.stopPropagation();
      if (!bookmarks.length) return;
      idx = (idx + 1) % bookmarks.length;
      render();
    });
  }
  // 읽기 시작! → 기존 전역 핸들러가 #quoteText.innerHTML을 localStorage에 저장하고 read.html 로드
  // (이미 app_home.js에 구현되어 있으므로 별도 처리 불필요)
  // 단, 혹시 커스텀 저장을 강제하고 싶다면 아래 주석 해제:
  /*
  if (readBtn) {
    readBtn.addEventListener('click', () => {
      localStorage.setItem('currentQuote', quoteEl.innerHTML);
      loadView('read');
    });
  }
  */
}

// ===== 북마크/커스텀문장 API 엔드포인트 =====
const BOOKMARK_LIST_ME_API   = '/api/v1/bookmarks';  // JWT 인증
const BOOKMARK_LIST_BYID_API = (uid) => `/api/v1/bookmarks?userId=${uid}`;  // 쿠키 id 기반
const CUSTOM_SENTENCE_API    = '/api/v1/custom-sentences';  // JWT 인증

/**
 * UR-USER-028: 커스텀 문장 읽기 뷰 초기화
 * 북마크 읽기와 동일한 패턴: 문장을 하나씩 표시하고 순환
 */
function initCustomView() {
  const quoteEl = app.querySelector('#quoteText');      // 커스텀 문장을 표시할 곳
  const nextBtn = app.querySelector('.bubble .next');   // 다음 커스텀 문장
  const ctaEl   = app.querySelector('.cta');            // 버튼 영역
  const readBtn = app.querySelector('[data-view="read"]');

  if (!quoteEl) return;

  // 상태
  let customSentences = []; // 커스텀 문장 배열
  let idx = 0;

  // 렌더
  const render = () => {
    if (!customSentences.length) {
      quoteEl.innerHTML = '저장된 커스텀 문장이 없어요.';
      if (nextBtn) nextBtn.disabled = true;

      // 읽기 버튼 대신 홈 버튼 표시
      if (ctaEl) {
        ctaEl.innerHTML = `
          <button type="button" data-view="home">홈으로</button>
        `;
      }
      return;
    }

    if (nextBtn) nextBtn.disabled = (customSentences.length <= 1);

    const current = customSentences[idx] ?? null;
    const text = current && typeof current.sentence === 'string' ? current.sentence.trim() : '';
    quoteEl.innerHTML = text ? text.replace(/\n/g, '<br/>') : '';

    // 저장된 커스텀 문장이 있으면 읽기/홈 버튼으로 복원
    if (ctaEl) {
      ctaEl.innerHTML = `
        <button type="button" data-view="read" style="padding:8px 12px;">읽기 시작!</button>
        <button type="button" data-view="home" style="padding:8px 12px; margin-left:8px;">홈으로</button>
      `;
    }
  };

  // 커스텀 문장 불러오기
  (async () => {
    quoteEl.innerHTML = '불러오는 중…';

    try {
      const data = await fetchJSONWithAuth(CUSTOM_SENTENCE_API, { method: 'GET' });
      const items = Array.isArray(data) ? data : [];

      customSentences = items.map(x => ({
        id: x.id ?? x._id ?? null,
        sentence: typeof x.sentence === 'string' ? x.sentence : String(x.text ?? x.content ?? '').trim()
      })).filter(entry => entry.sentence.length);

    } catch (e) {
      console.error('커스텀 문장 로드 실패:', e);
      customSentences = [];
    } finally {
      render();
    }
  })();

  // 다음 버튼: 커스텀 문장 순환
  if (nextBtn) {
    nextBtn.addEventListener('click', (event) => {
      event.stopPropagation();
      if (!customSentences.length) return;
      idx = (idx + 1) % customSentences.length;
      render();
    });
  }
  // 읽기 시작! → 기존 전역 핸들러가 #quoteText.innerHTML을 localStorage에 저장하고 read.html 로드
}

