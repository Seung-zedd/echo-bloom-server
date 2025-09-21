document.addEventListener('DOMContentLoaded', () => {
  /* =========================
     설정: API 베이스 & 엔드포인트
  ========================= */
  // 같은 도메인이면 '' 유지, 다른 도메인이면 'https://api.example.com' 등으로 교체
  const API_BASE = '';
  const ENDPOINTS = {
    LIST:   API_BASE + '/api/bookmarks/me',     // GET
    TOGGLE: API_BASE + '/api/bookmarks/toggle', // POST {id} 또는 {text}
  };

  const bmRoot  = document.getElementById('bmList');
  if (!bmRoot) { console.warn('#bmList 컨테이너 없음'); return; }

  const isFile  = location.protocol === 'file:';
  const useMock = new URLSearchParams(location.search).get('mock') === '1';

  /* =========================
     공통 유틸
  ========================= */
  function starSVG() {
    return `
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24
                 l-7.19-.61L12 2 9.19 8.63 2 9.24
                 l5.46 4.73L5.82 21z"></path>
      </svg>`;
  }

  function applyStarVisual(btn, on){
    const path = btn?.querySelector('path');
    if (!path) return;
    if (on) {
      path.setAttribute('fill', '#f4c430');
      path.setAttribute('stroke', '#f4c430');
      path.removeAttribute('stroke-width');
      path.removeAttribute('stroke-linejoin');
    } else {
      path.setAttribute('fill', 'none');
      path.setAttribute('stroke', '#f4c430');
      path.setAttribute('stroke-width', '2');
      path.setAttribute('stroke-linejoin', 'round');
    }
  }

  function renderEmpty(){
    bmRoot.innerHTML = `
      <div class="bm-empty">
        북마크한 문장이 없어요.
        <small>홈에서 문장을 읽고 북마크에 추가해 보세요!</small>
      </div>`;
  }

  function renderList(items){
    if (!items?.length) { renderEmpty(); return; }
    const lis = items.map(it => `
      <li class="bm-item" data-id="${it.id ?? ''}">
        <div class="bm-text">${String(it.text || '').replace(/\n/g,'<br/>')}</div>
        <button class="bm-star" type="button" aria-label="북마크 토글" data-on="${!!it.bookmarked}">
          ${starSVG()}
        </button>
      </li>
    `).join('');
    bmRoot.innerHTML = `<ul class="bm-ul">${lis}</ul>`;

    // 초기 시각 상태 반영
    bmRoot.querySelectorAll('.bm-star').forEach(btn => {
      applyStarVisual(btn, btn.dataset.on === 'true');
    });
  }

  // JWT가 있으면 Authorization 헤더 부착(선택)
  function getJwtToken(){
    return (
      localStorage.getItem('jwt') ||
      localStorage.getItem('access_token') ||
      sessionStorage.getItem('jwt') ||
      sessionStorage.getItem('access_token') ||
      null
    );
  }
  async function fetchJSONWithAuth(url, { method='GET', headers={}, body=null } = {}){
    const h = new Headers(headers);
    const token = getJwtToken();
    if (token && !h.has('Authorization')) h.set('Authorization', 'Bearer ' + token);

    const init = { method, headers:h, cache:'no-store', credentials:'same-origin' };
    if (body && typeof body === 'object' && !(body instanceof FormData)) {
      h.set('Content-Type','application/json'); init.body = JSON.stringify(body);
    } else if (body) { init.body = body; }

    const res = await fetch(url, init);
    if (!res.ok) throw new Error('HTTP ' + res.status);
    try { return await res.json(); } catch { return {}; }
  }

  /* =========================
     데이터 로드
  ========================= */
  async function loadBookmarks(){
    bmRoot.innerHTML = `<div class="bm-empty" style="opacity:.8;">불러오는 중…</div>`;

    // 파일로 직접 열었고 mock이 아니면 네트워크 없음 → 빈 상태
    if (isFile && !useMock) { renderEmpty(); return; }

    // 모크 모드: 샘플 데이터
    if (useMock) {
      const sample = [
        { id:'s1', text:'“어려워도 괜찮아, 나는 희망을 찾을 수 있을 거야.”', bookmarked:true },
        { id:'s2', text:'“천천히 가도 돼, 멈추지만 않으면 돼.”', bookmarked:true },
      ];
      renderList(sample);
      return;
    }

    // 실제 API
    try {
      const data = await fetchJSONWithAuth(ENDPOINTS.LIST, { method:'GET' });
      const items = Array.isArray(data?.items) ? data.items
                  : Array.isArray(data)        ? data
                  : [];
      renderList(items.map(x => ({
        id: x.id ?? x.bookmarkId ?? x._id ?? null,
        text: x.text ?? x.content ?? '',
        bookmarked: x.bookmarked ?? true
      })));
    } catch (e) {
      console.warn('bookmark load failed:', e);
      renderEmpty();
    }
  }

  /* =========================
     토글 (낙관적 UI + 롤백)
  ========================= */
  bmRoot.addEventListener('click', async (e) => {
    const btn = e.target.closest('.bm-star');
    if (!btn) return;

    const item = btn.closest('.bm-item');
    const id   = item?.dataset.id || null;
    const text = item?.querySelector('.bm-text')?.innerText?.trim() || '';

    // 상태 토글(낙관적)
    const next = !(btn.dataset.on === 'true');
    btn.dataset.on = String(next);
    applyStarVisual(btn, next);

    // 모크 모드면 여기서 종료(로컬 UI만)
    if (useMock) return;

    // 실제 API 호출
    try {
      await fetchJSONWithAuth(ENDPOINTS.TOGGLE, {
        method:'POST',
        body: id ? { id } : { text }
      });
    } catch (err) {
      // 실패 롤백
      console.error('toggle failed:', err);
      btn.dataset.on = String(!next);
      applyStarVisual(btn, !next);

      const t = document.createElement('div');
      t.textContent = '북마크를 변경하지 못했어요. 잠시 후 다시 시도해주세요.';
      t.style.cssText = 'position:fixed;left:50%;bottom:20px;transform:translateX(-50%);background:#222;color:#fff;padding:8px 12px;border-radius:999px;font-size:.85rem;z-index:9999;';
      document.body.appendChild(t);
      setTimeout(()=>t.remove(), 1800);
    }
  });

  // 초기 로드
  loadBookmarks();
});