// Enhanced AuctionWebSocket class with UI integration
class AuctionWebSocket {
    constructor(auctionId) {
        this.auctionId = auctionId;
        this.websocket = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;
        this.notificationCount = 0;
        this.isConnected = false;
        this.isConnecting = false;

        this.connect();
    }

    connect() {
        if (this.isConnecting || this.isConnected) {
            console.log('Already connecting or connected');
            return;
        }

        this.isConnecting = true;
        this.updateConnectionButtons();

        try {
            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const host = window.location.host;
            const contextPath = window.location.pathname.split('/')[1];

            // Multiple URL attempts for better compatibility
            const urls = [
                `${protocol}//${host}/${contextPath}/auction-updates/${this.auctionId}`,
                `${protocol}//${host}/auction-updates/${this.auctionId}`,
                `${protocol}//${host}:8080/${contextPath}/auction-updates/${this.auctionId}`,
                `${protocol}//${host}:8080/auction-updates/${this.auctionId}`
            ];

            const wsUrl = urls[0]; // Try first URL

            console.log('Connecting to WebSocket:', wsUrl);
            this.addNotification(`Connecting to auction ${this.auctionId}...`, 'info');
            this.addNotification(`WebSocket URL: ${wsUrl}`, 'info');

            this.websocket = new WebSocket(wsUrl);

            this.websocket.onopen = (event) => {
                console.log('WebSocket connected to auction:', this.auctionId);
                this.reconnectAttempts = 0;
                this.isConnected = true;
                this.isConnecting = false;
                this.showConnectionStatus('Connected', 'connected');
                this.updateConnectionButtons();
                this.updateCurrentAuctionDisplay();
                this.addNotification(`✅ Successfully connected to auction ${this.auctionId}!`, 'success');
            };

            this.websocket.onmessage = (event) => {
                console.log('WebSocket message received:', event.data);
                try {
                    const message = JSON.parse(event.data);
                    this.handleMessage(message);
                } catch (e) {
                    console.error('Failed to parse WebSocket message:', e);
                    this.addNotification(`❌ Failed to parse message: ${e.message}`, 'error');
                }
            };

            this.websocket.onclose = (event) => {
                console.log('WebSocket connection closed:', event.code, event.reason);
                this.isConnected = false;
                this.isConnecting = false;
                this.showConnectionStatus('Disconnected', 'disconnected');
                this.updateConnectionButtons();
                this.resetAuctionDisplay();
                this.addNotification(`🔌 Connection closed (Code: ${event.code})`, 'warning');

                // Only auto-reconnect if it wasn't a manual disconnect
                if (event.code !== 1000) {
                    this.handleReconnect();
                }
            };

            this.websocket.onerror = (error) => {
                console.error('WebSocket error:', error);
                this.isConnected = false;
                this.isConnecting = false;
                this.showConnectionStatus('Connection Error', 'error');
                this.updateConnectionButtons();
                this.addNotification(`💥 WebSocket connection error occurred`, 'error');
            };

        } catch (e) {
            console.error('Failed to create WebSocket connection:', e);
            this.isConnecting = false;
            this.updateConnectionButtons();
            this.addNotification(`💥 Failed to create WebSocket: ${e.message}`, 'error');
            this.handleReconnect();
        }
    }

    updateConnectionButtons() {
        const connectBtn = document.getElementById('connect-btn');
        const disconnectBtn = document.getElementById('disconnect-btn');

        if (this.isConnecting) {
            connectBtn.disabled = true;
            connectBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Connecting...';
            disconnectBtn.disabled = true;
        } else if (this.isConnected) {
            connectBtn.disabled = true;
            connectBtn.innerHTML = '<i class="fas fa-plug"></i> Connect';
            disconnectBtn.disabled = false;
        } else {
            connectBtn.disabled = false;
            connectBtn.innerHTML = '<i class="fas fa-plug"></i> Connect';
            disconnectBtn.disabled = true;
        }
    }

    updateCurrentAuctionDisplay() {
        const currentAuctionElement = document.getElementById('current-auction-id');
        if (currentAuctionElement) {
            currentAuctionElement.textContent = `#${this.auctionId}`;
        }
    }

