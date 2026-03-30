/* ===== 페이지 초기화 ===== */
document.addEventListener('DOMContentLoaded', async () => {
    // 로그인 상태 UI 업데이트
    updateAuthUI();

    // 로그인 상태면 사용자 정보 로드
    if (isLoggedIn()) {
        await loadUserInfo();
    }

    // 게시판 탭 로드
    await loadBoards();
});

/* ===== 게시판 목록 로드 ===== */
async function loadBoards() {
    try {
        const data = await apiGet('/boards');
        if (data && data.length > 0) {
            await loadPosts(data[0].id, 1);
        }
    } catch (e) {
        console.error('게시판 목록 로드 오류:', e);
    }
}

/* ===== 마이페이지 오픈 시 데이터 로드 ===== */
const _showOverlay = showOverlay;
window.showOverlay = async function(id) {
    await _showOverlay(id);

    if (id === 'overlay-mypage') {
        await loadUserInfo();
        await loadMyPosts();
    }
};