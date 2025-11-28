class BankAPI {
    constructor() {
        this.baseURL = '/api';
        this.token = sessionStorage.getItem('token');
    }

    async login(username, password) {
        const response = await fetch(`${this.baseURL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Login failed');
        }

        const user = await response.json();
        this.token = user.token;  // Store token from response
        
        // Make sure username is included in stored user data
        if (!user.username) {
            user.username = username;  // Add username if backend didn't include it
        }
        
        sessionStorage.setItem('token', this.token);
        sessionStorage.setItem('currentUser', JSON.stringify(user));
        return user;
    }

    async logout() {
        const response = await fetch(`${this.baseURL}/logout`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${this.token}`
            }
        });

        sessionStorage.removeItem('currentUser');
        sessionStorage.removeItem('token');
        this.token = null;
    }

    getCurrentUser() {
        const userStr = sessionStorage.getItem('currentUser');
        return userStr ? JSON.parse(userStr) : null;
    }

    async getAccounts() {
        const response = await fetch(`${this.baseURL}/accounts`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${this.token}`
            }
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to fetch accounts');
        }

        return await response.json();
    }

    async getAccountsByUser(userId) {
        const response = await fetch(`${this.baseURL}/accounts/user/${userId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${this.token}`
            }
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to fetch user accounts');
        }

        return await response.json();
    }

    async getTransactionHistory(accountNumber) {
        const response = await fetch(`${this.baseURL}/transactions/${accountNumber}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${this.token}`
            }
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to fetch transaction history');
        }

        return await response.json();
    }

    async makeTransaction(transactionData) {
        const response = await fetch(`${this.baseURL}/transaction`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${this.token}`
            },
            body: JSON.stringify(transactionData)
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Transaction failed');
        }

        return await response.json();
    }

    async createUser(userData) {
        const response = await fetch(`${this.baseURL}/admin/create-user`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${this.token}`
            },
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to create user');
        }

        return await response.json();
    }

    async updateUser(userId, userData) {
        const response = await fetch(`${this.baseURL}/admin/users/${userId}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${this.token}`
            },
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to update user');
        }

        return await response.json();
    }

    async updatePassword({ currentPassword, newPassword }) {
    const response = await fetch('/api/change-password', {
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.token}`   // <- IMPORTANT
        },
        body: JSON.stringify({ currentPassword, newPassword })
    });

    if (!response.ok) {
        const errText = await response.text();
        throw new Error(errText || 'Failed to update password');
    }

    return await response.json();
}



    async searchUsers(query) {
        const response = await fetch(`${this.baseURL}/search?q=${encodeURIComponent(query)}`, {
            headers: {
                'Authorization': `Bearer ${this.token}`
            }
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Search failed');
        }

        return await response.json();
    }

    async getAuditLogs() {
        const response = await fetch(`${this.baseURL}/admin/audit-logs`, {
            headers: {
                'Authorization': `Bearer ${this.token}`
            }
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to fetch audit logs');
        }

        return await response.json();
    }

}