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
        <div style="font-family: Arial, sans-serif; background-color: #0F172A; color: #FFFFFF; padding: 24px; border-radius: 8px; max-width: 600px; margin: 0 auto; border: 1.5.dp solid #2563EB;">
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
