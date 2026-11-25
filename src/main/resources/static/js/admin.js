/* 管理后台：图书、读者、借阅记录管理 */
const bookState = { page: 1, size: 10, keyword: '', pages: 1 };
const readerState = { page: 1, size: 10, keyword: '', pages: 1 };
const readerCache = {};

/* ---------- 标签页 ---------- */
document.querySelectorAll('.tab').forEach(function (tab) {
    tab.addEventListener('click', function () {
        document.querySelectorAll('.tab').forEach(function (t) { t.classList.remove('active'); });
        document.querySelectorAll('.tab-panel').forEach(function (p) { p.classList.remove('active'); });
        tab.classList.add('active');
        document.getElementById('panel-' + tab.dataset.tab).classList.add('active');
        if (tab.dataset.tab === 'readers') {
            loadReaders();
        } else if (tab.dataset.tab === 'borrows') {
            loadBorrows();
        }
    });
});

/* ---------- 弹窗 ---------- */
function openModal(id) {
    document.getElementById(id).classList.add('show');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('show');
}

document.querySelectorAll('[data-close]').forEach(function (btn) {
    btn.addEventListener('click', function () {
        closeModal(btn.dataset.close);
    });
});

document.querySelectorAll('.modal-mask').forEach(function (mask) {
    mask.addEventListener('click', function (event) {
        if (event.target === mask) {
            mask.classList.remove('show');
        }
    });
});

document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
        document.querySelectorAll('.modal-mask.show').forEach(function (m) {
            m.classList.remove('show');
        });
    }
});

/* ---------- 图书管理 ---------- */
async function loadBooks() {
    try {
        const page = await http.get('/books', {
            params: { keyword: bookState.keyword, page: bookState.page, size: bookState.size }
        });
        bookState.pages = page.pages || 1;
        const body = document.getElementById('book-body');
        const empty = document.getElementById('book-empty');

        if (!page.records.length) {
            body.innerHTML = '';
            empty.style.display = 'block';
        } else {
            empty.style.display = 'none';
            body.innerHTML = page.records.map(function (book) {
                return '' +
                    '<tr>' +
                    '  <td class="mono">#' + book.id + '</td>' +
                    '  <td><div class="strong">' + esc(book.title) + '</div><div class="sub">' + esc(book.author) + '</div></td>' +
                    '  <td class="mono">' + esc(book.isbn) + '</td>' +
                    '  <td><span class="tag blue">' + esc(book.category || '未分类') + '</span></td>' +
                    '  <td class="nowrap">¥ ' + Number(book.price || 0).toFixed(2) + '</td>' +
                    '  <td class="nowrap"><span class="tag ' + (book.availableCopies > 0 ? 'green' : 'red') + '">' +
                    book.availableCopies + ' / ' + book.totalCopies + '</span></td>' +
                    '  <td class="right nowrap">' +
                    '    <button type="button" class="btn-link js-book-edit" data-id="' + book.id + '">编辑</button>' +
                    '    <button type="button" class="btn-link danger js-book-del" data-id="' + book.id + '">删除</button>' +
                    '  </td>' +
                    '</tr>';
            }).join('');
        }

        const pager = document.getElementById('book-pager');
        if (page.pages > 1) {
            pager.style.display = 'flex';
            document.getElementById('book-page-info').textContent = '第 ' + page.page + ' / ' + page.pages + ' 页';
            document.getElementById('book-prev').disabled = page.page <= 1;
            document.getElementById('book-next').disabled = page.page >= page.pages;
        } else {
            pager.style.display = 'none';
        }
    } catch (e) {
        toast(e.message, 'error');
    }
}

function fillBookForm(book) {
    document.getElementById('book-modal-title').textContent = book ? '编辑图书' : '新增图书';
    document.getElementById('book-id').value = book ? book.id : '';
    document.getElementById('book-title').value = book ? book.title : '';
    document.getElementById('book-author').value = book ? book.author : '';
    document.getElementById('book-isbn').value = book ? book.isbn : '';
    document.getElementById('book-publisher').value = book ? (book.publisher || '') : '';
    document.getElementById('book-category').value = book ? (book.category || '') : '';
    document.getElementById('book-price').value = book ? book.price : '0.00';
    document.getElementById('book-total').value = book ? book.totalCopies : 1;
    document.getElementById('book-desc').value = book ? (book.description || '') : '';
}

