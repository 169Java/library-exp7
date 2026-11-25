/* 读者注册页 */
document.getElementById('register-form').addEventListener('submit', async function (event) {
    event.preventDefault();
    const button = document.getElementById('btn-register');

    const payload = {
        username: document.getElementById('username').value.trim(),
        name: document.getElementById('name').value.trim(),
        password: document.getElementById('password').value,
        phone: document.getElementById('phone').value.trim(),
        email: document.getElementById('email').value.trim()
    };
    const confirm = document.getElementById('confirm').value;

    if (!payload.username || !payload.name || !payload.password) {
        toast('请完整填写账号、姓名和密码', 'error');
        return;
    }
    if (payload.password !== confirm) {
        toast('两次输入的密码不一致', 'error');
        return;
    }

    button.disabled = true;
    button.textContent = '提交中...';
    try {
        await http.post('/auth/register', payload);
        toast('注册成功，即将跳转登录页', 'success');
        setTimeout(function () {
            window.location.href = '/login';
        }, 900);
    } catch (e) {
        toast(e.message, 'error');
        button.disabled = false;
        button.textContent = '提交注册';
    }
});
