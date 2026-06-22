# Simple dev-style container: install deps and run the Vite dev server.
# (For production you'd build static files and serve them via nginx; kept simple
#  here so the learning setup is one command.)
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 5173
# --host makes the dev server reachable from outside the container.
CMD ["npm", "run", "dev"]