document.getElementById('btn-book-add').addEventListener('click', function () {
    fillBookForm(null);
    openModal('book-modal');
});

document.getElementById('book-body').addEventListener('click', async function (event) {
    const editBtn = event.target.closest('.js-book-edit');
    const delBtn = event.target.closest('.js-book-del');

    if (editBtn) {
        try {
            const book = await http.get('/books/' + editBtn.dataset.id);
            fillBookForm(book);
            openModal('book-modal');
        } catch (e) {
            toast(e.message, 'error');
        }
    }

    if (delBtn) {
        if (!window.confirm('确定删除该图书吗？存在借阅记录的图书无法删除。')) {
            return;
        }
        try {
            await http.delete('/books/' + delBtn.dataset.id);
            toast('删除成功', 'success');
            await loadBooks();
        } catch (e) {
            toast(e.message, 'error');
        }
    }
});

document.getElementById('book-form').addEventListener('submit', async function (event) {
    event.preventDefault();
    const id = document.getElementById('book-id').value;
    const payload = {
        title: document.getElementById('book-title').value.trim(),
        author: document.getElementById('book-author').value.trim(),
        isbn: document.getElementById('book-isbn').value.trim(),
        publisher: document.getElementById('book-publisher').value.trim(),
        category: document.getElementById('book-category').value.trim(),
        price: Number(document.getElementById('book-price').value || 0),
        totalCopies: Number(document.getElementById('book-total').value || 1),
        description: document.getElementById('book-desc').value.trim()
    };
    if (!payload.title || !payload.author || !payload.isbn) {
        toast('书名、作者、ISBN 不能为空', 'error');
        return;
    }

    const button = document.getElementById('btn-book-save');
    button.disabled = true;
    try {
        if (id) {
            await http.put('/books/' + id, payload);
            toast('图书修改成功', 'success');
        } else {
            await http.post('/books', payload);
            toast('图书新增成功', 'success');
        }
        closeModal('book-modal');
        await loadBooks();
    } catch (e) {
        toast(e.message, 'error');
    } finally {
        button.disabled = false;
    }
});

document.getElementById('btn-book-search').addEventListener('click', function () {
    bookState.keyword = document.getElementById('book-keyword').value.trim();
    bookState.page = 1;
    loadBooks();
});

document.getElementById('book-keyword').addEventListener('keydown', function (event) {
    if (event.key === 'Enter') {
        document.getElementById('btn-book-search').click();
    }
});

document.getElementById('btn-book-reset').addEventListener('click', function () {
    document.getElementById('book-keyword').value = '';
    bookState.keyword = '';
    bookState.page = 1;
    loadBooks();
});

document.getElementById('book-prev').addEventListener('click', function () {
    if (bookState.page > 1) {
        bookState.page -= 1;
        loadBooks();
    }
});

document.getElementById('book-next').addEventListener('click', function () {
    if (bookState.page < bookState.pages) {
        bookState.page += 1;
        loadBooks();
    }
});

