<%--
  Enhanced Professional Landing Page
  Created by IntelliJ IDEA.
  User: ishara
  Date: 2025-06-07
  Time: 15:08 UTC
  Enhanced by: @isharax9
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nawwa Auction Solution</title>

    <!-- ===== FAVICON IMPLEMENTATION ===== -->
    <!-- Standard favicon -->
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">

    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        /* ===== ENHANCED PROFESSIONAL LANDING PAGE STYLES ===== */
        /* Copyright (c) 2025 Ishara Lakshitha (@isharax9). All rights reserved. */

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            line-height: 1.6;
            color: #333;
            overflow-x: hidden;
        }

        /* Animated Background */
        .bg-animation {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            overflow: hidden;
            z-index: -1;
        }

        .bg-animation::before {
            content: '';
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: linear-gradient(45deg, transparent, rgba(255,255,255,0.1), transparent);
            animation: rotate 20s linear infinite;
        }

        @keyframes rotate {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        /* Navigation Header */
        .nav-header {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            z-index: 1000;
            padding: 1rem 0;
            transition: all 0.3s ease;
        }

        .nav-content {
            max-width: 1400px;
            margin: 0 auto;
            padding: 0 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .nav-logo {
            display: flex;
            align-items: center;
            gap: 12px;
            font-size: 1.5rem;
            font-weight: 700;
            color: #333;
            text-decoration: none;
        }

        .nav-logo i {
            color: #667eea;
            font-size: 2rem;
        }

        .nav-links {
            display: flex;
            gap: 2rem;
            align-items: center;
        }

        .nav-link {
            color: #666;
            text-decoration: none;
            font-weight: 500;
            transition: color 0.3s ease;
        }

        .nav-link:hover {
            color: #667eea;
        }

        .nav-cta {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 12px 24px;
            border-radius: 25px;
            text-decoration: none;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        .nav-cta:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
        }

        /* Hero Section */
        .hero-section {
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 6rem 2rem 4rem;
            position: relative;
        }

        .hero-container {
            max-width: 1400px;
            width: 100%;
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 4rem;
            align-items: center;
        }

        .hero-content {
            color: white;
        }

        .hero-badge {
            background: rgba(255, 255, 255, 0.15);
            color: white;
            padding: 8px 20px;
            border-radius: 25px;
            font-size: 0.9rem;
            font-weight: 500;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 2rem;
            backdrop-filter: blur(10px);
        }

        .hero-title {
            font-size: 4rem;
            font-weight: 800;
            line-height: 1.1;
            margin-bottom: 1.5rem;
            background: linear-gradient(135deg, #ffffff 0%, #f0f0f0 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .hero-subtitle {
            font-size: 1.3rem;
            opacity: 0.9;
            margin-bottom: 2.5rem;
            font-weight: 400;
        }

        .hero-buttons {
            display: flex;
            gap: 1rem;
            flex-wrap: wrap;
        }

        .btn-primary {
            background: white;
            color: #667eea;
            padding: 16px 32px;
            border-radius: 50px;
            text-decoration: none;
            font-weight: 600;
            font-size: 1.1rem;
            display: flex;
            align-items: center;
            gap: 10px;
            transition: all 0.3s ease;
        }

        .btn-primary:hover {
            transform: translateY(-3px);
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
        }

        .btn-secondary {
            background: rgba(255, 255, 255, 0.1);
            color: white;
            padding: 16px 32px;
            border-radius: 50px;
            text-decoration: none;
            font-weight: 600;
            font-size: 1.1rem;
            display: flex;
            align-items: center;
            gap: 10px;
            transition: all 0.3s ease;
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.2);
        }

        .btn-secondary:hover {
            background: rgba(255, 255, 255, 0.2);
            transform: translateY(-3px);
        }

        /* Hero Dashboard Preview */
        .hero-preview {
            background: rgba(255, 255, 255, 0.1);
            border-radius: 20px;
            padding: 2rem;
            backdrop-filter: blur(20px);
            border: 1px solid rgba(255, 255, 255, 0.2);
            position: relative;
            overflow: hidden;
        }

        .preview-header {
            background: rgba(255, 255, 255, 0.9);
            border-radius: 15px;
            padding: 1.5rem;
            margin-bottom: 1.5rem;
            display: flex;
            align-items: center;
            justify-content: between;
        }

        .preview-title {
            color: #333;
            font-weight: 600;
            font-size: 1.1rem;
        }

        .preview-stats {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1rem;
            margin-bottom: 1.5rem;
        }

        .stat-card {
            background: rgba(255, 255, 255, 0.9);
            border-radius: 12px;
            padding: 1.5rem;
            text-align: center;
        }

        .stat-value {
            font-size: 2rem;
            font-weight: 700;
            color: #667eea;
            margin-bottom: 0.5rem;
        }

        .stat-label {
            color: #666;
            font-size: 0.9rem;
            font-weight: 500;
        }

        .live-indicator {
            position: absolute;
            top: 2rem;
            right: 2rem;
            background: #28a745;
            color: white;
            padding: 8px 15px;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 5px;
            animation: pulse 2s infinite;
        }

        .live-dot {
            width: 8px;
            height: 8px;
            background: white;
            border-radius: 50%;
            animation: blink 1s infinite;
        }

        @keyframes blink {
            0%, 50% { opacity: 1; }
            51%, 100% { opacity: 0; }
        }

        /* Features Section */
        .features-section {
            background: white;
            padding: 6rem 2rem;
            position: relative;
        }

        .section-container {
            max-width: 1400px;
            margin: 0 auto;
        }

        .section-header {
            text-align: center;
            margin-bottom: 4rem;
        }

        .section-badge {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 8px 20px;
            border-radius: 25px;
            font-size: 0.9rem;
            font-weight: 600;
            display: inline-block;
            margin-bottom: 1rem;
        }

        .section-title {
            font-size: 3rem;
            font-weight: 700;
            color: #333;
            margin-bottom: 1rem;
        }

        .section-subtitle {
            font-size: 1.2rem;
            color: #666;
            max-width: 600px;
            margin: 0 auto;
        }

        .features-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
            gap: 2rem;
        }

        .feature-card {
            background: #f8f9fa;
            border-radius: 20px;
            padding: 2.5rem;
            text-align: center;
            transition: all 0.3s ease;
            border-left: 5px solid transparent;
            position: relative;
            overflow: hidden;
        }

        .feature-card:hover {
            transform: translateY(-10px);
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
        }

        .feature-card:nth-child(1) { border-left-color: #667eea; }
        .feature-card:nth-child(2) { border-left-color: #28a745; }
        .feature-card:nth-child(3) { border-left-color: #fd7e14; }
        .feature-card:nth-child(4) { border-left-color: #dc3545; }
        .feature-card:nth-child(5) { border-left-color: #6f42c1; }
        .feature-card:nth-child(6) { border-left-color: #20c997; }

        .feature-icon {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 1.5rem;
            font-size: 2rem;
            color: white;
        }

        .feature-card:nth-child(1) .feature-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
        .feature-card:nth-child(2) .feature-icon { background: linear-gradient(135deg, #28a745, #20c997); }
        .feature-card:nth-child(3) .feature-icon { background: linear-gradient(135deg, #fd7e14, #ffc107); }
        .feature-card:nth-child(4) .feature-icon { background: linear-gradient(135deg, #dc3545, #e83e8c); }
        .feature-card:nth-child(5) .feature-icon { background: linear-gradient(135deg, #6f42c1, #e83e8c); }
        .feature-card:nth-child(6) .feature-icon { background: linear-gradient(135deg, #20c997, #17a2b8); }

        .feature-title {
            font-size: 1.3rem;
            font-weight: 600;
            color: #333;
            margin-bottom: 1rem;
        }

        .feature-desc {
            color: #666;
            line-height: 1.6;
            margin-bottom: 1.5rem;
        }

        .feature-tech {
            background: white;
            padding: 12px 20px;
            border-radius: 25px;
            font-size: 0.85rem;
            color: #667eea;
            font-weight: 600;
            display: inline-block;
        }

        /* Architecture Section */
        .architecture-section {
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
            padding: 6rem 2rem;
        }

        .architecture-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 4rem;
            align-items: center;
        }

        .architecture-diagram {
            background: white;
            border-radius: 20px;
            padding: 2rem;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
        }

        .layer {
            background: #f8f9fa;
            border-radius: 15px;
            padding: 1.5rem;
            margin-bottom: 1rem;
            border-left: 5px solid;
        }

        .layer:nth-child(1) { border-left-color: #667eea; }
        .layer:nth-child(2) { border-left-color: #28a745; }
        .layer:nth-child(3) { border-left-color: #fd7e14; }

        .layer-title {
            font-weight: 600;
            color: #333;
            margin-bottom: 0.5rem;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .layer-desc {
            color: #666;
            font-size: 0.9rem;
        }

        /* Stats Section */
        .stats-section {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 4rem 2rem;
            color: white;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 2rem;
            text-align: center;
        }

        .stat-item {
            padding: 2rem;
        }

        .stat-number {
            font-size: 3.5rem;
            font-weight: 800;
            line-height: 1;
            margin-bottom: 0.5rem;
        }

        .stat-text {
            font-size: 1.1rem;
            opacity: 0.9;
            font-weight: 500;
        }

        /* Tech Stack Section */
        .tech-section {
            background: white;
            padding: 6rem 2rem;
        }

        .tech-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 2rem;
            margin-top: 3rem;
        }

        .tech-category {
            background: #f8f9fa;
            border-radius: 20px;
            padding: 2rem;
            text-align: center;
        }

        .tech-category-icon {
            width: 60px;
            height: 60px;
            border-radius: 15px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 1rem;
            font-size: 1.5rem;
            color: white;
        }

        .tech-category:nth-child(1) .tech-category-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
        .tech-category:nth-child(2) .tech-category-icon { background: linear-gradient(135deg, #28a745, #20c997); }
        .tech-category:nth-child(3) .tech-category-icon { background: linear-gradient(135deg, #fd7e14, #ffc107); }
        .tech-category:nth-child(4) .tech-category-icon { background: linear-gradient(135deg, #dc3545, #e83e8c); }

        .tech-category-title {
            font-weight: 600;
            color: #333;
            margin-bottom: 1rem;
        }

        .tech-items {
            display: flex;
            flex-wrap: wrap;
            gap: 0.5rem;
            justify-content: center;
        }

        .tech-item {
            background: white;
            color: #666;
            padding: 6px 12px;
            border-radius: 15px;
            font-size: 0.85rem;
            font-weight: 500;
            border: 1px solid #e9ecef;
        }

        /* CTA Section */
        .cta-section {
            background: linear-gradient(135deg, #333 0%, #667eea 100%);
            padding: 6rem 2rem;
            text-align: center;
            color: white;
        }

        .cta-content {
            max-width: 800px;
            margin: 0 auto;
        }

        .cta-title {
            font-size: 3rem;
            font-weight: 700;
            margin-bottom: 1.5rem;
        }

        .cta-subtitle {
            font-size: 1.3rem;
            opacity: 0.9;
            margin-bottom: 3rem;
        }

        .cta-buttons {
            display: flex;
            gap: 1.5rem;
            justify-content: center;
            flex-wrap: wrap;
        }

        /* Footer */
        .footer {
            background: #333;
            color: #adb5bd;
            padding: 4rem 2rem 2rem;
        }

        .footer-grid {
            max-width: 1400px;
            margin: 0 auto;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 3rem;
            margin-bottom: 2rem;
        }

        .footer-section h4 {
            color: white;
            font-weight: 600;
            margin-bottom: 1rem;
        }

        .footer-section p,
        .footer-section a {
            color: #adb5bd;
            text-decoration: none;
            line-height: 1.8;
        }

        .footer-section a:hover {
            color: #667eea;
        }

        .footer-bottom {
            max-width: 1400px;
            margin: 0 auto;
            padding-top: 2rem;
            border-top: 1px solid #495057;
            text-align: center;
            color: #6c757d;
        }

        .social-links {
            display: flex;
            gap: 1rem;
            justify-content: center;
            margin-bottom: 1rem;
        }

        .social-link {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: #495057;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #adb5bd;
            text-decoration: none;
            transition: all 0.3s ease;
        }

        .social-link:hover {
            background: #667eea;
            color: white;
            transform: translateY(-2px);
        }

        /* Responsive Design */
        @media (max-width: 768px) {
            .nav-links {
                display: none;
            }

            .hero-container {
                grid-template-columns: 1fr;
                text-align: center;
            }

            .hero-title {
                font-size: 2.5rem;
            }

            .architecture-grid {
                grid-template-columns: 1fr;
            }

            .section-title {
                font-size: 2rem;
            }

            .cta-title {
                font-size: 2rem;
            }

            .hero-buttons,
            .cta-buttons {
                flex-direction: column;
                align-items: center;
            }
        }

        /* Smooth Scrolling */
        html {
            scroll-behavior: smooth;
        }

        /* Loading Animation */
        .loading-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 9999;
            transition: opacity 0.5s ease;
        }

        .loading-content {
            text-align: center;
            color: white;
        }

        .loading-spinner {
            width: 50px;
            height: 50px;
            border: 4px solid rgba(255, 255, 255, 0.3);
            border-top: 4px solid white;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin: 0 auto 1rem;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
<!--
    Enhanced Professional Landing Page
    Copyright (c) 2025 Ishara Lakshitha (@isharax9). All rights reserved.
-->

<!-- Loading Overlay -->
<div class="loading-overlay" id="loadingOverlay">
    <div class="loading-content">
        <div class="loading-spinner"></div>
        <h2>Loading Auction System...</h2>
        <p>Initializing enterprise components</p>
    </div>
</div>

<!-- Animated Background -->
<div class="bg-animation"></div>

<!-- Navigation Header -->
<nav class="nav-header" id="navbar">
    <div class="nav-content">
        <a href="#" class="nav-logo">
            <i class="fas fa-gavel"></i>
            <span>Nawwa Auction Solution</span>
        </a>
        <div class="nav-links">
            <a href="#features" class="nav-link">Features</a>
            <a href="#architecture" class="nav-link">Architecture</a>
            <a href="#technology" class="nav-link">Technology</a>
            <a href="https://isharax9.me/" class="nav-link">Developer Portfolio</a>
        </div>
        <a href="/AuctionSystem/auction/" class="nav-cta">
            <i class="fas fa-rocket"></i> Launch System
        </a>
    </div>
</nav>

<!-- Hero Section -->
<section class="hero-section">
    <div class="hero-container">
        <div class="hero-content">
            <div class="hero-badge">
                <i class="fas fa-star"></i>
                Enterprise-Grade Platform
            </div>
            <h1 class="hero-title">
                Customized Online<br>
                Auction System
            </h1>
            <p class="hero-subtitle">
                Professional enterprise auction platform built with Jakarta EE 10, featuring
                real-time bidding, WebSocket integration, and advanced EJB + JMS architecture
                for high-performance concurrent operations.
            </p>
            <div class="hero-buttons">
                <a href="/AuctionSystem/auction/" class="btn-primary">
                    <i class="fas fa-rocket"></i>
                    Enter Auction System
                </a>
                <a href="/AuctionSystem/real-time-notifications.html" class="btn-secondary">
                    <i class="fas fa-bell"></i>
                    Live Notifications
                </a>
            </div>
        </div>

        <div class="hero-preview">
            <div class="live-indicator">
                <div class="live-dot"></div>
                LIVE
            </div>
            <div class="preview-header">
                <div class="preview-title">
                    <i class="fas fa-chart-line"></i>
                    System Dashboard Preview
                </div>
            </div>
            <div class="preview-stats">
                <div class="stat-card">
                    <div class="stat-value" id="activeAuctions">12</div>
                    <div class="stat-label">Active Auctions</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value" id="onlineUsers">47</div>
                    <div class="stat-label">Online Users</div>
                </div>
            </div>
            <div class="preview-stats">
                <div class="stat-card">
                    <div class="stat-value">$89.2K</div>
                    <div class="stat-label">Total Volume</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">99.9%</div>
                    <div class="stat-label">Uptime</div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Features Section -->
<section class="features-section" id="features">
    <div class="section-container">
        <div class="section-header">
            <div class="section-badge">Core Features</div>
            <h2 class="section-title">Comprehensive Auction Platform</h2>
            <p class="section-subtitle">
                Built with enterprise-grade technologies to handle high-concurrency bidding
                with real-time updates and robust system reliability.
            </p>
        </div>
        <div class="features-grid">
            <div class="feature-card">
                <div class="feature-icon">
                    <i class="fas fa-bolt"></i>
                </div>
                <h3 class="feature-title">Real-time Bidding</h3>
                <p class="feature-desc">
                    Instant bid updates powered by WebSocket technology and JMS messaging.
                    Sub-200ms notification delivery ensures all participants see live auction activity.
                </p>
                <span class="feature-tech">WebSocket + JMS</span>
            </div>

            <div class="feature-card">
                <div class="feature-icon">
                    <i class="fas fa-users"></i>
                </div>
                <h3 class="feature-title">Concurrent Access</h3>
                <p class="feature-desc">
                    Thread-safe bid processing with EJB container management. Supports 100+
                    simultaneous bidders per auction with guaranteed data consistency.
                </p>
                <span class="feature-tech">EJB Concurrency</span>
            </div>

            <div class="feature-card">
                <div class="feature-icon">
                    <i class="fas fa-clock"></i>
                </div>
                <h3 class="feature-title">Flexible Duration</h3>
                <p class="feature-desc">
                    Advanced duration control with hours and minutes selection.
                    Range from 1 minute to 7 days with automatic expiration management.
                </p>
                <span class="feature-tech">Smart Scheduling</span>
            </div>

            <div class="feature-card">
                <div class="feature-icon">
                    <i class="fas fa-history"></i>
                </div>
                <h3 class="feature-title">Auction History</h3>
                <p class="feature-desc">
                    Complete auction lifecycle tracking with winner information,
                    final prices, and detailed bid history for analytics and reporting.
                </p>
                <span class="feature-tech">Data Management</span>
            </div>

            <div class="feature-card">
                <div class="feature-icon">
                    <i class="fas fa-shield-alt"></i>
                </div>
                <h3 class="feature-title">Security & Sessions</h3>
                <p class="feature-desc">
                    Enterprise-grade session management with IP validation,
                    token-based authentication, and automatic security cleanup.
                </p>
                <span class="feature-tech">Security Framework</span>
            </div>

            <div class="feature-card">
                <div class="feature-icon">
                    <i class="fas fa-chart-bar"></i>
                </div>
                <h3 class="feature-title">System Monitoring</h3>
                <p class="feature-desc">
                    Professional system status dashboard with real-time metrics,
                    service health monitoring, and performance analytics.
                </p>
                <span class="feature-tech">Monitoring Tools</span>
            </div>
        </div>
    </div>
</section>

<!-- Architecture Section -->
<section class="architecture-section" id="architecture">
    <div class="section-container">
        <div class="architecture-grid">
            <div>
                <div class="section-badge">System Architecture</div>
                <h2 class="section-title">Enterprise Architecture</h2>
                <p class="section-subtitle">
                    Three-tier distributed architecture leveraging Jakarta EE 10
                    specifications for maximum scalability and reliability.
                </p>

                <div class="architecture-diagram">
                    <div class="layer">
                        <div class="layer-title">
                            <i class="fas fa-desktop"></i>
                            Presentation Layer
                        </div>
                        <div class="layer-desc">
                            WebSocket endpoints, Servlets, responsive UI with real-time updates
                        </div>
                    </div>
                    <div class="layer">
                        <div class="layer-title">
                            <i class="fas fa-cogs"></i>
                            Business Layer
                        </div>
                        <div class="layer-desc">
                            EJB container with Stateless, Stateful, Singleton beans and JMS integration
                        </div>
                    </div>
                    <div class="layer">
                        <div class="layer-title">
                            <i class="fas fa-database"></i>
                            Data Layer
                        </div>
                        <div class="layer-desc">
                            In-memory concurrent data structures with session persistence
                        </div>
                    </div>
                </div>
            </div>

            <div>
                <h3 style="margin-bottom: 2rem; color: #333;">Key Architectural Benefits</h3>
                <div style="display: grid; gap: 1.5rem;">
                    <div style="background: white; padding: 1.5rem; border-radius: 15px; border-left: 5px solid #667eea;">
                        <h4 style="color: #333; margin-bottom: 0.5rem;">
                            <i class="fas fa-expand-arrows-alt"></i> Horizontal Scalability
                        </h4>
                        <p style="color: #666; margin: 0;">
                            Stateless design patterns enable seamless horizontal scaling
                            across multiple server instances.
                        </p>
                    </div>
                    <div style="background: white; padding: 1.5rem; border-radius: 15px; border-left: 5px solid #28a745;">
                        <h4 style="color: #333; margin-bottom: 0.5rem;">
                            <i class="fas fa-shield-alt"></i> Fault Tolerance
                        </h4>
                        <p style="color: #666; margin: 0;">
                            EJB container provides automatic failover and transaction
                            management for system reliability.
                        </p>
                    </div>
                    <div style="background: white; padding: 1.5rem; border-radius: 15px; border-left: 5px solid #fd7e14;">
                        <h4 style="color: #333; margin-bottom: 0.5rem;">
                            <i class="fas fa-rocket"></i> High Performance
                        </h4>
                        <p style="color: #666; margin: 0;">
                            Connection pooling and optimized resource management
                            ensure consistent high performance under load.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Stats Section -->
<section class="stats-section">
    <div class="section-container">
        <div class="stats-grid">
            <div class="stat-item">
                <div class="stat-number">99.9%</div>
                <div class="stat-text">System Uptime</div>
            </div>
            <div class="stat-item">
                <div class="stat-number">&lt;200ms</div>
                <div class="stat-text">Response Time</div>
            </div>
            <div class="stat-item">
                <div class="stat-number">100+</div>
                <div class="stat-text">Concurrent Users</div>
            </div>
            <div class="stat-item">
                <div class="stat-number">24/7</div>
                <div class="stat-text">Availability</div>
            </div>
        </div>
    </div>
</section>

<!-- Technology Stack Section -->
<section class="tech-section" id="technology">
    <div class="section-container">
        <div class="section-header">
            <div class="section-badge">Technology Stack</div>
            <h2 class="section-title">Enterprise Technologies</h2>
            <p class="section-subtitle">
                Built with cutting-edge Jakarta EE 10 technologies and industry best practices
                for enterprise-grade performance and reliability.
            </p>
        </div>
        <div class="tech-grid">
            <div class="tech-category">
                <div class="tech-category-icon">
                    <i class="fas fa-cube"></i>
                </div>
                <h3 class="tech-category-title">Core Framework</h3>
                <div class="tech-items">
                    <span class="tech-item">Jakarta EE 10</span>
                    <span class="tech-item">EJB 4.0</span>
                    <span class="tech-item">JMS 3.1</span>
                    <span class="tech-item">Servlets 6.0</span>
                    <span class="tech-item">WebSocket</span>
                </div>
            </div>

            <div class="tech-category">
                <div class="tech-category-icon">
                    <i class="fas fa-server"></i>
                </div>
                <h3 class="tech-category-title">Application Server</h3>
                <div class="tech-items">
                    <span class="tech-item">GlassFish 7.x</span>
                    <span class="tech-item">JDK 11+</span>
                    <span class="tech-item">Maven 3.x</span>
                    <span class="tech-item">Connection Pooling</span>
                </div>
            </div>

            <div class="tech-category">
                <div class="tech-category-icon">
                    <i class="fas fa-paint-brush"></i>
                </div>
                <h3 class="tech-category-title">Frontend</h3>
                <div class="tech-items">
                    <span class="tech-item">Responsive CSS</span>
                    <span class="tech-item">JavaScript ES6+</span>
                    <span class="tech-item">WebSocket Client</span>
                    <span class="tech-item">Font Awesome</span>
                </div>
            </div>

            <div class="tech-category">
                <div class="tech-category-icon">
                    <i class="fas fa-shield-alt"></i>
                </div>
                <h3 class="tech-category-title">Security & Quality</h3>
                <div class="tech-items">
                    <span class="tech-item">Session Management</span>
                    <span class="tech-item">Security Filters</span>
                    <span class="tech-item">Input Validation</span>
                    <span class="tech-item">XSS Protection</span>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Call to Action Section -->
<section class="cta-section">
    <div class="cta-content">
        <h2 class="cta-title">Ready to Experience Professional Auctions?</h2>
        <p class="cta-subtitle">
            Join the next generation of online auction platforms with enterprise-grade
            reliability and real-time performance.
        </p>
        <div class="cta-buttons">
            <a href="/AuctionSystem/auction/" class="btn-primary">
                <i class="fas fa-rocket"></i>
                Launch Auction System
            </a>
            <a href="/AuctionSystem/auction/status" class="btn-secondary">
                <i class="fas fa-heartbeat"></i>
                View System Status
            </a>
        </div>
    </div>
</section>

<!-- Footer -->
<footer class="footer">
    <div class="footer-grid">
        <div class="footer-section">
            <h4>Enhanced Auction System</h4>
            <p>Enterprise-grade online auction platform built with Jakarta EE 10,
                featuring real-time bidding and advanced EJB + JMS architecture.</p>
        </div>

        <div class="footer-section">
            <h4>Platform Features</h4>
            <a href="/AuctionSystem/auction/">Real-time Bidding</a><br>
            <a href="/AuctionSystem/real-time-notifications.html">Live Notifications</a><br>
            <a href="/AuctionSystem/auction/status">System Monitoring</a><br>
            <a href="#architecture">Enterprise Architecture</a>
        </div>

        <div class="footer-section">
            <h4>Technology</h4>
            <a href="#technology">Jakarta EE 10</a><br>
            <a href="#technology">EJB + JMS</a><br>
            <a href="#technology">WebSocket Integration</a><br>
            <a href="#technology">GlassFish Server</a>
        </div>

        <div class="footer-section">
            <h4>System Information</h4>
            <p><strong>Version:</strong> 2.0.0-Enhanced</p>
            <p><strong>Server Time:</strong> <%= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) %></p>
            <p><strong>Status:</strong> <span style="color: #28a745;">● All Systems Operational</span></p>
        </div>
    </div>

    <div class="footer-bottom">
        <div class="social-links">
            <a href="https://github.com/isharax9" class="social-link" target="_blank">
                <i class="fab fa-github"></i>
            </a>
            <a href="https://linkedin.com/in/isharax9" class="social-link" target="_blank">
                <i class="fab fa-linkedin"></i>
            </a>
            <a href="https://twitter.com/isharax9" class="social-link" target="_blank">
                <i class="fab fa-twitter"></i>
            </a>
        </div>
        <p>&copy; 2025 <strong>Ishara Lakshitha (@isharax9)</strong>. All rights reserved.</p>
        <p>Business Component Development 1 - Final Year Project | Enterprise Java Technologies</p>
    </div>
</footer>

<script>
    // Enhanced Landing Page Interactions
    // Copyright (c) 2025 Ishara Lakshitha (@isharax9). All rights reserved.

    // Loading animation
    window.addEventListener('load', function() {
        setTimeout(() => {
            document.getElementById('loadingOverlay').style.opacity = '0';
            setTimeout(() => {
                document.getElementById('loadingOverlay').style.display = 'none';
            }, 500);
        }, 1500);
    });

    // Navbar scroll effect
    window.addEventListener('scroll', function() {
        const navbar = document.getElementById('navbar');
        if (window.scrollY > 50) {
            navbar.style.background = 'rgba(255, 255, 255, 0.98)';
            navbar.style.boxShadow = '0 2px 20px rgba(0,0,0,0.1)';
        } else {
            navbar.style.background = 'rgba(255, 255, 255, 0.95)';
            navbar.style.boxShadow = 'none';
        }
    });

    // Animated counters for stats
    function animateCounter(element, target, duration = 2000) {
        let start = 0;
        const increment = target / (duration / 16);

        function updateCounter() {
            start += increment;
            if (start < target) {
                element.textContent = Math.floor(start);
                requestAnimationFrame(updateCounter);
            } else {
                element.textContent = target;
            }
        }
        updateCounter();
    }

    // Simulate live data updates
    function updateLiveStats() {
        const activeAuctions = document.getElementById('activeAuctions');
        const onlineUsers = document.getElementById('onlineUsers');

        if (activeAuctions && onlineUsers) {
            // Simulate fluctuating numbers
            const newAuctions = Math.floor(Math.random() * 20) + 10;
            const newUsers = Math.floor(Math.random() * 50) + 30;

            animateCounter(activeAuctions, newAuctions, 1000);
            animateCounter(onlineUsers, newUsers, 1000);
        }
    }

    // Update stats every 10 seconds
    setInterval(updateLiveStats, 10000);

    // Smooth scrolling for navigation links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // Add entrance animations on scroll
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);

    // Apply animation observer to cards
    document.addEventListener('DOMContentLoaded', function() {
        const animateElements = document.querySelectorAll('.feature-card, .tech-category, .stat-item');
        animateElements.forEach(el => {
            el.style.opacity = '0';
            el.style.transform = 'translateY(30px)';
            el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            observer.observe(el);
        });
    });

    // Console welcome message
    console.log(`
        ╔════════════════════════════════════════════════════════════════════╗
        ║                                                                    ║
        ║           🏺 Enhanced Online Auction System 🏺                    ║
        ║                                                                    ║
        ║  Enterprise-grade auction platform built with Jakarta EE 10       ║
        ║  Featuring EJB + JMS + WebSocket integration                      ║
        ║                                                                    ║
        ║  Developer: Ishara Lakshitha (@isharax9)                         ║
        ║  Version: 2.0.0-Enhanced                                          ║
        ║  © 2025 All rights reserved                                        ║
        ║                                                                    ║
        ╚════════════════════════════════════════════════════════════════════╝
        `);
</script>
</body>
</html>