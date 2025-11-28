

// Login page JavaScript


document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    
    if (loginForm) {
        const api = new BankAPI();
        
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            const errorMessage = document.getElementById('errorMessage');
            
            try {
                const user = await api.login(username, password);
                window.location.href = '/dashboard.html';
            } catch (error) {
                errorMessage.textContent = error.message;
            }
        });
    }
});