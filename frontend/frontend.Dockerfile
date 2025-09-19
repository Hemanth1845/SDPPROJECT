# Stage 1: Build the Vite React app
FROM node:20 AS build

WORKDIR /app

# Copy package files and install dependencies
# Using npm ci is faster and more reliable for CI/CD environments
COPY package*.json ./
RUN npm ci

# Copy the rest of the frontend code
COPY . .

# Build the application for production
# This creates an optimized 'dist' folder
RUN npm run build

# Stage 2: Serve the built app with Nginx
FROM nginx:alpine

# Copy the static build files from the 'build' stage to Nginx's web root
COPY --from=build /app/dist /usr/share/nginx/html

# Tell Docker that the container listens on port 80
EXPOSE 80

# The default command for the nginx image is to start the server.
# This line ensures it runs correctly.
CMD ["nginx", "-g", "daemon off;"]
