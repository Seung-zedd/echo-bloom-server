document.addEventListener('DOMContentLoaded', () => {
  /* =========================
     설정: API 베이스 & 엔드포인트
  ========================= */
  const API_BASE = '';
  const ENDPOINTS = {
    LIST: `${API_BASE}/api/v1/bookmarks`,
    ADD: `${API_BASE}/api/v1/bookmarks/add`,
    REMOVE: `${API_BASE}/api/v1/bookmarks/remove`,
  };
  const BOOKMARK_TONE_DEFAULT = 'normal';

  const bmRoot = document.getElementById('bmList');
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

  function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"]/g, (ch) => {
      switch (ch) {
        case '&': return '&amp;';
        case '<': return '&lt;';
        case '>': return '&gt;';
        case '"': return '&quot;';
        default: return ch;
      }
    }).replace(/'/g, '&#39;');
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

  function renderList(items){
    if (!items?.length) { renderEmpty(); return; }

    const lis = items.map(it => {
      const id = it.id ?? '';
      const sentence = String(it.sentence ?? '').trim();
      const tone = (it.tone ?? BOOKMARK_TONE_DEFAULT) || BOOKMARK_TONE_DEFAULT;
      const bookmarked = !!(it.bookmarked ?? true);
      const encodedSentence = encodeURIComponent(sentence);
      const encodedTone = encodeURIComponent(tone);
      const safeSentenceHtml = escapeHtml(sentence).replace(/\n/g,'<br/>');

      return `
      <li class="bm-item" data-id="${escapeHtml(id)}" data-sentence="${encodedSentence}" data-tone="${encodedTone}">
        <div class="bm-text">${safeSentenceHtml}</div>
        <button class="bm-star" type="button" aria-label="북마크 토글" data-on="${bookmarked ? 'true' : 'false'}">
          ${starSVG()}
        </button>
      </li>
      `;
    }).join('');

    bmRoot.innerHTML = `<ul class="bm-ul">${lis}</ul>`;

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

    const init = { method, headers:h, cache:'no-store', credentials:'include' };
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

    if (isFile && !useMock) { renderEmpty(); return; }

    if (useMock) {
      const sample = [
        { id:'s1', sentence:'“어려워도 괜찮아, 나는 희망을 찾을 수 있을 거야.”', tone: 'hope', bookmarked:true },
        { id:'s2', sentence:'“천천히 가도 돼, 멈추지만 않으면 돼.”', tone: 'calm', bookmarked:true },
      ];
      renderList(sample);
      return;
    }

    try {
      const data = await fetchJSONWithAuth(ENDPOINTS.LIST, { method:'GET' });
      const items = Array.isArray(data?.items) ? data.items
                  : Array.isArray(data)        ? data
                  : [];

      const normalized = items.map(x => ({
        id: x.id ?? x.bookmarkId ?? x._id ?? null,
        sentence: normalizeSentence(x),
        tone: typeof x.tone === 'string' && x.tone.trim().length ? x.tone : BOOKMARK_TONE_DEFAULT,
          bookmarked: (x.isBookmarked === true) || (x.bookmarked === true),
      })).filter(entry => entry.sentence.length);

      renderList(normalized);
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
    if (!item) return;

    const sentenceEncoded = item.dataset.sentence || '';
    const toneEncoded = item.dataset.tone || '';
    const sentence = sentenceEncoded ? decodeURIComponent(sentenceEncoded) : '';
    const tone = toneEncoded ? decodeURIComponent(toneEncoded) : BOOKMARK_TONE_DEFAULT;

    const next = !(btn.dataset.on === 'true');
    btn.dataset.on = String(next);
    applyStarVisual(btn, next);

    if (useMock) return;

    try {
      if (next) {
        await fetchJSONWithAuth(ENDPOINTS.ADD, {
          method:'POST',
          body: { sentence, tone: tone || BOOKMARK_TONE_DEFAULT }
        });
      } else {
        const removeUrl = `${ENDPOINTS.REMOVE}?sentence=${encodeURIComponent(sentence)}`;
        await fetchJSONWithAuth(removeUrl, { method:'DELETE' });
      }

      await loadBookmarks();
    } catch (err) {
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
