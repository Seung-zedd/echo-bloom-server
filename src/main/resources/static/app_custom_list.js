(function(){
  /* =========================
     설정: API 베이스 & 엔드포인트
  ========================= */
  const API_BASE = ''; // 같은 도메인이면 '' 유지, 분리 배포면 'https://api.example.com'
  const ENDPOINTS = {
    LIST:   API_BASE + '/api/v1/custom-sentences',     // GET
    CREATE: API_BASE + '/api/v1/custom-sentences',     // POST {sentence}
    UPDATE: (id) => API_BASE + `/api/v1/custom-sentences/${id}`, // PUT {sentence}
    DELETE: (id) => API_BASE + `/api/v1/custom-sentences/${id}`, // DELETE
  };

  const root  = document.getElementById('csList');
  const input = document.getElementById('csInput');
  const btnCreate = document.getElementById('csCreate');
  const live = document.getElementById('csLive');

  const isFile  = location.protocol === 'file:';
  const useMock = new URLSearchParams(location.search).get('mock') === '1';

  function say(msg){ if(live){ live.textContent = msg; } }

  // JWT 자동 부착(선택)
  function getJwtToken(){
    return localStorage.getItem('jwt') || localStorage.getItem('access_token')
        || sessionStorage.getItem('jwt') || sessionStorage.getItem('access_token') || null;
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
     렌더링
  ========================= */
  function renderEmpty(){
    root.innerHTML = `<div class="cs-empty">저장된 커스텀 문장이 없어요.</div>`;
  }

  function iconEdit(){return `<svg class="ico-edit" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 640"><!--!Font Awesome Free v7.0.0 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2025 Fonticons, Inc.--><path d="M100.4 417.2C104.5 402.6 112.2 389.3 123 378.5L304.2 197.3L338.1 163.4C354.7 180 389.4 214.7 442.1 267.4L476 301.3L442.1 335.2L260.9 516.4C250.2 527.1 236.8 534.9 222.2 539L94.4 574.6C86.1 576.9 77.1 574.6 71 568.4C64.9 562.2 62.6 553.3 64.9 545L100.4 417.2zM156 413.5C151.6 418.2 148.4 423.9 146.7 430.1L122.6 517L209.5 492.9C215.9 491.1 221.7 487.8 226.5 483.2L155.9 413.5zM510 267.4C493.4 250.8 458.7 216.1 406 163.4L372 129.5C398.5 103 413.4 88.1 416.9 84.6C430.4 71 448.8 63.4 468 63.4C487.2 63.4 505.6 71 519.1 84.6L554.8 120.3C568.4 133.9 576 152.3 576 171.4C576 190.5 568.4 209 554.8 222.5C551.3 226 536.4 240.9 509.9 267.4z"/></svg>`}
  // 휴지통 아이콘 (네가 준 Font Awesome 7 버전)
function iconDel(){
  return `
    <svg class="ico-del" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 640">
      <path d="M232.7 69.9C237.1 56.8 249.3 48 263.1 48L377 48C390.8 48 403 56.8 407.4 69.9L416 96L512 96C529.7 96 544 110.3 544 128C544 145.7 529.7 160 512 160L128 160C110.3 160 96 145.7 96 128C96 110.3 110.3 96 128 96L224 96L232.7 69.9zM128 208L512 208L512 512C512 547.3 483.3 576 448 576L192 576C156.7 576 128 547.3 128 512L128 208zM216 272C202.7 272 192 282.7 192 296L192 488C192 501.3 202.7 512 216 512C229.3 512 240 501.3 240 488L240 296C240 282.7 229.3 272 216 272zM320 272C306.7 272 296 282.7 296 296L296 488C296 501.3 306.7 512 320 512C333.3 512 344 501.3 344 488L344 296C344 282.7 333.3 272 320 272zM424 272C410.7 272 400 282.7 400 296L400 488C400 501.3 410.7 512 424 512C437.3 512 448 501.3 448 488L448 296C448 282.7 437.3 272 424 272z"/>
    </svg>`;
}
  function iconSave(){return `<svg class="cs-save" viewBox="0 0 24 24" fill="none"><path d="M20 6L9 17l-5-5" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>`}
  function iconCancel(){return `<svg class="cs-cancel" viewBox="0 0 24 24" fill="none"><path d="M6 6l12 12M18 6L6 18" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>`}

  function normalizeItems(data){
    const src = Array.isArray(data) ? data : Array.isArray(data?.items) ? data.items : [];
    return src.map((x,i)=>({
      id: x.id ?? x._id ?? i,
      text: String(x.sentence ?? x.text ?? x.content ?? '').trim()
    })).filter(x => x.text);
  }

  function renderList(items){
    if (!items?.length){ renderEmpty(); return; }
    root.innerHTML = `
      <ul class="cs-ul">
        ${items.map(it => `
          <li class="cs-item" data-id="${it.id}">
            <div class="cs-text">${escapeHtml(it.text)}</div>
            <div class="cs-actions">
              <button class="icon-btn act-edit" type="button" aria-label="수정">${iconEdit()}</button>
              <button class="icon-btn act-del"  type="button" aria-label="삭제">${iconDel()}</button>
            </div>
          </li>
        `).join('')}
      </ul>`;
  }

  function escapeHtml(t){
    return t.replace(/[&<>"']/g, (m)=>({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[m]));
  }

  /* =========================
     데이터 로드
  ========================= */
  async function loadList(){
    root.innerHTML = `<div class="cs-empty" style="opacity:.85;">불러오는 중…</div>`;
    if (isFile && !useMock) { renderEmpty(); return; }

    if (useMock){
      const sample = [
        { id:'m1', text:'나는 나 자신을 돌보며 배려할 줄 알아' },
        { id:'m2', text:'나는 나의 지금 모습 그대로도 좋아!' },
        { id:'m3', text:'나는 매일 변화하며 성장하는 사람이야' },
      ];
      renderList(sample);
      return;
    }

    try{
      const data = await fetchJSONWithAuth(ENDPOINTS.LIST, { method:'GET' });
      renderList(normalizeItems(data));
    }catch(e){
      console.warn(e);
      renderEmpty();
    }
  }

  /* =========================
     생성/수정/삭제
  ========================= */
  function validText(s){
    const t = s.replace(/\s+/g,' ').trim();
    return t.length >= 1 && t.length <= 70 ? t : null;
  }

  btnCreate?.addEventListener('click', async () => {
    const t = validText(input.value);
    if (!t){ alert('문구는 1자 이상 70자 이하로 입력해 주세요.'); input.focus(); return; }

    // 낙관적 추가
    const ul = root.querySelector('.cs-ul');
    const tempId = 'tmp_' + Date.now();
    const temp = { id: tempId, text: t };
    if (ul){
      ul.insertAdjacentHTML('afterbegin', `
        <li class="cs-item" data-id="${temp.id}">
          <div class="cs-text">${escapeHtml(temp.text)}</div>
          <div class="cs-actions">
            <button class="icon-btn act-edit" type="button" aria-label="수정">${iconEdit()}</button>
            <button class="icon-btn act-del"  type="button" aria-label="삭제">${iconDel()}</button>
          </div>
        </li>`);
    } else {
      renderList([temp]); // 빈 상태였다면 새로 그림
    }
    input.value = ''; say('문구가 추가되었어요.');

    if (useMock) return;

    try{
      const res = await fetchJSONWithAuth(ENDPOINTS.CREATE, { method:'POST', body:{ sentence: t } });
      const newId = res?.id;
      if (newId){
        const li = root.querySelector(`.cs-item[data-id="${tempId}"]`);
        if (li) li.dataset.id = newId;
      }
    }catch(e){
      // 실패 롤백
      const li = root.querySelector(`.cs-item[data-id="${tempId}"]`);
      li && li.remove();
      alert('저장에 실패했어요. 잠시 후 다시 시도해 주세요.');
    }
  });

  // Enter 키로 문구 저장
  input?.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      btnCreate?.click();
    }
  });

  // 위임: 수정/삭제
  root.addEventListener('click', async (e) => {
    const editBtn = e.target.closest('.act-edit');
    const delBtn  = e.target.closest('.act-del');
    const saveBtn = e.target.closest('.act-save');
    const cancelBtn = e.target.closest('.act-cancel');
    const li = e.target.closest('.cs-item');
    if (!li) return;

    // 삭제
    if (delBtn){
      if (!confirm('이 문구를 삭제할까요?')) return;
      const id = li.dataset.id;
      const backup = li.outerHTML; // 롤백용
      li.style.opacity = .5;

      if (!useMock){
        try{
          await fetchJSONWithAuth(ENDPOINTS.DELETE(id), { method:'DELETE' });
        }catch(e){
          li.outerHTML = backup;
          alert('삭제에 실패했어요.');
          return;
        }
      }
      li.remove();
      if (!root.querySelector('.cs-item')) renderEmpty();
      say('삭제되었어요.');
      return;
    }

    // 편집 시작
    if (editBtn){
      const textEl = li.querySelector('.cs-text');
      const original = textEl?.textContent || '';
      li.innerHTML = `
        <div class="cs-editbox">
          <input type="text" class="cs-input-inline" value="${escapeHtml(original)}" maxlength="70" />
        </div>
        <div class="cs-actions">
          <button class="icon-btn act-save"   type="button" aria-label="저장">${iconSave()}</button>
          <button class="icon-btn act-cancel" type="button" aria-label="취소">${iconCancel()}</button>
        </div>`;

      const inlineInput = li.querySelector('.cs-input-inline');
      inlineInput?.focus();

      // Enter 키로 수정 저장
      inlineInput?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
          e.preventDefault();
          li.querySelector('.act-save')?.click();
        }
      });

      return;
    }

    // 편집 취소
    if (cancelBtn){
      const id = li.dataset.id;
      const text = li.querySelector('.cs-input-inline')?.value || '';
      // 그냥 원복(서버 통신 없음, 기존 값 불러오려면 data-*에 저장해두는 방식도 가능)
      li.innerHTML = `
        <div class="cs-text">${escapeHtml(text)}</div>
        <div class="cs-actions">
          <button class="icon-btn act-edit" type="button" aria-label="수정">${iconEdit()}</button>
          <button class="icon-btn act-del"  type="button" aria-label="삭제">${iconDel()}</button>
        </div>`;
      return;
    }

    // 편집 저장
    if (saveBtn){
      const id = li.dataset.id;
      const v = validText(li.querySelector('.cs-input-inline')?.value || '');
      if (!v){ alert('문구는 1자 이상 70자 이하로 입력해 주세요.'); return; }

      // 낙관적 적용
      li.innerHTML = `
        <div class="cs-text">${escapeHtml(v)}</div>
        <div class="cs-actions">
          <button class="icon-btn act-edit" type="button" aria-label="수정">${iconEdit()}</button>
          <button class="icon-btn act-del"  type="button" aria-label="삭제">${iconDel()}</button>
        </div>`;
      say('수정되었어요.');

      if (useMock) return;

      try{
        await fetchJSONWithAuth(ENDPOINTS.UPDATE(id), { method:'PUT', body:{ sentence: v } });
      }catch(e){
        alert('수정에 실패했어요. 잠시 후 다시 시도해 주세요.');
      }
    }
  });

  // 시작
  loadList();
})();