    resetAuctionDisplay() {
        // Reset current auction display
        const currentAuctionElement = document.getElementById('current-auction-id');
        if (currentAuctionElement) {
            currentAuctionElement.textContent = 'None';
        }

        // Reset bid display
        const highestBidElement = document.getElementById('highest-bid');
        if (highestBidElement) {
            highestBidElement.textContent = '$0.00';
        }

        const highestBidderElement = document.getElementById('highest-bidder');
        if (highestBidderElement) {
            highestBidderElement.textContent = 'None';
        }
    }

    handleMessage(message) {
        switch (message.type) {
            case 'connection':
                console.log('Connection message:', message.message);
                this.addNotification(`🔗 ${message.message}`, 'info');
                break;

            case 'bidUpdate':
                console.log('Bid update received:', message.data);
                this.updateBidDisplay(message.data);
                this.showBidNotification(message.data);
                this.addBidNotification(message.data);
                break;

            default:
                console.log('Unknown message type:', message.type);
                this.addNotification(`❓ Unknown message type: ${message.type}`, 'warning');
        }
    }

    updateBidDisplay(bidData) {
        // Update current highest bid display
        const highestBidElement = document.getElementById('highest-bid');
        if (highestBidElement) {
            highestBidElement.textContent = '$' + bidData.bidAmount.toFixed(2);
            // Add animation effect
            highestBidElement.style.transform = 'scale(1.1)';
            setTimeout(() => {
                highestBidElement.style.transform = 'scale(1)';
            }, 200);
        }

        // Update highest bidder display
        const highestBidderElement = document.getElementById('highest-bidder');
        if (highestBidderElement) {
            highestBidderElement.textContent = bidData.bidderUsername;
            // Add animation effect
            highestBidderElement.style.transform = 'scale(1.1)';
            setTimeout(() => {
                highestBidderElement.style.transform = 'scale(1)';
            }, 200);
        }

        // Add new bid to bid history
        this.addBidToHistory(bidData);
    }

    addBidToHistory(bidData) {
        const bidHistoryElement = document.getElementById('bid-history');
        if (bidHistoryElement) {
            // Remove empty state if it exists
            const emptyState = bidHistoryElement.querySelector('.empty-state');
            if (emptyState) {
                emptyState.remove();
            }

            const bidElement = document.createElement('div');
            bidElement.className = 'bid-item new-bid';
            bidElement.innerHTML = `
                <div class="bid-amount">$${bidData.bidAmount.toFixed(2)}</div>
                <div class="bid-user">${bidData.bidderUsername}</div>
                <div class="bid-time">${new Date().toLocaleTimeString()}</div>
            `;

            bidHistoryElement.insertBefore(bidElement, bidHistoryElement.firstChild);

            // Remove highlight after animation
            setTimeout(() => {
                bidElement.classList.remove('new-bid');
            }, 2000);

            // Limit history to 20 items
            const bidItems = bidHistoryElement.querySelectorAll('.bid-item');
            if (bidItems.length > 20) {
                bidItems[bidItems.length - 1].remove();
            }
        }
    }

    addBidNotification(bidData) {
        const currentTime = new Date();
        const timeString = currentTime.toLocaleTimeString();
        const dateString = currentTime.toLocaleDateString();

        // Get auction title (fallback if not provided)
        const auctionTitle = bidData.auctionTitle || bidData.title || bidData.itemTitle || `Auction Title #${this.auctionId}`;

        // Create detailed notification message
        const message = `🎯 New Bid Received!`;
        const details = `
            <strong>Bid Amount:</strong> $${bidData.bidAmount.toFixed(2)}<br>
            <strong>Bidder:</strong> ${bidData.bidderUsername}<br>
            <strong>Auction Title:</strong> ${auctionTitle}<br>
            <strong>Auction ID:</strong> ${this.auctionId}<br>
            <strong>Date:</strong> ${dateString}<br>
            <strong>Time:</strong> ${timeString}
        `;

        this.addNotificationWithDetails(message, details, 'bid');
    }

