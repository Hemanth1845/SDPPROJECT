# Stage 1: Build Vite app
FROM node:20 AS build

WORKDIR /app

# Install latest npm
RUN npm install -g npm@latest

# Copy dependency files first (for caching)
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy project files
COPY . .

# Build Vite app -> dist/
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Copy build output from previous stage
COPY --from=build /app/dist /usr/share/nginx/html

# Expose frontend port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
