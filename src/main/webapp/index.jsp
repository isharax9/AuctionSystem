<%--
  Created by IntelliJ IDEA.
  User: ishara
  Date: 2025-06-03
  Time: 1:07 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Online Auction System - Welcome</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .container {
            background: white;
            padding: 2rem;
            border-radius: 15px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            text-align: center;
            max-width: 600px;
            width: 90%;
        }

        .logo {
            font-size: 3rem;
            margin-bottom: 1rem;
        }

        .title {
            color: #333;
            font-size: 2.5rem;
            margin-bottom: 1rem;
            font-weight: 700;
        }

        .subtitle {
            color: #666;
            font-size: 1.2rem;
            margin-bottom: 2rem;
            line-height: 1.6;
        }

        .features {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 1rem;
            margin: 2rem 0;
        }

        .feature {
            background: #f8f9fa;
            padding: 1rem;
            border-radius: 10px;
            border-left: 4px solid #667eea;
        }

        .feature-icon {
            font-size: 2rem;
            margin-bottom: 0.5rem;
        }

        .feature-title {
            font-weight: 600;
            color: #333;
            margin-bottom: 0.5rem;
        }

        .feature-desc {
            color: #666;
            font-size: 0.9rem;
        }

        .btn {
            display: inline-block;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 1rem 2rem;
            text-decoration: none;
            border-radius: 50px;
            font-weight: 600;
            font-size: 1.1rem;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            margin: 1rem 0.5rem;
        }

        .btn:hover {
            transform: translateY(-3px);
            box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
        }

        .tech-stack {
            background: #f8f9fa;
            padding: 1.5rem;
            border-radius: 10px;
            margin: 2rem 0;
        }

        .tech-title {
            color: #333;
            font-weight: 600;
            margin-bottom: 1rem;
        }

        .tech-list {
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            gap: 0.5rem;
        }

        .tech-item {
            background: white;
            padding: 0.5rem 1rem;
            border-radius: 20px;
            font-size: 0.9rem;
            color: #666;
            border: 1px solid #e9ecef;
        }

        .footer {
            margin-top: 2rem;
            padding-top: 1rem;
            border-top: 1px solid #e9ecef;
            color: #666;
            font-size: 0.9rem;
        }

        .status {
            background: #d4edda;
            color: #155724;
            padding: 1rem;
            border-radius: 10px;
            margin: 1rem 0;
            border-left: 4px solid #28a745;
        }

        @media (max-width: 768px) {
            .title {
                font-size: 2rem;
            }

            .features {
                grid-template-columns: 1fr;
            }

            .btn {
                display: block;
                margin: 0.5rem 0;
            }
        }
    </style>
</head>
<body>
<div class="container">
    <div class="logo">🏺</div>
    <h1 class="title">Online Auction System</h1>
    <p class="subtitle">
        Enterprise-grade distributed auction platform built with
        <strong>Enterprise JavaBeans (EJB)</strong> and
        <strong>Java Message Service (JMS)</strong>
    </p>

    <div class="status">
        <strong>✅ System Status:</strong> All services operational<br>
        <strong>📊 Server Time:</strong> <%= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) %>
    </div>

    <div class="features">
        <div class="feature">
            <div class="feature-icon">⚡</div>
            <div class="feature-title">Real-time Bidding</div>
            <div class="feature-desc">Live auction updates with JMS messaging</div>
        </div>

        <div class="feature">
            <div class="feature-icon">🔄</div>
            <div class="feature-title">Concurrent Access</div>
            <div class="feature-desc">Thread-safe bid processing with EJB</div>
        </div>

        <div class="feature">
            <div class="feature-icon">🏗️</div>
            <div class="feature-title">Scalable Architecture</div>
            <div class="feature-desc">Distributed system design patterns</div>
        </div>

        <div class="feature">
            <div class="feature-icon">📱</div>
            <div class="feature-title">Web Interface</div>
            <div class="feature-desc">Responsive auction management</div>
        </div>
    </div>

    <div>
        <a href="/AuctionSystem/auction/" class="btn">🚀 Enter Auction System</a>
        <a href="/AuctionSystem/auction/status" class="btn">📊 System Status</a>
    </div>

    <div class="tech-stack">
        <div class="tech-title">🛠️ Technology Stack</div>
        <div class="tech-list">
            <span class="tech-item">Java EE 10</span>
            <span class="tech-item">EJB 4.0</span>
            <span class="tech-item">JMS 3.1</span>
            <span class="tech-item">Servlets 6.0</span>
            <span class="tech-item">GlassFish 7.x</span>
            <span class="tech-item">Maven 3.x</span>
            <span class="tech-item">JDK 11</span>
        </div>
    </div>

    <div class="footer">
        <strong>Business Component Development Assignment</strong><br>
        Student: isharax9 | University Final Year Project<br>
        <em>Demonstrating enterprise Java technologies for distributed systems</em>
    </div>
</div>
</body>
</html>
