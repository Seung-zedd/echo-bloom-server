// app.js
const app = document.getElementById('app');
const screenEl = document.querySelector('.screen');
const TRANSITION_MS = 250;
const OVERLAY_ID = 'globalOverlay';

function ensureOverlay(on) {
  let ov = document.getElementById(OVERLAY_ID);
  if (on) {
    if (!ov) {
      ov = document.createElement('div');
      ov.id = OVERLAY_ID;
      ov.className = 'overlay';
      screenEl.appendChild(ov); // #app의 형제로 추가 → 페이드 제외
    }
  } else {
    ov && ov.remove();
  }
}

async function loadView(url) {
  // 1) leave 애니
  app.classList.remove('entered', 'entering');
  app.classList.add('leaving');

  // 2) fetch
  let html = '';
  try {
    const res = await fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
    if (!res.ok) throw new Error('load error');
    html = await res.text();
  } catch (e) {
    html = `<div data-view><h1 class="headline">불러오기 실패</h1>
            <div class="cta"><button class="kakao-btn" id="retry">다시 시도</button></div></div>`;
  }

  // 3) leave 끝난 뒤 교체
  await new Promise(r => setTimeout(r, TRANSITION_MS));

  // 문자열을 DOM으로 파싱해서 오버레이 필요 여부 확인
  const tpl = document.createElement('template');
  tpl.innerHTML = html.trim();
  const viewRoot = tpl.content.querySelector('[data-view]') || tpl.content.firstElementChild;

  const needOverlay = viewRoot?.dataset?.overlay === 'true';
  ensureOverlay(needOverlay);

  // 풀스크린 화면은 #app 패딩 제거
if (needOverlay) app.classList.add('full');
else app.classList.remove('full');

  // 콘텐츠 교체
  app.innerHTML = '';
  app.appendChild(viewRoot);

  // 4) enter 애니
  app.classList.remove('leaving');
  app.classList.add('entering');
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      app.classList.remove('entering');
      app.classList.add('entered');
      bindLocalEvents(); // 새 화면 버튼 이벤트 바인딩
    });
  });
}

function bindLocalEvents() {
  // 전환된 화면의 버튼들 이벤트 연결
  const retry = app.querySelector('#retry');
  retry && retry.addEventListener('click', () => location.reload());

  const goDeeper = app.querySelector('#goDeeper');
  goDeeper && goDeeper.addEventListener('click', () => loadView('views/intro2.html'));

  const goDeeper2 = app.querySelector('#goDeeper2');
  goDeeper2 && goDeeper2.addEventListener('click', () => loadView('views/intro3.html'));

  const goDeeper3 = app.querySelector('#goDeeper3');
  goDeeper3 && goDeeper3.addEventListener('click', () => loadView('views/intro4.html'));

  const startLoginKakao = app.querySelector('#startLoginKakao');
  if (startLoginKakao) {
    startLoginKakao.addEventListener('click', (e) => {
      e.preventDefault();
      location.href = 'views/search.html';
    });
  }

  const goBack = app.querySelector('#goBack');
  if (goBack) {
    goBack.addEventListener('click', (e) => {
      location.href = 'main.html';
    });
  }
}


// 초기 메인 화면 버튼 이벤트
document.getElementById('startKakao')?.addEventListener('click', async () => {
  // Trigger Kakao OAuth login
  try {
    const response = await fetch('/auth/kakao/login-url');
    if (!response.ok) throw new Error('Failed to get login URL');
    const kakaoLoginUrl = await response.text();
    // Redirect to Kakao OAuth login page
    window.location.href = kakaoLoginUrl;
  } catch (error) {
    console.error('Failed to start Kakao login:', error);
    alert('카카오 로그인을 시작할 수 없습니다. 잠시 후 다시 시도해주세요.');
  }
});
document.getElementById('skipLink')?.addEventListener('click', (e) => {
  e.preventDefault();
  window.location.href = '/landing_page.html';
});
