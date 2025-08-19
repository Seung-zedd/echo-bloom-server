
document.addEventListener('DOMContentLoaded', () => {
  const group   = document.getElementById('toneList');
  const buttons = Array.from(group.querySelectorAll('.opt'));
  const nextBtn = document.getElementById('toNext');

  // 서버 엔드포인트 & 성공 후 이동할 주소 (원하면 바꿔주세요)
  const ENDPOINT_TONE = '/api/tone';        // ← 백엔드 URL
  const NEXT_URL      = '../mypage.html';   // ← 성공 후 이동

  // 선택 상태 관리 (이미 구현돼있으면 이 블록은 생략해도 됨)
  group.setAttribute('role','radiogroup');
  buttons.forEach(b => {
    b.setAttribute('role','radio');
    b.setAttribute('aria-checked','false');
    b.addEventListener('click', () => select(b));
    b.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); select(b); }
    });
  });
  function select(btn){
    buttons.forEach(b => {
      const on = b === btn;
      b.classList.toggle('selected', on);
      b.setAttribute('aria-checked', on ? 'true' : 'false');
    });
    nextBtn?.classList.remove('is-disabled');
    nextBtn?.removeAttribute('aria-disabled');
  }

  // 다음 버튼 → 서버 전송
  nextBtn?.addEventListener('click', async () => {
    // 아직 선택 안 했으면 막기
    const selected = group.querySelector('.opt.selected');
    if (!selected) {
      // 간단 알림 (필요시 UI로 바꿔도 됨)
      alert('먼저 톤을 선택해주세요.');
      return;
    }
    const toneValue = selected.dataset.value; // 예: "hopeful_soft"

    // 전송 중 상태
    nextBtn.classList.add('is-disabled');
    nextBtn.setAttribute('aria-disabled','true');

    // CSRF 토큰 쓰는 프로젝트라면 meta에서 꺼내 헤더에 추가
    const CSRF = document.querySelector('meta[name="csrf-token"]')?.content;

    try {
      const res = await fetch(ENDPOINT_TONE, {
        method: 'POST',
        credentials: 'include', // 쿠키 인증 시
        headers: {
          'Content-Type': 'application/json',
          'X-Requested-With': 'XMLHttpRequest',
          ...(CSRF ? { 'X-CSRF-Token': CSRF } : {})
        },
        body: JSON.stringify({ tone: toneValue })
      });

      if (!res.ok) throw new Error('HTTP ' + res.status);

      // 성공 처리: 원하는 흐름으로 바꿔도 됨(이동/토스트 등)
      location.href = NEXT_URL;

    } catch (err) {
      console.error('tone submit failed:', err);
      alert('전송에 실패했어요. 네트워크 상태를 확인한 후 다시 시도해 주세요.');
      // 실패 시 다시 활성화
      nextBtn.classList.remove('is-disabled');
      nextBtn.removeAttribute('aria-disabled');
    }
  });
});

