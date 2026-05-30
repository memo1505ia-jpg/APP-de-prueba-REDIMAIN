const express = require('express');
const cors = require('cors');
const nodemailer = require('nodemailer');
const path = require('path');
require('dotenv').config({ path: path.resolve(__dirname, '../.env') });

const app = express();
app.use(cors());
app.use(express.json());

// In-memory store for OTP codes mapped to emails
const otpStore = new Map();

// In-memory log of recent OTP traffic for dashboard visibility
const recentOTPs = [];

// Helper to check if SMTP config is available
const getTransporter = () => {
  const host = process.env.SMTP_HOST;
  const port = parseInt(process.env.SMTP_PORT || '587');
  const user = process.env.SMTP_USER;
  const pass = process.env.SMTP_PASS;

  if (!host || !user || !pass) {
    throw new Error('Falta configuración de SMTP en las variables de entorno (SMTP_HOST, SMTP_USER, SMTP_PASS).');
  }

  return nodemailer.createTransport({
    host,
    port,
    secure: port === 465, // true for 465, false for other ports (587, 25)
    auth: {
      user,
      pass,
    },
  });
};

// Root Dashboard Endpoint
app.get('/', (req, res) => {
  const isSmtpConfigured = process.env.SMTP_HOST && process.env.SMTP_USER && process.env.SMTP_PASS;
  
  const otpRows = recentOTPs.length > 0 
    ? recentOTPs.map(item => `
        <tr>
          <td style="padding: 10px 4px; border-bottom: 1px solid rgba(255,255,255,0.05);">${item.email}</td>
          <td class="pin-code mono" style="padding: 10px 4px; border-bottom: 1px solid rgba(255,255,255,0.05); color: #10B981; font-weight: bold; font-family: 'Share Tech Mono', monospace; letter-spacing: 1px;">${item.otp}</td>
          <td class="mono" style="padding: 10px 4px; border-bottom: 1px solid rgba(255,255,255,0.05); color: #94A3B8; font-family: 'Share Tech Mono', monospace;">${item.timestamp}</td>
        </tr>
      `).join('')
    : `<tr><td colspan="3" style="text-align: center; color: #64748B; padding: 20px;">No hay tráfico de códigos registrado aún.</td></tr>`;

  const html = `
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>REDIMAIN - Panel Táctico de Control</title>
  <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;800&family=Share+Tech+Mono&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg-gradient: linear-gradient(135deg, #0F172A 0%, #020617 100%);
      --card-bg: #0B132B;
      --border-color: #1E293B;
      --accent-gold: #F59E0B;
      --accent-blue: #2563EB;
      --accent-emerald: #10B981;
      --text-main: #FFFFFF;
      --text-muted: #94A3B8;
    }
    body {
      margin: 0;
      padding: 0;
      font-family: 'Outfit', sans-serif;
      background: var(--bg-gradient);
      color: var(--text-main);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      box-sizing: border-box;
    }
    .radar {
      position: absolute;
      top: 5%;
      right: 5%;
      width: 150px;
      height: 150px;
      border: 1px solid rgba(245, 158, 11, 0.08);
      border-radius: 50%;
      pointer-events: none;
    }
    .container {
      max-width: 800px;
      width: 90%;
      z-index: 10;
      padding: 20px 0;
    }
    header {
      text-align: center;
      margin-bottom: 30px;
    }
    .badge {
      background: rgba(16, 185, 129, 0.1);
      border: 1px solid var(--accent-emerald);
      color: var(--accent-emerald);
      padding: 4px 12px;
      border-radius: 50px;
      font-size: 0.75rem;
      font-weight: 600;
      letter-spacing: 1px;
      text-transform: uppercase;
      display: inline-block;
      margin-bottom: 12px;
      box-shadow: 0 0 15px rgba(16, 185, 129, 0.2);
    }
    h1 {
      font-size: 2.2rem;
      font-weight: 800;
      margin: 0;
      letter-spacing: 2px;
      background: linear-gradient(to right, #FFFFFF, var(--text-muted));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    .subtitle {
      color: var(--text-muted);
      font-size: 0.9rem;
      margin-top: 5px;
    }
    .grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
    }
    @media (max-width: 768px) {
      .grid {
        grid-template-columns: 1fr;
      }
    }
    .card {
      background: var(--card-bg);
      border: 1px solid var(--border-color);
      border-radius: 12px;
      padding: 24px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
      position: relative;
    }
    .card::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 4px;
      height: 100%;
      background: var(--accent-blue);
      border-radius: 12px 0 0 12px;
    }
    .card.gold::before {
      background: var(--accent-gold);
    }
    .card h2 {
      font-size: 1.1rem;
      margin-top: 0;
      margin-bottom: 16px;
      font-weight: 600;
    }
    .status-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 12px;
      font-size: 0.9rem;
      border-bottom: 1px dashed rgba(255,255,255,0.05);
      padding-bottom: 8px;
    }
    .status-item:last-child {
      border: none;
      margin-bottom: 0;
      padding-bottom: 0;
    }
    .status-label {
      color: var(--text-muted);
    }
    .status-value {
      font-weight: 600;
    }
    .status-value.online {
      color: var(--accent-emerald);
    }
    .status-value.offline {
      color: #EF4444;
    }
    .table-container {
      max-height: 180px;
      overflow-y: auto;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.8rem;
    }
    th {
      padding: 8px 4px;
      text-align: left;
      border-bottom: 1px solid rgba(255,255,255,0.05);
      color: var(--text-muted);
      font-weight: 600;
    }
    .footer {
      text-align: center;
      margin-top: 40px;
      font-size: 0.75rem;
      color: var(--text-muted);
      letter-spacing: 1px;
    }
  </style>
</head>
<body>
  <div class="radar"></div>
  <div class="container">
    <header>
      <span class="badge" style="background: ${isSmtpConfigured ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)'}; border-color: ${isSmtpConfigured ? '#10B981' : '#EF4444'}; color: ${isSmtpConfigured ? '#10B981' : '#EF4444'}">${isSmtpConfigured ? 'Servidor Activo' : 'Sin Configuración'}</span>
      <h1>REDIMAIN CONTROL</h1>
      <div class="subtitle">Panel de Control Táctico de Correos y Comunicaciones</div>
    </header>

    <div class="grid">
      <div class="card">
        <h2>Estado de Enlace SMTP</h2>
        <div class="status-item">
          <span class="status-label">Servidor SMTP</span>
          <span class="status-value">${process.env.SMTP_HOST || 'No definido'}</span>
        </div>
        <div class="status-item">
          <span class="status-label">Usuario</span>
          <span class="status-value">${process.env.SMTP_USER || 'No definido'}</span>
        </div>
        <div class="status-item">
          <span class="status-label">Estado de Conexión</span>
          <span class="status-value ${isSmtpConfigured ? 'online' : 'offline'}">${isSmtpConfigured ? '● En Línea' : '● Desconectado'}</span>
        </div>
        <div class="status-item">
          <span class="status-label">Puerto</span>
          <span class="status-value">${process.env.SMTP_PORT || '587'}</span>
        </div>
      </div>

      <div class="card gold">
        <h2>Tránsito de Códigos OTP</h2>
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>Destinatario</th>
                <th>Código PIN</th>
                <th>Hora</th>
              </tr>
            </thead>
            <tbody>
              ${otpRows}
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="footer">
      SISTEMA TÁCTICO DE LA REGION DE DEFENSA INTEGRAL MARÍTIMA E INSULAR — 2026
    </div>
  </div>
</body>
</html>
  `;
  res.send(html);
});