/* ---------- 读者管理 ---------- */
async function loadReaders() {
    try {
        const page = await http.get('/readers', {
            params: { keyword: readerState.keyword, page: readerState.page, size: readerState.size }
        });
        readerState.pages = page.pages || 1;
        const body = document.getElementById('reader-body');
        const empty = document.getElementById('reader-empty');

        if (!page.records.length) {
            body.innerHTML = '';
            empty.style.display = 'block';
        } else {
            empty.style.display = 'none';
            body.innerHTML = page.records.map(function (reader) {
                readerCache[reader.id] = reader;
                return '' +
                    '<tr>' +
                    '  <td class="mono">#' + reader.id + '</td>' +
                    '  <td><div class="strong">' + esc(reader.name) + '</div><div class="sub">' + esc(reader.username) + '</div></td>' +
                    '  <td class="nowrap">' + esc(reader.phone || '—') + '</td>' +
                    '  <td>' + esc(reader.email || '—') + '</td>' +
                    '  <td>' + (reader.status === 1
                    ? '<span class="tag green">正常</span>'
                    : '<span class="tag red">已禁用</span>') + '</td>' +
                    '  <td class="nowrap">' + esc(String(reader.createTime || '').replace('T', ' ').substring(0, 16)) + '</td>' +
                    '  <td class="right nowrap">' +
                    '    <button type="button" class="btn-link js-reader-edit" data-id="' + reader.id + '">编辑</button>' +
                    '    <button type="button" class="btn-link js-reader-toggle" data-id="' + reader.id + '">' +
                    (reader.status === 1 ? '禁用' : '启用') + '</button>' +
                    '    <button type="button" class="btn-link danger js-reader-del" data-id="' + reader.id + '">删除</button>' +
                    '  </td>' +
                    '</tr>';
            }).join('');
        }

        const pager = document.getElementById('reader-pager');
        if (page.pages > 1) {
            pager.style.display = 'flex';
            document.getElementById('reader-page-info').textContent = '第 ' + page.page + ' / ' + page.pages + ' 页';
            document.getElementById('reader-prev').disabled = page.page <= 1;
            document.getElementById('reader-next').disabled = page.page >= page.pages;
        } else {
            pager.style.display = 'none';
        }
    } catch (e) {
        toast(e.message, 'error');
    }
}

function fillReaderForm(reader) {
    document.getElementById('reader-modal-title').textContent = reader ? '编辑读者' : '新增读者';
    document.getElementById('reader-id').value = reader ? reader.id : '';
    document.getElementById('reader-username').value = reader ? reader.username : '';
    document.getElementById('reader-name').value = reader ? reader.name : '';
    document.getElementById('reader-password').value = '';
    document.getElementById('reader-status').value = reader ? String(reader.status) : '1';
    document.getElementById('reader-phone').value = reader ? (reader.phone || '') : '';
    document.getElementById('reader-email').value = reader ? (reader.email || '') : '';
}

document.getElementById('btn-reader-add').addEventListener('click', function () {
    fillReaderForm(null);
    openModal('reader-modal');
});

document.getElementById('reader-body').addEventListener('click', async function (event) {
    const editBtn = event.target.closest('.js-reader-edit');
    const toggleBtn = event.target.closest('.js-reader-toggle');
    const delBtn = event.target.closest('.js-reader-del');

    if (editBtn) {
        fillReaderForm(readerCache[editBtn.dataset.id]);
        openModal('reader-modal');
    }

    if (toggleBtn) {
        const reader = readerCache[toggleBtn.dataset.id];
        if (!reader) {
            return;
        }
        try {
            await http.put('/readers/' + reader.id, {
                username: reader.username,
                name: reader.name,
                phone: reader.phone,
                email: reader.email,
                status: reader.status === 1 ? 0 : 1
            });
            toast(reader.status === 1 ? '读者已禁用' : '读者已启用', 'success');
            await loadReaders();
        } catch (e) {
            toast(e.message, 'error');
        }
    }

    if (delBtn) {
        if (!window.confirm('确定删除该读者吗？存在借阅记录的读者无法删除。')) {
            return;
        }
        try {
            await http.delete('/readers/' + delBtn.dataset.id);
            toast('读者已删除', 'success');
            await loadReaders();
        } catch (e) {
            toast(e.message, 'error');
        }
    }
});

