# Usar imagen oficial de Node.js
FROM node:20-alpine

# Establecer directorio de trabajo
WORKDIR /app

# Copiar package.json y package-lock.json
COPY package*.json ./

# Instalar dependencias
RUN npm ci --only=production

# Copiar código de la aplicación
COPY src ./src

# Exponer puerto
EXPOSE 8082

# Variables de entorno con valores por defecto
ENV EMAIL_SERVICE_API_KEY=default-email-key
ENV GMAIL_USER=your-email@gmail.com
ENV GMAIL_APP_PASSWORD=your-app-password

# Comando para ejecutar la aplicación
CMD ["node", "src/index.js"]