/* 首页：馆藏展示、搜索、借阅 */
const state = { page: 1, size: 8, keyword: '', pages: 1 };

const grid = document.getElementById('book-grid');
const emptyBox = document.getElementById('book-empty');
const pager = document.getElementById('pager');
const pageInfo = document.getElementById('page-info');
const resultTip = document.getElementById('result-tip');

async function loadStats() {
    try {
        const stats = await http.get('/books/stats');
        document.getElementById('stat-titles').textContent = stats.titles;
        document.getElementById('stat-total').textContent = stats.totalCopies;
        document.getElementById('stat-available').textContent = stats.available;
        document.getElementById('stat-borrowed').textContent = stats.borrowed;
    } catch (e) {
        /* 统计失败不影响主体内容 */
    }
}

function renderBooks(page) {
    state.pages = page.pages || 1;
    resultTip.textContent = '共 ' + page.total + ' 种图书';

    if (!page.records.length) {
        grid.innerHTML = '';
        emptyBox.style.display = 'block';
    } else {
        emptyBox.style.display = 'none';
        grid.innerHTML = page.records.map(function (book, index) {
            const coverClass = 'c' + ((Number(book.id) + index) % 5 + 1);
            const firstChar = book.title ? book.title.charAt(0) : '书';
            const canBorrow = book.availableCopies > 0;
            const btnText = window.APP.role === 'READER' ? '借阅' : (window.APP.role === 'ADMIN' ? '管理员不可借阅' : '登录后借阅');
            const disabled = !canBorrow || window.APP.role === 'ADMIN' ? 'disabled' : '';

            return '' +
                '<article class="book-card">' +
                '  <div class="book-cover ' + coverClass + '">' +
                '    <span class="book-asset">可借 ' + book.availableCopies + ' / ' + book.totalCopies + '</span>' +
                '    ' + esc(firstChar) +
                '  </div>' +
                '  <div class="book-body">' +
                '    <div class="book-title" title="' + esc(book.title) + '">' + esc(book.title) + '</div>' +
                '    <div class="book-author">' + esc(book.author) + '</div>' +
                '    <div class="book-meta">' +
                '      <span class="tag blue">' + esc(book.category || '未分类') + '</span>' +
                '      <span>' + esc(book.publisher || '') + '</span>' +
                '    </div>' +
                '    <div class="book-desc">' + esc(book.description || '暂无简介') + '</div>' +
                '    <div class="book-foot">' +
                '      <div class="stock ' + (canBorrow ? '' : 'empty') + '">可借 <strong>' + book.availableCopies + '</strong> / 共 ' + book.totalCopies + ' 本</div>' +
                '      <button type="button" class="btn btn-primary btn-sm js-borrow" data-id="' + book.id + '" ' + disabled + '>' + btnText + '</button>' +
                '    </div>' +
                '  </div>' +
                '</article>';
        }).join('');
    }

    if (page.pages > 1) {
        pager.style.display = 'flex';
        pageInfo.textContent = '第 ' + page.page + ' / ' + page.pages + ' 页';
        document.getElementById('prev-page').disabled = page.page <= 1;
        document.getElementById('next-page').disabled = page.page >= page.pages;
    } else {
        pager.style.display = 'none';
    }
}

async function loadBooks() {
    try {
        const page = await http.get('/books', {
            params: { keyword: state.keyword, page: state.page, size: state.size }
        });
        renderBooks(page);
    } catch (e) {
        toast(e.message, 'error');
    }
}

async function borrowBook(bookId) {
    if (!window.APP.role) {
        toast('请先登录后再借阅', 'info');
        setTimeout(function () {
            window.location.href = '/login';
        }, 700);
        return;
    }
    if (window.APP.role !== 'READER') {
        toast('管理员账号不参与借阅', 'info');
        return;
    }
    try {
        await http.post('/borrows', null, { params: { bookId: bookId } });
        toast('借阅成功，请在“我的借阅”中查看', 'success');
        await Promise.all([loadStats(), loadBooks()]);
    } catch (e) {
        toast(e.message, 'error');
    }
}

grid.addEventListener('click', function (event) {
    const btn = event.target.closest('.js-borrow');
    if (btn) {
        borrowBook(btn.dataset.id);
    }
});

document.getElementById('btn-search').addEventListener('click', function () {
    state.keyword = document.getElementById('keyword').value.trim();
    state.page = 1;
    loadBooks();
});

document.getElementById('keyword').addEventListener('keydown', function (event) {
    if (event.key === 'Enter') {
        document.getElementById('btn-search').click();
    }
});

document.getElementById('btn-reset').addEventListener('click', function () {
    document.getElementById('keyword').value = '';
    state.keyword = '';
    state.page = 1;
    loadBooks();
});

document.getElementById('prev-page').addEventListener('click', function () {
    if (state.page > 1) {
        state.page -= 1;
        loadBooks();
    }
});

document.getElementById('next-page').addEventListener('click', function () {
    if (state.page < state.pages) {
        state.page += 1;
        loadBooks();
    }
});

loadStats();
loadBooks();
