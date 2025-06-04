class AuctionWebSocket {
    constructor(auctionId) {
        this.auctionId = auctionId;
        this.websocket = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;

        this.connect();
    }

    connect() {
        try {
            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const wsUrl = `${protocol}//${window.location.host}${window.location.pathname.split('/')[1]}/auction-updates/${this.auctionId}`;

            console.log('Connecting to WebSocket:', wsUrl);
            this.websocket = new WebSocket(wsUrl);

            this.websocket.onopen = (event) => {
                console.log('WebSocket connected to auction:', this.auctionId);
                this.reconnectAttempts = 0;
                this.showConnectionStatus('Connected', 'success');
            };

            this.websocket.onmessage = (event) => {
                console.log('WebSocket message received:', event.data);
                try {
                    const message = JSON.parse(event.data);
                    this.handleMessage(message);
                } catch (e) {
                    console.error('Failed to parse WebSocket message:', e);
                }
            };

            this.websocket.onclose = (event) => {
                console.log('WebSocket connection closed:', event.code, event.reason);
                this.showConnectionStatus('Disconnected', 'error');
                this.handleReconnect();
            };

            this.websocket.onerror = (error) => {
                console.error('WebSocket error:', error);
                this.showConnectionStatus('Connection Error', 'error');
            };

        } catch (e) {
            console.error('Failed to create WebSocket connection:', e);
            this.handleReconnect();
        }
    }

    handleMessage(message) {
        switch (message.type) {
            case 'connection':
                console.log('Connection message:', message.message);
                break;

            case 'bidUpdate':
                console.log('Bid update received:', message.data);
                this.updateBidDisplay(message.data);
                this.showBidNotification(message.data);
                break;

            default:
                console.log('Unknown message type:', message.type);
        }
    }

    updateBidDisplay(bidData) {
        // Update current highest bid display
        const highestBidElement = document.getElementById('highest-bid-' + this.auctionId);
        if (highestBidElement) {
            highestBidElement.textContent = '$' + bidData.bidAmount.toFixed(2);
        }

        // Update highest bidder display
        const highestBidderElement = document.getElementById('highest-bidder-' + this.auctionId);
        if (highestBidderElement) {
            highestBidderElement.textContent = bidData.bidderUsername;
        }

        // Add new bid to bid history
        this.addBidToHistory(bidData);
    }

    addBidToHistory(bidData) {
        const bidHistoryElement = document.getElementById('bid-history-' + this.auctionId);
        if (bidHistoryElement) {
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
        }
    }

    showBidNotification(bidData) {
        // Create toast notification
        const notification = document.createElement('div');
        notification.className = 'bid-notification';
        notification.innerHTML = `
            <div class="notification-content">
                <strong>New Bid!</strong><br>
                $${bidData.bidAmount.toFixed(2)} by ${bidData.bidderUsername}
            </div>
        `;

        document.body.appendChild(notification);

        // Show notification
        setTimeout(() => {
            notification.classList.add('show');
        }, 100);

        // Hide and remove notification
        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => {
                document.body.removeChild(notification);
            }, 300);
        }, 3000);
    }

    showConnectionStatus(status, type) {
        const statusElement = document.getElementById('websocket-status');
        if (statusElement) {
            statusElement.textContent = status;
            statusElement.className = `connection-status ${type}`;
        }
    }

    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

            setTimeout(() => {
                this.connect();
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.log('Max reconnection attempts reached');
            this.showConnectionStatus('Connection Failed', 'error');
        }
    }

    disconnect() {
        if (this.websocket) {
            this.websocket.close();
        }
    }
}

// Auto-connect when page loads
document.addEventListener('DOMContentLoaded', function() {
    const auctionIdElement = document.getElementById('auction-id');
    if (auctionIdElement) {
        const auctionId = auctionIdElement.value;
        window.auctionWebSocket = new AuctionWebSocket(auctionId);
    }
});