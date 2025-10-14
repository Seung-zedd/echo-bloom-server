document.addEventListener('DOMContentLoaded', () => {
  const list      = document.getElementById('choiceList');
  const buttons   = Array.from(list.querySelectorAll('.opt'));
  const selCount  = document.getElementById('selCount');
  const nextBtn   = document.getElementById('toNext');
  const MAX       = 3;

  const FETCH_ENDPOINT = '/api/users/me/categories';
  const SAVE_ENDPOINT  = '/api/users/me/categories/problems';
  const NEXT_URL       = 'mypage.html';

  loadCurrentSelections();

  buttons.forEach(btn => {
    btn.addEventListener('click', () => {
      if (btn.classList.contains('selected')) {
        btn.classList.remove('selected');
      } else {
        const picked = list.querySelectorAll('.opt.selected').length;
        if (picked >= MAX) {
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

  async function loadCurrentSelections(){
    try {
      const res = await fetch(FETCH_ENDPOINT, { credentials: 'include' });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      const selectedIds = Array.isArray(data?.problems)
        ? data.problems.map(item => String(item?.id)).filter(Boolean)
        : [];
      buttons.forEach(btn => {
        if (selectedIds.includes(btn.dataset.value)) {
          btn.classList.add('selected');
        }
      });
      updateState();
    } catch (err) {
      console.warn('Failed to load current problem selections:', err);
    }
  }

  nextBtn.addEventListener('click', async () => {
    if (nextBtn.classList.contains('is-disabled')) return;

    const values = Array.from(list.querySelectorAll('.opt.selected')).map(b => Number(b.dataset.value));
    const filtered = values.filter((value, index, arr) => !Number.isNaN(value) && arr.indexOf(value) === index);

    if (filtered.length === 0) { alert('최소 1개 이상 선택해주세요.'); return; }

    nextBtn.classList.add('is-disabled');
    nextBtn.setAttribute('aria-disabled','true');

    const CSRF = document.querySelector('meta[name="csrf-token"]')?.content;

    try{
      const res = await fetch(SAVE_ENDPOINT, {
        method: 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-Requested-With': 'XMLHttpRequest',
          ...(CSRF ? {'X-CSRF-Token': CSRF} : {})
        },
        body: JSON.stringify({ categoryIds: filtered })
      });
      if (!res.ok) throw new Error('HTTP ' + res.status);

      // Clear cached affirmations so new ones are generated based on updated problems
      sessionStorage.removeItem('generated_affirmations');
      sessionStorage.removeItem('affirmation_idx');

      location.href = NEXT_URL;
    } catch (err) {
      console.error('submit failed:', err);
      alert('저장에 실패했어요. 네트워크 상태를 확인하고 다시 시도해주세요.');
      nextBtn.classList.remove('is-disabled');
      nextBtn.removeAttribute('aria-disabled');
    }
  });
});
