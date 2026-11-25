/* 读者借阅记录 */
let records = [];

function render(records) {
    const body = document.getElementById('record-body');
    const empty = document.getElementById('record-empty');
    document.getElementById('record-tip').textContent = '共 ' + records.length + ' 条';

    const borrowing = records.filter(function (r) { return r.status === 'BORROWED'; }).length;
    const returned = records.filter(function (r) { return r.status === 'RETURNED'; }).length;
    const overdue = records.filter(function (r) {
        return r.status === 'BORROWED' && r.dueDate && r.dueDate < todayStr();
    }).length;

    document.getElementById('stat-borrowing').textContent = borrowing;
    document.getElementById('stat-returned').textContent = returned;
    document.getElementById('stat-overdue').textContent = overdue;
    document.getElementById('stat-total').textContent = records.length;

    if (!records.length) {
        body.innerHTML = '';
        empty.style.display = 'block';
        return;
    }
    empty.style.display = 'none';
    body.innerHTML = records.map(function (r) {
        const canReturn = r.status === 'BORROWED';
        return '' +
            '<tr>' +
            '  <td class="mono">#' + r.id + '</td>' +
            '  <td><div class="strong">' + esc(r.bookTitle || '—') + '</div><div class="sub">图书编号 ' + r.bookId + '</div></td>' +
            '  <td class="nowrap">' + fmtDate(r.borrowDate) + '</td>' +
            '  <td class="nowrap">' + fmtDate(r.dueDate) + '</td>' +
            '  <td class="nowrap">' + fmtDate(r.returnDate) + '</td>' +
            '  <td>' + borrowStatusTag(r) + '</td>' +
            '  <td class="right">' + (canReturn
            ? '<button type="button" class="btn btn-ghost btn-sm js-return" data-id="' + r.id + '">归还</button>'
            : '<span class="mono">已完成</span>') + '</td>' +
            '</tr>';
    }).join('');
}

async function loadRecords() {
    try {
        records = await http.get('/borrows/my');
        render(records);
    } catch (e) {
        toast(e.message, 'error');
    }
}

document.getElementById('record-body').addEventListener('click', async function (event) {
    const btn = event.target.closest('.js-return');
    if (!btn) {
        return;
    }
    if (!window.confirm('确认归还这本图书吗？')) {
        return;
    }
    try {
        await http.put('/borrows/' + btn.dataset.id + '/return');
        toast('归还成功', 'success');
        await loadRecords();
    } catch (e) {
        toast(e.message, 'error');
    }
});

loadRecords();
