window.addEventListener('DOMContentLoaded', () => {
  fetch('/api/check-auth', {
    method: 'GET',
    credentials: 'include'
  })
    .then(response => {
      if (!response.ok) {
        alert('인증 오류! 재로그인해주세요.');
        window.location.href = 'main.html';
      }
    })
    .catch(error => console.error('인증 확인 오류:', error));
});