document.getElementById('reader-form').addEventListener('submit', async function (event) {
    event.preventDefault();
    const id = document.getElementById('reader-id').value;
    const payload = {
        username: document.getElementById('reader-username').value.trim(),
        name: document.getElementById('reader-name').value.trim(),
        password: document.getElementById('reader-password').value,
        phone: document.getElementById('reader-phone').value.trim(),
        email: document.getElementById('reader-email').value.trim(),
        status: Number(document.getElementById('reader-status').value)
    };
    if (!payload.username || !payload.name) {
        toast('读者账号和姓名不能为空', 'error');
        return;
    }

    const button = document.getElementById('btn-reader-save');
    button.disabled = true;
    try {
        if (id) {
            await http.put('/readers/' + id, payload);
            toast('读者信息修改成功', 'success');
        } else {
            await http.post('/readers', payload);
            toast('读者新增成功，初始密码为 123456', 'success');
        }
        closeModal('reader-modal');
        await loadReaders();
    } catch (e) {
        toast(e.message, 'error');
    } finally {
        button.disabled = false;
    }
});

document.getElementById('btn-reader-search').addEventListener('click', function () {
    readerState.keyword = document.getElementById('reader-keyword').value.trim();
    readerState.page = 1;
    loadReaders();
});

document.getElementById('reader-keyword').addEventListener('keydown', function (event) {
    if (event.key === 'Enter') {
        document.getElementById('btn-reader-search').click();
    }
});

document.getElementById('btn-reader-reset').addEventListener('click', function () {
    document.getElementById('reader-keyword').value = '';
    readerState.keyword = '';
    readerState.page = 1;
    loadReaders();
});

document.getElementById('reader-prev').addEventListener('click', function () {
    if (readerState.page > 1) {
        readerState.page -= 1;
        loadReaders();
    }
});

document.getElementById('reader-next').addEventListener('click', function () {
    if (readerState.page < readerState.pages) {
        readerState.page += 1;
        loadReaders();
    }
});

/* ---------- 借阅记录 ---------- */
async function loadBorrows() {
    try {
        const keyword = document.getElementById('borrow-keyword').value.trim();
        const records = await http.get('/borrows', { params: { keyword: keyword } });
        document.getElementById('borrow-tip').textContent = '共 ' + records.length + ' 条';
        const body = document.getElementById('borrow-body');
        const empty = document.getElementById('borrow-empty');

        if (!records.length) {
            body.innerHTML = '';
            empty.style.display = 'block';
            return;
        }
        empty.style.display = 'none';
        body.innerHTML = records.map(function (r) {
            return '' +
                '<tr>' +
                '  <td class="mono">#' + r.id + '</td>' +
                '  <td><div class="strong">' + esc(r.bookTitle || '—') + '</div><div class="sub">图书编号 ' + r.bookId + '</div></td>' +
                '  <td><div class="strong">' + esc(r.readerName || '—') + '</div><div class="sub">' + esc(r.readerUsername || '') + '</div></td>' +
                '  <td class="nowrap">' + fmtDate(r.borrowDate) + '</td>' +
                '  <td class="nowrap">' + fmtDate(r.dueDate) + '</td>' +
                '  <td class="nowrap">' + fmtDate(r.returnDate) + '</td>' +
                '  <td>' + borrowStatusTag(r) + '</td>' +
                '  <td class="right nowrap">' + (r.status === 'BORROWED'
                ? '<button type="button" class="btn-link js-return" data-id="' + r.id + '">登记归还</button>'
                : '<span class="mono">已完成</span>') + '</td>' +
                '</tr>';
        }).join('');
    } catch (e) {
        toast(e.message, 'error');
    }
}

document.getElementById('borrow-body').addEventListener('click', async function (event) {
    const btn = event.target.closest('.js-return');
    if (!btn) {
        return;
    }
    if (!window.confirm('确认将该借阅记录登记为已归还吗？')) {
        return;
    }
    try {
        await http.put('/borrows/' + btn.dataset.id + '/return');
        toast('归还登记成功', 'success');
        await loadBorrows();
    } catch (e) {
        toast(e.message, 'error');
    }
});

document.getElementById('btn-borrow-search').addEventListener('click', loadBorrows);
document.getElementById('borrow-keyword').addEventListener('keydown', function (event) {
    if (event.key === 'Enter') {
        loadBorrows();
    }
});
document.getElementById('btn-borrow-reset').addEventListener('click', function () {
    document.getElementById('borrow-keyword').value = '';
    loadBorrows();
});

loadBooks();
