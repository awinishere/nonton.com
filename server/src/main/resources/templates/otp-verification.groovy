package templates

html {
    head {
        meta('http-equiv': 'Content-Type', content: 'text/html; charset=UTF-8')
        meta(name: 'viewport', content: 'width=device-width, initial-scale=1.0')
        style('''
            @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap');

            body {
                font-family: 'Plus Jakarta Sans', -apple-system, 'Segoe UI', Roboto, Arial, sans-serif;
                background-color: #f2f4f7;
                padding: 40px 20px;
                margin: 0;
                color: #1a1a1a;
            }
            .container {
                max-width: 480px;
                margin: 0 auto;
            }
            .card {
                background: #ffffff;
                border-radius: 16px;
                padding: 40px 32px;
                box-shadow: 0 1px 3px rgba(0,0,0,0.06);
            }
            .logo {
                font-size: 20px;
                font-weight: 700;
                color: #111827;
                margin-bottom: 24px;
            }
            h2 {
                font-size: 20px;
                font-weight: 600;
                margin: 0 0 8px;
                color: #111827;
            }
            .subtitle {
                font-size: 14px;
                color: #6b7280;
                margin: 0 0 28px;
                line-height: 1.5;
            }
            .otp-box {
                background: #f8f9fb;
                border: 1px solid #e5e7eb;
                border-radius: 12px;
                padding: 20px;
                text-align: center;
                margin-bottom: 24px;
            }
            .otp {
                font-size: 36px;
                font-weight: 700;
                color: #2563eb;
                letter-spacing: 8px;
            }
            .expiry {
                font-size: 13px;
                color: #9ca3af;
                margin-top: 8px;
            }
            .warning {
                font-size: 13px;
                color: #6b7280;
                line-height: 1.6;
                border-top: 1px solid #f0f0f0;
                padding-top: 20px;
            }
            .footer {
                text-align: center;
                font-size: 12px;
                color: #9ca3af;
                margin-top: 24px;
            }
        ''')
    }
    body {
        div(class: 'container') {
            div(class: 'card') {
                div(class: 'logo', 'NONTON.COM')
                h2("Hi ${userName},")
                p(class: 'subtitle', 'Use the verification code below to complete your action.')
                div(class: 'otp-box') {
                    div(class: 'otp', otpCode)
                    div(class: 'expiry', 'Expires in 5 minutes')
                }
                p(class: 'warning', 'For your security, never share this code with anyone — including our support team. If you didn\'t request this code, you can safely ignore this email.')
            }
            div(class: 'footer', '© 2026 Nonton.com. All rights reserved.')
        }
    }
}