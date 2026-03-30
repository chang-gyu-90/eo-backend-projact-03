let currentBoardId = null;
let currentPage = 1;
let currentPostId = null;
let currentCategory = '전체';

/* 게시물 목록 조회  */
async function loadPosts(boardId, page = 1) {
    currentBoardId = boardId;
    currentPage = page;

    try {
        let url = `/board/list?page=${page}`;
        if (boardId) url += `&boardId=${boardId}`;

        const data = await apiGet(url);
        const postList = document.getElementById('post-list');
        postList.innerHTML = '';

        if (data.content && data.content.length > 0) {
            data.content.forEach(post => {
                const tr = document.createElement('tr');
                tr.onclick = () => loadPostDetail(post.id);
                tr.innerHTML = `
                    <td>${post.category}</td>
                    <td>${post.title} ${post.commentCount > 0 ? `[${post.commentCount}]` : ''}</td>
                    <td>${post.nickname}</td>
                    <td>${formatDate(post.createdAt)}</td>
                    <td>${post.views}</td>
                    <td>${post.likes}</td>
                `;

                postList.appendChild(tr);
            });
            renderPagination(data.totalPages, page);
        } else {
            postList.innerHTML = '<tr><td colspan="6">게시물이 없습니다.</td></tr>';
            document.getElementById('pagination').innerHTML = '';
        }
    } catch (e) {
        console.error('게시물 목록 조회 오류:', e);
    }
}

async function loadMyPosts() {
    if (!isLoggedIn()) return;

    try {
        // 본인 정보 먼저 가져오기
        const userInfo = await apiGet('/account/info');
        const data = await apiGet(`/board/list?page=1`);
        const myPostsList = document.getElementById('my-posts-list');
        if (!myPostsList) return;

        myPostsList.innerHTML = '';

        if (data.content && data.content.length > 0) {
            // 본인 글만 필터링
            const myPosts = data.content.filter(post => post.userId === userInfo.id);

            let totalLikes = 0;
            let totalComments = 0;

            if (myPosts.length > 0) {
                myPosts.forEach(post => {
                    totalLikes += post.likes || 0;
                    totalComments += post.commentCount || 0;

                    const li = document.createElement('li');
                    li.innerHTML = `
                        <span style="flex:1; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${post.title}</span>
                        <span style="font-size:12px; color:#888; margin-left:8px;">${formatDate(post.createdAt)}</span>
                    `;
                    li.onclick = () => {
                        hideOverlay('overlay-mypage');
                        loadPostDetail(post.id);
                    };
                    myPostsList.appendChild(li);
                });
            } else {
                myPostsList.innerHTML = '<li style="padding:10px 0; color:#888;">작성한 게시물이 없습니다.</li>';
            }

            // 통계 업데이트
            const statPosts = document.querySelector('#stat-posts span');
            const statLikes = document.querySelector('#stat-likes span');
            const statComments = document.querySelector('#stat-comments span');

            if (statPosts) statPosts.textContent = myPosts.length;
            if (statLikes) statLikes.textContent = totalLikes;
            if (statComments) statComments.textContent = totalComments;

        } else {
            myPostsList.innerHTML = '<li style="padding:10px 0; color:#888;">작성한 게시물이 없습니다.</li>';
        }
    } catch (e) {
        console.error('내가 쓴 글 조회 오류:', e);
    }
}

/* ===== 카테고리 필터 ===== */
function filterCategory(category, btnEl) {
    currentCategory = category;

    document.querySelectorAll('#category-tab-list > li > button').forEach(btn => {
        btn.classList.remove('active');
    });
    btnEl.classList.add('active');

    // 카테고리 필터링
    const rows = document.querySelectorAll('#post-list > tr');
    rows.forEach(row => {
        const categoryCell = row.querySelector('td:first-child');
        if (!categoryCell) return;

        if (category === '전체' || category === '인기') {
            row.style.display = '';
        } else {
            row.style.display = categoryCell.textContent === category ? '' : 'none';
        }
    });
}

/* ===== 페이징 렌더링 ===== */
function renderPagination(totalPages, currentPage) {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement('button');
        btn.textContent = i;
        if (i === currentPage) btn.classList.add('active');
        btn.onclick = () => loadPosts(currentBoardId, i);
        pagination.appendChild(btn);
    }
}

