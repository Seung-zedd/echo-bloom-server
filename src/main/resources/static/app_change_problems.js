document.addEventListener('DOMContentLoaded', () => {
  const list      = document.getElementById('choiceList');
  const buttons   = Array.from(list.querySelectorAll('.opt'));
  const selCount  = document.getElementById('selCount');
  const nextBtn   = document.getElementById('toNext');
  const MAX       = 3;

  // 서버 엔드포인트 & 성공 후 이동 경로 (원하는 값으로 교체)
  const ENDPOINT  = '/api/problems';     // ← 백엔드 URL (예: /api/mypage/problems)
  const NEXT_URL  = 'mypage.html';       // ← 저장 성공 후 이동할 페이지

  // 선택 토글(최대 3개)
  buttons.forEach(btn => {
    btn.addEventListener('click', () => {
      if (btn.classList.contains('selected')) {
        btn.classList.remove('selected');
      } else {
        const picked = list.querySelectorAll('.opt.selected').length;
        if (picked >= MAX) {
          // 가벼운 피드백(선택 초과)
          btn.animate([{transform:'scale(1)'},{transform:'scale(0.98)'},{transform:'scale(1)'}], {duration:150});
          return;
        }
        btn.classList.add('selected');
      }
      updateState();
    });
  });

  function updateState(){
    const picked = list.querySelectorAll('.opt.selected').length;
    selCount.textContent = picked ? `(${picked}/${MAX})` : '';
    if (picked > 0) {
      nextBtn.classList.remove('is-disabled');
      nextBtn.removeAttribute('aria-disabled');
    } else {
      nextBtn.classList.add('is-disabled');
      nextBtn.setAttribute('aria-disabled','true');
    }
  }

  // 다음 → 서버 전송
  nextBtn.addEventListener('click', async () => {
    if (nextBtn.classList.contains('is-disabled')) return;

    const values = Array.from(list.querySelectorAll('.opt.selected')).map(b => b.dataset.value);
    if (values.length === 0) { alert('최소 1개 이상 선택해주세요.'); return; }

    nextBtn.classList.add('is-disabled'); nextBtn.setAttribute('aria-disabled','true');

    // CSRF 토큰을 쓰는 경우(선택): <meta name="csrf-token" content="...">
    const CSRF = document.querySelector('meta[name="csrf-token"]')?.content;

    try{
      const res = await fetch(ENDPOINT, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-Requested-With': 'XMLHttpRequest',
          ...(CSRF ? {'X-CSRF-Token': CSRF} : {})
        },
        body: JSON.stringify({ problems: values })  // ← 서버에서 이 필드명에 맞춰 받기
      });
      if (!res.ok) throw new Error('HTTP ' + res.status);

      location.href = NEXT_URL;  // 성공 이동
    } catch (err) {
      console.error('submit failed:', err);
      alert('저장에 실패했어요. 네트워크 상태를 확인하고 다시 시도해주세요.');
      nextBtn.classList.remove('is-disabled'); nextBtn.removeAttribute('aria-disabled');
    }
  });
});