    showBidNotification(bidData) {
        // Get auction title (fallback if not provided)
        const auctionTitle = bidData.auctionTitle || bidData.title || bidData.itemTitle || `Auction Title #${this.auctionId}`;

        // Create enhanced toast notification
        const notification = document.createElement('div');
        notification.className = 'bid-notification';
        notification.innerHTML = `
            <div class="notification-content">
                <div class="bid-icon">
                    <i class="fas fa-gavel"></i>
                </div>
                <div class="bid-details">
                    <div class="bid-amount">$${bidData.bidAmount.toFixed(2)}</div>
                    <div class="bid-user">New bid by ${bidData.bidderUsername}</div>
                    <div class="auction-title">${auctionTitle}</div>
                </div>
            </div>
        `;

        document.body.appendChild(notification);

        // Show notification with sound effect (if supported)
        setTimeout(() => {
            notification.classList.add('show');
            // Optional: Add sound notification
            try {
                const audio = new Audio('data:audio/wav;base64,UklGRvQAAABXQVZFZm10IBAAAAABAAEBACAAAAAAAIBAAAAABAABAABAAP//TElTVAAAAAQAAABuZW1lAAAAAJAAABkCAAA=');
                audio.volume = 0.1;
                audio.play().catch(e => console.log('Audio play failed:', e));
            } catch (e) {
                // Ignore audio errors
            }
        }, 100);

        // Hide and remove notification
        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => {
                if (document.body.contains(notification)) {
                    document.body.removeChild(notification);
                }
            }, 300);
        }, 4000);
    }

    addNotification(message, type = 'info') {
        const notificationsContainer = document.getElementById('notifications');
        const time = new Date().toLocaleTimeString();

        // Remove empty state if it exists
        const emptyState = notificationsContainer.querySelector('.empty-state');
        if (emptyState) {
            emptyState.remove();
        }

        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;

        const iconMap = {
            success: 'fas fa-check-circle',
            error: 'fas fa-exclamation-circle',
            info: 'fas fa-info-circle',
            warning: 'fas fa-exclamation-triangle',
            bid: 'fas fa-gavel'
        };

        notification.innerHTML = `
            <div class="notification-header">
                <i class="${iconMap[type] || iconMap.info} notification-icon"></i>
                <span class="notification-time">${time}</span>
            </div>
            <div class="notification-message">${message}</div>
        `;

        notificationsContainer.insertBefore(notification, notificationsContainer.firstChild);

        // Update notification count
        this.notificationCount++;
        document.getElementById('notificationCount').textContent = `${this.notificationCount} events`;

        // Auto-scroll to top for new notifications
        notificationsContainer.scrollTop = 0;

        // Remove old notifications to prevent memory issues
        const notifications = notificationsContainer.querySelectorAll('.notification');
        if (notifications.length > 50) {
            notifications[notifications.length - 1].remove();
        }
    }

    addNotificationWithDetails(message, details, type = 'info') {
        const notificationsContainer = document.getElementById('notifications');
        const time = new Date().toLocaleTimeString();

        // Remove empty state if it exists
        const emptyState = notificationsContainer.querySelector('.empty-state');
        if (emptyState) {
            emptyState.remove();
        }

        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;

        const iconMap = {
            success: 'fas fa-check-circle',
            error: 'fas fa-exclamation-circle',
            info: 'fas fa-info-circle',
            warning: 'fas fa-exclamation-triangle',
            bid: 'fas fa-gavel'
        };

        notification.innerHTML = `
            <div class="notification-header">
                <i class="${iconMap[type] || iconMap.info} notification-icon"></i>
                <span class="notification-time">${time}</span>
            </div>
            <div class="notification-message">${message}</div>
            <div class="notification-details">${details}</div>
        `;

        notificationsContainer.insertBefore(notification, notificationsContainer.firstChild);

        // Update notification count
        this.notificationCount++;
        document.getElementById('notificationCount').textContent = `${this.notificationCount} events`;

        // Auto-scroll to top for new notifications
        notificationsContainer.scrollTop = 0;

        // Remove old notifications to prevent memory issues
        const notifications = notificationsContainer.querySelectorAll('.notification');
        if (notifications.length > 50) {
            notifications[notifications.length - 1].remove();
        }
    }

    showConnectionStatus(status, type) {
        const statusContainer = document.getElementById('websocket-status-container');
        const statusElement = document.getElementById('websocket-status');

        if (statusContainer && statusElement) {
            // Update container class
            statusContainer.className = `status status-${type}`;

            // Update status indicator
            statusElement.className = `status-indicator status-${type}`;
            statusElement.querySelector('span').textContent = status;
        }
    }

    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts && !this.isConnected) {
            this.reconnectAttempts++;
            this.addNotification(`🔄 Reconnecting... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`, 'warning');
            console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

            setTimeout(() => {
                if (!this.isConnected) { // Only reconnect if still disconnected
                    this.connect();
                }
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.log('Max reconnection attempts reached');
            this.showConnectionStatus('Connection Failed', 'error');
            this.addNotification(`❌ Max reconnection attempts reached. Please try manually.`, 'error');
        }
    }

    disconnect() {
        if (this.websocket && this.isConnected) {
            this.isConnected = false;
            this.websocket.close(1000, 'User disconnect'); // Normal closure
            this.addNotification(`🔌 Disconnected by user`, 'warning');
            this.updateConnectionButtons();
            this.resetAuctionDisplay();
        }
    }
}

