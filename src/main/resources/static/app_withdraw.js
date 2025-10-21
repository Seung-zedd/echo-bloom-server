    document.getElementById('goBack').addEventListener('click', () => {
    window.location.href = 'setting.html';
  });
  
  (function(){
    const agree = document.getElementById('agreeWithdraw');
    const btn = document.getElementById('withdrawBtn');

    function syncBtn(){
      const on = !!agree.checked;
      btn.disabled = !on;
      btn.setAttribute('aria-disabled', String(!on));
      btn.classList.toggle('enabled', on);
    }
    agree.addEventListener('change', syncBtn);
    syncBtn();

    btn.addEventListener('click', async (e) => {
      if (btn.disabled) return;
      // 탈퇴 로직 처리해주는 서버
      try {
        btn.textContent = '처리 중...';
        const res = await fetch('/api/users/me/withdraw', {
          method: 'DELETE',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include'
        });
        if (!res.ok) throw new Error('탈퇴 요청 실패');
        
        // Redirect to goodbye page (cookies already cleared by backend)
        location.href = './withdraw_done.html';
      } catch (err) {
        alert('잠시 후 다시 시도해 주세요.');
        console.error(err);
      } finally {
        btn.textContent = '회원 탈퇴하기';
      }
    });
  })();