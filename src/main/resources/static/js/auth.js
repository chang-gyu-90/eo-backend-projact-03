//  로그인
async function login() {
    const userId = document.getElementById('login-userId').value.trim();
    const password = document.getElementById('login-password').value.trim();
    const errorEl = document.getElementById('login-error');

    if (!userId || !password) {
        errorEl.textContent = '아이디와 비밀번호를 입력해주세요.';
        return;
    }

    try {
        const data = await apiPost('/account/login', { userId, password });

        if (data.accessToken) {
            setTokens(data.accessToken, data.refreshToken);
            hideOverlay('overlay-login');
            await loadUserInfo();
            updateAuthUI();
        } else {
            errorEl.textContent = data.message || '아이디 또는 비밀번호가 올바르지 않습니다.';
        }
    } catch (e) {
        errorEl.textContent = '로그인 중 오류가 발생했습니다.';
    }
}

// 회원가입
async function signup() {
    const userId = document.getElementById('signup-userId').value.trim();
    const nickname = document.getElementById('signup-nickname').value.trim();
    const password = document.getElementById('signup-password').value.trim();
    const passwordConfirm = document.getElementById('signup-passwordConfirm').value.trim();
    const email = document.getElementById('signup-email').value.trim();
    const errorEl = document.getElementById('signup-error');
    const checkResult = document.getElementById('userId-check-result');

    if (!userId || !nickname || !password || !passwordConfirm || !email) {
        errorEl.textContent = '모든 항목을 입력해주세요.';
        return;
    }
    if (password !== passwordConfirm) {
        errorEl.textContent = '비밀번호가 일치하지 않습니다.';
        return;
    }
    if (userId.length > 50) {
        errorEl.textContent = '아이디는 50자 이하여야 합니다.';
        return;
    }
    if (email.length > 20) {
        errorEl.textContent = '이메일은 20자 이하여야 합니다.';
        return;
    }
    if (nickname.length > 50) {
        errorEl.textContent = '닉네임은 50자 이하여야 합니다.';
        return;
    }

    // 중복확인 여부 체크
    if (!checkResult || checkResult.style.color !== 'green') {
        errorEl.textContent = '아이디 중복확인을 해주세요.';
        return;
    }

    try {
        const data = await apiPost('/account/signup', { userId, password, email, nickname });

        if (data.message === '회원가입이 완료되었습니다.') {
            switchOverlay('overlay-signup', 'overlay-login');
        } else {
            errorEl.textContent = data.message || '회원가입 중 오류가 발생했습니다.';
        }
    } catch (e) {
        errorEl.textContent = '회원가입 중 오류가 발생했습니다.';
    }
}

// 아이디 중복 체크
async function checkUserId() {
    const userId = document.getElementById('signup-userId').value.trim();
    const resultEl = document.getElementById('userId-check-result');

    if (!userId) {
        resultEl.textContent = '아이디를 입력해주세요.';
        resultEl.style.color = 'red';
        return;
    }

    try {
        const data = await apiPost('/account/check-userid', { userId });
        if (data.available) {
            resultEl.textContent = '사용 가능한 아이디입니다.';
            resultEl.style.color = 'green';
        } else {
            resultEl.textContent = '이미 사용중인 아이디입니다.';
            resultEl.style.color = 'red';
        }
    } catch (e) {
        resultEl.textContent = '중복 확인 중 오류가 발생했습니다.';
        resultEl.style.color = 'red';
    }
}

// 로그아웃
async function logout() {
    try {
        await apiPost('/account/logout', { refresh_token: getRefreshToken() });
    } catch (e) {
        console.error('로그아웃 오류:', e);
    } finally {
        clearTokens();
        hideAllOverlays();
        updateAuthUI();
        document.getElementById('user-nickname').textContent = '';
    }
}

// 사용자 정보 조회
async function loadUserInfo() {
    if (!isLoggedIn()) return;

    try {
        const data = await apiGet('/account/info');

        if (data.userId) {
            document.getElementById('user-nickname').textContent = data.nickname;

            if (document.getElementById('profile-nickname')) {
                document.getElementById('profile-nickname').textContent = data.nickname;
                document.getElementById('profile-userId').textContent = `@${data.userId}`;
                document.getElementById('profile-joinDate').textContent = `가입일 ${formatDateFull(data.createdAt)}`;
                document.getElementById('profile-avatar').textContent = data.nickname.charAt(0);
            }
        }
    } catch (e) {
        console.error('사용자 정보 조회 오류:', e);
    }
}

// 닉네임 수정
async function updateNickname() {
    const nickname = document.getElementById('update-nickname').value.trim();
    const errorEl = document.getElementById('update-nickname-error');

    if (!nickname) {
        errorEl.textContent = '닉네임을 입력해주세요.';
        errorEl.style.color = 'red';
        return;
    }
    if (nickname.length > 50) {
        errorEl.textContent = '닉네임은 50자 이하여야 합니다.';
        errorEl.style.color = 'red';
        return;
    }

    try {
        const userInfo = await apiGet('/account/info');
        const data = await apiPost('/account/update', {
            password: 'KEEP_CURRENT',
            nickname
        });

        if (data.userId) {
            errorEl.textContent = '닉네임이 변경되었습니다.';
            errorEl.style.color = 'green';
            await loadUserInfo();
        } else {
            errorEl.textContent = data.message || '닉네임 변경 중 오류가 발생했습니다.';
            errorEl.style.color = 'red';
        }
    } catch (e) {
        errorEl.textContent = '닉네임 변경 중 오류가 발생했습니다.';
        errorEl.style.color = 'red';
    }
}

// 비밀번호 수정
async function updatePassword() {
    const currentPassword = document.getElementById('update-currentPassword').value.trim();
    const newPassword = document.getElementById('update-newPassword').value.trim();
    const newPasswordConfirm = document.getElementById('update-newPasswordConfirm').value.trim();
    const errorEl = document.getElementById('update-error');

    if (!currentPassword || !newPassword || !newPasswordConfirm) {
        errorEl.textContent = '모든 항목을 입력해주세요.';
        errorEl.style.color = 'red';
        return;
    }
    if (newPassword !== newPasswordConfirm) {
        errorEl.textContent = '새 비밀번호가 일치하지 않습니다.';
        errorEl.style.color = 'red';
        return;
    }

    try {
        const userInfo = await apiGet('/account/info');
        const data = await apiPost('/account/update', {
            password: newPassword,
            nickname: userInfo.nickname
        });

        if (data.userId) {
            // 비밀번호 변경 성공 시 자동 로그아웃 후 로그인 창 띄우기
            alert('비밀번호가 변경되었습니다. 다시 로그인해주세요.');
            clearTokens();
            hideAllOverlays();
            updateAuthUI();
            document.getElementById('user-nickname').textContent = '';
            await showOverlay('overlay-login');
        } else {
            errorEl.textContent = data.message || '비밀번호 변경 중 오류가 발생했습니다.';
            errorEl.style.color = 'red';
        }
    } catch (e) {
        errorEl.textContent = '비밀번호 변경 중 오류가 발생했습니다.';
        errorEl.style.color = 'red';
    }
}