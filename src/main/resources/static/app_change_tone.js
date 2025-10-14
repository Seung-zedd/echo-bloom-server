document.addEventListener('DOMContentLoaded', () => {
  const group   = document.getElementById('toneList');
  const buttons = Array.from(group.querySelectorAll('.opt'));
  const nextBtn = document.getElementById('toNext');

  const FETCH_ENDPOINT = '/api/users/me/categories';
  const SAVE_ENDPOINT  = '/api/users/me/categories/tones';
  const NEXT_URL       = 'mypage.html';

  const toneNameByKey = {
    tone1: 'Joy',
    tone2: 'Wednesday',
    tone3: 'Zelda'
  };

  const toneKeyByName = Object.fromEntries(Object.entries(toneNameByKey).map(([key, value]) => [value, key]));

  group.setAttribute('role','radiogroup');
  buttons.forEach(b => {
    b.setAttribute('role','radio');
    b.setAttribute('aria-checked','false');
    b.addEventListener('click', () => select(b));
    b.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); select(b); }
    });
  });

  loadCurrentTone();

  function select(btn){
    buttons.forEach(b => {
      const on = b === btn;
      b.classList.toggle('selected', on);
      b.setAttribute('aria-checked', on ? 'true' : 'false');
    });
    nextBtn?.classList.remove('is-disabled');
    nextBtn?.removeAttribute('aria-disabled');
  }

  async function loadCurrentTone(){
    try {
      const res = await fetch(FETCH_ENDPOINT, { credentials: 'include' });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      const toneName = data?.tone?.name;
      if (!toneName || typeof toneName !== 'string') return;
      const toneKey = toneKeyByName[toneName];
      if (!toneKey) return;
      const btn = buttons.find(b => b.dataset.value === toneKey);
      if (btn) {
        select(btn);
      }
    } catch (err) {
      console.warn('Failed to load current tone selection:', err);
    }
  }

  nextBtn?.addEventListener('click', async () => {
    const selected = group.querySelector('.opt.selected');
    if (!selected) {
      alert('먼저 톤을 선택해주세요.');
      return;
    }

    const toneKey = selected.dataset.value;
    const toneName = toneNameByKey[toneKey];
    if (!toneName) {
      alert('선택한 톤 정보를 찾을 수 없어요. 다시 선택해 주세요.');
      return;
    }

    nextBtn.classList.add('is-disabled');
    nextBtn.setAttribute('aria-disabled','true');

    const CSRF = document.querySelector('meta[name="csrf-token"]')?.content;

    try {
      const res = await fetch(SAVE_ENDPOINT, {
        method: 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-Requested-With': 'XMLHttpRequest',
          ...(CSRF ? { 'X-CSRF-Token': CSRF } : {})
        },
        body: JSON.stringify({ toneName })
      });

      if (!res.ok) throw new Error('HTTP ' + res.status);

      // Clear cached affirmations so new ones are generated based on updated tone
      sessionStorage.removeItem('generated_affirmations');
      sessionStorage.removeItem('affirmation_idx');

      location.href = NEXT_URL;

    } catch (err) {
      console.error('tone submit failed:', err);
      alert('전송에 실패했어요. 네트워크 상태를 확인한 후 다시 시도해 주세요.');
      nextBtn.classList.remove('is-disabled');
      nextBtn.removeAttribute('aria-disabled');
    }
  });
});