// Endpoint to send OTP
app.post('/api/send-otp', async (req, res) => {
  const { email } = req.body;
  if (!email) {
    return res.status(400).json({ success: false, message: 'El correo electrónico es requerido.' });
  }

  try {
    // Generate a 6-digit PIN
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    
    // Store OTP in memory with 5 minutes expiration
    otpStore.set(email.toLowerCase(), {
      otp,
      expiresAt: Date.now() + 5 * 60 * 1000 // 5 minutes
    });

    const transporter = getTransporter();
    
    const mailOptions = {
      from: `"REDIMAIN - Sistema Táctico" <${process.env.SMTP_SENDER || process.env.SMTP_USER}>`,
      to: email,
      subject: 'Código de Verificación - REDIMAIN',
      text: `Su código de verificación para ingresar al sistema de la REDIMAIN es: ${otp}. Este código es válido por 5 minutos.`,
      html: `
        <div style="font-family: Arial, sans-serif; background-color: #0F172A; color: #FFFFFF; padding: 24px; border-radius: 8px; max-width: 600px; margin: 0 auto; border: 1.5px solid #2563EB;">
          <h2 style="color: #F59E0B; text-align: center; border-bottom: 2px solid #1E293B; padding-bottom: 12px;">REDIMAIN - SISTEMA TÁCTICO</h2>
          <p style="font-size: 16px; color: #94A3B8;">Usted ha solicitado un código de acceso para la Región de Defensa Integral Marítima e Insular (REDIMAIN).</p>
          <div style="background-color: #0B132B; border: 1px solid #1E293B; border-radius: 6px; padding: 16px; text-align: center; margin: 24px 0;">
            <span style="font-size: 32px; font-weight: bold; letter-spacing: 4px; color: #10B981;">${otp}</span>
          </div>
          <p style="font-size: 14px; color: #64748B; text-align: center;">Este código expira en 5 minutos. Si usted no solicitó este código, por favor ignore este mensaje.</p>
        </div>
      `
    };

    await transporter.sendMail(mailOptions);
    console.log(`[SMTP] PIN ${otp} enviado exitosamente a ${email}`);
    
    // Log in memory for the dashboard
    recentOTPs.unshift({
      email,
      otp,
      timestamp: new Date().toLocaleTimeString('es-VE', { hour12: false })
    });
    if (recentOTPs.length > 8) {
      recentOTPs.pop();
    }

    return res.json({ success: true, message: 'Código de verificación enviado.' });
  } catch (error) {
    console.error('Error enviando correo SMTP:', error);
    return res.status(500).json({ success: false, message: `Error al enviar correo: ${error.message}` });
  }
});

// Endpoint to verify OTP
app.post('/api/verify-otp', (req, res) => {
  const { email, pin } = req.body;
  if (!email || !pin) {
    return res.status(400).json({ success: false, message: 'Correo y PIN son requeridos.' });
  }

  const storedData = otpStore.get(email.toLowerCase());
  if (!storedData) {
    return res.status(400).json({ success: false, message: 'No hay un código pendiente para este correo. Solicite uno nuevo.' });
  }

  if (Date.now() > storedData.expiresAt) {
    otpStore.delete(email.toLowerCase());
    return res.status(400).json({ success: false, message: 'El código ha expirado. Solicite uno nuevo.' });
  }

  if (storedData.otp === pin.trim()) {
    otpStore.delete(email.toLowerCase()); // Consume PIN
    return res.json({ success: true, message: 'PIN verificado con éxito.' });
  } else {
    return res.status(400).json({ success: false, message: 'Código inválido. Intente nuevamente.' });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Servidor de producción de REDIMAIN corriendo en http://localhost:${PORT}`);
});

module.exports = app;
