const API_BASE = 'http://localhost:8080';

/* ===== 토큰 관리 ===== */
function getAccessToken() {
    return localStorage.getItem('accessToken');
}

function getRefreshToken() {
    return localStorage.getItem('refreshToken');
}

function setTokens(accessToken, refreshToken) {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
}

function clearTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
}

function isLoggedIn() {
    return !!getAccessToken();
}

/* ===== API 공통 호출 ===== */
async function apiGet(url) {
    const headers = { 'Content-Type': 'application/json' };
    if (isLoggedIn()) {
        headers['Authorization'] = `Bearer ${getAccessToken()}`;
    }
    const response = await fetch(API_BASE + url, {
        method: 'GET',
        headers
    });
    return response.json();
}

async function apiPost(url, body) {
    const headers = { 'Content-Type': 'application/json' };
    if (isLoggedIn()) {
        headers['Authorization'] = `Bearer ${getAccessToken()}`;
    }
    const response = await fetch(API_BASE + url, {
        method: 'POST',
        headers,
        body: JSON.stringify(body)
    });
    return response.json();
}

/* ===== 오버레이 관리 ===== */
async function showOverlay(id) {
    if (!document.getElementById(id)) {
        await loadOverlay(id);
    }

    document.getElementById(id).style.display = 'block';
    document.getElementById(id).style.zIndex = id === 'overlay-write' ? '1002' : '1000';
    document.getElementById('overlay-bg').style.display = 'block';
}

function hideOverlay(id) {
    const el = document.getElementById(id);
    if (el) el.style.display = 'none';

    // 회원가입 초기화
    if (id === 'overlay-signup') {
        const fields = ['signup-userId', 'signup-nickname', 'signup-password', 'signup-passwordConfirm', 'signup-email'];
        fields.forEach(fieldId => {
            const el = document.getElementById(fieldId);
            if (el) el.value = '';
        });
        const error = document.getElementById('signup-error');
        if (error) error.textContent = '';
    }

    // 로그인 초기화
    if (id === 'overlay-login') {
        const fields = ['login-userId', 'login-password'];
        fields.forEach(fieldId => {
            const el = document.getElementById(fieldId);
            if (el) el.value = '';
        });
        const error = document.getElementById('login-error');
        if (error) error.textContent = '';
    }

    // 정보수정 초기화
    if (id === 'overlay-update') {
        const fields = ['update-nickname', 'update-currentPassword', 'update-newPassword', 'update-newPasswordConfirm'];
        fields.forEach(fieldId => {
            const el = document.getElementById(fieldId);
            if (el) el.value = '';
        });
        const error = document.getElementById('update-error');
        if (error) error.textContent = '';
    }

    // 글쓰기 초기화
    if (id === 'overlay-write') {
        const category = document.getElementById('write-category');
        const title = document.getElementById('write-title');
        const content = document.getElementById('write-content');
        const error = document.getElementById('write-error');
        if (category) category.value = '';
        if (title) title.value = '';
        if (content) content.value = '';
        if (error) error.textContent = '';
    }

    const overlays = [
        'overlay-login',
        'overlay-signup',
        'overlay-mypage',
        'overlay-update',
        'overlay-write',
        'overlay-post'
    ];

    const anyVisible = overlays.some(overlayId => {
        const el = document.getElementById(overlayId);
        return el && el.style.display === 'block';
    });

    if (!anyVisible) {
        document.getElementById('overlay-bg').style.display = 'none';
    }
}

function switchOverlay(hideId, showId) {
    hideOverlay(hideId);
    showOverlay(showId);
}

function hideAllOverlays() {
    const overlays = [
        'overlay-login',
        'overlay-signup',
        'overlay-mypage',
        'overlay-update',
        'overlay-write',
        'overlay-post'
    ];
    overlays.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'none';
    });
    document.getElementById('overlay-bg').style.display = 'none';
}

async function loadOverlay(id) {
    const fileMap = {
        'overlay-login'  : '/html/overlay-login.html',
        'overlay-signup' : '/html/overlay-signup.html',
        'overlay-mypage' : '/html/overlay-mypage.html',
        'overlay-update' : '/html/overlay-update.html',
        'overlay-write'  : '/html/overlay-write.html',
        'overlay-post'   : '/html/overlay-post.html'
    };
    const response = await fetch(fileMap[id]);
    const html = await response.text();
    document.getElementById('overlay-container').insertAdjacentHTML('beforeend', html);
}

/* ===== 날짜 포맷 ===== */
function formatDate(dateStr) {
    const date = new Date(dateStr);
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${month}-${day}`;
}

function formatDateFull(dateStr) {
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
}

/* ===== 로그인 상태 UI 업데이트 ===== */
function updateAuthUI() {
    const authButtons = document.getElementById('auth-buttons');
    const userButtons = document.getElementById('user-buttons');
    const btnWrite = document.getElementById('btn-write');

    if (isLoggedIn()) {
        if (authButtons) authButtons.style.display = 'none';
        if (userButtons) userButtons.style.display = 'flex';
        if (btnWrite) btnWrite.style.display = 'block';
    } else {
        if (authButtons) authButtons.style.display = 'flex';
        if (userButtons) userButtons.style.display = 'none';
        if (btnWrite) btnWrite.style.display = 'none';
    }
}