// Global variables
let auctionWebSocket = null;

// Enhanced Auction ID Controls
function incrementAuctionId() {
    const auctionIdInput = document.getElementById('auction-id');
    let currentValue = parseInt(auctionIdInput.value) || 1;
    if (currentValue < 9999) {
        auctionIdInput.value = currentValue + 1;
    }
}

function decrementAuctionId() {
    const auctionIdInput = document.getElementById('auction-id');
    let currentValue = parseInt(auctionIdInput.value) || 1;
    if (currentValue > 1) {
        auctionIdInput.value = currentValue - 1;
    }
}

// Control functions
function connectToAuction() {
    const auctionIdElement = document.getElementById('auction-id');
    const auctionId = auctionIdElement.value;

    if (!auctionId) {
        addSystemNotification('Please enter an auction ID', 'warning');
        return;
    }

    // Disconnect existing connection if any
    if (auctionWebSocket && auctionWebSocket.isConnected) {
        auctionWebSocket.disconnect();
        // Wait a moment before creating new connection
        setTimeout(() => {
            auctionWebSocket = new AuctionWebSocket(auctionId);
        }, 500);
    } else {
        // Create new connection
        auctionWebSocket = new AuctionWebSocket(auctionId);
    }
}

function disconnectFromAuction() {
    if (auctionWebSocket && auctionWebSocket.isConnected) {
        auctionWebSocket.disconnect();
        auctionWebSocket = null;
    } else {
        addSystemNotification('No active connection to disconnect', 'warning');
    }
}

function clearAllLogs() {
    // Clear notifications
    const notificationsContainer = document.getElementById('notifications');
    notificationsContainer.innerHTML = `
        <div class="empty-state">
            <i class="fas fa-inbox"></i>
            <p>Logs cleared. Connect to start monitoring!</p>
        </div>
    `;

    // Clear bid history
    const bidHistoryContainer = document.getElementById('bid-history');
    bidHistoryContainer.innerHTML = `
        <div class="empty-state">
            <i class="fas fa-gavel"></i>
            <p>No bids yet. Connect to start monitoring!</p>
        </div>
    `;

    // Reset counters
    document.getElementById('notificationCount').textContent = '0 events';
    if (auctionWebSocket) {
        auctionWebSocket.notificationCount = 0;
    }

    addSystemNotification('All logs cleared successfully', 'info');
}

function addSystemNotification(message, type) {
    // Create a temporary notification system for when websocket is not available
    const notificationsContainer = document.getElementById('notifications');
    const time = new Date().toLocaleTimeString();

    // Remove empty state if it exists
    const emptyState = notificationsContainer.querySelector('.empty-state');
    if (emptyState) {
        emptyState.remove();
    }

    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;

    const iconMap = {
        success: 'fas fa-check-circle',
        error: 'fas fa-exclamation-circle',
        info: 'fas fa-info-circle',
        warning: 'fas fa-exclamation-triangle',
        bid: 'fas fa-gavel'
    };

    notification.innerHTML = `
        <div class="notification-header">
            <i class="${iconMap[type] || iconMap.info} notification-icon"></i>
            <span class="notification-time">${time}</span>
        </div>
        <div class="notification-message">${message}</div>
    `;

    notificationsContainer.insertBefore(notification, notificationsContainer.firstChild);
}

// Auto-initialize
window.onload = function() {
    addSystemNotification('🚀 Online Auction Notification Interface loaded successfully', 'success');
    addSystemNotification('👆 Enter an Auction ID and click Connect to begin monitoring', 'info');
};