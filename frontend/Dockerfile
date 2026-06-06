ARG DOCKER_REGISTRY=docker.m.daocloud.io/

FROM ${DOCKER_REGISTRY}library/node:18-alpine AS builder

ARG NPM_REGISTRY=https://registry.npmmirror.com

WORKDIR /app

RUN npm config set registry ${NPM_REGISTRY}

COPY package.json package-lock.json* ./

RUN if [ -f package-lock.json ]; then npm ci; else npm install; fi

COPY . .

RUN npm run build

FROM ${DOCKER_REGISTRY}library/nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
