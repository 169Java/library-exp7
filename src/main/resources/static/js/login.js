/* 登录页 */
document.getElementById('login-form').addEventListener('submit', async function (event) {
    event.preventDefault();
    const button = document.getElementById('btn-login');
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    if (!username || !password) {
        toast('请输入账号和密码', 'error');
        return;
    }

    button.disabled = true;
    button.textContent = '登录中...';
    try {
        const user = await http.post('/auth/login', { username: username, password: password });
        toast('欢迎回来，' + user.name, 'success');
        setTimeout(function () {
            window.location.href = user.role === 'ADMIN' ? '/admin' : '/';
        }, 500);
    } catch (e) {
        toast(e.message, 'error');
        button.disabled = false;
        button.textContent = '登 录';
    }
});
