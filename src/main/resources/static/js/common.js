/* 全局 axios 封装与通用工具 */
const http = axios.create({ baseURL: '/api', timeout: 10000 });

http.interceptors.response.use(
    function (response) {
        return response.data.data;
    },
    function (error) {
        const status = error.response ? error.response.status : 0;
        const body = error.response ? error.response.data : null;
        const message = (body && body.message) || (status ? '请求失败，请稍后重试' : '无法连接服务器，请确认后端已启动');

        if (status === 401 && window.location.pathname !== '/login') {
            toast(message, 'error');
            setTimeout(function () {
                window.location.href = '/login';
            }, 800);
        }
        return Promise.reject(new Error(message));
    }
);

function toast(message, type) {
    let box = document.getElementById('toast-box');
    if (!box) {
        box = document.createElement('div');
        box.id = 'toast-box';
        document.body.appendChild(box);
    }
    const item = document.createElement('div');
    item.className = 'toast-item ' + (type || 'info');
    item.textContent = message;
    box.appendChild(item);
    setTimeout(function () {
        item.style.opacity = '0';
        item.style.transition = 'opacity .25s';
        setTimeout(function () {
            item.remove();
        }, 250);
    }, 2600);
}

async function logout() {
    try {
        await http.post('/auth/logout');
    } catch (e) {
        /* 忽略退出失败，直接回到首页 */
    }
    window.location.href = '/';
}

function esc(value) {
    return String(value == null ? '' : value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function fmtDate(value) {
    return value ? String(value) : '—';
}

function todayStr() {
    const d = new Date();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return d.getFullYear() + '-' + m + '-' + day;
}

function borrowStatusTag(record) {
    if (record.status === 'RETURNED') {
        return '<span class="tag green">已归还</span>';
    }
    if (record.dueDate && record.dueDate < todayStr()) {
        return '<span class="tag red">已逾期</span>';
    }
    return '<span class="tag orange">借阅中</span>';
}
