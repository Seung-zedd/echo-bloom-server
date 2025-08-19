document.getElementById('goNotice')?.addEventListener('click', async (e) => {
  e.preventDefault(); // 기본 이동 막고
  const app = document.getElementById('app');
  if (!app) return;

  try {
    const res = await fetch('notice.html', { cache: 'no-cache' });
    if (!res.ok) throw new Error(res.statusText);
    const html = await res.text();
    app.innerHTML = html;         // #app 내용만 교체
    // 필요하면 전환 효과 클래스나 스크롤 처리 추가 가능:
    // app.scrollTo?.(0,0);
    // history.pushState({}, '', 'notice.html'); // 주소 표시줄 바꾸고 싶으면(선택)
  } catch (err) {
    // 실패하면 그냥 일반 이동으로 폴백
    location.href = 'notice.html';
  }
});


(function(){
  const logoutLink = document.getElementById('logoutLink');
  const modal = document.getElementById('logoutModal');
  const bodyEl = document.getElementById('logoutModalBody');

  if (logoutLink) {
    logoutLink.addEventListener('click', (e)=>{
      e.preventDefault();
      openConfirm();
    });
  }

  // 모달 열기/닫기
  function openModal(){ modal.classList.add('show'); modal.setAttribute('aria-hidden','false'); }
  function closeModal(){ modal.classList.remove('show'); modal.setAttribute('aria-hidden','true'); }

  // 바깥 클릭, 취소 버튼 닫기
  modal.addEventListener('click', (e)=>{
    if (e.target.dataset.close) closeModal();
    if (e.target.dataset.action === 'cancel') closeModal();
  });

  // 확인 모드
  function openConfirm(){
    bodyEl.innerHTML = `
      <h3 id="logoutTitle" style="margin:0 0 8px;">로그아웃 하시겠습니까?</h3>
      <p class="modal-sub">현재 기기에서 로그아웃됩니다.</p>
      <div class="modal-actions">
        <button type="button" class="btn ghost" data-action="cancel">아니오</button>
        <button type="button" class="btn danger" data-action="confirm">예</button>
      </div>
    `;
    openModal();
  }

  // 완료 모드
  function openDone(){
    bodyEl.innerHTML = `
      <h3 style="margin:0 0 8px;">안전하게 로그아웃 되었어요.</h3>
      <p class="modal-sub">다음에 또 만나요!</p>
      <div class="modal-actions">
        <a class="btn primary" href="../main.html">홈으로 돌아가기</a>
      </div>
    `;
  }

  // confirm 클릭 처리
  modal.addEventListener('click', async (e)=>{
    if (e.target.dataset.action !== 'confirm') return;

    // 1) 서버에 로그아웃 요청 (권장)
    let serverOk = false;
    try {
      const res = await fetch('/auth/kakao/logout', { // 로그아웃 로직 처리해주는 서버
        method: 'POST',
        credentials: 'include',
        headers: { 'X-Requested-With':'XMLHttpRequest' }
      });
      serverOk = res.ok;
    } catch(_) {}

    // 2) 프론트에서 보조 정리 (non-HttpOnly용)
    try {
      // 지우고 싶은 쿠키들 이름 예시(프로젝트에 맞춰 수정)
      ['auth_token','member_id','mb_id','ck_mb_id','ck_auto'].forEach(deleteCookie);
      // 로컬 저장소 정리(필요한 key만 지우세요)
      localStorage.removeItem('auth');
      sessionStorage.clear();
    } catch(_) {}

    openDone(); // 성공 메시지 표시
  });

  // JS로 삭제 가능한 쿠키(HTTPOnly 아님)만 삭제됨
  function deleteCookie(name){
    document.cookie = `${name}=; Max-Age=0; path=/;`;
  }
})();