/* ===== 게시물 상세 조회 ===== */
async function loadPostDetail(postId) {
    currentPostId = postId;

    try {
        const data = await apiGet(`/board/read?id=${postId}`);
        await showOverlay('overlay-post');

        document.getElementById('post-detail-category').textContent = data.category;
        document.getElementById('post-detail-title').textContent = data.title;
        document.getElementById('post-detail-nickname').textContent = data.nickname;
        document.getElementById('post-detail-date').textContent = formatDateFull(data.createdAt);
        document.getElementById('post-detail-views').textContent = `조회 ${data.views}`;
        document.getElementById('post-detail-likes').textContent = `추천 ${data.likes}`;
        document.getElementById('post-detail-content').textContent = data.content;

        const btnUpdate = document.getElementById('btn-post-update');
        const btnDelete = document.getElementById('btn-post-delete');

        // 버튼 먼저 숨기기
        btnUpdate.style.display = 'none';
        btnDelete.style.display = 'none';

        if (isLoggedIn()) {
            try {
                const userInfo = await apiGet('/account/info');
                if (userInfo.id === data.userId) {
                    btnUpdate.style.display = 'block';
                    btnDelete.style.display = 'block';
                }
            } catch (e) {
                console.error('유저정보 오류:', e);
            }
        }

        await loadComments(postId);

        const commentWrite = document.getElementById('comment-write');
        if (commentWrite) {
            commentWrite.style.display = isLoggedIn() ? 'flex' : 'none';
        }

    } catch (e) {
        console.error('게시물 상세 조회 오류:', e);
    }
}

/* ===== 게시물 작성 ===== */
async function writePost() {
    const category = document.getElementById('write-category').value;
    const title = document.getElementById('write-title').value.trim();
    const content = document.getElementById('write-content').value.trim();
    const errorEl = document.getElementById('write-error');

    if (!title || !content) {
        errorEl.textContent = '제목과 내용을 입력해주세요.';
        return;
    }
    if (title.length > 100) {
        errorEl.textContent = '제목은 100자 이하여야 합니다.';
        return;
    }

    const boardId = currentBoardId;

    if (!boardId) {
        errorEl.textContent = '게시판 정보를 불러올 수 없습니다.';
        return;
    }

    try {
        const data = await apiPost('/board/write', {
            boardId: boardId,
            category,
            title,
            content,
            likes: 0,
            views: 0
        });

        if (data.id) {
            hideOverlay('overlay-write');
            document.getElementById('write-category').value = '';
            document.getElementById('write-title').value = '';
            document.getElementById('write-content').value = '';
            errorEl.textContent = '';
            await loadPosts(currentBoardId, currentPage);
        } else {
            errorEl.textContent = data.message || '게시물 작성 중 오류가 발생했습니다.';
        }
    } catch (e) {
        errorEl.textContent = '게시물 작성 중 오류가 발생했습니다.';
    }
}

/* ===== 게시물 수정 ===== */
async function showPostUpdate() {
    try {
        const data = await apiGet(`/board/read?id=${currentPostId}`);
        await showOverlay('overlay-write');

        document.getElementById('write-category').value = data.category;
        document.getElementById('write-title').value = data.title;
        document.getElementById('write-content').value = data.content;

        const submitBtn = document.getElementById('write-submit');
        submitBtn.textContent = '수정 완료';
        submitBtn.onclick = updatePost;
    } catch (e) {
        console.error('게시물 수정 준비 오류:', e);
    }
}

async function updatePost() {
    const category = document.getElementById('write-category').value;
    const title = document.getElementById('write-title').value.trim();
    const content = document.getElementById('write-content').value.trim();
    const errorEl = document.getElementById('write-error');

    if (!title || !content) {
        errorEl.textContent = '제목과 내용을 입력해주세요.';
        return;
    }
    if (title.length > 100) {
        errorEl.textContent = '제목은 100자 이하여야 합니다.';
        return;
    }

    try {
        const data = await apiPost('/board/update', {
            id: currentPostId,
            category,
            title,
            content
        });

        if (data.message === '게시물이 수정되었습니다.') {
            hideOverlay('overlay-write');

            const submitBtn = document.getElementById('write-submit');
            submitBtn.textContent = '게시글 등록';
            submitBtn.onclick = writePost;

            await loadPostDetail(currentPostId);
        } else {
            errorEl.textContent = data.message || '게시물 수정 중 오류가 발생했습니다.';
        }
    } catch (e) {
        errorEl.textContent = '게시물 수정 중 오류가 발생했습니다.';
    }
}

/* ===== 게시물 삭제 ===== */
async function deletePost() {
    if (!confirm('게시물을 삭제하시겠습니까?')) return;

    try {
        const data = await apiPost('/board/delete', { id: currentPostId });

        if (data.message === '게시물이 삭제되었습니다.') {
            hideOverlay('overlay-post');
            await loadPosts(currentBoardId, currentPage);
        } else {
            alert(data.message || '게시물 삭제 중 오류가 발생했습니다.');
        }
    } catch (e) {
        console.error('게시물 삭제 오류:', e);
    }
}