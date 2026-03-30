/* ===== 댓글 목록 조회 ===== */
async function loadComments(postId) {
    try {
        const data = await apiGet(`/board/${postId}/comment/list`);
        const commentList = document.getElementById('comment-list');
        if (!commentList) return;

        commentList.innerHTML = '';

        if (data && data.length > 0) {
            data.forEach(comment => {
                const li = document.createElement('li');
                li.id = `comment-${comment.id}`;

                // 댓글 HTML 생성 (닉네임/날짜 왼쪽, 수정/삭제 버튼 오른쪽)
                li.innerHTML = `
                    <div id="comment-${comment.id}-header" style="display:flex; justify-content:space-between; align-items:center;">
                        <div>
                            <span id="comment-${comment.id}-nickname">${comment.nickname}</span>
                            <span id="comment-${comment.id}-date" style="margin-left:8px; color:#888; font-size:12px;">${formatDateFull(comment.createdAt)}</span>
                        </div>
                        <div id="comment-${comment.id}-buttons" style="display:none; gap:8px;">
                            <button onclick="showCommentUpdate(${comment.id}, '${comment.content}')" style="padding:4px 10px; border:1px solid #090909; border-radius:4px; font-size:12px; cursor:pointer;">수정</button>
                            <button onclick="deleteComment(${comment.id})" style="padding:4px 10px; border:1px solid #090909; border-radius:4px; font-size:12px; cursor:pointer;">삭제</button>
                        </div>
                    </div>
                    <div id="comment-${comment.id}-content" style="margin-top:6px;">${comment.content}</div>
                `;

                // 본인 댓글이면 수정/삭제 버튼 표시
                if (isLoggedIn()) {
                    apiGet('/account/info').then(userInfo => {
                        if (userInfo.id === comment.userId) {
                            const btnEl = document.getElementById(`comment-${comment.id}-buttons`);
                            if (btnEl) btnEl.style.display = 'flex';
                        }
                    });
                }

                commentList.appendChild(li);
            });
        } else {
            commentList.innerHTML = '<li>댓글이 없습니다.</li>';
        }
    } catch (e) {
        console.error('댓글 목록 조회 오류:', e);
    }
}

/* ===== 댓글 작성 ===== */
async function writeComment() {
    const content = document.getElementById('comment-content').value.trim();
    const errorEl = document.getElementById('comment-error');

    // 유효성 검사
    if (!content) {
        errorEl.textContent = '댓글 내용을 입력해주세요.';
        return;
    }
    if (content.length > 1000) {
        errorEl.textContent = '댓글은 1000자 이하로 작성해주세요.';
        return;
    }

    try {
        const data = await apiPost(`/board/${currentPostId}/comment/write`, { content });

        if (data.id) {
            // 작성 성공 시 입력창 초기화 후 댓글 목록 새로고침
            document.getElementById('comment-content').value = '';
            errorEl.textContent = '';
            await loadComments(currentPostId);
        } else {
            errorEl.textContent = data.message || '댓글 작성 중 오류가 발생했습니다.';
        }
    } catch (e) {
        errorEl.textContent = '댓글 작성 중 오류가 발생했습니다.';
    }
}

/* ===== 댓글 수정 폼 표시 ===== */
function showCommentUpdate(commentId, currentContent) {
    // 수정/삭제 버튼 숨기기
    const btnEl = document.getElementById(`comment-${commentId}-buttons`);
    if (btnEl) btnEl.style.display = 'none';

    // 댓글 내용을 수정 폼으로 교체
    const contentEl = document.getElementById(`comment-${commentId}-content`);
    contentEl.innerHTML = `
        <textarea id="comment-update-content" rows="3" style="width:100%; margin-top:6px;">${currentContent}</textarea>
        <div style="display:flex; justify-content:flex-start; gap:8px; margin-top:6px;">
            <button onclick="updateComment(${commentId})" style="padding:4px 10px; border:1px solid #090909; border-radius:4px; font-size:12px; cursor:pointer;">수정 완료</button>
            <button onclick="loadComments(${currentPostId})" style="padding:4px 10px; border:1px solid #090909; border-radius:4px; font-size:12px; cursor:pointer;">취소</button>
        </div>
    `;
}

/* ===== 댓글 수정 완료 ===== */
async function updateComment(commentId) {
    const content = document.getElementById('comment-update-content').value.trim();

    // 유효성 검사
    if (!content) {
        alert('댓글 내용을 입력해주세요.');
        return;
    }
    if (content.length > 1000) {
        alert('댓글은 1000자 이하로 작성해주세요.');
        return;
    }

    try {
        const data = await apiPost(`/board/${currentPostId}/comment/update`, {
            id: commentId,
            content
        });

        if (data.message === '댓글이 수정되었습니다.') {
            // 수정 성공 시 댓글 목록 새로고침
            await loadComments(currentPostId);
        } else {
            alert(data.message || '댓글 수정 중 오류가 발생했습니다.');
        }
    } catch (e) {
        console.error('댓글 수정 오류:', e);
    }
}

/* ===== 댓글 삭제 ===== */
async function deleteComment(commentId) {
    if (!confirm('댓글을 삭제하시겠습니까?')) return;

    try {
        const data = await apiPost(`/board/${currentPostId}/comment/delete`, {
            comment_id: commentId
        });

        if (data.message === '댓글이 삭제되었습니다.') {
            // 삭제 성공 시 댓글 목록 새로고침
            await loadComments(currentPostId);
        } else {
            alert(data.message || '댓글 삭제 중 오류가 발생했습니다.');
        }
    } catch (e) {
        console.error('댓글 삭제 오류:', e);
    }
}